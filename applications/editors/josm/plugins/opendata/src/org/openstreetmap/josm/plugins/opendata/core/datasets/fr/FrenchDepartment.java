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
package org.openstreetmap.josm.plugins.opendata.core.datasets.fr;

public final class FrenchDepartment {
	private final String code; 
	private final String name;
	
	private FrenchDepartment(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public final String getCode() {
		return code;
	}

	public final String getName() {
		return name;
	}

	public static final FrenchDepartment[] allDepartments = new FrenchDepartment[] {
		new FrenchDepartment("001", "Ain"),
		new FrenchDepartment("002", "Aisne"),
		new FrenchDepartment("003", "Allier"),
		new FrenchDepartment("004", "Alpes-de-Haute-Provence"),
		new FrenchDepartment("005", "Hautes-Alpes"),
		new FrenchDepartment("006", "Alpes-Maritimes"),
		new FrenchDepartment("007", "Ardèche"),
		new FrenchDepartment("008", "Ardennes"),
		new FrenchDepartment("009", "Ariège"),
		new FrenchDepartment("010", "Aube"),
		new FrenchDepartment("011", "Aude"),
		new FrenchDepartment("012", "Aveyron"),
		new FrenchDepartment("013", "Bouches-du-Rhône"),
		new FrenchDepartment("014", "Calvados"),
		new FrenchDepartment("015", "Cantal"),
		new FrenchDepartment("016", "Charente"),
		new FrenchDepartment("017", "Charente-Maritime"),
		new FrenchDepartment("018", "Cher"),
		new FrenchDepartment("019", "Corrèze"),
		new FrenchDepartment("02A", "Corse-du-Sud"),
		new FrenchDepartment("02B", "Haute-Corse"),
		new FrenchDepartment("021", "Côte-d'Or"),
		new FrenchDepartment("022", "Côtes-d'Armor"),
		new FrenchDepartment("023", "Creuse"),
		new FrenchDepartment("024", "Dordogne"),
		new FrenchDepartment("025", "Doubs"),
		new FrenchDepartment("026", "Drôme"),
		new FrenchDepartment("027", "Eure"),
		new FrenchDepartment("028", "Eure-et-Loir"),
		new FrenchDepartment("029", "Finistère"),
		new FrenchDepartment("030", "Gard"),
		new FrenchDepartment("031", "Haute-Garonne"),
		new FrenchDepartment("032", "Gers"),
		new FrenchDepartment("033", "Gironde"),
		new FrenchDepartment("034", "Hérault"),
		new FrenchDepartment("035", "Ille-et-Vilaine"),
		new FrenchDepartment("036", "Indre"),
		new FrenchDepartment("037", "Indre-et-Loire"),
		new FrenchDepartment("038", "Isère"),
		new FrenchDepartment("039", "Jura"),
		new FrenchDepartment("040", "Landes"),
		new FrenchDepartment("041", "Loir-et-Cher"),
		new FrenchDepartment("042", "Loire"),
		new FrenchDepartment("043", "Haute-Loire"),
		new FrenchDepartment("044", "Loire-Atlantique"),
		new FrenchDepartment("045", "Loiret"),
		new FrenchDepartment("046", "Lot"),
		new FrenchDepartment("047", "Lot-et-Garonne"),
		new FrenchDepartment("048", "Lozère"),
		new FrenchDepartment("049", "Maine-et-Loire"),
		new FrenchDepartment("050", "Manche"),
		new FrenchDepartment("051", "Marne"),
		new FrenchDepartment("052", "Haute-Marne"),
		new FrenchDepartment("053", "Mayenne"),
		new FrenchDepartment("054", "Meurthe-et-Moselle"),
		new FrenchDepartment("055", "Meuse"),
		new FrenchDepartment("056", "Morbihan"),
		new FrenchDepartment("057", "Moselle"),
		new FrenchDepartment("058", "Nièvre"),
		new FrenchDepartment("059", "Nord"),
		new FrenchDepartment("060", "Oise"),
		new FrenchDepartment("061", "Orne"),
		new FrenchDepartment("062", "Pas-de-Calais"),
		new FrenchDepartment("063", "Puy-de-Dôme"),
		new FrenchDepartment("064", "Pyrénées-Atlantiques"),
		new FrenchDepartment("065", "Hautes-Pyrénées"),
		new FrenchDepartment("066", "Pyrénées-Orientales"),
		new FrenchDepartment("067", "Bas-Rhin"),
		new FrenchDepartment("068", "Haut-Rhin"),
		new FrenchDepartment("069", "Rhône"),
		new FrenchDepartment("070", "Haute-Saône"),
		new FrenchDepartment("071", "Saône-et-Loire"),
		new FrenchDepartment("072", "Sarthe"),
		new FrenchDepartment("073", "Savoie"),
		new FrenchDepartment("074", "Haute-Savoie"),
		new FrenchDepartment("075", "Paris"),
		new FrenchDepartment("076", "Seine-Maritime"),
		new FrenchDepartment("077", "Seine-et-Marne"),
		new FrenchDepartment("078", "Yvelines"),
		new FrenchDepartment("079", "Deux-Sèvres"),
		new FrenchDepartment("080", "Somme"),
		new FrenchDepartment("081", "Tarn"),
		new FrenchDepartment("082", "Tarn-et-Garonne"),
		new FrenchDepartment("083", "Var"),
		new FrenchDepartment("084", "Vaucluse"),
		new FrenchDepartment("085", "Vendée"),
		new FrenchDepartment("086", "Vienne"),
		new FrenchDepartment("087", "Haute-Vienne"),
		new FrenchDepartment("088", "Vosges"),
		new FrenchDepartment("089", "Yonne"),
		new FrenchDepartment("090", "Territoire de Belfort"),
		new FrenchDepartment("091", "Essonne"),
		new FrenchDepartment("092", "Hauts-de-Seine"),
		new FrenchDepartment("093", "Seine-Saint-Denis"),
		new FrenchDepartment("094", "Val-de-Marne"),
		new FrenchDepartment("095", "Val-d'Oise"),
		new FrenchDepartment("971", "Guadeloupe"),
		new FrenchDepartment("972", "Martinique"),
		new FrenchDepartment("973", "Guyane"),
		new FrenchDepartment("974", "La Réunion"),
		new FrenchDepartment("976", "Mayotte"),
	};
}
