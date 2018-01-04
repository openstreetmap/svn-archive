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

/**
 * Class to provide the indoor tagging catalog.
 *
 * @author egru
 * @author rebsc
 *
 */

/**
 * Class to provide the indoor tagging catalog.
 *
 * @author egru
 * @author rebsc
 *
 */
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Tag;

public final class TagCatalog {

    /**
     * Function to get a specific tag-set out of the {@link TagCatalog}.
     *
     * @param o the object for which you want the tag-set
     * @return a list of tags for the specified object
     */
    public List<Tag> getTags(IndoorObject o) {

        List<Tag> tagList = new ArrayList<>();

        switch(o) {
        case CONCRETE_WALL:
            tagList.add(new Tag(tr("indoor"), tr("wall")));
            tagList.add(new Tag(tr("material"), tr("concrete")));
            return tagList;
        case DOOR_PRIVATE:
            tagList.add(new Tag(tr("door"), tr("yes")));
            tagList.add(new Tag ("access","private"));
            return tagList;
        case DOOR_PUBLIC:
            tagList.add(new Tag(tr("door"), tr("yes")));
            tagList.add(new Tag (tr("access"),tr("public")));
            return tagList;
        case ELEVATOR:
            tagList.add(new Tag(tr("highway"), tr("elevator")));
            return tagList;
        case ENTRANCE:
            tagList.add(new Tag(tr("entrance"), tr("yes")));
            return tagList;
        case ENTRANCE_EXIT_ONLY:
            tagList.add(new Tag(tr("entrance"), tr("exit")));
            return tagList;
        case ACCESS_PRIVATE:
        	tagList.add(new Tag(tr("access"),tr("private")));
        	return tagList;
        case ACCESS_PUBLIC:
        	tagList.add(new Tag(tr("access"),tr("public")));
        	return tagList;
        case TOILET_FEMALE:
            tagList.add(new Tag(tr("indoor"), tr("room")));
            tagList.add(new Tag(tr("amenity"), tr("toilets")));
            tagList.add(new Tag(tr("female"), tr("yes")));
            return tagList;
        case GLASS_WALL:
            tagList.add(new Tag(tr("indoor"), tr("wall")));
            tagList.add(new Tag(tr("material"), tr("glass")));
            return tagList;
        case TOILET_MALE:
            tagList.add(new Tag(tr("indoor"), tr("room")));
            tagList.add(new Tag(tr("amenity"), tr("toilets")));
            tagList.add(new Tag(tr("male"), tr("yes")));
            return tagList;
        case ROOM:
            tagList.add(new Tag(tr("indoor"), tr("room")));
            return tagList;
        case STEPS:
            tagList.add(new Tag(tr("highway"), tr("steps")));
            return tagList;
        case CORRIDOR:
        	tagList.add(new Tag(tr("indoor"), tr("corridor")));
        	return tagList;
        case BENCH:
        	tagList.add(new Tag(tr("amenity"),tr("bench")));
        	return tagList;
        case ZONE:
        	tagList.add(new Tag(tr("area"),tr("zone")));
        	return tagList;
        case NONE:
        	return tagList;
        default:
            tagList = null;
            return tagList;
        }
    }

    /**
     * {@link Enum} class for an easier access of elements in the {@link TagCatalog}
     *
     * @author egru
     * @author rebsc
     *
     */
    public enum IndoorObject {
        CONCRETE_WALL, GLASS_WALL, ROOM, TOILET_MALE, TOILET_FEMALE, ELEVATOR, DOOR_PRIVATE, DOOR_PUBLIC, ENTRANCE,
        ENTRANCE_EXIT_ONLY,ACCESS_PRIVATE,ACCESS_PUBLIC,STEPS,CORRIDOR,BENCH,ZONE,NONE;
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
