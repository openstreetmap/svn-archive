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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.AbstractCRS;
import org.geotools.referencing.crs.AbstractDerivedCRS;
import org.geotools.referencing.crs.AbstractSingleCRS;
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
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.gui.DialogPrompter;
import org.openstreetmap.josm.tools.ImageProvider;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public abstract class GeographicReader extends AbstractReader implements OdConstants {

	protected static CoordinateReferenceSystem wgs84;
	static {
		try {
			wgs84 = CRS.decode("EPSG:4326");
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}
	}
	
	private final GeographicHandler handler;
	private final GeographicHandler[] defaultHandlers;
	
	protected final Map<LatLon, Node> nodes;

	protected CoordinateReferenceSystem crs;
	protected MathTransform transform;

	public GeographicReader(GeographicHandler handler, GeographicHandler[] defaultHandlers) {
		this.nodes = new HashMap<LatLon, Node>();
		this.handler = handler;
		this.defaultHandlers = defaultHandlers;
	}
	
	protected Node getNode(Point p, LatLon key) {
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

	protected Node createOrGetNode(Point p) throws MismatchedDimensionException, TransformException {
		return createOrGetNode(p, null);
	}

	protected Node createOrGetNode(Point p, String ele) throws MismatchedDimensionException, TransformException {
		Point p2 = (Point) JTS.transform(p, transform);
		LatLon key = new LatLon(p2.getY(), p2.getX());
		Node n = getNode(p2, key);
		if (n == null) {
			n = new Node(key);
			if (ele != null) {
				n.put("ele", ele);
			}
			if (handler == null || handler.useNodeMap()) {
				nodes.put(key, n);
			}
			ds.addPrimitive(n);
		} else if (n.getDataSet() == null) {
		    // handler may have removed the node from DataSet (see Paris public light handler for example)
		    ds.addPrimitive(n);
		}
		return n;
	}
	
	protected <T extends OsmPrimitive> T addOsmPrimitive(T p) {
		ds.addPrimitive(p);
		return p;
	}

	protected final Way createWay() {
		return addOsmPrimitive(new Way());
	}

	protected final Way createWay(LineString ls) {
		Way w = createWay();
		if (ls != null) {
			for (int i=0; i<ls.getNumPoints(); i++) {
				try {
					w.addNode(createOrGetNode(ls.getPointN(i)));
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
		return w;
	}

	protected final Relation createMultipolygon() {
		Relation r = new Relation();
		r.put("type", "multipolygon");
		return addOsmPrimitive(r);
	}

	protected final void addWayToMp(Relation r, String role, Way w) {
		r.addMember(new RelationMember(role, w));
	}
	
    /**
     * returns true if the user wants to cancel, false if they
     * want to continue
     */
    protected static final boolean warnLenientMethod(final Component parent, final CoordinateReferenceSystem crs) {
    	return new DialogPrompter() {
			@Override
			protected ExtendedDialog buildDialog() {
		        final ExtendedDialog dlg = new ExtendedDialog(parent,
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
		        return dlg;
			}
		}.promptInEdt().getValue() != 2;
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
	
	protected void findMathTransform(Component parent, boolean findSimiliarCrs) throws FactoryException, UserCancelException, GeoMathTransformException {
		try {
			transform = CRS.findMathTransform(crs, wgs84);
		} catch (OperationNotFoundException e) {
			System.out.println(crs.getName()+": "+e.getMessage()); // Bursa wolf parameters required.
			
			if (findSimiliarCrs) { 
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
			}
			
			if (transform == null) {
				if (handler != null) {
					// ask handler if it can provide a math transform
					try {
						transform = handler.findMathTransform(crs, wgs84, false);
					} catch (OperationNotFoundException ex) {
						System.out.println(crs.getName()+": "+ex.getMessage()); // Bursa wolf parameters required.
					}
				} else {
					// ask default known handlers
					for (GeographicHandler geoHandler : defaultHandlers) {
						try {
							if ((transform = geoHandler.findMathTransform(crs, wgs84, false)) != null) {
								break;
							}
						} catch (OperationNotFoundException ex) {
							System.out.println(crs.getName()+": "+ex.getMessage()); // Bursa wolf parameters required.
						}
					}
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
			throw new GeoMathTransformException("Unable to find math transform !");
		}
	}
}
