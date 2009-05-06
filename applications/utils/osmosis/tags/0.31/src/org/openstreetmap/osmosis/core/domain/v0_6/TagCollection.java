// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.Collection;
import java.util.Map;

import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * An extension to basic Collection behaviour adding convenience methods for working with tags.
 * 
 * @author Brett Henderson
 */
public interface TagCollection extends Collection<Tag>, Storeable {
	/**
	 * Creates a map representation of the tags.
	 * 
	 * @return The tags represented as a map.
	 */
	Map<String, String> buildMap();
}
