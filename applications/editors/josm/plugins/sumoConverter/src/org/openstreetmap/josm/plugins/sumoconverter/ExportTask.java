/**
 * 
 */
package org.openstreetmap.josm.plugins.sumoconvert;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.xml.sax.SAXException;

/**
 * @author ignacio_palermo
 *
 */
public class ExportTask extends PleaseWaitRunnable {

	static Properties sumoConvertProperties = new Properties();
	
	public ExportTask() {
		super("sumoexport");
		try{
			sumoConvertProperties.load(ExportTask.class.getResourceAsStream("/resources/properties/sumoConvert.properties"));
			}
		catch(IOException e){
			e.printStackTrace();
		}
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.PleaseWaitRunnable#cancel()
	 */
	@Override
	protected void cancel() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.PleaseWaitRunnable#finish()
	 */
	@Override
	protected void finish() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.PleaseWaitRunnable#realRun()
	 */
	@Override
	protected void realRun() throws SAXException, IOException,
			OsmTransferException {
		
		try {
			
			String cmd = sumoConvertProperties.getProperty("resources") + 
					sumoConvertProperties.getProperty("netconvert") + 
					sumoConvertProperties.getProperty("netconvert.osmfiles") + " tandil-map.osm" + 
					sumoConvertProperties.getProperty("netconvert.plainoutputprefix") + " tandil-test";
			System.out.println(cmd);
			Runtime.getRuntime().exec(cmd, null, new File("../")); 	
			System.out.println("conversion realizada");
		} catch (IOException e) {
		     e.printStackTrace();
		    }
	}

}
