/* -*- Mode: Java; tab-width: 8; c-basic-offset: 4 -*-
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mozilla Communicator client code, released
 * March 31, 1998.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1998
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

/* more doc TODO:
 *  threads
 *  gc
 *
 *
 */

package netscape.javascript;

import java.applet.Applet;
import java.security.AccessControlException;
import java.security.AccessController;

import sun.applet.PluginAppletViewer;
import sun.applet.PluginDebug;

/**
 * JSObject allows Java to manipulate objects that are
 * defined in JavaScript.
 * Values passed from Java to JavaScript are converted as
 * follows:<ul>
 * <li>JSObject is converted to the original JavaScript object
 * <li>Any other Java object is converted to a JavaScript wrapper,
 *   which can be used to access methods and fields of the java object.
 *   Converting this wrapper to a string will call the toString method
 *   on the original object, converting to a number will call the
 *   doubleValue method if possible and fail otherwise.  Converting
 *   to a boolean will try to call the booleanValue method in the
 *   same way.
 * <li>Java arrays are wrapped with a JavaScript object that understands
 *   array.length and array[index]
 * <li>A Java boolean is converted to a JavaScript boolean
 * <li>Java byte, char, short, int, long, float, and double are converted
 *   to JavaScript numbers
 * </ul>
 * Values passed from JavaScript to Java are converted as follows:<ul>
 * <li>objects which are wrappers around java objects are unwrapped
 * <li>other objects are wrapped with a JSObject
 * <li>strings, numbers and booleans are converted to String, Double,
 *   and Boolean objects respectively
 * </ul>
 * This means that all JavaScript values show up as some kind
 * of java.lang.Object in Java.  In order to make much use of them,
 * you will have to cast them to the appropriate subclass of Object,
 * e.g. <code>(String) window.getMember("name");</code> or
 * <code>(JSObject) window.getMember("document");</code>.
 */
public final class JSObject {
    /* the internal object data */
    private long internal;

    /**
     * initialize
     */
    private static void initClass() {
        PluginDebug.debug("JSObject.initClass");
    }

    static {
        PluginDebug.debug("JSObject INITIALIZER");
    }

    /**
     * it is illegal to construct a JSObject manually
     */
    public JSObject(int jsobj_addr) {
        this((long) jsobj_addr);
    }

    /**
     * it is illegal to construct a JSObject manually
     */
    public JSObject(String jsobj_addr) {
        this(Long.parseLong(jsobj_addr));
    }

    public JSObject(long jsobj_addr) {

        // See if the caller has permission

        try {
            AccessController.getContext().checkPermission(new JSObjectCreatePermission());
        } catch (AccessControlException ace) {

            // If not, only caller with JSObject.getWindow on the stack may
            // make this call unprivileged.

            // Although this check is inefficient, it should happen only once
            // during applet init, so we look the other way

            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            boolean mayProceed = false;

            for (int i = 0; i < stack.length; i++) {
                if (stack[i].getClassName().equals("netscape.javascript.JSObject") &&
                        stack[i].getMethodName().equals("getWindow")) {
                    mayProceed = true;
                }
            }

            if (!mayProceed)
                throw ace;
        }

        PluginDebug.debug("JSObject long CONSTRUCTOR");
        internal = jsobj_addr;
    }

    /**
     * Retrieves a named member of a JavaScript object.
     * Equivalent to "this.<i>name</i>" in JavaScript.
     */
    public Object getMember(String name) {
        PluginDebug.debug("JSObject.getMember ", name);

        Object o = PluginAppletViewer.getMember(internal, name);
        PluginDebug.debug("JSObject.getMember GOT ", o);
        return o;
    }

    /**
     * Retrieves an indexed member of a JavaScript object.
     * Equivalent to "this[<i>index</i>]" in JavaScript.
     */
    //    public Object         getMember(int index) { return getSlot(index); }
    public Object getSlot(int index) {
        PluginDebug.debug("JSObject.getSlot ", index);

        return PluginAppletViewer.getSlot(internal, index);
    }

    /**
     * Sets a named member of a JavaScript object.
     * Equivalent to "this.<i>name</i> = <i>value</i>" in JavaScript.
     */
    public void setMember(String name, Object value) {
        PluginDebug.debug("JSObject.setMember ", name, " ", value);

        PluginAppletViewer.setMember(internal, name, value);
    }

    /**
     * Sets an indexed member of a JavaScript object.
     * Equivalent to "this[<i>index</i>] = <i>value</i>" in JavaScript.
     */
    //    public void           setMember(int index, Object value) {
    //        setSlot(index, value);
    //    }
    public void setSlot(int index, Object value) {
        PluginDebug.debug("JSObject.setSlot ", index, " ", value);

        PluginAppletViewer.setSlot(internal, index, value);
    }

    /**
     * Removes a named member of a JavaScript object.
     */
    public void removeMember(String name) {
        PluginDebug.debug("JSObject.removeMember ", name);

        PluginAppletViewer.removeMember(internal, name);
    }

    /**
     * Calls a JavaScript method.
     * Equivalent to "this.<i>methodName</i>(<i>args</i>[0], <i>args</i>[1], ...)" in JavaScript.
     */
    public Object call(String methodName, Object args[]) {
        if (args == null)
            args = new Object[0];

        PluginDebug.debug("JSObject.call ", methodName);
        for (int i = 0; i < args.length; i++)
            PluginDebug.debug(" ", args[i]);
        PluginDebug.debug("");
        return PluginAppletViewer.call(internal, methodName, args);
    }

    /**
     * Evaluates a JavaScript expression. The expression is a string
     * of JavaScript source code which will be evaluated in the context
     * given by "this".
     */
    public Object eval(String s) {
        PluginDebug.debug("JSObject.eval ", s);
        return PluginAppletViewer.eval(internal, s);
    }

    /**
     * Converts a JSObject to a String.
     */
    public String toString() {
        PluginDebug.debug("JSObject.toString");
        return PluginAppletViewer.javascriptToString(internal);
    }

    // should use some sort of identifier rather than String
    // is "property" the right word?
    //    native String[]                         listProperties();

    /**
     * get a JSObject for the window containing the given applet
     */
    public static JSObject getWindow(Applet applet) {
        PluginDebug.debug("JSObject.getWindow");
        // FIXME: handle long case as well.
        long internal = 0;
        internal = ((PluginAppletViewer)
                    applet.getAppletContext()).getWindow();
        PluginDebug.debug("GOT IT: ", internal);
        return new JSObject(internal);
    }

    /**
     * Finalization decrements the reference count on the corresponding
     * JavaScript object.
     */
    protected void finalize() {

        // Proceed if this is a valid object (0L == default long == invalid)
        if (internal == 0L)
            return;

        PluginDebug.debug("JSObject.finalize ");
        PluginAppletViewer.JavaScriptFinalize(internal);
    }
}
