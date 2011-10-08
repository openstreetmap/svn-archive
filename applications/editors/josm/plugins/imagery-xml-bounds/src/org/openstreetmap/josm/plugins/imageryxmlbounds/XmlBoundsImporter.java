//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
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
package org.openstreetmap.josm.plugins.imageryxmlbounds;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.imagery.ImageryInfo.ImageryBounds;
import org.openstreetmap.josm.data.imagery.ImageryInfo.ImageryType;
import org.openstreetmap.josm.data.imagery.Shape;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.FileImporter;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.imagery.ImageryReader;
import org.xml.sax.SAXException;

/**
 * @author Don-vip
 *
 */
public class XmlBoundsImporter extends FileImporter implements XmlBoundsConstants {

	public XmlBoundsImporter() {
		super(FILE_FILTER);
	}

	public DataSet convertImageryEntries(List<ImageryInfo> entries) {
		DataSet dataSet = new DataSet();
		
		for (ImageryInfo imagery : entries) {
			if (!imagery.isBlacklisted()) {
				ImageryBounds bounds = imagery.getBounds();
				if (bounds != null) {
					dataSet.addPrimitive(convertImagery(imagery, bounds, dataSet));
				}
			}
		}
		return dataSet;
	}
	
	private void safePut(OsmPrimitive p, String key, Object value) {
		if (value != null) {
			if (value instanceof Collection) {
				String s = "";
				for (Object elt : (Collection<?>)value) {
					if (elt != null && elt.toString() != null && !elt.toString().isEmpty()) {
						if (!s.isEmpty()) {
							s += ";";
						}
						s += elt.toString();
					}
				}
				if (!s.isEmpty()) {
					p.put(key, s);
				}
			} else if (!value.equals(0) && value.toString() != null && !value.toString().isEmpty()) {
				p.put(key, value.toString());
			}
		}
	}
	
    private Node getNode(LatLon latlon, DataSet dataSet) {
        List<Node> nodes = dataSet.searchNodes(new BBox(latlon, latlon));
        if (!nodes.isEmpty()) {
            return nodes.get(0);
        } else {
            Node node = new Node(latlon);
            dataSet.addPrimitive(node);
            return node;
        }
    }

	private Node getNode(double lat, double lon, DataSet dataSet) {
	    return getNode(new LatLon(lat, lon), dataSet);
	}
	
	private void ensureWayIsClosed(Way way) {
	    if (!way.getNode(0).equals(way.getNode(way.getNodesCount()-1))) {
	        way.addNode(way.getNode(0));
	    }
	}
	
	private OsmPrimitive convertImagery(ImageryInfo imagery, ImageryBounds bounds, DataSet dataSet) {
		OsmPrimitive osmImagery = null;
		if (bounds.getShapes().isEmpty()) {
			LatLon bottomLeft = bounds.getMin();
			LatLon topRight = bounds.getMax();
			LatLon topLeft = new LatLon(topRight.lat(), bottomLeft.lon());
			LatLon bottomRight = new LatLon(bottomLeft.lat(), topRight.lon());
			
			Way way = new Way();
			for (LatLon ll : new LatLon[]{bottomLeft, topLeft, topRight, bottomRight}) {
				way.addNode(getNode(ll, dataSet));
			}
			ensureWayIsClosed(way);
			osmImagery = way;
			
		} else {
			Relation relation = new Relation();
			relation.put("type", "multipolygon");
			for (Shape shape : bounds.getShapes()) {
				Way way = new Way();
				for (Coordinate coor : shape.getPoints()) {
					way.addNode(getNode(coor.getLat(), coor.getLon(), dataSet));
				}
				ensureWayIsClosed(way);
				dataSet.addPrimitive(way);
				relation.addMember(new RelationMember("outer", way));
			}
			osmImagery = relation;
		}
		
		safePut(osmImagery, KEY_NAME, imagery.getName());
		safePut(osmImagery, KEY_TYPE, imagery.getImageryType().getUrlString());
		safePut(osmImagery, KEY_DEFAULT, imagery.isDefaultEntry());
		safePut(osmImagery, KEY_URL, imagery.getUrl());
		safePut(osmImagery, KEY_PROJECTIONS, imagery.getServerProjections());
		safePut(osmImagery, KEY_EULA, imagery.getEulaAcceptanceRequired());
		safePut(osmImagery, KEY_ATTR_TEXT, imagery.getAttributionText(0, null, null));
		safePut(osmImagery, KEY_ATTR_URL, imagery.getAttributionLinkURL());
        safePut(osmImagery, KEY_TERMS_TEXT, imagery.getTermsOfUseText());
		safePut(osmImagery, KEY_TERMS_URL, imagery.getTermsOfUseURL());
		safePut(osmImagery, KEY_COUNTRY_CODE, imagery.getCountryCode());
		safePut(osmImagery, KEY_LOGO_URL, imagery.getAttributionImageURL());

		if (imagery.getImageryType().equals(ImageryType.TMS)) {
			safePut(osmImagery, KEY_MAX_ZOOM, imagery.getMaxZoom());
			safePut(osmImagery, KEY_MIN_ZOOM, imagery.getMinZoom());
		}
		
		return osmImagery;
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.FileImporter#importData(java.io.File, org.openstreetmap.josm.gui.progress.ProgressMonitor)
	 */
	@Override
	public void importData(final File file, ProgressMonitor progressMonitor)
			throws IOException, IllegalDataException {
		try {
			ImageryReader reader = null;
			
			try {
				reader = new ValidatingImageryReader(file.getAbsolutePath());
			} catch (SAXException e)  {
                if (JOptionPane.showConfirmDialog(
                        Main.parent,
                        tr("Validating error in file {0}:\n{1}\nDo you want to continue without validating the file ?", file.getPath(), e.getLocalizedMessage()),
                        tr("Open Imagery XML file"),
                        JOptionPane.YES_NO_CANCEL_OPTION) != JOptionPane.YES_OPTION) {
                	return;
                }

				reader = new ImageryReader(file.getAbsolutePath());
			}
			
			final DataSet dataSet = convertImageryEntries(reader.parse());
			        
	        final XmlBoundsLayer layer = new XmlBoundsLayer(dataSet, file.getName(), file);
	        Runnable uiStuff = new Runnable() {
	            @Override
	            public void run() {
	                if (dataSet.allPrimitives().isEmpty()) {
	                    JOptionPane.showMessageDialog(
	                            Main.parent,
	                            tr("No data found in file {0}.", file.getPath()),
	                            tr("Open Imagery XML file"),
	                            JOptionPane.INFORMATION_MESSAGE);
	                }
	                Main.main.addLayer(layer);
	                layer.onPostLoadFromFile();
	            }
	        };
	        if (SwingUtilities.isEventDispatchThread()) {
	            uiStuff.run();
	        } else {
	            SwingUtilities.invokeLater(uiStuff);
	        }
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.FileImporter#importData(java.util.List, org.openstreetmap.josm.gui.progress.ProgressMonitor)
	 */
	@Override
	public void importData(List<File> files, ProgressMonitor progressMonitor)
			throws IOException, IllegalDataException {
		for (File file : files) {
			importData(file, progressMonitor);
		}
	}
}
