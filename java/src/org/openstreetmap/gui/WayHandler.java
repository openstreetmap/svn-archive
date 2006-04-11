/**
 * 
 */
package org.openstreetmap.gui;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.processing.OsmApplet;
import org.openstreetmap.processing.PropertiesMode;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.LineOnlyId;
import org.openstreetmap.util.Way;

import thinlet.Thinlet;

/**
 * The handler class for the way properties dialog.
 *
 * @author Imi
 */
public final class WayHandler extends GuiHandler {

	private PropertiesMode mode = null;
	
	/**
	 * Update the basic tab (name, class or oneway)
	 */
	protected void updateBasic() {
		super.updateBasic();
		Object oneway = find("value_oneway");
		setBoolean(find("oneway"), "selected", getStringAsBoolean(oneway)); 
	}

	/**
	 * Update the thinlet segment list from the list of selected segments. 
	 * Signaled, if the user chanes the segments in the applet.
	 */
	public void updateSegmentsFromList() {
		Object segments = find("segments");
		removeAll(segments);
		for (Iterator it = ((Way)osm).lines.iterator(); it.hasNext();) {
			Line line = (Line)it.next();
			Object item = Thinlet.create("item");
			if (line.getName().equals("")) {
				if (line instanceof LineOnlyId)
					setString(item, "text", line.id+" (incomplete)");
				else
					setString(item, "text", line.id+" ("+line.from.id+" -> "+line.to.id+")");
			} else
				setString(item, "text", line.getName());
			add(segments, item);
			putProperty(item, "line_object", line);
		}
	}
	
	/**
	 * Updates the selected segment list from the gui thinlet list
	 */
	private void updateListFromSegments() {
		Way way = ((Way)osm);
		way.unregister();
		way.lines.clear();
		Object[] segs = getItems(find("segments"));
		for (int i = 0; i < segs.length; ++i)
			way.lines.add(getProperty(segs[i], "line_object"));
		way.register();
		applet.redraw();
	}
	
	/**
	 * @param way The way to handle with this property page
	 * @param applet The applet to redraw and set several properties from
	 * @param mode The property mode to change the "Change segment" flag. <code>null</code>
	 * 		means do not modify anything.
	 */
	public WayHandler(Way way, OsmApplet applet, PropertiesMode mode) {
		super(way, applet);
		this.mode = mode;
		if (mode == null) {
			setBoolean(find("changeSegment"), "selected", true);
			setBoolean(find("changeSegment"), "enabled", false);
		}
		updateSegmentsFromList();
		updateBasic();
	}

	public void ok() {
		updateListFromSegments();
		applet.extraHighlightedLine = null;
		super.ok();
	}

	public void cancel() {
		applet.extraHighlightedLine = null;
		applet.selectedLine.clear();
		super.cancel();
	}

	public void onewayChanged() {
		addOrRemoveOption("oneway");
	}

	public void up() {
		Object list = find("segments");
		Object sel = getSelectedItem(list);
		if (sel == null)
			return;
		List all = Arrays.asList(getItems(list));
		int i = all.indexOf(sel);
		if (i < 1)
			return;
		remove(sel);
		add(list, sel, i-1);
		updateListFromSegments();
	}
	
	public void down() {
		Object list = find("segments");
		Object sel = getSelectedItem(list);
		if (sel == null)
			return;
		List all = Arrays.asList(getItems(list));
		int i = all.indexOf(sel);
		if (i < 0 || i > all.size()-2)
			return;
		remove(sel);
		add(list, sel, i+1);
		updateListFromSegments();
	}
	
	public void deleteSegment() {
		Object list = find("segments");
		Object sel = getSelectedItem(list);
		if (sel == null)
			return;
		Object segment = getProperty(sel, "line_object");
		if (applet.extraHighlightedLine == ((Line)segment).key())
			applet.extraHighlightedLine = null;
		applet.selectedLine.remove(((Line)segment).key());
		remove(sel);
		updateListFromSegments();
		if (getCount(list) == 0 && mode == null)
			cancel();
	}

	/**
	 * Go into the select-deselect line segments modus.
	 */
	public void changeSegment() {
		applet.selectedLine.clear();
		boolean sel = getBoolean(find("changeSegment"), "selected");
		if (mode != null)
			mode.changeSegmentMode = sel;
		if (sel)
			for (Iterator it = ((Way)osm).lines.iterator(); it.hasNext();)
				applet.selectedLine.add(((Line)it.next()).key());
		applet.redraw();
	}

	public void segmentSelectionChanged() {
		Object sel = getSelectedItem(find("segments"));
		if (sel != null)
			applet.extraHighlightedLine = ((Line)getProperty(sel, "line_object")).key();
		else
			applet.extraHighlightedLine = null;
		applet.redraw();
	}
}
