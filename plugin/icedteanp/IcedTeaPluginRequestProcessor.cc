/* IcedTeaPluginRequestProcessor.cc

   Copyright (C) 2009, 2010  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

#include <typeinfo>

#include "IcedTeaScriptablePluginObject.h"
#include "IcedTeaNPPlugin.h"
#include "IcedTeaPluginRequestProcessor.h"

/*
 * This class processes requests made by Java. The requests include pointer
 * information, script execution and variable get/set
 */

// Initialize static members used by the queue processing framework
pthread_mutex_t message_queue_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t syn_write_mutex = PTHREAD_MUTEX_INITIALIZER;
std::vector< std::vector<std::string*>* >* message_queue = new std::vector< std::vector<std::string*>* >();

/**
 * PluginRequestProcessor constructor.
 *
 * Initializes various complex data structures used by the class.
 */

PluginRequestProcessor::PluginRequestProcessor()
{
    this->pendingRequests = new std::map<pthread_t, uintmax_t>();

    internal_req_ref_counter = 0;

    pthread_mutex_init(&message_queue_mutex, NULL);
    pthread_mutex_init(&syn_write_mutex, NULL);

    pthread_cond_init(&cond_message_available, NULL);
}

/**
 * PluginRequestProcessor destructor.
 *
 * Frees memory used by complex objects.
 */

PluginRequestProcessor::~PluginRequestProcessor()
{
    PLUGIN_DEBUG("PluginRequestProcessor::~PluginRequestProcessor\n");

    if (pendingRequests)
        delete pendingRequests;

    pthread_mutex_destroy(&message_queue_mutex);
    pthread_mutex_destroy(&syn_write_mutex);

    pthread_cond_destroy(&cond_message_available);
}

/**
 * Processes plugin (C++ side) requests from the Java side, and internally.
 *
 * @param message The message request to process
 * @return boolean indicating whether the message is serviceable by this object
 */

bool
PluginRequestProcessor::newMessageOnBus(const char* message)
{
    PLUGIN_DEBUG("PluginRequestProcessor processing %s\n", message);

    std::string* type;
    std::string* command;
    int counter = 0;

    std::vector<std::string*>* message_parts = IcedTeaPluginUtilities::strSplit(message, " ");

    std::vector<std::string*>::iterator the_iterator;
    the_iterator = message_parts->begin();

    IcedTeaPluginUtilities::printStringPtrVector("PluginRequestProcessor::newMessageOnBus:", message_parts);

    type = message_parts->at(0);
    command = message_parts->at(4);

    if (!type->find("instance"))
    {
        if (!command->find("GetWindow"))
        {
            // Window can be queried from the main thread only. And this call
            // returns immediately, so we do it in the same thread.
            this->sendWindow(message_parts);
            return true;
        } else if (!command->find("Finalize"))
        {
            // Object can be finalized from the main thread only. And this
        	// call returns immediately, so we do it in the same thread.
            this->finalize(message_parts);
            return true;
        } else if (!command->find("GetMember") ||
                   !command->find("SetMember") ||
                   !command->find("ToString") ||
                   !command->find("Call") ||
                   !command->find("GetSlot") ||
                   !command->find("SetSlot") ||
                   !command->find("Eval") ||
                   !command->find("LoadURL"))
        {

            // Update queue synchronously
            pthread_mutex_lock(&message_queue_mutex);
            message_queue->push_back(message_parts);
            pthread_mutex_unlock(&message_queue_mutex);

            // Broadcast that a message is now available
            pthread_cond_broadcast(&cond_message_available);

            return true;
        }

    }

    IcedTeaPluginUtilities::freeStringPtrVector(message_parts);

    // If we got here, it means we couldn't process the message. Let the caller know.
    return false;
}

/**
 * Sends the window pointer to the Java side.
 *
 * @param message_parts The request message.
 */

void
PluginRequestProcessor::sendWindow(std::vector<std::string*>* message_parts)
{
    std::string* type;
    std::string* command;
    int reference;
    std::string response = std::string();
    std::string window_ptr_str = std::string();
    NPVariant* variant = new NPVariant();
    static NPObject* window_ptr;
    int id;

    type = message_parts->at(0);
    id = atoi(message_parts->at(1)->c_str());
    reference = atoi(message_parts->at(3)->c_str());
    command = message_parts->at(4);

    NPP instance;
    get_instance_from_id(id, instance);

    browser_functions.getvalue(instance, NPNVWindowNPObject, &window_ptr);
    PLUGIN_DEBUG("ID=%d, Instance=%p, WindowPTR = %p\n", id, instance, window_ptr);

    OBJECT_TO_NPVARIANT(window_ptr, *variant);
    browser_functions.retainobject(window_ptr);
    IcedTeaPluginUtilities::JSIDToString(variant, &window_ptr_str);

    // We need the context 0 for backwards compatibility with the Java side
    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &response);
    response += " JavaScriptGetWindow ";
    response += window_ptr_str;

    plugin_to_java_bus->post(response.c_str());

    // store the instance pointer for future reference
    IcedTeaPluginUtilities::storeInstanceID(variant, instance);
}

/**
 * Evaluates the given script
 *
 * @param message_parts The request message.
 */

void
PluginRequestProcessor::eval(std::vector<std::string*>* message_parts)
{
    JavaRequestProcessor request_processor = JavaRequestProcessor();
    JavaResultData* java_result;

    NPVariant* window_ptr;
    NPP instance;
    std::string script;
    NPVariant result;
    int reference;
    std::string response = std::string();
    std::string return_type = std::string();
    int id;

    reference = atoi(message_parts->at(3)->c_str());
    window_ptr = (NPVariant*) IcedTeaPluginUtilities::stringToJSID(message_parts->at(5));
    instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(window_ptr);

    // If instance is invalid, do not proceed further
    if (!instance)
    	return;

    java_result = request_processor.getString(*(message_parts->at(6)));
    CHECK_JAVA_RESULT(java_result);
    script.append(*(java_result->return_string));

    AsyncCallThreadData thread_data = AsyncCallThreadData();
    thread_data.result_ready = false;
    thread_data.parameters = std::vector<void*>();
    thread_data.result = std::string();

    thread_data.parameters.push_back(instance);
    thread_data.parameters.push_back(NPVARIANT_TO_OBJECT(*window_ptr));
    thread_data.parameters.push_back(&script);

    IcedTeaPluginUtilities::callAndWaitForResult(instance, &_eval, &thread_data);

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &response);
    response += " JavaScriptEval ";
    response += thread_data.result;

    plugin_to_java_bus->post(response.c_str());
}

/**
 * Calls the given javascript script
 *
 * @param message_parts The request message.
 */

void
PluginRequestProcessor::call(std::vector<std::string*>* message_parts)
{
    NPP instance;
    std::string* window_ptr_str;
    NPVariant* window_ptr;
    int reference;
    std::string window_function_name;
    std::vector<NPVariant> args = std::vector<NPVariant>();
    std::vector<std::string> arg_ids = std::vector<std::string>();
    int arg_count;
    std::string response = std::string();
    JavaRequestProcessor java_request = JavaRequestProcessor();
    JavaResultData* java_result;
    NPVariant* result_variant;
    std::string result_variant_jniid = std::string();
    NPVariant* args_array = NULL;
    AsyncCallThreadData thread_data = AsyncCallThreadData();

    reference = atoi(message_parts->at(3)->c_str());

    // window
    window_ptr_str = message_parts->at(5);
    window_ptr = (NPVariant*) IcedTeaPluginUtilities::stringToJSID(window_ptr_str);

    // instance
    instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(window_ptr);

    // If instance is invalid, do not proceed further
    if (!instance)
    	goto cleanup;

    // function name
    java_result = java_request.getString(*(message_parts->at(6)));
    CHECK_JAVA_RESULT(java_result);
    window_function_name.append(*(java_result->return_string));

    // arguments
    for (int i=7; i < message_parts->size(); i++)
    {
        arg_ids.push_back(*(message_parts->at(i)));
    }

    // determine arguments
    for (int i=0; i < arg_ids.size(); i++)
    {
        NPVariant* variant = new NPVariant();
        java_result = java_request.getValue(arg_ids[i]);
        CHECK_JAVA_RESULT(java_result);

        IcedTeaPluginUtilities::javaResultToNPVariant(instance, java_result->return_string, variant);

        args.push_back(*variant);
    }

    arg_count = args.size();
    args_array = (NPVariant*) malloc(sizeof(NPVariant)*args.size());
    for (int i=0; i < args.size(); i++)
        args_array[i] = args[i];

    thread_data.result_ready = false;
    thread_data.parameters = std::vector<void*>();
    thread_data.result = std::string();

    thread_data.parameters.push_back(instance);
    thread_data.parameters.push_back(NPVARIANT_TO_OBJECT(*window_ptr));
    thread_data.parameters.push_back(&window_function_name);
    thread_data.parameters.push_back(&arg_count);
    thread_data.parameters.push_back(args_array);

    IcedTeaPluginUtilities::callAndWaitForResult(instance, &_call, &thread_data);

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &response);
    response += " JavaScriptCall ";
    response += thread_data.result;

    plugin_to_java_bus->post(response.c_str());

    cleanup:
    free(args_array);
}

/**
 * Sends the string value of the requested variable
 *
 * @param message_parts The request message.
 */
void
PluginRequestProcessor::sendString(std::vector<std::string*>* message_parts)
{
    std::string variant_ptr;
    NPVariant* variant;
    JavaRequestProcessor java_request = JavaRequestProcessor();
    JavaResultData* java_result;
    int reference;
    std::string response = std::string();

    reference = atoi(message_parts->at(3)->c_str());
    variant_ptr = *(message_parts->at(5));

    variant = (NPVariant*) IcedTeaPluginUtilities::stringToJSID(variant_ptr);
    AsyncCallThreadData thread_data = AsyncCallThreadData();
    thread_data.result_ready = false;
    thread_data.parameters = std::vector<void*>();
    thread_data.result = std::string();

    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(variant);

    // If instance is invalid, do not proceed further
    if (!instance)
    	return;

    thread_data.parameters.push_back(instance);
    thread_data.parameters.push_back(variant);

    IcedTeaPluginUtilities::callAndWaitForResult(instance, &_getString, &thread_data);

    // We need the context 0 for backwards compatibility with the Java side
    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &response);
    response += " JavaScriptToString ";
    response += thread_data.result;

    plugin_to_java_bus->post(response.c_str());
}

/**
 * Sets variable to given value
 *
 * @param message_parts The request message.
 */

void
PluginRequestProcessor::setMember(std::vector<std::string*>* message_parts)
{
    std::string propertyNameID;
    std::string value = std::string();
    std::string response = std::string();
    int reference;

    NPP instance;
    NPVariant* member;
    std::string property_id = std::string();
    bool int_identifier;

    JavaRequestProcessor java_request = JavaRequestProcessor();
    JavaResultData* java_result;

    IcedTeaPluginUtilities::printStringPtrVector("PluginRequestProcessor::_setMember - ", message_parts);

    reference = atoi(message_parts->at(3)->c_str());

    member = (NPVariant*) (IcedTeaPluginUtilities::stringToJSID(*(message_parts->at(5))));
    propertyNameID = *(message_parts->at(6));

    if (*(message_parts->at(7)) == "literalreturn")
    {
        value.append(*(message_parts->at(7)));
        value.append(" ");
        value.append(*(message_parts->at(8)));
    } else
    {
        value.append(*(message_parts->at(7)));
    }

    instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(member);

    // If instance is invalid, do not proceed further
    if (!instance)
    	return;

    if (*(message_parts->at(4)) == "SetSlot")
    {
    	property_id.append(*(message_parts->at(6)));
    	int_identifier = true;
    } else
    {
        java_result = java_request.getString(propertyNameID);

        // the result we want is in result_string (assuming there was no error)
        if (java_result->error_occurred)
        {
	    printf("Unable to get member name for setMember. Error occurred: %s\n", java_result->error_msg->c_str());
            //goto cleanup;
        }

        property_id.append(*(java_result->return_string));
    	int_identifier = false;
    }

    AsyncCallThreadData thread_data = AsyncCallThreadData();
    thread_data.result_ready = false;
    thread_data.parameters = std::vector<void*>();
    thread_data.result = std::string();

    thread_data.parameters.push_back(instance);
    thread_data.parameters.push_back(NPVARIANT_TO_OBJECT(*member));
    thread_data.parameters.push_back(&property_id);
    thread_data.parameters.push_back(&value);
    thread_data.parameters.push_back(&int_identifier);

    IcedTeaPluginUtilities::callAndWaitForResult(instance, &_setMember, &thread_data);

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &response);
    response.append(" JavaScriptSetMember ");
    plugin_to_java_bus->post(response.c_str());
}

/**
 * Sends request member pointer to the Java side.
 *
 * This is a static function, called in another thread. Since certain data
 * can only be requested from the main thread in Mozilla, this function
 * does whatever it can seperately, and then makes an internal request that
 * causes _getMember to do the rest of the work.
 *
 * @param message_parts The request message
 */

void
PluginRequestProcessor::sendMember(std::vector<std::string*>* message_parts)
{
    // member initialization
    std::vector<std::string> args;
    JavaRequestProcessor java_request = JavaRequestProcessor();
    JavaResultData* java_result;
    NPVariant* parent_ptr;
    NPVariant* member_ptr;

    //int reference;
    std::string member_id = std::string();
    std::string response = std::string();
    std::string result_id = std::string();

    NPIdentifier member_identifier;

    int method_id;
    int instance_id;
    int reference;
    bool int_identifier;

    // debug printout of parent thread data
    IcedTeaPluginUtilities::printStringPtrVector("PluginRequestProcessor::getMember:", message_parts);

    reference = atoi(message_parts->at(3)->c_str());

    // store info in local variables for easy access
    instance_id = atoi(message_parts->at(1)->c_str());
    parent_ptr = (NPVariant*) (IcedTeaPluginUtilities::stringToJSID(message_parts->at(5)));
    member_id.append(*(message_parts->at(6)));

    /** Request data from Java if necessary **/
    if (*(message_parts->at(4)) == "GetSlot")
    {
    	int_identifier=true;
    } else
    {
        // make a new request for getString, to get the name of the identifier
        java_result = java_request.getString(member_id);

        // the result we want is in result_string (assuming there was no error)
        if (java_result->error_occurred)
        {
	    printf("Unable to process getMember request. Error occurred: %s\n", java_result->error_msg->c_str());
            //goto cleanup;
        }

        member_id.assign(*(java_result->return_string));
    	int_identifier=false;
    }

    AsyncCallThreadData thread_data = AsyncCallThreadData();
    thread_data.result_ready = false;
    thread_data.parameters = std::vector<void*>();
    thread_data.result = std::string();

    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(parent_ptr);

    // If instance is invalid, do not proceed further
    if (!instance)
    	return;

    thread_data.parameters.push_back(instance);
    thread_data.parameters.push_back(NPVARIANT_TO_OBJECT(*parent_ptr));
    thread_data.parameters.push_back(&member_id);
    thread_data.parameters.push_back(&int_identifier);

    IcedTeaPluginUtilities::callAndWaitForResult(instance, &_getMember, &thread_data);

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &response);
    if (*(message_parts->at(4)) == "GetSlot")
    {
        response.append(" JavaScriptGetSlot ");
    } else {
        response.append(" JavaScriptGetMember ");
    }
    response.append(thread_data.result);
    plugin_to_java_bus->post(response.c_str());
}

/**
 * Decrements reference count to given object
 *
 * @param message_parts The request message.
 */

void
PluginRequestProcessor::finalize(std::vector<std::string*>* message_parts)
{
    std::string* type;
    std::string* command;
    int reference;
    std::string response = std::string();
    std::string* variant_ptr_str;
    NPVariant* variant_ptr;
    NPObject* window_ptr;
    int id;

    type = message_parts->at(0);
    id = atoi(message_parts->at(1)->c_str());
    reference = atoi(message_parts->at(3)->c_str());
    variant_ptr_str = message_parts->at(5);

    NPP instance;
    get_instance_from_id(id, instance);

    variant_ptr = (NPVariant*) IcedTeaPluginUtilities::stringToJSID(variant_ptr_str);
    window_ptr = NPVARIANT_TO_OBJECT(*variant_ptr);
    browser_functions.releaseobject(window_ptr);

    // remove reference
    IcedTeaPluginUtilities::removeInstanceID(variant_ptr);

    // clear memory
    free(variant_ptr);

    // We need the context 0 for backwards compatibility with the Java side
    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &response);
    response += " JavaScriptFinalize";

    plugin_to_java_bus->post(response.c_str());
}

/**
 * Fetches the URL and loads it into the given target
 *
 * @param message_parts The request message.
 */
void
PluginRequestProcessor::loadURL(std::vector<std::string*>* message_parts)
{

    int id = atoi(message_parts->at(1)->c_str());

    AsyncCallThreadData thread_data = AsyncCallThreadData();
    thread_data.result_ready = false;
    thread_data.parameters = std::vector<void*>();
    thread_data.result = std::string();

    NPP instance;
    get_instance_from_id(id, instance);

    // If instance is invalid, do not proceed further
    if (!instance)
    	return;

    thread_data.parameters.push_back(instance);
    thread_data.parameters.push_back(message_parts->at(5)); // push url
    thread_data.parameters.push_back(message_parts->at(6)); // push target

    thread_data.result_ready = false;
    IcedTeaPluginUtilities::callAndWaitForResult(instance, &_loadURL, &thread_data);
}

static void
queue_cleanup(void* data)
{

    pthread_mutex_destroy((pthread_mutex_t*) data);

    PLUGIN_DEBUG("Queue processing stopped.\n");
}

void*
queue_processor(void* data)
{

    PluginRequestProcessor* processor = (PluginRequestProcessor*) data;
    std::vector<std::string*>* message_parts = NULL;
    std::string command;
    pthread_mutex_t wait_mutex = PTHREAD_MUTEX_INITIALIZER;

    PLUGIN_DEBUG("Queue processor initialized. Queue = %p\n", message_queue);

    pthread_mutex_init(&wait_mutex, NULL);

    pthread_cleanup_push(queue_cleanup, (void*) &wait_mutex);

    while (true)
    {
        pthread_mutex_lock(&message_queue_mutex);
        if (message_queue->size() > 0)
        {
            message_parts = message_queue->front();
            message_queue->erase(message_queue->begin());
        }
        pthread_mutex_unlock(&message_queue_mutex);

        if (message_parts)
        {
            command = *(message_parts->at(4));

            if (command == "GetMember")
            {
                processor->sendMember(message_parts);
            } else if (command == "ToString")
            {
                processor->sendString(message_parts);
            } else if (command == "SetMember")
            {
            	// write methods are synchronized
            	pthread_mutex_lock(&syn_write_mutex);
                processor->setMember(message_parts);
                pthread_mutex_unlock(&syn_write_mutex);
            } else if (command == "Call")
            {
                // write methods are synchronized
                pthread_mutex_lock(&syn_write_mutex);
                processor->call(message_parts);
                pthread_mutex_unlock(&syn_write_mutex);
            } else if (command == "Eval")
            {
                // write methods are synchronized
                pthread_mutex_lock(&syn_write_mutex);
                processor->eval(message_parts);
                pthread_mutex_unlock(&syn_write_mutex);
            } else if (command == "GetSlot")
            {
                // write methods are synchronized
                pthread_mutex_lock(&syn_write_mutex);
                processor->sendMember(message_parts);
                pthread_mutex_unlock(&syn_write_mutex);
            } else if (command == "SetSlot")
            {
                // write methods are synchronized
                pthread_mutex_lock(&syn_write_mutex);
                processor->setMember(message_parts);
                pthread_mutex_unlock(&syn_write_mutex);
            } else if (command == "LoadURL") // For instance X url <url> <target>
            {
                // write methods are synchronized
                pthread_mutex_lock(&syn_write_mutex);
                processor->loadURL(message_parts);
                pthread_mutex_unlock(&syn_write_mutex);
            } else
            {
                // Nothing matched
                IcedTeaPluginUtilities::printStringPtrVector("Error: Unable to process message: ", message_parts);
            }

            // Free memory for message_parts
            IcedTeaPluginUtilities::freeStringPtrVector(message_parts);

        } else
        {
	    pthread_mutex_lock(&wait_mutex);
	    pthread_cond_wait(&cond_message_available, &wait_mutex);
	    pthread_mutex_unlock(&wait_mutex);
        }

        message_parts = NULL;

	pthread_testcancel();
    }

    pthread_cleanup_pop(1);
}

/******************************************
 * Functions delegated to the main thread *
 ******************************************/

void
_setMember(void* data)
{
    std::string* value;

    NPP instance;
    NPVariant value_variant = NPVariant();
    NPObject* member;
    NPIdentifier property_identifier;


    std::vector<void*> parameters = ((AsyncCallThreadData*) data)->parameters;
    instance = (NPP) parameters.at(0);
    member = (NPObject*) parameters.at(1);
    std::string*  property_id = (std::string*) parameters.at(2);
    value = (std::string*) parameters.at(3);
    bool* int_identifier = (bool*) parameters.at(4);

    if(*int_identifier==true)
    	property_identifier = browser_functions.getintidentifier(atoi(property_id->c_str()));
    else
    	property_identifier = browser_functions.getstringidentifier(property_id->c_str());

    PLUGIN_DEBUG("Setting %s on instance %p, object %p to value %s\n", browser_functions.utf8fromidentifier(property_identifier), instance, member, value->c_str());

    IcedTeaPluginUtilities::javaResultToNPVariant(instance, value, &value_variant);

    ((AsyncCallThreadData*) data)->call_successful = browser_functions.setproperty(instance, member, property_identifier, &value_variant);

    ((AsyncCallThreadData*) data)->result_ready = true;
}

void
_getMember(void* data)
{
    NPObject* parent_ptr;
    NPVariant* member_ptr = new NPVariant();
    std::string member_ptr_str = std::string();
    NPP instance;

    std::vector<void*> parameters = ((AsyncCallThreadData*) data)->parameters;

    instance = (NPP) parameters.at(0);
    parent_ptr = (NPObject*) parameters.at(1);
    std::string*  member_id = (std::string*) parameters.at(2);
    NPIdentifier member_identifier;

    bool* int_identifier = (bool*) parameters.at(3);

    if(*int_identifier==true)
    	member_identifier = browser_functions.getintidentifier(atoi(member_id->c_str()));
    else
    	member_identifier = browser_functions.getstringidentifier(member_id->c_str());

    // Get the NPVariant corresponding to this member
    PLUGIN_DEBUG("Looking for %p %p %p (%s)\n", instance, parent_ptr, member_identifier, browser_functions.utf8fromidentifier(member_identifier));

    if (!browser_functions.hasproperty(instance, parent_ptr, member_identifier))
    {
        printf("%s not found!\n", browser_functions.utf8fromidentifier(member_identifier));
    }
    ((AsyncCallThreadData*) data)->call_successful = browser_functions.getproperty(instance, parent_ptr, member_identifier, member_ptr);

    IcedTeaPluginUtilities::printNPVariant(*member_ptr);

    if (((AsyncCallThreadData*) data)->call_successful)
    {
        createJavaObjectFromVariant(instance, *member_ptr, &member_ptr_str);
        ((AsyncCallThreadData*) data)->result.append(member_ptr_str);

    }
    ((AsyncCallThreadData*) data)->result_ready = true;

    // store member -> instance link
    IcedTeaPluginUtilities::storeInstanceID(member_ptr, instance);

    PLUGIN_DEBUG("_getMember returning.\n");
}

void
_eval(void* data)
{
    NPP instance;
    NPObject* window_ptr;
    std::string* script_str;
    NPIdentifier script_identifier;
    NPString script = NPString();
    NPVariant* eval_variant = new NPVariant();
    std::string eval_variant_str = std::string();

    PLUGIN_DEBUG("_eval called\n");

    std::vector<void*>* call_data = (std::vector<void*>*) data;

    instance = (NPP) call_data->at(0);
    window_ptr = (NPObject*) call_data->at(1);
    script_str = (std::string*) call_data->at(2);

#if MOZILLA_VERSION_COLLAPSED < 1090200
    script.utf8characters = script_str->c_str();
    script.utf8length = script_str->size();

    PLUGIN_DEBUG("Evaluating: %s\n", script_str->c_str());
#else
    script.UTF8Characters = script_str->c_str();
    script.UTF8Length = script_str->size();

    PLUGIN_DEBUG("Evaluating: %s\n", script_str->c_str());
#endif

    ((AsyncCallThreadData*) data)->call_successful = browser_functions.evaluate(instance, window_ptr, &script, eval_variant);
    IcedTeaPluginUtilities::printNPVariant(*eval_variant);

    if (((AsyncCallThreadData*) data)->call_successful)
    {
        if (eval_variant)
        {
            createJavaObjectFromVariant(instance, *eval_variant, &eval_variant_str);
        } else
        {
            eval_variant_str = "0";
        }
    } else
    {
        eval_variant_str = "0";
    }

    ((AsyncCallThreadData*) data)->result.append(eval_variant_str);
    ((AsyncCallThreadData*) data)->result_ready = true;

    PLUGIN_DEBUG("_eval returning\n");
}


void
_call(void* data)
{
    NPP instance;
    NPObject* window_ptr;
    std::string* function_name;
    NPIdentifier function;
    int* arg_count;
    NPVariant* args;
    NPVariant* call_result = new NPVariant();
    std::string call_result_ptr_str = std::string();

    PLUGIN_DEBUG("_call called\n");

    std::vector<void*>* call_data = (std::vector<void*>*) data;

    instance = (NPP) call_data->at(0);
    window_ptr = (NPObject*) call_data->at(1);
    function_name = (std::string*) call_data->at(2);

    function = browser_functions.getstringidentifier(function_name->c_str());
    arg_count = (int*) call_data->at(3);
    args = (NPVariant*) call_data->at(4);

    for (int i=0; i < *arg_count; i++) {
        IcedTeaPluginUtilities::printNPVariant(args[i]);
    }

    PLUGIN_DEBUG("_calling\n");
    ((AsyncCallThreadData*) data)->call_successful = browser_functions.invoke(instance, window_ptr, function, args, *arg_count, call_result);
    PLUGIN_DEBUG("_called\n");

    IcedTeaPluginUtilities::printNPVariant(*call_result);

    if (((AsyncCallThreadData*) data)->call_successful)
    {

        if (call_result)
        {
            createJavaObjectFromVariant(instance, *call_result, &call_result_ptr_str);
        } else
        {
        	call_result_ptr_str = "0";
        }
    } else
    {
        call_result_ptr_str = "0";
    }

    ((AsyncCallThreadData*) data)->result.append(call_result_ptr_str);
    ((AsyncCallThreadData*) data)->result_ready = true;

    PLUGIN_DEBUG("_call returning\n");
}

void
_getString(void* data)
{
    NPP instance;
    NPObject* object;
    NPIdentifier toString = browser_functions.getstringidentifier("toString");
    NPVariant tostring_result;
    std::string result = std::string();

    std::vector<void*>* call_data = (std::vector<void*>*) data;
    instance = (NPP) call_data->at(0);
    NPVariant* variant = (NPVariant*) call_data->at(1);

    PLUGIN_DEBUG("_getString called with %p and %p\n", instance, variant);

    if (NPVARIANT_IS_OBJECT(*variant))
    {
        ((AsyncCallThreadData*) data)->call_successful = browser_functions.invoke(instance, NPVARIANT_TO_OBJECT(*variant), toString, NULL, 0, &tostring_result);
    }
    else
    {
        IcedTeaPluginUtilities::NPVariantToString(*variant, &result);
        tostring_result = NPVariant();
        STRINGZ_TO_NPVARIANT(result.c_str(), tostring_result);
        ((AsyncCallThreadData*) data)->call_successful = true;
    }

    PLUGIN_DEBUG("ToString result: ");
    IcedTeaPluginUtilities::printNPVariant(tostring_result);

    if (((AsyncCallThreadData*) data)->call_successful)
    {
        createJavaObjectFromVariant(instance, tostring_result, &(((AsyncCallThreadData*) data)->result));
    }
    ((AsyncCallThreadData*) data)->result_ready = true;

    PLUGIN_DEBUG("_getString returning\n");
}

void
_loadURL(void* data) {

    PLUGIN_DEBUG("_loadURL called\n");

    NPP instance;
    std::string* url;
    std::string* target;

    std::vector<void*> parameters = ((AsyncCallThreadData*) data)->parameters;

    instance = (NPP) parameters.at(0);
    url = (std::string*) parameters.at(1);
    target = (std::string*) parameters.at(2);

    PLUGIN_DEBUG("Launching %s in %s\n", url->c_str(), target->c_str());

    // Each decode can expand to 4 chars at the most
    gchar* decoded_url = (gchar*) calloc(strlen(url->c_str())*4 + 1, sizeof(gchar));
    IcedTeaPluginUtilities::decodeURL(url->c_str(), &decoded_url);

    ((AsyncCallThreadData*) data)->call_successful =
        (*browser_functions.geturl) (instance, decoded_url, target->c_str());

    ((AsyncCallThreadData*) data)->result_ready = true;

    free(decoded_url);
    decoded_url = NULL;

    PLUGIN_DEBUG("_loadURL returning %d\n", ((AsyncCallThreadData*) data)->call_successful);
}
