/**
 * 
 */
package org.openstreetmap.processing;

import java.awt.Point;

import org.openstreetmap.gui.GuiHandler;
import org.openstreetmap.gui.GuiLauncher;
import org.openstreetmap.gui.LineHandler;
import org.openstreetmap.gui.NodeHandler;
import org.openstreetmap.gui.WayHandler;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;
import org.openstreetmap.util.Way;


/**
 * The mode to change the properties of an object.
 */
public class PropertiesMode extends EditMode {
	/**
	 * Back reference to the applet.
	 */
	private final OsmApplet applet;
	/**
	 * The primitive that was last selected. To cycle through several primitives
	 * over each other.
	 */
	private OsmPrimitive lastSelected = null;
	/**
	 * The current active dialog. If a new property dialog should be opened, this is
	 * closed first.
	 */
	private GuiLauncher dlg;
	/**
	 * <code>true</code>, if we are in "change segments" mode (then this mode behaves
	 * almost exactly like the WayMode, except that it does not launch WayCreate command)
	 */
	public boolean changeSegmentMode = false;
	/**
	 * The current changed primitive (if any). May be <code>null</code>.
	 */
	private OsmPrimitive primitive;
	
	public PropertiesMode(OsmApplet applet) {
		this.applet = applet;
	}

	public void mouseReleased() {
		OsmPrimitive primitive = applet.getNearest(applet.mouseX, applet.mouseY);
		if (primitive == null)
			return;
		if (changeSegmentMode) {
			if (!(primitive instanceof Line))
				return;
			if (applet.selectedLine.contains(primitive)) {
				applet.selectedLine.remove(primitive);
				if (this.primitive instanceof Way)
					((Way)this.primitive).lines.remove(primitive);
			} else {
				applet.selectedLine.add(primitive);
				if (this.primitive instanceof Way)
					((Way)this.primitive).lines.add(primitive);
			}
			if (dlg != null)
				((WayHandler)dlg.handler).updateSegmentsFromList();
			applet.redraw();
		} else {
			// cycle through all ways and the line segment if subsequent selecting 
			// the point
			if (primitive instanceof Line && !((Line)primitive).ways.isEmpty()) {
				Line line = (Line)primitive;
				if (lastSelected != line.ways.get(line.ways.size()-1)) {
					int i = line.ways.indexOf(lastSelected);
					if (i != -1 && i < line.ways.size()-1)
						primitive = (OsmPrimitive)line.ways.get(i+1);
					else
						primitive = (OsmPrimitive)line.ways.get(0);
				}
			}
			lastSelected = primitive;
			openProperties(primitive);
		}
	}

	/**
	 * Open the property dialog for the given primitive. If the dialog is closed
	 * via Ok, also launch a new property changed command.
	 */
	public void openProperties(final OsmPrimitive old) {
		final GuiHandler guiHandler;
		String name;
		if (old == null)
			return;
		primitive = (OsmPrimitive)old.clone();
		if (primitive instanceof Way) {
			guiHandler = new WayHandler((Way)primitive, applet, this);
			name = ((Way)primitive).getName();
		} else if (primitive instanceof Line) {
			guiHandler = new LineHandler((Line)primitive);
			name = ((Line)primitive).getName();
		} else if (primitive instanceof Node) {
			guiHandler = new NodeHandler((Node)primitive);
			name = (String)primitive.tags.get("name");
		} else
			throw new IllegalArgumentException("unknown class "+primitive.getClass().getName());

		// get a cool name
		if (name == null || name.equals(""))
			name = ""+primitive.id;
		name = "Properties of "+primitive.getTypeName()+" "+name;
		
		
		Point location = dlg != null ? dlg.getLocation() : null;
		if (dlg != null)
			dlg.setVisible(false);
		dlg = new GuiLauncher(name, guiHandler){
			public void setVisible(boolean visible) {
				if (!visible) {
					if (!handler.cancelled) {
						if (primitive instanceof Way && ((Way)primitive).lines.isEmpty())
							applet.osm.removePrimitive(primitive);
						else
							applet.osm.updateProperty(old, primitive);
					}
					dlg = null;
					changeSegmentMode = false;
					applet.selectedLine.clear();
					applet.redraw();
				}
				super.setVisible(visible);
			}
		};
		changeSegmentMode = false;
		if (location != null)
			dlg.setLocation(location);
		dlg.setVisible(true);
	}

	public void draw() {
		applet.fill(0);
		applet.textFont(applet.font);
		applet.textSize(11);
		applet.textAlign(OsmApplet.CENTER);
		applet.text("A", 1 + applet.buttonWidth * 0.5f, 5 + (applet.buttonHeight * 0.5f));
	}

	public String getDescription() {
		return "Change the properties of objects";
	}
}