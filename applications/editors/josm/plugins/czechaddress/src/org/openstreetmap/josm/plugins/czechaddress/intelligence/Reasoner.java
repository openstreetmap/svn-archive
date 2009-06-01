package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    /* A list of {@link OsmPrimitive}s, for which there was no suitable match. */
    //private List<OsmPrimitive> notMatchable = new ArrayList<OsmPrimitive>();

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
//  private Map<Object,Integer> statusBefore = new HashMap<Object,Integer>();

    public Logger logger = Logger.getLogger("Reasoner");

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_CONFLICT = 1;
    public static final int STATUS_MATCH = 2;

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
    }

    private AddressElement getBest(OsmPrimitive prim) {

        Map<AddressElement, Integer> matches = primMatchIndex.get(prim);
        if (matches == null) return null;

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

    private OsmPrimitive getBest(AddressElement prim) {

        Map<OsmPrimitive, Integer> matches = elemMatchIndex.get(prim);
        if (matches == null) return null;

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

        Set<Object> changes = new HashSet<Object>();

        for (OsmPrimitive prim : primToUpdate) {
            AddressElement bestMatch = getBest(prim);

            if (primBestIndex.get(prim) != bestMatch) {
                if (bestMatch == null) {
                    logger.log(Level.INFO, "primitive has no longer best match",
                            AddressElement.getName(prim));
                    primBestIndex.remove(prim);
                } else {
                    logger.log(Level.INFO, "primitive has a new best match",
                            "prim=„" + AddressElement.getName(prim) + "“ → " +
                            "elem=„" + bestMatch + "“");
                    primBestIndex.put(prim, bestMatch);
                }
                changes.add(prim);
            }
        }
        primToUpdate.clear();


        for (AddressElement elem : elemToUpdate) {
            OsmPrimitive bestMatch = getBest(elem);

            if (elemBestIndex.get(elem) != bestMatch) {
                if (bestMatch == null) {
                    logger.log(Level.INFO, "element has no longer best match", elem);
                    elemBestIndex.remove(elem);
                } else {
                    logger.log(Level.INFO, "element has a new best match",
                            "elem=„" + elem + "“ → " +
                            "prim=„" + AddressElement.getName(bestMatch) + "“");
                    elemBestIndex.put(elem, bestMatch);
                }
                changes.add(elem);
            }
        }
        elemToUpdate.clear();

        for (Object change : changes) {
            if (change instanceof OsmPrimitive)
                for (ReasonerListener listener : listeners)
                    listener.primitiveChanged((OsmPrimitive) change);

            if (change instanceof AddressElement)
                for (ReasonerListener listener : listeners)
                    listener.elementChanged((AddressElement) change);
        }
    }

    private Set<ReasonerListener> listeners = new HashSet<ReasonerListener>();

    public void reconsider(OsmPrimitive prim, AddressElement elem) {

        int oldQ = getQ(prim, elem);
        int newQ = Match.evalQ(prim, elem, oldQ);

        if (oldQ != newQ) {
            logger.log(Level.INFO, "reconsidering match",
                    "q=" + String.valueOf(oldQ) + "→" + String.valueOf(newQ) + "; " +
                    "elem=„" + elem + "“; " +
                    "prim=„" + AddressElement.getName(prim) + "“");
            putQ(prim, elem, newQ);

            primToUpdate.add(prim);
            elemToUpdate.add(elem);
        }
    }

    public void consider(OsmPrimitive prim) {
        logger.log(Level.FINE, "considering primitive", AddressElement.getName(prim));

        Map<AddressElement, Integer> matches = primMatchIndex.get(prim);
        if (matches == null) {
            logger.log(Level.INFO, "new primitive detected", AddressElement.getName(prim));
            matches = new HashMap<AddressElement, Integer>();
            primMatchIndex.put(prim, matches);
        }

        for (AddressElement elem : elemMatchIndex.keySet())
            reconsider(prim, elem);
    }

    public void consider(AddressElement elem) {
        logger.log(Level.FINE, "considering element", elem);

        Map<OsmPrimitive, Integer> matches = elemMatchIndex.get(elem);
        if (matches == null) {
            logger.log(Level.INFO, "new element detected", elem);
            matches = new HashMap<OsmPrimitive, Integer>();
            elemMatchIndex.put(elem, matches);
        }

        for (OsmPrimitive prim : primMatchIndex.keySet())
            reconsider(prim, elem);
    }

    /*private int getStatus(OsmPrimitive prim) {
        if (primMatchIndex.get(prim) == null)   return STATUS_UNKNOWN;
        if (primMatchIndex.get(prim).size()==0) return STATUS_UNKNOWN;
        if (translate(prim) != null)            return STATUS_MATCH;
        return STATUS_CONFLICT;
    }

    private int getStatus(AddressElement elem) {
        if (elemMatchIndex.get(elem) == null)   return STATUS_UNKNOWN;
        if (elemMatchIndex.get(elem).size()==0) return STATUS_UNKNOWN;
        if (translate(elem) != null)            return STATUS_MATCH;
        return STATUS_CONFLICT;
    }*/

    public int getQ(OsmPrimitive prim, AddressElement elem) {
        if (primMatchIndex.get(prim) == null) return 0;
        if (elemMatchIndex.get(elem) == null) return 0;

        assert primMatchIndex.get(prim).get(elem) == elemMatchIndex.get(elem).get(prim);

        if (primMatchIndex.get(prim).get(elem) == null)
            return 0;
        else
            return primMatchIndex.get(prim).get(elem);
    }

    public void putQ(OsmPrimitive prim, AddressElement elem, int qVal) {
        
        if (qVal == Match.MATCH_NOMATCH) {
            primMatchIndex.get(prim).remove(elem);
            elemMatchIndex.get(elem).remove(prim);

            if (primMatchIndex.get(prim).size() == 0)
                primMatchIndex.put(prim, null);
            if (elemMatchIndex.get(elem).size() == 0)
                elemMatchIndex.put(elem, null);

        } else {
            primMatchIndex.get(prim).put(elem, qVal);
            elemMatchIndex.get(elem).put(prim, qVal);
        }
    }

    public AddressElement translate(OsmPrimitive prim) {
        AddressElement elem = primBestIndex.get(prim);
        if (elemBestIndex.get(elem) == prim)
            return elem;
        return null;
    }

    public OsmPrimitive translate(AddressElement elem) {
        OsmPrimitive prim = elemBestIndex.get(elem);
        if (primBestIndex.get(prim) == elem)
            return prim;
        return null;
    }

    public Set<AddressElement> conflicts(OsmPrimitive prim) {

        Set<AddressElement> result = new HashSet<AddressElement>();
        result.addAll(primMatchIndex.get(prim).keySet());
        
        AddressElement match = translate(prim);
        if (match != null)
            result.remove(match);

        return result;
    }

    public Set<OsmPrimitive> conflicts(AddressElement elem) {

        Set<OsmPrimitive> result = new HashSet<OsmPrimitive>();
        result.addAll(elemMatchIndex.get(elem).keySet());

        OsmPrimitive match = translate(elem);
        if (match != null)
            result.remove(match);

        return result;
    }

    public static void main(String[] args) {
        try {

            Reasoner r = new Reasoner();

            Handler  h = new FileHandler("log.xml");

            r.logger.addHandler(h);
            r.logger.setLevel(Level.ALL);
            
            Match.logger.addHandler(h);
            Match.logger.setLevel(Level.ALL);


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
            assert r.conflicts(n1).size() == 0;



            Node n2 = new Node(2);
            n2.put("addr:alternatenumber", "240");
            r.openTransaction();
            r.consider(n2);
            r.closeTransaction();
            assert r.translate(n2) == null;
            assert r.conflicts(n2).contains(h1);
            assert r.conflicts(n2).contains(i4);

            n2.put("addr:street", "Letní");
            n2.put("addr:housenumber", "4");
            r.openTransaction();
            r.consider(n2);
            r.closeTransaction();
            assert r.translate(n2) == i4;
            assert r.conflicts(n2).contains(h1);


            n2.deleted = true;
            r.openTransaction();
            r.consider(n2);
            r.closeTransaction();

            assert r.translate(n2) == null;
            
        } catch (Exception ex) {
            
            ex.printStackTrace();
        }
    }
}
