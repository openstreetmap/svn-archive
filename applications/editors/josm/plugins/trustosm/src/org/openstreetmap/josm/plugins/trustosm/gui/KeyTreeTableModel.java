package org.openstreetmap.josm.plugins.trustosm.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.bcpg.SignatureSubpacketTags;
import org.bouncycastle.bcpg.sig.NotationData;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.openstreetmap.josm.plugins.trustosm.TrustOSMplugin;

public class KeyTreeTableModel extends AbstractTreeTableModel {

	public static String convPGPSignatureToString(PGPSignature s) {
		if (s==null) return null;
		PGPSignatureSubpacketVector sv = s.getHashedSubPackets();
		if (sv != null && sv.hasSubpacket(SignatureSubpacketTags.SIGNER_USER_ID))
			return sv.getSignerUserID();

		PGPPublicKey pub = TrustOSMplugin.gpg.getPublicKeyFromRing(s.getKeyID());
		if (pub != null){
			Iterator i = pub.getUserIDs();
			if (i.hasNext())
				return (String)i.next();

		}
		return tr("unknown");
	}

	private final SignatureTreeNode root;
	private final String[] allTitle = {tr("UID"),tr("KeyID"),tr("OSM-Info"),tr("Signed")};
	private final List<String> columns = new ArrayList<String>(Arrays.asList(allTitle));

	public KeyTreeTableModel(Collection<PGPSignature> sigs) {
		root = new SignatureTreeNode();
		for (PGPSignature s : sigs){
			SignatureTreeNode sn = new SignatureTreeNode(s);
			PGPPublicKey pub = TrustOSMplugin.gpg.getPublicKeyFromRing(s.getKeyID());
			Iterator iter = pub.getSignatures();
			while (iter.hasNext()){
				PGPSignature ks = (PGPSignature)iter.next();
				sn.getChildren().add(new SignatureTreeNode(ks));
			}
			root.getChildren().add(sn);
		}
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public String getColumnName( int column ) {
		String title = columns.get(column);
		if (title != null)
			return title;
		return tr("Unknown");
	}


	@Override
	public Object getValueAt(Object node, int column) {
		SignatureTreeNode signode = ( SignatureTreeNode )node;
		String title = columns.get(column);
		if (title != null){
			if (title.equals(allTitle[0]))
				return signode.getUID();
			if (title.equals(allTitle[1]))
				return signode.getKeyID();
			if (title.equals(allTitle[2]))
				return signode.getOsmCertificate();
			if (title.equals(allTitle[3]))
				return signode.getSignatureDate();
		}
		return tr("Unknown");

	}

	@Override
	public Object getChild(Object node, int index) {
		SignatureTreeNode signode = ( SignatureTreeNode )node;
		return signode.getChildren().get( index );
	}

	@Override
	public int getChildCount(Object node) {
		SignatureTreeNode signode = ( SignatureTreeNode )node;
		return signode.getChildren().size();
	}

	@Override
	public int getIndexOfChild( Object parent, Object child ) {
		SignatureTreeNode signode = ( SignatureTreeNode )parent;
		for( int i=0; i>signode.getChildren().size(); i++ ) {
			if( signode.getChildren().get( i ) == child )
				return i;
		}
		return 0;
	}

	@Override
	public Object getRoot() {
		return root;
	}


	public class SignatureTreeNode {
		private PGPSignature s;
		private final List<SignatureTreeNode> children = new ArrayList<SignatureTreeNode>();
		private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:hh.mm.ss");
		public SignatureTreeNode() {
		}

		public SignatureTreeNode(PGPSignature s) {
			this.s = s;
		}

		public PGPSignature getSignature(){
			return s;
		}
		public String getUID() {
			return convPGPSignatureToString(s);
		}
		public String getKeyID() {
			return "0x"+Long.toHexString(s.getKeyID()).substring(8).toUpperCase();
		}
		public String getOsmCertificate() {
			String cert = "";
			for (NotationData nd : s.getHashedSubPackets().getNotationDataOccurences()){
				if (nd.getNotationName().equals("trustosm@openstreetmap.org")) {
					cert += nd.getNotationValue();
				}
			}
			return cert;
		}
		public String getSignatureDate() {
			return formatter.format(s.getCreationTime());
		}
		public List<SignatureTreeNode> getChildren() {
			return children;
		}

	}
}
