/*
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.HashMap;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * The class OSMAddress represents a single address node of OSM. It is a lightweight 
 * wrapper for a OSM node in order to simplify tag handling.
 */
public class OSMAddress extends OSMEntityBase {
	public static final String MISSING_TAG = "?";
	
	/** The dictionary containing guessed values. */
	private HashMap<String, String> guessedValues = new HashMap<String, String>();
	/** The dictionary containing indirect values. */
	private HashMap<String, String> derivedValues = new HashMap<String, String>();

	public OSMAddress(OsmPrimitive osmObject) {
		super(osmObject);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.NodeEntityBase#setOsmObject(org.openstreetmap.josm.data.osm.OsmPrimitive)
	 */
	@Override
	public void setOsmObject(OsmPrimitive osmObject) {
		super.setOsmObject(osmObject);
		
		String streetNameViaRel = OsmUtils.getAssociatedStreet(this.osmObject);
		if (!StringUtils.isNullOrEmpty(streetNameViaRel)) {
			setDerivedValue(TagUtils.ADDR_STREET_TAG, streetNameViaRel);
		}
	}
	
	/**
	 * Checks if the underlying address node has all tags usually needed to describe an address.
	 * @return
	 */
	public boolean isComplete() {
		return 	TagUtils.hasAddrCityTag(osmObject) && TagUtils.hasAddrCountryTag(osmObject) &&
				TagUtils.hasAddrHousenumberTag(osmObject) && TagUtils.hasAddrPostcodeTag(osmObject) &&
				TagUtils.hasAddrStateTag(osmObject) && 
				(TagUtils.hasAddrStreetTag(osmObject) || hasDerivedValue(TagUtils.ADDR_STREET_TAG));
	}
	
	/**
	 * Gets the name of the street associated with this address.
	 * @return
	 */
	public String getStreetName() {
		return getTagValueWithGuess(TagUtils.ADDR_STREET_TAG);
	}
	
	/**
	 * Gets the tag value with guess. If the object does not have the given tag, this mehtod looks for
	 * an appropriate guess. If both, real value and guess, are missing, a question mark is returned.
	 *
	 * @param tag the tag
	 * @return the tag value with guess
	 */
	private String getTagValueWithGuess(String tag) {
		if (StringUtils.isNullOrEmpty(tag)) return MISSING_TAG;
		if (osmObject == null) return MISSING_TAG;
		
		if (!osmObject.hasKey(tag) || StringUtils.isNullOrEmpty(osmObject.get(tag))) {
			if (!hasDerivedValue(tag)) {
				// object does not have this tag -> check for guess
				if (hasGuessedValue(tag)) {
					return "*" + getGuessedValue(tag);
				} else {
					// give up
					return MISSING_TAG;
				}
			} else { // ok, use derived value known via associated relation or way 
				return getDerivedValue(tag);
			}
		} else { // get existing tag value
			return osmObject.get(tag);			
		}
	}
	
	/**
	 * Returns <tt>true</tt>, if this address node has a street name.
	 * @return
	 */
	public boolean hasStreetName() {
		return TagUtils.hasAddrStreetTag(osmObject);
	}
	
	/**
	 * Returns the street name guessed by the nearest-neighbour search.
	 * @return the guessedStreetName
	 */
	public String getGuessedStreetName() {
		return getGuessedValue(TagUtils.ADDR_STREET_TAG);
	}

	/**
	 * @param guessedStreetName the guessedStreetName to set
	 */
	public void setGuessedStreetName(String guessedStreetName) {
		setGuessedValue(TagUtils.ADDR_STREET_TAG, guessedStreetName);
	}
	
	/**
	 * Checks for a guessed street name.
	 *
	 * @return true, if this instance has a guessed street name.
	 */
	public boolean hasGuessedStreetName() {
		return hasGuessedValue(TagUtils.ADDR_STREET_TAG);
	}
	
	/**
	 * @return the guessedPostCode
	 */
	public String getGuessedPostCode() {
		return getGuessedValue(TagUtils.ADDR_POSTCODE_TAG);
	}

	/**
	 * @param guessedPostCode the guessedPostCode to set
	 */
	public void setGuessedPostCode(String guessedPostCode) {
		setGuessedValue(TagUtils.ADDR_POSTCODE_TAG, guessedPostCode);
	}
	
	/**
	 * Checks for a guessed post code.
	 *
	 * @return true, if this instance has a guessed post code.
	 */
	public boolean hasGuessedPostCode() {
		return hasGuessedValue(TagUtils.ADDR_POSTCODE_TAG);
	}

	/**
	 * @return the guessedCity
	 */
	public String getGuessedCity() {
		return getGuessedValue(TagUtils.ADDR_CITY_TAG);
	}

	/**
	 * @param guessedCity the guessedCity to set
	 */
	public void setGuessedCity(String guessedCity) {
		setGuessedValue(TagUtils.ADDR_CITY_TAG, guessedCity);
	}

	/**
	 * Checks for a guessed city name.
	 *
	 * @return true, if this instance has a guessed city name.
	 */
	public boolean hasGuessedCity() {
		return hasGuessedValue(TagUtils.ADDR_CITY_TAG);
	}

	/**
	 * Returns true, if this instance has guesses regarding address tags.
	 * @return
	 */
	public boolean hasGuesses() {
		return guessedValues.size() > 0; 
	}
	
	/**
	 * Applies all guessed tags for this node.
	 */
	public void applyAllGuesses() {
		for (String tag : guessedValues.keySet()) {
			String val = guessedValues.get(tag);
			if (!StringUtils.isNullOrEmpty(val)) {
				setOSMTag(tag, val);
			}
		}
	}

	/**
	 * Gets the name of the post code associated with this address.
	 * @return
	 */
	public String getPostCode() {
		return getTagValueWithGuess(TagUtils.ADDR_POSTCODE_TAG);
	}
	
	/**
	 * Checks for post code tag.
	 *
	 * @return true, if successful
	 */
	public boolean hasPostCode() {
		return TagUtils.hasAddrPostcodeTag(osmObject);
	}
	
	/**
	 * Gets the name of the house number associated with this address.
	 * @return
	 */
	public String getHouseNumber() {
		if (!TagUtils.hasAddrHousenumberTag(osmObject)) {
			return MISSING_TAG;
		}
		return TagUtils.getAddrHousenumberValue(osmObject);
	}
	
	/**
	 * Gets the name of the city associated with this address.
	 * @return
	 */
	public String getCity() {
		return getTagValueWithGuess(TagUtils.ADDR_CITY_TAG);
	}
	
	/**
	 * Checks for city tag.
	 *
	 * @return true, if successful
	 */
	public boolean hasCity() {
		return TagUtils.hasAddrCityTag(osmObject);
	}
	
	/**
	 * Gets the name of the state associated with this address.
	 * @return
	 */
	public String getState() {
		if (!TagUtils.hasAddrStateTag(osmObject)) {
			return MISSING_TAG;
		}
		return TagUtils.getAddrStateValue(osmObject);
	}

	/**
	 * Gets the name of the state associated with this address.
	 * @return
	 */
	public String getCountry() {
		if (!TagUtils.hasAddrCountryTag(osmObject)) {
			return MISSING_TAG;
		}
		return TagUtils.getAddrCountryValue(osmObject);
	}
	
	/**
	 * Removes all addresss related tags from the node or way.
	 */
	public void removeAllAddressTags() {
		removeOSMTag(TagUtils.ADDR_CITY_TAG);
		removeOSMTag(TagUtils.ADDR_COUNTRY_TAG);
		removeOSMTag(TagUtils.ADDR_POSTCODE_TAG);
		removeOSMTag(TagUtils.ADDR_HOUSENUMBER_TAG);
		removeOSMTag(TagUtils.ADDR_STATE_TAG);
		removeOSMTag(TagUtils.ADDR_STREET_TAG);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.NodeEntityBase#compareTo(org.openstreetmap.josm.plugins.addressEdit.INodeEntity)
	 */
	@Override
	public int compareTo(IOSMEntity o) {
		if (o == null || !(o instanceof OSMAddress)) {
			return -1;
		}
		OSMAddress other = (OSMAddress) o;
		
		int cc = 0;
		cc = this.getCountry().compareTo(other.getCountry());
		if ( cc  == 0) {
			cc = this.getState().compareTo(other.getState());			
			if (cc  == 0) {
				cc = this.getCity().compareTo(other.getCity());				
				if (cc  == 0) {
					cc = this.getStreetName().compareTo(other.getStreetName());					
					if (cc  == 0) {
						if (hasGuessedStreetName()) {							
							if (other.hasStreetName()) {
								// Compare guessed name with the real name
								cc = this.getGuessedStreetName().compareTo(other.getStreetName());
								if (cc == 0) {
									cc = this.getHouseNumber().compareTo(other.getHouseNumber());
								}
							} else if (other.hasGuessedStreetName()){
								// Compare guessed name with the guessed name
								cc = this.getGuessedStreetName().compareTo(other.getGuessedStreetName());
								if (cc == 0) {
									cc = this.getHouseNumber().compareTo(other.getHouseNumber());
								}
							} // else: give up
						// No guessed name at all -> just compare the number
						} else {
							cc = this.getHouseNumber().compareTo(other.getHouseNumber());
						}
					}
				}
			}
		}
		
		return cc;
	}
	
	/**
	 * Applies the street name from the specified street node.
	 * @param node
	 */
	public void assignStreet(StreetNode node) {
		if (node == null || !node.hasName()) return;
		
		if (!node.getName().equals(getStreetName())) {
			setStreetName(node.getName());			
			node.addAddress(this);
			fireEntityChanged(this);
		}
	}
	
	/**
	 * Gets the guessed value for the given tag.
	 * @param tag The tag to get the guessed value for.
	 * @return
	 */
	public String getGuessedValue(String tag) {
		if (!hasGuessedValue(tag)) {
			return null;			
		}
		return guessedValues.get(tag);
	}
	
	/**
	 * Check if this instance needs guessed values. This is the case, if the underlying OSM node
	 * has either no street name, post code or city.
	 *
	 * @return true, if this instance needs at least one guessed value.
	 */
	public boolean needsGuess() {
		return 	needsGuessedValue(TagUtils.ADDR_CITY_TAG) ||
				needsGuessedValue(TagUtils.ADDR_POSTCODE_TAG) ||
				needsGuessedValue(TagUtils.ADDR_STREET_TAG);
	}
	
	/**
	 * Check if this instance needs guessed value for a given tag.
	 * @return true, if successful
	 */
	public boolean needsGuessedValue(String tag) {
		return MISSING_TAG.equals(getTagValueWithGuess(tag));
	}
	
	/**
	 * Clears all guessed values.
	 */
	public void clearAllGuesses() {
		guessedValues.clear();
	}
	
	/**
	 * Checks if given tag has a guessed value (tag exists and has a non-empty value).
	 *
	 * @param tag the tag
	 * @return true, if tag has a guessed value.
	 */
	private boolean hasGuessedValue(String tag) {
		return guessedValues.containsKey(tag) && 
			!StringUtils.isNullOrEmpty(guessedValues.get(tag));
	}
	
	/**
	 * Sets the guessed value with the given tag.
	 *
	 * @param tag the tag to set the guess for
	 * @param value the value of the guessed tag.
	 */
	public void setGuessedValue(String tag, String value) {
		guessedValues.put(tag, value);
		fireEntityChanged(this);
	}
	
	/**
	 * Checks if given tag has a derived value (value is available via a referrer).
	 *
	 * @param tag the tag
	 * @return true, if tag has a derived value.
	 */
	private boolean hasDerivedValue(String tag) {
		return derivedValues.containsKey(tag) && 
			!StringUtils.isNullOrEmpty(derivedValues.get(tag));
	}
	
	/**
	 * Gets the derived value for the given tag.
	 * @param tag The tag to get the derived value for.
	 * @return
	 */
	public String getDerivedValue(String tag) {
		if (!hasDerivedValue(tag)) {
			return null;			
		}
		return derivedValues.get(tag);
	}
		
	/**
	 * Sets the value known indirectly via a referrer with the given tag.
	 *
	 * @param tag the tag to set the derived value for
	 * @param value the value of the derived tag.
	 */
	public void setDerivedValue(String tag, String value) {
		derivedValues.put(tag, value);		
	}	
	
	/**
	 * Sets the street name of the address node.
	 * @param streetName
	 */
	public void setStreetName(String streetName) {
		if (streetName != null && streetName.length() == 0) return;
		
		setOSMTag(TagUtils.ADDR_STREET_TAG, streetName);
	}

	
	/**
	 * Sets the state of the address node.
	 * @param state
	 */
	public void setState(String state) {
		if (state != null && state.length() == 0) return;
		
		setOSMTag(TagUtils.ADDR_STATE_TAG, state);
	}
	
	/**
	 * Sets the country of the address node.
	 * @param country
	 */
	public void setCountry(String country) {
		if (country != null && country.length() == 0) return;
		
		setOSMTag(TagUtils.ADDR_COUNTRY_TAG, country);
	}
	
	/**
	 * Sets the post code of the address node.
	 * @param postCode
	 */
	public void setPostCode(String postCode) {
		if (postCode != null && postCode.length() == 0) return;
		
		setOSMTag(TagUtils.ADDR_POSTCODE_TAG, postCode);
	}
	
	@Override
	public String toString() {
		return OSMAddress.getFormatString(this);
	}

	public static String getFormatString(OSMAddress node) {
		// TODO: Add further countries here
		// DE
		String guessed = node.getGuessedStreetName();
		String sName = node.getStreetName();
		if (!StringUtils.isNullOrEmpty(guessed) && MISSING_TAG.equals(sName)) {
			sName = String.format("(%s)", guessed);
		}	
		
		return String.format("%s %s, %s-%s %s (%s) ", 
				sName,
				node.getHouseNumber(),
				node.getCountry(),
				node.getPostCode(),
				node.getCity(),
				node.getState());
	}
}
