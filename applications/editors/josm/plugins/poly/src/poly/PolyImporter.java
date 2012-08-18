package poly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.openstreetmap.josm.data.coor.LatLon;

import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.MirroredInputStream;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.xml.sax.SAXException;

/**
 * Imports poly files.
 *
 * @author zverik
 */
public class PolyImporter extends OsmImporter {
    public PolyImporter() {
        super(PolyType.FILE_FILTER);
    }

    protected DataSet parseDataSet( final String source ) throws IOException, SAXException, IllegalDataException {
        return parseDataSet(new MirroredInputStream(source), NullProgressMonitor.INSTANCE);
    }

    @Override
    protected DataSet parseDataSet( InputStream in, ProgressMonitor progressMonitor ) throws IllegalDataException {
        if( progressMonitor == null )
            progressMonitor = NullProgressMonitor.INSTANCE;
        CheckParameterUtil.ensureParameterNotNull(in, "in");
        BufferedReader reader = null;

        try {
            progressMonitor.beginTask(tr("Reading polygon filter file..."), 2);
            progressMonitor.indeterminateSubTask(tr("Reading polygon filter file..."));
            reader = new BufferedReader(new InputStreamReader(in));
            List<Area> areas = loadPolygon(reader);
            progressMonitor.worked(1);
            
            progressMonitor.indeterminateSubTask(tr("Preparing data set..."));
            DataSet ds = constructDataSet(areas);
            progressMonitor.worked(1);
            return ds;
        } catch( IOException e ) {
            throw new IllegalDataException(tr("Error reading poly file: {0}", e.getMessage()), e);
        } finally {
            try {
                if( reader != null )
                    reader.close();
            } catch( IOException e ) { }
            progressMonitor.finishTask();
        }
    }
    
    private List<Area> loadPolygon( BufferedReader reader ) throws IllegalDataException, IOException {
        String name = reader.readLine();
        if( name == null || name.trim().length() == 0 )
            throw new IllegalDataException(tr("The file must begin with a polygon name"));
        List<Area> areas = new ArrayList<Area>();
        Area area = null;
        boolean parsingSection = false;
        while(true) {
            String line;
            do {
                line = reader.readLine();
                if( line == null )
                    throw new IllegalDataException("File ended prematurely without END record");
                line = line.trim();
            } while( line.length() == 0 );

            if( line.equals("END") ) {
                if( !parsingSection )
                    break;
                else {
                    // an area has been read
                    if( area.getNodeCount() < 2 )
                        throw new IllegalDataException(tr("There are less than 2 points in an area"));
                    areas.add(area);
                    if( areas.size() == 1 )
                        areas.get(0).setPolygonName(name);
                    parsingSection = false;
                }
            }

            if( !parsingSection ) {
                area = new Area(line);
                parsingSection = true;
            } else {
                // reading area, parse coordinates
                String[] tokens = line.split("\\s+");
                double[] coords = new double[2];
                int tokenCount = 0;
                for( String token : tokens ) {
                    if( token.length() > 0 ) {
                        if( tokenCount > 2 )
                            throw new IllegalDataException(tr("A polygon coordinate line must contain exactly 2 numbers"));
                        try {
                            coords[tokenCount++] = Double.parseDouble(token);
                        } catch( NumberFormatException e ) {
                            throw new IllegalDataException(tr("Unable to parse {0} as a number", token));
                        }
                    }
                }
                if( tokenCount < 2 )
                    throw new IllegalDataException(tr("A polygon coordinate line must contain exactly 2 numbers"));
                LatLon coord = new LatLon(coords[1], coords[0]);
                if( !coord.isValid() )
                    throw new IllegalDataException(tr("Invalid coordinates were found"));
                area.addNode(coord);
            }
        }
        return areas;
    }

    private DataSet constructDataSet( List<Area> areas ) {
        DataSet ds = new DataSet();
        boolean foundInner = false;
        for( Area area : areas ) {
            if( !area.isOuter() )
                foundInner = true;
            area.constructWay(ds);
        }

        if( foundInner ) {
            Relation mp = new Relation();
            mp.put("type", "multipolygon");
            for( Area area : areas ) {
                mp.addMember(new RelationMember(area.isOuter() ? "outer" : "inner", area.getWay()));
            }
            ds.addPrimitive(mp);
        }

        return ds;
    }

    private class Area {
        private String name;
        private String polygonName;
        private List<LatLon> nodes;
        private boolean outer;
        private Way way;

        public Area( String name ) {
            this.name = name;
            outer = name.charAt(0) != '!';
            if( !outer )
                this.name = this.name.substring(1);
            nodes = new ArrayList<LatLon>();
            way = null;
            polygonName = null;
        }

        public void setPolygonName( String polygonName ) {
            this.polygonName = polygonName;
        }

        public void addNode( LatLon node ) {
            if( nodes.isEmpty() || !(nodes.get(nodes.size()-1).equals(node) || nodes.get(0).equals(node)) )
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

        public void constructWay( DataSet ds ) {
            way = new Way();
            for( LatLon coord : nodes )
                way.addNode(new Node(coord));
            way.addNode(way.getNode(0));
            way.put("ref", name);
            if( polygonName != null )
                way.put("name", polygonName);
            ds.addPrimitive(way);
        }
    }
}
