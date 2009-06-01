package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.proposal.Proposal;

/**
 * Match is a relation between {@link OsmPrimitive} and {@link AddressElement}.
 *
 * @author Radomír Cernoch radomir.cernoch@gmail.com
 */
public class Match {
    
    public static final int MATCH_OVERWRITE = 4;
    public static final int MATCH_ROCKSOLID = 3;
    public static final int MATCH_PARTIAL   = 2;
    public static final int MATCH_CONFLICT  = 1;
    public static final int MATCH_NOMATCH   = 0;
    
    public int qVal;
    public OsmPrimitive prim;
    public AddressElement elem;

    public String checkSum;
    public static Logger logger = Logger.getLogger("Match");

    public Match(AddressElement element, OsmPrimitive primitive,
                 int qualityFactor) {
        
        assert primitive != null;
        assert element   != null;
        assert !primitive.deleted;

        prim = primitive;
        elem = element;
        qVal = qualityFactor;
    }

    public List<Proposal> getDiff() {
        return this.elem.getDiff(this.prim);
    }

    public static Match createMatch(AddressElement element,
                                    OsmPrimitive primitive) {

        int quality = element.getMatchQuality(primitive);

        logger.log(Level.FINE, "asked to create match",
                               new Object[] {element, primitive, quality});

        if (quality > MATCH_NOMATCH)
            return new Match(element, primitive, quality);

        return null;
    }
/*
    public boolean setQ(int desiredQ) {
        int oldQ = evalQ();
        qVal = desiredQ;
        int newQ = evalQ();

        if (oldQ != newQ)
            logger.log(Level.INFO, "changing match value: " + oldQ + " --> " + newQ
                                 + " (desired value is " + desiredQ + ")", this);
        return oldQ != evalQ();
    }
*/
    public static int evalQ(OsmPrimitive prim, AddressElement elem, Integer oldQ) {
        
        if (prim.deleted)
            return MATCH_NOMATCH;

        if (oldQ == MATCH_OVERWRITE)
            return MATCH_OVERWRITE;

        return elem.getMatchQuality(prim);
    }

    @Override
    public String toString() {
        return "{Match: q=" + String.valueOf(qVal)
                   + "; elem='" + elem.toString()
                   +"'; prim='" + AddressElement.getName(prim) + "'}";
    }
}
