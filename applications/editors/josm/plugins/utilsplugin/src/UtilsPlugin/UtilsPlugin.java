package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

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
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (oldFrame == null && newFrame != null) {
			Main.map.toolBarActions.addSeparator();
			Main.map.toolBarActions.add(new MergeNodesAction());
			Main.map.toolBarActions.add(new MergeNodeWayAction());
			Main.map.toolBarActions.add(new SimplifyWayAction());
		}
	}
}
