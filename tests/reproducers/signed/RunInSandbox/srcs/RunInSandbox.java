import java.applet.Applet;

public class RunInSandbox extends Applet {

    @Override
    public void start() {
        System.out.println("RunInSandbox read: " + read("user.home"));
        System.out.println("*** APPLET FINISHED ***");
    }

    public static void main(String[] args) {
        new RunInSandbox().start();
        System.exit(0);
    }

    private String read(String key) {
        try {
            return System.getProperty(key);
        } catch (Exception e) {
            return e.toString();
        }
    }
}
