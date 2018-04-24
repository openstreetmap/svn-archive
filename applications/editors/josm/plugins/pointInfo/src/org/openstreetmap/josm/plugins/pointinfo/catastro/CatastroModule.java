// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo.catastro;

import java.net.URL;
import java.util.Locale;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

import org.openstreetmap.josm.plugins.pointinfo.AbstractPointInfoModule;

/**
 * A module for the Spanish Cadastre Web Services
 * @author Javier Sánchez Portero
 */
public class CatastroModule extends AbstractPointInfoModule {

    private static final String moduleName = "Catastro";
    private static final String areaName = "es";
    private static final String catURL = "http://ovc.catastro.meh.es/ovcservweb/OVCSWLocalizacionRC/OVCCoordenadas.asmx/Consulta_RCCOOR?SRS=EPSG:4326&Coordenada_X=%f&Coordenada_Y=%f";

    private CatastroRecord m_record = new CatastroRecord();

    public CatastroModule() {

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
     * Get a information about given position from Consulta_RCCOOR Web Service.
     * @param pos Position on the map
     */
    @Override
    public void prepareData(LatLon pos) {
        try {
            String request = String.format(Locale.ENGLISH, catURL, pos.lon(), pos.lat());
            String result = HttpClient.create(new URL(request)).connect().fetchContent();
            m_record.parseXML(result);
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
