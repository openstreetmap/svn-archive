// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils.api;

import java.util.Objects;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.openstreetmap.josm.plugins.streetside.StreetsideImage;

import org.openstreetmap.josm.plugins.streetside.StreetsideLocationChangeset;


public final class JsonLocationChangesetEncoder {
  private JsonLocationChangesetEncoder() {
    // Private constructor to avoid instantiation
  }

  public static JsonObjectBuilder encodeLocationChangeset(StreetsideLocationChangeset changeset) {
    Objects.requireNonNull(changeset);
    final JsonArrayBuilder imgChanges = Json.createArrayBuilder();
    for (StreetsideImage img : changeset) {
      imgChanges.add(encodeImageChanges(img));
    }
    return Json.createObjectBuilder()
      .add("type", "location")
      .add("changes", imgChanges)
      .add("request_comment", "JOSM-created");
  }

  private static JsonObjectBuilder encodeImageChanges(StreetsideImage img) {
    Objects.requireNonNull(img);

    final JsonObjectBuilder to = Json.createObjectBuilder();
    if (!img.getTempLatLon().equalsEpsilon(img.getLatLon())) {
      to.add("geometry", Json.createObjectBuilder()
        .add("coordinates", Json.createArrayBuilder()
          .add(img.getTempLatLon().getX())
          .add(img.getTempLatLon().getY())
        ).add("type", "Point")
      );
    }
    if (Math.abs(img.getHe() - img.getTempHe()) > 1e-9) {
      to.add("properties", Json.createObjectBuilder().add("ca", img.getTempHe()));
    } else {
      to.add("properties", Json.createObjectBuilder());
    }
    if (!img.getTempLatLon().equalsEpsilon(img.getLatLon())) {
      to.add("type", "Feature");
    }

    return Json.createObjectBuilder()
      .add("image_key", img.getId())
      .add("to", to);
  }
}
