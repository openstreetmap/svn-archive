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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DiskAccessAction;
import org.openstreetmap.josm.actions.SaveActionBase;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.Preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.CacheFiles;
import org.openstreetmap.josm.tools.ImageProvider;

import wmsplugin.GeorefImage.State;

/**
 * This is a layer that grabs the current screen from an WMS server. The data
 * fetched this way is tiled and managed to the disc to reduce server load.
 */
public class WMSLayer extends Layer implements PreferenceChangedListener {

    protected static final Icon icon =
        new ImageIcon(Toolkit.getDefaultToolkit().createImage(WMSPlugin.class.getResource("/images/wms_small.png")));

    public static final BooleanProperty PROP_ALPHA_CHANNEL = new BooleanProperty("wmsplugin.alpha_channel", true);
    WMSPlugin plugin = WMSPlugin.instance;

    public int messageNum = 5; //limit for messages per layer
    protected MapView mv;
    protected String resolution;
    protected int imageSize = 500;
    protected int dax = 10;
    protected int day = 10;
    protected int daStep = 5;
    protected int minZoom = 3;

    protected double dx = 0.0;
    protected double dy = 0.0;

    protected GeorefImage[][] images;
    protected final int serializeFormatVersion = 5;
    protected boolean autoDownloadEnabled = true;
    protected boolean settingsChanged;
    protected WMSInfo info;

    // Image index boundary for current view
    private volatile int bminx;
    private volatile int bminy;
    private volatile int bmaxx;
    private volatile int bmaxy;
    private volatile int leftEdge;
    private volatile int bottomEdge;

    // Request queue
    private final List<WMSRequest> requestQueue = new ArrayList<WMSRequest>();
    private final List<WMSRequest> finishedRequests = new ArrayList<WMSRequest>();
    private final Lock requestQueueLock = new ReentrantLock();
    private final Condition queueEmpty = requestQueueLock.newCondition();
    private final List<Grabber> grabbers = new ArrayList<Grabber>();
    private final List<Thread> grabberThreads = new ArrayList<Thread>();
    private int threadCount;
    private int workingThreadCount;
    private boolean canceled;


    /** set to true if this layer uses an invalid base url */
    private boolean usesInvalidUrl = false;
    /** set to true if the user confirmed to use an potentially invalid WMS base url */
    private boolean isInvalidUrlConfirmed = false;

    public WMSLayer() {
        this(new WMSInfo(tr("Blank Layer")));
    }

    public WMSLayer(WMSInfo info) {
        super(info.name);
        setBackgroundLayer(true); /* set global background variable */
        initializeImages();
        this.info = new WMSInfo(info);
        mv = Main.map.mapView;
        if(this.info.pixelPerDegree == 0.0)
            this.info.setPixelPerDegree(getPPD());
        resolution = mv.getDist100PixelText();

        if(info.url != null) {
            WMSGrabber.getProjection(info.url, true);
            startGrabberThreads();
            if(!info.url.startsWith("html:") && !WMSGrabber.isUrlWithPatterns(info.url)) {
                if (!(info.url.endsWith("&") || info.url.endsWith("?"))) {
                    if (!confirmMalformedUrl(info.url)) {
                        System.out.println(tr("Warning: WMS layer deactivated because of malformed base url ''{0}''", info.url));
                        usesInvalidUrl = true;
                        setName(getName() + tr("(deactivated)"));
                        return;
                    } else {
                        isInvalidUrlConfirmed = true;
                    }
                }
            }
        }

        Main.pref.addPreferenceChangeListener(this);
    }

    public void doSetName(String name) {
        setName(name);
        info.name = name;
    }

    public boolean hasAutoDownload(){
        return autoDownloadEnabled;
    }


    @Override
    public void destroy() {
        cancelGrabberThreads(false);
        Main.pref.removePreferenceChangeListener(this);
    }

    public void initializeImages() {
        GeorefImage[][] old = images;
        images = new GeorefImage[dax][day];
        if (old != null) {
            for (int i=0; i<old.length; i++) {
                for (int k=0; k<old[i].length; k++) {
                    GeorefImage o = old[i][k];
                    images[modulo(o.getXIndex(),dax)][modulo(o.getYIndex(),day)] = old[i][k];
                }
            }
        }
        for(int x = 0; x<dax; ++x) {
            for(int y = 0; y<day; ++y) {
                if (images[x][y] == null) {
                    images[x][y]= new GeorefImage(this);
                }
            }
        }
    }

    @Override public Icon getIcon() {
        return icon;
    }

    @Override public String getToolTipText() {
        if(autoDownloadEnabled)
            return tr("WMS layer ({0}), automatically downloading in zoom {1}", getName(), resolution);
        else
            return tr("WMS layer ({0}), downloading in zoom {1}", getName(), resolution);
    }

    @Override public boolean isMergable(Layer other) {
        return false;
    }

    @Override public void mergeFrom(Layer from) {
    }

    private int modulo (int a, int b) {
        return a % b >= 0 ? a%b : a%b+b;
    }

    private boolean zoomIsTooBig() {
        //don't download when it's too outzoomed
        return info.pixelPerDegree / getPPD() > minZoom;
    }

    @Override public void paint(Graphics2D g, final MapView mv, Bounds b) {
        if(info.url == null || (usesInvalidUrl && !isInvalidUrlConfirmed)) return;

        settingsChanged = false;

        ProjectionBounds bounds = mv.getProjectionBounds();
        bminx= getImageXIndex(bounds.min.east());
        bminy= getImageYIndex(bounds.min.north());
        bmaxx= getImageXIndex(bounds.max.east());
        bmaxy= getImageYIndex(bounds.max.north());

        leftEdge = (int)(bounds.min.east() * getPPD());
        bottomEdge = (int)(bounds.min.north() * getPPD());

        if (zoomIsTooBig()) {
            for(int x = bminx; x<=bmaxx; ++x) {
                for(int y = bminy; y<=bmaxy; ++y) {
                    images[modulo(x,dax)][modulo(y,day)].paint(g, mv, x, y, leftEdge, bottomEdge);
                }
            }
        } else {
            downloadAndPaintVisible(g, mv, false);
        }
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

    public double getPPD(){
        ProjectionBounds bounds = mv.getProjectionBounds();
        return mv.getWidth() / (bounds.max.east() - bounds.min.east());
    }

    public void displace(double dx, double dy) {
        settingsChanged = true;
        this.dx += dx;
        this.dy += dy;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public int getImageXIndex(double coord) {
        return (int)Math.floor( ((coord - dx) * info.pixelPerDegree) / imageSize);
    }

    public int getImageYIndex(double coord) {
        return (int)Math.floor( ((coord - dy) * info.pixelPerDegree) / imageSize);
    }

    public int getImageX(int imageIndex) {
        return (int)(imageIndex * imageSize * (getPPD() / info.pixelPerDegree) + dx * getPPD());
    }

    public int getImageY(int imageIndex) {
        return (int)(imageIndex * imageSize * (getPPD() / info.pixelPerDegree) + dy * getPPD());
    }

    public int getImageWidth(int xIndex) {
        int overlap = (int)(plugin.PROP_OVERLAP.get()?plugin.PROP_OVERLAP_EAST.get() * imageSize * getPPD() / info.pixelPerDegree / 100:0);
        return getImageX(xIndex + 1) - getImageX(xIndex) + overlap;
    }

    public int getImageHeight(int yIndex) {
        int overlap = (int)(plugin.PROP_OVERLAP.get()?plugin.PROP_OVERLAP_NORTH.get() * imageSize * getPPD() / info.pixelPerDegree / 100:0);
        return getImageY(yIndex + 1) - getImageY(yIndex) + overlap;
    }

    /**
     *
     * @return Size of image in original zoom
     */
    public int getBaseImageWidth() {
        int overlap = (plugin.PROP_OVERLAP.get()?plugin.PROP_OVERLAP_EAST.get() * imageSize / 100:0);
        return imageSize + overlap;
    }

    /**
     *
     * @return Size of image in original zoom
     */
    public int getBaseImageHeight() {
        int overlap = (plugin.PROP_OVERLAP.get()?plugin.PROP_OVERLAP_NORTH.get() * imageSize / 100:0);
        return imageSize + overlap;
    }


    /**
     *
     * @param xIndex
     * @param yIndex
     * @return Real EastNorth of given tile. dx/dy is not counted in
     */
    public EastNorth getEastNorth(int xIndex, int yIndex) {
        return new EastNorth((xIndex * imageSize) / info.pixelPerDegree, (yIndex * imageSize) / info.pixelPerDegree);
    }


    protected void downloadAndPaintVisible(Graphics g, final MapView mv, boolean real){

        int newDax = dax;
        int newDay = day;

        if (bmaxx - bminx >= dax || bmaxx - bminx < dax - 2 * daStep) {
            newDax = ((bmaxx - bminx) / daStep + 1) * daStep;
        }

        if (bmaxy - bminy >= day || bmaxy - bminx < day - 2 * daStep) {
            newDay = ((bmaxy - bminy) / daStep + 1) * daStep;
        }

        if (newDax != dax || newDay != day) {
            dax = newDax;
            day = newDay;
            initializeImages();
        }

        for(int x = bminx; x<=bmaxx; ++x) {
            for(int y = bminy; y<=bmaxy; ++y){
                images[modulo(x,dax)][modulo(y,day)].changePosition(x, y);
            }
        }

        gatherFinishedRequests();

        for(int x = bminx; x<=bmaxx; ++x) {
            for(int y = bminy; y<=bmaxy; ++y){
                GeorefImage img = images[modulo(x,dax)][modulo(y,day)];
                if (!img.paint(g, mv, x, y, leftEdge, bottomEdge)) {
                    WMSRequest request = new WMSRequest(x, y, info.pixelPerDegree, real);
                    addRequest(request);
                }
            }
        }
    }

    @Override public void visitBoundingBox(BoundingXYVisitor v) {
        for(int x = 0; x<dax; ++x) {
            for(int y = 0; y<day; ++y)
                if(images[x][y].getImage() != null){
                    v.visit(images[x][y].getMin());
                    v.visit(images[x][y].getMax());
                }
        }
    }

    @Override public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override public Action[] getMenuEntries() {
        return new Action[]{
                LayerListDialog.getInstance().createActivateLayerAction(this),
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                SeparatorLayerAction.INSTANCE,
                new LoadWmsAction(),
                new SaveWmsAction(),
                new BookmarkWmsAction(),
                SeparatorLayerAction.INSTANCE,
                new StartStopAction(),
                new ToggleAlphaAction(),
                new ChangeResolutionAction(),
                new ReloadErrorTilesAction(),
                new DownloadAction(),
                SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this)
        };
    }

    public GeorefImage findImage(EastNorth eastNorth) {
        int xIndex = getImageXIndex(eastNorth.east());
        int yIndex = getImageYIndex(eastNorth.north());
        GeorefImage result = images[modulo(xIndex, dax)][modulo(yIndex, day)];
        if (result.getXIndex() == xIndex && result.getYIndex() == yIndex) {
            return result;
        } else {
            return null;
        }
    }

    /**
     *
     * @param request
     * @return -1 if request is no longer needed, otherwise priority of request (lower number <=> more important request)
     */
    private int getRequestPriority(WMSRequest request) {
        if (request.getPixelPerDegree() != info.pixelPerDegree) {
            return -1;
        }
        if (bminx > request.getXIndex()
                || bmaxx < request.getXIndex()
                || bminy > request.getYIndex()
                || bmaxy < request.getYIndex()) {
            return -1;
        }

        EastNorth cursorEastNorth = mv.getEastNorth(mv.lastMEvent.getX(), mv.lastMEvent.getY());
        int mouseX = getImageXIndex(cursorEastNorth.east());
        int mouseY = getImageYIndex(cursorEastNorth.north());
        int dx = request.getXIndex() - mouseX;
        int dy = request.getYIndex() - mouseY;

        return dx * dx + dy * dy;
    }

    public WMSRequest getRequest() {
        requestQueueLock.lock();
        try {
            workingThreadCount--;
            Iterator<WMSRequest> it = requestQueue.iterator();
            while (it.hasNext()) {
                WMSRequest item = it.next();
                int priority = getRequestPriority(item);
                if (priority == -1) {
                    it.remove();
                } else {
                    item.setPriority(priority);
                }
            }
            Collections.sort(requestQueue);

            EastNorth cursorEastNorth = mv.getEastNorth(mv.lastMEvent.getX(), mv.lastMEvent.getY());
            int mouseX = getImageXIndex(cursorEastNorth.east());
            int mouseY = getImageYIndex(cursorEastNorth.north());
            boolean isOnMouse = requestQueue.size() > 0 && requestQueue.get(0).getXIndex() == mouseX && requestQueue.get(0).getYIndex() == mouseY;

            // If there is only one thread left then keep it in case we need to download other tile urgently
            while (!canceled &&
                    (requestQueue.isEmpty() || (!isOnMouse && threadCount - workingThreadCount == 0 && threadCount > 1))) {
                try {
                    queueEmpty.await();
                } catch (InterruptedException e) {
                    // Shouldn't happen
                }
            }

            workingThreadCount++;
            if (canceled) {
                return null;
            } else {
                return requestQueue.remove(0);
            }

        } finally {
            requestQueueLock.unlock();
        }
    }

    public void finishRequest(WMSRequest request) {
        if (request.getState() == null) {
            throw new IllegalArgumentException("Finished request without state");
        }
        requestQueueLock.lock();
        try {
            finishedRequests.add(request);
        } finally {
            requestQueueLock.unlock();
        }
    }

    public void addRequest(WMSRequest request) {
        requestQueueLock.lock();
        try {
            if (!requestQueue.contains(request)) {
                requestQueue.add(request);
                queueEmpty.signalAll();
            }
        } finally {
            requestQueueLock.unlock();
        }
    }

    public boolean requestIsValid(WMSRequest request) {
        return bminx <= request.getXIndex() && bmaxx >= request.getXIndex() && bminy <= request.getYIndex() && bmaxy >= request.getYIndex();
    }

    private void gatherFinishedRequests() {
        requestQueueLock.lock();
        try {
            for (WMSRequest request: finishedRequests) {
                GeorefImage img = images[modulo(request.getXIndex(),dax)][modulo(request.getYIndex(),day)];
                if (img.equalPosition(request.getXIndex(), request.getYIndex())) {
                    img.changeImage(request.getState(), request.getImage());
                }
            }
        } finally {
            finishedRequests.clear();
            requestQueueLock.unlock();
        }
    }

    public class DownloadAction extends AbstractAction {
        private static final long serialVersionUID = -7183852461015284020L;
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
                downloadAndPaintVisible(mv.getGraphics(), mv, true);
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
            info.setPixelPerDegree(getPPD());
            settingsChanged = true;
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
            plugin.cache.customCleanUp(CacheFiles.CLEAN_SMALL_FILES, 4096);

            for (int x = 0; x < dax; ++x) {
                for (int y = 0; y < day; ++y) {
                    GeorefImage img = images[modulo(x,dax)][modulo(y,day)];
                    if(img.getState() == State.FAILED){
                        addRequest(new WMSRequest(img.getXIndex(), img.getYIndex(), info.pixelPerDegree, true));
                        mv.repaint();
                    }
                }
            }
        }
    }

    public class ToggleAlphaAction extends AbstractAction implements LayerAction {
        public ToggleAlphaAction() {
            super(tr("Alpha channel"));
        }
        public void actionPerformed(ActionEvent ev) {
            JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) ev.getSource();
            boolean alphaChannel = checkbox.isSelected();
            PROP_ALPHA_CHANNEL.put(alphaChannel);

            // clear all resized cached instances and repaint the layer
            for (int x = 0; x < dax; ++x) {
                for (int y = 0; y < day; ++y) {
                    GeorefImage img = images[modulo(x, dax)][modulo(y, day)];
                    img.flushedResizedCachedInstance();
                }
            }
            mv.repaint();
        }
        public Component createMenuComponent() {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(this);
            item.setSelected(PROP_ALPHA_CHANNEL.get());
            return item;
        }
        public boolean supportLayers(List<Layer> layers) {
            return layers.size() == 1 && layers.get(0) instanceof WMSLayer;
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
                    oos.writeDouble(info.pixelPerDegree);
                    oos.writeObject(info.name);
                    oos.writeObject(info.getFullURL());
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
                autoDownloadEnabled = false;
                dax = ois.readInt();
                day = ois.readInt();
                imageSize = ois.readInt();
                info.setPixelPerDegree(ois.readDouble());
                doSetName((String)ois.readObject());
                info.setURL((String) ois.readObject());
                images = (GeorefImage[][])ois.readObject();
                ois.close();
                fis.close();
                for (GeorefImage[] imgs : images) {
                    for (GeorefImage img : imgs) {
                        if (img != null) {
                            img.setLayer(WMSLayer.this);
                        }
                    }
                }
                settingsChanged = true;
                mv.repaint();
                if(info.url != null)
                {
                    startGrabberThreads();
                }
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
            plugin.addLayer(new WMSInfo(info));
        }
    }

    private class StartStopAction extends AbstractAction implements LayerAction {

        public StartStopAction() {
            super(tr("Automatic downloading"));
        }

        public Component createMenuComponent() {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(this);
            item.setSelected(autoDownloadEnabled);
            return item;
        }

        public boolean supportLayers(List<Layer> layers) {
            return layers.size() == 1 && layers.get(0) instanceof WMSLayer;
        }

        public void actionPerformed(ActionEvent e) {
            autoDownloadEnabled = !autoDownloadEnabled;
            if (autoDownloadEnabled) {
                mv.repaint();
            }
        }
    }

    private void cancelGrabberThreads(boolean wait) {
        requestQueueLock.lock();
        try {
            canceled = true;
            for (Grabber grabber: grabbers) {
                grabber.cancel();
            }
            queueEmpty.signalAll();
        } finally {
            requestQueueLock.unlock();
        }
        if (wait) {
            for (Thread t: grabberThreads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    // Shouldn't happen
                    e.printStackTrace();
                }
            }
        }
    }

    private void startGrabberThreads() {
        int threadCount = plugin.PROP_SIMULTANEOUS_CONNECTIONS.get();
        requestQueueLock.lock();
        try {
            canceled = false;
            grabbers.clear();
            grabberThreads.clear();
            for (int i=0; i<threadCount; i++) {
                Grabber grabber = plugin.getGrabber(mv, this);
                grabbers.add(grabber);
                Thread t = new Thread(grabber, "WMS " + getName() + " " + i);
                t.setDaemon(true);
                t.start();
                grabberThreads.add(t);
            }
            this.workingThreadCount = grabbers.size();
            this.threadCount = grabbers.size();
        } finally {
            requestQueueLock.unlock();
        }
    }

    @Override
    public boolean isChanged() {
        requestQueueLock.lock();
        try {
            return !finishedRequests.isEmpty() || settingsChanged;
        } finally {
            requestQueueLock.unlock();
        }
    }

    public void preferenceChanged(PreferenceChangeEvent event) {
        if (event.getKey().equals(plugin.PROP_SIMULTANEOUS_CONNECTIONS.getKey())) {
            cancelGrabberThreads(true);
            startGrabberThreads();
        } else if (
                event.getKey().equals(plugin.PROP_OVERLAP.getKey())
                || event.getKey().equals(plugin.PROP_OVERLAP_EAST.getKey())
                || event.getKey().equals(plugin.PROP_OVERLAP_NORTH.getKey())) {
            for (int i=0; i<images.length; i++) {
                for (int k=0; k<images[i].length; k++) {
                    images[i][k] = new GeorefImage(this);
                }
            }

            settingsChanged = true;
        }
    }

}
