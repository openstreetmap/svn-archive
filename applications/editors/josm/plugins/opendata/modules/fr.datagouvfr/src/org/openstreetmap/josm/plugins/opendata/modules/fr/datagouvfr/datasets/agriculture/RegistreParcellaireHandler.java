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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.agriculture;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.preferences.SourceEditor.ExtendedSourceEntry;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class RegistreParcellaireHandler extends DataGouvDataSetHandler {
	
	protected static final int PAS_D_INFORMATION = 0;
	protected static final int BLE_TENDRE = 1;
	protected static final int MAIS_GRAIN_ET_ENSILAGE = 2;
	protected static final int ORGE = 3;
	protected static final int AUTRES_CEREALES = 4;
	protected static final int COLZA = 5;
	protected static final int TOURNESOL = 6;
	protected static final int AUTRES_OLEAGINEUX = 7;
	protected static final int PROTEAGINEUX = 8;
	protected static final int PLANTES_A_FIBRES = 9;
	protected static final int SEMENCES = 10;
	protected static final int GEL_SURFACES_GELEES_SANS_PRODUCTION = 11;
	protected static final int GEL_INDUSTRIEL = 12;
	protected static final int AUTRES_GELS = 13;
	protected static final int RIZ = 14;
	protected static final int LEGUMINEUSES_A_GRAINS = 15;
	protected static final int FOURRAGE = 16;
	protected static final int ESTIVES_LANDES = 17;
	protected static final int PRAIRIES_PERMANENTES = 18;
	protected static final int PRAIRIES_TEMPORAIRES = 19;
	protected static final int VERGERS = 20;
	protected static final int VIGNES = 21;
	protected static final int FRUITS_A_COQUE = 22;
	protected static final int OLIVIERS = 23;
	protected static final int AUTRES_CULTURES_INDUSTRIELLES = 24;
	protected static final int LEGUMES_FLEURS = 25;
	protected static final int CANNE_A_SUCRE = 26;
	protected static final int ARBORICULTURE = 27;
	protected static final int DIVERS = 28;
	
	public RegistreParcellaireHandler() {
		super();
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsShpFilename(filename, "RPG_20.._...");
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getMapPaintStyle()
	 */
	@Override
	public ExtendedSourceEntry getMapPaintStyle() {
		return getMapPaintStyle("Registre Parcellaire Graphique (France)");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (OsmPrimitive p : ds.allPrimitives()) {
			String code = p.get("CULT_MAJ");
			
			if (code != null && !code.isEmpty()) {
				replace(p, "NUM_ILOT", "ref:FR:RPG");
				replace(p, "CULT_MAJ", "code:FR:RPG");
				
				switch (Integer.parseInt(code)) {
				case ARBORICULTURE:
					p.put("landuse", "forest");
					break;
				case FOURRAGE:
				case PRAIRIES_PERMANENTES:
				case PRAIRIES_TEMPORAIRES:
				case ESTIVES_LANDES:
				case GEL_SURFACES_GELEES_SANS_PRODUCTION:
				case GEL_INDUSTRIEL:
				case AUTRES_GELS:
					p.put("landuse", "meadow");
					break;
				case OLIVIERS:
					p.put("trees", "olive_tree");
				case VERGERS:
					p.put("landuse", "orchard");
					break;
				case VIGNES:
					p.put("landuse", "vineyard");
					break;
				case PAS_D_INFORMATION:
				case BLE_TENDRE:
				case MAIS_GRAIN_ET_ENSILAGE:
				case ORGE:
				case AUTRES_CEREALES:
				case COLZA:
				case TOURNESOL:
				case AUTRES_OLEAGINEUX:
				case PROTEAGINEUX:
				case PLANTES_A_FIBRES:
				case SEMENCES:
				case RIZ:
				case LEGUMINEUSES_A_GRAINS:
				case FRUITS_A_COQUE:
				case AUTRES_CULTURES_INDUSTRIELLES:
				case LEGUMES_FLEURS:
				case CANNE_A_SUCRE:
				case DIVERS:
				default:
					p.put("landuse", "farm");
				}
			}
		}
	}
}
