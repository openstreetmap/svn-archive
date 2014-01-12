package org.openstreetmap.josm.plugins.czechaddress.proposal;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;

/**
 * Helpers allowing to create {@link Proposals}s easily.
 *
 * <p>Methods from this class are typically called from
 * {@link AddressElement}{@code .getDiff()} when the user wants to copy
 * attributes from an {@code AddressElement} to {@link OsmPrimitive}.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public abstract class ProposalFactory {

    /**
     * Unifies two strings to create a perfect match.
     *
     * @param key name of the attribute (eg. highway); not used when matching,
     * just passed to newly created {@link Proposal} to inform the user
     * @param current original value of the attribute (eg. residental);
     * usually the current value of some {@link OsmPrimitive}'s attribute
     * @param target the target value of the attribute (eg. service);
     * usually the value of some {@link AddressElement}.
     * @return the {@link Proposal}, whose application unifies <i>current</i>
     * and <i>target</i>
     */
    public static Proposal getStringFieldDiff(
            String key, String current, String target) {

        if (target == null) {
            if (current != null)
                return new RemoveKeyProposal(key);
            else
                return null;
        }

        if (current == null)
            return new AddKeyValueProposal(key, target);

        if (!current.toUpperCase().equals(target.toUpperCase()))
            return new KeyValueChangeProposal(
                        key, current,
                        key, target);

        return null;
    }

    /**
     * Makes the {@code current} string contain the {@code target}.
     *
     * <p>Some attributes consist of several values, which are delimited
     * by <tt>,</tt> or <tt>;</tt>. This checks if current list contains
     * such a value and if not, it's added.</p>
     *
     * @param key name of the attribute (eg. 'source'); not used when matching,
     * just passed to newly created {@link Proposal} to inform the user
     * @param current original value of the attribute (eg. 'cuzk');
     * usually the current value of some {@link OsmPrimitive}'s attribute
     * @param target a value that {@code current} should contain (eg. 'mvcr');
     * @return the {@link Proposal}, whose application unifies includes
     * {@code current} in {@code target} (eg. modify 'source' to
     * 'source=cuzk;mvcr')
     */
    public static Proposal getListFieldDiff(
            String key, String current, String target) {

        if (target == null)
            return null;

        if (current == null)
            return new AddKeyValueProposal(key, target);

        for (String itemRaw : current.split(","))
            for (String itemSplitted : itemRaw.split(";"))
                if (itemSplitted.trim().equals(target.trim()))
                    return null;

        return new KeyValueChangeProposal(
                     key, current,
                     key, current + ";" + target);
    }
}
