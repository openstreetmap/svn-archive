/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import livegps.LiveGpsLock;

import org.dinopolis.util.collection.Tuple;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;

import at.dallermassl.josm.plugin.surveyor.GpsActionEvent;
import at.dallermassl.josm.plugin.surveyor.SurveyorAction;

/**
 * Action that sets a node into the data layer. The first parameter is used as a property key
 * the second as the property value (e.g. amenity=parking). If there are more than two parameters
 * they are used as key/value pairs.
 * @author cdaller
 *
 */
public class SetNodeAction implements SurveyorAction {
    private Collection<Tuple<String, String>> keyValues;

    /**
     * Default Constructor
     */
    public SetNodeAction() {

    }

    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.SurveyorAction#setParameters(java.util.List)
     */
    //@Override
    public void setParameters(List<String> parameters) {
        keyValues = new ArrayList<Tuple<String, String>>();
        int pos;
        String key;
        String value;
        for (String keyValuePair : parameters) {
            pos = keyValuePair.indexOf('=');
            if(pos > 0) {
                key = keyValuePair.substring(0, pos);
                value = keyValuePair.substring(pos + 1);
                keyValues.add(new Tuple<String, String>(key, value));
            } else {
                System.err.println("SetNodeAction: ignoring invalid key value pair: " + keyValuePair);
            }
        }
    }

    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.ButtonAction#actionPerformed(at.dallermassl.josm.plugin.surveyor.GpsActionEvent)
     */
    public void actionPerformed(GpsActionEvent event) {
        LatLon coordinates = event.getCoordinates();
        System.out.println(getClass().getSimpleName() + " KOORD: " + coordinates.lat() + ", " + coordinates.lon() + " params: " + keyValues);
        Node node = new Node(coordinates);
        for(Entry<String, String> entry : keyValues) {
            node.put(entry.getKey(), entry.getValue());
        }
        node.put("created_by", "JOSM-surveyor-plugin");
        synchronized(LiveGpsLock.class) {
            Main.map.mapView.getEditLayer().data.nodes.add(node);
            Main.ds.setSelected(node);
        }
        Main.map.repaint();
    }


}
