// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_6;

import java.io.BufferedWriter;
import java.io.File;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.xml.common.BaseXmlWriter;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.v0_6.impl.OsmWriter;


/**
 * An OSM data sink for storing all data to an xml file.
 * 
 * @author Brett Henderson
 */
public class XmlWriter extends BaseXmlWriter implements Sink {
	
	private OsmWriter osmWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 * @param compressionMethod
	 *            Specifies the compression method to employ.
	 * @param enableProdEncodingHack
	 *            If true, a special encoding is enabled which works around an
	 *            encoding issue with the current production configuration where
	 *            data is double encoded as utf-8.
	 */
	public XmlWriter(File file, CompressionMethod compressionMethod, boolean enableProdEncodingHack) {
		super(file, compressionMethod, enableProdEncodingHack);
		
		osmWriter = new OsmWriter("osm", 0, true);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		initialize();
		
		osmWriter.process(entityContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void beginElementWriter() {
		osmWriter.begin();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void endElementWriter() {
		osmWriter.end();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setWriterOnElementWriter(BufferedWriter writer) {
		osmWriter.setWriter(writer);
	}
}
