package org.openstreetmap.josm.plugins.tofix.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.openstreetmap.josm.plugins.tofix.controller.StatusController;

/**
 *
 * @author ruben
 */
public class Status {

    final static String host = Config.HOST + "status";

    public static boolean server() {
        StatusController statusController = new StatusController(host);
        if (statusController.getStatusBean().getStatus().equals("a ok")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isInternetReachable() {
        HttpURLConnection activeConnection = null;
        try {
            URL url = new URL(Config.URL_OSM);
            activeConnection = (HttpURLConnection) url.openConnection();
            activeConnection.connect();
            return true;
        } catch (IOException e) {
            System.err.println("Couldn't connect to the osm server. Please check your internet connection.");
            return false;
        }
    }

}
