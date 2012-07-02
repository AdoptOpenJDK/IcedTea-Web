package net.sourceforge.jnlp.browsertesting.browsers;

import java.util.List;

public  abstract class MozillaFamilyLinuxBrowser  extends LinuxBrowser{

    public MozillaFamilyLinuxBrowser(String bin) {
        super(bin);
        fsdir="mozilla";
    }
 

    @Override
    public List<String> getComaptibilitySwitches() {
        return null;
    }

    @Override
    public List<String> getDefaultSwitches() {
        return null;
    }
   
    @Override
    public String getUserDefaultPluginExpectedLocation() {
        return   System.getProperty("user.home")+"/.mozilla/plugins";
    }


}
