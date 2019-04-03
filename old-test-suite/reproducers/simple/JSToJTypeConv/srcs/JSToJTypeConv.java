import java.applet.Applet;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import netscape.javascript.JSObject;

public class JSToJTypeConv extends Applet {

    public byte _byte = 0;
    public char _char = 'A';
    public short _short = 0;
    public int _int = 0;
    public long _long = 0L;
    public float _float = 0F;
    public double _double = 0.0;
    public boolean _boolean = false;

    public byte[] _byteArray = null;
    public char[] _charArray = null;
    public short[] _shortArray = null;
    public int[] _intArray = null;
    public long[] _longArray = null;
    public float[] _floatArray = null;
    public double[] _doubleArray = null;
    public char[][] _charArray2D = null;

    public Byte _Byte = null;
    public Character _Character = 'A';
    public Short _Short = 0;
    public Integer _Integer = 0;
    public Long _Long = 0L;
    public Float _Float = 0F;
    public Double _Double = 0.0;
    public String _String = "";
    public Boolean _Boolean = false;
    public JSObject _JSObject = null;

    public Byte[] _ByteArray = null;
    public Character[] _CharacterArray = null;
    public Short[] _ShortArray = null;
    public Integer[] _IntegerArray = null;
    public Long[] _LongArray = null;
    public Float[] _FloatArray = null;
    public Double[] _DoubleArray = null;
    public String[] _StringArray = null;
    public String[][] _StringArray2D = null;

    public Object _Object = null;


    public String getArrayAsStr(Object array) {
        if( array == null){
            return "null";
        }else{
            int size = Array.getLength(array);

            String ret = "";
            for (int i=0; i < size; i++) {
                ret += ((Array.get(array, i) == null) ? "null" : Array.get(array, i).toString());
                ret += ",";
            }

            if (ret.length() > 0) {
               ret = ret.substring(0, ret.length()-1);
            }

            return "["+ret+"]";
        }
    }

    public void init() {
        String initStr = "JSToJTypeConv applet initialized.";
        System.out.println(initStr);
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

    public void printNewValueAndFinish(String fieldname) throws Exception {
        if( fieldname.equals("_Object")){
            System.out.println( "New value is: " + _Object + " class is " + _Object.getClass().getName() + 
                                " superclass is " + _Object.getClass().getSuperclass().getName() );
        }else{

            Field field = getClass().getDeclaredField(fieldname);
            Object value = field.get(this);

            //2D arrays
            if( fieldname.contains("2D") ){
                Object row1 = Array.get(value,0);
                Object row2 = Array.get(value,1);
                Object row3 = Array.get(value,2);
                System.out.println( "New value is: [" + getArrayAsStr(row1) + "," + getArrayAsStr(row2) + "," + getArrayAsStr(row3) + "]");

            //arrays
            }else if (value != null && value.getClass().isArray()) {
                System.out.println("New value is: " + getArrayAsStr(value));

            //classic fields
            } else {
                System.out.println("New value is: " + value);
            }
        }

        System.out.println("afterTests");
    }
}
