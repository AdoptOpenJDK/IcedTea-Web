/* SecurityDialogPanel.java
Copyright (C) 2008-2010 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;

/**
 * Provides a JPanel for use in JNLP warning dialogs.
 */
public abstract class SecurityDialogPanel extends JPanel {

    private final static Logger LOG = LoggerFactory.getLogger(SecurityDialogPanel.class);

    protected SecurityDialog parent;

    protected JComponent initialFocusComponent = null;

    CertVerifier certVerifier = null;

    public SecurityDialogPanel(SecurityDialog dialog, CertVerifier certVerifier) {
        this.parent = dialog;
        this.certVerifier = certVerifier;
        this.setLayout(new BorderLayout());
    }

    public SecurityDialogPanel(SecurityDialog dialog) {
        this.parent = dialog;
        this.setLayout(new BorderLayout());
    }

    /**
     * Needed to get word wrap working in JLabels.
     * @param s string to be wrapped to html tag
     * @return 
     */
    public  static String htmlWrap(String s) {
        return "<html>" + s + "</html>";
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        requestFocusOnDefaultButton();
    }

    public void requestFocusOnDefaultButton() {
        if (initialFocusComponent != null) {
            initialFocusComponent.requestFocusInWindow();
        }
    }

    public abstract DialogResult getDefaultNegativeAnswer() ;

    public abstract DialogResult getDefaultPositiveAnswer();

    /** this is default SecurityDialog "toString".
     * All extending panels are recommended to override this.
     * However, this method is reading possible shown gui,  and printing it to output
     * so free of code, this to string have pretty nice results
     * @return text gathered from components placed on this panel and cleaned from some html tags
     */
    public String getText() {
        String s = traverse(this);
        if (s != null) {
            s = s.replace("<html>", "").replace("</html>", "")
                    .replace("<head>", "").replace("</head>", "")
                    .replace("<body>", "").replace("</body>", "")
                    .replace("<br>", PlainTextFormatter.getLineSeparator())
                    .replace("<BR>", PlainTextFormatter.getLineSeparator())
                    .replace("<br/>", PlainTextFormatter.getLineSeparator())
                    .replace("<BR/>", PlainTextFormatter.getLineSeparator()); //see htmlWrap and its usages.. but eg a href is ok to keep
            s = s.replaceAll("(?m)^\\s+$", "");
            while (s.contains(PlainTextFormatter.getLineSeparator() + PlainTextFormatter.getLineSeparator())) {
                s = s.replace(PlainTextFormatter.getLineSeparator() + PlainTextFormatter.getLineSeparator(), PlainTextFormatter.getLineSeparator());
            }
        }
        
        return s;
    }

    private String traverse(Container co) {
        return traverse(co, true, JButton.class, JRadioButton.class, JCheckBox.class);
    }

    private String traverse(Container co, boolean skipClassName, Class... skipClasses) {
        StringBuilder sb = new StringBuilder();
        Component[] c = co.getComponents();
        compIter:
        for (Component c1 : c) {
            //searching to depth is important
            if (c1 instanceof Container){
                String s = traverse((Container) c1);
                sb.append(s);
            } 
            //eg jlabel is also container
            for (Class clazz : skipClasses) {
                if (c1.getClass() == clazz){
                    continue compIter;
                }
            }            
            String s;
            Method getText = getGetText(c1.getClass());
            if (getText != null) {
                s = getText(c1, getText);
            } else {
                s = c1.toString();
            }
            if (s != null) {
                s = s.trim();
                if (s.isEmpty()){
                    continue;
                }
                if (!skipClassName){
                    sb.append(s).append(PlainTextFormatter.getLineSeparator());
                } else 
                if (!s.contains(c1.getClass().getSimpleName()))  {
                    sb.append(s).append(PlainTextFormatter.getLineSeparator());
                }
            }
        }
        return sb.toString();
    }

    private Method getGetText(Class aClass) {
        try {
            String methodName = "getText";
            return aClass.getMethod(methodName);
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return null;
        }
    }

    private String getText(Component c1, Method getText) {
        try {
            return (String) getText.invoke(c1);
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return null;
        }
    }

    public abstract DialogResult readFromStdIn(String what);

    public abstract String helpToStdIn() ;

}
