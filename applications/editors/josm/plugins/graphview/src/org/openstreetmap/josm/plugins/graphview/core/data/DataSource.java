package org.openstreetmap.josm.plugins.graphview.core.data;

/**
 * source of OSM data that can be used to build graphs from
 *
 * @param <N>  node type
 * @param <W>  way type
 * @param <R>  relation type
 */
public interface DataSource<N, W, R> {

	public static interface RelationMember {
		/** returns the member's role. != null */
		public String getRole();
		/** returns the member itself. Must be instance of N, W or R, != null */
		public Object getMember();
	}

	/** returns all nodes */
	public Iterable<N> getNodes();

	/** returns all ways */
	public Iterable<W> getWays();

	/** returns all relations */
	public Iterable<R> getRelations();

	/** returns a node's latitude */
	public double getLat(N node);

	/** returns a node's longitude */
	public double getLon(N node);

	/** returns a way's nodes */
	public Iterable<N> getNodes(W way);

	/** returns a relation's members */
	public Iterable<RelationMember> getMembers(R relation);

	/** returns a node's tags */
	public TagGroup getTagsN(N node);

	/** returns a way's tags */
	public TagGroup getTagsW(W way);

	/** returns a relation's tags */
	public TagGroup getTagsR(R relation);

	/**
	 * adds an observer.
	 * Does nothing if the parameter is already an observer of this DataSource.
	 *
	 * @param observer  observer object, != null
	 */
	public void addObserver(DataSourceObserver observer);

	/**
	 * deletes an observer that has been added using {@link #addObserver(DataSourceObserver)}.
	 * Does nothing if the parameter isn't currently an observer of this DataSource.
	 *
	 * @param observer  observer object, != null
	 */
	public void deleteObserver(DataSourceObserver observer);

}