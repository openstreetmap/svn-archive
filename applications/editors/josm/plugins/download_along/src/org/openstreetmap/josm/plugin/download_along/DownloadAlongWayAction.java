package org.openstreetmap.josm.plugin.download_along;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAlongAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.gpx.DownloadAlongPanel;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;

class DownloadAlongWayAction extends DownloadAlongAction {

	private static final String PREF_DOWNLOAD_ALONG_WAY_DISTANCE = "downloadAlongWay.distance";
	private static final String PREF_DOWNLOAD_ALONG_WAY_AREA = "downloadAlongWay.area";

	private static final String PREF_DOWNLOAD_ALONG_WAY_OSM = "downloadAlongWay.download.osm";
	private static final String PREF_DOWNLOAD_ALONG_WAY_GPS = "downloadAlongWay.download.gps";

	public DownloadAlongWayAction() {
		super(tr("Download along..."), "download_along", tr("Download OSM data along the selected ways."), 
				Shortcut.registerShortcut("tools:download_along", tr("Tool: {0}", tr("Download Along")), 
						KeyEvent.VK_D, Shortcut.ALT_SHIFT), true);
	}

        @Override
	public void actionPerformed(ActionEvent e) {
        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(Main.main.getCurrentDataSet().getSelected(), Way.class);

		if (selectedWays.isEmpty()) {
			JOptionPane.showMessageDialog(Main.parent, tr("Please select 1 or more ways to download along"));
			return;
		}

		final DownloadAlongPanel panel = new DownloadAlongPanel(
				PREF_DOWNLOAD_ALONG_WAY_OSM, PREF_DOWNLOAD_ALONG_WAY_GPS,
				PREF_DOWNLOAD_ALONG_WAY_DISTANCE, PREF_DOWNLOAD_ALONG_WAY_AREA, null);

		if (0 != panel.showInDownloadDialog(tr("Download from OSM along selected ways"), HelpUtil.ht("/Tools/DownloadAlong"))) {
			return;
		}

		/*
		 * Find the average latitude for the data we're contemplating, so we
		 * can know how many metres per degree of longitude we have.
		 */
		double latsum = 0;
		int latcnt = 0;

		for (Way way : selectedWays) {
			for (Node n : way.getNodes()) {
				latsum += n.getCoor().lat();
				latcnt++;
			}
		}

		double avglat = latsum / latcnt;
		double scale = Math.cos(Math.toRadians(avglat));

		/*
		 * Compute buffer zone extents and maximum bounding box size. Note
		 * that the maximum we ever offer is a bbox area of 0.002, while the
		 * API theoretically supports 0.25, but as soon as you touch any
		 * built-up area, that kind of bounding box will download forever
		 * and then stop because it has more than 50k nodes.
		 */
		double buffer_dist = panel.getDistance();
		double buffer_y = buffer_dist / 100000.0;
		double buffer_x = buffer_y / scale;
		double max_area = panel.getArea() / 10000.0 / scale;
		Area a = new Area();
		Rectangle2D r = new Rectangle2D.Double();

		/*
		 * Collect the combined area of all gpx points plus buffer zones
		 * around them. We ignore points that lie closer to the previous
		 * point than the given buffer size because otherwise this operation
		 * takes ages.
		 */
		LatLon previous = null;
		for (Way way : selectedWays) {
			for (Node p : way.getNodes()) {
				LatLon c = p.getCoor();
				ArrayList<LatLon> intermediateNodes = new ArrayList<LatLon>();
				if (previous != null && c.greatCircleDistance(previous) > buffer_dist) {
					Double d = c.greatCircleDistance(previous) / buffer_dist;
					int nbNodes = d.intValue();
					System.out.println(tr("{0} intermediate nodes to download.", nbNodes));
					System.out.println(tr("between {0} {1} and {2} {3}", c.lat(), c.lon(), previous.lat(),
							previous.lon()));
					for (int i = 1; i < nbNodes; i++) {
						intermediateNodes.add(new LatLon(previous.lat()
								+ (i * (c.lat() - previous.lat()) / (nbNodes + 1)), previous.lon()
								+ (i * (c.lon() - previous.lon()) / (nbNodes + 1))));
						System.out.println(tr("  adding {0} {1}", previous.lat()
								+ (i * (c.lat() - previous.lat()) / (nbNodes + 1)), previous.lon()
								+ (i * (c.lon() - previous.lon()) / (nbNodes + 1))));
					}
				}
				intermediateNodes.add(c);
				for (LatLon d : intermediateNodes) {
					if (previous == null || d.greatCircleDistance(previous) > buffer_dist) {
						// we add a buffer around the point.
						r.setRect(d.lon() - buffer_x, d.lat() - buffer_y, 2 * buffer_x, 2 * buffer_y);
						a.add(new Area(r));
						previous = d;
					}
				}
				previous = c;
			}
		}
		
		confirmAndDownloadAreas(a, max_area, panel.isDownloadOsmData(), panel.isDownloadGpxData(), 
				tr("Download from OSM along selected ways"), NullProgressMonitor.INSTANCE);
	}

	@Override
	protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
	}

	@Override
	protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(Utils.exists(selection, OsmPrimitive.wayPredicate));
	}
}