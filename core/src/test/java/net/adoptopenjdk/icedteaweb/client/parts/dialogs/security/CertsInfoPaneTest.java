package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CertsInfoPaneTest {

    @Test
    public void hexDump() {
        assertEquals("", CertsInfoPane.hexDump(new byte[0]));
        assertEquals("0000: 0A FF", CertsInfoPane.hexDump(new byte[]{0x0A, (byte) 0xFF}));
        final byte[] seventeen = new byte[17];
        seventeen[16] = 0x01;
        assertEquals("0000: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n0010: 01", CertsInfoPane.hexDump(seventeen));
    }
}
