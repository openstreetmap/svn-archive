package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;

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

    /* A list of {@link OsmPrimitive}s, for which there was no suitable match.*/

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

    public void reset() {
        primToUpdate.clear();
        elemToUpdate.clear();

        primMatchIndex.clear();
        elemMatchIndex.clear();
        primBestIndex.clear();
        primBestIndex.clear();

        for (ReasonerListener listener : listeners)
            listener.resonerReseted();
    }
    
    public Set<AddressElement> getCandidates(OsmPrimitive prim) {

        int best = Match.MATCH_NOMATCH;
        for (AddressElement elem : primMatchIndex.get(prim).keySet()) {
            int cand = primMatchIndex.get(prim).get(elem);
            if (best < cand)
                best = cand;
        }

        Set<AddressElement> result = new HashSet<AddressElement>();

        for (AddressElement elem : primMatchIndex.get(prim).keySet()) {
            int cand = primMatchIndex.get(prim).get(elem);
            if (best == cand)
                result.add(elem);
        }
        return result;
    }

    public Set<OsmPrimitive> getCandidates(AddressElement elem) {

        int best = Match.MATCH_NOMATCH;
        for (OsmPrimitive prim : elemMatchIndex.get(elem).keySet()) {
            int cand = elemMatchIndex.get(elem).get(prim);
            if (best < cand)
                best = cand;
        }

        Set<OsmPrimitive> result = new HashSet<OsmPrimitive>();
        
        for (OsmPrimitive prim : elemMatchIndex.get(elem).keySet()) {
            int cand = elemMatchIndex.get(elem).get(prim);
            if (best == cand)
                result.add(prim);
        }
        return result;
    }

    public AddressElement getStrictlyBest(OsmPrimitive prim) {

        Map<AddressElement, Integer> matches = primMatchIndex.get(prim);
        //if (matches == null) return null;

        AddressElement bestE = null;
        int bestQ = Match.MATCH_NOMATCH;

        for (AddressElement elem : matches.keySet()) {
            if (matches.get(elem) == bestQ)
                bestE = null;

            if (matches.get(elem) > bestQ) {
                bestQ = matches.get(elem);
                bestE = elem;
            }
        }

        return bestE;
    }

    public OsmPrimitive getStrictlyBest(AddressElement prim) {

        Map<OsmPrimitive, Integer> matches = elemMatchIndex.get(prim);
        //if (matches == null) return null;

        OsmPrimitive bestE = null;
        int bestQ = Match.MATCH_NOMATCH;

        for (OsmPrimitive elem : matches.keySet()) {
            if (matches.get(elem) == bestQ)
                bestE = null;

            if (matches.get(elem) > bestQ) {
                bestQ = matches.get(elem);
                bestE = elem;
            }
        }

        return bestE;
    }

    public void openTransaction() {
        assert primToUpdate.size() == 0;
        assert elemToUpdate.size() == 0;
    }

    public void closeTransaction() {

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

    private Set<ReasonerListener> listeners = new HashSet<ReasonerListener>();

    public void reconsider(OsmPrimitive prim, AddressElement elem) {

        int oldQ = getQ(prim, elem);
        int newQ = Match.evalQ(prim, elem, oldQ);

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

    public void consider(OsmPrimitive prim) {
        logger.log(Level.FINER, "considering primitive", AddressElement.getName(prim));

        Map<AddressElement, Integer> matches = primMatchIndex.get(prim);
        if (matches == null) {
            logger.log(Level.FINE, "new primitive detected", AddressElement.getName(prim));
            matches = new HashMap<AddressElement, Integer>();
            primMatchIndex.put(prim, matches);
        }

        for (AddressElement elem : elemMatchIndex.keySet())
            reconsider(prim, elem);
    }

    public void consider(AddressElement elem) {
        logger.log(Level.FINER, "considering element", elem);

        Map<OsmPrimitive, Integer> matches = elemMatchIndex.get(elem);
        if (matches == null) {
            logger.log(Level.FINE, "new element detected", elem);
            matches = new HashMap<OsmPrimitive, Integer>();
            elemMatchIndex.put(elem, matches);
        }

        for (OsmPrimitive prim : primMatchIndex.keySet())
            reconsider(prim, elem);
    }

    public int getQ(OsmPrimitive prim, AddressElement elem) {
        assert primMatchIndex.get(prim).get(elem)
            == elemMatchIndex.get(elem).get(prim);

        if (primMatchIndex.get(prim).get(elem) == null)
            return 0;
        else
            return primMatchIndex.get(prim).get(elem);
    }

    public void putQ(OsmPrimitive prim, AddressElement elem, int qVal) {
        
        if (qVal == Match.MATCH_NOMATCH) {
            primMatchIndex.get(prim).remove(elem);
            elemMatchIndex.get(elem).remove(prim);
        } else {
            primMatchIndex.get(prim).put(elem, qVal);
            elemMatchIndex.get(elem).put(prim, qVal);
        }
    }

    public AddressElement translate(OsmPrimitive prim) {
        if (prim == null) return null;

        AddressElement elem = primBestIndex.get(prim);
        if (elemBestIndex.get(elem) == prim)
            return elem;
        return null;
    }

    public OsmPrimitive translate(AddressElement elem) {
        if (elem == null) return null;

        OsmPrimitive prim = elemBestIndex.get(elem);
        if (primBestIndex.get(prim) == elem)
            return prim;
        return null;
    }

    public Set<AddressElement> getConflicts(OsmPrimitive prim) {

        Set<AddressElement> result = getCandidates(prim);
        AddressElement match = translate(prim);
        if (match != null)
            result.remove(match);

        return result;
    }

    public Set<OsmPrimitive> getConflicts(AddressElement elem) {

        Set<OsmPrimitive> result = getCandidates(elem);
        OsmPrimitive match = translate(elem);
        if (match != null)
            result.remove(match);

        return result;
    }

    public void addListener(ReasonerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ReasonerListener listener) {
        listeners.remove(listener);
    }

    public static void main(String[] args) {
        try {

            Reasoner r = Reasoner.getInstance();
            Reasoner.logger.setLevel(Level.ALL);
            
            Street s1 = new Street("Jarní");
            Street s2 = new Street("Letní");

            House h1 = new House("240", "1"); s1.addHouse(h1); r.consider(h1);
            House h2 = new House("241", "2"); s1.addHouse(h2); r.consider(h2);
            House h3 = new House("241", "3"); s1.addHouse(h3); r.consider(h3);
            House h4 = new House("242", "4"); s1.addHouse(h4); r.consider(h4);

            House i1 = new House("42",  "1"); s2.addHouse(i1); r.consider(i1);
            House i2 = new House("45",  "2"); s2.addHouse(i2); r.consider(i2);
            House i3 = new House("61",  "3"); s2.addHouse(i3); r.consider(i3);
            House i4 = new House("240", "4"); s2.addHouse(i4); r.consider(i4);


            
            Node n1 = new Node(1);
            n1.put("addr:street", "Jarní");
            n1.put("addr:alternatenumber", "242");

            r.openTransaction();
            r.consider(n1);
            r.closeTransaction();
            assert r.translate(n1) == h4;
            assert r.getConflicts(n1).size() == 0;



            Node n2 = new Node(2);
            n2.put("addr:alternatenumber", "240");
            r.openTransaction();
            r.consider(n2);
            r.closeTransaction();
            assert r.translate(n2) == null;
            assert r.getConflicts(n2).contains(h1);
            assert r.getConflicts(n2).contains(i4);

            n2.put("addr:street", "Letní");
            n2.put("addr:housenumber", "4");
            r.openTransaction();
            r.consider(n2);
            r.closeTransaction();
            assert r.translate(n2) == i4;


            n2.deleted = true;
            r.openTransaction();
            r.consider(n2);
            r.closeTransaction();
            assert r.translate(n2) == null;
            
        } catch (Exception ex) {
            
            ex.printStackTrace();
        }
    }

    public void doOverwrite(OsmPrimitive prim, AddressElement elem) {
        logger.log(Level.FINER, "overwriting match",
                    "elem=„" + elem + "“; " +
                    "prim=„" + AddressElement.getName(prim) + "“");

        consider(prim);
        consider(elem);
        putQ(prim, elem, Match.MATCH_OVERWRITE);

        primToUpdate.add(prim);
        elemToUpdate.add(elem);
    }

    public void unOverwrite(OsmPrimitive prim, AddressElement elem) {
        logger.log(Level.FINER, "unoverwriting match",
                    "elem=„" + elem + "“; " +
                    "prim=„" + AddressElement.getName(prim) + "“");

        consider(prim);
        consider(elem);
        putQ(prim, elem, Match.evalQ(prim, elem, Match.MATCH_NOMATCH));

        primToUpdate.add(prim);
        elemToUpdate.add(elem);
    }

    public boolean inConflict(OsmPrimitive prim) {
        return primMatchIndex.get(prim).size() > 0
            && translate(translate(prim)) != prim;
    }

    public boolean inConflict(AddressElement elem) {
        return elemMatchIndex.get(elem).size() > 0
            && translate(translate(elem)) != elem;
    }

    public Set<AddressElement> getUnassignedElements() {
        Set<AddressElement> result = new HashSet<AddressElement>();
        for (AddressElement elem : elemMatchIndex.keySet())
            if (elemMatchIndex.get(elem).size() == 0)
                result.add(elem);
        return result;
    }
}
