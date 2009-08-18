package org.openstreetmap.fma.jtiledownloader.views.main.slippymap;

//License: GPL. Copyright 2008 by Jan Peter Stotz

// Adapted for JTileDownloader by Sven Strickroth <email@cs-ware.de>, 2009

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.BBoxLatLonPanel;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 * 
 * Demonstrates the usage of {@link JMapViewer}
 * 
 * @author Jan Peter Stotz
 * 
 */
public class SlippyMapChooserWindow
    extends JFrame
{

    private static final long serialVersionUID = 1L;

    public SlippyMapChooserWindow(BBoxLatLonPanel bboxlatlonpanel)
    {
        super("Slippy Map Chooser");
        setSize(400, 400);
        final SlippyMapChooser map = new SlippyMapChooser(bboxlatlonpanel);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(map, BorderLayout.CENTER);
    }
}
