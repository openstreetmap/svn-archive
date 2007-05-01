package mappaint;

import java.io.File;
import java.io.FileReader;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class MapPaintPlugin extends Plugin implements LayerChangeListener {

	public static ElemStyles elemStyles = new ElemStyles();
	
	public static String iconsDir;

	public static String getIconsDir(){
		return iconsDir;
	}

	public MapPaintPlugin() {
		iconsDir = getPluginDir()+"icons/"; //some day we will support diferent icon directories over options
		String elemStylesFile = getPluginDir()+"elemstyles.xml";
		File f = new File(elemStylesFile);
		if (f.exists())
		{
			try
			{
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				ElemStyleHandler handler = new ElemStyleHandler();
				xmlReader.setContentHandler(handler);
				xmlReader.setErrorHandler(handler);
				handler.setElemStyles(elemStyles);
				// temporary only!
				xmlReader.parse(new InputSource(new FileReader(f)));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (newFrame != null)
			newFrame.mapView.addLayerChangeListener(this);
		else
			oldFrame.mapView.removeLayerChangeListener(this);
	}

	public void activeLayerChange(Layer oldLayer, Layer newLayer) {}

	public void layerAdded(Layer newLayer) {
		if (newLayer instanceof OsmDataLayer)
			((OsmDataLayer)newLayer).setMapPainter(new MapPaintVisitor());
    }

	public void layerRemoved(Layer oldLayer) {}
}
