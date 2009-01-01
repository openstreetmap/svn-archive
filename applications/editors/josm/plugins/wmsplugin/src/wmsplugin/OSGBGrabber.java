/*
package wmsplugin;

import uk.me.jstott.jcoord.OSRef;
import uk.me.jstott.jcoord.LatLng;

import java.io.IOException;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.Epsg4326;

// FIXME: Remove this hack when we have proper projection support.
public class OSGBGrabber extends WMSGrabber {
    public OSGBGrabber(String baseURL) {
        super(baseURL);
    }

    private Epsg4326 latlonProj = new Epsg4326();

    @Override public GeorefImage grab(Bounds b, Projection proj,
            double pixelPerDegree) throws IOException {
        Bounds bnew = toOSGB(b);
        double pixelPerDegreeNew =
            pixelPerDegree / (bnew.max.lon() - bnew.min.lon())
                * (b.max.lon() - b.min.lon());

        GeorefImage img = super.grab(bnew, latlonProj, pixelPerDegreeNew);

        img.min = proj.latlon2eastNorth(fromOSGB(img.min));
        img.max = proj.latlon2eastNorth(fromOSGB(img.max));

        return img;
    }

    protected static Bounds toOSGB(Bounds b) {
        LatLng[] lls = new LatLng[] {
            new LatLng(b.min.lat(), b.min.lon()),
            new LatLng(b.min.lat(), b.max.lon()),
            new LatLng(b.max.lat(), b.min.lon()),
            new LatLng(b.max.lat(), b.max.lon()) };

        for (LatLng ll : lls) ll.toOSGB36();

        OSRef[] grs = new OSRef[lls.length];
        for (int i = 0; i < lls.length; i++) grs[i] = lls[i].toOSRef();

        LatLon latlon = new LatLon(grs[0].getNorthing(), grs[0].getEasting());
        Bounds bnew = new Bounds(latlon, latlon);
        for (int i = 1; i < grs.length; i++)
            bnew.extend(new LatLon(grs[i].getNorthing(), grs[i].getEasting()));

        return bnew;
    }

    protected static LatLon fromOSGB(EastNorth en) {
        LatLng ll = new OSRef(en.east(), en.north()).toLatLng();
        ll.toWGS84();
        return new LatLon(ll.getLat(), ll.getLng());
    }
}
*/
