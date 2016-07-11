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
import java.util.List;

import org.openstreetmap.josm.data.osm.Tag;

/**
 * Class to provide the indoor tagging catalog.
 * 
 * @author egru
 *
 */

public final class TagCatalog {
    
    /**
     * Function to get a specific tag-set out of the {@link TagCatalog}. 
     * 
     * @param o the object for which you want the tag-set
     * @return a list of tags for the specified object
     */    
    public List<Tag> getTags(IndoorObject o) {
        
        List<Tag> tagList = new ArrayList<Tag>();
        
        switch(o) {
        case CONCRETE_WALL:
            tagList.add(new Tag("indoor:area", "wall"));
            tagList.add(new Tag("indoor:wall:material", "concrete"));
            return tagList;
        case DOOR:
            tagList.add(new Tag("indoor:door", "yes"));
            return tagList;
        case ELEVATOR:
            tagList.add(new Tag("indoor:area", "elevator"));
            return tagList;
        case ENTRANCE:
            tagList.add(new Tag("indoor:entrance", "yes"));
            return tagList;
        case TOILET_FEMALE:
            tagList.add(new Tag("indoor:area", "room"));
            tagList.add(new Tag("amenity", "toilets"));
            tagList.add(new Tag("female", "yes"));
            return tagList;
        case GLASS_WALL:
            tagList.add(new Tag("indoor:area", "wall"));
            tagList.add(new Tag("indoor:wall:material", "glass"));
            return tagList;
        case TOILET_MALE:
            tagList.add(new Tag("indoor:area", "room"));
            tagList.add(new Tag("amenity", "toilets"));
            tagList.add(new Tag("male", "yes"));
            return tagList;
        case ROOM:
            tagList.add(new Tag("indoor:area", "room"));
            return tagList;
        case SHELL:
            tagList.add(new Tag("indoor:area", "shell"));
            return tagList;
        case STAIRWAYS:
            tagList.add(new Tag("indoor:area", "stairways"));
            return tagList;
        case STEPS:
            tagList.add(new Tag("indoor:highway", "steps"));
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
     *
     */
    public enum IndoorObject {
        SHELL, CONCRETE_WALL, GLASS_WALL, ROOM, TOILET_MALE, TOILET_FEMALE, ELEVATOR, STAIRWAYS, STEPS, DOOR, ENTRANCE;
    }

}
