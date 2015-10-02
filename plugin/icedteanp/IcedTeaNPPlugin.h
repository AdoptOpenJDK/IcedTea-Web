/* IcedTeaNPPlugin.h

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

#ifndef __ICEDTEANPPLUGIN_H__
#define	__ICEDTEANPPLUGIN_H__

#include <npapi.h>
#include <npruntime.h>
#include <npfunctions.h>

// GLib includes.
#include <glib.h>
#include <glib/gstdio.h>

#include "IcedTeaPluginUtils.h"
#include "IcedTeaPluginRequestProcessor.h"

// ITNPPluginData stores all the data associated with a single plugin
// instance.  A separate plugin instance is created for each <APPLET>
// tag.  For now, each plugin instance spawns its own applet viewer
// process but this may need to change if we find pages containing
// multiple applets that expect to be running in the same VM.
struct ITNPPluginData
{
  // A unique identifier for this plugin window.
  gchar* instance_id;
  // The parameter list string sent to Java side
  gchar* parameters_string;
  // Mutex to protect appletviewer_alive.
  GMutex* appletviewer_mutex;
  // Back-pointer to the plugin instance to which this data belongs.
  // This should not be freed but instead simply set to NULL.
  NPP owner;
  // The address of the plugin window.  This should not be freed but
  // instead simply set to NULL.
  gpointer window_handle;
  // The last plugin window width sent to us by the browser.
  guint32 window_width;
  // The last plugin window height sent to us by the browser.
  guint32 window_height;
  // The source location for this instance
  std::string source;
  // If this is an actual applet instance, or a dummy instance for static calls
  bool is_applet_instance;

  ITNPPluginData() {
      instance_id = NULL;
      parameters_string = NULL;
      appletviewer_mutex = NULL;
      owner = (NPP)NULL;
      window_handle = NULL;
      window_width = 0;
      window_height = 0;
      is_applet_instance = false;
  }
  ~ITNPPluginData() {
      if (appletviewer_mutex) {
        g_mutex_free (appletviewer_mutex);
      }
      // cleanup_instance_string:
      g_free (instance_id);
      // cleanup applet tag
      g_free (parameters_string);
  }
};

// Have the browser allocate a new ITNPPluginData structure.
ITNPPluginData* plugin_data_new ();
void plugin_data_destroy (NPP instance);

NPError initialize_data_directory();
NPError start_jvm_if_needed();

// ID of plug-in thread
extern pthread_t itnp_plugin_thread_id;

/* Mutex around plugin async call queue ops */
extern pthread_mutex_t pluginAsyncCallMutex;

/*to sync pipe to apletviewer console*/
extern pthread_mutex_t debug_pipe_lock;

// debug switches
extern bool debug_initiated;
extern bool file_logs_initiated;
extern int plugin_debug;
extern bool plugin_debug_headers;
extern bool plugin_debug_to_file;
extern bool plugin_debug_to_streams;
extern bool plugin_debug_to_system;
extern bool plugin_debug_to_console;
extern FILE * plugin_file_log;
extern std::string plugin_file_log_name;
extern gchar* debug_pipe_name;

extern gboolean jvm_up;

// Browser function table.
extern NPNetscapeFuncs browser_functions;

// messages to the java side
extern MessageBus* plugin_to_java_bus;

// messages from the java side
extern MessageBus* java_to_plugin_bus;

// internal messages (e.g ones that need processing in main thread)
//extern MessageBus* internal_bus;

// subscribes to plugin_to_java_bus and sends messages over the link
extern JavaMessageSender java_request_processor;

// processes requests made to the plugin
extern PluginRequestProcessor plugin_request_processor;

/* Given an instance pointer, return its id */
void get_instance_from_id(int id, NPP& instance);

/* Given an instance id, return its pointer */
int get_id_from_instance(NPP instance);

/* Sends a message to the appletviewer */
void plugin_send_message_to_appletviewer(gchar const* message);
/*this method is not logging, do not add \n and is using different pipe*/
void plugin_send_message_to_appletviewer_console(gchar const* message);
void flush_plugin_send_message_to_appletviewer_console();

/* Returns an appropriate (package/object) scriptable npobject */
NPObject* get_scriptable_object(NPP instance);

/* Creates a new scriptable plugin object and returns it */
NPObject* allocate_scriptable_object(NPP npp, NPClass *aClass);

NPError plugin_start_appletviewer (ITNPPluginData* data);

#endif	/* __ICEDTEANPPLUGIN_H__ */
