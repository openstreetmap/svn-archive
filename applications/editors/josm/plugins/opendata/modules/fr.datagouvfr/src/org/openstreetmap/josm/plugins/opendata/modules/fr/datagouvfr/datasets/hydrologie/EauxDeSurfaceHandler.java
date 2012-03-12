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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.hydrologie;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;
import org.openstreetmap.josm.tools.Pair;

public class EauxDeSurfaceHandler extends DataGouvDataSetHandler {

	private static final String ZIP_PATTERN = "FR(I|J|K|L)_SW";
	private static final String SHP_PATTERN = "FR_(I|J|K|L)_SWB_.W_20......";
	
	private static final String[] letters = new String[]{"I","J","K","L"}; 
	private static final String[] names   = new String[]{"Guadeloupe","Martinique","Guyane","La Réunion"}; 
	private static final String[] urls    = new String[]{
		"Couche-SIG-des-caractéristiques-des-bassins-2010-%3A-eaux-de-surface---Guadeloupe-30381899",
		"Couche-SIG-des-caractéristiques-des-bassins-2010-%3A-eaux-de-surface---Martinique-30381935",
		"Couche-SIG-des-caractéristiques-des-bassins-2010-%3A-eaux-de-surface---Guyane-30381988",
		"Couche-SIG-des-caractéristiques-des-bassins-2010-%3A-eaux-de-surface---Réunion-30381991"
	}; 
	
	public EauxDeSurfaceHandler() {
		setName("Eaux de surface");
	}
	
	@Override
	public boolean acceptsFilename(String filename) {
		boolean result = acceptsZipFilename(filename, ZIP_PATTERN) || acceptsShpFilename(filename, SHP_PATTERN);
		if (result) {
			setNationalPortalPath(findPortalSuffix(filename));
		}
		return result;
	}
	
	@Override
	public boolean acceptsUrl(String url) {
		boolean result = super.acceptsUrl(url);
		if (result) {
			setNationalPortalPath(findPortalSuffix(url));
		}
		return result;
	}

	private String findPortalSuffix(String filename) {
		for (String pattern : new String[]{ZIP_PATTERN, SHP_PATTERN}) {
			Matcher m = Pattern.compile(".*"+pattern+"\\....").matcher(filename);
			if (m.matches()) {
				for (int i =0; i<letters.length; i++) {
					if (letters[i].equals(m.group(1))) {
						return urls[i];
					}
				}
			}
		}
		return null;
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO Auto-generated method stub
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataURLs()
	 */
	@Override
	public List<Pair<String, URL>> getDataURLs() {
		List<Pair<String, URL>> result = new ArrayList<Pair<String,URL>>();
		try {
			for (int i =0; i<letters.length; i++) {
				result.add(getDownloadURL(i));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private Pair<String, URL> getDownloadURL(int i) throws MalformedURLException {
		return new Pair<String, URL>(names[i], new URL("http://www.rapportage.eaufrance.fr/sites/default/files/SIG/FR"+letters[i]+"_SW.zip"));
	}
}
