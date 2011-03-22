package relcontext.actions;

import java.util.Collection;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import relcontext.ChosenRelation;

/**
 * Creates new multipolygon from selected ways.
 * Choose relation afterwards.
 *
 * @author Zverik
 */
public class CreateMultipolygonAction extends JosmAction {
    private static final String ACTION_NAME = "Create relation";
    protected ChosenRelation chRel;

    public CreateMultipolygonAction( ChosenRelation chRel ) {
        super("Multi", null, "Create a multipolygon from selected objects", null, false);
        this.chRel = chRel;
        updateEnabledState();
    }

    public CreateMultipolygonAction() {
        this(null);
    }

    public void actionPerformed( ActionEvent e ) {
        // todo!
        
        Relation rel = new Relation();
        rel.put("type", "multipolygon");
        for( OsmPrimitive selected : getCurrentDataSet().getSelected() ) {
            rel.addMember(new RelationMember("", selected));
        }

        Collection<Command> cmds = new LinkedList<Command>();
        Main.main.undoRedo.add(new AddCommand(rel));

        if( chRel != null ) {
            chRel.set(rel);
        }
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
        boolean enabled = true;
        if( selection == null || selection.isEmpty() )
            enabled = false;
        else {
            for( OsmPrimitive p : selection )
                if( !(p instanceof Way) ) {
                    enabled = false;
                    break;
                }
        }
        setEnabled(enabled);
    }
}
