// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.xml.v0_6.impl;

import java.util.logging.Logger;

import org.xml.sax.Attributes;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.xml.common.BaseElementProcessor;
import org.openstreetmap.osmosis.core.xml.common.ElementProcessor;


/**
 * Provides an element processor implementation for an osm element.
 * 
 * @author Brett Henderson
 */
public class OsmElementProcessor extends SourceElementProcessor {
	
	private static final Logger log = Logger.getLogger(OsmElementProcessor.class.getName());
	
	private static final String ELEMENT_NAME_BOUND = "bound";
	private static final String ELEMENT_NAME_NODE = "node";
	private static final String ELEMENT_NAME_WAY = "way";
	private static final String ELEMENT_NAME_RELATION = "relation";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	
	
	private BoundElementProcessor boundElementProcessor;
	private NodeElementProcessor nodeElementProcessor;
	private WayElementProcessor wayElementProcessor;
	private RelationElementProcessor relationElementProcessor;
	
	private boolean foundBound = false;
	private boolean foundEntities = false;
	private boolean validateVersion;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 *            @param validateVersion If true, a version attribute will be checked and validated.
	 */
	public OsmElementProcessor(
			BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing, boolean validateVersion) {
		super(parentProcessor, sink, enableDateParsing);
		
		this.validateVersion = validateVersion;
		
		boundElementProcessor = new BoundElementProcessor(this, getSink(), enableDateParsing);
		nodeElementProcessor = new NodeElementProcessor(this, getSink(), enableDateParsing);
		wayElementProcessor = new WayElementProcessor(this, getSink(), enableDateParsing);
		relationElementProcessor = new RelationElementProcessor(this, getSink(), enableDateParsing);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		if (validateVersion) {
			String fileVersion;
			
			fileVersion = attributes.getValue(ATTRIBUTE_NAME_VERSION);
			
			if (!XmlConstants.OSM_VERSION.equals(fileVersion)) {
				log.warning(
					"Expected version " + XmlConstants.OSM_VERSION
					+ " but received " + fileVersion + "."
				);
			}
		}
	}
	
	
	/**
	 * Retrieves the appropriate child element processor for the newly
	 * encountered nested element.
	 * 
	 * @param uri
	 *            The element uri.
	 * @param localName
	 *            The element localName.
	 * @param qName
	 *            The element qName.
	 * @return The appropriate element processor for the nested element.
	 */
	@Override
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (ELEMENT_NAME_BOUND.equals(qName)) {
			if (foundEntities) {
				throw new OsmosisRuntimeException("Bound element must come before any entities.");
			}
			if (foundBound) {
				throw new OsmosisRuntimeException("Only one bound element allowed.");
			}
			foundBound = true;
			return boundElementProcessor;
		} else if (ELEMENT_NAME_NODE.equals(qName)) {
			foundEntities = true;
			return nodeElementProcessor;
		} else if (ELEMENT_NAME_WAY.equals(qName)) {
			foundEntities = true;
			return wayElementProcessor;
		} else if (ELEMENT_NAME_RELATION.equals(qName)) {
			foundEntities = true;
			return relationElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		// This class produces no data and therefore doesn't need to do anything
		// when the end of the element is reached.
	}
}
