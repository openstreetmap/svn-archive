package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
  
  protected JTextField python = new JTextField(10);
  protected IntConfigurer maxSegs = new IntConfigurer();
  protected IntConfigurer maxNodes = new IntConfigurer();
  protected IntConfigurer threshold = new IntConfigurer();
  protected DoubleConfigurer epsilon = new DoubleConfigurer();
  
  public void addGui(PreferenceDialog gui) {
    python.setToolTipText(tr("Path to python executable."));
    maxSegs.setToolTipText(tr("Maximum nuber of nodes allowed in one way."));
    maxNodes.setToolTipText(tr("Maximum number of nodes to generate before bailing out (before simplifying lines)."));
    maxNodes.setToolTipText(tr("Maximum gray value to accept as water (based on Landsat IR-1 data). Can be in the range 0-255."));
    epsilon.setToolTipText(tr("Accuracy of Douglas-Peucker line simplification, measured in degrees. Lower values give more nodes, and more accurate lines. Defaults to 0.0003."));   
    
    String description = tr("An interlude to the Lakewalker Python module to trace water bodies on Landsat imagery.<br><br>Version: {0}", LakewalkerPlugin.VERSION);
    
    JPanel prefPanel = gui.createPreferenceTab("lakewalker.png", I18n.tr("Lakewalker Plugin Preferences"), description);
    buildPreferences(prefPanel);
    
    python.setText(Main.pref.get(PREF_PYTHON, "python.exe"));
    maxSegs.setValue(Main.pref.get(PREF_MAX_SEG, "250"));
    maxNodes.setValue(Main.pref.get(PREF_MAX_NODES, "50000"));
    threshold.setValue(Main.pref.get(PREF_THRESHOLD, "35"));
    epsilon.setValue(Main.pref.get(PREF_EPSILON, "0.0003"));
  }
  
  public void buildPreferences(JPanel prefPanel) {
    prefPanel.add(new JLabel(tr("Python executable")), GBC.std().insets(10,5,5,0));
    prefPanel.add(python, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
    prefPanel.add(new JLabel(tr("Maximum number of segments per way")), GBC.std().insets(10,5,5,0));
    prefPanel.add(maxSegs.getControls(), GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
    prefPanel.add(new JLabel(tr("Maximum number of nodes to trace")), GBC.std().insets(10,5,5,0));
    prefPanel.add(maxNodes.getControls(), GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
    prefPanel.add(new JLabel(tr("Maximum gray value to count as water")), GBC.std().insets(10,5,5,0));
    prefPanel.add(threshold.getControls(), GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
    prefPanel.add(new JLabel(tr("Line simplification accuracy, measured in degrees.")), GBC.std().insets(10,5,5,0));
    prefPanel.add(epsilon.getControls(), GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));

  }

  public void ok() {
    Main.pref.put(PREF_PYTHON, python.getText());
    Main.pref.put(PREF_MAX_SEG, maxSegs.getValueString());    
    Main.pref.put(PREF_MAX_NODES, maxNodes.getValueString());
    Main.pref.put(PREF_THRESHOLD, threshold.getValueString());
    Main.pref.put(PREF_EPSILON, epsilon.getValueString());
  }
  
}