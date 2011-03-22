package relcontext.actions;

import java.util.*;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
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
        super("±", null, "Add/remove member from the chosen relation", null, false);
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
        String name = "";
        if( getCurrentDataSet() == null || getCurrentDataSet().getSelected() == null
                || getCurrentDataSet().getSelected().size() == 0 || rel == null || rel.get() == null )
            name = "?";
        else {
            Collection<OsmPrimitive> toAdd = new ArrayList<OsmPrimitive>(getCurrentDataSet().getSelected());
            toAdd.remove(rel.get());
            int selectedSize = toAdd.size();
            if( selectedSize == 0 )
                name = "?";
            else {
                toAdd.removeAll(rel.get().getMemberPrimitives());
                if( toAdd.isEmpty() )
                    name = "-";
                else if( toAdd.size() < selectedSize )
                    name = "±";
                else
                    name = "+";
            }
        }
        putValue(Action.NAME, name);
    }
}
