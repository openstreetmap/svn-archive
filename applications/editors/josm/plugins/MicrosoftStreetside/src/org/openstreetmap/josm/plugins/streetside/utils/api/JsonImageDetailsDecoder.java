// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils.api;


import javax.json.JsonObject;
import javax.json.JsonValue;

import org.openstreetmap.josm.plugins.streetside.StreetsideData;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideURL.APIv3;


/**
 * Decodes the JSON returned by {@link APIv3} into Java objects.
 * Takes a {@link JsonObject} and {@link #decodeImageInfos(JsonObject, StreetsideData)} tries to add the timestamps.
 */
public final class JsonImageDetailsDecoder {
  private JsonImageDetailsDecoder() {
    // Private constructor to avoid instantiation
  }

  public static void decodeImageInfos(final JsonObject json, final StreetsideData data) {
    if (data != null) {
      JsonDecoder.decodeFeatureCollection(json, j -> {
        decodeImageInfo(j, data);
        return null;
      });
    }
  }

  private static void decodeImageInfo(final JsonObject json, final StreetsideData data) {
    if (json != null && data != null) {
      JsonValue properties = json.get("properties");
      if (properties instanceof JsonObject) {
        String id = ((JsonObject) properties).getString("id", null);
        Long he = JsonDecoder.decodeTimestamp(((JsonObject)properties).getString("he", null));
        if (id != null && he != null) {
          data.getImages().stream().filter(
            img -> img instanceof StreetsideImage && id.equals(((StreetsideImage) img).getId())
          ).forEach(img -> img.setHe(he));
        }
      }
    }
  }
}
