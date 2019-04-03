import java.applet.Applet;
import netscape.javascript.JSObject;

public class JSObjectWithoutToString extends Applet {

    public void callJSToString(JSObject jso) {
        System.out.println(jso.toString());
        System.out.println("*** APPLET FINISHED ***");
    }

}
