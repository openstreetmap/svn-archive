// License: GPL. For details, see LICENSE file.
package relcontext;

import org.openstreetmap.josm.data.osm.INode;
import org.openstreetmap.josm.data.osm.IRelation;
import org.openstreetmap.josm.data.osm.IWay;
import org.openstreetmap.josm.data.osm.NameFormatterHook;

/**
 * Formatter hook for some tags that Dirk does not want to support.
 *
 * @author Zverik
 */
public class ExtraNameFormatHook implements NameFormatterHook {

    @Override
    public String checkRelationTypeName(IRelation relation, String defaultName) {
        return null;
    }

    @Override
    public String checkFormat(INode node, String defaultName) {
        return null;
    }

    @Override
    public String checkFormat(IWay way, String defaultName) {
        if (way.get("place") != null && way.get("name") == null && way.get("place_name") != null)
            return way.get("place_name") + " " + defaultName;
        return null;
    }

    @Override
    public String checkFormat(IRelation relation, String defaultName) {
        String type = relation.get("type");
        if (type != null) {
            String name = relation.get("destination");
            if (type.equals("destination_sign") && name != null) {
                if (relation.get("distance") != null) {
                    name += " " + relation.get("distance");
                }
                if (defaultName.indexOf('"') < 0)
                    return '"' + name + "\" " + defaultName;
                else
                    return defaultName.replaceFirst("\".?+\"", '"'+name+'"');
            }
        }
        return null;
    }
}
