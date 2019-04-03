/* 
Copyright (C) 2014 Red Hat, Inc.

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

import java.applet.Applet;
import netscape.javascript.JSObject;

public class JToJSString extends Applet {

    public String[] myStringArray = { "a", "b", "c" };
    public String myString = "notarrayitem";

    private final String writetojs = "writetojs";
    private final String writetojss = "writetojss";

    private JSObject window;

    @Override
    public void init() {
        System.out.println("init");
        window = JSObject.getWindow(this);

        if (window == null) {
            System.out.println("init stage: window is null");
        } else {
            window.call("writeToJS", new Object[] { myStringArray });
            window.call("writeToJSs", new Object[] { myString });

            System.out.println("init stage: complete");
        }
    }

    public void jjsReadString(String element) {
        System.out.println("jjsReadString " + element);
        if (window == null) {
            window = JSObject.getWindow(this);
            System.out.println("jjsReadString stage: window is null");
        }

        Object value = window.call("getDOMElementByID", new Object[] { element });
        System.out.println(value);

    }

    public void readStringAndFinish() {
        System.out.println("readStringAndFinish");

        jjsReadString("writetojs");
        jjsReadString("writetojss");

        System.out.println("afterTests");
    }
    
    public void printFromJS(String message) {
        System.out.println(message);
    }

    @Override
    public void destroy() {
        System.out.println("destroy");
    }
}
