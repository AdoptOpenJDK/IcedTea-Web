/* package-info.java
 Copyright (C) 2014 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 IcedTea; see the file COPYING. If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is making a
 combined work based on this library. Thus, the terms and conditions of the GNU
 General Public License cover the whole combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent modules, and
 to copy and distribute the resulting executable under terms of your choice,
 provided that you also meet, for each linked independent module, the terms and
 conditions of the license of that module. An independent module is a module
 which is not derived from or based on this library. If you modify this library,
 you may extend this exception to your version of the library, but you are not
 obligated to do so. If you do not wish to do so, delete this exception
 statement from your version.*/
package sun.applet;

import java.applet.Applet;
import java.applet.AppletContext;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import net.sourceforge.jnlp.NetxPanel;
import net.sourceforge.jnlp.util.logging.OutputController;

public abstract class AppletViewerPanelAccess extends AppletViewerPanel {

    public AppletViewerPanelAccess(URL documentURL, Map<String, String> atts) {
        // note, this is copy.
        // But th eonly usecasein applet outside ITW is get on parameters
        super(documentURL, new Hashtable<>(atts));
    }

    protected URL getDocumentURL() {
        try {
            Field field = AppletViewerPanel.class.getDeclaredField("documentURL");
            field.setAccessible(true);
            return (URL) field.get(this);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void setApplet(Applet iapplet) {
        try {
            Field field = AppletPanel.class.getDeclaredField("applet");
            field.setAccessible(true);
            field.set(this, iapplet);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        // this is copypasted chunk from AppletPanel.run (the only current 
        // call of runLoader). Pray it do not change
        Thread curThread = Thread.currentThread();
        if (curThread == loaderThread) {
            ourRunLoader();
            return;
        }

        super.run();
    }

    @Override
    public AppletContext getAppletContext() {
        if (getParent() instanceof AppletContext) {
            return super.getAppletContext();
        }
        return ((NetxPanel)this).getAppInst().getAppletEnvironment();
    }
    
    /**
     * NOTE. We cannot override private method, and this call is unused and useless.
     * But kept for record of troubles to run on any openjdk.
     * upstream patch posted http://mail.openjdk.java.net/pipermail/awt-dev/2014-May/007828.html
     */
    private void superRunLoader() {
        try {
            Class klazz = AppletPanel.class;
            Method runLoaderMethod = klazz.getDeclaredMethod("runLoader");
            runLoaderMethod.setAccessible(true);
            runLoaderMethod.invoke(getApplet());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }


    protected URL getBaseURL() {
        try {
            Field field = AppletViewerPanel.class
                    .getDeclaredField("baseURL");
            field.setAccessible(
                    true);
            return (URL) field.get(
                    this);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }

    }
    
    @Override
    //remaining stub of unpatched jdk
    protected synchronized void createAppletThread() {
        throw new RuntimeException("Not yet implemented");
        //no need to call super, is overriden, and not used in  upstream
        //AppletViewerPanel or AppletPanel
    }

    abstract protected void ourRunLoader();

    /**
     * jdk9 removed doInit.
     * http://hg.openjdk.java.net/jdk9/jdk9/jdk/rev/2b680924a73f This is way how
     * to set it in older jdks and still compile on jdk9+
     *
     * @param a value to set to doInit if it exists
     */
    protected void setDoInitIfExists(boolean a) {
        //doInit = a;
        try {
            Class c = this.getClass();
            Field fs = null;
            while (c != null) {
                if (AppletPanel.class.equals(c)) {
                    fs = c.getDeclaredField("doInit");
                    break;
                }
                //known location is NetxPanel->AppeltViwerPannelAccess->AppletViwerPanel->AppletPanel
                c = c.getSuperclass();
            }
            if (fs == null) {
                throw new NoSuchFieldException("AppletPanel not found.");
            }
            fs.setAccessible(true);
            fs.set(this, a);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            OutputController.getLogger().log("Can't get/set doInit. Runing on JDK9 or higher?");
            OutputController.getLogger().log(ex);
        }

    }

}
