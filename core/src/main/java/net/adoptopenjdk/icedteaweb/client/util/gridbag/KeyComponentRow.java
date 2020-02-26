package net.adoptopenjdk.icedteaweb.client.util.gridbag;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;

/**
 * A row with a key on the left and a component on the right.
 */
public class KeyComponentRow implements GridBagRow {

    public final String key;
    public final JComponent component;

    public KeyComponentRow(final String key, final JComponent component) {
        this.key = key;
        this.component = component;
    }

    @Override
    public void addTo(JPanel panel, int row) {
        final JLabel keyLabel = new JLabel(key + ":");
        final GridBagConstraints keyLabelConstraints = createConstraint(row, 0);
        keyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(keyLabel, keyLabelConstraints);

        final JPanel separatorPanel = new JPanel();
        final GridBagConstraints separatorPanelConstraints = createConstraint(row, 1);
        separatorPanel.setSize(5, 0);
        panel.add(separatorPanel, separatorPanelConstraints);

        final GridBagConstraints valueLabelConstraints = createConstraint(row, 2);
        valueLabelConstraints.weightx = 1;
        panel.add(component, valueLabelConstraints);
    }

    private GridBagConstraints createConstraint(int row, int column) {
        final GridBagConstraints result = new GridBagConstraints();
        result.gridx = column;
        result.gridy = row;
        result.ipady = 5;
        result.fill = GridBagConstraints.HORIZONTAL;
        return result;
    }
}
