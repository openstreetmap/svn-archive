package org.openstreetmap.josm.plugins.osminspector;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ProgressMonitor;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;

public class GeoFabrikWFSClient {

	private Bounds bbox;
	private DataStore data;
	private boolean bInitialized = false;

	public GeoFabrikWFSClient(Bounds bounds) {
		bbox = bounds;
	}

	public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(
			String typeName, ProgressMonitor progressMonitor)
			throws IOException, NoSuchAuthorityCodeException, FactoryException {

		initializeDataStore();

		// Step 3 - discovery; enhance to iterate over all types with bounds
		SimpleFeatureType schema = data.getSchema(typeName);
		progressMonitor.setProgress(30);
		
		// Step 4 - target
		FeatureSource<SimpleFeatureType, SimpleFeature> source = data
				.getFeatureSource(typeName);
		System.out.println("Source Metadata Bounds:" + source.getBounds());
		System.out.println("Source schema: " + source.getSchema());

		progressMonitor.setProgress(40);
		
		// Step 5 - query
		List<AttributeDescriptor> listAttrs = schema.getAttributeDescriptors();
		String geomName = listAttrs.get(0).getLocalName();
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4236");

		Bounds bounds = Main.map.mapView.getLatLonBounds(Main.map.mapView
				.getBounds());

		LatLon minLL = bounds.getMin();
		LatLon maxLL = bounds.getMax();
		double minLat = Math.min(minLL.getY(), maxLL.getY());
		double maxLat = Math.max(minLL.getY(), maxLL.getY());
		double minLon = Math.min(minLL.getX(), maxLL.getX());
		double maxLon = Math.max(minLL.getX(), maxLL.getX());

		ReferencedEnvelope bboxRef = new ReferencedEnvelope(minLon, maxLon,
				minLat, maxLat, targetCRS);
		System.out.println("Reference Bounds:" + bboxRef);

		progressMonitor.setProgress(50);
		//
		// Ask WFS service for typeName data constrained by bboxRef
		//
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
				.getDefaultHints());
		Filter filterBB = ff.bbox(ff.property(geomName), bboxRef);
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = source
				.getFeatures(filterBB);

		progressMonitor.setProgress(80);
		return features;
	}

	public void initializeDataStore() throws IOException {
		if (bInitialized == true)
			return;

		String getCapabilities = "http://tools.geofabrik.de/osmi/view/routing_non_eu/wxs?SERVICE=WFS&VERSION=1.0.0&REQUEST=GetCapabilities";
		@SuppressWarnings("rawtypes")
		Map<String, Comparable> connectionParameters = new HashMap<String, Comparable>();
		connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL",
				getCapabilities);
		connectionParameters.put("WFSDataStoreFactory:WFS_STRATEGY",
				"mapserver");
		connectionParameters.put("WFSDataStoreFactory:LENIENT", true);
		connectionParameters.put("WFSDataStoreFactory:TIMEOUT", 20000);
		connectionParameters.put("WFSDataStoreFactory:BUFFER_SIZE", 10000);
		// Step 2 - connection
		data = DataStoreFinder.getDataStore(connectionParameters);

		bInitialized = true;
	}

	public String[] getTypeNames() throws IOException {
		return data.getTypeNames();
	}

	public DataStore getData() {
		return data;
	}

	public void setData(DataStore data) {
		this.data = data;
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		// try {
		// CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
		// GeoFabrikWFSClient theTest = new GeoFabrikWFSClient( new
		// Bounds(-124.0, -120.0, 32.0, 36.0));
		// theTest.initializeDataStore();
		// FeatureCollection<SimpleFeatureType, SimpleFeature> features =
		// theTest.getFeatures();
		// OsmInspectorLayer inspector = new OsmInspectorLayer(
		// theTest.getData());
		// inspector.setVisible(true);

		// ReferencedEnvelope bounds = new ReferencedEnvelope();
		// Iterator<SimpleFeature> iterator = features.iterator();
		// try {
		// while (iterator.hasNext()) {
		// Feature feature = iterator.next();
		// bounds.include(feature.getBounds());
		// }
		// System.out.println("Calculated Bounds:" + bounds);
		// } finally {
		// features.close(iterator);
		// }
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

}
