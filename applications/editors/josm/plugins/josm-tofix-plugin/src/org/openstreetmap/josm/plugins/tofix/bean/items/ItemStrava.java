package org.openstreetmap.josm.plugins.tofix.bean.items;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;

public class ItemStrava extends ItemTask {

    private String geom;

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public Node get_node() {
        String geoString = getGeom();
        geoString = geoString.replace("POINT(", "").replace(")", "");
        String[] array = geoString.split(" ");
        LatLon latLon = new LatLon(Double.parseDouble(array[1]), Double.parseDouble(array[0]));
        Node node = new Node(latLon);
        return node;
    }

}
