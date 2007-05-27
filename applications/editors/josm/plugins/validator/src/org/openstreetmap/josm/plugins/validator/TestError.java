package org.openstreetmap.josm.plugins.validator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.visitor.Visitor;
import org.openstreetmap.josm.gui.MapView;

/**
 * Validation error
 * @author frsantos
 */
public class TestError
{
	/** Severity */
	private Severity severity;
	/** The error message */
	private String message;
	/** The affected primitives */
	private List<OsmPrimitive> primitives;
	/** The tester that raised this error */
	private Test tester;
	/** Internal code used by testers to classify errors */
	private int internalCode;
    /** If this error is selected */
    private boolean selected;
	
	/**
	 * Constructor
	 */
	public TestError()
	{
	}

	/**
	 * Constructor
	 * @param tester The tester
	 * @param severity The severity of this error
	 * @param message The error message
	 * @param primitives The affected primitives
	 */
	public TestError(Test tester, Severity severity, String message, List<OsmPrimitive> primitives)
	{
		this.tester = tester;
		this.severity = severity;
		this.message = message;
		this.primitives = primitives;
	}
	
	/**
	 * Constructor
	 * @param tester The tester
	 * @param severity The severity of this error
	 * @param message The error message
	 * @param primitive The affected primitive
	 */
	public TestError(Test tester, Severity severity, String message, OsmPrimitive primitive)
	{
		this.tester = tester;
		this.severity = severity;
		this.message = message;
		
		List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
		primitives.add(primitive);
		
		this.primitives = primitives;
	}
	
	/**
	 * Constructor
	 * @param tester The tester
	 * @param severity The severity of this error
	 * @param message The error message
	 * @param primitive The affected primitive
	 * @param internalCode The internal code
	 */
	public TestError(Test tester, Severity severity, String message, OsmPrimitive primitive, int internalCode)
	{
		this(tester, severity, message, primitive);
		this.internalCode = internalCode;
	}
	
	/**
	 * Gets the error message
	 * @return the error message
	 */
	public String getMessage() 
	{
		return message;
	}
	
	/**
	 * Sets the error message
	 * @param message The error message
	 */
	public void setMessage(String message) 
	{
		this.message = message;
	}
	
	/**
	 * Gets the list of primitives affected by this error 
	 * @return the list of primitives affected by this error
	 */
	public List<OsmPrimitive> getPrimitives() 
	{
		return primitives;
	}

	/**
	 * Sets the list of primitives affected by this error 
	 * @param primitives the list of primitives affected by this error
	 */

	public void setPrimitives(List<OsmPrimitive> primitives) 
	{
		this.primitives = primitives;
	}

	/**
	 * Gets the severity of this error
	 * @return the severity of this error
	 */
	public Severity getSeverity() 
	{
		return severity;
	}

	/**
	 * Sets the severity of this error
	 * @param severity the severity of this error
	 */
	public void setSeverity(Severity severity) 
	{
		this.severity = severity;
	}

	/**
	 * Gets the tester that raised this error 
	 * @return the tester that raised this error
	 */
	public Test getTester() 
	{
		return tester;
	}

	/**
	 * Gets the internal code
	 * @return the internal code
	 */
	public int getInternalCode() 
	{
		return internalCode;
	}

	
	/**
	 * Sets the internal code
	 * @param internalCode The internal code
	 */
	public void setInternalCode(int internalCode) 
	{
		this.internalCode = internalCode;
	}
	
	/**
	 * Returns true if the error can be fixed automatically
	 * 
	 * @return true if the error can be fixed
	 */
	public boolean isFixable()
	{
		return tester != null && tester.isFixable(this);
	}
	
	/**
	 * Fixes the error with the appropiate command
	 * 
	 * @return The command to fix the error
	 */
	public Command getFix()
	{
		if( tester == null )
			return null;
		
		return tester.fixError(this);
	}

    /**
     * Paints the error on affected primitives
     * 
     * @param g The graphics
     * @param mv The MapView
     */
    public void paint(Graphics g, MapView mv)
    {
        PaintVisitor v = new PaintVisitor(g, mv);
        for( OsmPrimitive p : primitives)
        {
            if( !p.deleted )
                p.visit(v);
        }
    }	
    
    /**
     * Visitor that highlights the primitives affected by this error
     * @author frsantos
     */
    class PaintVisitor implements Visitor
    {
        /** The graphics */
        private final Graphics g;
        /** The MapView */
        private final MapView mv;
        
        /**
         * Constructor 
         * @param g The graphics 
         * @param mv The Mapview
         */
        public PaintVisitor(Graphics g, MapView mv)
        {
            this.g = g;
            this.mv = mv;
        }

        /**
         * Draws a circle around the node 
         * @param n The node
         * @param color The circle color
         */
        public void drawNode(Node n, Color color)
        {
            Point p = mv.getPoint(n.eastNorth);
            g.setColor(color);
            if( selected )
            {
                g.fillOval(p.x-5, p.y-5, 10, 10);
            }
            else
                g.drawOval(p.x-5, p.y-5, 10, 10);
        }

        /**
         * Draws a line around the segment
         * 
         * @param s The segment
         * @param color The color
         */
        public void drawSegment(Segment s, Color color)
        {
            Point p1 = mv.getPoint(s.from.eastNorth);
            Point p2 = mv.getPoint(s.to.eastNorth);
            g.setColor(color);
            
            double t = Math.atan2(p2.x-p1.x, p2.y-p1.y);
            double cosT = Math.cos(t);
            double sinT = Math.sin(t);
            int deg = (int)Math.toDegrees(t);
            if( selected )
            {
                int[] x = new int[] {(int)(p1.x + 5*cosT), (int)(p2.x + 5*cosT), (int)(p2.x - 5*cosT), (int)(p1.x - 5*cosT)};
                int[] y = new int[] {(int)(p1.y - 5*sinT), (int)(p2.y - 5*sinT), (int)(p2.y + 5*sinT), (int)(p1.y + 5*sinT)};
                g.fillPolygon(x, y, 4);
                g.fillArc(p1.x-5, p1.y-5, 10, 10, deg, 180 );
                g.fillArc(p2.x-5, p2.y-5, 10, 10, deg, -180);
            }
            else
            {
                g.drawLine((int)(p1.x + 5*cosT), (int)(p1.y - 5*sinT), (int)(p2.x + 5*cosT), (int)(p2.y - 5*sinT));
                g.drawLine((int)(p1.x - 5*cosT), (int)(p1.y + 5*sinT), (int)(p2.x - 5*cosT), (int)(p2.y + 5*sinT));
                g.drawArc(p1.x-5, p1.y-5, 10, 10, deg, 180 );
                g.drawArc(p2.x-5, p2.y-5, 10, 10, deg, -180);
            }
        }


        
        /**
         * Draw a small rectangle. 
         * White if selected (as always) or red otherwise.
         * 
         * @param n The node to draw.
         */
        public void visit(Node n) 
        {
            if( isNodeVisible(n) )
                drawNode(n, severity.getColor());
        }

        /**
         * Draw just a line between the points.
         * White if selected (as always) or green otherwise.
         */
        public void visit(Segment ls) 
        {
            if( isSegmentVisible(ls) )
                drawSegment(ls, severity.getColor());
        }
        
        public void visit(Way w)
        {
            for (Segment ls : w.segments)
            {
                if (isSegmentVisible(ls))
                {
                    drawSegment(ls, severity.getColor());
                }
            }
        }
        
        /**
         * Checks if the given node is in the visible area.
         * @param n The node to check for visibility
         * @return true if the node is visible
         */
        protected boolean isNodeVisible(Node n) {
            Point p = mv.getPoint(n.eastNorth);
            return !((p.x < 0) || (p.y < 0) || (p.x > mv.getWidth()) || (p.y > mv.getHeight()));
        }

        /**
         * Checks if the given segment is in the visible area.
         * NOTE: This will return true for a small number of non-visible
         *       segments.
         * @param ls The segment to check
         * @return true if the segment is visible
         */
        protected boolean isSegmentVisible(Segment ls) {
            if (ls.incomplete) return false;
            Point p1 = mv.getPoint(ls.from.eastNorth);
            Point p2 = mv.getPoint(ls.to.eastNorth);
            if ((p1.x < 0) && (p2.x < 0)) return false;
            if ((p1.y < 0) && (p2.y < 0)) return false;
            if ((p1.x > mv.getWidth()) && (p2.x > mv.getWidth())) return false;
            if ((p1.y > mv.getHeight()) && (p2.y > mv.getHeight())) return false;
            return true;
        }
    }

    /**
     * Sets the selection flag of this error
     * @param selected if this error is selected
     */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
}
