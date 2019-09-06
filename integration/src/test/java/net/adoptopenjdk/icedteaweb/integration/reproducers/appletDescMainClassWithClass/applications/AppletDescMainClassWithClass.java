package net.adoptopenjdk.icedteaweb.integration.reproducers.appletDescMainClassWithClass.applications;

import java.applet.Applet;

public class AppletDescMainClassWithClass extends Applet {

    public static String ID = "AppletDescMainClassWithClass";

    public void init() {
        System.out.println("init AppletDescMainClassWithClass");
        try {
           // writeFile(ID, writer -> writer.write("init AppletDescMainClassWithClass"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void start() {
        System.out.println("start AppletDescMainClassWithClass");
        try {
           // writeFile(ID , writer -> writer.write("start AppletDescMainClassWithClass"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
