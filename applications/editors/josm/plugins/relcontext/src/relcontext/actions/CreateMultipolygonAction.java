package relcontext.actions;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.MultipolygonCreate;
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
        // for now, just copying standard action
        MultipolygonCreate mpc = new MultipolygonCreate();
        String error = mpc.makeFromWays(getCurrentDataSet().getSelectedWays());
        if( error != null ) {
            JOptionPane.showMessageDialog(Main.parent, error);
            return;
        }
        Relation rel = new Relation();
        rel.put("type", "multipolygon");
        for( MultipolygonCreate.JoinedPolygon poly : mpc.outerWays )
            for( Way w : poly.ways )
                rel.addMember(new RelationMember("outer", w));
        for( MultipolygonCreate.JoinedPolygon poly : mpc.innerWays )
            for( Way w : poly.ways )
                rel.addMember(new RelationMember("inner", w));
        List<Command> list = removeTagsFromInnerWays(rel);
        list.add(new AddCommand(rel));
        Main.main.undoRedo.add(new SequenceCommand(tr("Create multipolygon"), list));
        
        if( chRel != null )
            chRel.set(rel);
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

    /**
     * This method removes tags/value pairs from inner ways that are present in relation or outer ways.
     * It was copypasted from the standard {@link org.openstreetmap.josm.actions.CreateMultipolygonAction}.
     * Todo: rewrite it.
     */
    private List<Command> removeTagsFromInnerWays(Relation relation) {
        Map<String, String> values = new HashMap<String, String>();

        if (relation.hasKeys()){
            for(String key: relation.keySet()) {
                values.put(key, relation.get(key));
            }
        }

        List<Way> innerWays = new ArrayList<Way>();

        for (RelationMember m: relation.getMembers()) {

            if (m.hasRole() && m.getRole() == "inner" && m.isWay() && m.getWay().hasKeys()) {
                innerWays.add(m.getWay());
            }

            if (m.hasRole() && m.getRole() == "outer" && m.isWay() && m.getWay().hasKeys()) {
                Way way = m.getWay();
                for (String key: way.keySet()) {
                    if (!values.containsKey(key)) { //relation values take precedence
                        values.put(key, way.get(key));
                    }
                }
            }
        }

        List<Command> commands = new ArrayList<Command>();

        for(String key: values.keySet()) {
            List<OsmPrimitive> affectedWays = new ArrayList<OsmPrimitive>();
            String value = values.get(key);

            for (Way way: innerWays) {
                if (value.equals(way.get(key))) {
                    affectedWays.add(way);
                }
            }

            if (affectedWays.size() > 0) {
                commands.add(new ChangePropertyCommand(affectedWays, key, null));
            }
        }

        return commands;
    }
}
