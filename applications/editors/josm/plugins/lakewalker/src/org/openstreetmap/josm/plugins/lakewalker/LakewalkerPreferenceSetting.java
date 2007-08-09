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
public class LakewalkerPreferenceSetting implements PreferenceSetting {

  protected JTextField python = new JTextField(10);
  
  public void addGui(PreferenceDialog gui) {
    python.setToolTipText(tr("<html>Path to python executable.</html>"));
    String description = tr("An interlude to the Lakewalker Python module to trace water bodies on Landsat imagery.<br><br>Version: {0}", LakewalkerPlugin.VERSION);
    JPanel prefPanel = gui.createPreferenceTab("lakewalker.png", I18n.tr("Lakewalker Plugin Preferences"), description);
    prefPanel.add(new JLabel(tr("Python executable")), GBC.std().insets(10,5,5,0));
    prefPanel.add(python, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
    
  }

  public void ok() {
    Main.pref.put(LakewalkerPlugin.PREF_PYTHON, python.getText());
    
  }
  
}