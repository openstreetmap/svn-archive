/*
 *      CommandLineAction.java
 *      
 *      Copyright 2010 Hind <foxhind@gmail.com>
 *      
 */
 
package CommandLine;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.Shortcut;

public class CommandLineAction extends JosmAction {
	private CommandLine parentPlugin;
	
	public CommandLineAction(CommandLine parentPlugin) {
		super(tr("Command line"), "blankmenu", tr("Set input focus to the command line."),
		Shortcut.registerShortcut("edit:simplifyArea", tr("Tool: {0}", tr("Command line")), KeyEvent.VK_ENTER, Shortcut.DIRECT), true);
		this.parentPlugin = parentPlugin;
	}

    public void actionPerformed(ActionEvent e) {
		parentPlugin.activate();
    }
}
