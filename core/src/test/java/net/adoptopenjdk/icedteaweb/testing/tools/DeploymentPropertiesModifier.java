/*
   Copyright (C) 2015 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.testing.tools;

import net.adoptopenjdk.icedteaweb.BasicFileUtils;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.util.FileUtils;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeploymentPropertiesModifier {
   
    
    /**
     * for advanced users, less verbose, less fool-proof multi-impl
     */
    public static class MultipleDeploymentPropertiesModifier {

        public final InfrastructureFileDescriptor src;
        private final AbstractMap.SimpleEntry<String, String>[] keyValue;
        private List<DeploymentPropertiesModifier> modifications;

        @SafeVarargs
        public MultipleDeploymentPropertiesModifier(final InfrastructureFileDescriptor src, final HashMap.SimpleEntry<String, String>... keyValue) {
            this.src = src;
            this.keyValue = keyValue;
        }

        public void setProperties() throws IOException {
            modifications = new ArrayList<>(keyValue.length);
            for (final AbstractMap.SimpleEntry<String, String> keyValue1 : keyValue) {
                final DeploymentPropertiesModifier dm = new DeploymentPropertiesModifier(src);
                dm.setProperties(keyValue1.getKey(), keyValue1.getValue());
                //adding to beginning, so restoring goes from last. Not necessary, but nice.
                modifications.add(0, dm);
            }
        }

        public void restoreProperties() throws IOException {
            for (final DeploymentPropertiesModifier dm : modifications) {
                dm.restoreProperties();
            }
        }

    }

    private final InfrastructureFileDescriptor src;
    private String savedValue;
    private String requestedProperty;
    private String requestedValue;
    private boolean isPropertiesSet;

    public DeploymentPropertiesModifier(final InfrastructureFileDescriptor src) {
        this.src = src;
        isPropertiesSet = false;
    }

    public void setProperties(final String property, final String value) throws IOException {
        if (isPropertiesSet) {
            throw new IllegalStateException("Properties can be set only once. Revert and use another instance.");            
        }
        isPropertiesSet = true;
        requestedProperty = property;
        requestedValue = value;

        setDeploymentProperties(requestedProperty, requestedValue);
    }

    public void restoreProperties() throws IOException {
        if (!isPropertiesSet) {
            throw new IllegalStateException("Properties must be set before they can be reverted");
        }
        isPropertiesSet = false;

        restoreDeploymentProperties();
    }

    private void setDeploymentProperties(final String property, final String value) throws IOException {
        String properties = FileUtils.loadFileAsString(src.getFile());

        for (final String line : properties.split("\n")) {
            if (line.contains(property)) {
                savedValue = line;
                properties = properties.replace(line, property + "=" + value + "\n");
            }
        }

        if (savedValue == null) {
            properties += property + "=" + value + "\n";
        }

        BasicFileUtils.saveFile(properties, src.getFile());
    }

    private void restoreDeploymentProperties() throws IOException {
        String properties = FileUtils.loadFileAsString(src.getFile());
        if (savedValue != null) {
            properties = properties.replace(requestedProperty + "=" + requestedValue + "\n", savedValue);
        } else {
            properties = properties.replace(requestedProperty + "=" + requestedValue + "\n", "");
        }

        BasicFileUtils.saveFile(properties, src.getFile());
    }

}
