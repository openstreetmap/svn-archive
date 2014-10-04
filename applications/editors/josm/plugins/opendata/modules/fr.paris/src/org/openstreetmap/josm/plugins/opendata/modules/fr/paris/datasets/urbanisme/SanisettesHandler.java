// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.urbanisme;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.josm.corrector.UserCancelException;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.core.datasets.WayCombiner;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.ParisDataSetHandler;

public class SanisettesHandler extends ParisDataSetHandler {

	public SanisettesHandler() {
		super(93);
		setName("Sanisettes");
		getShpHandler().setCheckNodeProximity(true);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsShpFilename(filename, "sanisettes") || acceptsZipFilename(filename, "sanisettes");
	}

	private boolean wayBelongsTo(Way a, List<Way> ways) {
		for (Way b : ways) {
			if (a.getNode(0).equals(b.getNode(0)) || a.getNode(0).equals(b.getNode(b.getNodesCount()-1))
			 || a.getNode(a.getNodesCount()-1).equals(b.getNode(0)) || a.getNode(a.getNodesCount()-1).equals(b.getNode(b.getNodesCount()-1))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean wayProcessed(Way a, List<List<Way>> waysToCombine) {
		for (List<Way> ways : waysToCombine) {
			for (Way b : ways) {
				if (a.equals(b)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		
		List<Way> sourceWays = new ArrayList<>(ds.getWays());
		List<List<Way>> waysToCombine = new ArrayList<>();
		
		for (Iterator<Way> it = sourceWays.iterator(); it.hasNext();) {
			Way w = it.next();
			it.remove();
			if (!wayProcessed(w, waysToCombine)) {
				List<Way> list = new ArrayList<>();
				list.add(w);
				boolean finished = false;
				List<Way> sourceWays2 = new ArrayList<>(sourceWays);
				while (!finished) {
					int before = list.size();
					for (Iterator<Way> it2 = sourceWays2.iterator(); it2.hasNext();) {
						Way w2 = it2.next();
						if (wayBelongsTo(w2, list)) {
							list.add(w2);
							it2.remove();
						}
					}
					int after = list.size();
					finished = (after == before);
				}
				if (list.size() > 1) {
					waysToCombine.add(list);
				}
			}
		}
				
		for (List<Way> ways : waysToCombine) {
			try {
				WayCombiner.combineWays(ways);
			} catch (UserCancelException e) {
				return;
			}
		}
		
		for (Way w : ds.getWays()) {
			if (w.getNodesCount() <= 3) {
				ds.removePrimitive(w);
				for (Node n : w.getNodes()) {
					ds.removePrimitive(n);
				}
			} else {
				w.put("amenity", "toilets");
			}
		}
	}

	@Override
	protected String getDirectLink() {
		return PORTAL+"hn/sanisettes.zip";
	}
}
