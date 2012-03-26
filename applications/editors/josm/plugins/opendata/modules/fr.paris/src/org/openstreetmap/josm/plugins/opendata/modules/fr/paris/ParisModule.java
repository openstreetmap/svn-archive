//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.environnement.ArbresRemarquablesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.urbanisme.EclairagePublicHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.urbanisme.SanisettesHandler;

public class ParisModule extends AbstractModule {

	public ParisModule(ModuleInformation info) {
		super(info);
        handlers.add(SanisettesHandler.class);
        handlers.add(ArbresRemarquablesHandler.class);
        //handlers.add(VolumesBatisHandler.class); // Disabled as the projection cannot be transformed
        //handlers.add(VolumesNonBatisHandler.class); // Disabled as the projection cannot be transformed
        //handlers.add(ElectriciteHandler.class); // Disabled (useless for OSM)
        handlers.add(EclairagePublicHandler.class);
    }
}
