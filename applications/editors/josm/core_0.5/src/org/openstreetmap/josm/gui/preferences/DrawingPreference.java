// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.gui.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.GBC;

public class DrawingPreference implements PreferenceSetting {

	private JCheckBox drawRawGpsLines = new JCheckBox(tr("Draw lines between raw gps points."));
	private JCheckBox forceRawGpsLines = new JCheckBox(tr("Force lines if no segments imported."));
	private JCheckBox largeGpsPoints = new JCheckBox(tr("Draw large GPS points."));
	private JCheckBox directionHint = new JCheckBox(tr("Draw Direction Arrows"));
	private JCheckBox segmentOrderNumber = new JCheckBox(tr("Draw segment order numbers"));
	private JCheckBox sourceBounds = new JCheckBox(tr("Draw boundaries of downloaded data"));
	private JCheckBox inactive = new JCheckBox(tr("Draw inactive layers in other color"));

	public void addGui(PreferenceDialog gui) {
		// drawRawGpsLines
		drawRawGpsLines.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (!drawRawGpsLines.isSelected())
					forceRawGpsLines.setSelected(false);
				forceRawGpsLines.setEnabled(drawRawGpsLines.isSelected());
			}
		});
		drawRawGpsLines.setSelected(Main.pref.getBoolean("draw.rawgps.lines"));
		drawRawGpsLines.setToolTipText(tr("If your gps device draw too few lines, select this to draw lines along your way."));
		gui.display.add(drawRawGpsLines, GBC.eol().insets(20,0,0,0));

		// forceRawGpsLines
		forceRawGpsLines.setToolTipText(tr("Force drawing of lines if the imported data contain no line information."));
		forceRawGpsLines.setSelected(Main.pref.getBoolean("draw.rawgps.lines.force"));
		forceRawGpsLines.setEnabled(drawRawGpsLines.isSelected());
		gui.display.add(forceRawGpsLines, GBC.eop().insets(40,0,0,0));
		
		// largeGpsPoints
		largeGpsPoints.setSelected(Main.pref.getBoolean("draw.rawgps.large"));
		largeGpsPoints.setToolTipText(tr("Draw larger dots for the GPS points."));
		gui.display.add(largeGpsPoints, GBC.eop().insets(20,0,0,0));
		
		// directionHint
		directionHint.setToolTipText(tr("Draw direction hints for all segments."));
		directionHint.setSelected(Main.pref.getBoolean("draw.segment.direction"));
		gui.display.add(directionHint, GBC.eop().insets(20,0,0,0));
		
		// segment order number
		segmentOrderNumber.setToolTipText(tr("Draw the order numbers of all segments within their way."));
		segmentOrderNumber.setSelected(Main.pref.getBoolean("draw.segment.order_number"));
		gui.display.add(segmentOrderNumber, GBC.eop().insets(20,0,0,0));
		
		// downloaded area
		sourceBounds.setToolTipText(tr("Draw the boundaries of data loaded from the server."));
		sourceBounds.setSelected(Main.pref.getBoolean("draw.data.downloaded_area", true));
		gui.display.add(sourceBounds, GBC.eop().insets(20,0,0,0));
		
		// background layers in inactive color
		inactive.setToolTipText(tr("Draw the inactive data layers in a different color."));
		inactive.setSelected(Main.pref.getBoolean("draw.data.inactive_color", true));
		gui.display.add(inactive, GBC.eop().insets(20,0,0,0));
	}

	public void ok() {
		Main.pref.put("draw.rawgps.lines", drawRawGpsLines.isSelected());
		Main.pref.put("draw.rawgps.lines.force", forceRawGpsLines.isSelected());
		Main.pref.put("draw.rawgps.large", largeGpsPoints.isSelected());
		Main.pref.put("draw.segment.direction", directionHint.isSelected());
		Main.pref.put("draw.segment.order_number", segmentOrderNumber.isSelected());
		Main.pref.put("draw.data.downloaded_area", sourceBounds.isSelected());
		Main.pref.put("draw.data.inactive_color", inactive.isSelected());
    }
}
