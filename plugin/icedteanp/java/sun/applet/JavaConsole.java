/* JavaConsole -- A java console for the plugin
   Copyright (C) 2009  Red Hat

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

package sun.applet;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;
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
 * A simple Java console for IcedTeaPlugin
 * 
 */
public class JavaConsole {

    private boolean initialized = false;

    JFrame consoleWindow;
    JTextArea stdErrText;
    JTextArea stdOutText;

    /**
     * Initialize the console
     */
    public void initialize() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String logDir = JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_USER_LOG_DIR);

        consoleWindow = new JFrame("Java Console");
        consoleWindow.setIconImages(ImageResources.INSTANCE.getApplicationImages());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints c;

        Font monoSpace = new Font("Monospaced", Font.PLAIN, 12);

        /* std out */

        stdOutText = new JTextArea();
        JScrollPane stdOutScrollPane = new JScrollPane(stdOutText);
        stdOutScrollPane.setBorder(new TitledBorder(
                new EmptyBorder(5, 5, 5, 5), "System.out"));
        stdOutText.setEditable(false);
        stdOutText.setFont(monoSpace);

        TextAreaUpdater stdOutUpdater = new TextAreaUpdater(new File(logDir,
                PluginMain.PLUGIN_STDOUT_FILE), stdOutText);
        stdOutUpdater.setName("IcedteaPlugin Console Thread(System.out)");

        /* std err */

        stdErrText = new JTextArea();
        JScrollPane stdErrScrollPane = new JScrollPane(stdErrText);
        stdErrScrollPane.setBorder(new TitledBorder(
                new EmptyBorder(5, 5, 5, 5), "System.err"));
        stdErrText.setEditable(false);
        stdErrText.setFont(monoSpace);

        TextAreaUpdater stdErrUpdater = new TextAreaUpdater(new File(logDir,
                PluginMain.PLUGIN_STDERR_FILE), stdErrText);
        stdErrUpdater.setName("IcedteaPlugin Console Thread(System.err)");

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

        JButton gcButton = new JButton("Run GC");
        buttonPanel.add(gcButton);
        gcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printMemoryInfo();
                System.out.print("Performing Garbage Collection....");
                System.gc();
                System.out.println("Done");
                printMemoryInfo();
            }

        });

        JButton finalizersButton = new JButton("Run Finalizers");
        buttonPanel.add(finalizersButton);
        finalizersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printMemoryInfo();
                System.out.print("Running finalization....");
                Runtime.getRuntime().runFinalization();
                System.out.println("Done");
                printMemoryInfo();
            }
        });

        JButton memoryButton = new JButton("Memory Info");
        buttonPanel.add(memoryButton);
        memoryButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                printMemoryInfo();
            }

        });

        JButton systemPropertiesButton = new JButton("System Properties");
        buttonPanel.add(systemPropertiesButton);
        systemPropertiesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printSystemProperties();
            }

        });

        JButton classloadersButton = new JButton("Classloaders");
        buttonPanel.add(classloadersButton);
        classloadersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printClassLoaders();
            }

        });

        JButton threadListButton = new JButton("Thread List");
        buttonPanel.add(threadListButton);
        threadListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printThreadInfo();
            }

        });

        JButton closeButton = new JButton("Close");
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        hideConsole();
                    }
                });
            }
        });

        stdOutUpdater.start();
        stdErrUpdater.start();

        consoleWindow.add(contentPanel);
        consoleWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        consoleWindow.pack();
        consoleWindow.setSize(new Dimension(900, 600));
        consoleWindow.setMinimumSize(new Dimension(900, 300));

        initialized = true;

        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
    }

    public void showConsole() {

        if (!initialized) {
            initialize();
        }

        consoleWindow.setVisible(true);
    }

    public void hideConsole() {
        consoleWindow.setVisible(false);
    }

    protected void printSystemProperties() {

        System.out.println(" ----");
        System.out.println("System Properties:");
        System.out.println();
        Properties p = System.getProperties();
        Set<Object> keys = p.keySet();
        for (Object key : keys) {
            System.out.println(key.toString() + ": " + p.get(key));
        }

        System.out.println(" ----");
    }

    private void printClassLoaders() {
        System.out.println(" ----");
        System.out.println("Available Classloaders: ");
        Set<String> loaders = PluginAppletSecurityContext.getLoaderInfo().keySet();
        for (String loader : loaders) {
            System.out.println(loader + "\n"
                    + "  codebase = "
                    + PluginAppletSecurityContext.getLoaderInfo().get(loader));
        }
        System.out.println(" ----");
    }

    private void printMemoryInfo() {
        System.out.println(" ----- ");
        System.out.println("  Memory Info:");
        System.out.println("    Max Memory:   "
                + String.format("%1$10d", Runtime.getRuntime().maxMemory()));
        System.out.println("    Total Memory: "
                + String.format("%1$10d", Runtime.getRuntime().totalMemory()));
        System.out.println("    Free Memory:  "
                + String.format("%1$10d", Runtime.getRuntime().freeMemory()));
        System.out.println(" ----");

    }

    private void printThreadInfo() {
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        Set<Thread> keys = map.keySet();
        for (Thread key : keys) {
            System.out.println("Thread " + key.getId() + ": " + key.getName());
            for (StackTraceElement element : map.get(key)) {
                System.out.println("  " + element);
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
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    console.showConsole();
                }
            });
        }

    }

    /**
     * This thread updates the text on a JTextArea based on the text in a file
     */
    class TextAreaUpdater extends Thread {

        File fileToRead;
        JTextArea outputTextArea;

        public TextAreaUpdater(File file, JTextArea textArea) {
            fileToRead = file;
            outputTextArea = textArea;
            setDaemon(true);
        }

        public void run() {

            try {
                BufferedReader reader = new BufferedReader(new FileReader(
                        fileToRead));
                String line;
                while (true) {
                    while ((line = reader.readLine()) != null) {
                        outputTextArea.insert(line + "\n", outputTextArea
                                .getDocument().getLength());
                        outputTextArea.setCaretPosition(outputTextArea
                                .getText().length());
                    }
                    Thread.sleep(1000);
                }

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }

    }

}
