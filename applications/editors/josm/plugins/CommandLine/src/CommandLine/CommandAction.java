/*
 *      CommandAction.java
 *
 *      Copyright 2011 Hind <foxhind@gmail.com>
 *
 */

package CommandLine;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

public class CommandAction extends JosmAction {
	private CommandLine parentPlugin;
	private Command parentCommand;
	public CommandAction(Command parentCommand, CommandLine parentPlugin) {
		super(tr(parentCommand.name), "blankmenu", tr(parentCommand.name), null, true, parentCommand.name, true);
		if (!parentCommand.icon.equals("")) {
			try {
				putValue(Action.SMALL_ICON, ImageProvider.get(parentPlugin.pluginDir, parentCommand.icon));
				putValue(Action.LARGE_ICON_KEY, ImageProvider.get(parentPlugin.pluginDir, parentCommand.icon));
			}
			catch (NullPointerException e) {
				putValue(Action.SMALL_ICON, ImageProvider.get("blankmenu"));
				putValue(Action.LARGE_ICON_KEY, ImageProvider.get("blankmenu"));
			}
		}

		this.parentCommand = parentCommand;
		this.parentPlugin = parentPlugin;
	}

	public void actionPerformed(ActionEvent e) {
		parentPlugin.startCommand(parentCommand);
		parentPlugin.history.addItem(parentCommand.name);
	}
}
