package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.LambertCC9Zones;
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
public class WMSLayer extends Layer implements ImageObserver {

    Component[] component = null;

    private int lambertZone = -1;

    protected static final Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(
            CadastrePlugin.class.getResource("/images/cadastre_small.png")));

    protected Vector<GeorefImage> images = new Vector<GeorefImage>();

    protected final int serializeFormatVersion = 2;

    private ArrayList<EastNorthBound> dividedBbox = new ArrayList<EastNorthBound>();

    private CacheControl cacheControl = null;

    private String location = "";

    private String codeCommune = "";

    public EastNorthBound communeBBox = new EastNorthBound(new EastNorth(0,0), new EastNorth(0,0));

    private boolean isRaster = false;
    
    private boolean isAlreadyGeoreferenced = false;
    
    public double X0, Y0, angle, fX, fY;

    private EastNorth rasterMin;
    private EastNorth rasterMax;
    private double rasterRatio;
    
    private JMenuItem saveAsPng;

    public WMSLayer() {
        this(tr("Blank Layer"), "", -1);
    }

    public WMSLayer(String location, String codeCommune, int lambertZone) {
        super(buildName(location, codeCommune));
        this.location = location;
        this.codeCommune = codeCommune;
        this.lambertZone = lambertZone;
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
        if (isRaster) {
            b = new Bounds(Main.proj.eastNorth2latlon(rasterMin), Main.proj.eastNorth2latlon(rasterMax));
            divideBbox(b, Integer.parseInt(Main.pref.get("cadastrewms.rasterDivider", 
                    CadastrePreferenceSetting.DEFAULT_RASTER_DIVIDER)));
        } else
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
        EastNorth lambertMin = Main.proj.latlon2eastNorth(b.getMin());
        EastNorth lambertMax = Main.proj.latlon2eastNorth(b.getMax());
        double minEast = lambertMin.east();
        double minNorth = lambertMin.north();
        double dEast = (lambertMax.east() - minEast) / factor;
        double dNorth = (lambertMax.north() - minNorth) / factor;
        dividedBbox.clear();
        if (factor < 4 || isRaster) {
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
            str += "\n"+tr("Raster size: {0}", communeBBox);
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
        synchronized(this){
            for (GeorefImage img : images)
                img.paint((Graphics2D) g, mv, CadastrePlugin.backgroundTransparent,
                        CadastrePlugin.transparency, CadastrePlugin.drawBoundaries);
        }
        if (this.isRaster) {
            paintCrosspieces(g, mv);
        }
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
        saveAsPng = new JMenuItem(new MenuActionSaveRasterAs(this));
        saveAsPng.setEnabled(isRaster);
        component = new Component[] { new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
                new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)),
                new JMenuItem(new MenuActionLoadFromCache()),
                saveAsPng,
                new JMenuItem(new LayerListPopup.InfoAction(this)),
                
        };
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
            Main.proj.latlon2eastNorth(bounds.getMin()),
            Main.proj.latlon2eastNorth(bounds.getMax()));
        for (GeorefImage img : images) {
            if (img.overlap(georefImage))
                return true;
        }
        return false;
    }

    public void saveToCache(GeorefImage image) {
        if (CacheControl.cacheEnabled && !isRaster()) {
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
        if (saveAsPng != null)
            saveAsPng.setEnabled(isRaster);
    }

    public boolean isAlreadyGeoreferenced() {
        return isAlreadyGeoreferenced;
    }

    public void setAlreadyGeoreferenced(boolean isAlreadyGeoreferenced) {
        this.isAlreadyGeoreferenced = isAlreadyGeoreferenced;
    }

    /**
     * Set raster positions used for grabbing and georeferencing. 
     * rasterMin is the Eaast North of bottom left corner raster image on the screen when image is grabbed.
     * The bounds width and height are the raster width and height. The image width matches the current view
     * and the image height is adapted.
     * Required: the communeBBox must be set (normally it is catched by CadastreInterface and saved by DownloadWMSPlanImage)   
     * @param bounds the current main map view boundaries
     */
    public void setRasterBounds(Bounds bounds) {
        EastNorth rasterCenter = Main.proj.latlon2eastNorth(bounds.getCenter());
        EastNorth eaMin = Main.proj.latlon2eastNorth(bounds.getMin());
        EastNorth eaMax = Main.proj.latlon2eastNorth(bounds.getMax());
        double rasterSizeX = communeBBox.max.getX() - communeBBox.min.getX(); 
        double rasterSizeY = communeBBox.max.getY() - communeBBox.min.getY();
        double ratio = rasterSizeY/rasterSizeX;
        // keep same ratio on screen as WMS bbox (stored in communeBBox)
        rasterMin = new EastNorth(eaMin.getX(), rasterCenter.getY()-(eaMax.getX()-eaMin.getX())*ratio/2);
        rasterMax = new EastNorth(eaMax.getX(), rasterCenter.getY()+(eaMax.getX()-eaMin.getX())*ratio/2);
        rasterRatio = (rasterMax.getX()-rasterMin.getX())/rasterSizeX;
    }

    /**
     * Called by CacheControl when a new cache file is created on disk.
     * Save only primitives to keep cache independent of software changes.
     * @param oos
     * @throws IOException
     */
    public void write(ObjectOutputStream oos) throws IOException {
        oos.writeInt(this.serializeFormatVersion);
        oos.writeObject(this.location);    // String
        oos.writeObject(this.codeCommune); // String
        oos.writeInt(this.lambertZone);
        oos.writeBoolean(this.isRaster);
        if (this.isRaster) {
            oos.writeDouble(this.rasterMin.getX());
            oos.writeDouble(this.rasterMin.getY());
            oos.writeDouble(this.rasterMax.getX());
            oos.writeDouble(this.rasterMax.getY());
            oos.writeDouble(this.rasterRatio);
        }
        oos.writeDouble(this.communeBBox.min.getX());
        oos.writeDouble(this.communeBBox.min.getY());
        oos.writeDouble(this.communeBBox.max.getX());
        oos.writeDouble(this.communeBBox.max.getY());
    }

    /**
     * Called by CacheControl when a cache file is read from disk.
     * Cache uses only primitives to stay independent of software changes.
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
        this.setRaster(ois.readBoolean());
        if (this.isRaster) {
            double X = ois.readDouble();
            double Y = ois.readDouble();
            this.rasterMin = new EastNorth(X, Y);
            X = ois.readDouble();
            Y = ois.readDouble();
            this.rasterMax = new EastNorth(X, Y);
            this.rasterRatio = ois.readDouble();
        }
        double minX = ois.readDouble();
        double minY = ois.readDouble();
        double maxX = ois.readDouble();
        double maxY = ois.readDouble();
        this.communeBBox =  new EastNorthBound(new EastNorth(minX, minY), new EastNorth(maxX, maxY));
        if (this.lambertZone != currentLambertZone && currentLambertZone != -1) {
            JOptionPane.showMessageDialog(Main.parent, tr("Lambert zone {0} in cache "+
                    "incompatible with current Lambert zone {1}",
                    this.lambertZone+1, currentLambertZone), tr("Cache Lambert Zone Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        synchronized(this){
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
        }
        return true;
    }
    
    /**
     * Join the grabbed images into one single.
     * Works only for images grabbed from non-georeferenced images (Feuilles cadastrales)(same amount of
     * images in x and y)
     */
    public void joinRasterImages() {
        if (images.size() > 1) {
            EastNorth min = images.get(0).min;
            EastNorth max = images.get(images.size()-1).max;
            int oldImgWidth = images.get(0).image.getWidth();
            int oldImgHeight = images.get(0).image.getHeight(); 
            int newWidth = oldImgWidth*(int)Math.sqrt(images.size());
            int newHeight = oldImgHeight*(int)Math.sqrt(images.size());
            BufferedImage new_img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics g = new_img.getGraphics();
            // Coordinate (0,0) is on top,left corner where images are grabbed from bottom left
            int rasterDivider = (int)Math.sqrt(images.size());
            for (int h = 0; h < rasterDivider; h++) {
                for (int v = 0; v < rasterDivider; v++) {
                    int newx = h*oldImgWidth;
                    int newy = newHeight - oldImgHeight - (v*oldImgHeight);
                    int j = h*rasterDivider + v;
                    g.drawImage(images.get(j).image, newx, newy, this);
                }
            }
            synchronized(this) {
                images.clear();
                images.add(new GeorefImage(new_img, min, max));
            }
        }
    }
    
    /**
     * Image cropping based on two EN coordinates pointing to two corners in diagonal
     * Because it's coming from user mouse clics, we have to sort de positions first.
     * Works only for raster image layer (only one image in collection).
     * Updates layer georeferences. 
     * @param en1
     * @param en2
     */
    public void cropImage(EastNorth en1, EastNorth en2){
        // adj1 is corner bottom, left 
        EastNorth adj1 = new EastNorth(en1.east() <= en2.east() ? en1.east() : en2.east(),
                en1.north() <= en2.north() ? en1.north() : en2.north());
        // adj2 is corner top, right
        EastNorth adj2 = new EastNorth(en1.east() > en2.east() ? en1.east() : en2.east(),
                en1.north() > en2.north() ? en1.north() : en2.north());
        // s1 and s2 have 0,0 at top, left where all EastNorth coord. have 0,0 at bottom, left
        int sx1 = (int)((adj1.getX() - images.get(0).min.getX())*images.get(0).getPixelPerEast());
        int sy1 = (int)((images.get(0).max.getY() - adj2.getY())*images.get(0).getPixelPerNorth());
        int sx2 = (int)((adj2.getX() - images.get(0).min.getX())*images.get(0).getPixelPerEast());
        int sy2 = (int)((images.get(0).max.getY() - adj1.getY())*images.get(0).getPixelPerNorth());
        int newWidth = Math.abs(sx2 - sx1);
        int newHeight = Math.abs(sy2 - sy1);
        BufferedImage new_img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = new_img.getGraphics();
        g.drawImage(images.get(0).image, 0, 0, newWidth-1, newHeight-1,
                (int)sx1, (int)sy1, (int)sx2, (int)sy2,
                this);
        images.set(0, new GeorefImage(new_img, adj1, adj2));
        // important: update the layer georefs !
        rasterMin = adj1;
        rasterMax = adj2;
        rasterRatio = (rasterMax.getX()-rasterMin.getX())/(communeBBox.max.getX() - communeBBox.min.getX());
        setCommuneBBox(new EastNorthBound(new EastNorth(0,0), new EastNorth(newWidth-1,newHeight-1)));
    }

    public EastNorthBound getCommuneBBox() {
        return communeBBox;
    }

    public void setCommuneBBox(EastNorthBound entireCommune) {
        this.communeBBox = entireCommune;
        if (Main.proj instanceof LambertCC9Zones)
            setLambertCC9Zone(communeBBox.min.north());
    }

    /**
     * Method required by ImageObserver when drawing an image 
     */
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return false;
    }

    public int getLambertZone() {
        return lambertZone;
    }

    public void setLambertCC9Zone(double north) {
        int lambertZone = LambertCC9Zones.north2ZoneNumber(north);
        this.lambertZone = lambertZone;
        if (LambertCC9Zones.layoutZone != lambertZone) {
            String currentZone = MenuActionLambertZone.lambert9zones[LambertCC9Zones.layoutZone+1];
            String destZone = MenuActionLambertZone.lambert9zones[lambertZone+1];
            if (Main.map.mapView.getAllLayers().size() == 1) {
                /* Enable this code below when JOSM will have a proper support of dynamic projection change
                 * 
                System.out.println("close all layers and change current Lambert zone from "+LambertCC9Zones.layoutZone+" to "+lambertZone);
                Bounds b = null;
                if (Main.map != null && Main.map.mapView != null)
                    b = Main.map.mapView.getRealBounds();
                LambertCC9Zones.layoutZone = lambertZone;
                Main.map.mapView.zoomTo(b);
                */
            } else {
                JOptionPane.showMessageDialog(Main.parent, tr("Current layer is in Lambert CC9 Zone \"{0}\"\n"+
                        "where the commune is in Lambert CC9 Zone \"{1}\".\n"+
                        "Upload your changes, close all layers and change\n"+
                        "manually the Lambert zone from the Cadastre menu"
                        , currentZone, destZone));
            }
        }
    }

    public EastNorth getRasterCenter() {
        return new EastNorth((images.get(0).max.east()+images.get(0).min.east())/2,
                (images.get(0).max.north()+images.get(0).min.north())/2);
    }
    
    public void displace(double dx, double dy) {
        this.rasterMin = new EastNorth(rasterMin.east() + dx, rasterMin.north() + dy);
        images.get(0).shear(dx, dy);
    }

    public void resize(EastNorth rasterCenter, double proportion) {
        this.rasterMin = rasterMin.interpolate(rasterCenter, proportion);
        images.get(0).scale(rasterCenter, proportion);
    }

    public void rotate(EastNorth rasterCenter, double angle) {
        this.rasterMin = rasterMin.rotate(rasterCenter, angle);
        images.get(0).rotate(rasterCenter, angle);
    }

    private void paintCrosspieces(Graphics g, MapView mv) {
        String crosspieces = Main.pref.get("cadastrewms.crosspieces", "0");
        if (!crosspieces.equals("0")) {
            int modulo = 50;
            if (crosspieces.equals("2")) modulo = 100; 
            EastNorthBound currentView = new EastNorthBound(mv.getEastNorth(0, mv.getHeight()),
                    mv.getEastNorth(mv.getWidth(), 0));
            int minX = ((int)currentView.min.east()/modulo+1)*modulo;
            int minY = ((int)currentView.min.north()/modulo+1)*modulo;
            int maxX = ((int)currentView.max.east()/modulo)*modulo;
            int maxY = ((int)currentView.max.north()/modulo)*modulo;
            int size=(maxX-minX)/modulo;
            if (size<20) {
                int px= size > 10 ? 2 : Math.abs(12-size);
                g.setColor(Color.green);
                for (int x=minX; x<=maxX; x+=modulo) {
                    for (int y=minY; y<=maxY; y+=modulo) {
                        Point p = mv.getPoint(new EastNorth(x,y));
                        g.drawLine(p.x-px, p.y, p.x+px, p.y);
                        g.drawLine(p.x, p.y-px, p.x, p.y+px);
                    }
                }
            }
        }
    }
    
}
