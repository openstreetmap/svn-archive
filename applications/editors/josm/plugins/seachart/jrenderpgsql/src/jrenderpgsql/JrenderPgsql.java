// License: GPL. For details, see LICENSE file.
package jrenderpgsql;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import render.ChartContext;
import render.Renderer;
import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.Snode;
import s57.S57osm;

import org.postgis.Point;
import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.Polygon;
import org.postgis.MultiPolygon;
import org.postgis.LinearRing;
import org.postgis.PGgeometry;

/**
 * @author Frederik Ramm
 * Based on Jrender by Malcom Herring
 */
public final class JrenderPgsql {
    private JrenderPgsql() {
        // Hide default constructor for utilities classes
    }

    public final static double RADIUS = 6378137.0;
    public final static double MERCATOR_WIDTH  = 40075016.685578488;
    public final static double MERCATOR_OFFSET = 20037508.342789244;

    static String dburl;
    static String outfile;
    static int xtile;
    static int ytile;
    static int zoom;
    static S57map map;
    static int empty;

    static StringBuilder nodebuf;
    static StringBuilder waybuf;
    static int nid;
    static int wid;

    static double scale = 1.0;
    static double tilesize = 256; 
    static double border = 256;

    static double minlon = 999.0;
    static double minlat = 999.0;
    static double maxlat = -999.0;
    static double maxlon = -999.0;

    static boolean debug = false;

    static HashMap<Point,Integer> nodes = new HashMap<Point, Integer>();

    /**
     *  helper for adding a <node> tag to the pseudo OSM XML
     *  @param p the node coordinates
     *  @param close whether to close the XML tag or not
     *  @returns ID of the node created (re-uses IDs at same location)
     */
    private static int addnode(Point p, boolean close)
    {
        if (close)
        {
            Integer existing = nodes.get(p);
            if (existing != null) return existing.intValue();
        }

        double lat = p.getY();
        double lon = p.getX();
        if (lat > maxlat) maxlat = lat;
        if (lat < minlat) minlat = lat;
        if (lon > maxlon) maxlon = lon;
        if (lon < minlon) minlon = lon;
        nodebuf.append("<node id=\"");
        nodebuf.append(++nid);
        nodebuf.append("\" lat=\"");
        nodebuf.append(p.getY());
        nodebuf.append("\" lon=\"");
        nodebuf.append(p.getX());
        nodebuf.append("\" version=\"1\" user=\"1\" uid=\"1\" changeset=\"1\" timestamp=\"1980-01-01T00:00:00Z\"");
        if (close) nodebuf.append("/");
        nodebuf.append(">\n");
        nodes.put(p, new Integer(nid));
        return nid;
    }

    private static void addpoly(Polygon po, String table, String osmid) 
    {
        if (po.numRings() > 1)
        {
            System.err.println("warning: polygons with holes not supported (" + table + " id=" + osmid + ")");
        }
        LinearRing lr = (LinearRing) po.getRing(0);
        waybuf.append("<way id=\"");
        waybuf.append(++wid);
        waybuf.append("\" version=\"1\" user=\"1\" uid=\"1\" changeset=\"1\" timestamp=\"1980-01-01T00:00:00Z\">\n");
        for (int i=0; i < lr.numPoints(); i++)
        {
            int n = addnode ((Point) lr.getPoint(i), true);
            waybuf.append("<nd ref=\"" + n + "\" />\n");
        }
    }

    /**
     *  helper for adding a PostGIS geometry to pseudo OSM XML
     *  @returns either the node or the way string buffer, depending on geom type
     */
    private static StringBuilder decode_geom(PGgeometry geom, String table, String osmid)
    {
        if (geom.getGeoType() == Geometry.POINT) 
        {
            addnode((Point) geom.getGeometry(), false);
            return nodebuf;
        }
        else if (geom.getGeoType() == Geometry.LINESTRING)
        {
            LineString ls = (LineString) geom.getGeometry();
            waybuf.append("<way id=\"");
            waybuf.append(++wid);
            waybuf.append("\" version=\"1\" user=\"1\" uid=\"1\" changeset=\"1\" timestamp=\"1980-01-01T00:00:00Z\">\n");
            for (int i=0; i < ls.numPoints(); i++)
            {
                int n = addnode ((Point) ls.getPoint(i), true);
                waybuf.append("<nd ref=\"" + n + "\" />\n");
            }
            return waybuf;
        }
        else if (geom.getGeoType() == Geometry.POLYGON)
        {
            addpoly((Polygon) geom.getGeometry(), table, osmid);
            return waybuf;
        }
        else if (geom.getGeoType() == Geometry.MULTIPOLYGON)
        {
            MultiPolygon po = (MultiPolygon) geom.getGeometry();
            for (Polygon p : po.getPolygons())
            {
                addpoly(p, table, osmid);
            }
            return waybuf;
        }
        
        System.err.println("bad geo type: " + geom.getGeoType());
        System.exit(-1);
        return null;
    }

    /**
     *  adds closing XML tag depending on geometry 
     */
    private static void finalize_geom(PGgeometry geom)
    {
        if (geom.getGeoType() == Geometry.POINT) 
        {
            nodebuf.append("</node>\n");
        }
        else if (geom.getGeoType() == Geometry.LINESTRING)
        {
            waybuf.append("</way>\n");
        }
        else if (geom.getGeoType() == Geometry.POLYGON)
        {
            waybuf.append("</way>\n");
        }
    }

    /**
     *  helper for escaping and writing a tag
     */
    private static void write_tag(StringBuilder buf, String k, String v)
    {
       buf.append("<tag k=\"" + k.replace("&", "&amp;").replace("\"", "&quot;") + "\" v=\"" + v.replace("&", "&amp;").replace("\"", "&quot;") + "\" />\n");
    }

    /**
     * converts a radian latitude to spherical Mercator Y
     */
    public static double radlat2y(double aLat) 
    {
       return Math.log(Math.tan(Math.PI / 4 + aLat / 2)) * RADIUS;
    }

    /**
     * converts a radian longitude to spherical Mercator X
     */
    public static double radlon2x(double aLong) 
    {
       return aLong * RADIUS;
    }

    public static void main(String[] args) throws Exception 
    {

        // parse command line
        // ------------------

        ArrayList<String> remain = new ArrayList<String>();

        for (int i=0; i<args.length; i++) 
        {
            if (args[i].equals("--scale"))
            {
                scale = Double.parseDouble(args[++i]);
            }
            else if (args[i].equals("--tilesize"))
            {
                tilesize = Double.parseDouble(args[++i]);
            }
            else if (args[i].equals("--debug"))
            {
                debug = true;
            }
            else
            {
                remain.add(args[i]);
            }
        }

        if (remain.size() < 5) 
        {
            System.err.println("Usage: java -jar jrenderpgsql.jar [--scale x] [--tilesize x] [--debug] <database connection string> <zoom> <xtile> <ytile> <outputfile>");
            System.err.println("format of the database connection string: jdbc:postgresql:///dbname?user=myuser&password=mypwd");
            System.exit(-1);
        }

        dburl = remain.get(0);
        zoom = Integer.parseInt(remain.get(1));
        xtile = Integer.parseInt(remain.get(2));
        ytile = Integer.parseInt(remain.get(3));
        outfile = remain.get(4);

        nodebuf = new StringBuilder();
        waybuf = new StringBuilder();

        Connection c = null;
        try 
        {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(dburl);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(-1);
        }

        double pow = Math.pow(2, zoom);
        border = (zoom < 12) ? (256 / (int) (Math.pow(2, (11 - zoom)))) : 256;
        double border_merc = MERCATOR_WIDTH / 256 / pow * border;

        // calculate spherical mercator bounds of the requested tile.

        double west = (xtile) * MERCATOR_WIDTH / pow - MERCATOR_OFFSET;
        double east = (xtile * 1.0 + tilesize / 256 / scale) * MERCATOR_WIDTH / pow - MERCATOR_OFFSET;
        double north = (pow - ytile) * MERCATOR_WIDTH / pow - MERCATOR_OFFSET;
        double south = (pow * 1.0 - ytile * 1.0 - tilesize / 256 / scale) * MERCATOR_WIDTH / pow - MERCATOR_OFFSET;

        // request data from PostGIS
        // -------------------------
        // This assumes the given database has the usual osm2pgsql tables, 
        // and a "tags" column (i.e. imported with --hstore). Caution, if
        // the import was made with --hstore-match-only then not all
        // seamarks will be present.

        Statement stmt = c.createStatement();
        for (String table : new String[] { "planet_osm_point", "planet_osm_line", "planet_osm_polygon" }) 
        {
            String query = "SELECT st_transform(way,4326) as mygeom, * FROM " 
              + table + " WHERE tags?'seamark:type' AND way && " 
              + "st_setsrid(st_makebox2d(st_makepoint(" + (west - border_merc) 
              + "," + (south - border_merc) + "), st_makepoint(" 
              + (east + border_merc) + "," + (north + border_merc) + ")),3857)";
            if (debug) System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);

            // analyse the result
            // ------------------
            // The result will contain of these columns:
            // 1. the spherical mercator geometry column "way" which we ignore
            // 2. the new geometry column "mygeom" which we use
            // 3. the "tags" column which requires special treatment
            // 4. lots of other, "normal" columns which we treat "normally"

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            int ogeomcol = 0;
            int geomcol = 0;
            int tagscol = 0;
            int idcol = 0;
            for (int i = 1; i<colCount; i++)
            {
                String n = meta.getColumnName(i);
                if (n.equals("way")) {
                    ogeomcol = i;
                } else if (n.equals("tags")) {
                    tagscol = i;
                } else if (n.equals("osm_id")) {
                    idcol = i;
                } else if (n.equals("mygeom")) {
                    geomcol = i;
                }
            }
            if (geomcol == 0) 
            {
                System.err.println("no geometry column in table " + table + "\n");
                System.exit(-1);
            }
            if (tagscol == 0) 
            {
                System.err.println("no tags column in table " + table + "\n");
                System.exit(-1);
            }

            // read data 
            // ---------
            // for each row, write a geomtry to the output stream, and
            // assemble its tags from the "normal" columns plus the "tags"
            // column.

            while (rs.next())
            {
                PGgeometry geom = (PGgeometry) rs.getObject(geomcol);
                String osmid = (idcol> 0) ? rs.getString(idcol) : "nil";
                StringBuilder currentbuf = decode_geom(geom, table, osmid);

                for (int i = 1; i<colCount; i++)
                {
                    if (i==ogeomcol) continue;
                    if (i==geomcol) continue;
                    if (i==tagscol) continue;
                    String k = meta.getColumnName(i);
                    String v = rs.getString(i);
                    if (v != null)
                    {
                        write_tag(currentbuf, k, v);
                    }
                }
                PGHStore h = new PGHStore(rs.getString(tagscol));
                for (Object k : h.keySet())
                {
                    write_tag(currentbuf, (String) k, (String) (h.get(k)));
                }

                finalize_geom(geom);
            }
        }

        // done querying database. build pseudo OSM file
        
        StringBuilder combinedBuf = new StringBuilder();
        combinedBuf.append("<osm version=\"0.6\">\n");

        combinedBuf.append("<bounds minlon=\"");
        combinedBuf.append(minlon);
        combinedBuf.append("\" maxlon=\"");
        combinedBuf.append(maxlon);
        combinedBuf.append("\" minlat=\"");
        combinedBuf.append(minlat);
        combinedBuf.append("\" maxlat=\"");
        combinedBuf.append(maxlat);
        combinedBuf.append("\" />\n");
        
        combinedBuf.append(nodebuf);
        combinedBuf.append(waybuf);
        combinedBuf.append("</osm>");

        if (debug) System.out.println(combinedBuf);

        // The pseudo OSM file is now complete, and we feed it to the S57
        // library where it will be parsed again.

        BufferedReader in = new BufferedReader(new StringReader(combinedBuf.toString()));
        map = new S57map(true);
        S57osm.OSMmap(in, map, false);
        in.close();

        // this ChartContext is mainly there for converting lat/lon to
        // tile x/y pixel coordinates.

        final double mile = 330.0 * pow / 16384.0 * scale;

        ChartContext context = new ChartContext() {
            public Point2D getPoint(Snode coord) {
                double x = border + (radlon2x(coord.lon) - west) * 256 * scale * pow / MERCATOR_WIDTH;
                double y = tilesize + border - ((radlat2y(coord.lat) - south) * scale * 256 * pow / MERCATOR_WIDTH);
                return new Point2D.Double(x, y);
            }

            public double mile(Feature feature) {
                return mile;
            }

            public boolean clip() {
                return false;
            }

            public Color background(S57map map) {
                return new Color(0, true);
            }

            public RuleSet ruleset() {
                return RuleSet.SEAMARK;
            }
        };

        // invoke renderer, and write file to disk
        // ---------------------------------------

        BufferedImage img = new BufferedImage((int)tilesize, (int)tilesize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.translate(-border, -border);
        Renderer.reRender(g2, new Rectangle((int)tilesize, (int)tilesize), zoom, scale * Math.pow(2, (zoom - 12)), map, context);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bos);
        FileOutputStream fos = new FileOutputStream(outfile);
        bos.writeTo(fos);
        fos.close();
        System.exit(0);
    }
}

