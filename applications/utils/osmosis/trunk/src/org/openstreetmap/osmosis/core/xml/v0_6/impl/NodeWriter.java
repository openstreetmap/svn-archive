// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.xml.v0_6.impl;

import java.io.BufferedWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.xml.common.ElementWriter;


/**
 * Renders a node as xml.
 * 
 * @author Brett Henderson
 */
public class NodeWriter extends ElementWriter {
	private TagWriter tagWriter;
	private NumberFormat numberFormat;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public NodeWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		tagWriter = new TagWriter("tag", indentLevel + 1);
		
		// Only write the first 7 decimal places.
		// Write in US locale so that a '.' is used as the decimal separator.
		numberFormat = new DecimalFormat(
			"0.#######;-0.#######",
			new DecimalFormatSymbols(Locale.US)
		);
	}
	
	
	/**
	 * Writes the node.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	public void process(Node node) {
		OsmUser user;
		Collection<Tag> tags;
		
		user = node.getUser();
		
		beginOpenElement();
		addAttribute("id", Long.toString(node.getId()));
		addAttribute("version", Integer.toString(node.getVersion()));
		addAttribute("timestamp", node.getFormattedTimestamp(getTimestampFormat()));
		
		if (!user.equals(OsmUser.NONE)) {
			addAttribute("uid", Integer.toString(user.getId()));
			addAttribute("user", user.getName());
		}
		
		if (node.getChangesetId() != 0) {
			addAttribute("changeset", Long.toString(node.getChangesetId()));
		}
		
		addAttribute("lat", numberFormat.format(node.getLatitude()));
		addAttribute("lon", numberFormat.format(node.getLongitude()));
		
		tags = node.getTags();
		
		if (tags.size() > 0) {
			endOpenElement(false);
			
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
		
		tagWriter.setWriter(writer);
	}
}
