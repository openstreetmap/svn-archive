// License: WTFPL
package geochat;

import org.json.JSONObject;

/**
 * A callback for {@link JsonQueryUtil}.
 *
 * @author zverik
 */
public interface JsonQueryCallback {

    /**
     * Process JSON response from a query. This method is called every time,
     * even on unsuccessful query.
     *
     * @param json JSON parsed response or null if the query was unsuccessful.
     */
    void processJson( JSONObject json );
}
