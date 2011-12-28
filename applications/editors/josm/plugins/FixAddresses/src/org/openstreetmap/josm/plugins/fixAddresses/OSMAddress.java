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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.HashMap;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AddressActions;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * The class OSMAddress represents a single address node of OSM. It is a lightweight
 * wrapper for a OSM node in order to simplify tag handling.
 */
public class OSMAddress extends OSMEntityBase {
	public static final String MISSING_TAG = "?";
	public static final String INTERPOLATION_TAG = "x..y";


	/** True, if address is part of an address interpolation. */
	private boolean isPartOfInterpolation;
	/**
	 * True, if address has derived values from an associated street relation.
	 */
	private boolean isPartOfAssocStreetRel;

	/** The dictionary containing guessed values. */
	private HashMap<String, String> guessedValues = new HashMap<String, String>();
	/** The dictionary containing guessed objects. */
	private HashMap<String, OsmPrimitive> guessedObjects = new HashMap<String, OsmPrimitive>();
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

		isPartOfInterpolation = OsmUtils.getValuesFromAddressInterpolation(this);
		isPartOfAssocStreetRel = OsmUtils.getValuesFromRelation(this);
	}

	/**
	 * Checks if the underlying address node has all tags usually needed to describe an address.
	 * @return
	 */
	public boolean isComplete() {
		boolean isComplete =    hasCity() &&
								hasHouseNumber() &&								
								hasCity() &&
								hasStreetName();

		// Check, if "addr:state" is required (US and AU)
		if (TagUtils.isStateRequired()) {
			isComplete = isComplete && hasState();
		}
		
		// Check, if user checked "ignore post code"
		if (!FixAddressesPlugin.getPreferences().isIgnorePostCode()) {
			isComplete = isComplete  && 
					hasPostalCode() &&
					PostalCodeChecker.hasValidPostalCode(this);
		}

		return isComplete;
	}

	/**
	 * Gets the name of the street associated with this address.
	 * @return
	 */
	public String getStreetName() {
		return getTagValueWithGuess(TagUtils.ADDR_STREET_TAG);
	}

	/**
	 * Gets the tag value with guess. If the object does not have the given tag, this method looks for
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
		return hasTag(TagUtils.ADDR_STREET_TAG);
	}

	/**
	 * Returns the street name guessed by the nearest-neighbor search.
	 * @return the guessedStreetName
	 */
	public String getGuessedStreetName() {
		return getGuessedValue(TagUtils.ADDR_STREET_TAG);
	}

	/**
	 * Sets the guessed street name.
	 *
	 * @param guessedStreetName the guessedStreetName to set
	 * @param srcObj the source object of the guess.
	 */
	public void setGuessedStreetName(String guessedStreetName, OsmPrimitive srcObj) {
		setGuessedValue(TagUtils.ADDR_STREET_TAG, guessedStreetName, srcObj);
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
	public String getGuessedPostalCode() {
		return getGuessedValue(TagUtils.ADDR_POSTCODE_TAG);
	}

	/**
	 * Sets the guessed post code.
	 *
	 * @param guessedPostCode the guessedPostCode to set
	 * @param srcObj srcObj the source object of the guess
	 */
	public void setGuessedPostalCode(String guessedPostCode, OsmPrimitive srcObj) {
		setGuessedValue(TagUtils.ADDR_POSTCODE_TAG, guessedPostCode, srcObj);
	}

	/**
	 * Checks for a guessed post code.
	 *
	 * @return true, if this instance has a guessed post code.
	 */
	public boolean hasGuessedPostalCode() {
		return hasGuessedValue(TagUtils.ADDR_POSTCODE_TAG);
	}

	/**
	 * @return the guessedCity
	 */
	public String getGuessedCity() {
		return getGuessedValue(TagUtils.ADDR_CITY_TAG);
	}

	/**
	 * Sets the guessed city.
	 *
	 * @param guessedCity the guessedCity to set
	 * @param srcObj the source object of the guess
	 */
	public void setGuessedCity(String guessedCity, OsmPrimitive srcObj) {
		setGuessedValue(TagUtils.ADDR_CITY_TAG, guessedCity, srcObj);
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
			applyGuessForTag(tag);
		}

		// Clear all guesses
		guessedValues.clear();
		guessedObjects.clear();
	}

	/**
	 * Apply the guessed value for the given tag.
	 *
	 * @param tag the tag to apply the guessed value for.
	 */
	public void applyGuessForTag(String tag) {
		if (guessedValues.containsKey(tag)) {
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
	public String getPostalCode() {
		String pc = getTagValueWithGuess(TagUtils.ADDR_POSTCODE_TAG);


		if (!MISSING_TAG.equals(pc) && !PostalCodeChecker.hasValidPostalCode(getCountry(), pc)) {
			pc = "(!)" + pc;
		}
		return pc;
	}

	/**
	 * Checks if this instance has a valid postal code.
	 *
	 * @return true, if successful
	 */
	public boolean hasValidPostalCode() {
		return PostalCodeChecker.hasValidPostalCode(this);
	}

	/**
	 * Checks for post code tag.
	 *
	 * @return true, if successful
	 */
	public boolean hasPostalCode() {
		return hasTag(TagUtils.ADDR_POSTCODE_TAG);
	}

	/**
	 * Gets the name of the house number associated with this address.
	 * @return
	 */
	public String getHouseNumber() {
		if (!TagUtils.hasAddrHousenumberTag(osmObject)) {
			if (!isPartOfInterpolation) {
				return MISSING_TAG;
			} else {
				return INTERPOLATION_TAG;
			}
		}
		return TagUtils.getAddrHousenumberValue(osmObject);
	}

	/**
	 * Checks for house number.
	 *
	 * @return true, if successful
	 */
	public boolean hasHouseNumber() {
		return TagUtils.hasAddrHousenumberTag(osmObject) || isPartOfInterpolation;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.OSMEntityBase#getName()
	 */
	public String getName() {
		String name = TagUtils.getNameValue(osmObject);
		if (!StringUtils.isNullOrEmpty(name)) {
			return TagUtils.getAddrHousenameValue(osmObject);
		}

		return "";
	}

	/**
	 * Checks if this address is part of a address interpolation.
	 *
	 * @return true, if is part of interpolation
	 */
	protected boolean isPartOfInterpolation() {
		return isPartOfInterpolation;
	}

	/**
	 * Checks if this address is part of an 'associated street' relation.
	 *
	 * @return true, if is part of interpolation
	 */
	protected boolean isPartOfRelation() {
		return isPartOfAssocStreetRel;
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
	 * @return true, if a city tag is present or available via referrer.
	 */
	public boolean hasCity() {
		return hasTag(TagUtils.ADDR_CITY_TAG);
	}

	/**
	 * Gets the name of the state associated with this address.
	 * @return
	 */
	public String getState() {
		return getTagValueWithGuess(TagUtils.ADDR_STATE_TAG);
	}

	/**
	 * Checks for state tag.
	 *
	 * @return true, if a state tag is present or available via referrer.
	 */
	public boolean hasState() {
		return hasTag(TagUtils.ADDR_STATE_TAG);
	}

	/**
	 * Gets the name of the country associated with this address.
	 * @return
	 */
	public String getCountry() {
		return getTagValueWithGuess(TagUtils.ADDR_COUNTRY_TAG);
	}

	/**
	 * Checks for country tag.
	 *
	 * @return true, if a country tag is present or available via referrer.
	 */
	public boolean hasCountry() {
		return hasTag(TagUtils.ADDR_COUNTRY_TAG);
	}

	/**
	 * Removes all address-related tags from the node or way.
	 */
	public void removeAllAddressTags() {
		removeOSMTag(TagUtils.ADDR_CITY_TAG);
		removeOSMTag(TagUtils.ADDR_COUNTRY_TAG);
		removeOSMTag(TagUtils.ADDR_POSTCODE_TAG);
		removeOSMTag(TagUtils.ADDR_HOUSENUMBER_TAG);
		removeOSMTag(TagUtils.ADDR_STATE_TAG);
		removeOSMTag(TagUtils.ADDR_STREET_TAG);
	}

	/**
	 * Checks if the associated OSM object has the given tag or if the tag is available via a referrer.
	 *
	 * @param tag the tag to look for.
	 * @return true, if there is a value for the given tag.
	 */
	public boolean hasTag(String tag) {
		if (StringUtils.isNullOrEmpty(tag)) return false;

		return TagUtils.hasTag(osmObject, tag) || hasDerivedValue(tag);
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
	public void assignStreet(OSMStreet node) {
		if (node == null || !node.hasName()) return;

		if (!node.getName().equals(getStreetName())) {
			setStreetName(node.getName());
			node.addAddress(this);
			fireEntityChanged(this);
		}
	}

	/**
	 * Gets the guessed value for the given tag.
	 *
	 * @param tag The tag to get the guessed value for.
	 * @return the guessed value
	 */
	public String getGuessedValue(String tag) {
		CheckParameterUtil.ensureParameterNotNull(tag, "tag");

		if (!hasGuessedValue(tag)) {
			return null;
		}
		return guessedValues.get(tag);
	}

	/**
	 * Gets the guessed object.
	 *
	 * @param tag the guessed tag
	 * @return the object which has been selected for the guess
	 */
	public OsmPrimitive getGuessedObject(String tag) {
		CheckParameterUtil.ensureParameterNotNull(tag, "tag");

		if (guessedObjects.containsKey(tag)) {
			return guessedObjects.get(tag);
		}
		return null;
	}

	/**
	 * Gets all guessed objects or an empty list, if no guesses have been made yet.
	 *
	 * @return the guessed objects.
	 */
	public Collection<OsmPrimitive> getGuessedObjects() {
		if (guessedObjects == null) return null;

		return guessedObjects.values();
	}

	/**
	 * Check if this instance needs guessed values. This is the case, if the underlying OSM node
	 * has either no street name, post code or city.
	 *
	 * @return true, if this instance needs at least one guessed value.
	 */
	public boolean needsGuess() {
		return  needsGuessedValue(TagUtils.ADDR_CITY_TAG) ||
				needsGuessedValue(TagUtils.ADDR_POSTCODE_TAG) ||
				needsGuessedValue(TagUtils.ADDR_COUNTRY_TAG) ||
				//needsGuessedValue(TagUtils.ADDR_STATE_TAG) ||
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
		CheckParameterUtil.ensureParameterNotNull(tag, "tag");

		return guessedValues.containsKey(tag) &&
			!StringUtils.isNullOrEmpty(guessedValues.get(tag));
	}

	/**
	 * Sets the guessed value with the given tag.
	 *
	 * @param tag the tag to set the guess for
	 * @param value the value of the guessed tag.
	 * @param osm the (optional) object which was used for the guess 
	 */
	public void setGuessedValue(String tag, String value, OsmPrimitive osm) {
		CheckParameterUtil.ensureParameterNotNull(tag, "tag");

		if (value != null && osm != null) {
			guessedValues.put(tag, value);
			if (osm != null) {
				guessedObjects.put(tag, osm);
			}
			fireEntityChanged(this);
		}
	}

	/**
	 * Checks if given tag has a derived value (value is available via a referrer).
	 *
	 * @param tag the tag
	 * @return true, if tag has a derived value.
	 */
	private boolean hasDerivedValue(String tag) {
		CheckParameterUtil.ensureParameterNotNull(tag, "tag");

		return derivedValues.containsKey(tag) &&
			!StringUtils.isNullOrEmpty(derivedValues.get(tag));
	}

	/**
	 * Returns true, if this instance has derived values from any referrer.
	 * @return
	 */
	public boolean hasDerivedValues() {
		return derivedValues.size() > 0;
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

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.OSMEntityBase#visit(org.openstreetmap.josm.plugins.fixAddresses.IProblemVisitor)
	 */
	@Override
	public void visit(IAllKnowingTrashHeap trashHeap, IProblemVisitor visitor) {
		CheckParameterUtil.ensureParameterNotNull(visitor, "visitor");

		// Check for street
		if (!hasStreetName()) {
			AddressProblem p = new AddressProblem(this, tr("Address has no street"));
			if (hasGuessedStreetName()) { // guess exists -> add solution entry
				String tag = TagUtils.ADDR_STREET_TAG;
				addGuessValueSolution(p, tag);
			}
			addRemoveAddressTagsSolution(p);
			visitor.addProblem(p);
		// Street name exists, but is invalid -> ask the all knowing trash heap
		} else if (!trashHeap.isValidStreetName(getStreetName())) {
			AddressProblem p = new AddressProblem(this, tr("Address has no valid street"));
			String match = trashHeap.getClosestStreetName(getStreetName());

			if (!StringUtils.isNullOrEmpty(match)) {
				setGuessedStreetName(match, null);
				addGuessValueSolution(p, TagUtils.ADDR_STREET_TAG);
			}
			visitor.addProblem(p);
		}

		// Check for postal code
		if (!hasPostalCode()) {
			AddressProblem p = new AddressProblem(this, tr("Address has no post code"));
			if (hasGuessedStreetName()) {
				String tag = TagUtils.ADDR_POSTCODE_TAG;
				addGuessValueSolution(p, tag);
			}
			addRemoveAddressTagsSolution(p);
			visitor.addProblem(p);
		}

		// Check for city
		if (!hasCity()) {
			AddressProblem p = new AddressProblem(this, tr("Address has no city"));
			if (hasGuessedStreetName()) {
				String tag = TagUtils.ADDR_CITY_TAG;
				addGuessValueSolution(p, tag);
			}
			addRemoveAddressTagsSolution(p);
			visitor.addProblem(p);
		}

		// Check for country
		if (!hasCountry()) {
			// TODO: Add guess for country
			AddressProblem p = new AddressProblem(this, tr("Address has no country"));
			addRemoveAddressTagsSolution(p);
			visitor.addProblem(p);
		}
	}

	/**
	 * Adds the guess value solution to a problem.
	 *
	 * @param p the problem to add the solution to.
	 * @param tag the tag to change.
	 */
	private void addGuessValueSolution(AddressProblem p, String tag) {
		AddressSolution s = new AddressSolution(
				String.format("%s '%s'", tr("Assign to"), getGuessedValue(tag)),
				AddressActions.getApplyGuessesAction(),
				SolutionType.Change);

		p.addSolution(s);
	}

	/**
	 * Adds the remove address tags solution entry to a problem.
	 *
	 * @param problem the problem
	 */
	private void addRemoveAddressTagsSolution(IProblem problem) {
		CheckParameterUtil.ensureParameterNotNull(problem, "problem");

		AddressSolution s = new AddressSolution(
						tr("Remove all address tags"),
						AddressActions.getRemoveTagsAction(),
						SolutionType.Remove);
		problem.addSolution(s);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.OSMEntityBase#toString()
	 */
	@Override
	public String toString() {
		return OSMAddress.getFormatString(this);
	}

	/**
	 * Gets the formatted string representation of the given node.
	 *
	 * @param node the node
	 * @return the format string
	 */
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
				node.getPostalCode(),
				node.getCity(),
				node.getState());
	}
}
