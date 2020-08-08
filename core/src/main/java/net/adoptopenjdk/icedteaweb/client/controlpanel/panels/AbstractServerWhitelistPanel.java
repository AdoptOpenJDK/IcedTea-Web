/* SecuritySettingsPanel.java -- Display possible security settings.
Copyright (C) 2010 Red Hat

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
package net.adoptopenjdk.icedteaweb.client.controlpanel.panels;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.controlpanel.NamedBorderPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils;
import net.sourceforge.jnlp.util.whitelist.WhitelistEntry;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * This provides a way for the user to display the server white list defined in <code>deployment.properties</code>.
 */
@SuppressWarnings("serial")
public abstract class AbstractServerWhitelistPanel extends NamedBorderPanel {
    private static final String ERROR_MARKER = "Error:";

    /**
     * This creates a new instance of the server white list panel.
     *
     * @param config Loaded DeploymentConfiguration file.
     */
    public AbstractServerWhitelistPanel(final DeploymentConfiguration config) {
        super(Translator.R("CPServerWhitelist"), new BorderLayout());

        Assert.requireNonNull(config, "config");

        final List<WhitelistEntry> whitelist = createWhitelist();

        final JTable table = new JTable(createTableModel(whitelist));
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setAutoCreateRowSorter(true);

        final TableColumnModel colModel = table.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(100);
        colModel.getColumn(1).setPreferredWidth(250);

        final ImageIcon icon = SunMiscLauncher.getSecureImageIcon("net/sourceforge/jnlp/resources/warn16.png");
        final DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final WhitelistEntryState wleState = (WhitelistEntryState) value;
                if (wleState != null) {
                    setText(wleState.getMessage());
                    if (!wleState.isValid()) {
                        setIcon(icon);
                    } else {
                        setIcon(null);
                    }
                } else {
                    setText(null);
                    setIcon(null);
                }
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                } else {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                return this;
            }
        };
        table.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
        cellRenderer.setIconTextGap(5);
        cellRenderer.setVerticalTextPosition(SwingConstants.CENTER);
        final JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }

    protected abstract List<WhitelistEntry> createWhitelist();

    private TableModel createTableModel(final List<WhitelistEntry> whitelist) {
        final String[] colNames = {R("SWPCol0Header"), R("SWPCol1Header")};
        return new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return whitelist.size();
            }

            public String getColumnName(int col) {
                return colNames[col].toString();
            }

            @Override
            public int getColumnCount() {
                return colNames.length;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                WhitelistEntry whitelistEntry = whitelist.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return whitelistEntry.getRawWhitelistEntry();
                    case 1:
                        return new WhitelistEntryState(whitelistEntry.isValid(), whitelistEntry.isValid() ? whitelistEntry.getEffectiveWhitelistEntry() : R("SWPINVALIDWLURL") + ": " + whitelistEntry.getErrorMessage());
                    default:
                        throw new IllegalArgumentException();
                }
            }
        };
    }

    private static class WhitelistEntryState {
        final private boolean valid;
        final private String message;

        public WhitelistEntryState(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
