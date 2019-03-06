// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.datasets.NationalHandlers;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.UserCancelException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Reader of SHP (Shapefile) files.
 */
public class ShpReader extends GeographicReader {

    private final ShpHandler handler;
    private final Set<OsmPrimitive> featurePrimitives = new HashSet<>();

    public ShpReader(ShpHandler handler) {
        super(handler, NationalHandlers.DEFAULT_SHP_HANDLERS);
        this.handler = handler;
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

    private void parseFeature(Feature feature, final Component parent) throws UserCancelException, GeoMathTransformException,
    FactoryException, GeoCrsException, MismatchedDimensionException, TransformException {
        featurePrimitives.clear();
        GeometryAttribute geometry = feature.getDefaultGeometryProperty();
        if (geometry != null) {

            GeometryDescriptor desc = geometry.getDescriptor();

            if (crs == null) {
                if (desc != null && desc.getCoordinateReferenceSystem() != null) {
                    crs = desc.getCoordinateReferenceSystem();
                } else if (!GraphicsEnvironment.isHeadless()) {
                    GuiHelper.runInEDTAndWait(() -> {
                        if (0 == JOptionPane.showConfirmDialog(
                                parent,
                                tr("Unable to detect Coordinate Reference System.\nWould you like to fallback to ESPG:4326 (WGS 84) ?"),
                                tr("Warning: CRS not found"),
                                JOptionPane.YES_NO_CANCEL_OPTION
                                )) {
                            crs = wgs84;
                        }
                    });
                } else {
                    // Always use WGS84 in headless mode (used for unit tests only)
                    crs = wgs84;
                }
                if (crs != null) {
                    findMathTransform(parent, true);
                } else {
                    throw new GeoCrsException(tr("Unable to detect CRS !"));
                }
            }

            Object geomObject = geometry.getValue();
            if (geomObject instanceof Point) {  // TODO: Support LineString and Polygon.
                // Sure you could have a Set of 1 object and join these 2 branches of
                // code, but I feel there would be a performance hit.
                OsmPrimitive primitive = createOrGetEmptyNode((Point) geomObject);
                readNonGeometricAttributes(feature, primitive);
            } else if (geomObject instanceof GeometryCollection) { // Deals with both MultiLineString and MultiPolygon
                Set<OsmPrimitive> primitives = processGeometryCollection((GeometryCollection) geomObject);
                for (OsmPrimitive prim : primitives) {
                    readNonGeometricAttributes(feature, prim);
                }
            } else {
                // Debug unknown geometry
                Logging.debug("\ttype: "+geometry.getType());
                Logging.debug("\tbounds: "+geometry.getBounds());
                Logging.debug("\tdescriptor: "+desc);
                Logging.debug("\tname: "+geometry.getName());
                Logging.debug("\tvalue: "+geomObject);
                Logging.debug("\tid: "+geometry.getIdentifier());
                Logging.debug("-------------------------------------------------------------");
            }
        }
    }

    protected Set<OsmPrimitive> processGeometryCollection(GeometryCollection gc) throws TransformException {
        // A feture may be a collection.  This set holds the items of the collection.
        Set<OsmPrimitive> primitives = new HashSet<>();
        int nGeometries = gc.getNumGeometries();
        if (nGeometries < 1) {
            Logging.error("empty geometry collection found");
        } else {
            // Create the primitive "op" and add it to the set of primitives.
            for (int i = 0; i < nGeometries; i++) {
                OsmPrimitive op = null;
                Geometry g = gc.getGeometryN(i);
                if (g instanceof Polygon) {
                    Relation r = (Relation) op;
                    Polygon p = (Polygon) g;
                    // Do not create relation if there's only one polygon without interior ring
                    // except if handler prefers it
                    if (r == null && (nGeometries > 1 || p.getNumInteriorRing() > 0 ||
                            (handler != null && handler.preferMultipolygonToSimpleWay()))) {
                        r = createMultipolygon();
                    }
                    Way w = createOrGetWay(p.getExteriorRing());
                    if (r != null) {
                        addWayToMp(r, "outer", w);
                        for (int j = 0; j < p.getNumInteriorRing(); j++) {
                            addWayToMp(r, "inner", createOrGetWay(p.getInteriorRingN(j)));
                        }
                    }
                } else if (g instanceof LineString) {
                    op = createOrGetWay((LineString) g);
                } else if (g instanceof Point) {
                    op = createOrGetNode((Point) g);
                } else {
                    Logging.error("unsupported geometry : "+g);
                }
                if (op != null) {
                    primitives.add(op);
                }
            }
        }
        return primitives;
    }

    public DataSet parse(File file, ProgressMonitor instance) throws IOException {
        crs = null;
        transform = null;
        try {
            if (file != null) {
                Map<String, Serializable> params = new HashMap<>();
                Charset charset = null;
                params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
                if (handler != null && handler.getDbfCharset() != null) {
                    charset = handler.getDbfCharset();
                } else {
                    String path = file.getAbsolutePath();
                    // See https://gis.stackexchange.com/a/3663/17245
                    path = path.substring(0, path.lastIndexOf('.')) + ".cpg";
                    Path cpg = new File(path).toPath();
                    if (Files.exists(cpg)) {
                        try (BufferedReader reader = Files.newBufferedReader(cpg, StandardCharsets.UTF_8)) {
                            String cs = reader.readLine();
                            if (cs.matches("\\d+")) {
                                // We only have the code page number, we need to find the good alias for java.nio API
                                // see https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html
                                for (String prefix : Arrays.asList("IBM", "IBM0", "IBM00", "x-IBM", "ISO-", "windows-", "x-windows-")) {
                                    try {
                                        charset = Charset.forName(prefix+cs);
                                        break;
                                    } catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
                                        Logging.trace(e);
                                    }
                                }
                            } else {
                                // We may have a charset name, such as "UTF-8"
                                charset = Charset.forName(cs);
                            }
                        } catch (IOException | UnsupportedCharsetException | IllegalCharsetNameException e) {
                            Logging.warn(e);
                        }
                    }
                }
                if (charset != null) {
                    Logging.info("Using charset "+charset);
                    params.put(ShapefileDataStoreFactory.DBFCHARSET.key, charset.name());
                }
                DataStore dataStore = new ShapefileDataStoreFactory().createDataStore(params);
                if (dataStore == null) {
                    throw new IOException(tr("Unable to find a data store for file {0}", file.getName()));
                }

                String[] typeNames = dataStore.getTypeNames();
                String typeName = typeNames[0];

                FeatureSource<?, ?> featureSource = dataStore.getFeatureSource(typeName);
                FeatureCollection<?, ?> collection = featureSource.getFeatures();

                if (instance != null) {
                    instance.beginTask(tr("Loading shapefile ({0} features)", collection.size()), collection.size());
                }

                int n = 0;

                Component parent = instance != null ? instance.getWindowParent() : MainApplication.getMainFrame();

                try (FeatureIterator<?> iterator = collection.features()) {
                    while (iterator.hasNext()) {
                        n++;
                        try {
                            Feature feature = iterator.next();
                            parseFeature(feature, parent);
                            if (handler != null) {
                                handler.notifyFeatureParsed(feature, ds, featurePrimitives);
                            }
                        } catch (UserCancelException e) {
                            e.printStackTrace();
                            return ds;
                        }
                        if (instance != null) {
                            instance.worked(1);
                            instance.setCustomText(n+"/"+collection.size());
                        }
                    }
                } finally {
                    nodes.clear();
                    if (instance != null) {
                        instance.setCustomText(null);
                    }
                }
            }
        } catch (IOException e) {
            Logging.error(e);
            throw e;
        } catch (Exception e) {
            Logging.error(e);
            throw new IOException(e);
        }
        return ds;
    }

    private static void readNonGeometricAttributes(Feature feature, OsmPrimitive primitive) {
        try {
            for (Property prop : feature.getProperties()) {
                if (!(prop instanceof GeometryAttribute)) {
                    Name name = prop.getName();
                    Object value = prop.getValue();
                    if (name != null && value != null) {
                        String sName = name.toString();
                        String sValue = value.toString();
                        if (value instanceof Date) {
                            sValue = new SimpleDateFormat("yyyy-MM-dd").format(value);
                        }
                        if (!sName.isEmpty() && !sValue.isEmpty()) {
                            primitive.put(sName, sValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error(e);
        }
    }

    @Override
    protected Node createOrGetNode(Point p) throws MismatchedDimensionException, TransformException {
        Node n = super.createOrGetNode(p);
        featurePrimitives.add(n);
        return n;
    }

    @Override
    protected <T extends OsmPrimitive> T addOsmPrimitive(T p) {
        featurePrimitives.add(p);
        return super.addOsmPrimitive(p);
    }
}
