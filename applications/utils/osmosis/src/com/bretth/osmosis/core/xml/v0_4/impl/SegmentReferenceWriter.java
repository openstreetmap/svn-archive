package com.bretth.osmosis.core.xml.v0_4.impl;

import com.bretth.osmosis.core.domain.v0_4.SegmentReference;
import com.bretth.osmosis.core.xml.common.ElementWriter;


/**
 * Renders a segment reference as xml.
 * 
 * @author Brett Henderson
 */
public class SegmentReferenceWriter extends ElementWriter {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public SegmentReferenceWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
	}
	
	
	/**
	 * Writes the tag.
	 * 
	 * @param segmentReference
	 *            The segmentReference to be processed.
	 */
	public void processSegmentReference(SegmentReference segmentReference) {
		beginOpenElement();
		addAttribute("id", Long.toString(segmentReference.getSegmentId()));
		endOpenElement(true);
	}
}
