// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import org.openstreetmap.josm.plugins.opendata.core.datasets.at.AustrianGmlHandler;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchShpHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.GmlHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.ShpHandler;

public abstract class NationalHandlers {

	public static final GmlHandler[] DEFAULT_GML_HANDLERS = new GmlHandler[]{
		new AustrianGmlHandler()
	};

	public static final ShpHandler[] DEFAULT_SHP_HANDLERS = new ShpHandler[]{
		new FrenchShpHandler()
	};
}
