// License: GPL. Copyright 2011 by Alexei Kasatkin and Martin Ždila
package utilsplugin2.selection;

import org.openstreetmap.josm.command.Command;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.*;

import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Unselects all nodes
 */
public class SelectModWaysAction extends JosmAction {
    private int lastHash;
    private Command lastCmd;

    public SelectModWaysAction() {
        super(tr("Select last modified ways"), "selmodways",
                tr("Select last modified ways"),
                Shortcut.registerShortcut("tools:selmodways", tr("Tool: {0}","Select last modified ways"),
                KeyEvent.VK_Z,  Shortcut.GROUP_EDIT, KeyEvent.ALT_MASK ), true);
        putValue("help", ht("/Action/SelectLastModifiedWays"));
    }

    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        Set<Node> selectedNodes = OsmPrimitive.getFilteredSet(selection, Node.class);
        getCurrentDataSet().clearSelection(selectedNodes);
        Command cmd =null;

        if (Main.main.undoRedo.commands == null) return;
        int num=Main.main.undoRedo.commands.size();
        if (num==0) return;
        int k=0,idx;
        if (selection!=null && !selection.isEmpty() && selection.hashCode() == lastHash) {
            // we are selecting next command in history if nothing is selected
            idx = Main.main.undoRedo.commands.indexOf(lastCmd);
           // System.out.println("My previous selection found "+idx);
        } else {
            idx=num;
           // System.out.println("last history item taken");
        }

        Set<Way> ways = new HashSet<Way>(10);
        do {  //  select next history element
            if (idx>0) idx--; else idx=num-1;
            cmd = Main.main.undoRedo.commands.get(idx);
            Collection<? extends OsmPrimitive> pp = cmd.getParticipatingPrimitives();
            ways.clear();
            for ( OsmPrimitive p : pp) {  // find all affected ways
                if (p instanceof Way && !p.isDeleted()) ways.add((Way)p);
            }
            if (!ways.isEmpty() && !getCurrentDataSet().getSelected().containsAll(ways)) {
                getCurrentDataSet().setSelected(ways);
                lastCmd = cmd; // remember last used command and last selection
                lastHash = getCurrentDataSet().getSelected().hashCode();
                return;
                }
            k++;
        } while ( k < num ); // try to find previous command if this affects nothing
        lastCmd=null; lastHash=0;
    }

    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(true);
    }
}
