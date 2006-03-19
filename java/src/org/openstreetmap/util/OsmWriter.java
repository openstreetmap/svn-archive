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
		encoding.put(Character.valueOf('<'), "&lt;");
		encoding.put(Character.valueOf('>'), "&gt;");
		encoding.put(Character.valueOf('"'), "&quot;");
		encoding.put(Character.valueOf('\''), "&apos;");
		encoding.put(Character.valueOf('&'), "&amp;");
		encoding.put(Character.valueOf('\n'), "&#xA;");
		encoding.put(Character.valueOf('\r'), "&#xD;");
		encoding.put(Character.valueOf('\t'), "&#x9;");
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
		out.print("  <"+"segment"+" id='"+ls.id+"'");
		out.print(" from='"+ls.from.id+"' to='"+ls.to.id+"'");
		addTags(ls.tags, "segment", true);
	}

	public void visit(Way w) {
		out.print("  <"+"way"+" id='"+w.id+"'");
		out.println(">");
		for (int i = 0; i < w.size(); ++i)
			out.println("    <seg id='"+w.get(i).id+"' />");
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
		StringBuilder buffer = null;
		for (int i = 0; i < unencoded.length(); ++i) {
			String encS = (String)encoding.get(Character.valueOf(unencoded.charAt(i)));
			if (encS != null) {
				if (buffer == null)
					buffer = new StringBuilder(unencoded.substring(0,i));
				buffer.append(encS);
			} else if (buffer != null)
				buffer.append(unencoded.charAt(i));
		}
		return (buffer == null) ? unencoded : buffer.toString();
	}
}
