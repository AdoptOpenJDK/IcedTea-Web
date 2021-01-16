// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.util.WeakList;

import java.applet.Applet;
import java.awt.Window;
import java.util.Optional;

/**
 * Represents a launched application instance created from a JNLP
 * file.  This class does not control the operation of the applet,
 * use the AppletEnvironment class to start and stop the applet.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.9 $
 */
public class AppletInstance extends ApplicationInstance {

    private static final Logger LOG = LoggerFactory.getLogger(AppletInstance.class);

    /** whether the applet's stop and destroy methods have been called */
    private boolean appletStopped = false;

    /** the applet */
    private Applet applet;

    /** the applet environment */
    private final AppletEnvironment environment;

    /**
     * weak list of windows opened by the application
     */
    private final WeakList<Window> weakWindows = new WeakList<>();

    /**
     * Create a New Task based on the Specified URL
     * @param file pluginbridge to build instance on
     *
     */
    public AppletInstance(JNLPFile file, final ThreadGroup threadGroup) throws LaunchException {
        super(file, threadGroup);
        this.environment = new AppletEnvironment(file, this);
    }

    /**
     * Set the applet of this launched application; can only be called once.
     * @param applet to be set
     */
    public void setApplet(Applet applet) {
        if (this.applet != null) {
                LOG.error("Applet can only be set once.", new IllegalStateException("Applet can only be set once."));
            return;
        }
        this.applet = applet;
    }

    /**
     * @return the applet environment.
     */
    public AppletEnvironment getAppletEnvironment() {
        return environment;
    }

    /**
     *  @return the applet.
     */
    public Applet getApplet() {
        return applet;
    }

    @Override
    protected String determineMainClass(JNLPFile file, ResourceTracker tracker) {
        return null;
    }

    /**
     * Stop the application and destroy its resources.
     */
    @Override
    public synchronized void destroy() {
        if (appletStopped)
            return;

        appletStopped = true;

        try {
            applet.stop();
            applet.destroy();
        } catch (Exception ex) {
            LOG.error("Exception while destroying AppletInstance", ex);
        }

        environment.destroy();

        weakWindows.forEach(w -> Optional.ofNullable(w).ifPresent(win -> win.dispose()));
        weakWindows.clear();

        super.destroy();
    }

    /**
     * Adds a window that this application opened.  When the
     * application is disposed, these windows will also be disposed.
     *
     * @param window to be added
     */
    void addWindow(Window window) {
        weakWindows.add(window);
        weakWindows.trimToSize();
    }

}
