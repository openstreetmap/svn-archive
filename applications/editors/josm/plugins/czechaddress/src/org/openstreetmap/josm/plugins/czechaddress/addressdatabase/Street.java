package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import java.util.List;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.NotNullList;
import org.openstreetmap.josm.plugins.czechaddress.PrimUtils;
import org.openstreetmap.josm.plugins.czechaddress.proposal.Proposal;

import static org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalFactory.getStringFieldDiff;

/**
 * Street is a member of {@link ElementWithStreets}
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class Street extends ElementWithHouses  {

    public Street(String name) {
        super(name);
    }

    @Override
    public void setParent(AddressElement parent) {
        assert parent instanceof ElementWithStreets;
        super.setParent(parent);
    }
    
    @Override
    public ElementWithStreets getParent() {
        assert parent instanceof ElementWithStreets;
        return (ElementWithStreets) parent;
    }

    @Override
    protected int[] getFieldMatchList(OsmPrimitive primitive) {
        int[] result = {0};
        if (!isMatchable(primitive)) return result;
        
        result[0] = matchFieldAbbrev(name, primitive.get("name"));
        return result;
    }

    @Override
    public List<Proposal> getDiff(OsmPrimitive prim) {
        List<Proposal> props = new NotNullList<Proposal>();
        
        props.add(getStringFieldDiff(PrimUtils.KEY_NAME, prim.get(PrimUtils.KEY_NAME), getName()));
        return props;
    }

    public static boolean isMatchable(OsmPrimitive prim) {
        return (prim.get(PrimUtils.KEY_HIGHWAY) != null);
    }

}
