//License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cubemap;

/**
* Interface for listeners of the class {@link StreetsideData}.
*
* @author renerr18
*
*/
public interface ITileDownloadingTaskListener {

/**
* Fired when a cubemap tile image is downloaded by a download worker.
*
*/
void tileAdded(String imageId);
}