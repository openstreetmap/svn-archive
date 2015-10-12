package org.openstreetmap.josm.plugins.tofix.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.tofix.TofixDialog;

/**
 *
 * @author ruben
 */
public class Download {

    private static Future<?> future;

    public static void Download(final DownloadOsmTask task, Bounds bounds, final Long osm_obj_id) {
        ProgressMonitor monitor = null;
        final Future<?> future = task.download(true, bounds, monitor);
        Runnable runAfterTask = new Runnable() {

            @Override
            public void run() {
                try {
                    if (osm_obj_id != 0) {
                        future.get();
                        //create object
                        Node node = new Node(osm_obj_id);
                        Relation relation = new Relation(osm_obj_id);
                        Way way = new Way(osm_obj_id);
                        //create list of objects
                        List<OsmPrimitive> selection = new ArrayList<>();
                        selection.add(way);
                        selection.add(node);
                        selection.add(relation);
                        //make selection ob objects
                        Main.main.getCurrentDataSet().setSelected(selection);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(TofixDialog.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(TofixDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        Main.worker.submit(runAfterTask);
    }
}
