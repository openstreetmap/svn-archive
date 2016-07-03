// License: WTFPL. For details, see LICENSE file.
package geochat;

import javax.json.JsonObject;

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
    void processJson(JsonObject json);
}
