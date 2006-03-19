package org.openstreetmap.gui;

import org.openstreetmap.util.Node;

public class NodeHandler extends GuiHandler {
	public NodeHandler(Node node) {
		super(node);
		updateBasic();
	}
}
