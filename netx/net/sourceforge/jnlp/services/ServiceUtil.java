// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.services;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.jnlp.BasicService;
import javax.jnlp.ClipboardService;
import javax.jnlp.DownloadService;
import javax.jnlp.ExtensionInstallerService;
import javax.jnlp.FileOpenService;
import javax.jnlp.FileSaveService;
import javax.jnlp.PersistenceService;
import javax.jnlp.PrintService;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.SecurityDialogs;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Provides static methods to interact useful for using the JNLP
 * services.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 * @version $Revision: 1.8 $
 */
public class ServiceUtil {

    /**
     * @return the BasicService reference, or null if the service is
     * unavailable.
     */
    public static BasicService getBasicService() {
        return (BasicService) getService("javax.jnlp.BasicService");
    }

    /**
     * @return the ClipboardService reference, or null if the service is
     * unavailable.
     */
    public static ClipboardService getClipboardService() {
        return (ClipboardService) getService("javax.jnlp.ClipboardService");
    }

    /**
     * @return the DownloadService reference, or null if the service is
     * unavailable.
     */
    public static DownloadService getDownloadService() {
        return (DownloadService) getService("javax.jnlp.DownloadService");
    }

    /**
     * @return the ExtensionInstallerService reference, or null if the service is
     * unavailable.
     */
    public static ExtensionInstallerService getExtensionInstallerService() {
        return (ExtensionInstallerService) getService("javax.jnlp.ExtensionInstallerService");
    }

    /**
     * @return the FileOpenService reference, or null if the service is
     * unavailable.
     */
    public static FileOpenService getFileOpenService() {
        return (FileOpenService) getService("javax.jnlp.FileOpenService");
    }

    /**
     * @return the FileSaveService reference, or null if the service is
     * unavailable.
     */
    public static FileSaveService getFileSaveService() {
        return (FileSaveService) getService("javax.jnlp.FileSaveService");
    }

    /**
     * @return the PersistenceService reference, or null if the service is
     * unavailable.
     */
    public static PersistenceService getPersistenceService() {
        return (PersistenceService) getService("javax.jnlp.PersistenceService");
    }

    /**
     * @return the PrintService reference, or null if the service is
     * unavailable.
     */
    public static PrintService getPrintService() {
        return (PrintService) getService("javax.jnlp.PrintService");
    }

    /**
     * @return the SingleInstanceService reference, or null if the service is
     * unavailable.
     */
    public static SingleInstanceService getSingleInstanceService() {
        return (SingleInstanceService) getService("javax.jnlp.SingleInstanceService");
    }

    /**
     * Checks that this application (represented by the jnlp) isnt already running
     * @param jnlpFile the {@link JNLPFile} that specifies the application
     *
     * @throws InstanceExistsException if an instance of this application already exists
     */
    public static void checkExistingSingleInstance(JNLPFile jnlpFile) {
        ExtendedSingleInstanceService esis = (ExtendedSingleInstanceService) getSingleInstanceService();
        esis.checkSingleInstanceRunning(jnlpFile);
    }

    /**
     * Returns the service, or null instead of an UnavailableServiceException
     */
    private static Object getService(String name) {
        try {
            return ServiceManager.lookup(name);
        } catch (UnavailableServiceException ex) {
            return null;
        }
    }

    /**
     * Creates a Proxy object implementing the specified interface
     * when makes all calls in the security context of the system
     * classes (ie, AllPermissions).  This means that the services
     * must be more than extremely careful in the operations they
     * perform.
     */
    static Object createPrivilegedProxy(Class<?> iface, final Object receiver) {
        return java.lang.reflect.Proxy.newProxyInstance(XServiceManagerStub.class.getClassLoader(),
                new Class<?>[] { iface },
                new PrivilegedHandler(receiver));
    }

    /**
     * calls the object's method using privileged access
     */
    private static class PrivilegedHandler implements InvocationHandler {
        private final Object receiver;

        PrivilegedHandler(Object receiver) {
            this.receiver = receiver;
        }

        @Override
        public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
            if (JNLPRuntime.isDebug()) {
                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "call privileged method: " + method.getName());
                if (args != null) {
                    for (Object arg : args) {
                        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "           arg: " + arg);
                    }
                }
            }

            PrivilegedExceptionAction<Object> invoker = new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    return method.invoke(receiver, args);
                }
            };

            try {
                Object result = AccessController.doPrivileged(invoker);

                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "        result: " + result);

                return result;
            } catch (PrivilegedActionException e) {
                // Any exceptions thrown by the actual methods are wrapped by a
                // InvocationTargetException, which is further wrapped by the
                // PrivilegedActionException. Lets unwrap them to make the
                // proxy transparent to the callers
                if (e.getCause() instanceof InvocationTargetException) {
                    throw e.getCause().getCause();
                } else {
                    throw e.getCause();
                }
            }

        }
    };

    /**
     * Returns whether the app requesting a JNLP service has the right permissions.
     * If it doesn't, user is prompted for permissions. This method should only be
     * used for JNLP API related permissions.
     *
     * @param type the type of access being requested
     * @param extras extra Strings (usually) that are passed to the dialog for
     * message formatting.
     * @return true if the access was granted, false otherwise.
     */
    public static boolean checkAccess(AccessType type, Object... extras) {
        return checkAccess(null, type, extras);
    }

    /**
     * Returns whether the app requesting a JNLP service has the right permissions.
     * If it doesn't, user is prompted for permissions. This method should only be
     * used for JNLP API related permissions.
     *
     * @param app the application which is requesting the check. If null, the current
     * application is used.
     * @param type the type of access being requested
     * @param extras extra Strings (usually) that are passed to the dialog for
     * message formatting.
     * @return true if the access was granted, false otherwise.
     */
    public static boolean checkAccess(ApplicationInstance app, AccessType type,
                Object... extras) {

        boolean trusted = isSigned(app);

        if (!trusted) {

            if (!shouldPromptUser()) {
                return false;
            }
            if (app == null) {
                app = JNLPRuntime.getApplication();
            }

            final AccessType tmpType = type;
            final Object[] tmpExtras = extras;
            final ApplicationInstance tmpApp = app;

            //We need to do this to allow proper icon loading for unsigned
            //applets, otherwise permissions won't be granted to load icons
            //from resources.jar.
            Boolean b = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    boolean b = SecurityDialogs.showAccessWarningDialogB(tmpType,
                                tmpApp.getJNLPFile(), tmpExtras);
                    return b;
                }
            });

            return b;
        }

        return true; //allow
    }

    /**
     * Returns whether the current runtime configuration allows prompting the
     * user for JNLP permissions.
     *
     * @return true if the user should be prompted for JNLP API related permissions.
     */
    private static boolean shouldPromptUser() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean >() {
            @Override
            public Boolean run() {
                return Boolean.valueOf(JNLPRuntime.getConfiguration()
                        .getProperty(DeploymentConfiguration.KEY_SECURITY_PROMPT_USER_FOR_JNLP));
            }
        });
    }
    
    /**
     * Returns whether the app requesting a JNLP service is a trusted
     * application
     * 
     * @param app
     *            the application which is requesting the check. If null, the
     *            current application is used.
     * @return true, if the app is a trusted application; false otherwise
     */

    public static boolean isSigned(ApplicationInstance app) {

        if (app == null) {
            app = JNLPRuntime.getApplication();
        }

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        for (StackTraceElement stack1 : stack) {
            Class<?> c = null;
            try {
                c = Class.forName(stack1.getClassName());
            } catch (Exception e1) {
                OutputController.getLogger().log(e1);
                try {
                    c = Class.forName(stack1.getClassName(), false, app.getClassLoader());
                }catch (Exception e2) {
                    OutputController.getLogger().log(e2);
                }
            }
            // Everything up to the desired class/method must be trusted
            if (c == null || // class not found
                    (c.getProtectionDomain().getCodeSource() != null && // class is not in bootclasspath
                    c.getProtectionDomain().getCodeSource().getCodeSigners() == null) // class is trusted
                    ) {
                return false;
            }
        }
        return true;
    }

}
