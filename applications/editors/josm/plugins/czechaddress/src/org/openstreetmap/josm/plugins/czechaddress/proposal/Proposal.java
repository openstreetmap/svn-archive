package org.openstreetmap.josm.plugins.czechaddress.proposal;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Represents a single modification of an {@link OsmPrimitive}.
 *
 * This class is intended to be subclassed to provide standard modifications
 * such as "adding a key-value pair" {@link AddKeyValueAlternation}.
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 * @see ProposalContainer
 * @see AddKeyValueProposal
 * @see RemoveKeyProposal
 * @see KeyValueChangeProposal
 * @see ExtractAddressIntoNodeProposal
 */
abstract public class Proposal {

    /**
     * Applies the stored modification to the given {@link OsmPrimitive}.
     * @param op the primitive to be altered
     */
    public void apply(OsmPrimitive op) {
        op.setModified(true);
    }

    /**
     * Provides textual representation of this modification.
     *
     * @return the string representation
     */
    @Override
    abstract public String toString();

}
