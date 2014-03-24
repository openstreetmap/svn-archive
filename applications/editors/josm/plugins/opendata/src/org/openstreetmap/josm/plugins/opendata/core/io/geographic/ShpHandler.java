// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.nio.charset.Charset;
import java.util.Set;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public interface ShpHandler extends GeographicHandler {

	public void notifyFeatureParsed(Object feature, DataSet result, Set<OsmPrimitive> featurePrimitives);

	public void setDbfCharset(Charset charset);
	
	public Charset getDbfCharset();
}
