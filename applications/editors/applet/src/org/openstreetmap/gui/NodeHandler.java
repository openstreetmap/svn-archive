package org.openstreetmap.gui;

import org.openstreetmap.processing.OsmApplet;
import org.openstreetmap.util.Node;

public class NodeHandler extends GuiHandler {
	public NodeHandler(Node node, OsmApplet applet) {
		super(node, applet);
		updateBasic();
	}
}
