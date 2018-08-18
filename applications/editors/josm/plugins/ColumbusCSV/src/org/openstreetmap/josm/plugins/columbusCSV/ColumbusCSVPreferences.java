// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.columbusCSV;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * Implements the preferences dialog for this plugin.
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 */
public class ColumbusCSVPreferences extends DefaultTabPreferenceSetting {
    public static final String PREFIX = "columbuscsv.";
    
    /**
     * Show summary after import.
     */
    public static final String SHOW_SUMMARY = PREFIX + "import.showSummary";
    /**
     * Disable auto zoom after import.
     */
    public static final String ZOOM_AFTER_IMPORT = PREFIX + "import.dontZoomAfterImport";
    /** 
     * If <tt>true</tt>, all DOP values (hdop, vdop, pdop) are ignored. If the V-900 runs in simple mode,
     * this setting has no effect.
     */
    public static final String IGNORE_VDOP = PREFIX + "import.ignoreVDOP";
    /**
     * Issue warning on missing audio files.
     */
    public static final String WARN_MISSING_AUDIO = PREFIX + "warn.missingAudio";
    /**
     * Issue warning on conversion errors.
     */
    public static final String WARN_CONVERSION_ERRORS = PREFIX + "warn.conversionErrors";
    
    /**
     * Ui elements for each flag.
     */
    private final JCheckBox colCSVShowSummary = new JCheckBox(tr("Show summary after import"));
    private final JCheckBox colCSVDontZoomAfterImport = new JCheckBox(tr("Do not zoom after import"));
    private final JCheckBox colCSVIgnoreVDOP = new JCheckBox(tr("Ignore hdop/vdop/pdop entries"));
    private final JCheckBox colCSVWarnMissingAudio = new JCheckBox(tr("Warn on missing audio files"));
    private final JCheckBox colCSVWarnConversionErrors = new JCheckBox(tr("Warn on conversion errors"));
    
    /**
     * Creates a new preferences instance.
     */
    public ColumbusCSVPreferences() {
       super();
    }

    /**
     * Applies the (new) settings after settings dialog has been closed successfully.
     */
    @Override
    public boolean ok() {
        Config.getPref().putBoolean(SHOW_SUMMARY, colCSVShowSummary.isSelected());
        Config.getPref().putBoolean(ZOOM_AFTER_IMPORT, colCSVDontZoomAfterImport.isSelected());
        Config.getPref().putBoolean(IGNORE_VDOP, colCSVIgnoreVDOP.isSelected());
        Config.getPref().putBoolean(WARN_CONVERSION_ERRORS, colCSVWarnConversionErrors.isSelected());
        Config.getPref().putBoolean(WARN_MISSING_AUDIO, colCSVWarnMissingAudio.isSelected());        
        return false;
    }
    
    /**
     * If <tt>true</tt>, a summary dialog is shown after import. Default is <tt>true</tt>.
     * @return <tt>true</tt> if a summary dialog is shown after import
     */
    public static boolean showSummary() {
        return Config.getPref().getBoolean(SHOW_SUMMARY, true);
    }
    
    /**
     * If <tt>true</tt>, the bounding box will not be scaled to the imported data.
     * @return <tt>true</tt> if the bounding box will not be scaled to the imported data
     */
    public static boolean zoomAfterImport() {
        return Config.getPref().getBoolean(ZOOM_AFTER_IMPORT, true);
    }
    
    /**
     * If <tt>true</tt>, all DOP values (hdop, vdop, pdop) are ignored. If the V-900 runs in simple mode,
     * this setting has no effect. 
     * Default is <tt>false</tt>.
     * @return <tt>true</tt> if all DOP values (hdop, vdop, pdop) are ignored
     */
    public static boolean ignoreDOP() {
        return Config.getPref().getBoolean(IGNORE_VDOP, false);
    }
    
    /**
     * If <tt>true</tt>, the plugin issues warnings when either date or position errors occurr. 
     * Default is <tt>true</tt>.
     * @return <tt>true</tt> if the plugin issues warnings when either date or position errors occurr.
     */
    public static boolean warnConversion() {
        return Config.getPref().getBoolean(WARN_CONVERSION_ERRORS, false);
    }
    
    /**
     * If <tt>true</tt>, the plugin issues a warning if a referenced audio file is missing. 
     * Default is <tt>true</tt>.
     * @return <tt>true</tt> if the plugin issues a warning when a referenced audio file is missing
     */
    public static boolean warnMissingAudio() {
        return Config.getPref().getBoolean(WARN_MISSING_AUDIO, false);
    }

    /**
     * Populates the UI with our settings.
     * @param gui The pane to populate.
     */
    @Override
    public void addGui(PreferenceTabbedPane gui) {
        // Import settings
        ButtonGroup gpsImportGroup = new ButtonGroup();
        gpsImportGroup.add(colCSVShowSummary);
        gpsImportGroup.add(colCSVDontZoomAfterImport);
        gpsImportGroup.add(colCSVIgnoreVDOP);
      
        // Warning settings
        ButtonGroup gpsWarningsGroup = new ButtonGroup();
        gpsWarningsGroup.add(colCSVWarnMissingAudio);
        gpsWarningsGroup.add(colCSVWarnConversionErrors);

        // Apply settings
        colCSVShowSummary.setSelected(Config.getPref().getBoolean(SHOW_SUMMARY, true));
        colCSVDontZoomAfterImport.setSelected(Config.getPref().getBoolean(ZOOM_AFTER_IMPORT, true));
        colCSVIgnoreVDOP.setSelected(Config.getPref().getBoolean(IGNORE_VDOP, false));
        colCSVWarnConversionErrors.setSelected(Config.getPref().getBoolean(WARN_CONVERSION_ERRORS, true));
        colCSVWarnMissingAudio.setSelected(Config.getPref().getBoolean(WARN_MISSING_AUDIO, true));
    }

    @Override
    public boolean isExpert() {
        return false;
    }
    
}
