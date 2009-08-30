package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmUtils;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.validator.PreferenceEditor;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Tests if there are segments that crosses in the same layer
 *
 * @author frsantos
 */
public class UnconnectedWays extends Test
{
    protected static int UNCONNECTED_WAYS = 1301;
    protected static final String PREFIX = PreferenceEditor.PREFIX + "." + UnconnectedWays.class.getSimpleName();

    Set<MyWaySegment> ways;
    Set<Node> endnodes; // nodes at end of way
    Set<Node> endnodes_highway; // nodes at end of way
    Set<Node> middlenodes; // nodes in middle of way
    Set<Node> othernodes; // nodes appearing at least twice

    double mindist;
    double minmiddledist;
    /**
     * Constructor
     */
    public UnconnectedWays()
    {
        super(tr("Unconnected ways."),
              tr("This test checks if a way has an endpoint very near to another way."));
    }

    @Override
    public void startTest()
    {
        ways = new HashSet<MyWaySegment>();
        endnodes = new HashSet<Node>();
        endnodes_highway = new HashSet<Node>();
        middlenodes = new HashSet<Node>();
        othernodes = new HashSet<Node>();
        mindist = Main.pref.getDouble(PREFIX + ".node_way_distance", 10.0)/6378135.0;
        minmiddledist = Main.pref.getDouble(PREFIX + ".way_way_distance", 0.0)/6378135.0;
    }

    @Override
    public void endTest()
    {
        Area a = Main.main.getCurrentDataSet().getDataSourceArea();
        Map<Node, Way> map = new HashMap<Node, Way>();
        for(Node en : endnodes_highway)
        {
            Boolean isexit = OsmUtils.getOsmBoolean(en.get("noexit"));
            if("turning_circle".equals(en.get("highway")) ||
            (isexit != null && isexit) || en.get("barrier") != null)
                continue;
            for(MyWaySegment s : ways)
            {
                if(!s.isBoundary && !s.isAbandoned && s.highway && s.nearby(en, mindist) && (a == null || a.contains(en.getCoor())))
                    map.put(en, s.w);
            }
        }
        if(map.size() > 0)
        {
            for(Map.Entry<Node, Way> error : map.entrySet())
            {
                errors.add(new TestError(this, Severity.WARNING,
                tr("Way end node near other highway"), UNCONNECTED_WAYS,
                Arrays.asList(error.getKey(), error.getValue())));
            }
        }
        map.clear();
        for(Node en : endnodes_highway)
        {
            for(MyWaySegment s : ways)
            {
                if(!s.isBoundary && !s.isAbandoned && !s.highway && s.nearby(en, mindist) && !s.isArea() && (a == null || a.contains(en.getCoor())))
                    map.put(en, s.w);
            }
        }
        for(Node en : endnodes)
        {
            for(MyWaySegment s : ways)
            {
                if(!s.isBoundary && !s.isAbandoned && s.nearby(en, mindist) && !s.isArea() && (a == null || a.contains(en.getCoor())))
                    map.put(en, s.w);
            }
        }
        if(map.size() > 0)
        {
            for(Map.Entry<Node, Way> error : map.entrySet())
            {
                errors.add(new TestError(this, Severity.WARNING,
                tr("Way end node near other way"), UNCONNECTED_WAYS,
                Arrays.asList(error.getKey(), error.getValue())));
            }
        }
        /* the following two use a shorter distance */
        if(minmiddledist > 0.0)
        {
            map.clear();
            for(Node en : middlenodes)
            {
                for(MyWaySegment s : ways)
                {
                    if(!s.isBoundary && !s.isAbandoned && s.nearby(en, minmiddledist) && (a == null || a.contains(en.getCoor())))
                        map.put(en, s.w);
                }
            }
            if(map.size() > 0)
            {
                for(Map.Entry<Node, Way> error : map.entrySet())
                {
                    errors.add(new TestError(this, Severity.OTHER,
                    tr("Way node near other way"), UNCONNECTED_WAYS,
                    Arrays.asList(error.getKey(), error.getValue())));
                }
            }
            map.clear();
            for(Node en : othernodes)
            {
                for(MyWaySegment s : ways)
                {
                    if(!s.isBoundary && !s.isAbandoned && s.nearby(en, minmiddledist) && (a == null || a.contains(en.getCoor())))
                        map.put(en, s.w);
                }
            }
            if(map.size() > 0)
            {
                for(Map.Entry<Node, Way> error : map.entrySet())
                {
                    errors.add(new TestError(this, Severity.OTHER,
                    tr("Connected way end node near other way"), UNCONNECTED_WAYS,
                    Arrays.asList(error.getKey(), error.getValue())));
                }
            }
        }
        ways = null;
        endnodes = null;
    }

    private class MyWaySegment
    {
        private Line2D line;
        public Way w;
        public Boolean isAbandoned = false;
        public Boolean isBoundary = false;
        public Boolean highway;

        public MyWaySegment(Way w, Node n1, Node n2)
        {
            this.w = w;
            String railway = w.get("railway");
            this.isAbandoned = railway != null && railway.equals("abandoned");
            this.highway = w.get("highway") != null || (railway != null && !isAbandoned);
            this.isBoundary = w.get("boundary") != null && w.get("boundary").equals("administrative") && !this.highway;
            line = new Line2D.Double(n1.getEastNorth().east(), n1.getEastNorth().north(),
            n2.getEastNorth().east(), n2.getEastNorth().north());
        }

        public boolean nearby(Node n, double dist)
        {
            return !w.containsNode(n)
            && line.ptSegDist(n.getEastNorth().east(), n.getEastNorth().north()) < dist;
        }

        public boolean isArea() {
            return w.get("landuse") != null
                || w.get("leisure") != null
                || w.get("building") != null;
        }
    }

    @Override
    public void visit(Way w)
    {
        if( w.deleted || w.incomplete )
            return;
        int size = w.getNodesCount();
        if(size < 2)
            return;
        for(int i = 1; i < size; ++i)
        {
            if(i < size-1)
                addNode(w.getNode(i), middlenodes);
            ways.add(new MyWaySegment(w, w.getNode(i-1), w.getNode(i)));
        }
        Set<Node> set = endnodes;
        if(w.get("highway") != null || w.get("railway") != null)
            set = endnodes_highway;
        addNode(w.getNode(0), set);
        addNode(w.getNode(size-1), set);
    }
    private void addNode(Node n, Set<Node> s)
    {
        Boolean m = middlenodes.contains(n);
        Boolean e = endnodes.contains(n);
        Boolean eh = endnodes_highway.contains(n);
        Boolean o = othernodes.contains(n);
        if(!m && !e && !o && !eh)
            s.add(n);
        else if(!o)
        {
            othernodes.add(n);
            if(e)
                endnodes.remove(n);
            else if(eh)
                endnodes_highway.remove(n);
            else
                middlenodes.remove(n);
        }
    }
}
