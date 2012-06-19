package net.sourceforge.jnlp.browsertesting.browsers;

import net.sourceforge.jnlp.browsertesting.Browsers;

public class Firefox extends MozillaFamilyLinuxBrowser {

    public Firefox(String bin) {
        super(bin);
    }

    @Override
    public Browsers getID() {
        return Browsers.firefox;
    }


    
}
