package buildings_tools;

import static buildings_tools.BuildingsToolsPlugin.latlon2eastNorth;

import java.util.TreeSet;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Pair;

public class AngleSnap {
    private static final double PI_2 = Math.PI / 2;
    TreeSet<Double> snapSet = new TreeSet<Double>();

    public void clear() {
        snapSet.clear();
    }

    public void addSnap(double snap) {
        snapSet.add(snap % PI_2);
    }

    public Double addSnap(Node[] nodes) {
        if (nodes.length == 2) {
            EastNorth p1, p2;
            p1 = latlon2eastNorth(((Node) nodes[0]).getCoor());
            p2 = latlon2eastNorth(((Node) nodes[1]).getCoor());
            double heading = p1.heading(p2);
            addSnap(heading);
            addSnap(heading + Math.PI / 4);
            return heading;
        } else {
            return null;
        }
    }

    public void addSnap(Way way) {
        for (Pair<Node, Node> pair : way.getNodePairs(false)) {
            EastNorth a, b;
            a = latlon2eastNorth(pair.a.getCoor());
            b = latlon2eastNorth(pair.b.getCoor());
            double heading = a.heading(b);
            addSnap(heading);
        }
    }

    public Double getAngle() {
        if (snapSet.isEmpty()) {
            return null;
        }
        double first = snapSet.first();
        double last = snapSet.last();
        if (first < Math.PI / 4 && last > Math.PI / 4) {
            last -= PI_2;
        }
        if (Math.abs(first - last) < 0.001) {
            double center = (first + last) / 2;
            if (center < 0)
                center += PI_2;
            return center;
        } else {
            return null;
        }
    }

    public double snapAngle(double angle) {
        if (snapSet.isEmpty()) {
            return angle;
        }
        int quadrant = (int) Math.floor(angle / PI_2);
        double ang = angle % PI_2;
        Double prev = snapSet.floor(ang);
        if (prev == null)
            prev = snapSet.last() - PI_2;
        Double next = snapSet.ceiling(ang);
        if (next == null)
            next = snapSet.first() + PI_2;

        if (Math.abs(ang - next) > Math.abs(ang - prev)) {
            if (Math.abs(ang - prev) > Math.PI / 8) {
                return angle;
            } else {
                double ret = prev + PI_2 * quadrant;
                if (ret < 0)
                    ret += 2 * Math.PI;
                return ret;
            }
        } else {
            if (Math.abs(ang - next) > Math.PI / 8) {
                return angle;
            } else {
                double ret = next + PI_2 * quadrant;
                if (ret > 2 * Math.PI)
                    ret -= 2 * Math.PI;
                return ret;
            }
        }
    }
}
