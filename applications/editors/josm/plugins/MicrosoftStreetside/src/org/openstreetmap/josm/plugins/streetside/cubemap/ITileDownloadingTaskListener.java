//License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cubemap;

/**
* Interface for listeners of the class {@link org.openstreetmap.josm.plugins.streetside.StreetsideData}.
*
* @author renerr18
*
*/
public interface ITileDownloadingTaskListener {

 /**
 * Fired when a cubemap tile image is downloaded by a download worker.
 * @param imageId image id 
 */
 void tileAdded(String imageId);
}