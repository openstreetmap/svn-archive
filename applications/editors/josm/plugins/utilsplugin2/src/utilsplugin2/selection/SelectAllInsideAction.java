// License: GPL. Copyright 2011 by Alexei Kasatkin
package utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Extends current selection by selecting nodes on all touched ways
 */
public class SelectAllInsideAction extends JosmAction {

    public SelectAllInsideAction() {
        super(tr("All inside [testing]"), "selinside", tr("Select all inside selected polygons"),
                Shortcut.registerShortcut("tools:selinside", tr("Tool: {0}","All inside"),
                KeyEvent.VK_I, Shortcut.ALT_SHIFT), true);
        putValue("help", ht("/Action/SelectAllInside"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        long t=System.currentTimeMillis();
        Collection<OsmPrimitive> insideSelected = NodeWayUtils.selectAllInside(getCurrentDataSet().getSelected(), getCurrentDataSet());
        
        if (!insideSelected.isEmpty()) {
            getCurrentDataSet().addSelected(insideSelected);
        } else{
        JOptionPane.showMessageDialog(Main.parent,
               tr("Nothing found. Please select some closed ways or multipolygons to find all primitives inside them!"),
               tr("Warning"), JOptionPane.WARNING_MESSAGE);
        }

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
        if (selection == null) {
            setEnabled(false);
            return;
        }
        setEnabled(!selection.isEmpty());
    }


}
