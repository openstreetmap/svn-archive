package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.lang.Math;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.data.coor.EastNorth;

/**
 * This is a layer that grabs the current screen from an WMS server. The data
 * fetched this way is tiled and managerd to the disc to reduce server load.
 */
public class WMSLayer extends Layer {

    protected static final Icon icon =
        new ImageIcon(Toolkit.getDefaultToolkit().createImage(WMSPlugin.class.getResource("/images/wms_small.png")));

    public int messageNum = 5; //limit for messages per layer
    protected MapView mv;
    protected String resolution;
    protected boolean stopAfterPaint = false;
    protected int ImageSize = 500;
    protected int dax = 10;
    protected int day = 10;
    protected int minZoom = 3;
    protected double dx = 0.0;
    protected double dy = 0.0;
    protected double pixelPerDegree;
    protected GeorefImage[][] images = new GeorefImage[dax][day];
    JCheckBoxMenuItem startstop = new JCheckBoxMenuItem(tr("Automatic downloading"), true);

    protected String baseURL;
    protected final int serializeFormatVersion = 4;

    private ExecutorService executor;

    public WMSLayer() {
        this(tr("Blank Layer"), null);
        initializeImages();
        mv = Main.map.mapView;
    }

    public WMSLayer(String name, String baseURL) {
        super(name);
        background = true; /* set global background variable */
        initializeImages();
        this.baseURL = baseURL;
        WMSGrabber.getProjection(baseURL, true);
        mv = Main.map.mapView;
        getPPD();

        executor = Executors.newFixedThreadPool(3);
    }

    public void getPPD(){
        pixelPerDegree = mv.getWidth() / (bounds().max.lon() - bounds().min.lon());
    }

    public void initializeImages() {
        images = new GeorefImage[dax][day];
        for(int x = 0; x<dax; ++x)
            for(int y = 0; y<day; ++y)
                images[x][y]= new GeorefImage(false);
    }

    @Override public Icon getIcon() {
        return icon;
    }

    public String scale(){
        LatLon ll1 = mv.getLatLon(0,0);
        LatLon ll2 = mv.getLatLon(100,0);
        double dist = ll1.greatCircleDistance(ll2);
        return dist > 1000 ? (Math.round(dist/100)/10.0)+" km" : Math.round(dist*10)/10+" m";
    }

    @Override public String getToolTipText() {
        if(startstop.isSelected())
            return tr("WMS layer ({0}), automatically downloading in zoom {1}", name, resolution);
        else
            return tr("WMS layer ({0}), downloading in zoom {1}", name, resolution);
    }

    @Override public boolean isMergable(Layer other) {
        return false;
    }

    @Override public void mergeFrom(Layer from) {
    }

    private Bounds XYtoBounds (int x, int y) {
        return new Bounds(
            new LatLon( x * ImageSize / pixelPerDegree,
                         y * ImageSize / pixelPerDegree),
            new LatLon((x + 1) *  ImageSize / pixelPerDegree,
                       (y + 1) * ImageSize / pixelPerDegree));
    }

    private int modulo (int a, int b) {
      if(a%b>=0)return a%b;
      else return a%b+b;
    }

    protected Bounds bounds(){
        return new Bounds(
            mv.getLatLon(0, mv.getHeight()),
            mv.getLatLon(mv.getWidth(), 0));
    }

    @Override public void paint(Graphics g, final MapView mv) {
        if(baseURL == null) return;

        if( !startstop.isSelected() || (pixelPerDegree / (mv.getWidth() / (bounds().max.lon() - bounds().min.lon())) > minZoom) ){ //don't download when it's too outzoomed
            for(int x = 0; x<dax; ++x)
                for(int y = 0; y<day; ++y)
                    images[modulo(x,dax)][modulo(y,day)].paint(g, mv, dx, dy);
        } else
            downloadAndPaintVisible(g, mv);
    }

    public void displace(double dx, double dy) {
        this.dx += dx;
        this.dy += dy;
    }

    protected void downloadAndPaintVisible(Graphics g, final MapView mv){
        int bminx= (int)Math.floor ((bounds().min.lat() * pixelPerDegree ) / ImageSize );
        int bminy= (int)Math.floor ((bounds().min.lon() * pixelPerDegree ) / ImageSize );
        int bmaxx= (int)Math.ceil  ((bounds().max.lat() * pixelPerDegree ) / ImageSize );
        int bmaxy= (int)Math.ceil  ((bounds().max.lon() * pixelPerDegree ) / ImageSize );

        if((bmaxx - bminx > dax) || (bmaxy - bminy > day)){
            JOptionPane.showMessageDialog(Main.parent, tr("The requested area is too big. Please zoom in a little, or change resolution"));
            return;
        }

        for(int x = bminx; x<bmaxx; ++x)
            for(int y = bminy; y<bmaxy; ++y){
                GeorefImage img = images[modulo(x,dax)][modulo(y,day)];
                if(!img.paint(g, mv, dx, dy) && !img.downloadingStarted){
                    img.downloadingStarted = true;
                    img.image = null;
                    Grabber gr = WMSPlugin.getGrabber(baseURL, XYtoBounds(x,y), Main.main.proj, pixelPerDegree, img, mv, this);
                    executor.submit(gr);
            }
        }
    }

    @Override public void visitBoundingBox(BoundingXYVisitor v) {
        for(int x = 0; x<dax; ++x)
            for(int y = 0; y<day; ++y)
                if(images[x][y].image!=null){
                    v.visit(images[x][y].min);
                    v.visit(images[x][y].max);
                }
    }

    @Override public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override public Component[] getMenuEntries() {
        return new Component[]{
                new JMenuItem(new LayerListDialog.ShowHideLayerAction(this)),
                new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),
                new JSeparator(),
                new JMenuItem(new LoadWmsAction()),
                new JMenuItem(new SaveWmsAction()),
                new JSeparator(),
                startstop,
                new JMenuItem(new changeResolutionAction()),
                new JMenuItem(new downloadAction()),
                new JSeparator(),
                new JMenuItem(new LayerListPopup.InfoAction(this))
        };
    }

    public GeorefImage findImage(EastNorth eastNorth) {
        for(int x = 0; x<dax; ++x)
            for(int y = 0; y<day; ++y)
                    if(images[x][y].image!=null && images[x][y].min!=null && images[x][y].max!=null)
                        if(images[x][y].contains(eastNorth, dx, dy))
                            return images[x][y];
        return null;
    }

    public class downloadAction extends AbstractAction {
        public downloadAction() {
            super(tr("Download visible tiles"));
        }
        public void actionPerformed(ActionEvent ev) {
            downloadAndPaintVisible(mv.getGraphics(), mv);
        }
    }

    public class changeResolutionAction extends AbstractAction {
        public changeResolutionAction() {
            super(tr("Change resolution"));
        }
        public void actionPerformed(ActionEvent ev) {
            initializeImages();
            resolution = scale();
            getPPD();
            mv.repaint();
        }
    }

    public class SaveWmsAction extends AbstractAction {
        public SaveWmsAction() {
            super(tr("Save WMS layer to file"), ImageProvider.get("save"));
        }
        public void actionPerformed(ActionEvent ev) {
            File f = openFileDialog(false);
            try
            {
                FileOutputStream fos = new FileOutputStream(f);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeInt(serializeFormatVersion);
                oos.writeInt(dax);
                oos.writeInt(day);
                oos.writeInt(ImageSize);
                oos.writeDouble(pixelPerDegree);
                oos.writeObject(name);
                oos.writeObject(baseURL);
                oos.writeObject(images);
                oos.close();
                fos.close();
            }
            catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    public class LoadWmsAction extends AbstractAction {
        public LoadWmsAction() {
            super(tr("Load WMS layer from file"), ImageProvider.get("load"));
        }
        public void actionPerformed(ActionEvent ev) {
            File f = openFileDialog(true);
            if (f == null) return;
            try
            {
                FileInputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                int sfv = ois.readInt();
                if (sfv != serializeFormatVersion) {
                    JOptionPane.showMessageDialog(Main.parent,
                        tr("Unsupported WMS file version; found {0}, expected {1}", sfv, serializeFormatVersion),
                        tr("File Format Error"),
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                startstop.setSelected(false);
                dax = ois.readInt();
                day = ois.readInt();
                ImageSize = ois.readInt();
                pixelPerDegree = ois.readDouble();
                name = (String) ois.readObject();
                baseURL = (String) ois.readObject();
                images = (GeorefImage[][])ois.readObject();
                ois.close();
                fis.close();
                mv.repaint();
            }
            catch (Exception ex) {
                // FIXME be more specific
                ex.printStackTrace(System.out);
                JOptionPane.showMessageDialog(Main.parent,
                    tr("Error loading file"),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    protected static JFileChooser createAndOpenFileChooser(boolean open, boolean multiple) {
        String curDir = Main.pref.get("lastDirectory");
        if (curDir.equals(""))
            curDir = ".";
        JFileChooser fc = new JFileChooser(new File(curDir));
        fc.setMultiSelectionEnabled(multiple);
        for (int i = 0; i < ExtensionFileFilter.filters.length; ++i)
            fc.addChoosableFileFilter(ExtensionFileFilter.filters[i]);
        fc.setAcceptAllFileFilterUsed(true);

        int answer = open ? fc.showOpenDialog(Main.parent) : fc.showSaveDialog(Main.parent);
        if (answer != JFileChooser.APPROVE_OPTION)
            return null;

        if (!fc.getCurrentDirectory().getAbsolutePath().equals(curDir))
            Main.pref.put("lastDirectory", fc.getCurrentDirectory().getAbsolutePath());

        if (!open) {
            File file = fc.getSelectedFile();
            if (file == null || (file.exists() && JOptionPane.YES_OPTION !=
                JOptionPane.showConfirmDialog(Main.parent, tr("File exists. Overwrite?"), tr("Overwrite"), JOptionPane.YES_NO_OPTION)))
                return null;
        }

        return fc;
    }

    public static File openFileDialog(boolean open) {
        JFileChooser fc = createAndOpenFileChooser(open, false);
        if (fc == null)
            return null;

        File file = fc.getSelectedFile();

        String fn = file.getPath();
        if (fn.indexOf('.') == -1) {
            FileFilter ff = fc.getFileFilter();
            if (ff instanceof ExtensionFileFilter)
                fn = "." + ((ExtensionFileFilter)ff).defaultExtension;
            else
                fn += ".osm";
            file = new File(fn);
        }
        return file;
    }
}
