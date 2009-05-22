package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Element, which consists of a number of houses.
 *
 * @author Radomir Cernoch radomir.cernoch@gmail.com
 */
abstract public class ElementWithHouses extends AddressElement {

    /**
     * Default constructor setting the name of this element.
     */
    public ElementWithHouses(String name) {
        super(name);
    }

    protected List<House> houses = new ArrayList<House>();

    /**
     * Inserts a new house into this element.
     */
    public void addHouse(House houseToAdd) {
        houses.add(houseToAdd);
        houseToAdd.setParent(this);
    }

    /**
     * Inserts houses into this element.
     */
    public void addHouses(List<House> housesToAdd) {
        for (House houseToAdd : housesToAdd)
            addHouse(houseToAdd);
    }

    /**
     * Replaces the internal list of houses by a new one.
     */
    public void setHouses(List<House> houses) {
        this.houses = houses;
        for (House house : this.houses)
            house.setParent(this);
    }

    /**
     * Returns all houses directly contained in this element.
     * 
     * NOTICE: If a subclass element contains other data structured capable
     * of storing houses, they are not included in the returned set.
     *
     * Eg. {@code ElementWithStreets.getHouses()} returns only such houses, which
     * do not belong to any street.
     */
    public List<House> getHouses() {
        return houses;
    }



}
