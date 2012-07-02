package net.sourceforge.jnlp.browsertesting;

import net.sourceforge.jnlp.annotations.TestInBrowsers;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.browsertesting.browsers.Chrome;
import net.sourceforge.jnlp.browsertesting.browsers.Chromium;
import net.sourceforge.jnlp.browsertesting.browsers.Epiphany;
import net.sourceforge.jnlp.browsertesting.browsers.Firefox;
import net.sourceforge.jnlp.browsertesting.browsers.Midory;
import net.sourceforge.jnlp.browsertesting.browsers.Opera;

public class BrowserFactory {

    private static final BrowserFactory factory = new BrowserFactory(System.getProperty(ServerAccess.USED_BROWSERS));
    private  List<Browser> configuredBrowsers;
    Random oneGenerator = new Random();

    public static BrowserFactory getFactory() {
        return factory;
    }

    /**
     * This is public just for testing purposes!
     */
    public BrowserFactory(String browsers) {
        if (browsers == null) {
            configuredBrowsers = new ArrayList<Browser>(0);
        } else {
            String[] s = browsers.split(File.pathSeparator);
            configuredBrowsers = new ArrayList<Browser>(s.length);
            for (int i = 0; i < s.length; i++) {
                String string = s[i];
                String[] p = string.split("/");
                if (p.length > 1) {
                    string = p[p.length - 1];
                }
                if (string.equals(Browsers.chromiumBrowser.toString())) {
                    configuredBrowsers.add(new Chromium(s[i]));
                }
                if (string.equals(Browsers.googleChrome.toString())) {
                    configuredBrowsers.add(new Chrome(s[i]));
                }
                if (string.equals(Browsers.opera.toString())) {
                    configuredBrowsers.add(new Opera(s[i]));
                }
                if (string.equals(Browsers.firefox.toString())) {
                    configuredBrowsers.add(new Firefox(s[i]));
                }
                if (string.equals(Browsers.epiphany.toString())) {
                    configuredBrowsers.add(new Epiphany(s[i]));
                }
                if (string.equals(Browsers.midori.toString())) {
                    configuredBrowsers.add(new Midory(s[i]));
                }
            }
        }

    }

    public Browser getBrowser(Browsers id) {
        for (int i = 0; i < configuredBrowsers.size(); i++) {
            Browser browser = configuredBrowsers.get(i);
            if (browser.getID() == id) {
                return browser;
            }

        }
        return null;

    }

    public Browser getFirst() {
        for (int i = 0; i < configuredBrowsers.size(); i++) {
            Browser browser = configuredBrowsers.get(i);
            return browser;

        }
        return null;

    }

    public Browser getRandom() {
        if (configuredBrowsers.isEmpty()){
            return null;
        }
        return configuredBrowsers.get(oneGenerator.nextInt(configuredBrowsers.size()));
    }

    public List<Browser> getAllBrowsers() {
        return Collections.unmodifiableList(configuredBrowsers);
    }

    public List<Browsers> getBrowsers(TestInBrowsers tib) {
        return getBrowsers(tib.testIn());
    }
     public List<Browsers> getBrowsers(Browsers[] testIn) {
        List<Browser> q = translateAnnotationSilently(testIn);
         if (q==null || q.isEmpty()){
            List<Browsers> qq = new ArrayList<Browsers>(0);
               qq.add(Browsers.none);
               return qq;
            }
        List<Browsers> qq = new ArrayList<Browsers>(q.size());
         for (Browser browser : q) {
             qq.add(browser.getID());
         }
         return qq;

     }
    /**
     *
     * @param testIn Bbrowsers which should be transformed to list of Browser
     * @return all matching browser, if browser do not exists, this is ignored and run is silently continued
     */
    public List<Browser> translateAnnotationSilently(Browsers[] testIn) {
        if (testIn==null) {
            return null;
        }
        List<Browser> r = new ArrayList<Browser>(configuredBrowsers.size());
        for (Browsers b : testIn) {
            if (b == Browsers.all) {
                if (getAllBrowsers().isEmpty()) {
                    ServerAccess.logErrorReprint("You try to add all browsers, but there is none");
                } else {
                    r.addAll(getAllBrowsers());
                }
            } else if (b == Browsers.one) {
                Browser bb = getRandom();
                if (bb == null) {
                    ServerAccess.logErrorReprint("You try to add random browser, but there is none");
                } else {
                    r.add(bb);
                }
            } else {
                Browser bb = getBrowser(b);
                if (bb == null) {
                    ServerAccess.logErrorReprint("You try to add " + b.toString() + " browser, but it do not exists");
                } else {
                    r.add(bb);
                }

            }
        }

        return r;

    }

    /**
     *
     * @param tib
     * @return all matching browser, if browser do not exists, exception is thrown
     */
    public List<Browser> translateAnnotationLaudly(TestInBrowsers tib) {
        return translateAnnotationLaudly(tib.testIn());
    }
    public List<Browser> translateAnnotationLaudly(Browsers[] testIn) {
        List<Browser> r = new ArrayList<Browser>(configuredBrowsers.size());
        for (Browsers b :testIn) {
            if (b == Browsers.all) {
                if (getAllBrowsers().isEmpty()) {
                    throw new IllegalStateException("You try to add all browsers, but there is none");
                }
                r.addAll(getAllBrowsers());
            } else if (b == Browsers.one) {
                Browser bb = getRandom();
                if (bb == null) {
                    throw new IllegalStateException("You try to add random browser, but there is none");
                }
                r.add(bb);
            } else {
                Browser bb = getBrowser(b);
                if (bb == null) {
                    throw new IllegalStateException("You try to add " + b.toString() + " browser, but it do not exists");
                }
                r.add(bb);

            }
        }

        return r;

    }
}
