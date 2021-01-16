package net.adoptopenjdk.icedteaweb.client.util.gridbag;

import javax.swing.JPanel;

/**
 * API of a single row in a GridBag layouted Panel.
 */
public interface GridBagRow {
    void addTo(JPanel panel, int row);
}
