package org.openstreetmap.josm.plugins.validator.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.visitor.Visitor;

/**
 * A visitor that aggregates all primitives it visits.
 * <p>
 * The primitives are sorted according to their type: first nodes, then ways.
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

	public void visit(Way w) 
	{
		aggregatedData.add(w);
		for (Node n : w.nodes)
			visit(n);
	}

	public void visit(Relation r) {
		// Relations can be cyclic so don't visit them twice.
		if (!aggregatedData.contains(r)) {
			aggregatedData.add(r);
			for (RelationMember m : r.members) {
				m.member.visit(this);
			}
		}
	}

	/**
	 * A comparator that orders nodes first, ways last.
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
			} else {
				return o1.hashCode() - o2.hashCode();
			}
		}
	}
}
