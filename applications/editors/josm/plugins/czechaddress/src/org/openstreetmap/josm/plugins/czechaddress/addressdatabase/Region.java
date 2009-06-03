package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import java.util.ArrayList;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Region is an area within the country. It contains {@link ViToCi}s (villages,
 * towns, cities).
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class Region extends ElementWithStreets {
    
    private ArrayList<ViToCi> vitocis
            = new ArrayList<ViToCi>();
    
    /**
     * Adds a single municipality into this element.
     */
    public void addViToCi(ViToCi municipality) {
        municipality.setParent(this);
        vitocis.add(municipality);
    }
    
    /**
     * Replaces the list of municipalities of this element.
     */
    public void setViToCis(ArrayList<ViToCi> municipalities) {
        this.vitocis = municipalities;
        for (ViToCi obec : municipalities)
            obec.setParent(this);
    }
    
    /**
     * Returns the list of all municipalities in this region.
     */
    public ArrayList<ViToCi> getViToCis() {
        return vitocis;
    }

    public ViToCi findViToCi(String viToCiName) {

        if (viToCiName == null) return null;

        viToCiName = viToCiName.toUpperCase();

        for (ViToCi vitoci : vitocis)
            if (vitoci.getName().toUpperCase().equals(name))
                return vitoci;

        return null;
    }

    String nuts3name = null;
    String nuts4name = null;
    
    /**
     * Default constructor setting the name of this region.
     * @param name
     */
    public Region(String name) {
        super(name);
    }
    
    /**
     * Constructor which sets the region's name together with higher
     * administrative areas.
     */
    public Region(String name, String nuts3name, String nuts4name) {
        super(name);
        if (nuts3name != null) this.nuts3name = nuts3name;
        if (nuts4name != null) this.nuts4name = nuts4name;
    }
    
    public String getNuts3Name() {
        return nuts3name;
    }
    
    public String getNuts4Name() {
        return nuts4name;
    }
    
    /**
     * Returns the name of this region. If the NUTS3 name was entered,
     * its name is appended to the result.
     */
    @Override
    public String toString() {
        
        String thisString = name;
        
        if (nuts3name != null)
            thisString += " (kraj " + nuts3name + ")";
        
        return thisString;
    }
}
