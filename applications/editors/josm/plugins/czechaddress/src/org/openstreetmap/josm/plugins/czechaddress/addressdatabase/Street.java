package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

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



    /*int[] getFieldMatchList(OsmPrimitive primitive) {
        int[] result = {0};
        
        if (primitive.get("highway") == null)
            return result;
        
        result[0] = matchField(name, primitive.get("name"));
        
        return result;
    }*/
}
