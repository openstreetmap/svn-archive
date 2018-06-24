// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.api;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.JosmRuntimeException;

/**
 * Interface to Cadastre GeoAPI.
 */
public final class CadastreAPI {

    private static final String API_ENDPOINT = "https://sandbox.geo.api.gouv.fr/cadastre/";

    private CadastreAPI() {
        // Hide public sontructor for utility classes
    }

    /**
     * Returns the set of cadastral sheet ids for the given bounding box.
     * @param bounds bounding box
     * @return the set of cadastral sheet ids for the given bounding box
     * @throws IOException if any I/O error occurs
     */
    public static Set<String> getSheets(Bounds bounds) throws IOException {
        return getSheets(bounds.getMinLon(), bounds.getMinLat(), bounds.getMaxLon(), bounds.getMaxLat());
    }

    /**
     * Returns the set of cadastral sheet ids for the given bounding box.
     * @param minlon minimum longitude
     * @param minlat minimum latitude
     * @param maxlon maximum longitude
     * @param maxlat maximum latitude
     * @return the set of cadastral sheet ids for the given bounding box
     * @throws IOException if any I/O error occurs
     */
    public static Set<String> getSheets(double minlon, double minlat, double maxlon, double maxlat) throws IOException {
        try {
            return Json.createReader(new StringReader(
                    HttpClient.create(new URL(API_ENDPOINT + "/feuilles?bbox=" + String.join(",",
                            Double.toString(minlon), Double.toString(minlat), Double.toString(maxlon), Double.toString(maxlat))))
                    .connect().fetchContent()))
                    .readArray().stream().map(x -> x.asJsonObject().getString("id")).collect(Collectors.toSet());
        } catch (MalformedURLException e) {
            throw new JosmRuntimeException(e);
        }
    }
}
