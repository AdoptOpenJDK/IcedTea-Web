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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;


public class PacEvaluatorFactory {

    private final static Logger LOG = LoggerFactory.getLogger(PacEvaluatorFactory.class);

    public static PacEvaluator getPacEvaluator(URL pacUrl) {
        boolean useRhino = false;

        ClassLoader cl = PacEvaluatorFactory.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        Properties properties = null;
        try (InputStream in = cl.getResourceAsStream("net/sourceforge/jnlp/build.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (Exception e) {
            LOG.error("PAC provider is broken or don't exists. This is ok unless your application is using JavaScript.", e);
        }

        if (properties == null) {
            LOG.debug("Build properties are null, create {}", FakePacEvaluator.class.getSimpleName());
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
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            } catch (IllegalAccessException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            } catch (NoSuchMethodException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            } catch (IllegalArgumentException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            } catch (InvocationTargetException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            }
        }

        LOG.debug("Rhino-based PAC evaluator not available.");
        return new FakePacEvaluator();
    }
}
