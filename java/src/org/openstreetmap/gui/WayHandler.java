/**
 * 
 */
package org.openstreetmap.gui;

import java.util.Arrays;
import java.util.List;

import org.openstreetmap.processing.OsmApplet;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Way;

import thinlet.Thinlet;

/**
 * The handler class for the way properties dialog.
 *
 * @author Imi
 */
public final class WayHandler extends GuiHandler {

	private final OsmApplet applet;
	
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
		for (int i = 0; i < ((Way)osm).size(); ++i) {
			Line line = ((Way)osm).get(i);
			Object item = Thinlet.create("item");
			if (line.getName().equals(""))
				setString(item, "text", line.id+" ("+line.from.id+" -> "+line.to.id+")");
			else
				setString(item, "text", line.getName());
			add(segments, item);
			putProperty(item, "line_object", line);
		}
	}
	
	/**
	 * Updates the selected segment list from the gui thinlet list
	 */
	private void updateListFromSegments() {
		((Way)osm).removeAll();
		Object[] segs = getItems(find("segments"));
		for (int i = 0; i < segs.length; ++i)
			((Way)osm).add((Line)getProperty(segs[i], "line_object"));
		applet.redraw();
	}
	
	public WayHandler(Way way, OsmApplet applet) {
		super(way);
		this.applet = applet;
		updateSegmentsFromList();
		updateBasic();
	}

	public void ok() {
		Object seg = find("segments");
		Object[] segmentArr = getItems(seg);
		((Way)osm).removeAll();
		for (int i = 0; i < segmentArr.length; ++i)
			((Way)osm).add((Line)getProperty(segmentArr[i], "line_object"));

		super.ok();
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
		remove(sel);
		updateListFromSegments();
		if (getCount(list) == 0)
			cancel();
	}
}
