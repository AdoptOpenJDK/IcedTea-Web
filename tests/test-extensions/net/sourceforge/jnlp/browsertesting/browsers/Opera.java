package net.sourceforge.jnlp.browsertesting.browsers;

import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.browsertesting.Browsers;

public class Opera extends LinuxBrowser {

    public Opera(String bin) {
        super(bin);
        fsdir="opera";
    }

    @Override
    public Browsers getID() {
        return Browsers.opera;
    }

    @Override
    public String getUserDefaultPluginExpectedLocation() {
        return null;
    }


    String[] cs={"-nosession", "-nomail", "-nolirc", "-newtab"};

    @Override
    public List<String> getComaptibilitySwitches() {
        return Arrays.asList(cs);
    }

    @Override
    public List<String> getDefaultSwitches() {
        return null;
    }

}