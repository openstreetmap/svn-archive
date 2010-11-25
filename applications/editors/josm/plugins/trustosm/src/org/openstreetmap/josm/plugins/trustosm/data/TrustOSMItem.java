package org.openstreetmap.josm.plugins.trustosm.data;

import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.openpgp.PGPSignature;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.trustosm.util.TrustGPG;

public class TrustOSMItem {

	private OsmPrimitive osm;
	private final Map<String, TrustSignatures> keySig = new HashMap<String, TrustSignatures>();
	private final Map<Node, TrustSignatures> geomSig = new HashMap<Node, TrustSignatures>();

	public TrustOSMItem(OsmPrimitive osmItem) {
		this.osm = osmItem;

		/*for (String key: osm.keySet()) {
			keySig.put(key, new TrustSignatures());
		}*/
	}

	public OsmPrimitive getOsmItem() {
		return osm;
	}

	public void setOsmItem(OsmPrimitive osmItem) {
		this.osm = osmItem;
	}

	public void storeAllTagSigs(Map<String, TrustSignatures> sigs){
		keySig.putAll(sigs);
	}

	public void storeTagSig(String key, PGPSignature sig) {
		if (keySig.containsKey(key)) {
			keySig.get(key).addSignature(sig, TrustGPG.generateTagSigtext(osm, key));
			return;
		} else if (osm.keySet().contains(key)) {
			keySig.put(key, new TrustSignatures(sig, TrustGPG.generateTagSigtext(osm, key), TrustSignatures.SIG_VALID));
		}
	}

	public void storeNodeSig(Node node, PGPSignature sig) {
		if (geomSig.containsKey(node)) {
			geomSig.get(node).addSignature(sig, TrustGPG.generateNodeSigtext(osm, node));
		} else {
			geomSig.put(node, new TrustSignatures(sig, TrustGPG.generateNodeSigtext(osm, node), TrustSignatures.SIG_VALID));
		}
	}

	public Map<Node, TrustSignatures> getGeomSigs() {
		return geomSig;
	}

	public TrustSignatures getSigsOnNode(Node node) {
		return geomSig.get(node);
	}

	public Map<String, TrustSignatures> getTagSigs() {
		return keySig;
	}

	public TrustSignatures getSigsOnKey(String key) {
		return keySig.get(key);
	}

	public void updateTagSigStatus(String key, byte status) {
		if (keySig.containsKey(key)) {
			keySig.get(key).setStatus(status);
		} else if (osm.keySet().contains(key)) {
			TrustSignatures tsigs = new TrustSignatures();
			tsigs.setStatus(status);
			keySig.put(key, tsigs);
		}
	}

	public void updateNodeSigStatus(Node node, byte status) {
		if (geomSig.containsKey(node)) {
			geomSig.get(node).setStatus(status);
		} else {
			TrustSignatures tsigs = new TrustSignatures();
			tsigs.setStatus(status);
			geomSig.put(node, tsigs);
		}
	}
}
