/* PacEvaluatorFactory.java
   Copyright (C) 2011 Red Hat, Inc.

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

package net.sourceforge.jnlp.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;
import net.sourceforge.jnlp.util.logging.OutputController;


public class PacEvaluatorFactory {

    public static PacEvaluator getPacEvaluator(URL pacUrl) {
        boolean useRhino = false;

        ClassLoader cl = PacEvaluatorFactory.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        InputStream in = cl.getResourceAsStream("net/sourceforge/jnlp/build.properties");
        Properties properties = null;
        try {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            OutputController.getLogger().log(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                OutputController.getLogger().log(e);
            }
        }

        if (properties == null) {
            return new FakePacEvaluator();
        }

        String available = properties.getProperty("rhino.available");
        useRhino = Boolean.valueOf(available);

        if (useRhino) {
            try {
                Class<?> evaluator = Class.forName("net.sourceforge.jnlp.runtime.RhinoBasedPacEvaluator");
                Constructor<?> constructor = evaluator.getConstructor(URL.class);
                return (PacEvaluator) constructor.newInstance(pacUrl);
            } catch (ClassNotFoundException e) {
                // ignore
            } catch (InstantiationException e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            } catch (IllegalAccessException e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            } catch (NoSuchMethodException e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            } catch (IllegalArgumentException e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            } catch (InvocationTargetException e) {
                if (e.getCause() != null) {
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e.getCause());
                }
            }
        }

        return new FakePacEvaluator();
    }
}
