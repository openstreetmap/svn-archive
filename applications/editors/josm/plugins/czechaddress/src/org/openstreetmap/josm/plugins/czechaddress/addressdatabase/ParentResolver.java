/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

/**
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class ParentResolver {

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
}
