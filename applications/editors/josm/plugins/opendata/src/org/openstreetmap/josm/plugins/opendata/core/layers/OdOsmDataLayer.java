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
package org.openstreetmap.josm.plugins.opendata.core.layers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

public class OdOsmDataLayer extends OsmDataLayer implements OdLayer {

	private final OdDataLayer dataLayer;

	public OdOsmDataLayer(OdDataLayer dataLayer, DataSet data, String name) {
		super(data, name, null);
		this.dataLayer = dataLayer;
	}
	
    public final void removeForbiddenTags() {
		if (dataLayer != null && dataLayer.handler != null && dataLayer.handler.hasForbiddenTags()) {
			Main.worker.submit(new Runnable() {
				@Override
				public void run() {
					data.clearSelection();
					for (Iterator<OsmPrimitive> it = data.allPrimitives().iterator(); it.hasNext();) {
						OsmPrimitive p = it.next();
						if (dataLayer.handler.isForbidden(p)) {
							data.addSelected(p);
							
							List<Node> nodes = null;
							if (p instanceof Way) {
								nodes = ((Way) p).getNodes();
							}
							//data.removePrimitive(p);
							if (nodes != null) {
								for (Node n : nodes) {
									List<OsmPrimitive> refferingAllowedWays = new ArrayList<OsmPrimitive>();
									for (OsmPrimitive referrer : n.getReferrers()) {
										if (referrer instanceof Way && !dataLayer.handler.isForbidden(referrer)) {
											refferingAllowedWays.add(referrer);
										}
									}
									
									if (refferingAllowedWays.isEmpty()) {
										//data.removePrimitive(n);
										data.addSelected(n);
									}
								}
							}
							/*for (OsmPrimitive referrer : p.getReferrers()) {
								System.out.println(referrer);
							}*/
						}
					}
					Collection<OsmPrimitive> sel = data.getSelected();
					if (!sel.isEmpty()) {
						Main.main.menu.purge.actionPerformed(null);
					}
				}
			});
		}
    }

	@Override
	public OdDataLayer getDataLayer() {
		return dataLayer;
	}
}
