package net.sourceforge.jnlp.browsertesting;

/**
 * When all represent all configured browser, one represens one random
 * (the first found) configured browser. Each other represents inidivdual browsers
 * 
 */
public enum Browsers {

   none, all, one, opera, googleChrome, chromiumBrowser, firefox, midori,epiphany;

    public String toExec() {
        switch (this) {
            case opera:
                return "opera";
            case googleChrome:
                return "google-chrome";
            case chromiumBrowser:
                return "chromium-browser";
            case firefox:
                return "firefox";
            case midori:
                return "midori";
            case epiphany:
                return "epiphany";
            default:
                return null;

        }
    }

    @Override
     public String toString() {
        if (toExec()!=null) return  toExec();
        switch (this) {
            case all:
                return "all";
            case one:
                return "one";
             case none:
                return "unset_browser";
           default:  return "unknown";

        }
    }
}
