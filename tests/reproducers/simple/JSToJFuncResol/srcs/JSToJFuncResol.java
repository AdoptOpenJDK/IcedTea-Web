import java.applet.Applet;
import netscape.javascript.JSObject;

public class JSToJFuncResol extends Applet {

    /****** Primitive (numeric) value resolutions ******/

    /*
     * Javascript primitive numeric (int) value resolutions: - to an analogous
     * primitive Java type (best - lowest cost) - to another primitive numeric
     * Java type (long) (second lowest) - to Java String type (third lowest)
     */

    public void numeric(int p) {
        System.out.println("numeric(int) with " + p);
    }

    public void numeric(long p) {
        System.out.println("numeric(long) with " + p);
    }

    public void numeric(String p) {
        System.out.println("numeric(String) with " + p);
    }

    /*
     * Javascript primitive numeric (int) value resolutions: - to a different
     * primitive Java numeric type (double) (best - second lowest cost) - to
     * Java string (third lowest cost)
     */

    public void numericToDifferentNumeric(double p) {
        System.out.println("numericToDifferentNumeric(double) with " + p);
    }

    public void numericToDifferentNumeric(String p) {
        System.out.println("numericToDifferentNumeric(String) with " + p);
    }

    /*
     * Javascript primitive numeric (floating point) value resolutions: - to a
     * primitive Java numeric type (double) (best - lowest cost) - to Java char
     */

    public void numericToDouble(double p) {
        System.out.println("numericToDouble(double) with " + p);
    }

    public void numericToDouble(char p) {
        System.out.println("numericToDouble(char) with " + p);
    }

    /****** Null resolutions ******/

    /*
     * Javascript null value resolutions: - to any nonprimitive Java type (e.g.
     * Integer) (best) - to a primitive Java type (int) (not allowed)
     */

    public void nullToInteger(Integer p) {

        System.out.println("nullToInteger(Integer) with " + p);
    }

    public void nullToInteger(int p) {
        System.out.println("nullToInteger(int) with " + p);
    }

    /****** Java inherited class resolutions ******/

    /*
     * Java inherited class (OverloadTestHelper2) value resolutions: - to the
     * same class type (OverloadTestHelper2) (best) - to a superclass
     * (OverloadTestHelper1) (second best) - to a subclass (OverloadTestHelper3)
     * (not possible)
     */

    public void inheritedClass(OverloadTestHelper2 p) {
        System.out.println("inheritedClass(OverloadTestHelper2) with " + p);
    }

    public void inheritedClass(OverloadTestHelper1 p) {
        System.out.println("inheritedClass(OverloadTestHelper1) with " + p);
    }

    public void inheritedClass(OverloadTestHelper3 p) {
        System.out.println("inheritedClass(OverloadTestHelper3) with " + p);
    }

    /*
     * Java inherited class (OverloadTestHelper3) value resolutions: - to a
     * superclass (OverloadTestHelper2) (best - second lowest cost) - to a
     * superclass of superclass (OverloadTestHelper1) (higher cost)
     */

    public void inheritedClassToParent1(OverloadTestHelper2 p) {
        System.out.println("inheritedClassToParent1(OverloadTestHelper2) with " + p);
    }

    public void inheritedClassToParent1(OverloadTestHelper1 p) {
        System.out.println("inheritedClassToParent1(OverloadTestHelper1) with " + p);
    }

    /*
     * Java inherited class (OverloadTestHelper2) resolutions: - to the
     * superclass (OverloadTestHelper1) (best - second lowest cost) - to Java
     * String (third lowest cost)
     */

    public void inheritedClassToParent2(OverloadTestHelper1 p) {
        System.out.println("inheritedClassToParent2(OverloadTestHelper1) with " + p);
    }

    public void inheritedClassToParent2(String p) {
        System.out.println("inheritedClassToParent2(String) with " + p);
    }

    /****** Java object resolutions ******/

    /*
     * Java object (OverloadTestHelper1) value resolutions: - to Java String
     * (best - third lowest cost) - to a different nonprimitive Java class
     * (JSObject) (not possible)
     */

    public void javaObjectToString(String p) {
        System.out.println("javaObjectToString(String) with " + p);
    }

    public void javaObjectToString(JSObject p) {
        System.out.println("javaObjectToString(JSObject) with " + p);
    }

    /****** String resolutions ******/

    /*
     * Javascript string value resolutions: - to a primitive numeric Java type
     * (double) (best - second lowest cost) - to a nonprimitive Java class
     * (OverloadTestHelper1 as a dummy)(not possible)
     */

    public void javascriptStringToNumeric(double p) {
        System.out.println("javascriptStringToNumeric(double) with " + p);
    }

    public void javascriptStringToNumeric(OverloadTestHelper1 p) {
        System.out.println("javascriptStringToNumeric(OverloadTestHelper1) with " + p);
    }

    /****** Javascript object resolutions ******/

    /*
     * Javascript object value resolutions: - to JSObject Java type (best -
     * lowest cost) - to Java String type (fourth lowest cost) - to Java array
     * of Strings (fourth lowest cost) - to a Java superclass (Object) (second
     * lowest cost)
     */

    public void javascriptObject(JSObject p) {
        System.out.println("javascriptObject(JSObject) with " + p);
    }

    public void javascriptObject(String p) {
        System.out.println("javascriptObject(String) with " + p);
    }

    public void javascriptObject(String[] p) {
        System.out.println("javascriptObject(String[]) with " + p);
    }

    public void javascriptObject(Object p) {
        System.out.println("javascriptObject(Object) with " + p);
    }

    /*
     * Javascript object (array) value resolutions: - to a Java array of
     * primitive numeric Java type (int[]) (best - fourth lowest cost) - to a
     * nonprimitive Java class Integer (impossible)
     */

    public void javascriptObjectToArray(int[] p) {
        System.out.println("javascriptObjectToArray(int[]) with " + p);
    }

    public void javascriptObjectToArray(Integer p) {
        System.out.println("javascriptObjectToArray(Integer) with " + p);
    }

    /****** Not allowed resolutions *******/

    /*
     * Impossible resolutions all should result in
     * "Error on Java side: No suitable method named ... with matching args found"
     * - null to a primitive numeric Java type (int) - JSObject (window) to a
     * different nonprimitive Java class (OverloadTestHelper1) - non-array value
     * (numeric primitive 25) to array
     */

    public void nullToPrimitive(int p) {
        System.out.println("nullToPrimitive(int) with " + p);
    }

    public void javascriptObjectToUnrelatedType(OverloadTestHelper1 p) {
        System.out.println("javascriptObjectToUnrelatedType(OverloadTesthelper1) with " + p);
    }

    public void unsupported(Object[] p) {
        System.out.println("unsupported(Object[]) with " + p);
    }

    /****** Auxiliary methods and classes ******/

    public void init() {
        String initStr = "JSToJFuncResol applet initialized.";
        System.out.println(initStr);
    }

    public void writeAfterTests() {
        System.out.println("afterTests");
    }

    // dummy classes for passing objects as function parameters
    public class OverloadTestHelper1 {
    };

    public class OverloadTestHelper2 extends OverloadTestHelper1 {
    };

    public class OverloadTestHelper3 extends OverloadTestHelper2 {
    };

    public OverloadTestHelper1 getNewOverloadTestHelper1() {
        return new OverloadTestHelper1();
    }

    public OverloadTestHelper2 getNewOverloadTestHelper2() {
        return new OverloadTestHelper2();
    }

    public OverloadTestHelper3 getNewOverloadTestHelper3() {
        return new OverloadTestHelper3();
    }

}
