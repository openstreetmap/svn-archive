/**
 *  Tracer2 - plug-in for JOSM to capture contours
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openstreetmap.josm.plugins.tracer2.server;

import java.util.ArrayList;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.tracer2.preferences.ServerParam;

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
    public void run() {
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
            
            if (!strResponse.startsWith("(") || !strResponse.endsWith(")")){
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
        	m_listLatLon = new ArrayList<>();
        }
    }
    
}
