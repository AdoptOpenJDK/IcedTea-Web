package net.sourceforge.jnlp.browsertesting;

import java.util.List;

/**
 * interface which represents individual browsers
 */
public interface Browser {
    public String getDefaultBin();
    public String getDefaultPluginExpectedLocation();
    public String getBin();
    //public void setBin(String bin);
    public String getUserDefaultPluginExpectedLocation();
    public Browsers getID();
    public List<String> getComaptibilitySwitches();
    public List<String> getDefaultSwitches();


}
