// License: Public Domain. For details, see LICENSE file.
package livegps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.preferences.CachingProperty;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.MapViewGraphics;
import org.openstreetmap.josm.gui.layer.gpx.GpxDrawHelper;
import org.openstreetmap.josm.spi.preferences.Config;

public class LiveGpsDrawHelper extends GpxDrawHelper {
    private final LiveGpsLayer layer;

    public static final String C_LIVEGPS_COLOR_POSITION = "color.livegps.position";
    public static final String C_LIVEGPS_COLOR_POSITION_ESTIMATE = "color.livegps.position_estimate";

    private static final CachingProperty<Color> COLOR_POSITION =
            new NamedColorProperty(C_LIVEGPS_COLOR_POSITION, Color.RED).cached();
    private static final CachingProperty<Color> COLOR_POSITION_ESTIMATE =
            new NamedColorProperty(C_LIVEGPS_COLOR_POSITION_ESTIMATE, Color.CYAN).cached();

    private static final String C_CURSOR_H = "livegps.cursor_height"; /* in pixels */
    private static final String C_CURSOR_W = "livegps.cursor_width"; /* in pixels */
    private static final String C_CURSOR_T = "livegps.cursor_thickness"; /* in pixels */

    public LiveGpsDrawHelper(LiveGpsLayer livegpslayer) {
        super(livegpslayer);
        layer = livegpslayer;
    }

    @Override
    public void paint(MapViewGraphics mvg) {
        super.paint(mvg);

        MapView mv = mvg.getMapView();
        Graphics2D g = mvg.getDefaultGraphics();
        WayPoint lastPoint = layer.lastPoint;
        LiveGpsData lastData = layer.lastData;

        if (lastPoint == null)
            return;

        Point screen = mv.getPoint(lastPoint.getCoor());

        int TriaHeight = Config.getPref().getInt(C_CURSOR_H, 20);
        int TriaWidth = Config.getPref().getInt(C_CURSOR_W, 10);
        int TriaThick = Config.getPref().getInt(C_CURSOR_T, 4);

        /*
         * Draw a bold triangle.
         * In case of deep zoom draw also a thin DOP oval.
         */

        g.setColor(COLOR_POSITION_ESTIMATE.get());
        int w, h;
        double ppm = 100 / mv.getDist100Pixel();    /* pixels per metre */

        w = (int) Math.round(lastData.getEpx() * ppm);
        h = (int) Math.round(lastData.getEpy() * ppm);

        if (w > TriaWidth || h > TriaWidth) {
            int xo, yo;

            yo = screen.y - (h/2);
            xo = screen.x - (w/2);

            g.drawOval(xo, yo, w, h);
        }

        int[] x = new int[4];
        int[] y = new int[4];
        float course = lastData.getCourse();
        float csin = (float) Math.sin(Math.toRadians(course));
        float ccos = (float) Math.cos(Math.toRadians(course));
        float csin120 = (float) Math.sin(Math.toRadians(course + 120));
        float ccos120 = (float) Math.cos(Math.toRadians(course + 120));
        float csin240 = (float) Math.sin(Math.toRadians(course + 240));
        float ccos240 = (float) Math.cos(Math.toRadians(course + 240));

        g.setColor(COLOR_POSITION.get());

        for (int i = 0; i < TriaThick; i++, TriaHeight--, TriaWidth--) {

            x[0] = screen.x + Math.round(TriaHeight * csin);
            y[0] = screen.y - Math.round(TriaHeight * ccos);
            x[1] = screen.x + Math.round(TriaWidth * csin120);
            y[1] = screen.y - Math.round(TriaWidth * ccos120);
            x[2] = screen.x;
            y[2] = screen.y;
            x[3] = screen.x + Math.round(TriaWidth * csin240);
            y[3] = screen.y - Math.round(TriaWidth * ccos240);

            g.drawPolygon(x, y, 4);
        }

    }
}
