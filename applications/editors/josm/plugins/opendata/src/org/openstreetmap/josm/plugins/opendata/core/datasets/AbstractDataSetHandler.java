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
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.AbstractDerivedCRS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.operation.projection.LambertConformal;
import org.geotools.referencing.operation.projection.LambertConformal1SP;
import org.geotools.referencing.operation.projection.LambertConformal2SP;
import org.geotools.referencing.operation.projection.MapProjection.AbstractProvider;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.MathTransform;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.projection.AbstractProjection;
import org.openstreetmap.josm.data.projection.Ellipsoid;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.data.projection.proj.LambertConformalConic;
import org.openstreetmap.josm.data.projection.proj.LambertConformalConic.Parameters;
import org.openstreetmap.josm.data.projection.proj.LambertConformalConic.Parameters1SP;
import org.openstreetmap.josm.data.projection.proj.LambertConformalConic.Parameters2SP;
import org.openstreetmap.josm.gui.preferences.SourceEditor.ExtendedSourceEntry;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.util.NamesFrUtils;
import org.openstreetmap.josm.tools.Pair;

public abstract class AbstractDataSetHandler implements OdConstants {
	
	public abstract boolean acceptsFilename(String filename);
	
	public boolean acceptsFile(File file) {
		return acceptsFilename(file.getName());
	}
	
	public abstract void updateDataSet(DataSet ds);

	public void checkDataSetSource(DataSet ds) {
		if (ds != null) {
			for (OsmPrimitive p : ds.allPrimitives()) {
				if (p.hasKeys() || p.getReferrers().isEmpty()) {
					if (getSource() != null && p.get("source") == null) {
						p.put("source", getSource());
					}
					if (sourceDate != null && p.get("source:date") == null) {
						p.put("source:date", sourceDate);
					}
				}
			}
		}
	}
	
	public void checkNames(DataSet ds) {
		if (ds != null) {
			for (OsmPrimitive p : ds.allPrimitives()) {
				if (p.get("name") != null) {
					p.put("name", NamesFrUtils.checkDictionary(p.get("name")));
				}
			}
		}
	}

	private String name;
	private DataSetCategory category;
	private String sourceDate;
	private File associatedFile;

	public AbstractDataSetHandler() {
	}
	
	private final boolean acceptsFilename(String filename, String[] expected, String ... extensions ) {
		if (filename != null) {
			for (String name : expected) {
				for (String ext : extensions) {
					if (Pattern.compile(name+"\\."+ext, Pattern.CASE_INSENSITIVE).matcher(filename).matches()) {
					//if (filename.equalsIgnoreCase(name+"."+ext)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected final boolean acceptsCsvFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, CSV_EXT);
	}

	protected final boolean acceptsXlsFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, XLS_EXT);
	}

	protected final boolean acceptsOdsFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, ODS_EXT);
	}

	protected final boolean acceptsShpFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, SHP_EXT);
	}

	protected final boolean acceptsMifFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, MIF_EXT);
	}

	protected final boolean acceptsMifTabFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, MIF_EXT, TAB_EXT);
	}

	protected final boolean acceptsShpMifFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, SHP_EXT, MIF_EXT);
	}

	protected final boolean acceptsKmlFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, KML_EXT);
	}

	protected final boolean acceptsKmzFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, KMZ_EXT);
	}

	protected final boolean acceptsKmzTabFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, KMZ_EXT, TAB_EXT);
	}

	protected final boolean acceptsZipFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, ZIP_EXT);
	}

	protected final boolean acceptsCsvKmzFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, CSV_EXT, KMZ_EXT);
	}

	protected final boolean acceptsCsvKmzTabFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, CSV_EXT, KMZ_EXT, TAB_EXT);
	}
	
	protected final boolean acceptsCsvXlsFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, CSV_EXT, XLS_EXT);
	}
	
	public URL getWikiURL() {return null;}
	
	public URL getLocalPortalURL() {return null;}
	
	public URL getNationalPortalURL() {return null;}

	public URL getLicenseURL() {return null;}

	public URL getDataURL() {return null;}
	
	public AbstractReader getReaderForUrl(String url) {return null;}

	public final DataSetCategory getCategory() {
		return category;
	}

	public final void setCategory(DataSetCategory category) {
		this.category = category;
	}

	public final Collection<String> getOsmXapiRequests(Bounds bounds) {
		return getOsmXapiRequests(
				LatLon.roundToOsmPrecisionStrict(bounds.getMin().lon())+","+
				LatLon.roundToOsmPrecisionStrict(bounds.getMin().lat())+","+
				LatLon.roundToOsmPrecisionStrict(bounds.getMax().lon())+","+
				LatLon.roundToOsmPrecisionStrict(bounds.getMax().lat()));
	}
	
	protected Collection<String> getOsmXapiRequests(String bbox) {return null;}
	
	public final String getOverpassApiRequest(Bounds bounds) {
		return getOverpassApiRequest(
				"w=\""+LatLon.roundToOsmPrecisionStrict(bounds.getMin().lon())+"\" "+
				"s=\""+LatLon.roundToOsmPrecisionStrict(bounds.getMin().lat())+"\" "+
				"e=\""+LatLon.roundToOsmPrecisionStrict(bounds.getMax().lon())+"\" "+
				"n=\""+LatLon.roundToOsmPrecisionStrict(bounds.getMax().lat())+"\"");
	}

	public enum OaQueryType {
		NODE ("node"),
		WAY ("way"),
		RELATION ("relation");
		@Override
		public String toString() { return this.value; }
		private OaQueryType(final String value) { this.value = value; }
		private final String value;
	}

	public enum OaRecurseType {
		RELATION_RELATION ("relation-relation"),
		RELATION_BACKWARDS ("relation-backwards"),
		RELATION_WAY ("relation-way"),
		RELATION_NODE ("relation-node"),
		WAY_NODE ("way-node"),
		WAY_RELATION ("way-relation"),
		NODE_RELATION ("node-relation"),
		NODE_WAY ("node-way");
		@Override
		public String toString() { return this.value; }
		private OaRecurseType(final String value) { this.value = value; }
		private final String value;
	}

	protected String getOverpassApiRequest(String bbox) {return null;}
	
	protected final String oaUnion(String ... queries) {
		String result = "<union>\n";
		for (String query : queries) {
			if (query != null) {
				result += query + "\n";
			}
		}
		result += "</union>";
		return result;
	}
	
	protected final String oaQuery(String bbox, OaQueryType type, String ... conditions) {
		String result = "<query type=\""+type+"\" >\n";
		if (bbox != null) {
			result += "<bbox-query "+bbox+"/>\n";
		}
		for (String condition : conditions) {
			if (condition != null) {
				result += condition + "\n";
			}
		}
		result += "</query>";
		return result;
	}

	protected final String oaRecurse(OaRecurseType type, String into) {
		return "<recurse type=\""+type+"\" into=\""+into+"\"/>\n";
	}

	protected final String oaRecurse(OaRecurseType ... types) {
		String result = "";
		for (OaRecurseType type : types) {
			result += "<recurse type=\""+type+"\"/>\n";
		}
		return result;
	}
	
	protected final String oaPrint() {
		return "<print mode=\"meta\"/>";
	}
	
	protected final String oaHasKey(String key) {
		return oaHasKey(key, null);
	}

	protected final String oaHasKey(String key, String value) {
		return "<has-kv k=\""+key+"\" "+(value != null && !value.isEmpty() ? "v=\""+value+"\"" : "")+" />";
	}

	public boolean equals(IPrimitive p1, IPrimitive p2) {return false;}
	
	public boolean isRelevant(IPrimitive p) {return false;}
	
	public final Collection<IPrimitive> extractRelevantPrimitives(DataSet ds) {
		ArrayList<IPrimitive> result = new ArrayList<IPrimitive>();
		for (IPrimitive p : ds.allPrimitives()) {
			if (isRelevant(p)) {
				result.add(p);
			}
		}
		return result;
	}
	
	public boolean isForbidden(IPrimitive p) {return false;}
	
	public boolean hasForbiddenTags() {return false;}
	
	public interface ValueReplacer {
		public String replace(String value);
	}
	
	protected final void replace(IPrimitive p, String dataKey, String osmKey) {
		addOrReplace(p, dataKey, osmKey, null, null, null, true);
	}

	protected final void replace(IPrimitive p, String dataKey, String osmKey, ValueReplacer replacer) {
		addOrReplace(p, dataKey, osmKey, null, null, replacer, true);
	}

	protected final void replace(IPrimitive p, String dataKey, String osmKey, String[] dataValues, String[] osmValues) {
		addOrReplace(p, dataKey, osmKey, dataValues, osmValues, null, true);
	}
	
	protected final void add(IPrimitive p, String dataKey, String osmKey, ValueReplacer replacer) {
		addOrReplace(p, dataKey, osmKey, null, null, replacer, false);
	}

	protected final void add(IPrimitive p, String dataKey, String osmKey, String[] dataValues, String[] osmValues) {
		addOrReplace(p, dataKey, osmKey, dataValues, osmValues, null, false);
	}
	
	private final void addOrReplace(IPrimitive p, String dataKey, String osmKey, String[] dataValues, String[] osmValues, ValueReplacer replacer, boolean replace) {
		String value = p.get(dataKey);
		if (value != null) {
			int index = -1;
			for (int i = 0; dataValues != null && index == -1 && i < dataValues.length; i++) {
				if (Pattern.compile(dataValues[i], Pattern.CASE_INSENSITIVE).matcher(value).matches()) {
					index = i;
				}
				/*if (value.equalsIgnoreCase(csvValues[i])) {
					index = i;
				}*/
			}
			if (index > -1 && osmValues != null) {
				doAddReplace(p, dataKey, osmKey, osmValues[index], replace);
			} else if (replacer != null) {
				doAddReplace(p, dataKey, osmKey, replacer.replace(value), replace);
			} else if (dataValues == null || osmValues == null) {
				doAddReplace(p, dataKey, osmKey, value, replace);
			}
		}
	}
	
	private final void doAddReplace(IPrimitive p, String dataKey, String osmKey, String osmValue, boolean replace) {
		if (replace) {
			p.remove(dataKey);
		}
		p.put(osmKey, osmValue);
	}

	public String getSource() {
		return null;
	}
		
	public final String getSourceDate() {
		return sourceDate;
	}
	
	public final void setSourceDate(String sourceDate) {
		this.sourceDate = sourceDate;
	}

	public final String getName() {
		return name;
	}
	
	public final void setName(String name) {
		this.name = name;
	}

	public String getLocalPortalIconName() {
		return ICON_CORE_24;
	}
		
	public String getNationalPortalIconName() {
		return ICON_CORE_24;
	}
		
	public String getDataLayerIconName() {
		return ICON_CORE_16;
	}
	
	public boolean handlesSpreadSheetProjection() {
		return false;
	}
	
	public List<Projection> getSpreadSheetProjections() {
		return null;
	}

	public LatLon getSpreadSheetCoor(EastNorth en, String[] fields) {
		return null;
	}
	
	public Charset getCsvCharset() {
		return null;
	}
	
	public String getCsvSeparator() {
		return null;
	}
	
	public int getSheetNumber() {
		return -1;
	}
	
	public ExtendedSourceEntry getMapPaintStyle() {
		return null;
	}

	public ExtendedSourceEntry getTaggingPreset() {
		return null;
	}

	protected final ExtendedSourceEntry getMapPaintStyle(String displayName) {
		return getMapPaintStyle(displayName, this.getClass().getSimpleName().replace("Handler", ""));
	}

	protected final ExtendedSourceEntry getMapPaintStyle(String displayName, String fileNameWithoutExtension) {
		return new ExtendedSourceEntry(displayName,	PROTO_RSRC+//"/"+
				this.getClass().getPackage().getName().replace(".", "/")+"/"+
				fileNameWithoutExtension+"."+MAPCSS_EXT);
	}
	
	public boolean preferMultipolygonToSimpleWay() {
		return false;
	}
	
	public final void setAssociatedFile(File associatedFile) {
		this.associatedFile = associatedFile;
	}

	public final File getAssociatedFile() {
		return this.associatedFile;
	}
	
	private static final List<Pair<org.opengis.referencing.datum.Ellipsoid, Ellipsoid>> 
		ellipsoids = new ArrayList<Pair<org.opengis.referencing.datum.Ellipsoid, Ellipsoid>>();
	static {
		ellipsoids.add(new Pair<org.opengis.referencing.datum.Ellipsoid, Ellipsoid>(DefaultEllipsoid.GRS80, Ellipsoid.GRS80));
		ellipsoids.add(new Pair<org.opengis.referencing.datum.Ellipsoid, Ellipsoid>(DefaultEllipsoid.WGS84, Ellipsoid.WGS84));
	}
	
	private static final Double get(ParameterValueGroup values, ParameterDescriptor desc) {
		return (Double) values.parameter(desc.getName().getCode()).getValue();
	}
	
	private static final boolean equals(Double a, Double b) {
		boolean res = Math.abs(a - b) <= Main.pref.getDouble(PREF_CRS_COMPARISON_TOLERANCE, DEFAULT_CRS_COMPARISON_TOLERANCE);
		if (Main.pref.getBoolean(PREF_CRS_COMPARISON_DEBUG, false)) {
			System.out.println("Comparing "+a+" and "+b+" -> "+res);
		}
		return res; 
	}
	
	public MathTransform findMathTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, boolean lenient) throws FactoryException {
		if (sourceCRS instanceof AbstractDerivedCRS && sourceCRS.getName().getCode().equalsIgnoreCase("Lambert_Conformal_Conic")) {
			List<MathTransform> result = new ArrayList<MathTransform>();
			AbstractDerivedCRS crs = (AbstractDerivedCRS) sourceCRS;
			MathTransform transform = crs.getConversionFromBase().getMathTransform();
			if (transform instanceof LambertConformal && crs.getDatum() instanceof GeodeticDatum) {
				LambertConformal lambert = (LambertConformal) transform;
				GeodeticDatum geo = (GeodeticDatum) crs.getDatum();
				for (Projection p : Projections.getProjections()) {
					if (p instanceof AbstractProjection) {
						AbstractProjection ap = (AbstractProjection) p;
						if (ap.getProj() instanceof LambertConformalConic) {
							for (Pair<org.opengis.referencing.datum.Ellipsoid, Ellipsoid> pair : ellipsoids) {
								if (pair.a.equals(geo.getEllipsoid()) && pair.b.equals(ap.getEllipsoid())) {
									boolean ok = true;
									ParameterValueGroup values = lambert.getParameterValues();
									Parameters params = ((LambertConformalConic) ap.getProj()).getParameters();
									
									ok = ok ? equals(get(values, AbstractProvider.LATITUDE_OF_ORIGIN), params.latitudeOrigin) : ok;
									ok = ok ? equals(get(values, AbstractProvider.CENTRAL_MERIDIAN), ap.getCentralMeridian()) : ok;
									ok = ok ? equals(get(values, AbstractProvider.SCALE_FACTOR), ap.getScaleFactor()) : ok;
									ok = ok ? equals(get(values, AbstractProvider.FALSE_EASTING), ap.getFalseEasting()) : ok;
									ok = ok ? equals(get(values, AbstractProvider.FALSE_NORTHING), ap.getFalseNorthing()) : ok;
									
									if (lambert instanceof LambertConformal2SP && params instanceof Parameters2SP) {
										Parameters2SP param = (Parameters2SP) params;
										ok = ok ? equals(Math.min(get(values, AbstractProvider.STANDARD_PARALLEL_1),get(values, AbstractProvider.STANDARD_PARALLEL_2)), 
														 Math.min(param.standardParallel1, param.standardParallel2)) : ok;
										ok = ok ? equals(Math.max(get(values, AbstractProvider.STANDARD_PARALLEL_1), get(values, AbstractProvider.STANDARD_PARALLEL_2)),
												         Math.max(param.standardParallel1, param.standardParallel2)) : ok;
										
									} else if (!(lambert instanceof LambertConformal1SP && params instanceof Parameters1SP)) {
										ok = false;
									}

									if (ok) {
										try {
											result.add(CRS.findMathTransform(CRS.decode(p.toCode()), targetCRS, lenient));
										} catch (FactoryException e) {
											System.err.println(e.getMessage());
										}
									}
								}
							}
						}
					}
				}
			}
			if (!result.isEmpty()) {
				if (result.size() > 1) {
					System.err.println("Found multiple projections !"); // TODO: something
				}
				return result.get(0);
			}
		}
		return null;
	}

	public boolean checkShpNodeProximity() {
		return false;
	}
}
