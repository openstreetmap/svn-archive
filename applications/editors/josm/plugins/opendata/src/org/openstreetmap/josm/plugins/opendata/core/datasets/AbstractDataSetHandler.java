// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.preferences.SourceEditor.ExtendedSourceEntry;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.archive.DefaultArchiveHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.archive.ArchiveHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.DefaultGmlHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.DefaultShpHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.GmlHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.ShpHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.CsvHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.DefaultCsvHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.SpreadSheetHandler;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.plugins.opendata.core.util.NamesFrUtils;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.tools.Pair;

public abstract class AbstractDataSetHandler {
	
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
	private ImageIcon menuIcon;

	public AbstractDataSetHandler() {
		setShpHandler(new DefaultShpHandler());
		setArchiveHandler(new DefaultArchiveHandler());
		setCsvHandler(new DefaultCsvHandler());
		setGmlHandler(new DefaultGmlHandler());
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
		return acceptsFilename(filename, expected, OdConstants.CSV_EXT);
	}

	protected final boolean acceptsXlsFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.XLS_EXT);
	}

	protected final boolean acceptsOdsFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.ODS_EXT);
	}

	protected final boolean acceptsShpFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.SHP_EXT);
	}

	protected final boolean acceptsMifFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.MIF_EXT);
	}

	protected final boolean acceptsMifTabFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.MIF_EXT, OdConstants.TAB_EXT);
	}

	protected final boolean acceptsShpMifFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.SHP_EXT, OdConstants.MIF_EXT);
	}

	protected final boolean acceptsKmlFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.KML_EXT);
	}

	protected final boolean acceptsKmzFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.KMZ_EXT);
	}

	protected final boolean acceptsKmzShpFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.KMZ_EXT, OdConstants.SHP_EXT);
	}

	protected final boolean acceptsKmzTabFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.KMZ_EXT, OdConstants.TAB_EXT);
	}

	protected final boolean acceptsZipFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.ZIP_EXT);
	}

    protected final boolean accepts7ZipFilename(String filename, String ... expected) {
        return acceptsFilename(filename, expected, OdConstants.SEVENZIP_EXT);
    }

	protected final boolean acceptsCsvKmzFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.CSV_EXT, OdConstants.KMZ_EXT);
	}

	protected final boolean acceptsCsvKmzTabFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.CSV_EXT, OdConstants.KMZ_EXT, OdConstants.TAB_EXT);
	}
		
	protected final boolean acceptsCsvXlsFilename(String filename, String ... expected) {
		return acceptsFilename(filename, expected, OdConstants.CSV_EXT, OdConstants.XLS_EXT);
	}
	
	// -------------------- License --------------------
	
	private License license;
	
	public License getLicense() {
		return license;
	}

	public final void setLicense(License license) {
		this.license = license;
	}

	// --------------------- URLs ---------------------
	
	private URL dataURL;
	private URL wikiURL;
	private URL localPortalURL;
	private URL nationalPortalURL;
	
	public URL getDataURL() {
		return dataURL;
	}

	public final void setDataURL(URL dataURL) {
		this.dataURL = dataURL;
	}

	public final void setDataURL(String dataURL) throws MalformedURLException {
		setDataURL(new URL(dataURL));
	}

	public URL getWikiURL() {
		return wikiURL;
	}

	public final void setWikiURL(URL wikiURL) {
		this.wikiURL = wikiURL;
	}

	public final void setWikiURL(String wikiURL) throws MalformedURLException {
		setWikiURL(new URL(wikiURL));
	}

	public URL getLocalPortalURL() {
		return localPortalURL;
	}

	public final void setLocalPortalURL(URL localPortalURL) {
		this.localPortalURL = localPortalURL;
	}

	public final void setLocalPortalURL(String localPortalURL) throws MalformedURLException {
		setLocalPortalURL(new URL(localPortalURL));
	}

	public URL getNationalPortalURL() {
		return nationalPortalURL;
	}

	public final void setNationalPortalURL(URL nationalPortalURL) {
		this.nationalPortalURL = nationalPortalURL;
	}

	public final void setNationalPortalURL(String nationalPortalURL) throws MalformedURLException {
		setNationalPortalURL(new URL(nationalPortalURL));
	}

	public List<Pair<String,URL>> getDataURLs() {return null;}

	public AbstractReader getReaderForUrl(String url) {return null;}
	
	private boolean hasLicenseToBeAccepted = true;

	public final boolean hasLicenseToBeAccepted() {
		return hasLicenseToBeAccepted;
	}

	public final void setHasLicenseToBeAccepted(boolean hasLicenseToBeAccepted) {
		this.hasLicenseToBeAccepted = hasLicenseToBeAccepted;
	}

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


	protected String getOverpassApiRequest(String bbox) {return null;}

	public boolean equals(IPrimitive p1, IPrimitive p2) {return false;}
	
	public boolean isRelevant(IPrimitive p) {return false;}
	
	public final Collection<IPrimitive> extractRelevantPrimitives(DataSet ds) {
		ArrayList<IPrimitive> result = new ArrayList<>();
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
		return OdConstants.ICON_CORE_24;
	}
		
	public String getNationalPortalIconName() {
		return OdConstants.ICON_CORE_24;
	}
		
	public String getDataLayerIconName() {
		return OdConstants.ICON_CORE_16;
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
		return new ExtendedSourceEntry(displayName,	OdConstants.PROTO_RSRC+//"/"+
				this.getClass().getPackage().getName().replace(".", "/")+"/"+
				fileNameWithoutExtension+"."+OdConstants.MAPCSS_EXT);
	}
	
    public final ImageIcon getMenuIcon() {
        return menuIcon;
    }

    public final void setMenuIcon(ImageIcon icon) {
        this.menuIcon = icon;
    }

    public final void setMenuIcon(String iconName) {
        setMenuIcon(OdUtils.getImageIcon(iconName));
    }

	public final void setAssociatedFile(File associatedFile) {
		this.associatedFile = associatedFile;
	}

	public final File getAssociatedFile() {
		return this.associatedFile;
	}
	
	public boolean acceptsUrl(String url) {
	    URL dataURL = getDataURL();
		if (dataURL != null && url.equals(dataURL.toString())) {
			return true;
		}
		List<Pair<String, URL>> dataURLs = getDataURLs();
		if (dataURLs != null) {
			for (Pair<String, URL> pair : dataURLs) {
				if (pair.b != null && url.equals(pair.b.toString())) {
					return true;
				}
			}
		}
		return false;
	}
	
	// --------- Shapefile handling ---------
	
	private ShpHandler shpHandler;

	public final void setShpHandler(ShpHandler handler) {
		shpHandler = handler;
	}
	
	public final ShpHandler getShpHandler() {
		return shpHandler;
	}

    // --------- MIF handling ---------
    
    private MifHandler mifHandler;

    public final void setMifHandler(MifHandler handler) {
        mifHandler = handler;
    }
    
    public final MifHandler getMifHandler() {
        return mifHandler;
    }

	// --------- GML handling ---------
	
	private GmlHandler gmlHandler;

	public final void setGmlHandler(GmlHandler handler) {
		gmlHandler = handler;
	}
	
	public final GmlHandler getGmlHandler() {
		return gmlHandler;
	}

	// ------------ Archive handling ------------
	
	private ArchiveHandler archiveHandler;

	public final void setArchiveHandler(ArchiveHandler handler) {
		archiveHandler = handler;
	}
	
	public ArchiveHandler getArchiveHandler() {
		return archiveHandler;
	}
	
	// ------ Spreadsheet handling ----------

	private SpreadSheetHandler ssHandler;

	public final void setSpreadSheetHandler(SpreadSheetHandler handler) {
		ssHandler = handler;
	}
	
	public final SpreadSheetHandler getSpreadSheetHandler() {
		return ssHandler;
	}

	public final void setCsvHandler(CsvHandler handler) {
		setSpreadSheetHandler(handler);
	}
	
	public final CsvHandler getCsvHandler() {
		if (ssHandler instanceof CsvHandler) {
			return (CsvHandler) ssHandler;
		} else {
			return null;
		}
	}
	
	// Tools

	private final Collection<JosmAction> tools = new ArrayList<>();
	
	public final Collection<JosmAction> getTools() {
	    return tools;
	}
	
	public final boolean addTool(JosmAction tool) {
        return tool != null ? tools.add(tool) : false;
	}
	
	public final boolean removeTool(JosmAction tool) {
	    return tool != null ? tools.remove(tool) : false;
	}

    public void notifyActive() {
        // To be overriden when the handler has specific treatments to perform when its layer becomes active
    }
}
