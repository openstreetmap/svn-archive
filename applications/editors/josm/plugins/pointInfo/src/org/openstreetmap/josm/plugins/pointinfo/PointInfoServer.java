// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class PointInfoServer {

    public PointInfoServer() {

    }

    /**
     * Call Info server.
     * @param urlString Input parameters.
     * @return Result text.
     */
    public String callServer(String urlString) {
        try {
            URL url = new URL(urlString);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() == 0)
                    sb.append(line);
                else
                    sb.append(" "+line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
