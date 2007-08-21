package org.openstreetmap.josm.plugins.duplicateway;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * A plugin to add a duplicate way option to assist with creating divided roads
 * 
 * @author Brent Easton
 */
public class DuplicateWayPlugin extends Plugin {

  protected String name;

  public DuplicateWayPlugin() {
    name = tr("Duplicate Way");
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
      toolsMenu.add(new JMenuItem(new DuplicateWayAction(name)));
      Main.main.menu.add(toolsMenu, 2);
    }
    else {
      toolsMenu.addSeparator();
      toolsMenu.add(new JMenuItem(new DuplicateWayAction(name)));
    }
    
  }

}
