package org.openstreetmap.josm.plugins.trustosm.gui.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.TreePath;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.jdesktop.swingx.JXTreeTable;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.trustosm.TrustOSMplugin;
import org.openstreetmap.josm.plugins.trustosm.data.TrustNode;
import org.openstreetmap.josm.plugins.trustosm.data.TrustOsmPrimitive;
import org.openstreetmap.josm.plugins.trustosm.data.TrustSignatures;
import org.openstreetmap.josm.plugins.trustosm.data.TrustWay;
import org.openstreetmap.josm.plugins.trustosm.gui.KeyTreeTableModel;
import org.openstreetmap.josm.plugins.trustosm.gui.KeyTreeTableModel.SignatureTreeNode;
import org.openstreetmap.josm.plugins.trustosm.util.TrustGPG;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;



public class TrustSignaturesDialog {


	private static String createLabel(String plain, int type) {
		if (type == 0) {
			String[] kv = TrustOsmPrimitive.generateTagsFromSigtext(plain);
			return tr("Signed key value pair was: {0}={1}", kv[0],kv[1]);
		} else if (type == 1) {
			Node node = TrustNode.generateNodeFromSigtext(plain);
			//return tr("Signed node was: {0}", node.getDisplayName(DefaultNameFormatter.getInstance()));
			return "ID:"+node.getUniqueId()+" ("+node.getCoor().toString() + ")";
		} else if (type == 2) {
			List<Node> nodes = TrustWay.generateSegmentFromSigtext(plain);
			return "From:"+nodes.get(0).getUniqueId()+", To:"+nodes.get(nodes.size()-1).getUniqueId();
		}
		return "No known type";
	}

	private static void showDialog(TrustSignatures sigs, String label, int type) {
		JPanel p = new JPanel(new GridBagLayout());
		p.add(new JLabel(label),GBC.eol());

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:hh.mm.ss");

		for (String plain : sigs.getAllPlainTexts()) {
			JTextArea sigtext = new JTextArea(sigs.getArmoredFulltextSignatureAll(plain));
			sigtext.setEditable(false);
			JPanel textcontent = new JPanel();
			textcontent.add(sigtext);

			p.add(new JCollapsiblePanel(createLabel(plain, type),textcontent),GBC.eol());

			List<PGPSignature> siglist = sigs.getSignaturesByPlaintext(plain);
			JPanel signerPanel = new JPanel(new GridBagLayout());
			//signerPanel.add(createSignerTree(siglist));
			KeyTreeTableModel km = new KeyTreeTableModel(siglist);
			final JXTreeTable t = new JXTreeTable( km );
			//t.setHorizontalScrollEnabled(true);
			//t.setRootVisible(false);
			t.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						TreePath selPath = t.getPathForLocation(e.getX(), e.getY());
						if (selPath == null)
							return;
						SignatureTreeNode sn = (SignatureTreeNode)selPath.getLastPathComponent();
						PGPPublicKey pub = TrustOSMplugin.gpg.getPublicKeyFromRing(sn.getSignature().getKeyID());
						TrustGPG.showKeyDetails(pub);
					}
				}
			});
			t.setLeafIcon(ImageProvider.get("dialogs/sign"));
			t.setOpenIcon(ImageProvider.get("dialogs/sign_color"));
			t.setClosedIcon(ImageProvider.get("dialogs/sign_color"));
			t.expandAll();
			t.packAll();
			t.collapseAll();
			signerPanel.add(new JScrollPane(t));


			//			JTreeTable tt = new JTreeTable();

			/*				for (PGPSignature s : siglist) {
				signerPanel.add(createKeyButton(tr("Signature created at {0} by User {1}",formatter.format(s.getCreationTime()),s.getHashedSubPackets().getSignerUserID()),s.getKeyID()),GBC.eol());
				//signerPanel.add(new JLabel(tr("Signature created at {0} by User {1}",formatter.format(s.getCreationTime()),s.getHashedSubPackets().getSignerUserID())),GBC.eol());
			}*/

			p.add(new JCollapsiblePanel(tr("{0} Signatures found.", siglist.size()),signerPanel),GBC.eol().insets(20,0,0,0));
		}
		p.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.BOTH));
		JScrollPane scroller = new JScrollPane(p);
		//JPanel content = new JPanel();
		scroller.setPreferredSize(new Dimension(700,500));
		//content.add(scroller);
		//JOptionPane.showMessageDialog(Main.parent,scroller, tr("Clearsigned Signature"), JOptionPane.PLAIN_MESSAGE);
		String[] buttons = {tr("Ok")};
		ExtendedDialog info = new ExtendedDialog(Main.parent, tr("Signature Info"),buttons,false);
		info.setContent(scroller,false);
		info.showDialog();
	}


	public static void showSignaturesDialog(TrustNode trust) {
		TrustSignatures sigs;
		if ((sigs = trust.getNodeSigs()) == null) {
			JOptionPane.showMessageDialog(null,tr("Sorry, there are no Signatures for the selected Node."), tr("No Signature found"), JOptionPane.WARNING_MESSAGE);
		} else {
			String nodename = ((Node)trust.getOsmPrimitive()).getDisplayName(DefaultNameFormatter.getInstance());
			showDialog(sigs, tr("Selected node was:\n{0}",nodename),1);
		}
	}

	public static void showSignaturesDialog(TrustOsmPrimitive trust, String key) {
		TrustSignatures sigs;
		if ((sigs = trust.getSigsOnKey(key)) == null) {
			JOptionPane.showMessageDialog(null,tr("Sorry, there are no Signatures for the selected Attribute."), tr("No Signature found"), JOptionPane.WARNING_MESSAGE);
		} else {
			showDialog(sigs, tr("Selected key value pair was:\n{0}={1}",key,trust.getOsmPrimitive().get(key)), 0);
		}
	}

	public static void showSignaturesDialog(TrustWay trust, List<Node> nodes) {
		TrustSignatures sigs;
		if ((sigs = trust.getSigsOnSegment(nodes)) == null) {
			JOptionPane.showMessageDialog(null,tr("Sorry, there are no Signatures for the selected Segment."), tr("No Signature found"), JOptionPane.WARNING_MESSAGE);
		} else {
			showDialog(sigs, tr("Selected WaySegment was:"), 2);
		}
	}

	/*
	public static JButton createKeyButton(String label,final long keyID) {
		JButton detailsButton = new JButton(label);
		detailsButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				showKeySignaturesDialog(keyID);
			}
		});
		return detailsButton;
	}

	public static void showKeySignaturesDialog(long keyID) {
		PGPPublicKey pub = TrustOSMplugin.gpg.getPublicKeyFromRing(keyID);
		JPanel p = new JPanel(new GridBagLayout());

		Iterator sigIt = pub.getSignatures();
		while (sigIt.hasNext()) {
			PGPSignature s = (PGPSignature)sigIt.next();
			p.add(createKeyButton(s.getHashedSubPackets().getSignerUserID(),pub.getKeyID()));
		}

		String uid = String.valueOf(pub.getKeyID());
		Iterator i = pub.getUserIDs();
		if (i.hasNext())
			uid = (String)i.next();

		p.add(createKeyButton(uid,pub.getKeyID()));
		JOptionPane.showMessageDialog(Main.parent,p, tr("Key Signatures"), JOptionPane.PLAIN_MESSAGE);
	}


	public static String convPGPSignatureToString(PGPSignature s) {
		PGPSignatureSubpacketVector sv = s.getHashedSubPackets();
		if (sv.hasSubpacket(SignatureSubpacketTags.SIGNER_USER_ID))
			return sv.getSignerUserID();

		PGPPublicKey pub = TrustOSMplugin.gpg.getPublicKeyFromRing(s.getKeyID());
		if (pub != null){
			Iterator i = pub.getUserIDs();
			if (i.hasNext())
				return (String)i.next();
		}
		return tr("unknown");
	}

	public static JTree createSignerTree(Collection<PGPSignature> sigs) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		for (PGPSignature s : sigs){
			DefaultMutableTreeNode sn = new DefaultMutableTreeNode(s);
			PGPPublicKey pub = TrustOSMplugin.gpg.getPublicKeyFromRing(s.getKeyID());
			Iterator iter = pub.getSignatures();
			while (iter.hasNext()){
				PGPSignature ks = (PGPSignature)iter.next();
				sn.add(new DefaultMutableTreeNode(ks));
			}
			root.add(sn);
		}


		JTree t = new JTree(new DefaultTreeModel(root));
		t.setRootVisible(false);
		t.setBackground(TrustDialog.BGCOLOR_NO_SIG);
		t.setCellRenderer(new DefaultTreeCellRenderer(){

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean selected, boolean expanded, boolean leaf, int row,
					boolean hasFocus)
			{
				super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

				DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
				if (node.isRoot()) return this;
				PGPSignature s = (PGPSignature) node.getUserObject();
				setIcon(ImageProvider.get("dialogs/sign"));
				setText(convPGPSignatureToString(s));
				PGPSignatureSubpacketVector sv = s.getHashedSubPackets();
				setBackgroundNonSelectionColor(TrustDialog.BGCOLOR_NO_SIG);
				if (sv.hasSubpacket(SignatureSubpacketTags.NOTATION_DATA)) {
					for (NotationData nd : sv.getNotationDataOccurences()){
						System.out.println(nd.getNotationName()+"="+nd.getNotationValue());
						if (nd.getNotationName().equals("trustosm@openstreetmap.org")) {
							setBackgroundNonSelectionColor(TrustDialog.BGCOLOR_VALID_SIG);
							setToolTipText(tr("Key certified for OSM-Usage with comment:{0}",nd.getNotationValue()));
						}
					}
				}
				return this;
			}


		});
		return t;
	}

	 */

}
