package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.I18n;

public class LakewalkerPreferences extends DefaultTabPreferenceSetting {
    public static final String[] DIRECTIONS = new String[]
    {marktr("east"), marktr("northeast"), marktr("north"), marktr("northwest"),
    marktr("west"), marktr("southwest"), marktr("south"), marktr("southeast")};
    public static final String[] WAYTYPES = new String[]
    {marktr("water"), marktr("coastline"), marktr("land"), marktr("none")};
    public static final String[] WMSLAYERS = new String[] {"IR1", "IR2", "IR3"};

    public static final String PREF_MAX_SEG = "lakewalker.max_segs_in_way";
    public static final String PREF_MAX_NODES = "lakewalker.max_nodes";
    public static final String PREF_THRESHOLD_VALUE = "lakewalker.thresholdvalue";
    public static final String PREF_EPSILON = "lakewalker.epsilon";
    public static final String PREF_LANDSAT_RES = "lakewalker.landsat_res";
    public static final String PREF_LANDSAT_SIZE = "lakewalker.landsat_size";
    public static final String PREF_EAST_OFFSET = "lakewalker.east_offset";
    public static final String PREF_NORTH_OFFSET = "lakewalker.north_offset";
    public static final String PREF_START_DIR = "lakewalker.startdir";
    public static final String PREF_WAYTYPE = "lakewalker.waytype";
    public static final String PREF_WMS = "lakewalker.wms";
    public static final String PREF_SOURCE = "lakewalker.source";
    public static final String PREF_MAXCACHESIZE = "lakewalker.maxcachesize";
    public static final String PREF_MAXCACHEAGE = "lakewalker.maxcacheage";

    protected IntConfigurer maxSegsConfig = new IntConfigurer();
    protected JLabel maxSegsLabel = new JLabel(tr("Maximum number of segments per way"));
    protected IntConfigurer maxNodesConfig = new IntConfigurer();
    protected JLabel maxNodesLabel = new JLabel(tr("Maximum number of nodes in initial trace"));
    protected IntConfigurer thresholdConfig = new IntConfigurer();
    protected JLabel thresholdLabel = new JLabel(tr("Maximum gray value to count as water (0-255)"));
    protected DoubleConfigurer epsilonConfig = new DoubleConfigurer();
    protected JLabel epsilonLabel = new JLabel(tr("Line simplification accuracy (degrees)"));
    protected IntConfigurer landsatResConfig = new IntConfigurer();
    protected JLabel landsatResLabel = new JLabel(tr("Resolution of Landsat tiles (pixels per degree)"));
    protected IntConfigurer landsatSizeConfig = new IntConfigurer();
    protected JLabel landsatSizeLabel = new JLabel(tr("Size of Landsat tiles (pixels)"));
    protected DoubleConfigurer eastOffsetConfig = new DoubleConfigurer();
    protected JLabel eastOffsetLabel = new JLabel(tr("Shift all traces to east (degrees)"));
    protected DoubleConfigurer northOffsetConfig = new DoubleConfigurer();
    protected JLabel northOffsetLabel = new JLabel(tr("Shift all traces to north (degrees)"));
    protected StringEnumConfigurer startDirConfig = new StringEnumConfigurer(DIRECTIONS);
    protected JLabel startDirLabel = new JLabel(tr("Direction to search for land"));
    protected StringEnumConfigurer lakeTypeConfig = new StringEnumConfigurer(WAYTYPES);
    protected JLabel lakeTypeLabel = new JLabel(tr("Tag ways as"));
    protected StringEnumConfigurer wmsConfig = new StringEnumConfigurer(WMSLAYERS);
    protected JLabel wmsLabel = new JLabel(tr("WMS Layer"));
    protected IntConfigurer maxCacheSizeConfig = new IntConfigurer();
    protected JLabel maxCacheSizeLabel = new JLabel(tr("Maximum cache size (MB)"));
    protected IntConfigurer maxCacheAgeConfig = new IntConfigurer();
    protected JLabel maxCacheAgeLabel = new JLabel(tr("Maximum cache age (days)"));
    protected StringConfigurer sourceConfig = new StringConfigurer();
    protected JLabel sourceLabel = new JLabel(tr("Source text"));

    public LakewalkerPreferences() {
        super("lakewalker.png", I18n.tr("Lakewalker Plugin Preferences"), tr("A plugin to trace water bodies on Landsat imagery."));
    }

    public void addGui(PreferenceTabbedPane gui) {
        maxSegsConfig.setToolTipText(tr("Maximum number of segments allowed in each generated way. Default 250."));
        maxNodesConfig.setToolTipText(tr("Maximum number of nodes to generate before bailing out (before simplifying lines). Default 50000."));
        thresholdConfig.setToolTipText(tr("Maximum gray value to accept as water (based on Landsat IR-1 data). Can be in the range 0-255. Default 90."));
        epsilonConfig.setToolTipText(tr("Accuracy of Douglas-Peucker line simplification, measured in degrees.<br>Lower values give more nodes, and more accurate lines. Default 0.0003."));
        landsatResConfig.setToolTipText(tr("Resolution of Landsat tiles, measured in pixels per degree. Default 4000."));
        landsatSizeConfig.setToolTipText(tr("Size of one landsat tile, measured in pixels. Default 2000."));
        eastOffsetConfig.setToolTipText(tr("Offset all points in East direction (degrees). Default 0."));
        northOffsetConfig.setToolTipText(tr("Offset all points in North direction (degrees). Default 0."));
        startDirConfig.setToolTipText(tr("Direction to search for land. Default east."));
        lakeTypeConfig.setToolTipText(tr("Tag ways as water, coastline, land or nothing. Default is water."));
        wmsConfig.setToolTipText(tr("Which WMS layer to use for tracing against. Default is IR1."));
        maxCacheSizeConfig.setToolTipText(tr("Maximum size of each cache directory in bytes. Default is 300MB"));
        maxCacheAgeConfig.setToolTipText(tr("Maximum age of each cached file in days. Default is 100"));
        sourceConfig.setToolTipText(tr("Data source text. Default is Landsat."));

        String description = tr("A plugin to trace water bodies on Landsat imagery.");
        JPanel prefPanel = gui.createPreferenceTab(this);
        buildPreferences(prefPanel);

        maxSegsConfig.setValue(Main.pref.getInteger(PREF_MAX_SEG, 500));
        maxNodesConfig.setValue(Main.pref.getInteger(PREF_MAX_NODES, 50000));
        thresholdConfig.setValue(Main.pref.getInteger(PREF_THRESHOLD_VALUE, 90));
        epsilonConfig.setValue(Main.pref.getDouble(PREF_EPSILON, 0.0003));
        landsatResConfig.setValue(Main.pref.getInteger(PREF_LANDSAT_RES, 4000));
        landsatSizeConfig.setValue(Main.pref.getInteger(PREF_LANDSAT_SIZE, 2000));
        eastOffsetConfig.setValue(Main.pref.getDouble(PREF_EAST_OFFSET, 0.0));
        northOffsetConfig.setValue(Main.pref.getDouble(PREF_NORTH_OFFSET, 0.0));
        startDirConfig.setValue(Main.pref.get(PREF_START_DIR, "east"));
        lakeTypeConfig.setValue(Main.pref.get(PREF_WAYTYPE, "water"));
        wmsConfig.setValue(Main.pref.get(PREF_WMS, "IR1"));
        sourceConfig.setValue(Main.pref.get(PREF_SOURCE, "Landsat"));
        maxCacheSizeConfig.setValue(Main.pref.getInteger(PREF_MAXCACHESIZE, 300));
        maxCacheAgeConfig.setValue(Main.pref.getInteger(PREF_MAXCACHEAGE, 100));
    }

    public void buildPreferences(JPanel prefPanel) {
        GBC labelConstraints = GBC.std().insets(10,5,5,0);
        GBC dataConstraints = GBC.eol().insets(0,5,0,0).fill(GridBagConstraints.HORIZONTAL);

        prefPanel.add(maxSegsLabel, labelConstraints);
        prefPanel.add(maxSegsConfig.getControls(), dataConstraints);
        prefPanel.add(maxNodesLabel, labelConstraints);
        prefPanel.add(maxNodesConfig.getControls(), dataConstraints);
        prefPanel.add(thresholdLabel, labelConstraints);
        prefPanel.add(thresholdConfig.getControls(), dataConstraints);
        prefPanel.add(epsilonLabel, labelConstraints);
        prefPanel.add(epsilonConfig.getControls(), dataConstraints);
        prefPanel.add(landsatResLabel, labelConstraints);
        prefPanel.add(landsatResConfig.getControls(), dataConstraints);
        prefPanel.add(landsatSizeLabel, labelConstraints);
        prefPanel.add(landsatSizeConfig.getControls(), dataConstraints);
        prefPanel.add(eastOffsetLabel, labelConstraints);
        prefPanel.add(eastOffsetConfig.getControls(), dataConstraints);
        prefPanel.add(northOffsetLabel, labelConstraints);
        prefPanel.add(northOffsetConfig.getControls(), dataConstraints);
        prefPanel.add(startDirLabel, labelConstraints);
        prefPanel.add(startDirConfig.getControls(), dataConstraints);
        prefPanel.add(lakeTypeLabel, labelConstraints);
        prefPanel.add(lakeTypeConfig.getControls(), dataConstraints);
        prefPanel.add(wmsLabel, labelConstraints);
        prefPanel.add(wmsConfig.getControls(), dataConstraints);
        prefPanel.add(maxCacheSizeLabel, labelConstraints);
        prefPanel.add(maxCacheSizeConfig.getControls(), dataConstraints);
        prefPanel.add(maxCacheAgeLabel, labelConstraints);
        prefPanel.add(maxCacheAgeConfig.getControls(), dataConstraints);
        prefPanel.add(sourceLabel, labelConstraints);
        prefPanel.add(sourceConfig.getControls(), dataConstraints);

        prefPanel.add(Box.createVerticalGlue(), GBC.eol().fill(GridBagConstraints.VERTICAL));
    }

    /*
    * Save entered preference values on OK button
    */
    public boolean ok() {
        Main.pref.put(PREF_MAX_SEG, maxSegsConfig.getValueString());
        Main.pref.put(PREF_MAX_NODES, maxNodesConfig.getValueString());
        Main.pref.put(PREF_THRESHOLD_VALUE, thresholdConfig.getValueString());
        Main.pref.put(PREF_EPSILON, epsilonConfig.getValueString());
        Main.pref.put(PREF_LANDSAT_RES, landsatResConfig.getValueString());
        Main.pref.put(PREF_LANDSAT_SIZE, landsatSizeConfig.getValueString());
        Main.pref.put(PREF_EAST_OFFSET, eastOffsetConfig.getValueString());
        Main.pref.put(PREF_NORTH_OFFSET, northOffsetConfig.getValueString());
        Main.pref.put(PREF_START_DIR, startDirConfig.getValueString());
        Main.pref.put(PREF_WAYTYPE, lakeTypeConfig.getValueString());
        Main.pref.put(PREF_WMS, wmsConfig.getValueString());
        Main.pref.put(PREF_MAXCACHESIZE, maxCacheSizeConfig.getValueString());
        Main.pref.put(PREF_MAXCACHEAGE, maxCacheAgeConfig.getValueString());
        Main.pref.put(PREF_SOURCE, sourceConfig.getValueString());
        return false;
    }
}
