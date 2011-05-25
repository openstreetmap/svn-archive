package org.openstreetmap.josm.plugins.trustosm.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bouncycastle.openpgp.PGPSignature;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;

public class TrustRelation extends TrustOsmPrimitive {


	public static RelationMember generateRelationMemberFromSigtext(String sigtext) {
		Pattern p = Pattern.compile("^RelID=(\\w*)\n(\\d*),(.*)");
		Matcher m = p.matcher(sigtext);
		if (m.matches()) {
			OsmPrimitive osm = createOsmPrimitiveFromUniqueObjectIdentifier(m.group(2));
			return new RelationMember(m.group(3),osm);
		}
		return null;
	}

	public static String generateRelationMemberSigtext(TrustRelation trust, String memID) {
		Relation r = (Relation)trust.getOsmPrimitive();
		List<RelationMember> members = r.getMembers();
		RelationMember member = null;
		for (RelationMember m : members) {
			if (TrustOsmPrimitive.createUniqueObjectIdentifier(m.getMember()).equals(memID)) {
				member = m;
				break;
			}
		}
		if (member == null) return "";
		String sigtext = "RelID=" + r.getUniqueId() + "\n";
		sigtext += TrustOsmPrimitive.createUniqueObjectIdentifier(member.getMember())+","+member.getRole();
		return sigtext;
	}

	private final Map<String, TrustSignatures> memberSig = new HashMap<String, TrustSignatures>();

	public TrustRelation(OsmPrimitive osmItem) {
		super(osmItem);
	}

	@Override
	public void setOsmPrimitive(OsmPrimitive osmItem) {
		if(osmItem instanceof Relation) {
			osm = osmItem;
		} else {
			System.err.println("Error while creating TrustRelation: OsmPrimitive "+osmItem.getUniqueId()+" is not a Relation!");
		}
	}


	public void storeMemberSig(String memID, PGPSignature sig) {
		if (memberSig.containsKey(memID)) {
			memberSig.get(memID).addSignature(sig, TrustRelation.generateRelationMemberSigtext(this, memID));
			return;
		} else {
			memberSig.put(memID, new TrustSignatures(sig, TrustRelation.generateRelationMemberSigtext(this, memID), TrustSignatures.SIG_VALID));
		}
	}

	public void setMemberRating(String memID, TrustSignatures tsigs) {
		memberSig.put(memID, tsigs);
	}

	public Map<String, TrustSignatures> getMemberSigs() {
		return memberSig;
	}

}
