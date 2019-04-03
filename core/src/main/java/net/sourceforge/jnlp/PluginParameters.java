/* PluginAppletAttributes -- Provides parsing for applet attributes
   Copyright (C) 2012  Red Hat

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

package net.sourceforge.jnlp;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static net.sourceforge.jnlp.runtime.Translator.R;

/**
 * Represents plugin applet parameters, backed by a HashMap.
 */

public class PluginParameters {
    private final Map<String, String> parameters;

    public PluginParameters(Map<String, String> params) {
        this.parameters = createParameterTable(params);

        if (this.parameters.get("code") == null
                && this.parameters.get("object") == null
                //If code/object parameters are missing, we can still determine the main-class name from the jnlp file passed using jnlp_href
                && this.parameters.get("jnlp_href") == null) {
            throw new PluginParameterException(R("BNoCodeOrObjectApplet"));
        }
    }

    // Note, lower-case key expected
    public String get(String key) {
        return this.parameters.get(key);
    }

    public void put(String key, String value) {
        parameters.put(key.toLowerCase(), value);
    }

    public Map<String, String> getUnmodifiableMap() {
        return Collections.unmodifiableMap(parameters);
    }

    public  Map<String, String> getUnderlyingMap() {
        return parameters;
    }

    public String getDefaulted(String key, String defaultStr) {
        String value = get(key);
        return (value != null) ? value : defaultStr;
    }

    public String getAppletTitle() {
        String name = get("name");
        if (name == null) {
            return "Applet";
        } else {
            return name + " applet";
        }
    }

    public boolean useCodebaseLookup() {
        return Boolean.valueOf(getDefaulted("codebase_lookup", "true"));
    }

    public String getArchive() {
        return getDefaulted("archive", "");
    }

    public String getJavaArchive() {
        return getDefaulted("java_archive", "");
    }

    public String getJavaArguments() {
        return getDefaulted("java_arguments", "");
    }

    public String getCacheArchive() {
        return getDefaulted("cache_archive", "");
    }

    public String getCacheArchiveEx() {
        return getDefaulted("cache_archive_ex", "");
    }

    public String getCacheOption() {
        return getDefaulted("cache_option", "");
    }

    public String getCacheVersion() {
        return getDefaulted("cache_version", "");
    }

    public String getCode() {
        return getDefaulted("code", "");
    }

    public String getJNLPHref() {
        return get("jnlp_href");
    }

    public String getJNLPEmbedded() {
        return get("jnlp_embedded");
    }

    public String getJarFiles() {
        return getDefaulted("archive", "");
    }

    public int getWidth() {
        String widthStr = getDefaulted("width", "0");
        return Integer.valueOf(widthStr);
    }

    public int getHeight() {
        String heightStr = getDefaulted("height", "0");
        return Integer.valueOf(heightStr);
    }

    public String getPermissions() {
        return get("permissions");
    }

    public void updateSize(int width, int height) {
        parameters.put("width", Integer.toString(width));
        parameters.put("height", Integer.toString(height));
    }

    public String getUniqueKey(URL codebase) {
        /* According to http://download.oracle.com/javase/6/docs/technotes/guides/deployment/deployment-guide/applet-compatibility.html, 
        * classloaders are shared iff these properties match:
        * codebase, cache_archive, java_archive, archive
        * 
        * To achieve this, we create the uniquekey based on those 4 values,
        * always in the same order. The initial "<NAME>=" parts ensure a 
        * bad tag cannot trick the loader into getting shared with another.
        */
        return "codebase=" + codebase.toExternalForm() + "cache_archive="
                + getCacheArchive() + "java_archive=" + getJavaArchive()
                + "archive=" + getArchive();
    }

    /**
     * Replace an attribute with its 'java_'-prefixed version.      
     * Note that java_* aliases override older names:
     * http://java.sun.com/j2se/1.4.2/docs/guide/plugin/developer_guide/using_tags.html#in-nav
     */
    static void ensureJavaPrefixTakesPrecedence(Map<String, String> params,
            String attribute) {
        String javaPrefixAttribute = params.get("java_" + attribute);
        if (javaPrefixAttribute != null) {
            params.put(attribute, javaPrefixAttribute);
        }
    }

    /**
     * Creates the underlying hash table with the proper overrides. Ensure all
     * keys are lowercase consistently.
     * 
     * @param rawParams the properties, before parameter aliasing rules.
     * @return the resulting parameter table
     */
    static Map<String, String> createParameterTable(
            Map<String, String> rawParams) {
        Map<String, String> params = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : rawParams.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();
            params.put(key, value);
        }

        String codeTag = params.get("code");
        String classID = params.get("classid");

        // If there is a classid and no code tag present, transform it to code tag
        if (codeTag == null && classID != null && !classID.startsWith("clsid:")) {
            codeTag = classID;
            params.put("code", codeTag);
        }

        // remove java: from code tag
        if (codeTag != null && codeTag.startsWith("java:")) {
            codeTag = codeTag.substring("java:".length());
            params.put("code", codeTag);
        }

        // java_* aliases override older names:
        // http://java.sun.com/j2se/1.4.2/docs/guide/plugin/developer_guide/using_tags.html#in-nav
        ensureJavaPrefixTakesPrecedence(params, "code");
        ensureJavaPrefixTakesPrecedence(params, "codebase");
        ensureJavaPrefixTakesPrecedence(params, "archive");
        ensureJavaPrefixTakesPrecedence(params, "object");
        ensureJavaPrefixTakesPrecedence(params, "type");

        return params;
    }

    public String toString() {
        return parameters.toString();
    }
}
