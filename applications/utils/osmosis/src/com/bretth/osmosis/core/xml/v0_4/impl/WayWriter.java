package com.bretth.osmosis.core.xml.v0_4.impl;

import java.io.BufferedWriter;
import java.util.List;

import com.bretth.osmosis.core.domain.v0_4.SegmentReference;
import com.bretth.osmosis.core.domain.v0_4.Tag;
import com.bretth.osmosis.core.domain.v0_4.Way;
import com.bretth.osmosis.core.xml.common.ElementWriter;


/**
 * Renders a way as xml.
 * 
 * @author Brett Henderson
 */
public class WayWriter extends ElementWriter {
	private SegmentReferenceWriter segmentReferenceWriter;
	private TagWriter tagWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public WayWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		tagWriter = new TagWriter("tag", indentLevel + 1);
		segmentReferenceWriter = new SegmentReferenceWriter("seg", indentLevel + 1);
	}
	
	
	/**
	 * Writes the way.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	public void process(Way way) {
		List<SegmentReference> segmentReferences;
		List<Tag> tags;
		
		beginOpenElement();
		addAttribute("id", Long.toString(way.getId()));
		addAttribute("timestamp", formatDate(way.getTimestamp()));
		if (way.getUser() != null && way.getUser().length() > 0) {
			addAttribute("user", way.getUser());
		}
		
		segmentReferences = way.getSegmentReferenceList();
		tags = way.getTagList();
		
		if (segmentReferences.size() > 0 || tags.size() > 0) {
			endOpenElement(false);

			for (SegmentReference segmentReference : segmentReferences) {
				segmentReferenceWriter.processSegmentReference(segmentReference);
			}
			
			for (Tag tag : tags) {
				tagWriter.process(tag);
			}
			
			closeElement();
			
		} else {
			endOpenElement(true);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWriter(BufferedWriter writer) {
		super.setWriter(writer);
		
		segmentReferenceWriter.setWriter(writer);
		tagWriter.setWriter(writer);
	}
}
