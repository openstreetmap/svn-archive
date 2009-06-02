package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import java.util.List;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.NotNullList;
import org.openstreetmap.josm.plugins.czechaddress.PrimUtils;
import org.openstreetmap.josm.plugins.czechaddress.StringUtils;
import org.openstreetmap.josm.plugins.czechaddress.proposal.Proposal;

import static org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalFactory.getStringFieldDiff;
import static org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalFactory.getListFieldDiff;

/**
 * Represents a single house.
 *
 * <p> Every house contains a number within the street
 * (číslo orientační) or the number within the suburb (číslo popisné).
 * Both numbers are stored as string to allow house numbers like '1a'.</p>
 *
 * <p><b>WARNING:</b> Every house must have a parent, whose type is
 * {@link ElementWithHouses}. The {@code setParent()} method should
 * be called immediatelly after the constructor.</p>
 * 
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class House extends AddressElement {

    /** Stores the number unique in a suburb (číslo popisné). */
    protected String cp = null;
    /** Stores the number unique in a street (číslo orientační). */
    protected String co = null;

    /**
     * Default constructor setting the numbers of this House.
     *
     * @param cp number unique in a suburb (číslo popisné)
     * @param co number unique in a street (číslo orientační)
     */
    public House(String cp, String co) {
        // Firstly we pretend that this element has no name...
        super("");

        if (cp != null) this.cp = cp.toLowerCase();
        if (co != null) this.co = co.toLowerCase();

        assert (co != null) || (cp != null);
        
        //... but the the name is overwritten.
        this.name = generateName(this.cp, this.co);
    }
    
    /**
     * Returns the number unique in a suburb (číslo popisné)
     */
    public String getCP() {
        return cp;
    }

    /**
     * Returns the number unique in a street (číslo orientační)
     */
    public String getCO() {
        return co;
    }

    /**
     * Generates a name from house numbers.
     *
     * <p><b>WARNING:</b> Only one number can be {@link null}.</p>
     *
     * <p>If only one number is given, it is returned directly. If both numbers
     * are given, they are combined in the way, which is standard in CZ.</p>
     *
     * @param cp number unique in a suburb (číslo popisné)
     * @param co number unique in a street (číslo orientační)
     * @return string representation of a house with given house numbers.
     */
    public static String generateName(String cp, String co) {

        if ((cp != null) && (co != null))
            return cp + "/" + co;

        if (co != null) return co;
        if (cp != null) return cp;

        assert false;
        return ""; // <-- just to make compiler happy.
    }

    /**
     * Returns the full name of this house (including street/suburb).
     *
     * <p><b>WARNING:</b> Every house must have a parent, whose type is
     * {@link ElementWithHouses}. The {@code setParent()} method should
     * be called immediatelly after the constructor.</p>
     */
    @Override
    public String getName() {
        assert parent != null : "A house must always have a parent.";
        return parent.getName() + " " + name;
    }

    /**
     * Sets the parent of this house with type-checking.
     *
     * <p><b>WARNING:</b> Every house must have a parent, whose type is
     * {@link ElementWithHouses}. The {@code setParent()} method should
     * be called immediatelly after the constructor.</p>
     */
    @Override
    public void setParent(AddressElement parent) {
        assert parent instanceof ElementWithHouses;
        super.setParent(parent);
    }
    
    /**
     * Returns the parent of this house with type-checking.
     *
     * <p><b>WARNING:</b> Every house must have a parent, whose type is
     * {@link ElementWithHouses}. The {@code setParent()} method should
     * be called immediatelly after the constructor.</p>
     */
    @Override
    public ElementWithHouses getParent() {
        assert parent instanceof ElementWithHouses;
        return (ElementWithHouses) parent;
    }

    /**
     * Returns an array describing how the house fits to given primitive.
     *
     * <p>Values of the array are described in
     * {@link AddressElement}{@code .getFieldMatchList()}.</p>
     *
     * <p>First elemtn of the returned array corresponds to the CP
     * (číslo popisné), the second one to the combination of Street+CO.</p>
     */
    @Override
    protected int[] getFieldMatchList(OsmPrimitive prim) {
        int[] result = {0, 0};
                
        // First field is the AlternateNubmer
        result[0] = matchField(this.cp, prim.get(PrimUtils.KEY_ADDR_CP));
        
        // Second field is the Housenumber
        if (parent instanceof Street)
            result[1] = Math.min(
                matchFieldAbbrev(parent.getName(), prim.get(PrimUtils.KEY_ADDR_STREET)),
                matchField(      this.co,          prim.get(PrimUtils.KEY_ADDR_CO)) );
        return result;
    }
    
    /**
     * Gives all proposals to make the primitive be an address primitive.
     *
     * <p>Tries to fill {@code prim}'s attributes to have all attributes that
     * this House has.</p>
     */
    @Override
    public List<Proposal> getDiff(OsmPrimitive prim) {
        
        List<Proposal> props = new NotNullList<Proposal>();
        ParentResolver pr = new ParentResolver(this);

        props.add(getStringFieldDiff(PrimUtils.KEY_ADDR_CP, prim.get(PrimUtils.KEY_ADDR_CP), getCP()));
        props.add(getStringFieldDiff(PrimUtils.KEY_ADDR_CO, prim.get(PrimUtils.KEY_ADDR_CO), getCO()));

        props.add(getStringFieldDiff(PrimUtils.KEY_ADDR_COUNTRY,
                            prim.get(PrimUtils.KEY_ADDR_COUNTRY), "CZ"));

        if (pr.parentStreet != null)
            props.add(getStringFieldDiff(PrimUtils.KEY_ADDR_STREET,
                                prim.get(PrimUtils.KEY_ADDR_STREET),
                                pr.parentStreet.getName()));

        if (parent.parent != null) // For sure our parent is a ElemWithStreets
            props.add(getStringFieldDiff(PrimUtils.KEY_IS_IN,
                                prim.get(PrimUtils.KEY_IS_IN),
                                parent.parent.getIsIn()));

        // If we have added any proposal so far, add the source info as well.
        if (props.size() > 0)
            props.add(getListFieldDiff("source:addr", prim.get("source:addr"), "mvcr:adresa"));

        return props;
    }

    public static boolean isMatchable(OsmPrimitive prim) {
        
        for (String key : prim.keySet()) {
            String value = prim.get(key);
            if (value != null && value.startsWith("addr:"))
                return true;
        }
        return false;
    }

    @Override
    public int compareTo(AddressElement o) {
        // Most important criterion is the street
        int val = super.compareTo(o);
        if (val != 0) return val;
        if (!(o instanceof House)) return val;

        House house = (House) o;

        // Second most important is the "CO"
        if (co != null && house.co != null)
            val = co.compareTo(house.co);
        if (val != 0) return val;

        // Third most important is the "CP"
        if (cp != null && house.cp != null)
            val = cp.compareTo(house.cp);
        return val;
    }
}
