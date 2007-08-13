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
  
  protected JTextField python = new JTextField(10);
  protected IntConfigurer maxSegs = new IntConfigurer();
  protected IntConfigurer maxNodes = new IntConfigurer();
  protected IntConfigurer threshold = new IntConfigurer();
  
  public void addGui(PreferenceDialog gui) {
    python.setToolTipText(tr("Path to python executable."));
    maxSegs.setToolTipText(tr("Maximum number of segments per way."));
    maxNodes.setToolTipText(tr("Maximum number of nodes to trace."));
    maxNodes.setToolTipText(tr("Gray threshold."));
    String description = tr("An interlude to the Lakewalker Python module to trace water bodies on Landsat imagery.<br><br>Version: {0}", LakewalkerPlugin.VERSION);
    
    JPanel prefPanel = gui.createPreferenceTab("lakewalker.png", I18n.tr("Lakewalker Plugin Preferences"), description);
    prefPanel.add(new JLabel(tr("Python executable")), GBC.std().insets(10,5,5,0));
    prefPanel.add(python, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
    prefPanel.add(new JLabel(tr("Maximum number of segments per way")), GBC.std().insets(10,5,5,0));
    prefPanel.add(maxSegs.getControls(), GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
    prefPanel.add(new JLabel(tr("Maximum number of nodes to trace")), GBC.std().insets(10,5,5,0));
    prefPanel.add(maxNodes.getControls(), GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
    prefPanel.add(new JLabel(tr("Maximum gray value to count as water")), GBC.std().insets(10,5,5,0));
    prefPanel.add(threshold.getControls(), GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
       
    python.setText(Main.pref.get(PREF_PYTHON, "python.exe"));
    maxSegs.setValue(Main.pref.get(PREF_MAX_SEG, "250"));
    maxNodes.setValue(Main.pref.get(PREF_MAX_NODES, "50000"));
    threshold.setValue(Main.pref.get(PREF_THRESHOLD, "35"));
  }

  public void ok() {
    Main.pref.put(PREF_PYTHON, python.getText());
    Main.pref.put(PREF_MAX_SEG, maxSegs.getValueString());    
    Main.pref.put(PREF_MAX_NODES, maxNodes.getValueString());
    Main.pref.put(PREF_THRESHOLD, threshold.getValueString());
  }
  
}