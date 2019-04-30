/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.adoptopenjdk.icedteaweb.testing.util;

import net.adoptopenjdk.icedteaweb.lockingfile.WindowsLockableFileTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_NAME;

/**
 *
 * @author jvanek
 */
public class NonWindowsLockableFile extends WindowsLockableFileTest {

    private static String os;

    @BeforeClass
    public static void smuggleOs() {
        os = System.getProperty(OS_NAME);
        System.setProperty(OS_NAME, "No Microsoft OS for itw");
    }

    @AfterClass
    public static void restoreOs() {
        System.setProperty(OS_NAME, os);
    }

}
