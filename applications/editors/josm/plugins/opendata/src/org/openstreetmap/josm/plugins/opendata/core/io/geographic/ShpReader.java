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
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.AbstractCRS;
import org.geotools.referencing.crs.AbstractDerivedCRS;
import org.geotools.referencing.crs.AbstractSingleCRS;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.corrector.UserCancelException;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.tools.ImageProvider;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ShpReader extends AbstractReader implements OdConstants {

	private final ShpHandler handler;
	
	private final CoordinateReferenceSystem wgs84;
	private final Map<String, Node> nodes;

	private CoordinateReferenceSystem crs;
	private MathTransform transform;
	
	private final Set<OsmPrimitive> featurePrimitives = new HashSet<OsmPrimitive>();
	
	public ShpReader(ShpHandler handler) throws NoSuchAuthorityCodeException, FactoryException {
		this.handler = handler;
		this.wgs84 = CRS.decode("EPSG:4326");
		this.nodes = new HashMap<String, Node>();
	}

	public static DataSet parseDataSet(InputStream in, File file,
			AbstractDataSetHandler handler, ProgressMonitor instance) throws IOException {
		if (in != null) {
			in.close();
		}
		try {
			return new ShpReader(handler != null ? handler.getShpHandler() : null).parse(file, instance);
		} catch (IOException e) {
			throw e;
		} catch (Throwable t) {
			throw new IOException(t);
		}
	}
	
	private static final void compareDebug(CoordinateReferenceSystem crs1, CoordinateReferenceSystem crs2) {
		System.out.println("-- COMPARING "+crs1.getName()+" WITH "+crs2.getName()+" --");
		compareDebug("class", crs1.getClass(), crs2.getClass());
		CoordinateSystem cs1 = crs1.getCoordinateSystem();
		CoordinateSystem cs2 = crs2.getCoordinateSystem();
		if (!compareDebug("cs", cs1, cs2)) {
			Integer dim1 = cs1.getDimension();
			Integer dim2 = cs2.getDimension();
			if (compareDebug("cs.dim", dim1, dim2)) {
				for (int i = 0; i<dim1; i++) {
					compareDebug("cs.axis"+i, cs1.getAxis(i), cs1.getAxis(i));
				}
			}
		}
		if (crs1 instanceof AbstractSingleCRS) {
			Datum datum1 = ((AbstractSingleCRS) crs1).getDatum();
			Datum datum2 = ((AbstractSingleCRS) crs2).getDatum();
			if (!compareDebug("datum", datum1, datum2)) {
				AbstractIdentifiedObject adatum1 = (AbstractIdentifiedObject) datum1;
				AbstractIdentifiedObject adatum2 = (AbstractIdentifiedObject) datum2;
				compareDebug("datum.name1", adatum1.nameMatches(adatum2.getName().getCode()), adatum1.getName(), adatum2.getName());
				compareDebug("datum.name2", adatum2.nameMatches(adatum1.getName().getCode()), adatum2.getName(), adatum1.getName());
			}
			if (crs1 instanceof AbstractDerivedCRS) {
				AbstractDerivedCRS adcrs1 = (AbstractDerivedCRS) crs1;
				AbstractDerivedCRS adcrs2 = (AbstractDerivedCRS) crs2;
				compareDebug("baseCRS", adcrs1.getBaseCRS(), adcrs2.getBaseCRS());
				compareDebug("conversionFromBase", adcrs1.getConversionFromBase(), adcrs2.getConversionFromBase());
			}
		}
		System.out.println("-- COMPARING FINISHED --");
	}
	
	private static final boolean compareDebug(String text, Object o1, Object o2) {
		return compareDebug(text, o1.equals(o2), o1, o2);
	}
	
	private static final boolean compareDebug(String text, IdentifiedObject o1, IdentifiedObject o2) {
		return compareDebug(text, (AbstractIdentifiedObject)o1, (AbstractIdentifiedObject)o2);
	}
	
	private static final boolean compareDebug(String text, AbstractIdentifiedObject o1, AbstractIdentifiedObject o2) {
		return compareDebug(text, o1.equals(o2, false), o1, o2);
	}

	private static final boolean compareDebug(String text, boolean result, Object o1, Object o2) {
		System.out.println(text + ": " + result + "("+o1+", "+o2+")");
		return result;
	}
	
	private void findCrsAndMathTransform(CoordinateReferenceSystem coordinateReferenceSystem, Component parent) throws FactoryException, UserCancelException, ShpMathTransformException {
		crs = coordinateReferenceSystem;
		try {
			transform = CRS.findMathTransform(crs, wgs84);
		} catch (OperationNotFoundException e) {
			System.out.println(crs.getName()+": "+e.getMessage()); // Bursa wolf parameters required.
			
			List<CoordinateReferenceSystem> candidates = new ArrayList<CoordinateReferenceSystem>();
			
			// Find matching CRS with Bursa Wolf parameters in EPSG database
			for (String code : CRS.getAuthorityFactory(false).getAuthorityCodes(ProjectedCRS.class)) {
				CoordinateReferenceSystem candidate = CRS.decode(code);
				if (candidate instanceof AbstractCRS && crs instanceof AbstractIdentifiedObject) {
					
					Hints.putSystemDefault(Hints.COMPARISON_TOLERANCE, 
							Main.pref.getDouble(PREF_CRS_COMPARISON_TOLERANCE, DEFAULT_CRS_COMPARISON_TOLERANCE));
					if (((AbstractCRS)candidate).equals((AbstractIdentifiedObject)crs, false)) {
						System.out.println("Found a potential CRS: "+candidate.getName());
						candidates.add(candidate);
					} else if (Main.pref.getBoolean(PREF_CRS_COMPARISON_DEBUG, false)) {
						compareDebug(crs, candidate);
					}
					Hints.removeSystemDefault(Hints.COMPARISON_TOLERANCE);
				}
			}
			
			if (candidates.size() > 1) {
				System.err.println("Found several potential CRS.");//TODO: ask user which one to use
			}
			
			if (candidates.size() > 0) {
				CoordinateReferenceSystem newCRS = candidates.get(0);
				try {
					transform = CRS.findMathTransform(newCRS, wgs84, false);
				} catch (OperationNotFoundException ex) {
					System.err.println(newCRS.getName()+": "+e.getMessage());
				}
			}
			
			if (transform == null) {
				if (handler != null) {
					// ask handler if it can provide a math transform
					transform = handler.findMathTransform(crs, wgs84, false);
				}
				if (transform == null) {
					// ask user before trying lenient method
					if (warnLenientMethod(parent, crs)) {
						// User canceled
						throw new UserCancelException();
					}
					System.out.println("Searching for a lenient math transform.");
					transform = CRS.findMathTransform(crs, wgs84, true);
				}
			}
		}
		if (transform == null) {
			throw new ShpMathTransformException("Unable to find math transform !");
		}
	}
	
	private void parseFeature(Feature feature, Component parent) 
			throws UserCancelException, ShpMathTransformException, FactoryException, ShpCrsException, MismatchedDimensionException, TransformException {
		featurePrimitives.clear();
		GeometryAttribute geometry = feature.getDefaultGeometryProperty();
		if (geometry != null) {

			GeometryDescriptor desc = geometry.getDescriptor();
			
			if (crs == null && desc != null && desc.getCoordinateReferenceSystem() != null) {
				findCrsAndMathTransform(desc.getCoordinateReferenceSystem(), parent);
			} else if (crs == null) {
				throw new ShpCrsException("Unable to detect CRS !");
			}
			
			OsmPrimitive primitive = null;
			
			if (geometry.getValue() instanceof Point) {
				primitive = createOrGetNode((Point) geometry.getValue());
				
			} else if (geometry.getValue() instanceof GeometryCollection) { // Deals with both MultiLineString and MultiPolygon
				GeometryCollection mp = (GeometryCollection) geometry.getValue();
				int nGeometries = mp.getNumGeometries(); 
				if (nGeometries < 1) {
					System.err.println("Error: empty geometry collection found");
				} else {
					Relation r = null;
					Way w = null;
					
					for (int i=0; i<nGeometries; i++) {
						Geometry g = mp.getGeometryN(i);
						if (g instanceof Polygon) {
							Polygon p = (Polygon) g;
							// Do not create relation if there's only one polygon without interior ring
							// except if handler prefers it
							if (r == null && (nGeometries > 1 || p.getNumInteriorRing() > 0 || (handler != null && handler.preferMultipolygonToSimpleWay()))) {
								r = createMultipolygon();
							}
							w = createWay(p.getExteriorRing());
							if (r != null) {
								addWayToMp(r, "outer", w);
								for (int j=0; j<p.getNumInteriorRing(); j++) {
									addWayToMp(r, "inner", createWay(p.getInteriorRingN(j)));
								}
							}
						} else if (g instanceof LineString) {
							w = createWay((LineString) g);
						} else if (g instanceof Point) {
							// Some belgian data sets hold points into collections ?!
							readNonGeometricAttributes(feature, createOrGetNode((Point) g));
						} else {
							System.err.println("Error: unsupported geometry : "+g);
						}
					}
					primitive = r != null ? r : w;
				}
			} else {
				// Debug unknown geometry
				System.out.println("\ttype: "+geometry.getType());
				System.out.println("\tbounds: "+geometry.getBounds());
				System.out.println("\tdescriptor: "+desc);
				System.out.println("\tname: "+geometry.getName());
				System.out.println("\tvalue: "+geometry.getValue());
				System.out.println("\tid: "+geometry.getIdentifier());
				System.out.println("-------------------------------------------------------------");
			}
			
			if (primitive != null) {
				// Read primitive non geometric attributes
				readNonGeometricAttributes(feature, primitive);
			}
		}
	}

	public DataSet parse(File file, ProgressMonitor instance) throws IOException {
		crs = null;
		transform = null;
		try {
			if (file != null) { 
		        Map params = new HashMap();
		        params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
		        if (handler != null && handler.getDbfCharset() != null) {
		        	params.put(ShapefileDataStoreFactory.DBFCHARSET.key, handler.getDbfCharset());
		        }
				DataStore dataStore = new ShapefileDataStoreFactory().createDataStore(params);//FIXME
				if (dataStore == null) {
					throw new IOException(tr("Unable to find a data store for file {0}", file.getName()));
				}
				
				String[] typeNames = dataStore.getTypeNames();
				String typeName = typeNames[0];
	
				FeatureSource<?,?> featureSource = dataStore.getFeatureSource(typeName);
				FeatureCollection<?,?> collection = featureSource.getFeatures();
				FeatureIterator<?> iterator = collection.features();
				
				if (instance != null) {
					instance.beginTask(tr("Loading shapefile ({0} features)", collection.size()), collection.size());
				}
				
				int n = 0;
				
				Component parent = instance != null ? instance.getWindowParent() : Main.parent;
				
				try {
					while (iterator.hasNext()) {
						n++;
						try {
							Object feature = iterator.next();
							parseFeature(iterator.next(), parent);
							if (handler != null) {
								handler.notifyFeatureParsed(feature, ds, featurePrimitives);
							}
						} catch (UserCancelException e) {
							return ds;
						}
						if (instance != null) {
							instance.worked(1);
							instance.setCustomText(n+"/"+collection.size());
						}
					}
				} finally {
					iterator.close();
					nodes.clear();
					if (instance != null) {
						instance.setCustomText(null);
					}
				}
			}
		} catch (IOException e) {
			throw e;
		} catch (Throwable t) {
			throw new IOException(t);
		}
		return ds;
	}
	
    /**
     * returns true if the user wants to cancel, false if they
     * want to continue
     */
    private static final boolean warnLenientMethod(Component parent, CoordinateReferenceSystem crs) {
        ExtendedDialog dlg = new ExtendedDialog(parent,
                tr("Cannot transform to WGS84"),
                new String[] {tr("Cancel"), tr("Continue")});
        dlg.setContent("<html>" +
                tr("JOSM was unable to find a strict mathematical transformation between ''{0}'' and WGS84.<br /><br />"+
                        "Do you want to try a <i>lenient</i> method, which will perform a non-precise transformation (<b>with location errors up to 1 km</b>) ?<br/><br/>"+
                        "If so, <b>do NOT upload</b> such data to OSM !", crs.getName())+
                "</html>");
        dlg.setButtonIcons(new Icon[] {
                ImageProvider.get("cancel"),
                ImageProvider.overlay(
                        ImageProvider.get("ok"),
                        new ImageIcon(ImageProvider.get("warning-small").getImage().getScaledInstance(10 , 10, Image.SCALE_SMOOTH)),
                        ImageProvider.OverlayPosition.SOUTHEAST)});
        dlg.setToolTipTexts(new String[] {
                tr("Cancel"),
                tr("Try lenient method")});
        dlg.setIcon(JOptionPane.WARNING_MESSAGE);
        dlg.setCancelButton(1);
        return dlg.showDialog().getValue() != 2;
    }
	
	private static final void readNonGeometricAttributes(Feature feature, OsmPrimitive primitive) {
		for (Property prop : feature.getProperties()) {
			if (!(prop instanceof GeometryAttribute)) {
				Name name = prop.getName();
				Object value = prop.getValue();
				if (name != null && value != null) {
					String sName = name.toString();
					String sValue = value.toString();
					if (!sName.isEmpty() && !sValue.isEmpty()) {
						primitive.put(sName, sValue);
					}
				}
			}
		}
	}
	
	private Node getNode(Point p, String key) {
		Node n = nodes.get(key);
		if (n == null && handler != null && handler.checkNodeProximity()) {
			LatLon ll = new LatLon(p.getY(), p.getX());
			for (Node node : nodes.values()) {
				if (node.getCoor().equalsEpsilon(ll)) {
					return node;
				}
			}
		}
		return n;
	}
	
	private Node createOrGetNode(Point p) throws MismatchedDimensionException, TransformException {
		Point p2 = (Point) JTS.transform(p, transform);
		String key = p2.getX()+"/"+p2.getY();
		//String key = LatLon.roundToOsmPrecisionStrict(p2.getX())+"/"+LatLon.roundToOsmPrecisionStrict(p2.getY());
		Node n = getNode(p2, key);
		if (n == null) {
			n = new Node(new LatLon(p2.getY(), p2.getX()));
			if (handler == null || handler.useNodeMap()) {
				nodes.put(key, n);
			}
			ds.addPrimitive(n);
		} else if (n.getDataSet() == null) {
		    // ShpHandler may have removed the node from DataSet (see Paris public light handler for example)
		    ds.addPrimitive(n);
		}
		featurePrimitives.add(n);
		return n;
	}
	
	private Way createWay(LineString ls) {
		Way w = new Way();
		if (ls != null) {
			for (int i=0; i<ls.getNumPoints(); i++) {
				try {
					w.addNode(createOrGetNode(ls.getPointN(i)));
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
		return addOsmPrimitive(w);
	}

	private Relation createMultipolygon() {
		Relation r = new Relation();
		r.put("type", "multipolygon");
		return addOsmPrimitive(r);
	}

	private void addWayToMp(Relation r, String role, Way w) {
		//result.addPrimitive(w);
		r.addMember(new RelationMember(role, w));
	}
	
	private <T extends OsmPrimitive> T addOsmPrimitive(T p) {
		ds.addPrimitive(p);
		featurePrimitives.add(p);
		return p;
	}
}
