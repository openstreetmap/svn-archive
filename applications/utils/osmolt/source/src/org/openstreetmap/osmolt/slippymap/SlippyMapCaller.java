package org.openstreetmap.osmolt.slippymap;

import java.awt.Dimension;

import javax.swing.JPanel;

public interface SlippyMapCaller {
	void addSlipyyMapPane(SlippyMapBBoxChooser SlipyyMapPane);

	void setBoundingBox(double minlon, double minlat, double maxlon, double maxlat);

	Dimension getSlippyMapCurrentSize();

	Dimension getSlippyMapSurroundingSize();

	void setSlippyMapSize(int x, int y, int w, int h);

	public JPanel getSlipyyMapSurroundingPane();

	boolean isSetBBox();

	BBox getBBox();

	void boundingBoxChanged(SlippyMapBBoxChooser chooser);
}
