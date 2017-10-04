// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tracer2.server;

import java.util.ArrayList;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.tracer2.preferences.ServerParam;
import org.openstreetmap.josm.tools.Logging;

public class GetTrace extends Request {

    private LatLon m_oLatLon;
    private ServerParam m_oServerParam;
    public ArrayList<LatLon> m_listLatLon = new ArrayList<>();

    /**
     * Trace s simple shape on position.
     * @param oLatLon position of starting traceing.
     * @param oParam parameter for tracing.
     */
    public GetTrace(LatLon oLatLon, ServerParam oParam) {
        m_oLatLon = oLatLon;
        m_oServerParam = oParam;
    }

    /**
     * Thread that get a shape from the Server.
     */
    @Override
    public void run() {
        m_listLatLon = new ArrayList<>();

        try {
            String strResponse = callServer("traceOrder=GetTrace"
                    + "&traceLat=" + m_oLatLon.lat()
                    + "&traceLon=" + m_oLatLon.lon()
                    + "&traceName=" + m_oServerParam.getName()
                    + "&traceUrl=" + m_oServerParam.getUrl()
                    + "&traceTileSize=" + m_oServerParam.getTileSize()
                    + "&traceResolution=" + m_oServerParam.getResolution()
                    //+ "&traceSkipBottom=" + param.getSkipBottom()
                    + "&traceMode=" + m_oServerParam.getMode()
                    + "&traceThreshold=" + m_oServerParam.getThreshold()
                    + "&tracePointsPerCircle=" + m_oServerParam.getPointsPerCircle()
                    );

            if (strResponse == null || strResponse.equals("")) {
                return;
            }

            if (checkError(strResponse) == true) {
                return;
            }

            if (strResponse.startsWith("(")) {
                GetPoints(strResponse);
                return;
            }
            String[] astrParts = strResponse.split("&");

            for (String strPart : astrParts) {
                if (strPart.contains("tracePoints=")) {
                    String strPoints = strPart.replace("tracePoints=", "");
                    GetPoints(strPoints);
                    return;
                }
            }
        } catch (Exception e) {
            //m_listLatLon = new ArrayList<>();
            Logging.warn(e);
        }
    }

    /**
     * Get points from string
     */
    public void GetPoints(String strResponse) {
        try {
            if (!strResponse.startsWith("(") || !strResponse.endsWith(")")) {
                return;
            }
            strResponse = strResponse.substring(1, strResponse.length()-1);

            ArrayList<LatLon> nodelist = new ArrayList<>();

            String[] astrPoints = strResponse.split("\\)\\(");
            for (String strPoint : astrPoints) {
                String[] astrParts = strPoint.split(":");
                double x = Double.parseDouble(astrParts[0]);
                double y = Double.parseDouble(astrParts[1]);
                nodelist.add(new LatLon(x, y));
            }
            m_listLatLon = nodelist;
        } catch (Exception e) {
            //m_listLatLon = new ArrayList<>();
            Logging.warn(e);
        }
    }

}
