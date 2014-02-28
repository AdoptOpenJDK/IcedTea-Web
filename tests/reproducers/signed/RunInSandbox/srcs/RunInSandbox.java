import java.applet.Applet;

public class RunInSandbox extends Applet {

    @Override
    public void start() {
        System.out.println("RunInSandbox read: " + read("user.home"));
        System.out.println("*** APPLET FINISHED ***");
        System.exit(0);
    }

    public static void main(String[] args) {
        new RunInSandbox().start();
    }

    private String read(String key) {
        try {
            return System.getProperty(key);
        } catch (Exception e) {
            return e.toString();
        }
    }
}
