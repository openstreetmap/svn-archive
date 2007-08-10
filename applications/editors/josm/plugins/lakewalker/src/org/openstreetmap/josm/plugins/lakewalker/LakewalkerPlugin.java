package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * Interface to Darryl Shpak's Lakewalker python module
 * 
 * @author Brent Easton
 */
public class LakewalkerPlugin extends Plugin {

  public static final String VERSION = "0.2";
  public static final String PREF_PYTHON = "lakewalker.python";
  
  protected String name;

  public LakewalkerPlugin() {
    name = tr("Lake Walker");
    JMenu toolsMenu = null;
    for (int i = 0; i < Main.main.menu.getMenuCount() && toolsMenu == null; i++) {
      JMenu menu = Main.main.menu.getMenu(i);
      String name = menu.getText();
      if (name != null && name.equals(tr("Tools"))) {
        toolsMenu = menu;
      }
    }

    if (toolsMenu == null) {
      toolsMenu = new JMenu(name);
      toolsMenu.add(new JMenuItem(new LakewalkerAction(name)));
      Main.main.menu.add(toolsMenu, 2);
    }
    else {
      toolsMenu.addSeparator();
      toolsMenu.add(new JMenuItem(new LakewalkerAction(name)));
    }
    
  }
  
  public PreferenceSetting getPreferenceSetting() 
  {
    return new LakewalkerPreferenceSetting();
  }

}
