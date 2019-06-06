/* PluginPanel.java
Copyright (C) 2012, Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version.
 */
package net.adoptopenjdk.icedteaweb.client.controlpanel.panels;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.adoptopenjdk.icedteaweb.client.controlpanel.DocumentAdapter;
import net.adoptopenjdk.icedteaweb.client.controlpanel.NamedBorderPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

@SuppressWarnings("serial")
public class JVMPanel extends NamedBorderPanel {

    private static final Logger LOG = LoggerFactory.getLogger(JVMPanel.class);

    public static class JvmValidationResult {

        public enum STATE {
            EMPTY, NOT_DIR, NOT_VALID_DIR, NOT_VALID_JDK, VALID_JDK;
        }

        public final String formattedText;
        public final STATE id;
        private final String stds;

        JvmValidationResult(String formattedText, STATE id, String stdouts) {
            this.id = id;
            this.formattedText = formattedText;
            this.stds = stdouts;
        }
    }

    private final JTextField testFieldArgumentsExec;
    private File lastPath = new File("/usr/lib/jvm/java/jre/");

    public JVMPanel(DeploymentConfiguration config) {
        super(Translator.R("CPHeadJVMSettings"), new GridBagLayout());

        final JLabel description = new JLabel("<html>" + Translator.R("CPJVMPluginArguments") + "<hr /></html>");
        final JTextField testFieldArguments = new JTextField(25);

        testFieldArguments.getDocument().addDocumentListener(new DocumentAdapter(config, ConfigurationConstants.KEY_PLUGIN_JVM_ARGUMENTS));
        testFieldArguments.setText(config.getProperty(ConfigurationConstants.KEY_PLUGIN_JVM_ARGUMENTS));

        final JLabel descriptionExec = new JLabel("<html>" + Translator.R("CPJVMitwExec") + "<hr /></html>");
        testFieldArgumentsExec = new JTextField(100);
        final JLabel validationResult = new JLabel(resetValidationResult(testFieldArgumentsExec.getText(), "", "CPJVMnone"));
        validationResult.setToolTipText("");
        final JCheckBox allowTypoTimeValidation = new JCheckBox(Translator.R("CPJVMPluginAllowTTValidation"), true);
        allowTypoTimeValidation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validationResult.setText(resetValidationResult(testFieldArgumentsExec.getText(), "", "CPJVMnone"));
                validationResult.setToolTipText("");
            }
        });
        testFieldArgumentsExec.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (allowTypoTimeValidation.isSelected()) {
                    JvmValidationResult s = validateJvm(testFieldArgumentsExec.getText());
                    validationResult.setText(resetValidationResult(testFieldArgumentsExec.getText(), s.formattedText, "CPJVMvalidated"));
                    validationResult.setToolTipText(s.stds);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (allowTypoTimeValidation.isSelected()) {
                    JvmValidationResult s = validateJvm(testFieldArgumentsExec.getText());
                    validationResult.setText(resetValidationResult(testFieldArgumentsExec.getText(), s.formattedText, "CPJVMvalidated"));
                    validationResult.setToolTipText(s.stds);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (allowTypoTimeValidation.isSelected()) {
                    JvmValidationResult s = validateJvm(testFieldArgumentsExec.getText());
                    validationResult.setText(resetValidationResult(testFieldArgumentsExec.getText(), s.formattedText, "CPJVMvalidated"));
                    validationResult.setToolTipText(s.stds);
                }
            }
        });

        testFieldArgumentsExec.getDocument().addDocumentListener(new DocumentAdapter(config, ConfigurationConstants.KEY_JRE_DIR));
        testFieldArgumentsExec.setText(config.getProperty(ConfigurationConstants.KEY_JRE_DIR));

        final JButton selectJvm = new JButton(Translator.R("CPJVMPluginSelectExec"));
        selectJvm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfch;
                if (lastPath != null && lastPath.exists()) {
                    jfch = new JFileChooser(lastPath);
                } else {
                    jfch = new JFileChooser();
                }
                jfch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int i = jfch.showOpenDialog(JVMPanel.this);
                if (i == JFileChooser.APPROVE_OPTION && jfch.getSelectedFile() != null) {
                    lastPath = jfch.getSelectedFile().getParentFile();
                    String nws = jfch.getSelectedFile().getAbsolutePath();
                    String olds = testFieldArgumentsExec.getText();
                    if (!nws.equals(olds)) {
                        validationResult.setText(resetValidationResult(testFieldArgumentsExec.getText(), "", "CPJVMnone"));
                        validationResult.setToolTipText("");
                    }
                    testFieldArgumentsExec.setText(nws);
                }

            }
        });
        final JButton validateJvm = new JButton(Translator.R("CPJVMitwExecValidation"));
        validateJvm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JvmValidationResult s = validateJvm(testFieldArgumentsExec.getText());
                validationResult.setText(resetValidationResult(testFieldArgumentsExec.getText(), s.formattedText, "CPJVMvalidated"));
                validationResult.setToolTipText(s.stds);

            }
        });

        // Filler to pack the bottom of the panel.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 4, 4);

        this.add(description, c);
        c.gridy++;
        this.add(testFieldArguments, c);
        c.gridy++;
        this.add(descriptionExec, c);
        c.gridy++;
        this.add(testFieldArgumentsExec, c);
        c.gridy++;
        GridBagConstraints cb1 = (GridBagConstraints) c.clone();
        cb1.fill = GridBagConstraints.NONE;
        cb1.gridwidth = 1;
        this.add(selectJvm, cb1);
        GridBagConstraints cb3 = (GridBagConstraints) c.clone();
        cb3.fill = GridBagConstraints.NONE;
        cb3.gridx = 2;
        cb3.gridwidth = 1;
        this.add(allowTypoTimeValidation, cb3);
        GridBagConstraints cb2 = (GridBagConstraints) c.clone();
        cb2.fill = GridBagConstraints.NONE;
        cb2.gridx = 3;
        cb2.gridwidth = 1;
        this.add(validateJvm, cb2);
        c.gridy++;
        this.add(validationResult, c);

        // This is to keep it from expanding vertically if resized.
        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.gridy++;
        c.weighty++;
        this.add(filler, c);
    }

    public void resetTestFieldArgumentsExec() {
        testFieldArgumentsExec.setText("");
    }

    public static JvmValidationResult validateJvm(String cmd) {
        if (cmd == null || cmd.trim().equals("")) {
            return new JvmValidationResult("<span color=\"orange\">" + Translator.R("CPJVMvalueNotSet") + "</span>",
                    JvmValidationResult.STATE.EMPTY, "");
        }
        String validationResult = "";
        File jreDirFile = new File(cmd);
        JvmValidationResult.STATE latestOne = JvmValidationResult.STATE.EMPTY;
        if (jreDirFile.isDirectory()) {
            validationResult += "<span color=\"green\">" + Translator.R("CPJVMisDir") + "</span><br />";
        } else {
            validationResult += "<span color=\"red\">" + Translator.R("CPJVMnotDir") + "</span><br />";
            latestOne = JvmValidationResult.STATE.NOT_DIR;
        }
        File javaFile = new File(cmd + File.separator + "bin" + 
                                       File.separator + "java" + 
                                       (OsUtil.isWindows() ? ".exe" : ""));
        if (javaFile.isFile()) {
            validationResult += "<span color=\"green\">" + Translator.R("CPJVMjava") + "</span><br />";
        } else {
            validationResult += "<span color=\"red\">" + Translator.R("CPJVMnoJava") + "</span><br />";
            if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
            }
        }
        ProcessBuilder sb = new ProcessBuilder(javaFile.getAbsolutePath(), "-version");
        Process p = null;
        String processErrorStream = "";
        String processStdOutStream = "";
        Integer r = null;
        try {
            p = sb.start();
            StreamUtils.waitForSafely(p);
            processErrorStream = StreamUtils.readStreamAsString(p.getErrorStream());
            processStdOutStream = StreamUtils.readStreamAsString(p.getInputStream());
            r = p.exitValue();
            LOG.debug(processErrorStream);
            LOG.info(processStdOutStream);
            processErrorStream = processErrorStream.toLowerCase();
            processStdOutStream = processStdOutStream.toLowerCase();
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);

        }
        if (r == null) {
            validationResult += "<span color=\"red\">" + Translator.R("CPJVMnotLaunched") + "</span>";
            if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
            }
            return new JvmValidationResult(validationResult, latestOne, "");
        }

        String reportableOutputs = processErrorStream + "\n" + processStdOutStream;

        if (r != 0) {
            validationResult += "<span color=\"red\">" + Translator.R("CPJVMnoSuccess") + "</span>";
            if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
            }
            return new JvmValidationResult(validationResult, latestOne, reportableOutputs);
        }

        boolean findRT = false;
        boolean jdk9up = false;
        for (int i = 9; i <= 99; i++) {
            if (processErrorStream.contains("\"" + i) || processStdOutStream.contains("\"" + i)) {
                jdk9up = true;
            }
        }
        if (jdk9up) {
            validationResult += "<span color=\"green\">" + Translator.R("CPJVMjdk9") + "</span><br />";
            findRT = false;
        } else if (processErrorStream.contains("1.8.0") || processStdOutStream.contains("1.8.0")) {
            validationResult += "<span color=\"#00EE00\">" + Translator.R("CPJVMjdk8") + "</span><br />";
            findRT = true;
        } else if (processErrorStream.contains("1.7.0") || processStdOutStream.contains("1.7.0")) {
            validationResult += "<span color=\"#EE0000\">" + Translator.R("CPJVMjdk7") + "</span><br />";
            if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
                findRT = true;

            }
        } else if (processErrorStream.contains("1.6.0") || processStdOutStream.contains("1.6.0")) {
            validationResult += "<span color=\"#EE0000\">" + Translator.R("CPJVMjdk6") + "</span><br />";
            if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
                findRT = true;
            }
        } else {
            validationResult += "<span color=\"yellow\">" + Translator.R("CPJVMjdk") + "</span><br />";
            if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
                findRT = false;
            }
        }

        if (findRT) {
            File rtFile = new File(cmd + File.separator + "lib" + File.separator + "rt.jar");
            if (rtFile.isFile()) {
                validationResult += "<span color=\"green\">" + Translator.R("CPJVMrtJar") + "</span><br />";
            } else {
                validationResult += "<span color=\"red\">" + Translator.R("CPJVMnoRtJar") + "</span><br />";
                if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                    latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
                }
            }
        }

        if (processErrorStream.contains("openjdk") || processStdOutStream.contains("openjdk")) {
            validationResult += "<span color=\"#00EE00\">" + Translator.R("CPJVMopenJdkFound") + "</span>";
            return new JvmValidationResult(validationResult, JvmValidationResult.STATE.VALID_JDK, reportableOutputs);
        }
        if (processErrorStream.contains("ibm") || processStdOutStream.contains("ibm")) {
            validationResult += "<span color=\"green\">" + Translator.R("CPJVMibmFound") + "</span>";
            if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
            }
            return new JvmValidationResult(validationResult, latestOne, reportableOutputs);
        }
        if (processErrorStream.contains("gij") || processStdOutStream.contains("gij")) {
            validationResult += "<span color=\"orange\">" + Translator.R("CPJVMgijFound") + "</span>";
            if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
            }
            return new JvmValidationResult(validationResult, latestOne, reportableOutputs);
        }
        if (processErrorStream.contains("oracle") || processStdOutStream.contains("oracle")
                || processErrorStream.contains("java(tm)") || processStdOutStream.contains("java(tm)")) {
            validationResult += "<span color=\"green\">" + Translator.R("CPJVMoracleFound") + "</span>";
            if (latestOne != JvmValidationResult.STATE.NOT_DIR) {
                latestOne = JvmValidationResult.STATE.NOT_VALID_JDK;
            }
            return new JvmValidationResult(validationResult, latestOne, reportableOutputs);
        }
        validationResult += "<span color=\"orange\">" + Translator.R("CPJVMstrangeProcess") + "</span>";
        return new JvmValidationResult(validationResult, JvmValidationResult.STATE.NOT_VALID_JDK, reportableOutputs);

    }

    private String resetValidationResult(final String value, String result, String headerKey) {
        return "<html>" + Translator.R(headerKey) + ": <br />" + value + " <br />" + result + "<hr /></html>";
    }
}
