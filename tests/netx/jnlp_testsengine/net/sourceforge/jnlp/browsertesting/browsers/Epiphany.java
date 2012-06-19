package net.sourceforge.jnlp.browsertesting.browsers;

import net.sourceforge.jnlp.browsertesting.Browsers;

public class Epiphany extends MozillaFamilyLinuxBrowser {

    public Epiphany(String bin) {
        super(bin);
    }

    @Override
    public Browsers getID() {
        return Browsers.epiphany;
    }


    
}
