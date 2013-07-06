package org.openstreetmap.josm.plugin.download_along;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.GBC;

public class DownloadPanel extends JPanel {

	private static final Integer dist[] = { 5000, 500, 50 };
	private static final Integer area[] = { 20, 10, 5, 1 };
	
	private final JList buffer;
	private final JList maxRect;
	
	private static final double getPreferenceValue(String prefKey, Integer array[]) {
		int legacyIndex = Main.pref.getInteger(prefKey, -1);
		return (0 <= legacyIndex && legacyIndex < array.length)
				? array[legacyIndex] 
				: Main.pref.getDouble(prefKey, array[0]);
	}
	
	public DownloadPanel() {
		super(new GridBagLayout());

		add(new JLabel(tr("Download everything within:")), GBC.eol());
		String s[] = new String[dist.length];
		for (int i = 0; i < dist.length; ++i) {
			s[i] = tr("{0} meters", dist[i]);
		}
		buffer = new JList(s);
		
		double distanceValue = getPreferenceValue(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_DISTANCE, dist);
		int distanceLegacyIndex = Main.pref.getInteger(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_DISTANCE, -1);
		if (distanceLegacyIndex == -1) {
			for (int i = 0; i < dist.length; i++) {
				if (dist[i] == (int)distanceValue) {
					distanceLegacyIndex = i;
					break;
				}
			}
		}
		
		buffer.setSelectedIndex(distanceLegacyIndex);
		add(buffer, GBC.eol());

		add(new JLabel(tr("Maximum area per request:")), GBC.eol());
		s = new String[area.length];
		for (int i = 0; i < area.length; ++i) {
			s[i] = tr("{0} sq km", area[i]);
		}
		maxRect = new JList(s);

		double areaValue = getPreferenceValue(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_AREA, area);
		int areaLegacyIndex = Main.pref.getInteger(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_AREA, -1);
		if (areaLegacyIndex == -1) {
			for (int i = 0; i < area.length; i++) {
				if (area[i] == (int)areaValue) {
					areaLegacyIndex = i;
					break;
				}
			}
		}
		
		maxRect.setSelectedIndex(areaLegacyIndex);
		add(maxRect, GBC.eol());
	}
	
	public final double getDistance() {
		return dist[buffer.getSelectedIndex()];
	}

	public final double getArea() {
		return area[maxRect.getSelectedIndex()];
	}
}
