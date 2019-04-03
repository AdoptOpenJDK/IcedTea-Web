import java.applet.Applet;

import netscape.javascript.JSObject;

public class JavascriptSet extends Applet {

    private JSObject window;

    public void init() {
        window = JSObject.getWindow(this);
        String initStr = "JToJSSet applet initialized.";
        System.out.println(initStr);
    }

    // methods for testing setting of JavaScript variables
    public void jjsSetInt() {
        setWindowMember((int) 1);
    }

    public void jjsSetInteger() {
        setWindowMember(new Integer(2));
    }

    public void jjsSetdouble() {
        setWindowMember((double) 2.5);
    }

    public void jjsSetDouble() {
        setWindowMember(new Double(2.5));
    }

    public void jjsSetfloat() {
        setWindowMember((float) 2.5);
    }

    public void jjsSetFloat() {
        setWindowMember(new Float(2.5));
    }

    public void jjsSetshort() {
        setWindowMember((short) 3);
    }

    public void jjsSetShort() {
        setWindowMember(new Short((short) 4));
    }

    public void jjsSetlong() {
        setWindowMember((long) 4294967296L);
    }

    public void jjsSetLong() {
        setWindowMember(new Long(4294967297L));
    }

    public void jjsSetbyte() {
        setWindowMember((byte) 5);
    }

    public void jjsSetByte() {
        setWindowMember(new Byte((byte) 6));
    }

    public void jjsSetchar() {
        setWindowMember((char) 'a');
    }

    public void jjsSetCharacter() {
        setWindowMember(new Character('a'));
    }

    public void jjsSetboolean() {
        setWindowMember((boolean) true);
    }

    public void jjsSetBoolean() {
        setWindowMember(new Boolean(true));
    }

    public void jjsSetString() {
        setWindowMember("𠁎〒£$ǣ€𝍖");
    }

    public void jjsSetObject() {
        DummyObject dummyObject = new DummyObject("DummyObject2");
        setWindowMember(dummyObject);
    }

    public void jjsSet1DArray() {
        if (window == null) {
            window = JSObject.getWindow(this);
            System.out.println("Null window");
        }
        ((JSObject) window.getMember("setvar")).setSlot(1, 100);
    }

    public void jjsSet2DArray() {
        if (window == null) {
            window = JSObject.getWindow(this);
            System.out.println("Null window");
        }
        ((JSObject) ((JSObject) window.getMember("setvar")).getSlot(1)).setSlot(1, 200);
    }

    public void jjsSetJSObject() {
        setWindowMember(window);
    }

    public void setWindowMember(Object value) {
        if (window == null) {
            window = JSObject.getWindow(this);
            System.out.println("Null window");
        }
        window.setMember("setvar", value);
    }

    // auxiliary class and method for writing output:
    public void printStrAndFinish(String str) {
        System.out.println(str);
        System.out.println("afterTests");
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

}
