package org.openstreetmap.josm.plugins.czechaddress.proposal;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Proposal changing key-value pair to different pair.
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class KeyValueChangeProposal extends Proposal {

    String oldKey;
    String oldVal;
    String newKey;
    String newVal;

    public KeyValueChangeProposal(String oldKey, String oldVal,
                                     String newKey, String newVal) {
        this.oldKey = oldKey;
        this.oldVal = oldVal;
        this.newKey = newKey;
        this.newVal = newVal;
    }

    @Override
    public void apply(OsmPrimitive op) {
        super.apply(op);
        op.put(newKey, newVal);
    }

    @Override
    public String toString() {
        if (oldKey.equals(newKey))
            return "Hodnotu '" + oldKey + "=" + oldVal + "' změnit na '" + newVal + "'";
        else
            return "Nahradit '" + oldKey + "=" + oldVal + "' za '" + newKey + "=" + newVal + "'";
    }


}
