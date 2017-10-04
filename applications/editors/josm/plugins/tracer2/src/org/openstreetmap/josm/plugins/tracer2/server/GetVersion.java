// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tracer2.server;

import org.openstreetmap.josm.tools.Logging;

public class GetVersion extends Request {

    public int m_nVersionMajor = -1;
    public int m_nVersionMinor = -1;
    public int m_nVersionBuild = -1;
    public int m_nVersionRevision = -1;

    /**
     * Get version from server.
     */
    public GetVersion() {
    }

    /**
     * Thread that get the version of the Server.
     */
    @Override
    public void run() {
        try {
            String strResponse = callServer("traceOrder=GetVersion");

            if (strResponse == null || strResponse.equals("")) {
                return;
            }

            if (checkError(strResponse) == true) {
                return;
            }

            String[] astrParts = strResponse.split(":");
            if (astrParts.length < 2) {
                return;
            }
            if (astrParts.length > 0) m_nVersionMajor = Integer.parseInt(astrParts[0]);
            if (astrParts.length > 1) m_nVersionMinor = Integer.parseInt(astrParts[1]);
            if (astrParts.length > 2) m_nVersionBuild = Integer.parseInt(astrParts[2]);
            if (astrParts.length > 3) m_nVersionRevision = Integer.parseInt(astrParts[3]);
        } catch (Exception e) {
            Logging.warn(e);
        }
    }

}
