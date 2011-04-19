package dumbutils;

import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.RelationData;
import org.openstreetmap.josm.data.osm.PrimitiveData;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.*;
import java.util.*;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.tools.Shortcut;
import java.awt.event.ActionEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Pastes relation membership from objects in the paste buffer onto selected object(s).
 *
 * @author Zverik
 */
class PasteRelationsAction extends JosmAction {
    private static final String TITLE = "Paste relations";

    public PasteRelationsAction() {
        super(tr(TITLE), "pasterelations", tr("Paste relation membership from objects in the buffer onto selected object(s)"),
                Shortcut.registerShortcut("tools:pasterelations", tr(TITLE), KeyEvent.VK_V, Shortcut.GROUP_MENU, Shortcut.SHIFT_DEFAULT), true);
    }

    public void actionPerformed( ActionEvent e ) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        if( selection.isEmpty() )
            return;

        Map<Relation, String> relations = new HashMap<Relation, String>();
        for( PrimitiveData pdata : Main.pasteBuffer.getDirectlyAdded() ) {
            OsmPrimitive p = getCurrentDataSet().getPrimitiveById(pdata.getId(), pdata.getType());
            for( Relation r : OsmPrimitive.getFilteredList(p.getReferrers(), Relation.class)) {
                String role = relations.get(r);
                for( RelationMember m : r.getMembers() ) {
                    if( m.getMember().equals(p) ) {
                        String newRole = m.getRole();
                        if( newRole != null && role == null )
                            role = newRole;
                        else if( newRole != null ? !newRole.equals(role) : role != null ) {
                            role = "";
                            break;
                        }
                    }
                }
                relations.put(r, role);
            }
        }

        List<Command> commands = new ArrayList<Command>();
        for( Relation rel : relations.keySet() ) {
            Relation r = new Relation(rel);
            boolean changed = false;
            for( OsmPrimitive p : selection ) {
                if( !r.getMemberPrimitives().contains(p) ) {
                    r.addMember(new RelationMember(relations.get(rel), p));
                    changed = true;
                }
            }
            if( changed )
                commands.add(new ChangeCommand(rel, r));
        }

        if( !commands.isEmpty() )
            Main.main.undoRedo.add(new SequenceCommand(tr(TITLE), commands));
    }

    @Override
    protected void updateEnabledState() {
        if( getCurrentDataSet() == null ) {
            setEnabled(false);
        }  else
            updateEnabledState(getCurrentDataSet().getSelected());
    }

    @Override
    protected void updateEnabledState( Collection<? extends OsmPrimitive> selection ) {
        setEnabled(selection != null && !selection.isEmpty() );
    }
}
