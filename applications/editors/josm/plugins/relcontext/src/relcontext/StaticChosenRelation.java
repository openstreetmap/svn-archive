package relcontext;

import org.openstreetmap.josm.data.osm.Relation;

/**
 * A chosen relation that is only a container for one relation and does not listen to anything.
 *
 * @author Zverik
 */
public class StaticChosenRelation extends ChosenRelation {

    public StaticChosenRelation( Relation rel ) {
        chosenRelation = rel;
        analyse();
    }

    @Override
    public void set( Relation rel ) {
        throw new UnsupportedOperationException("Changing static relation is not supported.");
    }
}
