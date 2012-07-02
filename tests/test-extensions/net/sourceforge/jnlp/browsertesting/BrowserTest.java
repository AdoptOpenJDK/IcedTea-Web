package net.sourceforge.jnlp.browsertesting;

import net.sourceforge.jnlp.ServerAccess;
import org.junit.runner.RunWith;


@RunWith(value = BrowserTestRunner.class)
public abstract class BrowserTest {

    public static Browsers browser=null;
    public static final ServerAccess server = new ServerAccess();

    public static void setBrowser(Browsers b) {
        browser = b;
        server.setCurrentBrowser(browser);
    }

    public static Browsers getBrowser() {
        return browser;
    }


}
