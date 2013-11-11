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

public class GetVersion extends Request {
	
    public int m_nVersionMajor = -1;
    public int m_nVersionMinor = -1;
    
    /**
     * Get version from server.
     */
	public GetVersion() {
    }
    
	/**
	 * Thread that get the version of the Server.
	 */
    public void run() {
        try {
            String strResponse = callServer("traceOrder=GetVersion" );
            
            if (strResponse == null || strResponse.equals("")) {
            	return;
            }
            
            if (checkError(strResponse) == true) {
            	return;
            }
            
            String[] astrParts = strResponse.split(":");
            if (astrParts.length != 2) {
            	return;
            }
            int nMajor = Integer.parseInt(astrParts[0]);
            int nMinor = Integer.parseInt(astrParts[1]);
            m_nVersionMajor = nMajor;
            m_nVersionMinor = nMinor;
        } catch (Exception e) {
        }
    }
    
}
