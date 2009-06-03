package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Element consisting of a number of streets (apart from houses inherited
 * from {@link ElementWithHouse}).
 *
 * @author Radomir Cernoch radomir.cernoch@gmail.com
 */
public abstract class ElementWithStreets extends ElementWithHouses {

    private ArrayList<Street> streets = new ArrayList<Street>();

    public ElementWithStreets(String name) {
        super(name);
    }

    /**
     * Adds a new street into this element.
     */
    public void addStreet(Street streetToAdd) {
        //if (streetToAdd.getParent() instanceof ElementWithStreets)
        
        streetToAdd.setParent(this);
        streets.add(streetToAdd);
    }

    /**
     * Adds new streets into this element.
     */
    public void addStreets(Collection<Street> streetsToAdd) {
        streets.ensureCapacity(streets.size() + streetsToAdd.size() );
        for (Street streetToAdd : streetsToAdd)
            addStreet(streetToAdd);
    }

    /**
     * Replaces the internal list of streets with another one.
     */
    public void setStreets(ArrayList<Street> streets) {
        this.streets = streets;
        for (Street street : streets) {
            street.setParent(this);
        }
    }

    /**
     * Returns the list of streets.
     */
    public List<Street> getStreets() {
        return streets;
    }

    public List<Street> getAllStreets() {
        return getStreets();
    }


    public Street findStreet(String streetName) {

        if (streetName == null) return null;

        streetName = streetName.toUpperCase();

        for (Street street : streets)
            if (street.getName().toUpperCase().equals(streetName))
                return street;
        return null;
    }

    /**
     * Returns all houses belonging directly to this element together
     * with all houses from streets in this element.
     */
    public List<House> getAllHouses() {
        // We make an conservative estimate...
        List<House> result = new ArrayList<House>(20 * streets.size());
        
        result.addAll(this.houses);
        
        for (Street street : streets)
            result.addAll(street.getHouses());
        
        return result;
    }
}
