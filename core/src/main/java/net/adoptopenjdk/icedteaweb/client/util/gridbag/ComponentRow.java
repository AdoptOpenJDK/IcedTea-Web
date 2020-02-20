package net.adoptopenjdk.icedteaweb.client.util.gridbag;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;

/**
 * Row which holds a single component and spans the entire width of the grid.
 */
public class ComponentRow implements GridBagRow {

    private final JComponent component;

    public ComponentRow(JComponent component) {
        this.component = component;
    }

    @Override
    public void addTo(JPanel panel, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.ipady = 0;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, constraints);
    }
}
