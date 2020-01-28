// License: GPL. For details, see LICENSE file.
package model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.data.osm.Tag;

/**
 *
 * The class to save a level of the building.
 *
 * @author egru
 *
 */

public class IndoorLevel {

    private static final Pattern RANGE = Pattern.compile("(-?[0-9]+)-(-?[0-9]+)");

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
            Matcher m = RANGE.matcher(val);

            //Extract values
            if (m.matches()) {
                firstVal = Integer.parseInt(m.group(1));
                secVal = Integer.parseInt(m.group(2));
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
