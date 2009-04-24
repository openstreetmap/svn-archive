package org.openstreetmap.josm.plugins.slippymap;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.tools.GBC;

/**
 * Preference Dialog for Slippy Map Tiles
 *
 * @author Hakan Tandogan <hakan@gurkensalat.com>
 *
 */
public class SlippyMapPreferenceSetting implements PreferenceSetting {
    /**
     * ComboBox with all known tile sources.
     */
    private JComboBox tileSourceCombo;
    
    private JCheckBox autozoomActive = new JCheckBox(tr("autozoom"));
    private JCheckBox autoloadTiles = new JCheckBox(tr("autoload tiles"));
    private JSpinner maxZoomLvl = new JSpinner();
    private JSlider fadeBackground = new JSlider(0, 100);
    
    public void addGui(PreferenceDialog gui)
    {
        //String description = tr("A plugin that adds to JOSM new layer. This layer could render external tiles.");
        JPanel slippymapTab = gui.createPreferenceTab("slippymap.png", tr("SlippyMap"), tr("Settings for the SlippyMap plugin."));
        String[] allMapUrls = SlippyMapPreferences.getAllMapUrls();
        tileSourceCombo = new JComboBox(allMapUrls);
        tileSourceCombo.setEditable(true);
        String source = SlippyMapPreferences.getMapUrl();
        tileSourceCombo.setSelectedItem(source);
        slippymapTab.add(new JLabel(tr("Tile Sources")), GBC.std());
        slippymapTab.add(GBC.glue(5, 0), GBC.std());
        slippymapTab.add(tileSourceCombo, GBC.eol().fill(GBC.HORIZONTAL));
        
        slippymapTab.add(new JLabel(tr("Auto zoom: ")), GBC.std());
        slippymapTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        slippymapTab.add(autozoomActive, GBC.eol().fill(GBC.HORIZONTAL));
        
        slippymapTab.add(new JLabel(tr("Autoload Tiles: ")), GBC.std());
        slippymapTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        slippymapTab.add(autoloadTiles, GBC.eol().fill(GBC.HORIZONTAL));
        
        slippymapTab.add(new JLabel(tr("Max zoom lvl: ")), GBC.std());
        slippymapTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        slippymapTab.add(this.maxZoomLvl, GBC.eol().fill(GBC.HORIZONTAL));
        
        slippymapTab.add(new JLabel(tr("Fade background: ")), GBC.std());
        slippymapTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        slippymapTab.add(this.fadeBackground, GBC.eol().fill(GBC.HORIZONTAL));
        
        slippymapTab.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

        this.loadSettings();
    }


    /**
     * <p>
     * Load settings from {@link SlippyMapPreferences} class. Loaded preferences are stored to local GUI components.
     * Actualy this method loads and sets this params:<br>
     * <ul>
     * 	<li>autozoom - {@link #autozoomActive} - {@link SlippyMapPreferences#getAutozoom()}</li>
     * 	<li>autoload - {@link #autoloadTiles} - {@link SlippyMapPreferences#getAutoloadTiles()}</li>
     * 	<li>maxZoomLvl - {@link #maxZoomLvl} - {@link SlippyMapPreferences#getMaxZoomLvl()}</li>
     * </ul>
     * </p>
     */
    private void loadSettings() {
        this.autozoomActive.setSelected(SlippyMapPreferences.getAutozoom());
        this.autoloadTiles.setSelected(SlippyMapPreferences.getAutoloadTiles());
        this.maxZoomLvl.setValue(SlippyMapPreferences.getMaxZoomLvl());
        this.fadeBackground.setValue(Math.round(SlippyMapPreferences.getFadeBackground()*100f));
    }
    
    /**
     * <p>
     * Someone pressed the "ok" button
     * </p>
     * <p>
     * This method saves actual state from GUI objects to actual preferences.
     * </p>
     */
    public boolean ok()
    {
        SlippyMapPreferences.setMapUrl(this.tileSourceCombo.getSelectedItem().toString());
        SlippyMapPreferences.setAutozoom(this.autozoomActive.isSelected());
        SlippyMapPreferences.setAutoloadTiles(this.autoloadTiles.isSelected());
        SlippyMapPreferences.setMaxZoomLvl((Integer)this.maxZoomLvl.getValue());
        SlippyMapPreferences.setFadeBackground(this.fadeBackground.getValue()/100f);
        //restart isn't required
        return false;
    }
}
