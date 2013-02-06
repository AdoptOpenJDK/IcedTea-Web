import java.applet.Applet;
import java.awt.Label;
import java.awt.BorderLayout;
import netscape.javascript.JSObject;

public class JSToJGet extends Applet {

    public static final int i = 42;
    public static final double d = 42.42;
    public static final float f = 42.1F;
    public static final long l = 4294967296L;
    public static final boolean b = true;
    public static final char c = '\u2323';
    public static final byte by = 43;
    public static final String rs = "I'm a string!";
    public static final String ss = "𠁎〒£$ǣ€𝍖";
    public static final Object n = null;
    public int[] ia = new int[5];

    public static final Integer I = 24;
    public static final Double D = 24.24;
    public static final Float F = 24.124F;
    public static final Long L = 6927694924L;
    public static final Boolean B = false;
    public static final Character C = '\u1526';
    public static final Byte By = 34;
    public Double[] Da1 = new Double[10];
    public Double[] Da2 = null;

    public char[] ca = new char[3];
    public Character[] Ca = new Character[3];
    public JSObject jso;

    private Label statusLabel;

    public void start(){
        JSObject win = JSObject.getWindow(this);
        jso = (JSObject) win.getMember("document");
        jso.setMember("key1","value1");
        ia[4] = 1024;
        Da1[9] = D;

        String setupStr = "JSToJGet applet set up for GET tests.";
        System.out.println(setupStr);
        statusLabel.setText(setupStr);
    }

    public void init() {
        setLayout(new BorderLayout());
        statusLabel = new Label();
        add(statusLabel);
        String initStr = "JSToJGet applet initialized.";
        System.out.println(initStr);
        statusLabel.setText(initStr);
    }

    // auxiliary method for setting the statusLabel text:
    public void setStatusLabel(String s) {
        statusLabel.setText(s);
    }

    // auxiliary methods for writing to stdout and stderr:
    public void stdOutWrite(String s) {
        System.out.print(s);
    }

    public void stdErrWrite(String s) {
        System.err.print(s);
    }

    public void stdOutWriteln(String s) {
        System.out.println(s);
    }

    public void stdErrWriteln(String s) {
        System.err.println(s);
    }
}
