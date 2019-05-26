/* JavaConsole -- A java console for the plugin
Copyright (C) 2009, 2013  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */
package net.adoptopenjdk.icedteaweb.client.console;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.ImageResources;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.logging.TeeOutputStream;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;
import net.sourceforge.jnlp.util.logging.headers.ObservableMessagesProvider;
import net.sourceforge.jnlp.util.logging.headers.PluginMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * A simple Java console for IcedTeaPlugin and JavaWS
 */
public class JavaConsole implements ObservableMessagesProvider {

    private final static Logger LOG = LoggerFactory.getLogger(JavaConsole.class);

    final private List<MessageWithHeader> rawData = Collections.synchronizedList(new ArrayList<MessageWithHeader>());
    final private List<ConsoleOutputPane> outputs = new ArrayList<ConsoleOutputPane>();

    public JavaConsole() {
        //add middleware, which catches client's application stdout/err
        //and will submit it into console
        System.setErr(new TeeOutputStream(System.err, true));
        System.setOut(new TeeOutputStream(System.out, false));
        //internal stdOut/Err are going throughs outLog/errLog
        //when console is off, those tees are not installed

        // initialize SwingUtils
        updateModel();
    }

    private void refreshOutputs() {
        refreshOutputs(outputsPanel, (Integer) numberOfOutputs.getValue());
    }

    private void refreshOutputs(JPanel pane, int count) {
        pane.removeAll();
        while (outputs.size() > count) {
            getObservable().deleteObserver(outputs.get(outputs.size() - 1));
            outputs.remove(outputs.size() - 1);
        }
        while (outputs.size() < count) {
            ConsoleOutputPane c1 = new ConsoleOutputPane(this);
            observable.addObserver(c1);
            outputs.add(c1);
        }
        if (outputs.size() == 1) {
            pane.add(outputs.get(0));
        } else {
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, outputs.get(outputs.size() - 2), outputs.get(outputs.size() - 1));
            splitPane.setDividerLocation(0.5);
            splitPane.setResizeWeight(0.5);

            for (int i = outputs.size() - 3; i >= 0; i--) {
                JSplitPane outerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, outputs.get(i), splitPane);
                outerPane.setDividerLocation(0.5);
                outerPane.setResizeWeight(0.5);
                splitPane = outerPane;
            }
            pane.add(splitPane);

        }
        pane.validate();
    }

    private static class PublicObservable extends Observable {

        @Override
        public synchronized void setChanged() {
            super.setChanged();
        }
    }

    public static interface ClassLoaderInfoProvider {

        public Map<String, String> getLoaderInfo();
    }

    private static JavaConsole console;

    private Dimension lastSize;
    private JDialog consoleWindow;
    private JPanel contentPanel;
    private JPanel outputsPanel;
    private ClassLoaderInfoProvider classLoaderInfoProvider;
    private JSpinner numberOfOutputs;
    private PublicObservable observable = new PublicObservable();
    private boolean initialized = false;

    private static class JavaConsoleHolder {

        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static final JavaConsole INSTANCE = new JavaConsole();
    }

    public static JavaConsole getConsole() {
        return JavaConsoleHolder.INSTANCE;
    }

    public static boolean isEnabled() {
        return isEnabled(JNLPRuntime.getConfiguration());
    }

    public static boolean isEnabled(DeploymentConfiguration config) {
        return !ConfigurationConstants.CONSOLE_DISABLE.equals(config.getProperty(ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE))
                && !JNLPRuntime.isHeadless();
    }

    public static boolean canShowOnStartup(boolean isApplication) {
        return canShowOnStartup(isApplication, JNLPRuntime.getConfiguration());
    }

    public static boolean canShowOnStartup(boolean isApplication, DeploymentConfiguration config) {
        if (!isEnabled(config)) {
            return false;
        }
        return ConfigurationConstants.CONSOLE_SHOW.equals(config.getProperty(ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE))
                || (ConfigurationConstants.CONSOLE_SHOW_PLUGIN.equals(config.getProperty(ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE))
                && !isApplication)
                || (ConfigurationConstants.CONSOLE_SHOW_JAVAWS.equals(config.getProperty(ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE))
                && isApplication);
    }

    private void initializeWindow() {
        if (!initialized) {
            initialize();
        }
        if (!JNLPRuntime.isHeadless()) {
            initializeWindow(lastSize, contentPanel);
        }
    }

    private void initializeWindow(Dimension size, JPanel content) {
        consoleWindow = new JDialog((JFrame) null, R("DPJavaConsole"));
        consoleWindow.setName("JavaConsole");
        SwingUtils.info(consoleWindow);

        consoleWindow.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                lastSize = consoleWindow.getSize();
            }

        });
        consoleWindow.setIconImages(ImageResources.INSTANCE.getApplicationImages());
        //view is added after console is made visible so no performance impact when hidden/
        refreshOutputs();
        consoleWindow.add(content);
        consoleWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //HIDE_ON_CLOSE can cause shut down deadlock
        consoleWindow.pack();
        if (size != null) {
            consoleWindow.setSize(size);
        } else {
            consoleWindow.setSize(new Dimension(900, 600));
        }
        consoleWindow.setMinimumSize(new Dimension(300, 300));

    }

    /**
     * Initialize the console
     */
    private void initialize() {

        contentPanel = new JPanel();
        outputsPanel = new JPanel();

        outputsPanel.setLayout(new BorderLayout());
        contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints c;
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 10;
        c.weighty = 1;

        contentPanel.add(outputsPanel, c);

        /* buttons */
        c = new GridBagConstraints();
        c.gridy = 10;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0;

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 0, 10, 10));
        contentPanel.add(buttonPanel, c);

        JButton gcButton = new JButton(R("CONSOLErungc"));
        buttonPanel.add(gcButton);
        gcButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printMemoryInfo();
                LOG.info("Performing Garbage Collection....");
                System.gc();
                LOG.info("ButDone");
                printMemoryInfo();
                updateModel();
            }
        });

        JButton finalizersButton = new JButton(R("CONSOLErunFinalizers"));
        buttonPanel.add(finalizersButton);
        finalizersButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printMemoryInfo();
                LOG.info("CONSOLErunningFinalizers");
                Runtime.getRuntime().runFinalization();
                LOG.info("ButDone");
                printMemoryInfo();
                updateModel();
            }
        });

        JButton memoryButton = new JButton(R("CONSOLEmemoryInfo"));
        buttonPanel.add(memoryButton);
        memoryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printMemoryInfo();
                updateModel();
            }
        });

        JButton systemPropertiesButton = new JButton(R("CONSOLEsystemProperties"));
        buttonPanel.add(systemPropertiesButton);
        systemPropertiesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printSystemProperties();
                updateModel();
            }
        });

        JButton classloadersButton = new JButton(R("CONSOLEclassLoaders"));
        buttonPanel.add(classloadersButton);
        classloadersButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printClassLoaders();
                updateModel();
            }
        });

        JButton threadListButton = new JButton(R("CONSOLEthreadList"));
        buttonPanel.add(threadListButton);
        threadListButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printThreadInfo();
                updateModel();
            }
        });

        JLabel numberOfOutputsL = new JLabel("  Number of outputs: ");
        buttonPanel.add(numberOfOutputsL);
        numberOfOutputs = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JComponent comp = numberOfOutputs.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        numberOfOutputs.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                refreshOutputs();
            }
        });
        buttonPanel.add(numberOfOutputs);

        JButton closeButton = new JButton(R("ButClose"));
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtils.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        hideConsole();
                    }
                });
            }
        });

        JButton cleanButton = new JButton(R("CONSOLEClean"));
        buttonPanel.add(cleanButton);
        cleanButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (rawData) {
                    rawData.clear();
                    updateModel(true);
                }
            }
        });

        initialized = true;
    }

    public void showConsole() {
        showConsole(false);
    }

    public void showConsole(boolean modal) {
        if (!JNLPRuntime.isHeadless()) {
            if (consoleWindow == null || !consoleWindow.isVisible()) {
                initializeWindow();
                consoleWindow.setModal(modal);
                consoleWindow.setVisible(true);
            }
        }
    }

    public void hideConsole() {
        //no need to update when hidden
        outputsPanel.removeAll();//??
        getObservable().deleteObservers();
        consoleWindow.setModal(false);
        consoleWindow.setVisible(false);
        consoleWindow.dispose();
    }

    public void showConsoleLater() {
        showConsoleLater(false);
    }

    public void showConsoleLater(final boolean modal) {
        SwingUtils.invokeLater(new Runnable() {

            @Override
            public void run() {
                JavaConsole.getConsole().showConsole(modal);
            }
        });
    }

    public void hideConsoleLater() {
        SwingUtils.invokeLater(new Runnable() {

            @Override
            public void run() {
                JavaConsole.getConsole().hideConsole();
            }
        });
    }

    protected void printSystemProperties() {

        LOG.info(" ----");
        LOG.info("CONSOLEsystemProperties" + ":");
        LOG.info("");
        Properties p = System.getProperties();
        Set<Object> keys = p.keySet();
        for (Object key : keys) {
            LOG.info(key.toString() + ": " + p.get(key));
        }
        LOG.info(" ----");
    }

    public void setClassLoaderInfoProvider(ClassLoaderInfoProvider clip) {
        classLoaderInfoProvider = clip;
    }

    private void printClassLoaders() {
        if (classLoaderInfoProvider == null) {
            LOG.debug("CONSOLEnoClassLoaders");
        } else {
            LOG.debug(" ----");
            LOG.debug("CONSOLEclassLoaders" + ": ");
            Set<String> loaders = classLoaderInfoProvider.getLoaderInfo().keySet();
            for (String loader : loaders) {
                LOG.debug(loader + "\n"
                        + "  codebase = "
                        + classLoaderInfoProvider.getLoaderInfo().get(loader));
            }
            LOG.debug(" ----");
        }
    }

    private void printMemoryInfo() {
        LOG.info(" ----- ");
        LOG.info("  " + "CONSOLEmemoryInfo" + ":");
        LOG.info("   " + "CONSOLEmemoryMax" + ":   " + String.format("%1$10d", Runtime.getRuntime().maxMemory()));
        LOG.info("    " + "CONSOLEmemoryTotal" + ": " + String.format("%1$10d", Runtime.getRuntime().totalMemory()));
        LOG.info("    " + "CONSOLEmemoryFree" + ":  " + String.format("%1$10d", Runtime.getRuntime().freeMemory()));
        LOG.info(" ----");

    }

    private void printThreadInfo() {
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        Set<Thread> keys = map.keySet();
        for (Thread key : keys) {
            LOG.info("CONSOLEthread" + " " + key.getId() + ": " + key.getName());
            for (StackTraceElement element : map.get(key)) {
                LOG.info("  " + element);
            }

        }
    }

    public static void main(String[] args) {

        final JavaConsole cconsole = new JavaConsole();

        boolean toShowConsole = true;

        for (String arg : args) {
            if ("--show-console".equals(arg)) {
                toShowConsole = true;
            }
        }

        if (toShowConsole) {
            cconsole.showConsoleLater();
        }

    }

    public synchronized void addMessage(MessageWithHeader m) {
        rawData.add(m);
        updateModel();
    }

    private synchronized void updateModel() {
        updateModel(null);
    }

    private synchronized void updateModel(final Boolean force) {
        observable.setChanged();

        SwingUtils.invokeLater(new Runnable() {

            @Override
            public void run() {
                // avoid too much processing if already processed:
                synchronized (observable) {
                    if (observable.hasChanged() || (Boolean.TRUE.equals(force))) {
                        observable.notifyObservers(force);
                    }
                }
            }
        });
    }

    /**
     * parse plugin message and add it as header+message to data
     *
     * @param s string to be parsed
     */
    private void processPluginMessage(String s) {
        PluginMessage pm = new PluginMessage(s);
        OutputController.getLogger().log(pm);
    }

    @Override
    public List<MessageWithHeader> getData() {
        return rawData;
    }

    @Override
    public Observable getObservable() {
        return observable;
    }

    public void createPluginReader(final File file) {
        LOG.debug("Starting processing of plugin-debug-to-console {}", file.getAbsolutePath());
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8));
                    //never ending loop
                    while (true) {
                        try {
                            String s = br.readLine();
                            if (s == null) {
                                break;
                            }
                            processPluginMessage(s);
                        } catch (Exception ex) {
                            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                        }
                    }
                } catch (Exception ex) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception exx) {
                            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, exx);
                        }
                    }
                }
                LOG.debug("Ended processing of plugin-debug-to-console {}", file.getAbsolutePath());
            }
        }, "plugin-debug-to-console reader thread");
        t.setDaemon(true);
        t.start();

        LOG.debug("Started processing of plugin-debug-to-console {}", file.getAbsolutePath());
    }
}
