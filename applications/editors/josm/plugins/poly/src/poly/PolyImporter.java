// License: GPL. For details, see LICENSE file.
package poly;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.UploadPolicy;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.importexport.OsmImporter;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * Imports poly files.
 *
 * @author zverik
 * @author Gerd Petermann
 */
public class PolyImporter extends OsmImporter {
    /**
     * Create importer.
     */
    public PolyImporter() {
        super(PolyType.FILE_FILTER);
    }

    protected DataSet parseDataSet(final String source) throws IOException, IllegalDataException {
        try (CachedFile cf = new CachedFile(source)) {
            return parseDataSet(cf.getInputStream(), NullProgressMonitor.INSTANCE);
        }
    }

    @Override
    protected DataSet parseDataSet(InputStream in, ProgressMonitor progressMonitor) throws IllegalDataException {
        if (progressMonitor == null)
            progressMonitor = NullProgressMonitor.INSTANCE;
        CheckParameterUtil.ensureParameterNotNull(in, "in");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            progressMonitor.beginTask(tr("Reading polygon filter file..."), 3); 
            progressMonitor.indeterminateSubTask(tr("Reading polygon filter file..."));
            List<Area> areas = loadPolygon(reader);
            progressMonitor.worked(1);

            progressMonitor.indeterminateSubTask(tr("Preparing data set..."));
            DataSet ds = constructDataSet(areas);
            progressMonitor.worked(1);
            return ds;
        } catch (IOException e) {
            throw new IllegalDataException(tr("Error reading poly file: {0}", e.getMessage()), e);
        } finally {
            progressMonitor.finishTask();
        }
    }

    private static List<Area> loadPolygon(BufferedReader reader) throws IllegalDataException, IOException {
        String name = reader.readLine();
        if (name == null || name.trim().length() == 0)
            throw new IllegalDataException(tr("The file must begin with a polygon name"));
        List<Area> areas = new ArrayList<>();
        Area area = null;
        boolean parsingSection = false;
        int fixedCoords = 0;
        while (true) {
            String line;
            line = reader.readLine();
            if (line == null) 
                break;
            line = line.trim();
            if (line.isEmpty()) {
                // empty line is only allowed after a complete file (one or more rings belonging to one polygon)
                if (parsingSection)
                    throw new IllegalDataException(tr("Empty line in coordinate section"));
                area = null;
                parsingSection = false;
                name = null;
            } else if (line.equals("END")) {
                if (!parsingSection) {
                    area = null;
                } else {
                    // an area has been read
                    if (area.getNodeCount() < 2)
                        throw new IllegalDataException(tr("There are less than 2 points in an area"));
                    areas.add(area);
                    area.setPolygonName(name);
                    parsingSection = false;
                }
            } else if (name == null) {
                name = line;
            } else if (line.length() > 0) {
                if (!parsingSection) {
                    if (line.indexOf(' ') >= 0) {
                        boolean coordInsteadOfName = false;
                        try {
                            LatLon ll = parseCoordinate(line); 
                            if (ll.isValid()) {
                                coordInsteadOfName = true;
                            }
                        } catch (IllegalDataException e) {
                            coordInsteadOfName = false;
                        }
                        if (coordInsteadOfName) {
                            throw new IllegalDataException(tr("Found coordinates ''{0}'' instead of name", line));
                        }
                    }
                    area = new Area(line);
                    parsingSection = true;
                } else {
                    LatLon coord = parseCoordinate(line);
                    if (!coord.isValid()) {
                        // fix small deviations
                        double lat = coord.lat();
                        double lon = coord.lon();
                        if (lon < -180.0 && lon > -185.0) lon = -180.0;
                        if (lon > 180.0 && lon < 185.0) lon = 180.0;
                        if (lat < -90.0 && lat > -95.0) lat = -90.0;
                        if (lat > 90.0 && lat < 95.0) lat = 90.0;
                        coord = new LatLon(lat, lon);
                        fixedCoords++;
                        if (!coord.isValid())
                            throw new IllegalDataException(tr("Invalid coordinates were found: {0}, {1}", coord.lat(), coord.lon()));
                    }
                    area.addNode(parseCoordinate(line));
                }
            }
        }
        if (fixedCoords > 0)
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), 
                    tr("{0} points were outside world bounds and were moved", fixedCoords), "Import poly", JOptionPane.WARNING_MESSAGE);
        return areas;
    }

    /**
     * Parse a line that should contain two double values which describe a latitude/longitude pair. 
     * @param line the line to parse
     * @return a new LatLon 
     * @throws IllegalDataException in case of error
     */
    private static LatLon parseCoordinate(String line) throws IllegalDataException {
        String[] tokens = line.split("\\s+");
        double[] coords = new double[2];
        int tokenCount = 0;
        for (String token : tokens) {
            if (token.length() > 0) {
                if (tokenCount > 2)
                    break;
                try {
                    coords[tokenCount++] = Double.parseDouble(token);
                } catch (NumberFormatException e) {
                    throw new IllegalDataException(tr("Unable to parse {0} as a number", token));
                }
            }
        }
        if (tokenCount != 2)
            throw new IllegalDataException(tr("A polygon coordinate line must contain exactly 2 numbers"));
        return new LatLon(coords[1], coords[0]);
    }

    private static DataSet constructDataSet(List<Area> areas) {
        DataSet ds = new DataSet();
        ds.setUploadPolicy(UploadPolicy.DISCOURAGED);

        
        List<Area> curretSet = new ArrayList<>();
        
        for (Area area : areas) {
            if (!curretSet.isEmpty() && !area.polygonName.equals(curretSet.get(0).polygonName)) {
                constructPrimitive(ds, curretSet);
                curretSet.clear();
            }
            curretSet.add(area);
        }
        if (!curretSet.isEmpty())
            constructPrimitive(ds, curretSet);

        return ds;
    }

    private static void constructPrimitive(DataSet ds, List<Area> areas) {
        boolean isMultipolygon = areas.size() > 1;
        for (Area area : areas) {
            area.constructWay(ds, isMultipolygon);
        }

        if (isMultipolygon) {
            Relation mp = new Relation();
            mp.put("type", "multipolygon");
            Area outer = areas.get(0);
            if (outer.polygonName != null) {
                mp.put("name", outer.polygonName);
            }
            for (Area area : areas) {
                mp.addMember(new RelationMember(area.isOuter() ? "outer" : "inner", area.getWay()));
            }
            ds.addPrimitive(mp);
        }
    }

    private static class Area {
        private String name;
        private String polygonName;
        private List<LatLon> nodes;
        private boolean outer;
        private Way way;

        Area(String name) {
            this.name = name;
            outer = name.charAt(0) != '!';
            if (!outer)
                this.name = this.name.substring(1);
            nodes = new ArrayList<>();
            way = null;
            polygonName = "";
        }

        public void setPolygonName(String polygonName) {
            if (polygonName != null) {
                this.polygonName = polygonName;
            }
        }

        public void addNode(LatLon node) {
            if (nodes.isEmpty() || !(nodes.get(nodes.size()-1).equals(node) || nodes.get(0).equals(node)))
                nodes.add(node);
        }

        public boolean isOuter() {
            return outer;
        }

        public int getNodeCount() {
            return nodes.size();
        }

        public Way getWay() {
            return way;
        }

        public void constructWay(DataSet ds, boolean isMultipolygon) {
            way = new Way();
            for (LatLon coord : nodes) {
                Node node = new Node(coord);
                ds.addPrimitive(node);
                way.addNode(node);
            }
            way.addNode(way.getNode(0));
            if (isMultipolygon && name != null)
                way.put("name", name);
            else {
                if (polygonName != null)
                    way.put("name", polygonName);
            }
            ds.addPrimitive(way);
        }
    }
}
