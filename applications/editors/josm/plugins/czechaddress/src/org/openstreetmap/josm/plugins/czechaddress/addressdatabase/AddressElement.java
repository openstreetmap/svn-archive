package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import java.util.List;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.czechaddress.PrimUtils;
import org.openstreetmap.josm.plugins.czechaddress.StringUtils;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.proposal.Proposal;

/**
 * The most general element of the houses and streets database.
 *
 * <p>Every element must have a <i>name</i> and may have a <i>parent</i>.</p>
 * 
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public abstract class AddressElement implements Comparable<AddressElement> {

    protected String name;
    protected AddressElement parent = null;

    /**
     * Constructor setting the name of this element.
     */
    public AddressElement (String name) {
        if (name == null)
            throw new NullPointerException("You must specify the name of this AddressElement");
        this.name = name;
    }

    /**
     * Sets the new name of this element.
     *
     * <p><b>NOTE:</b> Unlike in the default constructor, the name is <b>not</b>
     * capitalized.</p>
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Returns the name of this element.
     *
     * <p>The name is returned "as it was entered" in the constructor.</p>
     */
    public String getName() {
        return name;
    }


    public static String getName(Object o) {
        if (o instanceof OsmPrimitive)
            return getName((OsmPrimitive) o);

        if (o instanceof AddressElement)
            return ((AddressElement) o).getName();

        return null;
    }

    public static String getName(OsmPrimitive prim) {
        String cp     = prim.get(PrimUtils.KEY_ADDR_CP);
        String co     = prim.get(PrimUtils.KEY_ADDR_CO);
        String street = prim.get(PrimUtils.KEY_ADDR_STREET);
        String city   = prim.get(PrimUtils.KEY_ADDR_CITY);
        String name   = prim.get(PrimUtils.KEY_NAME);

        String result = "";

        // Firstly we try to construct the name from name + place.
        if (name != null)
            result += name;

        // We prefer to display street. If not present, try city.
        if (street != null) {
            if (result.length() > 0) result += ", ";
            result += street;
        } else
            if (city != null) {
                if (result.length() > 0) result += ", ";
                result += city;
            }

        // If we can display CP, do it!
        if (cp != null) {
            if (result.length() > 0) result += " ";
            result += cp;
        }

        // If we can display CO, do it!
        if (co != null) {
            if (co != null) result += "/";
            result += co;
        }

        if (prim instanceof Node)
            result += " " + StringUtils.latLonToString(((Node) prim).getCoor());
        else if (prim instanceof Way)
            result += " " + StringUtils.latLonToString(((Way) prim).firstNode().getCoor());

        return result;
    }

    /**
     * Sets the new parent of this element.
     *
     * <p><b>NOTE:</b> Parent is NOT notified that this element has been
     * assigned to him. This can be useful for some "ugly hacks".</p>
     */
    public void setParent(AddressElement parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent of this element.
     *
     * @return the parent element or {@code null} if no parent is present.
     */
    public AddressElement getParent() {
        return parent;
    }

    /**
     * Compares 2 elements.
     * 
     * <p>Basic criterion to comparing elements is their <i>name</i> and the
     * </i>parent</i>. Notice that this behaviour might be changed
     * in subclasses.</p>
     *
     * @return {@code false} if <i>name</i> does not match, or <i>parent</i>
     * does not match or {@code null} is given as a parameter.
     * {@code true} otherwise.
     */
    public boolean equals(AddressElement elem) {
        if (elem == null)
            return false;
        else
            return name.equals(elem.name) && this.parent == elem.parent;
    }

    /**
     * Returns the string representation of this element.
     *
     * <p>If the <i>parent</i> of the element is {@code null}, then this equals
     * to the element's <i>name</i>. Otherwise the result is created recursively
     * as <tt>"name, parent.name"</tt>.</p>
     */
    @Override
    public String toString() {
        return (parent == null) ? getName() : getName() + ", " + parent.toString();
    }


    /**
     * Compute differences between two strings.
     *
     * <p>Typical usage of this function is during {@code OsmPrimitive} to
     * {@code AddressElement} matching. This function helps to whether the given
     * field does match, does not or causes a conflict.</p>
     *
     * <p>This method returns {@code 1} if both fields do match,
     * {@code 0} if they can be matched and {@code -1} if they do not match.</p>
     *
     * <ul>
     * <li>If the {@code elemValue} is {@code null}, the result is {@code 0}.
     *     The reason for this is that the database is considered untrustworthy
     *     by default (compared to the map) and therefore a missing field
     *     in the database can be matched with an element from the map.</li>
     *
     * <li>If the {@code primValue} is {@code null}, but {@code elemValue} is
     *     not, the map should be changeded. If the database already contains
     *     a value, we trust it's right. Therefore {@code -1} is returned.</li>
     *
     * <li>When both fields are not {@code null}, they are compared by their
     *     upper-case trimmed value. {@code 1} is equals, {@code -1}
     *     otherwise.</li>
     * </ul>
     *
     * @param elemValue value of the {@link AddressElement}'s field
     * @param primValue value of the {@link OsmPrimitive}'s field
     *
     * @return {@code -1}, {@code 0} or {@code 1} (see above)
     */
    public static int matchField(String elemValue, String primValue) {

        if (elemValue == null) return  0;
        if (primValue == null) return -1;

        return (primValue.trim().toUpperCase().equals(
                elemValue.trim().toUpperCase())) ? 1 : -1;
    }

    /**
     * Does the same as {@code matchField()}, but allows the string to contain
     * abbreviations.
     *
     * <p>For detailed description see {@code matchField()}.</p>
     */
    public static int matchFieldAbbrev(String elemValue, String primValue) {

        if (elemValue == null) return  0;
        if (primValue == null) return -1;

        return StringUtils.matchAbbrev(primValue, elemValue) ? 1 : -1;
    }

    
    protected int[] getFieldMatchList(OsmPrimitive primitive) {
        int[] result = {0};        
        return result;
    }

    protected int[] getAdditionalFieldMatchList(OsmPrimitive primitive) {
        int[] result = {0};
        return result;
    }

    /**
     * Makes the given primitive have the same key-values as the current element.
     */
    public List<Proposal> getDiff(OsmPrimitive prim) {
        return null;
    }

    /**
     * Returns the "quality" of a element-primitive match.
     *
     * <p>Returns a value from {@link Reasoner}{@code .MATCH_*}, which
     * describes how well does the given primitive match to this element.</p>
     */
    public int getQ(OsmPrimitive primitive) {
        
        // Firstly get integers representing a match of every matchable field.
        int[] fieldMatches = getFieldMatchList(primitive);
        assert fieldMatches.length > 0;
        
        // Now find the max and min of this array.
        int minVal = fieldMatches[0];
        int maxVal = fieldMatches[0];
        for (int i=1; i<fieldMatches.length; i++) {
            if (minVal > fieldMatches[i])
                minVal = fieldMatches[i];
            if (maxVal < fieldMatches[i])
                maxVal = fieldMatches[i];
        }

        // Check valid results
        assert Math.abs(minVal) <= 1;
        assert Math.abs(maxVal) <= 1;
        
        // If the best among all fields is 'neutral' match, the given primitive
        // has nothing to do with our field.
        if (maxVal <= 0)
            return Reasoner.MATCH_NOMATCH;
        
        // If all fields are 1    --> ROCKSOLID MATCH
        // If some are 1, none -1 --> PARTIAL MATCH
        // If some are 1, some -1 --> CONFLICT
        switch (minVal * maxVal) {
            case -1 : return Reasoner.MATCH_CONFLICT;
            case  0 : return Reasoner.MATCH_PARTIAL;
            case +1 : return Reasoner.MATCH_ROCKSOLID;
        }
        
        return 0; // <-- just to make compilers happy. We cannot get here.
    }

    public int compareTo(AddressElement elem) {

        ParentResolver r1 = new ParentResolver(this);
        ParentResolver r2 = new ParentResolver(elem);

        int retVal = r1.compareTo(r2);
        if (retVal != 0) return retVal;

        return toString().compareTo(((AddressElement) elem).toString());
    }
}
