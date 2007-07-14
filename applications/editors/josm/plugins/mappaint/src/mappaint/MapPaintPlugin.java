package mappaint;

import java.io.File;
import java.io.FileReader;

import java.net.URL;
import org.openstreetmap.josm.Main;
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
	
	public static String styleDir;

	public static String getStyleDir(){
		return styleDir;
	}

	public MapPaintPlugin() {
		String styleName = Main.pref.get("mappaint.style", "standard");
		styleDir = Main.pref.getPreferencesDir()+"plugins/mappaint/"+styleName+"/"; //some day we will support different icon directories over options
		String elemStylesFile = getStyleDir()+"elemstyles.xml";
		
		// System.out.println("mappaint: Using style: " + styleName);
		// System.out.println("mappaint: Using style dir: " + styleDir);
		// System.out.println("mappaint: Using style file: " + elemStylesFile);
		
		File f = new File(elemStylesFile);
		if (f.exists())
		{
			try	// reading file from file system
			{
				// System.out.println("mappaint: Using style file: \"" + f + "\"");
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
		else{	// reading the builtin file from the plugin jar file
			URL elemStylesPath = getClass().getResource("/"+styleName+"/elemstyles.xml");
			
			// System.out.println("mappaint: Using jar's elemstyles.xml: \"" + elemStylesPath + "\"");
			if (elemStylesPath != null)
			{
				try
				{
					XMLReader xmlReader = XMLReaderFactory.createXMLReader();
					ElemStyleHandler handler = new ElemStyleHandler();
					xmlReader.setContentHandler(handler);
					xmlReader.setErrorHandler(handler);
					handler.setElemStyles(elemStyles);
					// temporary only!
					xmlReader.parse(new InputSource(elemStylesPath.openStream()));
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			} else {
				System.out.println("mappaint: Couldn't find style: \"" + styleDir + "\"elemstyles.xml");
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
