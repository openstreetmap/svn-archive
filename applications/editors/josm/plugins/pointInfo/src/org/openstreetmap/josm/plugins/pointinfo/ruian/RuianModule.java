// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo.ruian;

import java.net.URL;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

/**
 * A module for the Czech RUIAN database
 * @author Marián Kyral
 */
public class RuianModule {

    private String URL = "http://josm.poloha.net/pointInfo/v4/index.php";

    private RuianRecord m_record = new RuianRecord();

    public RuianModule() {

    }

    /**
     * Return Html text representation
     * @return String htmlText
     */
    public String getHtml() {
        return m_record.getHtml();
    }

    /**
     * Perform given action
     *  e.g.: copy tags to clipboard
     * @param act Action to be performed
     */
    public void performAction(String act) {
        m_record.performAction(act);
    }

    /**
     * Get a information about given position from RUIAN database.
     * @param pos Position on the map
     */
    public void prepareData(LatLon pos) {
        try {
            String request = URL + "?lat=" + pos.lat() + "&lon=" + pos.lon();
            m_record.parseJSON(HttpClient.create(new URL(request)).connect().fetchContent());
        } catch (Exception e) {
            Logging.warn(e);
        }
    }
}
