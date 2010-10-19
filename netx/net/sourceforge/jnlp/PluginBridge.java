/*
 * Copyright 2007 Red Hat, Inc.
 * This file is part of IcedTea, http://icedtea.classpath.org
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package net.sourceforge.jnlp;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import net.sourceforge.jnlp.runtime.JNLPRuntime;


public class PluginBridge extends JNLPFile
{

    String name;
    String[] jars = new String[0];
    String[] cache_jars = new String[0];
    String[] cache_ex_jars = new String[0];
    Hashtable atts;

    public PluginBridge(URL codebase, URL documentBase, String jar, String main,
                        int width, int height, Hashtable atts)
    throws Exception
    {
        specVersion = new Version("1.0");
        fileVersion = new Version("1.1");
        this.codeBase = codebase;
        this.sourceLocation = documentBase;

        // also, see if cache_archive is specified
        if (atts.get("cache_archive") != null && ((String) atts.get("cache_archive")).length() > 0) {

            String[] versions = new String[0];

            // are there accompanying versions?
            if (atts.get("cache_version") != null) {
                versions = ((String) atts.get("cache_version")).split(",");
            }

            String[] jars = ((String) atts.get("cache_archive")).split(",");
            cache_jars = new String[jars.length];

            for (int i=0; i < jars.length; i++) {

                cache_jars[i] = jars[i].trim();

                if (versions.length > 0) {
                    cache_jars[i] += ";" + versions[i].trim();
                }
            }
        }

        if (atts.get("cache_archive_ex") != null && ((String) atts.get("cache_archive_ex")).length() > 0) {
            cache_ex_jars = ((String) atts.get("cache_archive_ex")).split(",");
        }

        if (jar != null && jar.length() > 0) {
            this.jars = jar.split(",");
            if (JNLPRuntime.isDebug()) {
                System.err.println("Jar string: " + jar);
                System.err.println("jars length: " + jars.length);
            }
        }
        this.atts = atts;

        name = (String) atts.get("name");
        if (name == null)
            name = "Applet";
        else
            name = name + " applet";

        if (main.endsWith(".class"))
            main = main.substring(0, main.length() - 6);

        launchType = new AppletDesc(name, main, documentBase, width,
                                    height, atts);

        if (main.endsWith(".class")) //single class file only
            security = new SecurityDesc(this, SecurityDesc.SANDBOX_PERMISSIONS,
                                        codebase.getHost());
        else
            security = null;

        this.uniqueKey = Calendar.getInstance().getTimeInMillis() + "-" +
                         Math.abs(((new java.util.Random()).nextInt())) + "-" +
                         documentBase;
    }

    public String getTitle()
    {
        return name;
    }

    public InformationDesc getInformation(final Locale locale)
    {
        return new InformationDesc(this, new Locale[] {locale}) {
            protected List getItems(Object key)
            {
                // Should we populate this list with applet attribute tags?
                List result = new ArrayList();
                return result;
            }
        };
    }

    public ResourcesDesc getResources(final Locale locale, final String os,
                                      final String arch)
    {
        return new ResourcesDesc(this, new Locale[] {locale}, new String[] {os},
        new String[] {arch}) {
            public List getResources(Class launchType)
            {
                List result = new ArrayList();
                result.addAll(sharedResources.getResources(launchType));

                // Need to add the JAR manually...
                //should this be done to sharedResources on init?
                try
                {
                    if (launchType.equals(JARDesc.class))
                    {
                        for (int i = 0; i < jars.length; i++)
                            if (jars[i].length() > 0)
                                result.add(new JARDesc(new URL(codeBase, jars[i]),
                                        null, null, false, true, false, true));

                        boolean cacheable = true;

                        if (atts.get("cache_option") != null &&
                                ((String) atts.get("cache_option")).equalsIgnoreCase("no"))
                            cacheable = false;

                        for (int i = 0; i < cache_jars.length; i++) {

                            String[] jar_and_ver = cache_jars[i].split(";");

                            String jar = jar_and_ver[0];
                            Version version = null;

                            if (jar.length() == 0)
                                continue;

                            if (jar_and_ver.length > 1) {
                                version = new Version(jar_and_ver[1]);
                            }

                            result.add(new JARDesc(new URL(codeBase, jar),
                                    version, null, false, true, false, cacheable));
                        }

                        for (int i = 0; i < cache_ex_jars.length; i++) {

                            if (cache_ex_jars[i].length() == 0)
                                continue;

                            String[] jar_info = cache_ex_jars[i].split(";");

                            String jar = jar_info[0].trim();
                            Version version = null;
                            boolean lazy = true;

                            if (jar_info.length > 1) {

                                // format is name[[;preload];version]

                                if (jar_info[1].equals("preload")) {
                                    lazy = false;
                                } else {
                                    version = new Version(jar_info[1].trim());
                                }

                                if (jar_info.length > 2) {
                                    lazy = false;
                                    version = new Version(jar_info[2].trim());
                                }
                            }

                            result.add(new JARDesc(new URL(codeBase, jar),
                                    version, null, lazy, true, false, false));
                        }
                    }
                }
                catch (MalformedURLException ex)
                    { }
                return result;
            }

            public JARDesc[] getJARs() {
                List resources = getResources(JARDesc.class);
                ArrayList<JARDesc> jars = new ArrayList<JARDesc>();

                //Only get the JARDescs
                for (int i = 0; i < resources.size(); i++) {
                    Object resource = resources.get(i);
                    if (resource instanceof JARDesc)
                        jars.add((JARDesc) resource);
                }

                Object[] objectArray = jars.toArray();
                JARDesc[] jarArray = new JARDesc[objectArray.length];

                for (int i = 0; i < objectArray.length; i++)
                    jarArray[i] = (JARDesc) objectArray[i];

                return jarArray;
            }

            public void addResource(Object resource)
            {
                // todo: honor the current locale, os, arch values
                sharedResources.addResource(resource);
            }

        };
    }

    public boolean isApplet() {
        return true;
    }
    public boolean isApplication() {
        return false;
    }
    public boolean isComponent() {
        return false;
    }
    public boolean isInstaller() {
        return false;
    }
}
