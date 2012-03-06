package org.openstreetmap.josm.plugins.opendata.core.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.regex.Matcher;

import org.openstreetmap.josm.data.projection.LambertCC9Zones;
import org.openstreetmap.josm.data.projection.Projection;

public class LambertCC9ZonesProjectionPatterns extends ProjectionPatterns {
	
	public static final LambertCC9Zones[] lambertCC9Zones = new LambertCC9Zones[9];
	static {
		for (int i=0; i<lambertCC9Zones.length; i++) {
			lambertCC9Zones[i] = new LambertCC9Zones(i);
		}
	}

	public LambertCC9ZonesProjectionPatterns(String proj) {
		super(proj);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.io.CoordinatePatterns#getProjection(java.lang.String, java.lang.String)
	 */
	@Override
	public Projection getProjection(String xFieldName, String yFieldName) {

		Matcher mx = getXPattern().matcher(xFieldName);
		Matcher my = getYPattern().matcher(yFieldName);
		mx.find();
		my.find();
		String ccx = mx.group(1);
		String ccy = mx.group(1);
		if (!ccx.equals(ccy)) {
			throw new IllegalArgumentException(tr("''Lambert CC 9 zones'' coordinates found with different zone codes for X and Y: "+xFieldName+", "+yFieldName));
		}
		return lambertCC9Zones[Integer.parseInt(ccx)-42];
	}
}
