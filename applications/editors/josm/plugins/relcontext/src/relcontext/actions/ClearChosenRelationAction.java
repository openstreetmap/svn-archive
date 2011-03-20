package relcontext.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openstreetmap.josm.data.osm.Relation;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class ClearChosenRelationAction extends AbstractAction implements ChosenRelationListener {

	private ChosenRelation rel;

	public ClearChosenRelationAction( ChosenRelation rel ) {
		this.rel = rel;
	}

	public void actionPerformed( ActionEvent e ) {
		rel.clear();
	}

	public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
		setEnabled(newRelation != null);
	}
}
