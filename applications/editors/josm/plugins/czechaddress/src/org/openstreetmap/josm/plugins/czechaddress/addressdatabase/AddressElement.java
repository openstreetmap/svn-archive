package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import java.util.List;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.czechaddress.StringUtils;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Match;
import org.openstreetmap.josm.plugins.czechaddress.proposal.Proposal;

/**
 * The most general element of the houses and streets database.
 *
 * <p>Every element must hav a name and may have a parent element.</p>
 * 
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public abstract class AddressElement {

    protected String name;
    protected AddressElement parent = null;

    protected static final String KEY_ADDR_CP      = "addr:alternatenumber";
    protected static final String KEY_ADDR_CO      = "addr:housenumber";
    protected static final String KEY_ADDR_STREET  = "addr:street";
    protected static final String KEY_ADDR_CITY    = "addr:city";
    protected static final String KEY_ADDR_COUNTRY = "addr:country";
    protected static final String KEY_IS_IN        = "is_in";
    protected static final String KEY_NAME         = "name";

    /**
     * Constructor setting the name of this element.
     *
     * <p><b>TODO:</b> The name is capitalized during the assignment,
     * because the database contains all names IN CAPITALS. However
     * this should not be done here, but in the {@link DatabaseParser}.</p>
     */
    public AddressElement (String name) {
        if (name == null)
            throw new NullPointerException("You must specify the name of this AddressElement");
        this.name = capitalize(name);
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
        String cp     = prim.get("addr:alternatenumber");
        String co     = prim.get("addr:housenumber");
        String street = prim.get("addr:street");
        String city   = prim.get("addr:city");
        String name   = prim.get("name");

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
            result += " " + StringUtils.latLonToString(((Node) prim).coor);
        else if (prim instanceof Way)
            result += " " + StringUtils.latLonToString(((Way) prim).firstNode().coor);

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
     * Capitalizes the given string (first letter of every word upper-case,
     * others lower-case). Czech grammar rules are more or less obeyed.
     *
     * <p><b>TODO:</b> This should be moved somewhere else.</p>
     * 
     * @param s string to be capitalized
     * @return capitaized string
     */
    protected static String capitalize(String s) {

        if (s == null)
            return s;

        String result = "";

        char last = ' ';
        for (char ch : s.toCharArray()) {
            if ((last >= 'a') && (last <= 'ž') ||
                (last >= 'A') && (last <= 'Ž'))
                ch = Character.toLowerCase(ch);
            else
                ch = Character.toTitleCase(ch);

            last = ch;
            result = result + ch;
        }

        String[] noCapitalize = { "Nad", "Pod", "U", "Na" };
        for (String noc : noCapitalize)
            result = result.replaceAll(" "+noc+" ", " "+noc.toLowerCase()+" ");

        return result;
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
    
    
    protected int[] getFieldMatchList(OsmPrimitive primitive) {
        int[] result = {0};        
        return result;
    }


    public List<Proposal> getDiff(OsmPrimitive prim) {
        return null;
    }

    
    public int getMatchQuality(OsmPrimitive primitive) {
        
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
            return Match.MATCH_NOMATCH;
        
        // If all fields are 1    --> ROCKSOLID MATCH
        // If some are 1, none -1 --> PARTIAL MATCH
        // If some are 1, some -1 --> CONFLICT
        switch (minVal * maxVal) {
            case -1 : return Match.MATCH_CONFLICT;
            case  0 : return Match.MATCH_PARTIAL;
            case +1 : return Match.MATCH_ROCKSOLID;   
        }
        
        return 0; // <-- just to make compilers happy. We cannot get here.
    }



    public String getIsIn() {
        return getIsIn(null);
    }

    protected String getIsInName() {
        return getName();
    }

    private String getIsIn(String childString) {

        String result = "";

        if (getIsInName() != null  &&  !getIsInName().equals(childString)) {
            result += getIsInName() + ", ";
        }

        if (parent != null)
            result += parent.getIsIn(getIsInName());
        else
            result += "CZ";
        
        return result;
    }
}
