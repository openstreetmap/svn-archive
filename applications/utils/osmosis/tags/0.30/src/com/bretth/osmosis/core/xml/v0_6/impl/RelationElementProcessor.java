// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_6.impl;

import org.xml.sax.Attributes;

import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.xml.common.BaseElementProcessor;
import com.bretth.osmosis.core.xml.common.ElementProcessor;


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
	private Relation relation;
	
	
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
		
		relation = new Relation(id, version, timestampContainer, user);
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
		getSink().process(new RelationContainer(relation));
		relation = null;
	}
	
	
	/**
	 * This is called by child element processors when a tag object is
	 * encountered.
	 * 
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(Tag tag) {
		relation.addTag(tag);
	}
	
	
	/**
	 * This is called by child element processors when a way node object is
	 * encountered.
	 * 
	 * @param relationMember
	 *            The wayNode to be processed.
	 */
	public void processRelationMember(RelationMember relationMember) {
		relation.addMember(relationMember);
	}
}
