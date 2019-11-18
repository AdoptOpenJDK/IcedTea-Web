package net.sourceforge.jnlp.runtime;

import net.sourceforge.jnlp.JNLPFile;

/**
 * ...
 */
public interface MenuAndDesktopIntegration {

    /**
     * Creates menu and desktop entries if required by the jnlp file or settings
     */
    void addMenuAndDesktopEntries(JNLPFile file);
}
