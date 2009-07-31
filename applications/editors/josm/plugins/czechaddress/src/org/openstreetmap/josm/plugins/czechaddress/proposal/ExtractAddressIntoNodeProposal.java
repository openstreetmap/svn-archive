package org.openstreetmap.josm.plugins.czechaddress.proposal;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;

/**
 * Extracts address from a {@link Way} and creates
 * a standalone {@link Node} with the address information.
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class ExtractAddressIntoNodeProposal extends Proposal {

    /**
     * Tells whether this proposal is applicable to the given primitive.
     *
     * The primitive must be a {@link Way} and it must contain at least
     * one tag starting with 'addr:'. Then {@code true} is returned.
     * Otherwise false.
     *
     * @param primitive tested primitive
     * @return true if this proposal is applicable
     */
    static public boolean isApplicable(OsmPrimitive primitive) {
        if (!(primitive instanceof Way))
            return false;

        for (String key : primitive.keySet())
            if (key.startsWith("addr:"))
                return true;

        return false;
    }

    /**
     * Extracts the address information from the given primitive into
     * a newly created {@link Node}. The new node is added into the
     * JOSM database.
     *
     * If the extraction in not applicable, nothing happens.
     *
     * @param primitive the {@link Way} from which the address will be extracted
     */
    @Override
    public void apply(OsmPrimitive primitive) {

        if (!isApplicable(primitive))
            return;

        Way way = (Way) primitive;

        BoundingXYVisitor visitor = new BoundingXYVisitor();
        way.visit(visitor);

        Node addrNode = new Node(visitor.getBounds().getCenter());

        for (String key : way.keySet())
            if (key.startsWith("addr"))
                addrNode.put(key, way.get(key));

        for (String key : addrNode.keySet())
           way.remove(key);

        Main.ds.addPrimitive(addrNode);
    }

    /**
     * Returns textual representation of this proposal.
     *
     * Currently the string is in Czech language (see {@link CzechAddressPlugin}).
     */
    @Override
    public String toString() {
        return "Vytvořit z budovy samostatný adresní uzel.";
    }



}
