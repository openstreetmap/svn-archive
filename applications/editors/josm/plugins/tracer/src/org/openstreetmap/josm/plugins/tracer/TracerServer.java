/**
 * Tracer - plugin for JOSM
 * Jan Bilak
 * This program is free software and licensed under GPL.
 */

package org.openstreetmap.josm.plugins.tracer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import org.openstreetmap.josm.data.coor.LatLon;

public class TracerServer {

    static final String URL = "http://localhost:5050/";
    
    public TracerServer() {

    }
   
    /**
     * Call Trace server.
     * @param urlString Input parameters.
     * @return Result text.
     */
    private String callServer(String urlString) {
        try {
            URL url = new URL(URL + urlString);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Trace building on position.
     * @param pos Position of building.
     * @return Building border.
     */
    public ArrayList<double[]> trace(LatLon pos) {
        try {
            String content = callServer("trace/simple/" + pos.lat() + ";" + pos.lon());
            ArrayList<double[]> nodelist = new ArrayList<double[]>();
            String[] lines = content.split("\\|");
            for (String line : lines) {
                String[] items = line.split(";");
                double x = Double.parseDouble(items[0]);
                double y = Double.parseDouble(items[1]);
                double[] d = new double[2];
                d[0] = x;
                d[1] = y;
                nodelist.add(d);
            }
            return nodelist;
        } catch (Exception e) {
            ArrayList<double[]> nodelist = new ArrayList<double[]>();
            return nodelist;
        }
    }

    /**
     * Log message to server.
     * @param message Message to log.
     */
    public void log(String message) {
        callServer("log/" + message);
    }

}
