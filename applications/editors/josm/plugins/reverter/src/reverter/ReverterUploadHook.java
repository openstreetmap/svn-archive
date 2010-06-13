// License: GPL. For details, see LICENSE file.
package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

import org.openstreetmap.josm.actions.upload.UploadHook;

public class ReverterUploadHook implements UploadHook {
    private UndeletedObjectsStorage undeletedStorage;
    
    public ReverterUploadHook(UndeletedObjectsStorage undeletedStorage) {
        this.undeletedStorage = undeletedStorage;
    }
    
    public boolean checkUpload(APIDataSet apiDataSet) {
        // Determine DataSet associated with APIDataSet
        DataSet ds = null;
        if (!apiDataSet.getPrimitivesToAdd().isEmpty()) {
            ds = apiDataSet.getPrimitivesToAdd().get(0).getDataSet();
        } else if (!apiDataSet.getPrimitivesToUpdate().isEmpty()) {
            ds = apiDataSet.getPrimitivesToUpdate().get(0).getDataSet();
        } else if (!apiDataSet.getPrimitivesToDelete().isEmpty()) {
            ds = apiDataSet.getPrimitivesToDelete().get(0).getDataSet();
        }
        if (ds == null) return true;
        
        /* Sort modified objects before uploading in order: nodes, ways, relations.
         * It is needed because objects undeleted by reverter is marked as "modified", but they
         * cannot be referenced as well as deleted objects. Without this the "precondition failed"
         * error appears when trying to upload objects undeleted by reverter.
         */
        if (undeletedStorage.haveUndeletedObjects(ds)) {
            Collections.sort(
                    apiDataSet.getPrimitivesToUpdate(),
                    new Comparator<OsmPrimitive>() {
                        public int compare(OsmPrimitive o1, OsmPrimitive o2) {
                            if (o1 instanceof Node && o2 instanceof Node)
                                return 0;
                            else if (o1 instanceof Node)
                                return -1;
                            else if (o2 instanceof Node)
                                return 1;
            
                            if (o1 instanceof Way && o2 instanceof Way)
                                return 0;
                            else if (o1 instanceof Way && o2 instanceof Relation)
                                return -1;
                            else if (o2 instanceof Way && o1 instanceof Relation)
                                return 1;
            
                            return 0;
                        }
                    }
                    );
        }
        /* Remove primitives that was undeleted and deleted again from list of primitives
         * to be deleted.
         */
        apiDataSet.getPrimitivesToDelete().removeAll(undeletedStorage.getUndeletedObjects(ds));

        if (apiDataSet.isEmpty()) {
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("No changes to upload."),
                    tr("Warning"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return false;
        }
        return true;
    }
}
