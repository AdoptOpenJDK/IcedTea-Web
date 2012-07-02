package net.sourceforge.jnlp.browsertesting.browsers;

import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.browsertesting.Browsers;

public class Firefox extends MozillaFamilyLinuxBrowser {

    public Firefox(String bin) {
        super(bin);
    }

    String[] cs={"-no-remote", "-new-tab"};

    @Override
    public Browsers getID() {
        return Browsers.firefox;
    }

    @Override
    public List<String> getComaptibilitySwitches() {
        return Arrays.asList(cs);
    }




    
}
