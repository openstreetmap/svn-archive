package relcontext;

import org.openstreetmap.josm.data.osm.Relation;

/**
 * Listener for {@link RelContextDialog}'s chosen relation field.
 *
 * @author Zverik
 */
public interface ChosenRelationListener {
	void chosenRelationChanged( Relation oldRelation, Relation newRelation );
}
