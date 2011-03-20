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
import relcontext.ChosenRelation;

/**
 * Simple create relation with no tags and all selected objects in it with no roles.
 * Choose relation afterwards.
 *
 * @author Zverik
 */
public class CreateRelationAction extends JosmAction {

	private static final String ACTION_NAME = "Create relation";
	protected ChosenRelation chRel;

	public CreateRelationAction( ChosenRelation chRel ) {
		super(tr(ACTION_NAME), "create_relation", "Create a relation from selected objects",
				null, false);
		this.chRel = chRel;
	}

	public CreateRelationAction() {
		this(null);
	}

	public void actionPerformed( ActionEvent e ) {
		// todo: ask user for relation type
		String type = "";

		Relation rel = new Relation();
		if( type != null && type.length() > 0 )
			rel.put("type", type);
		for( OsmPrimitive selected : getCurrentDataSet().getSelected() ) {
			rel.addMember(new RelationMember("", selected));
		}

		Collection<Command> cmds = new LinkedList<Command>();
		Main.main.undoRedo.add(new AddCommand(rel));

		if( chRel != null)
			chRel.set(rel);
	}

	@Override
	protected void updateEnabledState() {
		if( getCurrentDataSet() == null )
			setEnabled(false);
		else
			updateEnabledState(getCurrentDataSet().getSelected());
	}

	@Override
	protected void updateEnabledState( Collection<? extends OsmPrimitive> selection ) {
		setEnabled(selection != null && !selection.isEmpty());
	}
}
