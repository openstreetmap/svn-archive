package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.I18n;
public class LakewalkerPreferences implements PreferenceSetting {

  public static final String PREF_PYTHON = "lakewalker.python";
  public static final String PREF_MAX_SEG = "lakewalker.max_segs_in_way";
  public static final String PREF_MAX_NODES = "lakewalker.max_nodes";
  public static final String PREF_THRESHOLD = "lakewalker.threshold";
  public static final String PREF_EPSILON = "lakewalker.epsilon";
  public static final String PREF_LANDSAT_RES = "lakewalker.landsat_res";
  public static final String PREF_LANDSAT_SIZE = "lakewalker.landsat_size";
  public static final String PREF_EAST_OFFSET = "lakewalker.east_offset";
  public static final String PREF_NORTH_OFFSET = "lakewalker.north_offset";
  
  protected StringConfigurer pythonConfig = new StringConfigurer();
  protected JLabel pythonLabel = new JLabel(tr("Python executable"));
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
  
  public void addGui(PreferenceDialog gui) {
    pythonConfig.setToolTipText(tr("Path to python executable."));
    maxSegsConfig.setToolTipText(tr("Maximum number of segments allowed in each generated way. Default 250."));
    maxNodesConfig.setToolTipText(tr("Maximum number of nodes to generate before bailing out (before simplifying lines). Default 50000."));
    thresholdConfig.setToolTipText(tr("Maximum gray value to accept as water (based on Landsat IR-1 data). Can be in the range 0-255. Default 35."));
    epsilonConfig.setToolTipText(tr("Accuracy of Douglas-Peucker line simplification, measured in degrees.<br>Lower values give more nodes, and more accurate lines. Default 0.0003."));
    landsatResConfig.setToolTipText(tr("Resolution of Landsat tiles, measured in pixels per degree. Default 4000."));
    landsatSizeConfig.setToolTipText(tr("Size of one landsat tile, measured in pixels. Default 2000."));
    eastOffsetConfig.setToolTipText(tr("Offset all points in East direction (degrees). Default 0."));   
    northOffsetConfig.setToolTipText(tr("Offset all points in North direction (degrees). Default 0."));   

    String description = tr("An interlude to the Lakewalker Python module to trace water bodies on Landsat imagery.<br><br>Version: {0}", LakewalkerPlugin.VERSION);
    JPanel prefPanel = gui.createPreferenceTab("lakewalker.png", I18n.tr("Lakewalker Plugin Preferences"), description);
    buildPreferences(prefPanel);
    
    pythonConfig.setValue(Main.pref.get(PREF_PYTHON, "python.exe"));
    maxSegsConfig.setValue(Main.pref.get(PREF_MAX_SEG, "250"));
    maxNodesConfig.setValue(Main.pref.get(PREF_MAX_NODES, "50000"));
    thresholdConfig.setValue(Main.pref.get(PREF_THRESHOLD, "35"));
    epsilonConfig.setValue(Main.pref.get(PREF_EPSILON, "0.0003"));
    landsatResConfig.setValue(Main.pref.get(PREF_LANDSAT_RES, "4000"));
    landsatSizeConfig.setValue(Main.pref.get(PREF_LANDSAT_SIZE, "2000"));
    eastOffsetConfig.setValue(Main.pref.get(PREF_EAST_OFFSET, "0.0"));
    northOffsetConfig.setValue(Main.pref.get(PREF_NORTH_OFFSET, "0.0"));
  }
  
  public void buildPreferences(JPanel prefPanel) {
    GBC labelConstraints = GBC.std().insets(10,5,5,0);
    GBC dataConstraints = GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL);
    
    prefPanel.add(pythonLabel, labelConstraints);
    prefPanel.add(pythonConfig.getControls(), dataConstraints);
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
  }

  /*
   * Save entered preference values on OK button
   */
  public void ok() {
    Main.pref.put(PREF_PYTHON, pythonConfig.getValueString());
    Main.pref.put(PREF_MAX_SEG, maxSegsConfig.getValueString());    
    Main.pref.put(PREF_MAX_NODES, maxNodesConfig.getValueString());
    Main.pref.put(PREF_THRESHOLD, thresholdConfig.getValueString());
    Main.pref.put(PREF_EPSILON, epsilonConfig.getValueString());
    Main.pref.put(PREF_LANDSAT_RES, landsatResConfig.getValueString());
    Main.pref.put(PREF_LANDSAT_SIZE, landsatSizeConfig.getValueString());
    Main.pref.put(PREF_EAST_OFFSET, eastOffsetConfig.getValueString());
    Main.pref.put(PREF_NORTH_OFFSET, northOffsetConfig.getValueString());
  }
  
}