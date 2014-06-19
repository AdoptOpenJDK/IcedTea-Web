import java.applet.Applet;
import netscape.javascript.JSObject;

public class JavascriptFuncParam extends Applet {

    public DummyObject dummyObject = new DummyObject("DummyObject1");
    private JSObject window;

    private final String jsFunctionName = "JJSParameterTypeFunc";

    public void init() {
        window = JSObject.getWindow(this);
        String initStr = "JToJSFuncParam applet initialized.";
        System.out.println(initStr);
    }

    // methods for testing calling JavaScript function with different parameters
    public void jjsCallintParam() {
        int i = 1;
        passToJavascript(i);
    }

    public void jjsCalldoubleParam() {
        double d = 1.1;
        passToJavascript(d);
    }

    public void jjsCallfloatParam() {
        float f = 1.5f;
        passToJavascript(f);
    }

    public void jjsCalllongParam() {
        long l = 10000;
        passToJavascript(l);
    }

    public void jjsCallshortParam() {
        short s = 1;
        passToJavascript(s);
    }

    public void jjsCallbyteParam() {
        byte b = 1;
        passToJavascript(b);
    }

    public void jjsCallcharParam() {
        char c = 'a';
        passToJavascript(c, "97");
    }

    public void jjsCallbooleanParam() {
        boolean b = true;
        passToJavascript(b);
    }

    public void jjsCallIntegerParam() {
        Integer i = new Integer(1);
        passToJavascript(i);
    }

    public void jjsCallDoubleParam() {
        Double i = new Double(1.5);
        passToJavascript(i);
    }

    public void jjsCallFloatParam() {
        Float i = new Float(1.5);
        passToJavascript(i);
    }

    public void jjsCallLongParam() {
        Long i = new Long(10000);
        passToJavascript(i);
    }

    public void jjsCallShortParam() {
        Short i = new Short((short) 1);
        passToJavascript(i);
    }

    public void jjsCallByteParam() {
        Byte i = new Byte((byte) 1);
        passToJavascript(i);
    }

    public void jjsCallBooleanParam() {
        Boolean i = new Boolean(true);
        passToJavascript(i);
    }

    public void jjsCallCharacterParam() {
        Character i = new Character('a');// 97
        passToJavascript(i, "97");
    }

    public void jjsCallStringParam() {
        String i = "teststring";
        passToJavascript(i, "\"teststring\"");
    }

    public void jjsCallDummyObjectParam() {
        DummyObject i = new DummyObject("dummy1");
        passToJavascript(i, "applet.getNewDummyObject(\"dummy1\")");
    }

    public void jjsCallJSObjectParam() {
        passToJavascript(JSObject.getWindow(this), "window");
    }

    private void passToJavascript(Object obj, String repr) {
        if (window == null) {
            window = JSObject.getWindow(this);
            System.out.println("Null window.");
        }
        window.call(jsFunctionName, new Object[] { obj, repr });
    }

    private void passToJavascript(Object obj) {
        passToJavascript(obj, obj.toString());
    }

    // auxiliary methods and class:
    public void printOut(String s) {
        System.out.println(s);
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

    public DummyObject getNewDummyObject(String s) {
        return new DummyObject(s);
    }

}
