package net.sourceforge.jnlp.util.logging;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.html.HTMLDocument;

import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.logging.headers.ObservableMessagesProvider;

public class ConsoleOutputPane extends JPanel implements Observer {
    
    private boolean canChange = true;

    @Override
    public synchronized void update(final Observable o, final Object arg) {
        boolean force = false;
        if (arg instanceof Boolean && ((Boolean)arg).booleanValue()) {
            force = true;
        }
        if (force) {
             refreshPane();
             return;
        }
        if (!autorefresh.isSelected()) {
            statistics.setText(model.createStatisticHint());
            return;
        }
        final boolean passed = model.shouldUpdate();

        if (!passed) {
            statistics.setText(model.createStatisticHint());
            return;
        }
        if (sortBy.getSelectedIndex() == 0) {
            //no sort, we can just update
            updatePane(false);
        } else {
            refreshPane();
        }
    }

    private final ConsoleOutputPaneModel model;
    private int lastPostion; //index of search
    private final DefaultHighlighter.DefaultHighlightPainter searchHighligh = new DefaultHighlighter.DefaultHighlightPainter(Color.blue);
    private Object lastSearchTag;

    public ConsoleOutputPane(final ObservableMessagesProvider dataProvider) {
        model = new ConsoleOutputPaneModel(dataProvider);
        // Create final JComponents members

        jPanel2 = new JPanel();
        jpanel2scrollpane = new JScrollPane(jPanel2);
        showHeaders = new JCheckBox();
        showUser = new JCheckBox();
        sortCopyAll = new JCheckBox();
        showOrigin = new JCheckBox();
        showLevel = new JCheckBox();
        showDate = new JCheckBox();
        showThread1 = new JCheckBox();
        showThread2 = new JCheckBox();
        showMessage = new JCheckBox();
        showOut = new JCheckBox();
        showErr = new JCheckBox();
        showJava = new JCheckBox();
        showPlugin = new JCheckBox();
        showPreInit = new JCheckBox();
        sortByLabel = new JLabel();
        regExLabel = new JCheckBox();
        sortBy = new JComboBox<>();
        searchLabel = new JLabel();
        autorefresh = new JCheckBox();
        refresh = new JButton();
        apply = new JButton();
        regExFilter = new JTextField();
        copyPlain = new JButton();
        copyRich = new JButton();
        next = new JButton();
        previous = new JButton();
        search = new JTextField();
        caseSensitive = new JCheckBox();
        showIncomplete = new JCheckBox();
        highLight = new JCheckBox();
        wordWrap = new JCheckBox();
        showDebug = new JCheckBox();
        showInfo = new JCheckBox();
        showItw = new JCheckBox();
        showApp = new JCheckBox();
        showCode = new JCheckBox();
        statistics = new JLabel();
        showPostInit = new JCheckBox();
        showComplete = new JCheckBox();
        match = new JRadioButton();
        notMatch = new JRadioButton();
        revertSort = new JCheckBox();
        mark = new JCheckBox();
        jScrollPane1 = new JScrollPane();
        jEditorPane1 = new JTextPane();
        showHide = new JButton();
        insertChars = new JPopupMenu();
        initComponents();
        regExFilter.setText(ConsoleOutputPaneModel.defaultPattern.pattern());
        showHeaders.setSelected(LogConfig.getLogConfig().isEnableHeaders());
        setHeadersCheckBoxesEnabled(showHeaders.isSelected());
        setMessagesCheckBoxesEnabled(showMessage.isSelected());
        refresh.setEnabled(!autorefresh.isSelected());
        if (JNLPRuntime.isWebstartApplication()) {
            showPlugin.setSelected(false);
            showPreInit.setSelected(false);
            showPostInit.setSelected(false);
            showIncomplete.setSelected(false);
            showComplete.setSelected(false);

            showPlugin.setEnabled(false);
            showPreInit.setEnabled(false);
            showPostInit.setEnabled(false);
            showIncomplete.setEnabled(false);
            showComplete.setEnabled(false);
        }
        regExFilter.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public final void insertUpdate(final DocumentEvent e) {
                colorize();
            }

            @Override
            public final void removeUpdate(final DocumentEvent e) {
                colorize();
            }

            @Override
            public final void changedUpdate(final DocumentEvent e) {
                colorize();
            }

            private final void colorize() {
                try {
                    final String s = regExFilter.getText();
                    final Pattern p = Pattern.compile(s);
                    model.lastValidPattern = p;
                    regExLabel.setForeground(Color.green);
                } catch (Exception ex) {
                    regExLabel.setForeground(Color.red);
                }
            }
        });
        regExFilter.addMouseListener(new MouseAdapter() {

            @Override
            public final void mouseClicked(final MouseEvent e) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public final void run() {
                        try {
                            if (e.getButton() != MouseEvent.BUTTON3) {
                                insertChars.setVisible(false);
                                return;
                            }
                            insertChars.setLocation(e.getXOnScreen(), e.getYOnScreen());
                            insertChars.setVisible(!insertChars.isVisible());
                        } catch (Exception ex) {
                            OutputController.getLogger().log(ex);
                        }
                    }
                });
            }
        });
        regExFilter.addKeyListener(new KeyAdapter() {

            @Override
            public final void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_CONTEXT_MENU) {
                    return;
                }
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public final void run() {
                        try {
                            insertChars.setLocation(regExFilter.getLocationOnScreen());
                            insertChars.setVisible(!insertChars.isVisible());
                        } catch (Exception ex) {
                            OutputController.getLogger().log(ex);
                        }
                    }
                });
            }
        });

        final ButtonGroup matches = new ButtonGroup();
        matches.add(match);
        matches.add(notMatch);
        showHideActionPerformed(null);
        updateModel();
        refreshPane();
    }

    private final ActionListener createDefaultAction() {
        return new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent evt) {
                if (evt == null) return;

                final Object source;
                if ((source = evt.getSource()) == showHeaders) {
                    setHeadersCheckBoxesEnabled(showHeaders.isSelected());
                } else if (source == showMessage) {
                    setMessagesCheckBoxesEnabled(showMessage.isSelected());
                } else if (source == autorefresh) {
                    refresh.setEnabled(!autorefresh.isSelected());
                }
                refreshAction();
            }
        };
    }

    final ActionListener defaultActionSingleton = createDefaultAction();

    private final ActionListener getDefaultActionSingleton() {
        return defaultActionSingleton;
    }

    private synchronized final void refreshPane() {
        if (highLight.isSelected()) {
            jEditorPane1.setContentType("text/html");
        } else {
            jEditorPane1.setContentType("text/plain");
        }
        model.lastUpdateIndex = 0;
        updatePane(true);
    }
    /**
     * when various threads update (and it can be)underlying jeditorpane
     * simultaneously, then it can lead to unpredictable issues synchronization
     * is done in invoke later
     */
    private final AtomicBoolean done = new AtomicBoolean(true);

    private synchronized final void updatePane(final boolean reset) {
        if (!done.get()) {
            return;
        }
        done.set(false);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public final void run() {
                try {
                    refreshPaneBody(reset);
                } catch (Exception ex) {
                    OutputController.getLogger().log(ex);
                } finally {
                    done.set(true);
                }
            }
        });
    }

    private final void refreshPaneBody(final boolean reset) throws BadLocationException, IOException {
        if (reset) {
            jEditorPane1.setText(model.importList(0));
        } else {
            final String s = model.importList();
            if (highLight.isSelected()) {
                HTMLDocument orig = (HTMLDocument) jEditorPane1.getDocument();
                if (revertSort.isSelected()) {
                    orig.insertAfterEnd(orig.getRootElements()[0].getElement(0)/*body*/, s);
                } else {
                    orig.insertBeforeEnd(orig.getRootElements()[0], s);
                }
            } else {
                if (revertSort.isSelected()) {
                    jEditorPane1.setText(s + jEditorPane1.getText());
                } else {
                    jEditorPane1.setText(jEditorPane1.getText() + s);
                }
            }
        }
        jEditorPane1.setCaretPosition(0);
        //jEditorPane1.repaint();
        if (mark.isSelected()) {
            markActionPerformed(null);
        }
        statistics.setText(model.createStatisticHint());
    }

    private final void initComponents() {
        //this is crucial, otherwie PlainDocument implementatin is repalcing all \n by space
        ((PlainDocument)regExFilter.getDocument()).getDocumentProperties().remove("filterNewlines");

        sortCopyAll.setSelected(true);
        sortCopyAll.setText(Translator.R("COPsortCopyAllDate"));
        sortCopyAll.setToolTipText("The sort by date is a bit more time consuming, but most natural for posting purposes");
        
        showHeaders.setSelected(true);
        showHeaders.setText(Translator.R("COPshowHeaders"));
        showHeaders.addActionListener(getDefaultActionSingleton());

        showUser.setSelected(true);
        showUser.setText(Translator.R("COPuser"));
        showUser.addActionListener(getDefaultActionSingleton());

        showOrigin.setSelected(true);
        showOrigin.setText(Translator.R("COPorigin"));
        showOrigin.addActionListener(getDefaultActionSingleton());

        showLevel.setSelected(true);
        showLevel.setText(Translator.R("COPlevel"));
        showLevel.addActionListener(getDefaultActionSingleton());

        showDate.setSelected(true);
        showDate.setText(Translator.R("COPdate"));
        showDate.addActionListener(getDefaultActionSingleton());

        showThread1.setSelected(true);
        showThread1.setText(Translator.R("COPthread1"));
        showThread1.addActionListener(getDefaultActionSingleton());

        showThread2.setSelected(true);
        showThread2.setText(Translator.R("COPthread2"));
        showThread2.addActionListener(getDefaultActionSingleton());

        showMessage.setSelected(true);
        showMessage.setText(Translator.R("COPShowMessages"));
        showMessage.addActionListener(getDefaultActionSingleton());

        showOut.setSelected(true);
        showOut.setText(Translator.R("COPstdOut"));
        showOut.addActionListener(getDefaultActionSingleton());

        showErr.setSelected(true);
        showErr.setText(Translator.R("COPstdErr"));
        showErr.addActionListener(getDefaultActionSingleton());

        showJava.setSelected(true);
        showJava.setText(Translator.R("COPjava"));
        showJava.addActionListener(getDefaultActionSingleton());

        showPlugin.setSelected(true);
        showPlugin.setText(Translator.R("COPplugin"));
        showPlugin.addActionListener(getDefaultActionSingleton());

        showPreInit.setSelected(true);
        showPreInit.setText(Translator.R("COPpreInit"));
        showPreInit.setToolTipText(Translator.R("COPpluginOnly"));
        showPreInit.addActionListener(getDefaultActionSingleton());

        sortByLabel.setText(Translator.R("COPSortBy") + ":");

        regExLabel.setText(Translator.R("COPregex") + ":");
        regExLabel.addActionListener(getDefaultActionSingleton());

        sortBy.setModel(new DefaultComboBoxModel<>(new String[] {
            Translator.R("COPAsArrived"),
            Translator.R("COPuser"),
            Translator.R("COPorigin"),
            Translator.R("COPlevel"),
            Translator.R("COPdate"),
            Translator.R("COPcode"),
            Translator.R("COPthread1"),
            Translator.R("COPthread2"),
            Translator.R("COPmessage")}));
        sortBy.addActionListener(getDefaultActionSingleton());

        searchLabel.setText(Translator.R("COPSearch") + ":");

        autorefresh.setSelected(true);
        autorefresh.setText(Translator.R("COPautoRefresh"));
        autorefresh.addActionListener(getDefaultActionSingleton());

        refresh.setText(Translator.R("COPrefresh"));
        refresh.addActionListener(getDefaultActionSingleton());

        apply.setText(Translator.R("COPApply"));
        apply.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent evt) {
                model.usedPattern = model.lastValidPattern;
                refreshAction();
            }
        });

        regExFilter.setText(".*");

        copyPlain.setText(Translator.R("COPCopyAllPlain"));
        copyPlain.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent evt) {
                copyPlainActionPerformed(evt);
            }
        });

        copyRich.setText(Translator.R("COPCopyAllRich"));
        copyRich.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent evt) {
                copyRichActionPerformed(evt);
            }
        });

        next.setText(Translator.R("COPnext"));
        next.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent evt) {
                nextActionPerformed(evt);
            }
        });

        previous.setText(Translator.R("COPprevious"));
        previous.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent evt) {
                previousActionPerformed(evt);
            }
        });

        caseSensitive.setText(Translator.R("COPcaseSensitive"));

        showIncomplete.setSelected(true);
        showIncomplete.setText(Translator.R("COPincomplete"));
        showIncomplete.setToolTipText(Translator.R("COPpluginOnly"));
        showIncomplete.addActionListener(getDefaultActionSingleton());

        highLight.setSelected(true);
        highLight.setText(Translator.R("COPhighlight"));
        highLight.addActionListener(getDefaultActionSingleton());

        wordWrap.setText(Translator.R("COPwordWrap"));
        wordWrap.addActionListener(getDefaultActionSingleton());

        showDebug.setSelected(true);
        showDebug.setText(Translator.R("COPdebug"));
        showDebug.addActionListener(getDefaultActionSingleton());

        showInfo.setSelected(true);
        showInfo.setText(Translator.R("COPinfo"));
        showInfo.addActionListener(getDefaultActionSingleton());
        
        showItw.setSelected(true);
        showItw.setText(Translator.R("COPitw"));
        showItw.addActionListener(getDefaultActionSingleton());
        
        showApp.setSelected(true);
        showApp.setText(Translator.R("COPclientApp"));
        showApp.addActionListener(getDefaultActionSingleton());

        showCode.setSelected(true);
        showCode.setText(Translator.R("COPcode"));
        showCode.addActionListener(getDefaultActionSingleton());

        statistics.setText("x/y");

        showPostInit.setSelected(true);
        showPostInit.setText(Translator.R("COPpostInit"));
        showPostInit.setToolTipText(Translator.R("COPpluginOnly"));
        showPostInit.addActionListener(getDefaultActionSingleton());

        showComplete.setSelected(true);
        showComplete.setText(Translator.R("COPcomplete"));
        showComplete.setToolTipText(Translator.R("COPpluginOnly"));
        showComplete.addActionListener(getDefaultActionSingleton());

        match.setSelected(true);
        match.setText(Translator.R("COPmatch"));
        match.addActionListener(getDefaultActionSingleton());

        notMatch.setText(Translator.R("COPnot"));
        notMatch.addActionListener(getDefaultActionSingleton());

        revertSort.setSelected(true);
        revertSort.setText(Translator.R("COPrevert"));
        revertSort.addActionListener(getDefaultActionSingleton());

        mark.setText(Translator.R("COPmark"));
        mark.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent evt) {
                markActionPerformed(evt);
            }
        });

        final GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(
                GroupLayout.Alignment.LEADING).
                addGroup(
                jPanel2Layout.createSequentialGroup().addContainerGap().addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addGroup(
                jPanel2Layout.createSequentialGroup().
                addComponent(showHeaders).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showUser).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).
                addComponent(showOrigin).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showLevel).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).
                addComponent(showDate).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showCode).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showThread1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showThread2)).
                addGroup(jPanel2Layout.createSequentialGroup().addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(
                GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                .addComponent(previous).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(mark).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(next).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE).
                addComponent(wordWrap).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(highLight).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(sortCopyAll).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(copyRich).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(copyPlain)).addGroup(GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup().
                addComponent(searchLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(search, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(caseSensitive)).addGroup(
                GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup().
                addComponent(showMessage).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showOut).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showErr).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showJava).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showPlugin).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showDebug).addGap(6, 6, 6).
                addComponent(showInfo).addGap(6, 6, 6).
                addComponent(showItw).addGap(6, 6, 6).
                addComponent(showApp)
                )).addGap(2, 2, 2).
                addComponent(statistics)).addGroup(
                GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup().
                addComponent(showPreInit).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showPostInit).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showIncomplete).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showComplete)).
                addGroup(jPanel2Layout.createSequentialGroup().
                addComponent(autorefresh).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(refresh).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(sortByLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(revertSort).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).
                addComponent(sortBy, 0, 327, Short.MAX_VALUE)).
                addGroup(jPanel2Layout.createSequentialGroup().
                addComponent(regExLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(match).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(notMatch).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(regExFilter, GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(apply, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(
                GroupLayout.Alignment.LEADING).
                addGroup(
                jPanel2Layout.createSequentialGroup().addContainerGap().addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(showHeaders).
                addComponent(showUser).
                addComponent(showLevel).
                addComponent(showDate).
                addComponent(showOrigin).
                addComponent(showCode).
                addComponent(showThread1).
                addComponent(showThread2)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(showMessage).
                addComponent(showOut).
                addComponent(showErr).
                addComponent(showJava).
                addComponent(showPlugin).
                addComponent(showDebug).
                addComponent(showInfo).
                addComponent(showItw).
                addComponent(showApp).
                addComponent(statistics)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(showPreInit).
                addComponent(showIncomplete).
                addComponent(showPostInit).
                addComponent(showComplete)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addGroup(
                jPanel2Layout.createSequentialGroup().addGap(32, 32, 32).addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(regExLabel).addComponent(regExFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).
                addComponent(apply).
                addComponent(match).
                addComponent(notMatch))).
                addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(autorefresh).
                addComponent(refresh).
                addComponent(sortByLabel).addComponent(sortBy, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).
                addComponent(revertSort))).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(searchLabel).addComponent(search, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).
                addComponent(caseSensitive)).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addComponent(previous).
                addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(sortCopyAll).
                addComponent(copyPlain).
                addComponent(copyRich).
                addComponent(highLight).
                addComponent(wordWrap)).
                addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(mark).
                addComponent(next))).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        jEditorPane1.setEditable(false);
        jScrollPane1.setViewportView(jEditorPane1);

        showHide.setText(Translator.R("ButHideDetails"));
        showHide.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent evt) {
                showHideActionPerformed(evt);
            }
        });

        final GroupLayout jPanel1Layout = new GroupLayout(this);
        super.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().
                addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE).
                addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                addComponent(showHide, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE).
                addComponent(jpanel2scrollpane, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()))));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup().addContainerGap().
                addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(jpanel2scrollpane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addComponent(showHide).addContainerGap()));

        final JMenuItem tab = new JMenuItem("insert \\t");
        tab.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public final void run() {
                        try {
                            final int i = regExFilter.getCaretPosition();
                            final StringBuilder s = new StringBuilder(regExFilter.getText());
                            s.insert(i, "\t");
                            regExFilter.setText(s.toString());
                            regExFilter.setCaretPosition(i + 1);
                            insertChars.setVisible(false);
                        } catch (Exception ex) {
                            OutputController.getLogger().log(ex);
                        }
                    }
                });
            }
        });
        final JMenuItem newLine = new JMenuItem("insert \\n");
        newLine.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public final void run() {
                        try {
                            final int i = regExFilter.getCaretPosition();
                            final StringBuilder s = new StringBuilder(regExFilter.getText());
                            s.insert(i, "\n");
                            regExFilter.setText(s.toString());
                            regExFilter.setCaretPosition(i + 1);
                            insertChars.setVisible(false);
                        } catch (Exception ex) {
                            OutputController.getLogger().log(ex);
                        }
                    }
                });
            }
        });

        final JMenuItem resetRegex = new JMenuItem("reset default");
        resetRegex.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(final ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public final void run() {
                        try {
                            regExFilter.setText(ConsoleOutputPaneModel.defaultPattern.pattern());
                            model.lastValidPattern = ConsoleOutputPaneModel.defaultPattern;
                            model.usedPattern = model.lastValidPattern;
                            insertChars.setVisible(false);
                        } catch (Exception ex) {
                            OutputController.getLogger().log(ex);
                        }
                    }
                });
            }
        });

        insertChars.add(newLine);
        insertChars.add(tab);
        insertChars.add(resetRegex);

        validate();
    }

    private final void setHeadersCheckBoxesEnabled(final boolean enable) {
        showUser.setEnabled(enable);
        showOrigin.setEnabled(enable);
        showLevel.setEnabled(enable);
        showDate.setEnabled(enable);
        showCode.setEnabled(enable);
        showThread1.setEnabled(enable);
        showThread2.setEnabled(enable);
    }

    private final void setMessagesCheckBoxesEnabled(final boolean enable) {
        showOut.setEnabled(enable);
        showErr.setEnabled(enable);
        showJava.setEnabled(enable);
        showPlugin.setEnabled(enable);
        showDebug.setEnabled(enable);
        showInfo.setEnabled(enable);
        showItw.setEnabled(enable);
        showApp.setEnabled(enable);
    }

    private final void refreshAction() {
        updateModel();
        refreshPane();
    }

    private final void markActionPerformed(final ActionEvent evt) {
        int matches = 0;
        if (!mark.isSelected()) {
            jEditorPane1.getHighlighter().removeAllHighlights();
            return;
        }
        try {
            final Document document = jEditorPane1.getDocument();
            final String find = search.getText();
            if (find.length() == 0) {
                jEditorPane1.getHighlighter().removeAllHighlights();
                return;
            }
            for (int index = 0; index + find.length() < document.getLength(); index++) {
                final String subMatch = document.getText(index, find.length());
                if ((caseSensitive.isSelected() && find.equals(subMatch)) || (!caseSensitive.isSelected() && find.equalsIgnoreCase(subMatch))) {
                    matches++;
                    DefaultHighlighter.DefaultHighlightPainter highlightPainter =
                            new DefaultHighlighter.DefaultHighlightPainter(Color.orange);
                    jEditorPane1.getHighlighter().addHighlight(index, index + find.length(),
                            highlightPainter);
                }
            }
            mark.setText(Translator.R("COPmark") + "(" + matches + ")");
        } catch (BadLocationException ex) {
            OutputController.getLogger().log(ex);
        }
    }

    private final void previousActionPerformed(final ActionEvent evt) {
        try {
            final Document document = jEditorPane1.getDocument();
            final String find = search.getText();
            if (find.length() == 0) {
                lastPostion = document.getLength() - find.length() - 1;
                return;
            }
            for (int index = lastPostion; index >= 0; index--) {
                final String subMatch = document.getText(index, find.length());
                if ((caseSensitive.isSelected() && find.equals(subMatch)) || (!caseSensitive.isSelected() && find.equalsIgnoreCase(subMatch))) {
                    if (lastSearchTag != null) {
                        jEditorPane1.getHighlighter().removeHighlight(lastSearchTag);
                    }
                    lastSearchTag = jEditorPane1.getHighlighter().addHighlight(index, index + find.length(), searchHighligh);
                    jEditorPane1.setCaretPosition(index);

                    lastPostion = index - find.length() - 1;
                    return;
                }
            }
            lastPostion = document.getLength() - find.length() - 1;
        } catch (BadLocationException ex) {
            OutputController.getLogger().log(ex);
        }
    }

    private final void nextActionPerformed(final ActionEvent evt) {
        try {
            final Document document = jEditorPane1.getDocument();
            final String find = search.getText();
            if (find.length() == 0) {
                lastPostion = 0;
                return;
            }
            for (int index = lastPostion; index + find.length() < document.getLength(); index++) {
                final String subMatch = document.getText(index, find.length());
                if ((caseSensitive.isSelected() && find.equals(subMatch)) || (!caseSensitive.isSelected() && find.equalsIgnoreCase(subMatch))) {
                    if (lastSearchTag != null) {
                        jEditorPane1.getHighlighter().removeHighlight(lastSearchTag);
                    }
                    lastSearchTag = jEditorPane1.getHighlighter().addHighlight(index, index + find.length(), searchHighligh);
                    jEditorPane1.setCaretPosition(index);

                    lastPostion = index + 1;
                    return;
                }
            }
            lastPostion = 0;
        } catch (BadLocationException ex) {
            OutputController.getLogger().log(ex);
        }
    }

    private final void showHideActionPerformed(final ActionEvent evt) {
        if (jpanel2scrollpane.isVisible()) {
            jpanel2scrollpane.setVisible(false);
            showHide.setText(Translator.R("ButShowDetails"));
        } else {
            jpanel2scrollpane.setVisible(true);
            showHide.setText(Translator.R("ButHideDetails"));
        }
    }

    private final void copyPlainActionPerformed(final ActionEvent evt) {
        if (canChange) {
            showApp.setSelected(false);
            refreshAction();
            canChange = false;
        }
        fillClipBoard(false, sortCopyAll.isSelected());
    }

    private final void copyRichActionPerformed(final ActionEvent evt) {
        if (canChange) {
            showApp.setSelected(false);
            refreshAction();
            canChange = false;
        }
        fillClipBoard(true, sortCopyAll.isSelected());
    }
    
    private final void fillClipBoard(final boolean mark, final boolean forceSort){
        final StringSelection stringSelection;
        if (forceSort){
            stringSelection = new StringSelection(model.importList(mark, 0, 4/*date*/));
        } else {
            stringSelection = new StringSelection(model.importList(mark, 0));
        }
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public static final void main(final String args[]) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public final void run() {
                final JFrame dialog = new JFrame();
                dialog.setSize(800, 600);
                dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                final ObservableMessagesProvider producer = new ConsoleOutputPaneModel.TestMessagesProvider();
                final ConsoleOutputPane jPanel1 = new ConsoleOutputPane(producer);
                producer.getObservable().addObserver(jPanel1);
                dialog.getContentPane().add(jPanel1, BorderLayout.CENTER);
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

    private final void updateModel() {
        model.highLight = highLight.isSelected();
        model.matchPattern = match.isSelected();
        model.regExLabel = regExLabel.isSelected();
        model.revertSort = revertSort.isSelected();
        model.showCode = showCode.isSelected();
        model.showComplete = showComplete.isSelected();
        model.showDate = showDate.isSelected();
        model.showDebug = showDebug.isSelected();
        model.showErr = showErr.isSelected();
        model.showHeaders = showHeaders.isSelected();
        model.showIncomplete = showIncomplete.isSelected();
        model.showInfo = showInfo.isSelected();
        model.showItw = showItw.isSelected();
        model.showApp = showApp.isSelected();
        model.showJava = showJava.isSelected();
        model.showLevel = showLevel.isSelected();
        model.showMessage = showMessage.isSelected();
        model.showOrigin = showOrigin.isSelected();
        model.showOut = showOut.isSelected();
        model.showPlugin = showPlugin.isSelected();
        model.showPostInit = showPostInit.isSelected();
        model.showPreInit = showPreInit.isSelected();
        model.showThread1 = showThread1.isSelected();
        model.showThread2 = showThread2.isSelected();
        model.showUser = showUser.isSelected();
        model.sortBy = sortBy.getSelectedIndex();
        model.wordWrap = wordWrap.isSelected();
    }

    private final JButton apply;
    private final JCheckBox autorefresh;
    private final JCheckBox caseSensitive;
    private final JButton copyPlain;
    private final JButton copyRich;
    private final JCheckBox highLight;
    private final JEditorPane jEditorPane1;
    private final JScrollPane jpanel2scrollpane;
    private final JPanel jPanel2;
    private final JScrollPane jScrollPane1;
    private final JCheckBox mark;
    private final JRadioButton match;
    private final JButton next;
    private final JRadioButton notMatch;
    private final JButton previous;
    private final JButton refresh;
    private final JTextField regExFilter;
    private final JCheckBox regExLabel;
    private final JCheckBox revertSort;
    private final JTextField search;
    private final JLabel searchLabel;
    private final JCheckBox showCode;
    private final JCheckBox showComplete;
    private final JCheckBox showDate;
    private final JCheckBox showDebug;
    private final JCheckBox showErr;
    private final JCheckBox showHeaders;
    private final JButton showHide;
    private final JCheckBox showIncomplete;
    private final JCheckBox showInfo;
    private final JCheckBox showItw;
    private final JCheckBox showApp;
    private final JCheckBox showJava;
    private final JCheckBox showLevel;
    private final JCheckBox showMessage;
    private final JCheckBox showOrigin;
    private final JCheckBox showOut;
    private final JCheckBox showPlugin;
    private final JCheckBox showPostInit;
    private final JCheckBox showPreInit;
    private final JCheckBox showThread1;
    private final JCheckBox showThread2;
    private final JCheckBox showUser;
    private final JCheckBox sortCopyAll;
    private final JComboBox<String> sortBy;
    private final JLabel sortByLabel;
    private final JLabel statistics;
    private final JCheckBox wordWrap;
    private final JPopupMenu insertChars;
}
