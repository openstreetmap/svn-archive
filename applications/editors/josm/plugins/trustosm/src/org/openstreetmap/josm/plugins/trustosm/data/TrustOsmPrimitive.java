package org.openstreetmap.josm.plugins.trustosm.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPSignature;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

abstract public class TrustOsmPrimitive {

	public static String createUniqueObjectIdentifier(OsmPrimitive osm) {
		String id = "";
		if(osm instanceof Node) {
			id = "n";
		} else if(osm instanceof Way) {
			id = "w";
		} else if(osm instanceof Relation) {
			id = "r";
		}
		id += osm.getUniqueId();
		return id;
	}

	public static OsmPrimitive createOsmPrimitiveFromUniqueObjectIdentifier(String oid) {
		char type = oid.charAt(0);
		long id = Long.parseLong(oid.substring(1));
		switch (type) {
		case 'n': return new Node(id);
		case 'w': return new Way(id);
		case 'r': return new Relation(id);
		}
		return null;
	}

	public static TrustOsmPrimitive createTrustOsmPrimitive(OsmPrimitive osm) {
		if(osm instanceof Node) {
			return new TrustNode((Node) osm);
		} else if(osm instanceof Way) {
			return new TrustWay(osm);
		} else if(osm instanceof Relation) {
			return new TrustRelation(osm);
		}
		return null;
	}

	protected OsmPrimitive osm;
	private final Map<String, TrustSignatures> keySig = new HashMap<String, TrustSignatures>();

	public TrustOsmPrimitive(OsmPrimitive osmItem) {
		setOsmPrimitive(osmItem);
	}

	public OsmPrimitive getOsmPrimitive() {
		return osm;
	}

	public abstract void setOsmPrimitive(OsmPrimitive osmItem);


	public static String[] generateTagsFromSigtext(String sigtext) {
		String[] keyValue = sigtext.substring(sigtext.indexOf('\n')+1).split("=");
		return keyValue;
	}

	public static String generateTagSigtext(OsmPrimitive osm, String key) {
		String sigtext = "ID=" + osm.getUniqueId() + "\n";
		sigtext += key + "=" + osm.get(key);
		return sigtext;
	}

	public void storeTagSig(String key, PGPSignature sig) {
		if (keySig.containsKey(key)) {
			keySig.get(key).addSignature(sig, TrustOsmPrimitive.generateTagSigtext(osm, key));
			return;
		} else if (osm.keySet().contains(key)) {
			keySig.put(key, new TrustSignatures(sig, TrustOsmPrimitive.generateTagSigtext(osm, key), TrustSignatures.SIG_VALID));
		}
	}

	public void setTagRatings(String key, TrustSignatures tsigs) {
		keySig.put(key, tsigs);
	}

	/*
	public Map<Node, TrustSignatures> getGeomSigs() {
		return geomSig;
	}

	public TrustSignatures getSigsOnNode(Node node) {
		return geomSig.get(node);
	}*/

	public Set<String> getSignedKeys() {
		return keySig.keySet();
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
	/*
	public void updateNodeSigStatus(Node node, byte status) {
		if (geomSig.containsKey(node)) {
			geomSig.get(node).setStatus(status);
		} else {
			TrustSignatures tsigs = new TrustSignatures();
			tsigs.setStatus(status);
			geomSig.put(node, tsigs);
		}
	}*/
}
