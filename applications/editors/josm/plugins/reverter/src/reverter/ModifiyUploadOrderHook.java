// License: GPL. For details, see LICENSE file.
package reverter;

import java.util.Collections;
import java.util.Comparator;

import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

import org.openstreetmap.josm.actions.upload.UploadHook;

/**
 * Sort modified objects before uploading in order: nodes, ways, relations
 * It is needed because objects undeleted by reverter is marked as "modified",
 * but in fact they're re-added to JOSM. Without this the "precondition failed"
 * error appears when trying to upload objects undeleted by reverter.
 * 
 */
public class ModifiyUploadOrderHook implements UploadHook {

    public boolean checkUpload(APIDataSet apiDataSet) {
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
        return true;
    }
}
