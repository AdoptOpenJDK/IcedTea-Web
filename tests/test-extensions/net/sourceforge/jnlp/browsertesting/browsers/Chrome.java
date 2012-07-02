package net.sourceforge.jnlp.browsertesting.browsers;

import net.sourceforge.jnlp.browsertesting.Browsers;

public class Chrome extends MozillaFamilyLinuxBrowser {

    public Chrome(String bin) {
        super(bin);
    }

    @Override
    public Browsers getID() {
        return Browsers.googleChrome;
    }
}
