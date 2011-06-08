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
#include <errno.h>
#include <libgen.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

// Liveconnect extension
#include "IcedTeaScriptablePluginObject.h"
#include "IcedTeaNPPlugin.h"

#if MOZILLA_VERSION_COLLAPSED < 1090100
// Documentbase retrieval includes.
#include <nsIPluginInstance.h>
#include <nsIPluginInstancePeer.h>
#include <nsIPluginTagInfo2.h>

// API's into Mozilla
#include <nsCOMPtr.h>
#include <nsICookieService.h>
#include <nsIDNSRecord.h>
#include <nsIDNSService.h>
#include <nsINetUtil.h>
#include <nsIProxyInfo.h>
#include <nsIProtocolProxyService.h>
#include <nsIScriptSecurityManager.h>
#include <nsIIOService.h>
#include <nsIURI.h>
#include <nsNetCID.h>
#include <nsStringAPI.h>
#include <nsServiceManagerUtils.h>
#endif

// Error reporting macros.
#define PLUGIN_ERROR(message)                                       \
  g_printerr ("%s:%d: thread %p: Error: %s\n", __FILE__, __LINE__,  \
              g_thread_self (), message)

#define PLUGIN_ERROR_TWO(first, second)                                 \
  g_printerr ("%s:%d: thread %p: Error: %s: %s\n", __FILE__, __LINE__,  \
              g_thread_self (), first, second)

#define PLUGIN_ERROR_THREE(first, second, third)                        \
  g_printerr ("%s:%d: thread %p: Error: %s: %s: %s\n", __FILE__,        \
              __LINE__, g_thread_self (), first, second, third)

// Plugin information passed to about:plugins.
#define PLUGIN_FULL_NAME PLUGIN_NAME " (using " PLUGIN_VERSION ")"
#define PLUGIN_DESC "The <a href=\"" PACKAGE_URL "\">" PLUGIN_NAME "</a> executes Java applets."

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
  "application/x-java-applet;jpi-version=1.6.0_" JDK_UPDATE_VERSION ":class,jar:IcedTea;"  \
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
  "application/x-java-bean;jpi-version=1.6.0_" JDK_UPDATE_VERSION ":class,jar:IcedTea;"    \
  "application/x-java-vm-npruntime::IcedTea;"

#define PLUGIN_URL NS_INLINE_PLUGIN_CONTRACTID_PREFIX NS_JVM_MIME_TYPE
#define PLUGIN_MIME_TYPE "application/x-java-vm"
#define PLUGIN_FILE_EXTS "class,jar,zip"
#define PLUGIN_MIME_COUNT 1

#define FAILURE_MESSAGE "icedteanp plugin error: Failed to run %s." \
  "  For more detail rerun \"firefox -g\" in a terminal window."

#if MOZILLA_VERSION_COLLAPSED < 1090100
// Documentbase retrieval required definition.
static NS_DEFINE_IID (kIPluginTagInfo2IID, NS_IPLUGINTAGINFO2_IID);
#endif

// Data directory for plugin.
static gchar* data_directory = NULL;

// Fully-qualified appletviewer executable.
static gchar* appletviewer_executable = NULL;

// Applet viewer input channel (needs to be static because it is used in plugin_in_pipe_callback)
static GIOChannel* in_from_appletviewer = NULL;

// Applet viewer input pipe name.
gchar* in_pipe_name;

// Applet viewer input watch source.
gint in_watch_source;

// Applet viewer output pipe name.
gchar* out_pipe_name;

// Applet viewer output watch source.
gint out_watch_source;

// Thread ID of plug-in thread
pthread_t itnp_plugin_thread_id;

// Mutex to lock async call queue
pthread_mutex_t pluginAsyncCallMutex;

// Applet viewer output channel.
GIOChannel* out_to_appletviewer;

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

#if MOZILLA_VERSION_COLLAPSED < 1090100
// Documentbase retrieval type-punning union.
typedef union
{
  void** void_field;
  nsIPluginTagInfo2** info_field;
} info_union;
#endif

// Static instance helper functions.
// Have the browser allocate a new ITNPPluginData structure.
static void plugin_data_new (ITNPPluginData** data);
// Retrieve the current document's documentbase.
static gchar* plugin_get_documentbase (NPP instance);
// Notify the user that the appletviewer is not installed correctly.
static void plugin_display_failure_dialog ();
// Callback used to monitor input pipe status.
static gboolean plugin_in_pipe_callback (GIOChannel* source,
                                         GIOCondition condition,
                                         gpointer plugin_data);
// Callback used to monitor output pipe status.
static gboolean plugin_out_pipe_callback (GIOChannel* source,
                                          GIOCondition condition,
                                          gpointer plugin_data);
static NPError plugin_start_appletviewer (ITNPPluginData* data);
static gchar* plugin_create_applet_tag (int16_t argc, char* argn[],
                                        char* argv[]);
static void plugin_stop_appletviewer ();
// Uninitialize ITNPPluginData structure
static void plugin_data_destroy (NPP instance);

NPError get_cookie_info(const char* siteAddr, char** cookieString, uint32_t* len);
NPError get_proxy_info(const char* siteAddr, char** proxy, uint32_t* len);
void consume_message(gchar* message);
void start_jvm_if_needed();
static void appletviewer_monitor(GPid pid, gint status, gpointer data);
void plugin_send_initialization_message(char* instance, gulong handle,
                                               int width, int height,
                                               char* url);

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

int plugin_debug = getenv ("ICEDTEAPLUGIN_DEBUG") != NULL;
int plugin_debug_suspend = (getenv("ICEDTEAPLUGIN_DEBUG") != NULL) &&
        (strcmp(getenv("ICEDTEAPLUGIN_DEBUG"), "suspend") == 0);

pthread_cond_t cond_message_available = PTHREAD_COND_INITIALIZER;

// Functions prefixed by ITNP_ are instance functions.  They are called
// by the browser and operate on instances of ITNPPluginData.
// Functions prefixed by plugin_ are static helper functions.
// Functions prefixed by NP_ are factory functions.  They are called
// by the browser and provide functionality needed to create plugin
// instances.

// INSTANCE FUNCTIONS

// Creates a new icedtea np plugin instance.  This function creates a
// ITNPPluginData* and stores it in instance->pdata.  The following
// ITNPPluginData fiels are initialized: instance_id, in_pipe_name,
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
	printf("%s not found!\n", "document");
  }
  browser_functions.getproperty(instance, window_ptr, identifier, &member_ptr);

  PLUGIN_DEBUG("Got variant %p\n", &member_ptr);


  NPError np_error = NPERR_NO_ERROR;
  ITNPPluginData* data = NULL;

  gchar* documentbase = NULL;
  gchar* read_message = NULL;
  gchar* applet_tag = NULL;
  gchar* cookie_info = NULL;

  NPObject* npPluginObj = NULL;

  if (!instance)
    {
      PLUGIN_ERROR ("Browser-provided instance pointer is NULL.");
      np_error = NPERR_INVALID_INSTANCE_ERROR;
      goto cleanup_done;
    }

  // data
  plugin_data_new (&data);
  if (data == NULL)
    {
      PLUGIN_ERROR ("Failed to allocate plugin data.");
      np_error = NPERR_OUT_OF_MEMORY_ERROR;
      goto cleanup_done;
    }

  // start the jvm if needed
  start_jvm_if_needed();

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

  // Documentbase retrieval.
  documentbase = plugin_get_documentbase (instance);
  if (documentbase && argc != 0)
    {
      // Send applet tag message to appletviewer.
      applet_tag = plugin_create_applet_tag (argc, argn, argv);

      data->applet_tag = (gchar*) malloc(strlen(applet_tag)*sizeof(gchar) + strlen(documentbase)*sizeof(gchar) + 32);
      g_sprintf(data->applet_tag, "tag %s %s", documentbase, applet_tag);

      data->is_applet_instance = true;
    }

  if (argc == 0)
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

  goto cleanup_done;

 cleanup_appletviewer_mutex:
  g_free (data->appletviewer_mutex);
  data->appletviewer_mutex = NULL;

  // cleanup_instance_string:
  g_free (data->instance_id);
  data->instance_id = NULL;

  // cleanup applet tag:
  g_free (data->applet_tag);
  data->applet_tag = NULL;

  // cleanup_data:
  // Eliminate back-pointer to plugin instance.
  data->owner = NULL;
  (*browser_functions.memfree) (data);
  data = NULL;

  // Initialization failed so return a NULL pointer for the browser
  // data.
  instance->pdata = NULL;

 cleanup_done:
  g_free (applet_tag);
  applet_tag = NULL;
  g_free (read_message);
  read_message = NULL;
  g_free (documentbase);
  documentbase = NULL;

  // store an identifier for this plugin
  PLUGIN_DEBUG("Mapping id %d and instance %p\n", instance_counter, instance);
  g_hash_table_insert(instance_to_id_map, instance, GINT_TO_POINTER(instance_counter));
  g_hash_table_insert(id_to_instance_map, GINT_TO_POINTER(instance_counter), instance);
  instance_counter++;

  PLUGIN_DEBUG ("ITNP_New return\n");

  return np_error;
}

// Starts the JVM if it is not already running
void start_jvm_if_needed()
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
      return;
  }

  PLUGIN_DEBUG("No JVM is running. Attempting to start one...\n");

  NPError np_error = NPERR_NO_ERROR;
  ITNPPluginData* data = NULL;

  // Create appletviewer-to-plugin pipe which we refer to as the input
  // pipe.

  // in_pipe_name
  in_pipe_name = g_strdup_printf ("%s/%d-icedteanp-appletviewer-to-plugin",
                                         data_directory, getpid());
  if (!in_pipe_name)
    {
      PLUGIN_ERROR ("Failed to create input pipe name.");
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
      PLUGIN_ERROR_TWO ("Failed to create input pipe", strerror (errno));
      np_error = NPERR_GENERIC_ERROR;
      goto cleanup_in_pipe_name;
    }
  PLUGIN_DEBUG ("ITNP_New: created input fifo: %s\n", in_pipe_name);

  // Create plugin-to-appletviewer pipe which we refer to as the
  // output pipe.

  // out_pipe_name
  out_pipe_name = g_strdup_printf ("%s/%d-icedteanp-plugin-to-appletviewer",
                                         data_directory, getpid());

  if (!out_pipe_name)
    {
      PLUGIN_ERROR ("Failed to create output pipe name.");
      np_error = NPERR_OUT_OF_MEMORY_ERROR;
      goto cleanup_out_pipe_name;
    }

  // clean up any older pip
  unlink (out_pipe_name);

  PLUGIN_DEBUG ("ITNP_New: creating output fifo: %s\n", out_pipe_name);
  if (mkfifo (out_pipe_name, 0600) == -1 && errno != EEXIST)
    {
      PLUGIN_ERROR_TWO ("Failed to create output pipe", strerror (errno));
      np_error = NPERR_GENERIC_ERROR;
      goto cleanup_out_pipe_name;
    }
  PLUGIN_DEBUG ("ITNP_New: created output fifo: %s\n", out_pipe_name);

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
          PLUGIN_ERROR_TWO ("Failed to create output channel",
                            channel_error->message);
          g_error_free (channel_error);
          channel_error = NULL;
        }
      else
        PLUGIN_ERROR ("Failed to create output channel");

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
          PLUGIN_ERROR_TWO ("Failed to create input channel",
                            channel_error->message);
          g_error_free (channel_error);
          channel_error = NULL;
        }
      else
        PLUGIN_ERROR ("Failed to create input channel");

      np_error = NPERR_GENERIC_ERROR;
      goto cleanup_in_from_appletviewer;
    }

  // Watch for hangup and error signals on the input pipe.
  in_watch_source =
    g_io_add_watch (in_from_appletviewer,
                    (GIOCondition) (G_IO_IN | G_IO_ERR | G_IO_HUP),
                    plugin_in_pipe_callback, (gpointer) in_from_appletviewer);

  jvm_up = TRUE;

  goto done;

  // Free allocated data

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

 done:

  // Now other threads may re-enter.. unlock the mutex
  g_mutex_unlock(vm_start_mutex);

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
      PLUGIN_ERROR ("Unknown plugin value requested.");
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
      PLUGIN_ERROR ("Invalid instance.");

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
    		  data->window_width, data->window_height, data->applet_tag);

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
#if MOZILLA_VERSION_COLLAPSED < 1090100
  nsresult rv;
  nsCOMPtr<nsIScriptSecurityManager> sec_man =
    do_GetService(NS_SCRIPTSECURITYMANAGER_CONTRACTID, &rv);

  if (!sec_man) {
    return NPERR_GENERIC_ERROR;
  }

  nsCOMPtr<nsIIOService> io_svc = do_GetService("@mozilla.org/network/io-service;1", &rv);

  if (NS_FAILED(rv) || !io_svc) {
    return NPERR_GENERIC_ERROR;
  }

  nsCOMPtr<nsIURI> uri;
  io_svc->NewURI(nsCString(siteAddr), NULL, NULL, getter_AddRefs(uri));

  nsCOMPtr<nsICookieService> cookie_svc = do_GetService("@mozilla.org/cookieService;1", &rv);

  if (NS_FAILED(rv) || !cookie_svc) {
    return NPERR_GENERIC_ERROR;
  }

  rv = cookie_svc->GetCookieString(uri, NULL, cookieString);

  if (NS_FAILED(rv) || !*cookieString) {
    return NPERR_GENERIC_ERROR;
  }

#else

  // getvalueforurl needs an NPP instance. Quite frankly, there is no easy way
  // to know which instance needs the information, as applets on Java side can
  // be multi-threaded and the thread making a proxy.cookie request cannot be
  // easily tracked.

  // Fortunately, XULRunner does not care about the instance as long as it is
  // valid. So we just pick the first valid one and use it. Proxy/Cookie
  // information is not instance specific anyway, it is URL specific.

  if (browser_functions.getvalueforurl)
  {
      GHashTableIter iter;
      gpointer id, instance;

      g_hash_table_iter_init (&iter, instance_to_id_map);
      g_hash_table_iter_next (&iter, &instance, &id);

      return browser_functions.getvalueforurl((NPP) instance, NPNURLVCookie, siteAddr, cookieString, len);
  } else
  {
      return NPERR_GENERIC_ERROR;
  }

#endif

  return NPERR_NO_ERROR;
}

// HELPER FUNCTIONS

static void
plugin_data_new (ITNPPluginData** data)
{
  PLUGIN_DEBUG ("plugin_data_new\n");

  *data = (ITNPPluginData*)
    (*browser_functions.memalloc) (sizeof (struct ITNPPluginData));

  // appletviewer_alive is false until the applet viewer is spawned.
  if (*data)
    memset (*data, 0, sizeof (struct ITNPPluginData));

  PLUGIN_DEBUG ("plugin_data_new return\n");
}



// Documentbase retrieval.  This function gets the current document's
// documentbase.  This function relies on browser-private data so it
// will only work when the plugin is loaded in a Mozilla-based
// browser.  We could not find a way to retrieve the documentbase
// using the original Netscape plugin API so we use the XPCOM API
// instead.
#if MOZILLA_VERSION_COLLAPSED < 1090100
static gchar*
plugin_get_documentbase (NPP instance)
{
  PLUGIN_DEBUG ("plugin_get_documentbase\n");

  nsIPluginInstance* xpcom_instance = NULL;
  nsIPluginInstancePeer* peer = NULL;
  nsresult result = 0;
  nsIPluginTagInfo2* pluginTagInfo2 = NULL;
  info_union u = { NULL };
  char const* documentbase = NULL;
  gchar* documentbase_copy = NULL;

  xpcom_instance = (nsIPluginInstance*) (instance->ndata);
  if (!xpcom_instance)
    {
      PLUGIN_ERROR ("xpcom_instance is NULL.");
      goto cleanup_done;
    }

  xpcom_instance->GetPeer (&peer);
  if (!peer)
    {
      PLUGIN_ERROR ("peer is NULL.");
      goto cleanup_done;
    }

  u.info_field = &pluginTagInfo2;

  result = peer->QueryInterface (kIPluginTagInfo2IID,
                                 u.void_field);
  if (result || !pluginTagInfo2)
    {
      PLUGIN_ERROR ("pluginTagInfo2 retrieval failed.");
      goto cleanup_peer;
    }

  pluginTagInfo2->GetDocumentBase (&documentbase);

  if (!documentbase)
    {
      // NULL => dummy instantiation for LiveConnect
      goto cleanup_plugintaginfo2;
    }

  documentbase_copy = g_strdup (documentbase);

  // Release references.
 cleanup_plugintaginfo2:
  NS_RELEASE (pluginTagInfo2);

 cleanup_peer:
  NS_RELEASE (peer);

 cleanup_done:
  PLUGIN_DEBUG ("plugin_get_documentbase return\n");

  PLUGIN_DEBUG("plugin_get_documentbase returning: %s\n", documentbase_copy);
  return documentbase_copy;
}
#else
static gchar*
plugin_get_documentbase (NPP instance)
{
  PLUGIN_DEBUG ("plugin_get_documentbase\n");

  char const* documentbase = NULL;
  gchar* documentbase_copy = NULL;

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

  // Strip everything after the last "/"
#if MOZILLA_VERSION_COLLAPSED < 1090200
  gchar** parts = g_strsplit (NPVARIANT_TO_STRING(href).utf8characters, "/", -1);
#else
  gchar** parts = g_strsplit (NPVARIANT_TO_STRING(href).UTF8Characters, "/", -1);
#endif
  guint parts_sz = g_strv_length (parts);

  std::string location_str;
  for (int i=0; i < parts_sz - 1; i++)
  {
      location_str += parts[i];
      location_str += "/";
  }

  documentbase_copy = g_strdup (location_str.c_str());

  // Release references.
  browser_functions.releasevariantvalue(&href);
  browser_functions.releasevariantvalue(&location);
 cleanup_done:
  PLUGIN_DEBUG ("plugin_get_documentbase return\n");
  PLUGIN_DEBUG("plugin_get_documentbase returning: %s\n", documentbase_copy);

  return documentbase_copy;
}
#endif

// This function displays an error message if the appletviewer has not
// been installed correctly.
static void
plugin_display_failure_dialog ()
{
  GtkWidget* dialog = NULL;

  PLUGIN_DEBUG ("plugin_display_failure_dialog\n");

  dialog = gtk_message_dialog_new (NULL,
                                   GTK_DIALOG_DESTROY_WITH_PARENT,
                                   GTK_MESSAGE_ERROR,
                                   GTK_BUTTONS_CLOSE,
                                   FAILURE_MESSAGE,
                                   appletviewer_executable);
  gtk_widget_show_all (dialog);
  gtk_dialog_run (GTK_DIALOG (dialog));
  gtk_widget_destroy (dialog);

  PLUGIN_DEBUG ("plugin_display_failure_dialog return\n");
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
              PLUGIN_ERROR_TWO ("Failed to read line from input channel",
                                channel_error->message);
              g_error_free (channel_error);
              channel_error = NULL;
            }
          else
            PLUGIN_ERROR ("Failed to read line from input channel");
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
          sprintf(parts[0], "");
          sprintf(parts[1], "");
          sprintf(parts[2], "");

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
      // internal plugin related message
      gchar** parts = g_strsplit (message, " ", 5);
      if (g_str_has_prefix(parts[1], "PluginProxyInfo"))
      {
        gchar* proxy;
        uint32_t len;

        gchar* decoded_url = (gchar*) calloc(strlen(parts[4]) + 1, sizeof(gchar));
        IcedTeaPluginUtilities::decodeURL(parts[4], &decoded_url);
        PLUGIN_DEBUG("parts[0]=%s, parts[1]=%s, reference, parts[3]=%s, parts[4]=%s -- decoded_url=%s\n", parts[0], parts[1], parts[3], parts[4], decoded_url);

        gchar* proxy_info;

#if MOZILLA_VERSION_COLLAPSED < 1090100
	proxy = (char*) malloc(sizeof(char)*2048);
#endif

        proxy_info = g_strconcat ("plugin PluginProxyInfo reference ", parts[3], " ", NULL);
        if (get_proxy_info(decoded_url, &proxy, &len) == NPERR_NO_ERROR)
          {
            proxy_info = g_strconcat (proxy_info, proxy, NULL);
          }

        PLUGIN_DEBUG("Proxy info: %s\n", proxy_info);
        plugin_send_message_to_appletviewer(proxy_info);

        g_free(decoded_url);
        decoded_url = NULL;
        g_free(proxy_info);
        proxy_info = NULL;

#if MOZILLA_VERSION_COLLAPSED < 1090100
	g_free(proxy);
	proxy = NULL;
#endif

      } else if (g_str_has_prefix(parts[1], "PluginCookieInfo"))
      {
        gchar* decoded_url = (gchar*) calloc(strlen(parts[4])+1, sizeof(gchar));
        IcedTeaPluginUtilities::decodeURL(parts[4], &decoded_url);

        gchar* cookie_info = g_strconcat ("plugin PluginCookieInfo reference ", parts[3], " ", NULL);
        gchar* cookie_string;
        uint32_t len;
        if (get_cookie_info(decoded_url, &cookie_string, &len) == NPERR_NO_ERROR)
        {
            cookie_info = g_strconcat (cookie_info, cookie_string, NULL);
        }

        PLUGIN_DEBUG("Cookie info: %s\n", cookie_info);
        plugin_send_message_to_appletviewer(cookie_info);

        g_free(decoded_url);
        decoded_url = NULL;
        g_free(cookie_info);
        cookie_info = NULL;
      }
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
#if MOZILLA_VERSION_COLLAPSED < 1090100
  nsresult rv;

  // Initialize service variables
  nsCOMPtr<nsIProtocolProxyService> proxy_svc = do_GetService("@mozilla.org/network/protocol-proxy-service;1", &rv);

  if (!proxy_svc) {
      printf("Cannot initialize proxy service\n");
      return NPERR_GENERIC_ERROR;
  }

  nsCOMPtr<nsIIOService> io_svc = do_GetService("@mozilla.org/network/io-service;1", &rv);

  if (NS_FAILED(rv) || !io_svc) {
    printf("Cannot initialize io service\n");
    return NPERR_GENERIC_ERROR;
  }

  // uri which needs to be accessed
  nsCOMPtr<nsIURI> uri;
  io_svc->NewURI(nsCString(siteAddr), NULL, NULL, getter_AddRefs(uri));

  // find the proxy address if any
  nsCOMPtr<nsIProxyInfo> info;
  proxy_svc->Resolve(uri, 0, getter_AddRefs(info));

  // if there is no proxy found, return immediately
  if (!info) {
     PLUGIN_DEBUG("%s does not need a proxy\n", siteAddr);
     return NPERR_GENERIC_ERROR;
  }

  // if proxy info is available, extract it
  nsCString phost;
  PRInt32 pport;
  nsCString ptype;

  info->GetHost(phost);
  info->GetPort(&pport);
  info->GetType(ptype);

  // resolve the proxy address to an IP
  nsCOMPtr<nsIDNSService> dns_svc = do_GetService("@mozilla.org/network/dns-service;1", &rv);

  if (!dns_svc) {
      printf("Cannot initialize DNS service\n");
      return NPERR_GENERIC_ERROR;
  }

  nsCOMPtr<nsIDNSRecord> record;
  dns_svc->Resolve(phost, 0U, getter_AddRefs(record));

  // TODO: Add support for multiple ips
  nsDependentCString ipAddr;
  record->GetNextAddrAsString(ipAddr);

  if (!strcmp(ptype.get(), "http"))
  {
      snprintf(*proxy, sizeof(char)*1024, "%s %s:%d", "PROXY", ipAddr.get(), pport);
  } else
  {
      snprintf(*proxy, sizeof(char)*1024, "%s %s:%d", "SOCKS", ipAddr.get(), pport);
  }

  *len = strlen(*proxy);

  PLUGIN_DEBUG("Proxy info for %s: %s\n", siteAddr, *proxy);

#else

  if (browser_functions.getvalueforurl)
  {

      // As in get_cookie_info, we use the first active instance
      GHashTableIter iter;
      gpointer id, instance;

      g_hash_table_iter_init (&iter, instance_to_id_map);
      g_hash_table_iter_next (&iter, &instance, &id);

      browser_functions.getvalueforurl((NPP) instance, NPNURLVProxy, siteAddr, proxy, len);
  } else
  {
      return NPERR_GENERIC_ERROR;
  }
#endif

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
  PLUGIN_DEBUG ("plugin_test_appletviewer: %s\n", appletviewer_executable);
  NPError error = NPERR_NO_ERROR;

  gchar* command_line[3] = { NULL, NULL, NULL };
  gchar** environment;

  command_line[0] = g_strdup (appletviewer_executable);
  command_line[1] = g_strdup("-version");
  command_line[2] = NULL;

  environment = plugin_filter_environment();

  if (!g_spawn_async (NULL, command_line, environment,
		      (GSpawnFlags) 0,
                      NULL, NULL, NULL, &channel_error))
    {
      if (channel_error)
        {
          PLUGIN_ERROR_TWO ("Failed to spawn applet viewer",
                            channel_error->message);
          g_error_free (channel_error);
          channel_error = NULL;
        }
      else
        PLUGIN_ERROR ("Failed to spawn applet viewer");
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

static NPError
plugin_start_appletviewer (ITNPPluginData* data)
{
  PLUGIN_DEBUG ("plugin_start_appletviewer\n");
  NPError error = NPERR_NO_ERROR;

  gchar** command_line;
  gchar** environment;

  int cmd_num = 0;
  if (plugin_debug)
  {
      command_line = (gchar**) malloc(sizeof(gchar*)*11);
      command_line[cmd_num++] = g_strdup(appletviewer_executable);
      command_line[cmd_num++] = g_strdup(PLUGIN_BOOTCLASSPATH);
      // set the classpath to avoid using the default (cwd).
      command_line[cmd_num++] = g_strdup("-classpath");
      command_line[cmd_num++] = g_strdup_printf("%s/lib/rt.jar", ICEDTEA_WEB_JRE);
      command_line[cmd_num++] = g_strdup("-Xdebug");
      command_line[cmd_num++] = g_strdup("-Xnoagent");
      if (plugin_debug_suspend)
      {
          command_line[cmd_num++] = g_strdup("-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y");
      } else
      {
          command_line[cmd_num++] = g_strdup("-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n");
      }
      command_line[cmd_num++] = g_strdup("sun.applet.PluginMain");
      command_line[cmd_num++] = g_strdup(out_pipe_name);
      command_line[cmd_num++] = g_strdup(in_pipe_name);
      command_line[cmd_num] = NULL;
  } else
  {
      command_line = (gchar**) malloc(sizeof(gchar*)*8);
      command_line[cmd_num++] = g_strdup(appletviewer_executable);
      command_line[cmd_num++] = g_strdup(PLUGIN_BOOTCLASSPATH);
      command_line[cmd_num++] = g_strdup("-classpath");
      command_line[cmd_num++] = g_strdup_printf("%s/lib/rt.jar", ICEDTEA_WEB_JRE);
      command_line[cmd_num++] = g_strdup("sun.applet.PluginMain");
      command_line[cmd_num++] = g_strdup(out_pipe_name);
      command_line[cmd_num++] = g_strdup(in_pipe_name);
      command_line[cmd_num] = NULL;
  }

  environment = plugin_filter_environment();

  if (!g_spawn_async (NULL, command_line, environment,
		      (GSpawnFlags) G_SPAWN_DO_NOT_REAP_CHILD,
                      NULL, NULL, &appletviewer_pid, &channel_error))
    {
      if (channel_error)
        {
          PLUGIN_ERROR_TWO ("Failed to spawn applet viewer",
                            channel_error->message);
          g_error_free (channel_error);
          channel_error = NULL;
        }
      else
        PLUGIN_ERROR ("Failed to spawn applet viewer");
      error = NPERR_GENERIC_ERROR;
    }

  g_strfreev (environment);

  for (int i = 0; i < cmd_num; i++) {
    g_free (command_line[i]);
    command_line[i] = NULL;
  }

  g_free(command_line);
  command_line = NULL;

  if (appletviewer_pid)
    {
      PLUGIN_DEBUG("Initialized VM with pid=%d\n", appletviewer_pid);
      appletviewer_watch_id = g_child_watch_add(appletviewer_pid, (GChildWatchFunc) appletviewer_monitor, (gpointer) appletviewer_pid);
    }


  PLUGIN_DEBUG ("plugin_start_appletviewer return\n");
  return error;
}

/*
 * Replaces certain characters (\r, \n, etc) with HTML escape equivalents.
 *
 * Return string is allocated on the heap. Caller assumes responsibility 
 * for freeing the memory via free()
 */
static char*
encode_string(char* to_encode)
{

  // Do nothing for an empty string
  if (to_encode == '\0')
      return to_encode;

  // worst case scenario -> all characters are newlines or
  // returns, each of which translates to 5 substitutions
  char* encoded = (char*) calloc(((strlen(to_encode)*5)+1), sizeof(char));

  strcpy(encoded, "");

  for (int i=0; i < strlen(to_encode); i++)
  {
      if (to_encode[i] == '\r')
          encoded = strcat(encoded, "&#13;");
      else if (to_encode[i] == '\n')
          encoded = strcat(encoded, "&#10;");
      else if (to_encode[i] == '>')
          encoded = strcat(encoded, "&gt;");
      else if (to_encode[i] == '<')
          encoded = strcat(encoded, "&lt;");
      else if (to_encode[i] == '&')
          encoded = strcat(encoded, "&amp;");
      else if (to_encode[i] == '"')
          encoded = strcat(encoded, "&quot;");
      else
      {
           char* orig_char = (char*) calloc(2, sizeof(char));
           orig_char[0] = to_encode[i];
           orig_char[1] = '\0';
 
           strcat(encoded, orig_char);
 
           free(orig_char);
           orig_char = NULL;
      }
  }

  return encoded;
}

// Build up the applet tag string that we'll send to the applet
// viewer.
static gchar*
plugin_create_applet_tag (int16_t argc, char* argn[], char* argv[])
{
  PLUGIN_DEBUG ("plugin_create_applet_tag\n");

  gchar* applet_tag = g_strdup ("<EMBED ");
  gchar* parameters = g_strdup ("");

  for (int16_t i = 0; i < argc; i++)
    {
      gchar* argn_escaped = encode_string(argn[i]);
      gchar* argv_escaped = encode_string(argv[i]);

      if (!g_ascii_strcasecmp (argn_escaped, "code"))
        {
          gchar* code = g_strdup_printf ("CODE=\"%s\" ", argv_escaped);
          applet_tag = g_strconcat (applet_tag, code, NULL);
          g_free (code);
          code = NULL;
    }
      else if (!g_ascii_strcasecmp (argn_escaped, "java_code"))
    {
          gchar* java_code = g_strdup_printf ("JAVA_CODE=\"%s\" ", argv_escaped);
          applet_tag = g_strconcat (applet_tag, java_code, NULL);
          g_free (java_code);
          java_code = NULL;
    }
      else if (!g_ascii_strcasecmp (argn_escaped, "codebase"))
    {
          gchar* codebase = g_strdup_printf ("CODEBASE=\"%s\" ", argv_escaped);
          applet_tag = g_strconcat (applet_tag, codebase, NULL);
          g_free (codebase);
          codebase = NULL;
    }
      else if (!g_ascii_strcasecmp (argn_escaped, "java_codebase"))
    {
          gchar* java_codebase = g_strdup_printf ("JAVA_CODEBASE=\"%s\" ", argv_escaped);
          applet_tag = g_strconcat (applet_tag, java_codebase, NULL);
          g_free (java_codebase);
          java_codebase = NULL;
    }
      else if (!g_ascii_strcasecmp (argn_escaped, "classid"))
    {
          gchar* classid = g_strdup_printf ("CLASSID=\"%s\" ", argv_escaped);
          applet_tag = g_strconcat (applet_tag, classid, NULL);
          g_free (classid);
          classid = NULL;
    }
      else if (!g_ascii_strcasecmp (argn_escaped, "archive"))
    {
          gchar* archive = g_strdup_printf ("ARCHIVE=\"%s\" ", argv_escaped);
          applet_tag = g_strconcat (applet_tag, archive, NULL);
          g_free (archive);
          archive = NULL;
    }
      else if (!g_ascii_strcasecmp (argn_escaped, "java_archive"))
    {
          gchar* java_archive = g_strdup_printf ("JAVA_ARCHIVE=\"%s\" ", argv_escaped);
          applet_tag = g_strconcat (applet_tag, java_archive, NULL);
          g_free (java_archive);
          java_archive = NULL;
    }
      else if (!g_ascii_strcasecmp (argn_escaped, "width"))
    {
          gchar* width = g_strdup_printf ("width=\"%s\" ", argv_escaped);
          applet_tag = g_strconcat (applet_tag, width, NULL);
          g_free (width);
          width = NULL;
    }
      else if (!g_ascii_strcasecmp (argn_escaped, "height"))
    {
          gchar* height = g_strdup_printf ("height=\"%s\" ", argv_escaped);
          applet_tag = g_strconcat (applet_tag, height, NULL);
          g_free (height);
          height = NULL;
    }
      else
        {

          if (argv_escaped != '\0')
            {
              parameters = g_strconcat (parameters, "<PARAM NAME=\"", argn_escaped,
                                        "\" VALUE=\"", argv_escaped, "\">", NULL);
            }
        }

      free(argn_escaped);
      free(argv_escaped);

      argn_escaped = NULL;
      argv_escaped = NULL;
    }

  applet_tag = g_strconcat (applet_tag, ">", parameters, "</EMBED>", NULL);

  g_free (parameters);
  parameters = NULL;

  PLUGIN_DEBUG ("plugin_create_applet_tag return\n");

  return applet_tag;
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
              PLUGIN_ERROR_TWO ("Failed to write bytes to output channel",
                                channel_error->message);
              g_error_free (channel_error);
              channel_error = NULL;
            }
          else
            PLUGIN_ERROR ("Failed to write bytes to output channel");
        }

      if (g_io_channel_flush (out_to_appletviewer, &channel_error)
          != G_IO_STATUS_NORMAL)
        {
          if (channel_error)
            {
              PLUGIN_ERROR_TWO ("Failed to flush bytes to output channel",
                                channel_error->message);
              g_error_free (channel_error);
              channel_error = NULL;
            }
          else
            PLUGIN_ERROR ("Failed to flush bytes to output channel");
        }
      g_free (newline_message);
      newline_message = NULL;

      PLUGIN_DEBUG ("  PIPE: plugin wrote: %s\n", message);
    }

  PLUGIN_DEBUG ("plugin_send_message_to_appletviewer return\n");
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
                  PLUGIN_ERROR_TWO ("Failed to write shutdown message to"
                                    " appletviewer", channel_error->message);
                  g_error_free (channel_error);
                  channel_error = NULL;
                }
              else
                PLUGIN_ERROR ("Failed to write shutdown message to");
            }

          if (g_io_channel_flush (out_to_appletviewer, &channel_error)
              != G_IO_STATUS_NORMAL)
            {
              if (channel_error)
                {
                  PLUGIN_ERROR_TWO ("Failed to write shutdown message to"
                                    " appletviewer", channel_error->message);
                  g_error_free (channel_error);
                  channel_error = NULL;
                }
              else
                PLUGIN_ERROR ("Failed to write shutdown message to");
            }

          if (g_io_channel_shutdown (out_to_appletviewer,
                                     TRUE, &channel_error)
              != G_IO_STATUS_NORMAL)
            {
              if (channel_error)
                {
                  PLUGIN_ERROR_TWO ("Failed to shut down appletviewer"
                                    " output channel", channel_error->message);
                  g_error_free (channel_error);
                  channel_error = NULL;
                }
              else
                PLUGIN_ERROR ("Failed to shut down appletviewer");
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
                  PLUGIN_ERROR_TWO ("Failed to shut down appletviewer"
                                    " input channel", channel_error->message);
                  g_error_free (channel_error);
                  channel_error = NULL;
                }
              else
                PLUGIN_ERROR ("Failed to shut down appletviewer");
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

static void
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

  tofree->window_handle = NULL;
  tofree->window_height = 0;
  tofree->window_width = 0;

  // cleanup_appletviewer_mutex:
  g_free (tofree->appletviewer_mutex);
  tofree->appletviewer_mutex = NULL;

  // cleanup_instance_string:
  g_free (tofree->instance_id);
  tofree->instance_id = NULL;

  // cleanup applet tag
  g_free (tofree->applet_tag);
  tofree->applet_tag = NULL;

  g_free(tofree->source);
  tofree->source = NULL;

  // cleanup_data:
  // Eliminate back-pointer to plugin instance.
  tofree->owner = NULL;
  (*browser_functions.memfree) (tofree);
  tofree = NULL;

  PLUGIN_DEBUG ("plugin_data_destroy return\n");
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
NPError
NP_Initialize (NPNetscapeFuncs* browserTable, NPPluginFuncs* pluginTable)
{
  PLUGIN_DEBUG ("NP_Initialize\n");

  if ((browserTable == NULL) || (pluginTable == NULL))
  {
    PLUGIN_ERROR ("Browser or plugin function table is NULL.");

    return NPERR_INVALID_FUNCTABLE_ERROR;
  }

  // Ensure that the major version of the plugin API that the browser
  // expects is not more recent than the major version of the API that
  // we've implemented.
  if ((browserTable->version >> 8) > NP_VERSION_MAJOR)
    {
      PLUGIN_ERROR ("Incompatible version.");

      return NPERR_INCOMPATIBLE_VERSION_ERROR;
    }

  // Ensure that the plugin function table we've received is large
  // enough to store the number of functions that we may provide.
  if (pluginTable->size < sizeof (NPPluginFuncs))
    {
      PLUGIN_ERROR ("Invalid plugin function table.");

      return NPERR_INVALID_FUNCTABLE_ERROR;
    }

  // Ensure that the browser function table is large enough to store
  // the number of browser functions that we may use.
  if (browserTable->size < sizeof (NPNetscapeFuncs))
    {
      fprintf (stderr, "ERROR: Invalid browser function table. Some functionality may be restricted.\n");
    }

  // Store in a local table the browser functions that we may use.
  browser_functions.size                    = browserTable->size;
  browser_functions.version                 = browserTable->version;
  browser_functions.geturlnotify            = browserTable->geturlnotify;
  browser_functions.geturl                  = browserTable->geturl;
  browser_functions.posturlnotify           = browserTable->posturlnotify;
  browser_functions.posturl                 = browserTable->posturl;
  browser_functions.requestread             = browserTable->requestread;
  browser_functions.newstream               = browserTable->newstream;
  browser_functions.write                   = browserTable->write;
  browser_functions.destroystream           = browserTable->destroystream;
  browser_functions.status                  = browserTable->status;
  browser_functions.uagent                  = browserTable->uagent;
  browser_functions.memalloc                = browserTable->memalloc;
  browser_functions.memfree                 = browserTable->memfree;
  browser_functions.memflush                = browserTable->memflush;
  browser_functions.reloadplugins           = browserTable->reloadplugins;
  browser_functions.getJavaEnv              = browserTable->getJavaEnv;
  browser_functions.getJavaPeer             = browserTable->getJavaPeer;
  browser_functions.getvalue                = browserTable->getvalue;
  browser_functions.setvalue                = browserTable->setvalue;
  browser_functions.invalidaterect          = browserTable->invalidaterect;
  browser_functions.invalidateregion        = browserTable->invalidateregion;
  browser_functions.forceredraw             = browserTable->forceredraw;
  browser_functions.getstringidentifier     = browserTable->getstringidentifier;
  browser_functions.getstringidentifiers    = browserTable->getstringidentifiers;
  browser_functions.getintidentifier        = browserTable->getintidentifier;
  browser_functions.identifierisstring      = browserTable->identifierisstring;
  browser_functions.utf8fromidentifier      = browserTable->utf8fromidentifier;
  browser_functions.intfromidentifier       = browserTable->intfromidentifier;
  browser_functions.createobject            = browserTable->createobject;
  browser_functions.retainobject            = browserTable->retainobject;
  browser_functions.releaseobject           = browserTable->releaseobject;
  browser_functions.invoke                  = browserTable->invoke;
  browser_functions.invokeDefault           = browserTable->invokeDefault;
  browser_functions.evaluate                = browserTable->evaluate;
  browser_functions.getproperty             = browserTable->getproperty;
  browser_functions.setproperty             = browserTable->setproperty;
  browser_functions.removeproperty          = browserTable->removeproperty;
  browser_functions.hasproperty             = browserTable->hasproperty;
  browser_functions.hasmethod               = browserTable->hasmethod;
  browser_functions.releasevariantvalue     = browserTable->releasevariantvalue;
  browser_functions.setexception            = browserTable->setexception;
  browser_functions.pluginthreadasynccall   = browserTable->pluginthreadasynccall;
#if MOZILLA_VERSION_COLLAPSED >= 1090100
  browser_functions.getvalueforurl          = browserTable->getvalueforurl;
  browser_functions.setvalueforurl          = browserTable->setvalueforurl;
#endif

  // Return to the browser the plugin functions that we implement.
  pluginTable->version = (NP_VERSION_MAJOR << 8) + NP_VERSION_MINOR;
  pluginTable->size = sizeof (NPPluginFuncs);

#if MOZILLA_VERSION_COLLAPSED < 1090100
  pluginTable->newp = NewNPP_NewProc (ITNP_New);
  pluginTable->destroy = NewNPP_DestroyProc (ITNP_Destroy);
  pluginTable->setwindow = NewNPP_SetWindowProc (ITNP_SetWindow);
  pluginTable->newstream = NewNPP_NewStreamProc (ITNP_NewStream);
  pluginTable->destroystream = NewNPP_DestroyStreamProc (ITNP_DestroyStream);
  pluginTable->asfile = NewNPP_StreamAsFileProc (ITNP_StreamAsFile);
  pluginTable->writeready = NewNPP_WriteReadyProc (ITNP_WriteReady);
  pluginTable->write = NewNPP_WriteProc (ITNP_Write);
  pluginTable->print = NewNPP_PrintProc (ITNP_Print);
  pluginTable->urlnotify = NewNPP_URLNotifyProc (ITNP_URLNotify);
  pluginTable->getvalue = NewNPP_GetValueProc (ITNP_GetValue);
#else
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
#endif

  // Re-setting the above tables multiple times is OK (as the 
  // browser may change its function locations). However 
  // anything beyond this point should only run once.
  if (initialized)
    return NPERR_NO_ERROR;

  // Make sure the plugin data directory exists, creating it if
  // necessary.
  data_directory = g_strconcat (P_tmpdir, NULL);
  if (!data_directory)
    {
      PLUGIN_ERROR ("Failed to create data directory name.");
      return NPERR_OUT_OF_MEMORY_ERROR;
    }
  NPError np_error = NPERR_NO_ERROR;
  gchar* filename = NULL;

  // If P_tmpdir does not exist, try /tmp directly

  if (!g_file_test (data_directory,
                    (GFileTest) (G_FILE_TEST_EXISTS | G_FILE_TEST_IS_DIR)))
    {
      int file_error = 0;

      data_directory = g_strconcat ("/tmp", NULL);
        if (!data_directory)
          {
            PLUGIN_ERROR ("Failed to create data directory name.");
            return NPERR_OUT_OF_MEMORY_ERROR;
          }

    }

  data_directory = g_strconcat (data_directory, "/icedteaplugin-", getenv("USER"), NULL);

  if (!data_directory)
  {
      PLUGIN_ERROR ("Failed to create data directory name.");
      return NPERR_OUT_OF_MEMORY_ERROR;
  }

  // Now create a icedteaplugin subdir
  if (!g_file_test (data_directory,
                    (GFileTest) (G_FILE_TEST_EXISTS | G_FILE_TEST_IS_DIR)))
    {
      int file_error = 0;

      file_error = g_mkdir (data_directory, 0700);
      if (file_error != 0)
        {
          PLUGIN_ERROR_THREE ("Failed to create data directory",
                          data_directory,
                          strerror (errno));
          np_error = NPERR_GENERIC_ERROR;
          goto cleanup_data_directory;
        }
    }


  // If data directory doesn't exit by this point, bail
  if (!g_file_test (data_directory,
                    (GFileTest) (G_FILE_TEST_EXISTS | G_FILE_TEST_IS_DIR)))
    {
      PLUGIN_ERROR_THREE ("Temp directory does not exist: ",
                          data_directory,
                          strerror (errno));

      np_error = NPERR_GENERIC_ERROR;
            goto cleanup_data_directory;

    }

  // Set appletviewer_executable.
  filename = g_strdup(ICEDTEA_WEB_JRE);
  appletviewer_executable = g_strdup_printf ("%s/bin/java",
                                             filename);
  PLUGIN_DEBUG("Executing java at %s\n", appletviewer_executable);
  if (!appletviewer_executable)
    {
      PLUGIN_ERROR ("Failed to create appletviewer executable name.");
      np_error = NPERR_OUT_OF_MEMORY_ERROR;
      goto cleanup_filename;
    }

  np_error = plugin_test_appletviewer ();
  if (np_error != NPERR_NO_ERROR)
    {
      plugin_display_failure_dialog ();
      goto cleanup_appletviewer_executable;
    }
  g_free (filename);

  initialized = true;

  // Initialize threads (needed for mutexes).
  if (!g_thread_supported ())
    g_thread_init (NULL);

  plugin_instance_mutex = g_mutex_new ();

  PLUGIN_DEBUG ("NP_Initialize: using %s\n", appletviewer_executable);

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

 cleanup_appletviewer_executable:
  if (appletviewer_executable)
    {
      g_free (appletviewer_executable);
      appletviewer_executable = NULL;
    }

 cleanup_filename:
  if (filename)
    {
      g_free (filename);
      filename = NULL;
    }

 cleanup_data_directory:
  if (data_directory)
    {
      g_free (data_directory);
      data_directory = NULL;
    }


  return np_error;
}

// Returns a string describing the MIME type that this plugin
// handles.
char*
NP_GetMIMEDescription ()
{
  PLUGIN_DEBUG ("NP_GetMIMEDescription\n");

  PLUGIN_DEBUG ("NP_GetMIMEDescription return\n");

  return (char*) PLUGIN_MIME_DESC;
}

// Returns a value relevant to the plugin as a whole.  The browser
// calls this function to obtain information about the plugin.
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
      PLUGIN_ERROR ("Unknown plugin value requested.");
      result = NPERR_GENERIC_ERROR;
      break;
    }

  PLUGIN_DEBUG ("NP_GetValue return\n");

  return result;
}

// Shuts down the plugin.  Called after the last plugin instance is
// destroyed.
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

  if (data_directory)
    {
      g_free (data_directory);
      data_directory = NULL;
    }

  if (appletviewer_executable)
    {
      g_free (appletviewer_executable);
      appletviewer_executable = NULL;
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

  PLUGIN_DEBUG ("NP_Shutdown return\n");

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
            plugin_send_initialization_message(data->instance_id, 0, 0, 0, data->applet_tag);
        }

        java_result = java_request.getAppletObjectInstance(id_str);

        g_free(id_str);

        if (java_result->error_occurred)
        {
            printf("Error: Unable to fetch applet instance id from Java side.\n");
            return NULL;
        }

        instance_id.append(*(java_result->return_string));

        java_result = java_request.getClassID(instance_id);

        if (java_result->error_occurred)
        {
            printf("Error: Unable to fetch applet instance id from Java side.\n");
            return NULL;
        }

        applet_class_id.append(*(java_result->return_string));

        obj = IcedTeaScriptableJavaPackageObject::get_scriptable_java_object(instance, applet_class_id, instance_id, false);

    } else
    {
        obj = IcedTeaScriptablePluginObject::get_scriptable_java_package_object(instance, "");
    }

	return obj;
}

NPObject*
allocate_scriptable_object(NPP npp, NPClass *aClass)
{
	PLUGIN_DEBUG("Allocating new scriptable object\n");
	return new IcedTeaScriptablePluginObject(npp);
}
