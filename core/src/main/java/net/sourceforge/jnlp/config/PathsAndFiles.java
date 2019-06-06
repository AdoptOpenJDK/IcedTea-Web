/* 
   Copyright (C) 2008 Red Hat, Inc.

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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.config.FilesystemConfiguration;
import net.adoptopenjdk.icedteaweb.config.Target;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;

public class PathsAndFiles {

    private final static Logger LOG = LoggerFactory.getLogger(PathsAndFiles.class);


    public static final InfrastructureFileDescriptor MOZILA_USER = new HomeFileDescriptor(Target.PLUGIN);

    public static final InfrastructureFileDescriptor MOZILA_GLOBAL_64 = new InfrastructureFileDescriptor(ConfigurationConstants.ICEDTEA_SO, "/usr/lib64/mozilla/plugins/", "",  "FILEmozillaglobal64", Target.PLUGIN);

    public static final InfrastructureFileDescriptor MOZILA_GLOBAL_32 = new InfrastructureFileDescriptor(ConfigurationConstants.ICEDTEA_SO, "/usr/lib/mozilla/plugins/", "",  "FILEmozillaglobal32", Target.PLUGIN);

    public static final InfrastructureFileDescriptor OPERA_64 = new InfrastructureFileDescriptor(ConfigurationConstants.ICEDTEA_SO, "/usr/lib64/opera/plugins/", "",  "FILEopera64", Target.PLUGIN);

    public static final InfrastructureFileDescriptor OPERA_32 = new InfrastructureFileDescriptor(ConfigurationConstants.ICEDTEA_SO, "/usr/lib/opera/plugins/", "",  "FILEopera32", Target.PLUGIN);
    
    public static final InfrastructureFileDescriptor CACHE_DIR = new ItwCacheFileDescriptor("cache", "FILEcache", Target.JAVAWS, Target.ITWEB_SETTINGS) {
    
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_CACHE_DIR;
        }

    };
    

    // this one is depending on CACHE_DIR, so initialize it lazily
    public static InfrastructureFileDescriptor getRecentlyUsedFile() {
        return RECENTLY_USED_FILE_HOLDER.RECENTLY_USED_FILE;
    }

    private static class RECENTLY_USED_FILE_HOLDER {
        static final InfrastructureFileDescriptor RECENTLY_USED_FILE = new ItwCacheFileDescriptor(ConfigurationConstants.CACHE_INDEX_FILE_NAME, CACHE_DIR.getFile().getName(), "FILErecentlyUsed", Target.JAVAWS, Target.ITWEB_SETTINGS){

            @Override
            public String getFullPath() {
                return clean(CACHE_DIR.getFullPath()+File.separator+this.getFileName());
            }
          
        };
    }
    
    public static final InfrastructureFileDescriptor PCACHE_DIR = new ItwCacheFileDescriptor("pcache", "FILEappdata", Target.JAVAWS, Target.ITWEB_SETTINGS){

        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_PERSISTENCE_CACHE_DIR;
        }
    };
    public static final InfrastructureFileDescriptor LOG_DIR = new ItwConfigFileDescriptor("log", "FILElogs", Target.JAVAWS, Target.ITWEB_SETTINGS){

        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_LOG_DIR;
        }
    
        
    };
    //javaws is saving here, itweb-settings may modify them
    public static final InfrastructureFileDescriptor ICONS_DIR = new ItwConfigFileDescriptor("icons", "FILEicons", Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor GEN_JNLPS_DIR = new ItwConfigFileDescriptor("generated_jnlps", "FILEjnlps", Target.PLUGIN, Target.ITWEB_SETTINGS);
    //javaws is saving here, itweb-settings may modify them
    public static final InfrastructureFileDescriptor MENUS_DIR = new MenuFileDescriptor(Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor APPLET_TRUST_SETTINGS_USER = new ItwConfigFileDescriptor(ConfigurationConstants.APPLET_TRUST_SETTINGS, "FILEextasuser", Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor APPLET_TRUST_SETTINGS_SYS = new SystemDeploymentConfigFileDescriptor(ConfigurationConstants.APPLET_TRUST_SETTINGS, "FILEextasadmin", Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor ETC_DEPLOYMENT_CFG = new SystemDeploymentConfigFileDescriptor(ConfigurationConstants.DEPLOYMENT_CONFIG_FILE, "FILEglobaldp", Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor TMP_DIR = new ItwCacheFileDescriptor("tmp", "FILEtmpappdata", Target.JAVAWS, Target.ITWEB_SETTINGS){
        
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_TMP_DIR;
        }
        
    };
    public static final InfrastructureFileDescriptor LOCKS_DIR = new TmpUsrFileDescriptor("locks", "netx", "FILElocksdir") {

        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_LOCKS_DIR;
        }
        
    };
    public static final InfrastructureFileDescriptor MAIN_LOCK = new TmpUsrFileDescriptor("netx_running", "netx" + File.separator + "locks", "FILEmainlock") {

        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_NETX_RUNNING_FILE;
        }
        
    };
    public static final InfrastructureFileDescriptor JAVA_POLICY = new UserSecurityConfigFileDescriptor("java.policy", "FILEpolicy", Target.JAVAWS, Target.POLICY_EDITOR){

                   @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_SECURITY_POLICY;
        }

        @Override
        public File getFile() {
            throw new IllegalStateException("Only getFullPath should be used. This is returning URL String.");
        }
    };
    public static final InfrastructureFileDescriptor USER_CACERTS = new UserCacertsFileDescriptor("trusted.cacerts") {
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_TRUSTED_CA_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor USER_JSSECAC = new UserCacertsFileDescriptor("trusted.jssecacerts") {
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_TRUSTED_JSSE_CA_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor USER_CERTS = new UserCacertsFileDescriptor("trusted.certs") {
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_TRUSTED_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor USER_JSSECER = new UserCacertsFileDescriptor("trusted.jssecerts") {
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_TRUSTED_JSSE_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor USER_CLIENTCERT = new UserCacertsFileDescriptor("trusted.clientcerts") {
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_USER_TRUSTED_CLIENT_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor SYS_CACERT = new SystemJavaSecurityFileDescriptor("cacerts") {
        
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_SYSTEM_TRUSTED_CA_CERTS;
        }
        
    };
    public static final InfrastructureFileDescriptor SYS_JSSECAC = new SystemJavaSecurityFileDescriptor("jssecacerts") {
      
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor SYS_CERT = new SystemJavaSecurityFileDescriptor("trusted.certs"){
        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_SYSTEM_TRUSTED_CERTS;
        }
    };
    public static final InfrastructureFileDescriptor SYS_JSSECERT = new SystemJavaSecurityFileDescriptor("trusted.jssecerts") {

        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_SYSTEM_TRUSTED_JSSE_CERTS;
        }
        
    };
    public static final InfrastructureFileDescriptor SYS_CLIENTCERT = new SystemJavaSecurityFileDescriptor("trusted.clientcerts") {

        @Override
        public String getPropertiesKey() {
            return ConfigurationConstants.KEY_SYSTEM_TRUSTED_CLIENT_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor JAVA_DEPLOYMENT_PROP_FILE = new SystemJavaLibFileDescriptor(Target.JAVAWS, Target.ITWEB_SETTINGS) {

        @Override
        public String getDescription() {
             return Translator.R(getDescriptionKey(), ConfigurationConstants.KEY_JRE_DIR);
        }
        
    };
    public static final InfrastructureFileDescriptor USER_DEPLOYMENT_FILE = new ItwConfigFileDescriptor(ConfigurationConstants.DEPLOYMENT_PROPERTIES, "FILEuserdp", Target.JAVAWS, Target.ITWEB_SETTINGS);

    public static List<InfrastructureFileDescriptor> getAllFiles() {
        return getAllFiles(null);
    }


    private static List<InfrastructureFileDescriptor> getAllFiles(Target desired) {
        return getAllFiles(desired, InfrastructureFileDescriptor.class);
    }
    private static List<InfrastructureFileDescriptor> getAllFiles(Target desired, Class c) {
        List<InfrastructureFileDescriptor> r = new ArrayList<>();
        Field[] all = PathsAndFiles.class.getDeclaredFields();
        for (Field field : all) {
            try {
                Object o = field.get(null);
                if (c.isInstance(o)) {
                    InfrastructureFileDescriptor i = (InfrastructureFileDescriptor) o;
                    for (Target target : i.target) {
                        if (desired == null || target == desired) {
                            r.add(i);
                            break;
                        }
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            }

        }
        r.add(getRecentlyUsedFile());
        return r;
    }

    public static List<InfrastructureFileDescriptor> getAllJavaWsFiles() {
        return getAllFiles(Target.JAVAWS);
    }

    public static List<InfrastructureFileDescriptor> getAllItWebSettingsFiles() {
        return getAllFiles(Target.ITWEB_SETTINGS);
    }

    public static List<InfrastructureFileDescriptor> getAllPEFiles() {
        return getAllFiles(Target.POLICY_EDITOR);
    }

    public static List<InfrastructureFileDescriptor> getAllPluginFiles() {
        return getAllFiles(Target.PLUGIN);
    }


    private static class HomeFileDescriptor extends InfrastructureFileDescriptor {

        private HomeFileDescriptor(Target... target) {
            super(ConfigurationConstants.ICEDTEA_SO, ".mozilla/plugins", System.getProperty(ConfigurationConstants.HOME_PROP), "FILEmozillauser", target);
        }
              @Override
        public String getSystemPathStubAcronym() {
            return FilesystemConfiguration.getVariablePrefix() + "HOME";
        }


    }

    private static class SystemConfigFileDescriptor extends InfrastructureFileDescriptor {

        private static final String windowsPathSuffix = File.separator + "Sun" + File.separator + "Java";
        private static final String unixPathSuffix = File.separator + "etc" + File.separator + ".java";

        private static String getSystemConfigDir() {
            if (OsUtil.isWindows()) {
                return System.getenv(ConfigurationConstants.WINDIR) + windowsPathSuffix;
            } else {
                return unixPathSuffix;
            }
        }

        @Override
        public String getSystemPathStubAcronym() {
            //note the hardcoded % instead of VARIABLE (actually leading to idea, that docs, when generated on windows may not make sense)
            return "{" + "%" + ConfigurationConstants.WINDIR + windowsPathSuffix + " or " + unixPathSuffix + "}";
        }

        private SystemConfigFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, "deployment", getSystemConfigDir(), description, target);
        }

    }

    private static class SystemDeploymentConfigFileDescriptor extends SystemConfigFileDescriptor {

        private SystemDeploymentConfigFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, description, target);
        }

    }

    private static class SystemJavaFileDescriptor extends InfrastructureFileDescriptor {

        private SystemJavaFileDescriptor(String fileName, String pathSub, String description, Target... target) {
            super(fileName, pathSub, System.getProperty(ConfigurationConstants.JAVA_PROP), description, target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return FilesystemConfiguration.getVariablePrefix() + "JAVA_HOME";
        }

    }

    private static class SystemJavaLibFileDescriptor extends SystemJavaFileDescriptor {

        private SystemJavaLibFileDescriptor(Target... target) {
            super(ConfigurationConstants.DEPLOYMENT_CONFIG_FILE, "lib", "FILEjavadp", target);
        }

        private SystemJavaLibFileDescriptor(String fileName, Target... target) {
            super(fileName, "lib" + File.separator + ConfigurationConstants.SECURITY_WORD, "FILEjavacerts", target);
        }

    }

    private static class SystemJavaSecurityFileDescriptor extends SystemJavaLibFileDescriptor {

        private SystemJavaSecurityFileDescriptor(String fileName) {
            super(fileName, Target.JAVAWS);
        }

    }
    
    private static class DataFileDescriptor extends InfrastructureFileDescriptor {

        private DataFileDescriptor(String fileName, Target... target) {
            super(fileName, "applications", FilesystemConfiguration.getDataHome(), "FILEmenus", target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return FilesystemConfiguration.getVariablePrefix() + "" + ConfigurationConstants.XDG_DATA_HOME_VAR;
        }

    }

    /**
     * http://standards.freedesktop.org/menu-spec/menu-spec-1.0.html#paths
     */
    private static class MenuFileDescriptor extends DataFileDescriptor {

        private MenuFileDescriptor(Target... target) {
            super(JAVAWS, target);
        }
    }


    private static class RuntimeFileDescriptor extends InfrastructureFileDescriptor {

        private RuntimeFileDescriptor(Target... target) {
            super("icedteaplugin-user-*", FilesystemConfiguration.getRuntimeHome(), "", "FILEpipe", target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return FilesystemConfiguration.getVariablePrefix() + "" + ConfigurationConstants.XDG_RUNTIME_DIR_VAR;
        }

    }

    private static class ConfigFileDescriptor extends InfrastructureFileDescriptor {

        private ConfigFileDescriptor(String fileName, String pathStub, String description, Target... target) {
            super(fileName, pathStub, FilesystemConfiguration.getConfigHome(), description, target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return FilesystemConfiguration.getVariablePrefix() + "" + ConfigurationConstants.XDG_CONFIG_HOME_VAR;
        }

    }

    private static class CacheFileDescriptor extends InfrastructureFileDescriptor {

        private CacheFileDescriptor(String fileName, String pathStub, String description, Target... target) {
            super(fileName, pathStub, FilesystemConfiguration.getCacheHome(), description, target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return FilesystemConfiguration.getVariablePrefix() + "" + ConfigurationConstants.XDG_CACHE_HOME_VAR;
        }

    }

    private static class ItwConfigFileDescriptor extends ConfigFileDescriptor {

        private ItwConfigFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, ConfigurationConstants.DEPLOYMENT_SUBDIR_DIR, description, target);
        }

        private ItwConfigFileDescriptor(String fileName, String followingPath, String description, Target... target) {
            super(fileName, ConfigurationConstants.DEPLOYMENT_SUBDIR_DIR + File.separator + followingPath, description, target);
        }

    }

    private static class UserSecurityConfigFileDescriptor extends ItwConfigFileDescriptor {

        private UserSecurityConfigFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, ConfigurationConstants.SECURITY_WORD, description, target);
        }

    }

    private static class UserCacertsFileDescriptor extends UserSecurityConfigFileDescriptor {

        private UserCacertsFileDescriptor(String fileName) {
            super(fileName, "FILEusercerts", Target.JAVAWS);
        }

    }

    private static class ItwCacheFileDescriptor extends CacheFileDescriptor {

        private ItwCacheFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, ConfigurationConstants.DEPLOYMENT_SUBDIR_DIR, description, target);
        }

        private ItwCacheFileDescriptor(String fileName, String followingPath, String description, Target... target) {
            super(fileName, ConfigurationConstants.DEPLOYMENT_SUBDIR_DIR + File.separator + followingPath, description, target);
        }
    }

    private static class TmpUsrFileDescriptor extends InfrastructureFileDescriptor {

        private TmpUsrFileDescriptor(String fileName, String pathStub, String description) {
            super(fileName, pathStub, System.getProperty(ConfigurationConstants.TMP_PROP) + File.separator + System.getProperty(ConfigurationConstants.USER_PROP), description, Target.JAVAWS);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return FilesystemConfiguration.getVariablePrefix() + "TMP" + File.separator + FilesystemConfiguration.getVariablePrefix() + "USER";
        }

    }

}
