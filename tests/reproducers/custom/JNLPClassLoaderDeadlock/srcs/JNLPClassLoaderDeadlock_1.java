import java.applet.Applet;
import java.awt.*;

public class JNLPClassLoaderDeadlock_1 extends Applet {

    @Override
    public void init() {
        System.out.println("JNLPClassLoaderDeadlock_1 applet initialized");
        final String version = System.getProperty("java.version") + " (" + System.getProperty("java.vm.version") + ")";
        final String vendor = System.getProperty("java.vendor");
        final TextField tf = new TextField(40);
        tf.setText(version + " -- " + vendor);
        tf.setEditable(false);
        tf.setBackground(Color.white);
        setBackground(Color.white);
        add(tf);
        System.out.println("JNLPClassLoaderDeadlock_1 applet finished");
    }

    public static void main(String[] args) {
        new JNLPClassLoaderDeadlock_1().init();
    }
}
