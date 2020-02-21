package net.adoptopenjdk.icedteaweb.client.util.gridbag;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;

/**
 * A row with a key on the left and a value on the right.
 */
public class KeyValueRow implements GridBagRow {

    public final String key;
    public final String value;

    public KeyValueRow(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void addTo(JPanel panel, int row) {
        final JLabel keyLabel = new JLabel(key + ":");
        final GridBagConstraints keyLabelConstraints = createConstraint(row, 0);
        keyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(keyLabel, keyLabelConstraints);

        final JPanel separatorPanel = new JPanel();
        final GridBagConstraints separatorPanelConstraints = createConstraint(row, 1);
        separatorPanel.setSize(8, 0);
        panel.add(separatorPanel, separatorPanelConstraints);

        final JLabel valueLabel = new JLabel(value);
        final GridBagConstraints valueLabelConstraints = createConstraint(row, 2);
        valueLabelConstraints.weightx = 1;
        panel.add(valueLabel, valueLabelConstraints);
    }

    private GridBagConstraints createConstraint(int row, int column) {
        final GridBagConstraints result = new GridBagConstraints();
        result.gridx = column;
        result.gridy = row;
        result.ipady = 8;
        result.fill = GridBagConstraints.HORIZONTAL;
        return result;
    }
}
