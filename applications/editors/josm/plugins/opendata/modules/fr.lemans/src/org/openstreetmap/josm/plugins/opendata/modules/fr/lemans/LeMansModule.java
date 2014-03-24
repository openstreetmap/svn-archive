// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets.CantonsSartheHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets.CirconscriptionsLegislativesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets.CodesPostauxHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets.CommunesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets.ConseilsQuartiersHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets.PointsApportVolontaireHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets.ServicesCommunautairesMunicipauxHandler;

public class LeMansModule extends AbstractModule {

	public LeMansModule(ModuleInformation info) {
		super(info);
		handlers.add(ServicesCommunautairesMunicipauxHandler.class);
		handlers.add(CantonsSartheHandler.class);
		handlers.add(CommunesHandler.class);
		handlers.add(CodesPostauxHandler.class);
		handlers.add(CirconscriptionsLegislativesHandler.class);
		handlers.add(ConseilsQuartiersHandler.class);
		handlers.add(PointsApportVolontaireHandler.class);
    }
}
