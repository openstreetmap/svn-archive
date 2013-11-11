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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;

public class Request extends Thread {
	
    static final String URL = "http://localhost:49243/";
    
    public Request() {
    }
    
    /**
     * Send request to the server.
     * @param strUrl request.
     * @return Result text.
     */
    protected String callServer(String strUrl) {
        try {
            URL oUrl = new URL(URL + strUrl);
            BufferedReader oReader = new BufferedReader(new InputStreamReader(oUrl.openStream()));
            StringBuilder oBuilder = new StringBuilder();
            String strLine;
            while ((strLine = oReader.readLine()) != null) {
                oBuilder.append(strLine);
            }
            return oBuilder.toString();
        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(Main.parent, tr("Tracer2Server isn''t running. Pleas start the Server.\nIf you don''t have the Server please download it at"
            		+ "\nhttp://sourceforge.net/projects/tracer2server/.") , tr("Error"),  JOptionPane.ERROR_MESSAGE);
            return "";
        } catch (Exception e) {
            JOptionPane.showMessageDialog(Main.parent, tr("Tracer2Server has nothing found.") + "\n", tr("Error"),  JOptionPane.ERROR_MESSAGE);
    		return "";
    	}
    }
    
    /**
     * Checks errors in response from the server.
     * @param strResponse response from the server.
     * @return Result text.
     */
    protected boolean checkError(String strResponse) {
        String strIdentifier = "&traceError=";
        if (strResponse.contains(strIdentifier)) {
        	String strError = strResponse.replaceFirst(strIdentifier, "").trim();
            JOptionPane.showMessageDialog(Main.parent, tr("Tracer2Server has an Error detected.") + "\n" + strError, tr("Error"),  JOptionPane.ERROR_MESSAGE);
        	return true;
        }
        return false;
    }
    
    public void run() {
    }
    
}
