package net.sourceforge.jnlp.browsertesting.browsers;

import net.sourceforge.jnlp.browsertesting.Browsers;

public class Chromium extends MozillaFamilyLinuxBrowser {

    public Chromium(String bin) {
        super(bin);
    }

    @Override
    public Browsers getID() {
        return Browsers.chromiumBrowser;
    }
}
