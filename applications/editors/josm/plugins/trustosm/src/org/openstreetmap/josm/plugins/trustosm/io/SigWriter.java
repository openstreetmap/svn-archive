package org.openstreetmap.josm.plugins.trustosm.io;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.io.XmlWriter;
import org.openstreetmap.josm.plugins.trustosm.data.TrustOSMItem;
import org.openstreetmap.josm.plugins.trustosm.data.TrustSignatures;

public class SigWriter extends XmlWriter {

	private String indent = "";

	public SigWriter(PrintWriter out) {
		super(out);
	}

	public SigWriter(OutputStream out) throws UnsupportedEncodingException {
		super(new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))));
	}

	public void write(Collection<TrustOSMItem> items) {
		writeHeader();
		indent = "  ";
		writeItems(items);
		writeFooter();
		out.flush();
	}

	private void writeDTD() {
		out.println("<!DOCTYPE trustXML [");
		out.println("  <!ELEMENT trustcollection (trustitem)*>");
		out.println("  <!ATTLIST trustcollection version CDATA #IMPLIED creator CDATA #IMPLIED >");
		out.println("  <!ELEMENT trustitem (signatures)*>");
		out.println("  <!ATTLIST trustitem osmid CDATA #REQUIRED type CDATA #REQUIRED >");
		out.println("  <!ELEMENT signatures (tags|geometry)*>");
		out.println("  <!ELEMENT tags (key)*>");
		out.println("  <!ELEMENT key (openpgp)*>");
		out.println("  <!ATTLIST key k CDATA #REQUIRED >");
		out.println("  <!ELEMENT geometry (node)*>");
		out.println("  <!ELEMENT node (openpgp)*>");
		out.println("  <!ATTLIST node id CDATA #REQUIRED >");
		out.println("  <!ELEMENT openpgp (#PCDATA)*>");
		out.println("]>");
	}

	private void writeHeader() {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		writeDTD();
		out.println("<trustcollection version=\"0.1\" creator=\"JOSM Signature export\">");
	}

	private void writeFooter() {
		out.println("</trustcollection>");
	}

	private void writeSigs(TrustSignatures tsigs) {
		for (String plain : tsigs.getAllPlainTexts()) {
			simpleTag("openpgp",tsigs.getArmoredFulltextSignatureAll(plain));
		}

	}

	private void writeItems(Collection<TrustOSMItem> items) {
		Map<String, TrustSignatures> tagsigs;
		Map<Node, TrustSignatures> nodesigs;

		for (TrustOSMItem item : items){
			OsmPrimitive osm = item.getOsmItem();

			openAtt("trustitem", "osmid=\""+String.valueOf(osm.getUniqueId())+"\" type=\""+osm.getType().getAPIName()+"\"");
			openln("signatures");

			tagsigs = item.getTagSigs();
			openln("tags");
			for (String key : tagsigs.keySet()) {
				openAtt("key","k=\""+key+"\"");

				writeSigs(tagsigs.get(key));

				closeln("key");
			}
			closeln("tags");

			nodesigs = item.getGeomSigs();
			openln("geometry");
			for (Node node : nodesigs.keySet()) {
				openAtt("node","id=\""+String.valueOf(node.getUniqueId())+"\"");

				writeSigs(nodesigs.get(node));

				closeln("node");
			}
			closeln("geometry");
			closeln("signatures");
			closeln("trustitem");
		}
	}

	private void openln(String tag) {
		open(tag);
		out.println();
	}

	private void open(String tag) {
		out.print(indent + "<" + tag + ">");
		indent += "  ";
	}

	private void openAtt(String tag, String attributes) {
		out.println(indent + "<" + tag + " " + attributes + ">");
		indent += "  ";
	}

	private void inline(String tag, String attributes) {
		out.println(indent + "<" + tag + " " + attributes + " />");
	}

	private void close(String tag) {
		indent = indent.substring(2);
		out.print(indent + "</" + tag + ">");
	}

	private void closeln(String tag) {
		close(tag);
		out.println();
	}

	/**
	 * if content not null, open tag, write encoded content, and close tag
	 * else do nothing.
	 */
	private void simpleTag(String tag, String content) {
		if (content != null && content.length() > 0) {
			open(tag);
			out.print(encode(content));
			//out.print(content);
			out.println("</" + tag + ">");
			indent = indent.substring(2);
		}
	}

}
