package org.openstreetmap.util;

/**
 * A line for which only the id is known.
 * @author Imi
 */
public class LineOnlyId extends Line {

	public LineOnlyId(long id) {
		super(null, null);
		if (id <= 0)
			throw new IllegalArgumentException("id must not be 0 or negative");
		this.id = id;
	}

	public void register() {
	}

	public void unregister() {
	}
}
