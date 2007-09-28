package com.bretth.osmosis.core.xml.v0_4.impl;

import java.io.BufferedWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import com.bretth.osmosis.core.domain.v0_4.Node;
import com.bretth.osmosis.core.domain.v0_4.Tag;
import com.bretth.osmosis.core.xml.common.ElementWriter;


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
		
		numberFormat = new DecimalFormat("0.#######;-0.#######");
	}
	
	
	/**
	 * Writes the node.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	public void process(Node node) {
		List<Tag> tags;
		
		beginOpenElement();
		addAttribute("id", Long.toString(node.getId()));
		addAttribute("lat", numberFormat.format(node.getLatitude()));
		addAttribute("lon", numberFormat.format(node.getLongitude()));
		addAttribute("timestamp", formatDate(node.getTimestamp()));
		if (node.getUser() != null && node.getUser().length() > 0) {
			addAttribute("user", node.getUser());
		}
		
		tags = node.getTagList();
		
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
