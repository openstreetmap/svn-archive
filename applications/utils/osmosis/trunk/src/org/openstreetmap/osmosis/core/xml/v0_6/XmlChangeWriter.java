// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_6;

import java.io.BufferedWriter;
import java.io.File;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.xml.common.BaseXmlWriter;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.v0_6.impl.OsmChangeWriter;


/**
 * An OSM change sink for storing all data to an xml file.
 * 
 * @author Brett Henderson
 */
public class XmlChangeWriter extends BaseXmlWriter implements ChangeSink {
	
	private OsmChangeWriter osmChangeWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 * @param compressionMethod
	 *            Specifies the compression method to employ.
	 */
	public XmlChangeWriter(File file, CompressionMethod compressionMethod) {
		super(file, compressionMethod);
		
		osmChangeWriter = new OsmChangeWriter("osmChange", 0);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer changeContainer) {
		initialize();
		
		osmChangeWriter.process(changeContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void beginElementWriter() {
		osmChangeWriter.begin();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void endElementWriter() {
		osmChangeWriter.end();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setWriterOnElementWriter(BufferedWriter writer) {
		osmChangeWriter.setWriter(writer);
	}
}
