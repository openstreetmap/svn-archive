package iodb;

import java.util.HashMap;
import java.util.List;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.ImageryLayer;

/**
 * Some common static methods for querying imagery layers.
 * 
 * @author zverik
 */
public class ImageryOffsetTools {
    private static HashMap<String, String> imageryAliases;
    
    public static ImageryLayer getTopImageryLayer() {
        List<ImageryLayer> layers = Main.map.mapView.getLayersOfType(ImageryLayer.class);
        for( ImageryLayer layer : layers ) {
            if( layer.isVisible() ) {
                return layer;
            }
        }
        return null;
    }
    
    private static LatLon getMapCenter() {
        Projection proj = Main.getProjection();
        return Main.map == null || Main.map.mapView == null
                ? new LatLon(0, 0) : proj.eastNorth2latlon(Main.map.mapView.getCenter());
    }
    
    public static LatLon getLayerOffset( ImageryLayer layer, LatLon center ) {
        Projection proj = Main.getProjection();
        EastNorth offsetCenter = proj.latlon2eastNorth(center);
        EastNorth centerOffset = offsetCenter.add(layer.getDx(), layer.getDy()); // todo: add or substract?
        LatLon offsetLL = proj.eastNorth2latlon(centerOffset);
        return offsetLL;
    }
    
    public static void applyLayerOffset( ImageryLayer layer, ImageryOffset offset ) {
        Projection proj = Main.getProjection();
        EastNorth center = proj.latlon2eastNorth(offset.getPosition());
        EastNorth offsetPos = proj.latlon2eastNorth(offset.getImageryPos());
        layer.setOffset(offsetPos.getX() - center.getX(), offsetPos.getY() - center.getY()); // todo: + or -?
    }
    
    public static String getImageryID( ImageryLayer layer ) {
        if( layer == null )
            return null;
        
        String url = layer.getInfo().getUrl();
        if( url == null )
            return null;
        
        if( imageryAliases == null )
            loadImageryAliases();
        for( String substr : imageryAliases.keySet() )
            if( url.contains(substr) )
                return imageryAliases.get(substr);
        
        return url; // todo: strip parametric parts, etc
    }
    
    private static void loadImageryAliases() {
        if( imageryAliases == null )
            imageryAliases = new HashMap<String, String>();
        else
            imageryAliases.clear();
        
        // { substring, alias }
        imageryAliases.put("bing", "bing");
        // todo: load from a resource?
    }
    
    // Following three methods were snatched from TMSLayer
    private static double latToTileY(double lat, int zoom) {
        double l = lat / 180 * Math.PI;
        double pf = Math.log(Math.tan(l) + (1 / Math.cos(l)));
        return Math.pow(2.0, zoom - 1) * (Math.PI - pf) / Math.PI;
    }

    private static double lonToTileX(double lon, int zoom) {
        return Math.pow(2.0, zoom - 3) * (lon + 180.0) / 45.0;
    }

    public static int getCurrentZoom() {
        if (Main.map == null || Main.map.mapView == null) {
            return 1;
        }
        MapView mv = Main.map.mapView;
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        double x1 = lonToTileX(topLeft.lon(), 1);
        double y1 = latToTileY(topLeft.lat(), 1);
        double x2 = lonToTileX(botRight.lon(), 1);
        double y2 = latToTileY(botRight.lat(), 1);

        int screenPixels = mv.getWidth() * mv.getHeight();
        double tilePixels = Math.abs((y2 - y1) * (x2 - x1) * 256 * 256);
        if (screenPixels == 0 || tilePixels == 0) {
            return 1;
        }
        double factor = screenPixels / tilePixels;
        double result = Math.log(factor) / Math.log(2) / 2 + 1;
        int intResult = (int) Math.floor(result);
        return intResult;
    }
}
