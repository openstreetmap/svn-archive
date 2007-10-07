package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.plugins.validator.*;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.plugins.validator.util.Util;

/**
 * Tests if there are segments that crosses in the same layer
 * 
 * @author frsantos
 */
public class CrossingWays extends Test 
{
	/** All way segments, grouped by cells */
	Map<Point2D,List<ExtendedSegment>> cellSegments;
    /** The already detected errors */
	HashSet<WaySegment> errorSegments;
	
	/**
	 * Constructor
	 */
	public CrossingWays() 
	{
		super(tr("Crossing ways."),
			  tr("This test checks if two roads, railways or waterways crosses in the same layer, but are not connected by a node."));
	}


	@Override
	public void startTest() 
	{
		cellSegments = new HashMap<Point2D,List<ExtendedSegment>>(1000);
        errorSegments = new HashSet<WaySegment>();
	}

	@Override
	public void endTest() 
	{
		cellSegments = null;
		errorSegments = null;
	}

	@Override
	public void visit(Way w) 
	{
        if( w.deleted )
            return;
        
        String coastline1 = w.get("natural"); 
        boolean isCoastline1 = coastline1 != null && (coastline1.equals("water") || coastline1.equals("coastline"));
        String railway1 = w.get("railway"); 
        boolean isSubway1 = railway1 != null && railway1.equals("subway");
        if( w.get("highway") == null && w.get("waterway") == null && !isSubway1 && !isCoastline1) 
        	return;
        
        String layer1 = w.get("layer");

		int nodesSize = w.nodes.size();
		for (int i = 0; i < nodesSize - 1; i++) {
			WaySegment ws = new WaySegment(w, i);
            ExtendedSegment es1 = new ExtendedSegment(ws, layer1, railway1, coastline1);
            List<List<ExtendedSegment>> cellSegments = getSegments(es1.n1, es1.n2);
            for( List<ExtendedSegment> segments : cellSegments)
            {
	            for( ExtendedSegment es2 : segments)
	            {
					if (errorSegments.contains(ws) && errorSegments.contains(es2.ws))
	            		continue;
	            	
	                String layer2 = es2.layer;
	                String railway2 = es2.railway;
	                String coastline2 = es2.coastline;
					if (layer1 == null ? layer2 != null : !layer1.equals(layer2))
	                	continue;
	                
	                if( !es1.intersects(es2) ) continue;
		            if( isSubway1 && "subway".equals(railway2)) continue;
		            
		            boolean isCoastline2 = coastline2 != null && (coastline2.equals("water") || coastline2.equals("coastline"));
	                if( isCoastline1 != isCoastline2 ) continue;
	                
                    List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
                    primitives.add(es1.ws.way);
                    primitives.add(es2.ws.way);
                    errors.add( new TestError(this, Severity.WARNING, tr("Crossing ways"), primitives) );
	            }
	            segments.add(es1);
            }
        }
	}
	
	/**
     * Returns all the cells this segment crosses.  Each cell contains the list
     * of segments already processed
     * 
     * @param n1 The first node.
     * @param n2 The second node.
     * @return A list with all the cells the segment crosses.
     */
	public List<List<ExtendedSegment>> getSegments(Node n1, Node n2)
	{
		List<List<ExtendedSegment>> cells = new ArrayList<List<ExtendedSegment>>();
		for( Point2D cell : Util.getSegmentCells(n1, n2, 10000) )
		{
            List<ExtendedSegment> segments = cellSegments.get( cell );
            if( segments == null )
            {
                segments = new ArrayList<ExtendedSegment>();
                cellSegments.put(cell, segments);
            }
            cells.add(segments);
        }
        
		return cells;
	}
    
    /**
     * A way segment with some additional information
     * @author frsantos
     */
    private class ExtendedSegment
    {
		public Node n1, n2;

		public WaySegment ws;
        
        /** The layer */
        public String layer;
        
        /** The railway type */
		public String railway;

		/** The coastline type */
		public String coastline;
        
        /**
         * Constructor
         * @param ws The way segment
         * @param layer The layer of the way this segment is in
         * @param railway The railway type of the way this segment is in
         * @param coastline The coastlyne typo of the way the segment is in
         */
        public ExtendedSegment(WaySegment ws, String layer, String railway, String coastline)
        {
            this.ws = ws;
			this.n1 = ws.way.nodes.get(ws.lowerIndex);
			this.n2 = ws.way.nodes.get(ws.lowerIndex + 1);
            this.layer = layer;
            this.railway = railway;
            this.coastline = coastline;
        }
        
        /**
         * Checks whether this segment crosses other segment
         * @param s2 The other segment
         * @return true if both segements crosses
         */
        public boolean intersects(ExtendedSegment s2)
        {
			if( n1.equals(s2.n1) || n2.equals(s2.n2) ||  
        		n1.equals(s2.n2)   || n2.equals(s2.n1) )
        	{
                return false;
        	}
            
    		return Line2D.linesIntersect(
				n1.eastNorth.east(), n1.eastNorth.north(),
				n2.eastNorth.east(), n2.eastNorth.north(),
				s2.n1.eastNorth.east(), s2.n1.eastNorth.north(),
				s2.n2.eastNorth.east(), s2.n2.eastNorth.north());
        }
    }
}
