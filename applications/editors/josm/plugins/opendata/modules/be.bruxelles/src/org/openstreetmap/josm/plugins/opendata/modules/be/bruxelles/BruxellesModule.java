// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.be.bruxelles;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.be.bruxelles.datasets.culture.BDHandler;

public class BruxellesModule extends AbstractModule {

	public BruxellesModule(ModuleInformation info) {
		super(info);
		handlers.add(BDHandler.class);
    }
}
