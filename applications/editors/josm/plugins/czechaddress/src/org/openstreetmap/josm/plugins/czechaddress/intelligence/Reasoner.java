package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.NotNullList;
import org.openstreetmap.josm.plugins.czechaddress.StatusListener;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalContainer;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalDatabase;

/**
 * Intended to concentrate all intelligence of
 * {@link House}-{@link OsmPrimitive} matching.
 *
 * <p>HouseReasoner holds the relations between House and OsmPrimitive
 * and also tries to keep it consistent with the state of the map.
 * Firstly it is initialised by {@code initReasoner()} and subsequently and
 * change done to the map should be reflected in HouseReasoner by calling
 * {@code addPrimitive} or {@code removePrimitive}.</p>
 *
 * <p><b>NOTE:</b> Currently there is no known way of adding a hook into JOSM
 * to detect changed or deleted elements. Therefore every call of
 * {@code ensureConsistency()} checks for deleted elements, which is rather
 * inefficient. Moreover there is a listener for selectionChanged, which
 * checks deselcted items for their change. Again, inefficient.</p>
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class Reasoner {

    /** A database of all matches, which are not in a conflict. */
    private NotNullList<Match> matches = new NotNullList<Match>();

    /** A list of {@link Match}es, which are in a conflict. */
    private NotNullList<Match> conflicts = new NotNullList<Match>();

    /** A list of {@link House}s, which have not yet been matched to any
     * {@link OsmPrimitive} without a conflict. */
    private ArrayList<AddressElement> elemPool = new ArrayList<AddressElement>();

    /** A list of {@link OsmPrimitive}s, for which there was no suitable match. */
    private List<OsmPrimitive> notMatchable = new ArrayList<OsmPrimitive>();

    private HashMap<OsmPrimitive,   Match> primMatchHashIndex
      = new HashMap<OsmPrimitive,   Match>();
    private HashMap<AddressElement, Match> elemMatchHashIndex
      = new HashMap<AddressElement, Match>();

    private List<OsmPrimitive>   primConflictListIndex = new ArrayList<OsmPrimitive>();
    private List<AddressElement> elemConflictListIndex = new ArrayList<AddressElement>();
    private HashMap<OsmPrimitive,   List<Match>> primConflictHashIndex
      = new HashMap<OsmPrimitive,   List<Match>>();
    private HashMap<AddressElement, List<Match>> elemConflictHashIndex
      = new HashMap<AddressElement, List<Match>>();


    /**
     * Default constructor, which initializes the pool of
     * {@link AddressElement}s to be used for matching.
     */
    public Reasoner(ArrayList<AddressElement> elementPool) {
        elemPool = elementPool;
    }    
    
    /**
     * Adds a single new primitive to the database, finds corresponding
     * {@link House} for it and seeks for potential conflicts.
     * 
     * <p>If you intend to add a list of new primitives, {@link addPrimitives}
     * will be faster, because conflicts are traced after adding all the
     * primitives and not after every single one.</p>
     *
     * <p>Moreover there is one substantial difference discussed in
     * {@link addPrimitives()}.</p>
     *
     * @param newPrimitive new primitive to be added
     */
    public synchronized void addPrimitive(OsmPrimitive newPrimitive) {

        int firstNewIndex = matches.size();

        matchPrimitive(newPrimitive);
        
        ensureConsistency(firstNewIndex);
    }

    /**
     * Adds new primitives to the database, finds suitable {@link House}s for
     * them and afterwards seeks for potential conflicts.
     *
     * <p><b>NOTE:</b> Adding primitives through {@code addPrimitives()} has
     * different effect from adding primitives one by one through
     * {@code addPrimitive()}.</p>
     * 
     * <p>The preffered way of handling this is to add a large bunch of elements
     * from the database via {@code addPrimitives()}, because the possible
     * conflicts are between all possible element-primitive combinations.
     * For adding single node by user, {@code addPrimitives()} is the preferred
     * way, because immediatelly after the matching the best suitable element
     * is removed from the element pool and it cannot be matched to any other
     * primitive.</p>
     */
    public synchronized void addPrimitives(Collection<OsmPrimitive> newPrimitives) {

        int firstNewIndex = matches.size();

        for (OsmPrimitive primitive : newPrimitives)
            matchPrimitive(primitive);

        ensureConsistency(firstNewIndex);
    }

    /**
     * Removes the given primitive from the database and cancels all conflicts
     * which can be solved by this removal.
     */
    public synchronized void removePrimitive(OsmPrimitive primitive) {

        // TODO: Implement the removal of a primitive
    }

    /**
     * Forcibly matches given house to given primitive.
     *
     * <p>This can be useful in cases, where the user manually resolves
     * a conflict by stating that this particular combination of
     * house-primitive it the correct one.</p>
     *
     * <p>The quality is set to {@link Match}{@code .MATCH_OVERWRITE}.</p>
     */
    public synchronized void overwriteMatch(AddressElement elem, OsmPrimitive prim) {

        int firstNewIndex = matches.size();

        matches.add(new Match(elem, prim, Match.MATCH_OVERWRITE));
        matchesDirty = true;

        reconsider(elem);
        reconsider(prim);

        ensureConsistency(firstNewIndex);
    }

    /**
     * Returns all elements available for matching.
     */
    public List<AddressElement> getElementPool() {
        return elemPool;
    }

    /** Returns all primitives, which have not been assigned any element during
     * the matching. */
    public List<OsmPrimitive> getNotMatchable() {
        return notMatchable;
    }

    public List<Match> getAllMatches() {

        return matches;
    }

    public Match findMatch(OsmPrimitive prim) {
        return primMatchHashIndex.get(prim);
    }

    public Match findMatch(AddressElement elem) {
        return primMatchHashIndex.get(elem);
    }

    /**
     * Finds given primitive in list of {@code  matches} and returns
     * the corresponding element.
     * 
     * <p>If the primitive is not found, {@code null} is returned.</p>
     */
    public AddressElement translate(OsmPrimitive prim) {
        Match m = primMatchHashIndex.get(prim);
        if (m == null) return null;
        return m.elem;
    }

    /**
     * Finds given element in list of {@code matches} and returns the corresponding
     * primitive.
     *
     * <p>If the element is not found, {@code null} is returned.</p>
     */
    public OsmPrimitive translate(AddressElement elem) {
        Match m = elemMatchHashIndex.get(elem);
        if (m == null) return null;
        return m.prim;
    }

    /**
     * Returns a list all conflicts corresponding to the given {@link AddressElement}.
     */
    public List<Match> getConflicts(AddressElement elem) {
        return elemConflictHashIndex.get(elem);
    }

    /**
     * Returns a list all conflicts corresponding to the given {@link OsmPrimitive}.
     */
    public List<Match> getConflicts(OsmPrimitive prim) {
        return primConflictHashIndex.get(prim);
    }

    /**
     * Returns a sorted list of all elements in conflict.
     */
    public List<AddressElement> getElementsInConflict() {
        return elemConflictListIndex;
    }

    /**
     * Returns a sorted list of all primitives in conflict.
     */
    public List<OsmPrimitive> getPrimitivesInConflict() {
        return primConflictListIndex;
    }

    /**
     * Returns the list of all conflicts.
     */
    public List<Match> getAllConflicts() {
        return conflicts;
    }

    /**
     * Goes through all matches and returns proposals for changing
     * every primitive (if it differs from the matched element).
     */
    public ProposalDatabase getProposals() {

        ProposalDatabase proposals = new ProposalDatabase();

        for (Match match : matches) {
            
            ProposalContainer proposalContainer
                    = new ProposalContainer(match.prim);
            
            proposalContainer.addProposals(match.getDiff());

            if (proposalContainer.getProposals().size() > 0)
                proposals.addContainer(proposalContainer);
        }

        return proposals;
    }

    /**
     * Finds suitable {@code Match}es in the pool of {@link AddressElement}s.
     *
     * @param primitive the primitive to be matched with elements of
     * {@code elemPool}.
     * @return the list of all matches, whose <i>quality</i> &gt;
     * {@link Match}{@code .MATCH_NOMATCH}.
     */
    public NotNullList<Match> getMatchesForPrimitive(OsmPrimitive primitive) {

        NotNullList<Match> result = new NotNullList<Match>();
        
        for (AddressElement elem : elemPool)
            result.add(Match.createMatch(elem, primitive));
        return result;
    }

    /**
     * Method for adding matches for a single primitive into the resoner.
     *
     * <p>This method uses {@code getMatchesForPrimitive()} for getting the
     * list of suitable matches for the given primitive. Then it selects
     * <u>all</u> matches with the highest {@code quality} and adds them
     * into the {@code matches} list.
     */
    protected void matchPrimitive(OsmPrimitive prim) {
        boolean assertions = false;
        assert  assertions = true;

        if (prim.deleted) return;

        NotNullList<Match> suitable = getMatchesForPrimitive(prim);
        NotNullList<Match> toDelete = new NotNullList<Match>(suitable.size());

        for (Match match1 : suitable)
            for (Match match2 : suitable)
                if (   match1        != match2
                    && match1.quality > match2.quality) {
                    toDelete.add(match2);
                    if (assertions)
                        System.out.println("Reasoner: Dominated match: " + match2.toString());
                }

        suitable.removeAll(toDelete);

        // Make sure we reconsider all elements.
        for (Match match : matches) {
            reconsider(match.elem);
            reconsider(match.prim);
        }

        if (suitable.size() > 0) {
            matches.addAll(suitable);
            matchesDirty = true;
        } else
            notMatchable.add(prim);
    }

//==============================================================================
//  MESSAGE HANDLING SYSTEM
//==============================================================================

    /**
     * Should be true whenever {@code matches} have changed, but the
     * {@link StatusListener}{@code .MESSAGE_MATCHES_CHANGED} message has
     * not yet been sent.
     */
    protected boolean matchesDirty = false;

    /**
     * Should be true whenever {@code conflicts} have changed, but the
     * {@link StatusListener}{@code .MESSAGE_CONFLICT_CHANGED} message has
     * not yet been sent.
     */
    protected boolean conflictsDirty = false;

    /**
     * Broadcasts information about the changes of reasoner status.
     *
     * <p>If the {@code matchesDirty} or {@code conflictsDirty} flag is
     * {@link true}, this method informs all listeners about the change
     * of plugin's status.</p>
     */
    protected void handleDirt() {

        if (matchesDirty || conflictsDirty)
            regenerateIndexes();

        if (matchesDirty) CzechAddressPlugin.broadcastStatusChanged(
                StatusListener.MESSAGE_MATCHES_CHANGED);

        if (conflictsDirty) CzechAddressPlugin.broadcastStatusChanged(
                StatusListener.MESSAGE_CONFLICT_CHANGED);

        matchesDirty = conflictsDirty = false;
    }

//==============================================================================
//  CONSISTENCY CHECKING
//==============================================================================

    /**
     * Should be called after modifying <b>anything</b> to put reasoner into
     * consistent state.
     *
     * <p>This method should be called at the end of every public method,
     * which changes anything in the reasoner.</p>
     */
    public void ensureConsistency() {
        ensureConsistency(0);
    }

    /**
     * Should be called after modifying <b>anything</b> to put reasoner into
     * consistent state.
     *
     * <p>This method should be called at the end of every public method,
     * which changes anything in the reasoner.</p>
     *
     * <p>Checking every match with every match is computationally inefficient.
     * Therefore by specifying {@code startElementIndex > 0}, we can
     * reduce this cost by checking <i>every match</i> with <i>every match,
     * whose index is greater than {@code startElementIndex}.</p>
     */
    public void ensureConsistency(int startElementIndex) {
        startElementIndex = 0;

        NotNullList<Match> toDel = new NotNullList<Match>(10);
        for (Match match1 : matches) for (Match match2 : matches) {

            if (match1 == match2) continue;

            if (match1.prim == match2.prim && match1.quality > match2.quality) {
                System.out.println("Reasoner: Redundancy clean: " + match2);
                toDel.add(match2);
                matchesDirty = true;
            }

            if (match1.prim == match2.prim && match1.quality >= match2.quality
             && match1.elem == match2.elem && !toDel.contains(match1)) {
                System.out.println("Reasoner: Hyper redundancy: " + match2);
                toDel.add(match2);
                matchesDirty = true;
            }
        }
        matches.removeAll(toDel);

        toDel.clear();
        for (Match conflict1 : conflicts) for (Match conflict2 : conflicts) {

            if (conflict1 == conflict2) continue;

            if (conflict1.prim == conflict2.prim
             && conflict1.elem == conflict2.elem && !toDel.contains(conflict1)) {
                System.out.println("Reasoner: Confl redundancy: " + conflict2);
                toDel.add(conflict2);
                conflictsDirty = true;
            }
        }
        conflicts.removeAll(toDel);

        for (Match match : matches)
            for (Match conflict : conflicts) {
                assert match      != conflict;
                assert match.prim != conflict.prim;
            }



        if (handleDeletedPrimitivesSlowButSafe())
            startElementIndex = 0;
        handleInconsistentMatches(startElementIndex);
        handleDirt();

        CzechAddressPlugin.broadcastStatusChanged(
                                    StatusListener.MESSAGE_REASONER_REASONED);
    }

    /**
     * Finds conflicts in the {@code matches} list and moves them to
     * {@code conflicts} list.
     */
    protected void handleInconsistentMatches(int startElementIndex) {
        boolean assertions = false;
        assert  assertions = true;


        // Move all conflicting matches into 'conflicts' array.
        int pos1 = 0;
        while (pos1 < matches.size()) {

            int pos2 = Math.max(pos1 + 1, startElementIndex);
            while (pos2 < matches.size()) {

                Match item1 = matches.get(pos1);
                Match item2 = matches.get(pos2);

                if ((item1.elem == item2.elem) || (item1.prim == item2.prim)) {

                    if (assertions) {
                        System.out.println("1. match in conflict: " + item2);
                        System.out.println("2. match in conflict: " + item1);
                    }

                    if (item1.quality >= item2.quality) {
                        if (assertions)
                            System.out.println("1. match moved to 'conflicts'.");
                        matches.remove(pos2);
                        conflicts.add(item2);
                        matchesDirty = conflictsDirty = true;
                        pos2--;
                    }

                    if (item1.quality <= item2.quality) {
                        if (assertions) {
                            System.out.println("2. match moved to 'conflicts'.");
                            System.out.println("----------------------------------------------------------------------");
                        }
                        matches.remove(pos1);
                        conflicts.add(item1);
                        matchesDirty = conflictsDirty = true;
                        pos1--;
                        break;
                    }

                    if (assertions)
                        System.out.println("----------------------------------------------------------------------");
                }

                pos2++;
            }
            pos1++;
        }
    }

    /**
     * Seeks and handles primitives, which have been deleted.
     *
     * <p>If a primitive has been deleted, the first immediate action is to
     * remove it from the {@code matches} and {@code conflicts} lists.
     * However there might be another conflict, which was caused by the
     * deleted primitive. This method finds such conflicts and
     * moves them back to {@code matches} list to
     * be reconsideres later.</p>
     */
    protected boolean handleDeletedPrimitives() {

        boolean somethingChanged = false;
        NotNullList<OsmPrimitive> blockers = new NotNullList<OsmPrimitive>();

        // Firstly create a list of all primitives, which were deleted.
        for (Match match : matches)
            if (match.prim.deleted)
                blockers.add(match.prim);

        for (Match conflict : conflicts)
            if (conflict.prim.deleted)
                blockers.add(conflict.prim);

        // Now for every deleted primitive...
        int i=0;
        while (i < blockers.size()) {
            OsmPrimitive blocker = blockers.get(i);

            // ... remove its' entries in the 'matches' ...
            if (primMatchHashIndex.get(blocker) != null) {
                Match toRemove = primMatchHashIndex.get(blocker);
                matches.remove(toRemove);
                primMatchHashIndex.remove(toRemove.prim);
                elemMatchHashIndex.remove(toRemove.elem);
                somethingChanged = matchesDirty = true;
            }

            // ... and reconsider all 'conflicts', which may have been caused
            // by the deleted primitive. We must do it recursively...

            // Every conflict, which has the 'blocker' as a primitive
            if (primConflictHashIndex.get(blocker) != null) {
                for (Match match : primConflictHashIndex.get(blocker)) {
                    // Find the correspoding element and find all primitives,
                    // which are mapped to this element.
                    if (elemConflictHashIndex.get(match.elem) != null) {
                        for (Match novy : elemConflictHashIndex.get(match.elem)) {
                            // If this primitive has not yet been handled, do it!
                            if (!blockers.contains(novy.prim))
                                blockers.add(novy.prim);
                            // And move the conflict back to 'matches' for reconsideration.
                            if (!novy.prim.deleted && !matches.contains(novy)) {
                                matches.add(novy);
                                somethingChanged = matchesDirty = true;
                            }
                        }
                        // Finally remove the reconsidered conflicts...
                        conflicts.removeAll(elemConflictHashIndex.get(match.elem));
                        elemConflictHashIndex.remove(match.elem);
                        elemConflictListIndex.remove(match.elem);
                        somethingChanged = conflictsDirty = true;
                    }
                    // [move the conflict back to 'matches' for reconsideration]
                    if (!match.prim.deleted && !matches.contains(match)) {
                        matches.add(match);
                        somethingChanged = matchesDirty = true;
                    }
                }
                // ...and once again remove reconsidered conflicts.
                conflicts.removeAll(primConflictHashIndex.get(blocker));
                primConflictHashIndex.remove(blocker);
                primConflictListIndex.remove(blocker);
                somethingChanged = conflictsDirty = true;
            }
            i++;
        }

        return somethingChanged;
    }

    /**
     * Seeks and handles primitives, which have been deleted.
     *
     * <p>Slower, but safer counterpart of {@code handleDeletedPrimitives))}.</p>
     */
    protected boolean handleDeletedPrimitivesSlowButSafe() {

        boolean fire = false;

        for (Match match : matches)
            fire |= match.qualityChanged();

        for (Match conflict : conflicts)
            fire |= conflict.qualityChanged();

        if (fire) {
            matches.addAll(conflicts);
            conflicts.clear();
            matchesDirty = conflictsDirty = true;

            int i=0;
            while (i<matches.size()) {
                Match match = matches.get(i);
                if (match.quality <= Match.MATCH_NOMATCH) {
                    System.out.println("Reasoner: Deleting " + matches.get(i));
                    matches.remove(i);

                    assert fire;
                    assert !matches.contains(match);
                } else
                    i++;
            }
        }

        return fire;
    }

    protected void reconsider(OsmPrimitive prim) {

        List<Match> reconsider;

        reconsider = getConflicts(prim);
        if (reconsider != null) {
            matches.addAll(reconsider);
            conflicts.removeAll(reconsider);
            matchesDirty = conflictsDirty = true;
        }
    }

    protected void reconsider(AddressElement elem) {

        List<Match> reconsider;

        reconsider = getConflicts(elem);
        if (reconsider != null) {
            matches.addAll(reconsider);
            conflicts.removeAll(reconsider);
            matchesDirty = conflictsDirty = true;
        }
    }

    /**
     * Recreates all {@code *Index} fields if this reasoner.
     */
    protected void regenerateIndexes() {
        boolean assertions = false;
        assert  assertions = true;

        elemMatchHashIndex.clear();
        primMatchHashIndex.clear();
        elemConflictHashIndex.clear();
        primConflictHashIndex.clear();
        elemConflictListIndex.clear();
        primConflictListIndex.clear();

        for (Match match : matches) {
            elemMatchHashIndex.put(match.elem, match);
            primMatchHashIndex.put(match.prim, match);
        }

        for (Match conflict : conflicts) {

            List<Match> elemConflicts = elemConflictHashIndex.get(conflict.elem);
            if (elemConflicts == null) {
                elemConflicts = new ArrayList<Match>();
                elemConflictHashIndex.put(conflict.elem, elemConflicts);
            }
            elemConflicts.add(conflict);

            List<Match> primConflicts = primConflictHashIndex.get(conflict.prim);
            if (primConflicts == null) {
                primConflicts = new ArrayList<Match>();
                primConflictHashIndex.put(conflict.prim, primConflicts);
            }
            primConflicts.add(conflict);
        }

        for (AddressElement elem : elemConflictHashIndex.keySet())
            elemConflictListIndex.add(elem);

        for (OsmPrimitive prim : primConflictHashIndex.keySet())
            primConflictListIndex.add(prim);

        assert elemConflictHashIndex.size() == elemConflictListIndex.size();
        assert primConflictHashIndex.size() == primConflictListIndex.size();

        for (AddressElement elem : elemConflictListIndex)
            assert elemConflictHashIndex.get(elem) != null;
        for (OsmPrimitive prim : primConflictListIndex)
            assert primConflictHashIndex.get(prim) != null;

        if (assertions) {
            System.out.println("Spárovaných dvojic: " + String.valueOf(matches.size()));
            System.out.println("Konfliktů (celkem): " + String.valueOf(conflicts.size())
                    + "; " + String.valueOf(elemConflictListIndex.size())
                    + "+"  + String.valueOf(primConflictListIndex.size())
                    + " elementů+primitiv");
        }
    }
}
