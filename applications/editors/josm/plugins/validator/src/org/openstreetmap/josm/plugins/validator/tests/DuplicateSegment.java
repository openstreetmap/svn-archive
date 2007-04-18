package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;
/**
 * Tests if there are duplicate segments
 * 
 * @author frsantos
 */
public class DuplicateSegment extends Test 
{
	/** Bag of all segments */
	Bag<CompoundLatLon, OsmPrimitive> segments;
	
	/**
	 * Constructor
	 */
	public DuplicateSegment() 
	{
		super(tr("Duplicated segments."),
			  tr("This test checks that two nodes are not used by more than one segment."));
		
	}


	@Override
	public void startTest() 
	{
		segments = new Bag<CompoundLatLon, OsmPrimitive>(1000);
	}

	@Override
	public void endTest() 
	{
		for(List<OsmPrimitive> duplicated : segments.values() )
		{
			if( duplicated.size() > 1)
			{
				errors.add( new TestError(this, Severity.ERROR, tr("Duplicated segments"), duplicated) );
			}
		}
		segments = null;
	}

	@Override
	public void visit(Segment s) 
	{
		if( !s.incomplete ) segments.add( new CompoundLatLon(s), s);
	}
	
	/**
	 * Compound LatLong for easy duplicity check
	 * @author frsantos
	 */
	class CompoundLatLon
	{
		/** From position */
		LatLon from;
		/** To position */
		LatLon to;
		
		/**
		 * Constructor
		 * @param s The segment
		 */
		public CompoundLatLon(Segment s)
		{
			this.from = s.from.coor;
			this.to = s.to.coor;
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (obj == null || getClass() != obj.getClass() )
				return super.equals(obj);
			
			CompoundLatLon other = (CompoundLatLon)obj;
			return from.equals(other.from) && to.equals(other.to) ||
				   to.equals(other.from) && from.equals(other.to);
		}

		@Override
		public int hashCode() 
		{
			return from.hashCode() + to.hashCode();
		}
	}
}
