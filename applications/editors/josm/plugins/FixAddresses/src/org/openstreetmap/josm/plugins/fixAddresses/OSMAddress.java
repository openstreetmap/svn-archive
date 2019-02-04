// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.HashMap;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AddressActions;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * The class OSMAddress represents a single address node of OSM. It is a
 * lightweight wrapper for a OSM node in order to simplify tag handling.
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
    private HashMap<String, String> guessedValues = new HashMap<>();
    /** The dictionary containing guessed objects. */
    private HashMap<String, OsmPrimitive> guessedObjects = new HashMap<>();
    /** The dictionary containing indirect values. */
    private HashMap<String, String> derivedValues = new HashMap<>();

    public OSMAddress(OsmPrimitive osmObject) {
        super(osmObject);
    }

    @Override
    public void setOsmObject(OsmPrimitive osmObject) {
        super.setOsmObject(osmObject);

        isPartOfInterpolation = OsmUtils.getValuesFromAddressInterpolation(this);
        isPartOfAssocStreetRel = OsmUtils.getValuesFromRelation(this);
    }

    /**
     * Checks if the underlying address node has all tags usually needed to
     * describe an address.
     *
     * @return {@code true} if the underlying address node has all tags usually needed to describe an address
     */
    public boolean isComplete() {
        boolean isComplete = hasCity() && hasHouseNumber() && hasCity() && hasStreetName();

        // Check, if "addr:state" is required (US and AU)
        if (TagUtils.isStateRequired()) {
            isComplete = isComplete && hasState();
        }

        // Check, if user checked "ignore post code"
        if (!FixAddressesPlugin.getPreferences().isIgnorePostCode()) {
            isComplete = isComplete && hasPostalCode()
                && PostalCodeChecker.hasValidPostalCode(this);
        }

        return isComplete;
    }

    /**
     * Gets the name of the street associated with this address.
     *
     * @return the name of the street associated with this address
     */
    public String getStreetName() {
        return getTagValueWithGuess(TagConstants.ADDR_STREET_TAG);
    }

    /**
     * Gets the tag value with guess. If the object does not have the given tag,
     * this method looks for an appropriate guess. If both, real value and
     * guess, are missing, a question mark is returned.
     *
     * @param tag
     *            the tag
     * @return the tag value with guess
     */
    private String getTagValueWithGuess(String tag) {
        if (StringUtils.isNullOrEmpty(tag))
            return MISSING_TAG;
        if (osmObject == null)
            return MISSING_TAG;

        if (!osmObject.hasKey(tag)
            || StringUtils.isNullOrEmpty(osmObject.get(tag))) {
            if (!hasDerivedValue(tag)) {
                // object does not have this tag -> check for guess
                if (hasGuessedValue(tag)) {
                    return "*" + getGuessedValue(tag);
                } else {
                    // give up
                    return MISSING_TAG;
                }
            } else { // ok, use derived value known via associated relation or
                // way
                return getDerivedValue(tag);
            }
        } else { // get existing tag value
            return osmObject.get(tag);
        }
    }

    /**
     * Returns <tt>true</tt>, if this address node has a street name.
     *
     * @return <tt>true</tt>, if this address node has a street name
     */
    public boolean hasStreetName() {
        return hasTag(TagConstants.ADDR_STREET_TAG) || isPartOfRelation();
    }

    /**
     * Returns the street name guessed by the nearest-neighbor search.
     *
     * @return the guessedStreetName
     */
    public String getGuessedStreetName() {
        return getGuessedValue(TagConstants.ADDR_STREET_TAG);
    }

    /**
     * Sets the guessed street name.
     *
     * @param guessedStreetName
     *            the guessedStreetName to set
     * @param srcObj
     *            the source object of the guess.
     */
    public void setGuessedStreetName(String guessedStreetName, OsmPrimitive srcObj) {
        setGuessedValue(TagConstants.ADDR_STREET_TAG, guessedStreetName, srcObj);
    }

    /**
     * Checks for a guessed street name.
     *
     * @return true, if this instance has a guessed street name.
     */
    public boolean hasGuessedStreetName() {
        return hasGuessedValue(TagConstants.ADDR_STREET_TAG);
    }

    /**
     * @return the guessedPostCode
     */
    public String getGuessedPostalCode() {
        return getGuessedValue(TagConstants.ADDR_POSTCODE_TAG);
    }

    /**
     * Sets the guessed post code.
     *
     * @param guessedPostCode
     *            the guessedPostCode to set
     * @param srcObj
     *            srcObj the source object of the guess
     */
    public void setGuessedPostalCode(String guessedPostCode, OsmPrimitive srcObj) {
        setGuessedValue(TagConstants.ADDR_POSTCODE_TAG, guessedPostCode, srcObj);
    }

    /**
     * Checks for a guessed post code.
     *
     * @return true, if this instance has a guessed post code.
     */
    public boolean hasGuessedPostalCode() {
        return hasGuessedValue(TagConstants.ADDR_POSTCODE_TAG);
    }

    /**
     * @return the guessedCity
     */
    public String getGuessedCity() {
        return getGuessedValue(TagConstants.ADDR_CITY_TAG);
    }

    /**
     * Sets the guessed city.
     *
     * @param guessedCity
     *            the guessedCity to set
     * @param srcObj
     *            the source object of the guess
     */
    public void setGuessedCity(String guessedCity, OsmPrimitive srcObj) {
        setGuessedValue(TagConstants.ADDR_CITY_TAG, guessedCity, srcObj);
    }

    /**
     * Checks for a guessed city name.
     *
     * @return true, if this instance has a guessed city name.
     */
    public boolean hasGuessedCity() {
        return hasGuessedValue(TagConstants.ADDR_CITY_TAG);
    }

    /**
     * Returns true, if this instance has guesses regarding address tags.
     *
     * @return true, if this instance has guesses regarding address tags
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
     * @param tag
     *            the tag to apply the guessed value for.
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
     *
     * @return the name of the post code associated with this address
     */
    public String getPostalCode() {
        String pc = getTagValueWithGuess(TagConstants.ADDR_POSTCODE_TAG);

        if (!MISSING_TAG.equals(pc)
            && !PostalCodeChecker.hasValidPostalCode(getCountry(), pc)) {
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
        return hasTag(TagConstants.ADDR_POSTCODE_TAG);
    }

    /**
     * Gets the name of the house number associated with this address.
     *
     * @return the name of the house number associated with this address
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

    @Override
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
     *
     * @return the name of the city associated with this address
     */
    public String getCity() {
        return getTagValueWithGuess(TagConstants.ADDR_CITY_TAG);
    }

    /**
     * Checks for city tag.
     *
     * @return true, if a city tag is present or available via referrer.
     */
    public boolean hasCity() {
        return hasTag(TagConstants.ADDR_CITY_TAG);
    }

    /**
     * Gets the name of the state associated with this address.
     *
     * @return the name of the state associated with this address
     */
    public String getState() {
        return getTagValueWithGuess(TagConstants.ADDR_STATE_TAG);
    }

    /**
     * Checks for state tag.
     *
     * @return true, if a state tag is present or available via referrer.
     */
    public boolean hasState() {
        return hasTag(TagConstants.ADDR_STATE_TAG);
    }

    /**
     * Gets the name of the country associated with this address.
     *
     * @return the name of the country associated with this address
     */
    public String getCountry() {
        return getTagValueWithGuess(TagConstants.ADDR_COUNTRY_TAG);
    }

    /**
     * Checks for country tag.
     *
     * @return true, if a country tag is present or available via referrer.
     */
    public boolean hasCountry() {
        return hasTag(TagConstants.ADDR_COUNTRY_TAG);
    }

    /**
     * Removes all address-related tags from the node or way.
     */
    public void removeAllAddressTags() {
        removeOSMTag(TagConstants.ADDR_CITY_TAG);
        removeOSMTag(TagConstants.ADDR_COUNTRY_TAG);
        removeOSMTag(TagConstants.ADDR_POSTCODE_TAG);
        removeOSMTag(TagConstants.ADDR_HOUSENUMBER_TAG);
        removeOSMTag(TagConstants.ADDR_STATE_TAG);
        removeOSMTag(TagConstants.ADDR_STREET_TAG);
    }

    /**
     * Checks if the associated OSM object has the given tag or if the tag is
     * available via a referrer.
     *
     * @param tag
     *            the tag to look for.
     * @return true, if there is a value for the given tag.
     */
    public boolean hasTag(String tag) {
    if (StringUtils.isNullOrEmpty(tag))
        return false;

    return TagUtils.hasTag(osmObject, tag) || hasDerivedValue(tag);
    }

    @Override
    public int compareTo(IOSMEntity o) {
    if (o == null || !(o instanceof OSMAddress)) {
        return -1;
    }
    OSMAddress other = (OSMAddress) o;

    if (this.equals(other))
        return 0;

    int cc = 0;
    cc = this.getCountry().compareTo(other.getCountry());
    if (cc == 0) {
        cc = this.getState().compareTo(other.getState());
        if (cc == 0) {
        cc = this.getCity().compareTo(other.getCity());
        if (cc == 0) {
            cc = this.getStreetName().compareTo(other.getStreetName());
            if (cc == 0) {
                if (hasGuessedStreetName()) {
                    if (other.hasStreetName()) {
                        // Compare guessed name with the real name
                        /*String gsm =*/ this.getGuessedStreetName();
                        cc = this.getGuessedStreetName().compareTo(
                            other.getStreetName());
                        if (cc == 0) {
                            cc = this.getHouseNumber().compareTo(
                                other.getHouseNumber());
                        }
                        } else if (other.hasGuessedStreetName()) {
                        // Compare guessed name with the guessed name
                        cc = this.getGuessedStreetName().compareTo(
                            other.getGuessedStreetName());
                        if (cc == 0) {
                            cc = this.getHouseNumber().compareTo(
                                other.getHouseNumber());
                        }
                    } // else: give up
                    // No guessed name at all -> just compare the number
                } else {
                    cc = this.getHouseNumber().compareTo(
                        other.getHouseNumber());
                }
            }
        }
        }
    }

    return cc;
    }

    /**
     * Applies the street name from the specified street node.
     *
     * @param node street node
     */
    public void assignStreet(OSMStreet node) {
        if (node == null || !node.hasName())
            return;

        if (!node.getName().equals(getStreetName())) {
            setStreetName(node.getName());
            node.addAddress(this);
            fireEntityChanged(this);
        }
    }

    /**
     * Gets the guessed value for the given tag.
     *
     * @param tag
     *            The tag to get the guessed value for.
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
     * @param tag
     *            the guessed tag
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
     * Gets all guessed objects or an empty list, if no guesses have been made
     * yet.
     *
     * @return the guessed objects.
     */
    public Collection<OsmPrimitive> getGuessedObjects() {
        if (guessedObjects == null)
            return null;

        return guessedObjects.values();
    }

    /**
     * Check if this instance needs guessed values. This is the case, if the
     * underlying OSM node has either no street name, post code or city.
     *
     * @return true, if this instance needs at least one guessed value.
     */
    public boolean needsGuess() {
    return needsGuessedValue(TagConstants.ADDR_CITY_TAG)
        || needsGuessedValue(TagConstants.ADDR_POSTCODE_TAG)
        || needsGuessedValue(TagConstants.ADDR_COUNTRY_TAG) ||
        // needsGuessedValue(TagConstants.ADDR_STATE_TAG) ||
        needsGuessedValue(TagConstants.ADDR_STREET_TAG);
    }

    /**
     * Check if this instance needs guessed value for a given tag.
     * @param tag tag to analyze
     *
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
     * Checks if given tag has a guessed value (tag exists and has a non-empty
     * value).
     *
     * @param tag
     *            the tag
     * @return true, if tag has a guessed value.
     */
    private boolean hasGuessedValue(String tag) {
    CheckParameterUtil.ensureParameterNotNull(tag, "tag");

    return guessedValues.containsKey(tag)
        && !StringUtils.isNullOrEmpty(guessedValues.get(tag));
    }

    /**
     * Sets the guessed value with the given tag.
     *
     * @param tag
     *            the tag to set the guess for
     * @param value
     *            the value of the guessed tag.
     * @param osm
     *            the (optional) object which was used for the guess
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
     * Checks if given tag has a derived value (value is available via a
     * referrer).
     *
     * @param tag
     *            the tag
     * @return true, if tag has a derived value.
     */
    private boolean hasDerivedValue(String tag) {
        CheckParameterUtil.ensureParameterNotNull(tag, "tag");

        return derivedValues.containsKey(tag)
            && !StringUtils.isNullOrEmpty(derivedValues.get(tag));
    }

    /**
     * Returns true, if this instance has derived values from any referrer.
     *
     * @return true, if this instance has derived values from any referrer
     */
    public boolean hasDerivedValues() {
        return derivedValues.size() > 0;
    }

    /**
     * Gets the derived value for the given tag.
     *
     * @param tag
     *            The tag to get the derived value for.
     * @return the derived value for the given tag
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
     * @param tag
     *            the tag to set the derived value for
     * @param value
     *            the value of the derived tag.
     */
    public void setDerivedValue(String tag, String value) {
        derivedValues.put(tag, value);
    }

    /**
     * Sets the street name of the address node.
     *
     * @param streetName street name of the address node
     */
    public void setStreetName(String streetName) {
        if (streetName != null && streetName.length() == 0)
            return;

        setOSMTag(TagConstants.ADDR_STREET_TAG, streetName);
    }

    /**
     * Sets the state of the address node.
     *
     * @param state state of the address node
     */
    public void setState(String state) {
        if (state != null && state.length() == 0)
            return;

        setOSMTag(TagConstants.ADDR_STATE_TAG, state);
    }

    /**
     * Sets the country of the address node.
     *
     * @param country country of the address node
     */
    public void setCountry(String country) {
        if (country != null && country.length() == 0)
            return;

        setOSMTag(TagConstants.ADDR_COUNTRY_TAG, country);
    }

    /**
     * Sets the post code of the address node.
     *
     * @param postCode post code of the address node
     */
    public void setPostCode(String postCode) {
        if (postCode != null && postCode.length() == 0)
            return;

        setOSMTag(TagConstants.ADDR_POSTCODE_TAG, postCode);
    }

    @Override
    public void visit(IAllKnowingTrashHeap trashHeap, IProblemVisitor visitor) {
        CheckParameterUtil.ensureParameterNotNull(visitor, "visitor");

        // Check for street
        if (!hasStreetName()) {
            AddressProblem p = new AddressProblem(this,
                tr("Address has no street"));
            if (hasGuessedStreetName()) { // guess exists -> add solution entry
            String tag = TagConstants.ADDR_STREET_TAG;
            addGuessValueSolution(p, tag);
            }
            addRemoveAddressTagsSolution(p);
            visitor.addProblem(p);
            // Street name exists, but is invalid -> ask the all knowing trash
            // heap
        } else if (!trashHeap.isValidStreetName(getStreetName())) {
            AddressProblem p = new AddressProblem(this,
                tr("Address has no valid street"));
            String match = trashHeap.getClosestStreetName(getStreetName());

            if (!StringUtils.isNullOrEmpty(match)) {
                setGuessedStreetName(match, null);
                addGuessValueSolution(p, TagConstants.ADDR_STREET_TAG);
            }
            visitor.addProblem(p);
        }

        // Check for postal code
        if (!hasPostalCode()) {
            AddressProblem p = new AddressProblem(this, tr("Address has no post code"));
            if (hasGuessedStreetName()) {
                String tag = TagConstants.ADDR_POSTCODE_TAG;
                addGuessValueSolution(p, tag);
            }
            addRemoveAddressTagsSolution(p);
            visitor.addProblem(p);
        }

        // Check for city
        if (!hasCity()) {
            AddressProblem p = new AddressProblem(this, tr("Address has no city"));
            if (hasGuessedStreetName()) {
                String tag = TagConstants.ADDR_CITY_TAG;
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
     * @param p
     *            the problem to add the solution to.
     * @param tag
     *            the tag to change.
     */
    private void addGuessValueSolution(AddressProblem p, String tag) {
    AddressSolution s = new AddressSolution(String.format("%s '%s'",
        tr("Assign to"), getGuessedValue(tag)),
        AddressActions.getApplyGuessesAction(), SolutionType.Change);

    p.addSolution(s);
    }

    /**
     * Adds the remove address tags solution entry to a problem.
     *
     * @param problem
     *            the problem
     */
    private void addRemoveAddressTagsSolution(IProblem problem) {
    CheckParameterUtil.ensureParameterNotNull(problem, "problem");

    AddressSolution s = new AddressSolution(tr("Remove all address tags"),
        AddressActions.getRemoveTagsAction(), SolutionType.Remove);
    problem.addSolution(s);
    }

    @Override
    public String toString() {
    return OSMAddress.getFormatString(this);
    }

    /**
     * Gets the formatted string representation of the given node.
     *
     * @param node
     *            the node
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

    return String.format("%s %s, %s-%s %s (%s) ", sName,
        node.getHouseNumber(), node.getCountry(), node.getPostalCode(),
        node.getCity(), node.getState());
    }
}
