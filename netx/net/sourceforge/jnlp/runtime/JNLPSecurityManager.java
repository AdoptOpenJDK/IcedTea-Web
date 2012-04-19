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

package net.sourceforge.jnlp.runtime;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.Frame;
import java.awt.Window;
import java.lang.ref.WeakReference;
import java.net.SocketPermission;
import java.security.AllPermission;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.SecurityPermission;

import javax.swing.JWindow;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.services.ServiceUtil;
import net.sourceforge.jnlp.util.WeakList;
import sun.awt.AWTSecurityManager;
import sun.awt.AppContext;
import sun.security.util.SecurityConstants;

/**
 * Security manager for JNLP environment.  This security manager
 * cannot be replaced as it always denies attempts to replace the
 * security manager or policy.<p>
 *
 * The JNLP security manager tracks windows created by an
 * application, allowing those windows to be disposed when the
 * application exits but the JVM does not.  If security is not
 * enabled then the first application to call System.exit will
 * halt the JVM.<p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.17 $
 */
class JNLPSecurityManager extends AWTSecurityManager {

    // todo: some apps like JDiskReport can close the VM even when
    // an exit class is set - fix!

    // todo: create an event dispatch thread for each application,
    // so that the context classloader doesn't have to be switched
    // to the foreground application (the currently the approach
    // since some apps need their classloader as event dispatch
    // thread's context classloader).

    // todo: use a custom Permission object to identify the current
    // application in an AccessControlContext by setting a side
    // effect in its implies method.  Use a custom
    // AllPermissions-like permission to do this for apps granted
    // all permissions (but investigate whether this will nuke
    // the all-permission optimizations in the JRE).

    // todo: does not exit app if close button pressed on JFrame
    // with CLOSE_ON_EXIT (or whatever) set; if doesn't exit, use an
    // WindowListener to catch WindowClosing event, then if exit is
    // called immediately afterwards from AWT thread.

    // todo: deny all permissions to applications that should have
    // already been 'shut down' by closing their resources and
    // interrupt the threads if operating in a shared-VM (exit class
    // set).  Deny will probably will slow checks down a lot though.

    // todo: weak remember last getProperty application and
    // re-install properties if another application calls, or find
    // another way for different apps to have different properties
    // in java.lang.Sytem with the same names.

    /** only class that can exit the JVM, if set */
    private Object exitClass = null;

    /** this exception prevents exiting the JVM */
    private SecurityException closeAppEx = // making here prevents huge stack traces
    new SecurityException(R("RShutdown"));

    /** weak list of windows created */
    private WeakList<Window> weakWindows = new WeakList<Window>();

    /** weak list of applications corresponding to window list */
    private WeakList<ApplicationInstance> weakApplications =
            new WeakList<ApplicationInstance>();

    /** Sets whether or not exit is allowed (in the context of the plugin, this is always false) */
    private boolean exitAllowed = true;

    /**
     * The AppContext of the main application (netx). We need to store this here
     * so we can return this when no code from an external application is
     * running on the thread
     */
    private AppContext mainAppContext;

    /**
     * Creates a JNLP SecurityManager.
     */
    JNLPSecurityManager() {
        // this has the side-effect of creating the Swing shared Frame
        // owner.  Since no application is running at this time, it is
        // not added to any window list when checkTopLevelWindow is
        // called for it (and not disposed).

        if (!JNLPRuntime.isHeadless())
            new JWindow().getOwner();

        mainAppContext = AppContext.getAppContext();
    }

    /**
     * Returns whether the exit class is present on the stack, or
     * true if no exit class is set.
     */
    public boolean isExitClass() {
        return isExitClass(getClassContext());
    }

    /**
     * Returns whether the exit class is present on the stack, or
     * true if no exit class is set.
     */
    private boolean isExitClass(Class stack[]) {
        if (exitClass == null)
            return true;

        for (int i = 0; i < stack.length; i++)
            if (stack[i] == exitClass)
                return true;

        return false;
    }

    /**
     * Set the exit class, which is the only class that can exit the
     * JVM; if not set then any class can exit the JVM.
     *
     * @param exitClass the exit class
     * @throws IllegalStateException if the exit class is already set
     */
    public void setExitClass(Class exitClass) throws IllegalStateException {
        if (this.exitClass != null)
            throw new IllegalStateException(R("RExitTaken"));

        this.exitClass = exitClass;
    }

    /**
     * Return the current Application, or null if none can be
     * determined.
     */
    protected ApplicationInstance getApplication() {
        return getApplication(Thread.currentThread(), getClassContext(), 0);
    }

    /**
     * Return the application the opened the specified window (only
     * call from event dispatch thread).
     */
    protected ApplicationInstance getApplication(Window window) {
        for (int i = weakWindows.size(); i-- > 0;) {
            Window w = weakWindows.get(i);
            if (w == null) {
                weakWindows.remove(i);
                weakApplications.remove(i);
            }

            if (w == window)
                return weakApplications.get(i);
        }

        return null;
    }

    /**
     * Return the current Application, or null.
     */
    protected ApplicationInstance getApplication(Thread thread, Class<?> stack[], int maxDepth) {
        ClassLoader cl;
        JNLPClassLoader jnlpCl;

        cl = thread.getContextClassLoader();
        while (cl != null) {
            jnlpCl = getJnlpClassLoader(cl);
            if (jnlpCl != null && jnlpCl.getApplication() != null) {
                return jnlpCl.getApplication();
            }
            cl = cl.getParent();
        }

        if (maxDepth <= 0)
            maxDepth = stack.length;

        // this needs to be tightened up
        for (int i = 0; i < stack.length && i < maxDepth; i++) {
            cl = stack[i].getClassLoader();
            while (cl != null) {
                jnlpCl = getJnlpClassLoader(cl);
                if (jnlpCl != null && jnlpCl.getApplication() != null) {
                    return jnlpCl.getApplication();
                }
                cl = cl.getParent();
            }
        }
        return null;
    }

    /**
     * Returns the JNLPClassLoader associated with the given ClassLoader, or
     * null.
     * @param cl a ClassLoader
     * @return JNLPClassLoader or null
     */
    private JNLPClassLoader getJnlpClassLoader(ClassLoader cl) {
        // Since we want to deal with JNLPClassLoader, extract it if this
        // is a codebase loader
        if (cl instanceof JNLPClassLoader.CodeBaseClassLoader)
            cl = ((JNLPClassLoader.CodeBaseClassLoader) cl).getParentJNLPClassLoader();

        if (cl instanceof JNLPClassLoader) {
            JNLPClassLoader loader = (JNLPClassLoader) cl;
            return loader;
        }

        return null;
    }

    /**
     * Returns the application's thread group if the application can
     * be determined; otherwise returns super.getThreadGroup()
     */
    public ThreadGroup getThreadGroup() {
        ApplicationInstance app = getApplication();
        if (app == null)
            return super.getThreadGroup();

        return app.getThreadGroup();
    }

    /**
     * Throws a SecurityException if the permission is denied,
     * otherwise return normally.  This method always denies
     * permission to change the security manager or policy.
     */
    public void checkPermission(Permission perm) {
        String name = perm.getName();

        // Enable this manually -- it'll produce too much output for -verbose
        // otherwise.
        //      if (true)
        //        System.out.println("Checking permission: " + perm.toString());

        if (!JNLPRuntime.isWebstartApplication() &&
                ("setPolicy".equals(name) || "setSecurityManager".equals(name)))
            throw new SecurityException(R("RCantReplaceSM"));

        try {
            // deny all permissions to stopped applications
            // The call to getApplication() below might not work if an
            // application hasn't been fully initialized yet.
            //            if (JNLPRuntime.isDebug()) {
            //                if (!"getClassLoader".equals(name)) {
            //                    ApplicationInstance app = getApplication();
            //                    if (app != null && !app.isRunning())
            //                        throw new SecurityException(R("RDenyStopped"));
            //                }
            //            }

            super.checkPermission(perm);
        } catch (SecurityException ex) {
            if (JNLPRuntime.isDebug()) {
                System.out.println("Denying permission: " + perm);
            }
            throw ex;
        }
    }

    /**
     * Asks the user whether or not to grant permission.
     * @param perm the permission to be granted
     * @return true if the permission was granted, false otherwise.
     */
    private boolean askPermission(Permission perm) {

        ApplicationInstance app = getApplication();
        if (app != null && !app.isSigned()) {
            if (perm instanceof SocketPermission
                                && ServiceUtil.checkAccess(AccessType.NETWORK, perm.getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a permission to the JNLPClassLoader.
     * @param perm the permission to add to the JNLPClassLoader
     */
    private void addPermission(Permission perm) {
        if (JNLPRuntime.getApplication().getClassLoader() instanceof JNLPClassLoader) {

            JNLPClassLoader cl = (JNLPClassLoader) JNLPRuntime.getApplication().getClassLoader();
            cl.addPermission(perm);
            if (JNLPRuntime.isDebug()) {
                if (cl.getPermissions(null).implies(perm))
                    System.err.println("Added permission: " + perm.toString());
                else
                    System.err.println("Unable to add permission: " + perm.toString());
            }
        } else {
            if (JNLPRuntime.isDebug())
                System.err.println("Unable to add permission: " + perm + ", classloader not JNLP.");
        }
    }

    /**
     * Checks whether the window can be displayed without an applet
     * warning banner, and adds the window to the list of windows to
     * be disposed when the calling application exits.
     */
    public boolean checkTopLevelWindow(Object window) {
        ApplicationInstance app = getApplication();

        // remember window -> application mapping for focus, close on exit
        if (app != null && window instanceof Window) {
            Window w = (Window) window;

            if (JNLPRuntime.isDebug())
                System.err.println("SM: app: " + app.getTitle() + " is adding a window: " + window + " with appContext " + AppContext.getAppContext());

            weakWindows.add(w); // for mapping window -> app
            weakApplications.add(app);

            app.addWindow(w);
        }

        // todo: set awt.appletWarning to custom message
        // todo: logo on with glass pane on JFrame/JWindow?

        return super.checkTopLevelWindow(window);
    }

    /**
     * Checks whether the caller can exit the system.  This method
     * identifies whether the caller is a real call to Runtime.exec
     * and has special behavior when returning from this method
     * would exit the JVM and an exit class is set: if the caller is
     * not the exit class then the calling application will be
     * stopped and its resources destroyed (when possible), and an
     * exception will be thrown to prevent the JVM from shutting
     * down.<p>
     *
     * Calls not from Runtime.exit or with no exit class set will
     * behave normally, and the exit class can always exit the JVM.
     */
    public void checkExit(int status) {

        // applets are not allowed to exit, but the plugin main class (primordial loader) is
        Class stack[] = getClassContext();
        if (!exitAllowed) {
            for (int i = 0; i < stack.length; i++)
                if (stack[i].getClassLoader() != null)
                    throw new AccessControlException("Applets may not call System.exit()");
        }

        super.checkExit(status);

        boolean realCall = (stack[1] == Runtime.class);

        if (isExitClass(stack)) // either exitClass called or no exitClass set
            return; // to Runtime.exit or fake call to see if app has permission

        // not called from Runtime.exit()
        if (!realCall) {
            // apps that can't exit should think they can exit normally
            super.checkExit(status);
            return;
        }

        // but when they really call, stop only the app instead of the JVM
        ApplicationInstance app = getApplication(Thread.currentThread(), stack, 0);
        if (app == null) {
            throw new SecurityException(R("RExitNoApp"));
        }

        app.destroy();

        throw closeAppEx;
    }

    protected void disableExit() {
        exitAllowed = false;
    }

    /**
     * This returns the appropriate {@link AppContext}. Hooks in AppContext
     * check if the current {@link SecurityManager} is an instance of
     * AWTSecurityManager and if so, call this method to give it a chance to
     * return the appropriate appContext based on the application that is
     * running.<p>
     *
     * This can be called from any thread (possibly a swing thread) to find out
     * the AppContext for the thread (which may correspond to a particular
     * applet).
     */
    @Override
    public AppContext getAppContext() {
        ApplicationInstance app = getApplication();
        if (app == null) {
            /*
             * if we cannot find an application based on the code on the stack,
             * then assume it is the main application
             */
            return mainAppContext;
        } else {
            return app.getAppContext();
        }

    }

    /**
     * Tests if a client can get access to the AWT event queue. This version allows
     * complete access to the EventQueue for its own AppContext-specific EventQueue.
     *
     * FIXME there are probably huge security implications for this. Eg:
     * http://hg.openjdk.java.net/jdk7/awt/jdk/rev/8022709a306d
     *
     * @exception  SecurityException  if the caller does not have
     *             permission to accesss the AWT event queue.
     */
    public void checkAwtEventQueueAccess() {
        /*
         * this is the templace of the code that should allow applets access to
         * eventqueues
         */

        // AppContext appContext = AppContext.getAppContext();
        // ApplicationInstance instance = getApplication();

        // if ((appContext == mainAppContext) && (instance != null)) {
        // If we're about to allow access to the main EventQueue,
        // and anything untrusted is on the class context stack,
        // disallow access.
        super.checkAwtEventQueueAccess();
        // }
    }

}
