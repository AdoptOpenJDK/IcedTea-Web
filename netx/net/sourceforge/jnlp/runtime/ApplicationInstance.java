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

import java.awt.Window;
import java.io.File;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

import javax.swing.event.EventListenerList;

import sun.awt.AppContext;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.PropertyDesc;
import net.sourceforge.jnlp.SecurityDesc;
import net.sourceforge.jnlp.ShortcutDesc;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.event.ApplicationEvent;
import net.sourceforge.jnlp.event.ApplicationListener;
import net.sourceforge.jnlp.security.SecurityDialogs;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.security.dialogs.AccessWarningPaneComplexReturn;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.WeakList;
import net.sourceforge.jnlp.util.XDesktopEntry;

/**
 * Represents a running instance of an application described in a
 * JNLPFile. This class provides a way to track the application's
 * resources and destroy the application.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.15 $
 */
public class ApplicationInstance {

    // todo: should attempt to unload the environment variables
    // installed by the application.

    /** the file */
    private final JNLPFile file;

    /** the thread group */
    private final ThreadGroup group;

    /** the classloader */
    private final ClassLoader loader;

    /**
     * <p>
     * Every application/applet gets its own AppContext. This allows us to do
     * things like have two different look and feels for two different applets
     * (running in the same VM), allows untrusted programs to manipulate the
     * event queue (safely) and (possibly) more.
     * </p>
     * <p>
     * It is set to the AppContext which created this ApplicationInstance
     * </p>
     */
    private final AppContext appContext;

    /** whether the application has stopped running */
    private boolean stopped = false;

    /** weak list of windows opened by the application */
    private final WeakList<Window> weakWindows = new WeakList<>();

    /** list of application listeners  */
    private final EventListenerList listeners = new EventListenerList();

    /** whether or not this application is signed */
    private boolean isSigned = false;

    /**
     * Create an application instance for the file. This should be done in the
     * appropriate {@link ThreadGroup} only.
     * @param file jnlpfile for which the instance do exists
     * @param group thread group to which it belongs
     * @param loader loader for this application
     */
    public ApplicationInstance(JNLPFile file, ThreadGroup group, ClassLoader loader) {
        this.file = file;
        this.group = group;
        this.loader = loader;
        this.isSigned = ((JNLPClassLoader) loader).getSigning();
        this.appContext = AppContext.getAppContext();
    }

    /**
     * Add an Application listener
     * @param listener listener to be added
     */
    public void addApplicationListener(ApplicationListener listener) {
        listeners.add(ApplicationListener.class, listener);
    }

    /**
     * Remove an Application Listener
     * @param listener to be removed
     */
    public void removeApplicationListener(ApplicationListener listener) {
        listeners.remove(ApplicationListener.class, listener);
    }

    /**
     * Notify listeners that the application has been terminated.
     */
    protected void fireDestroyed() {
        Object list[] = listeners.getListenerList();
        ApplicationEvent event = null;

        for (int i = list.length - 1; i > 0; i -= 2) { // last to first required
            if (event == null)
                event = new ApplicationEvent(this);

            ((ApplicationListener) list[i]).applicationDestroyed(event);
        }
    }

    /**
     * Initialize the application's environment (installs
     * environment variables, etc).
     */
    public void initialize() {
        installEnvironment();
        addMenuAndDesktopEntries();
    }

    /**
     * Creates menu and desktop entries if required by the jnlp file or settings
     */

    private void addMenuAndDesktopEntries() {
        XDesktopEntry entry = new XDesktopEntry(file);
        ShortcutDesc sd = file.getInformation().getShortcut();
        File possibleDesktopFile = entry.getLinuxDesktopIconFile();
        File possibleMenuFile = entry.getLinuxMenuIconFile();
        File generatedJnlp = entry.getGeneratedJnlpFileName();
        //if one of menu or desktop exists, do not bother user
        boolean exists = false;
        if (possibleDesktopFile.exists()) {
            OutputController.getLogger().log("ApplicationInstance.addMenuAndDesktopEntries(): file - "
                    + possibleDesktopFile.getAbsolutePath() + " already exists. Refreshing and not proceeding with desktop additions");
            exists = true;
            if (JNLPRuntime.isOnline()) {
                entry.refreshExistingShortcuts(false, true); //update
            }
        }
        if (possibleMenuFile.exists()) {
            OutputController.getLogger().log("ApplicationInstance.addMenuAndDesktopEntries(): file - "
                    + possibleMenuFile.getAbsolutePath() + " already exists. Refreshing and not proceeding with desktop additions");
            exists = true;
            if (JNLPRuntime.isOnline()) {
                entry.refreshExistingShortcuts(true, false); //update
            }
        }
        if (generatedJnlp.exists()) {
            OutputController.getLogger().log("ApplicationInstance.addMenuAndDesktopEntries(): generated file - "
                    + generatedJnlp.getAbsolutePath() + " already exists. Refreshing and not proceeding with desktop additions");
            exists = true;
            if (JNLPRuntime.isOnline()) {
                entry.refreshExistingShortcuts(true, true); //update
            }
        }
        if (exists){
            return;
        }
        AccessWarningPaneComplexReturn ics = getComplexReturn(sd);
        if (ics.getRegularReturnAsBoolean()) {
            entry.createDesktopShortcuts(ics.getMenu(), ics.getDekstop(), isSigned());
        }

    }

    /**
     * Indicates whether a desktop launcher/shortcut should be created for this
     * application instance
     *
     * @param sd the ShortcutDesc element from the JNLP file
     * @return true if a desktop shortcut should be created
     */
    private AccessWarningPaneComplexReturn getComplexReturn(ShortcutDesc sd) {
        if (JNLPRuntime.isTrustAll()) {
            boolean mainResult = (sd != null && (sd.onDesktop() || sd.getMenu() != null));
            AccessWarningPaneComplexReturn r = new AccessWarningPaneComplexReturn(mainResult?0:1);
            if (mainResult){
                if (sd.onDesktop()){
                    r.setDekstop(new AccessWarningPaneComplexReturn.ShortcutResult(true));
                    r.getDekstop().setBrowser(XDesktopEntry.getBrowserBin());
                    r.getDekstop().setShortcutType(AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.BROWSER);
                }
                if (sd.getMenu() != null){
                    r.setMenu(new AccessWarningPaneComplexReturn.ShortcutResult(true));
                    r.getMenu().setBrowser(XDesktopEntry.getBrowserBin());
                    r.getMenu().setShortcutType(AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.BROWSER);
                }
            }
            return r;
        }
        String currentSetting = JNLPRuntime.getConfiguration()
                .getProperty(DeploymentConfiguration.KEY_CREATE_DESKTOP_SHORTCUT);

        /*
         * check configuration and possibly prompt user to find out if a
         * shortcut should be created or not
         */
        switch (currentSetting) {
            case ShortcutDesc.CREATE_NEVER:
                return new AccessWarningPaneComplexReturn(1);
            case ShortcutDesc.CREATE_ALWAYS:
                return new AccessWarningPaneComplexReturn(0);
            case ShortcutDesc.CREATE_ASK_USER:
                return SecurityDialogs.showAccessWarningDialogComplexReturn(AccessType.CREATE_DESTKOP_SHORTCUT, file);
            case ShortcutDesc.CREATE_ASK_USER_IF_HINTED:
                if (sd != null && (sd.onDesktop() || sd.toMenu())) {
                    return SecurityDialogs.showAccessWarningDialogComplexReturn(AccessType.CREATE_DESTKOP_SHORTCUT, file);
                }
            case ShortcutDesc.CREATE_ALWAYS_IF_HINTED:
                if (sd != null && (sd.onDesktop() || sd.toMenu())) {
                    return new AccessWarningPaneComplexReturn(0);
                }
        }

        return new AccessWarningPaneComplexReturn(1);
    }
    
     /**
     * Releases the application's resources before it is collected.
     * Only collectable if classloader and thread group are
     * also collectable so basically is almost never called (an
     * application would have to close its windows and exit its
     * threads but not call JNLPRuntime.exit).
     */
    @Override
    public void finalize() {
        destroy();
    }

    /**
     * Install the environment variables.
     */
    void installEnvironment() {
        final PropertyDesc props[] = file.getResources().getProperties();

        CodeSource cs = new CodeSource((URL) null, (java.security.cert.Certificate[]) null);

        JNLPClassLoader loader = (JNLPClassLoader) this.loader;
        SecurityDesc s = loader.getSecurity();

        ProtectionDomain pd = new ProtectionDomain(cs, s.getPermissions(cs), null, null);

        // Add to hashmap
        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[] { pd });

        PrivilegedAction<Object> installProps = new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                for (PropertyDesc propDesc : props) {
                    System.setProperty(propDesc.getKey(), propDesc.getValue());
                }

                return null;
            }
        };
        AccessController.doPrivileged(installProps, acc);
    }

    /**
     * Returns the jnlpfile on which is this application based
     * @return JNLP file for this task.
     */
    public JNLPFile getJNLPFile() {
        return file;
    }

    /**
     * Returns the application title.
     * @return the title of this application
     */
    public String getTitle() {
        return file.getTitle();
    }

    /**
     * Returns whether the application is running.
     * @return state of application
     */
    public boolean isRunning() {
        return !stopped;
    }

    /**
     * Stop the application and destroy its resources.
     */
    @SuppressWarnings("deprecation")
    public void destroy() {
        if (stopped)
            return;

        try {
            // destroy resources
            for (Window w : weakWindows) {
                if (w != null)
                    w.dispose();
            }

            weakWindows.clear();

            // interrupt threads
            Thread threads[] = new Thread[group.activeCount() * 2];
            int nthreads = group.enumerate(threads);
            for (int i = 0; i < nthreads; i++) {
                OutputController.getLogger().log("Interrupt thread: " + threads[i]);
                threads[i].interrupt();
            }

            // then stop
            Thread.yield();
            nthreads = group.enumerate(threads);
            for (int i = 0; i < nthreads; i++) {
                OutputController.getLogger().log("Stop thread: " + threads[i]);
                threads[i].stop();
            }

            // then destroy - except Thread.destroy() not implemented in jdk

        } finally {
            stopped = true;
            fireDestroyed();
        }
    }

    /**
     * Returns the thread group.
     *
     * @return thread group of this application, unless it is stopped
     * @throws IllegalStateException if the app is not running
     */
    public ThreadGroup getThreadGroup() throws IllegalStateException {
        if (stopped)
            throw new IllegalStateException();

        return group;
    }

    /**
     * Returns the classloader.
     *
     * @return the classloader of this application, unless it is stopped
     * @throws IllegalStateException if the app is not running
     */
    public ClassLoader getClassLoader() throws IllegalStateException {
        if (stopped)
            throw new IllegalStateException();

        return loader;
    }

    /**
     * Adds a window that this application opened.  When the
     * application is disposed, these windows will also be disposed.
     * @param window to be added
     */
    protected void addWindow(Window window) {
        weakWindows.add(window);
        weakWindows.trimToSize();
    }

    /**
     * @return whether or not this application is signed.
     */
    public boolean isSigned() {
        return isSigned;
    }

    /**
     *
     * @return application context of this instance
     */
    public AppContext getAppContext() {
        return appContext;
    }

}
