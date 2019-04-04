import java.applet.Applet;
import java.security.AccessControlException;

public class CustomPolicies extends Applet {

    @Override
    public void start() {
        System.out.println("CustomPolicies applet read: " + read("user.home"));
        System.exit(0);
    }

    private String read(String key) {
        try {
            return System.getProperty(key);
        } catch (AccessControlException ace) {
            return ace.toString();
        }
    }

    public static void main(String[] args) {
        new CustomPolicies().start();
    }
}
