// License: GPL. Copyright 2007 by Christian Gallioz (aka khris78)
// Parts of code from Geotagged plugin (by Rob Neild) 
// and the core JOSM source code (by Immanuel Scholz and others)

package org.openstreetmap.josm.plugins.agpifoj;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.agpifoj.AgpifojLayer.ImageEntry;
import org.openstreetmap.josm.tools.ImageProvider;

public class AgpifojDialog extends ToggleDialog implements ActionListener {

    private ImageDisplay imgDisplay = new ImageDisplay();
    private boolean centerView = false;
    
    // Only one instance of thar class
    static private AgpifojDialog INSTANCE = null;
    
    public static AgpifojDialog getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AgpifojDialog();
        }
        return INSTANCE;
    }

    private AgpifojDialog() {
        super(tr("AgPifoJ - Geotagged pictures"), "agpifoj", tr("Display geotagged photos"), KeyEvent.VK_Y, 200);
        
        if (INSTANCE != null) {
            throw new IllegalStateException("Agpifoj dialog should not be instanciated twice !");
        }
        
        INSTANCE = this;
        
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());

        content.add(imgDisplay, BorderLayout.CENTER);
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        
        JButton button;
        
        button = new JButton();
        button.setIcon(ImageProvider.get("dialogs", "previous"));
        button.setActionCommand("previous");
        button.setToolTipText(tr("Previous"));
        button.addActionListener(this);
        buttons.add(button);
        
        button = new JButton();
        button.setIcon(ImageProvider.get("dialogs", "delete"));
        button.setActionCommand("remove");
        button.setToolTipText(tr("Remove photo from layer"));
        button.addActionListener(this);
        buttons.add(button);
        
        button = new JButton();
        button.setIcon(ImageProvider.get("dialogs", "next"));
        button.setActionCommand("next");
        button.setToolTipText(tr("Next"));
        button.addActionListener(this);
        buttons.add(button);
        
        JToggleButton tb = new JToggleButton();
        tb.setIcon(ImageProvider.get("dialogs", "centreview"));
        tb.setActionCommand("centre");
        tb.setToolTipText(tr("Center view"));
        tb.addActionListener(this);
        buttons.add(tb);
        
        button = new JButton();
        button.setIcon(ImageProvider.get("dialogs", "zoom-best-fit"));
        button.setActionCommand("zoom");
        button.setToolTipText(tr("Zoom best fit and 1:1"));
        button.addActionListener(this);
        buttons.add(button);
        
        content.add(buttons, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
        
    }

    public void actionPerformed(ActionEvent e) {
        if ("next".equals(e.getActionCommand())) {
            if (currentLayer != null) {
                currentLayer.showNextPhoto();
            }
        } else if ("previous".equals(e.getActionCommand())) {
            if (currentLayer != null) {
                currentLayer.showPreviousPhoto();
            }
            
        } else if ("centre".equals(e.getActionCommand())) {
            centerView = ((JToggleButton) e.getSource()).isSelected();
            if (centerView && currentEntry != null && currentEntry.pos != null) {
                Main.map.mapView.zoomTo(currentEntry.pos, Main.map.mapView.getScale());
            }
            
        } else if ("zoom".equals(e.getActionCommand())) {
            imgDisplay.zoomBestFitOrOne();
            
        } else if ("remove".equals(e.getActionCommand())) {
            if (currentLayer != null) {
               currentLayer.removeCurrentPhoto();
            }
        }
        
    }

    public static void showImage(AgpifojLayer layer, ImageEntry entry) {
        if (INSTANCE == null) {
            Main.main.map.addToggleDialog(new AgpifojDialog());
        }
        
        if (INSTANCE != null) {
            INSTANCE.displayImage(layer, entry);
        }
        
    }

    private AgpifojLayer currentLayer = null;
    private ImageEntry currentEntry = null;
    
    public void displayImage(AgpifojLayer layer, ImageEntry entry) {
        synchronized(this) {
            if (currentLayer == layer && currentEntry == entry) {
                repaint();
                return;
            }
        
            if (centerView && entry != null && entry.pos != null) {
                Main.map.mapView.zoomTo(entry.pos, Main.map.mapView.getScale());
            }

            currentLayer = layer;
            currentEntry = entry;
        }
        
        imgDisplay.setImage(entry != null ? entry.file : null);
        StringBuffer osd = new StringBuffer(entry.file.getName());
        if (entry.elevation != null) {
            osd.append(tr("\nAltitude: ")).append(entry.elevation.longValue()).append(" m");
        }
        if (entry.speed != null) {
            osd.append("\n").append((long) (3.6 * entry.speed)).append(tr(" km/h"));
        }
        imgDisplay.setOsdText(osd.toString());
    }

}
