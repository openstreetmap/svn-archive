package wmsplugin.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.io.FileExporter;

public class WMSLayerExporter extends FileExporter{
	
	public WMSLayerExporter() {
		super(new ExtensionFileFilter("wms", "wms", tr("WMS Files (*.wms)")));
	}
}