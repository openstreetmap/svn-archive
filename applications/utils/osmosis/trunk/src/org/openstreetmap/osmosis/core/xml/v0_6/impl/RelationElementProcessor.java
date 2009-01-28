// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.xml.v0_6.impl;

import org.xml.sax.Attributes;

import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.xml.common.BaseElementProcessor;
import org.openstreetmap.osmosis.core.xml.common.ElementProcessor;


/**
 * Provides an element processor implementation for a relation.
 * 
 * @author Brett Henderson
 */
public class RelationElementProcessor extends EntityElementProcessor implements TagListener, RelationMemberListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ELEMENT_NAME_MEMBER = "member";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	private static final String ATTRIBUTE_NAME_USER = "user";
	private static final String ATTRIBUTE_NAME_USERID = "uid";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	
	private TagElementProcessor tagElementProcessor;
	private RelationMemberElementProcessor relationMemberElementProcessor;
	private RelationBuilder relationBuilder;
	
	
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
	public RelationElementProcessor(BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing) {
		super(parentProcessor, sink, enableDateParsing);
		
		relationBuilder = new RelationBuilder();
		tagElementProcessor = new TagElementProcessor(this, this);
		relationMemberElementProcessor = new RelationMemberElementProcessor(this, this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		int version;
		TimestampContainer timestampContainer;
		String rawUserId;
		String rawUserName;
		OsmUser user;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		version = Integer.parseInt(attributes.getValue(ATTRIBUTE_NAME_VERSION));
		timestampContainer = createTimestampContainer(attributes.getValue(ATTRIBUTE_NAME_TIMESTAMP));
		rawUserId = attributes.getValue(ATTRIBUTE_NAME_USERID);
		rawUserName = attributes.getValue(ATTRIBUTE_NAME_USER);
		
		user = buildUser(rawUserId, rawUserName);
		
		relationBuilder.initialize(id, version, timestampContainer, user);
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
		if (ELEMENT_NAME_MEMBER.equals(qName)) {
			return relationMemberElementProcessor;
		} else if (ELEMENT_NAME_TAG.equals(qName)) {
			return tagElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		getSink().process(new RelationContainer(relationBuilder.buildEntity()));
	}
	
	
	/**
	 * This is called by child element processors when a tag object is
	 * encountered.
	 * 
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(Tag tag) {
		relationBuilder.addTag(tag);
	}
	
	
	/**
	 * This is called by child element processors when a way node object is
	 * encountered.
	 * 
	 * @param relationMember
	 *            The wayNode to be processed.
	 */
	public void processRelationMember(RelationMember relationMember) {
		relationBuilder.addMember(relationMember);
	}
}
