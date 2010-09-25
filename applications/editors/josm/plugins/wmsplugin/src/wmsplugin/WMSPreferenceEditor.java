package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

public class WMSPreferenceEditor implements PreferenceSetting {
    private WMSLayerTableModel model;
    private JComboBox browser;

    JCheckBox overlapCheckBox;
    JSpinner spinEast;
    JSpinner spinNorth;
    JSpinner spinSimConn;
    JCheckBox remoteCheckBox;
    boolean allowRemoteControl = true;
    WMSPlugin plugin = WMSPlugin.instance;

    public void addGui(final PreferenceTabbedPane gui) {
        JPanel p = gui.createPreferenceTab("wms", tr("WMS Plugin Preferences"), tr("Modify list of WMS servers displayed in the WMS plugin menu"));

        model = new WMSLayerTableModel();
        final JTable list = new JTable(model) {
            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                return (String) model.getValueAt(rowAtPoint(p), columnAtPoint(p));
            }
        };
        JScrollPane scroll = new JScrollPane(list);
        p.add(scroll, GBC.eol().fill(GridBagConstraints.BOTH));
        scroll.setPreferredSize(new Dimension(200, 200));

        final WMSDefaultLayerTableModel modeldef = new WMSDefaultLayerTableModel();
        final JTable listdef = new JTable(modeldef) {
            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                return (String) modeldef.getValueAt(rowAtPoint(p), columnAtPoint(p));
            }
        };
        JScrollPane scrolldef = new JScrollPane(listdef);
        // scrolldef is added after the buttons so it's clearer the buttons
        // control the top list and not the default one
        scrolldef.setPreferredSize(new Dimension(200, 200));

        TableColumnModel mod = listdef.getColumnModel();
        mod.getColumn(1).setPreferredWidth(800);
        mod.getColumn(0).setPreferredWidth(200);
        mod = list.getColumnModel();
        mod.getColumn(2).setPreferredWidth(50);
        mod.getColumn(1).setPreferredWidth(800);
        mod.getColumn(0).setPreferredWidth(200);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton add = new JButton(tr("Add"));
        buttonPanel.add(add, GBC.std().insets(0, 5, 0, 0));
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AddWMSLayerPanel p = new AddWMSLayerPanel();
                int answer = JOptionPane.showConfirmDialog(
                        gui, p,
                        tr("Add WMS URL"),
                        JOptionPane.OK_CANCEL_OPTION);
                if (answer == JOptionPane.OK_OPTION) {
                    model.addRow(new WMSInfo(p.getUrlName(), p.getUrl()));
                }
            }
        });

        JButton delete = new JButton(tr("Delete"));
        buttonPanel.add(delete, GBC.std().insets(0, 5, 0, 0));
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list.getSelectedRow() == -1)
                    JOptionPane.showMessageDialog(gui, tr("Please select the row to delete."));
                else {
                    Integer i;
                    while ((i = list.getSelectedRow()) != -1)
                        model.removeRow(i);
                }
            }
        });

        JButton copy = new JButton(tr("Copy Selected Default(s)"));
        buttonPanel.add(copy, GBC.std().insets(0, 5, 0, 0));
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] lines = listdef.getSelectedRows();
                if (lines.length == 0) {
                    JOptionPane.showMessageDialog(
                            gui,
                            tr("Please select at least one row to copy."),
                            tr("Information"),
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                outer: for (int i = 0; i < lines.length; i++) {
                    WMSInfo info = modeldef.getRow(lines[i]);

                    // Check if an entry with exactly the same values already
                    // exists
                    for (int j = 0; j < model.getRowCount(); j++) {
                        if (info.equalsBaseValues(model.getRow(j))) {
                            // Select the already existing row so the user has
                            // some feedback in case an entry exists
                            list.getSelectionModel().setSelectionInterval(j, j);
                            list.scrollRectToVisible(list.getCellRect(j, 0, true));
                            continue outer;
                        }
                    }

                    if (info.eulaAcceptanceRequired != null) {
                        if (!confirmeEulaAcceptance(gui, info.eulaAcceptanceRequired))
                            continue outer;
                    }

                    model.addRow(new WMSInfo(info));
                    int lastLine = model.getRowCount() - 1;
                    list.getSelectionModel().setSelectionInterval(lastLine, lastLine);
                    list.scrollRectToVisible(list.getCellRect(lastLine, 0, true));
                }
            }
        });

        p.add(buttonPanel);
        p.add(Box.createHorizontalGlue(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
        // Add default item list
        p.add(scrolldef, GBC.eol().insets(0, 5, 0, 0).fill(GridBagConstraints.BOTH));

        browser = new JComboBox(new String[] {
                "webkit-image {0}",
                "gnome-web-photo --mode=photo --format=png {0} /dev/stdout",
                "gnome-web-photo-fixed {0}",
                "webkit-image-gtk {0}"});
        browser.setEditable(true);
        browser.setSelectedItem(Main.pref.get("wmsplugin.browser", "webkit-image {0}"));
        p.add(new JLabel(tr("Downloader:")), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
        p.add(browser);

        // Overlap
        p.add(Box.createHorizontalGlue(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));

        overlapCheckBox = new JCheckBox(tr("Overlap tiles"), plugin.PROP_OVERLAP.get());
        JLabel labelEast = new JLabel(tr("% of east:"));
        JLabel labelNorth = new JLabel(tr("% of north:"));
        spinEast = new JSpinner(new SpinnerNumberModel(plugin.PROP_OVERLAP_EAST.get(), 1, 50, 1));
        spinNorth = new JSpinner(new SpinnerNumberModel(plugin.PROP_OVERLAP_NORTH.get(), 1, 50, 1));

        JPanel overlapPanel = new JPanel(new FlowLayout());
        overlapPanel.add(overlapCheckBox);
        overlapPanel.add(labelEast);
        overlapPanel.add(spinEast);
        overlapPanel.add(labelNorth);
        overlapPanel.add(spinNorth);

        p.add(overlapPanel);

        // Simultaneous connections
        p.add(Box.createHorizontalGlue(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
        JLabel labelSimConn = new JLabel(tr("Simultaneous connections"));
        spinSimConn = new JSpinner(new SpinnerNumberModel(plugin.PROP_SIMULTANEOUS_CONNECTIONS.get(), 1, 30, 1));
        JPanel overlapPanelSimConn = new JPanel(new FlowLayout());
        overlapPanelSimConn.add(labelSimConn);
        overlapPanelSimConn.add(spinSimConn);
        p.add(overlapPanelSimConn, GBC.eol().fill(GridBagConstraints.HORIZONTAL));

        allowRemoteControl = Main.pref.getBoolean("wmsplugin.remotecontrol", true);
        remoteCheckBox = new JCheckBox(tr("Allow remote control (reqires remotecontrol plugin)"), allowRemoteControl);
        JPanel remotePanel = new JPanel(new FlowLayout());
        remotePanel.add(remoteCheckBox);

        p.add(remotePanel);
    }

    public boolean ok() {
        plugin.info.save();
        plugin.refreshMenu();

        plugin.PROP_OVERLAP.put(overlapCheckBox.getModel().isSelected());
        plugin.PROP_OVERLAP_EAST.put((Integer) spinEast.getModel().getValue());
        plugin.PROP_OVERLAP_NORTH.put((Integer) spinNorth.getModel().getValue());
        plugin.PROP_SIMULTANEOUS_CONNECTIONS.put((Integer) spinSimConn.getModel().getValue());
        allowRemoteControl = remoteCheckBox.getModel().isSelected();

        Main.pref.put("wmsplugin.browser", browser.getEditor().getItem().toString());

        Main.pref.put("wmsplugin.remotecontrol", String.valueOf(allowRemoteControl));
        return false;
    }

    /**
     * Updates a server URL in the preferences dialog. Used by other plugins.
     *
     * @param server
     *            The server name
     * @param url
     *            The server URL
     */
    public void setServerUrl(String server, String url) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (server.equals(model.getValueAt(i, 0).toString())) {
                model.setValueAt(url, i, 1);
                return;
            }
        }
        model.addRow(new String[] { server, url });
    }

    /**
     * Gets a server URL in the preferences dialog. Used by other plugins.
     *
     * @param server
     *            The server name
     * @return The server URL
     */
    public String getServerUrl(String server) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (server.equals(model.getValueAt(i, 0).toString())) {
                return model.getValueAt(i, 1).toString();
            }
        }
        return null;
    }

    /**
     * The table model for the WMS layer
     *
     */
    class WMSLayerTableModel extends DefaultTableModel {
        public WMSLayerTableModel() {
            setColumnIdentifiers(new String[] { tr("Menu Name"), tr("WMS URL"), trc("layer", "Zoom") });
        }

        public WMSInfo getRow(int row) {
            return plugin.info.layers.get(row);
        }

        public void addRow(WMSInfo i) {
            plugin.info.add(i);
            int p = getRowCount() - 1;
            fireTableRowsInserted(p, p);
        }

        @Override
        public void removeRow(int i) {
            plugin.info.remove(getRow(i));
            fireTableRowsDeleted(i, i);
        }

        @Override
        public int getRowCount() {
            return plugin.info.layers.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            WMSInfo info = plugin.info.layers.get(row);
            switch (column) {
            case 0:
                return info.name;
            case 1:
                return info.getFullURL();
            case 2:
                return info.pixelPerDegree == 0.0 ? "" : info.pixelPerDegree;
            }
            return null;
        }

        @Override
        public void setValueAt(Object o, int row, int column) {
            WMSInfo info = plugin.info.layers.get(row);
            switch (column) {
            case 0:
                info.name = (String) o;
            case 1:
                info.setURL((String)o);
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return (column != 2);
        }
    }

    /**
     * The table model for the WMS layer
     *
     */
    class WMSDefaultLayerTableModel extends DefaultTableModel {
        public WMSDefaultLayerTableModel() {
            setColumnIdentifiers(new String[] { tr("Menu Name (Default)"), tr("WMS URL (Default)") });
        }

        public WMSInfo getRow(int row) {
            return plugin.info.defaultLayers.get(row);
        }

        @Override
        public int getRowCount() {
            return plugin.info.defaultLayers.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            WMSInfo info = plugin.info.defaultLayers.get(row);
            switch (column) {
            case 0:
                return info.name;
            case 1:
                return info.getFullURL();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private boolean confirmeEulaAcceptance(PreferenceTabbedPane gui, String eulaUrl) {
        URL url = null;
        try {
            url = new URL(eulaUrl.replaceAll("\\{lang\\}", Locale.getDefault().toString()));
            JEditorPane htmlPane = null;
            try {
                htmlPane = new JEditorPane(url);
            } catch (IOException e1) {
                // give a second chance with a default Locale 'en'
                try {
                    url = new URL(eulaUrl.replaceAll("\\{lang\\}", "en"));
                    htmlPane = new JEditorPane(url);
                } catch (IOException e2) {
                    JOptionPane.showMessageDialog(gui ,tr("EULA license URL not available: {0}", eulaUrl));
                    return false;
                }
            }
            Box box = Box.createVerticalBox();
            htmlPane.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(htmlPane);
            scrollPane.setPreferredSize(new Dimension(400, 400));
            box.add(scrollPane);
            int option = JOptionPane.showConfirmDialog(Main.parent, box, tr("Please abort if you are not sure"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                return true;
            }
        } catch (MalformedURLException e2) {
            JOptionPane.showMessageDialog(gui ,tr("Malformed URL for the EULA licence: {0}", eulaUrl));
        }
        return false;
    }
}
