import java.applet.Applet;
import java.net.URL;
import netscape.javascript.JSObject;

public class JavascriptProtocol extends Applet {
    public String state = "HasntRun";
    @Override
    public void start() {
        try {
            getAppletContext().showDocument(new URL("javascript:runSomeJS()"));
            System.out.println("State after showDocument was " + state);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("*** APPLET FINISHED ***");
    }
    // Utility for JS side
    public void print(String s) {
        System.out.println(s);
    }
}

