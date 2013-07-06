package org.openstreetmap.josm.plugin.download_along;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTaskList;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.HelpAwareOptionPane.ButtonSpec;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;

class DownloadAlongAction extends JosmAction {

	public DownloadAlongAction() {
		super(tr("Download along..."), "download_along", tr("Download OSM data along the selected ways."), 
				Shortcut.registerShortcut("tools:download_along", tr("Tool: {0}", tr("Download Along")), 
						KeyEvent.VK_D, Shortcut.ALT_SHIFT), true);
	}

	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> selection = Main.main.getCurrentDataSet().getSelected();

		int ways = 0;
		for (OsmPrimitive prim : selection) {
			if (prim instanceof Way)
				ways++;
		}

		if (ways < 1) {
			JOptionPane.showMessageDialog(Main.parent, tr("Please select 1 or more ways to download along"));
			return;
		}

		DownloadPanel panel = new DownloadPanel();
		
        ButtonSpec[] options = new ButtonSpec[] {
                new ButtonSpec(
                        tr("Download"),
                        ImageProvider.get("download"),
                        tr("Click to download"),
                        null // no specific help text
                ),
                new ButtonSpec(
                        tr("Cancel"),
                        ImageProvider.get("cancel"),
                        tr("Click to cancel"),
                        null // no specific help text
                )
        };

		if (0 != HelpAwareOptionPane.showOptionDialog(Main.parent, panel, tr("Download from OSM along this track"),
				JOptionPane.QUESTION_MESSAGE, null, options, options[0], HelpUtil.ht("/Tools/DownloadAlong"))) {
			return;
		}

		Main.pref.putDouble(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_DISTANCE, panel.getDistance());
		Main.pref.putDouble(DownloadAlong.PREF_DOWNLOAD_ALONG_TRACK_AREA, panel.getArea());

		/*
		 * Find the average latitude for the data we're contemplating, so we
		 * can know how many metres per degree of longitude we have.
		 */
		double latsum = 0;
		int latcnt = 0;

		for (OsmPrimitive prim : selection) {
			if (prim instanceof Way) {
				Way way = (Way) prim;
				for (Node n : way.getNodes()) {
					latsum += n.getCoor().lat();
					latcnt++;
				}
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
		for (OsmPrimitive prim : selection) {
			if (prim instanceof Way) {
				Way way = (Way) prim;
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
		}

		/*
		 * Area "a" now contains the hull that we would like to download
		 * data for. however we can only download rectangles, so the
		 * following is an attempt at finding a number of rectangles to
		 * download.
		 * 
		 * The idea is simply: Start out with the full bounding box. If it
		 * is too large, then split it in half and repeat recursively for
		 * each half until you arrive at something small enough to download.
		 * The algorithm is improved by always using the intersection
		 * between the rectangle and the actual desired area. For example,
		 * if you have a track that goes like this: +----+ | /| | / | | / |
		 * |/ | +----+ then we would first look at downloading the whole
		 * rectangle (assume it's too big), after that we split it in half
		 * (upper and lower half), but we donot request the full upper and
		 * lower rectangle, only the part of the upper/lower rectangle that
		 * actually has something in it.
		 */

		List<Rectangle2D> toDownload = new ArrayList<Rectangle2D>();

		addToDownload(a, a.getBounds(), toDownload, max_area);

		JPanel msg = new JPanel(new GridBagLayout());

		msg.add(new JLabel(tr("<html>This action will require {0} individual<br>"
				+ "download requests. Do you wish<br>to continue?</html>", toDownload.size())), GBC.eol());

		if (toDownload.size() > 1) {
			if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Main.parent, msg, tr("Download from OSM along this track"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
				return;
			}
		}
		final PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor(tr("Download data"));
		final Future<?> future = new DownloadOsmTaskList().download(false, toDownload, monitor);
		Main.worker.submit(new Runnable() {
			public void run() {
				try {
					future.get();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				monitor.close();
			}
		});
	}

	private static void addToDownload(Area a, Rectangle2D r, Collection<Rectangle2D> results, double max_area) {
		Area tmp = new Area(r);
		// intersect with sought-after area
		tmp.intersect(a);
		if (tmp.isEmpty())
			return;
		Rectangle2D bounds = tmp.getBounds2D();
		if (bounds.getWidth() * bounds.getHeight() > max_area) {
			// the rectangle gets too large; split it and make recursive
			// call.
			Rectangle2D r1;
			Rectangle2D r2;
			if (bounds.getWidth() > bounds.getHeight()) {
				// rectangles that are wider than high are split into a left
				// and right
				// half,
				r1 = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth() / 2, bounds.getHeight());
				r2 = new Rectangle2D.Double(bounds.getX() + bounds.getWidth() / 2, bounds.getY(),
						bounds.getWidth() / 2, bounds.getHeight());
			} else {
				// others into a top and bottom half.
				r1 = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight() / 2);
				r2 = new Rectangle2D.Double(bounds.getX(), bounds.getY() + bounds.getHeight() / 2,
						bounds.getWidth(), bounds.getHeight() / 2);
			}
			addToDownload(a, r1, results, max_area);
			addToDownload(a, r2, results, max_area);
		} else {
			results.add(bounds);
		}
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