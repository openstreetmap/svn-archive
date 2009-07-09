package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalContainer;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalDatabase;

/**
 * Intended to concentrate all intelligence of
 * {@link AddressElement}-{@link OsmPrimitive} matching.
 *
 * <p>Reasoner holds the relations between AddressElement and OsmPrimitive
 * and also tries to keep it consistent with the state of the map.</p>
 *
 * <p>You can imagine data model as a big matrix, whose rows consist of
 * {@code AddressElement}s and columns are {@code OsmPrimitive}s. The cell
 * of this matrix is a so-called "quality", which says how well the primitive
 * and element fit together (see {@code MATCH_*} for details).
 * Through the documentation we will use <tt><b>Q(prim, elem)</b></tt> notation
 * for this matrix. The reasoner is memory-efficient iff most of the Q values
 * are equal to {@code MATCH_NOMATCH}.</p>
 *
 * <p><b>NOTE:</b> Currently there is no known way of adding a hook into JOSM
 * to detect changed or deleted elements. Therefore there is a
 * {@link SelectionMonitor}, which passes every selected primitive to
 * the reasoner.</p>
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class Reasoner {

    public static final int MATCH_OVERWRITE = 4;
    public static final int MATCH_ROCKSOLID = 3;
    public static final int MATCH_PARTIAL   = 2;
    public static final int MATCH_CONFLICT  = 1;
    public static final int MATCH_NOMATCH   = 0;

    private     Map<OsmPrimitive, AddressElement> primBestIndex
      = new HashMap<OsmPrimitive, AddressElement> ();
    private     Map<AddressElement, OsmPrimitive> elemBestIndex
      = new HashMap<AddressElement, OsmPrimitive> ();

    private     Map<OsmPrimitive,   Map<AddressElement, Integer>> primMatchIndex
      = new HashMap<OsmPrimitive,   Map<AddressElement, Integer>> ();
    private     Map<AddressElement, Map<OsmPrimitive,   Integer>> elemMatchIndex
      = new HashMap<AddressElement, Map<OsmPrimitive,   Integer>> ();

    private Set<OsmPrimitive>   primToUpdate = new HashSet<OsmPrimitive>();
    private Set<AddressElement> elemToUpdate = new HashSet<AddressElement>();

    public static Logger logger = Logger.getLogger(Reasoner.class.getName());

    private Reasoner() {}
    private static Reasoner singleton = null;
    public  static Reasoner getInstance() {
        if (singleton == null)
            singleton = new Reasoner();
        return singleton;
    }

//==============================================================================
// INPUT METHODS
//==============================================================================

    /**
     * Brings the reasoner to the initial state
     */
    public void reset() {
        primToUpdate.clear();
        elemToUpdate.clear();

        primMatchIndex.clear();
        elemMatchIndex.clear();
        primBestIndex.clear();
        primBestIndex.clear();

        transactionOpened = false;

        for (ReasonerListener listener : listeners)
            listener.resonerReseted();
    }

    /**
     * Indicates whether there is currently an open transaction
     */
    private boolean transactionOpened = false;

    /**
     * Prepares reasoner to modify its data.
     *
     * <p>This method must be called before <u>any</u> method, which might
     * modify the data in the reasoner.
     * The only exception is {@code reset()}.</p>
     * 
     * <p>When there's an open transaction, the result of most output methods
     * undefined. Exceptions to this rules are indicated.</p>
     *
     * <p><b>Transactions:</b> This method requires a closed transaction.</p>
     */
    public void openTransaction() {
        assert primToUpdate.size() == 0;
        assert elemToUpdate.size() == 0;
        assert !transactionOpened;

        primToUpdate.clear();
        elemToUpdate.clear();
        transactionOpened = true;
    }

    /**
     * Turns the reasoner back into consistent state.
     *
     * <p>Recreates {@code *BestIndex} indexes, sends notification to
     * all listeners about changed elements/primitives and closes
     * the transaction.</p>
     *
     * <p><b>Transactions:</b> This method requires an open transaction.</p>
     */
    public void closeTransaction() {
        assert transactionOpened;

        Set<AddressElement> elemChanges = new HashSet<AddressElement>();
        Set<OsmPrimitive>   primChanges = new HashSet<OsmPrimitive>();

        for (OsmPrimitive prim : primToUpdate) {
            AddressElement bestMatch = getStrictlyBest(prim);

            if (primBestIndex.get(prim) != bestMatch) {
                if (bestMatch == null) {
                    logger.log(Level.FINE, "primitive has no longer best match",
                                           AddressElement.getName(prim));
                    primBestIndex.remove(prim);
                } else {
                    logger.log(Level.FINE, "primitive has a new best match",
                            "prim=„" + AddressElement.getName(prim) + "“ → " +
                            "elem=„" + bestMatch + "“");
                    elemChanges.add(primBestIndex.get(prim));
                    primBestIndex.put(prim, bestMatch);
                }
            }
        }


        for (AddressElement elem : elemToUpdate) {
            OsmPrimitive bestMatch = getStrictlyBest(elem);

            if (elemBestIndex.get(elem) != bestMatch) {
                if (bestMatch == null) {
                    logger.log(Level.FINE, "element has no longer best match", elem);
                    elemBestIndex.remove(elem);
                } else {
                    logger.log(Level.FINE, "element has a new best match",
                            "elem=„" + elem + "“ → " +
                            "prim=„" + AddressElement.getName(bestMatch) + "“");
                    primChanges.add(elemBestIndex.get(elem));
                    elemBestIndex.put(elem, bestMatch);
                }
            }
        }

        elemToUpdate.addAll(elemChanges);
        primToUpdate.addAll(primChanges);
        transactionOpened = false;

        for (ReasonerListener listener : listeners) {
            for (AddressElement elem : elemToUpdate)
                if (elem != null)
                    listener.elementChanged(elem);

            for (OsmPrimitive prim : primToUpdate)
                if (prim != null)
                    listener.primitiveChanged(prim);
        }

        primToUpdate.clear();
        elemToUpdate.clear();
    }

    /**
     * Update all relations of the given primitive.
     *
     * <p>If the primitive is unknown to the reasoner, it's added.
     * Then it updates all cells in the Q matrix's column, which corresponds
     * to the provided primitive. In the Q-matrix analogy is roughly equivalent
     * to doing an update
     * <center>∀ elem. Q(elem, prim) ← elem.getQ(prim).</center>
     * Hence its time complexity is linear.</p>
     *
     * <p><b>Transactions:</b> This method requires an open transaction.</p>
     */
    public void update(OsmPrimitive prim) {
        logger.log(Level.FINER, "considering primitive", AddressElement.getName(prim));
        assert transactionOpened;


        Map<AddressElement, Integer> matches = primMatchIndex.get(prim);
        if (matches == null) {
            logger.log(Level.FINE, "new primitive detected", AddressElement.getName(prim));
            matches = new HashMap<AddressElement, Integer>();
            primMatchIndex.put(prim, matches);
        }

        for (AddressElement elem : elemMatchIndex.keySet())
            reconsider(prim, elem);
    }

    /**
     * Update all relations of the given element
     *
     * <p>If the primitive is unknown to the reasoner, it's added.
     * Then it updates all cells in the Q matrix's row, which corresponds
     * to the provided element.In the Q-matrix analogy is roughly equivalent
     * to doing an update
     * <center>∀ prim. Q(elem, prim) ← elem.getQ(prim).</center>
     * Hence its time complexity is linear.</p>
     *
     * <p><b>Transactions:</b> This method requires an open transaction.</p>
     */
    public void update(AddressElement elem) {
        logger.log(Level.FINER, "considering element", elem);
        assert transactionOpened;

        Map<OsmPrimitive, Integer> matches = elemMatchIndex.get(elem);
        if (matches == null) {
            logger.log(Level.FINE, "new element detected", elem);
            matches = new HashMap<OsmPrimitive, Integer>();
            elemMatchIndex.put(elem, matches);
        }

        for (OsmPrimitive prim : primMatchIndex.keySet())
            reconsider(prim, elem);
    }

    /**
     * Internal method for doing the actual Q value update.
     */
    private void reconsider(OsmPrimitive prim, AddressElement elem) {
        assert transactionOpened;

        int oldQ = getQ(prim, elem);
        int newQ = evalQ(prim, elem, oldQ);

        if (oldQ != newQ) {
            logger.log(Level.FINE, "reconsidering match",
                    "q=" + String.valueOf(oldQ) + "→" + String.valueOf(newQ) + "; " +
                    "elem=„" + elem + "“; " +
                    "prim=„" + AddressElement.getName(prim) + "“");
            putQ(prim, elem, newQ);

            primToUpdate.add(prim);
            elemToUpdate.add(elem);

            primToUpdate.addAll(elemMatchIndex.get(elem).keySet());
            elemToUpdate.addAll(primMatchIndex.get(prim).keySet());
        }
    }

    /**
     * Sets the relation's Q value to highest possible.
     *
     * <p>Regardless of how well the primitive and element pair fits
     * together, it assings their Q value to {@code MATCH_OVERWRITE}.</p>
     *
     * <p><b>Transactions:</b> This method requires an open transaction.</p>
     */
    public void doOverwrite(OsmPrimitive prim, AddressElement elem) {
        logger.log(Level.FINER, "overwriting match",
                    "elem=„" + elem + "“; " +
                    "prim=„" + AddressElement.getName(prim) + "“");
        assert transactionOpened;

        update(prim);
        update(elem);
        putQ(prim, elem, MATCH_OVERWRITE);

        primToUpdate.add(prim);
        elemToUpdate.add(elem);
    }

    /**
     * Sets the relation to its original Q value.
     *
     * <p>If the element-primitive pair was previously edited by
     * {@code doOverwrite()} method, this returns their Q to the
     * original value, which is determined by {@code evalQ()}.</p>
     *
     * <p><b>Transactions:</b> This method requires an open transaction.</p>
     */
    public void unOverwrite(OsmPrimitive prim, AddressElement elem) {
        logger.log(Level.FINER, "unoverwriting match",
                    "elem=„" + elem + "“; " +
                    "prim=„" + AddressElement.getName(prim) + "“");
        assert transactionOpened;

        update(prim);
        update(elem);
        putQ(prim, elem, evalQ(prim, elem, MATCH_NOMATCH));

        primToUpdate.add(prim);
        elemToUpdate.add(elem);
    }

    /**
     * Returns the Q value of the given primitive-element relation.
     */
    private int getQ(OsmPrimitive prim, AddressElement elem) {

        // TODO: This is a workaround. We should not be here at all.
        if (elemMatchIndex.get(elem) == null) return MATCH_NOMATCH;
        if (primMatchIndex.get(prim) == null) return MATCH_NOMATCH;

        assert primMatchIndex.get(prim).get(elem)
            == elemMatchIndex.get(elem).get(prim);

        if (primMatchIndex.get(prim).get(elem) == null)
            return 0;
        else
            return primMatchIndex.get(prim).get(elem);
    }

    /**
     * Sets the Q value of the given primitive-element relation.
     */
    private void putQ(OsmPrimitive prim, AddressElement elem, int qVal) {

        if (qVal == MATCH_NOMATCH) {
            primMatchIndex.get(prim).remove(elem);
            elemMatchIndex.get(elem).remove(prim);
        } else {
            primMatchIndex.get(prim).put(elem, qVal);
            elemMatchIndex.get(elem).put(prim, qVal);
        }
    }

    /**
     * Evaluates the Q value between the given primitive and element.
     *
     * <p>If {@code oldQ} is {@code MATCH_OVERWRITE}, it is preserved.</p>
     */
    private int evalQ(OsmPrimitive prim, AddressElement elem, Integer oldQ) {

        if (prim.deleted)
            return MATCH_NOMATCH;

        if (oldQ == MATCH_OVERWRITE)
            return MATCH_OVERWRITE;

        return elem.getQ(prim);
    }

//==============================================================================
// OUTPUT METHODS
//==============================================================================

    /**
     * Returns the primitive, which is a unique counterpart of the element.
     *
     * <p>This method is probably the single most used method of the reasoner.
     * It allows the unique translation between map and the database.</p>
     *
     * <p>An element <i>elem</i> and primitive <i>prim</i> can be translated
     * between each other iff
     * <center>[∄ <i>prim'</i>. Q(elem, prim') ≥ Q(elem, prim)] ∧
     *         [∄ <i>elem'</i>. Q(elem', prim) ≥ Q(elem, prim)].</center>
     * In other words, the cell at Q(elem, prim) is the strictly greatest one
     * in both its row and the column of the Q matrix.</p>
     *
     * <p>This method depends on {@code getStrictlyBest()}, which induces its
     * complexity properties.</p>
     *
     * <p><b>Transactions:</b> Can be called regardless of transaction state.
     * However if the transaction is closed, its time-complexity reduces to
     * constant thanks to using indexes.</p>
     */
    public AddressElement translate(OsmPrimitive prim) {
        if (prim == null) return null;

        AddressElement elem = getStrictlyBest(prim);
        if (getStrictlyBest(elem) == prim)
            return elem;
        
        return null;
    }

    /**
     * Returns the element, which is a unique counterpart of the primitive.
     *
     * <p>This method is probably the single most used method of the reasoner.
     * It allows the unique translation between map and the database.</p>
     *
     * <p>An element <i>elem</i> and primitive <i>prim</i> can be translated
     * between each other iff
     * <center>[∄ <i>prim'</i>. Q(elem, prim') ≥ Q(elem, prim)] ∧
     *         [∄ <i>elem'</i>. Q(elem', prim) ≥ Q(elem, prim)].</center>
     * In other words, the cell at Q(elem, prim) is the strictly greatest one
     * in both its row and the column of the Q matrix.</p>
     *
     * <p>This method depends on {@code getStrictlyBest()}, which induces its
     * complexity properties.</p>
     *
     * <p><b>Transactions:</b> Can be called regardless of transaction state.
     * However if the transaction is closed, its time-complexity reduces to
     * constant thanks to using indexes.</p>
     */
    public OsmPrimitive translate(AddressElement elem) {
        if (elem == null) return null;

        OsmPrimitive prim = getStrictlyBest(elem);
        if (getStrictlyBest(prim) == elem)
            return prim;
        
        return null;
    }

    /**
     * Says whether the given primitive has a conflict.
     *
     * <p>There are two conditions for a primitive to be in a conflict. It must
     * be at least partially fitting to some element, but it cannot be
     * uniquely translatable.
     * <center> [∃ elem. Q(elem, prim) > NO_MATCH] ∧
     *          ]∄ elem. elem = translate(prim)] ,</center>
     * which is equivalent to saying that
     * <center>|getCandidates(prim)| ≥ 2</center></p>
     */
    public boolean inConflict(OsmPrimitive prim) {
        if (primMatchIndex.get(prim) == null) return false;
        return primMatchIndex.get(prim).size() > 0
            && translate(translate(prim)) != prim;
    }

    /**
     * Says whether the given element has a conflict.
     *
     * <p>There are two conditions for a element to be in a conflict. It must
     * be at least partially fitting to some primitive, but it cannot be
     * uniquely translatable.
     * <center> [∃ prim. Q(elem, prim) > NO_MATCH] ∧
     *          [∄ prim. prim = translate(elem)] ,</center>
     * which is equivalent to saying that
     * <center>|getCandidates(prim)| ≥ 2</center></p>
     */
    public boolean inConflict(AddressElement elem) {
        if (elemMatchIndex.get(elem) == null) return false;
        return elemMatchIndex.get(elem).size() > 0
            && translate(translate(elem)) != elem;
    }


    /**
     * Returns elements having the best quality for the given primitive.
     *
     * <p>It searches among all Q values corresponding to the given primitive
     * and returns a set of all elements, whose relation has the greatest
     * Q value. Formally we can write that the output is a set
     * <center>{elem |   Q(elem, prim) > MATCH_NOMATCH
     *                 ∧ ∀ elem'. Q(elem, prim) ≥ Q(elem', prim)}.</center></p>
     *
     * <p><b>Transactions:</b> Can be called regardless of transaction state.</p>
     *
     * @return A new set, which can be freely manipulated. Changes are not
     * reflected in the reasoner.
     */
    public Set<AddressElement> getCandidates(OsmPrimitive prim) {

        Set<AddressElement> result = new HashSet<AddressElement>();
        if (primMatchIndex.get(prim) == null) return result;

        int best = MATCH_NOMATCH;
        for (AddressElement elem : primMatchIndex.get(prim).keySet()) {
            int cand = primMatchIndex.get(prim).get(elem);
            if (best < cand)
                best = cand;
        }

        for (AddressElement elem : primMatchIndex.get(prim).keySet()) {
            int cand = primMatchIndex.get(prim).get(elem);
            if (best == cand)
                result.add(elem);
        }
        return result;
    }

    /**
     * Returns primitives having the best quality for the given element.
     *
     * <p>It searches among all Q values corresponding to the given element
     * and returns a set of all primitives, whose relation has the greatest
     * Q value. Formally we can write that the output is a set
     * <center>{prim |   Q(elem, prim) > MATCH_NOMATCH
     *                 ∧ ∀ prim'. Q(elem, prim) ≥ Q(elem, prim')}.</center></p>
     *
     * <p><b>Transactions:</b> Can be called regardless of transaction state.</p>
     *
     * @return A new set, which can be freely manipulated. Changes are not
     * reflected in the reasoner.
     */
    public Set<OsmPrimitive> getCandidates(AddressElement elem) {

        Set<OsmPrimitive> result = new HashSet<OsmPrimitive>();
        if (elemMatchIndex.get(elem) == null) return result;

        int best = MATCH_NOMATCH;
        for (OsmPrimitive prim : elemMatchIndex.get(elem).keySet()) {
            int cand = elemMatchIndex.get(elem).get(prim);
            if (best < cand)
                best = cand;
        }
        
        for (OsmPrimitive prim : elemMatchIndex.get(elem).keySet()) {
            int cand = elemMatchIndex.get(elem).get(prim);
            if (best == cand)
                result.add(prim);
        }
        return result;
    }

    /**
     * Returns the element having the best quality for the given primitive.
     *
     * <p>It searches among all Q values corresponding to the given primitive
     * and returns such an element, whose relation has the greatest
     * Q value. If there are more of them, {@code null} is returned.
     * Formally we can write that the output is a primitive
     * <center>elem: Q(elem, prim) > MATCH_NOMATCH
     *             ∧ ∀ elem'. Q(elem, prim) > Q(elem', prim)}.</center></p>
     *
     * <p>If {@code getCandidates(prim)} returns exactly one element,
     * this method should return the same one. Otherwise {@code null}.</p>
     *
     * <p><b>Transactions:</b> Can be called regardless of transaction state.
     * However if the transaction is closed, its time-complexity reduces to
     * constant thanks to using indexes.</p>
     */
    public AddressElement getStrictlyBest(OsmPrimitive prim) {
        
        AddressElement result = null;
        try {
            if (!transactionOpened)
                return primBestIndex.get(prim);

            Map<AddressElement, Integer> matches = primMatchIndex.get(prim);
            if (matches == null) {
                return null;
            }

            int bestQ = MATCH_NOMATCH;
            for (AddressElement elem : matches.keySet()) {
                if (matches.get(elem) == bestQ)
                    result = null;

                if (matches.get(elem) > bestQ) {
                    bestQ = matches.get(elem);
                    result = elem;
                }
            }
            
        } catch (NullPointerException except) {
            System.err.println("Strange exception occured." +
                " If you find a way to reproduce this situation, please "+
                "e-mail the author of the CzechAddress plugin.");
            except.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the primitive having the best quality for the given element.
     *
     * <p>It searches among all Q values corresponding to the given element
     * and returns sucha  primitive, whose relation has the greatest
     * Q value. If there are more of them, {@code null} is returned.
     * Formally we can write that the output is a primitive
     * <center>prim: Q(elem, prim) > MATCH_NOMATCH
     *             ∧ ∀ prim'. Q(elem, prim) > Q(elem', prim)}.</center></p>
     *
     * <p>If {@code getCandidates(elem)} returns exactly one primitive,
     * this method should return the same one. Otherwise {@code null}.</p>
     *
     * <p><b>Transactions:</b> Can be called regardless of transaction state.
     * However if the transaction is closed, its time-complexity reduces to
     * constant thanks to using indexes.</p>
     */
    public OsmPrimitive getStrictlyBest(AddressElement elem) {

        OsmPrimitive result = null;
        try {
            if (!transactionOpened)
                return elemBestIndex.get(elem);

            Map<OsmPrimitive, Integer> matches = elemMatchIndex.get(elem);
            if (matches == null) {
                return null;
            }
            
            int bestQ = MATCH_NOMATCH;
            for (OsmPrimitive prim : matches.keySet()) {
                if (matches.get(prim) == bestQ) {
                    result = null;
                }

                if (matches.get(prim) > bestQ) {
                    bestQ = matches.get(prim);
                    result = prim;
                }
            }

        } catch (NullPointerException except) {
            System.err.println("Strange exception occured." +
                " If you find a way to reproduce this situation, please "+
                "e-mail the author of the CzechAddress plugin.");
            except.printStackTrace();
        }
        return result;
    }

    /**
     * Returns all elements which are not translatable.
     */
    public Set<AddressElement> getUnassignedElements() {
        Set<AddressElement> result = new HashSet<AddressElement>();
        for (AddressElement elem : elemMatchIndex.keySet())
            if (translate(elem) == null)
                result.add(elem);
        return result;
    }

    /**
     * Returns all primitives which are not translatable.
     */
    public Set<OsmPrimitive> getUnassignedPrimitives() {
        Set<OsmPrimitive> result = new HashSet<OsmPrimitive>();
        for (OsmPrimitive prim : primMatchIndex.keySet())
            if (translate(prim) == null)
                result.add(prim);
        return result;
    }

    /**
     * Returns all elements, which were {@code update}d from
     * the last {@code reset}.
     */
    public Set<AddressElement> getAllElements() {
        Set<AddressElement> result = new HashSet<AddressElement>();
        result.addAll(elemMatchIndex.keySet());
        return result;
    }

    /**
     * Returns all elements, which were {@code update}d from
     * the last {@code reset}.
     */
    public Set<OsmPrimitive> getAllPrimitives() {
        Set<OsmPrimitive> result = new HashSet<OsmPrimitive>();
        result.addAll(primMatchIndex.keySet());
        return result;
    }

//==============================================================================
// MISC METHODS
//==============================================================================

    /**
     * Returns proposals to fit all translatable primitives to their elements.
     */
    public ProposalDatabase getProposals() {
        ProposalDatabase database = new ProposalDatabase();

        // We can go only over primBestIndex to save some iterations.
        // A primitive cannot be translated unless contained in primBestIndex.
        for (OsmPrimitive prim : primBestIndex.keySet()) {
            AddressElement elem = translate(prim);
            if (elem == null) continue;

            ProposalContainer container = new ProposalContainer(prim);
            container.addProposals(elem.getDiff(prim));
            
            if (container.getProposals().size() > 0)
                database.addContainer(container);
        }

        Collections.sort(database.getContainers());
        return database;
    }

    /**
     * Set of listeners currently hooked to changes in this reasoner.
     */
    private Set<ReasonerListener> listeners = new HashSet<ReasonerListener>();

    /**
     * Adds a new listener to receive reasoner's status changes.
     */
    public void addListener(ReasonerListener listener) {
        listeners.add(listener);
    }

    /**
     * Stops the listener to receive reasoner's status changes.
     */
    public void removeListener(ReasonerListener listener) {
        listeners.remove(listener);
    }

}
