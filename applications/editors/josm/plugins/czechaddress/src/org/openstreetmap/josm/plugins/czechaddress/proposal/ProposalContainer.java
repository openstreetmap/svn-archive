package org.openstreetmap.josm.plugins.czechaddress.proposal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Class encapsulating an {@link OsmPrimitive} and a list of proposed
 * {@link Proposal}.
 *
 * List of these objects are usually stored in a {@link ProposalDatabase},
 * which also provides means of displaying is in a {@link JTree}.
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 * @see ProposalDatabase
 * @see OsmPrimitive
 * @see Proposal
 */
public class ProposalContainer implements ListModel {


    /**
     * List of listeners for implementing the {@code ListModel}.
     */
    private List<ListDataListener> listeners =
            new ArrayList<ListDataListener>();

    /**
     * The internal reference to an {@link OsmPrimitive}, to which the
     * proposals shall be applied.
     */
    protected OsmPrimitive target;

    /**
     * Default constructor setting the internal reference to target
     * {@code OsmPrimitive}.
     */
    public ProposalContainer(OsmPrimitive target) {
        this.target = target;
    }

    /**
     * Sets a new target, to which the {@link Proposal}s should be applied.
     */
    public void setTarget(OsmPrimitive newTarget) {
        this.target = newTarget;
    }

    /**
     * Returns the target, to which the {@link Proposal}s should be applied.
     */
    public OsmPrimitive getTarget() {
        return target;
    }

//==============================================================================

    /**
     * The list of proposals to be applied to encapsulated primitive.
     */
    protected List<Proposal> proposals
            = new ArrayList<Proposal>();

    /**
     * Adds a new {@link Proposal}.
     * @param a new alternation to be added
     */
    public void addProposal(Proposal a) {
        proposals.add(a);

        ListDataEvent evt = new ListDataEvent(
                this,
                ListDataEvent.INTERVAL_ADDED,
                proposals.size()-1,
                proposals.size()-1);

        for (ListDataListener l : listeners)
            l.contentsChanged(evt);
    }

    /**
     * Adds a new set of {@link Proposal}s.
     * @param a collection of proposals to be added
     */
    public void addProposals(Collection<Proposal> a) {

        int index1 = proposals.size();
        proposals.addAll(a);
        int index2 = proposals.size()-1;

        ListDataEvent evt = new ListDataEvent(
                this,
                ListDataEvent.INTERVAL_ADDED,
                index1, index2);

        for (ListDataListener l : listeners)
            l.contentsChanged(evt);
    }


    /**
     * Removes the give proposal from the list of proposals.
     * @param proposal the proposal to be removed
     */
    public void removeProposal(Proposal proposal) {

        int index = proposals.indexOf(proposal);
        if (index == -1)
            return;

        proposals.remove(index);

        ListDataEvent evt = new ListDataEvent(
                this,
                ListDataEvent.INTERVAL_REMOVED,
                index, index);

        for (ListDataListener l : listeners)
            l.contentsChanged(evt);
    }

    /**
     * Replaces the internal list of proposals with the given one.
     * @param a collection of proposals to be added
     */
    public void setProposals(List<Proposal> proposals) {
        this.proposals = proposals;

        ListDataEvent evt = new ListDataEvent(
                this, ListDataEvent.CONTENTS_CHANGED,
                0, proposals.size()-1);

        for (ListDataListener l : listeners)
            l.contentsChanged(evt);
    }

    /**
     * @return the list of proposed proposals.
     */
    public List<Proposal> getProposals() {
        return proposals;
    }

    /**
     * Removes all proposals.
     */
    public void clear() {
        ListDataEvent evt = new ListDataEvent(
                this,
                ListDataEvent.INTERVAL_REMOVED,
                0, proposals.size()-1);

        proposals.clear();

        for (ListDataListener l : listeners)
            l.contentsChanged(evt);
    }

    /**
     * Applies all stored {@link Proposal}s to the target {@link OsmPrimitive}.
     */
    public void applyAll() {
        for (Proposal proposal : proposals)
            proposal.apply(target);
    }

//==============================================================================

    /**
     * Tries to decode the name of the referenced primitive. Otherwise
     * it returns the ID of that primitive.
     *
     * Currently the string is in Czech language (see {@link CzechAddressPlugin}).
     */
    @Override
    public String toString() {
        /*if (target.keySet().contains("name"))
            return target.get("name");

        if (   target.keySet().contains("addr:alternatenumber")
            || target.keySet().contains("addr:housenumber")) {

            String cp = target.get("addr:alternatenumber");
            String co = target.get("addr:housenumber");
            String ul = target.get("addr:street");

            if (cp == null) cp = "?";
            if (co == null) co = "?";
            if (ul == null) ul = "" ; else ul = " " + ul;

            return "Dům " + String.valueOf(cp) + "/" + String.valueOf(co) + ul;
        }*/

        return target.toString();
    }

    public int getSize() {
        return proposals.size();
    }

    public Object getElementAt(int index) {
        return proposals.get(index);
    }

    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

}
