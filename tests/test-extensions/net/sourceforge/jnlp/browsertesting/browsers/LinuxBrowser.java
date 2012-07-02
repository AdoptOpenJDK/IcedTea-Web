package net.sourceforge.jnlp.browsertesting.browsers;

import net.sourceforge.jnlp.browsertesting.Browser;


public abstract class LinuxBrowser implements Browser{
      public static final String DEFAULT_PLUGIN_NAME="libjavaplugin.so";
      public static final String DEFAULT_BIN_PATH="/usr/bin/";

      protected final String bin;
      protected String fsdir="unknown";

    public LinuxBrowser(String bin) {
        this.bin = bin;
    }

      

    @Override
    public String getBin() {
        return bin;
    }

//    @Override
//    public void setBin(String bin) {
//        this.bin=bin;
//    }

    @Override
    public String getDefaultBin() {
       return DEFAULT_BIN_PATH+getID().toExec();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Browser)) return false;
        Browser b=(Browser) obj;
        return b.getBin().equals(getBin());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.bin != null ? this.bin.hashCode() : 0);
        return hash;
    }

      @Override
    public String getDefaultPluginExpectedLocation() {
        if (System.getProperty("os.arch").contains("64")) {
            return "/usr/lib64/"+fsdir+"/plugins";
        } else {
            return "/usr/lib/"+fsdir+"/plugins";

        }
    }


    
}
