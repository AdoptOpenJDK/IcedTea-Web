/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.jnlp;

import java.net.MalformedURLException;

/**
 *
 * @author jvanek
 */
public interface Authentication511Requester {

    public void setNeedsAuthentication511(boolean needsAuthentication511);

    public void setWasuthenticated511(boolean wasuthenticated011);

    public boolean isNeedsAuthentication511();

    public boolean isWasuthenticated011();

    public String createReply1(String memory) throws MalformedURLException;
    public String createReply2(String memory) throws MalformedURLException;

    /**
     * When you put this to true, then , after correct login, the orginal url is returned
     * @return 
     */
    public boolean isRememberOrigianlUrl();
    public void setRememberOrigianlUrl(boolean remberUrl);
}
