// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.actions.downloadtasks;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.Collection;

import javax.swing.JCheckBox;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAction;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.download.DownloadDialog.DownloadTask;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.RawGpsLayer;
import org.openstreetmap.josm.gui.layer.RawGpsLayer.GpsPoint;
import org.openstreetmap.josm.io.BoundingBoxDownloader;
import org.xml.sax.SAXException;

public class DownloadGpsTask implements DownloadTask {

	private static class Task extends PleaseWaitRunnable {
		private BoundingBoxDownloader reader;
		private DownloadAction action;
		private Collection<Collection<GpsPoint>> rawData;
		private final boolean newLayer;

		public Task(boolean newLayer, BoundingBoxDownloader reader, DownloadAction action) {
			super(tr("Downloading GPS data"));
			this.reader = reader;
			this.action = action;
			this.newLayer = newLayer;
		}

		@Override public void realRun() throws IOException, SAXException {
			rawData = reader.parseRawGps();
		}

		@Override protected void finish() {
			if (rawData == null)
				return;
			String name = action.dialog.minlat + " " + action.dialog.minlon + " x " + action.dialog.maxlat + " " + action.dialog.maxlon;
			RawGpsLayer layer = new RawGpsLayer(true, rawData, name, null);
			if (newLayer || findMergeLayer() == null)
	            Main.main.addLayer(layer);
			else
				findMergeLayer().mergeFrom(layer);
		}

		private Layer findMergeLayer() {
			if (Main.map == null)
				return null;
	        Layer active = Main.map.mapView.getActiveLayer();
	        if (active != null && active instanceof RawGpsLayer)
	        	return active;
	        for (Layer l : Main.map.mapView.getAllLayers())
	        	if (l instanceof RawGpsLayer && ((RawGpsLayer)l).fromServer)
	        		return l;
	        return null;
        }

		@Override protected void cancel() {
			if (reader != null)
				reader.cancel();
		}
	}

	private JCheckBox checkBox = new JCheckBox(tr("Raw GPS data"));

	public void download(DownloadAction action, double minlat, double minlon, double maxlat, double maxlon) {
		Task task = new Task(action.dialog.newLayer.isSelected(), new BoundingBoxDownloader(minlat, minlon, maxlat, maxlon), action);
		Main.worker.execute(task);
	}

	public JCheckBox getCheckBox() {
	    return checkBox;
    }

	public String getPreferencesSuffix() {
	    return "gps";
    }
}
