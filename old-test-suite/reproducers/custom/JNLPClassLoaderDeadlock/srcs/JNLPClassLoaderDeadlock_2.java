import java.applet.Applet;
import java.awt.*;

import java.security.*;
import java.util.PropertyPermission;

public class JNLPClassLoaderDeadlock_2 extends Applet implements Runnable {

    private static final String propertyNames[] = {
        "java.version",
        "java.vendor",
        "java.vendor.url",
        "java.home",
        "java.vm.specification.version",
        "java.vm.specification.vendor",
        "java.vm.specification.name",
        "java.vm.version",
        "java.vm.name",
        "java.vm.home",
        "java.specification.version",
        "java.specification.vendor",
        "java.specification.name",
        "java.class.version",
        "java.class.path",
        "os.name",
        "os.arch",
        "os.version",
        "file.separator",
        "path.separator",
        "line.separator",
        "user.home",
        "user.name",
        "user.dir",
    };

    private Label[] propertyValues;

    @Override
    public void init() {
        System.out.println("JNLPClassLoaderDeadlock_2 applet initialized");
        GridBagLayout gridbaglayout = new GridBagLayout();
        setLayout(gridbaglayout);

        GridBagConstraints leftColumn = new GridBagConstraints();
        leftColumn.anchor = 20;
        leftColumn.ipadx = 16;

        GridBagConstraints rightColumn = new GridBagConstraints();
        rightColumn.fill = 2;
        rightColumn.gridwidth = 0;
        rightColumn.weightx = 1.0D;

        Label labels[] = new Label[propertyNames.length];
        propertyValues = new Label[propertyNames.length];
        final String preloadText = "...";

        for (int i = 0; i < propertyNames.length; ++i) {
            labels[i] = new Label(propertyNames[i]);
            gridbaglayout.setConstraints(labels[i], leftColumn);
            add(labels[i]);

            propertyValues[i] = new Label(preloadText);
            gridbaglayout.setConstraints(propertyValues[i], rightColumn);
            add(propertyValues[i]);
        }

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        for (int i = 0; i < propertyNames.length; ++i) {
            try {
                final String propertyValue = System.getProperty(propertyNames[i]);
                propertyValues[i].setText(propertyValue);
            } catch (SecurityException securityexception) {
            }
        }
        System.out.println("JNLPClassLoaderDeadlock_2 applet finished");
    }
}
