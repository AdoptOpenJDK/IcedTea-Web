package net.adoptopenjdk.icedteaweb.integration.reproducers.missingCodebases.applications;

import java.applet.Applet;


public class MissingCodebases extends Applet {

    public static String ID = "MissingCodebases";

    public static void main(String... args) {
        System.out.println("main MissingCodebases");
    }

    public void init() {
        System.out.println("init MissingCodebases");
        try {
           // writeFile(ID, writer -> writer.write("init MissingCodebases"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void start() {
        System.out.println("start MissingCodebases");
        try {
           // writeFile(ID , writer -> writer.write("start MissingCodebases"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
