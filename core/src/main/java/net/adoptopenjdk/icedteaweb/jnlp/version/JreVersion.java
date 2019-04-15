package net.adoptopenjdk.icedteaweb.jnlp.version;

import javax.swing.JOptionPane;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is special case of version, used only for checking jre version. If
 * jre do not isMatchOf, in strict not-headless mode the dialog with
 * confirrmation appears If jre do not isMatchOf, in strict headless mode the
 * exception is thrown If jre isMatchOf, or non-strict mode is run, then only
 * message is printed
 *
 * @deprecated use {@link VersionId} and {@link VersionString} instead
 */
@Deprecated
public class JreVersion extends Version {

    private final static Logger LOG = LoggerFactory.getLogger(JreVersion.class);

    public static boolean warned = false;

    public JreVersion(String v, boolean strict) {
        this(v, strict, JNLPRuntime.isHeadless());
    }

    /*
     *  for testing purposes
     */
    JreVersion(String v, boolean strict, boolean headless) {
        super(v);
        boolean match = matchesJreVersion();
        if (!match) {
            String s = Translator.R("JREversionDontMatch", getJreVersion(), v);
            String e = "Strict run is  deffined, and your JRE - " + getJreVersion() + " - dont isMatchOf requested JRE(s) - " + v;
            if (strict) {
                if (!headless) {
                    if (!warned) {
                        int r = JOptionPane.showConfirmDialog(null, s + "\n" + Translator.R("JREContinueDialogSentence2"), Translator.R("JREContinueDialogSentenceTitle"), JOptionPane.YES_NO_OPTION);
                        if (r == JOptionPane.NO_OPTION) {
                            throw new RuntimeException(e);
                        }
                        warned = true;
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } else {
                LOG.warn(s);
            }
        } else {
            LOG.info("good - your JRE - {} - isMatchOf requested JRE - {}", getJreVersion(), v);
        }
    }

    public boolean matchesJreVersion() {
        return matches(getJreVersion());
    }

    private String getJreVersion() {
        return System.getProperty("java.version");
    }

}
