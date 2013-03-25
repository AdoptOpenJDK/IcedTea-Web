import java.applet.Applet;
import java.util.Arrays;
import netscape.javascript.JSObject;

public class JavascriptGet extends Applet {

    public DummyObject dummyObject = new DummyObject("DummyObject1");
    public Object value;
    private JSObject window;

    private final String jsvar = "jsvar";

    public void init() {
        window = JSObject.getWindow(this);
    
        String initStr = "JToJSGet applet initialized.";
        System.out.println(initStr);
    }

    // methods for testing read from JavaScript variables
    public void jjsReadInt() {
//        value = new Integer(window.getMember(jsvar).toString());
        int num = ((Number) window.getMember(jsvar)).intValue();
        System.out.println(value);
    }

    public void jjsReadDouble() {
        value = new Double(window.getMember(jsvar).toString());
        System.out.println(value);
    }

    public void jjsReadBoolean() {
        value = new Boolean(window.getMember(jsvar).toString());
        System.out.println(value);
    }

    public void jjsReadString() {
        value = window.getMember(jsvar).toString();
        System.out.println(value);
    }

    public void jjsReadObject() {
        value = window.getMember(jsvar).toString();
        System.out.println(value);
    }

    public void jjsRead1DArray() {
        Object[] arrayvalue = (Object[]) window.getMember(jsvar);

        System.out.println(Arrays.toString(arrayvalue));
    }

    public void jjsRead2DArray() {
        Object[][] arrayvalue = (Object[][])window.getMember(jsvar);

        System.out.println(Arrays.deepToString(arrayvalue));
    }

    public void jjsReadJSObject() {
        JSObject jsobjectvalue = (JSObject) window.getMember(jsvar);

        System.out.println(jsobjectvalue);
    }

    //auxiliary class DummyObject
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

    //auxiliary methods:
    public DummyObject getNewDummyObject(String s){
        return new DummyObject(s);
    }

    public void writeAfterTests(){
        System.out.println("afterTests");
    }

}
