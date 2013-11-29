/* IcedTeaPluginUtils.h

   Copyright (C) 2013  Red Hat

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
 * Utility classes for parsing values from properties files
 */
#include <string>
#include <glib.h>

//public api
std::string  user_properties_file();
std::string  get_log_dir();
bool  find_system_config_file(std::string& dest);
bool  find_custom_jre(std::string& dest);
bool  read_deploy_property_value(std::string property, std::string& dest);
bool  is_debug_on();
bool  is_debug_header_on();
bool  is_logging_to_file();
bool  is_logging_to_stds();
bool  is_logging_to_system();
bool  is_java_console_enabled();
//half public api
extern const std::string default_file_ITW_deploy_props_name;
extern const std::string default_itw_log_dir_name;
extern const std::string custom_jre_key;
//end of public api
