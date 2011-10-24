//    JOSM PBF plugin.
//    Copyright (C) 2011 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.pbf.action;

import java.util.concurrent.Future;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.pbf.PbfConstants;
import org.openstreetmap.josm.plugins.pbf.io.PbfServerReader;

public class DownloadPbfTask extends DownloadOsmTask implements PbfConstants {

	@Override
	public Future<?> download(boolean newLayer, Bounds downloadArea,
			ProgressMonitor progressMonitor) {
		return null;
	}

	@Override
	public Future<?> loadUrl(boolean newLayer, String url,
			ProgressMonitor progressMonitor) {
        downloadTask = new DownloadTask(newLayer,
                new PbfServerReader(url), progressMonitor);
        // We need submit instead of execute so we can wait for it to finish and get the error
        // message if necessary. If no one calls getErrorMessage() it just behaves like execute.
        return Main.worker.submit(downloadTask);
	}

	@Override
	public boolean acceptsUrl(String url) {
		return url != null && url.endsWith(EXTENSION);
	}
}
