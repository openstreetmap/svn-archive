package geochat;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openstreetmap.josm.Main;

/**
 * A static class to query the server and return parsed JSON hash.
 *
 * @author zverik
 */
public class JsonQueryUtil implements Runnable {

    /**
     * Query the server synchronously.
     * @param query Query string, starting with action. Example: <tt>get&lat=1.0&lon=-2.0&uid=12345</tt>
     * @return Parsed JSONObject if the query was successful, <tt>null</tt> otherwise.
     * @throws IOException There was a problem connecting to the server or parsing JSON.
     */
    public static JSONObject query( String query ) throws IOException {
        try {
            String serverURL = Main.pref.get("geochat.server", "http://zverik.dev.openstreetmap.org/osmochat.php?action=");
            URL url = new URL(serverURL + query);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            if( connection.getResponseCode() != 200 ) {
                throw new IOException("HTTP Response code " + connection.getResponseCode() + " (" + connection.getResponseMessage() + ")");
            }
            InputStream inp = connection.getInputStream();
            if( inp == null )
                throw new IOException("Empty response");
            try {
                JSONTokener tokener = new JSONTokener(inp);
                JSONObject result = new JSONObject(tokener);
                return result;
            } catch( JSONException e ) {
                throw new IOException("Failed to parse JSON: " + e.getMessage());
            } finally {
                connection.disconnect();
            }
        } catch( MalformedURLException ex ) {
            throw new IOException("Malformed URL: " + ex.getMessage());
        }
    }

    // Asynchronous operation

    private String query;
    private JsonQueryCallback callback;

    private JsonQueryUtil() {}

    private JsonQueryUtil( String query, JsonQueryCallback callback ) {
        this.query = query;
        this.callback = callback;
    }

    /**
     * Query the server asynchronously.
     * @param query Query string (see {@link #query}).
     * @param callback Callback listener to process the JSON response.
     */
    public static void queryAsync( String query, JsonQueryCallback callback ) {
        Main.worker.submit(new JsonQueryUtil(query, callback));
    }

    private void doRealRun() {
        JSONObject obj;
        try {
            obj = query(query);
        } catch( IOException e ) {
            Main.warn(e.getClass().getName() + " while connecting to a chat server: " + e.getMessage());
            obj = null;
        }
        if( callback != null )
            callback.processJson(obj);
    }

    public void run() {
        if( EventQueue.isDispatchThread() ) {
            new Thread(new Runnable() {
                public void run() {
                    doRealRun();
                }
            }).start();
        } else {
            doRealRun();
        }
    }
}
