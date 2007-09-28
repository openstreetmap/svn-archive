package com.bretth.osmosis.core.xml.v0_4.impl;

import java.util.Date;

import org.xml.sax.Attributes;

import com.bretth.osmosis.core.container.v0_4.SegmentContainer;
import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.domain.v0_4.Tag;
import com.bretth.osmosis.core.task.v0_4.Sink;
import com.bretth.osmosis.core.xml.common.BaseElementProcessor;
import com.bretth.osmosis.core.xml.common.ElementProcessor;


/**
 * Provides an element processor implementation for a segment.
 * 
 * @author Brett Henderson
 */
public class SegmentElementProcessor extends SourceElementProcessor implements TagListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	private static final String ATTRIBUTE_NAME_USER = "user";
	private static final String ATTRIBUTE_NAME_FROM = "from";
	private static final String ATTRIBUTE_NAME_TO = "to";
	
	private TagElementProcessor tagElementProcessor;
	private Segment segment;
	
	
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
	public SegmentElementProcessor(BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing) {
		super(parentProcessor, sink, enableDateParsing);
		
		tagElementProcessor = new TagElementProcessor(this, this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		Date timestamp;
		long from;
		long to;
		String user;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		timestamp = parseTimestamp(attributes.getValue(ATTRIBUTE_NAME_TIMESTAMP));
		from = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_FROM));
		to = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_TO));
		user = attributes.getValue(ATTRIBUTE_NAME_USER);
		if (user == null) {
			user = "";
		}
		
		segment = new Segment(id, timestamp, user, from, to);
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
		if (ELEMENT_NAME_TAG.equals(qName)) {
			return tagElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		getSink().process(new SegmentContainer(segment));
		segment = null;
	}
	
	
	/**
	 * This is called by child element processors when a tag object is
	 * encountered.
	 * 
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(Tag tag) {
		segment.addTag(tag);
	}
}
