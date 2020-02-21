package net.adoptopenjdk.icedteaweb.client.util.gridbag;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import java.awt.GridBagConstraints;

/**
 * Row which acts as a spacer between other rows and spans the entire width of the grid.
 */
public class SeparatorRow implements GridBagRow {

    public static SeparatorRow createBlankRow() {
        return new SeparatorRow(new JPanel());
    }

    public static SeparatorRow createHorizontalLine() {
        return new SeparatorRow(new JSeparator());
    }

    private final JComponent separator;

    private SeparatorRow(JComponent separator) {
        this.separator = separator;
    }

    @Override
    public void addTo(JPanel panel, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.ipady = 4;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(separator, constraints);
    }
}
