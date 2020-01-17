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

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.util.WeakList;
import sun.awt.AppContext;

import java.awt.Window;
import java.security.Permission;

/**
 * Security manager for JNLP environment. This security manager
 * cannot be replaced as it always denies attempts to replace the
 * security manager or policy.
 * <p>
 * The JNLP security manager tracks windows created by an
 * application, allowing those windows to be disposed when the
 * application exits but the JVM does not. If security is not
 * enabled then the first application to call System.exit will
 * halt the JVM.
 * </p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.17 $
 */
class JNLPSecurityManager extends SecurityManager {

    private static final Logger LOG = LoggerFactory.getLogger(JNLPSecurityManager.class);

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
    // in java.lang.System with the same names.

    /**
     * weak list of windows created
     */
    private WeakList<Window> weakWindows = new WeakList<>();

    /**
     * weak list of applications corresponding to window list
     */
    private WeakList<ApplicationInstance> weakApplications = new WeakList<>();

    /**
     * Creates a JNLP SecurityManager.
     */
    JNLPSecurityManager() {
        // this has the side-effect of creating the Swing shared Frame
        // owner.  Since no application is running at this time, it is
        // not added to any window list when checkTopLevelWindow is
        // called for it (and not disposed).

        if (!JNLPRuntime.isHeadless()) {
            /* is it really useful ? */
            SwingUtils.getOrCreateWindowOwner();
        }

        AppContext.getAppContext();
    }

    /**
     * Return the current Application, or null if none can be
     * determined.
     */
    protected ApplicationInstance getApplication() {
        return ApplicationManager.getApplication();
    }

    /**
     * Return the application the opened the specified window (only
     * call from event dispatch thread).
     */
    protected ApplicationInstance getApplication(Window window) {
        for (int i = weakWindows.size(); i-- > 0; ) {
            Window w = weakWindows.get(i);
            if (w == null) {
                weakWindows.remove(i);
                weakApplications.remove(i);
            }

            if (w == window) {
                return weakApplications.get(i);
            }
        }

        return null;
    }

    /**
     * Returns the application's thread group if the application can
     * be determined; otherwise returns super.getThreadGroup()
     */
    @Override
    public ThreadGroup getThreadGroup() {
        ApplicationInstance app = getApplication();
        if (app == null) {
            return super.getThreadGroup();
        }

        return app.getThreadGroup();
    }

    /**
     * Throws a SecurityException if the permission is denied,
     * otherwise return normally.  This method always denies
     * permission to change the security manager or policy.
     */
    @Override
    public void checkPermission(Permission perm) {
        String name = perm.getName();

        // Enable this manually -- it can kill ITW if left uncommented
        //      if (false)
        //        System.out.println("Checking permission: " + perm.toString());

        if (!JNLPRuntime.isWebstartApplication() &&
                ("setPolicy".equals(name) || "setSecurityManager".equals(name))) {
            throw new SecurityException("Changing the SecurityManager is not allowed.");
        }

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
            LOG.debug("Denying permission: {}", perm);
            throw ex;
        }
    }
}
