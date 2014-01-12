package org.openstreetmap.josm.plugins.trustosm.util;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.bouncycastle.openpgp.PGPSignature;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.trustosm.TrustOSMplugin;
import org.openstreetmap.josm.plugins.trustosm.data.TrustNode;
import org.openstreetmap.josm.plugins.trustosm.data.TrustOsmPrimitive;
import org.openstreetmap.josm.plugins.trustosm.data.TrustRelation;
import org.openstreetmap.josm.plugins.trustosm.data.TrustSignatures;
import org.openstreetmap.josm.plugins.trustosm.data.TrustWay;

public class TrustAnalyzer {

	public static void showManipulationWarning(){
		JOptionPane.showMessageDialog(Main.parent, tr("The Signature is broken!"), tr("Manipulation Warning"), JOptionPane.WARNING_MESSAGE);
	}


	public static double computeReputation(TrustOsmPrimitive trust, Map<String, List<PGPSignature>> textsigs) {
		/** just for simplicity - count all valid sigs */
		int count = 0;
		for (List<PGPSignature> siglist : textsigs.values()) {
			count += siglist.size();
		}
		return count;
	}

	public static boolean isTagRatingValid(TrustOsmPrimitive trust, String key, String signedPlaintext) {
		/** Rating is valid if signed plaintext matches the current plaintext */
		String currentSigtext = TrustOsmPrimitive.generateTagSigtext(trust.getOsmPrimitive(),key);
		return currentSigtext.equals(signedPlaintext);
	}


	public static void checkTag(TrustOsmPrimitive trust, String key) {
		Map<String, List<PGPSignature>> validRatings = new HashMap<String, List<PGPSignature>>();

		TrustSignatures sigs;
		if ((sigs = trust.getSigsOnKey(key))!=null) {
			for (PGPSignature sig : sigs.getSignatures()) {
				/** Here we have a full rating
				 *  The first question: Is the Signature valid?
				 *  It could be manipulated...
				 * */
				String signedPlaintext = sigs.getSigtext(sig);
				if (TrustOSMplugin.gpg.verify(signedPlaintext, sig)) {
					/** If it is valid...
					 * Second question: Is the rating valid?
					 */
					if (isTagRatingValid(trust,key,signedPlaintext)) {
						/** if the rating is good, we can try to compute a reputation value at the end
						 *  so we save the important rating stuff
						 */
						if (validRatings.containsKey(signedPlaintext)) {
							validRatings.get(signedPlaintext).add(sig);
						} else {
							List<PGPSignature> l = new ArrayList<PGPSignature>();
							l.add(sig);
							validRatings.put(signedPlaintext, l);
						}

						//if (sigs.getStatus() == TrustSignatures.SIG_UNKNOWN) sigs.setStatus(TrustSignatures.SIG_VALID);
					} else {
						//sigs.setStatus(TrustSignatures.SIG_BROKEN);
					}
				} else {
					//sigs.setStatus(TrustSignatures.SIG_BROKEN);
					showManipulationWarning();
				}
			}
			/** now we know which ratings are valid to compute a reputation */
			sigs.setReputation(computeReputation(trust, validRatings));
			/** if all available signatures are valid we can set the TrustSignatures status to valid */
			System.out.println(validRatings.size()+":"+sigs.countSigs());
			if (validRatings.size() == 1) sigs.setStatus(TrustSignatures.SIG_VALID);
			else sigs.setStatus(TrustSignatures.SIG_BROKEN);
		}
	}


	public static boolean isNodeRatingValid(TrustNode trust, String signedPlaintext, PGPSignature sig) {
		/** Rating is valid if Node from signed plaintext is inside Tolerance given in Signature */
		Node signedNode = TrustNode.generateNodeFromSigtext(signedPlaintext);
		Node currentNode = (Node)trust.getOsmPrimitive();
		double dist = signedNode.getCoor().greatCircleDistance(currentNode.getCoor());

		/** is distance between signed Node and current Node inside tolerance? */
		return dist<=TrustGPG.searchTolerance(sig);
	}

	/**
	 * Check if the ratings made for a Node are valid for the current position of that node
	 * and compute reputation.
	 * @param trust	The current TrustNode with its ratings
	 */
	public static void checkNode(TrustNode trust) {
		Map<String, List<PGPSignature>> validRatings = new HashMap<String, List<PGPSignature>>();
		Node node = (Node)trust.getOsmPrimitive();
		TrustSignatures sigs;
		if ((sigs = trust.getNodeSigs())!=null) {
			for (String signedPlaintext : sigs.getAllPlainTexts()) {
				for (PGPSignature sig : sigs.getSignaturesByPlaintext(signedPlaintext)) {
					/** first thing: check signature */
					if (TrustOSMplugin.gpg.verify(signedPlaintext,sig)) {
						/** if signature is valid check rating */
						if (isNodeRatingValid(trust,signedPlaintext,sig)) {
							/** if the rating is good, we can try to compute a reputation value at the end
							 *  so we save the important rating stuff
							 */
							if (validRatings.containsKey(signedPlaintext)) {
								validRatings.get(signedPlaintext).add(sig);
							} else {
								List<PGPSignature> l = new ArrayList<PGPSignature>();
								l.add(sig);
								validRatings.put(signedPlaintext, l);
							}

							//if (sigs.getStatus() == TrustSignatures.SIG_UNKNOWN) sigs.setStatus(TrustSignatures.SIG_VALID);
						} else {
							//sigs.setStatus(TrustSignatures.SIG_BROKEN);
						}

					} else {
						//sigs.setStatus(TrustSignatures.SIG_BROKEN);
						showManipulationWarning();
					}
				}
			}
			/** now we know which ratings are valid to compute a reputation */
			sigs.setReputation(computeReputation(trust, validRatings));
			/** if all available signatures are valid we can set the TrustSignatures status to valid */
			if (validRatings.size() == 1) sigs.setStatus(TrustSignatures.SIG_VALID);
			else sigs.setStatus(TrustSignatures.SIG_BROKEN);
		}
	}

	/**
	 * Check if the ratings made for a specific WaySegment are valid for the current form of that WaySegment
	 * @param trust
	 * @param seg
	 * @param signedPlaintext
	 * @param sig
	 * @return
	 */
	public static boolean isSegmentRatingValid(TrustWay trust, List<Node> nodes, String signedPlaintext, PGPSignature sig) {
		/** Rating is valid if Nodes from Segment of signed plaintext are inside Tolerance given in Signature */
		List<Node> signedSegment = TrustWay.generateSegmentFromSigtext(signedPlaintext);

		double tolerance = TrustGPG.searchTolerance(sig);

		for (int i = 0; i<2; i++){
			Node signedNode = signedSegment.get(i);
			Node currentNode = nodes.get(i);
			double dist = signedNode.getCoor().greatCircleDistance(currentNode.getCoor());
			if (dist>tolerance) return false;
		}
		return true;
	}

	/**
	 * Check if there are ratings for a current WaySegment of a TrustWay
	 * and if so, compute Reputation
	 * @param trust the current TrustWay
	 * @param seg the current WaySegment to check for reputation
	 */
	public static void checkSegment(TrustWay trust, List<Node> nodes) {
		Map<String, List<PGPSignature>> validRatings = new HashMap<String, List<PGPSignature>>();

		TrustSignatures sigs;
		if ((sigs = trust.getSigsOnSegment(nodes))!=null) {
			for (String signedPlaintext : sigs.getAllPlainTexts()) {
				for (PGPSignature sig : sigs.getSignaturesByPlaintext(signedPlaintext)) {
					/** first thing: check signature */
					if (TrustOSMplugin.gpg.verify(signedPlaintext,sig)) {
						/** if signature is valid check rating */
						if (isSegmentRatingValid(trust,nodes,signedPlaintext,sig)) {
							/** if the rating is good, we can try to compute a reputation value at the end
							 *  so we save the important rating stuff
							 */
							if (validRatings.containsKey(signedPlaintext)) {
								validRatings.get(signedPlaintext).add(sig);
							} else {
								List<PGPSignature> l = new ArrayList<PGPSignature>();
								l.add(sig);
								validRatings.put(signedPlaintext, l);
							}

							//if (sigs.getStatus() == TrustSignatures.SIG_UNKNOWN) sigs.setStatus(TrustSignatures.SIG_VALID);
						} else {
							//sigs.setStatus(TrustSignatures.SIG_BROKEN);
						}

					} else {
						//sigs.setStatus(TrustSignatures.SIG_BROKEN);
						showManipulationWarning();
					}
				}
			}
			/** now we know which ratings are valid to compute a reputation */
			sigs.setReputation(computeReputation(trust, validRatings));
			/** if all available signatures are valid we can set the TrustSignatures status to valid */
			if (validRatings.size() == sigs.countSigs()) sigs.setStatus(TrustSignatures.SIG_VALID);
			else sigs.setStatus(TrustSignatures.SIG_BROKEN);
		}
	}


	public static void checkEverything(TrustOsmPrimitive trust) {
		/** check every single tag for reputation */
		for (String key : trust.getSignedKeys()){
			checkTag(trust, key);
		}
		if (trust instanceof TrustNode) {
			/** check all reputation of this single Node */
			checkNode((TrustNode) trust);
		} else if (trust instanceof TrustWay){
			TrustWay tw = (TrustWay) trust;
			/** check all reputation for every Segment of this Way */
			List<Node> wayNodes = ((Way)tw.getOsmPrimitive()).getNodes();
			for (int i=0; i<wayNodes.size()-1; i++) {
				List<Node> nodes = new ArrayList<Node>();
				nodes.add(wayNodes.get(i));
				nodes.add(wayNodes.get(i+1));
				checkSegment(tw,nodes);
			}

		} else if (trust instanceof TrustRelation){
			TrustRelation tr = (TrustRelation) trust;
		}

	}
}
