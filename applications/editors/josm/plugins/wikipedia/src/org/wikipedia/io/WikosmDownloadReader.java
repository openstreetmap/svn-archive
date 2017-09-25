// License: GPL. For details, see LICENSE file.
package org.wikipedia.io;

import org.openstreetmap.josm.actions.DownloadPrimitiveAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.DataSource;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.preferences.ListProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.*;
import org.openstreetmap.josm.io.NameFinder.SearchResult;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Utils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Read content from an Wikosm server.
 */
public class WikosmDownloadReader extends BoundingBoxDownloader {

    /**
     * Property for current Wikosm server.
     */
    public static final StringProperty WIKOSM_SERVER = new StringProperty("download.wikosm.server",
            "http://88.99.164.208/bigdata/namespace/wdq/sparql");
    /**
     * Property for list of known Wikosm servers.
     */
// TODO: Core dependency:
//    public static final ListProperty WIKOSM_SERVER_HISTORY = new ListProperty("download.wikosm.servers",
//            Arrays.asList("http://88.99.164.208/bigdata/namespace/wdq/sparql"));
    public static final ListProperty WIKOSM_SERVER_HISTORY = new ListProperty("download.wikosm.servers",
            Arrays.asList("http://88.99.164.208/bigdata/namespace/wdq/sparql"));

    private static final String DATA_PREFIX = "?query=";

    private final String wikosmServer;
    private final String wikosmQuery;
    private final boolean asNewLayer;
    private final boolean downloadReferrers;
    private final boolean downloadFull;

    /**
     * Constructs a new {@code WikosmDownloadReader}.
     *
     * @param downloadArea The area to download
     * @param wikosmServer The Wikosm server to use
     * @param wikosmQuery  The Wikosm query
     */
    public WikosmDownloadReader(Bounds downloadArea, String wikosmServer, String wikosmQuery,
                                boolean asNewLayer, boolean downloadReferrers, boolean downloadFull) {
        super(downloadArea);
        setDoAuthenticate(false);
        this.wikosmServer = wikosmServer;
        this.wikosmQuery = wikosmQuery.trim();
        this.asNewLayer = asNewLayer;
        this.downloadReferrers = downloadReferrers;
        this.downloadFull = downloadFull;
    }

    @Override
    protected String getBaseUrl() {
        return wikosmServer;
    }

    @Override
    protected String getRequestForBbox(double lon1, double lat1, double lon2, double lat2) {
        final String query = this.wikosmQuery
                .replace("{{boxParams}}", boxParams(lon1, lat1, lon2, lat2))
                .replace("{{center}}", center(lon1, lat1, lon2, lat2));
        return DATA_PREFIX + Utils.encodeUrl(query);
    }

    public static String boxParams(double lon1, double lat1, double lon2, double lat2) {
        return "\nbd:serviceParam wikibase:cornerWest " + point(lon1, lat1) + ".\n" +
                "bd:serviceParam wikibase:cornerEast " + point(lon2, lat2) + ".\n";
    }

    public static String center(double lon1, double lat1, double lon2, double lat2) {
        LatLon c = new BBox(lon1, lat1, lon2, lat2).getCenter();
        return point(c.lon(), c.lat());
    }

    public static String point(double lon, double lat) {
        return "\"Point(" + lon + " " + lat + ")\"^^geo:wktLiteral";
    }

    static String date(String humanDuration, LocalDateTime from) {
        // Convert to ISO 8601. Replace months by X temporarily to avoid conflict with minutes
        String duration = humanDuration.toLowerCase(Locale.ENGLISH).replace(" ", "")
                .replaceAll("years?", "Y").replaceAll("months?", "X").replaceAll("weeks?", "W")
                .replaceAll("days?", "D").replaceAll("hours?", "H").replaceAll("minutes?", "M").replaceAll("seconds?", "S");
        Matcher matcher = Pattern.compile(
                "((?:[0-9]+Y)?(?:[0-9]+X)?(?:[0-9]+W)?)"+
                "((?:[0-9]+D)?)" +
                "((?:[0-9]+H)?(?:[0-9]+M)?(?:[0-9]+(?:[.,][0-9]{0,9})?S)?)?").matcher(duration);
        boolean javaPer = false;
        boolean javaDur = false;
        if (matcher.matches()) {
            javaPer = matcher.group(1) != null && !matcher.group(1).isEmpty();
            javaDur = matcher.group(3) != null && !matcher.group(3).isEmpty();
            duration = 'P' + matcher.group(1).replace('X', 'M') + matcher.group(2);
            if (javaDur) {
                duration += 'T' + matcher.group(3);
            }
        }

        // Duration is now a full ISO 8601 duration string. Unfortunately Java does not allow to parse it entirely.
        // We must split the "period" (years, months, weeks, days) from the "duration" (days, hours, minutes, seconds).
        Period p = null;
        Duration d = null;
        int idx = duration.indexOf('T');
        if (javaPer) {
            p = Period.parse(javaDur ? duration.substring(0, idx) : duration);
        }
        if (javaDur) {
            d = Duration.parse(javaPer ? 'P' + duration.substring(idx, duration.length()) : duration);
        } else if (!javaPer) {
            d = Duration.parse(duration);
        }

        // Now that period and duration are known, compute the correct date/time
        LocalDateTime dt = from;
        if (p != null) {
            dt = dt.minus(p);
        }
        if (d != null) {
            dt = dt.minus(d);
        }

        // Returns the date/time formatted in ISO 8601
        return dt.toInstant(ZoneOffset.UTC).toString();
    }

    private static SearchResult searchName(String area) throws IOException {
        return NameFinder.queryNominatim(area).stream().filter(
                x -> !OsmPrimitiveType.NODE.equals(x.getOsmId().getType())).iterator().next();
    }

    static String geocodeArea(String area) throws IOException {
        // Offsets defined in https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL#By_element_id
        final EnumMap<OsmPrimitiveType, Long> idOffset = new EnumMap<>(OsmPrimitiveType.class);
        idOffset.put(OsmPrimitiveType.NODE, 0L);
        idOffset.put(OsmPrimitiveType.WAY, 2_400_000_000L);
        idOffset.put(OsmPrimitiveType.RELATION, 3_600_000_000L);
        final PrimitiveId osmId = searchName(area).getOsmId();
        return String.format("area(%d)", osmId.getUniqueId() + idOffset.get(osmId.getType()));
    }

    static String geocodeBbox(String area) throws IOException {
        Bounds bounds = searchName(area).getBounds();
        return bounds.getMinLat() + "," + bounds.getMinLon() + "," + bounds.getMaxLat() + "," + bounds.getMaxLon();
    }

    static String geocodeCoords(String area) throws IOException {
        SearchResult result = searchName(area);
        return result.getLat() + "," + result.getLon();
    }

    static String geocodeId(String area) throws IOException {
        PrimitiveId osmId = searchName(area).getOsmId();
        return String.format("%s(%d)", osmId.getType().getAPIName(), osmId.getUniqueId());
    }

    @Override
    protected InputStream getInputStreamRaw(String urlStr, ProgressMonitor progressMonitor, String reason,
                                            boolean uncompressAccordingToContentDisposition) throws OsmTransferException {
        try {
            return super.getInputStreamRaw(urlStr, progressMonitor, reason, uncompressAccordingToContentDisposition);
        } catch (OsmApiException ex) {
//            final String errorIndicator = "Error</strong>: ";
//            if (ex.getMessage() != null && ex.getMessage().contains(errorIndicator)) {
//                final String errorPlusRest = ex.getMessage().split(errorIndicator)[1];
//                if (errorPlusRest != null) {
//                    ex.setErrorHeader(errorPlusRest.split("</")[0].replaceAll(".*::request_read_and_idx::", ""));
//                }
//            }
            throw ex;
        }
    }

    @Override
    protected void adaptRequest(HttpClient request) {
        // see https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL#timeout
        final Matcher timeoutMatcher = Pattern.compile("#timeout:(\\d+)").matcher(wikosmQuery);
        final int timeout;
        if (timeoutMatcher.find()) {
            timeout = (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(timeoutMatcher.group(1)));
        } else {
            timeout = (int) TimeUnit.MINUTES.toMillis(3);
        }
        request.setConnectTimeout(timeout);
        request.setReadTimeout(timeout);
        request.setAccept("application/sparql-results+json");
    }

    @Override
    protected String getTaskName() {
        return tr("Contacting Server...");
    }

    @Override
    protected DataSet parseDataSet(InputStream source, ProgressMonitor progressMonitor) throws IllegalDataException {
        try {
            List<PrimitiveId> ids = getPrimitiveIds(source);

            // REVIEW: this seems like a bad way to initiate download from parsing, and makes it hard to test
            DownloadPrimitiveAction.processItems(asNewLayer, ids, downloadReferrers, downloadFull);
        } catch (IOException e) {
            Logging.error(e);
        }

        return null;
    }

    static List<PrimitiveId> getPrimitiveIds(InputStream source) throws UnsupportedEncodingException {
        Pattern uriPattern = Pattern.compile("^https://www\\.openstreetmap\\.org/(node|way|relation)/(\\d+)");
        List<PrimitiveId> ids = new ArrayList<>();

        JsonArray results = Json.createReader(new InputStreamReader(source, "UTF-8"))
                .readObject()
                .getJsonObject("results")
                .getJsonArray("bindings");

        for (JsonObject row : results.getValuesAs(JsonObject.class)) {
            for (JsonValue column : row.values()) {
                JsonObject columnObj = (JsonObject) column;
                if (columnObj.getString("type").equals("uri"))  {
                    Matcher matcher = uriPattern.matcher(columnObj.getString("value"));
                    if (matcher.matches()) {
                        ids.add(new SimplePrimitiveId(Long.parseLong(matcher.group(2)),
                                OsmPrimitiveType.from(matcher.group(1))));
                    }
                }
            }
        }

        return ids;
    }

    @Override
    public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {

        DataSet ds = super.parseOsm(progressMonitor);

        // add bounds if necessary (note that Wikosm API does not return bounds in the response XML)
        if (ds != null && ds.getDataSources().isEmpty() && wikosmQuery.contains("{{boxParams}}")) {
            if (crosses180th) {
                Bounds bounds = new Bounds(lat1, lon1, lat2, 180.0);
                DataSource src = new DataSource(bounds, getBaseUrl());
                ds.addDataSource(src);

                bounds = new Bounds(lat1, -180.0, lat2, lon2);
                src = new DataSource(bounds, getBaseUrl());
                ds.addDataSource(src);
            } else {
                Bounds bounds = new Bounds(lat1, lon1, lat2, lon2);
                DataSource src = new DataSource(bounds, getBaseUrl());
                ds.addDataSource(src);
            }
        }

        return ds;
    }
}
