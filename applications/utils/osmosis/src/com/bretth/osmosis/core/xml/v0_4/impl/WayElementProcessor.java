package com.bretth.osmosis.core.xml.v0_4.impl;

import java.util.Date;

import org.xml.sax.Attributes;

import com.bretth.osmosis.core.container.v0_4.WayContainer;
import com.bretth.osmosis.core.domain.v0_4.SegmentReference;
import com.bretth.osmosis.core.domain.v0_4.Tag;
import com.bretth.osmosis.core.domain.v0_4.Way;
import com.bretth.osmosis.core.task.v0_4.Sink;
import com.bretth.osmosis.core.xml.common.BaseElementProcessor;
import com.bretth.osmosis.core.xml.common.ElementProcessor;


/**
 * Provides an element processor implementation for a way.
 * 
 * @author Brett Henderson
 */
public class WayElementProcessor extends SourceElementProcessor implements TagListener, SegmentReferenceListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ELEMENT_NAME_SEGMENT = "seg";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	private static final String ATTRIBUTE_NAME_USER = "user";
	
	private TagElementProcessor tagElementProcessor;
	private SegmentReferenceElementProcessor segmentReferenceElementProcessor;
	private Way way;
	
	
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
	 */
	public WayElementProcessor(BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing) {
		super(parentProcessor, sink, enableDateParsing);
		
		tagElementProcessor = new TagElementProcessor(this, this);
		segmentReferenceElementProcessor = new SegmentReferenceElementProcessor(this, this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		Date timestamp;
		String user;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		timestamp = parseTimestamp(attributes.getValue(ATTRIBUTE_NAME_TIMESTAMP));
		user = attributes.getValue(ATTRIBUTE_NAME_USER);
		if (user == null) {
			user = "";
		}
		
		way = new Way(id, timestamp, user);
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
		if (ELEMENT_NAME_SEGMENT.equals(qName)) {
			return segmentReferenceElementProcessor;
		} else if (ELEMENT_NAME_TAG.equals(qName)) {
			return tagElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		getSink().process(new WayContainer(way));
		way = null;
	}
	
	
	/**
	 * This is called by child element processors when a tag object is
	 * encountered.
	 * 
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(Tag tag) {
		way.addTag(tag);
	}


	/**
	 * This is called by child element processors when a segment reference
	 * object is encountered.
	 * 
	 * @param segmentReference
	 *            The segmentReference to be processed.
	 */
	public void processSegmentReference(SegmentReference segmentReference) {
		way.addSegmentReference(segmentReference);	
	}
}
