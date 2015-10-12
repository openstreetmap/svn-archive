package org.openstreetmap.josm.plugins.tofix.bean.items;

import java.util.Arrays;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;

/**
 *
 * @author ruben
 */
public class ItemKeeprightBean extends ItemTask {

    String object_type;
    Long object_id;
    String st_astext;

    public String getObject_type() {
        return object_type;
    }

    public void setObject_type(String object_type) {
        this.object_type = object_type;
    }

    public Long getObject_id() {
        return object_id;
    }

    public void setObject_id(Long object_id) {
        this.object_id = object_id;
    }

    public String getSt_astext() {
        return st_astext;
    }

    public void setSt_astext(String st_astext) {
        this.st_astext = st_astext;
    }

    public Node get_node() {
        String str = getSt_astext().replaceAll("[^-?0-9.]+", " ");
        Double lat = Double.parseDouble(Arrays.asList(str.trim().split(" ")).get(1));
        Double lon = Double.parseDouble(Arrays.asList(str.trim().split(" ")).get(0));
        LatLon coor = new LatLon(lat, lon);
        Node node = new Node(coor);
        return node;
    }

}
