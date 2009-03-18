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
    JMenu toolsMenu = Main.main.menu.toolsMenu;
    toolsMenu.addSeparator();
    toolsMenu.add(new JMenuItem(new DuplicateWayAction()));
  }
}
