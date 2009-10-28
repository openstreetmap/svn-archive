package org.openstreetmap.josm.plugins.slippymap;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.util.Collection;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;

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
    private JSpinner maxZoomLvl;
    private JSpinner minZoomLvl = new JSpinner();
    private JSlider fadeBackground = new JSlider(0, 100);
    
    public void addGui(PreferenceDialog gui)
    {
        minZoomLvl = new JSpinner(new SpinnerNumberModel(SlippyMapPreferences.DEFAULT_MIN_ZOOM, SlippyMapPreferences.MIN_ZOOM, SlippyMapPreferences.MAX_ZOOM, 1));
        maxZoomLvl = new JSpinner(new SpinnerNumberModel(SlippyMapPreferences.DEFAULT_MAX_ZOOM, SlippyMapPreferences.MIN_ZOOM, SlippyMapPreferences.MAX_ZOOM, 1));
        //String description = tr("A plugin that adds to JOSM new layer. This layer could render external tiles.");
        JPanel slippymapTab = gui.createPreferenceTab("slippymap.png", tr("SlippyMap"), tr("Settings for the SlippyMap plugin."));
        Collection<TileSource> allSources = SlippyMapPreferences.getAllMapSources();
        //Collection<String> allSources = SlippyMapPreferences.getAllMapNames();
        tileSourceCombo = new JComboBox(allSources.toArray());
        //tileSourceCombo.setEditable(true);
        tileSourceCombo.setSelectedItem(SlippyMapPreferences.getMapSource());
        slippymapTab.add(new JLabel(tr("Tile Sources")), GBC.std());
        slippymapTab.add(GBC.glue(5, 0), GBC.std());
        slippymapTab.add(tileSourceCombo, GBC.eol().fill(GBC.HORIZONTAL));
        
        slippymapTab.add(new JLabel(tr("Auto zoom: ")), GBC.std());
        slippymapTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        slippymapTab.add(autozoomActive, GBC.eol().fill(GBC.HORIZONTAL));
        
        slippymapTab.add(new JLabel(tr("Autoload Tiles: ")), GBC.std());
        slippymapTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        slippymapTab.add(autoloadTiles, GBC.eol().fill(GBC.HORIZONTAL));
        
        slippymapTab.add(new JLabel(tr("Min zoom lvl: ")), GBC.std());
        slippymapTab.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        slippymapTab.add(this.minZoomLvl, GBC.eol().fill(GBC.HORIZONTAL));
        
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
     * 	<li>minZoomLvl - {@link #minZoomLvl} - {@link SlippyMapPreferences#getMaxZoomLvl()}</li>
     * </ul>
     * </p>
     */
    private void loadSettings() {
        this.autozoomActive.setSelected(SlippyMapPreferences.getAutozoom());
        this.autoloadTiles.setSelected(SlippyMapPreferences.getAutoloadTiles());
        this.maxZoomLvl.setValue(SlippyMapPreferences.getMaxZoomLvl());
        this.minZoomLvl.setValue(SlippyMapPreferences.getMinZoomLvl());
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
        SlippyMapPreferences.setMapSource((TileSource)this.tileSourceCombo.getSelectedItem());
        SlippyMapPreferences.setAutozoom(this.autozoomActive.isSelected());
        SlippyMapPreferences.setAutoloadTiles(this.autoloadTiles.isSelected());
        SlippyMapPreferences.setMaxZoomLvl((Integer)this.maxZoomLvl.getValue());
        SlippyMapPreferences.setMinZoomLvl((Integer)this.minZoomLvl.getValue());
        SlippyMapPreferences.setFadeBackground(this.fadeBackground.getValue()/100f);
        //restart isn't required
        return false;
    }
}
