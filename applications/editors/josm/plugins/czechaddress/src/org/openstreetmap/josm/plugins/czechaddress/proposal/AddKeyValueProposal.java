package org.openstreetmap.josm.plugins.czechaddress.proposal;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Proposal for adding a key-value attribute.
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class AddKeyValueProposal extends Proposal {

    String key;
    String val;

    /**
     * Default constructor setting the internal key-value pair.
     * @param key key of the new attribute
     * @param val value of the new attribute
     */
    public AddKeyValueProposal(String key, String val) {
        this.key = key;
        this.val = val;
    }

    /**
     * Adds the internally stored key-value pair to
     * a given {@link OsmPrimitive}.
     */
    @Override
    public void apply(OsmPrimitive op) {
        super.apply(op);
        op.put(key, val);
    }

    /**
     * Provides textual representation of this modification.
     *
     * Currently the string is in Czech language (see {@link CzechAddressPlugin}).
     */
    @Override
    public String toString() {
        return "Přidat '" + key + "=" + val + "'";
    }

}
