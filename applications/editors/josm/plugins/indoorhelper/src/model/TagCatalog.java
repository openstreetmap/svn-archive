// License: GPL. For details, see LICENSE file.
package model;

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
            tagList.add(new Tag("indoor", "wall"));
            tagList.add(new Tag("material", "concrete"));
            return tagList;
        case DOOR_PRIVATE:
            tagList.add(new Tag("door", "yes"));
            tagList.add(new Tag("access", "private"));
            return tagList;
        case DOOR_PUBLIC:
            tagList.add(new Tag("door", "yes"));
            tagList.add(new Tag("access", "yes"));
            return tagList;
        case ELEVATOR:
            tagList.add(new Tag("highway", "elevator"));
            return tagList;
        case ENTRANCE:
            tagList.add(new Tag("entrance", "yes"));
            return tagList;
        case ENTRANCE_EXIT_ONLY:
            tagList.add(new Tag("entrance", "exit"));
            return tagList;
        case ACCESS_PRIVATE:
            tagList.add(new Tag("access", "private"));
            return tagList;
        case ACCESS_PUBLIC:
            tagList.add(new Tag("access", "yes"));
            return tagList;
        case TOILET_FEMALE:
            tagList.add(new Tag("indoor", "room"));
            tagList.add(new Tag("amenity", "toilets"));
            tagList.add(new Tag("female", "yes"));
            return tagList;
        case GLASS_WALL:
            tagList.add(new Tag("indoor", "wall"));
            tagList.add(new Tag("material", "glass"));
            return tagList;
        case TOILET_MALE:
            tagList.add(new Tag("indoor", "room"));
            tagList.add(new Tag("amenity", "toilets"));
            tagList.add(new Tag("male", "yes"));
            return tagList;
        case ROOM:
            tagList.add(new Tag("indoor", "room"));
            return tagList;
        case STEPS:
            tagList.add(new Tag("highway", "steps"));
            return tagList;
        case CORRIDOR:
            tagList.add(new Tag("indoor", "corridor"));
            return tagList;
        case BENCH:
            tagList.add(new Tag("amenity", "bench"));
            return tagList;
        case AREA:
            tagList.add(new Tag("indoor", "area"));
            return tagList;
        case NONE :
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
     */
    public enum IndoorObject {
        CONCRETE_WALL, GLASS_WALL, ROOM, TOILET_MALE, TOILET_FEMALE, ELEVATOR, DOOR_PRIVATE, DOOR_PUBLIC, ENTRANCE,
        ENTRANCE_EXIT_ONLY, ACCESS_PRIVATE, ACCESS_PUBLIC, STEPS, CORRIDOR, BENCH, AREA, NONE;
    }
}
