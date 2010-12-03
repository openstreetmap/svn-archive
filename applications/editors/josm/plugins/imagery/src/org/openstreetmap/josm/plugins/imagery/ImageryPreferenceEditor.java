package org.openstreetmap.josm.plugins.imagery;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.imagery.ImageryInfo.ImageryType;
import org.openstreetmap.josm.plugins.imagery.tms.TMSPreferences;
import org.openstreetmap.josm.plugins.imagery.wms.AddWMSLayerPanel;
import org.openstreetmap.josm.plugins.imagery.wms.WMSAdapter;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.GBC;

public class ImageryPreferenceEditor implements PreferenceSetting {
    private ImageryLayerTableModel model;
    private JComboBox browser;

    // Common settings
    private Color colFadeColor;
    private JButton btnFadeColor;
    private JSlider fadeAmount = new JSlider(0, 100);

    // WMS Settings
    JCheckBox overlapCheckBox;
    JSpinner spinEast;
    JSpinner spinNorth;
    JSpinner spinSimConn;
    JCheckBox remoteCheckBox;
    boolean allowRemoteControl = true;
    WMSAdapter wmsAdapter = ImageryPlugin.wmsAdapter;
    ImageryPlugin plugin = ImageryPlugin.instance;

    //TMS settings controls
    private JCheckBox autozoomActive = new JCheckBox();
    private JCheckBox autoloadTiles = new JCheckBox();
    private JSpinner minZoomLvl;
    private JSpinner maxZoomLvl;


    private JPanel buildImageryProvidersPanel(final PreferenceTabbedPane gui) {
        final JPanel p = new JPanel(new GridBagLayout());
        model = new ImageryLayerTableModel();
        final JTable list = new JTable(model) {
            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                return model.getValueAt(rowAtPoint(p), columnAtPoint(p)).toString();
            }
        };
        JScrollPane scroll = new JScrollPane(list);
        p.add(scroll, GBC.eol().fill(GridBagConstraints.BOTH));
        scroll.setPreferredSize(new Dimension(200, 200));

        final ImageryDefaultLayerTableModel modeldef = new ImageryDefaultLayerTableModel();
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
            @Override
            public void actionPerformed(ActionEvent e) {
                AddWMSLayerPanel p = new AddWMSLayerPanel();
                int answer = JOptionPane.showConfirmDialog(
                        gui, p,
                        tr("Add Imagery URL"),
                        JOptionPane.OK_CANCEL_OPTION);
                if (answer == JOptionPane.OK_OPTION) {
                    model.addRow(new ImageryInfo(p.getUrlName(), p.getUrl()));
                }
            }
        });

        JButton delete = new JButton(tr("Delete"));
        buttonPanel.add(delete, GBC.std().insets(0, 5, 0, 0));
        delete.addActionListener(new ActionListener() {
            @Override
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
            @Override
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
                    ImageryInfo info = modeldef.getRow(lines[i]);

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

                    model.addRow(new ImageryInfo(info));
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

        return p;
    }

    private JPanel buildCommonSettingsPanel(final PreferenceTabbedPane gui) {
        final JPanel p = new JPanel(new GridBagLayout());

        this.colFadeColor = ImageryPreferences.getFadeColor();
        this.btnFadeColor = new JButton();
        this.btnFadeColor.setBackground(colFadeColor);
        this.btnFadeColor.setText(ColorHelper.color2html(colFadeColor));

        this.btnFadeColor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JColorChooser chooser = new JColorChooser(colFadeColor);
                int answer = JOptionPane.showConfirmDialog(
                        gui, chooser,
                        tr("Choose a color for {0}", tr("imagery fade")),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                if (answer == JOptionPane.OK_OPTION) {
                    colFadeColor = chooser.getColor();
                    btnFadeColor.setBackground(colFadeColor);
                    btnFadeColor.setText(ColorHelper.color2html(colFadeColor));
                }
            }
        });

        p.add(new JLabel(tr("Fade Color: ")), GBC.std());
        p.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        p.add(this.btnFadeColor, GBC.eol().fill(GBC.HORIZONTAL));

        p.add(new JLabel(tr("Fade amount: ")), GBC.std());
        p.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        p.add(this.fadeAmount, GBC.eol().fill(GBC.HORIZONTAL));

        this.fadeAmount.setValue(ImageryPreferences.PROP_FADE_AMOUNT.get());
        return p;
    }

    private JPanel buildWMSSettingsPanel() {
        final JPanel p = new JPanel(new GridBagLayout());
        browser = new JComboBox(new String[] {
                "webkit-image {0}",
                "gnome-web-photo --mode=photo --format=png {0} /dev/stdout",
                "gnome-web-photo-fixed {0}",
                "webkit-image-gtk {0}"});
        browser.setEditable(true);
        browser.setSelectedItem(Main.pref.get("wmsplugin.browser", "webkit-image {0}"));
        p.add(new JLabel(tr("Downloader:")), GBC.eol().fill(GBC.HORIZONTAL));
        p.add(browser);

        // Overlap
        p.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));

        overlapCheckBox = new JCheckBox(tr("Overlap tiles"), wmsAdapter.PROP_OVERLAP.get());
        JLabel labelEast = new JLabel(tr("% of east:"));
        JLabel labelNorth = new JLabel(tr("% of north:"));
        spinEast = new JSpinner(new SpinnerNumberModel(wmsAdapter.PROP_OVERLAP_EAST.get(), 1, 50, 1));
        spinNorth = new JSpinner(new SpinnerNumberModel(wmsAdapter.PROP_OVERLAP_NORTH.get(), 1, 50, 1));

        JPanel overlapPanel = new JPanel(new FlowLayout());
        overlapPanel.add(overlapCheckBox);
        overlapPanel.add(labelEast);
        overlapPanel.add(spinEast);
        overlapPanel.add(labelNorth);
        overlapPanel.add(spinNorth);

        p.add(overlapPanel);

        // Simultaneous connections
        p.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));
        JLabel labelSimConn = new JLabel(tr("Simultaneous connections"));
        spinSimConn = new JSpinner(new SpinnerNumberModel(wmsAdapter.PROP_SIMULTANEOUS_CONNECTIONS.get(), 1, 30, 1));
        JPanel overlapPanelSimConn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        overlapPanelSimConn.add(labelSimConn);
        overlapPanelSimConn.add(spinSimConn);
        p.add(overlapPanelSimConn, GBC.eol().fill(GBC.HORIZONTAL));

        allowRemoteControl = Main.pref.getBoolean("wmsplugin.remotecontrol", true);
        remoteCheckBox = new JCheckBox(tr("Allow remote control (reqires remotecontrol plugin)"), allowRemoteControl);
        JPanel remotePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remotePanel.add(remoteCheckBox);

        p.add(remotePanel,GBC.eol().fill(GBC.HORIZONTAL));
        return p;
    }

    private JPanel buildTMSSettingsPanel() {
        JPanel tmsTab = new JPanel(new GridBagLayout());
        minZoomLvl = new JSpinner(new SpinnerNumberModel(TMSPreferences.DEFAULT_MIN_ZOOM, TMSPreferences.MIN_ZOOM, TMSPreferences.MAX_ZOOM, 1));
        maxZoomLvl = new JSpinner(new SpinnerNumberModel(TMSPreferences.DEFAULT_MAX_ZOOM, TMSPreferences.MIN_ZOOM, TMSPreferences.MAX_ZOOM, 1));

        tmsTab.add(new JLabel(tr("Auto zoom by default: ")), GBC.std());
        tmsTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        tmsTab.add(autozoomActive, GBC.eol().fill(GBC.HORIZONTAL));

        tmsTab.add(new JLabel(tr("Autoload tiles by default: ")), GBC.std());
        tmsTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        tmsTab.add(autoloadTiles, GBC.eol().fill(GBC.HORIZONTAL));

        tmsTab.add(new JLabel(tr("Min zoom lvl: ")), GBC.std());
        tmsTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        tmsTab.add(this.minZoomLvl, GBC.eol().fill(GBC.HORIZONTAL));

        tmsTab.add(new JLabel(tr("Max zoom lvl: ")), GBC.std());
        tmsTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        tmsTab.add(this.maxZoomLvl, GBC.eol().fill(GBC.HORIZONTAL));

        tmsTab.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

        this.autozoomActive.setSelected(TMSPreferences.PROP_DEFAULT_AUTOZOOM.get());
        this.autoloadTiles.setSelected(TMSPreferences.PROP_DEFAULT_AUTOLOAD.get());
        this.maxZoomLvl.setValue(TMSPreferences.getMaxZoomLvl(null));
        this.minZoomLvl.setValue(TMSPreferences.getMinZoomLvl(null));
        return tmsTab;
    }

    private void addSettingsSection(final JPanel p, String name, JPanel section) {
        final JLabel lbl = new JLabel(name);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        p.add(lbl,GBC.std());
        p.add(new JSeparator(), GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 0));
        p.add(section,GBC.eol().insets(20,5,0,5));
    }

    private Component buildSettingsPanel(final PreferenceTabbedPane gui) {
        final JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        addSettingsSection(p, tr("Common Settings"), buildCommonSettingsPanel(gui));
        addSettingsSection(p, tr("WMS Settings"), buildWMSSettingsPanel());
        addSettingsSection(p, tr("TMS Settings"), buildTMSSettingsPanel());

        p.add(new JPanel(),GBC.eol().fill(GBC.BOTH));
        return new JScrollPane(p);
    }

    @Override
    public void addGui(final PreferenceTabbedPane gui) {
        JPanel p = gui.createPreferenceTab("imagery", tr("Imagery Preferences"), tr("Modify list of imagery layers displayed in the Imagery menu"));
        JTabbedPane pane = new JTabbedPane();
        pane.add(buildImageryProvidersPanel(gui));
        pane.add(buildSettingsPanel(gui));
        pane.setTitleAt(0, tr("Imagery providers"));
        pane.setTitleAt(1, tr("Settings"));
        p.add(pane,GBC.std().fill(GBC.BOTH));
    }

    @Override
    public boolean ok() {
        plugin.info.save();
        plugin.refreshMenu();

        wmsAdapter.PROP_OVERLAP.put(overlapCheckBox.getModel().isSelected());
        wmsAdapter.PROP_OVERLAP_EAST.put((Integer) spinEast.getModel().getValue());
        wmsAdapter.PROP_OVERLAP_NORTH.put((Integer) spinNorth.getModel().getValue());
        wmsAdapter.PROP_SIMULTANEOUS_CONNECTIONS.put((Integer) spinSimConn.getModel().getValue());
        allowRemoteControl = remoteCheckBox.getModel().isSelected();

        Main.pref.put("wmsplugin.browser", browser.getEditor().getItem().toString());

        Main.pref.put("wmsplugin.remotecontrol", String.valueOf(allowRemoteControl));

        TMSPreferences.PROP_DEFAULT_AUTOZOOM.put(this.autozoomActive.isSelected());
        TMSPreferences.PROP_DEFAULT_AUTOLOAD.put(this.autoloadTiles.isSelected());
        TMSPreferences.setMaxZoomLvl((Integer)this.maxZoomLvl.getValue());
        TMSPreferences.setMinZoomLvl((Integer)this.minZoomLvl.getValue());

        ImageryPreferences.PROP_FADE_AMOUNT.put(this.fadeAmount.getValue());
        ImageryPreferences.setFadeColor(this.colFadeColor);

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
    class ImageryLayerTableModel extends DefaultTableModel {
        public ImageryLayerTableModel() {
            setColumnIdentifiers(new String[] { tr("Menu Name"), tr("Imagery URL"), trc("layer", "Zoom") });
        }

        public ImageryInfo getRow(int row) {
            return plugin.info.layers.get(row);
        }

        public void addRow(ImageryInfo i) {
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
            ImageryInfo info = plugin.info.layers.get(row);
            switch (column) {
            case 0:
                return info.name;
            case 1:
                return info.getFullURL();
            case 2:
                return (info.imageryType == ImageryType.WMS) ? (info.pixelPerDegree == 0.0 ? "" : info.pixelPerDegree)
                                                             : (info.maxZoom == 0 ? "" : info.maxZoom);
            }
            return null;
        }

        @Override
        public void setValueAt(Object o, int row, int column) {
            ImageryInfo info = plugin.info.layers.get(row);
            switch (column) {
            case 0:
                info.name = (String) o;
            case 1:
                info.setURL((String)o);
            case 2:
                if(info.imageryType == ImageryType.WMS)
                    info.pixelPerDegree = Double.parseDouble((String) o);
                else
                    info.maxZoom = Integer.parseInt((String) o);
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }
    }

    /**
     * The table model for the WMS layer
     *
     */
    class ImageryDefaultLayerTableModel extends DefaultTableModel {
        public ImageryDefaultLayerTableModel() {
            setColumnIdentifiers(new String[] { tr("Menu Name (Default)"), tr("Imagery URL (Default)") });
        }

        public ImageryInfo getRow(int row) {
            return plugin.info.defaultLayers.get(row);
        }

        @Override
        public int getRowCount() {
            return plugin.info.defaultLayers.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            ImageryInfo info = plugin.info.defaultLayers.get(row);
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
