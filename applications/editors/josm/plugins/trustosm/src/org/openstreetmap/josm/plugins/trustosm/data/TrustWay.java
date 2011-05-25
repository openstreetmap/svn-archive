package org.openstreetmap.josm.plugins.trustosm.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.openpgp.PGPSignature;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;

public class TrustWay extends TrustOsmPrimitive {

	/*
	public static List<WaySegment> generateSegmentListFromWay(Way w) {
		List<WaySegment> segList = new ArrayList<WaySegment>();
		for (int i = 0; i < w.getNodesCount()-1; i++) {
			segList.add(new WaySegment(w,i));
		}
		return segList;
	}
	 */

	public static List<Node> generateSegmentFromSigtext(String sigtext) {
		String[] lines = sigtext.split("\n");
		List<Node> nodes = new ArrayList<Node>();
		for (int i=1; i<lines.length; i++){
			nodes.add(TrustNode.generateNodeFromSigtext(lines[i]));
		}
		return nodes;
	}

	public static String generateSegmentSigtext(TrustWay trust, List<Node> nodes) {
		String sigtext = "WayID=" + trust.getOsmPrimitive().getUniqueId();
		for (Node n : nodes) {
			sigtext += "\n" + TrustNode.generateNodeSigtext(n);
		}
		return sigtext;
	}


	private final Map<List<Node>, TrustSignatures> segmentSig = new HashMap<List<Node>, TrustSignatures>();

	public TrustWay(OsmPrimitive osmItem) {
		super(osmItem);
	}

	@Override
	public void setOsmPrimitive(OsmPrimitive osmItem) {
		if(osmItem instanceof Way) {
			osm = osmItem;
		} else {
			System.err.println("Error while creating TrustWay: OsmPrimitive "+osmItem.getUniqueId()+" is not a Way!");
		}
	}


	public void storeSegmentSig(List<Node> nodes, PGPSignature sig) {
		if (segmentSig.containsKey(nodes)) {
			segmentSig.get(nodes).addSignature(sig, TrustWay.generateSegmentSigtext(this,nodes));
		} else {
			segmentSig.put(nodes, new TrustSignatures(sig, TrustWay.generateSegmentSigtext(this,nodes), TrustSignatures.SIG_VALID));
		}
	}

	public void setSegmentRatings(List<Node> nodes, TrustSignatures tsigs) {
		segmentSig.put(nodes, tsigs);
	}

	public Map<List<Node>, TrustSignatures> getSegmentSigs() {
		return segmentSig;
	}

	public TrustSignatures getSigsOnSegment(WaySegment seg) {
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(seg.getFirstNode());
		nodes.add(seg.getSecondNode());
		return getSigsOnSegment(nodes);
	}

	public TrustSignatures getSigsOnSegment(List<Node> nodes) {
		return segmentSig.get(nodes);
	}

}
