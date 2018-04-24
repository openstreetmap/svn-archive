// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo.ruian;

import java.net.URL;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

import org.openstreetmap.josm.plugins.pointinfo.AbstractPointInfoModule;

/**
 * A module for the Czech RUIAN database
 * @author Marián Kyral
 */
public class RuianModule extends AbstractPointInfoModule {

    private static final String moduleName = "RUIAN";
    private static final String areaName = "cz";
    private static final String URL = "http://josm.poloha.net/pointInfo/v4/index.php";

    private RuianRecord m_record = new RuianRecord();

    public RuianModule() {

    }

    @Override
    public String getHtml() {
        return m_record.getHtml();
    }

    @Override
    public void performAction(String act) {
        m_record.performAction(act);
    }

    /**
     * Get a information about given position from RUIAN database.
     * @param pos Position on the map
     */
    @Override
    public void prepareData(LatLon pos) {
        try {
            String request = URL + "?lat=" + pos.lat() + "&lon=" + pos.lon();
            m_record.parseJSON(HttpClient.create(new URL(request)).connect().fetchContent());
        } catch (Exception e) {
            Logging.warn(e);
        }
    }

    @Override
    public String getName() {
        return moduleName;   
    }

    @Override
    public String getArea() {
        return areaName;
    }
}
