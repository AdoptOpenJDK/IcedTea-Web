/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.jnlp.util.lockingfile;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author jvanek
 */
public class NonWindowsLockedFile extends WindowsLockedFileTest {

    private static String os;

    @BeforeClass
    public static void smuggleOs() {
        os = System.getProperty("os.name");
        System.setProperty("os.name", "No Windows for itw");
    }

    @AfterClass
    public static void restoreOs() {
        System.setProperty("os.name", os);
    }

}
