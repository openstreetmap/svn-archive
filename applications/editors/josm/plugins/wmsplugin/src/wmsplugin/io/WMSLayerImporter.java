package wmsplugin.io;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.io.FileImporter;
import static org.openstreetmap.josm.tools.I18n.tr;

public class WMSLayerImporter extends FileImporter{

	public WMSLayerImporter() {
		super(new ExtensionFileFilter("wms", "wms", tr("WMS Files (*.wms)")));
	}
	
}
