package relcontext.relationfix;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Relation;

/**
 * Default fixer that does nothing - every relation is OK for this class.
 */
public class NothingFixer extends RelationFixer {

	public NothingFixer() {
		super("");
	}
	@Override
	public boolean isFixerApplicable(Relation rel) {
		return true;
	}
	@Override
	public boolean isRelationGood(Relation rel) {
		return true;
	}

	@Override
	public Command fixRelation(Relation rel) {
		return null;
	}

}
