package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DiskAccessAction;
import org.openstreetmap.josm.actions.SaveActionBase;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.CacheFiles;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.data.Preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;

/**
 * This is a layer that grabs the current screen from an WMS server. The data
 * fetched this way is tiled and managed to the disc to reduce server load.
 */
public class WMSLayer extends Layer implements PreferenceChangedListener {
    protected static final Icon icon =
        new ImageIcon(Toolkit.getDefaultToolkit().createImage(WMSPlugin.class.getResource("/images/wms_small.png")));

    public int messageNum = 5; //limit for messages per layer
    protected MapView mv;
    protected String resolution;
    protected boolean stopAfterPaint = false;
    protected int imageSize = 500;
    protected int dax = 10;
    protected int day = 10;
    protected int minZoom = 3;
    protected double dx = 0.0;
    protected double dy = 0.0;
    protected double pixelPerDegree;
    protected GeorefImage[][] images = new GeorefImage[dax][day];
    JCheckBoxMenuItem startstop = new JCheckBoxMenuItem(tr("Automatic downloading"), true);
    protected JCheckBoxMenuItem alphaChannel = new JCheckBoxMenuItem(new ToggleAlphaAction());
    protected String baseURL;
    protected String cookies;
    protected final int serializeFormatVersion = 5;

    private ExecutorService executor = null;

    /** set to true if this layer uses an invalid base url */
    private boolean usesInvalidUrl = false;
    /** set to true if the user confirmed to use an potentially invalid WMS base url */
    private boolean isInvalidUrlConfirmed = false;

    public WMSLayer() {
        this(tr("Blank Layer"), null, null);
        initializeImages();
        mv = Main.map.mapView;
    }

    public WMSLayer(String name, String baseURL, String cookies) {
        super(name);
        alphaChannel.setSelected(Main.pref.getBoolean("wmsplugin.alpha_channel"));
        setBackgroundLayer(true); /* set global background variable */
        initializeImages();
        this.baseURL = baseURL;
        this.cookies = cookies;
        WMSGrabber.getProjection(baseURL, true);
        mv = Main.map.mapView;

        // quick hack to predefine the PixelDensity to reuse the cache
        int codeIndex = getName().indexOf("#PPD=");
        if (codeIndex != -1) {
            pixelPerDegree = Double.valueOf(getName().substring(codeIndex+5));
        } else {
            pixelPerDegree = getPPD();
        }
        resolution = mv.getDist100PixelText();

        executor = Executors.newFixedThreadPool(
            Main.pref.getInteger("wmsplugin.numThreads",
            WMSPlugin.simultaneousConnections));
        if (baseURL != null && !baseURL.startsWith("html:") && !WMSGrabber.isUrlWithPatterns(baseURL)) {
            if (!(baseURL.endsWith("&") || baseURL.endsWith("?"))) {
                if (!confirmMalformedUrl(baseURL)) {
                    System.out.println(tr("Warning: WMS layer deactivated because of malformed base url ''{0}''", baseURL));
                    usesInvalidUrl = true;
                    setName(getName() + tr("(deactivated)"));
                    return;
                } else {
                    isInvalidUrlConfirmed = true;
                }
            }
        }

        Main.pref.addPreferenceChangeListener(this);
    }

    public boolean hasAutoDownload(){
        return startstop.isSelected();
    }

    public double getDx(){
        return dx;
    }

    public double getDy(){
        return dy;
    }

    @Override
    public void destroy() {
        try {
            executor.shutdownNow();
            // Might not be initialized, so catch NullPointer as well
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

    public double getPPD(){
        ProjectionBounds bounds = mv.getProjectionBounds();
        return mv.getWidth() / (bounds.max.east() - bounds.min.east());
    }

    public void initializeImages() {
        images = new GeorefImage[dax][day];
        for(int x = 0; x<dax; ++x) {
            for(int y = 0; y<day; ++y) {
                images[x][y]= new GeorefImage(false);
            }
        }
    }

    @Override public Icon getIcon() {
        return icon;
    }

    @Override public String getToolTipText() {
        if(startstop.isSelected())
            return tr("WMS layer ({0}), automatically downloading in zoom {1}", getName(), resolution);
        else
            return tr("WMS layer ({0}), downloading in zoom {1}", getName(), resolution);
    }

    @Override public boolean isMergable(Layer other) {
        return false;
    }

    @Override public void mergeFrom(Layer from) {
    }

    private ProjectionBounds XYtoBounds (int x, int y) {
        return new ProjectionBounds(
            new EastNorth(      x * imageSize / pixelPerDegree,       y * imageSize / pixelPerDegree),
            new EastNorth((x + 1) * imageSize / pixelPerDegree, (y + 1) * imageSize / pixelPerDegree));
    }

    private int modulo (int a, int b) {
        return a % b >= 0 ? a%b : a%b+b;
    }

    private boolean zoomIsTooBig() {
        //don't download when it's too outzoomed
        return pixelPerDegree / getPPD() > minZoom;
    }

    @Override public void paint(Graphics2D g, final MapView mv, Bounds bounds) {
        if(baseURL == null) return;
        if (usesInvalidUrl && !isInvalidUrlConfirmed) return;

        if (zoomIsTooBig()) {
            for(int x = 0; x<dax; ++x) {
                for(int y = 0; y<day; ++y) {
                    images[modulo(x,dax)][modulo(y,day)].paint(g, mv, dx, dy);
                }
            }
        } else {
            downloadAndPaintVisible(g, mv);
        }
    }

    public void displace(double dx, double dy) {
        this.dx += dx;
        this.dy += dy;
    }

    protected boolean confirmMalformedUrl(String url) {
        if (isInvalidUrlConfirmed)
            return true;
        String msg  = tr("<html>The base URL<br>"
            + "''{0}''<br>"
            + "for this WMS layer does neither end with a ''&'' nor with a ''?''.<br>"
            + "This is likely to lead to invalid WMS request. You should check your<br>"
            + "preference settings.<br>"
            + "Do you want to fetch WMS tiles anyway?",
            url);
        String [] options = new String[] {
            tr("Yes, fetch images"),
            tr("No, abort")
        };
        int ret = JOptionPane.showOptionDialog(
            Main.parent,
            msg,
            tr("Invalid URL?"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options, options[1]
        );
        switch(ret) {
        case JOptionPane.YES_OPTION: return true;
        default: return false;
        }
    }

    protected void downloadAndPaintVisible(Graphics g, final MapView mv){
        if (usesInvalidUrl)
            return;

        ProjectionBounds bounds = mv.getProjectionBounds();
        int bminx= (int)Math.floor (((bounds.min.east() - dx) * pixelPerDegree) / imageSize );
        int bminy= (int)Math.floor (((bounds.min.north() - dy) * pixelPerDegree) / imageSize );
        int bmaxx= (int)Math.ceil  (((bounds.max.east() - dx) * pixelPerDegree) / imageSize );
        int bmaxy= (int)Math.ceil  (((bounds.max.north() - dy) * pixelPerDegree) / imageSize );

        for(int x = bminx; x<bmaxx; ++x) {
            for(int y = bminy; y<bmaxy; ++y){
                GeorefImage img = images[modulo(x,dax)][modulo(y,day)];
                if(!img.paint(g, mv, dx, dy) && !img.downloadingStarted){
                    img.downloadingStarted = true;
                    img.image = null;
                    img.flushedResizedCachedInstance();
                    Grabber gr = WMSPlugin.getGrabber(XYtoBounds(x,y), img, mv, this);
                    if(!gr.loadFromCache()){
                        gr.setPriority(1);
                        executor.submit(gr);
                    }
                }
            }
        }
    }

    @Override public void visitBoundingBox(BoundingXYVisitor v) {
        for(int x = 0; x<dax; ++x) {
            for(int y = 0; y<day; ++y)
                if(images[x][y].image!=null){
                    v.visit(images[x][y].min);
                    v.visit(images[x][y].max);
                }
        }
    }

    @Override public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override public Component[] getMenuEntries() {
        return new Component[]{
            new JMenuItem(LayerListDialog.getInstance().createActivateLayerAction(this)),
            new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
            new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)),
            new JSeparator(),
            new JMenuItem(new LoadWmsAction()),
            new JMenuItem(new SaveWmsAction()),
            new JMenuItem(new BookmarkWmsAction()),
            new JSeparator(),
            startstop,
            alphaChannel,
            new JMenuItem(new ChangeResolutionAction()),
            new JMenuItem(new ReloadErrorTilesAction()),
            new JMenuItem(new DownloadAction()),
            new JSeparator(),
            new JMenuItem(new LayerListPopup.InfoAction(this))
        };
    }

    public GeorefImage findImage(EastNorth eastNorth) {
        for(int x = 0; x<dax; ++x) {
            for(int y = 0; y<day; ++y)
                if(images[x][y].image!=null && images[x][y].min!=null && images[x][y].max!=null)
                    if(images[x][y].contains(eastNorth, dx, dy))
                        return images[x][y];
        }
        return null;
    }

    public class DownloadAction extends AbstractAction {
        public DownloadAction() {
            super(tr("Download visible tiles"));
        }
        public void actionPerformed(ActionEvent ev) {
            if (zoomIsTooBig()) {
                JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("The requested area is too big. Please zoom in a little, or change resolution"),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE
                );
            } else {
                downloadAndPaintVisible(mv.getGraphics(), mv);
            }
        }
    }

    public class ChangeResolutionAction extends AbstractAction {
        public ChangeResolutionAction() {
            super(tr("Change resolution"));
        }
        public void actionPerformed(ActionEvent ev) {
            initializeImages();
            resolution = mv.getDist100PixelText();
            pixelPerDegree = getPPD();
            mv.repaint();
        }
    }

    public class ReloadErrorTilesAction extends AbstractAction {
        public ReloadErrorTilesAction() {
            super(tr("Reload erroneous tiles"));
        }
        public void actionPerformed(ActionEvent ev) {
            // Delete small files, because they're probably blank tiles.
            // See https://josm.openstreetmap.de/ticket/2307
            WMSPlugin.cache.customCleanUp(CacheFiles.CLEAN_SMALL_FILES, 4096);

            for (int x = 0; x < dax; ++x) {
                for (int y = 0; y < day; ++y) {
                    GeorefImage img = images[modulo(x,dax)][modulo(y,day)];
                    if(img.failed){
                        img.image = null;
                        img.flushedResizedCachedInstance();
                        img.downloadingStarted = false;
                        img.failed = false;
                        mv.repaint();
                    }
                }
            }
        }
    }

    public class ToggleAlphaAction extends AbstractAction {
        public ToggleAlphaAction() {
            super(tr("Alpha channel"));
        }
        public void actionPerformed(ActionEvent ev) {
            JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) ev.getSource();
            boolean alphaChannel = checkbox.isSelected();
            Main.pref.put("wmsplugin.alpha_channel", alphaChannel);

            // clear all resized cached instances and repaint the layer
            for (int x = 0; x < dax; ++x) {
                for (int y = 0; y < day; ++y) {
                    GeorefImage img = images[modulo(x, dax)][modulo(y, day)];
                    img.flushedResizedCachedInstance();
                }
            }
            mv.repaint();
        }
    }

    public class SaveWmsAction extends AbstractAction {
        public SaveWmsAction() {
            super(tr("Save WMS layer to file"), ImageProvider.get("save"));
        }
        public void actionPerformed(ActionEvent ev) {
            File f = SaveActionBase.createAndOpenSaveFileChooser(
                tr("Save WMS layer"), ".wms");
            try {
                if (f != null) {
                    ObjectOutputStream oos = new ObjectOutputStream(
                        new FileOutputStream(f)
                    );
                    oos.writeInt(serializeFormatVersion);
                    oos.writeInt(dax);
                    oos.writeInt(day);
                    oos.writeInt(imageSize);
                    oos.writeDouble(pixelPerDegree);
                    oos.writeObject(getName());
                    oos.writeObject(baseURL);
                    oos.writeObject(images);
                    oos.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    public class LoadWmsAction extends AbstractAction {
        public LoadWmsAction() {
            super(tr("Load WMS layer from file"), ImageProvider.get("load"));
        }
        public void actionPerformed(ActionEvent ev) {
            JFileChooser fc = DiskAccessAction.createAndOpenFileChooser(true,
                false, tr("Load WMS layer"), "wms");
            if(fc == null) return;
            File f = fc.getSelectedFile();
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
                imageSize = ois.readInt();
                pixelPerDegree = ois.readDouble();
                setName((String)ois.readObject());
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
    /**
     * This action will add a WMS layer menu entry with the current WMS layer
     * URL and name extended by the current resolution.
     * When using the menu entry again, the WMS cache will be used properly.
     */
    public class BookmarkWmsAction extends AbstractAction {
        public BookmarkWmsAction() {
            super(tr("Set WMS Bookmark"));
        }
        public void actionPerformed(ActionEvent ev) {
            int i = 0;
            while (Main.pref.hasKey("wmsplugin.url."+i+".url")) {
                i++;
            }
            String baseName;
            // cut old parameter
            int parameterIndex = getName().indexOf("#PPD=");
            if (parameterIndex != -1) {
                baseName = getName().substring(0,parameterIndex);
            }
            else {
                baseName = getName();
            }
            Main.pref.put("wmsplugin.url."+ i +".url",baseURL );
            Main.pref.put("wmsplugin.url."+String.valueOf(i)+".name",
                baseName + "#PPD=" + pixelPerDegree );
            WMSPlugin.refreshMenu();
        }
    }

    public void preferenceChanged(PreferenceChangeEvent event) {
        if (event.getKey().equals("wmsplugin.simultaneousConnections")) {
            executor.shutdownNow();
            executor = Executors.newFixedThreadPool(
                Main.pref.getInteger("wmsplugin.numThreads",
                WMSPlugin.simultaneousConnections));
        }
    }
}
