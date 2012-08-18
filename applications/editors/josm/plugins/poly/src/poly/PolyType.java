package poly;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Extension and file filter for poly type.
 *
 * @author zverik
 */
public interface PolyType {
    public static final String EXTENSION = "poly";
    public static final ExtensionFileFilter FILE_FILTER = new ExtensionFileFilter(EXTENSION, EXTENSION, tr("Osmosis polygon files") + " (*." + EXTENSION + ")");
}
