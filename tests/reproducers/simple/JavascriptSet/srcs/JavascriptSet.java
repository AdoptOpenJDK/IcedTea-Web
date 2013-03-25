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
        window.setMember("setvar", (int) 1);
    }

    public void jjsSetInteger() {
        window.setMember("setvar", new Integer(2));
    }

    public void jjsSetdouble() {
        window.setMember("setvar", (double) 2.5);
    }

    public void jjsSetDouble() {
        window.setMember("setvar", new Double(2.5));
    }

    public void jjsSetfloat() {
        window.setMember("setvar", (float) 2.5);
    }

    public void jjsSetFloat() {
        window.setMember("setvar", new Float(2.5));
    }

    public void jjsSetshort() {
        window.setMember("setvar", (short) 3);
    }

    public void jjsSetShort() {
        window.setMember("setvar", new Short((short) 4));
    }

    public void jjsSetlong() {
        window.setMember("setvar", (long) 4294967296L);
    }

    public void jjsSetLong() {
        window.setMember("setvar", new Long(4294967297L));
    }

    public void jjsSetbyte() {
        window.setMember("setvar", (byte) 5);
    }

    public void jjsSetByte() {
        window.setMember("setvar", new Byte((byte) 6));
    }

    public void jjsSetchar() {
        window.setMember("setvar", (char) 'a');
    }

    public void jjsSetCharacter() {
        window.setMember("setvar", new Character('a'));
    }

    public void jjsSetboolean() {
        window.setMember("setvar", (boolean) true);
    }

    public void jjsSetBoolean() {
        window.setMember("setvar", new Boolean(true));
    }

    public void jjsSetString() {
        window.setMember("setvar", "𠁎〒£$ǣ€𝍖");
    }

    public void jjsSetObject() {
        DummyObject dummyObject = new DummyObject("DummyObject2");
        window.setMember("setvar", dummyObject);
    }

    public void jjsSet1DArray() {
        ((JSObject) window.getMember("setvar")).setSlot(1, 100);
    }

    public void jjsSet2DArray() {
        ((JSObject) ((JSObject) window.getMember("setvar")).getSlot(1)).setSlot(1, 200);
    }

    public void jjsSetJSObject(){
        window.setMember("setvar", window);
    }


    // auxiliary class and method for writing output:
    public void printStrAndFinish(String str){
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
