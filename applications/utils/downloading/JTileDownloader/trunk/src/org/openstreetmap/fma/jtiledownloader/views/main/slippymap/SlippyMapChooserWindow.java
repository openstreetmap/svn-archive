package org.openstreetmap.fma.jtiledownloader.views.main.slippymap;

//License: GPL. Copyright 2008 by Jan Peter Stotz

// Adapted for JTileDownloader by Sven Strickroth <email@cs-ware.de>, 2009 - 2010

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
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

    private SlippyMapChooser map = null;
    private static final long serialVersionUID = 1L;

    public SlippyMapChooserWindow(BBoxLatLonPanel bboxlatlonpanel, TileProviderIf tileProvider, String tileDirectory)
    {
        super("Slippy Map Chooser");
        setSize(400, 400);
        map = new SlippyMapChooser(bboxlatlonpanel, tileDirectory, tileProvider);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        add(map, BorderLayout.CENTER);
    }

    public SlippyMapChooser getSlippyMapChooser()
    {
        return map;
    }
}
