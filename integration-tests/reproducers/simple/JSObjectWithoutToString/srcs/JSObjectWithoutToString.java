import netscape.javascript.JSObject;

import java.applet.Applet;

public class JSObjectWithoutToString extends Applet {

    public void callJSToString(JSObject jso) {
        System.out.println(jso.toString());
        System.out.println("*** APPLET FINISHED ***");
    }

}
