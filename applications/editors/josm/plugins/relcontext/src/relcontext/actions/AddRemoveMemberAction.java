package relcontext.actions;

import java.util.*;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.tools.ImageProvider;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

/**
 * An action to add or remove (or both) member(s) from the chosen relation.
 * In time should be able to determine correct position for new members.
 * Also, there should be some support for entering a role for new members.
 *
 * @author Zverik
 */
public class AddRemoveMemberAction extends JosmAction implements ChosenRelationListener {
    private static final String ACTION_NAME = "Add/remove member";
    private ChosenRelation rel;

    public AddRemoveMemberAction( ChosenRelation rel ) {
        super(null, "relcontext/addremove", tr("Add/remove members from the chosen relation"), null, false);
        this.rel = rel;
        rel.addChosenRelationListener(this);
        updateEnabledState();
    }

    public void actionPerformed( ActionEvent e ) {
        if( rel.get() == null )
            return;

        Relation r = new Relation(rel.get());

        Collection<OsmPrimitive> toAdd = new ArrayList<OsmPrimitive>(getCurrentDataSet().getSelected());
        toAdd.remove(rel.get());
        toAdd.removeAll(r.getMemberPrimitives());

        // 1. remove all present members
        r.removeMembersFor(getCurrentDataSet().getSelected());

        // 2. add all new members
        for( OsmPrimitive p : toAdd ) {
            r.addMember(new RelationMember("", p));
        }

        if( !r.getMemberPrimitives().equals(rel.get().getMemberPrimitives()) )
            Main.main.undoRedo.add(new ChangeCommand(rel.get(), r));
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        updateEnabledState();
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledState(getCurrentDataSet() == null ? null : getCurrentDataSet().getSelected());
    }

    @Override
    protected void updateEnabledState( Collection<? extends OsmPrimitive> selection ) {
        updateIcon();
        if( rel == null || rel.get() == null || selection == null || selection.isEmpty() ) {
            setEnabled(false);
            return;
        }
        if( selection.size() == 1 && selection.contains(rel.get()) ) {
            setEnabled(false);
            return;
        }
        setEnabled(true);
    }

    protected void updateIcon() {
        // todo: change icon based on selection
        int state = 0; // 0=unknown, 1=add, 2=remove, 3=both
        if( getCurrentDataSet() == null || getCurrentDataSet().getSelected() == null
                || getCurrentDataSet().getSelected().isEmpty() || rel == null || rel.get() == null )
            state = 0;
        else {
            Collection<OsmPrimitive> toAdd = new ArrayList<OsmPrimitive>(getCurrentDataSet().getSelected());
            toAdd.remove(rel.get());
            int selectedSize = toAdd.size();
            if( selectedSize == 0 )
                state = 0;
            else {
                toAdd.removeAll(rel.get().getMemberPrimitives());
                if( toAdd.isEmpty() )
                    state = 2;
                else if( toAdd.size() < selectedSize )
                    state = 3;
                else
                    state = 1;
            }
        }
//        String name = state == 0 ? "?" : state == 1 ? "+" : state == 2 ? "-" : "±";
//        putValue(Action.NAME, name);
        if( state == 0 ) {
//            putValue(NAME, "?");
            putValue(SMALL_ICON, ImageProvider.get("relcontext", "addremove"));
        } else {
            String iconName = state == 1 ? "add" : state == 2 ? "remove" : "addremove";
            putValue(NAME, null);
            putValue(SMALL_ICON, ImageProvider.get("relcontext", iconName));
        }
    }
}
