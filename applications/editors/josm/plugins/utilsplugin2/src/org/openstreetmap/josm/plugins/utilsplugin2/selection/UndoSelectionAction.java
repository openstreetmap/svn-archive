// License: GPL. Copyright 2011 by Alexei Kasatkin and Martin Ždila
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
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
                KeyEvent.VK_Z, Shortcut.CTRL_SHIFT), true);
        putValue("help", ht("/Action/UndoSelection"));
    }

    private int myAutomaticSelectionHash;
    private Collection<OsmPrimitive> lastSel;
    private int index;
    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        LinkedList<Collection<? extends OsmPrimitive>> history = ds.getSelectionHistory();
        if (history==null || history.isEmpty()) return; // empty history
        int num=history.size();
        
        Collection<OsmPrimitive> selection = ds.getSelected();

        if (selection!= null &&  selection.hashCode() != myAutomaticSelectionHash) {
            // manual selection or another pluging selection noticed
            index=history.indexOf(lastSel);
            // first is selected, next list is previous selection
        }
        int k=0;
        Set<OsmPrimitive> newsel = new HashSet<>();
        do {
            if (index+1<history.size()) index++; else index=0;
            Collection<? extends OsmPrimitive> histsel = history.get(index);
            // remove deleted entities from selection
            newsel.clear();
            newsel.addAll(histsel);
            newsel.retainAll(ds.allNonDeletedPrimitives());
            if (newsel.size() > 0 ) break;
            k++;
        } while ( k < num );

        ds.setSelected(newsel);
        lastSel = ds.getSelected();
        myAutomaticSelectionHash = lastSel.hashCode();
        // remeber last automatic selection
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(true);
    }
}
