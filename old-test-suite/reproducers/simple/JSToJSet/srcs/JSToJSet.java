import java.applet.Applet;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import netscape.javascript.JSObject;

public class JSToJSet extends Applet {

    public int _int;
    public double _double;
    public float _float;
    public long _long;
    public boolean _boolean;
    public char _char;
    public byte _byte;
    public String _String;
    public String _specialString;
    public Object _Object = new String("non-null object");
    public int[] _intArray = new int[1];
    public Integer _Integer;
    public Double _Double;
    public Float _Float;
    public Long _Long;
    public Boolean _Boolean;
    public Character _Character = 'B';
    public Byte _Byte;
    public Double[] _DoubleArray = new Double[10];
    public Double[] _DoubleArray2;
    public char[] _charArray = new char[1];
    public Character[] _CharacterArray = new Character[1];
    public JSObject _JSObject;

    public void init() {
        String initStr = "JSToJSet applet initialized.";
        System.out.println(initStr);
    }

    public void printNewValueAndFinish(String fieldname) throws Exception {

        Field field = getClass().getDeclaredField(fieldname);
        Object value = field.get(this);
        System.out.println("Old value is: " + value);

        if (fieldname.equals("_JSObject")) {
            Integer mph = (Integer) _JSObject.getMember("mph");
            String color = (String) _JSObject.getMember("color");
            System.out.println("New value is: " + mph + ", " + color);
        } else if (value != null && value.getClass().isArray()) {
            System.out.println("New array value is: " + Array.get(value, 0));
        } else {
            System.out.println("New value is: " + value);
        }
        System.out.println("afterTests");
    }
}
