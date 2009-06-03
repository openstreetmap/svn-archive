/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

/**
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class ParentResolver implements Comparable<ParentResolver> {

    public Street parentStreet = null;
    public Suburb parentSuburb = null;
    public ViToCi parentViToCi = null;
    public Region parentRegion = null;

    public ParentResolver(AddressElement queryElement) {

        if (queryElement.getParent() instanceof Street) {
            parentStreet = (Street) queryElement.getParent();
            queryElement = queryElement.getParent();
        }

        if (queryElement.getParent() instanceof Suburb) {
            parentSuburb = (Suburb) queryElement.getParent();
            queryElement = queryElement.getParent();
        }

        if (queryElement.getParent() instanceof ViToCi) {
            parentViToCi = (ViToCi) queryElement.getParent();
            queryElement = queryElement.getParent();
        }

        if (queryElement.getParent() instanceof Region) {
            parentRegion = (Region) queryElement.getParent();
            queryElement = queryElement.getParent();
        }
    }

    public String getIsIn() {
        String result = "";
        String last = "";

        if (parentSuburb != null && !last.equals(parentSuburb.getName())) {
            result += parentSuburb.getName() + ", ";
            last = parentSuburb.getName();
        }

        if (parentViToCi != null && !last.equals(parentViToCi.getName())) {
            result += parentViToCi.getName() + ", ";
            last = parentViToCi.getName();
        }

        if (parentRegion != null && parentRegion.getNuts3Name() != null &&
                                    !last.equals(parentRegion.getNuts3Name())) {
            result += parentRegion.getNuts3Name() + " kraj, ";
            last = parentRegion.getNuts3Name();
        }

        return result + "CZ";
    }

    public int compareTo(ParentResolver o) {
        int val = 0;

        if (parentRegion != null && o.parentRegion != null)
            val = parentRegion.compareTo(o.parentRegion);
        if (val != 0)
            return val;

        if (parentViToCi != null && o.parentViToCi != null)
            val = parentViToCi.compareTo(o.parentViToCi);
        if (val != 0)
            return val;

        if (parentSuburb != null && o.parentSuburb != null)
            val = parentSuburb.compareTo(o.parentSuburb);
        if (val != 0)
            return val;

        if (parentStreet != null && o.parentStreet != null)
            val = parentStreet.compareTo(o.parentStreet);
        return val;
    }
}
