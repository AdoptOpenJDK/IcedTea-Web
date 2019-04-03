/*
 Copyright (C) 2012 Red Hat, Inc.

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

import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import netscape.javascript.JSObject;

public class AppletJsAppletDeadlock extends java.applet.Applet {

    private TextArea outputText = null;

    public void printOutput(String msg) {
        System.out.println(msg);
        outputText.setText(outputText.getText() + msg + "\n");
    }

    public void jsCallback(String location) {
        printOutput("Callback function called");

        // try requesting the page
        try {
            URL url = new URL(location);
            URLConnection con = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                System.err.println(line);
            }

            printOutput("Succesfully connected to " + location);
        } catch (Exception e) {
            printOutput("Failed to connect to '" + location + "': " + e.getMessage());
        }

    }

    @Override
    public void start() {
//    public void init() {
        outputText = new TextArea("", 12, 95);
        this.add(outputText);

        printOutput("AppletJsAppletDeadlock started");

        JSObject win = JSObject.getWindow(this);
        win.eval("callApplet();");

        printOutput("JS call finished");
    }
}
