// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.namemanager.countryData;

import java.util.HashMap;
import java.util.Map;

public final class CountryDataMemory {

    private static Map<String, Country> countryCache;
    
    private CountryDataMemory() {
        // Hide default constructor for utilities classes
    }

    public static void instantiateCountryCache() {
        if (countryCache == null) {
            countryCache = new HashMap<>();
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
