package org.openstreetmap.josm.plugins.validator.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.visitor.Visitor;

/**
 * A visitor that aggregates all primitives it visits.
 * <p>
 * The primitives are sorted according to their tyep: first Nodes, then
 * Segments, and Ways last.
 * 
 * @author frsantos
 */
public class AgregatePrimitivesVisitor implements Visitor
{
	/** Aggregated data */
	Collection<OsmPrimitive> aggregatedData;

	/**
	 * Constructor
	 */
	public AgregatePrimitivesVisitor() 
	{
		aggregatedData = new TreeSet<OsmPrimitive>( new PrimitiveComparator());
	}

	/**
	 * Visits a collection of primitives 
	 * @param data The collection of primitives 
	 * @return The aggregated primitives
	 */
	public Collection<OsmPrimitive> visit(Collection<OsmPrimitive> data) 
	{
		for (OsmPrimitive osm : data) 
		{
			osm.visit(this);
		}
		
		return aggregatedData;
	}

	public void visit(Node n) 
	{
		aggregatedData.add(n);
	}

	public void visit(Segment s) 
	{
		aggregatedData.add(s);
		if( s.from != null ) visit(s.from);
		if( s.to != null )   visit(s.to);
	}

	public void visit(Way w) 
	{
		aggregatedData.add(w);
		for(Segment s : w.segments)
			visit(s);
	}

	/**
	 * A comparator that orders Nodes first, then Segments and Ways last.
	 * 
	 * @author frsantos
	 */
	class PrimitiveComparator implements Comparator<OsmPrimitive>
	{
		public int compare(OsmPrimitive o1, OsmPrimitive o2) 
		{
			if( o1 instanceof Node)
			{
				return o2 instanceof Node ? o1.hashCode() - o2.hashCode() : -1;
			}
			else if( o1 instanceof Way)
			{
				return o2 instanceof Way ? o1.hashCode() - o2.hashCode() : 1;
			}
			else // o1 is a segment
			{
				if( o2 instanceof Node ) return 1;
				if( o2 instanceof Way ) return -1;
				return o1.hashCode() - o2.hashCode();
			}
		}
	}
}
