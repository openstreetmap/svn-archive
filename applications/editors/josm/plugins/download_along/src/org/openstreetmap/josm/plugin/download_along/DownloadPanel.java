package org.openstreetmap.josm.plugin.download_along;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.GBC;

public class DownloadPanel extends JPanel {

	// Data types to download
    private final JCheckBox cbDownloadOsmData;
    private final JCheckBox cbDownloadGpxData;

    // Legacy list of values
	private static final Integer dist[] = { 5000, 500, 50 };
	private static final Integer area[] = { 20, 10, 5, 1 };
	
	private final JList buffer;
	private final JList maxRect;
	
	// Get value from preferences, taking into account legacy plugin stored only index from list of values instead of real value
	private static final double getPreferenceValue(String prefKey, Integer array[]) {
		int legacyIndex = Main.pref.getInteger(prefKey, array[0]);
		return (0 <= legacyIndex && legacyIndex < array.length)
				? array[legacyIndex] 
				: Main.pref.getDouble(prefKey, array[0]);
	}
	
	/**
	 * Constructs a new {@code DownloadPanel}.
	 */
	public DownloadPanel() {
		super(new GridBagLayout());

		cbDownloadOsmData = new JCheckBox(tr("OpenStreetMap data"), Main.pref.getBoolean(DownloadAlong.PREF_DOWNLOAD_ALONG_OSM, true));
        cbDownloadOsmData.setToolTipText(tr("Select to download OSM data."));
        add(cbDownloadOsmData,  GBC.std().insets(1,5,1,5));
        cbDownloadGpxData = new JCheckBox(tr("Raw GPS data"), Main.pref.getBoolean(DownloadAlong.PREF_DOWNLOAD_ALONG_GPS, false));
        cbDownloadGpxData.setToolTipText(tr("Select to download GPS traces."));
        add(cbDownloadGpxData,  GBC.eol().insets(5,5,1,5));
        
		add(new JLabel(tr("Download everything within:")), GBC.eol());
		String s[] = new String[dist.length];
		for (int i = 0; i < dist.length; ++i) {
			s[i] = tr("{0} meters", dist[i]);
		}
		buffer = new JList(s);
		
		double distanceValue = getPreferenceValue(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_DISTANCE, dist);
		int distanceLegacyIndex = Main.pref.getInteger(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_DISTANCE, dist[0]);
		if (distanceLegacyIndex == dist[0]) {
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
		int areaLegacyIndex = Main.pref.getInteger(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_AREA, area[0]);
		if (areaLegacyIndex == area[0]) {
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
	
	/**
	 * Gets the maximum distance in meters
	 * @return The maximum distance, in meters
	 */
	public final double getDistance() {
		return dist[buffer.getSelectedIndex()];
	}

	/**
	 * Gets the maximum area in squared kilometers
	 * @return The maximum distance, in squared kilometers
	 */
	public final double getArea() {
		return area[maxRect.getSelectedIndex()];
	}
	
    /**
     * Replies true if the user selected to download OSM data
     *
     * @return true if the user selected to download OSM data
     */
    public boolean isDownloadOsmData() {
        return cbDownloadOsmData.isSelected();
    }

    /**
     * Replies true if the user selected to download GPX data
     *
     * @return true if the user selected to download GPX data
     */
    public boolean isDownloadGpxData() {
        return cbDownloadGpxData.isSelected();
    }
	
    /**
     * Remembers the current settings in the download panel
     */
    public final void rememberSettings() {
		Main.pref.putDouble(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_DISTANCE, getDistance());
		Main.pref.putDouble(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_AREA, getArea());
		Main.pref.put(DownloadAlong.PREF_DOWNLOAD_ALONG_OSM, isDownloadOsmData());
		Main.pref.put(DownloadAlong.PREF_DOWNLOAD_ALONG_GPS, isDownloadGpxData());
    }
    
    /**
     * Adds a change listener to comboboxes
     * @param listener The listener that will be notified of each combobox change
     */
    public final void addChangeListener(ChangeListener listener) {
    	cbDownloadGpxData.addChangeListener(listener);
    	cbDownloadOsmData.addChangeListener(listener);
    }
}
