package org.openstreetmap.josm.plugins.videomapping.video;
import java.awt.Canvas;
import java.io.File;

import javax.swing.JComponent;

// a specific synced video
public class GPSVideo extends Video{
	public JComponent SyncComponent;
	public GPSVideo(File filename, Canvas canvas) {
		super(filename, canvas);
	}
   

}
