package org.openstreetmap.josm.plugins.osminspector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ProgressMonitor;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Filter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.osminspector.gui.OsmInspectorDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

@SuppressWarnings({ "deprecation"})
public class OsmInspectorLayer extends Layer {

	private StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
	private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
	private StreamingRenderer renderer;
	private CoordinateReferenceSystem crsOSMI;
	private GeomType geometryType;
	private String geometryAttributeName;

	private SimpleFeatureSource featureSource;
	private MapContext context;
	private boolean bIsChanged;

	private int layerOffset = 1;

	private ArrayList<GeomType> selectGeomType;
	private Color[] featureFills = { new Color(255, 0, 0),
			new Color(0, 0, 255), // duplicate ways
			new Color(204, 204, 0), // minor 5
			new Color(255, 230, 128), // minor 2
			new Color(255, 204, 0), // minor 1
			new Color(255, 102, 102), // major 5
			new Color(255, 148, 77), // major 2
			new Color(255, 0, 0), // major 1
			new Color(255, 255, 64), // selected
			new Color(255, 0, 0), new Color(255, 0, 0) };

	/**
	 * dialog showing the bug info
	 */
	private OsmInspectorDialog dialog;

	/**
	 * supported actions
	 */

	// Container for bugs from Osmi
	private ArrayList<OSMIFeatureTracker> arrFeatures;
	private LinkedHashMap<BugInfo, Long> osmiBugInfo;

	public Geometry getOsmBugGeometry(int index) {
		BugInfo[] array = new BugInfo[osmiBugInfo.keySet().size()];
		array = osmiBugInfo.keySet().toArray(array);
		return array[index].getGeom();
	}

	public Map<BugInfo, Long> getOsmiBugInfo() {
		return osmiBugInfo;
	}

	public SimpleFeatureSource getFeatureSource() {
		return featureSource;
	}

	public void setFeatureSource(SimpleFeatureSource featureSource) {
		this.featureSource = featureSource;
	}

	public boolean isbIsChanged() {
		return bIsChanged;
	}

	public void setbIsChanged(boolean bIsChanged) {
		this.bIsChanged = bIsChanged;
	}

	public ArrayList<OSMIFeatureTracker> getArrFeatures() {
		return arrFeatures;
	}

	public void setArrFeatures(ArrayList<OSMIFeatureTracker> arrFeatures) {
		this.arrFeatures = arrFeatures;
	}

	public BugIndex getOsmiIndex() {
		return osmiIndex;
	}

	// Pointer to prev and next osmi bugs
	private BugIndex osmiIndex;

	/**
	 * 
	 * The Bug attribute class: hold geom, id and description for that bug
	 * 
	 * @author snikhil
	 * 
	 */
	public class BugInfo implements Comparable<BugInfo>{

		public Geometry getGeom() {
			return geom;
		}

		public String getDesc() {
			return desc;
		}

		public String getId() {
			return id;
		}

		@Override
		public int hashCode() {
			String fid = attributes.get("FID");
			String hash =  (fid == null || fid.isEmpty()) ? attributes.get("problem_id") : fid;
			return hash.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			String fid = attributes.get("FID");
			String hash =  (fid == null || fid.isEmpty()) ? attributes.get("problem_id") : fid;
			
			
			if (obj instanceof BugInfo) {
				BugInfo b = (BugInfo) obj;
				
				String bfid = b.attributes.get("FID");
				String bhash =  (bfid == null || bfid.isEmpty()) ? b.attributes.get("problem_id") : bfid;
				return hash.equals(bhash);
			}
			return false;
		}

		// private final long bugId; //incremental bugId
		private final long bugId;
		private final Geometry geom;
		private final String desc;
		private final String id;
		private final Name name;
		private final Map<String, String> attributes;

		public BugInfo(SimpleFeature next, long idx)
				throws IndexOutOfBoundsException, ParseException {

			bugId = idx;
			attributes = new HashMap<String, String>();
			Collection<Property> properties = next.getProperties();
			Iterator<Property> it = properties.iterator();
			while (it.hasNext()) {
				Property p = it.next();
				attributes.put(p.getName().toString(), p.getValue().toString());
			}
			this.geom = (Geometry) next.getAttribute(0);
			this.desc = (String) next.getAttribute("error_desc");
			this.id = next.getID();
			name = next.getName();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("BugId_").append(String.valueOf(bugId)).append("\n");
			return sb.toString();
		}

		public String getContentString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Layer:").append(name.getLocalPart()).append("\n");
			Iterator<Entry<String, String>> it = attributes.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, String> next = it.next();
				sb.append(next.getKey()).append(":").append(next.getValue())
						.append("\n");
			}
			return sb.toString();
		}

		public long getBugId() {
			return bugId;
		}

		@Override
		public int compareTo(BugInfo o) {
			String fid = attributes.get("FID");
			String hash =  (fid == null || fid.isEmpty()) ? attributes.get("problem_id") : fid;
			
			String ofid = o.attributes.get("FID");
			String ohash =  (ofid == null || ofid.isEmpty()) ? o.attributes.get("problem_id") : ofid;
			return hash.compareTo(ohash);
		}
	}

	/**
	 * Helper class that stores the bug next and prev pointers and can navigate
	 * the entire bug list
	 * 
	 * @author snikhil
	 * 
	 */
	public class BugIndex {
		private int nextIndex;
		private int previousIndex;
		private ArrayList<BugInfo> osmBugs;

		public BugIndex(Map<BugInfo, Long> bugs) {
			osmBugs = new ArrayList<BugInfo>(bugs.keySet());
			nextIndex = 0;
			previousIndex = -1;
		}

		public BugIndex(Map<BugInfo, Long> bugs, int n, int p) {
			osmBugs = new ArrayList<BugInfo>(bugs.keySet());
			nextIndex = n;
			previousIndex = p;
		}

		public void next() {
			previousIndex = nextIndex;
			nextIndex = ++nextIndex % osmBugs.size();
		}

		public void prev() {
			nextIndex = previousIndex;
			previousIndex = previousIndex - 1 < 0 ? osmBugs.size() - 1
					: --previousIndex;
		}

		public BugInfo getItemPointedByNext() {
			return osmBugs.get(nextIndex);
		}

		public BugInfo getItemPointedByPrev() {
			return osmBugs.get(nextIndex);
		}

		public int indexOf(BugInfo b) {
			return osmBugs.indexOf(b);
		}

		public BugInfo getNext() {
			next();
			return osmBugs.get(nextIndex);
		}

		public BugInfo getPrev() {
			prev();
			return osmBugs.get(nextIndex);
		}

		public ArrayList<BugInfo> getBugs() {
			return osmBugs;
		}

		public void append(LinkedHashMap<BugInfo, Long> osmiBugInfo) {
			Iterator<BugInfo> it = osmiBugInfo.keySet().iterator();
			while(it.hasNext()){
				BugInfo next = it.next();
				if(!osmBugs.contains(next)){
					this.osmBugs.add(next);
				}
			}
		}
	}

	private enum GeomType {
		POINT, LINE, POLYGON
	};

	private static final Color SELECTED_COLOUR = Color.ORANGE;
	private static final float SELECTED_POINT_SIZE = 15.0f;
	private static final float OPACITY = 1.0f;
	private static final float LINE_WIDTH = 1.0f;
	private static final float POINT_SIZE = 10.0f;

	/**
	 * 
	 * @param wfsClient
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws IOException
	 * @throws IndexOutOfBoundsException
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public OsmInspectorLayer(GeoFabrikWFSClient wfsClient,
			ProgressMonitor monitor) throws NoSuchAuthorityCodeException,
			FactoryException, IOException, IndexOutOfBoundsException,
			ParseException {
		super("OsmInspector");

		arrFeatures = new ArrayList<OSMIFeatureTracker>();
		osmiBugInfo = new LinkedHashMap<BugInfo, Long>();
		selectGeomType = new ArrayList<GeomType>();

		// Step 3 - discovery; enhance to iterate over all types with bounds

		String typeNames[] = wfsClient.getTypeNames();
		renderer = new StreamingRenderer();
		CRS.decode(Main.getProjection().toCode());
		crsOSMI = CRS.decode("EPSG:4326");
		context = new DefaultMapContext(crsOSMI);

		selectGeomType.add(GeomType.POINT);
		for (int idx = 1; idx < typeNames.length; ++idx) {
			String typeName = typeNames[idx];
			Set<FeatureId> selectedFeatures = new HashSet<FeatureId>();

			FeatureCollection<SimpleFeatureType, SimpleFeature> features = wfsClient
					.getFeatures(typeName, monitor);
			setGeometry(selectGeomType, typeName);

			System.out.println("Osm Inspector Features size: "
					+ features.size());
			Style style = createDefaultStyle(idx, selectedFeatures);

			OSMIFeatureTracker tracker = new OSMIFeatureTracker(features);
			arrFeatures.add(tracker);
			FeatureIterator<SimpleFeature> it = tracker.getFeatures()
					.features();

			while (it.hasNext()) {
				BugInfo theInfo = new BugInfo(it.next(), osmiBugInfo.size());
				osmiBugInfo.put(theInfo, theInfo.bugId);
			}

			context.addLayer(tracker.getFeatures(), style);
		}

		osmiIndex = new BugIndex(osmiBugInfo);
		context.setTitle("Osm Inspector Errors");
		renderer.setContext(context);
		bIsChanged = true;

		// finally initialize the dialog
		dialog = new OsmInspectorDialog(this);
		this.updateView();
	}

	/**
	 * 
	 * @param wfsClient
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws IOException
	 * @throws ParseException
	 * @throws NoSuchElementException
	 * @throws IndexOutOfBoundsException
	 */
	@SuppressWarnings("unchecked")
	public void loadFeatures(GeoFabrikWFSClient wfsClient)
			throws NoSuchAuthorityCodeException, FactoryException, IOException,
			IndexOutOfBoundsException, NoSuchElementException, ParseException {
		String typeNames[] = wfsClient.getTypeNames();

		context.clearLayerList();
		selectGeomType.clear();
		selectGeomType.add(GeomType.POINT);

		ProgressMonitor monitor = new ProgressMonitor(Main.map.mapView,
				"Loading features", "", 0, 100);

		for (int idx = 1; idx < typeNames.length; ++idx) {
			String typeName = typeNames[idx];
			Set<FeatureId> selectedFeatures = new HashSet<FeatureId>();

			monitor.setProgress(100 / typeNames.length * idx);
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = wfsClient
					.getFeatures(typeName, monitor);
			setGeometry(selectGeomType, typeName);

			System.out.println("Osm Inspector Features size: "
					+ features.size());

			OSMIFeatureTracker tracker = arrFeatures.get(idx - layerOffset);
			tracker.mergeFeatures(features);

			FeatureIterator<SimpleFeature> it = tracker.getFeatures()
					.features();

			while (it.hasNext()) {
				BugInfo theInfo = new BugInfo(it.next(), osmiBugInfo.size());
				if (!osmiBugInfo.keySet().contains(theInfo)) {
					osmiBugInfo.put(theInfo, theInfo.bugId);
				}
			}

			Style style = createDefaultStyle(idx, selectedFeatures);
			context.addLayer(tracker.getFeatures(), style);
		}

		osmiIndex.append(osmiBugInfo);
		
		
		monitor.setProgress(100);
		monitor.close();
		bIsChanged = true;
		//dialog.updateDialog(this);
		dialog.refreshModel();
		//dialog.updateNextPrevAction(this);
		
		this.updateView();
	}

	private Style createDefaultStyle(int idx, Set<FeatureId> IDs) {
		Color fillColor = featureFills[idx];

		Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR, true);
		selectedRule.setFilter(ff.id(IDs));

		Rule rule = createRule(fillColor, fillColor, false);
		rule.setElseFilter(true);

		FeatureTypeStyle fts = sf.createFeatureTypeStyle();
		fts.rules().add(selectedRule);
		fts.rules().add(rule);

		Style style = sf.createStyle();
		style.featureTypeStyles().add(fts);
		return style;
	}

	private Rule createRule(Color outlineColor, Color fillColor,
			boolean bSelected) {
		Symbolizer symbolizer = null;
		Fill fill = null;
		Stroke stroke = sf.createStroke(ff.literal(outlineColor),
				ff.literal(LINE_WIDTH));

		switch (geometryType) {
		case POLYGON:
			fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
			symbolizer = sf.createPolygonSymbolizer(stroke, fill,
					geometryAttributeName);
			break;

		case LINE:
			symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
			break;

		case POINT:
			fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));

			Mark mark = sf.getTriangleMark();
			mark.setFill(fill);
			mark.setStroke(stroke);

			Graphic graphic = sf.createDefaultGraphic();
			graphic.graphicalSymbols().clear();
			graphic.graphicalSymbols().add(mark);
			graphic.setSize(ff.literal(bSelected ? SELECTED_POINT_SIZE
					: POINT_SIZE));

			symbolizer = sf.createPointSymbolizer(graphic,
					geometryAttributeName);
		}

		Rule rule = sf.createRule();
		rule.symbolizers().add(symbolizer);
		return rule;
	}

	private void setGeometry(ArrayList<GeomType> selectedTypes, String typename) {
		System.out.println("Passed type is" + typename);
		if (typename.compareTo("duplicate_ways") == 0) {
			geometryType = GeomType.LINE;
		} else
			geometryType = GeomType.POINT;

		selectedTypes.add(geometryType);
	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("layer/osmdata_small");
	}

	@Override
	public Object getInfoComponent() {
		return getToolTipText();
	}

	@Override
	public Action[] getMenuEntries() {
		return new Action[] {};
	}

	@Override
	public String getToolTipText() {
		return org.openstreetmap.josm.tools.I18n.tr("OsmInspector");
	}

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void mergeFrom(Layer from) {
		return;
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds box) {
		LatLon min = box.getMin();
		LatLon max = box.getMax();

		Envelope envelope2 = new Envelope(Math.min(min.lat(), max.lat()),
				Math.max(min.lat(), max.lat()), Math.min(min.lon(), max.lon()),
				Math.max(min.lon(), max.lon()));

		ReferencedEnvelope mapArea = new ReferencedEnvelope(envelope2, crsOSMI);

		renderer.setInteractive(false);
		renderer.paint(g, mv.getBounds(), mapArea);
		bIsChanged = false;
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor v) {
	}

	public boolean isChanged() {
		return bIsChanged;
	}

	public void updateView() {
		this.dialog.revalidate();
		Main.map.mapView.revalidate();
		Main.map.repaint();
	}

	public void selectFeatures(int x, int y) {
		int pixelDelta = 2;
		LatLon clickUL = Main.map.mapView.getLatLon(x - pixelDelta, y
				- pixelDelta);
		LatLon clickLR = Main.map.mapView.getLatLon(x + pixelDelta, y
				+ pixelDelta);

		Envelope envelope = new Envelope(
				Math.min(clickUL.lon(), clickLR.lon()), Math.max(clickUL.lon(),
						clickLR.lon()), Math.min(clickUL.lat(), clickLR.lat()),
				Math.max(clickUL.lat(), clickLR.lat()));

		ReferencedEnvelope mapArea = new ReferencedEnvelope(envelope, crsOSMI);

		Filter filter = (Filter) ff.intersects(ff.property("msGeometry"),
				ff.literal(mapArea));
		//
		// Select features in all layers
		//
		context.clearLayerList();

		// Iterate through features and build a list that intersects the above
		// envelope
		for (int idx = 0; idx < arrFeatures.size(); ++idx) {
			OSMIFeatureTracker tracker = arrFeatures.get(idx);
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = tracker
					.getFeatures();

			SimpleFeatureSource tempfs = DataUtilities.source(features);
			FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures;

			try {
				selectedFeatures = tempfs.getFeatures(filter);

				FeatureIterator<SimpleFeature> iter = selectedFeatures
						.features();
				Set<FeatureId> IDs = new HashSet<FeatureId>();

				System.out.println("Selected features "
						+ selectedFeatures.size());

				while (iter.hasNext()) {
					SimpleFeature feature = iter.next();
					IDs.add(feature.getIdentifier());
				}

				iter.close();

				geometryType = selectGeomType.get(idx + layerOffset);
				Style style = createDefaultStyle(idx + layerOffset, IDs);
				context.addLayer(features, style);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		bIsChanged = true;
	}

	public void selectFeatures(LatLon center) {
		Point point = Main.map.mapView.getPoint(center);
		selectFeatures(point.x, point.y);
	}

	public void setOsmiIndex(int firstIndex) {
		osmiIndex.nextIndex = firstIndex;
		osmiIndex.previousIndex = firstIndex - 1 >= 0 ? firstIndex - 1
				: osmiBugInfo.size() - 1;
	}

}