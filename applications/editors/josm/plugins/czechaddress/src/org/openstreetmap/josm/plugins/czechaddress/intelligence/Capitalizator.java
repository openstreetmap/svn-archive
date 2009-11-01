package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.StringUtils;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;

/**
 * Updates the names in the database according the map.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class Capitalizator {

    Map<Street, OsmPrimitive> map;
    Logger logger =  Logger.getLogger(Capitalizator.class.getName());

    public Capitalizator(List<OsmPrimitive> prims, List<Street> elems) {

        int expResults = elems.size()/2;

        map  = new HashMap<Street, OsmPrimitive>(expResults);
        ExecutorService serv = Executors.newCachedThreadPool();
        Map<Street, Future<OsmPrimitive>> results
                = new HashMap<Street, Future<OsmPrimitive>>(expResults);

        for (Street elem : elems)
             results.put(elem, serv.submit(new StreetMatcher(elem, prims)));

        for (Street elem : results.keySet()) {
            try {

                OsmPrimitive match = results.get(elem).get();
                if (match == null) continue;

                map.put(elem, match);

            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, "Thread interrupted during matching", ex);
            } catch (ExecutionException ex) {
                logger.log(Level.SEVERE, "Unknown error during matching", ex);
            }
        }
    }

    private class StreetMatcher implements Callable<OsmPrimitive> {

        private AddressElement elem;
        private List<OsmPrimitive> prims;

        public StreetMatcher(AddressElement elem, List<OsmPrimitive> prims) {
            this.elem = elem;
            this.prims = prims;
        }

        public OsmPrimitive call() throws Exception {

            OsmPrimitive candidate = null;
            for (OsmPrimitive prim : prims) {

                if (prim.get("name") == null)
                    continue;

                if (prim.get("name").equals(elem.getName()))
                    return prim;

                if (StringUtils.matchAbbrev(prim.get("name"), elem.getName()))
                    candidate = prim;
            }

            return candidate;
        }
    }

    public OsmPrimitive translate(Street elem) {
        return map.get(elem);
    }

    public Set<Street> getCapitalised() {
        return map.keySet();
    }
}
