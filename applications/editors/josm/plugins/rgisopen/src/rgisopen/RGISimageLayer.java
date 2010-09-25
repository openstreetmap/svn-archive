package rgisopen;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Icon;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * Image Layer (based on PicLayer source code) that displays RGIS data.
 * Geolocated with url parameters, but adjustable.
 *
 * @author Zverik
 */
public class RGISimageLayer extends Layer {

    private static int counter = 0;
    private BufferedImage image = null;
    private EastNorth position;
    private double scale = 1.0;
    private String tooltipText = "";
    private Icon layerIcon = null;

    // init parameters
    private File imageFile = null;
    private String url;

    // parsed url parameters
    private double iscale;
    private LatLon ipos;

    public RGISimageLayer( File file, String url ) {
        super("RGIS Image" + (++counter > 1 ? " " + counter : ""));
        imageFile = file;
        this.url = url;
        tooltipText = file.getAbsolutePath();
    }

    public void init() throws Exception {
        try {
            image = ImageIO.read(imageFile);
        } catch( Exception e ) {
            image = null;
            System.err.println("Cannot load image " + imageFile.getAbsolutePath() + ": " + e.getMessage());
        }

        Map<String, Double> params = parseUrl(url);
        int width = params.get("width").intValue();
        int height = params.get("height").intValue();
        double x = params.get("x");
        double y = params.get("y");
        double zoom = params.get("zoom");

        if( width != image.getWidth() || height != image.getHeight() )
            throw new IllegalArgumentException("Image size is different from URL parameters");
        if( x < 50000 || x > 150000 || y < 50000 || y > 150000 )
            throw new IllegalArgumentException("URL coordinates are out of bounds");
        double scale = zoom / width * 3779.527559;
        if( scale < 500 || scale > 1000000 )
            throw new IllegalArgumentException("Scale in URL is out of bounds");
        iscale = scale;
        ipos = convertCoords(x, y);
        position = Main.proj.latlon2eastNorth(ipos);
    }

    private LatLon convertCoords( double x, double y ) {
        Projection proj = ProjectionFactory.fromPROJ4Specification(new String[] {
            "+proj=merc",
            "+lat_0=0",
            "+lon_0=30",
            "+k=1",
            "+x_0=95936",
            "+y_0=-6552814",
            "+ellps=krass",
            "+units=m",
            "+towgs84=23.57,-141.00,-79.85,0.000,-0.350,-0.790,0.00",
            "+no_defs"
        });
        Point2D.Double coord = proj.inverseTransform(new Point2D.Double(x, y), new Point2D.Double());
        return new LatLon(coord.x, coord.y);
    }

    /**
     * parses query parameters into a map.
     */
    private static Map<String, Double> parseUrl( String url ) throws MalformedURLException {
        Map<String, Double> result = new HashMap<String, Double>();
        int state = 0; // 0=search for ?, 1=key, 2=value
        char[] urlc = (url+"&").toCharArray();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        for( char ch : urlc ) {
            if( state == 0 && (ch == '?' || ch == '&') ) {
                state = 1;
                key.setLength(0);
            } else if( state == 1 ) {
                if( ch == '=' ) {
                    state = 2;
                    value.setLength(0);
                } else if( ch == '&' ) {
                    // empty key - of no use
                    key = new StringBuilder();
                } else {
                    key.append(Character.toLowerCase(ch));
                }
            } else if( state == 2 ) {
                if( ch == '&' ) {
                    // found key/value
                    try {
                        result.put(key.toString(), Double.parseDouble(value.toString()));
                    } catch( NumberFormatException e ) {}
                } else if( Character.isDigit(ch) || ch == '.' ) {
                    value.append(ch);
                } else {
                    // unknown character - skip this key
                    state = 0;
                }
            }
        }
        return result;
    }

    @Override
    public void paint(Graphics2D g2, MapView mv, Bounds box) {
        if( image != null ) {
            // Position image at the right graphical place
            EastNorth center = Main.map.mapView.getCenter();
            EastNorth leftop = Main.map.mapView.getEastNorth( 0, 0 );
            double pixel_per_en = ( Main.map.mapView.getWidth() / 2.0 ) / ( center.east() - leftop.east() );

            //     This is now the offset in screen pixels
            double pic_offset_x = (( position.east() - leftop.east() ) * pixel_per_en);
            double pic_offset_y = (( leftop.north() - position.north() ) * pixel_per_en);

            // Let's use Graphics 2D
            Graphics2D g = (Graphics2D)g2.create();
            // Move
            g.translate( pic_offset_x, pic_offset_y );
            // Scale
            double scalex = scale / Main.map.mapView.getDist100Pixel();
//            g.scale( scalex, scalex );

            // Draw picture
            g.drawImage( image, -image.getWidth() / 2, -image.getHeight() / 2, null );
        } else {
            System.err.println("RGISImagelayer: where is image?");
        }
    }

    @Override
    public Icon getIcon() {
        return layerIcon;
    }

    @Override
    public String getToolTipText() {
        return tooltipText;
    }

    @Override
    public void mergeFrom(Layer from) {
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
    }

    @Override
    public Object getInfoComponent() {
        return null;
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[] {};
    }

}
