// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

/**
 * List of possible grab factors each time we call the grab action.
 * X1 means that only one bounding box is grabbed where X2 means that the current
 * view is split in 2x2 bounding boxes and X3 is 3x3 boxes.
 * SQUARE_100M is a special value where bounding boxes have a fixed size of 100x100 meters
 * and east,north are rounded to the lowest 100 meter as well, thus none of the bounding boxes
 * are overlapping each others.
 */
public enum Scale {
    X1("1"),
    X2("2"),
    X3("3"),
    SQUARE_100M("4");

    /**
     * value is the string equivalent stored in the preferences file
     */
    public final String value;

    Scale(String value) {
        this.value = value;
    }
    public String toString() {
        return value;
    }
}
