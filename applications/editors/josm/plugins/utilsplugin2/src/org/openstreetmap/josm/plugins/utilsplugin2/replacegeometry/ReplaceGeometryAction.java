package org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.Notification;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Replaces already existing object (id>0) with a new object (id<0).
 *
 * @author Zverik
 */
public class ReplaceGeometryAction extends JosmAction {
    private static final String TITLE = tr("Replace Geometry");

    public ReplaceGeometryAction() {
        super(TITLE, "dumbutils/replacegeometry", tr("Replace geometry of selected object with a new one"),
                Shortcut.registerShortcut("tools:replacegeometry", tr("Tool: {0}", tr("Replace Geometry")), KeyEvent.VK_G, Shortcut.CTRL_SHIFT)
                , true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (getLayerManager().getEditDataSet() == null) {
            return;
        }

        // There must be two ways selected: one with id > 0 and one new.
        List<OsmPrimitive> selection = new ArrayList<>(getLayerManager().getEditDataSet().getSelected());
        if (selection.size() != 2) {
            new Notification(
                    tr("This tool replaces geometry of one object with another, and so requires exactly two objects to be selected.")
                ).setIcon(JOptionPane.WARNING_MESSAGE).show();  
            return;
        }

        OsmPrimitive firstObject = selection.get(0);
        OsmPrimitive secondObject = selection.get(1);
        
        try {
            ReplaceGeometryCommand replaceCommand =
                    ReplaceGeometryUtils.buildReplaceWithNewCommand(firstObject, secondObject);
            
            // action was canceled
            if (replaceCommand == null)
                return;
            
            Main.main.undoRedo.add(replaceCommand);
        } catch (IllegalArgumentException ex) {
            new Notification(
                ex.getMessage()
            ).setIcon(JOptionPane.WARNING_MESSAGE).show(); 
        } catch (ReplaceGeometryException ex) {
            new Notification(
                ex.getMessage()
            ).setIcon(JOptionPane.WARNING_MESSAGE).show(); 
        }
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState( Collection<? extends OsmPrimitive> selection ) {
        setEnabled(selection != null && selection.size() >= 2 );
    }
}
