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
		OsmPrimitive released = applet.getNearest(applet.mouseX, applet.mouseY);
		if (released == null)
			return;
		if (changeSegmentMode) {
			if (!(released instanceof Line))
				return;
			if (applet.selectedLine.contains(released.key())) {
				applet.selectedLine.remove(released.key());
				if (primitive instanceof Way) {
					((Way)primitive).lines.remove(released);
					((Line)released).ways.remove(primitive);
				}
			} else {
				applet.selectedLine.add(released.key());
				if (primitive instanceof Way) {
					((Way)primitive).lines.add(released);
					((Line)released).ways.add(primitive);
				}
			}
			if (dlg != null)
				((WayHandler)dlg.handler).updateSegmentsFromList();
			applet.redraw();
		} else {
			// cycle through all ways and the line segment if subsequent selecting 
			// the point
			if (released instanceof Line && !((Line)released).ways.isEmpty()) {
				Line line = (Line)released;
				if (lastSelected != line.ways.get(line.ways.size()-1)) {
					int i = line.ways.indexOf(lastSelected);
					if (i != -1 && i < line.ways.size()-1)
						released = (OsmPrimitive)line.ways.get(i+1);
					else
						released = (OsmPrimitive)line.ways.get(0);
				}
			}
			lastSelected = released;
			openProperties(released);
		}
	}

	/**
	 * Open the property dialog for the given primitive. If the dialog is closed
	 * via Ok, also launch a new property changed command.
	 */
	public void openProperties(final OsmPrimitive p) {
		final GuiHandler guiHandler;
		String name;
		if (p == null)
			return;
		
		Point location = dlg != null ? dlg.getLocation() : null;
		if (dlg != null)
			dlg.setVisible(false);

		final OsmPrimitive old = (OsmPrimitive)p.clone();
		primitive = p;
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
		
		
		dlg = new GuiLauncher(name, guiHandler){
			public void setVisible(boolean visible) {
				if (!visible)
					doDone(old, handler);
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

	/**
	 * Finish the mode when the dialog get closed. Has to handle cancel as well as ok button.
	 */
	private void doDone(final OsmPrimitive old, GuiHandler handler) {
		if (!handler.cancelled) {
			// ok pressed. Send something to the server.
			// first reset to the status quo. Since the WayHandler has changed the registration
			// back references for 'primitive', we have first to change back to 'old'
			primitive.unregister();
			old.register();
			if (primitive instanceof Way && ((Way)primitive).lines.isEmpty())
				applet.osm.removePrimitive(old);
			else
				applet.osm.updateProperty(old, primitive);
		} else {
			// copy the content from the old back to the changed primitive (undo)
			primitive.copyFrom(old);
		}
		// clean up after the mode
		dlg = null;
		primitive = null;
		changeSegmentMode = false;
		applet.selectedLine.clear();
		applet.extraHighlightedLine = null;
		applet.redraw();
	}
}