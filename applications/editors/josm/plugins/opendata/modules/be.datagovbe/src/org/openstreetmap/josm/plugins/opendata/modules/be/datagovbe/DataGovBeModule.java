// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.be.datagovbe;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.be.datagovbe.datasets.culture.ArchitecturalHeritageHandler;

public class DataGovBeModule extends AbstractModule {

	public DataGovBeModule(ModuleInformation info) {
		super(info);
		handlers.add(ArchitecturalHeritageHandler.class);
    }
}
