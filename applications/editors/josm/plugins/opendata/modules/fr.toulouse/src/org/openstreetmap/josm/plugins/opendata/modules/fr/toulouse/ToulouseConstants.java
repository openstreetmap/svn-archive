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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse;

import org.openstreetmap.josm.plugins.opendata.core.datasets.DataSetCategory;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchConstants;

public interface ToulouseConstants extends FrenchConstants {
	
	/**
	 * Sources
	 */
	public static final String SOURCE_GRAND_TOULOUSE = "GrandToulouse";
	public static final String SOURCE_TISSEO = "Tisséo SMTC";
	
	/**
	 * Wiki
	 */
	public static final String WIKI = "http://wiki.openstreetmap.org/wiki/Toulouse/GrandToulouseData";

	/**
	 * Portal
	 */
	public static final String PORTAL = "http://data.grandtoulouse.fr/les-donnees/-/opendata/card/";

	/**
	 * Icons
	 */
	public static final String ICON_CROIX_16 = "data.fr.toulouse_16.png";
	public static final String ICON_CROIX_24 = "data.fr.toulouse_24.png";

	/**
	 * NEPTUNE XML Schema modified to accept Tisséo files
	 */
	public static final String TOULOUSE_NEPTUNE_XSD = "/neptune_toulouse/neptune.xsd";
	
	/**
	 * Categories: TODO: icons
	 */
	public static final DataSetCategory CAT_ASSOCIATIONS = new DataSetCategory("Associations", "");
	public static final DataSetCategory CAT_CITOYENNETE = new DataSetCategory("Citoyenneté", "");
	public static final DataSetCategory CAT_CULTURE = new DataSetCategory("Culture", "");
	public static final DataSetCategory CAT_ENFANCE = new DataSetCategory("Enfance", "");
	public static final DataSetCategory CAT_ENVIRONNEMENT = new DataSetCategory("Environnement", "");
	public static final DataSetCategory CAT_PATRIMOINE = new DataSetCategory("Patrimoine", "");
	public static final DataSetCategory CAT_SPORT = new DataSetCategory("Sport", "");
	public static final DataSetCategory CAT_TOPOGRAPHIE = new DataSetCategory("Topographie", "");
	public static final DataSetCategory CAT_TRANSPORT = new DataSetCategory("Transport", "");
	public static final DataSetCategory CAT_URBANISME = new DataSetCategory("Urbanisme", "");
}
