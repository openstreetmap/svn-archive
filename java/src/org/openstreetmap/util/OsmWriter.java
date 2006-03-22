package org.openstreetmap.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Save the dataset into a stream as osm intern xml format. This is not using any
 * xml library for storing.
 * @author imi
 */
public class OsmWriter {

	/**
	 * The output writer to save the values to.
	 */
	private PrintWriter out;

	private final static HashMap encoding = new HashMap();
	static {
		encoding.put(new Character('<'), "&lt;");
		encoding.put(new Character('>'), "&gt;");
		encoding.put(new Character('"'), "&quot;");
		encoding.put(new Character('\''), "&apos;");
		encoding.put(new Character('&'), "&amp;");
		encoding.put(new Character('\n'), "&#xA;");
		encoding.put(new Character('\r'), "&#xD;");
		encoding.put(new Character('\t'), "&#x9;");
	}
	
	/**
	 * Output the data to the stream
	 */
	public static void output(Writer out, Object obj) {
		OsmWriter writer = new OsmWriter(out);
		writer.out.println("<?xml version='1.0' encoding='UTF-8'?>");
		writer.out.println("<osm version='0.3' generator='applet'>");
		if (obj instanceof Node)
			writer.visit((Node)obj);
		else if (obj instanceof Line)
			writer.visit((Line)obj);
		else if (obj instanceof Way)
			writer.visit((Way)obj);
		else
			throw new IllegalArgumentException("Neither a node, a line or a way.");
		writer.out.println("</osm>");
	}

	private OsmWriter(Writer out) {
		if (out instanceof PrintWriter)
			this.out = (PrintWriter)out;
		else
			this.out = new PrintWriter(out);
	}

	public void visit(Node n) {
		out.print("  <node id='"+n.id+"'");
		out.print(" lat='"+n.coor.lat+"' lon='"+n.coor.lon+"'");
		addTags(n.tags, "node", true);
	}

	public void visit(Line ls) {
		if (ls instanceof LineOnlyId)
			throw new IllegalArgumentException("Cannot osmwrite an incomplete line segment.");
		out.print("  <"+"segment"+" id='"+ls.id+"'");
		out.print(" from='"+ls.from.id+"' to='"+ls.to.id+"'");
		addTags(ls.tags, "segment", true);
	}

	public void visit(Way w) {
		out.print("  <"+"way"+" id='"+w.id+"'");
		out.println(">");
		for (Iterator it = w.lines.iterator(); it.hasNext();)
			out.println("    <seg id='"+((Line)it.next()).id+"' />");
		addTags(w.tags, "way", false);
	}

	private void addTags(Map tags, String tagname, boolean tagOpen) {
		if (!tags.isEmpty()) {
			if (tagOpen)
				out.println(">");
			for (Iterator it = tags.keySet().iterator(); it.hasNext();) {
				String key = (String)it.next();
				String value = (String)tags.get(key);
				out.println("    <tag k='"+ encode(key) + "' v='"+encode(value)+ "' />");
			}
			out.println("  </" + tagname + ">");
		} else if (tagOpen)
			out.println(" />");
		else
			out.println("  </" + tagname + ">");
	}

	/**
	 * Encode the given string in XML1.0 format.
	 * Optimized to fast pass strings that don't need encoding (normal case).
	 */
	public String encode(String unencoded) {
		StringBuffer buffer = null;
		for (int i = 0; i < unencoded.length(); ++i) {
			String encS = (String)encoding.get(new Character(unencoded.charAt(i)));
			if (encS != null) {
				if (buffer == null)
					buffer = new StringBuffer(unencoded.substring(0,i));
				buffer.append(encS);
			} else if (buffer != null)
				buffer.append(unencoded.charAt(i));
		}
		return (buffer == null) ? unencoded : buffer.toString();
	}
}
