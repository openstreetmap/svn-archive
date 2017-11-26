/**
 * 
 */
package org.openstreetmap.josm.plugins.sumoconvert;

import java.io.IOException;
import java.util.Properties;

import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.xml.sax.SAXException;

/**
 * @author Ignacio Palermo - Julio Rivera
 *
 */
public class ExportTask extends PleaseWaitRunnable {

	static Properties sumoConvertProperties = new Properties();
	
	public ExportTask() {
		super("sumoexport");
		try {
			sumoConvertProperties.load(ExportTask.class.getResourceAsStream("/resources/properties/sumoConvert.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void cancel() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void finish() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void realRun() throws SAXException, IOException, OsmTransferException {
		try {
			Runtime.getRuntime().exec(sumoConvertProperties.getProperty("resources") +
									  sumoConvertProperties.getProperty("netconvert") +
									  sumoConvertProperties.getProperty("netconvert.osmfiles") +
									  sumoConvertProperties.getProperty("netconvert.plainoutput"),
									  null, 
									  null
			); 			
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
}
