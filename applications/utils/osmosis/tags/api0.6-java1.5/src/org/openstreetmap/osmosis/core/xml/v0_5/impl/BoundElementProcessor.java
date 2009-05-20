// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_5.impl;

import org.xml.sax.Attributes;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_5.BoundContainer;
import org.openstreetmap.osmosis.core.domain.v0_5.Bound;
import org.openstreetmap.osmosis.core.task.v0_5.Sink;
import org.openstreetmap.osmosis.core.xml.common.BaseElementProcessor;

/**
 * Provides an element processor implementation for a node.
 * 
 * @author Karl Newman
 */
public class BoundElementProcessor extends SourceElementProcessor {
	private static final String ATTRIBUTE_NAME_BOX = "box";
	private static final String ATTRIBUTE_NAME_ORIGIN = "origin";

	private Bound bound;


	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current date will be used
	 *            thus saving parsing time.
	 */
	public BoundElementProcessor(BaseElementProcessor parentProcessor,
	        Sink sink,
	        boolean enableDateParsing) {
		super(parentProcessor, sink, enableDateParsing);
	}


	/**
	 * {@inheritDoc}
	 */
	
	public void begin(Attributes attributes) {
		String boxString;
		String origin;
		String[] boundStrings;
		Double right;
		Double left;
		Double top;
		Double bottom;
		
		boxString = attributes.getValue(ATTRIBUTE_NAME_BOX);
		
		if (boxString == null) {
			throw new OsmosisRuntimeException("Missing required box attribute of bound element");
		}
		boundStrings = boxString.split(",");
		if (boundStrings.length != 4) {
			throw new OsmosisRuntimeException("Badly formed box attribute of bound element");
		}
		try {
			bottom = Double.parseDouble(boundStrings[0]);
			left = Double.parseDouble(boundStrings[1]);
			top = Double.parseDouble(boundStrings[2]);
			right = Double.parseDouble(boundStrings[3]);
		} catch (NumberFormatException e) {
			throw new OsmosisRuntimeException("Can't parse box attribute of bound element", e);
		}
		origin = attributes.getValue(ATTRIBUTE_NAME_ORIGIN);
		if (origin == null || origin.equals("")) {
			throw new OsmosisRuntimeException("Origin attribute of bound element is empty or missing.");
		}
		bound = new Bound(right, left, top, bottom, origin);
	}


	/**
	 * {@inheritDoc}
	 */
	
	public void end() {
		getSink().process(new BoundContainer(bound));
		bound = null;
	}

}
