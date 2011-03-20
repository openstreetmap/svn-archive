package relcontext.actions;

import java.util.Collection;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Relation;
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
        super(tr(ACTION_NAME), "add_remove_member", "Add/remove member from the chosen relation",
                null, false);
        this.rel = rel;
    }

    public void actionPerformed( ActionEvent e ) {
        // todo
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null && getCurrentDataSet() != null);
    }

    @Override
    protected void updateEnabledState() {
        if( getCurrentDataSet() == null ) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState( Collection<? extends OsmPrimitive> selection ) {
        if( rel.get() == null || selection == null || selection.isEmpty() ) {
            setEnabled(false);
            return;
        }
        setEnabled(true);
        // todo: change icon based on selection
    }
}
