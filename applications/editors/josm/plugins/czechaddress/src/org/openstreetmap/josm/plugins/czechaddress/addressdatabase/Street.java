package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

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
        
        if (primitive.get("highway") == null)
            return result;
        
        result[0] = matchField(name, primitive.get("name"));

        if (primitive.get("name") != null) {
            String[] parts1 = primitive.get("name").split("\\.* +");
            String[] parts2 =                  name.split("\\.* +");
            for (String p : parts1)
                System.out.println("X: " + p);
            for (String p : parts2)
                System.out.println("Y: " + p);
        }

        return result;
    }
}
