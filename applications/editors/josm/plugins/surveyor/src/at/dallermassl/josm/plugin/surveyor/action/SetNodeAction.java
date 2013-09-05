/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.Pair;

import at.dallermassl.josm.plugin.surveyor.GpsActionEvent;
import at.dallermassl.josm.plugin.surveyor.SurveyorAction;
import at.dallermassl.josm.plugin.surveyor.SurveyorLock;

/**
 * Action that sets a node into the data layer. The first parameter is used as a property key
 * the second as the property value (e.g. amenity=parking). If there are more than two parameters
 * they are used as key/value pairs.
 * @author cdaller
 *
 */
public class SetNodeAction implements SurveyorAction {
    private Collection<Pair<String, String>> keyValues;

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
        keyValues = new ArrayList<Pair<String, String>>();
        int pos;
        String key;
        String value;
        for (String keyValuePair : parameters) {
            pos = keyValuePair.indexOf('=');
            if(pos > 0) {
                key = keyValuePair.substring(0, pos);
                value = keyValuePair.substring(pos + 1);
                keyValues.add(new Pair<String, String>(key, value));
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
        //System.out.println(getClass().getSimpleName() + " KOORD: " + coordinates.lat() + ", " + coordinates.lon() + " params: " + keyValues);
        Node node = new Node(coordinates);
        for(Pair<String, String> entry : keyValues) {
            node.put(entry.a, entry.b);
        }
        synchronized(SurveyorLock.class) {
            DataSet ds = Main.main.getCurrentDataSet();
            if(ds != null)
            {
                ds.addPrimitive(node);
                ds.setSelected(node);
            }
        }
        Main.map.repaint();
    }
}
