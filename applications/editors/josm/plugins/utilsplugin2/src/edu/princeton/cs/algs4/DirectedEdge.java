// License: GPL v3 or later courtesy of author Kevin Wayne
package edu.princeton.cs.algs4;

/*************************************************************************
 *  Compilation:  javac DirectedEdge.java
 *  Execution:    java DirectedEdge
 *
 *  Immutable weighted directed edge.
 *
 *************************************************************************/

/**
 *  The <tt>DirectedEdge</tt> class represents a weighted edge in an directed graph.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/44sp">Section 4.4</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */

public class DirectedEdge {
    private final int v;
    private final int w;
    private final double weight;

    /**
     * Create a directed edge from v to w with given weight.
     */
    public DirectedEdge(int v, int w, double weight) {
        this.v = v;
        this.w = w;
        this.weight = weight;
    }

    /**
     * Return the vertex where this edge begins.
     */
    public int from() {
        return v;
    }

    /**
     * Return the vertex where this edge ends.
     */
    public int to() {
        return w;
    }

    /**
     * Return the weight of this edge.
     */
    public double weight() { return weight; }

    /**
     * Return a string representation of this edge.
     */
    @Override
    public String toString() {
        return v + "->" + w + " " + String.format("%5.2f", weight);
    }
}
