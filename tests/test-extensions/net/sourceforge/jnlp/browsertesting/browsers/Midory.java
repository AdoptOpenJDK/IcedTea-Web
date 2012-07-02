package net.sourceforge.jnlp.browsertesting.browsers;

import net.sourceforge.jnlp.browsertesting.Browsers;

public class Midory extends MozillaFamilyLinuxBrowser {

    public Midory(String bin) {
        super(bin);
    }

    @Override
    public Browsers getID() {
        return Browsers.midori;
    }


    
}
