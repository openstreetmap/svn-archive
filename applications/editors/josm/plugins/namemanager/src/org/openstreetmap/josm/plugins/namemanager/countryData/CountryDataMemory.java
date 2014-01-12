package org.openstreetmap.josm.plugins.namemanager.countryData;

import java.util.HashMap;
import java.util.Map;

public class CountryDataMemory {

    private static Map<String, Country> countryCache;

    public static void instantiateCountryCache() {
        if (countryCache == null) {
            countryCache = new HashMap<String, Country>();
        }
    }

    public static void addCountry(Country country) {
        if (country != null) {
            countryCache.put(country.getCountryName(), country);
        }
    }

    public static void clearCache() {
        if (countryCache != null) {
            countryCache.clear();
        }
    }

    public static boolean isEmpty() {
        if (countryCache != null) {
            return countryCache.isEmpty();
        }
        return true;
    }

    public static Map<String, Country> getCountryCache() {
        return countryCache;
    }

}
