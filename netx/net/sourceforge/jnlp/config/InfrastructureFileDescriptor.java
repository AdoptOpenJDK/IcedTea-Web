/*
   Copyright (C) 2012 Red Hat

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

package net.sourceforge.jnlp.config;

import java.io.File;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.Translator;


public class InfrastructureFileDescriptor {
    private final String fileName;
    private final String pathStub;
    private final String systemPathStub;
    private final String descriptionKey;
    final PathsAndFiles.Target[] target;


    //simple constructor to allow testing instances based on overrides
    protected InfrastructureFileDescriptor() {
        this("undef", "undef", "undef", "undef");
    }
    
    InfrastructureFileDescriptor(String fileName, String pathStub, String systemPathStub, String descriptionKey, PathsAndFiles.Target... target) {
        this.fileName = fileName;
        this.pathStub = pathStub;
        this.systemPathStub = systemPathStub;
        this.descriptionKey = descriptionKey;
        this.target = target;
    }

    /** setup-able files have to override this
     * if they don't, they are read only, and set value will fail
     * if it is desired to write value of property, then override and use known key.
     * @return null by default. Should return key to configuration if overriden.
     */
    protected String getPropertiesKey() {
        return null;
    }

    public File getFile() {
        return new File(getFullPath());
    }

    public void setValue(String value) {
        setValue(value, JNLPRuntime.getConfiguration());
    }
    
      public String getFullPath() {
        return getFullPath(JNLPRuntime.getConfiguration());
    }
    
    /**
     * overload version for control panel, which is saving to internal copy.
     * @param value new path of file
     * @param config  config where t write this value (note, usually JNLPRuntime.getConfiguration()
     * so you don't need it, but our config gui tool is using two sets to allow undo.
     */
    public void setValue(String value, DeploymentConfiguration config) {
        String key = getPropertiesKey();
        if (key == null) {
            throw new IllegalStateException("This file is read only");
        } else {
            config.setProperty(key, value);
        }
    }
    
    /**
     * overload version for control panel, which is saving to internal copy.
     * @param config config from where to readthis value (note, usually JNLPRuntime.getConfiguration()
     * so you don't need it, but our config gui tool is using two sets to allow undo.
     * @return configured property or default
     */
    public String getFullPath(DeploymentConfiguration config) {
        String key = getPropertiesKey();
        if (key == null) {
            return getDefaultFullPath();
        } else {
            return config.getProperty(key);
        }
    }

    public File getDefaultFile() {
        return new File(getDefaultFullPath());
    }

    public String getDefaultDir() {
        return clean(systemPathStub + File.separator + pathStub);
    }

    public String getDefaultFullPath() {
        return clean(systemPathStub + File.separator + pathStub + File.separator + fileName);
    }

    //returns path acronym for default location
    protected String getSystemPathStubAcronym() {
        return systemPathStub;
    }

    protected String getFileName() {
        return fileName;
    }

    protected String getDescriptionKey() {
        return descriptionKey;
    }

    /**
     * This remaining part of file declaration, when acronym is removed.
     * See getDirViaAcronym.
     *
     * @return
     */
    private String getStub() {
        return clean(pathStub + File.separator + fileName);
    }

    @Override
    public String toString() {
        return clean(getSystemPathStubAcronym() + File.separator + getStub());
    }

    /**
     * For documentation purposes, the descriptor may be created as VARIABLE/custom/path.
     *
     * This is whole part, which is considered as setup-able.
     * @return directory acronym for nice docs
     */
    public String getDirViaAcronym() {
        return clean(getSystemPathStubAcronym() + File.separator + pathStub);
    }

    /**
     * Remove garbage from paths.
     *
     * Currently this methods unify all multiple occurrences of separators
     * to single one. Eg /path/to//file will become /path/to/file.
     *
     * Those artifacts maybe spread during various s=path+deparator+subdir+separator
     * file=s+separator+filename
     *
     * @param s string to be cleaned
     * @return cleaned string
     */
    protected String clean(String s) {
        while (s.contains(File.separator + File.separator)) {
            s = s.replace(File.separator + File.separator, File.separator);
        }
        return s;
    }

    /**
     * @return the translated description
     */
    public String getDescription() {
        return Translator.R(descriptionKey);
    }
    
}
