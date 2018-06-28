// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.io.download;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Function;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.plugins.streetside.StreetsideData;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideURL.APIv3;


public class ImageDetailsDownloadRunnable extends BoundsDownloadRunnable {
  private static final Function<Bounds, URL> URL_GEN = APIv3::searchStreetsideImages;

  private final StreetsideData data;

  public ImageDetailsDownloadRunnable(final StreetsideData data, final Bounds bounds) {
    super(bounds);
    this.data = data;
  }

  // TODO: image infos for 360 degree viewer? @rrh
  @Override
  public void run(final URLConnection con) throws IOException {
     // TODO: modifiy decoder to handle Streetside image info. @rrh
	  /*try (JsonReader reader = Json.createReader(new BufferedInputStream(con.getInputStream()))) {
      JsonImageDetailsDecoder.decodeImageInfos(reader.readObject(), data);
      logConnectionInfo(con, null);
      StreetsideMainDialog.getInstance().updateTitle();
    } catch (JsonException | NumberFormatException e) {
      throw new IOException(e);
    }*/
  }

  @Override
  protected Function<Bounds, URL> getUrlGenerator() {
    return URL_GEN;
  }

}
