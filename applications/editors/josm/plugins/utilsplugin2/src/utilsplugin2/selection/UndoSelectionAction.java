// License: GPL. Copyright 2011 by Alexei Kasatkin and Martin Ždila
package utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.*;

import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Use selection istory to restore previous selection
 */
public class UndoSelectionAction extends JosmAction {
    
    public UndoSelectionAction() {
        super(tr("Undo selection"), "undoselection",
                tr("Reselect last added object or selection form history"),
                Shortcut.registerShortcut("tools:undoselection", tr("Tool: {0}","Undo selection"),
                KeyEvent.VK_Z, Shortcut.GROUPS_ALT1+Shortcut.GROUP_MENU), true);
        putValue("help", ht("/Action/UndoSelection"));
    }

    private int myAutomaticSelectionHash;
    private Collection<OsmPrimitive> lastSel;
    private int index;
    public void actionPerformed(ActionEvent e) {
        LinkedList<Collection<? extends OsmPrimitive>>history
                    = getCurrentDataSet().getSelectionHistory();
        int num=history.size();
        if (history==null || num==0) return; // empty history
        
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();


        if (selection!= null &&  selection.hashCode() != myAutomaticSelectionHash) {
            // manual selection or another pluging selection noticed
            index=history.indexOf(lastSel);
            // first is selected, next list is previous selection
        }
        int k=0;
        Set<OsmPrimitive> newsel = new HashSet<OsmPrimitive>();
        do {
            if (index+1<history.size()) index++; else index=0;
            Collection<? extends OsmPrimitive> histsel = history.get(index);
            // remove deleted entities from selection
            newsel.clear();
            newsel.addAll(histsel);
            newsel.retainAll(getCurrentDataSet().allNonDeletedPrimitives());
            if (newsel.size() > 0 ) break;
            k++;
        } while ( k < num );

        getCurrentDataSet().setSelected(newsel);
        lastSel = getCurrentDataSet().getSelected();
        myAutomaticSelectionHash = lastSel.hashCode();
        // remeber last automatic selection
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
