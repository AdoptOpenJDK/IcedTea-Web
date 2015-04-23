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

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jnlp.util.logging.OutputController;
import static net.sourceforge.jnlp.config.DeploymentConfiguration.APPLET_TRUST_SETTINGS;
import static net.sourceforge.jnlp.config.DeploymentConfiguration.DEPLOYMENT_CONFIG_FILE;
import static net.sourceforge.jnlp.config.DeploymentConfiguration.DEPLOYMENT_PROPERTIES;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.Translator;

public class PathsAndFiles {

    public static final String DEPLOYMENT_SUBDIR_DIR = "icedtea-web";

    private static final String CONFIG_HOME;
    private static final String CACHE_HOME;
    private static final String DATA_HOME;
    private static final String RUNTIME_HOME;
    public static final String USER_CONFIG_HOME;
    public static final String USER_CACHE_HOME;
    public static final String USER_DEFAULT_SECURITY_DIR;
    public static final String XDG_CONFIG_HOME_VAR = "XDG_CONFIG_HOME";
    public static final String XDG_CACHE_HOME_VAR = "XDG_CACHE_HOME";
    public static final String XDG_RUNTIME_DIR_VAR = "XDG_RUNTIME_DIR";
    private static final String XDG_DATA_HOME = "XDG_DATA_HOME";
    private static final String TMP_PROP = "java.io.tmpdir";
    private static final String HOME_PROP = "user.home";
    private static final String JAVA_PROP = "java.home";
    private static final String USER_PROP = "user.name";
    private static final String VARIABLE = JNLPRuntime.isWindows() ? "%" : "$";
    private static final String securityWord = "security";
    public static final String ICEDTEA_SO = "IcedTeaPlugin.so";
    public static final String CACHE_INDEX_FILE_NAME = "recently_used";

    static {
        String configHome = System.getProperty(HOME_PROP) + File.separator + ".config";
        String cacheHome = System.getProperty(HOME_PROP) + File.separator + ".cache";
        String dataHome = System.getProperty(HOME_PROP) +  File.separator + ".local" + File.separator + "share";
        String runtimeHome = System.getProperty(TMP_PROP);
        String xdg_config_home = System.getenv(XDG_CONFIG_HOME_VAR);
        String xdg_cache_home = System.getenv(XDG_CACHE_HOME_VAR);
        String xdg_runtime_home = System.getenv(XDG_RUNTIME_DIR_VAR);
        String xdg_data_home = System.getenv(XDG_DATA_HOME);
        if (xdg_config_home != null) {
            CONFIG_HOME = xdg_config_home;
        } else {
            CONFIG_HOME = configHome;
        }
        if (xdg_cache_home != null) {
            CACHE_HOME = xdg_cache_home;
        } else {
            CACHE_HOME = cacheHome;
        }
        if (xdg_runtime_home != null) {
            RUNTIME_HOME = xdg_runtime_home;
        } else {
            RUNTIME_HOME = runtimeHome;
        }
         if (xdg_data_home != null) {
           DATA_HOME = xdg_data_home;
        } else {
            DATA_HOME = dataHome;
        }
        USER_CONFIG_HOME = CONFIG_HOME + File.separator + DEPLOYMENT_SUBDIR_DIR;
        USER_CACHE_HOME = CACHE_HOME + File.separator + DEPLOYMENT_SUBDIR_DIR;
        USER_DEFAULT_SECURITY_DIR = USER_CONFIG_HOME + File.separator + securityWord;
    }

    /**
     * PIPES_DIR and both Plugin Dirs  are not instatiated to be used. Do exists  only for documentation purposes.
     * Maintained by native part of ITW. Or outside ITW.
     */
    public static final InfrastructureFileDescriptor PIPES_DIR = new RuntimeFileDescriptor("icedteaplugin-user-*", "FILEpipe", Target.PLUGIN);
    public static final InfrastructureFileDescriptor MOZILA_USER = new HomeFileDescriptor(ICEDTEA_SO, ".mozilla/plugins", "FILEmozillauser", Target.PLUGIN);
    public static final InfrastructureFileDescriptor MOZILA_GLOBAL_64 = new InfrastructureFileDescriptor(ICEDTEA_SO, "/usr/lib64/mozilla/plugins/", "",  "FILEmozillaglobal64", Target.PLUGIN);
    public static final InfrastructureFileDescriptor MOZILA_GLOBAL_32 = new InfrastructureFileDescriptor(ICEDTEA_SO, "/usr/lib/mozilla/plugins/", "",  "FILEmozillaglobal32", Target.PLUGIN);
    public static final InfrastructureFileDescriptor OPERA_64 = new InfrastructureFileDescriptor(ICEDTEA_SO, "/usr/lib64/opera/plugins/", "",  "FILEopera64", Target.PLUGIN);
    public static final InfrastructureFileDescriptor OPERA_32 = new InfrastructureFileDescriptor(ICEDTEA_SO, "/usr/lib/opera/plugins/", "",  "FILEopera32", Target.PLUGIN);
    
    public static final InfrastructureFileDescriptor CACHE_DIR = new ItwCacheFileDescriptor("cache", "FILEcache", Target.JAVAWS, Target.ITWEB_SETTINGS) {
    
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_CACHE_DIR;
        }

    };
    

    // this one is depnding on CACHE_DIR, so initialize it lazily
    public static InfrastructureFileDescriptor getRecentlyUsedFile() {
        return RECENTLY_USED_FILE_HOLDER.RECENTLY_USED_FILE;
    }

    private static class RECENTLY_USED_FILE_HOLDER {
        static final InfrastructureFileDescriptor RECENTLY_USED_FILE = new ItwCacheFileDescriptor(CACHE_INDEX_FILE_NAME, CACHE_DIR.getFile().getName(), "FILErecentlyUsed", Target.JAVAWS, Target.ITWEB_SETTINGS){

            @Override
            public String getFullPath() {
                return clean(CACHE_DIR.getFullPath()+File.separator+this.getFileName());
            }
          
        };
    }
    
    public static final InfrastructureFileDescriptor PCACHE_DIR = new ItwCacheFileDescriptor("pcache", "FILEappdata", Target.JAVAWS, Target.ITWEB_SETTINGS){

        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_PERSISTENCE_CACHE_DIR;
        }
    };
    public static final InfrastructureFileDescriptor LOG_DIR = new ItwConfigFileDescriptor("log", "FILElogs", Target.JAVAWS, Target.ITWEB_SETTINGS){

        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_LOG_DIR;
        }
    
        
    };
    //javaws is saving here, itweb-settings may modify them
    public static final InfrastructureFileDescriptor ICONS_DIR = new ItwConfigFileDescriptor("icons", "FILEicons", Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor GEN_JNLPS_DIR = new ItwConfigFileDescriptor("generated_jnlps", "FILEjnlps", Target.PLUGIN, Target.ITWEB_SETTINGS);
    //javaws is saving here, itweb-settings may modify them
    public static final InfrastructureFileDescriptor MENUS_DIR = new MenuFileDescriptor("javaws", "FILEmenus", Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor APPLET_TRUST_SETTINGS_USER = new ItwConfigFileDescriptor(APPLET_TRUST_SETTINGS, "FILEextasuser", Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor APPLET_TRUST_SETTINGS_SYS = new SystemDeploymentCofigFileDescriptor(APPLET_TRUST_SETTINGS, "FILEextasadmin", Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor ETC_DEPLOYMENT_CFG = new SystemDeploymentCofigFileDescriptor(DEPLOYMENT_CONFIG_FILE, "FILEglobaldp", Target.JAVAWS, Target.ITWEB_SETTINGS);
    public static final InfrastructureFileDescriptor TMP_DIR = new ItwCacheFileDescriptor("tmp", "FILEtmpappdata", Target.JAVAWS, Target.ITWEB_SETTINGS){
        
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_TMP_DIR;
        }
        
    };
    public static final InfrastructureFileDescriptor LOCKS_DIR = new TmpUsrFileDescriptor("locks", "netx", "FILElocksdir", Target.JAVAWS) {

        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_LOCKS_DIR;
        }
        
    };
    public static final InfrastructureFileDescriptor MAIN_LOCK = new TmpUsrFileDescriptor("netx_running", "netx" + File.separator + "locks", "FILEmainlock", Target.JAVAWS) {

        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_NETX_RUNNING_FILE;
        }
        
    };
    public static final InfrastructureFileDescriptor JAVA_POLICY = new UserSecurityConfigFileDescriptor("java.policy", "FILEpolicy", Target.JAVAWS, Target.POLICY_EDITOR){

                   @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_SECURITY_POLICY;
        }

        @Override
        public File getFile() {
            throw new IllegalStateException("Only getFullPath should be used. This is returning URL String.");
        }
        
        @Override
        public File getDefaultFile() {
            throw new IllegalStateException("Only getDefaultFullPath should be used. This is returning URL String.");
        }
        
    };
    public static final InfrastructureFileDescriptor USER_CACERTS = new UserCacertsFileDescriptor("trusted.cacerts") {
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_TRUSTED_CA_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor USER_JSSECAC = new UserCacertsFileDescriptor("trusted.jssecacerts") {
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_TRUSTED_JSSE_CA_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor USER_CERTS = new UserCacertsFileDescriptor("trusted.certs") {
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_TRUSTED_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor USER_JSSECER = new UserCacertsFileDescriptor("trusted.jssecerts") {
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_TRUSTED_JSSE_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor USER_CLIENTCERT = new UserCacertsFileDescriptor("trusted.clientcerts") {
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_USER_TRUSTED_CLIENT_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor SYS_CACERT = new SystemJavaSecurityFileDescriptor("cacerts") {
        
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_SYSTEM_TRUSTED_CA_CERTS;
        }
        
    };
    public static final InfrastructureFileDescriptor SYS_JSSECAC = new SystemJavaSecurityFileDescriptor("jssecacerts") {
      
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor SYS_CERT = new SystemJavaSecurityFileDescriptor("trusted.certs"){
        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_SYSTEM_TRUSTED_CERTS;
        }
    };
    public static final InfrastructureFileDescriptor SYS_JSSECERT = new SystemJavaSecurityFileDescriptor("trusted.jssecerts") {

        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_SYSTEM_TRUSTED_JSSE_CERTS;
        }
        
    };
    public static final InfrastructureFileDescriptor SYS_CLIENTCERT = new SystemJavaSecurityFileDescriptor("trusted.clientcerts") {

        @Override
        public String getPropertiesKey() {
            return DeploymentConfiguration.KEY_SYSTEM_TRUSTED_CLIENT_CERTS;
        }

    };
    public static final InfrastructureFileDescriptor JAVA_DEPLOYMENT_PROP_FILE = new SystemJavaLibFileDescriptor(DEPLOYMENT_CONFIG_FILE, "FILEjavadp", Target.JAVAWS, Target.ITWEB_SETTINGS) {

        @Override
        public String getDescription() {
             return Translator.R(getDescriptionKey(), DeploymentConfiguration.KEY_JRE_DIR);
        }
        
    };
    public static final InfrastructureFileDescriptor USER_DEPLOYMENT_FILE = new ItwConfigFileDescriptor(DEPLOYMENT_PROPERTIES, "FILEuserdp", Target.JAVAWS, Target.ITWEB_SETTINGS);

    static enum Target {
        JAVAWS, PLUGIN, ITWEB_SETTINGS, POLICY_EDITOR;
    }

    public static List<InfrastructureFileDescriptor> getAllFiles() {
        return getAllFiles(null);
    }
    
    public static List<InfrastructureFileDescriptor> getAllSecurityFiles() {
        return getAllFiles(null, UserSecurityConfigFileDescriptor.class);
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
                    for (Target targe : i.target) {
                        if (desired == null || targe == desired) {
                            r.add(i);
                            break;
                        }
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                OutputController.getLogger().log(ex);
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

        private HomeFileDescriptor(String fileName, String pathSub, String description, Target... target) {
            super(fileName, pathSub, System.getProperty(HOME_PROP), description, target);
        }
              @Override
        public String getSystemPathStubAcronym() {
            return VARIABLE + "HOME";
        }


    }

    private static class SystemCofigFileDescriptor extends InfrastructureFileDescriptor {

        private SystemCofigFileDescriptor(String fileName, String pathSub, String description, Target... target) {
            super(fileName, pathSub, File.separator + "etc" + File.separator + ".java", description, target);
        }

    }

    private static class SystemDeploymentCofigFileDescriptor extends SystemCofigFileDescriptor {

        private SystemDeploymentCofigFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, "deployment", description, target);
        }

    }

    private static class SystemJavaFileDescriptor extends InfrastructureFileDescriptor {

        private SystemJavaFileDescriptor(String fileName, String pathSub, String description, Target... target) {
            super(fileName, pathSub, System.getProperty(JAVA_PROP), description, target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return VARIABLE + "JAVA_HOME";
        }

    }

    private static class SystemJavaLibFileDescriptor extends SystemJavaFileDescriptor {

        private SystemJavaLibFileDescriptor(String fileName, String desc, Target... target) {
            super(fileName, "lib", desc, target);
        }

        private SystemJavaLibFileDescriptor(String fileName, String subpath, String description, Target... target) {
            super(fileName, "lib" + File.separator + subpath, description, target);
        }

    }

    private static class SystemJavaSecurityFileDescriptor extends SystemJavaLibFileDescriptor {

        private SystemJavaSecurityFileDescriptor(String fileName) {
            super(fileName, securityWord, "FILEjavacerts", Target.JAVAWS);
        }

    }
    
    private static class DataFileDescriptor extends InfrastructureFileDescriptor {

        private DataFileDescriptor(String fileName, String sp, String description, Target... target) {
            super(fileName, sp, DATA_HOME, description, target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return VARIABLE + "" + XDG_DATA_HOME;
        }

    }

    /**
     * http://standards.freedesktop.org/menu-spec/menu-spec-1.0.html#paths
     */
    private static class MenuFileDescriptor extends DataFileDescriptor {

        private MenuFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, "applications", description, target);
        }
    }


    private static class RuntimeFileDescriptor extends InfrastructureFileDescriptor {

        private RuntimeFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, RUNTIME_HOME, "", description, target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return VARIABLE + "" + XDG_RUNTIME_DIR_VAR;
        }

    }

    private static class ConfigFileDescriptor extends InfrastructureFileDescriptor {

        private ConfigFileDescriptor(String fileName, String pathStub, String description, Target... target) {
            super(fileName, pathStub, CONFIG_HOME, description, target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return VARIABLE + "" + XDG_CONFIG_HOME_VAR;
        }

    }

    private static class CacheFileDescriptor extends InfrastructureFileDescriptor {

        private CacheFileDescriptor(String fileName, String pathStub, String description, Target... target) {
            super(fileName, pathStub, CACHE_HOME, description, target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return VARIABLE + "" + XDG_CACHE_HOME_VAR;
        }

    }

    private static class ItwConfigFileDescriptor extends ConfigFileDescriptor {

        private ItwConfigFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, DEPLOYMENT_SUBDIR_DIR, description, target);
        }

        private ItwConfigFileDescriptor(String fileName, String folowingPath, String description, Target... target) {
            super(fileName, DEPLOYMENT_SUBDIR_DIR + File.separator + folowingPath, description, target);
        }

    }

    private static class UserSecurityConfigFileDescriptor extends ItwConfigFileDescriptor {

        private UserSecurityConfigFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, securityWord, description, target);
        }

    }

    private static class UserCacertsFileDescriptor extends UserSecurityConfigFileDescriptor {

        private UserCacertsFileDescriptor(String fileName) {
            super(fileName, "FILEusercerts", Target.JAVAWS);
        }

    }

    private static class ItwCacheFileDescriptor extends CacheFileDescriptor {

        private ItwCacheFileDescriptor(String fileName, String description, Target... target) {
            super(fileName, DEPLOYMENT_SUBDIR_DIR, description, target);
        }

        private ItwCacheFileDescriptor(String fileName, String folowingPath, String description, Target... target) {
            super(fileName, DEPLOYMENT_SUBDIR_DIR + File.separator + folowingPath, description, target);
        }
    }

    private static class TmpUsrFileDescriptor extends InfrastructureFileDescriptor {

        private TmpUsrFileDescriptor(String fileName, String pathStub, String description, Target... target) {
            super(fileName, pathStub, System.getProperty(TMP_PROP) + File.separator + System.getProperty(USER_PROP), description, target);
        }

        @Override
        public String getSystemPathStubAcronym() {
            return VARIABLE + "TMP" + File.separator + VARIABLE + "USER";
        }

    };
    
}
