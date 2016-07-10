// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.graphview.core.data;

/**
 * source of OSM data that can be used to build graphs from
 *
 * @param <N>  node type
 * @param <W>  way type
 * @param <R>  relation type
 */
public interface DataSource<N, W, R, M> {

    /** returns all nodes */
    Iterable<N> getNodes();

    /** returns all ways */
    Iterable<W> getWays();

    /** returns all relations */
    Iterable<R> getRelations();

    /** returns a node's latitude */
    double getLat(N node);

    /** returns a node's longitude */
    double getLon(N node);

    /** returns a way's nodes */
    Iterable<N> getNodes(W way);

    /** returns a relation's members */
    Iterable<M> getMembers(R relation);

    /** returns a node's tags */
    TagGroup getTagsN(N node);

    /** returns a way's tags */
    TagGroup getTagsW(W way);

    /** returns a relation's tags */
    TagGroup getTagsR(R relation);

    /** returns a relation member's role */
    String getRole(M member);

    /** returns a relation member's member object */
    Object getMember(M member);

    /** returns whether a relation member is a node */
    boolean isNMember(M member);

    /** returns whether a relation member is a way */
    boolean isWMember(M member);

    /** returns whether a relation member is a relation */
    boolean isRMember(M member);

    /**
     * adds an observer.
     * Does nothing if the parameter is already an observer of this DataSource.
     *
     * @param observer  observer object, != null
     */
    void addObserver(DataSourceObserver observer);

    /**
     * deletes an observer that has been added using {@link #addObserver(DataSourceObserver)}.
     * Does nothing if the parameter isn't currently an observer of this DataSource.
     *
     * @param observer  observer object, != null
     */
    void deleteObserver(DataSourceObserver observer);
}
