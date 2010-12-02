/* Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.mapdust.service.value;


/**
 * Defines the attributes of the <code>Address</code> object.
 *
 * @author Bea
 *
 */
public class Address {

    /** The country code */
    private String countryCode;

    /** The city */
    private String city;

    /** The state code */
    private String stateCode;

    /** The ZIP code */
    private String zipCode;

    /** The street name */
    private String streetName;

    /** The house number */
    private String houseNumber;

    /**
     * Builds a <code>Address</code> object.
     */
    public Address() {}

    /**
     * Builds a <code>Address</code> object based on the given arguments.
     *
     * @param countryCode The country code
     * @param city The city
     * @param stateCode The state code
     * @param zipCode The ZIP code
     * @param streetName The street name
     * @param houseNumber The house number
     */
    public Address(String countryCode, String city, String stateCode,
            String zipCode, String streetName, String houseNumber) {
        this.countryCode = countryCode;
        this.city = city;
        this.stateCode = stateCode;
        this.zipCode = zipCode;
        this.streetName = streetName;
        this.houseNumber = houseNumber;
    }

    /**
     * Returns the country code
     *
     * @return the countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the country code
     *
     * @param countryCode the countryCode to set
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Returns the city
     *
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city
     *
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Returns the state code
     *
     * @return the stateCode
     */
    public String getStateCode() {
        return stateCode;
    }

    /**
     * Sets the state code
     *
     * @param stateCode the stateCode to set
     */
    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    /**
     * Returns the zip code
     *
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Sets the zip code
     *
     * @param zipCode the zipCode to set
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Returns the street name
     *
     * @return the streetName
     */
    public String getStreetName() {
        return streetName;
    }

    /**
     * Sets the street name
     *
     * @param streetName the streetName to set
     */
    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    /**
     * Returns the house number
     *
     * @return the houseNumber
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * Sets the house number
     *
     * @param houseNumber the houseNumber to set
     */
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    @Override
    public String toString() {
        String addressStr = "";
        if (countryCode != null && !countryCode.isEmpty()) {
            addressStr += countryCode;
        }
        if (city != null && !city.isEmpty()) {
            if (!addressStr.isEmpty()) {
                addressStr += ", ";
            }
            addressStr += city;
        }

        if (stateCode != null && !stateCode.isEmpty()) {
            if (!addressStr.isEmpty()) {
                addressStr += ", ";
            }
            addressStr += stateCode;
        }

        if (zipCode != null && !zipCode.isEmpty()) {
            if (!addressStr.isEmpty()) {
                addressStr += ", ";
            }
            addressStr += zipCode;
        }
        if (streetName != null && !streetName.isEmpty()) {
            if (!addressStr.isEmpty()) {
                addressStr += ", ";
            }
            addressStr += streetName;
        }
        if (houseNumber != null && !houseNumber.isEmpty()) {
            if (!addressStr.isEmpty()) {
                addressStr += ", ";
            }
            addressStr += houseNumber;
        }
        return addressStr;
    }
}
