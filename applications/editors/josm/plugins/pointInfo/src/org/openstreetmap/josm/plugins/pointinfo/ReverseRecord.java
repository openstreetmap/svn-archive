// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo;

import javax.json.JsonObject;

/**
 * Class to contain Nominatim Reverse Geocoding data
 * @author Javier Sánchez Portero
 */
class ReverseRecord {

    private String countryCode;
    private String country;
    private String state;
    private String stateDistrict;
    private String county;
    private String city;
    private String town;
    private String village;
    private String cityDistrict;
    private String suburb;

    /**
     * Default constructor
     *
     */
    ReverseRecord() {
        init();
    }

    /**
     * Constructor from JSON
     * @param obj the Json object
     *
     */
    ReverseRecord(JsonObject obj) {
        init();
        JsonObject address = obj.getJsonObject("address");
        if (address != null) {
            countryCode = address.getString("country_code", null);
            country = address.getString("country", null);
            state = address.getString("state", null);
            stateDistrict = address.getString("state_district", null);
            county = address.getString("county", null);
            city = address.getString("city", null);
            town = address.getString("town", null);
            village = address.getString("village", null);
            cityDistrict = address.getString("city_district", null);
            suburb = address.getString("suburb", null);
        }
    }

    /**
     * Initialization
     *
     */
    private void init() {
        countryCode = null;
        country = null;
        state = null;
        stateDistrict = null;
        county = null;
        city = null;
        town = null;
        village = null;
        cityDistrict = null;
        suburb = null;
    }

    /**
     * Returns true if area is equals to any address value
     * @param area area to be checked
     * @return match area matched
     */
    public Boolean matchAnyArea(String area) {
        if (area.equals(countryCode)) return true;
        if (area.equals(country)) return true;
        if (area.equals(state)) return true;
        if (area.equals(stateDistrict)) return true;
        if (area.equals(county)) return true;
        if (area.equals(city)) return true;
        if (area.equals(town)) return true;
        if (area.equals(village)) return true;
        if (area.equals(cityDistrict)) return true;
        if (area.equals(suburb)) return true;
        return false;
    }
}
