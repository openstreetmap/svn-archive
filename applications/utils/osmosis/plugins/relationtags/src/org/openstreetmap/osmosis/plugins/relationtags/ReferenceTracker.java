
package org.openstreetmap.osmosis.plugins.relationtags;

import org.openstreetmap.osmosis.core.filter.common.IdTracker;
import java.util.*;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerFactory;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.util.LongAsInt;

/**
 * This class stores referencing IDs for every ID. For example, it can used to track relations
 * that contain specific nodes. It's like 2D array of IdTrackers.
 * 
 * @author Zverik
 */
public class ReferenceTracker {
    private IdTracker[] trackers; // continous list of trackers
    private int trackerCount;
    private int[][] idList; // mapping from nodeId to index of tracker

    public ReferenceTracker() {
        trackers = new IdTracker[1];
        trackerCount = 0;
        idList = new int[100][2];

        // from ListIdTracker
        idOffset = 0;
        maxIdAdded = Integer.MIN_VALUE;
        sorted = true;
    }

    public void set( long nodeId, long refId ) {
        IdTracker t = get(nodeId);
        if( t == null )
            t = addTracker(nodeId);
        t.set(refId);
    }

    public IdTracker get( long nodeId ) {
        Integer pos = getMapping(nodeId);
        return pos == null ? null : trackers[pos];
    }

    /**
     * Adds a new IdTracker to trackers array and returns it.
     */
    protected IdTracker addTracker( long nodeId ) {
        IdTracker t = IdTrackerFactory.createInstance(IdTrackerType.IdList);
        if( trackerCount == trackers.length ) {
            int newListLength = (int)(trackers.length * LIST_SIZE_EXTENSION_FACTOR);
            if( newListLength == trackers.length ) {
                newListLength++;
            }

            IdTracker[] newTrackerList = new IdTracker[newListLength];

            System.arraycopy(trackers, 0, newTrackerList, 0, trackers.length);

            trackers = newTrackerList;
        }
        setMapping(nodeId, trackerCount);
        trackers[trackerCount++] = t;
        return t;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("ReferenceTracker[");
        boolean first;
        for( int i = 0; i < idOffset; i++ ) {
            if( i > 0 )
                res.append("; ");
            res.append(idList[i][0]).append(':');
            first = true;
            for( Iterator<Long> iter = trackers[idList[i][1]].iterator(); iter.hasNext(); ) {
                if( !first )
                    res.append(',');
                else
                    first = false;
                res.append(iter.next());
            }
        }
        return res.append(']').toString();
    }

    public static void main( String[] args ) {
        ReferenceTracker t = new ReferenceTracker();
        t.set(10, 3);
        t.set(62, 5);
        t.set(11, 3);
        t.set(11, 1);
        t.set(31, 9);
        t.set(32, 9);
        t.set(32, 3);
        t.set(62, 1);
        System.out.println(t);
    }

    // ----------------------- most of code below this point is copied from ListIdTracker.java -------------------------

    /**
     * The internal list size is multiplied by this factor when more space is
     * required.
     */
    private static final double LIST_SIZE_EXTENSION_FACTOR = 1.5;

    /**
     * Flags where the maximum written id offset occurs in the list. If new
     * values are added and the list is full, new space must be allocated.
     */
    /* package */ int idOffset;
    private int maxIdAdded;
    private boolean sorted;

    /**
     * Increases the size of the id list to make space for new ids.
     */
    private void extendIdList() {
        int[][] newIdList;
        int newListLength;

        newListLength = (int)(idList.length * LIST_SIZE_EXTENSION_FACTOR);
        if( newListLength == idList.length ) {
            newListLength++;
        }

        newIdList = new int[newListLength][2];

        System.arraycopy(idList, 0, newIdList, 0, idList.length);

        idList = newIdList;
    }

    /**
     * If the list is unsorted, this method will re-order the contents.
     */
    private void ensureListIsSorted() {
        if( !sorted ) {
            List<IntInt> tmpList;
            int newIdOffset;

            tmpList = new ArrayList<IntInt>(idOffset);

            for( int i = 0; i < idOffset; i++ ) {
                tmpList.add(new IntInt(idList[i]));
            }

            Collections.sort(tmpList);

            newIdOffset = 0;
            for( int i = 0; i < idOffset; i++ ) {
                int[] nextValue;

                nextValue = tmpList.get(i).getPair();

                if( newIdOffset <= 0 || nextValue[0] > idList[newIdOffset - 1][0] ) {
                    idList[newIdOffset++] = nextValue;
                }
            }
            idOffset = newIdOffset;

            sorted = true;
        }
    }

    /**
     * Stores key-value pair for sorting.
     */
    private static class IntInt implements Comparable {
        private int[] pair;

        public IntInt( int[] values ) {
            this.pair = values;
        }

        public int[] getPair() {
            return pair;
        }

        public int compareTo( Object o ) {
            int okey = ((IntInt)o).pair[0];
            return pair[0] < okey ? -1 : (pair[0] == okey ? 0 : 1);
        }

        @Override
        public boolean equals( Object obj ) {
            return obj instanceof IntInt && ((IntInt)obj).pair[0] == pair[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setMapping( long id, long ref ) {
        int integerId;

        integerId = LongAsInt.longToInt(id);

        // Increase the id list size if it is full.
        if( idOffset >= idList.length ) {
            extendIdList();
        }

        idList[idOffset][1] = LongAsInt.longToInt(ref);
        idList[idOffset++][0] = integerId;

        // If ids are added out of order, the list will have to be sorted before
        // it can be searched using a binary search algorithm.
        if( integerId < maxIdAdded ) {
            sorted = false;
        } else {
            maxIdAdded = integerId;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Integer getMapping( long id ) {
        int integerId;
        int intervalBegin;
        int intervalEnd;
        boolean idFound;

        integerId = LongAsInt.longToInt(id);

        // If the list is not sorted, it must be sorted prior to a search being
        // performed.
        ensureListIsSorted();

        // Perform a binary search splitting the list in half each time until
        // the requested id is confirmed as existing or not.
        intervalBegin = 0;
        intervalEnd = idOffset;
        idFound = false;
        int foundValue = 0;
        for( boolean searchComplete = false; !searchComplete; ) {
            int intervalSize;

            // Calculate the interval size.
            intervalSize = intervalEnd - intervalBegin;

            // Divide and conquer if the size is large, otherwise commence
            // linear search.
            if( intervalSize >= 2 ) {
                int intervalMid;
                int currentId;

                // Split the interval in two.
                intervalMid = intervalSize / 2 + intervalBegin;

                // Check whether the midpoint id is above or below the id
                // required.
                currentId = idList[intervalMid][0];
                if( currentId == integerId ) {
                    idFound = true;
                    foundValue = idList[intervalMid][1];
                    searchComplete = true;
                } else if( currentId < integerId ) {
                    intervalBegin = intervalMid + 1;
                } else {
                    intervalEnd = intervalMid;
                }

            } else {
                // Iterate through the entire interval.
                for( int currentOffset = intervalBegin; currentOffset < intervalEnd; currentOffset++ ) {
                    int currentId;

                    // Check if the current offset contains the id required.
                    currentId = idList[currentOffset][0];

                    if( currentId == integerId ) {
                        idFound = true;
                        foundValue = idList[currentOffset][1];
                        break;
                    }
                }

                searchComplete = true;
            }
        }

        return idFound ? Integer.valueOf(foundValue) : null;
    }
}
