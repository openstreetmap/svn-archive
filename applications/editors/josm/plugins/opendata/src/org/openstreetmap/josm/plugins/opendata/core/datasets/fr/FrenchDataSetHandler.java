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

import static org.openstreetmap.josm.plugins.opendata.core.io.LambertCC9ZonesProjectionPatterns.lambertCC9Zones;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.projection.Lambert;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.UTM;
import org.openstreetmap.josm.data.projection.UTM.Hemisphere;
import org.openstreetmap.josm.plugins.opendata.core.datasets.SimpleDataSetHandler;
import org.xml.sax.SAXException;

public abstract class FrenchDataSetHandler extends SimpleDataSetHandler implements FrenchConstants {

	private Projection singleProjection;

	private String nationalPortalPath;

	protected static final Projection lambert93 = PRJ_LAMBERT_93.getProjection(); // France metropolitaine
	protected static final UTM utm20 = new UTM(20, Hemisphere.North, false); // Guadeloupe, Martinique
	protected static final UTM utm22 = new UTM(22, Hemisphere.North, false); // Guyane
	protected static final UTM utm38 = new UTM(38, Hemisphere.South, false); // Mayotte
	protected static final UTM utm40 = new UTM(40, Hemisphere.South, false); // Reunion
	
	protected static final Lambert[] lambert4Zones = new Lambert[4];
	static {
		for (int i=0; i<lambert4Zones.length; i++) {
			lambert4Zones[i] = new Lambert();
			lambert4Zones[i].setPreferences(Arrays.asList(Integer.toString(i+1)));
		}
	}

	protected static final Projection[] projections = new Projection[]{
		lambert93, // France metropolitaine
		utm20, // Guadeloupe, Martinique
		utm22, // Guyane
		utm38, // Mayotte
		utm40, // Reunion
	};

	public FrenchDataSetHandler() {
		
	}

	public FrenchDataSetHandler(String relevantTag) {
		super(relevantTag);
	}

	public FrenchDataSetHandler(boolean relevantUnion, String[] relevantTags) {
		super(relevantUnion, relevantTags);
	}

	public FrenchDataSetHandler(boolean relevantUnion, Tag[] relevantTags) {
		super(relevantUnion, relevantTags);
	}
	
	protected final void setNationalPortalPath(String nationalPortalPath) {
		this.nationalPortalPath = nationalPortalPath;
	}

	protected final void setSingleProjection(Projection singleProjection) {
		this.singleProjection = singleProjection;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getNationalPortalURL()
	 */
	@Override
	public URL getNationalPortalURL() {
		try {
			if (nationalPortalPath != null && !nationalPortalPath.isEmpty()) {
				return new URL(FRENCH_PORTAL + nationalPortalPath);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getLocalPortalIconName()
	 */
	@Override
	public String getLocalPortalIconName() {
		return ICON_FR_24;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getNationalPortalIconName()
	 */
	@Override
	public String getNationalPortalIconName() {
		return ICON_FR_24;
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#handlesCsvProjection()
	 */
	@Override
	public boolean handlesSpreadSheetProjection() {
		return singleProjection != null ? true : super.handlesSpreadSheetProjection();
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getCsvProjections()
	 */
	@Override
	public List<Projection> getSpreadSheetProjections() {
		if (singleProjection != null) {
			return Arrays.asList(new Projection[]{singleProjection});
		} else {
			return Arrays.asList(projections);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getCsvCoor(org.openstreetmap.josm.data.coor.EastNorth, java.lang.String[])
	 */
	@Override
	public LatLon getSpreadSheetCoor(EastNorth en, String[] fields) {
		if (singleProjection != null) {
			return singleProjection.eastNorth2latlon(en);
		} else {
			return super.getSpreadSheetCoor(en, fields);
		}
	}
	
	protected static final LatLon getLatLonByDptCode(EastNorth en, String dpt, boolean useCC9) {
		if (dpt.equals("971") || dpt.equals("972") || dpt.equals("977") || dpt.equals("978")) {	// Antilles
			return utm20.eastNorth2latlon(en);
		} else if (dpt.equals("973")) {	// Guyane
			return utm22.eastNorth2latlon(en);
		} else if (dpt.equals("974")) {	// La Réunion
			return utm40.eastNorth2latlon(en);
		} else if (dpt.equals("976") || dpt.equals("985")) { // 985 = ancien code de Mayotte ? (présent dans geofla)
			return utm38.eastNorth2latlon(en);
		} else if (!useCC9) {
			return lambert93.eastNorth2latlon(en);
		} else if (dpt.endsWith("2A") || dpt.endsWith("2B") || dpt.endsWith("20")) {
			return lambertCC9Zones[0].eastNorth2latlon(en);
		} else if (dpt.endsWith("64") || dpt.endsWith("65") || dpt.endsWith("31") || dpt.endsWith("09") || dpt.endsWith("11") || dpt.endsWith("66") || dpt.endsWith("34") || dpt.endsWith("83")) {
			return lambertCC9Zones[1].eastNorth2latlon(en);
		} else if (dpt.endsWith("40") || dpt.endsWith("32") || dpt.endsWith("47") || dpt.endsWith("82") || dpt.endsWith("81") || dpt.endsWith("12") || dpt.endsWith("48") || dpt.endsWith("30") || dpt.endsWith("13") || dpt.endsWith("84") || dpt.endsWith("04") || dpt.endsWith("06")) {
			return lambertCC9Zones[2].eastNorth2latlon(en);
		} else if (dpt.endsWith("33") || dpt.endsWith("24") || dpt.endsWith("46") || dpt.endsWith("19") || dpt.endsWith("15") || dpt.endsWith("43") || dpt.endsWith("07") || dpt.endsWith("26") || dpt.endsWith("05") || dpt.endsWith("38") || dpt.endsWith("73")) {
			return lambertCC9Zones[3].eastNorth2latlon(en);
		} else if (dpt.endsWith("17") || dpt.endsWith("16") || dpt.endsWith("87") || dpt.endsWith("23") || dpt.endsWith("63") || dpt.endsWith("03") || dpt.endsWith("42") || dpt.endsWith("69") || dpt.endsWith("01") || dpt.endsWith("74")) {
			return lambertCC9Zones[4].eastNorth2latlon(en);
		} else if (dpt.endsWith("44") || dpt.endsWith("85") || dpt.endsWith("49") || dpt.endsWith("79") || dpt.endsWith("37") || dpt.endsWith("86") || dpt.endsWith("36") || dpt.endsWith("18") || dpt.endsWith("58") || dpt.endsWith("71") || dpt.endsWith("21") || dpt.endsWith("39") || dpt.endsWith("25")) {
			return lambertCC9Zones[5].eastNorth2latlon(en);
		} else if (dpt.endsWith("29") || dpt.endsWith("56") || dpt.endsWith("22") || dpt.endsWith("35") || dpt.endsWith("53") || dpt.endsWith("72") || dpt.endsWith("41") || dpt.endsWith("28") || dpt.endsWith("45") || dpt.endsWith("89") || dpt.endsWith("10") || dpt.endsWith("52") || dpt.endsWith("70") || dpt.endsWith("88") || dpt.endsWith("68") || dpt.endsWith("90")) {
			return lambertCC9Zones[6].eastNorth2latlon(en);
		} else if (dpt.endsWith("50") || dpt.endsWith("14") || dpt.endsWith("61") || dpt.endsWith("27") || dpt.endsWith("60") || dpt.endsWith("95") || dpt.endsWith("78") || dpt.endsWith("91") || dpt.endsWith("92") || dpt.endsWith("93") || dpt.endsWith("94") || dpt.endsWith("75") || dpt.endsWith("77") || dpt.endsWith("60") || dpt.endsWith("02") || dpt.endsWith("51") || dpt.endsWith("55") || dpt.endsWith("54") || dpt.endsWith("57") || dpt.endsWith("67")) {
			return lambertCC9Zones[7].eastNorth2latlon(en);
		} else if (dpt.endsWith("76") || dpt.endsWith("80") || dpt.endsWith("62") || dpt.endsWith("59") || dpt.endsWith("08")) {
			return lambertCC9Zones[8].eastNorth2latlon(en);
		} else {
			throw new IllegalArgumentException("Unsupported department code: "+dpt);
		}
	}

	private void replaceFaxPhone(OsmPrimitive p, String dataKey, String osmKey) {
		String phone = p.get(dataKey);
		if (phone != null) {
			p.put(osmKey, phone.replace(" ", "").replace(".", "").replaceFirst("0", "+33"));
			p.remove(dataKey);
		}
	}

	protected void replaceFax(OsmPrimitive p, String dataKey) {
		replaceFaxPhone(p, dataKey, "contact:fax");
	}
	
	protected void replacePhone(OsmPrimitive p, String dataKey) {
		replaceFaxPhone(p, dataKey, "contact:phone");
	}
	
	private static final String dayFr = "L|Lu|M|Ma|Me|J|Je|V|Ve|S|Sa|D|Di";
	private static final String dayEn = "Mo|Mo|Tu|Tu|We|Th|Th|Fr|Fr|Sa|Sa|Su|Su";
	
	private static final String[] dayFrSplit = dayFr.split("\\|");
	private static final String[] dayEnSplit = dayEn.split("\\|");
	
	protected void replaceOpeningHours(OsmPrimitive p, String dataKey) {
		String hours = p.get(dataKey);
		if (hours != null) {
			hours = hours.replace("h", ":").replace(": ", ":00 ");
			hours = hours.replace(" - ", "-").replace(" au ", "-").replace(" à ", "-");
			hours = hours.replace(" et ", ",").replace("/", ",");
			hours = hours.replace(" (sf vacances)", "; PH off");
			if (hours.endsWith(":")) {
				hours += "00";
			}
			String dayGroup = "("+dayFr+")";
			String dayNcGroup = "(?:"+dayFr+")";
			// FIXME: this doesn't work yet
			for (String sep : new String[]{"-",","}) {
				boolean finished = false;
				while (!finished) {
					Matcher m = Pattern.compile(".*("+dayNcGroup+"(?:"+sep+dayNcGroup+")+).*").matcher(hours);
					if (m.matches()) {
						String range = m.group(1);
						Matcher m2 = Pattern.compile(dayGroup+"(?:"+sep+dayGroup+")+").matcher(range);
						if (m2.matches()) {
							String replacement = "";
							for (int i=0; i<m2.groupCount(); i++) {
								if (i > 0) {
									replacement += sep;
								}
								replacement += getEnDay(m2.group(i+1));
							}
							hours = hours.replace(range, replacement);
						}
					} else {
						finished = true;
					}
				}
			}
			p.put("opening_hours", hours);
			p.remove(dataKey);
		}
	}
	
	private String getEnDay(String frDay) {
		for (int i=0; i<dayFrSplit.length; i++) {
			if (dayFrSplit[i].equals(frDay)) {
				return dayEnSplit[i];
			}
		}
		return "";
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#findMathTransform(org.opengis.referencing.crs.CoordinateReferenceSystem, org.opengis.referencing.crs.CoordinateReferenceSystem, boolean)
	 */
	@Override
	public MathTransform findMathTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, boolean lenient)
			throws FactoryException {
		if (sourceCRS.getName().getCode().equalsIgnoreCase("RGM04")) {
			return CRS.findMathTransform(CRS.decode("EPSG:4471"), targetCRS, lenient);
		} else if (sourceCRS.getName().getCode().equalsIgnoreCase("RGFG95_UTM_Zone_22N")) {
			return CRS.findMathTransform(CRS.decode("EPSG:2972"), targetCRS, lenient);
		} else {
			return super.findMathTransform(sourceCRS, targetCRS, lenient);
		}
	}
	
	protected URL getNeptuneSchema() {
		return FrenchDataSetHandler.class.getResource(NEPTUNE_XSD);
	}

	public final boolean acceptsXmlNeptuneFile(File file) {
		
		Source xmlFile = new StreamSource(file);
		
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(getNeptuneSchema());
			Validator validator = schema.newValidator();
			validator.validate(xmlFile);
			System.out.println(xmlFile.getSystemId() + " is valid");
			return true;
		} catch (SAXException e) {
			System.out.println(xmlFile.getSystemId() + " is NOT valid");
			System.out.println("Reason: " + e.getLocalizedMessage());
		} catch (IOException e) {
			System.out.println(xmlFile.getSystemId() + " is NOT valid");
			System.out.println("Reason: " + e.getLocalizedMessage());
		}
		
		return false;
	}
}
