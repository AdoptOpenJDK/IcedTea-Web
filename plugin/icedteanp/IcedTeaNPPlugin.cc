/* IcedTeaNPPlugin.cc -- web browser plugin to execute Java applets
   Copyright (C) 2003, 2004, 2006, 2007  Free Software Foundation, Inc.
   Copyright (C) 2009, 2010 Red Hat

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
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

// System includes.
#include <dlfcn.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <errno.h>
#include <libgen.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <string>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <new>

//IcedTea-plugin includes
#include "IcedTeaPluginUtils.h"
#include "IcedTeaParseProperties.h"
// Liveconnect extension
#include "IcedTeaScriptablePluginObject.h"
#include "IcedTeaNPPlugin.h"


// Plugin information passed to about:plugins.
#define PLUGIN_FULL_NAME PLUGIN_NAME " (using " PLUGIN_VERSION ")"
#define PLUGIN_DESC "The <a href=\"" PACKAGE_URL "\">" PLUGIN_NAME "</a> executes Java applets."

#ifdef HAVE_JAVA9
 #define JPI_VERSION "1.9.0_" JDK_UPDATE_VERSION
 #define PLUGIN_APPLET_MIME_DESC \
  "application/x-java-applet;version=1.8:class,jar:IcedTea;"\
  "application/x-java-applet;version=1.9:class,jar:IcedTea;"
 #define PLUGIN_BEAN_MIME_DESC \
  "application/x-java-bean;version=1.8:class,jar:IcedTea;" \
  "application/x-java-bean;version=1.9:class,jar:IcedTea;"
#elif HAVE_JAVA8
 #define JPI_VERSION "1.8.0_" JDK_UPDATE_VERSION
 #define PLUGIN_APPLET_MIME_DESC \
  "application/x-java-applet;version=1.8:class,jar:IcedTea;"
 #define PLUGIN_BEAN_MIME_DESC \
  "application/x-java-bean;version=1.8:class,jar:IcedTea;"
#else
 #define JPI_VERSION "1.7.0_" JDK_UPDATE_VERSION
 #define PLUGIN_APPLET_MIME_DESC
 #define PLUGIN_BEAN_MIME_DESC
#endif

#define PLUGIN_MIME_DESC                                               \
  "application/x-java-vm:class,jar:IcedTea;"                           \
  "application/x-java-applet:class,jar:IcedTea;"                       \
  "application/x-java-applet;version=1.1:class,jar:IcedTea;"           \
  "application/x-java-applet;version=1.1.1:class,jar:IcedTea;"         \
  "application/x-java-applet;version=1.1.2:class,jar:IcedTea;"         \
  "application/x-java-applet;version=1.1.3:class,jar:IcedTea;"         \
  "application/x-java-applet;version=1.2:class,jar:IcedTea;"           \
  "application/x-java-applet;version=1.2.1:class,jar:IcedTea;"         \
  "application/x-java-applet;version=1.2.2:class,jar:IcedTea;"         \
  "application/x-java-applet;version=1.3:class,jar:IcedTea;"           \
  "application/x-java-applet;version=1.3.1:class,jar:IcedTea;"         \
  "application/x-java-applet;version=1.4:class,jar:IcedTea;"           \
  "application/x-java-applet;version=1.4.1:class,jar:IcedTea;"         \
  "application/x-java-applet;version=1.4.2:class,jar:IcedTea;"         \
  "application/x-java-applet;version=1.5:class,jar:IcedTea;"           \
  "application/x-java-applet;version=1.6:class,jar:IcedTea;"           \
  "application/x-java-applet;version=1.7:class,jar:IcedTea;"           \
  PLUGIN_APPLET_MIME_DESC \
  "application/x-java-applet;jpi-version=" JPI_VERSION ":class,jar:IcedTea;"  \
  "application/x-java-bean:class,jar:IcedTea;"                         \
  "application/x-java-bean;version=1.1:class,jar:IcedTea;"             \
  "application/x-java-bean;version=1.1.1:class,jar:IcedTea;"           \
  "application/x-java-bean;version=1.1.2:class,jar:IcedTea;"           \
  "application/x-java-bean;version=1.1.3:class,jar:IcedTea;"           \
  "application/x-java-bean;version=1.2:class,jar:IcedTea;"             \
  "application/x-java-bean;version=1.2.1:class,jar:IcedTea;"           \
  "application/x-java-bean;version=1.2.2:class,jar:IcedTea;"           \
  "application/x-java-bean;version=1.3:class,jar:IcedTea;"             \
  "application/x-java-bean;version=1.3.1:class,jar:IcedTea;"           \
  "application/x-java-bean;version=1.4:class,jar:IcedTea;"             \
  "application/x-java-bean;version=1.4.1:class,jar:IcedTea;"           \
  "application/x-java-bean;version=1.4.2:class,jar:IcedTea;"           \
  "application/x-java-bean;version=1.5:class,jar:IcedTea;"             \
  "application/x-java-bean;version=1.6:class,jar:IcedTea;"             \
  "application/x-java-bean;version=1.7:class,jar:IcedTea;"             \
  PLUGIN_BEAN_MIME_DESC \
  "application/x-java-bean;jpi-version=" JPI_VERSION ":class,jar:IcedTea;"    \
  "application/x-java-vm-npruntime::IcedTea;"

#define PLUGIN_URL NS_INLINE_PLUGIN_CONTRACTID_PREFIX NS_JVM_MIME_TYPE
#define PLUGIN_MIME_TYPE "application/x-java-vm"
#define PLUGIN_FILE_EXTS "class,jar,zip"
#define PLUGIN_MIME_COUNT 1

#define FAILURE_MESSAGE "icedteanp plugin error: Failed to run %s." \
  "  For more detail rerun \"firefox -g\" in a terminal window."

// Data directory for plugin.
static std::string data_directory;
static DIR *data_directory_descriptor;

// Fully-qualified appletviewer default  executable and rt.jar
static const char* appletviewer_default_executable = ICEDTEA_WEB_JRE "/bin/java";
static const char* appletviewer_default_rtjar = ICEDTEA_WEB_JRE "/lib/rt.jar";
//javaws name and binary
static const char* javaws_bin_property = "-Dicedtea-web.bin.location=" JAVAWS_BIN;
static const char* javaws_name_property = "-Dicedtea-web.bin.name=" JAVAWS_NAME;


// Applet viewer input channel (needs to be static because it is used in plugin_in_pipe_callback)
static GIOChannel* in_from_appletviewer = NULL;

// Applet viewer input pipe name.
gchar* in_pipe_name;

// Applet viewer input watch source.
gint in_watch_source;

// Applet viewer output pipe name.
gchar* out_pipe_name;

// Applet viewer debug pipe name.
gchar* debug_pipe_name = NULL;

// Applet viewer output watch source.
gint out_watch_source;

// Thread ID of plug-in thread
pthread_t itnp_plugin_thread_id;

// Mutex to lock async call queue
pthread_mutex_t pluginAsyncCallMutex;

/*to sync pipe to apletviewer console*/
pthread_mutex_t debug_pipe_lock = PTHREAD_MUTEX_INITIALIZER;

// Applet viewer output channel.
GIOChannel* out_to_appletviewer;

// Applet viewer debug channel.
GIOChannel* debug_to_appletviewer = NULL;

// Tracks jvm status
gboolean jvm_up = FALSE;

// Keeps track of initialization. NP_Initialize should only be
// called once.
gboolean initialized = false;

// browser functions into mozilla
NPNetscapeFuncs browser_functions;

// Various message buses carrying information to/from Java, and internally
MessageBus* plugin_to_java_bus;
MessageBus* java_to_plugin_bus;
//MessageBus* internal_bus = new MessageBus();

// Processor for plugin requests
PluginRequestProcessor* plugin_req_proc;

// Sends messages to Java over the bus
JavaMessageSender* java_req_proc;

// Queue processing threads
static pthread_t plugin_request_processor_thread1;
static pthread_t plugin_request_processor_thread2;
static pthread_t plugin_request_processor_thread3;

// Static instance helper functions.
// Retrieve the current document's documentbase.
static std::string plugin_get_documentbase (NPP instance);
// Callback used to monitor input pipe status.
static gboolean plugin_in_pipe_callback (GIOChannel* source,
                                         GIOCondition condition,
                                         gpointer plugin_data);
// Callback used to monitor output pipe status.
static gboolean plugin_out_pipe_callback (GIOChannel* source,
                                          GIOCondition condition,
                                          gpointer plugin_data);
std::string plugin_parameters_string (int argc, char* argn[], char* argv[]);
static void plugin_stop_appletviewer ();

NPError get_cookie_info(const char* siteAddr, char** cookieString, uint32_t* len);
NPError get_proxy_info(const char* siteAddr, char** proxy, uint32_t* len);
void consume_message(gchar* message);
static void appletviewer_monitor(GPid pid, gint status, gpointer data);
void plugin_send_initialization_message(char* instance, gulong handle,
                                               int width, int height,
                                               char* url);
/* Returns JVM options set in itw-settings */
std::vector<std::string*>* get_jvm_args();

// Global instance counter.
// Mutex to protect plugin_instance_counter.
static GMutex* plugin_instance_mutex = NULL;
// A global variable for reporting GLib errors.  This must be free'd
// and set to NULL after each use.
static GError* channel_error = NULL;

static GHashTable* instance_to_id_map = g_hash_table_new(NULL, NULL);
static GHashTable* id_to_instance_map = g_hash_table_new(NULL, NULL);
static gint instance_counter = 1;
static GPid appletviewer_pid = -1;
static guint appletviewer_watch_id = -1;

bool debug_initiated = false;
bool file_logs_initiated = false;
int plugin_debug = getenv ("ICEDTEAPLUGIN_DEBUG") != NULL;
bool plugin_debug_headers = false;
bool plugin_debug_to_file = false ;
bool plugin_debug_to_streams = true ;
bool plugin_debug_to_system = false;
bool plugin_debug_to_console = true;
FILE *  plugin_file_log = NULL;
std::string plugin_file_log_name;

int plugin_debug_suspend = (getenv("ICEDTEAPLUGIN_DEBUG") != NULL) &&
        (strcmp(getenv("ICEDTEAPLUGIN_DEBUG"), "suspend") == 0);


#ifdef LEGACY_GLIB
// Returns key from first item stored in hashtable
gboolean
find_first_item_in_hash_table(gpointer key, gpointer value, gpointer user_data)
{
    user_data = key;
    return (gboolean)TRUE;
}

int
g_strcmp0(char *str1, char *str2)
{
   if (str1 != NULL)
     return str2 != NULL ? strcmp(str1, str2) : 1;
   else // str1 == NULL
     return str2 != NULL ? 1 : 0;
}


#endif

static std::string get_plugin_executable(){
      std::string custom_jre;
      bool custom_jre_defined = find_custom_jre(custom_jre);
      if (custom_jre_defined) {
            if (IcedTeaPluginUtilities::file_exists(custom_jre+"/bin/java")){
                  return custom_jre+"/bin/java";
            } else {
                 PLUGIN_ERROR("Your custom jre (/bin/java check) %s is not valid. Please fix %s in your %s. In attempt to run using default one. \n", custom_jre.c_str(), custom_jre_key.c_str(), default_file_ITW_deploy_props_name.c_str());
            }
      }
      return appletviewer_default_executable;      
}

static std::string get_plugin_rt_jar(){
      std::string custom_jre;
      bool custom_jre_defined = find_custom_jre(custom_jre);
      if (custom_jre_defined) {
            if (IcedTeaPluginUtilities::file_exists(custom_jre+"/lib/rt.jar")){
                  return custom_jre+"/lib/rt.jar";
            } else {
                  PLUGIN_ERROR("Your custom jre (/lib/rt.jar check) %s is not valid. Please fix %s in your %s. In attempt to run using default one. \n", custom_jre.c_str(), custom_jre_key.c_str(), default_file_ITW_deploy_props_name.c_str());
            }
      }
      return appletviewer_default_rtjar;      
}

static void cleanUpDir(){
  //free data_directory descriptor 
  if (data_directory_descriptor != NULL) {
    closedir(data_directory_descriptor);
  }
  //clean up pipes directory
  PLUGIN_DEBUG ("Removing runtime directory %s \n", data_directory.c_str());
  int removed = rmdir(data_directory.c_str());
  if (removed != 0) {
    PLUGIN_ERROR ("Failed to remove runtime directory %s, because of  %s \n", data_directory.c_str(), strerror(errno));
  } else {
    PLUGIN_DEBUG ("Removed runtime directory %s \n", data_directory.c_str());
  }
  data_directory_descriptor = NULL;
}
/* 
 * Find first member in GHashTable* depending on version of glib
 */
gpointer getFirstInTableInstance(GHashTable* table)
{
      gpointer id, instance;
      #ifndef LEGACY_GLIB
        GHashTableIter iter;
        g_hash_table_iter_init (&iter, table);
        g_hash_table_iter_next (&iter, &instance, &id);
      #else
        g_hash_table_find(table, (GHRFunc)find_first_item_in_hash_table, &instance);
      #endif
        return instance;
}

// Functions prefixed by ITNP_ are instance functions.  They are called
// by the browser and operate on instances of ITNPPluginData.
// Functions prefixed by plugin_ are static helper functions.
// Functions prefixed by NP_ are factory functions.  They are called
// by the browser and provide functionality needed to create plugin
// instances.

// INSTANCE FUNCTIONS

// Creates a new icedtea np plugin instance.  This function creates a
// ITNPPluginData* and stores it in instance->pdata.  The following
// ITNPPluginData fields are initialized: instance_id, in_pipe_name,
// in_from_appletviewer, in_watch_source, out_pipe_name,
// out_to_appletviewer, out_watch_source, appletviewer_mutex, owner,
// appletviewer_alive.  In addition two pipe files are created.  All
// of those fields must be properly destroyed, and the pipes deleted,
// by ITNP_Destroy.  If an error occurs during initialization then this
// function will free anything that's been allocated so far, set
// instance->pdata to NULL and return an error code.
NPError
ITNP_New (NPMIMEType pluginType, NPP instance, uint16_t mode,
         int16_t argc, char* argn[], char* argv[],
         NPSavedData* saved)
{
  PLUGIN_DEBUG("ITNP_New\n");

  static NPObject *window_ptr;
  NPIdentifier identifier;
  NPVariant member_ptr;
  browser_functions.getvalue(instance, NPNVWindowNPObject, &window_ptr);
  identifier = browser_functions.getstringidentifier("document");
  if (!browser_functions.hasproperty(instance, window_ptr, identifier))
  {
	PLUGIN_ERROR("%s not found!\n", "document");
  }
  browser_functions.getproperty(instance, window_ptr, identifier, &member_ptr);

  PLUGIN_DEBUG("Got variant %p\n", &member_ptr);

  if (!instance)
  {
      PLUGIN_ERROR ("Browser-provided instance pointer is NULL.\n");
      return NPERR_INVALID_INSTANCE_ERROR;
  }

  // data
  ITNPPluginData* data = plugin_data_new ();
  if (data == NULL)
  {
      PLUGIN_ERROR ("Failed to allocate plugin data.\n");
      return NPERR_OUT_OF_MEMORY_ERROR;
    }

  // start the jvm if needed
   NPError startup_error = start_jvm_if_needed();
   if (startup_error != NPERR_NO_ERROR) {
	   PLUGIN_ERROR ("Failed to start JVM\n");
	   return startup_error;
   }

  // Initialize data->instance_id.
  //
  // instance_id should be unique for this process so we use a
  // combination of getpid and plugin_instance_counter.
  //
  // Critical region.  Reference and increment plugin_instance_counter
  // global.
  g_mutex_lock (plugin_instance_mutex);

  // data->instance_id
  data->instance_id = g_strdup_printf ("%d",
                                           instance_counter);

  g_mutex_unlock (plugin_instance_mutex);

  // data->appletviewer_mutex
  data->appletviewer_mutex = g_mutex_new ();

  g_mutex_lock (data->appletviewer_mutex);

  std::string documentbase = plugin_get_documentbase (instance);
  // Documentbase retrieval.
  if (argc != 0)
  {
      // Send parameters to appletviewer.
      std::string params_string = plugin_parameters_string(argc, argn, argv);

      data->parameters_string =  g_strdup_printf("tag %s %s", documentbase.c_str(), params_string.c_str());

      data->is_applet_instance = true;
  }
  else
  {
      data->is_applet_instance = false;
  }

  g_mutex_unlock (data->appletviewer_mutex);

  // If initialization succeeded entirely then we store the plugin
  // data in the instance structure and return.  Otherwise we free the
  // data we've allocated so far and set instance->pdata to NULL.

  // Set back-pointer to owner instance.
  data->owner = instance;

  // source of this instance
  // don't use documentbase, it is cleared later
  data->source = plugin_get_documentbase(instance);

  instance->pdata = data;

  // store an identifier for this plugin
  PLUGIN_DEBUG("Mapping id %d and instance %p\n", instance_counter, instance);
  g_hash_table_insert(instance_to_id_map, instance, GINT_TO_POINTER(instance_counter));
  g_hash_table_insert(id_to_instance_map, GINT_TO_POINTER(instance_counter), instance);
  instance_counter++;

  PLUGIN_DEBUG ("ITNP_New return\n");

  return NPERR_NO_ERROR;
}

// Starts the JVM if it is not already running
NPError start_jvm_if_needed()
{

  // This is asynchronized function. It must
  // have exclusivity when running.

  GMutex *vm_start_mutex = g_mutex_new();
  g_mutex_lock(vm_start_mutex);

  PLUGIN_DEBUG("Checking JVM status...\n");

  // If the jvm is already up, do nothing
  if (jvm_up)
  {
      PLUGIN_DEBUG("JVM is up. Returning.\n");
      return  NPERR_NO_ERROR;
  }

  PLUGIN_DEBUG("No JVM is running. Attempting to start one...\n");

  NPError np_error = NPERR_NO_ERROR;
  ITNPPluginData* data = NULL;

  // Create appletviewer-to-plugin pipe which we refer to as the input
  // pipe.

  // in_pipe_name
  in_pipe_name = g_strdup_printf ("%s/%d-icedteanp-appletviewer-to-plugin",
                                         data_directory.c_str(), getpid());
  if (!in_pipe_name)
    {
      PLUGIN_ERROR ("Failed to create input pipe name.\n");
      np_error = NPERR_OUT_OF_MEMORY_ERROR;
      // If in_pipe_name is NULL then the g_free at
      // cleanup_in_pipe_name will simply return.
      goto cleanup_in_pipe_name;
    }

  // clean up any older pip
  unlink (in_pipe_name);

  PLUGIN_DEBUG ("ITNP_New: creating input fifo: %s\n", in_pipe_name);
  if (mkfifo (in_pipe_name, 0600) == -1 && errno != EEXIST)
    {
      PLUGIN_ERROR ("Failed to create input pipe\n", strerror (errno));
      np_error = NPERR_GENERIC_ERROR;
      goto cleanup_in_pipe_name;
    }
  PLUGIN_DEBUG ("ITNP_New: created input fifo: %s\n", in_pipe_name);

  // Create plugin-to-appletviewer pipe which we refer to as the
  // output pipe.

  // out_pipe_name
  out_pipe_name = g_strdup_printf ("%s/%d-icedteanp-plugin-to-appletviewer",
                                         data_directory.c_str(), getpid());

  if (!out_pipe_name)
    {
      PLUGIN_ERROR ("Failed to create output pipe name.\n");
      np_error = NPERR_OUT_OF_MEMORY_ERROR;
      goto cleanup_out_pipe_name;
    }

  // clean up any older pip
  unlink (out_pipe_name);

  PLUGIN_DEBUG ("ITNP_New: creating output fifo: %s\n", out_pipe_name);
  if (mkfifo (out_pipe_name, 0600) == -1 && errno != EEXIST)
    {
      PLUGIN_ERROR ("Failed to create output pipe\n", strerror (errno));
      np_error = NPERR_GENERIC_ERROR;
      goto cleanup_out_pipe_name;
    }
  PLUGIN_DEBUG ("ITNP_New: created output fifo: %s\n", out_pipe_name);

  // Create plugin-debug-to-appletviewer pipe which we refer to as the
  // debug pipe.
  initialize_debug();//should be already initialized, but...
  if (plugin_debug_to_console){
    // debug_pipe_name
    debug_pipe_name = g_strdup_printf ("%s/%d-icedteanp-plugin-debug-to-appletviewer",
                                         data_directory.c_str(), getpid());

    if (!debug_pipe_name)
      {
        PLUGIN_ERROR ("Failed to create debug pipe name.\n");
        np_error = NPERR_OUT_OF_MEMORY_ERROR;
        goto cleanup_debug_pipe_name;
      }

    // clean up any older pip
    unlink (debug_pipe_name);

    PLUGIN_DEBUG ("ITNP_New: creating debug fifo: %s\n", debug_pipe_name);
    if (mkfifo (debug_pipe_name, 0600) == -1 && errno != EEXIST)
      {
        PLUGIN_ERROR ("Failed to create debug pipe\n", strerror (errno));
        np_error = NPERR_GENERIC_ERROR;
        goto cleanup_debug_pipe_name;
      }
    PLUGIN_DEBUG ("ITNP_New: created debug fifo: %s\n", debug_pipe_name);
  }

  // Start a separate appletviewer process for each applet, even if
  // there are multiple applets in the same page.  We may need to
  // change this behaviour if we find pages with multiple applets that
  // rely on being run in the same VM.

  np_error = plugin_start_appletviewer (data);

  // Create plugin-to-appletviewer channel.  The default encoding for
  // the file is UTF-8.
  // out_to_appletviewer
  out_to_appletviewer = g_io_channel_new_file (out_pipe_name,
                                               "w", &channel_error);
  if (!out_to_appletviewer)
    {
      if (channel_error)
        {
          PLUGIN_ERROR ("Failed to create output channel, '%s'\n",
                            channel_error->message);
          g_error_free (channel_error);
          channel_error = NULL;
        }
      else
        PLUGIN_ERROR ("Failed to create output channel\n");

      np_error = NPERR_GENERIC_ERROR;
      goto cleanup_out_to_appletviewer;
    }

  // Watch for hangup and error signals on the output pipe.
  out_watch_source =
    g_io_add_watch (out_to_appletviewer,
                    (GIOCondition) (G_IO_ERR | G_IO_HUP),
                    plugin_out_pipe_callback, (gpointer) out_to_appletviewer);

  // Create appletviewer-to-plugin channel.  The default encoding for
  // the file is UTF-8.
  // in_from_appletviewer
  in_from_appletviewer = g_io_channel_new_file (in_pipe_name,
                                                      "r", &channel_error);
  if (!in_from_appletviewer)
    {
      if (channel_error)
        {
          PLUGIN_ERROR ("Failed to create input channel, '%s'\n",
                            channel_error->message);
          g_error_free (channel_error);
          channel_error = NULL;
        }
      else
        PLUGIN_ERROR ("Failed to create input channel\n");

      np_error = NPERR_GENERIC_ERROR;
      goto cleanup_in_from_appletviewer;
    }

  // Watch for hangup and error signals on the input pipe.
  in_watch_source =
    g_io_add_watch (in_from_appletviewer,
                    (GIOCondition) (G_IO_IN | G_IO_ERR | G_IO_HUP),
                    plugin_in_pipe_callback, (gpointer) in_from_appletviewer);
                    
  // Create plugin-to-appletviewer console debug channel.  The default encoding for
  // the file is UTF-8.
  // debug_to_appletviewer
  if (plugin_debug_to_console){
    debug_to_appletviewer = g_io_channel_new_file (debug_pipe_name,
                                                 "w", &channel_error);
    if (!debug_to_appletviewer)
      {
        if (channel_error)
          {
            PLUGIN_ERROR ("Failed to debug output channel, '%s'\n",
                              channel_error->message);
            g_error_free (channel_error);
            channel_error = NULL;
          }
        else
          PLUGIN_ERROR ("Failed to create debug channel\n");

        np_error = NPERR_GENERIC_ERROR;
        goto cleanup_debug_to_appletviewer;
     }
  }

  jvm_up = TRUE;
  
  if (plugin_debug_to_console){
    //jvm is up, we can start console producer thread
    pthread_t debug_to_console_consumer;
    pthread_create(&debug_to_console_consumer,NULL,&flush_pre_init_messages,NULL);
  }
  goto done;

  // Free allocated data in case of error
 cleanup_debug_to_appletviewer:
  if (plugin_debug_to_console){
    if (debug_to_appletviewer)
      g_io_channel_unref (debug_to_appletviewer);
    debug_to_appletviewer = NULL;
  }

 cleanup_in_watch_source:
  // Removing a source is harmless if it fails since it just means the
  // source has already been removed.
  g_source_remove (in_watch_source);
  in_watch_source = 0;

 cleanup_in_from_appletviewer:
  if (in_from_appletviewer)
    g_io_channel_unref (in_from_appletviewer);
  in_from_appletviewer = NULL;

  // cleanup_out_watch_source:
  g_source_remove (out_watch_source);
  out_watch_source = 0;

 cleanup_out_to_appletviewer:
  if (out_to_appletviewer)
    g_io_channel_unref (out_to_appletviewer);
  out_to_appletviewer = NULL;

  if (plugin_debug_to_console){
    // cleanup_debug_pipe:
    // Delete output pipe.
    PLUGIN_DEBUG ("ITNP_New: deleting debug fifo: %s\n", debug_pipe_name);
    unlink (debug_pipe_name);
    PLUGIN_DEBUG ("ITNP_New: deleted debug fifo: %s\n", debug_pipe_name);
  }
 cleanup_debug_pipe_name:
  if (plugin_debug_to_console){
    g_free (debug_pipe_name);
    debug_pipe_name = NULL;
  }



  // cleanup_out_pipe:
  // Delete output pipe.
  PLUGIN_DEBUG ("ITNP_New: deleting input fifo: %s\n", in_pipe_name);
  unlink (out_pipe_name);
  PLUGIN_DEBUG ("ITNP_New: deleted input fifo: %s\n", in_pipe_name);

 cleanup_out_pipe_name:
  g_free (out_pipe_name);
  out_pipe_name = NULL;

  // cleanup_in_pipe:
  // Delete input pipe.
  PLUGIN_DEBUG ("ITNP_New: deleting output fifo: %s\n", out_pipe_name);
  unlink (in_pipe_name);
  PLUGIN_DEBUG ("ITNP_New: deleted output fifo: %s\n", out_pipe_name);

 cleanup_in_pipe_name:
  g_free (in_pipe_name);
  in_pipe_name = NULL;

  cleanUpDir();
 done:

  IcedTeaPluginUtilities::printDebugStatus();
  // Now other threads may re-enter.. unlock the mutex
  g_mutex_unlock(vm_start_mutex);
  return np_error;

}

NPError
ITNP_GetValue (NPP instance, NPPVariable variable, void* value)
{
  PLUGIN_DEBUG ("ITNP_GetValue\n");

  NPError np_error = NPERR_NO_ERROR;

  switch (variable)
    {
    // This plugin needs XEmbed support.
    case NPPVpluginNeedsXEmbed:
      {
        PLUGIN_DEBUG ("ITNP_GetValue: returning TRUE for NeedsXEmbed.\n");
        bool* bool_value = (bool*) value;
        *bool_value = true;
      }
      break;
    case NPPVpluginScriptableNPObject:
      {
         *(NPObject **)value = get_scriptable_object(instance);
      }
      break;
    default:
      PLUGIN_ERROR ("Unknown plugin value requested.\n");
      np_error = NPERR_GENERIC_ERROR;
      break;
    }

  PLUGIN_DEBUG ("ITNP_GetValue return\n");

  return np_error;
}

NPError
ITNP_Destroy (NPP instance, NPSavedData** save)
{
  PLUGIN_DEBUG ("ITNP_Destroy %p\n", instance);

  ITNPPluginData* data = (ITNPPluginData*) instance->pdata;

  int id = get_id_from_instance(instance);

  // Let Java know that this applet needs to be destroyed
  gchar* msg = (gchar*) g_malloc(512*sizeof(gchar)); // 512 is more than enough. We need < 100
  g_sprintf(msg, "instance %d destroy", id);
  plugin_send_message_to_appletviewer(msg);
  g_free(msg);
  msg = NULL;

  if (data)
    {
      // Free plugin data.
      plugin_data_destroy (instance);
    }

  g_hash_table_remove(instance_to_id_map, instance);
  g_hash_table_remove(id_to_instance_map, GINT_TO_POINTER(id));

  IcedTeaPluginUtilities::invalidateInstance(instance);

  PLUGIN_DEBUG ("ITNP_Destroy return\n");

  return NPERR_NO_ERROR;
}

NPError
ITNP_SetWindow (NPP instance, NPWindow* window)
{
  PLUGIN_DEBUG ("ITNP_SetWindow\n");

  if (instance == NULL)
    {
      PLUGIN_ERROR ("Invalid instance.\n");

      return NPERR_INVALID_INSTANCE_ERROR;
    }

  gpointer id_ptr = g_hash_table_lookup(instance_to_id_map, instance);

  gint id = 0;
  if (id_ptr)
    {
      id = GPOINTER_TO_INT(id_ptr);
    }

  ITNPPluginData* data = (ITNPPluginData*) instance->pdata;

  // Simply return if we receive a NULL window.
  if ((window == NULL) || (window->window == NULL))
    {
      PLUGIN_DEBUG ("ITNP_SetWindow: got NULL window.\n");

      return NPERR_NO_ERROR;
    }

  if (data->window_handle)
    {
      // The window already exists.
      if (data->window_handle == window->window)
    {
          // The parent window is the same as in previous calls.
          PLUGIN_DEBUG ("ITNP_SetWindow: window already exists.\n");

          // Critical region.  Read data->appletviewer_mutex and send
          // a message to the appletviewer.
          g_mutex_lock (data->appletviewer_mutex);

      if (jvm_up)
        {
          gboolean dim_changed = FALSE;

          // The window is the same as it was for the last
          // SetWindow call.
          if (window->width != data->window_width)
        {
                  PLUGIN_DEBUG ("ITNP_SetWindow: window width changed.\n");
          // The width of the plugin window has changed.

                  // Store the new width.
                  data->window_width = window->width;
                  dim_changed = TRUE;
        }

          if (window->height != data->window_height)
        {
                  PLUGIN_DEBUG ("ITNP_SetWindow: window height changed.\n");
          // The height of the plugin window has changed.

                  // Store the new height.
                  data->window_height = window->height;

                  dim_changed = TRUE;
        }

        if (dim_changed) {
            gchar* message = g_strdup_printf ("instance %d width %d height %d",
                                                id, window->width, window->height);
            plugin_send_message_to_appletviewer (message);
            g_free (message);
            message = NULL;
        }


        }
      else
        {
              // The appletviewer is not running.
          PLUGIN_DEBUG ("ITNP_SetWindow: appletviewer is not running.\n");
        }

          g_mutex_unlock (data->appletviewer_mutex);
    }
      else
    {
      // The parent window has changed.  This branch does run but
      // doing nothing in response seems to be sufficient.
      PLUGIN_DEBUG ("ITNP_SetWindow: parent window changed.\n");
    }
    }
  else
    {

	  // Else this is initialization
      PLUGIN_DEBUG ("ITNP_SetWindow: setting window.\n");

      // Critical region.  Send messages to appletviewer.
      g_mutex_lock (data->appletviewer_mutex);

      // Store the window handle and dimensions
      data->window_handle = window->window;
      data->window_width = window->width;
      data->window_height = window->height;

      // Now we have everything. Send this data to the Java side
      plugin_send_initialization_message(
    		  data->instance_id, (gulong) data->window_handle,
    		  data->window_width, data->window_height, data->parameters_string);

      g_mutex_unlock (data->appletviewer_mutex);

    }

  PLUGIN_DEBUG ("ITNP_SetWindow return\n");

  return NPERR_NO_ERROR;
}

NPError
ITNP_NewStream (NPP instance, NPMIMEType type, NPStream* stream,
               NPBool seekable, uint16_t* stype)
{
  PLUGIN_DEBUG ("ITNP_NewStream\n");

  PLUGIN_DEBUG ("ITNP_NewStream return\n");

  return NPERR_GENERIC_ERROR;
}

void
ITNP_StreamAsFile (NPP instance, NPStream* stream, const char* filename)
{
  PLUGIN_DEBUG ("ITNP_StreamAsFile\n");

  PLUGIN_DEBUG ("ITNP_StreamAsFile return\n");
}

NPError
ITNP_DestroyStream (NPP instance, NPStream* stream, NPReason reason)
{
  PLUGIN_DEBUG ("ITNP_DestroyStream\n");

  PLUGIN_DEBUG ("ITNP_DestroyStream return\n");

  return NPERR_NO_ERROR;
}

int32_t
ITNP_WriteReady (NPP instance, NPStream* stream)
{
  PLUGIN_DEBUG ("ITNP_WriteReady\n");

  PLUGIN_DEBUG ("ITNP_WriteReady return\n");

  return 0;
}

int32_t
ITNP_Write (NPP instance, NPStream* stream, int32_t offset, int32_t len,
           void* buffer)
{
  PLUGIN_DEBUG ("ITNP_Write\n");

  PLUGIN_DEBUG ("ITNP_Write return\n");

  return 0;
}

void
ITNP_Print (NPP instance, NPPrint* platformPrint)
{
  PLUGIN_DEBUG ("ITNP_Print\n");

  PLUGIN_DEBUG ("ITNP_Print return\n");
}

int16_t
ITNP_HandleEvent (NPP instance, void* event)
{
  PLUGIN_DEBUG ("ITNP_HandleEvent\n");

  PLUGIN_DEBUG ("ITNP_HandleEvent return\n");

  return 0;
}

void
ITNP_URLNotify (NPP instance, const char* url, NPReason reason,
               void* notifyData)
{
  PLUGIN_DEBUG ("ITNP_URLNotify\n");

  PLUGIN_DEBUG ("ITNP_URLNotify return\n");
}

NPError
get_cookie_info(const char* siteAddr, char** cookieString, uint32_t* len)
{
  // Only attempt to perform this operation if there is a valid plugin instance
  if (g_hash_table_size(instance_to_id_map) <= 0)
  {
    return NPERR_GENERIC_ERROR;
  }
  // getvalueforurl needs an NPP instance. Quite frankly, there is no easy way
  // to know which instance needs the information, as applets on Java side can
  // be multi-threaded and the thread making a proxy.cookie request cannot be
  // easily tracked.

  // Fortunately, XULRunner does not care about the instance as long as it is
  // valid. So we just pick the first valid one and use it. Proxy/Cookie
  // information is not instance specific anyway, it is URL specific.

  if (browser_functions.getvalueforurl)
  {
      gpointer instance=getFirstInTableInstance(instance_to_id_map);
      return browser_functions.getvalueforurl((NPP) instance, NPNURLVCookie, siteAddr, cookieString, len);
  } else
  {
      return NPERR_GENERIC_ERROR;
  }

  return NPERR_NO_ERROR;
}

static NPError
set_cookie_info(const char* siteAddr, const char* cookieString, uint32_t len)
{
  // Only attempt to perform this operation if there is a valid plugin instance
  if (g_hash_table_size(instance_to_id_map) > 0 && browser_functions.getvalueforurl)
  {
      // We arbitrarily use the first valid instance we can grab
      // For an explanation of the logic behind this, see get_cookie_info
      gpointer instance = getFirstInTableInstance(instance_to_id_map);
      return browser_functions.setvalueforurl((NPP) instance, NPNURLVCookie, siteAddr, cookieString, len);
  }

  return NPERR_GENERIC_ERROR;;
}

// HELPER FUNCTIONS

ITNPPluginData*
plugin_data_new ()
{
  PLUGIN_DEBUG ("plugin_data_new\n");

  ITNPPluginData* data = (ITNPPluginData*)browser_functions.memalloc(sizeof (struct ITNPPluginData));

  if (data)
  {
      // Call constructor on allocated data
      new (data) ITNPPluginData();
  }
  PLUGIN_DEBUG ("plugin_data_new return\n");

  return data;
}



// Documentbase retrieval.  This function gets the current document's
// documentbase.  This function relies on browser-private data so it
// will only work when the plugin is loaded in a Mozilla-based
// browser.
static std::string
plugin_get_documentbase (NPP instance)
{
  PLUGIN_DEBUG ("plugin_get_documentbase\n");

  // FIXME: This method is not ideal, but there are no known NPAPI call
  // for this. See thread for more information:
  // http://www.mail-archive.com/chromium-dev@googlegroups.com/msg04844.html

  // Additionally, since it is insecure, we cannot use it for making
  // security decisions.
  NPObject* window;
  browser_functions.getvalue(instance, NPNVWindowNPObject, &window);

  NPVariant location;
  NPIdentifier location_id = browser_functions.getstringidentifier("location");
  browser_functions.getproperty(instance, window, location_id, &location);

  NPVariant href;
  NPIdentifier href_id = browser_functions.getstringidentifier("href");
  browser_functions.getproperty(instance, NPVARIANT_TO_OBJECT(location), 
                               href_id, &href);

  std::string href_str = IcedTeaPluginUtilities::NPVariantAsString(href);

  // Release references.
  browser_functions.releasevariantvalue(&href);
  browser_functions.releasevariantvalue(&location);

  PLUGIN_DEBUG ("plugin_get_documentbase return\n");
  PLUGIN_DEBUG("plugin_get_documentbase returning: %s\n", href_str.c_str());

  return href_str;
}

// plugin_in_pipe_callback is called when data is available on the
// input pipe, or when the appletviewer crashes or is killed.  It may
// be called after data has been destroyed in which case it simply
// returns FALSE to remove itself from the glib main loop.
static gboolean
plugin_in_pipe_callback (GIOChannel* source,
                         GIOCondition condition,
                         gpointer plugin_data)
{
  PLUGIN_DEBUG ("plugin_in_pipe_callback\n");

  gboolean keep_installed = TRUE;

  if (condition & G_IO_IN)
    {
      gchar* message = NULL;

      if (g_io_channel_read_line (in_from_appletviewer,
                                  &message, NULL, NULL,
                                  &channel_error)
          != G_IO_STATUS_NORMAL)
        {
          if (channel_error)
            {
              PLUGIN_ERROR ("Failed to read line from input channel, %s\n",
                                channel_error->message);
              g_error_free (channel_error);
              channel_error = NULL;
            }
          else
            PLUGIN_ERROR ("Failed to read line from input channel\n");
        } else
        {
          consume_message(message);
        }

      g_free (message);
      message = NULL;

      keep_installed = TRUE;
    }

  if (condition & (G_IO_ERR | G_IO_HUP))
    {
      PLUGIN_DEBUG ("appletviewer has stopped.\n");
      keep_installed = FALSE;
    }

  PLUGIN_DEBUG ("plugin_in_pipe_callback return\n");

  return keep_installed;
}

static
void consume_plugin_message(gchar* message) {
  // internal plugin related message
  gchar** parts = g_strsplit (message, " ", 5);
  if (g_str_has_prefix(parts[1], "PluginProxyInfo"))
  {
    gchar* proxy = NULL;
    uint32_t len = 0;

    gchar* decoded_url = (gchar*) calloc(strlen(parts[4]) + 1, sizeof(gchar));
    IcedTeaPluginUtilities::decodeURL(parts[4], &decoded_url);
    PLUGIN_DEBUG("parts[0]=%s, parts[1]=%s, reference, parts[3]=%s, parts[4]=%s -- decoded_url=%s\n", parts[0], parts[1], parts[3], parts[4], decoded_url);

    gchar* proxy_info = NULL;

    proxy_info = g_strconcat ("plugin PluginProxyInfo reference ", parts[3], " ", NULL);
    if (get_proxy_info(decoded_url, &proxy, &len) == NPERR_NO_ERROR)
      {
        proxy_info = g_strconcat (proxy_info, proxy, NULL);
      }

    PLUGIN_DEBUG("Proxy info: %s\n", proxy_info);
    plugin_send_message_to_appletviewer(proxy_info);

    free(decoded_url);
    decoded_url = NULL;
    g_free(proxy_info);
    proxy_info = NULL;

    g_free(proxy);
    proxy = NULL;

  } else if (g_str_has_prefix(parts[1], "PluginCookieInfo"))
  {
    gchar* decoded_url = (gchar*) calloc(strlen(parts[4])+1, sizeof(gchar));
    IcedTeaPluginUtilities::decodeURL(parts[4], &decoded_url);

    gchar* cookie_info = g_strconcat ("plugin PluginCookieInfo reference ", parts[3], " ", NULL);
    gchar* cookie_string = NULL;
    uint32_t len;
    if (get_cookie_info(decoded_url, &cookie_string, &len) == NPERR_NO_ERROR)
    {
        cookie_info = g_strconcat (cookie_info, cookie_string, NULL);
    }

    PLUGIN_DEBUG("Cookie info: %s\n", cookie_info);
    plugin_send_message_to_appletviewer(cookie_info);

    free(decoded_url);
    decoded_url = NULL;
    g_free(cookie_info);
    cookie_info = NULL;
    g_free(cookie_string);
    cookie_string = NULL;
  } else if (g_str_has_prefix(parts[1], "PluginSetCookie"))
  {
    // Message structure: plugin PluginSetCookie reference -1 <url> <cookie>
    gchar** cookie_parts = g_strsplit (message, " ", 6);

    if (g_strv_length(cookie_parts) < 6)
    {
       g_strfreev (parts);
       g_strfreev (cookie_parts);
       return; // Defensive, message _should_ be properly formatted
    }

    gchar* decoded_url = (gchar*) calloc(strlen(cookie_parts[4])+1, sizeof(gchar));
    IcedTeaPluginUtilities::decodeURL(cookie_parts[4], &decoded_url);

    gchar* cookie_string = cookie_parts[5];
    uint32_t len = strlen(cookie_string);
    if (set_cookie_info(decoded_url, cookie_string, len) == NPERR_NO_ERROR)
    {
  	  PLUGIN_DEBUG("Setting cookie for URL %s to %s\n", decoded_url, cookie_string);
    } else
    {
  	  PLUGIN_DEBUG("Not able to set cookie for URL %s to %s\n", decoded_url, cookie_string);
    }

    free(decoded_url);
    decoded_url = NULL;
    g_strfreev (cookie_parts);
    cookie_parts = NULL;
  }

  g_strfreev (parts);
  parts = NULL;
}

void consume_message(gchar* message) {

	PLUGIN_DEBUG ("  PIPE: plugin read: %s\n", message);

  if (g_str_has_prefix (message, "instance"))
    {

	  ITNPPluginData* data;
      gchar** parts = g_strsplit (message, " ", -1);
      guint parts_sz = g_strv_length (parts);

      int instance_id = atoi(parts[1]);
      NPP instance = (NPP) g_hash_table_lookup(id_to_instance_map,
                                         GINT_TO_POINTER(instance_id));

      if (instance_id > 0 && !instance)
        {
          PLUGIN_DEBUG("Instance %d is not active. Refusing to consume message \"%s\"\n", instance_id, message);
          return;
        }
      else if (instance)
        {
           data = (ITNPPluginData*) instance->pdata;
        }

      if (g_str_has_prefix (parts[2], "status"))
        {

          // clear the "instance X status" parts
          strcpy(parts[0], "");
          strcpy(parts[1], "");
          strcpy(parts[2], "");

          // join the rest
          gchar* status_message = g_strjoinv(" ", parts);
          PLUGIN_DEBUG ("plugin_in_pipe_callback: setting status %s\n", status_message);
          (*browser_functions.status) (data->owner, status_message);

          g_free(status_message);
          status_message = NULL;
        }
      else if (g_str_has_prefix (parts[1], "internal"))
    	{
    	  //s->post(message);
    	}
      else
        {
          // All other messages are posted to the bus, and subscribers are
          // expected to take care of them. They better!

    	  java_to_plugin_bus->post(message);
        }

        g_strfreev (parts);
        parts = NULL;
    }
  else if (g_str_has_prefix (message, "context"))
    {
	      java_to_plugin_bus->post(message);
    }
  else if (g_str_has_prefix (message, "plugin "))
    {
        consume_plugin_message(message);
    }
  else
    {
        g_print ("  Unable to handle message: %s\n", message);
    }

}

void get_instance_from_id(int id, NPP& instance)
{
    instance = (NPP) g_hash_table_lookup(id_to_instance_map,
                                       GINT_TO_POINTER(id));
}

int get_id_from_instance(NPP instance)
{
    int id = GPOINTER_TO_INT(g_hash_table_lookup(instance_to_id_map,
                                                       instance));
    PLUGIN_DEBUG("Returning id %d for instance %p\n", id, instance);
    return id;
}

NPError
get_proxy_info(const char* siteAddr, char** proxy, uint32_t* len)
{
  // Only attempt to perform this operation if there is a valid plugin instance
  if (g_hash_table_size(instance_to_id_map) <= 0)
  {
	  return NPERR_GENERIC_ERROR;
  }
  if (browser_functions.getvalueforurl)
  {
      NPError err;
      // As in get_cookie_info, we use the first active instance
      gpointer instance=getFirstInTableInstance(instance_to_id_map);
      err = browser_functions.getvalueforurl((NPP) instance, NPNURLVProxy, siteAddr, proxy, len);

      if (err != NPERR_NO_ERROR) 
      {
        *proxy = (char *) malloc(sizeof **proxy * 7);
        *len = g_strlcpy(*proxy, "DIRECT", 7);
      }
  } else
  {
      return NPERR_GENERIC_ERROR;
  }

  return NPERR_NO_ERROR;
}

// plugin_out_pipe_callback is called when the appletviewer crashes or
// is killed.  It may be called after data has been destroyed in which
// case it simply returns FALSE to remove itself from the glib main
// loop.
static gboolean
plugin_out_pipe_callback (GIOChannel* source,
                          GIOCondition condition,
                          gpointer plugin_data)
{
  PLUGIN_DEBUG ("plugin_out_pipe_callback\n");

  ITNPPluginData* data = (ITNPPluginData*) plugin_data;

  PLUGIN_DEBUG ("plugin_out_pipe_callback: appletviewer has stopped.\n");

  PLUGIN_DEBUG ("plugin_out_pipe_callback return\n");

  return FALSE;
}

// remove all components from LD_LIBRARY_PATH, which start with
// MOZILLA_FIVE_HOME; firefox has its own NSS based security provider,
// which conflicts with the one configured in nss.cfg.
static gchar*
plugin_filter_ld_library_path(gchar *path_old)
{
  gchar *moz_home = g_strdup (g_getenv ("MOZILLA_FIVE_HOME"));
  gchar *moz_prefix;
  gchar *path_new;
  gchar** components;
  int i1, i2;

  if (moz_home == NULL || path_old == NULL || strlen (path_old) == 0)
    return path_old;
  if (g_str_has_suffix (moz_home, "/"))
    moz_home[strlen (moz_home - 1)] = '\0';
  moz_prefix = g_strconcat (moz_home, "/", NULL);

  components = g_strsplit (path_old, ":", -1);
  for (i1 = 0, i2 = 0; components[i1] != NULL; i1++)
    {
      if (g_strcmp0 (components[i1], moz_home) == 0
	  || g_str_has_prefix (components[i1], moz_home))
	components[i2] = components[i1];
      else
	components[i2++] = components[i1];
    }
  components[i2] = NULL;

  if (i1 > i2)
    path_new = g_strjoinv (":", components);
  g_strfreev (components);
  g_free (moz_home);
  g_free (moz_prefix);
  g_free (path_old);

  if (path_new == NULL || strlen (path_new) == 0)
    {
      PLUGIN_DEBUG("Unset LD_LIBRARY_PATH\n");
      return NULL;
    }
  else
    {
      PLUGIN_DEBUG ("Set LD_LIBRARY_PATH: %s\n", path_new);
      return path_new;
    }
}

// build the environment to pass to the external plugin process
static gchar**
plugin_filter_environment(void)
{
  gchar **var_names = g_listenv();
  gchar **new_env = (gchar**) malloc(sizeof(gchar*) * (g_strv_length (var_names) + 1));
  int i_var, i_env;

  for (i_var = 0, i_env = 0; var_names[i_var] != NULL; i_var++)
    {
      gchar *env_value = g_strdup (g_getenv (var_names[i_var]));

      if (g_str_has_prefix (var_names[i_var], "LD_LIBRARY_PATH"))
	env_value = plugin_filter_ld_library_path (env_value);
      if (env_value != NULL)
	{
	  new_env[i_env++] = g_strdup_printf ("%s=%s", var_names[i_var], env_value);
	  g_free (env_value);
	}
    }
  new_env[i_env] = NULL;
  return new_env;
}

static NPError
plugin_test_appletviewer ()
{

  PLUGIN_DEBUG ("plugin_test_appletviewer: %s\n", get_plugin_executable().c_str());
  NPError error = NPERR_NO_ERROR;

  gchar* command_line[3] = { NULL, NULL, NULL };
  gchar** environment;

  command_line[0] = g_strdup (get_plugin_executable().c_str());
  command_line[1] = g_strdup("-version");
  command_line[2] = NULL;

  environment = plugin_filter_environment();

  if (!g_spawn_async (NULL, command_line, environment,
		      (GSpawnFlags) 0,
                      NULL, NULL, NULL, &channel_error))
    {
      if (channel_error)
        {
          PLUGIN_ERROR ("Failed to spawn applet viewer %s\n",
                            channel_error->message);
          g_error_free (channel_error);
          channel_error = NULL;
        }
      else
        PLUGIN_ERROR ("Failed to spawn applet viewer\n");
      error = NPERR_GENERIC_ERROR;
    }

  g_strfreev (environment);

  g_free (command_line[0]);
  command_line[0] = NULL;
  g_free (command_line[1]);
  command_line[1] = NULL;
  g_free (command_line[2]);
  command_line[2] = NULL;

  PLUGIN_DEBUG ("plugin_test_appletviewer return\n");
  return error;
}

NPError
plugin_start_appletviewer (ITNPPluginData* data)
{
  PLUGIN_DEBUG ("plugin_start_appletviewer\n");
  NPError error = NPERR_NO_ERROR;

  std::vector<std::string> command_line;
  gchar** environment = NULL;
  std::vector<std::string*>* jvm_args = get_jvm_args();

  // Construct command line parameters

  command_line.push_back(get_plugin_executable());
  //for javaws shortcuts
  command_line.push_back(javaws_bin_property);
  command_line.push_back(javaws_name_property);
  //Add JVM args to command_line
  for (int i = 0; i < jvm_args->size(); i++)
  {
    command_line.push_back(*jvm_args->at(i));
  }

  command_line.push_back(PLUGIN_BOOTCLASSPATH);
  // set the classpath to avoid using the default (cwd).
  command_line.push_back("-classpath");
  command_line.push_back(get_plugin_rt_jar());

  // Enable coverage agent if we are running instrumented plugin
#ifdef COVERAGE_AGENT
  command_line.push_back(COVERAGE_AGENT);
#endif

  if (plugin_debug)
  {
    command_line.push_back("-Xdebug");
    command_line.push_back("-Xnoagent");

    //Debug flags
    std::string debug_flags = "-Xrunjdwp:transport=dt_socket,address=8787,server=y,";
    debug_flags += plugin_debug_suspend ? "suspend=y" : "suspend=n";
    command_line.push_back(debug_flags);
  }

  command_line.push_back("sun.applet.PluginMain");
  command_line.push_back(out_pipe_name);
  command_line.push_back(in_pipe_name);
  if (plugin_debug_to_console){
      command_line.push_back(debug_pipe_name);
  }

  // Finished command line parameters

  environment = plugin_filter_environment();
  std::vector<gchar*> vector_gchar = IcedTeaPluginUtilities::vectorStringToVectorGchar(&command_line);
  gchar **command_line_args = &vector_gchar[0];

  if (!g_spawn_async (NULL, command_line_args, environment,
                     (GSpawnFlags) G_SPAWN_DO_NOT_REAP_CHILD,
                      NULL, NULL, &appletviewer_pid, &channel_error))
    {
      if (channel_error)
        {
          PLUGIN_ERROR ("Failed to spawn applet viewer %s\n",
                            channel_error->message);
          g_error_free (channel_error);
          channel_error = NULL;
        }
      else
        PLUGIN_ERROR ("Failed to spawn applet viewer\n");
      error = NPERR_GENERIC_ERROR;
    }

  //Free memory
  g_strfreev(environment);
  IcedTeaPluginUtilities::freeStringPtrVector(jvm_args);
  jvm_args = NULL;
  command_line_args = NULL;

  if (appletviewer_pid)
    {
      PLUGIN_DEBUG("Initialized VM with pid=%d\n", appletviewer_pid);
      appletviewer_watch_id = g_child_watch_add(appletviewer_pid, (GChildWatchFunc) appletviewer_monitor, (gpointer) appletviewer_pid);
    }


  PLUGIN_DEBUG ("plugin_start_appletviewer return\n");
  return error;
}

/*
 * Returns JVM options set in itw-settings
 */
std::vector<std::string*>*
get_jvm_args()
{
  std::string output;
  std::vector<std::string*>* tokenOutput = NULL;
  bool args_defined = read_deploy_property_value("deployment.plugin.jvm.arguments", output);
  if (!args_defined){
    return new std::vector<std::string*>();
  }
  tokenOutput = IcedTeaPluginUtilities::strSplit(output.c_str(), " \n");
  return tokenOutput;
}


/*
 * Escape characters for passing to Java.
 * "\n" for new line, "\\" for "\", "\:" for ";"
 */
std::string
escape_parameter_string(const char* to_encode) {
  std::string encoded;

  if (to_encode == NULL)
  {
      return encoded;
  }

  size_t length = strlen(to_encode);
  for (int i = 0; i < length; i++)
  {
      if (to_encode[i] == '\n')
          encoded += "\\n";
      else if (to_encode[i] == '\\')
    	  encoded += "\\\\";
      else if (to_encode[i] == ';')
    	  encoded += "\\:";
      else
          encoded += to_encode[i];
  }

  return encoded;
}

/*
 * Build a string containing an encoded list of parameters to send to the applet viewer.
 * The parameters are separated as 'key1;value1;key2;value2;'. As well, they are
 * separated and escaped as:
 * "\n" for new line, "\\" for "\", "\:" for ";"
 */
std::string
plugin_parameters_string (int argc, char* argn[], char* argv[])
{
  PLUGIN_DEBUG ("plugin_parameters_string\n");

  std::string parameters;

  for (int i = 0; i < argc; i++)
  {
    if (argv[i] != NULL)
    {
        std::string name_escaped = escape_parameter_string(argn[i]);
        std::string value_escaped = escape_parameter_string(argv[i]);

        //Encode parameters and send as 'key1;value1;key2;value2;' etc
        parameters += name_escaped;
        parameters += ';';
        parameters += value_escaped;
        parameters += ';';
    }
  }

  PLUGIN_DEBUG ("plugin_parameters_string return\n");

  return parameters;
}

// plugin_send_message_to_appletviewer must be called while holding
// data->appletviewer_mutex.
void
plugin_send_message_to_appletviewer (gchar const* message)
{
  PLUGIN_DEBUG ("plugin_send_message_to_appletviewer\n");

  if (jvm_up)
    {
      gchar* newline_message = NULL;
      gsize bytes_written = 0;

      // Send message to appletviewer.
      newline_message = g_strdup_printf ("%s\n", message);

      // g_io_channel_write_chars will return something other than
      // G_IO_STATUS_NORMAL if not all the data is written.  In that
      // case we fail rather than retrying.
      if (g_io_channel_write_chars (out_to_appletviewer,
                                    newline_message, -1, &bytes_written,
                                    &channel_error)
            != G_IO_STATUS_NORMAL)
        {
          if (channel_error)
            {
              PLUGIN_ERROR ("Failed to write bytes to output channel '%s' \n",
                                channel_error->message);
              g_error_free (channel_error);
              channel_error = NULL;
            }
          else
            PLUGIN_ERROR ("Failed to write bytes to output channel for %s", newline_message);
        }

      if (g_io_channel_flush (out_to_appletviewer, &channel_error)
          != G_IO_STATUS_NORMAL)
        {
          if (channel_error)
            {
              PLUGIN_ERROR ("Failed to flush bytes to output channel '%s'\n",
                                channel_error->message);
              g_error_free (channel_error);
              channel_error = NULL;
            }
          else
            PLUGIN_ERROR ("Failed to flush bytes to output channel for %s", newline_message);
        }
      g_free (newline_message);
      newline_message = NULL;

      PLUGIN_DEBUG ("  PIPE: plugin wrote(?): %s\n", message);
    }

  PLUGIN_DEBUG ("plugin_send_message_to_appletviewer return\n");
}

// unlike like  plugin_send_message_to_appletviewer
// do not debug
// do not error
// do not have its own line end
// is accesed by only one thread
// have own pipe
// jvm must be up
void
plugin_send_message_to_appletviewer_console (gchar const* newline_message)
{
  gsize bytes_written = 0;
  if (g_io_channel_write_chars (debug_to_appletviewer,
                                newline_message, -1, &bytes_written,
                                &channel_error) != G_IO_STATUS_NORMAL)  {
          if (channel_error) {
              //error must be freed
              g_error_free (channel_error);
              channel_error = NULL;
            }
        }
}
//flush only when its full
void flush_plugin_send_message_to_appletviewer_console (){
  if (g_io_channel_flush (debug_to_appletviewer, &channel_error)
                                                 != G_IO_STATUS_NORMAL) {
          if (channel_error) {
              g_error_free (channel_error);
              channel_error = NULL;
            }
        }
}

/*
 * Sends the initialization message (handle/size/url) to the plugin
 */
void
plugin_send_initialization_message(char* instance, gulong handle,
                                   int width, int height, char* url)
{
  PLUGIN_DEBUG ("plugin_send_initialization_message\n");

  gchar *window_message = g_strdup_printf ("instance %s handle %ld width %d height %d %s",
                                            instance, handle, width, height, url);
  plugin_send_message_to_appletviewer (window_message);
  g_free (window_message);
  window_message = NULL;

  PLUGIN_DEBUG ("plugin_send_initialization_message return\n");
}


// Stop the appletviewer process.  When this is called the
// appletviewer can be in any of three states: running, crashed or
// hung.  If the appletviewer is running then sending it "shutdown"
// will cause it to exit.  This will cause
// plugin_out_pipe_callback/plugin_in_pipe_callback to be called and
// the input and output channels to be shut down.  If the appletviewer
// has crashed then plugin_out_pipe_callback/plugin_in_pipe_callback
// would already have been called and data->appletviewer_alive cleared
// in which case this function simply returns.  If the appletviewer is
// hung then this function will be successful and the input and output
// watches will be removed by plugin_data_destroy.
// plugin_stop_appletviewer must be called with
// data->appletviewer_mutex held.
static void
plugin_stop_appletviewer ()
{
  PLUGIN_DEBUG ("plugin_stop_appletviewer\n");

  if (jvm_up)
    {
      // Shut down the appletviewer.
      gsize bytes_written = 0;

      if (out_to_appletviewer)
        {
          if (g_io_channel_write_chars (out_to_appletviewer, "shutdown",
                                        -1, &bytes_written, &channel_error)
              != G_IO_STATUS_NORMAL)
            {
              if (channel_error)
                {
                  PLUGIN_ERROR ("Failed to write shutdown message to "
                                    " appletviewer, %s \n", channel_error->message);
                  g_error_free (channel_error);
                  channel_error = NULL;
                }
              else
                PLUGIN_ERROR ("Failed to write shutdown message to\n");
            }

          if (g_io_channel_flush (out_to_appletviewer, &channel_error)
              != G_IO_STATUS_NORMAL)
            {
              if (channel_error)
                {
                  PLUGIN_ERROR ("Failed to write shutdown message to"
                                    " appletviewer %s \n", channel_error->message);
                  g_error_free (channel_error);
                  channel_error = NULL;
                }
              else
                PLUGIN_ERROR ("Failed to write shutdown message to\n");
            }

          if (g_io_channel_shutdown (out_to_appletviewer,
                                     TRUE, &channel_error)
              != G_IO_STATUS_NORMAL)
            {
              if (channel_error)
                {
                  PLUGIN_ERROR ("Failed to shut down appletviewer"
                                    " output channel %s \n", channel_error->message);
                  g_error_free (channel_error);
                  channel_error = NULL;
                }
              else
                PLUGIN_ERROR ("Failed to shut down appletviewer\n");
            }
        }

      if (in_from_appletviewer)
        {
          if (g_io_channel_shutdown (in_from_appletviewer,
                                     TRUE, &channel_error)
              != G_IO_STATUS_NORMAL)
            {
              if (channel_error)
                {
                  PLUGIN_ERROR ("Failed to shut down appletviewer"
                                    " input channel %s \n", channel_error->message);
                  g_error_free (channel_error);
                  channel_error = NULL;
                }
              else
                PLUGIN_ERROR ("Failed to shut down appletviewer\n");
            }
        }
    }

  jvm_up = FALSE;
  sleep(2); /* Needed to prevent crashes during debug (when JDWP port is not freed by the kernel right away) */

  PLUGIN_DEBUG ("plugin_stop_appletviewer return\n");
}

static void appletviewer_monitor(GPid pid, gint status, gpointer data)
{
    PLUGIN_DEBUG ("appletviewer_monitor\n");
    jvm_up = FALSE;
    pid = -1;
    PLUGIN_DEBUG ("appletviewer_monitor return\n");
}

void
plugin_data_destroy (NPP instance)
{
  PLUGIN_DEBUG ("plugin_data_destroy\n");

  ITNPPluginData* tofree = (ITNPPluginData*) instance->pdata;

  // Remove instance from map
  gpointer id_ptr = g_hash_table_lookup(instance_to_id_map, instance);

  if (id_ptr)
    {
      gint id = GPOINTER_TO_INT(id_ptr);
      g_hash_table_remove(instance_to_id_map, instance);
      g_hash_table_remove(id_to_instance_map, id_ptr);
    }

  /* Explicitly call destructor */
  tofree->~ITNPPluginData();

  (*browser_functions.memfree) (tofree);

  PLUGIN_DEBUG ("plugin_data_destroy return\n");
}

static bool
initialize_browser_functions(const NPNetscapeFuncs* browserTable)
{
#define NPNETSCAPEFUNCS_LAST_FIELD_USED (browserTable->setvalueforurl)

  //Determine the size in bytes, as a difference of the address past the last used field
  //And the browser table address
  size_t usedSize = (char*)(1 + &NPNETSCAPEFUNCS_LAST_FIELD_USED) - (char*)browserTable;

  // compare the reported size versus the size we required
  if (browserTable->size < usedSize)
  {
    return false;
  }

  //Ensure any unused fields are NULL
  memset(&browser_functions, 0, sizeof(NPNetscapeFuncs));

  //browserTable->size can be larger than sizeof(NPNetscapeFuncs) (PR1106)
  size_t copySize = browserTable->size < sizeof(NPNetscapeFuncs) ?
                    browserTable->size : sizeof(NPNetscapeFuncs);

  //Copy fields according to given size
  memcpy(&browser_functions, browserTable, copySize);

  return true;
}

/* Set the plugin table to the correct contents, taking care not to write past
 * the provided object space */
static bool
initialize_plugin_table(NPPluginFuncs* pluginTable)
{
#define NPPLUGINFUNCS_LAST_FIELD_USED (pluginTable->getvalue)

  //Determine the size in bytes, as a difference of the address past the last used field
  //And the browser table address
  size_t usedSize = (char*)(1 + &NPPLUGINFUNCS_LAST_FIELD_USED) - (char*)pluginTable;

  // compare the reported size versus the size we required
  if (pluginTable->size < usedSize)
    return false;

  pluginTable->version = (NP_VERSION_MAJOR << 8) + NP_VERSION_MINOR;
  pluginTable->size = sizeof (NPPluginFuncs);
  pluginTable->newp = NPP_NewProcPtr (ITNP_New);
  pluginTable->destroy = NPP_DestroyProcPtr (ITNP_Destroy);
  pluginTable->setwindow = NPP_SetWindowProcPtr (ITNP_SetWindow);
  pluginTable->newstream = NPP_NewStreamProcPtr (ITNP_NewStream);
  pluginTable->destroystream = NPP_DestroyStreamProcPtr (ITNP_DestroyStream);
  pluginTable->asfile = NPP_StreamAsFileProcPtr (ITNP_StreamAsFile);
  pluginTable->writeready = NPP_WriteReadyProcPtr (ITNP_WriteReady);
  pluginTable->write = NPP_WriteProcPtr (ITNP_Write);
  pluginTable->print = NPP_PrintProcPtr (ITNP_Print);
  pluginTable->urlnotify = NPP_URLNotifyProcPtr (ITNP_URLNotify);
  pluginTable->getvalue = NPP_GetValueProcPtr (ITNP_GetValue);

  return true;
}

// Make sure the plugin data directory exists, creating it if necessary.
NPError
initialize_data_directory()
{

  data_directory = IcedTeaPluginUtilities::getRuntimePath() + "/icedteaplugin-";
  if (getenv("USER") != NULL) {
    data_directory = data_directory + getenv("USER") + "-";
  }
  data_directory += "XXXXXX";
  // Now create a icedteaplugin subdir
  char fileNameX[data_directory.length()+1];
  std::strcpy (fileNameX, data_directory.c_str());
  char * fileName = mkdtemp(fileNameX);
  if (fileName == NULL) {
    PLUGIN_ERROR ("Failed to create data directory %s, %s\n",
                        data_directory.c_str(),
                        strerror (errno));
    return NPERR_GENERIC_ERROR;
  }
  data_directory = std::string(fileName);

  //open uniques icedteaplugin subdir for one single run  
  data_directory_descriptor = opendir(data_directory.c_str());
  if (data_directory_descriptor == NULL) {
      PLUGIN_ERROR ("Failed to open data directory %s %s\n",
                      data_directory.c_str(), strerror (errno));
      return NPERR_GENERIC_ERROR;
  }

  return NPERR_NO_ERROR;
}

// FACTORY FUNCTIONS

// Provides the browser with pointers to the plugin functions that we
// implement and initializes a local table with browser functions that
// we may wish to call.  Called once, after browser startup and before
// the first plugin instance is created.
// The field 'initialized' is set to true once this function has
// finished. If 'initialized' is already true at the beginning of
// this function, then it is evident that NP_Initialize has already
// been called. There is no need to call this function more than once and
// this workaround avoids any duplicate calls.
__attribute__ ((visibility ("default")))
NPError
NP_Initialize (NPNetscapeFuncs* browserTable, NPPluginFuncs* pluginTable)
{
  PLUGIN_DEBUG ("NP_Initialize\n");

  if ((browserTable == NULL) || (pluginTable == NULL))
  {
    PLUGIN_ERROR ("Browser or plugin function table is NULL.\n");

    return NPERR_INVALID_FUNCTABLE_ERROR;
  }

  // Ensure that the major version of the plugin API that the browser
  // expects is not more recent than the major version of the API that
  // we've implemented.
  if ((browserTable->version >> 8) > NP_VERSION_MAJOR)
    {
      PLUGIN_ERROR ("Incompatible version.\n");

      return NPERR_INCOMPATIBLE_VERSION_ERROR;
    }

  // Copy into a global table (browser_functions) the browser functions that we may use.
  // If the browser functions needed change, update NPNETSCAPEFUNCS_LAST_FIELD_USED
  // within this function
  bool browser_functions_supported = initialize_browser_functions(browserTable);

  // Check if everything we rely on is supported
  if ( !browser_functions_supported )
  {
	PLUGIN_ERROR ("Invalid browser function table.\n");

	return NPERR_INVALID_FUNCTABLE_ERROR;
  }

  // Return to the browser the plugin functions that we implement.
  // If the plugin functions needed change, update NPPLUGINFUNCS_LAST_FIELD_USED
  // within this function
  bool plugin_functions_supported = initialize_plugin_table(pluginTable);

  // Check if everything we rely on is supported
  if ( !plugin_functions_supported )
  {
    PLUGIN_ERROR ("Invalid plugin function table.\n");

    return NPERR_INVALID_FUNCTABLE_ERROR;
  }

  // Re-setting the above tables multiple times is OK (as the 
  // browser may change its function locations). However 
  // anything beyond this point should only run once.
  if (initialized)
    return NPERR_NO_ERROR;

  // create directory for pipes
  NPError np_error =  initialize_data_directory();
  if (np_error != NPERR_NO_ERROR)
    {
      PLUGIN_ERROR("Unable to create data directory %s\n", data_directory.c_str());
      return np_error;
    }
    
  // Set appletviewer_executable.
  PLUGIN_DEBUG("Executing java at %s\n", get_plugin_executable().c_str());
  np_error = plugin_test_appletviewer ();
  if (np_error != NPERR_NO_ERROR)
    {
      PLUGIN_ERROR("Unable to find java executable %s\n", get_plugin_executable().c_str());
      return np_error;
    }

  initialized = true;

  // Initialize threads (needed for mutexes).
  if (!g_thread_supported ())
    g_thread_init (NULL);

  plugin_instance_mutex = g_mutex_new ();

  PLUGIN_DEBUG ("NP_Initialize: using %s\n", get_plugin_executable().c_str());

  plugin_req_proc = new PluginRequestProcessor();
  java_req_proc = new JavaMessageSender();

  java_to_plugin_bus = new MessageBus();
  plugin_to_java_bus = new MessageBus();

  java_to_plugin_bus->subscribe(plugin_req_proc);
  plugin_to_java_bus->subscribe(java_req_proc);

  pthread_create (&plugin_request_processor_thread1, NULL, &queue_processor, (void*) plugin_req_proc);
  pthread_create (&plugin_request_processor_thread2, NULL, &queue_processor, (void*) plugin_req_proc);
  pthread_create (&plugin_request_processor_thread3, NULL, &queue_processor, (void*) plugin_req_proc);

  itnp_plugin_thread_id = pthread_self();

  pthread_mutexattr_t attribute;
  pthread_mutexattr_init(&attribute);
  pthread_mutexattr_settype(&attribute, PTHREAD_MUTEX_RECURSIVE);
  pthread_mutex_init(&pluginAsyncCallMutex, &attribute);
  pthread_mutexattr_destroy(&attribute);

  PLUGIN_DEBUG ("NP_Initialize return\n");

  return NPERR_NO_ERROR;
}

// Returns a string describing the MIME type that this plugin
// handles.
__attribute__ ((visibility ("default")))
#ifdef LEGACY_XULRUNNERAPI
  char* 
#else
  const char* 
#endif
NP_GetMIMEDescription ()
{
  //this function is called severaltimes between lunches
  PLUGIN_DEBUG ("NP_GetMIMEDescription\n");

  PLUGIN_DEBUG ("NP_GetMIMEDescription return\n");

  return (char*) PLUGIN_MIME_DESC;
}

// Returns a value relevant to the plugin as a whole.  The browser
// calls this function to obtain information about the plugin.
__attribute__ ((visibility ("default")))
NPError
NP_GetValue (void* future, NPPVariable variable, void* value)
{
  PLUGIN_DEBUG ("NP_GetValue\n");

  NPError result = NPERR_NO_ERROR;
  gchar** char_value = (gchar**) value;

  switch (variable)
    {
    case NPPVpluginNameString:
      PLUGIN_DEBUG ("NP_GetValue: returning plugin name.\n");
      *char_value = g_strdup (PLUGIN_FULL_NAME);
      break;

    case NPPVpluginDescriptionString:
      PLUGIN_DEBUG ("NP_GetValue: returning plugin description.\n");
      *char_value = g_strdup (PLUGIN_DESC);
      break;

    default:
      PLUGIN_ERROR ("Unknown plugin value requested.\n");
      result = NPERR_GENERIC_ERROR;
      break;
    }

  PLUGIN_DEBUG ("NP_GetValue return\n");

  return result;
}

// Shuts down the plugin.  Called after the last plugin instance is
// destroyed.
__attribute__ ((visibility ("default")))
NPError
NP_Shutdown (void)
{
  PLUGIN_DEBUG ("NP_Shutdown\n");

  // Free mutex.
  if (plugin_instance_mutex)
    {
      g_mutex_free (plugin_instance_mutex);
      plugin_instance_mutex = NULL;
    }

  // stop the appletviewer
  plugin_stop_appletviewer();

  // remove monitor
  if (appletviewer_watch_id != -1)
    g_source_remove(appletviewer_watch_id);

  // Removing a source is harmless if it fails since it just means the
  // source has already been removed.
  g_source_remove (in_watch_source);
  in_watch_source = 0;

  // cleanup_in_from_appletviewer:
  if (in_from_appletviewer)
    g_io_channel_unref (in_from_appletviewer);
  in_from_appletviewer = NULL;

  // cleanup_out_watch_source:
  g_source_remove (out_watch_source);
  out_watch_source = 0;

  // cleanup_out_to_appletviewer:
  if (out_to_appletviewer)
    g_io_channel_unref (out_to_appletviewer);
  out_to_appletviewer = NULL;

  // cleanup_out_pipe:
  // Delete output pipe.
  PLUGIN_DEBUG ("NP_Shutdown: deleting output fifo: %s\n", out_pipe_name);
  unlink (out_pipe_name);
  PLUGIN_DEBUG ("NP_Shutdown: deleted output fifo: %s\n", out_pipe_name);

  // cleanup_out_pipe_name:
  g_free (out_pipe_name);
  out_pipe_name = NULL;

  // cleanup_in_pipe:
  // Delete input pipe.
  PLUGIN_DEBUG ("NP_Shutdown: deleting input fifo: %s\n", in_pipe_name);
  unlink (in_pipe_name);
  PLUGIN_DEBUG ("NP_Shutdown: deleted input fifo: %s\n", in_pipe_name);

  // cleanup_in_pipe_name:
  g_free (in_pipe_name);
  in_pipe_name = NULL;

  if (plugin_debug_to_console){
    //jvm_up is now false
    if (g_io_channel_shutdown (debug_to_appletviewer,
                                     TRUE, &channel_error)
              != G_IO_STATUS_NORMAL)
            {
              if (channel_error)
                {
                  PLUGIN_ERROR ("Failed to shut down appletviewer"
                                    " debug channel\n", channel_error->message);
                  g_error_free (channel_error);
                  channel_error = NULL;
                }
              else
                PLUGIN_ERROR ("Failed to shut down debug to appletviewer\n");
            }
    // cleanup_out_to_appletviewer:
    if (debug_to_appletviewer)
     g_io_channel_unref (debug_to_appletviewer);
    out_to_appletviewer = NULL;
    // cleanup_debug_pipe:
    // Delete debug pipe.
    PLUGIN_DEBUG ("NP_Shutdown: deleting debug fifo: %s\n", debug_pipe_name);
    unlink (debug_pipe_name);
    PLUGIN_DEBUG ("NP_Shutdown: deleted debug fifo: %s\n", debug_pipe_name);
    // cleanup_out_pipe_name:
    g_free (debug_pipe_name);
    debug_pipe_name = NULL;
  }
  
  // Destroy the call queue mutex
  pthread_mutex_destroy(&pluginAsyncCallMutex);

  initialized = false;

  pthread_cancel(plugin_request_processor_thread1);
  pthread_cancel(plugin_request_processor_thread2);
  pthread_cancel(plugin_request_processor_thread3);

  pthread_join(plugin_request_processor_thread1, NULL);
  pthread_join(plugin_request_processor_thread2, NULL);
  pthread_join(plugin_request_processor_thread3, NULL);

  java_to_plugin_bus->unSubscribe(plugin_req_proc);
  plugin_to_java_bus->unSubscribe(java_req_proc);
  //internal_bus->unSubscribe(java_req_proc);
  //internal_bus->unSubscribe(plugin_req_proc);

  delete plugin_req_proc;
  delete java_req_proc;
  delete java_to_plugin_bus;
  delete plugin_to_java_bus;
  //delete internal_bus;

  cleanUpDir();
  
  PLUGIN_DEBUG ("NP_Shutdown return\n");
  
  if (plugin_debug_to_file){
    fflush (plugin_file_log);
    //fclose (plugin_file_log);
    //keep writing untill possible!
  }  

  return NPERR_NO_ERROR;
}

NPObject*
get_scriptable_object(NPP instance)
{
    NPObject* obj;
    ITNPPluginData* data = (ITNPPluginData*) instance->pdata;

    if (data->is_applet_instance) // dummy instance/package?
    {
        JavaRequestProcessor java_request = JavaRequestProcessor();
        JavaResultData* java_result;
        std::string instance_id = std::string();
        std::string applet_class_id = std::string();

        int id = get_id_from_instance(instance);
        gchar* id_str = g_strdup_printf ("%d", id);

        // Some browsers.. (e.g. chromium) don't call NPP_SetWindow
        // for 0x0 plugins and therefore require initialization with
        // a 0 handle
        if (!data->window_handle)
        {
            plugin_send_initialization_message(data->instance_id, 0, 0, 0, data->parameters_string);
        }

        java_result = java_request.getAppletObjectInstance(id_str);

        g_free(id_str);

        if (java_result->error_occurred)
        {
            PLUGIN_ERROR("Error: Unable to fetch applet instance id from Java side.\n");
            return NULL;
        }

        instance_id.append(*(java_result->return_string));

        java_result = java_request.getClassID(instance_id);

        if (java_result->error_occurred)
        {
            PLUGIN_ERROR("Error: Unable to fetch applet instance id from Java side.\n");
            return NULL;
        }

        applet_class_id.append(*(java_result->return_string));

        obj = IcedTeaScriptableJavaObject::get_scriptable_java_object(instance, applet_class_id, instance_id, false);

    } else
    {
        obj = IcedTeaScriptableJavaPackageObject::get_scriptable_java_package_object(instance, "");
    }

	return obj;
}

NPObject*
allocate_scriptable_object(NPP npp, NPClass *aClass)
{
	PLUGIN_DEBUG("Allocating new scriptable object\n");
	return new IcedTeaScriptablePluginObject(npp);
}
