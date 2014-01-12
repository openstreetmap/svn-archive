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
package org.openstreetmap.josm.plugins.opendata.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.SimpleDataSetHandler;

public abstract class NamesFrUtils implements OdConstants {
	
	private static Map<String, String> dictionary = initDictionary();

	public static final String checkDictionary(String value) {
		String result = "";
		for (String word : value.split(" ")) {
			if (!result.isEmpty()) {
				result += " ";
			}
			result += dictionary.containsKey(word) ?  dictionary.get(word) : word;
		}
		return result;
	}
	
	private static Map<String, String> initDictionary() {
		Map<String, String> result = new HashMap<String, String>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					SimpleDataSetHandler.class.getResourceAsStream(DICTIONARY_FR), UTF8));
			String line = reader.readLine(); // Skip first line
			while ((line = reader.readLine()) != null) {
				String[] tab = line.split(";");
				result.put(tab[0].replace("\"", ""), tab[1].replace("\"", ""));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static final String getStreetLabel(String label) {
		if (label == null) {
			return label;
		} else if (label.equals("All")) {
			return "Allée";
		} else if (label.equals("Autoroute")) {
			return label;
		} else if (label.startsWith("Anc")) { // Anc, Ancien
			return "Ancien";
		} else if (label.equals("Av")) {
			return "Avenue";
		} else if (label.equals("Bd")) {
			return "Boulevard";
		} else if (label.equals("Bre")) {
			return "Bré";
		} else if (label.equals("Caminot")) {
			return label;
		} else if (label.equals("Carrefour")) {
			return label;
		} else if (label.startsWith("Che")) { // Che, Chem
			return "Chemin";
		} else if (label.equals("Cite")) {
			return "Cité";
		} else if (label.equals("Clos")) {
			return label;
		} else if (label.equals("Cote")) {
			return "Côte";
		} else if (label.equals("Cours")) {
			return label;
		} else if (label.equals("Dom")) {
			return "Domaine";
		} else if (label.equals("Dsc")) {
			return "Descente";
		} else if (label.equals("Esp")) {
			return "Esplanade";
		} else if (label.equals("Espa")) {
			return "Espace";
		} else if (label.equals("Giratoire")) {
			return label;
		} else if (label.equals("Grande-rue")) {
			return label;
		} else if (label.equals("Hameau")) {
			return label;
		} else if (label.equals("Imp")) {
			return "Impasse";
		} else if (label.equals("Itineraire")) {
			return "Itinéraire";
		} else if (label.equals("Jardin")) {
			return label;
		} else if (label.startsWith("L'") || label.equals("La") || label.equals("Le") || label.equals("Les") || label.equals("Saint")) { // Lieux-dits
			return label;
		} else if (label.equals("Lot")) {
			return "Lotissement";
		} else if (label.equals("Mail")) {
			return label;
		} else if (label.equals("Mas")) {
			return label;
		} else if (label.equals("Parc")) {
			return label;
		} else if (label.equals("Pas")) {
			return "Passage";
		} else if (label.equals("Passerelle")) {
			return label;
		} else if (label.equals("Pch")) {
			return "Petit Chemin";
		} else if (label.equals("Petit")) {
			return label;
		} else if (label.equals("Petite-allée")) {
			return label;
		} else if (label.equals("Petite-rue")) {
			return label;
		} else if (label.equals("Pl")) {
			return "Place";
		} else if (label.equals("Plan")) {
			return label;
		} else if (label.equals("Pont")) {
			return label;
		} else if (label.equals("Port")) {
			return label;
		} else if (label.equals("Porte")) {
			return label;
		} else if (label.equals("Prom")) {
			return "Promenade";
		} else if (label.equals("Prv")) {
			return "Parvis";
		} else if (label.equals("Qu")) {
			return "Quai";
		} else if (label.equals("Rampe")) {
			return label;
		} else if (label.equals("Residence")) {
			return "Résidence";
		} else if (label.equals("Rocade")) {
			return label;
		} else if (label.equals("Rpt")) {
			return "Rond-Point";
		} else if (label.equals("Rte")) {
			return "Route";
		} else if (label.equals("Rue")) {
			return label;
		} else if (label.equals("Sentier")) {
			return label;
		} else if (label.equals("Sq")) {
			return "Square";
		} else if (label.equals("Tra")) {
			return "Traverse";
		} else if (label.equals("Vieux")) {
			return label;
		} else if (label.equals("Voie")) {
			return label;
		} else if (label.equals("Zone")) {
			return label;
		} else {
			System.err.println("Warning: unknown street label: "+label);
			return label;
		}
	}

	public static final String checkStreetName(OsmPrimitive p, String key) {
		String value = null;
		if (p != null) {
			value = p.get(key);
			if (value != null) {
				value = WordUtils.capitalizeFully(value);
				// Cas particuliers 
				if (value.equals("Boulingrin")) { // square Boulingrin, mal formé
					value = "Sq Boulingrin";
				} else if (value.matches("A[0-9]+")) { // Autoroutes sans le mot "Autoroute"
					value = "Autoroute "+value;
				} else if (value.equals("All A61")) { // A61 qualifiée d'Allée ?
					value = "Autoroute A61";
				} else if (value.startsWith("Che Vieux Che")) { // "Che" redondant
					value = value.replaceFirst("Che ", "");
				} else if (value.startsWith("Petite Allee ")) { // Tiret, comme grand-rue, petite-rue
					value = value.replaceFirst("Petite Allee ", "Petite-allée ");
				} else if (value.startsWith("Ld De ")) { // Lieux-dit
					value = value.replaceFirst("Ld De ", "");
				}
				while (value.startsWith("Ld ")) { // Lieux-dit, inutile. Plus le cas avec "Ld Ld"
					value = value.replaceFirst("Ld ", "");
				}
				if (value.startsWith("L ")) {
					value = value.replaceFirst("L ", "L'");
				}
				String[] words = value.split(" ");
				if (words.length > 0) {
					value = "";
					List<String> list = Arrays.asList(words);
					words[0] = getStreetLabel(words[0]);
					if (words[0].equals("Ancien") && words.length > 1 && words[1].equals("Che")) {
						words[1] = "Chemin";
					}
					for (int i = 0; i < words.length; i++) {
						if (i > 0) {
							value += " ";
							// Prénoms/Noms propres abrégés
							if (words[i].equals("A") && list.contains("Bernard")) {
								words[i] = "Arnaud";
							} else if (words[i].equals("A") && list.contains("Passerieu")) {
								words[i] = "Ariste";
							} else if (words[i].equals("A") && list.contains("Bougainville")) {
								words[i] = "Antoine";
							} else if (words[i].equals("Ch") && list.contains("Leconte")) {
								words[i] = "Charles";
							} else if (words[i].equals("Frs") && list.contains("Dugua")) {
								words[i] = "François";
							} else if (words[i].equals("G") && list.contains("Latecoere")) {
								words[i] = "Georges";
							} else if (words[i].equals("H") && list.contains("Lautrec")) {
								words[i] = "Henri";
							} else if (words[i].equals("J") && list.contains("Dieulafoy")) {
								words[i] = "Jane";
							} else if (words[i].equals("J") && (list.contains("Champollion") || list.contains("Stanislas"))) {
								words[i] = "Jean";
							} else if (words[i].equals("L") && list.contains("Zamenhof")) {
								words[i] = "Ludwik";
							} else if (words[i].equals("L") && list.contains("Sacha")) {
								words[i] = "Lucien";
								if (!list.contains("Et")) {
									words[i] += " et";
								}
							} else if (words[i].equals("L") && (list.contains("Vauquelin") || list.contains("Bougainville"))) {
								words[i] = "Louis";
							} else if (words[i].equals("M") && list.contains("Dieulafoy")) {
								words[i] = "Marcel";
							} else if (words[i].equals("M") && list.contains("Arifat")) {
								words[i] = "Marie";
							} else if (words[i].equals("N") && list.contains("Djamena")) {
								words[i] = "N'";
							} else if (words[i].equals("Oo")) {
								words[i] = "Oô";
							} else if (words[i].equals("Ph") && list.contains("Ravary")) {
								words[i] = "Philippe";
							} else if (words[i].equals("R") && list.contains("Folliot")) {
								words[i] = "Raphaël";
							} else if (words[i].equals("W") && list.contains("Booth")) {
								words[i] = "William";
							// Mots de liaison non couverts par le dictionnaire
							} else if (words[i].equals("A")) {
								words[i] = "à";
							} else if (words[i].equals("D") || words[i].equals("L")) {
								words[i] = words[i].toLowerCase()+"'";
							} else if (words[i].equals("La") || words[i].equals("Le")) {
								words[i] = words[i].toLowerCase();
							}
						}
						value += words[i];
					}
				}
				// Ponctuation
				value = value.replace("' ", "'");
				// Dictionnaire
				value = checkDictionary(value);
				p.put(key, value);
			}
		}
		return value;
	}
}
