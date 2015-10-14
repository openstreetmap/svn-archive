package org.openstreetmap.josm.plugins.tofix.bean.items;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;

/**
 *
 * @author ruben
 */
public class ItemUsaBuildingsBean extends ItemTask {

    private Double lat;
    private Double lon;
    private String elems;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getElems() {
        return elems;
    }

    public void setElems(String elems) {
        this.elems = elems;
    }

    public Long osm_obj_id() {
        String[] arr = getElems().replace("way", "").split("_");
        return Long.valueOf(arr[0]);
    }

    public Node get_node() {
        Node node = new Node(new LatLon(getLat(), getLon()));
        return node;
    }

}
