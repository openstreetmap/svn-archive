package org.openstreetmap.josm.plugins.graphview.core.transition;

import java.util.Collection;

/**
 * physical or legal restriction that forbids entering and leaving a set
 * of {@link Segment}s using certain in- and outbound segments
 * despite those Segments being connected by sharing nodes
 */
public interface Restriction {

	/**
	 * returns the starting segment that will trigger the restriction when used;
	 * != null
	 */
	public Segment getFrom();

	/**
	 * returns the "via" segments.
	 * The restriction will remain active as long as only via segments are used.
	 *
	 * @return  unmodifiable collection of segments; != null
	 */
	public Collection<Segment> getVias();

	/**
	 * returns the forbidden "to" segments.
	 * The restriction prevents leaving the via segment set by using one of the to segments.
	 *
	 * @return  unmodifiable collection of segments; != null
	 */
	public Collection<Segment> getTos();

}
