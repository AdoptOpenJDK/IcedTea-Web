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
package net.sourceforge.jnlp.util.logging;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.ImageResources;

/**
 * A simple Java console for IcedTeaPlugin and JavaWS
 * 
 */
public class JavaConsole {

    public static interface ClassLoaderInfoProvider {

        public Map<String, String> getLoaderInfo();
    }
    private static JavaConsole console;
    private static Dimension lastSize;

    public static JavaConsole getConsole() {
        if (console == null) {
            console = new JavaConsole();
        }
        return console;
    }

    public static boolean isEnabled() {
        return isEnabled(JNLPRuntime.getConfiguration());
    }

    public static boolean isEnabled(DeploymentConfiguration config) {
        return !DeploymentConfiguration.CONSOLE_DISABLE.equals(config.getProperty(DeploymentConfiguration.KEY_CONSOLE_STARTUP_MODE))
                && !JNLPRuntime.isHeadless();
    }

    public static boolean canShowOnStartup(boolean isApplication) {
        return canShowOnStartup(isApplication, JNLPRuntime.getConfiguration());
    }

    public static boolean canShowOnStartup(boolean isApplication, DeploymentConfiguration config) {
        if (!isEnabled(config)) {
            return false;
        }
        return DeploymentConfiguration.CONSOLE_SHOW.equals(config.getProperty(DeploymentConfiguration.KEY_CONSOLE_STARTUP_MODE))
                || (DeploymentConfiguration.CONSOLE_SHOW_PLUGIN.equals(config.getProperty(DeploymentConfiguration.KEY_CONSOLE_STARTUP_MODE))
                && !isApplication)
                || (DeploymentConfiguration.CONSOLE_SHOW_JAVAWS.equals(config.getProperty(DeploymentConfiguration.KEY_CONSOLE_STARTUP_MODE))
                && isApplication);
    }
    private JDialog consoleWindow;
    private JTextArea stdErrText;
    private JTextArea stdOutText;
    private JPanel contentPanel = new JPanel();
    private ClassLoaderInfoProvider classLoaderInfoProvider;

    public JavaConsole() {
        initialize();
    }

    
    private void initializeWindow() {
        initializeWindow(lastSize, contentPanel);
    }
    
    private void initializeWindow(Dimension size, JPanel content) {
        consoleWindow = new JDialog((JFrame) null, R("DPJavaConsole"));
        consoleWindow.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                lastSize=consoleWindow.getSize();
            }
            
        });        
        consoleWindow.setIconImages(ImageResources.INSTANCE.getApplicationImages());
        
        consoleWindow.add(content);
        consoleWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //HIDE_ON_CLOSE can cause shut down deadlock
        consoleWindow.pack();
        if (size!=null){
            consoleWindow.setSize(size);
        } else {
            consoleWindow.setSize(new Dimension(900, 600));
        }
        consoleWindow.setMinimumSize(new Dimension(900, 300));

    }
    
    /**
     * Initialize the console
     */
    private void initialize() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }


        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints c;

        Font monoSpace = new Font("Monospaced", Font.PLAIN, 12);


        stdOutText = new JTextArea();
        JScrollPane stdOutScrollPane = new JScrollPane(stdOutText);
        stdOutScrollPane.setBorder(new TitledBorder(
                new EmptyBorder(5, 5, 5, 5), "System.out"));
        stdOutText.setEditable(false);
        stdOutText.setFont(monoSpace);

        stdErrText = new JTextArea();
        JScrollPane stdErrScrollPane = new JScrollPane(stdErrText);
        stdErrScrollPane.setBorder(new TitledBorder(
                new EmptyBorder(5, 5, 5, 5), "System.err"));
        stdErrText.setEditable(false);
        stdErrText.setFont(monoSpace);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                stdOutScrollPane, stdErrScrollPane);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 10;
        c.weighty = 1;

        contentPanel.add(splitPane, c);

        /* buttons */

        c = new GridBagConstraints();
        c.gridy = 10;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0;

        JPanel buttonPanel = new JPanel();
        contentPanel.add(buttonPanel, c);

        JButton gcButton = new JButton(R("CONSOLErungc"));
        buttonPanel.add(gcButton);
        gcButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printMemoryInfo();
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Performing Garbage Collection....");
                System.gc();
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("ButDone"));
                printMemoryInfo();
            }
        });

        JButton finalizersButton = new JButton(R("CONSOLErunFinalizers"));
        buttonPanel.add(finalizersButton);
        finalizersButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printMemoryInfo();
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CONSOLErunningFinalizers"));
                Runtime.getRuntime().runFinalization();
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("ButDone"));
                printMemoryInfo();
            }
        });

        JButton memoryButton = new JButton(R("CONSOLEmemoryInfo"));
        buttonPanel.add(memoryButton);
        memoryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printMemoryInfo();
            }
        });

        JButton systemPropertiesButton = new JButton(R("CONSOLEsystemProperties"));
        buttonPanel.add(systemPropertiesButton);
        systemPropertiesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printSystemProperties();
            }
        });

        JButton classloadersButton = new JButton(R("CONSOLEclassLoaders"));
        buttonPanel.add(classloadersButton);
        classloadersButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printClassLoaders();
            }
        });

        JButton threadListButton = new JButton(R("CONSOLEthreadList"));
        buttonPanel.add(threadListButton);
        threadListButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                printThreadInfo();
            }
        });

        JButton closeButton = new JButton(R("ButClose"));
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        hideConsole();
                    }
                });
            }
        });

        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
    }

    public void showConsole() {
        showConsole(false);
    }

    public void showConsole(boolean modal) {
        initializeWindow();
        consoleWindow.setModal(modal);
        consoleWindow.setVisible(true);
    }

    public void hideConsole() {
        consoleWindow.setModal(false);
        consoleWindow.setVisible(false);
        consoleWindow.dispose();
    }

    public void showConsoleLater() {
        showConsoleLater(false);
    }

    public void showConsoleLater(final boolean modal) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JavaConsole.getConsole().showConsole(modal);
            }
        });
    }

    public void hideConsoleLater() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JavaConsole.getConsole().hideConsole();
            }
        });
    }

    protected void printSystemProperties() {

        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, " ----");
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CONSOLEsystemProperties") + ":");
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "");
        Properties p = System.getProperties();
        Set<Object> keys = p.keySet();
        for (Object key : keys) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, key.toString() + ": " + p.get(key));
        }

        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, " ----");
    }

    public void setClassLoaderInfoProvider(ClassLoaderInfoProvider clip) {
        classLoaderInfoProvider = clip;
    }

    private void printClassLoaders() {
        if (classLoaderInfoProvider == null) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CONSOLEnoClassLoaders"));
        } else {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, " ----");
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CONSOLEclassLoaders") + ": ");
            Set<String> loaders = classLoaderInfoProvider.getLoaderInfo().keySet();
            for (String loader : loaders) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, loader + "\n"
                        + "  codebase = "
                        + classLoaderInfoProvider.getLoaderInfo().get(loader));
            }
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, " ----");
        }
    }

    private void printMemoryInfo() {
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, " ----- ");
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "  " + R("CONSOLEmemoryInfo") + ":");
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "   " + R("CONSOLEmemoryMax") + ":   "
                + String.format("%1$10d", Runtime.getRuntime().maxMemory()));
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "    " + R("CONSOLEmemoryTotal") + ": "
                + String.format("%1$10d", Runtime.getRuntime().totalMemory()));
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "    " + R("CONSOLEmemoryFree") + ":  "
                + String.format("%1$10d", Runtime.getRuntime().freeMemory()));
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, " ----");

    }

    private void printThreadInfo() {
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        Set<Thread> keys = map.keySet();
        for (Thread key : keys) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CONSOLEthread") + " " + key.getId() + ": " + key.getName());
            for (StackTraceElement element : map.get(key)) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "  " + element);
            }

        }
    }

    public static void main(String[] args) {

        final JavaConsole console = new JavaConsole();

        boolean toShowConsole = false;

        for (String arg : args) {
            if ("--show-console".equals(arg)) {
                toShowConsole = true;
            }
        }

        if (toShowConsole) {
            console.showConsoleLater();
        }

    }

    void logOutput(String s) {
        stdOutText.setText(stdOutText.getText() + s + "\n");
    }

    void logError(String s) {
        stdErrText.setText(stdErrText.getText() + s + "\n");
    }
}
