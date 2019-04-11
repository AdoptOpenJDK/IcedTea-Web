/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.adoptopenjdk.icedteaweb.testing;

import java.net.MalformedURLException;

/**
 *
 * @author jvanek
 */
public interface Authentication511Requester {

    void setWasuthenticated511(boolean wasuthenticated011);

    boolean isNeedsAuthentication511();

    boolean isWasuthenticated011();

    String createReply1(String memory) throws MalformedURLException;
    String createReply2(String memory);

    /**
     * When you put this to true, then , after correct login, the orginal url is returned
     * @return 
     */
    boolean isRememberOrigianlUrl();
}
