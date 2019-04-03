import java.applet.Applet;

import netscape.javascript.JSObject;

public class JavascriptFuncReturn extends Applet {

    private JSObject window;

    public void init() {
        window = JSObject.getWindow(this);
        String initStr = "JToJSFuncReturn applet initialized.";
        System.out.println(initStr);
    }

    // method for testing return types of JavaScript function
    public void jCallJSFunction() {
        if (window == null) {
            window = JSObject.getWindow(this);
            System.out.println("Null window.");
        }
        String  returnTypeTestFuncName = "jsReturningFunction";
        Object ret = window.call(returnTypeTestFuncName, new Object[]{});
        System.out.println(ret.toString());
    }

    // auxiliary class and methods
    public void writeAfterTests() {
        System.out.print("afterTests");
    }

    public class DummyObject {
        private String str;

        public DummyObject(String s) {
            this.str = s;
        }

        public void setStr(String s) {
            this.str = s;
        }

        public String toString() {
            return str;
        }
    }

    public DummyObject getNewDummyObject(String s){
        return new DummyObject(s);
    }

}
