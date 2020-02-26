package net.adoptopenjdk.icedteaweb.client.util.gridbag;

import javax.swing.JLabel;

/**
 * A row with a key on the left and a value on the right.
 */
public class KeyValueRow extends KeyComponentRow {

    public KeyValueRow(String key, String value) {
        super(key, new JLabel(value));
    }
}
