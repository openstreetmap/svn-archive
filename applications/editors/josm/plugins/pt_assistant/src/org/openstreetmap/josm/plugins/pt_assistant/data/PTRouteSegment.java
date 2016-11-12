// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.data;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Represents a piece of a route that includes two consecutive stops and the
 * ways between them. Route segments are ordered, i.e. for most routes there
 * will be two route segments between each pair of consecutive stops, one in
 * each direction.
 *
 * @author darya
 *
 */

public class PTRouteSegment {

    /* first stop of the route segment */
    private PTStop firstStop;

    /* last stop of the route segment */
    private PTStop lastStop;

    /* ptways that belong to this route segment */
    private List<PTWay> ptways;

    /* fix variants available for this route segment */
    private List<List<PTWay>> fixVariants;

    /* route relation for which this route segment was created */
    private Relation relation;

    /**
     * Constructor
     * @param firstStop first stop of the route segment
     * @param lastStop last stop of the route segment
     * @param ways ways PTWays that belong to this route segment
     * @param relation the route relation for which this route segment is created
     */
    public PTRouteSegment(PTStop firstStop, PTStop lastStop, List<PTWay> ways, Relation relation) {
        this.firstStop = firstStop;
        this.lastStop = lastStop;
        this.ptways = new ArrayList<>(ways.size());
        ptways.addAll(ways);
        fixVariants = new ArrayList<>();
        this.relation = relation;
    }

    /**
     * Returns the PTWays of this route segment
     * @return the PTWays of this route segment
     */
    public List<PTWay> getPTWays() {
        return this.ptways;
    }

    /**
     * Sets the PTWays of this route segment to the given list
     * @param ptwayList list of ways
     */
    public void setPTWays(List<PTWay> ptwayList) {
        this.ptways = ptwayList;
        this.fixVariants.clear();
    }

    /**
     * Returns the first stop of this route segment
     * @return the first stop of this route segment
     */
    public PTStop getFirstStop() {
        return this.firstStop;
    }

    /**
     * Returns the last stop of this route segment
     * @return the last stop of this route segment
     */
    public PTStop getLastStop() {
        return this.lastStop;
    }

    /**
     * Returns the first PTWay of this route segment
     * @return the first PTWay of this route segment
     */
    public PTWay getFirstPTWay() {
        if (ptways.isEmpty()) {
            return null;
        }
        return ptways.get(0);
    }

    /**
     * Returns the last PTWay of this route segment
     * @return the last PTWay of this route segment
     */
    public PTWay getLastPTWay() {
        if (ptways.isEmpty()) {
            return null;
        }
        return ptways.get(ptways.size() - 1);
    }

    /**
     * Returns the first way of this route segment
     * @return the first way of this route segment
     */
    public Way getFirstWay() {
        if (ptways.isEmpty()) {
            return null;
        }
        return ptways.get(0).getWays().get(0);
    }

    /**
     * Returns the last way of this route segment
     * @return the last way of this route segment
     */
    public Way getLastWay() {
        if (ptways.isEmpty()) {
            return null;
        }
        List<Way> waysOfLast = ptways.get(ptways.size() - 1).getWays();
        return waysOfLast.get(waysOfLast.size() - 1);
    }

    /**
     * Adds the new fix variant if an identical fix variant (i.e. same ways) is
     * not already contained in the list of the fix variants of this.
     *
     * @param list the PTWays of the new fix variant
     */
    public synchronized void addFixVariant(List<PTWay> list) {
        List<Way> otherWays = new ArrayList<>();
        for (PTWay ptway : list) {
            otherWays.addAll(ptway.getWays());
        }

        for (List<PTWay> fixVariant : this.fixVariants) {
            List<Way> thisWays = new ArrayList<>();
            for (PTWay ptway : fixVariant) {
                thisWays.addAll(ptway.getWays());
            }
            boolean listsEqual = (thisWays.size() == otherWays.size());
            if (listsEqual) {
                for (int i = 0; i < thisWays.size(); i++) {
                    if (thisWays.get(i).getId() != otherWays.get(i).getId()) {
                        listsEqual = false;
                        break;
                    }
                }
            }
            if (listsEqual) {
                return;
            }
        }

        this.fixVariants.add(list);
    }

    /**
     * Returns the fix variants stored for this route segment
     * @return the fix variants stored for this route segment
     */
    public List<List<PTWay>> getFixVariants() {
        return this.fixVariants;
    }

    /**
     * Returns the route relation for which this route segment was created
     * @return the route relation for which this route segment was created
     */
    public Relation getRelation() {
        return this.relation;
    }

    /**
     * Checks if this and the other route segments are equal
     *
     * @param other other route segment
     * @return {@code true} if this and the other route segments are equal
     */
    public boolean equalsRouteSegment(PTRouteSegment other) {

        List<Way> thisWays = new ArrayList<>();
        for (PTWay ptway : this.ptways) {
            thisWays.addAll(ptway.getWays());
        }
        List<Way> otherWays = new ArrayList<>();
        for (PTWay ptway : other.getPTWays()) {
            otherWays.addAll(ptway.getWays());
        }

        if (thisWays.size() != otherWays.size()) {
            return false;
        }

        for (int i = 0; i < thisWays.size(); i++) {
            if (thisWays.get(i).getId() != otherWays.get(i).getId()) {
                return false;
            }
        }

        return true;
    }

}
