package org.openstreetmap.josm.plugins.imagery;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.imagery.tms.TMSPreferences;
import org.openstreetmap.josm.plugins.imagery.wms.WMSAdapter;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.GBC;

public class ImageryPreferenceEditor implements PreferenceSetting {
    ImageryProvidersPanel imageryProviders;

    // Common settings
    private Color colFadeColor;
    private JButton btnFadeColor;
    private JSlider fadeAmount = new JSlider(0, 100);
    private JCheckBox remoteCheckBox;
    boolean allowRemoteControl = true;

    // WMS Settings
    private JComboBox browser;
    JCheckBox overlapCheckBox;
    JSpinner spinEast;
    JSpinner spinNorth;
    JSpinner spinSimConn;
    WMSAdapter wmsAdapter = ImageryPlugin.wmsAdapter;
    ImageryPlugin plugin = ImageryPlugin.instance;

    //TMS settings controls
    private JCheckBox autozoomActive = new JCheckBox();
    private JCheckBox autoloadTiles = new JCheckBox();
    private JSpinner minZoomLvl;
    private JSpinner maxZoomLvl;

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

        allowRemoteControl = ImageryPreferences.PROP_REMOTE_CONTROL.get();
        remoteCheckBox = new JCheckBox(tr("Allow remote control (reqires remotecontrol plugin)"), allowRemoteControl);
        p.add(remoteCheckBox,GBC.eol().fill(GBC.HORIZONTAL));

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
        p.add(section,GBC.eol().insets(20,5,0,10));
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
        imageryProviders = new ImageryProvidersPanel(gui, plugin.info);
        pane.add(imageryProviders);
        pane.add(buildSettingsPanel(gui));
        pane.add(new OffsetBookmarksPanel(gui));
        pane.setTitleAt(0, tr("Imagery providers"));
        pane.setTitleAt(1, tr("Settings"));
        pane.setTitleAt(2, tr("Offset bookmarks"));
        p.add(pane,GBC.std().fill(GBC.BOTH));
    }

    @Override
    public boolean ok() {
        plugin.info.save();
        plugin.refreshMenu();
        OffsetBookmark.saveBookmarks();

        wmsAdapter.PROP_OVERLAP.put(overlapCheckBox.getModel().isSelected());
        wmsAdapter.PROP_OVERLAP_EAST.put((Integer) spinEast.getModel().getValue());
        wmsAdapter.PROP_OVERLAP_NORTH.put((Integer) spinNorth.getModel().getValue());
        wmsAdapter.PROP_SIMULTANEOUS_CONNECTIONS.put((Integer) spinSimConn.getModel().getValue());
        allowRemoteControl = remoteCheckBox.getModel().isSelected();

        Main.pref.put("wmsplugin.browser", browser.getEditor().getItem().toString());


        TMSPreferences.PROP_DEFAULT_AUTOZOOM.put(this.autozoomActive.isSelected());
        TMSPreferences.PROP_DEFAULT_AUTOLOAD.put(this.autoloadTiles.isSelected());
        TMSPreferences.setMaxZoomLvl((Integer)this.maxZoomLvl.getValue());
        TMSPreferences.setMinZoomLvl((Integer)this.minZoomLvl.getValue());

        ImageryPreferences.PROP_REMOTE_CONTROL.put(allowRemoteControl);
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
        for (int i = 0; i < imageryProviders.model.getRowCount(); i++) {
            if (server.equals(imageryProviders.model.getValueAt(i, 0).toString())) {
                imageryProviders.model.setValueAt(url, i, 1);
                return;
            }
        }
        imageryProviders.model.addRow(new String[] { server, url });
    }

    /**
     * Gets a server URL in the preferences dialog. Used by other plugins.
     *
     * @param server
     *            The server name
     * @return The server URL
     */
    public String getServerUrl(String server) {
        for (int i = 0; i < imageryProviders.model.getRowCount(); i++) {
            if (server.equals(imageryProviders.model.getValueAt(i, 0).toString())) {
                return imageryProviders.model.getValueAt(i, 1).toString();
            }
        }
        return null;
    }
}
