// License: GPL. For details, see LICENSE file.
package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import model.TagCatalog.IndoorObject;

/**
 * Counter for the calls of specific indoor objects, to track which items were used most frequently.
 *
 * @author egru
 * @author rebsc
 */
public class PresetCounter {

    private List<IndoorObject> rankingList;
    private final List<ObjectCounter> counterList = new ArrayList<>();

    /**
     * Initiates the counterList with the available IndoorObjects.
     */

    public PresetCounter() {
        counterList.add(new ObjectCounter(IndoorObject.CONCRETE_WALL, 0));
        counterList.add(new ObjectCounter(IndoorObject.DOOR_PRIVATE, 0));
        counterList.add(new ObjectCounter(IndoorObject.DOOR_PUBLIC, 0));
        counterList.add(new ObjectCounter(IndoorObject.ELEVATOR, 0));
        counterList.add(new ObjectCounter(IndoorObject.ENTRANCE, 0));
        counterList.add(new ObjectCounter(IndoorObject.ENTRANCE_EXIT_ONLY, 0));
        counterList.add(new ObjectCounter(IndoorObject.ACCESS_PRIVATE, 0));
        counterList.add(new ObjectCounter(IndoorObject.ACCESS_PUBLIC, 0));
        counterList.add(new ObjectCounter(IndoorObject.GLASS_WALL, 0));
        counterList.add(new ObjectCounter(IndoorObject.ROOM, 0));
        counterList.add(new ObjectCounter(IndoorObject.STEPS, 0));
        counterList.add(new ObjectCounter(IndoorObject.CORRIDOR, 0));
        counterList.add(new ObjectCounter(IndoorObject.TOILET_FEMALE, 0));
        counterList.add(new ObjectCounter(IndoorObject.TOILET_MALE, 0));
        counterList.add(new ObjectCounter(IndoorObject.AREA, 0));
        counterList.add(new ObjectCounter(IndoorObject.BENCH, 0));
    }

    /**
     * Increments the counter of a specific IndoorObject in the list.
     * @param object the IndoorObject, which counter should be incremented
     */
    public void count(IndoorObject object) {
        ListIterator<ObjectCounter> iterator = this.counterList.listIterator();

        // Go through the list and increment the corresponding objects counter value.
        while (iterator.hasNext()) {
            ObjectCounter counterTemp = iterator.next();
            if (counterTemp.getObject().equals(object)) {
                    counterList.get(iterator.nextIndex()-1).increment();
            }
        }

        //Sort the list.
        this.sort();
    }

    private void sort() {
        Collections.sort(counterList);
        Collections.reverse(counterList);
    }

    public List<IndoorObject> getRanking() {
        rankingList = new ArrayList<>();

        rankingList.add(counterList.get(0).getObject());
        rankingList.add(counterList.get(1).getObject());
        rankingList.add(counterList.get(2).getObject());
        rankingList.add(counterList.get(3).getObject());

        return rankingList;
    }

    private static class ObjectCounter implements Comparable<ObjectCounter> {
        private IndoorObject object;
        private int count;

        ObjectCounter(IndoorObject o, int c) {
            this.object = o;
            this.count = c;
        }

        public int getCount() {
            return this.count;
        }

        public IndoorObject getObject() {
            return this.object;
        }

        public void increment() {
            this.count += 1;
        }

        @Override
        public int compareTo(ObjectCounter o) {
            if (this.getCount() < o.getCount()) {
                return -1;
            }
            if (this.getCount() == o.getCount()) {
                return 0;
            }
            if (this.getCount() > o.getCount()) {
                return 1;
            }

            return 0;
        }
    }
}
