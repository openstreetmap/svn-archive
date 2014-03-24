// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

/**
 * SolutionType represents the solution kind for a problem, e. g. 'remove' deletes the
 * tag causing the problem.
 */
public enum SolutionType {
    Remove,
    Change,
    Add
}
