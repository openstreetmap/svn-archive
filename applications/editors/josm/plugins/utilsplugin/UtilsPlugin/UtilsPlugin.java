package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import UtilsPlugin.*;
//import UtilsPlugin.JosmLint.JosmLint;
import org.openstreetmap.josm.gui.IconToggleButton;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import javax.swing.JPanel;
import javax.swing.BoxLayout;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.gui.MapFrame;

public class UtilsPlugin extends Plugin {

    private JMenu toolsMenu;
    private JMenuItem mergePointsMenu = new JMenuItem(new MergePointsAction());
    private JMenuItem mergePointLineMenu = new JMenuItem(new MergePointLineAction());
    private JMenuItem mergeWaysMenu = new JMenuItem(new MergeWaysAction());
    private JMenuItem deduplicateWayMenu =  new JMenuItem(new DeduplicateWayAction());
    private JMenuItem simplifyWayMenu =  new JMenuItem(new SimplifyWayAction());
        
    public UtilsPlugin() {
	JMenuBar menu = Main.main.menu;
	toolsMenu = menu.getMenu(4);
/*
	This code doesn't work, because getName returns null always, so we get two menus
	
	for (int i = 0; i < menu.getMenuCount(); ++i) {
	    javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("Menu ["+menu.getMenu(i).getName()+","+tr("Edit")+"]"));
	    if (menu.getMenu(i) != null && tr("Edit").equals(menu.getMenu(i).getName())) {
		editMenu = menu.getMenu(i);
		break;
	    }
	}
*/
	if (toolsMenu == null) {
	    toolsMenu = new JMenu(tr("Tools"));
	    menu.add(toolsMenu, 5);
	    toolsMenu.setVisible(false);
	}
	toolsMenu.add(mergePointsMenu);
	toolsMenu.add(mergePointLineMenu);
	toolsMenu.add(mergeWaysMenu);
	toolsMenu.add(deduplicateWayMenu);
	toolsMenu.add(simplifyWayMenu);
	mergePointsMenu.setVisible(false);
	mergePointLineMenu.setVisible(false);
	mergeWaysMenu.setVisible(false);
	deduplicateWayMenu.setVisible(false);
	simplifyWayMenu.setVisible(false);
    }
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (oldFrame != null && newFrame == null) {
			// disable
			mergePointsMenu.setVisible(false);
			mergePointLineMenu.setVisible(false);
			mergeWaysMenu.setVisible(false);
			deduplicateWayMenu.setVisible(false);
			simplifyWayMenu.setVisible(false);
//			JosmLint.stopPlugin();
			if (toolsMenu.getMenuComponentCount() == 4)
				toolsMenu.setVisible(false);
		} else if (oldFrame == null && newFrame != null) {
			// enable
			mergePointsMenu.setVisible(true);
			mergePointLineMenu.setVisible(true);
			mergeWaysMenu.setVisible(true);
			deduplicateWayMenu.setVisible(true);
			simplifyWayMenu.setVisible(true);

//			JosmLint.setupPlugin();
			
			if (toolsMenu.getMenuComponentCount() == 4)
				toolsMenu.setVisible(true);
		}
	}

}
