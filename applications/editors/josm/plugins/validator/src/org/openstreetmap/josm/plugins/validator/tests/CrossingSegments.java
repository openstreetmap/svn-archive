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
public class CrossingSegments extends Test 
{
	/** All segments, grouped by cells */
	Map<Point2D,List<ExtendedSegment>> cellSegments;
    /** The already detected errors */
    Bag<Segment, Segment> errorSegments;
	
	/**
	 * Constructor
	 */
	public CrossingSegments() 
	{
		super(tr("Crossing roads."),
			  tr("This test checks if two roads,railways or waterways crosses in the same layer, but are not connected by a node."));
	}


	@Override
	public void startTest() 
	{
		cellSegments = new HashMap<Point2D,List<ExtendedSegment>>(1000);
        errorSegments = new Bag<Segment, Segment>();
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
        for(Segment s : w.segments)
        {
        	if( s.incomplete )
        		continue;
        	
            ExtendedSegment es1 = new ExtendedSegment(s, layer1, railway1, coastline1);
            List<List<ExtendedSegment>> cellSegments = getSegments(s);
            for( List<ExtendedSegment> segments : cellSegments)
            {
	            for( ExtendedSegment es2 : segments)
	            {
	            	if( errorSegments.contains(s, es2.s) || errorSegments.contains(es2.s, s))
	            		continue;
	            	
	                String layer2 = es2.layer;
	                String railway2 = es2.railway;
	                String coastline2 = es2.coastline;
	                if( (layer1 != null || layer2 != null) && (layer1 == null || !layer1.equals(layer2)) )
	                	continue;
	                
	                if( !es1.intersects(es2) ) continue;
		            if( isSubway1 && "subway".equals(railway2)) continue;
		            
		            boolean isCoastline2 = coastline2 != null && (coastline2.equals("water") || coastline2.equals("coastline"));
	                if( isCoastline1 != isCoastline2 ) continue;
	                
                    List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
                    primitives.add(es1.s);
                    primitives.add(es2.s);
                    errors.add( new TestError(this, Severity.WARNING, tr("Crossing roads"), primitives) );
	            }
	            segments.add(es1);
            }
        }
	}
	
	/**
     * Returns all the cells this segment crosses . Each cell contains the list
     * of segments already processed
     * 
     * @param s The segment
     * @return A list with all the cells the segment crosses.
     */
	public List<List<ExtendedSegment>> getSegments(Segment s)
	{
		List<List<ExtendedSegment>> cells = new ArrayList<List<ExtendedSegment>>();
		for( Point2D cell : Util.getSegmentCells(s, 10000) )
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
     * A segment is a line with the formula "y = a*x+b"
     * @author frsantos
     */
    class ExtendedSegment
    {
        /** The segment */
        Segment s;
        
        /** The layer */
        String layer;
        
        /** The railway type */
		private String railway;

		/** The coastline type */
		private String coastline;
        
        /**
         * Constructor
         * @param s The segment
         * @param layer The layer of the way this segment is in
         * @param railway The railway type of the way this segment is in
         * @param coastline The coastlyne typo of the way the segment is in
         */
        public ExtendedSegment(Segment s, String layer, String railway, String coastline)
        {
            this.s = s;
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
        	Node n1From = this.s.from;
			Node n1To = this.s.to;
			Node n2From = s2.s.from;
			Node n2To = s2.s.to;
			if( n1From.equals(n2From) || n1To.equals(n2To) ||  
        		n1From.equals(n2To)   || n1To.equals(n2From) )
        	{
                return false;
        	}
            
    		return Line2D.linesIntersect(n1From.eastNorth.east(), 
					    				 n1From.eastNorth.north(),
					    				 n1To.eastNorth.east(),
					    				 n1To.eastNorth.north(),
					    				 n2From.eastNorth.east(),
					    				 n2From.eastNorth.north(),
					    				 n2To.eastNorth.east(),
					    				 n2To.eastNorth.north());
        }
    }
}
