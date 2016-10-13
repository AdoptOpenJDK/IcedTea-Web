/*   Copyright (C) 2016 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
 exception statement from your version.
 */
package net.sourceforge.jnlp.controlpanel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import javax.swing.JDialog;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * utility class to find any Interface implementing classes in netx/icedtea-web
 */
public class ClassFinder extends JDialog {

    public static final String JAVA_CLASS_PATH_PROPERTY = "java.class.path";
    public static final String CUSTOM_CLASS_PATH_PROPERTY = "custom.class.path";
    public static final String BOOT_CLASS_PATH_PROPERTY = "sun.boot.class.path";

    static public <T> List<Class<? extends T>> findAllMatchingTypes(Class<T> toFind) {
        List<Class<? extends T>> returnedClasses = new ArrayList<>();
        Set<Class> foundClasses = walkClassPath(toFind);
        for (Class<?> clazz : foundClasses) {
            if (!clazz.isInterface()) {
                returnedClasses.add((Class<? extends T>) clazz);
            }
        }
        return returnedClasses;
    }

    static private Set<Class> walkClassPath(Class toFind) {
        Set<Class> results = new HashSet<>();
        Set<String> classPathRoots = getClassPathRoots();
        for (String classpathEntry : classPathRoots) {
            //need to avoid base jdk jars/modules
            if (classpathEntry.toLowerCase().contains("icedtea-web")
                    || classpathEntry.toLowerCase().contains("netx")
                    || classpathEntry.toLowerCase().contains("plugin")) {
                File f = new File(classpathEntry);
                if (!f.exists()) {
                    continue;
                }
                if (f.isDirectory()) {
                    traverse(f.getAbsolutePath(), f, toFind, results);
                } else {
                    File jar = new File(classpathEntry);
                    try {
                        JarInputStream is = new JarInputStream(new FileInputStream(jar));
                        JarEntry entry;
                        while ((entry = is.getNextJarEntry()) != null) {
                            Class c = determine(entry.getName(), toFind);
                            if (c != null) {
                                results.add(c);
                            }
                        }
                    } catch (IOException ex) {
                        OutputController.getLogger().log(ex);
                    }
                }
            }
        }
        return results;
    }

    static private Set<String> getClassPathRoots() {
        String classapth1 = System.getProperty(CUSTOM_CLASS_PATH_PROPERTY);
        String classapth2 = System.getProperty(JAVA_CLASS_PATH_PROPERTY);
        String classapth3 = System.getProperty(BOOT_CLASS_PATH_PROPERTY);
        String classpath = "";
        if (classapth1 != null) {
            classpath = classpath + classapth1 + File.pathSeparator;
        }
        if (classapth2 != null) {
            classpath = classpath + classapth2 + File.pathSeparator;
        }
        if (classapth3 != null) {
            classpath = classpath + classapth3 + File.pathSeparator;
        }
        String[] pathElements = classpath.split(File.pathSeparator);
        Set<String> s = new HashSet<>(Arrays.asList(pathElements));
        return s;
    }

    static private Class determine(String name, Class toFind) {
        if (name.contains("$")) {
            return null;
        }
        try {
            if (name.endsWith(".class")) {
                name = name.replace(".class", "");
                name = name.replace("/", ".");
                name = name.replace("\\", ".");
                Class clazz = Class.forName(name);
                if (toFind.isAssignableFrom(clazz)) {
                    return clazz;
                }
            }
        } catch (Throwable ex) {
            //blacklisted classes
            //System.out.println(name);
        }
        return null;
    }

    static private void traverse(String root, File current, Class toFind, Set<Class> result) {
        File[] fs = current.listFiles();
        for (File f : fs) {
            if (f.isDirectory()) {
                traverse(root, f, toFind, result);
            } else {
                String ff = f.getAbsolutePath();
                String name = ff.substring(root.length());
                while (name.startsWith(File.separator)) {
                    name = name.substring(1);
                }
                Class c = determine(name, toFind);
                if (c != null) {
                    result.add(c);
                }
            }

        }
    }

}
