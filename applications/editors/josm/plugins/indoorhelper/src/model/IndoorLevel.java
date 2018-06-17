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

import org.openstreetmap.josm.data.osm.Tag;

/**
 *
 * The class to save a level of the building.
 *
 * @author egru
 *
 */

public class IndoorLevel {

    private Tag levelNumberTag;
    private Tag nameTag;

    /**
     * Constructor which adds the level number.
     *
     * @param levelNumber number of the level
     */
    public IndoorLevel(int levelNumber) {
        this.setLevelNumber(levelNumber);
    }

    /**
     * Constructor which adds level number and name tag.
     *
     * @param levelNumber number of the level
     * @param nameTag optional name tag for the level
     */
    public IndoorLevel(int levelNumber, String nameTag) {
        this.setLevelNumber(levelNumber);
        this.setNameTag(nameTag);
    }

    /**
     * Getter for the level tag
     *
     * @return the complete level number tag
     */
    public Tag getLevelNumberTag() {
        return this.levelNumberTag;
    }

    /**
     * Function to get the level number
     *
     * @return level number as an Integer
     */
    public int getLevelNumber() {
        return Integer.parseInt(this.levelNumberTag.getValue());
    }

    /**
     * Setter for the level number
     *
     * @param levelNumber number of the level
     */
    public void setLevelNumber(int levelNumber) {
        this.levelNumberTag = new Tag("indoor:level", Integer.toString(levelNumber));
    }

    /**
     * Getter for the name tag
     *
     * @return the complete name tag
     */
    public Tag getNameTag() {
        return this.nameTag;
    }

    /**
     * Function to get the optional name of the level.
     *
     * @return String with the optional name.
     */
    public String getName() {
        return this.nameTag.getValue();
    }

    /**
     * Setter for the name tag
     *
     * @param nameTag String which optionally describes the level
     */
    public void setNameTag(String nameTag) {
        this.nameTag = new Tag("indoor:level:name", nameTag);
    }

    public boolean hasEmptyName() {
        if (this.nameTag == null) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPartOfWorkingLevel(String vals, int level) {
        for (String val : vals.split(";")) {
            int firstVal, secVal;

            //Extract values
            if (val.indexOf("-") == 0) {
                firstVal = (Integer.parseInt(val.split("-", 2)[1].split("-", 2)[0]))*-1;
                secVal = Integer.parseInt(val.split("-", 2)[1].split("-", 2)[1]);
            } else if (val.contains("-")) {
                firstVal = Integer.parseInt(val.split("-")[0]);
                secVal = Integer.parseInt(val.split("-")[1]);
            } else {
                firstVal = Integer.parseInt(val);
                secVal = firstVal;
            }

            // Compare values to current working level
            if (level >= firstVal && level <= secVal) {
                return true;
            }
        }

        return false;
    }
}
