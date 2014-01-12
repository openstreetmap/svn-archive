package org.openstreetmap.josm.plugins.czechaddress.proposal;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Proposal for removing a key-value pair.
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class RemoveKeyProposal extends Proposal {

    String key;

    /**
     * Default constructor, which stores the 'key' name to be removed.
     * @param key the name of the key to be removed
     */
    public RemoveKeyProposal(String key) {
        this.key = key;
    }

    /**
     * Removes the key from the specified primitive.
     * @param op primitive, from which the key will be removed
     */
    @Override
    public void apply(OsmPrimitive op) {
        super.apply(op);
        op.remove(key);
    }

    /**
     * Textual representation of this proposal.
     *
     * Currently the string is in Czech language (see {@link CzechAddressPlugin}).
     */
    @Override
    public String toString() {
        return "Odstranit atribut '" + key + "'";
    }


}
