// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Imported from plugin UtilsPlugin
 * @author 
 *
 */
public class SimplifyWay {
    public void simplifyWay(Way w/*, DataSet dataSet*/, double threshold) {
        Way wnew = new Way(w);

        simplifyWayRange(wnew, 0, wnew.getNodesCount() - 1, threshold);
        w.setNodes(wnew.getNodes());
    }

    public void simplifyWayRange(Way wnew, int from, int to, double thr) {
        if (to - from >= 2) {
            ArrayList<Node> ns = new ArrayList<>();
            simplifyWayRange(wnew, from, to, ns, thr);
            List<Node> nodes = wnew.getNodes();
            for (int j = to - 1; j > from; j--)
                nodes.remove(j);
            nodes.addAll(from+1, ns);
            wnew.setNodes(nodes);
        }
    }
    
    /*
     * Takes an interval [from,to] and adds nodes from (from,to) to ns.
     * (from and to are indices of wnew.nodes.)
     */
    public void simplifyWayRange(Way wnew, int from, int to, ArrayList<Node> ns, double thr) {
        Node fromN = wnew.getNode(from), toN = wnew.getNode(to);

        int imax = -1;
        double xtemax = 0;
        for (int i = from + 1; i < to; i++) {
            Node n = wnew.getNode(i);
            double xte = Math.abs(EARTH_RAD
                    * xtd(fromN.getCoor().lat() * Math.PI / 180, fromN.getCoor().lon() * Math.PI / 180, toN.getCoor().lat() * Math.PI
                            / 180, toN.getCoor().lon() * Math.PI / 180, n.getCoor().lat() * Math.PI / 180, n.getCoor().lon() * Math.PI
                            / 180));
            if (xte > xtemax) {
                xtemax = xte;
                imax = i;
            }
        }

        if (imax != -1 && xtemax >= thr) {
            simplifyWayRange(wnew, from, imax, ns, thr);
            ns.add(wnew.getNode(imax));
            simplifyWayRange(wnew, imax, to, ns, thr);
        }
    }
    public static double EARTH_RAD = 6378137.0;
    /* From Aviaton Formulary v1.3
     * http://williams.best.vwh.net/avform.htm
     */
    public static double dist(double lat1, double lon1, double lat2, double lon2) {
        return 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((lat1 - lat2) / 2), 2) + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin((lon1 - lon2) / 2), 2)));
    }

    public static double course(double lat1, double lon1, double lat2, double lon2) {
        return Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2), Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(lon1 - lon2))
                % (2 * Math.PI);
    }
    public static double xtd(double lat1, double lon1, double lat2, double lon2, double lat3, double lon3) {
        double dist_AD = dist(lat1, lon1, lat3, lon3);
        double crs_AD = course(lat1, lon1, lat3, lon3);
        double crs_AB = course(lat1, lon1, lat2, lon2);
        return Math.asin(Math.sin(dist_AD) * Math.sin(crs_AD - crs_AB));
    }

}
