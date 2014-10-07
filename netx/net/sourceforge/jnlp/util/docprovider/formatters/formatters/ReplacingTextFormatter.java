package net.sourceforge.jnlp.util.docprovider.formatters.formatters;

import net.sourceforge.jnlp.runtime.Boot;
import net.sourceforge.jnlp.runtime.Translator;

public abstract class ReplacingTextFormatter implements Formatter {
    
    public static String backupVersion;

    @Override
    public String process(String s) {
        return s.replace("@NWLINE@", getNewLine())
                .replace("@BOLD_CLOSE_NWLINE_BOLD_OPEN@", getBoldCloseNwlineBoldOpen())
                .replace("@BOLD_OPEN@", getBoldOpening()).replace("@BOLD_CLOSE@", getBoldClosing())
                .replace("@NWLINE_BOLD_OPEN@", getBreakAndBold()).replace("@BOLD_CLOSE_NWLINE@", getCloseBoldAndBreak());
    }

    protected String localizeTitle(String s) {
        return Translator.R("man"+s);
    }

    @Override
    public String getBold(String s) {
        return getBoldOpening() + s + getBoldClosing();
    }

    @Override
    public String getUrl(String url) {
        return getUrl(url, url);
    }
  
    public String getVersion() {
        if (Boot.version == null) {
            if (backupVersion == null) {
                return "unknown version";
            }
            return backupVersion;
        } else {
            return Boot.version;
        }
    }

    @Override
    public String getNewLine(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(getNewLine());
        }
        return sb.toString();
    }
    
    
    
    

}
