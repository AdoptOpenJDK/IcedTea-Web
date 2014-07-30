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
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::deAllocate %p\n", npobj);
}

void
IcedTeaScriptablePluginObject::invalidate(NPObject *npobj)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::invalidate %p\n", npobj);
}

bool
IcedTeaScriptablePluginObject::hasMethod(NPObject *npobj, NPIdentifier name_id)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::hasMethod %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::invoke(NPObject *npobj, NPIdentifier name_id, const NPVariant *args,
			uint32_t argCount,NPVariant *result)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::invoke %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::invokeDefault(NPObject *npobj, const NPVariant *args,
			       uint32_t argCount, NPVariant *result)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::invokeDefault %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::hasProperty(NPObject *npobj, NPIdentifier name_id)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::hasProperty %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::getProperty(NPObject *npobj, NPIdentifier name_id, NPVariant *result)
{
	// Package request?
	if (IcedTeaPluginUtilities::NPIdentifierAsString(name_id) == "java")
	{
		//NPObject* obj = IcedTeaScriptableJavaPackageObject::get_scriptable_java_package_object(getInstanceFromMemberPtr(npobj), name);
		//OBJECT_TO_NPVARIANT(obj, *result);

		//PLUGIN_ERROR ("Filling variant %p with object %p\n", result);
	}

	return false;
}

bool
IcedTeaScriptablePluginObject::setProperty(NPObject *npobj, NPIdentifier name_id, const NPVariant *value)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::setProperty %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::removeProperty(NPObject *npobj, NPIdentifier name_id)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::removeProperty %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::enumerate(NPObject *npobj, NPIdentifier **value, uint32_t *count)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::enumerate %p\n", npobj);
	return false;
}

bool
IcedTeaScriptablePluginObject::construct(NPObject *npobj, const NPVariant *args, uint32_t argCount,
	           NPVariant *result)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptablePluginObject::construct %p\n", npobj);
	return false;
}

NPObject*
allocate_scriptable_jp_object(NPP npp, NPClass *aClass)
{
    PLUGIN_DEBUG("Allocating new scriptable Java Package object\n");
    return new IcedTeaScriptableJavaPackageObject(npp);
}

static NPClass
scriptable_plugin_object_class() {
    NPClass np_class;
    np_class.structVersion = NP_CLASS_STRUCT_VERSION;
    np_class.allocate = allocate_scriptable_jp_object;
    np_class.deallocate = IcedTeaScriptableJavaPackageObject::deAllocate;
    np_class.invalidate = IcedTeaScriptableJavaPackageObject::invalidate;
    np_class.hasMethod = IcedTeaScriptableJavaPackageObject::hasMethod;
    np_class.invoke = IcedTeaScriptableJavaPackageObject::invoke;
    np_class.invokeDefault = IcedTeaScriptableJavaPackageObject::invokeDefault;
    np_class.hasProperty = IcedTeaScriptableJavaPackageObject::hasProperty;
    np_class.getProperty = IcedTeaScriptableJavaPackageObject::getProperty;
    np_class.setProperty = IcedTeaScriptableJavaPackageObject::setProperty;
    np_class.removeProperty = IcedTeaScriptableJavaPackageObject::removeProperty;
    np_class.enumerate = IcedTeaScriptableJavaPackageObject::enumerate;
    np_class.construct = IcedTeaScriptableJavaPackageObject::construct;
    return np_class;
}

NPObject*
IcedTeaScriptableJavaPackageObject::get_scriptable_java_package_object(NPP instance, const NPUTF8* name)
{
    /* Shared NPClass instance for IcedTeaScriptablePluginObject */
    static NPClass np_class = scriptable_plugin_object_class();

    NPObject* scriptable_object = browser_functions.createobject(instance, &np_class);
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
    this->package_name->assign(name);
}

std::string
IcedTeaScriptableJavaPackageObject::getPackageName()
{
    return *this->package_name;
}

void
IcedTeaScriptableJavaPackageObject::deAllocate(NPObject *npobj)
{
    delete (IcedTeaScriptableJavaPackageObject*)npobj;
}

void
IcedTeaScriptableJavaPackageObject::invalidate(NPObject *npobj)
{
	// nothing to do for these
}

bool
IcedTeaScriptableJavaPackageObject::hasMethod(NPObject *npobj, NPIdentifier name_id)
{
    // Silly caller. Methods are for objects!
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::invoke(NPObject *npobj, NPIdentifier name_id, const NPVariant *args,
			uint32_t argCount,NPVariant *result)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptableJavaPackageObject::invoke %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::invokeDefault(NPObject *npobj, const NPVariant *args,
			       uint32_t argCount, NPVariant *result)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptableJavaPackageObject::invokeDefault %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::hasProperty(NPObject *npobj, NPIdentifier name_id)
{
	std::string name = IcedTeaPluginUtilities::NPIdentifierAsString(name_id);

	PLUGIN_DEBUG("IcedTeaScriptableJavaPackageObject::hasProperty %s\n", name.c_str());

	bool hasProperty = false;
	JavaResultData* java_result;
	JavaRequestProcessor* java_request = new JavaRequestProcessor();
    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);
    int plugin_instance_id = get_id_from_instance(instance);
	IcedTeaScriptableJavaPackageObject* scriptable_obj = (IcedTeaScriptableJavaPackageObject*)npobj;

	PLUGIN_DEBUG("Object package name: \"%s\"\n", scriptable_obj->getPackageName().c_str());

	// "^java" is always a package
	if (scriptable_obj->getPackageName().empty() && (name == "java" || name == "javax"))
	{
	    return true;
	}

	std::string property_name = scriptable_obj->getPackageName();
	if (!property_name.empty())
	    property_name += ".";
	property_name += name;

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
IcedTeaScriptableJavaPackageObject::getProperty(NPObject *npobj, NPIdentifier name_id, NPVariant *result)
{
    std::string name = IcedTeaPluginUtilities::NPIdentifierAsString(name_id);

	PLUGIN_DEBUG("IcedTeaScriptableJavaPackageObject::getProperty %s\n", name.c_str());

	if (!browser_functions.identifierisstring(name_id))
	    return false;

	bool isPropertyClass = false;
	JavaResultData* java_result;
	JavaRequestProcessor java_request = JavaRequestProcessor();
    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);
    int plugin_instance_id = get_id_from_instance(instance);
    IcedTeaScriptableJavaPackageObject* scriptable_obj = (IcedTeaScriptableJavaPackageObject*)npobj;

    std::string property_name = scriptable_obj->getPackageName();
    if (!property_name.empty())
        property_name += ".";
    property_name += name;

	java_result = java_request.findClass(plugin_instance_id, property_name);
	isPropertyClass = (java_result->return_identifier == 0);

	//NPIdentifier property = browser_functions.getstringidentifier(property_name.c_str());

	NPObject* obj;

	if (isPropertyClass)
	{
		PLUGIN_DEBUG("Returning package object\n");
		obj = IcedTeaScriptableJavaPackageObject::get_scriptable_java_package_object(
                                  IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj),
                                  property_name.c_str());
	}
	else
	{
		PLUGIN_DEBUG("Returning Java object\n");
		obj = IcedTeaScriptableJavaObject::get_scriptable_java_object(
		                IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj),
		                *(java_result->return_string), "0", false);
	}

	OBJECT_TO_NPVARIANT(obj, *result);

	return true;
}

bool
IcedTeaScriptableJavaPackageObject::setProperty(NPObject *npobj, NPIdentifier name_id, const NPVariant *value)
{
	// Can't be going around setting properties on namespaces.. that's madness!
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::removeProperty(NPObject *npobj, NPIdentifier name_id)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptableJavaPackageObject::removeProperty %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::enumerate(NPObject *npobj, NPIdentifier **value, uint32_t *count)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptableJavaPackageObject::enumerate %p\n", npobj);
	return false;
}

bool
IcedTeaScriptableJavaPackageObject::construct(NPObject *npobj, const NPVariant *args, uint32_t argCount,
	           NPVariant *result)
{
	PLUGIN_ERROR ("** Unimplemented: IcedTeaScriptableJavaPackageObject::construct %p\n", npobj);
	return false;
}

NPObject*
allocate_scriptable_java_object(NPP npp, NPClass *aClass)
{
    PLUGIN_DEBUG("Allocating new scriptable Java object\n");
    return new IcedTeaScriptableJavaObject(npp);
}


static NPClass
scriptable_java_package_object_class() {
    NPClass np_class;
    np_class.structVersion = NP_CLASS_STRUCT_VERSION;
    np_class.allocate = allocate_scriptable_java_object;
    np_class.deallocate = IcedTeaScriptableJavaObject::deAllocate;
    np_class.invalidate = IcedTeaScriptableJavaObject::invalidate;
    np_class.hasMethod = IcedTeaScriptableJavaObject::hasMethod;
    np_class.invoke = IcedTeaScriptableJavaObject::invoke;
    np_class.invokeDefault = IcedTeaScriptableJavaObject::invokeDefault;
    np_class.hasProperty = IcedTeaScriptableJavaObject::hasProperty;
    np_class.getProperty = IcedTeaScriptableJavaObject::getProperty;
    np_class.setProperty = IcedTeaScriptableJavaObject::setProperty;
    np_class.removeProperty = IcedTeaScriptableJavaObject::removeProperty;
    np_class.enumerate = IcedTeaScriptableJavaObject::enumerate;
    np_class.construct = IcedTeaScriptableJavaObject::construct;
    return np_class;
}

NPObject*
IcedTeaScriptableJavaObject::get_scriptable_java_object(NPP instance,
                                    std::string class_id,
                                    std::string instance_id,
                                    bool isArray)
{
    /* Shared NPClass instance for IcedTeaScriptablePluginObject */
    static NPClass np_class = scriptable_java_package_object_class();

    std::string obj_key = class_id + ":" + instance_id;

    PLUGIN_DEBUG("get_scriptable_java_object searching for %s...\n", obj_key.c_str());
    IcedTeaScriptableJavaObject* scriptable_object = (IcedTeaScriptableJavaObject*) IcedTeaPluginUtilities::getNPObjectFromJavaKey(obj_key);

    if (scriptable_object != NULL)
    {
        PLUGIN_DEBUG("Returning existing object %p\n", scriptable_object);
        browser_functions.retainobject(scriptable_object);
        return scriptable_object;
    }

    // try to create normally
    scriptable_object = (IcedTeaScriptableJavaObject*)browser_functions.createobject(instance, &np_class);

    // didn't work? try creating asynch
    if (!scriptable_object)
    {
        AsyncCallThreadData thread_data = AsyncCallThreadData();
        thread_data.result_ready = false;
        thread_data.parameters = std::vector<void*>();
        thread_data.result = std::string();

        thread_data.parameters.push_back(instance);
        thread_data.parameters.push_back(&np_class);
        thread_data.parameters.push_back(&scriptable_object);

        IcedTeaPluginUtilities::callAndWaitForResult(instance, &_createAndRetainJavaObject, &thread_data);
    } else
    {
        // Else retain object and continue
        browser_functions.retainobject(scriptable_object);
    }

    PLUGIN_DEBUG("Constructed new Java Object with classid=%s, instanceid=%s, isArray=%d and scriptable_object=%p\n", class_id.c_str(), instance_id.c_str(), isArray, scriptable_object);

    scriptable_object->class_id = class_id;
    scriptable_object->is_object_array = isArray;

	if (instance_id != "0")
	    scriptable_object->instance_id = instance_id;

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

bool
IcedTeaScriptableJavaObject::hasMethod(NPObject *npobj, NPIdentifier name_id)
{
    std::string name = IcedTeaPluginUtilities::NPIdentifierAsString(name_id);
    IcedTeaScriptableJavaObject* scriptable_object = (IcedTeaScriptableJavaObject*) npobj;

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::hasMethod %s (ival=%d)\n", name.c_str(), browser_functions.intfromidentifier(name_id));
    bool hasMethod = false;

    // If object is an array and requested "method" may be a number, check for it first
    if ( !scriptable_object->is_object_array  ||
         (browser_functions.intfromidentifier(name_id) < 0))
    {

        if (!browser_functions.identifierisstring(name_id))
            return false;

        JavaResultData* java_result;
        JavaRequestProcessor java_request = JavaRequestProcessor();

        java_result = java_request.hasMethod(scriptable_object->class_id, name);
        hasMethod = java_result->return_identifier != 0;
    }

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::hasMethod returning %d\n", hasMethod);
    return hasMethod;
}

bool
IcedTeaScriptableJavaObject::invoke(NPObject *npobj, NPIdentifier name_id, const NPVariant *args,
			uint32_t argCount, NPVariant *result)
{
    std::string name = IcedTeaPluginUtilities::NPIdentifierAsString(name_id);

    // Extract arg type array
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::invoke %s. Args follow.\n", name.c_str());
    for (int i=0; i < argCount; i++)
    {
        IcedTeaPluginUtilities::printNPVariant(args[i]);
    }

    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();

    IcedTeaScriptableJavaObject* scriptable_object = (IcedTeaScriptableJavaObject*)npobj;

    std::string instance_id = scriptable_object->instance_id;
    std::string class_id = scriptable_object->class_id;

    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);

    // First, load the arguments into the java-side table
    std::string id = std::string();
    std::vector<std::string> arg_ids = std::vector<std::string>();
    for (int i=0; i < argCount; i++) {
        id.clear();
        createJavaObjectFromVariant(instance, args[i], &id);

        if (id == "-1")
        {
            PLUGIN_ERROR("Unable to create arguments on Java side\n");
            return false;
        }

        arg_ids.push_back(id);
    }

    if (instance_id.length() == 0) // Static
    {
        PLUGIN_DEBUG("Calling static method\n");
        java_result = java_request.callStaticMethod(
                        IcedTeaPluginUtilities::getSourceFromInstance(instance),
                        scriptable_object->class_id, name, arg_ids);
    } else
    {
        PLUGIN_DEBUG("Calling method normally\n");
        java_result = java_request.callMethod(
                        IcedTeaPluginUtilities::getSourceFromInstance(instance),
                        scriptable_object->instance_id, name, arg_ids);
    }

    if (java_result->error_occurred)
    {
        browser_functions.setexception(npobj, java_result->error_msg->c_str());
        return false;
    }

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::invoke converting and returning.\n");
    return IcedTeaPluginUtilities::javaResultToNPVariant(instance, java_result->return_string, result);
}

bool
IcedTeaScriptableJavaObject::hasProperty(NPObject *npobj, NPIdentifier name_id)
{
    std::string name = IcedTeaPluginUtilities::NPIdentifierAsString(name_id);

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::hasProperty %s (ival=%d)\n", name.c_str(), browser_functions.intfromidentifier(name_id));
    bool hasProperty = false;

    IcedTeaScriptableJavaObject* scriptable_object = (IcedTeaScriptableJavaObject*)npobj;
    // If it is an array, only length and indexes are valid
    if (scriptable_object->is_object_array)
    {
        if (browser_functions.intfromidentifier(name_id) >= 0 || name == "length")
            hasProperty = true;

    } else
    {
        if (!browser_functions.identifierisstring(name_id))
            return false;

        if (name == "Packages")
        {
            hasProperty = true;
        } else {

            JavaResultData* java_result;
            JavaRequestProcessor java_request = JavaRequestProcessor();

            java_result = java_request.hasField(scriptable_object->class_id, name);

            hasProperty = java_result->return_identifier != 0;
        }
    }

	PLUGIN_DEBUG("IcedTeaScriptableJavaObject::hasProperty returning %d\n", hasProperty);
	return hasProperty;
}

bool
IcedTeaScriptableJavaObject::getProperty(NPObject *npobj, NPIdentifier name_id, NPVariant *result)
{
    std::string name = IcedTeaPluginUtilities::NPIdentifierAsString(name_id);
    bool is_string_id = browser_functions.identifierisstring(name_id);
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::getProperty %s (ival=%d)\n", name.c_str(), browser_functions.intfromidentifier(name_id));

    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();

    IcedTeaScriptableJavaObject* scriptable_object = (IcedTeaScriptableJavaObject*)npobj;

    std::string instance_id = scriptable_object->getInstanceID();
    std::string class_id = scriptable_object->getClassID();
    NPP instance = scriptable_object->instance;

    if (instance_id.length() > 0) // Could be an array or a simple object
    {
        // If array and requesting length
        if ( scriptable_object->is_object_array && name == "length")
        {
            java_result = java_request.getArrayLength(instance_id);
        } else if ( scriptable_object->is_object_array &&
                    browser_functions.intfromidentifier(name_id) >= 0) // else if array and requesting index
        {

            java_result = java_request.getArrayLength(instance_id);
            if (java_result->error_occurred)
            {
                PLUGIN_ERROR("ERROR: Couldn't fetch array length\n");
                return false;
            }

            int length = atoi(java_result->return_string->c_str());

            // Access beyond size?
            if (browser_functions.intfromidentifier(name_id) >= length)
            {
                VOID_TO_NPVARIANT(*result);
                return true;
            }

            std::string index = std::string();
            IcedTeaPluginUtilities::itoa(browser_functions.intfromidentifier(name_id), &index);
            java_result = java_request.getSlot(instance_id, index);

        } else // Everything else
        {
            if (!is_string_id) {
                return false;
            }

            if (name == "Packages")
            {
                NPObject* pkgObject = IcedTeaScriptableJavaPackageObject::get_scriptable_java_package_object(instance, "");
                OBJECT_TO_NPVARIANT(pkgObject, *result);
                return true;
            }

            java_result = java_request.getField(
                        IcedTeaPluginUtilities::getSourceFromInstance(instance),
                        class_id, instance_id, name);
        }
    }
    else
    {
        if (!is_string_id) {
            return false;
        }

        java_result = java_request.getStaticField(
                                IcedTeaPluginUtilities::getSourceFromInstance(instance),
                                class_id, name);
    }

    if (java_result->error_occurred)
    {
        return false;
    }

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::getProperty converting and returning.\n");
    return IcedTeaPluginUtilities::javaResultToNPVariant(instance, java_result->return_string, result);
}

bool
IcedTeaScriptableJavaObject::setProperty(NPObject *npobj, NPIdentifier name_id, const NPVariant *value)
{
    std::string name = IcedTeaPluginUtilities::NPIdentifierAsString(name_id);
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::setProperty %s (ival=%d) to:\n", name.c_str(), browser_functions.intfromidentifier(name_id));
    IcedTeaPluginUtilities::printNPVariant(*value);

    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();
    IcedTeaScriptableJavaObject* scriptable_object = (IcedTeaScriptableJavaObject*)npobj;

    std::string instance_id = scriptable_object->getInstanceID();
    std::string class_id = scriptable_object->getClassID();

    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);

    if (instance_id.length() > 0) // Could be an array or a simple object
    {
        // If array
        if (scriptable_object->is_object_array && name == "length")
        {
            PLUGIN_ERROR("ERROR: Array length is not a modifiable property\n");
            return false;
        } else if ( scriptable_object->is_object_array &&
                    browser_functions.intfromidentifier(name_id) >= 0) // else if array and requesting index
        {

            java_result = java_request.getArrayLength(instance_id);
            if (java_result->error_occurred)
            {
                PLUGIN_ERROR("ERROR: Couldn't fetch array length\n");
                return false;
            }

            int length = atoi(java_result->return_string->c_str());

            // Access beyond size?
            if (browser_functions.intfromidentifier(name_id) >= length)
            {
                return true;
            }

            std::string index = std::string();
            IcedTeaPluginUtilities::itoa(browser_functions.intfromidentifier(name_id), &index);

            std::string value_id = std::string();
            createJavaObjectFromVariant(instance, *value, &value_id);

            java_result = java_request.setSlot(instance_id, index, value_id);

        } else // Everything else
        {
            std::string value_id = std::string();
            createJavaObjectFromVariant(instance, *value, &value_id);

            java_result = java_request.setField(
                        IcedTeaPluginUtilities::getSourceFromInstance(instance),
                        class_id, instance_id, name, value_id);
        }
    }
    else
    {
        std::string value_id = std::string();
        createJavaObjectFromVariant(instance, *value, &value_id);

        java_result = java_request.setStaticField(
                                IcedTeaPluginUtilities::getSourceFromInstance(instance),
                                class_id, name, value_id);
    }

    if (java_result->error_occurred)
    {
        return false;
    }

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::setProperty returning.\n");
    return true;
}

bool
IcedTeaScriptableJavaObject::construct(NPObject *npobj, const NPVariant *args, uint32_t argCount,
	           NPVariant *result)
{
    IcedTeaScriptableJavaObject* scriptable_object = (IcedTeaScriptableJavaObject*)npobj;
    // Extract arg type array
    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::construct %s. Args follow.\n", scriptable_object->getClassID().c_str());
    for (int i=0; i < argCount; i++)
    {
        IcedTeaPluginUtilities::printNPVariant(args[i]);
    }

    JavaResultData* java_result;
    JavaRequestProcessor java_request = JavaRequestProcessor();

    NPP instance = IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj);

    // First, load the arguments into the java-side table
    std::string id = std::string();
    std::vector<std::string> arg_ids = std::vector<std::string>();
    for (int i=0; i < argCount; i++) {
        id.clear();
        createJavaObjectFromVariant(instance, args[i], &id);
        if (id == "0")
        {
            browser_functions.setexception(npobj, "Unable to create argument on Java side");
            return false;
        }

        arg_ids.push_back(id);
    }

    java_result = java_request.newObject(
                            IcedTeaPluginUtilities::getSourceFromInstance(instance),
                            scriptable_object->class_id,
                            arg_ids);

    if (java_result->error_occurred)
    {
        browser_functions.setexception(npobj, java_result->error_msg->c_str());
        return false;
    }

    std::string return_obj_instance_id = *java_result->return_string;
    std::string return_obj_class_id = scriptable_object->class_id;

    NPObject* obj = IcedTeaScriptableJavaObject::get_scriptable_java_object(
                                IcedTeaPluginUtilities::getInstanceFromMemberPtr(npobj),
                                return_obj_class_id, return_obj_instance_id, false);

    OBJECT_TO_NPVARIANT(obj, *result);

    PLUGIN_DEBUG("IcedTeaScriptableJavaObject::construct returning.\n");
    return true;
}
