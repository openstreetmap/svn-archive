// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import org.openstreetmap.hot.sds.SeparateDataStorePlugin.QueueItem;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerWritePostprocessor;

public class WritePostprocessor implements OsmServerWritePostprocessor {

    SeparateDataStorePlugin plugin;

    public WritePostprocessor(SeparateDataStorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void postprocessUploadedPrimitives(Collection<OsmPrimitive> primitives, ProgressMonitor progress) {

        StringWriter swriter = new StringWriter();
        try (SdsWriter sdsWriter = new SdsWriter(new PrintWriter(swriter))) {
            sdsWriter.header();
            boolean somethingWritten = false;

            for (OsmPrimitive p : primitives) {
                for (QueueItem q : plugin.uploadQueue) {
                    if (q.primitive.equals(p) && !q.sdsOnly) {
                        sdsWriter.write(q.primitive, q.tags);
                        somethingWritten = true;
                        q.processed = true;
                        continue;
                    }
                }
            }

            for (QueueItem q : plugin.uploadQueue) {
                if (q.sdsOnly) {
                    sdsWriter.write(q.primitive, q.tags);
                    somethingWritten = true;
                    q.processed = true;
                }
            }

            if (somethingWritten) {
                sdsWriter.footer();

                SdsApi api = SdsApi.getSdsApi();
                System.out.println("sending message:\n" + swriter.toString());
                api.updateSds(swriter.toString(), progress);
            }
        }

        for (OsmPrimitive p : primitives) {
            plugin.learn(p);
        }

        for (QueueItem q : plugin.uploadQueue) {
            if (q.sdsOnly) {
                q.primitive.setModified(false);
                plugin.learn(q.primitive);
            }
        }

        plugin.clearQueue();
        // TODO: if exception -> resetQueue
    }

}
