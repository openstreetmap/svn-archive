// License: WTFPL. For details, see LICENSE file.
package geochat;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.HttpClient.Response;
import org.openstreetmap.josm.tools.Logging;

/**
 * A static class to query the server and return parsed JSON hash.
 *
 * @author zverik
 */
public final class JsonQueryUtil implements Runnable {

    /**
     * Query the server synchronously.
     * @param query Query string, starting with action. Example: <tt>get&amp;lat=1.0&amp;lon=-2.0&amp;uid=12345</tt>
     * @return Parsed JsonObject if the query was successful, <tt>null</tt> otherwise.
     * @throws IOException There was a problem connecting to the server or parsing JSON.
     */
    public static JsonObject query(String query) throws IOException {
        return query(query, false);
    }

    /**
     * Query the server synchronously.
     * @param query Query string, starting with action. Example: <tt>get&amp;lat=1.0&amp;lon=-2.0&amp;uid=12345</tt>
     * @param logAtDebug {@code true} to set http client connection log at DEBUG level instead of default INFO level
     * @return Parsed JsonObject if the query was successful, <tt>null</tt> otherwise.
     * @throws IOException There was a problem connecting to the server or parsing JSON.
     */
    public static JsonObject query(String query, boolean logAtDebug) throws IOException {
        String serverURL = Config.getPref().get("geochat.server", "https://zverik.dev.openstreetmap.org/osmochat.php?action=");
        URL url = new URL(serverURL + query);
        Response connection = HttpClient.create(url).setLogAtDebug(logAtDebug).connect();
        if (connection.getResponseCode() != 200) {
            throw new IOException("HTTP Response code " + connection.getResponseCode() + " (" + connection.getResponseMessage() + ")");
        }
        InputStream inp = connection.getContent();
        if (inp == null)
            throw new IOException("Empty response");
        try {
            return Json.createReader(inp).readObject();
        } catch (JsonException e) {
            throw new IOException("Failed to parse JSON: " + e.getMessage(), e);
        } finally {
            connection.disconnect();
        }
    }

    // Asynchronous operation

    private String query;
    private JsonQueryCallback callback;

    private JsonQueryUtil() {}

    private JsonQueryUtil(String query, JsonQueryCallback callback) {
        this.query = query;
        this.callback = callback;
    }

    /**
     * Query the server asynchronously.
     * @param query Query string (see {@link #query}).
     * @param callback Callback listener to process the JSON response.
     */
    public static void queryAsync(String query, JsonQueryCallback callback) {
        MainApplication.worker.submit(new JsonQueryUtil(query, callback));
    }

    private void doRealRun() {
        JsonObject obj;
        try {
            obj = query(query);
        } catch (IOException e) {
            Logging.warn(e.getClass().getName() + " while connecting to a chat server: " + e.getMessage());
            obj = null;
        }
        if (callback != null)
            callback.processJson(obj);
    }

    @Override
    public void run() {
        if (EventQueue.isDispatchThread()) {
            new Thread(this::doRealRun).start();
        } else {
            doRealRun();
        }
    }
}
