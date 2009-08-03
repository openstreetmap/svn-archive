package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.Lambert;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.data.coor.EastNorth;

/**
 * This is a layer that grabs the current screen from the French cadastre WMS
 * server. The data fetched this way is tiled and managed to the disc to reduce
 * server load.
 */
public class WMSLayer extends Layer {

    Component[] component = null;

    public int lambertZone = -1;

    protected static final Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(
            CadastrePlugin.class.getResource("/images/cadastre_small.png")));

    protected ArrayList<GeorefImage> images = new ArrayList<GeorefImage>();

    protected final int serializeFormatVersion = 2;

    private ArrayList<EastNorthBound> dividedBbox = new ArrayList<EastNorthBound>();

    private CacheControl cacheControl = null;

    private String location = "";

    private String codeCommune = "";

    private EastNorthBound communeBBox = new EastNorthBound(new EastNorth(0,0), new EastNorth(0,0));

    private boolean isRaster = false;

    private EastNorth rasterMin;

    private EastNorth rasterCenter;

    private double rasterRatio;

    double cRasterMaxSizeX = 12286;
    double cRasterMaxSizeY = 8730;

    public WMSLayer() {
        this(tr("Blank Layer"), "", -1);
    }

    public WMSLayer(String location, String codeCommune, int lambertZone) {
        super(buildName(location, codeCommune));
        this.location = location;
        this.codeCommune = codeCommune;
        this.lambertZone = Lambert.layoutZone;
        // enable auto-sourcing option
        CadastrePlugin.pluginUsed = true;
    }

    private static String buildName(String location, String codeCommune) {
        String ret = new String(location.toUpperCase());
        if (codeCommune != null && !codeCommune.equals(""))
            ret += "(" + codeCommune + ")";
        return  ret;
    }

    private String rebuildName() {
        return buildName(this.location.toUpperCase(), this.codeCommune);
    }

    public void grab(CadastreGrabber grabber, Bounds b) throws IOException {
        divideBbox(b, Integer.parseInt(Main.pref.get("cadastrewms.scale", Scale.X1.toString())));

        for (EastNorthBound n : dividedBbox) {
            GeorefImage newImage;
            try {
                newImage = grabber.grab(this, n.min, n.max);
            } catch (IOException e) {
                System.out.println("Download action cancelled by user or server did not respond");
                break;
            } catch (OsmTransferException e) {
                System.out.println("OSM transfer failed");
                break;
            }
            if (grabber.getWmsInterface().downloadCancelled) {
                System.out.println("Download action cancelled by user");
                break;
            }
            if (CadastrePlugin.backgroundTransparent) {
                for (GeorefImage img : images) {
                    if (img.overlap(newImage))
                        // mask overlapping zone in already grabbed image
                        img.withdraw(newImage);
                    else
                        // mask overlapping zone in new image only when new
                        // image covers completely the existing image
                        newImage.withdraw(img);
                }
            }
            images.add(newImage);
            saveToCache(newImage);
            Main.map.mapView.repaint();
            /*
            try { if (dividedBbox.size() > 1) Thread.sleep(1000);
            } catch (InterruptedException e) {};*/
        }
    }

    /**
     *
     * @param b      the original bbox, usually the current bbox on screen
     * @param factor 1 = source bbox 1:1
     *               2 = source bbox divided by 2x2 smaller boxes
     *               3 = source bbox divided by 3x3 smaller boxes
     *               4 = hard coded size of boxes (100 meters) rounded allowing
     *                   grabbing of next contiguous zone
     */
    private void divideBbox(Bounds b, int factor) {
        EastNorth lambertMin = Main.proj.latlon2eastNorth(b.min);
        EastNorth lambertMax = Main.proj.latlon2eastNorth(b.max);
        double minEast = lambertMin.east();
        double minNorth = lambertMin.north();
        double dEast = (lambertMax.east() - minEast) / factor;
        double dNorth = (lambertMax.north() - minNorth) / factor;
        dividedBbox.clear();
        if (factor < 4) {
            for (int xEast = 0; xEast < factor; xEast++)
                for (int xNorth = 0; xNorth < factor; xNorth++) {
                    dividedBbox.add(new EastNorthBound(new EastNorth(minEast + xEast * dEast, minNorth + xNorth * dNorth),
                                new EastNorth(minEast + (xEast + 1) * dEast, minNorth + (xNorth + 1) * dNorth)));
            }
        } else {
            // divide to fixed size squares
            int cSquare = Integer.parseInt(Main.pref.get("cadastrewms.squareSize", "100"));
            minEast = minEast - minEast % cSquare;
            minNorth = minNorth - minNorth % cSquare;
            for (int xEast = (int)minEast; xEast < lambertMax.east(); xEast+=cSquare)
                for (int xNorth = (int)minNorth; xNorth < lambertMax.north(); xNorth+=cSquare) {
                    dividedBbox.add(new EastNorthBound(new EastNorth(xEast, xNorth),
                                new EastNorth(xEast + cSquare, xNorth + cSquare)));
            }
        }
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getToolTipText() {
        String str = tr("WMS layer ({0}), {1} tile(s) loaded", getName(), images.size());
        if (isRaster) {
            str += "\n"+tr("Is not vectorized.");
            str += "\n"+tr("Raster center: {0}", rasterCenter);
        } else
            str += "\n"+tr("Is vectorized.");
            str += "\n"+tr("Commune bbox: {0}", communeBBox);
        return str;
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
    }

    @Override
    public void paint(Graphics g, final MapView mv) {
        for (GeorefImage img : images)
            img.paint((Graphics2D) g, mv, CadastrePlugin.backgroundTransparent,
                    CadastrePlugin.transparency, CadastrePlugin.drawBoundaries);
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        for (GeorefImage img : images) {
            v.visit(img.min);
            v.visit(img.max);
        }
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Component[] getMenuEntries() {
        component = new Component[] { new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
                new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)), new JMenuItem(new MenuActionLoadFromCache()),
                new JMenuItem(new LayerListPopup.InfoAction(this)) };
        return component;
    }

    public GeorefImage findImage(EastNorth eastNorth) {
        // Iterate in reverse, so we return the image which is painted last.
        // (i.e. the topmost one)
        for (int i = images.size() - 1; i >= 0; i--) {
            if (images.get(i).contains(eastNorth)) {
                return images.get(i);
            }
        }
        return null;
    }

    public boolean isOverlapping(Bounds bounds) {
        GeorefImage georefImage =
            new GeorefImage(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB ), // not really important
            Main.proj.latlon2eastNorth(bounds.min),
            Main.proj.latlon2eastNorth(bounds.max));
        for (GeorefImage img : images) {
            if (img.overlap(georefImage))
                return true;
        }
        return false;
    }

    public void saveToCache(GeorefImage image) {
        if (CacheControl.cacheEnabled) {
            getCacheControl().saveCache(image);
        }
    }

    public void saveNewCache() {
        if (CacheControl.cacheEnabled) {
            getCacheControl().deleteCacheFile();
            for (GeorefImage image : images)
                getCacheControl().saveCache(image);
        }
    }

    public CacheControl getCacheControl() {
        if (cacheControl == null)
            cacheControl = new CacheControl(this);
        return cacheControl;
    }

    /**
     * Convert the eastNorth input coordinates to raster coordinates.
     * The original raster size is [0,0,12286,8730] where 0,0 is the upper left corner and
     * 12286,8730 is the approx. raster max size.
     * @return the raster coordinates for the wms server request URL (minX,minY,maxX,maxY)
     */
    public String eastNorth2raster(EastNorth min, EastNorth max) {
        double minX = (min.east() - rasterMin.east()) / rasterRatio;
        double minY = (min.north() - rasterMin.north()) / rasterRatio;
        double maxX = (max.east() - rasterMin.east()) / rasterRatio;
        double maxY = (max.north() - rasterMin.north()) / rasterRatio;
        return minX+","+minY+","+maxX+","+maxY;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        setName(rebuildName());
    }

    public String getCodeCommune() {
        return codeCommune;
    }

    public void setCodeCommune(String codeCommune) {
        this.codeCommune = codeCommune;
        setName(rebuildName());
    }

    public boolean isRaster() {
        return isRaster;
    }

    public void setRaster(boolean isRaster) {
        this.isRaster = isRaster;
    }

    /**
     * Set the eastNorth position in rasterMin which is the 0,0 coordinate (bottom left corner).
     * The bounds width is the raster width and height is calculate on a fixed image ratio.
     * @param bounds
     */
    public void setRasterBounds(Bounds bounds) {
        rasterMin = new EastNorth(Main.proj.latlon2eastNorth(bounds.min).east(), Main.proj.latlon2eastNorth(bounds.min).north());
        EastNorth rasterMax = new EastNorth(Main.proj.latlon2eastNorth(bounds.max).east(), Main.proj.latlon2eastNorth(bounds.max).north());
        // now, resize on same proportion as wms server raster images (bounds center)
        double rasterHalfHeight = (rasterMax.east() - rasterMin.east())/cRasterMaxSizeX*cRasterMaxSizeY/2;
        double rasterMid = rasterMin.north() + (rasterMax.north()-rasterMin.north())/2;
        rasterMin.setLocation(rasterMin.east(), rasterMid - rasterHalfHeight);
        rasterMax.setLocation(rasterMax.east(), rasterMid + rasterHalfHeight);
        rasterCenter = new EastNorth(rasterMin.east()+(rasterMax.east()-rasterMin.east())/2,
                rasterMin.north()+(rasterMax.north()-rasterMin.north())/2);
        rasterRatio = (rasterMax.east() - rasterMin.east()) / cRasterMaxSizeX;
    }

    public EastNorth getRasterMin() {
        return rasterMin;
    }

    public void setRasterMin(EastNorth rasterMin) {
        this.rasterMin = rasterMin;
    }

    public void displace(double dx, double dy) {
        this.rasterMin = new EastNorth(rasterMin.east() + dx, rasterMin.north() + dy);
        this.rasterCenter = new EastNorth(rasterCenter.east() + dx, rasterCenter.north() + dy);
        for (GeorefImage img : images)
            img.displace(dx, dy);
    }

    public void resize(double proportion) {
        this.rasterMin = rasterMin.interpolate(rasterCenter, proportion);
        for (GeorefImage img : images)
            img.resize(rasterCenter, proportion);
    }

    public void rotate(double angle) {
        this.rasterMin = rasterMin.rotate(rasterCenter, angle);
        for (GeorefImage img : images)
            img.rotate(rasterCenter, angle);
    }

    /**
     * Called by CacheControl when a new cache file is created on disk
     * @param oos
     * @throws IOException
     */
    public void write(ObjectOutputStream oos, ArrayList<GeorefImage> imgs) throws IOException {
        oos.writeInt(this.serializeFormatVersion);
        oos.writeObject(this.location);
        oos.writeObject(this.codeCommune);
        oos.writeInt(this.lambertZone);
        oos.writeBoolean(this.isRaster);
        if (this.isRaster) {
            oos.writeObject(this.rasterMin);
            oos.writeObject(this.rasterCenter);
            oos.writeDouble(this.rasterRatio);
        } else {
            oos.writeObject(this.communeBBox);
        }
        for (GeorefImage img : imgs) {
            oos.writeObject(img);
        }
    }

    /**
     * Called by CacheControl when a cache file is read from disk
     * @param ois
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean read(ObjectInputStream ois, int currentLambertZone) throws IOException, ClassNotFoundException {
        int sfv = ois.readInt();
        if (sfv != this.serializeFormatVersion) {
            JOptionPane.showMessageDialog(Main.parent, tr("Unsupported cache file version; found {0}, expected {1}\nCreate a new one.",
                    sfv, this.serializeFormatVersion), tr("Cache Format Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        this.setLocation((String) ois.readObject());
        this.setCodeCommune((String) ois.readObject());
        this.lambertZone = ois.readInt();
        this.isRaster = ois.readBoolean();
        if (this.isRaster) {
            this.rasterMin = (EastNorth) ois.readObject();
            this.rasterCenter = (EastNorth) ois.readObject();
            this.rasterRatio = ois.readDouble();
        } else {
            this.communeBBox = (EastNorthBound) ois.readObject();
        }
        if (this.lambertZone != currentLambertZone) {
            JOptionPane.showMessageDialog(Main.parent, tr("Lambert zone {0} in cache "+
                    "incompatible with current Lambert zone {1}",
                    this.lambertZone+1, currentLambertZone), tr("Cache Lambert Zone Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        boolean EOF = false;
        try {
            while (!EOF) {
                GeorefImage newImage = (GeorefImage) ois.readObject();
                for (GeorefImage img : this.images) {
                    if (CadastrePlugin.backgroundTransparent) {
                        if (img.overlap(newImage))
                            // mask overlapping zone in already grabbed image
                            img.withdraw(newImage);
                        else
                            // mask overlapping zone in new image only when
                            // new image covers completely the existing image
                            newImage.withdraw(img);
                    }
                }
                this.images.add(newImage);
            }
        } catch (EOFException ex) {
            // expected exception when all images are read
        }
        return true;
    }

    public double getRasterRatio() {
        return rasterRatio;
    }

    public void setRasterRatio(double rasterRatio) {
        this.rasterRatio = rasterRatio;
    }

    public EastNorth getRasterCenter() {
        return rasterCenter;
    }

    public void setRasterCenter(EastNorth rasterCenter) {
        this.rasterCenter = rasterCenter;
    }

    public EastNorthBound getCommuneBBox() {
        return communeBBox;
    }

    public void setCommuneBBox(EastNorthBound entireCommune) {
        this.communeBBox = entireCommune;
    }

}
