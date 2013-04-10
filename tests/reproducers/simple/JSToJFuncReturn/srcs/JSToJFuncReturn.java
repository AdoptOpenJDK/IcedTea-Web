import java.applet.Applet;
import java.awt.Label;
import java.awt.BorderLayout;
import netscape.javascript.JSObject;

public class JSToJFuncReturn extends Applet {

    private Label statusLabel;

    public int _int() {
        int i = 1;
        return i;
    }

    public double _double() {
        double d = 1.1;
        return d;
    }

    public float _float() {
        float f = 1.1F;
        return f;
    }
    
    public long _long() {
        long l = 10000L;
        return l;
    }
    
    public boolean _boolean() {
        boolean b = true;
        return b;
    }

    public char _char() {
        char c = 'a';
        return c;
    }

    public byte _byte() {
        byte by = 10; 
        return by;
    }

    public char _charArrayElement(){
        char[] ca = new char[]{'a', 'b', 'c'};

        return ca[0];
    }

    public char[] _charArray() {
        char[] ca = new char[]{'a', 'b', 'c'};

        return ca;
    }

    public String _regularString() {
        String rs = "test";
        return rs;
    }

    public String _specialString() {
        String ss = "𠁎〒£$ǣ€𝍖";
        return ss;
    }

    public void _void() {
    }

    public Object _null() {
        return null;
    }

    public Integer _Integer() {
        Integer I = 1;
        return I;
    }

    public Double _Double() {
        Double D = 1.1;
        return D;
    }

    public Float _Float() {
        Float F = 1.1F;
        return F;
    }

    public Long _Long() {
        Long L = 10000L;
        return L;
    }

    public Boolean _Boolean() {
        Boolean B = true;
        return B;
    }

    public Character _CharacterArrayElement(){
        Character[] Ca = new Character[]{'A', 'B', 'C'};

        return Ca[0];
    }

    public Character _Character() {
        Character C = 'A';
        return C;
    }

    public Byte _Byte() {
        Byte By = 10;
        return By;
    }

    public Character[] _CharacterArray() {
        Character[] Ca = new Character[]{'A', 'B', 'C'};

        return Ca;
    }

    public JSObject _JSObject(){
        JSObject win = JSObject.getWindow(this);
        JSObject jso = (JSObject) win.getMember("document");
        jso.setMember("key1","value1");
  
        return jso;
    }

    public void init() {
        setLayout(new BorderLayout());
        statusLabel = new Label();
        add(statusLabel);
        String initStr = "JSToJFuncReturn applet initialized.";
        System.out.println(initStr);
        statusLabel.setText(initStr);
    }

    public void printStringAndFinish(String str){
        System.out.println(str);
        System.out.println("afterTests");
    }
}
