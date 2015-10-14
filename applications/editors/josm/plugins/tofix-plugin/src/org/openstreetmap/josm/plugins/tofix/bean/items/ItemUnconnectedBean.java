package org.openstreetmap.josm.plugins.tofix.bean.items;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;

/**
 *
 * @author ruben
 */
public class ItemUnconnectedBean extends ItemTask {
    Long way_id;
    Long node_id;
    String st_astext;
    
    public Long getWay_id() {
        return way_id;
    }

    public void setWay_id(Long way_id) {
        this.way_id = way_id;
    }

    public Long getNode_id() {
        return node_id;
    }

    public void setNode_id(Long node_id) {
        this.node_id = node_id;
    }

    public String getSt_astext() {
        return st_astext;
    }

    public void setSt_astext(String st_astext) {
        this.st_astext = st_astext;
    }

    public Node get_node() {
        String geoString = getSt_astext();
        geoString = geoString.replace("POINT(", "").replace(")", "");
        String[] array = geoString.split(" ");
        LatLon latLon = new LatLon(Double.parseDouble(array[1]), Double.parseDouble(array[0]));
        Node node = new Node(latLon);
        return node;
    }

}
