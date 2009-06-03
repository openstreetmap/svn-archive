package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import java.util.List;
import java.util.ArrayList;

/**
 * ViToCi is either a village, town or a city.
 * 
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class ViToCi extends ElementWithStreets {

    private ArrayList<Suburb> suburbs = new ArrayList<Suburb>();

    /**
     * Default constructor setting the name of this element.
     */
    public ViToCi(String name) {
        super(name);
    }

    /**
     * Adds a new suburb to this municipality.
     */
    public void addSuburb(Suburb suburbToAdd) {
        suburbToAdd.setParent(this);
        suburbs.add(suburbToAdd);
    }

    /**
     * Adds new suburbs to this municipality.
     */
    public void addSuburbs(List<Suburb> suburbsToAdd) {
        for (Suburb suburbToAdd : suburbsToAdd)
            addSuburb(suburbToAdd);
    }

    /**
     * Returns the list of all suburbs of this municipality.
     */
    public ArrayList<Suburb> getSuburbs() {
        return suburbs;
    }

    public Suburb findSuburb(String suburbName) {

        if (suburbName == null) return null;

        suburbName = suburbName.toUpperCase();

        for (Suburb suburb : suburbs)
            if (suburb.getName().toUpperCase().equals(suburbName))
                return suburb;

        return null;
    }

    @Override
    public List<Street> getAllStreets() {
        List<Street> result = super.getAllStreets();
        for (Suburb suburb : suburbs)
            result.addAll(suburb.getAllStreets());
        return result;
    }

    @Override
    public List<House> getAllHouses() {
        List<House> result = super.getAllHouses();
        for (Suburb suburb : suburbs)
            result.addAll(suburb.getAllHouses());
        return result;
    }
}
