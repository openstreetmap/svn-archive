package org.openstreetmap.josm.plugins.trustosm.io;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.io.XmlWriter;
import org.openstreetmap.josm.plugins.trustosm.data.TrustNode;
import org.openstreetmap.josm.plugins.trustosm.data.TrustOsmPrimitive;
import org.openstreetmap.josm.plugins.trustosm.data.TrustRelation;
import org.openstreetmap.josm.plugins.trustosm.data.TrustSignatures;
import org.openstreetmap.josm.plugins.trustosm.data.TrustWay;

public class SigWriter extends XmlWriter {

	private String indent = "";

	public SigWriter(PrintWriter out) {
		super(out);
	}

	public SigWriter(OutputStream out) throws UnsupportedEncodingException {
		super(new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))));
	}

	public void write(Collection<TrustOsmPrimitive> items) {
		writeHeader();
		indent = "  ";
		writeItems(items);
		writeFooter();
		out.flush();
	}

	private void writeDTD() {
		out.println("<!DOCTYPE trustXML [");
		out.println("  <!ELEMENT trustXML (trustnode|trustway|trustrelation)*>");
		out.println("  <!ATTLIST trustXML version CDATA #IMPLIED creator CDATA #IMPLIED >");
		out.println("  <!ELEMENT trustnode (tags?,node?)>");
		out.println("  <!ELEMENT trustway (tags?,segmentlist?)>");
		out.println("  <!ELEMENT trustrelation (tags?,memberlist?)>");
		out.println("  <!ATTLIST trustnode osmid CDATA #IMPLIED >");
		out.println("  <!ATTLIST trustway osmid CDATA #IMPLIED >");
		out.println("  <!ATTLIST trustrelation osmid CDATA #IMPLIED >");
		out.println("  <!ELEMENT tags (key)+>");
		out.println("  <!ELEMENT key (openpgp)+>");
		out.println("  <!ATTLIST key k CDATA #IMPLIED >");
		out.println("  <!ELEMENT node (openpgp)>");
		//		out.println("  <!ATTLIST node id CDATA #REQUIRED >");
		out.println("  <!ELEMENT segmentlist (segment)*>");
		out.println("  <!ELEMENT segment (openpgp)+>");
		out.println("  <!ELEMENT memberlist (member)*>");
		out.println("  <!ELEMENT member (openpgp)+>");
		out.println("  <!ELEMENT openpgp (#PCDATA)*>");
		out.println("]>");
	}

	private void writeHeader() {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		writeDTD();
		out.println("<trustXML version=\"0.1\" creator=\"JOSM Signature export\">");
	}

	private void writeFooter() {
		out.println("</trustXML>");
	}

	private void writeSigs(TrustSignatures tsigs) {
		for (String plain : tsigs.getAllPlainTexts()) {
			simpleTag("openpgp",tsigs.getArmoredFulltextSignatureAll(plain));
		}

	}

	private void writeTags(TrustOsmPrimitive trust) {
		Map<String, TrustSignatures> tagsigs = trust.getTagSigs();
		Set<String> signedKeys = tagsigs.keySet();
		if (signedKeys.isEmpty()) return;
		openln("tags");
		for (String key : signedKeys) {
			openAtt("key","k=\""+key+"\"");

			writeSigs(tagsigs.get(key));

			closeln("key");
		}
		closeln("tags");
	}

	private void writeNode(TrustNode tn) {
		TrustSignatures tsigs = tn.getNodeSigs();
		if (tsigs == null) return;
		openln("node");
		writeSigs(tsigs);
		closeln("node");
	}

	private void writeSegments(TrustWay tw) {
		Map<List<Node>, TrustSignatures> segmentSig = tw.getSegmentSigs();
		Set<List<Node>> signedSegments = segmentSig.keySet();
		if (signedSegments.isEmpty()) return;
		openln("segmentlist");
		for (List<Node> segment : signedSegments) {
			openln("segment");
			writeSigs(segmentSig.get(segment));
			closeln("segment");
		}
		closeln("segmentlist");
	}

	private void writeMembers(TrustRelation tr) {
		Map<String, TrustSignatures> memberSig = tr.getMemberSigs();
		Set<String> signedMembers = memberSig.keySet();
		if (signedMembers.isEmpty()) return;
		openln("memberlist");
		for (String member : signedMembers) {
			openln("member");
			writeSigs(memberSig.get(member));
			closeln("member");
		}
		closeln("memberlist");
	}

	private void writeItems(Collection<TrustOsmPrimitive> items) {

		for (TrustOsmPrimitive trust : items){
			OsmPrimitive osm = trust.getOsmPrimitive();
			if (trust instanceof TrustNode) {
				TrustNode tn = (TrustNode) trust;
				openAtt("trustnode", "osmid=\""+String.valueOf(osm.getUniqueId())+"\"");
				writeTags(tn);
				writeNode(tn);
				closeln("trustnode");
			} else if (trust instanceof TrustWay) {
				TrustWay tw = (TrustWay) trust;
				openAtt("trustway", "osmid=\""+String.valueOf(osm.getUniqueId())+"\"");
				writeTags(tw);
				writeSegments(tw);
				closeln("trustway");
			} else if (trust instanceof TrustRelation) {
				TrustRelation tr = (TrustRelation) trust;
				openAtt("trustrelation", "osmid=\""+String.valueOf(osm.getUniqueId())+"\"");
				writeTags(tr);
				writeMembers(tr);
				closeln("trustrelation");
			}

			//			openAtt("trustitem", "osmid=\""+String.valueOf(osm.getUniqueId())+"\" type=\""+osm.getType().getAPIName()+"\"");

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
