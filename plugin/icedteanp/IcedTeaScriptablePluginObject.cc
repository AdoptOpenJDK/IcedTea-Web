/* IcedTeaScriptablePluginObject.cc

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

IcedTeaScriptablePluginObject::IcedTeaScriptablePluginObject(NPP instance)
{
	this->instance = instance;
	IcedTeaPluginUtilities::storeInstanceID(this, instance);
}

void
IcedTeaScriptablePluginObject::deAllocate(NPObject *npobj)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::deAllocate %p\n", npobj);
}

void
IcedTeaScriptablePluginObject::invalidate(NPObject *npobj)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::invalidate %p\n", npobj);
}

bool
IcedTeaScriptablePluginObject::hasMethod(NPObject *npobj, NPIdentifier name)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::hasMethod %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::invoke(NPObject *npobj, NPIdentifier name, const NPVariant *args,
			uint32_t argCount,NPVariant *result)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::invoke %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::invokeDefault(NPObject *npobj, const NPVariant *args,
			       uint32_t argCount, NPVariant *result)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::invokeDefault %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::hasProperty(NPObject *npobj, NPIdentifier name)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::hasProperty %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::getProperty(NPObject *npobj, NPIdentifier name, NPVariant *result)
{
	// Package request?
	if (!strcmp(browser_functions.utf8fromidentifier(name), "java"))
	{
		//NPObject* obj = IcedTeaScriptablePluginObject::get_scriptable_java_package_object(getInstanceFromMemberPtr(npobj), name);
		//OBJECT_TO_NPVARIANT(obj, *result);

		//printf ("Filling variant %p with object %p\n", result);
	}

	return false;
}

bool
IcedTeaScriptablePluginObject::setProperty(NPObject *npobj, NPIdentifier name, const NPVariant *value)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::setProperty %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::removeProperty(NPObject *npobj, NPIdentifier name)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::removeProperty %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::enumerate(NPObject *npobj, NPIdentifier **value, uint32_t *count)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::enumerate %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::construct(NPObject *npobj, const NPVariant *args, uint32_t argCount,
	           NPVariant *result)
{
	printf ("** Unimplemented: IcedTeaScriptablePluginObject::construct %p\n", npobj);
	return false;
}

NPObject*
allocate_scriptable_jp_object(NPP npp, NPClass *aClass)
{
    PLUGIN_DEBUG("Allocating new scriptable Java Package object\n");
    return new IcedTeaScriptableJavaPackageObject(npp);
}

NPObject*
IcedTeaScriptablePluginObject::get_scriptable_java_package_object(NPP instance, const NPUTF8* name)
{

	NPObject* scriptable_object;

	NPClass* np_class = new NPClass();
	np_class->structVersion = NP_CLASS_STRUCT_VERSION;
	np_class->allocate = allocate_scriptable_jp_object;
	np_class->deallocate = IcedTeaScriptableJavaPackageObject::deAllocate;
	np_class->invalidate = IcedTeaScriptableJavaPackageObject::invalidate;
	np_class->hasMethod = IcedTeaScriptableJavaPackageObject::hasMethod;
	np_class->invoke = IcedTeaScriptableJavaPackageObject::invoke;
	np_class->invokeDefault = IcedTeaScriptableJavaPackageObject::invokeDefault;
	np_class->hasProperty = IcedTeaScriptableJavaPackageObject::hasProperty;
	np_class->getProperty = IcedTeaScriptableJavaPackageObject::getProperty;
	np_class->setProperty = IcedTeaScriptableJavaPackageObject::setProperty;
	np_class->removeProperty = IcedTeaScriptableJavaPackageObject::removeProperty;
	np_class->enumerate = IcedTeaScriptableJavaPackageObject::enumerate;
	np_class->construct = IcedTeaScriptableJavaPackageObject::construct;

	scriptable_object = browser_functions.createobject(instance, np_class);
	PLUGIN_DEBUG("Returning new scriptable package class: %p from instance %p with name %s\n", scriptable_object, instance, name);

    ((IcedTeaScriptableJavaPackageObject*) scriptable_object)->setPackageName(name);

    IcedTeaPluginUtilities::storeInstanceID(scriptable_object, instance);

	return scriptable_object;
}

IcedTeaScriptableJavaPackageObject::IcedTeaScriptableJavaPackageObject(NPP instance)
{
    PLUGIN_DEBUG("Constructing new scriptable java package object\n");
	this->instance = instance;
	this->package_name = new std::string();
}

IcedTeaScriptableJavaPackageObject::~IcedTeaScriptableJavaPackageObject()
{
    delete this->package_name;
}

void
IcedTeaScriptableJavaPackageObject::setPackageName(const NPUTF8* name)
{
    this->package_name->append(name);
}

std::string
IcedTeaScriptableJavaPackageObject::getPackageName()
{
    return this->package_name->c_str();
}

void
IcedTeaScriptableJavaPackageObject::deAllocate(NPObject *npobj)
{
    browser_functions.releaseobject(npobj);
}

void
IcedTeaScriptableJavaPackageObject::invalidate(NPObject *npobj)
{
	// nothing to do for these
}

bool
IcedTeaScriptableJavaPackageObject::hasMethod(NPObject *npobj, NPIdentifier name)
{
    // Silly caller. Methods are for objects!
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::invoke(NPObject *npobj, NPIdentifier name, const NPVariant *args,
			uint32_t argCount,NPVariant *result)
{
	printf ("** Unimplemented: IcedTeaScriptableJavaPackageObject::invoke %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::invokeDefault(NPObject *npobj, const NPVariant *args,
			       uint32_t argCount, NPVariant *result)
{
	printf ("** Unimplemented: IcedTeaScriptableJavaPackageObject::invokeDefault %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::hasProperty(NPObject *npobj, NPIdentifier name)
{
	PLUGIN_DEBUG("IcedTeaScriptableJavaPackageObject::hasProperty %s\n", browser_functions.utf8fromidentifier(name));

	bool hasProperty = false;
	JavaResultData* java_result;
	JavaRequestProcessor* java_request = new JavaRequestProcessor();
    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);
    int plugin_instance_id = get_id_from_instance(instance);

	PLUGIN_DEBUG("Object package name: \"%s\"\n", ((IcedTeaScriptableJavaPackageObject*) npobj)->getPackageName().c_str());

	// "^java" is always a package
	if (((IcedTeaScriptableJavaPackageObject*) npobj)->getPackageName().length() == 0 &&
	    (  !strcmp(browser_functions.utf8fromidentifier(name), "java") ||
	       !strcmp(browser_functions.utf8fromidentifier(name), "javax")))
	{
	    return true;
	}

	std::string property_name = ((IcedTeaScriptableJavaPackageObject*) npobj)->getPackageName();
	if (property_name.length() > 0)
	    property_name += ".";
	property_name += browser_functions.utf8fromidentifier(name);

	PLUGIN_DEBUG("Looking for name \"%s\"\n", property_name.c_str());

	java_result = java_request->hasPackage(plugin_instance_id, property_name);

	if (!java_result->error_occurred && java_result->return_identifier != 0) hasProperty = true;

	// No such package. Do we have a class with that name?
	if (!hasProperty)
	{
		java_result = java_request->findClass(plugin_instance_id, property_name);
	}

	if (java_result->return_identifier != 0) hasProperty = true;

	delete java_request;

	return hasProperty;
}

bool
IcedTeaScriptableJavaPackageObject::getProperty(NPObject *npobj, NPIdentifier name, NPVariant *result)
{

	PLUGIN_DEBUG("IcedTeaScriptableJavaPackageObject::getProperty %s\n", browser_functions.utf8fromidentifier(name));

	if (!browser_functions.utf8fromidentifier(name))
	    return false;

	bool isPropertyClass = false;
	JavaResultData* java_result;
	JavaRequestProcessor java_request = JavaRequestProcessor();
    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);
    int plugin_instance_id = get_id_from_instance(instance);

	std::string property_name = ((IcedTeaScriptableJavaPackageObject*) npobj)->getPackageName();
	if (property_name.length() > 0)
	    property_name += ".";
	property_name += browser_functions.utf8fromidentifier(name);

	java_result = java_request.findClass(plugin_instance_id, property_name);
	isPropertyClass = (java_result->return_identifier == 0);

	//NPIdentifier property = browser_functions.getstringidentifier(property_name.c_str());

	NPObject* obj;

	if (isPropertyClass)
	{
		PLUGIN_DEBUG("Returning package object\n");
		obj = IcedTeaScriptablePluginObject::get_scriptable_java_package_object(
                                  IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj),
                                  property_name.c_str());
	}
	else
	{
		PLUGIN_DEBUG("Returning Java object\n");
		obj = IcedTeaScriptableJavaPackageObject::get_scriptable_java_object(
		                IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj),
		                *(java_result->return_string), "0", false);
	}

	OBJECT_TO_NPVARIANT(obj, *result);

	return true;
}

bool
IcedTeaScriptableJavaPackageObject::setProperty(NPObject *npobj, NPIdentifier name, const NPVariant *value)
{
	// Can't be going around setting properties on namespaces.. that's madness!
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::removeProperty(NPObject *npobj, NPIdentifier name)
{
	printf ("** Unimplemented: IcedTeaScriptableJavaPackageObject::removeProperty %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::enumerate(NPObject *npobj, NPIdentifier **value, uint32_t *count)
{
	printf ("** Unimplemented: IcedTeaScriptableJavaPackageObject::enumerate %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::construct(NPObject *npobj, const NPVariant *args, uint32_t argCount,
	           NPVariant *result)
{
	printf ("** Unimplemented: IcedTeaScriptableJavaPackageObject::construct %p\n", npobj);
	return false;
}

NPObject*
allocate_scriptable_java_object(NPP npp, NPClass *aClass)
{
    PLUGIN_DEBUG("Allocating new scriptable Java object\n");
    return new IcedTeaScriptableJavaObject(npp);
}

NPObject*
IcedTeaScriptableJavaPackageObject::get_scriptable_java_object(NPP instance,
                                    std::string class_id,
                                    std::string instance_id,
                                    bool isArray)
{
    NPObject* scriptable_object;

    std::string obj_key = std::string();
    obj_key += class_id;
    obj_key += ":";
    obj_key += instance_id;

    PLUGIN_DEBUG("get_scriptable_java_object searching for %s...\n", obj_key.c_str());
    scriptable_object = IcedTeaPluginUtilities::getNPObjectFromJavaKey(obj_key);

    if (scriptable_object != NULL)
    {
        PLUGIN_DEBUG("Returning existing object %p\n", scriptable_object);
        browser_functions.retainobject(scriptable_object);
        return scriptable_object;
    }


	NPClass* np_class = new NPClass();
	np_class->structVersion = NP_CLASS_STRUCT_VERSION;
	np_class->allocate = allocate_scriptable_java_object;
	np_class->deallocate = IcedTeaScriptableJavaObject::deAllocate;
	np_class->invalidate = IcedTeaScriptableJavaObject::invalidate;
	np_class->hasMethod = IcedTeaScriptableJavaObject::hasMethod;
	np_class->invoke = IcedTeaScriptableJavaObject::invoke;
	np_class->invokeDefault = IcedTeaScriptableJavaObject::invokeDefault;
	np_class->hasProperty = IcedTeaScriptableJavaObject::hasProperty;
	np_class->getProperty = IcedTeaScriptableJavaObject::getProperty;
	np_class->setProperty = IcedTeaScriptableJavaObject::setProperty;
	np_class->removeProperty = IcedTeaScriptableJavaObject::removeProperty;
	np_class->enumerate = IcedTeaScriptableJavaObject::enumerate;
	np_class->construct = IcedTeaScriptableJavaObject::construct;

	// try to create normally
    scriptable_object =  browser_functions.createobject(instance, np_class);

    // didn't work? try creating asynch
    if (!scriptable_object)
    {
        AsyncCallThreadData thread_data = AsyncCallThreadData();
        thread_data.result_ready = false;
        thread_data.parameters = std::vector<void*>();
        thread_data.result = std::string();

        thread_data.parameters.push_back(instance);
        thread_data.parameters.push_back(np_class);
        thread_data.parameters.push_back(&scriptable_object);

        IcedTeaPluginUtilities::callAndWaitForResult(instance, &_createAndRetainJavaObject, &thread_data);
    } else
    {
        // Else retain object and continue
        browser_functions.retainobject(scriptable_object);
    }

    PLUGIN_DEBUG("Constructed new Java Object with classid=%s, instanceid=%s, isArray=%d and scriptable_object=%p\n", class_id.c_str(), instance_id.c_str(), isArray, scriptable_object);

	((IcedTeaScriptableJavaObject*) scriptable_object)->setClassIdentifier(class_id);
    ((IcedTeaScriptableJavaObject*) scriptable_object)->setIsArray(isArray);

	if (instance_id != "0")
	    ((IcedTeaScriptableJavaObject*) scriptable_object)->setInstanceIdentifier(instance_id);

	IcedTeaPluginUtilities::storeInstanceID(scriptable_object, instance);
	IcedTeaPluginUtilities::storeObjectMapping(obj_key, scriptable_object);

	PLUGIN_DEBUG("Inserting into object_map key %s->%p\n", obj_key.c_str(), scriptable_object);
	return scriptable_object;
}

/* Creates and retains a scriptable java object (intended to be called asynch.) */
void
_createAndRetainJavaObject(void* data)
{
    PLUGIN_DEBUG("Asynchronously creating/retaining object ...\n");

    std::vector<void*> parameters = ((AsyncCallThreadData*) data)->parameters;
    NPP instance = (NPP) parameters.at(0);
    NPClass* np_class = (NPClass*) parameters.at(1);
    NPObject** scriptable_object = (NPObject**) parameters.at(2);

    *scriptable_object = browser_functions.createobject(instance, np_class);
    browser_functions.retainobject(*scriptable_object);

    ((AsyncCallThreadData*) data)->result_ready = true;
}

bool
IcedTeaScriptableJavaPackageObject::is_valid_java_object(NPObject* object_ptr) {
    return IcedTeaPluginUtilities::getInstanceFromMemberPtr(object_ptr) != NULL;
}

IcedTeaScriptableJavaObject::IcedTeaScriptableJavaObject(NPP instance)
{
	this->instance = instance;
	this->class_id = new std::string();
	this->instance_id = new std::string();
}

IcedTeaScriptableJavaObject::~IcedTeaScriptableJavaObject()
{
	delete this->class_id;
	delete this->instance_id;
}

void
IcedTeaScriptableJavaObject::setClassIdentifier(std::string class_id)
{
	this->class_id->append(class_id);
}

void
IcedTeaScriptableJavaObject::setInstanceIdentifier(std::string instance_id)
{
	this->instance_id->append(instance_id);
}

void
IcedTeaScriptableJavaObject::setIsArray(bool isArray)
{
    this->isObjectArray = isArray;
}

void
IcedTeaScriptableJavaObject::deAllocate(NPObject *npobj)
{
	browser_functions.releaseobject(npobj);
}

void
IcedTeaScriptableJavaObject::invalidate(NPObject *npobj)
{
	IcedTeaPluginUtilities::removeInstanceID(npobj);

	std::string obj_key = std::string();
	obj_key += ((IcedTeaScriptableJavaObject*) npobj)->getClassID();
	obj_key += ":";
	obj_key += ((IcedTeaScriptableJavaObject*) npobj)->getInstanceID();

	IcedTeaPluginUtilities::removeObjectMapping(obj_key);
}

bool
IcedTeaScriptableJavaObject::hasMethod(NPObject *npobj, NPIdentifier name)
{
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::hasMethod %s (ival=%d)\n", browser_functions.utf8fromidentifier(name), browser_functions.intfromidentifier(name));
    bool hasMethod = false;

    // If object is an array and requested "method" may be a number, check for it first
    if ( !((IcedTeaScriptableJavaObject*) npobj)->isArray()  ||
         (browser_functions.intfromidentifier(name) < 0))
    {

        if (!browser_functions.utf8fromidentifier(name))
            return false;

        JavaResultData* java_result;
        JavaRequestProcessor java_request = JavaRequestProcessor();

        std::string classId = std::string(((IcedTeaScriptableJavaObject*) npobj)->getClassID());
        std::string methodName = browser_functions.utf8fromidentifier(name);

        java_result = java_request.hasMethod(classId, methodName);
        hasMethod = java_result->return_identifier != 0;
    }

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::hasMethod returning %d\n", hasMethod);
    return hasMethod;
}

bool
IcedTeaScriptableJavaObject::invoke(NPObject *npobj, NPIdentifier name, const NPVariant *args,
			uint32_t argCount, NPVariant *result)
{
    NPUTF8* method_name = browser_functions.utf8fromidentifier(name);

    // Extract arg type array
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::invoke %s. Args follow.\n", method_name);
    for (int i=0; i < argCount; i++)
    {
        IcedTeaPluginUtilities::printNPVariant(args[i]);
    }

    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();

    NPObject* obj;
    std::string instance_id = ((IcedTeaScriptableJavaObject*) npobj)->getInstanceID();
    std::string class_id = ((IcedTeaScriptableJavaObject*) npobj)->getClassID();
    std::string callee;
    std::string source;

    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);

    // First, load the arguments into the java-side table
    std::string id = std::string();
    std::vector<std::string> arg_ids = std::vector<std::string>();
    for (int i=0; i < argCount; i++) {
        id.clear();
        createJavaObjectFromVariant(instance, args[i], &id);

        if (id == "-1")
        {
            printf("Unable to create arguments on Java side\n");
            return false;
        }

        arg_ids.push_back(id);
    }

    if (instance_id.length() == 0) // Static
    {
        PLUGIN_DEBUG("Calling static method\n");
        callee = ((IcedTeaScriptableJavaObject*) npobj)->getClassID();
        java_result = java_request.callStaticMethod(
                        IcedTeaPluginUtilities::getSourceFromInstance(instance),
                        callee, browser_functions.utf8fromidentifier(name), arg_ids);
    } else
    {
        PLUGIN_DEBUG("Calling method normally\n");
        callee = ((IcedTeaScriptableJavaObject*) npobj)->getInstanceID();
        java_result = java_request.callMethod(
                        IcedTeaPluginUtilities::getSourceFromInstance(instance),
                        callee, browser_functions.utf8fromidentifier(name), arg_ids);
    }

    if (java_result->error_occurred)
    {
        // error message must be allocated on heap
        char* error_msg = (char*) malloc(java_result->error_msg->length()*sizeof(char));
        strcpy(error_msg, java_result->error_msg->c_str());
        browser_functions.setexception(npobj, error_msg);
        return false;
    }

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::invoke converting and returning.\n");
    return IcedTeaPluginUtilities::javaResultToNPVariant(instance, java_result->return_string, result);
}

bool
IcedTeaScriptableJavaObject::invokeDefault(NPObject *npobj, const NPVariant *args,
			       uint32_t argCount, NPVariant *result)
{
	printf ("** Unimplemented: IcedTeaScriptableJavaObject::invokeDefault %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaObject::hasProperty(NPObject *npobj, NPIdentifier name)
{
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::hasProperty %s (ival=%d)\n", browser_functions.utf8fromidentifier(name), browser_functions.intfromidentifier(name));
    bool hasProperty = false;

    // If it is an array, only length and indexes are valid
    if (((IcedTeaScriptableJavaObject*) npobj)->isArray())
    {
        if (browser_functions.intfromidentifier(name) >= 0 ||
            !strcmp(browser_functions.utf8fromidentifier(name), "length"))
            hasProperty = true;

    } else
    {

        if (!browser_functions.utf8fromidentifier(name))
            return false;

        if (!strcmp(browser_functions.utf8fromidentifier(name), "Packages"))
        {
            hasProperty = true;
        } else {

            JavaResultData* java_result;
            JavaRequestProcessor java_request = JavaRequestProcessor();

            std::string class_id = std::string(((IcedTeaScriptableJavaObject*) npobj)->getClassID());
            std::string fieldName = browser_functions.utf8fromidentifier(name);

            java_result = java_request.hasField(class_id, fieldName);

            hasProperty = java_result->return_identifier != 0;
        }
    }

	PLUGIN_DEBUG("IcedTeaScriptableJavaObject::hasProperty returning %d\n", hasProperty);
	return hasProperty;
}

bool
IcedTeaScriptableJavaObject::getProperty(NPObject *npobj, NPIdentifier name, NPVariant *result)
{
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::getProperty %s (ival=%d)\n", browser_functions.utf8fromidentifier(name), browser_functions.intfromidentifier(name));

    bool isPropertyClass = false;
    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();

    NPObject* obj;
    std::string instance_id = ((IcedTeaScriptableJavaObject*) npobj)->getInstanceID();
    std::string class_id = ((IcedTeaScriptableJavaObject*) npobj)->getClassID();
    NPP instance = ((IcedTeaScriptableJavaObject*) npobj)->getInstance();

    if (instance_id.length() > 0) // Could be an array or a simple object
    {
        // If array and requesting length
        if ( ((IcedTeaScriptableJavaObject*) npobj)->isArray() &&
             browser_functions.utf8fromidentifier(name) &&
             !strcmp(browser_functions.utf8fromidentifier(name), "length"))
        {
            java_result = java_request.getArrayLength(instance_id);
        } else if ( ((IcedTeaScriptableJavaObject*) npobj)->isArray() &&
                    browser_functions.intfromidentifier(name) >= 0) // else if array and requesting index
        {

            java_result = java_request.getArrayLength(instance_id);
            if (java_result->error_occurred)
            {
                printf("ERROR: Couldn't fetch array length\n");
                return false;
            }

            int length = atoi(java_result->return_string->c_str());

            // Access beyond size?
            if (browser_functions.intfromidentifier(name) >= length)
            {
                VOID_TO_NPVARIANT(*result);
                return true;
            }

            std::string index = std::string();
            IcedTeaPluginUtilities::itoa(browser_functions.intfromidentifier(name), &index);
            java_result = java_request.getSlot(instance_id, index);

        } else // Everything else
        {
            if (!browser_functions.utf8fromidentifier(name))
                return false;

            if (!strcmp(browser_functions.utf8fromidentifier(name), "Packages"))
            {
                NPObject* pkgObject = IcedTeaScriptablePluginObject::get_scriptable_java_package_object(instance, "");
                OBJECT_TO_NPVARIANT(pkgObject, *result);
                return true;
            }

            java_result = java_request.getField(
                        IcedTeaPluginUtilities::getSourceFromInstance(instance),
                        class_id, instance_id, browser_functions.utf8fromidentifier(name));
        }
    }
    else
    {
        if (!browser_functions.utf8fromidentifier(name))
            return true;

        java_result = java_request.getStaticField(
                                IcedTeaPluginUtilities::getSourceFromInstance(instance),
                                class_id, browser_functions.utf8fromidentifier(name));
    }

    if (java_result->error_occurred)
    {
        return false;
    }

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::getProperty converting and returning.\n");
    return IcedTeaPluginUtilities::javaResultToNPVariant(instance, java_result->return_string, result);
}

bool
IcedTeaScriptableJavaObject::setProperty(NPObject *npobj, NPIdentifier name, const NPVariant *value)
{
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::setProperty %s (ival=%d) to:\n", browser_functions.utf8fromidentifier(name), browser_functions.intfromidentifier(name));
    IcedTeaPluginUtilities::printNPVariant(*value);

    bool isPropertyClass = false;
    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();

    NPObject* obj;
    std::string instance_id = ((IcedTeaScriptableJavaObject*) npobj)->getInstanceID();
    std::string class_id = ((IcedTeaScriptableJavaObject*) npobj)->getClassID();

    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);

    if (instance_id.length() > 0) // Could be an array or a simple object
    {
        // If array
        if ( ((IcedTeaScriptableJavaObject*) npobj)->isArray() &&
             browser_functions.utf8fromidentifier(name) &&
             !strcmp(browser_functions.utf8fromidentifier(name), "length"))
        {
            printf("ERROR: Array length is not a modifiable property\n");
            return false;
        } else if ( ((IcedTeaScriptableJavaObject*) npobj)->isArray() &&
                    browser_functions.intfromidentifier(name) >= 0) // else if array and requesting index
        {

            java_result = java_request.getArrayLength(instance_id);
            if (java_result->error_occurred)
            {
                printf("ERROR: Couldn't fetch array length\n");
                return false;
            }

            int length = atoi(java_result->return_string->c_str());

            // Access beyond size?
            if (browser_functions.intfromidentifier(name) >= length)
            {
                return true;
            }

            std::string index = std::string();
            IcedTeaPluginUtilities::itoa(browser_functions.intfromidentifier(name), &index);

            std::string value_id = std::string();
            createJavaObjectFromVariant(instance, *value, &value_id);

            java_result = java_request.setSlot(instance_id, index, value_id);

        } else // Everything else
        {
            std::string value_id = std::string();
            createJavaObjectFromVariant(instance, *value, &value_id);

            java_result = java_request.setField(
                        IcedTeaPluginUtilities::getSourceFromInstance(instance),
                        class_id, instance_id, browser_functions.utf8fromidentifier(name), value_id);
        }
    }
    else
    {
        std::string value_id = std::string();
        createJavaObjectFromVariant(instance, *value, &value_id);

        java_result = java_request.setStaticField(
                                IcedTeaPluginUtilities::getSourceFromInstance(instance),
                                class_id, browser_functions.utf8fromidentifier(name), value_id);
    }

    if (java_result->error_occurred)
    {
        return false;
    }

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::setProperty returning.\n");
    return true;
}

bool
IcedTeaScriptableJavaObject::removeProperty(NPObject *npobj, NPIdentifier name)
{
	printf ("** Unimplemented: IcedTeaScriptableJavaObject::removeProperty %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaObject::enumerate(NPObject *npobj, NPIdentifier **value, uint32_t *count)
{
	printf ("** Unimplemented: IcedTeaScriptableJavaObject::enumerate %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaObject::construct(NPObject *npobj, const NPVariant *args, uint32_t argCount,
	           NPVariant *result)
{
    // Extract arg type array
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::construct %s. Args follow.\n", ((IcedTeaScriptableJavaObject*) npobj)->getClassID().c_str());
    for (int i=0; i < argCount; i++)
    {
        IcedTeaPluginUtilities::printNPVariant(args[i]);
    }

    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();

    NPObject* obj;
    std::string class_id = ((IcedTeaScriptableJavaObject*) npobj)->getClassID();
    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);

    // First, load the arguments into the java-side table
    std::string id = std::string();
    std::vector<std::string> arg_ids = std::vector<std::string>();
    for (int i=0; i < argCount; i++) {
        id.clear();
        createJavaObjectFromVariant(instance, args[i], &id);
        if (id == "0")
        {
            // error message must be allocated on heap
            char* error_msg = (char*) malloc(1024*sizeof(char));
            strcpy(error_msg, "Unable to create argument on Java side");

            browser_functions.setexception(npobj, error_msg);
            return false;
        }

        arg_ids.push_back(id);
    }

    java_result = java_request.newObject(
                            IcedTeaPluginUtilities::getSourceFromInstance(instance),
                            class_id,
                            arg_ids);

    if (java_result->error_occurred)
    {
        // error message must be allocated on heap
        int length = java_result->error_msg->length();
        char* error_msg = (char*) malloc((length+1)*sizeof(char));
        strcpy(error_msg, java_result->error_msg->c_str());

        browser_functions.setexception(npobj, error_msg);
        return false;
    }

    std::string return_obj_instance_id = std::string();
    std::string return_obj_class_id = class_id;
    return_obj_instance_id.append(*(java_result->return_string));

    obj = IcedTeaScriptableJavaPackageObject::get_scriptable_java_object(
                                IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj),
                                return_obj_class_id, return_obj_instance_id, false);

    OBJECT_TO_NPVARIANT(obj, *result);

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::construct returning.\n");
    return true;
}
