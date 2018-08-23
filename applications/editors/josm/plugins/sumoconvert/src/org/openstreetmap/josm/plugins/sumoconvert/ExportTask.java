/**
 * 
 */
package org.openstreetmap.josm.plugins.sumoconvert;

import java.io.IOException;
import java.util.Properties;

import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.Logging;
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
			Logging.error(e);
		}
	}

	@Override
	protected void cancel() {
		// Do nothing
	}

	@Override
	protected void finish() {
		// Do nothing
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
		    Logging.error(e);
		}
	}
}
