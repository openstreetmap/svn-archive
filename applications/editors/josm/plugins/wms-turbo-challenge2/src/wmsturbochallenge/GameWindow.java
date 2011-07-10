/*
 * GPLv2 or 3, Copyright (c) 2010  Andrzej Zaborowski
 *
 * This implements the game logic.
 */
package wmsturbochallenge;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.ImmutableGpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.WMSLayer;

public class GameWindow extends JFrame implements ActionListener {
    public GameWindow(Layer ground) {
        setTitle(tr("The Ultimate WMS Super-speed Turbo Challenge II"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setSize(s.getScreenSize().width, s.getScreenSize().height);
        setLocationRelativeTo(null);
        setResizable(false);

        while (s.getScreenSize().width < width * scale ||
                s.getScreenSize().height < height * scale)
            scale --;
        add(panel);

        setVisible(true);

        /* TODO: "Intro" screen perhaps with "Hall of Fame" */

        screen_image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        screen = screen_image.getGraphics();

        this.ground = ground;
        ground_view = new fake_map_view(Main.map.mapView, 0.0000001);

        /* Retrieve start position */
        EastNorth start = ground_view.parent.getCenter();
        lat = start.north();
        lon = start.east();

        addKeyListener(new TAdapter());

        timer = new Timer(80, this);
        timer.start();

        car_gps = new gps();
        car_gps.start();

        car_engine = new engine();
        car_engine.start();

        for (int i = 0; i < maxsprites; i ++)
            sprites[i] = new sprite_pos();

        generate_sky();
    }

    protected engine car_engine;

    protected gps car_gps;
    protected class gps extends Timer implements ActionListener {
        public gps() {
            super(1000, null);
            addActionListener(this);

            trackSegs = new ArrayList<Collection<WayPoint>>();
        }

        protected Collection<WayPoint> segment;
        protected Collection<Collection<WayPoint>> trackSegs;

        public void actionPerformed(ActionEvent e) {
            /* We should count the satellites here, see if we
             * have a fix and add any distortions.  */

            segment.add(new WayPoint(Main.getProjection().eastNorth2latlon(
                    new EastNorth(lon, lat))));
        }

        @Override
        public void start() {
            super.start();

            /* Start recording */
            segment = new ArrayList<WayPoint>();
            trackSegs.add(segment);
            actionPerformed(null);
        }

        public void save_trace() {
            int len = 0;
            for (Collection<WayPoint> seg : trackSegs)
                len += seg.size();

            /* Don't save traces shorter than 5s */
            if (len <= 5)
                return;

            GpxData data = new GpxData();
            data.tracks.add(new ImmutableGpxTrack(trackSegs,
                        new HashMap<String, Object>()));

            ground_view.parent.addLayer(
                    new GpxLayer(data, "Car GPS trace"));
        }
    }

    /* These are EastNorth, not actual LatLon */
    protected double lat, lon;
    /* Camera's altitude above surface (same units as lat/lon above) */
    protected double ele = 0.000003;
    /* Cut off at ~75px from bottom of the screen */
    protected double horizon = 0.63;
    /* Car's distance from the camera lens */
    protected double cardist = ele * 3;

    /* Pixels per pixel, the bigger the more oldschool :-)  */
    protected int scale = 5;

    protected BufferedImage screen_image;
    protected Graphics screen;
    protected int width = 320;
    protected int height = 200;
    protected int centre = width / 2;

    double maxdist = ele / (horizon - 0.6);
    double realwidth = maxdist * width / height;
    double pixelperlat = 1.0 * width / realwidth;
    double sratio = 0.85;
    protected int sw = (int) (2 * Math.PI * maxdist * pixelperlat * sratio);

    /* TODO: figure out how to load these dynamically after splash
     * screen is shown */
    protected static final ImageIcon car[] = new ImageIcon[] {
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/car0-l.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/car0.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/car0-r.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/car1-l.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/car1.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/car1-r.png"))),
    };
    protected static final ImageIcon bg[] = new ImageIcon[] {
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/bg0.png"))),
    };
    protected static final ImageIcon skyline[] = new ImageIcon[] {
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/horizon.png"))),
    };
    protected static final ImageIcon cactus[] = new ImageIcon[] {
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/cactus0.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/cactus1.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/cactus2.png"))),
    };
    protected static final ImageIcon cloud[] = new ImageIcon[] {
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/cloud0.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/cloud1.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/cloud2.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/cloud3.png"))),
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/cloud4.png"))),
    };
    protected static final ImageIcon aircraft[] = new ImageIcon[] {
            new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/aircraft0.png"))),
    };
    protected static final ImageIcon loading = new ImageIcon(
            Toolkit.getDefaultToolkit().createImage(
                    WMSRacer.class.getResource(
                        "/images/loading.png")));
    protected static Toolkit s = Toolkit.getDefaultToolkit();
    protected int current_bg = 0;
    protected int current_car = 0;
    protected boolean cacti_on = true;
    protected List<EastNorth> cacti = new ArrayList<EastNorth>();
    protected List<EastNorth> todelete = new ArrayList<EastNorth>();
    protected int splashframe = -1;
    protected EastNorth splashcactus;

    protected Layer ground;
    protected double heading = 0.0;
    protected double wheelangle = 0.0;
    protected double speed = 0.0;
    protected boolean key_down[] = new boolean[] {
        false, false, false, false, };

    protected void move() {
        /* Left */
        /* (At high speeds make more gentle turns) */
        if (key_down[0])
            wheelangle -= 0.1 / (1.0 + Math.abs(speed));
        /* Right */
        if (key_down[1])
            wheelangle += 0.1 / (1.0 + Math.abs(speed));
        if (wheelangle > 0.3)
            wheelangle = 0.3; /* Radians */
        if (wheelangle < -0.3)
            wheelangle = -0.3;

        wheelangle *= 0.7;

        /* Up */
        if (key_down[2])
            speed += speed >= 0.0 ? 1.0 / (2.0 + speed) : 0.5;
        /* Down */
        if (key_down[3]) {
            if (speed >= 0.5) /* Brake (TODO: sound) */
                speed -= 0.5;
            else if (speed >= 0.01) /* Brake (TODO: sound) */
                speed = 0.0;
            else /* Reverse */
                speed -= 0.5 / (4.0 - speed);
        }

        speed *= 0.97;
        car_engine.set_speed(speed);

        if (speed > -0.1 && speed < 0.1)
            speed = 0;

        heading += wheelangle * speed;

        boolean chop = false;
        double newlat = lat + Math.cos(heading) * speed * ele * 0.2;
        double newlon = lon + Math.sin(heading) * speed * ele * 0.2;
        for (EastNorth pos : cacti) {
            double alat = Math.abs(pos.north() - newlat);
            double alon = Math.abs(pos.east() - newlon);
            if (alat + alon < ele * 1.0) {
                if (Math.abs(speed) < 2.0) {
                    if (speed > 0.0)
                        speed = -0.5;
                    else
                        speed = 0.3;
                    newlat = lat;
                    newlon = lon;
                    break;
                }

                chop = true;
                splashframe = 0;
                splashcactus = pos;
                todelete.add(pos);
            }
        }

        lat = newlat;
        lon = newlon;

        /* Seed a new cactus if we're moving.
         * TODO: hook into data layers and avoid putting the cactus on
         * the road!
         */
        if (cacti_on && Math.random() * 30.0 < speed) {
            double left_x = maxdist * (width - centre) / height;
            double right_x = maxdist * (0 - centre) / height;
            double x = left_x + Math.random() * (right_x - left_x);
            double clat = lat + (maxdist - cardist) *
                Math.cos(heading) - x * Math.sin(heading);
            double clon = lon + (maxdist - cardist) *
                Math.sin(heading) + x * Math.cos(heading);

            cacti.add(new EastNorth(clon, clat));
            chop = true;
        }

        /* Chop down any cactus far enough that it can't
         * be seen.  ``If a cactus falls in a forest and
         * there is nobody around did it make a sound?''
         */
        if (chop) {
            for (EastNorth pos : cacti) {
                double alat = Math.abs(pos.north() - lat);
                double alon = Math.abs(pos.east() - lon);
                if (alat + alon > 2 * maxdist)
                    todelete.add(pos);
            }
            cacti.removeAll(todelete);
            todelete = new ArrayList<EastNorth>();
        }
    }

    int frame;
    boolean downloading = false;
    protected void screen_repaint() {
        /* Draw background first */
        sky_paint();

        /* On top of it project the floor */
        ground_paint();

        /* Messages */
        frame ++;
        if ((frame & 8) == 0 && downloading)
            screen.drawImage(loading.getImage(), centre -
                    loading.getIconWidth() / 2, 50, this);

        /* Sprites */
        sprites_paint();
    }

    static double max3(double x[]) {
        return x[0] > x[1] ? x[2] > x[0] ? x[2] : x[0] :
            (x[2] > x[1] ? x[2] : x[1]);
    }
    static double min3(double x[]) {
        return x[0] < x[1] ? x[2] < x[0] ? x[2] : x[0] :
            (x[2] < x[1] ? x[2] : x[1]);
    }

    protected void ground_paint() {
        double sin = Math.sin(heading);
        double cos = Math.cos(heading);

        /* First calculate the bounding box for the visible area.
         * The area will be (nearly) a triangle, so calculate the
         * EastNorth for the three corners and make a bounding box.
         */
        double left_x = maxdist * (width - centre) / height;
        double right_x = maxdist * (0 - centre) / height;
        double e_lat[] = new double[] {
            lat + (maxdist - cardist) * cos - left_x * sin,
            lat + (maxdist - cardist) * cos - right_x * sin,
            lat - cardist * cos, };
        double e_lon[] = new double[] {
            lon + (maxdist - cardist) * sin + left_x * cos,
            lon + (maxdist - cardist) * sin + right_x * cos,
            lon - cardist * sin, };
        ground_view.setProjectionBounds(new ProjectionBounds(
                new EastNorth(min3(e_lon), min3(e_lat)),
                new EastNorth(max3(e_lon), max3(e_lat))));

        /* If the layer is a WMS layer, check if any tiles are
         * missing */
        if (ground instanceof WMSLayer) {
            WMSLayer wms = (WMSLayer) ground;
            downloading = wms.hasAutoDownload() && (
                    null == wms.findImage(new EastNorth(
                            e_lon[0], e_lat[0])) ||
                    null == wms.findImage(new EastNorth(
                            e_lon[0], e_lat[0])) ||
                    null == wms.findImage(new EastNorth(
                            e_lon[0], e_lat[0])));
        }

        /* Request the image from ground layer */
        ground.paint(ground_view.graphics, ground_view, null);

        for (int y = (int) (height * horizon + 0.1); y < height; y ++) {
            /* Assume a 60 deg vertical Field of View when
             * calculating the distance at given pixel.  */
            double dist = ele / (1.0 * y / height - 0.6);
            double lat_off = lat + (dist - cardist) * cos;
            double lon_off = lon + (dist - cardist) * sin;

            for (int x = 0; x < width; x ++) {
                double p_x = dist * (x - centre) / height;

                EastNorth en = new EastNorth(
                        lon_off + p_x * cos,
                        lat_off - p_x * sin);

                Point pt = ground_view.getPoint(en);

                int rgb = ground_view.ground_image.getRGB(
                        pt.x, pt.y);
                screen_image.setRGB(x, y, rgb);
            }
        }
    }

    protected BufferedImage sky_image;
    protected Graphics sky;
    public void generate_sky() {
        sky_image = new BufferedImage(sw, 70,
                BufferedImage.TYPE_INT_ARGB);
        sky = sky_image.getGraphics();

        int n = (int) (Math.random() * sw * 0.03);
        for (int i = 0; i < n; i ++) {
            int t = (int) (Math.random() * 5.0);
            int x = (int) (Math.random() *
                    (sw - cloud[t].getIconWidth()));
            int y = (int) ((1 - Math.random() * Math.random()) *
                    (70 - cloud[t].getIconHeight()));
            sky.drawImage(cloud[t].getImage(), x, y, this);
        }

        if (Math.random() < 0.5) {
            int t = 0;
            int x = (int) (300 + Math.random() * (sw - 500 -
                        aircraft[t].getIconWidth()));
            sky.drawImage(aircraft[t].getImage(), x, 0, this);
        }
    }

    public void sky_paint() {
        /* for x -> 0, lim sin(x) / x = 1 */
        int hx = (int) (-heading * maxdist * pixelperlat);
        int hw = skyline[current_bg].getIconWidth();
        hx = ((hx % hw) - hw) % hw;

        int sx = (int) (-heading * maxdist * pixelperlat * sratio);
        sx = ((sx % sw) - sw) % sw;

        screen.drawImage(bg[current_bg].getImage(), 0, 0, this);
        screen.drawImage(sky_image, sx, 50, this);
        if (sw + sx < width)
            screen.drawImage(sky_image, sx + sw, 50, this);
        screen.drawImage(skyline[current_bg].getImage(), hx, 66, this);
        if (hw + hx < width)
            screen.drawImage(skyline[current_bg].getImage(),
                    hx + hw, 66, this);
    }

    protected class sprite_pos implements Comparable {
        double dist;

        int x, y, sx, sy;
        Image sprite;

        public sprite_pos() {
        }

        public int compareTo(Object x) {
            sprite_pos other = (sprite_pos) x;
            return (int) ((other.dist - this.dist) * 1000000.0);
        }
    }

    /* sizes decides how many zoom levels the sprites have.  We
     * could do just normal scalling according to distance but
     * that's not what old games did, they had prescaled sprites
     * for the different distances and you could see the feature
     * grow discretely as you approached it.  */
    protected final static int sizes = 8;

    protected final static int maxsprites = 32;
    protected sprite_pos sprites[] = new sprite_pos[maxsprites];

    protected void sprites_paint() {
        /* The vehicle */
        int orientation = (wheelangle > -0.02 ? wheelangle < 0.02 ?
                1 : 2 : 0) + current_car * 3;
        sprites[0].sprite = car[orientation].getImage();
        sprites[0].dist = cardist;
        sprites[0].sx = car[orientation].getIconWidth();
        sprites[0].x = centre - sprites[0].sx / 2;
        sprites[0].sy = car[orientation].getIconHeight();
        sprites[0].y = height - sprites[0].sy - 10; /* TODO */

        /* The cacti */
        double sin = Math.sin(-heading);
        double cos = Math.cos(-heading);
        int i = 1;

        for (EastNorth ll : cacti) {
            double clat = ll.north() - lat;
            double clon = ll.east() - lon;
            double dist = (clat * cos - clon * sin) + cardist;
            double p_x = clat * sin + clon * cos;

            if (dist * 8 <= cardist || dist > maxdist)
                continue;

            int x = (int) (p_x * height / dist + centre);
            int y = (int) ((ele / dist + 0.6) * height);

            if (i >= maxsprites)
                break;
            if (x < -10 || x > width + 10)
                continue;

            int type = (((int) (ll.north() * 10000000.0) & 31) % 3);
            int sx = cactus[type].getIconWidth();
            int sy = cactus[type].getIconHeight();

            sprite_pos pos = sprites[i ++];
            pos.dist = dist;
            pos.sprite = cactus[type].getImage();
            pos.sx = (int) (sx * cardist * 0.7 / dist);
            pos.sy = (int) (sy * cardist * 0.7 / dist);
            pos.x = x - pos.sx / 2;
            pos.y = y - pos.sy;
        }

        Arrays.sort(sprites, 0, i);
        for (sprite_pos sprite : sprites)
            if (i --> 0)
                screen.drawImage(sprite.sprite,
                        sprite.x, sprite.y,
                        sprite.sx, sprite.sy, this);
            else
                break;

        if (splashframe >= 0) {
            splashframe ++;
            if (splashframe >= 8)
                splashframe = -1;

            int type = (((int) (splashcactus.north() *
                            10000000.0) & 31) % 3);
            int sx = cactus[type].getIconWidth();
            int sy = cactus[type].getIconHeight();
            Image image = cactus[type].getImage();

            for (i = 0; i < 50; i ++) {
                int x = (int) (Math.random() * sx);
                int y = (int) (Math.random() * sy);
                int w = (int) (Math.random() * 20);
                int h = (int) (Math.random() * 20);
                int nx = centre + splashframe * (x - sx / 2);
                int ny = height - splashframe * (sy - y);
                int nw = w + splashframe;
                int nh = h + splashframe;

                screen.drawImage(image,
                        nx, ny, nx + nw, ny + nh,
                        x, y, x + w, y + h, this);
            }
        }
    }

    public boolean no_super_repaint = false;
    protected class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(Color.BLACK);
            setDoubleBuffered(true);
        }

        @Override
        public void paint(Graphics g) {
            int w = (int) getSize().getWidth();
            int h = (int) getSize().getHeight();

            if (no_super_repaint)
                no_super_repaint = false;
            else
                super.paint(g);

            g.drawImage(screen_image, (w - width * scale) / 2,
                    (h - height * scale) / 2,
                    width * scale, height * scale, this);

            Toolkit.getDefaultToolkit().sync();
        }
    }
    JPanel panel = new GamePanel();

    protected void quit() {
        timer.stop();

        car_engine.stop();

        car_gps.stop();
        car_gps.save_trace();

        setVisible(false);
        panel = null;
        screen_image = null;
        screen = null;
        dispose();
    }

    /*
     * Supposedly a thread drawing frames and sleeping in a loop is
     * better than for animating than swing Timers.  For the moment
     * I'll use a timer because I don't want to deal with all the
     * potential threading issues.
     */
    protected Timer timer;
    public void actionPerformed(ActionEvent e) {
        move();
        screen_repaint();

        no_super_repaint = true;
        panel.repaint();
    }

    protected class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_LEFT && !key_down[0]) {
                wheelangle -= 0.02;
                key_down[0] = true;
            }

            if (key == KeyEvent.VK_RIGHT && !key_down[1]) {
                wheelangle += 0.02;
                key_down[1] = true;
            }

            if (key == KeyEvent.VK_UP)
                key_down[2] = true;

            if (key == KeyEvent.VK_DOWN)
                key_down[3] = true;

            if (key == KeyEvent.VK_ESCAPE)
                quit();

            /* Toggle sound */
            if (key == KeyEvent.VK_S) {
                if (car_engine.is_on())
                    car_engine.stop();
                else
                    car_engine.start();
            }

            /* Toggle cacti */
            if (key == KeyEvent.VK_C) {
                cacti_on = !cacti_on;
                if (!cacti_on)
                    cacti = new ArrayList<EastNorth>();
            }

            /* Switch vehicle */
            if (key == KeyEvent.VK_V)
                if (current_car ++>= 1)
                    current_car = 0;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_LEFT)
                key_down[0] = false;

            if (key == KeyEvent.VK_RIGHT)
                key_down[1] = false;

            if (key == KeyEvent.VK_UP)
                key_down[2] = false;

            if (key == KeyEvent.VK_DOWN)
                key_down[3] = false;
        }
    }
    protected fake_map_view ground_view;
}
