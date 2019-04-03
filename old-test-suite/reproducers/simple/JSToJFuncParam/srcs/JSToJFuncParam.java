import java.applet.Applet;
import java.util.Arrays;
import netscape.javascript.JSObject;

public class JSToJFuncParam extends Applet {

    public void init() {
        String initStr = "JSToJFuncParam applet initialized.";
        System.out.println(initStr);
    }

    public void intParam(int i) {
        System.out.println("intParam " + i);
    }

    public void doubleParam(double d) {
        System.out.println("doubleParam " + d);
    }

    public void floatParam(float f) {
        System.out.println("floatParam " + f);
    }

    public void longParam(long l) {
        System.out.println("longParam " + l);
    }

    public void booleanParam(boolean b) {
        System.out.println("booleanParam " + b);
    }

    public void charParam(char c) {
        System.out.println("charParam " + c);
    }

    public void byteParam(byte b) {
        System.out.println("byteParam " + b);
    }

    public void charArrayParam(char[] ca) {
        System.out.println("charArrayParam " + Arrays.toString(ca));
    }

    public void StringParam(String s) {
        System.out.println("StringParam " + s);
    }

    public void IntegerParam(Integer p) {
        System.out.println("IntegerParam " + p);
    }

    public void DoubleParam(Double p) {
        System.out.println("DoubleParam " + p);
    }

    public void FloatParam(Float p) {
        System.out.println("FloatParam " + p);
    }

    public void LongParam(Long p) {
        System.out.println("LongParam " + p);
    }

    public void BooleanParam(Boolean p) {
        System.out.println("BooleanParam " + p);
    }

    public void CharacterParam(Character p) {
        System.out.println("CharacterParam " + p);
    }

    public void ByteParam(Byte p) {
        System.out.println("ByteParam " + p);
    }

    public void StringIntMixedParam(String[] s) {
        System.out.println("StringIntMixedParam " + Arrays.toString(s));
    }

    public void DummyObjectArrayParam(DummyObject[] ca) {
        System.out.println("DummyObjectArrayParam " + Arrays.toString(ca));
    }

    public void JSObjectParam(JSObject car) {
        Integer mph = (Integer) car.getMember("mph");
        String color = (String) car.getMember("color");

        System.out.println("JSObjectParam " + mph + ", " + color);
    }

    public void writeAfterTest() {
        System.out.println("afterTests");
    }

    public void readFromJS(String message) {
        System.out.println(message);
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

    public DummyObject getNewDummyObject(String str) {
        return new DummyObject(str);
    }
}
