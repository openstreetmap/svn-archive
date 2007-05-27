package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Tests if there are segments that crosses in the same layer
 * 
 * @author frsantos
 */
public class CrossingSegments extends Test 
{
	/** All segments, grouped by cells */
	Map<Point2D,List<ExtendedSegment>> cellSegments;
	
	/**
	 * Constructor
	 */
	public CrossingSegments() 
	{
		super(tr("Crossing roads."),
			  tr("This test checks if two roads crosses in the same layer, but are not connected by a node."));
	}


	@Override
	public void startTest() 
	{
		cellSegments = new HashMap<Point2D,List<ExtendedSegment>>(1000);
	}

	@Override
	public void endTest() 
	{
		cellSegments = null;
	}

	@Override
	public void visit(Way w) 
	{

        if( w.deleted || w.get("highway") == null)
            return;
        
        String layer1 = w.get("layer");
        for(Segment s : w.segments)
        {
        	if( s.incomplete )
        		continue;
        	
            ExtendedSegment es1 = new ExtendedSegment(s, layer1);
            List<List<ExtendedSegment>> cellSegments = getSegments(s);
            for( List<ExtendedSegment> segments : cellSegments)
            {
	            for( ExtendedSegment es2 : segments)
	            {
	                String layer2 = es2.layer;
	                if( (layer1 == null && layer2 == null || layer1 != null && layer1.equals(layer2)) &&
	                	es1.intersects(es2))
	                {
	                    List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
	                    primitives.add(es1.s);
	                    primitives.add(es2.s);
	                    errors.add( new TestError(this, Severity.WARNING, tr("Crossing roads"), primitives) );
	                }
	            }
	            segments.add(es1);
            }
        }
	}
	
	/**
	 * Returns all the cells this segment crosses . Each cell contains the list
	 * of segments already processed
	 * 
	 * @param s
	 *            The segment
	 * @return A list with all the cells the segment crosses.
	 */
	public List<List<ExtendedSegment>> getSegments(Segment s)
	{
		List<List<ExtendedSegment>> cells = new ArrayList<List<ExtendedSegment>>();
		long x0 = Math.round(s.from.eastNorth.east()*100);
		long x1 = Math.round(s.to.eastNorth.east()*100);
		for( ; x0<=x1; x0++)
		{
			long y0 = Math.round(s.from.eastNorth.north()*100);
			long y1 = Math.round(s.to.eastNorth.north()*100);
			for( ; y0<=y1; y0++)
			{
				Point2D p = new Point2D.Double(x0, y0);
				List<ExtendedSegment> segments = cellSegments.get( p );
				if( segments == null )
				{
					segments = new ArrayList<ExtendedSegment>();
					cellSegments.put(p, segments);
				}
				
				cells.add(segments);
			}
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
        
        /**
         * Constructor
         * @param s The segment
         * @param layer The layer of the way this segment is in
         */
        public ExtendedSegment(Segment s, String layer)
        {
            this.s = s;
            this.layer = layer;
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
