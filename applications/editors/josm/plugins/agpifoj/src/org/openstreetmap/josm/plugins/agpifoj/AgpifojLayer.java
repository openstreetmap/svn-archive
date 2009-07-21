// License: GPL. Copyright 2007 by Christian Gallioz (aka khris78)
// Parts of code from Geotagged plugin (by Rob Neild)
// and the core JOSM source code (by Immanuel Scholz and others)

package org.openstreetmap.josm.plugins.agpifoj;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.coor.CachedLatLon;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ExifReader;
import org.openstreetmap.josm.tools.ImageProvider;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

public class AgpifojLayer extends Layer {

    List<ImageEntry> data;

    private Icon icon = ImageProvider.get("dialogs/agpifoj-marker");
    private Icon selectedIcon = ImageProvider.get("dialogs/agpifoj-marker-selected");

    private int currentPhoto = -1;

    // These are used by the auto-guess function to store the result,
    // so when the dialig is re-opened the users modifications don't
    // get overwritten
    public boolean hasTimeoffset = false;
    public long timeoffset = 0;

    /*
     * Stores info about each image
     */

    static final class ImageEntry implements Comparable<ImageEntry> {
        File file;
        Date time;
        LatLon exifCoor;
        CachedLatLon pos;
        /** Speed in kilometer per second */
        Double speed;
        /** Elevation (altitude) in meters */
        Double elevation;

        public void setCoor(LatLon latlon)
        {
            pos = new CachedLatLon(latlon);
        }
        public int compareTo(ImageEntry image) {
            if (time != null && image.time != null) {
                return time.compareTo(image.time);
            } else if (time == null && image.time == null) {
                return 0;
            } else if (time == null) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    /** Loads a set of images, while displaying a dialog that indicates what the plugin is currently doing.
     * In facts, this object is instantiated with a list of files. These files may be JPEG files or
     * directories. In case of directories, they are scanned to find all the images they contain.
     * Then all the images that have be found are loaded as ImageEntry instances.
     */
    private static final class Loader extends PleaseWaitRunnable {

        private boolean cancelled = false;
        private AgpifojLayer layer;
        private final File[] selection;
        private HashSet<String> loadedDirectories = new HashSet<String>();
        private String errorMessage = "";

        public Loader(File[] selection) {
            super(tr("Extracting GPS locations from EXIF"));
            this.selection = selection;
        }

        @Override protected void realRun() throws IOException {

            progressMonitor.subTask(tr("Starting directory scan"));
            List<File> files = new ArrayList<File>();
            try {
                addRecursiveFiles(files, selection);
            } catch(NullPointerException npe) {
                errorMessage += tr("One of the selected files was null !!!");
            }

            if (cancelled) {
                return;
            }

            progressMonitor.subTask(tr("Read photos..."));
            progressMonitor.setTicksCount(files.size());

            // read the image files
            ArrayList<ImageEntry> data = new ArrayList<ImageEntry>(files.size());

            for (File f : files) {

                if (cancelled) {
                    break;
                }

                progressMonitor.subTask(tr("Reading {0}...", f.getName()));
                progressMonitor.worked(1);

                ImageEntry e = new ImageEntry();

                // Changed to silently cope with no time info in exif. One case
                // of person having time that couldn't be parsed, but valid GPS info

                try {
                    e.time = ExifReader.readTime(f);
                } catch (ParseException e1) {
                    e.time = null;
                }
                e.file = f;
                extractExif(e);
                data.add(e);
            }
            layer = new AgpifojLayer(data);
            files.clear();
            progressMonitor.setErrorMessage(errorMessage);
        }

        private void addRecursiveFiles(List<File> files, File[] sel) {
            boolean nullFile = false;

            for (File f : sel) {

                if(cancelled) {
                    break;
                }

                if (f == null) {
                    nullFile = true;

                } else if (f.isDirectory()) {
                    String canonical = null;
                    try {
                        canonical = f.getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorMessage += tr("Unable to get canonical path for directory {0}\n",
                                           f.getAbsolutePath());
                    }

                    if (canonical == null || loadedDirectories.contains(canonical)) {
                        continue;
                    } else {
                        loadedDirectories.add(canonical);
                    }

                    File[] children = f.listFiles(AgpifojPlugin.JPEG_FILE_FILTER);
                    if (children != null) {
                        progressMonitor.subTask(tr("Scanning directory {0}", f.getPath()));
                        try {
                            addRecursiveFiles(files, children);
                        } catch(NullPointerException npe) {
                            npe.printStackTrace();
                            errorMessage += tr("Found null file in directory {0}\n", f.getPath());
                        }
                    } else {
                    	errorMessage += tr("Error while getting files from directory {0}\n", f.getPath());
                    }

                } else {
                      files.add(f);
                }
            }

            if (nullFile) {
                throw new NullPointerException();
            }
        }

        @Override protected void finish() {
            if (layer != null) {
                Main.main.addLayer(layer);
                layer.hook_up_mouse_events(); // Main.map.mapView should exist
                                              // now. Can add mouse listener

                if (! cancelled && layer.data.size() > 0) {
                    boolean noGeotagFound = true;
                    for (ImageEntry e : layer.data) {
                        if (e.pos != null) {
                            noGeotagFound = false;
                        }
                    }
                    if (noGeotagFound) {
                        new CorrelateGpxWithImages(layer).actionPerformed(null);
                    }
                }
            }
        }

        @Override protected void cancel() {
            cancelled = true;
        }
    }

    public static void create(File[] files) {
        Loader loader = new Loader(files);
        Main.worker.execute(loader);
    }

    private AgpifojLayer(final List<ImageEntry> data) {

        super(tr("Geotagged Images"));

        Collections.sort(data);
        this.data = data;
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("dialogs/agpifoj");
    }

    @Override
    public Object getInfoComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Component[] getMenuEntries() {

        JMenuItem correlateItem = new JMenuItem(tr("Correlate to GPX"), ImageProvider.get("dialogs/gpx2img"));
        correlateItem.addActionListener(new CorrelateGpxWithImages(this));

        return new Component[] {
                new JMenuItem(new LayerListDialog.ShowHideLayerAction(this)),
                new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),
                new JMenuItem(new RenameLayerAction(null, this)),
                new JSeparator(),
                correlateItem
                };
    }

    @Override
    public String getToolTipText() {
        int i = 0;
        for (ImageEntry e : data)
            if (e.pos != null)
                i++;
        return data.size() + " " + trn("image", "images", data.size())
                + " loaded. " + tr("{0} were found to be gps tagged.", i);
    }

    @Override
    public boolean isMergable(Layer other) {
        return other instanceof AgpifojLayer;
    }

    @Override
    public void mergeFrom(Layer from) {
        AgpifojLayer l = (AgpifojLayer) from;

        ImageEntry selected = null;
        if (l.currentPhoto >= 0) {
            selected = l.data.get(l.currentPhoto);
        }

        data.addAll(l.data);
        Collections.sort(data);

        // Supress the double photos.
        if (data.size() > 1) {
            ImageEntry cur;
            ImageEntry prev = data.get(data.size() - 1);
            for (int i = data.size() - 2; i >= 0; i--) {
                cur = data.get(i);
                if (cur.file.equals(prev.file)) {
                    data.remove(i);
                } else {
                    prev = cur;
                }
            }
        }

        if (selected != null) {
            for (int i = 0; i < data.size() ; i++) {
                if (data.get(i) == selected) {
                    currentPhoto = i;
                    AgpifojDialog.showImage(AgpifojLayer.this, data.get(i));
                    break;
                }
            }
        }

        name = l.name;

    }

    @Override
    public void paint(Graphics g, MapView mv) {

        int iconWidth = icon.getIconWidth() / 2;
        int iconHeight = icon.getIconHeight() / 2;

        for (ImageEntry e : data) {
            if (e.pos != null) {
                Point p = mv.getPoint(e.pos);

                Rectangle r = new Rectangle(p.x - iconWidth,
                                            p.y - iconHeight,
                                            icon.getIconWidth(),
                                            icon.getIconHeight());
                icon.paintIcon(mv, g, r.x, r.y);
            }
        }

        // Draw the selection on top of the other pictures.
        if (currentPhoto >= 0 && currentPhoto < data.size()) {
            ImageEntry e = data.get(currentPhoto);

            if (e.pos != null) {
                Point p = mv.getPoint(e.pos);

                Rectangle r = new Rectangle(p.x - selectedIcon.getIconWidth() / 2,
                                            p.y - selectedIcon.getIconHeight() / 2,
                                            selectedIcon.getIconWidth(),
                                            selectedIcon.getIconHeight());
                selectedIcon.paintIcon(mv, g, r.x, r.y);
            }
        }
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        for (ImageEntry e : data)
            v.visit(e.pos);
    }

    /*
     * Extract gps from image exif
     *
     * If successful, fills in the LatLon and EastNorth attributes of passed in
     * image;
     */

    private static void extractExif(ImageEntry e) {

        try {
            int deg;
            float min, sec;
            double lon, lat;

            Metadata metadata = JpegMetadataReader.readMetadata(e.file);
            Directory dir = metadata.getDirectory(GpsDirectory.class);

            // longitude

            Rational[] components = dir
                    .getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE);

            deg = components[0].intValue();
            min = components[1].floatValue();
            sec = components[2].floatValue();

            lon = (deg + (min / 60) + (sec / 3600));

            if (dir.getString(GpsDirectory.TAG_GPS_LONGITUDE_REF).charAt(0) == 'W')
                lon = -lon;

            // latitude

            components = dir.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE);

            deg = components[0].intValue();
            min = components[1].floatValue();
            sec = components[2].floatValue();

            lat = (deg + (min / 60) + (sec / 3600));

            if (dir.getString(GpsDirectory.TAG_GPS_LATITUDE_REF).charAt(0) == 'S')
                lat = -lat;

            // Store values

            e.setCoor(new LatLon(lat, lon));
            e.exifCoor = e.pos;

        } catch (Exception p) {
            e.pos = null;
        }
    }

    public void showNextPhoto() {
        if (data != null && data.size() > 0) {
            currentPhoto++;
            if (currentPhoto >= data.size()) {
                currentPhoto = data.size() - 1;
            }
            AgpifojDialog.showImage(this, data.get(currentPhoto));
        } else {
            currentPhoto = -1;
        }
        Main.main.map.repaint();
    }

    public void showPreviousPhoto() {
        if (data != null && data.size() > 0) {
            currentPhoto--;
            if (currentPhoto < 0) {
                currentPhoto = 0;
            }
            AgpifojDialog.showImage(this, data.get(currentPhoto));
        } else {
            currentPhoto = -1;
        }
        Main.main.map.repaint();
    }

    public void removeCurrentPhoto() {
        if (data != null && data.size() > 0 && currentPhoto >= 0 && currentPhoto < data.size()) {
            data.remove(currentPhoto);
            if (currentPhoto >= data.size()) {
                currentPhoto = data.size() - 1;
            }
            if (currentPhoto >= 0) {
                AgpifojDialog.showImage(this, data.get(currentPhoto));
            } else {
                AgpifojDialog.showImage(this, null);
            }
        }
        Main.main.map.repaint();
    }

    private MouseAdapter mouseAdapter = null;

    private void hook_up_mouse_events() {
        mouseAdapter = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {

                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                if (visible)
                    Main.map.mapView.repaint();
            }

            @Override public void mouseReleased(MouseEvent ev) {
                if (ev.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                if (!visible) {
                    return;
                }
                for (int i = data.size() - 1; i >= 0; --i) {
                    ImageEntry e = data.get(i);
                    if (e.pos == null)
                        continue;
                    Point p = Main.map.mapView.getPoint(e.pos);
                    Rectangle r = new Rectangle(p.x - icon.getIconWidth() / 2,
                                                p.y - icon.getIconHeight() / 2,
                                                icon.getIconWidth(),
                                                icon.getIconHeight());
                    if (r.contains(ev.getPoint())) {
                        currentPhoto = i;
                        AgpifojDialog.showImage(AgpifojLayer.this, e);
                        Main.main.map.repaint();
                        break;
                    }
                }
                Main.map.mapView.repaint();
            }
        };
        Main.map.mapView.addMouseListener(mouseAdapter);
        Layer.listeners.add(new LayerChangeListener() {
            public void activeLayerChange(Layer oldLayer, Layer newLayer) {
                if (newLayer == AgpifojLayer.this && currentPhoto >= 0) {
                    Main.main.map.repaint();
                    AgpifojDialog.showImage(AgpifojLayer.this, data.get(currentPhoto));
                }
            }

            public void layerAdded(Layer newLayer) {
            }

            public void layerRemoved(Layer oldLayer) {
                if (oldLayer == AgpifojLayer.this) {
                    Main.map.mapView.removeMouseListener(mouseAdapter);
                    currentPhoto = -1;
                    data.clear();
                    data = null;
                }
            }
        });
    }

}
