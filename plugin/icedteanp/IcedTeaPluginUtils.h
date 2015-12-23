/* IcedTeaPluginUtils.h

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

/**
 * Utility classes for the IcedTeaPlugin
 */

#ifndef __ICEDTEAPLUGINUTILS_H__
#define __ICEDTEAPLUGINUTILS_H__

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <syslog.h>
#include <sys/time.h>
#include <sys/stat.h>

#include <fcntl.h>
#include <cstring>
#include <iostream>
#include <list>
#include <map>
#include <queue>
#include <sstream>
#include <string>
#include <vector>
#include <queue>

#include <npapi.h>
#include <glib.h>
#include <npruntime.h>

#include "IcedTeaParseProperties.h"

void *flush_pre_init_messages(void* data);
void push_pre_init_messages(char * ldm);
void reset_pre_init_messages();

// debugging macro.
#define initialize_debug()                                                    \
  do                                                                          \
  {                                                                           \
    if (!debug_initiated) {                                                   \
      debug_initiated = true;                                                 \
      plugin_debug = getenv ("ICEDTEAPLUGIN_DEBUG") != NULL || is_debug_on(); \
      plugin_debug_headers = is_debug_header_on();                            \
      plugin_debug_to_file = is_logging_to_file();                            \
      plugin_debug_to_streams = is_logging_to_stds();                         \
      plugin_debug_to_system = is_logging_to_system();                        \
      plugin_debug_to_console = is_java_console_enabled();                    \
      if (plugin_debug_to_file) {                                             \
           IcedTeaPluginUtilities::initFileLog();                             \
           file_logs_initiated = true;                                        \
      }                                                                       \
      if (plugin_debug_to_console) {                                          \
          /*initialisation done during jvm startup*/                          \
      }                                                                       \
      IcedTeaPluginUtilities::printDebugStatus();                             \
    }                                                                         \
  } while (0) 


#define  HEADER_SIZE  500
#define  BODY_SIZE  500
#define  MESSAGE_SIZE  HEADER_SIZE + BODY_SIZE 
#define  LDEBUG_MESSAGE_SIZE MESSAGE_SIZE+50

//header is destination char array
#define CREATE_HEADER(ldebug_header)                   \
  do                                                   \
  {                                                    \
    char times[100];                                   \
    time_t t = time(NULL);                             \
    struct tm  p;                                      \
    localtime_r(&t, &p);                               \
    strftime(times, 100, "%a %b %d %H:%M:%S %Z %Y", &p);\
    const char *userNameforDebug = (getenv("USERNAME") == NULL) ? "unknown user" : getenv("USERNAME");  \
    /*this message is parsed in JavaConsole*/          \
    snprintf(ldebug_header, HEADER_SIZE, "[%s][ITW-C-PLUGIN][MESSAGE_DEBUG][%s][%s:%d] ITNPP Thread# %ld, gthread %p: ",        \
    userNameforDebug, times, __FILE__, __LINE__,  pthread_self(), g_thread_self ());                        \
  } while (0)
  

#define PLUGIN_DEBUG(...)              \
  do                                   \
  {                                    \
    initialize_debug();                \
    if (plugin_debug)  {               \
      char ldebug_header[HEADER_SIZE]; \
      char ldebug_body[BODY_SIZE];     \
      char ldebug_message[MESSAGE_SIZE];\
      if (plugin_debug_headers) {      \
        CREATE_HEADER(ldebug_header);  \
      } else {                         \
        sprintf(ldebug_header,"");     \
      }                                \
      snprintf(ldebug_body, BODY_SIZE,  __VA_ARGS__);                               \
      if (plugin_debug_to_streams) {   \
        snprintf(ldebug_message, MESSAGE_SIZE, "%s%s", ldebug_header, ldebug_body); \
        fprintf  (stdout, "%s", ldebug_message);\
      }                                \
      if (plugin_debug_to_file && file_logs_initiated) {      \
        snprintf(ldebug_message, MESSAGE_SIZE, "%s%s", ldebug_header, ldebug_body);   \
        fprintf (plugin_file_log, "%s", ldebug_message);   \
        fflush(plugin_file_log);       \
      }                                \
      if (plugin_debug_to_console) {   \
        /*headers are always going to console*/            \
        if (!plugin_debug_headers){      \
          CREATE_HEADER(ldebug_header);  \
        }                                \
        snprintf(ldebug_message, MESSAGE_SIZE, "%s%s", ldebug_header, ldebug_body); \
        char ldebug_channel_message[LDEBUG_MESSAGE_SIZE];                               \
        struct timeval current_time;   \
        gettimeofday (&current_time, NULL);\
        if (jvm_up) {                  \
          snprintf(ldebug_channel_message, LDEBUG_MESSAGE_SIZE, "%s %ld %s", "plugindebug", current_time.tv_sec*1000000L+current_time.tv_usec, ldebug_message);   \
          push_pre_init_messages(ldebug_channel_message);                           \
        } else {                       \
          snprintf(ldebug_channel_message, LDEBUG_MESSAGE_SIZE, "%s %ld %s", "preinit_plugindebug", current_time.tv_sec*1000000L+current_time.tv_usec, ldebug_message);   \
          push_pre_init_messages(ldebug_channel_message);                            \
        }                              \
      }                                \
     if (plugin_debug_to_system){      \
     /*no debug messages to systemlog*/\
     }                                 \
    }                                  \
  } while (0)


#define PLUGIN_ERROR(...)              \
  do                                   \
  {                                    \
    initialize_debug();                \
    char ldebug_header[HEADER_SIZE];   \
    char ldebug_body[BODY_SIZE];       \
    char ldebug_message[MESSAGE_SIZE]; \
    if (plugin_debug_headers) {        \
      CREATE_HEADER(ldebug_header);    \
    } else {                           \
      sprintf(ldebug_header,"");       \
    }                                  \
    snprintf(ldebug_body, BODY_SIZE,  __VA_ARGS__);   \
    if (plugin_debug_to_streams) {     \
      snprintf(ldebug_message, MESSAGE_SIZE, "%s%s", ldebug_header, ldebug_body); \
      fprintf  (stderr, "%s", ldebug_message);                                    \
    }                                  \
    if (plugin_debug_to_file && file_logs_initiated) {        \
      snprintf(ldebug_message, MESSAGE_SIZE, "%s%s", ldebug_header, ldebug_body); \
      fprintf (plugin_file_log, "%s", ldebug_message);   \
      fflush(plugin_file_log);         \
    }                                  \
    if (plugin_debug_to_console) {     \
      /*headers are always going to console*/            \
      if (!plugin_debug_headers){            \
        CREATE_HEADER(ldebug_header);  \
      }                                \
      snprintf(ldebug_message, MESSAGE_SIZE, "%s%s", ldebug_header, ldebug_body); \
      char ldebug_channel_message[LDEBUG_MESSAGE_SIZE];                               \
      struct timeval current_time;     \
      gettimeofday (&current_time, NULL);\
        if (jvm_up) {                  \
          snprintf(ldebug_channel_message, LDEBUG_MESSAGE_SIZE, "%s %ld %s", "pluginerror", current_time.tv_sec*1000000L+current_time.tv_usec, ldebug_message);   \
          push_pre_init_messages(ldebug_channel_message);                         \
        } else {                       \
          snprintf(ldebug_channel_message, LDEBUG_MESSAGE_SIZE, "%s %ld %s", "preinit_pluginerror", current_time.tv_sec*1000000L+current_time.tv_usec, ldebug_message);   \
          push_pre_init_messages(ldebug_channel_message);                         \
        }                              \
    }                                  \
    if (plugin_debug_to_system){      \
      /*java can not have prefix*/    \
      openlog("", LOG_NDELAY, LOG_USER);\
      syslog(LOG_ERR, "%s", "IcedTea-Web c-plugin - for more info see itweb-settings debug options or console. See http://icedtea.classpath.org/wiki/IcedTea-Web#Filing_bugs for help.");\
      syslog(LOG_ERR, "%s", "IcedTea-Web c-plugin error manual log:");\
      /*no headers to syslog*/        \
      syslog(LOG_ERR, "%s", ldebug_body);   \
      closelog();                     \
    }                                 \
   } while (0)


#define CHECK_JAVA_RESULT(result_data)                               \
{                                                                    \
    if (((JavaResultData*) result_data)->error_occurred)             \
    {                                                                \
        PLUGIN_ERROR("Error: Error occurred on Java side: %s.\n",    \
               ((JavaResultData*) result_data)->error_msg->c_str()); \
        return;                                                      \
    }                                                                \
}

#define HEX_TO_INT(c) \
    ((*c >= 'a') ? *c - 'a' + 10 : \
     (*c >= 'A') ? *c - 'A' + 10 : \
     *c - '0')

#define IS_VALID_HEX(c) \
    ((*c >= '0' && *c <= '9') || \
     (*c >= 'a' && *c <= 'f') || \
     (*c >= 'A' && *c <= 'F'))

//long long max ~ 19 chars + terminator
//leave some room for converting strings like "<var> = %d"
const size_t NUM_STR_BUFFER_SIZE = 32;

/*
 * This struct holds data specific to a Java operation requested by the plugin
 */
typedef struct java_result_data
{

    // Return identifier (if applicable)
    int return_identifier;

    // Return string (if applicable)
    std::string* return_string;

    // Return wide/mb string (if applicable)
    std::wstring* return_wstring;

    // Error message (if an error occurred)
    std::string* error_msg;

    // Boolean indicating if an error occurred
    bool error_occurred;

} JavaResultData;

/**
 * This struct holds data to do calls that need to be run in the plugin thread
 */
typedef struct plugin_thread_call
{
   // The plugin instance
   NPP instance;

   // The function to call
   void (*func) (void *);

   // The data to pass to the function
   void *userData;

} PluginThreadCall;

/**
 * Data structure passed to functions called in a new thread.
 */

typedef struct async_call_thread_data
{
    std::vector<void*> parameters;
    std::string result;
    bool result_ready;
    bool call_successful;
} AsyncCallThreadData;

/*
 * Misc. utility functions
 *
 * This class is never instantiated and should contain static functions only
 */

/* Function to process all pending async calls */
void processAsyncCallQueue(void*);

class IcedTeaPluginUtilities
{

    private:
        static int reference; /* Reference count */

        /* Mutex lock for updating reference count */
        static pthread_mutex_t reference_mutex;

        /* Map holding window pointer<->instance relationships */
        static std::map<void*, NPP>* instance_map;

        /* Map holding java-side-obj-key->NPObject relationship  */
        static std::map<std::string, NPObject*>* object_map;

        /* Posts a call in the async call queue */
        static bool postPluginThreadAsyncCall(NPP instance, void (*func) (void *), void* data);

    public:

    	/* Constructs message prefix with given context */
    	static void constructMessagePrefix(int context,
                                           std::string* result);

    	/* Constructs message prefix with given context and reference */
    	static void constructMessagePrefix(int context, int reference,
                                           std::string* result);

    	/* Constructs message prefix with given context, reference and src */
    	static void constructMessagePrefix(int context, int reference,
                                           std::string address,
                                           std::string* result);

    	/* Converts given pointer to a string representation */
    	static void JSIDToString(void* id, std::string* result);

    	/* Converts the given string representation to a pointer */
    	static void* stringToJSID(std::string id_str);
    	static void* stringToJSID(std::string* id_str);

    	/* Increments reference count and returns it */
    	static int getReference();

    	/* Decrements reference count */
    	static void releaseReference();

    	/* Converts the given integer to a string */
    	static void itoa(int i, std::string* result);

    	/* Copies a variant data type into a C++ string */
    	static std::string NPVariantAsString(NPVariant variant);

        /* This must be freed with browserfunctions.memfree */
        static NPString NPStringCopy(const std::string& result);

        /* This must be freed with browserfunctions.releasevariantvalue */
        static NPVariant NPVariantStringCopy(const std::string& result);

        /* Returns an std::string represented by the given identifier. */
        static std::string NPIdentifierAsString(NPIdentifier id);

    	/* Frees the given vector and the strings that its contents point to */
    	static void freeStringPtrVector(std::vector<std::string*>* v);

    	/* Splits the given string based on the delimiter provided */
    	static std::vector<std::string*>* strSplit(const char* str,
    			                                  const char* delim);

    	/* Converts given unicode integer byte array to UTF8 string  */
    	static void getUTF8String(int length, int begin,
    			std::vector<std::string*>* unicode_byte_array,
    			std::string* result_unicode_str);

    	/* Converts given UTF8 string to unicode integer byte array */
    	static void convertStringToUTF8(std::string* str,
    			                        std::string* utf_str);

    	/* Converts given unicode integer byte array to UTF16LE/UCS-2 string */
    	static void getUTF16LEString(int length, int begin,
    			std::vector<std::string*>* unicode_byte_array,
    			std::wstring* result_unicode_str);

    	/* Prints contents of given string vector */
    	static void printStringVector(const char* prefix, std::vector<std::string>* cv);

    	/* Prints contents of given string pointer vector */
    	static void printStringPtrVector(const char* prefix, std::vector<std::string*>* cv);

    	static std::string* variantToClassName(NPVariant variant);

    	static void printNPVariant(NPVariant variant);

        static void NPVariantToString(NPVariant variant, std::string* result);

        static bool javaResultToNPVariant(NPP instance,
                                          std::string* java_result,
                                          NPVariant* variant);

    	static const gchar* getSourceFromInstance(NPP instance);

    	static void storeInstanceID(void* member_ptr, NPP instance);

    	static void removeInstanceID(void* member_ptr);

    	/* Clear object_map. Useful for tests. */
    	static void clearInstanceIDs();

    	static NPP getInstanceFromMemberPtr(void* member_ptr);

    	static NPObject* getNPObjectFromJavaKey(std::string key);

    	static void storeObjectMapping(std::string key, NPObject* object);

    	static void removeObjectMapping(std::string key);

    	/* Clear object_map. Useful for tests. */
    	static void clearObjectMapping();

    	static void invalidateInstance(NPP instance);

    	static bool isObjectJSArray(NPP instance, NPObject* object);

    	static void decodeURL(const char* url, char** decoded_url);

    	/* Returns a vector of gchar* pointing to the elements of the vector string passed in*/
    	static std::vector<gchar*> vectorStringToVectorGchar(const std::vector<std::string>* stringVec);

    	/* Posts call in async queue and waits till execution completes */
    	static void callAndWaitForResult(NPP instance, void (*func) (void *), AsyncCallThreadData* data);

        /*cutting whitespaces from end and start of string*/
        static void trim(std::string& str);
        /*Unescape various escaped chars like \\ -> \ or \= -> =  or \: -> \*/
        static void unescape(std::string& str);
        static bool file_exists(std::string filename);
        static bool is_directory(std::string filename);
        //file-loggers helpers
        static std::string generateLogFileName();
        static void initFileLog();
        static void printDebugStatus();
        static std::string getTmpPath();
        static std::string getRuntimePath();
        static bool create_dir(std::string);
};

/*
 * A bus subscriber interface. Implementors must implement the newMessageOnBus
 * method.
 */
class BusSubscriber
{
    private:

    public:
    	BusSubscriber() {}

    	/* Notifies this subscriber that a new message as arrived */
        virtual bool newMessageOnBus(const char* message) = 0;
};

/*
 * This implementation is very simple and is therefore folded into this file
 * rather than a new one.
 */
class JavaMessageSender : public BusSubscriber
{
    private:
    public:

    	/* Sends given message to Java side */
        virtual bool newMessageOnBus(const char* message);
};

/*
 * Represents a message bus.
 * The bus can also have subscribers who are notified when a new message
 * arrives.
 */
class MessageBus
{
    private:
    	/* Mutex for locking the message queue */
    	pthread_mutex_t msg_queue_mutex;

    	/* Mutex used when adjusting subscriber list */
    	pthread_mutex_t subscriber_mutex;

    	/* Subscriber list */
        std::list<BusSubscriber*> subscribers;

        /* Queued messages */
        std::queue<char*> msgQueue;

    public:
    	MessageBus();

        ~MessageBus();

        /* subscribe to this bus */
        void subscribe(BusSubscriber* b);

        /* unsubscribe from this bus */
        void unSubscribe(BusSubscriber* b);

        /* Post a message on to the bus (it is safe to free the message pointer
           after this function returns) */
        void post(const char* message);
};



#endif // __ICEDTEAPLUGINUTILS_H__
