package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.plugins.validator.*;
import org.openstreetmap.josm.plugins.validator.util.Bag;

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
        
        if( w.get("highway") == null && w.get("waterway") == null && w.get("railway") == null )
        	return;
        
        String layer1 = w.get("layer");
        String railway1 = w.get("railway");
        for(Segment s : w.segments)
        {
        	if( s.incomplete )
        		continue;
        	
            ExtendedSegment es1 = new ExtendedSegment(s, layer1, railway1);
            List<List<ExtendedSegment>> cellSegments = getSegments(s);
            for( List<ExtendedSegment> segments : cellSegments)
            {
	            for( ExtendedSegment es2 : segments)
	            {
	            	if( errorSegments.contains(s, es2.s) || errorSegments.contains(es2.s, s))
	            		continue;
	            	
	                String layer2 = es2.layer;
	                String railway2 = es2.railway;
	                if( (layer1 == null && layer2 == null || layer1 != null && layer1.equals(layer2)) &&
	                	!("subway".equals(railway1) && "subway".equals(railway2)) &&	
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
     * <p>
     * This method uses the Bresenham algorithm to follow all cells this segment
     * crosses, so, in very few cases (when the segment is very long, and
     * crosses a cell very close to the corner), it can miss a cell.
     * 
     * @param s The segment
     * @return A list with all the cells the segment crosses.
     */
	public List<List<ExtendedSegment>> getSegments(Segment s)
	{
		List<List<ExtendedSegment>> cells = new ArrayList<List<ExtendedSegment>>();
		long x0 = Math.round(s.from.eastNorth.east() * 1000);
		long x1 = Math.round(s.to.eastNorth.east()   * 1000);
        long y0 = Math.round(s.from.eastNorth.north()* 1000);
        long y1 = Math.round(s.to.eastNorth.north()  * 1000);
        
        boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        long aux;
        if( steep )
        {
            aux = x0; x0 = x1; x1 = aux;
            aux = y0; y0 = y1; y1 = aux;
        }
        if( x0 > x1 )
        {
            aux = x0; x0 = x1; x1 = aux;
            aux = y0; y0 = y1; y1 = aux;
        }
        long dx  = x1 - x0,
             dy  = Math.abs(y1 - y0),
             y   = y0,
             error = -dx/2,
             ystep = y0 < y1 ? 1 : -1;
      
        for( long x = x0; x <= x1; x++ )  
        {
            Point2D p = steep ? new Point2D.Double(y, x) : new Point2D.Double(x, y);
            List<ExtendedSegment> segments = cellSegments.get( p );
            if( segments == null )
            {
                segments = new ArrayList<ExtendedSegment>();
                cellSegments.put(p, segments);
            }
            cells.add(segments);
            
            error += dy;
            if( error > 0 )
            {
                error -= dx;
                y += ystep;
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
        
        /** The railway type */
		private String railway;
        
        /**
         * Constructor
         * @param s The segment
         * @param layer The layer of the way this segment is in
         * @param railway The railway type of the way this segment is in
         */
        public ExtendedSegment(Segment s, String layer, String railway)
        {
            this.s = s;
            this.layer = layer;
            this.railway = railway;
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
