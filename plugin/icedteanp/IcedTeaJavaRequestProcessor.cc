/* IcedTeaJavaRequestProcessor.cc

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

#include "IcedTeaJavaRequestProcessor.h"
#include "IcedTeaScriptablePluginObject.h"

/*
 * This class processes LiveConnect requests from JavaScript to Java.
 *
 * It sends the requests to Java, gets the return information, and sends it
 * back to the browser/JavaScript
 */

/**
 * Processes return information from JavaSide (return messages of requests)
 *
 * @param message The message request to process
 * @return boolean indicating whether the message is serviceable by this object
 */

bool
JavaRequestProcessor::newMessageOnBus(const char* message)
{

	// Anything we are waiting for _MUST_ have and instance id and reference #
	std::vector<std::string*>* message_parts = IcedTeaPluginUtilities::strSplit(message, " ");

	IcedTeaPluginUtilities::printStringPtrVector("JavaRequest::newMessageOnBus:", message_parts);

	if (*(message_parts->at(0)) == "context" && *(message_parts->at(2)) == "reference")
		if (atoi(message_parts->at(1)->c_str()) == this->instance && atoi(message_parts->at(3)->c_str()) == this->reference)
		{
			// Gather the results

			// Let's get errors out of the way first
			if (!message_parts->at(4)->find("Error"))
			{
				for (int i=5; i < message_parts->size(); i++)
				{
					result->error_msg->append(*(message_parts->at(i)));
					result->error_msg->append(" ");
				}

				printf("Error on Java side: %s\n", result->error_msg->c_str());

				result->error_occurred = true;
				result_ready = true;
			}
			else if (!message_parts->at(4)->find("GetStringUTFChars") ||
			         !message_parts->at(4)->find("GetToStringValue"))
			{
				// first item is length, and it is radix 10
				int length = strtol(message_parts->at(5)->c_str(), NULL, 10);

				IcedTeaPluginUtilities::getUTF8String(length, 6 /* start at */, message_parts, result->return_string);
				result_ready = true;
			}
			else if (!message_parts->at(4)->find("GetStringChars")) // GetStringChars (UTF-16LE/UCS-2)
			{
				// first item is length, and it is radix 10
				int length = strtol(message_parts->at(5)->c_str(), NULL, 10);

				IcedTeaPluginUtilities::getUTF16LEString(length, 6 /* start at */, message_parts, result->return_wstring);
				result_ready = true;
			} else if (!message_parts->at(4)->find("FindClass") ||
			           !message_parts->at(4)->find("GetClassName") ||
			           !message_parts->at(4)->find("GetClassID") ||
			           !message_parts->at(4)->find("GetMethodID") ||
			           !message_parts->at(4)->find("GetStaticMethodID") ||
			           !message_parts->at(4)->find("GetObjectClass") ||
			           !message_parts->at(4)->find("NewObject") ||
			           !message_parts->at(4)->find("NewStringUTF") ||
			           !message_parts->at(4)->find("HasPackage") ||
			           !message_parts->at(4)->find("HasMethod") ||
			           !message_parts->at(4)->find("HasField") ||
			           !message_parts->at(4)->find("GetStaticFieldID") ||
			           !message_parts->at(4)->find("GetFieldID") ||
			           !message_parts->at(4)->find("GetJavaObject") ||
			           !message_parts->at(4)->find("IsInstanceOf") ||
			           !message_parts->at(4)->find("NewArray"))
			{
				result->return_identifier = atoi(message_parts->at(5)->c_str());
				result->return_string->append(*(message_parts->at(5))); // store it as a string as well, for easy access
				result_ready = true;
			}  else if (!message_parts->at(4)->find("DeleteLocalRef") ||
			            !message_parts->at(4)->find("NewGlobalRef"))
			{
			    result_ready = true; // nothing else to do
			} else if (!message_parts->at(4)->find("CallMethod") ||
			           !message_parts->at(4)->find("CallStaticMethod") ||
			           !message_parts->at(4)->find("GetField") ||
			           !message_parts->at(4)->find("GetStaticField") ||
			           !message_parts->at(4)->find("GetValue") ||
			           !message_parts->at(4)->find("GetObjectArrayElement"))
			{

			    if (!message_parts->at(5)->find("literalreturn"))
                {
			        // literal returns don't have a corresponding jni id
			        result->return_identifier = 0;
			        result->return_string->append(*(message_parts->at(5)));
			        result->return_string->append(" ");
			        result->return_string->append(*(message_parts->at(6)));

                } else
			    {
                    // Else it is a complex object

			        result->return_identifier = atoi(message_parts->at(5)->c_str());
			        result->return_string->append(*(message_parts->at(5))); // store it as a string as well, for easy access
			    }

				result_ready = true;
			} else if (!message_parts->at(4)->find("GetArrayLength"))
            {
			    result->return_identifier = 0; // length is not an "identifier"
			    result->return_string->append(*(message_parts->at(5)));
			    result_ready = true;
            } else if (!message_parts->at(4)->find("SetField") ||
                       !message_parts->at(4)->find("SetObjectArrayElement"))
            {

                // nothing to do

                result->return_identifier = 0;
                result_ready = true;
            }

            IcedTeaPluginUtilities::freeStringPtrVector(message_parts);
			return true;
		}

    IcedTeaPluginUtilities::freeStringPtrVector(message_parts);
	return false;
}

/**
 * Constructor.
 *
 * Initializes the result data structure (heap)
 */

JavaRequestProcessor::JavaRequestProcessor()
{
    PLUGIN_DEBUG("JavaRequestProcessor constructor\n");

	// caller frees this
	result = new JavaResultData();
	result->error_msg = new std::string();
	result->return_identifier = 0;
	result->return_string = new std::string();
	result->return_wstring = new std::wstring();
	result->error_occurred = false;

	result_ready = false;
}

/**
 * Destructor
 *
 * Frees memory used by the result struct
 */

JavaRequestProcessor::~JavaRequestProcessor()
{
    PLUGIN_DEBUG("JavaRequestProcessor::~JavaRequestProcessor\n");

	if (result)
	{
		if (result->error_msg)
			delete result->error_msg;

		if (result->return_string)
			delete result->return_string;

		if (result->return_wstring)
			delete result->return_wstring;

		delete result;
	}
}

/**
 * Resets the results
 */
void
JavaRequestProcessor::resetResult()
{
	// caller frees this
	result->error_msg->clear();
	result->return_identifier = 0;
	result->return_string->clear();
	result->return_wstring->clear();
	result->error_occurred = false;

	result_ready = false;
}

void
JavaRequestProcessor::postAndWaitForResponse(std::string message)
{
    struct timespec t;
    clock_gettime(CLOCK_REALTIME, &t);
    t.tv_sec += REQUESTTIMEOUT; // 1 minute timeout

    // Clear the result
    resetResult();

    java_to_plugin_bus->subscribe(this);
    plugin_to_java_bus->post(message.c_str());

    // Wait for result to be filled in.
    struct timespec curr_t;

    bool isPluginThread = false;

    if (pthread_self() == itnp_plugin_thread_id)
    {
        isPluginThread = true;
        PLUGIN_DEBUG("JRP is in plug-in thread...\n");
    }

    do
    {
        clock_gettime(CLOCK_REALTIME, &curr_t);

        if (!result_ready && (curr_t.tv_sec < t.tv_sec))
        {
            if (isPluginThread)
            {
                processAsyncCallQueue(NULL);

                // Let the browser run its pending events too
                if (g_main_context_pending(NULL))
                {
                    g_main_context_iteration(NULL, false);
                } else
                {
                    usleep(1000); // 1ms
                }
            } else
            {
                usleep(1000); // 1ms
            }
        }
        else
        {
            break;
        }
    } while (1);

    if (curr_t.tv_sec >= t.tv_sec)
    {
    	result->error_occurred = true;
    	result->error_msg->append("Error: Timed out when waiting for response");

    	// Report error
    	PLUGIN_DEBUG("Error: Timed out when waiting for response to %s\n", message.c_str());
    }

    java_to_plugin_bus->unSubscribe(this);
}

/**
 * Given an object id, fetches the toString() value from Java
 *
 * @param object_id The ID of the object
 * @return A JavaResultData struct containing the result of the request
 */

JavaResultData*
JavaRequestProcessor::getToStringValue(std::string object_id)
{
	std::string message = std::string();

	this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
	this->reference = IcedTeaPluginUtilities::getReference();

	IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" GetToStringValue "); // get it in UTF8
    message.append(object_id);

    postAndWaitForResponse(message);

	IcedTeaPluginUtilities::releaseReference();

	return result;
}

/**
 * Given an object id, fetches the value of that ID from Java
 *
 * @param object_id The ID of the object
 * @return A JavaResultData struct containing the result of the request
 */

JavaResultData*
JavaRequestProcessor::getValue(std::string object_id)
{
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" GetValue "); // get it in UTF8
    message.append(object_id);

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

/**
 * Given a string id, fetches the actual string from Java side
 *
 * @param string_id The ID of the string
 * @return A JavaResultData struct containing the result of the request
 */

JavaResultData*
JavaRequestProcessor::getString(std::string string_id)
{
    std::string message = std::string();

	this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
	this->reference = IcedTeaPluginUtilities::getReference();

	IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" GetStringUTFChars "); // get it in UTF8
    message.append(string_id);

    postAndWaitForResponse(message);

	IcedTeaPluginUtilities::releaseReference();

	return result;
}

/**
 * Decrements reference count by 1
 *
 * @param object_id The ID of the object
 */

void
JavaRequestProcessor::deleteReference(std::string object_id)
{
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" DeleteLocalRef ");
    message.append(object_id);

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();
}

/**
 * Increments reference count by 1
 *
 * @param object_id The ID of the object
 */

void
JavaRequestProcessor::addReference(std::string object_id)
{
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" NewGlobalRef ");
    message.append(object_id);

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

}

JavaResultData*
JavaRequestProcessor::findClass(int plugin_instance_id,
                                std::string name)
{
    std::string message = std::string();
    std::string plugin_instance_id_str = std::string();

    IcedTeaPluginUtilities::itoa(plugin_instance_id, &plugin_instance_id_str);

	this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
	this->reference = IcedTeaPluginUtilities::getReference();

	IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" FindClass ");
    message.append(plugin_instance_id_str);
    message.append(" ");
    message.append(name);

    postAndWaitForResponse(message);

	return result;
}

JavaResultData*
JavaRequestProcessor::getClassName(std::string objectID)
{
    std::string message = std::string();

	this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
	this->reference = IcedTeaPluginUtilities::getReference();

	IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" GetClassName ");
    message.append(objectID);

    postAndWaitForResponse(message);

	return result;
}

JavaResultData*
JavaRequestProcessor::getClassID(std::string objectID)
{
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" GetClassID ");
    message.append(objectID);

    postAndWaitForResponse(message);

    return result;
}

JavaResultData*
JavaRequestProcessor::getArrayLength(std::string objectID)
{
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" GetArrayLength ");
    message.append(objectID);

    postAndWaitForResponse(message);

    return result;
}

JavaResultData*
JavaRequestProcessor::getSlot(std::string objectID, std::string index)
{
    std::string message = std::string();
    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" GetObjectArrayElement ");
    message.append(objectID);
    message.append(" ");
    message.append(index);

    postAndWaitForResponse(message);

    return result;
}

JavaResultData*
JavaRequestProcessor::setSlot(std::string objectID,
                              std::string index,
                              std::string value_id)
{
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" SetObjectArrayElement ");
    message.append(objectID);
    message.append(" ");
    message.append(index);
    message.append(" ");
    message.append(value_id);

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

JavaResultData*
JavaRequestProcessor::newArray(std::string array_class,
                               std::string length)
{
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);

    message.append(" NewArray ");
    message.append(array_class);
    message.append(" ");
    message.append(length);

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

JavaResultData*
JavaRequestProcessor::getFieldID(std::string classID, std::string fieldName)
{
	JavaResultData* java_result;
	JavaRequestProcessor* java_request = new JavaRequestProcessor();
	std::string message = std::string();

	java_result = java_request->newString(fieldName);

	this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
	this->reference = IcedTeaPluginUtilities::getReference();

	IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
	message.append(" GetFieldID ");
	message.append(classID);
	message.append(" ");
	message.append(java_result->return_string->c_str());

	postAndWaitForResponse(message);

	IcedTeaPluginUtilities::releaseReference();

	delete java_request;

	return result;
}

JavaResultData*
JavaRequestProcessor::getStaticFieldID(std::string classID, std::string fieldName)
{
    JavaResultData* java_result;
    JavaRequestProcessor* java_request = new JavaRequestProcessor();
    std::string message = std::string();

    java_result = java_request->newString(fieldName);

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
    message.append(" GetStaticFieldID ");
    message.append(classID);
    message.append(" ");
    message.append(java_result->return_string->c_str());

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    delete java_request;

    return result;
}

JavaResultData*
JavaRequestProcessor::getField(std::string source,
                               std::string classID,
                               std::string objectID,
                               std::string fieldName)
{
    JavaResultData* java_result;
    JavaRequestProcessor* java_request = new JavaRequestProcessor();
    std::string message = std::string();

    java_result = java_request->getFieldID(classID, fieldName);

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, source, &message);
    message.append(" GetField ");
    message.append(objectID);
    message.append(" ");
    message.append(java_result->return_string->c_str());

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    delete java_request;

    return result;
}

JavaResultData*
JavaRequestProcessor::getStaticField(std::string source, std::string classID,
                                     std::string fieldName)
{
    JavaResultData* java_result;
    JavaRequestProcessor* java_request = new JavaRequestProcessor();
    std::string message = std::string();

    java_result = java_request->getStaticFieldID(classID, fieldName);

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, source, &message);
    message.append(" GetStaticField ");
    message.append(classID);
    message.append(" ");
    message.append(java_result->return_string->c_str());

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    delete java_request;

    return result;
}


JavaResultData*
JavaRequestProcessor::set(std::string source,
                          bool isStatic,
                          std::string classID,
                          std::string objectID,
                          std::string fieldName,
                          std::string value_id)
{
    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();
    std::string message = std::string();

    java_result = java_request.getFieldID(classID, fieldName);

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, source, &message);

    if (isStatic)
    {
        message.append(" SetStaticField ");
        message.append(classID);
    } else
    {
        message.append(" SetField ");
        message.append(objectID);
    }

    message.append(" ");
    message.append(java_result->return_string->c_str());
    message.append(" ");
    message.append(value_id);

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

JavaResultData*
JavaRequestProcessor::setStaticField(std::string source,
                                     std::string classID,
                                     std::string fieldName,
                                     std::string value_id)
{
    return set(source, true, classID, "", fieldName, value_id);
}

JavaResultData*
JavaRequestProcessor::setField(std::string source,
                               std::string classID,
                               std::string objectID,
                               std::string fieldName,
                               std::string value_id)
{
    return set(source, false, classID, objectID, fieldName, value_id);
}

JavaResultData*
JavaRequestProcessor::getMethodID(std::string classID, NPIdentifier methodName,
                                  std::vector<std::string> args)
{
	JavaRequestProcessor* java_request;
	std::string message = std::string();
    std::string* signature;

    signature = new std::string();
    *signature += "(";

    // FIXME: Need to determine how to extract array types and complex java objects
    for (int i=0; i < args.size(); i++)
    {
    	*signature += args[i];
    }

    *signature += ")";

	this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
	this->reference = IcedTeaPluginUtilities::getReference();

	IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
	message += " GetMethodID ";
	message += classID;
	message += " ";
	message += browser_functions.utf8fromidentifier(methodName);
	message += " ";
	message += *signature;

	postAndWaitForResponse(message);

	IcedTeaPluginUtilities::releaseReference();
	delete signature;

	return result;
}

JavaResultData*
JavaRequestProcessor::getStaticMethodID(std::string classID, NPIdentifier methodName,
                                  std::vector<std::string> args)
{
    JavaRequestProcessor* java_request;
    std::string message = std::string();
    std::string* signature;

    signature = new std::string();
    *signature += "(";

    // FIXME: Need to determine how to extract array types and complex java objects
    for (int i=0; i < args.size(); i++)
    {
        *signature += args[i];
    }

    *signature += ")";

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
    message += " GetStaticMethodID ";
    message += classID;
    message += " ";
    message += browser_functions.utf8fromidentifier(methodName);
    message += " ";
    message += *signature;

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();
    delete signature;

    return result;
}

void
getArrayTypeForJava(NPP instance, NPVariant element, std::string* type)
{

    if (NPVARIANT_IS_BOOLEAN(element)) {
        type->append("string");
    } else if (NPVARIANT_IS_INT32(element)) {
        type->append("string");
    } else if (NPVARIANT_IS_DOUBLE(element)) {
        type->append("string");
    } else if (NPVARIANT_IS_STRING(element)) {
        type->append("string");
    } else if (NPVARIANT_IS_OBJECT(element)) {

        NPObject* first_element_obj = NPVARIANT_TO_OBJECT(element);
        if (IcedTeaScriptableJavaPackageObject::is_valid_java_object(first_element_obj))
        {
            std::string class_id = std::string(((IcedTeaScriptableJavaObject*) first_element_obj)->getClassID());
            type->append(class_id);
        } else
        {
            type->append("jsobject");
        }
    } else {
        type->append("jsobject"); // Else it is a string
    }
}

void
createJavaObjectFromVariant(NPP instance, NPVariant variant, std::string* id)
{
	JavaResultData* java_result;

	std::string className;
	std::string jsObjectClassID = std::string();
	std::string jsObjectConstructorID = std::string();

	std::string stringArg = std::string();
	std::vector<std::string> args = std::vector<std::string>();

	JavaRequestProcessor java_request = JavaRequestProcessor();
	bool alreadyCreated = false;

    if (NPVARIANT_IS_VOID(variant))
    {
    	PLUGIN_DEBUG("VOID %d\n", variant);
    	id->append("0");
    	return; // no need to go further
    } else if (NPVARIANT_IS_NULL(variant))
    {
    	PLUGIN_DEBUG("NULL\n", variant);
    	id->append("0");
    	return; // no need to go further
    } else if (NPVARIANT_IS_BOOLEAN(variant))
    {
    	className = "java.lang.Boolean";

    	if (NPVARIANT_TO_BOOLEAN(variant))
    		stringArg = "true";
    	else
    		stringArg = "false";

    } else if (NPVARIANT_IS_INT32(variant))
    {
    	className = "java.lang.Integer";

    	char* valueStr = (char*) malloc(sizeof(char)*32);
    	sprintf(valueStr, "%d", NPVARIANT_TO_INT32(variant));
    	stringArg += valueStr;
    	free(valueStr);
    } else if (NPVARIANT_IS_DOUBLE(variant))
    {
    	className = "java.lang.Double";

    	char* valueStr = (char*) malloc(sizeof(char)*1024);
    	sprintf(valueStr, "%f", NPVARIANT_TO_DOUBLE(variant));
    	stringArg += valueStr;
    	free(valueStr);
    } else if (NPVARIANT_IS_STRING(variant))
    {
    	className = "java.lang.String";
    	stringArg = IcedTeaPluginUtilities::NPVariantAsString(variant);
    } else if (NPVARIANT_IS_OBJECT(variant))
    {

        NPObject* obj = NPVARIANT_TO_OBJECT(variant);
        if (IcedTeaScriptableJavaPackageObject::is_valid_java_object(obj))
        {
            PLUGIN_DEBUG("NPObject is a Java object\n");
            alreadyCreated = true;
        } else
        {
            PLUGIN_DEBUG("NPObject is not a Java object\n");
            NPIdentifier length_id = browser_functions.getstringidentifier("length");
            bool isJSObjectArray = false;

            // FIXME: We currently only handle <= 2 dim arrays. Do we really need more though?

            // Is it an array?
            if (IcedTeaPluginUtilities::isObjectJSArray(instance, obj)) {
                PLUGIN_DEBUG("NPObject is an array\n");

                std::string array_id = std::string();
                std::string java_array_type = std::string();
                NPVariant length = NPVariant();
                browser_functions.getproperty(instance, obj, length_id, &length);

                std::string length_str = std::string();
                IcedTeaPluginUtilities::itoa(NPVARIANT_TO_INT32(length), &length_str);

                if (NPVARIANT_TO_INT32(length) >= 0)
                {
                    NPIdentifier id_0 = browser_functions.getintidentifier(0);
                    NPVariant first_element = NPVariant();
                    browser_functions.getproperty(instance, obj, id_0, &first_element);

                    // Check for multi-dim array
                    if (NPVARIANT_IS_OBJECT(first_element) &&
                        IcedTeaPluginUtilities::isObjectJSArray(instance, NPVARIANT_TO_OBJECT(first_element))) {

                        NPVariant first_nested_element = NPVariant();
                        browser_functions.getproperty(instance, NPVARIANT_TO_OBJECT(first_element), id_0, &first_nested_element);

                        getArrayTypeForJava(instance, first_nested_element, &java_array_type);

                        length_str.append(" 0"); // secondary array is created on the fly
                    } else
                    {
                        getArrayTypeForJava(instance, first_element, &java_array_type);
                    }
                }

                // For JSObject arrays, we create a regular object (accessible via JSObject.getSlot())
                if (NPVARIANT_TO_INT32(length) < 0 || !java_array_type.compare("jsobject"))
                {
                    isJSObjectArray = true;
                    goto createRegularObject;
                }

                java_result = java_request.newArray(java_array_type, length_str);

                if (java_result->error_occurred) {
                    printf("Unable to create array\n");
                    id->append("-1");
                    return;
                }

                id->append(*(java_result->return_string));

                NPIdentifier index_id = NPIdentifier();
                for (int i=0; i < NPVARIANT_TO_INT32(length); i++)
                {
                    NPVariant value = NPVariant();

                    index_id = browser_functions.getintidentifier(i);
                    browser_functions.getproperty(instance, obj, index_id, &value);

                    std::string value_id = std::string();
                    createJavaObjectFromVariant(instance, value, &value_id);

                    if (value_id == "-1") {
                        printf("Unable to populate array\n");
                        id->clear();
                        id->append("-1");
                        return;
                    }

                    std::string value_str = std::string();
                    IcedTeaPluginUtilities::itoa(i, &value_str);
                    java_result = java_request.setSlot(*id, value_str, value_id);

                }

                // Got here => no errors above. We're good to return!
                return;
            }

            createRegularObject:
            if (!IcedTeaPluginUtilities::isObjectJSArray(instance, obj) || isJSObjectArray) // Else it is not an array
            {

                NPVariant* variant_copy = new NPVariant();
                OBJECT_TO_NPVARIANT(NPVARIANT_TO_OBJECT(variant), *variant_copy);

                className = "netscape.javascript.JSObject";
                IcedTeaPluginUtilities::JSIDToString(variant_copy, &stringArg);
                browser_functions.retainobject(NPVARIANT_TO_OBJECT(variant));

                std::string jsObjectClassID = std::string();
                std::string jsObjectConstructorID = std::string();
                std::vector<std::string> args = std::vector<std::string>();

                java_result = java_request.findClass(0, "netscape.javascript.JSObject");

                // the result we want is in result_string (assuming there was no error)
                if (java_result->error_occurred)
                {
                    printf("Unable to get JSObject class id\n");
                    id->clear();
                    id->append("-1");
                    return;
                }

                jsObjectClassID.append(*(java_result->return_string));
                args.push_back("J");

                java_result = java_request.getMethodID(jsObjectClassID,
                                                       browser_functions.getstringidentifier("<init>"),
                                                       args);

                // the result we want is in result_string (assuming there was no error)
                if (java_result->error_occurred)
                {
                    printf("Unable to get JSObject constructor id\n");
                    id->clear();
                    id->append("-1");
                    return;
                }

                jsObjectConstructorID.append(*(java_result->return_string));

                // We have the method id. Now create a new object.

                args.clear();
                args.push_back(stringArg);
                java_result = java_request.newObjectWithConstructor("",
                                                     jsObjectClassID,
                                                     jsObjectConstructorID,
                                                     args);

                // Store the instance ID for future reference
                IcedTeaPluginUtilities::storeInstanceID(variant_copy, instance);

                // the result we want is in result_string (assuming there was no error)
                // the result we want is in result_string (assuming there was no error)
                if (java_result->error_occurred)
                {
                    printf("Unable to create JSObject\n");
                    id->clear();
                    id->append("-1");
                    return;
                }

                id->append(*(java_result->return_string));
                return;
            }
        }
    }

    if (!alreadyCreated) {
		java_result = java_request.findClass(0, className);

		// the result we want is in result_string (assuming there was no error)
		if (java_result->error_occurred) {
			printf("Unable to find classid for %s\n", className.c_str());
			id->append("-1");
			return;
		}

		jsObjectClassID.append(*(java_result->return_string));

		std::string stringClassName = "Ljava/lang/String;";
		args.push_back(stringClassName);

		java_result = java_request.getMethodID(jsObjectClassID,
				      browser_functions.getstringidentifier("<init>"), args);

		// the result we want is in result_string (assuming there was no error)
		if (java_result->error_occurred) {
			printf("Unable to find string constructor for %s\n", className.c_str());
			id->append("-1");
            return;
		}

		jsObjectConstructorID.append(*(java_result->return_string));

		// We have class id and constructor ID. So we know we can create the
		// object.. now create the string that will be provided as the arg
		java_result = java_request.newString(stringArg);

		if (java_result->error_occurred) {
			printf("Unable to create requested object\n");
			id->append("-1");
            return;
		}

		// Create the object
		args.clear();
		std::string arg = std::string();
		arg.append(*(java_result->return_string));
		args.push_back(arg);
		java_result = java_request.newObjectWithConstructor("[System]", jsObjectClassID, jsObjectConstructorID, args);

        if (java_result->error_occurred) {
            printf("Unable to create requested object\n");
            id->append("-1");
            return;
        }


		id->append(*(java_result->return_string));

	} else {
	    // Else already created

	    std::string classId = std::string(((IcedTeaScriptableJavaObject*) NPVARIANT_TO_OBJECT(variant))->getClassID());
	    std::string instanceId = std::string(((IcedTeaScriptableJavaObject*) NPVARIANT_TO_OBJECT(variant))->getInstanceID());

	    if (instanceId.length() == 0)
	        id->append(classId.c_str());
	    else
	        id->append(instanceId.c_str());
	}

}

JavaResultData*
JavaRequestProcessor::callStaticMethod(std::string source, std::string classID,
                                       std::string methodName,
                                       std::vector<std::string> args)
{
    return call(source, true, classID, methodName, args);
}

JavaResultData*
JavaRequestProcessor::callMethod(std::string source,
                                 std::string objectID, std::string methodName,
                                 std::vector<std::string> args)
{
    return call(source, false, objectID, methodName, args);
}

JavaResultData*
JavaRequestProcessor::call(std::string source,
                           bool isStatic, std::string objectID,
                           std::string methodName,
                           std::vector<std::string> args)
{
    std::string message = std::string();
    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, source, &message);

    if (isStatic)
        message += " CallStaticMethod ";
    else
        message += " CallMethod ";

    message += objectID;
    message += " ";
    message += methodName;
    message += " ";

    for (int i=0; i < args.size(); i++)
    {
        message += args[i];
        message += " ";
    }

	postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

JavaResultData*
JavaRequestProcessor::getObjectClass(std::string objectID)
{
    JavaRequestProcessor* java_request;
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
    message += " GetObjectClass ";
    message += objectID;

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

JavaResultData*
JavaRequestProcessor::newObject(std::string source, std::string classID,
                                std::vector<std::string> args)
{
    JavaRequestProcessor* java_request;
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, source, &message);
    message += " NewObject ";
    message += classID;
    message += " ";

    for (int i=0; i < args.size(); i++)
    {
        message += args[i];
        message += " ";
    }

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

JavaResultData*
JavaRequestProcessor::newObjectWithConstructor(std::string source, std::string classID,
                                std::string methodID,
                                std::vector<std::string> args)
{
	JavaRequestProcessor* java_request;
	std::string message = std::string();

	this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
	this->reference = IcedTeaPluginUtilities::getReference();

	IcedTeaPluginUtilities::constructMessagePrefix(0, reference, source, &message);
	message += " NewObjectWithConstructor ";
	message += classID;
	message += " ";
	message += methodID;
	message += " ";

	for (int i=0; i < args.size(); i++)
	{
		message += args[i];
		message += " ";
	}

	postAndWaitForResponse(message);

	IcedTeaPluginUtilities::releaseReference();

	return result;
}

JavaResultData*
JavaRequestProcessor::newString(std::string str)
{
	std::string utf_string = std::string();
	std::string message = std::string();

	IcedTeaPluginUtilities::convertStringToUTF8(&str, &utf_string);

	this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
	this->reference = IcedTeaPluginUtilities::getReference();

	IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
	message.append(" NewStringUTF ");
	message.append(utf_string);

	postAndWaitForResponse(message);

	IcedTeaPluginUtilities::releaseReference();

	return result;
}

JavaResultData*
JavaRequestProcessor::hasPackage(int plugin_instance_id,
                                 std::string package_name)
{
	JavaResultData* java_result;
	JavaRequestProcessor* java_request = new JavaRequestProcessor();
	std::string message = std::string();
	std::string plugin_instance_id_str = std::string();
	IcedTeaPluginUtilities::itoa(plugin_instance_id, &plugin_instance_id_str);

	java_result = java_request->newString(package_name);

	this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
	this->reference = IcedTeaPluginUtilities::getReference();

	IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
	message.append(" HasPackage ");
    message.append(plugin_instance_id_str);
    message.append(" ");
	message.append(java_result->return_string->c_str());

	postAndWaitForResponse(message);

	IcedTeaPluginUtilities::releaseReference();

	delete java_request;

	return result;
}

JavaResultData*
JavaRequestProcessor::hasMethod(std::string classID, std::string method_name)
{
    JavaResultData* java_result;
    JavaRequestProcessor* java_request = new JavaRequestProcessor();
    std::string message = std::string();

    java_result = java_request->newString(method_name);

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
    message.append(" HasMethod ");
    message.append(classID);
    message.append(" ");
    message.append(java_result->return_string->c_str());

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    delete java_request;

    return result;
}

JavaResultData*
JavaRequestProcessor::hasField(std::string classID, std::string method_name)
{
    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();
    std::string message = std::string();

    java_result = java_request.newString(method_name);

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
    message.append(" HasField ");
    message.append(classID);
    message.append(" ");
    message.append(java_result->return_string->c_str());

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

JavaResultData*
JavaRequestProcessor::isInstanceOf(std::string objectID, std::string classID)
{
    std::string message = std::string();

    this->instance = 0; // context is always 0 (needed for java-side backwards compat.)
    this->reference = IcedTeaPluginUtilities::getReference();

    IcedTeaPluginUtilities::constructMessagePrefix(0, reference, &message);
    message.append(" IsInstanceOf ");
    message.append(objectID);
    message.append(" ");
    message.append(classID);

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

JavaResultData*
JavaRequestProcessor::getAppletObjectInstance(std::string instanceID)
{
    std::string message = std::string();
    std::string ref_str = std::string();

    this->instance = 0;
    this->reference = IcedTeaPluginUtilities::getReference();
    IcedTeaPluginUtilities::itoa(reference, &ref_str);

    message = "instance ";
    message += instanceID;
    message += " reference ";
    message += ref_str;
    message += " GetJavaObject";

    postAndWaitForResponse(message);

    IcedTeaPluginUtilities::releaseReference();

    return result;
}

