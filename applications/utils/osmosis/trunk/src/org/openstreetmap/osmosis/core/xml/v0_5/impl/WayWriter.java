// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_5.impl;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_5.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.domain.v0_5.WayNode;
import org.openstreetmap.osmosis.core.xml.common.ElementWriter;


/**
 * Renders a way as xml.
 * 
 * @author Brett Henderson
 */
public class WayWriter extends ElementWriter {
	private WayNodeWriter wayNodeWriter;
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
		wayNodeWriter = new WayNodeWriter("nd", indentLevel + 1);
	}
	
	
	/**
	 * Writes the way.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	public void process(Way way) {
		OsmUser user;
		List<WayNode> wayNodes;
		List<Tag> tags;
		
		user = way.getUser();
		
		beginOpenElement();
		addAttribute("id", Long.toString(way.getId()));
		addAttribute("timestamp", way.getFormattedTimestamp(getTimestampFormat()));
		
		if (!user.equals(OsmUser.NONE)) {
			addAttribute("uid", Integer.toString(user.getId()));
			addAttribute("user", user.getName());
		}
		
		wayNodes = way.getWayNodeList();
		tags = way.getTagList();
		
		if (wayNodes.size() > 0 || tags.size() > 0) {
			endOpenElement(false);

			for (WayNode wayNode : wayNodes) {
				wayNodeWriter.processWayNode(wayNode);
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
	public void setWriter(final Writer writer) {
		super.setWriter(writer);
		
		wayNodeWriter.setWriter(writer);
		tagWriter.setWriter(writer);
	}
}
