// License: WTFPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imagery_offset_db;

/**
 * A listener for {@link SimpleOffsetQueryTask}.
 *
 * @author Zverik
 * @license WTFPL
 */
public interface QuerySuccessListener {

    /**
     * Query has been processed and did not fail.
     */
    void queryPassed();
}
