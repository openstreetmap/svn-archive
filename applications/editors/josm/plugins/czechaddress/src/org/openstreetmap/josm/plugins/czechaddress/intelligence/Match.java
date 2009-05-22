package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import java.util.List;
import org.openstreetmap.josm.data.osm.Node;
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
    
    public int quality;
    public OsmPrimitive prim;
    public AddressElement elem;

    public String checkSum;

    public Match(AddressElement element, OsmPrimitive primitive,
                 int qualityFactor) {
        
        assert primitive != null;
        assert element != null;
        assert qualityFactor <= MATCH_OVERWRITE;
        assert qualityFactor > MATCH_NOMATCH
             : "MATCH_NOMATCH represents no relation."
             + "It's pointless to waste memory for it.";

        assert !primitive.deleted;

        prim = primitive;
        elem = element;
        quality = qualityFactor;

        checkSum = toString();
    }

    public List<Proposal> getDiff() {
        return this.elem.getDiff(this.prim);
    }

    public static Match createMatch(AddressElement element,
                                    OsmPrimitive primitive) {

        int quality = element.getMatchQuality(primitive);
        if (quality > MATCH_NOMATCH)
            return new Match(element, primitive, quality);

        return null;
    }

    public boolean qualityChanged() {
        int newQuality
        = prim.deleted ? MATCH_NOMATCH
                       : quality == MATCH_OVERWRITE ? MATCH_OVERWRITE
                                                    : elem.getMatchQuality(prim);
        if (prim instanceof Node) {
            Node node = (Node) prim;
            if (node.coor == null || node.eastNorth == null) {
                newQuality = MATCH_NOMATCH;
                System.out.println("Suspicious node, ignoring it: " + AddressElement.getName(node));
            }
        }
            
        
        boolean difference = newQuality != quality;
        quality = newQuality;

        return difference;
    }

    @Override
    public String toString() {
        return "{Match: q=" + String.valueOf(quality)
                   + "; elem='" + elem.toString()
                   +"'; prim='" + AddressElement.getName(prim) + "'}";
    }
}
