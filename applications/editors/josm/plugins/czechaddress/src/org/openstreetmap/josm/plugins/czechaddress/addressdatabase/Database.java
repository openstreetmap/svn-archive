package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import java.util.ArrayList;

/**
 * Stores the whole database of all regions, municipalities, suburbs, streets
 * and houses in the Czech republic. The database can be feeded via
 * XML and is capable of downloading it from www.mvcr.cz
 * 
 * @see AddressElement
 * @see DatabaseParser

 * @author Radomir Cernoch radomir.cernoch@gmail.com
 */
public class Database {

    private Database() {}
    private static Database singleton = null;
    public  static Database getInstance() {
        if (singleton == null)
            singleton = new Database();

        return singleton;
    }

    /**
     * List of regions, which are in this database.
     */
    public ArrayList<Region> regions = new ArrayList<Region>();

    public Region findRegion(String name, String nuts3, String nuts4) {

        if (name == null) return null;

        name = name.toUpperCase();
        if (nuts3 != null) nuts3 = nuts3.toUpperCase();
        if (nuts4 != null) nuts4 = nuts4.toUpperCase();

        for (Region region : regions) {

            if (!region.getName().toUpperCase().equals(name))
                continue;

            if ( region.getNuts3Name() != null &&
                !region.getNuts3Name().toUpperCase().equals(nuts3))
                continue;

            if ( region.getNuts4Name() != null &&
                !region.getNuts4Name().toUpperCase().equals(nuts3))
                continue;

            return region;
        }

        return null;
    }
}
