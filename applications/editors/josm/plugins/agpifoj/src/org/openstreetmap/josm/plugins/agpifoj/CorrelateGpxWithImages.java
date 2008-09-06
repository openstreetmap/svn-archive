// License: GPL. Copyright 2007 by Christian Gallioz (aka khris78)
// Parts of code from Geotagged plugin (by Rob Neild) 
// and the core JOSM source code (by Immanuel Scholz and others)

package org.openstreetmap.josm.plugins.agpifoj;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.GpxReader;
import org.openstreetmap.josm.plugins.agpifoj.AgpifojLayer.ImageEntry;
import org.openstreetmap.josm.tools.ExifReader;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.PrimaryDateParser;
import org.xml.sax.SAXException;

/** This class displays the window to select the GPX file and the offset (timezone + delta). 
 * Then it correlates the images of the layer with that GPX file.
 */
public class CorrelateGpxWithImages implements ActionListener {

    private static List<GpxData> loadedGpxData = new ArrayList<GpxData>();

    public static class CorrelateParameters {
        GpxData gpxData;
        float timezone;
        long offset;
    }
    
    AgpifojLayer yLayer = null;
    
    private static class GpxDataWrapper {
        String name;
        GpxData data;
        File file;
        
        public GpxDataWrapper(String name, GpxData data, File file) {
            this.name = name;
            this.data = data; 
            this.file = file;
        }
        
        public String toString() {
            return name;
        }
    }
    
    Vector gpxLst = new Vector();
    JPanel panel = null;
    JComboBox cbGpx = null;
    JTextField tfTimezone = null;
    JTextField tfOffset = null;
    JRadioButton rbAllImg = null;
    JRadioButton rbUntaggedImg = null;
    JRadioButton rbNoExifImg = null;
    
    /** This class is called when the user doesn't find the GPX file he needs in the files that have 
     * been loaded yet. It displays a FileChooser dialog to select the GPX file to be loaded.
     */ 
    private class LoadGpxDataActionListener implements ActionListener {
        
        public void actionPerformed(ActionEvent arg0) {
            JFileChooser fc = new JFileChooser(Main.pref.get("lastDirectory"));
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setMultiSelectionEnabled(false);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setFileFilter(new FileFilter(){
                @Override public boolean accept(File f) {
                    return (f.isDirectory()
                            || f .getName().toLowerCase().endsWith(".gpx") 
                            || f.getName().toLowerCase().endsWith(".gpx.gz"));
                }
                @Override public String getDescription() {
                    return tr("GPX Files (*.gpx *.gpx.gz)");
                }
            });
            fc.showOpenDialog(Main.parent);
            File sel = fc.getSelectedFile();
            if (sel == null)
                return;
            
            try {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                Main.pref.put("lastDirectory", sel.getPath());
                
                for (int i = gpxLst.size() - 1 ; i >= 0 ; i--) {
                    if (gpxLst.get(i) instanceof GpxDataWrapper) {
                        GpxDataWrapper wrapper = (GpxDataWrapper) gpxLst.get(i); 
                        if (sel.equals(wrapper.file)) {
                            cbGpx.setSelectedIndex(i);
                            if (!sel.getName().equals(wrapper.name)) {
                                JOptionPane.showMessageDialog(Main.parent, 
                                        tr("File {0} is loaded yet under the name \"{1}\"", sel.getName(), wrapper.name));
                            }
                            return;
                        }
                    }
                }
                GpxData data = null;
                try {
                    InputStream iStream;
                    if (sel.getName().toLowerCase().endsWith(".gpx.gz")) {
                        iStream = new GZIPInputStream(new FileInputStream(sel));
                    } else {
                        iStream = new FileInputStream(sel); 
                    }
                    data = new GpxReader(iStream, sel).data;
                    data.storageFile = sel;
               
                } catch (SAXException x) {
                    x.printStackTrace();
                    JOptionPane.showMessageDialog(Main.parent, tr("Error while parsing {0}",sel.getName())+": "+x.getMessage());
                    return;
                } catch (IOException x) {
                    x.printStackTrace();
                    JOptionPane.showMessageDialog(Main.parent, tr("Could not read \"{0}\"",sel.getName())+"\n"+x.getMessage());
                    return;
                }
                
                loadedGpxData.add(data);
                if (gpxLst.get(0) instanceof String) {
                    gpxLst.remove(0);
                }
                gpxLst.add(new GpxDataWrapper(sel.getName(), data, sel));
                cbGpx.setSelectedIndex(cbGpx.getItemCount() - 1);
            } finally {
                panel.setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    /** This action listener is called when the user has a photo of the time of his GPS receiver. It 
     * displays the list of photos of the layer, and upon selection displays the selected photo.
     * From that photo, the user can key in the time of the GPS. 
     * Then values of timezone and delta are set. 
     * @author chris
     *
     */
    private class SetOffsetActionListener implements ActionListener {
        JPanel panel;
        JLabel lbExifTime;
        JTextField tfGpsTime;
        JComboBox cbTimezones;
        ImageDisplay imgDisp;
        JList imgList;
        
        public void actionPerformed(ActionEvent arg0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(new JLabel(tr("<html>Take a photo of your GPS receiver while it displays the time.<br>"
                                    + "Display that photo here.<br>"
                                    + "And then, simply capture the time you read on the photo and select a timezone<hr></html>")), 
                                    BorderLayout.NORTH);
            
            imgDisp = new ImageDisplay();
            imgDisp.setPreferredSize(new Dimension(300, 225));
            panel.add(imgDisp, BorderLayout.CENTER);
            
            JPanel panelTf = new JPanel();
            panelTf.setLayout(new GridBagLayout());
            
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = gc.gridy = 0;
            gc.gridwidth = gc.gridheight = 1;
            gc.weightx = gc.weighty = 0.0;
            gc.fill = GridBagConstraints.NONE;
            gc.anchor = GridBagConstraints.WEST;
            panelTf.add(new JLabel(tr("Photo time (from exif):")), gc);

            lbExifTime = new JLabel();
            gc.gridx = 1;
            gc.weightx = 1.0;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.gridwidth = 2;
            panelTf.add(lbExifTime, gc);
            
            gc.gridx = 0;
            gc.gridy = 1;
            gc.gridwidth = gc.gridheight = 1;
            gc.weightx = gc.weighty = 0.0;
            gc.fill = GridBagConstraints.NONE;
            gc.anchor = GridBagConstraints.WEST;
            panelTf.add(new JLabel(tr("Gps time (read from the above photo): ")), gc);

            tfGpsTime = new JTextField();
            tfGpsTime.setEnabled(false);
            tfGpsTime.setMinimumSize(new Dimension(150, tfGpsTime.getMinimumSize().height));
            gc.gridx = 1;
            gc.weightx = 1.0;
            gc.fill = GridBagConstraints.HORIZONTAL;
            panelTf.add(tfGpsTime, gc);

            gc.gridx = 2;
            gc.weightx = 0.2;
            panelTf.add(new JLabel(tr(" [dd/mm/yyyy hh:mm:ss]")), gc);
            
            gc.gridx = 0;
            gc.gridy = 2;
            gc.gridwidth = gc.gridheight = 1;
            gc.weightx = gc.weighty = 0.0;
            gc.fill = GridBagConstraints.NONE;
            gc.anchor = GridBagConstraints.WEST;
            panelTf.add(new JLabel(tr("I'm in the timezone of: ")), gc);
            
            Vector vtTimezones = new Vector<String>();
            String[] tmp = TimeZone.getAvailableIDs();
            
            for (String tzStr : tmp) {
                TimeZone tz = TimeZone.getTimeZone(tzStr);
                 
                String tzDesc = new StringBuffer(tzStr).append(" (")
                                        .append(formatTimezone(tz.getRawOffset() / 3600000.0))
                                        .append(')').toString();
                vtTimezones.add(tzDesc);
            }
            
            Collections.sort(vtTimezones);
            
            cbTimezones = new JComboBox(vtTimezones);
            
            String tzId = Main.pref.get("tagimages.timezoneid", "");
            TimeZone defaultTz;
            if (tzId.length() == 0) {
                defaultTz = TimeZone.getDefault();
            } else {
                defaultTz = TimeZone.getTimeZone(tzId);
            }
            
            cbTimezones.setSelectedItem(new StringBuffer(defaultTz.getID()).append(" (")
                    .append(formatTimezone(defaultTz.getRawOffset() / 3600000.0))
                    .append(')').toString());
            
            gc.gridx = 1;
            gc.weightx = 1.0;
            gc.gridwidth = 2;
            gc.fill = GridBagConstraints.HORIZONTAL;
            panelTf.add(cbTimezones, gc);
            
            panel.add(panelTf, BorderLayout.SOUTH);

            JPanel panelLst = new JPanel();
            panelLst.setLayout(new BorderLayout());
            
            imgList = new JList(new AbstractListModel() {
                public Object getElementAt(int i) {
                    return yLayer.data.get(i).file.getName();
                }

                public int getSize() {
                    return yLayer.data.size();
                }
            });
            imgList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            imgList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent arg0) {
                    int index = imgList.getSelectedIndex();
                    imgDisp.setImage(yLayer.data.get(index).file);
                    Date date = yLayer.data.get(index).time;
                    if (date != null) {
                        lbExifTime.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date));                    
                        tfGpsTime.setText(new SimpleDateFormat("dd/MM/yyyy ").format(date));
                        tfGpsTime.setCaretPosition(tfGpsTime.getText().length());
                        tfGpsTime.setEnabled(true);
                    } else {                        
                        lbExifTime.setText(tr("No date"));
                        tfGpsTime.setText("");
                        tfGpsTime.setEnabled(false);
                    }
                }
                
            });
            panelLst.add(new JScrollPane(imgList), BorderLayout.CENTER);
            
            JButton openButton = new JButton(tr("Open an other photo"));
            openButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    JFileChooser fc = new JFileChooser(Main.pref.get("tagimages.lastdirectory"));
                    fc.setAcceptAllFileFilterUsed(false);
                    fc.setMultiSelectionEnabled(false);
                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fc.setFileFilter(new FileFilter(){
                        @Override public boolean accept(File f) {
                            return (f.isDirectory()
                                    || f .getName().toLowerCase().endsWith(".jpg") 
                                    || f.getName().toLowerCase().endsWith(".jpeg"));
                        }
                        @Override public String getDescription() {
                            return tr("JPEG images (*.jpg)");
                        }
                    });
                    fc.showOpenDialog(Main.parent);
                    File sel = fc.getSelectedFile();
                    if (sel == null) {
                        return;
                    }

                    imgDisp.setImage(sel);

                    Date date = null;
                    try {
                        date = ExifReader.readTime(sel);
                    } catch (Exception e) {
                    }
                    if (date != null) {
                        lbExifTime.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date));                    
                        tfGpsTime.setText(new SimpleDateFormat("dd/MM/yyyy ").format(date));
                        tfGpsTime.setEnabled(true);
                    } else {                        
                        lbExifTime.setText(tr("No date"));
                        tfGpsTime.setText("");
                        tfGpsTime.setEnabled(false);
                    }
                }
            });
            panelLst.add(openButton, BorderLayout.PAGE_END);
            
            panel.add(panelLst, BorderLayout.LINE_START);
            
            boolean isOk = false;
            while (! isOk) {
                int answer = JOptionPane.showConfirmDialog(Main.parent, panel, tr("Synchronize time from a photo of the GPS receiver"), JOptionPane.OK_CANCEL_OPTION);
                if (answer == JOptionPane.CANCEL_OPTION) {
                    return;
                }

                long delta; 
                   
                try {
                    delta = dateFormat.parse(lbExifTime.getText()).getTime() 
                            - dateFormat.parse(tfGpsTime.getText()).getTime();
                } catch(ParseException e) {
                    JOptionPane.showMessageDialog(Main.parent, tr("Error while parsing the date.\n" 
                            + "Please use the requested format"), 
                            tr("Invalid date"), JOptionPane.ERROR_MESSAGE );
                    continue;
                }
                
                String selectedTz = (String) cbTimezones.getSelectedItem();
                int pos = selectedTz.lastIndexOf('(');
                tzId = selectedTz.substring(0, pos - 1);
                String tzValue = selectedTz.substring(pos + 1, selectedTz.length() - 1); 
                
                Main.pref.put("tagimages.timezoneid", tzId);
                tfOffset.setText(Long.toString(delta / 1000));
                tfTimezone.setText(tzValue);
                
                isOk = true;
                
            }
            
        }
    }
    
    public CorrelateGpxWithImages(AgpifojLayer layer) {
        this.yLayer = layer;
    }

    public void actionPerformed(ActionEvent arg0) {
        // Construct the list of loaded GPX tracks
        Collection<Layer> layerLst = Main.main.map.mapView.getAllLayers();
        Iterator<Layer> iterLayer = layerLst.iterator();
        while (iterLayer.hasNext()) {
            Layer cur = iterLayer.next();
            if (cur instanceof GpxLayer) {
                gpxLst.add(new GpxDataWrapper(((GpxLayer) cur).name, 
                                              ((GpxLayer) cur).data, 
                                              ((GpxLayer) cur).data.storageFile)); 
            }
        }
        for (GpxData data : loadedGpxData) {
            gpxLst.add(new GpxDataWrapper(data.storageFile.getName(), 
                                          data,
                                          data.storageFile)); 
        }
        
        if (gpxLst.size() == 0) {
            gpxLst.add(tr("<No GPX track loaded yet>"));
        }
        
        JPanel panelCb = new JPanel();
        panelCb.setLayout(new FlowLayout());
        
        panelCb.add(new JLabel(tr("GPX track: ")));
        
        cbGpx = new JComboBox(gpxLst);
        panelCb.add(cbGpx);
        
        JButton buttonOpen = new JButton(tr("Open an other GPXtrace"));
        buttonOpen.setIcon(ImageProvider.get("agpifoj-open"));
        buttonOpen.addActionListener(new LoadGpxDataActionListener());
        
        panelCb.add(buttonOpen);
        
        JPanel panelTf = new JPanel();
        panelTf.setLayout(new GridBagLayout());
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        
        gc.gridx = gc.gridy = 0;
        gc.gridwidth = gc.gridheight = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = gc.weighty = 0.0;
        panelTf.add(new JLabel(tr("Timezone: ")), gc);

        float gpstimezone = Float.parseFloat(Main.pref.get("tagimages.doublegpstimezone", "0.0"));
        if (gpstimezone == 0.0) {
            gpstimezone = - Long.parseLong(Main.pref.get("tagimages.gpstimezone", "0"));
        }
        tfTimezone = new JTextField();
        tfTimezone.setText(formatTimezone(gpstimezone));

        gc.gridx = 1;
        gc.gridy = 0;
        gc.gridwidth = gc.gridheight = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        panelTf.add(tfTimezone, gc);
        
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = gc.gridheight = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = gc.weighty = 0.0;
        panelTf.add(new JLabel(tr("Offset:")), gc);

        long delta = Long.parseLong(Main.pref.get("tagimages.delta", "0")) / 1000;
        tfOffset = new JTextField();
        tfOffset.setText(Long.toString(delta));
        gc.gridx = gc.gridy = 1;
        gc.gridwidth = gc.gridheight = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        panelTf.add(tfOffset, gc);

        JButton buttonViewGpsPhoto = new JButton(tr("<html>I can take a picture of my GPS receiver.<br>"
                                                    + "Can this help?</html>"));
        buttonViewGpsPhoto.addActionListener(new SetOffsetActionListener());
        gc.gridx = 2;
        gc.gridy = 0;
        gc.gridwidth = 1;
        gc.gridheight = 2;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 0.5;
        gc.weighty = 1.0;
        panelTf.add(buttonViewGpsPhoto, gc);
        
        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = gc.gridheight = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = gc.weighty = 0.0;
        panelTf.add(new JLabel(tr("Update position for: ")), gc);
        
        gc.gridx = 1;
        gc.gridy = 2;
        gc.gridwidth = 2;
        gc.gridheight = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        rbAllImg = new JRadioButton(tr("All images"));
        panelTf.add(rbAllImg, gc);
        
        gc.gridx = 1;
        gc.gridy = 3;
        gc.gridwidth = 2;
        gc.gridheight = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        rbNoExifImg = new JRadioButton(tr("Images with no exif position"));
        panelTf.add(rbNoExifImg, gc);
        
        gc.gridx = 1;
        gc.gridy = 4;
        gc.gridwidth = 2;
        gc.gridheight = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        rbUntaggedImg = new JRadioButton(tr("Not yet tagged images"));
        panelTf.add(rbUntaggedImg, gc);
        
        ButtonGroup group = new ButtonGroup();
        group.add(rbAllImg);
        group.add(rbNoExifImg);
        group.add(rbUntaggedImg);
        
        rbUntaggedImg.setSelected(true);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        panel.add(panelCb, BorderLayout.PAGE_START);
        panel.add(panelTf, BorderLayout.CENTER);

        boolean isOk = false;
        GpxDataWrapper selectedGpx = null;
        while (! isOk) {
            int answer = JOptionPane.showConfirmDialog(Main.parent, panel, tr("Correlate images with GPX track"), JOptionPane.OK_CANCEL_OPTION);
            if (answer == JOptionPane.CANCEL_OPTION) {
                return;
            }
            // Check the selected values
            Object item = cbGpx.getSelectedItem();

            if (item == null || ! (item instanceof GpxDataWrapper)) {
                JOptionPane.showMessageDialog(Main.parent, tr("You should select a GPX track"), 
                                              tr("No selected GPX track"), JOptionPane.ERROR_MESSAGE );
                continue;
            }
            selectedGpx = ((GpxDataWrapper) item);

            Float timezoneValue = parseTimezone(tfTimezone.getText().trim());
            if (timezoneValue == null) {
                JOptionPane.showMessageDialog(Main.parent, tr("Error while parsing timezone.\nExpected format: {0}", "+H:MM"), 
                        tr("Invalid timezone"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            gpstimezone = timezoneValue.floatValue();
            
            try {
                delta = Long.parseLong(tfOffset.getText());
            } catch(NumberFormatException nfe) {
                JOptionPane.showMessageDialog(Main.parent, tr("Error while parsing offset.\nExpected format: {0}", "number"), 
                        tr("Invalid offset"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            Main.pref.put("tagimages.doublegpstimezone", Double.toString(gpstimezone));
            Main.pref.put("tagimages.gpstimezone", Long.toString(- ((long) gpstimezone)));
            Main.pref.put("tagimages.delta", Long.toString(delta * 1000));
            
            isOk = true;
        }
        
        // Construct a list of images that have a date, and sort them on the date.
        ArrayList<ImageEntry> dateImgLst = new ArrayList<ImageEntry>(yLayer.data.size());
        if (rbAllImg.isSelected()) {
            for (ImageEntry e : yLayer.data) {
                if (e.time != null) {
                    dateImgLst.add(e);
                }
            }
        
        } else if (rbNoExifImg.isSelected()) {
            for (ImageEntry e : yLayer.data) {
                if (e.time != null && e.exifCoor == null) {
                    dateImgLst.add(e);
                }
            }

        } else { // rbUntaggedImg.isSelected()  
            for (ImageEntry e : yLayer.data) {
                if (e.time != null && e.coor == null) {
                    dateImgLst.add(e);
                }
            }
        }
        
        int matched = matchGpxTrack(dateImgLst, selectedGpx.data, (long) (gpstimezone * 3600000) + delta * 1000);

        // Search whether an other layer has yet defined some bounding box. 
        // If none, we'll zoom to the bounding box of the layer with the photos.
        Collection<Layer> layerCol = Main.map.mapView.getAllLayers();
        Iterator<Layer> layerIter = layerCol.iterator();
        boolean boundingBoxedLayerFound = false; 
        while (layerIter.hasNext()) {
            Layer l = layerIter.next();
            if (l != yLayer) {
                BoundingXYVisitor bbox = new BoundingXYVisitor();
                l.visitBoundingBox(bbox);
                if (bbox.min != null && bbox.max != null) {
                    boundingBoxedLayerFound = true;
                    break;
                }
            }
        }
        if (! boundingBoxedLayerFound) {
            BoundingXYVisitor bbox = new BoundingXYVisitor();
            yLayer.visitBoundingBox(bbox);
            Main.map.mapView.recalculateCenterScale(bbox);
        }
        
        Main.main.map.repaint();
        
        JOptionPane.showMessageDialog(Main.parent, tr("Found {0} matchs of {1} in GPX track {2}", matched, dateImgLst.size(), selectedGpx.name),
                tr("GPX Track loaded"), 
                ((dateImgLst.size() > 0 && matched == 0) ? JOptionPane.WARNING_MESSAGE 
                                                         : JOptionPane.INFORMATION_MESSAGE));
        
    }

    private int matchGpxTrack(ArrayList<ImageEntry> dateImgLst, GpxData selectedGpx, long offset) {
        int ret = 0;
        
        Collections.sort(dateImgLst, new Comparator<ImageEntry>() {
            public int compare(ImageEntry arg0, ImageEntry arg1) {
                return arg0.time.compareTo(arg1.time);
            }
        });
        
        PrimaryDateParser dateParser = new PrimaryDateParser();
        
        for (GpxTrack trk : selectedGpx.tracks) {
            for (Collection<WayPoint> segment : trk.trackSegs) {
                
                long prevDateWp = 0;
                WayPoint prevWp = null;
                
                for (WayPoint curWp : segment) {
                
                    String curDateWpStr = (String) curWp.attr.get("time"); 
                    if (curDateWpStr != null) {
                        
                        try {
                            long curDateWp = dateParser.parse(curDateWpStr).getTime() + offset;
                            ret += matchPoints(dateImgLst, prevWp, prevDateWp, curWp, curDateWp);
                            
                            prevWp = curWp;
                            prevDateWp = curDateWp;

                        } catch(ParseException e) {
                            System.err.println("Error while parsing date \"" + curDateWpStr + '"');
                            e.printStackTrace();
                            prevWp = null;
                            prevDateWp = 0;
                        }
                    } else {
                        prevWp = null;
                        prevDateWp = 0;
                    }
                }
            }
        }
        return ret;
    }

    private int matchPoints(ArrayList<ImageEntry> dateImgLst, WayPoint prevWp, long prevDateWp, WayPoint curWp, long curDateWp) {
        int ret = 0;
        int i = getLastIndexOfListBefore(dateImgLst, curDateWp);
        if (i >= 0 && i < dateImgLst.size() && dateImgLst.get(i).time.getTime() > prevDateWp) {
            Double speed = null;
            Double prevElevation = null;
            Double curElevation = null;
            if (prevWp != null) {
                double distance = getDistance(prevWp, curWp);
                speed = new Double((1000 * distance) / (curDateWp - prevDateWp));
                try {
                    prevElevation = new Double((String) prevWp.attr.get("ele"));
                } catch(Exception e) {
                }
            }
            try {
                curElevation = new Double((String) curWp.attr.get("ele"));
            } catch (Exception e) {
            }
            
            while(i >= 0 
                    && dateImgLst.get(i).time.getTime() == curDateWp) {
                dateImgLst.get(i).pos = curWp.eastNorth; 
                dateImgLst.get(i).coor = Main.proj.eastNorth2latlon(dateImgLst.get(i).pos);
                dateImgLst.get(i).speed = speed;
                dateImgLst.get(i).elevation = curElevation;
                ret++;
                i--;
            }
            
            if (prevDateWp != 0) {
                long imgDate;
                while(i >= 0 
                        && (imgDate = dateImgLst.get(i).time.getTime()) > prevDateWp) {
                    dateImgLst.get(i).pos = new EastNorth(
                            prevWp.eastNorth.east() + ((curWp.eastNorth.east() - prevWp.eastNorth.east()) * (imgDate - prevDateWp)) / (curDateWp - prevDateWp),
                            prevWp.eastNorth.north() + ((curWp.eastNorth.north() - prevWp.eastNorth.north()) * (imgDate - prevDateWp)) / (curDateWp - prevDateWp)); 
                    dateImgLst.get(i).coor = Main.proj.eastNorth2latlon(dateImgLst.get(i).pos);
                    dateImgLst.get(i).speed = speed;
                    if (curElevation != null && prevElevation != null) {
                        dateImgLst.get(i).elevation = prevElevation + ((curElevation - prevElevation) * (imgDate - prevDateWp)) / (curDateWp - prevDateWp);
                    } 
                    ret++;
                    i--;
                }
            }
        }
        return ret;
    }

    private int getLastIndexOfListBefore(ArrayList<ImageEntry> dateImgLst, long searchedDate) {
        int lstSize = dateImgLst.size();
        if (lstSize == 0 || searchedDate < dateImgLst.get(0).time.getTime()) {
            return -1;
        } else if (searchedDate > dateImgLst.get(lstSize - 1).time.getTime()) {
            return lstSize;
        } else if (searchedDate == dateImgLst.get(lstSize - 1).time.getTime()) {
            return lstSize - 1;
        } else if (searchedDate == dateImgLst.get(0).time.getTime()) {
            int curIndex = 0;
            while (curIndex + 1 < lstSize
                    && dateImgLst.get(curIndex + 1).time.getTime() == searchedDate) {
                curIndex++;
            }
            return curIndex;
        }
        
        int curIndex = 0;
        int startIndex=0;
        int endIndex = lstSize - 1;
        while (endIndex - startIndex > 1) {
            curIndex = (endIndex + startIndex) / 2;
            long curDate = dateImgLst.get(curIndex).time.getTime();
            if (curDate < searchedDate) {
                startIndex = curIndex;
            } else if (curDate > searchedDate) {
                endIndex = curIndex;
            } else {
                // Check that there is no image _after_ that one that have exactly the same date.
                while (curIndex + 1 < lstSize
                        && dateImgLst.get(curIndex + 1).time.getTime() == searchedDate) {
                    curIndex++;
                }
                return curIndex;
            }
        }
        return startIndex;
    }

    private String formatTimezone(double timezone) {
        StringBuffer ret = new StringBuffer();
        
        if (timezone < 0) {
            ret.append('-');
            timezone = -timezone;
        } else {
            ret.append('+');
        }
        ret.append((long) timezone).append(':');
        int minutes = (int) ((timezone % 1) * 60);
        if (minutes < 10) {
            ret.append('0');
        }
        ret.append(minutes);
        
        return ret.toString();
    }
    
    private Float parseTimezone(String timezone) {
        char sgnTimezone = '+';
        String hTimezone = "";
        String mTimezone = "";
        int state = 1; // 1=start/sign, 2=hours, 3=minutes.
        for (int i = 0; i < timezone.length(); i++) {
            char c = timezone.charAt(i);
            switch (c) {
            case ' ' :
                if (state != 2 || hTimezone.length() != 0) {
                    return null;
                }
                break;
            case '+' :
            case '-' : 
                if (state == 1) {
                    sgnTimezone = c;
                    state = 2;
                } else {
                    return null;
                }
                break;
            case ':' : 
            case '.' : 
                if (state == 2) {
                    state = 3;
                } else {
                    return null;
                }
                break;
            case '0' : case '1' : case '2' : case '3' : case '4' : 
            case '5' : case '6' : case '7' : case '8' : case '9' :
                switch(state) {
                case 1 : 
                    state = 2;
                    hTimezone += c;
                    break;
                case 2 : 
                    hTimezone += c;
                    break;
                case 3 : 
                    mTimezone += c;
                    break;
                default : 
                    return null;
                }
                break;
            default : 
                return null;
            }
        }
        int h = Integer.parseInt(hTimezone);
        int m = Integer.parseInt(mTimezone);
        if (h > 12 || m > 59 ) {
            return null;
        }
        return new Float((h + m / 60.0) * (sgnTimezone == '-' ? -1 : 1));
    }

    /** Return the distance in meters between 2 points 
     * Formula and earth radius from : http://en.wikipedia.org/wiki/Great-circle_distance */
    public double getDistance(WayPoint p1, WayPoint p2) {
        double p1Lat = p1.latlon.lat() * Math.PI / 180;
        double p1Lon = p1.latlon.lon() * Math.PI / 180; 
        double p2Lat = p2.latlon.lat() * Math.PI / 180;
        double p2Lon = p2.latlon.lon() * Math.PI / 180;
        double ret = Math.atan2(Math.sqrt(Math.pow(Math.cos(p2Lat) * Math.sin(p2Lon - p1Lon), 2) 
                                          + Math.pow(Math.cos(p1Lat) * Math.sin(p2Lat)
                                                     - Math.sin(p1Lat) * Math.cos(p2Lat) * Math.cos(p2Lon - p1Lon), 2)), 
                                Math.sin(p1Lat) * Math.sin(p2Lat) 
                                + Math.cos(p1Lat) * Math.cos(p2Lat) * Math.cos(p2Lon - p1Lon))
                     * 6372795; // Earth radius, in meters
        return ret;
    }
}
