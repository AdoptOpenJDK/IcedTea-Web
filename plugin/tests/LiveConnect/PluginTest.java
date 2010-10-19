import javax.swing.JApplet;
import java.awt.Graphics;
import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import netscape.javascript.JSObject;
import java.lang.reflect.Array;

public class PluginTest extends JApplet {

    public int i = 42;
    public double d = 42.42;
    public float f = 42.1F;
    public long l = 4294967296L;
    public boolean b = true;
    public char c = '\u2323';
    public byte by = 43;
    public String rs = "I'm a string!";
    public String ss = "𠁎〒£$ǣ€𝍖";
    public Object n = null;
    public int[] ia = new int[5];

    public Integer I = 24;
    public Double D = 24.24;
    public Float F = 24.124F;
    public Long L = 6927694924L;
    public Boolean B = false;
    public Character C = '\u1526';
    public Byte By = 34;
    public Double[] Da1 = new Double[10];
    public Double[] Da2 = null;

    public char[] ca = new char[3];
    public Character[] Ca = new Character[3];

    public void setUpForGMTests() {
        i = 42;
        d = 42.42;
        f = 42.1F;
        l = 4294967296L;
        b = true;
        c = '\u2323';
        by = 43;
        rs = "I'm a string!";
        ss = "𠁎〒£$ǣ€𝍖";
        n = null;

        I = 24;
        D = 24.24;
        F = 24.124F;
        L = 6927694924L;
        B = false;
        C = '\u1526';
        By = 34;

        ia[4] = 1024;
        Da1[9] = D;
    }

    public void setUpForSMTests() {
        i = 0;
        d = 0.0;
        f = 0F;
        l = 0L;
        b = false;
        c = 'A';
        by = 0;
        rs = "";
        ss = "";
        n = new String("non-null object");

        I = 0;
        D = 0.0;
        F = 0F;
        L = 0L;
        B = false;
        C = 'A';
        By = null;

        ia[4] = 0;
        Da1[9] = D;
    }

    /*
     *****************************************
     * JS -> Java Parameter conversion tests *
     *****************************************
    */
    public void setUpForReturnTests() {
        i = 41;
        d = 41.41;
        f = 41.411F;
        l = 4294967297L;
        b = true;
        c = '\u2329';
        by = 44;
        rs = "I'm a string too!";
        ss = "𠁎〒£$ǣ€𝍖";
        n = null;

        I = 14;
        D = 14.14;
        F = 14.114F;
        L = 6927694925L;
        B = false;
        C = '\u2417';
        By = 46;
    }

    /*
     **************************************
     * JS -> Java invocation return tests *
     **************************************
    */

    public int intReturnTest() { return i; }

    public double doubleReturnTest() { return d; }

    public float floatReturnTest() { return f; }

    public long longReturnTest() { return l; }

    public boolean booleanReturnTest() { return b; }

    public char charReturnTest() { return c; }

    public byte byteReturnTest() { return by; }
    
    public char[] charArrayReturnTest() { 
        ca[0] = '\u2410';
        ca[1] = '\u2411';
        ca[2] = '\u2412';
        return ca; 
    }

    public String regularStringReturnTest() { return rs; }

    public String specialStringReturnTest() { return ss; }

    public void voidReturnTest() { }

    public Object nullReturnTest() { return null; }

    public Integer IntegerReturnTest() { return I; }

    public Double DoubleReturnTest() { return D; }
    public void DoubleSetTest(double set) { D = set; }

    public Float FloatReturnTest() { return F; }

    public Long LongReturnTest() { return L; }

    public Boolean BooleanReturnTest() { return B; }

    public Character CharacterReturnTest() { return C; }

    public Byte ByteReturnTest() { return By; }

    public Character[] CharacterArrayReturnTest() { 
        Ca[0] = '\u2350';
        Ca[1] = '\u2351';
        Ca[2] = '\u2352';
        return Ca; 
    }

    /*
     **************************************
     * JS -> Java parameter passing tests *
     **************************************
    */

    public void setUpForParameterTests() {
        i = 41;
        d = 41.41;
        f = 41.411F;
        l = 4294967297L;
        b = true;
        c = '\u2329';
        by = 44;
        rs = "I'm a string too!";
        ss = "𠁎〒£$ǣ€𝍖";
        n = null;

        I = 14;
        D = 14.14;
        F = 14.114F;
        L = 6927694925L;
        B = false;
        C = '\u2417';
        By = 46;
    }

    public String functioniParamTest(int i) {
       String ret = Integer.toString(i);
       return ret;
    }

    public String functiondParamTest(double d) {
        String ret = Double.toString(d);
        return ret;
     }

    public String functionfParamTest(float f) {
        String ret = Float.toString(f);
        return ret;
     }

    public String functionlParamTest(long l) {
        String ret = Long.toString(l);
        return ret;
    }
    
    public String functionbParamTest(boolean b) {
        String ret = Boolean.toString(b);
        return ret;
     }
    
    public String functioncParamTest(char c) {
        String ret = Character.toString(c);
        return ret;
     }
    
    public String functionbyParamTest(byte b) {
        String ret = Byte.toString(b);
        return ret;
     }
    
     public String functioncaParamTest(char[] ca) {
    
        String ret = "";
        ret += ca[0];
        for (int i=1 ; i < ca.length; i++) {
            ret += ":" + ca[i];
        }

        return ret;
    }
    
    public String functionsiaParamTest(String[] s) {
    
        String ret = s[0];
        for (int i=1 ; i < s.length; i++) {
            ret += ":" + s[i];
        }

        return ret;
    }

    public String functionsParamTest(String s) {
        return s + ":" + s.getClass().getName();
    }
    
    public String functionIParamTest(Integer p) {
        String ret = p.toString() + ":" + p.getClass().getName();
        return ret;
    }
    
    public String functionDParamTest(Double p) {
        String ret = p.toString() + ":" + p.getClass().getName();
        return ret;
    }
    
    public String functionFParamTest(Float p) {
        String ret = p.toString() + ":" + p.getClass().getName();
        return ret;
    }
    
    public String functionLParamTest(Long p) {
        String ret = p.toString() + ":" + p.getClass().getName();
        return ret;
    }
    
    public String functionBParamTest(Boolean p) {
        String ret = p.toString() + ":" + p.getClass().getName();
        return ret;
    }
    
    public String functionCParamTest(Character p) {
        String ret = p.toString() + ":" + p.getClass().getName();
        return ret;
    }
    
    public String functionBParamTest(Byte p) {
        String ret = p.toString() + ":" + p.getClass().getName();
        return ret;
    }
    
    public String functionCaParamTest(Character p) {
        String ret = p.toString() + ":" + p.getClass().getName();
        return ret;
    }
    
    public String functioncomplexaParamTest(DummyObject[] ca) {
        String ret = ca[0].toString();
        for (int i=1 ; i < ca.length; i++) {
            ret += ":" + ca[i].toString();
        }

        return ret;
    }

    /*
     ***********************************************
     * JS -> Java overload resolution plugin tests *
     ***********************************************
    */

    /* Numeric type to the analogous Java primitive type */
    
    public String foo_num_to_num(int p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":int"; }
    
    // int -> int is lower than:
    // int to double
    public String foo_num_to_num(long p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":long"; }
    // int to String
    public String foo_num_to_num(String p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }


    /* Null to any non-primitive type */
    public String foo_null_to_nonprim(Integer p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":Integer"; }
    
    // Null to non-prim is better than:
    // null -> prim (not allowed)
    public String foo_null_to_nonprim(int p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":int"; }


    /* JSObject to JSObject */
    public String foo_jso_to_jso(JSObject p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":JSObject"; }

    // JSO -> JSO is better than:
    // JSO -> String
    public String foo_jso_to_jso(String p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }
    // JSO -> Java array
    public String foo_jso_to_jso(String[] p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }
    // JSO -> Superclass (Object)
    public String foo_jso_to_jso(Object p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }


    /* Class type to Class type where the types are equal */
    public String foo_ct_to_ct(OverloadTestHelper2 p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }
    
    // CT -> CT is better than:
    // CT -> Superclass
    public String foo_ct_to_ct(OverloadTestHelper1 p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }
    // CT->Subclass
    public String foo_ct_to_ct(OverloadTestHelper3 p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }



    /* Numeric type to a different primitive type */
    public String foo_multiprim(double p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":double"; }
    
    // Num -> Diff. prim. is better than:
    // Better than anything else.. using string as a dummy
    public String foo_multiprim(String p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }



    /* String to numeric */
    public String foo_strnum(double p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":double"; }
    
    // Str -> Num is better than:
    // Anything else .. using OverloadTestHelper1 as a dummy
    public String foo_strnum(OverloadTestHelper1 p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }
    

    /* Class type to superclass type (with subclass passed) */
    public String foo_ct_to_sc(OverloadTestHelper1 p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":OverloadTestHelper1"; }

    // CT -> Superclass is better than CT to String
    public String foo_ct_to_sc(String p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }
    
    

    /* Any Java value to String */
    public String foo_jv_to_str(String p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }
    
    // JV -> Str is better than anything else allowed
    public String foo_jv_to_str(JSObject p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }
    
    
    
    /* JSO to Array (lower cost) */
    public String foo_jso_to_array(int[] p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":int[]"; }

    // JSO to array is better than:
    // something not possible
    public String foo_jso_to_array(Integer p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }
    
    
    /****** Not allowed resolutions *******/
    
    /* null to primitive */
    public String foo_null_to_prim(int p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":int"; }

    /* JSObject to something else */
    public String foo_jso_to_somethingelse(OverloadTestHelper1 p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }

    /* Any other conversion not described ... e.g. sending non-array to array */
    public String foo_unsupported(Object[] p) { return (new Throwable()).getStackTrace()[0].getMethodName() + ":" + p.getClass().getName(); }

    /*
     ******************************
     * JS -> Java type conversion *
     ******************************
    */

    public byte byte_type = 0;
    public char char_type = 'A';
    public short short_type = 0;
    public int int_type = 0;
    public long long_type = 0L;
    public float float_type = 0F;
    public double double_type = 0.0;
    public boolean boolean_type = false;
    
    public byte[] byte_array = null;
    public char[] char_array = null;
    public short[] short_array = null;
    public int[] int_array = null;
    public long[] long_array = null;
    public float[] float_array = null;
    public double[] double_array = null;
    public char[][] char_array_array = null;

    public Byte Byte_type = null;    
    public Character Character_type = 'A';
    public Short Short_type = 0;
    public Integer Integer_type = 0;
    public Long Long_type = 0L;
    public Float Float_type = 0F;
    public Double Double_type = 0.0;
    public String String_type = "";
    public Boolean Boolean_type = false;
    public JSObject JSObject_type = null;

    public Byte[] Byte_array = null;    
    public Character[] Character_array = null;
    public Short[] Short_array = null;
    public Integer[] Integer_array = null;
    public Long[] Long_array = null;
    public Float[] Float_array = null;
    public Double[] Double_array = null;
    public String[] String_array = null;
    public String[][] String_array_array = null;

    public Object Object_type = null;

    public String getArrayAsStr(Object array) {
        int size = Array.getLength(array);
        
        String ret = "";
        for (int i=0; i < size; i++) {
            ret += Array.get(array, i) == null ? "null" : Array.get(array, i).toString();
            ret += ",";
        }

        if (ret.length() > 0) {
            ret = ret.substring(0, ret.length()-1);
        }
        
        return ret;
    }

    /*
    **************************
    **************************
    * Begin Java -> JS tests *
    **************************
    **************************
    */

    public DummyObject dummyObject = new DummyObject("DummyObject1");
    public Object value;
    private JSObject window;

    /*
    *************************
    * Java -> JS read tests *
    *************************
    */

    public boolean jjsReadIntTest() {
        value = new Integer(window.getMember("intvar").toString());
        return ((Integer) value).equals(1);
    }

    public boolean jjsReadDoubleTest() {
        value = new Double(window.getMember("doublevar").toString());
        return ((Double) value).equals(1.1);
    }

    public boolean jjsReadBooleanTest() {
        value = new Boolean(window.getMember("boolvar").toString());
        return ((Boolean) value).equals(true);
    }

    public boolean jjsReadStringTest() {
        value = window.getMember("stringvar").toString();
        return ((String) value).equals("stringvar");
    }

    public boolean jjsReadObjectTest() {
        value = window.getMember("objectvar").toString();
        return value.equals("DummyObject1");
    }
    
    public boolean jjsRead1DArrayTest() {
        value = ((JSObject) window.getMember("arrayvar")).getSlot(1);
        return value.toString().equals("100");
    }

    public boolean jjsRead2DArrayTest() {
        value = ((JSObject) ((JSObject) window.getMember("arrayvar2")).getSlot(1)).getSlot(2);
        return value.toString().equals("200");
    }

    /*
    **************************
    * Java -> JS write tests *
    **************************
    */

    public void jjsSetIntTest() {
        window.setMember("setvar", (int) 1);
    }

    public void jjsSetIntegerTest() {
        window.setMember("setvar", new Integer(2));
    }

    public void jjsSetdoubleTest() {
        window.setMember("setvar", (double) 2.1);
    }

    public void jjsSetDoubleTest() {
        window.setMember("setvar", new Double(2.2));
    }

    public void jjsSetfloatTest() {
        window.setMember("setvar", (float) 2.3);
    }

    public void jjsSetFloatTest() {
        window.setMember("setvar", new Float(2.4));
    }

    public void jjsSetshortTest() {
        window.setMember("setvar", (short) 3);
    }

    public void jjsSetShortTest() {
        window.setMember("setvar", new Short((short) 4));
    }

    public void jjsSetlongTest() {
        window.setMember("setvar", (long) 4294967296L);
    }

    public void jjsSetLongTest() {
        window.setMember("setvar", new Long(4294967297L));
    }

    public void jjsSetbyteTest() {
        window.setMember("setvar", (byte) 5);
    }

    public void jjsSetByteTest() {
        window.setMember("setvar", new Byte((byte) 6));
    }

    public void jjsSetcharTest() {
        window.setMember("setvar", (char) '\u2323');
    }

    public void jjsSetCharacterTest() {
        window.setMember("setvar", new Character('\u2324'));
    }

    public void jjsSetbooleanTest() {
        window.setMember("setvar", (boolean) true);
    }

    public void jjsSetBooleanTest() {
        window.setMember("setvar", new Boolean(false));
    }

    public void jjsSetStringTest() {
        window.setMember("setvar", "𠁎〒£$ǣ€𝍖");
    }

    public void jjsSetObjectTest() {
        dummyObject = new DummyObject("DummyObject2");
        window.setMember("setvar", dummyObject);
    }
    
    public void jjsSet1DArrayTest() {
        ((JSObject) window.getMember("setvar")).setSlot(1, 100);
    }

    public void jjsSet2DArrayTest() {
        ((JSObject) ((JSObject) window.getMember("setvar")).getSlot(1)).setSlot(2, 200);
    }

    /*
    ****************************************
    * Java -> JS call parameter conversion *
    ****************************************
    */

    public String jjsCallParamTest(String type) {

        Object ret = new Object();
        
        int i = 1;
        double d = 1.1;
        float f = 1.2F;
        long l = 4294967296L;
        short s = 2;
        byte b = 3;
        char c = '\u2323';
        boolean bl = true;
        Integer I = 4;
        Double D = 4.1;
        Float F = 4.2F;
        Long L = 4294967297L;
        Short S = 5;
        Byte B = 6;
        Boolean Bl = false;
        Character C = '\u2324';
        String str = "𠁎〒£$ǣ€𝍖";
        Object o = new DummyObject("d1");
        
        String  callParamTestFuncName = "JJSParameterTypeCallTest";
        
        if (type.equals("int"))
            ret = window.call(callParamTestFuncName, new Object[]{i});
        else if (type.equals("double"))
            ret = window.call(callParamTestFuncName, new Object[]{d});
        else if (type.equals("float"))
            ret = window.call(callParamTestFuncName, new Object[]{f});
        else if (type.equals("long"))
            ret = window.call(callParamTestFuncName, new Object[]{l});
        else if (type.equals("short"))
            ret = window.call(callParamTestFuncName, new Object[]{s});
        else if (type.equals("byte"))
            ret = window.call(callParamTestFuncName, new Object[]{b});
        else if (type.equals("char"))
            ret = window.call(callParamTestFuncName, new Object[]{c});
        else if (type.equals("boolean"))
            ret = window.call(callParamTestFuncName, new Object[]{bl});
        else if (type.equals("java.lang.Integer"))
            ret = window.call(callParamTestFuncName, new Object[]{I});
        else if (type.equals("java.lang.Double"))
            ret = window.call(callParamTestFuncName, new Object[]{D});
        else if (type.equals("java.lang.Float"))
            ret = window.call(callParamTestFuncName, new Object[]{F});
        else if (type.equals("java.lang.Long"))
            ret = window.call(callParamTestFuncName, new Object[]{L});
        else if (type.equals("java.lang.Short"))
            ret = window.call(callParamTestFuncName, new Object[]{S});
        else if (type.equals("java.lang.Byte"))
            ret = window.call(callParamTestFuncName, new Object[]{B});
        else if (type.equals("java.lang.Boolean"))
            ret = window.call(callParamTestFuncName, new Object[]{Bl});
        else if (type.equals("java.lang.Character"))
            ret = window.call(callParamTestFuncName, new Object[]{C});
        else if (type.equals("java.lang.String"))
            ret = window.call(callParamTestFuncName, new Object[]{str});
        else if (type.equals("PluginTest.Packages.DummyObject"))
            ret = window.call(callParamTestFuncName, new Object[]{o});
        else
            ret = "Unknown param type: " + type;

        return ret.toString();
    }
    
    /*
    *******************************************
    * Java -> JS invocation return type tests *
    *******************************************
    */

    public String jjsReturnTypeTest(String type) {
    
        String  returnTypeTestFuncName = "JJSReturnTypeCallTest";
        Object ret = window.call(returnTypeTestFuncName, new Object[]{type});

        return ret.toString();        
    }


    /*
    ***********************************
    * Java -> JS invocation eval test *
    ***********************************
    */

    public String jjsEvalTest(String str) {
        return window.eval(str).toString();
    }

    public void init() {
        window = JSObject.getWindow(this);
        //JSObject.getWindow(this).call("appletLoaded", new Object[]{});
    }

}

