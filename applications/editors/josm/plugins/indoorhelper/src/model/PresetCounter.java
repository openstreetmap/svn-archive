/*
 * Indoorhelper is a JOSM plug-in to support users when creating their own indoor maps.
 *  Copyright (C) 2016  Erik Gruschka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    private List<ObjectCounter> counterList;

    /**
     * Initiates the counterList with the available IndoorObjects.
     */

    public PresetCounter() {
        this.init();
    }

    private void init() {
        counterList = new ArrayList<>();

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
        counterList.add(new ObjectCounter(IndoorObject.ZONE, 0));
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



/**
*
*
*
*
*
*
*
*
*
*/
}
