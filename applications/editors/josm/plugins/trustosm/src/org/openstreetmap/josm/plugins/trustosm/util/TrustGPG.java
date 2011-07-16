package org.openstreetmap.josm.plugins.trustosm.util;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.bouncycastle.bcpg.sig.NotationData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.util.encoders.Hex;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.trustosm.data.TrustNode;
import org.openstreetmap.josm.plugins.trustosm.data.TrustOsmPrimitive;
import org.openstreetmap.josm.plugins.trustosm.data.TrustWay;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JSpinnerDateEditor;

public class TrustGPG {

	//	private GnuPG gpg;
	private char[] password;
	private PGPSecretKeyRingCollection pgpSec;
	private PGPPublicKeyRingCollection pgpPub;
	private static int digest = PGPUtil.SHA1;
	private PGPSecretKey pgpSecKey;
	public boolean keepkey = false;

	public static final String NOTATION_DATA_KEY = "trustosm@openstreetmap.org";

	public TrustGPG() {
		Security.addProvider(new BouncyCastleProvider());
		try {
			readGpgFiles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PGPPublicKey getPublicKeyFromRing(long keyID) {
		try {
			if (pgpPub.contains(keyID))
				return pgpPub.getPublicKey(keyID);
			else if (pgpSec.contains(keyID))
				return pgpSec.getSecretKey(keyID).getPublicKey();
		} catch (PGPException e) {
			System.err.println("Could not read a PGPPublic key from your KeyRingCollectionFile. Stacktrace:");
			e.printStackTrace();
		}
		return null;
	}

	public static String secKeytoString(PGPSecretKey k) {
		String keyText = "0x"+Long.toHexString(k.getKeyID()).substring(8).toUpperCase() + " ";
		//			keyText = new String(Hex.encode(sigKey.getPublicKey().getFingerprint()),"UTF-8") + " ";
		Iterator iter = k.getUserIDs();
		if (iter.hasNext()) {
			keyText += (String)iter.next();
		}
		/*			iter = sigKey.getUserIDs();
		while (iter.hasNext()) {
			keyText += (String)iter.next() + "; ";
		}
		 */
		return keyText.trim();
	}

	private void readSecretKey() {

		// if there is no KeyRingCollection we have to create a new one
		if (pgpSec == null) {
			try {
				generateKey();
			} catch (Exception e) {
				System.err.println("GPG Key Ring File could not be created in: "+Main.pref.getPluginsDirectory().getPath() + "/trustosm/gnupg/secring.gpg");
			}
		}
		//
		// we just loop through the collection till we find a key suitable for encryption, in the real
		// world you would probably want to be a bit smarter about this.
		//
		if (keepkey) return;

		final ArrayList<PGPSecretKey> sigKeys = new ArrayList<PGPSecretKey>();

		//
		// iterate through the key rings.
		//
		Iterator rIt = pgpSec.getKeyRings();

		while (rIt.hasNext()) {

			PGPSecretKeyRing    kRing = (PGPSecretKeyRing)rIt.next();
			Iterator            kIt = kRing.getSecretKeys();

			while (kIt.hasNext()) {
				PGPSecretKey    k = (PGPSecretKey)kIt.next();

				if (k.isSigningKey()) {
					sigKeys.add(k);
				}
			}
		}


		Iterator<PGPSecretKey> skIt = sigKeys.iterator();

		final Vector<String> keys = new Vector<String>();

		while (skIt.hasNext()) {
			PGPSecretKey sigKey = skIt.next();
			keys.add(secKeytoString(sigKey));
		}

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		Dimension d = new Dimension(0,20);

		JLabel head = new JLabel(tr("Select a signing key from your keyring-file:"));
		head.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(head);

		final JComboBox keyBox = new JComboBox(keys);
		keyBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(keyBox);

		JCheckBox keepkeyBox = new JCheckBox(tr("Don''t ask again for the key"));
		keepkeyBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(keepkeyBox);

		JButton detailsButton = new JButton(tr("Details"), ImageProvider.get("keydetails"));
		detailsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		detailsButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				PGPSecretKey sk = sigKeys.get(keyBox.getSelectedIndex());
				showKeyDetails(getPublicKeyFromRing(sk.getKeyID()));
			}});
		p.add(detailsButton);

		JCheckBox random = new JCheckBox(tr("Use a random key from this list"));
		random.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(random);

		p.add(Box.createRigidArea(d));

		JButton createButton = new JButton(tr("Create new Key"), ImageProvider.get("key"));
		createButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		createButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					PGPSecretKey secKey = generateKey();
					if (secKey != null) {
						keyBox.addItem(secKeytoString(secKey));
						sigKeys.add(secKey);
					}
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchProviderException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PGPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}});
		p.add(createButton);
		p.add(Box.createRigidArea(d));

		int n = JOptionPane.showOptionDialog(Main.parent, p, tr("Select a Key to sign"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, ImageProvider.get("keyring"), null, null);

		if (n == JOptionPane.OK_OPTION) {
			keepkey = keepkeyBox.isSelected();
			if (random.isSelected()) {
				Random r = new Random();
				pgpSecKey = sigKeys.get(r.nextInt(sigKeys.size()-1));
			} else {
				pgpSecKey = sigKeys.get(keyBox.getSelectedIndex());
			}
		} else {
			pgpSecKey = null;
		}
		//String selection = (String) JOptionPane.showInputDialog(null, tr("Select a Key to sign"),tr("Secret Key Choice"), JOptionPane.OK_CANCEL_OPTION, null, keys, keys[0]);

		//System.out.println(selection);

		//		return pgpSecKey;
	}

	public void readGpgFiles() throws PGPException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
		FileInputStream pubIn;
		FileInputStream secIn;
		try {
			pubIn = new FileInputStream(Main.pref.getPluginsDirectory().getPath() + "/trustosm/gnupg/secring.gpg");
			secIn = new FileInputStream(Main.pref.getPluginsDirectory().getPath() + "/trustosm/gnupg/pubring.gpg");
			//pubIn = new FileInputStream("/tmp/secring.gpg");
			//secIn = new FileInputStream("/tmp/pubring.gpg");
			pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(pubIn));
			pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(secIn));
		} catch (FileNotFoundException e) {
			System.err.println("No gpg files found in "+Main.pref.getPluginsDirectory().getPath() + "/trustosm/gnupg/secring.gpg");
			pgpSec = null;
			pgpPub = null;
		}

	}

	public void writeGpgFiles() throws FileNotFoundException, IOException {
		FileOutputStream    pubOut = new FileOutputStream(Main.pref.getPluginsDirectory().getPath() + "/trustosm/gnupg/pubring.gpg");
		FileOutputStream    secOut = new FileOutputStream(Main.pref.getPluginsDirectory().getPath() + "/trustosm/gnupg/secring.gpg");
		pgpSec.encode(secOut);
		pgpPub.encode(pubOut);
		pubOut.flush();
		secOut.flush();
		pubOut.close();
		secOut.close();
	}



	public void getPasswordfromUser() {

		final JPasswordField passwordField = new JPasswordField();
		JOptionPane jop = new JOptionPane(passwordField, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, ImageProvider.get("lock"));
		JDialog dialog = jop.createDialog("Password:");
		dialog.addComponentListener(new ComponentAdapter(){
			@Override
			public void componentShown(ComponentEvent e){
				passwordField.requestFocusInWindow();
			}
		});
		dialog.setVisible(true);
		int result = (Integer)jop.getValue();
		dialog.dispose();
		if(result == JOptionPane.OK_OPTION){
			password = passwordField.getPassword();
		}


		/*final JPasswordField passwordField = new JPasswordField(10);
		JOptionPane.showMessageDialog(Main.parent, passwordField, "Enter password", JOptionPane.OK_OPTION, ImageProvider.get("lock"));
		password = passwordField.getPassword();
		 */
	}
	/*
	public void checkTag(TrustOsmPrimitive trust, String key) {
		String sigtext = TrustOsmPrimitive.generateTagSigtext(trust.getOsmPrimitive(),key);
		TrustSignatures sigs;
		if ((sigs = trust.getSigsOnKey(key))!=null)
			for (PGPSignature sig : sigs.getSignatures()) {
				trust.updateTagSigStatus(key, verify(sigtext,sig)? TrustSignatures.SIG_VALID : TrustSignatures.SIG_BROKEN);
			}
	}



	/*	public void checkAll(TrustOsmPrimitive trust) {
		OsmPrimitive osm = trust.getOsmPrimitive();
		for (String key : osm.keySet()) {
			checkTag(trust, key);
		}

		if(osm instanceof Node) {
			checkNode((TrustNode) trust);
		} else if(osm instanceof Way) {
			/*			Iterator<Node> iter = ((Way)osm).getNodes().iterator();
			while (iter.hasNext()) {
				checkNode(trust, iter.next());
			}/
		} else if(osm instanceof Relation) {

		}
	}
	 */

	public void invalidIDWarning(OsmPrimitive osm) {
		JOptionPane.showMessageDialog(Main.parent, tr("The object with the ID \"{0}\" ({1}) is newly created.\nYou can not sign it, because the signature would lose the ID-Reference after uploading it to the OSM-server.",osm.getUniqueId(),osm.toString()), tr("Signing canceled!"), JOptionPane.ERROR_MESSAGE);
	}
	/*
	public TrustOsmPrimitive signGeometry(TrustOsmPrimitive trust) {
		PGPSignatureSubpacketGenerator spGen = chooseAccuracy();
		PGPSignature s;
		Node node;
		OsmPrimitive osm = trust.getOsmPrimitive();
		if (osm.isNew()) {
			invalidIDWarning(osm);
			return trust;
		}
		if(osm instanceof Node) {
			s = signNode(osm,(Node)osm, spGen);
			if (s != null) ((TrustNode)trust).storeNodeSig(s);
		} else if(osm instanceof Way) {
			Iterator<Node> iter = ((Way)osm).getNodes().iterator();
			while (iter.hasNext()) {
				node = iter.next();
				s = signNode(osm,node,spGen);
				if (s != null) ((TrustNode)trust).storeNodeSig(s);
			}
		} else if(osm instanceof Relation) {

		}
		return trust;
	}*/

	public TrustWay signWay(TrustWay trust) {
		PGPSignature s;
		Way w = (Way) trust.getOsmPrimitive();
		if (w.isNew()) {
			invalidIDWarning(w);
			return trust;
		}
		/*
		List<Node> nodes = w.getNodes();
		s = signSegment(trust,nodes);
		if (s != null) trust.storeSegmentSig(nodes,s);
		 */
		List<Node> wayNodes = w.getNodes();
		for (int i=0; i<wayNodes.size()-1; i++) {
			List<Node> nodes = new ArrayList<Node>();
			nodes.add(wayNodes.get(i));
			nodes.add(wayNodes.get(i+1));
			s = signSegment(trust,nodes);
			if (s != null) trust.storeSegmentSig(nodes,s);
		}

		return trust;
	}

	public PGPSignature signSegment(TrustWay trust, List<Node> nodes) {
		Way w = (Way) trust.getOsmPrimitive();
		if (w.isNew()) {
			invalidIDWarning(w);
			return null;
		}
		String tosign = TrustWay.generateSegmentSigtext(trust,nodes);
		PGPSignatureSubpacketGenerator spGen = chooseAccuracy();
		return sign(tosign,spGen);
	}

	public PGPSignature signNode(Node node) {
		PGPSignatureSubpacketGenerator  spGen = chooseAccuracy();
		return signNode(node,spGen);
	}

	public PGPSignature signNode(Node node, PGPSignatureSubpacketGenerator spGen) {
		if (node.isNew()) {
			invalidIDWarning(node);
			return null;
		}
		String tosign = TrustNode.generateNodeSigtext(node);
		return sign(tosign,spGen);
	}

	public boolean signTag(TrustOsmPrimitive trust, String key) {
		OsmPrimitive osm = trust.getOsmPrimitive();
		if (osm.isNew()) {
			invalidIDWarning(osm);
			return false;
		}
		PGPSignature s;
		String tosign = TrustOsmPrimitive.generateTagSigtext(osm,key);
		//s = sign(tosign);
		s = sign(tosign,chooseInformationSource());
		if (s != null) {
			trust.storeTagSig(key, s);
			return true;
		}
		return false;
	}

	/**
	 * Search in a given Signature for Tolerance information.
	 * @param sig
	 * @return found tolerance as double or 0 if no Tolerance is given
	 */

	public static double searchTolerance(PGPSignature sig) {
		/** Take the first NotationData packet that seems to have Tolerance information */
		for (NotationData nd : sig.getHashedSubPackets().getNotationDataOccurences()){
			if (nd.getNotationName().equals(TrustGPG.NOTATION_DATA_KEY)) {
				String notation = nd.getNotationValue();
				Pattern p = Pattern.compile("^Tolerance:(\\d*\\.?\\d*)m");
				Matcher m = p.matcher(notation);
				if (m.matches()) { // we found a valid Tolerance
					return Double.parseDouble(m.group(1));
				}
			}
		}
		return 0;
	}

	public PGPSignatureSubpacketGenerator chooseAccuracy() {
		PGPSignatureSubpacketGenerator  spGen = new PGPSignatureSubpacketGenerator();
		JPanel p = new JPanel(new GridBagLayout());
		p.add(new JLabel(tr("Please give a tolerance in meters")),GBC.eol());

		JFormattedTextField meters = new JFormattedTextField(NumberFormat.getNumberInstance());
		meters.setValue(new Double(10));
		meters.setColumns(5);

		p.add(meters,GBC.std());
		p.add(new JLabel(tr("meters")),GBC.eol());

		int n = JOptionPane.showOptionDialog(Main.parent, p, tr("Accuracy"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (n == JOptionPane.OK_OPTION) {
			spGen.setNotationData(false, true, TrustGPG.NOTATION_DATA_KEY, "Tolerance:"+meters.getValue()+"m");
			return spGen;
		}
		return null;
	}

	public PGPSignatureSubpacketGenerator chooseInformationSource() {
		PGPSignatureSubpacketGenerator  spGen = new PGPSignatureSubpacketGenerator();
		JPanel p = new JPanel(new GridBagLayout());
		p.add(new JLabel(tr("Select as much as you like:")),GBC.eol());

		JCheckBox survey = new JCheckBox(tr("Survey"));
		p.add(survey,GBC.eol());

		JCheckBox aerial = new JCheckBox(tr("Aerial Photography"));
		p.add(aerial,GBC.eol());

		JCheckBox web = new JCheckBox(tr("Web Recherche"));
		p.add(web,GBC.eol());

		JCheckBox trusted = new JCheckBox(tr("Trusted persons told me"));
		p.add(trusted,GBC.eol());

		int n = JOptionPane.showOptionDialog(Main.parent, p, tr("Which source did you use?"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (n == JOptionPane.OK_OPTION) {
			String sources = "Sources:";
			if (survey.isSelected()) sources += ":survey";
			if (aerial.isSelected()) sources += ":aerial";
			if (web.isSelected()) sources += ":web";
			if (trusted.isSelected()) sources += ":trusted";
			spGen.setNotationData(false, true, TrustGPG.NOTATION_DATA_KEY, sources);
			return spGen;
		}
		return null;
	}

	public PGPSignature sign(String tosign) {
		PGPSignatureSubpacketGenerator  spGen = new PGPSignatureSubpacketGenerator();
		return sign(tosign,spGen);
	}

	public PGPSignature sign(String tosign, PGPSignatureSubpacketGenerator spGen) {

		if (spGen == null) return null;
		PGPSignature sig;
		try{

			readSecretKey();
			if (pgpSec == null) return null;

			if (password == null) {
				getPasswordfromUser();
			}

			PGPPrivateKey pgpPrivKey = pgpSecKey.extractPrivateKey(password, "BC");
			PGPSignatureGenerator sGen = new PGPSignatureGenerator(pgpSecKey.getPublicKey().getAlgorithm(), digest, "BC");
			sGen.initSign(PGPSignature.CANONICAL_TEXT_DOCUMENT, pgpPrivKey);

			Iterator it = pgpSecKey.getPublicKey().getUserIDs();
			if (it.hasNext()) {
				spGen.setSignerUserID(false, (String)it.next());
			}
			sGen.setHashedSubpackets(spGen.generate());
			sGen.update(tosign.getBytes(Charset.forName("UTF-8")));
			sig = sGen.generate();
			//System.out.println(new String(sGen.generateOnePassVersion(false).getEncoded(),Charset.forName("UTF-8")));
			//writeSignatureToFile(sig, tosign, new FileOutputStream("/tmp/sigtest.asc"));
			//sig.encode(new BCPGOutputStream(new ArmoredOutputStream(new FileOutputStream("/tmp/sigtest.asc"))));
			return sig;
		}catch (Exception e){//Catch exception if any
			System.err.println("PGP Signing Error: " + e.getMessage());
		}


		/*		String seckeys = gpg.listSecretKeys()? gpg.getResult() : "GPG-ERROR: " + gpg.getErrorString();
		System.out.println("Die gelisteten keys sehen so aus:\n"+seckeys);
		String[] keys = seckeys.split("\n");
		System.out.println("Das Array hat so viele einträge:"+keys.length);
		if (keys.length <= 1) {
			System.out.println("Auf auf zum lustigen generieren!");
			generateKey();
		}
		System.out.println("Achtung die Errorausgabe sieht so aus:\n"+gpg.getErrorString());
		String sig = gpg.sign(tosign, password)? gpg.getResult() : "GPG-ERROR: " + gpg.getErrorString();
		 */


		return null;
	}

	public boolean verify(String sigtext, PGPSignature sig) {
		/*		if (gpg.verifySignature(sig)) {
		success = trust.updateSigStatus(key, gpg.getResult().equals(sigtext)? TrustSignatures.SIG_VALID : TrustSignatures.SIG_BROKEN);
	}*/
		try {
			sig.initVerify(pgpPub.getPublicKey(sig.getKeyID()), "BC");
			sig.update(sigtext.getBytes(Charset.forName("UTF-8")));
			return sig.verify();
		}catch (Exception e){//Catch exception if any
			System.err.println("PGP Verification Error: " + e.getMessage());
		}
		return false;
	}


	//	public static void writeSignatureToFile(PGPSignature sig, String clearText, FileOutputStream fout) throws Exception {
	//		ArmoredOutputStream aOut = new ArmoredOutputStream(fout);
	//		aOut.beginClearText(digest);
	//		aOut.write(clearText.getBytes(Charset.forName("UTF-8")));
	//		aOut.write('\n');
	//		aOut.endClearText();
	//
	//		BCPGOutputStream bOut = new BCPGOutputStream(aOut);
	//		sig.encode(bOut);
	//		aOut.close();
	//		bOut.close();
	//	}
	//
	//	public Map<String, String> getKeyValueFromSignature(PGPSignature sig) {
	//		Map<String, String> tags = new HashMap<String, String>();
	//		try {
	//			String sigtext = new String(sig.getEncoded(), Charset.forName("UTF-8"));
	//			String[] kv = TrustOsmPrimitive.generateTagsFromSigtext(sigtext);
	//			tags.put(kv[0],kv[1]);
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		return tags;
	//	}

	public static void showKeyDetails(PGPPublicKey key) {
		String userid = "Unknown";
		Iterator iter = key.getUserIDs();
		if (iter.hasNext()) {
			userid = (String)iter.next();
		}

		String fingerprint = new String(Hex.encode(key.getFingerprint())).toUpperCase();
		String keyid = "0x"+Long.toHexString(key.getKeyID()).substring(8).toUpperCase();

		String algorithm = "";
		int algo = key.getAlgorithm();
		switch(algo) {
		case PGPPublicKey.DIFFIE_HELLMAN:
			algorithm = "Diffie Hellman (DH)"; break;
		case PGPPublicKey.DSA:
			algorithm = "Digital Signature Algorithm (DSA)"; break;
		case PGPPublicKey.EC:
			algorithm = "Elliptic Curve (EC)"; break;
		case PGPPublicKey.ECDSA:
			algorithm = "Elliptic Curve Digital Signature Algorithm (ECDSA)"; break;
		case PGPPublicKey.ELGAMAL_ENCRYPT:
			algorithm = "Elgamal encrypt-only"; break;
		case PGPPublicKey.ELGAMAL_GENERAL:
			algorithm = "Elgamal"; break;
		case PGPPublicKey.RSA_ENCRYPT:
			algorithm = "Rivest Shamir Adleman (RSA) encrypt-only"; break;
		case PGPPublicKey.RSA_GENERAL:
			algorithm = "Rivest Shamir Adleman (RSA)"; break;
		case PGPPublicKey.RSA_SIGN:
			algorithm = "Rivest Shamir Adleman (RSA) sign-only"; break;
		default:
			algorithm = "Unknown algorithm ID: "+algo; break;
		}

		String strength = String.valueOf(key.getBitStrength());

		//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:hh.mm.ss");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String creationTime = formatter.format(key.getCreationTime());

		long validSeconds = key.getValidSeconds();
		String expirationTime;
		if (validSeconds == 0) {
			expirationTime = tr("never");
		} else {
			expirationTime = formatter.format(new Date(key.getCreationTime().getTime()+validSeconds*1000));
		}


		String[] labels = {tr("Primary user-ID: "), tr("Key-ID: "), tr("Fingerprint: "), tr("Algorithm: "), tr("Strength in bit: "), tr("Creation date: "), tr("Expiration date: ")};
		String[] values = {userid, keyid, fingerprint, algorithm, strength, creationTime, expirationTime};
		int numPairs = labels.length;

		//Create and populate the panel.
		JPanel p = new JPanel(new SpringLayout());
		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			p.add(l);
			JTextField textField = new JTextField(values[i]);
			textField.setEditable(false);
			l.setLabelFor(textField);
			p.add(textField);
		}

		//Lay out the panel.
		SpringUtilities.makeCompactGrid(p,
				numPairs, 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad


		//		JPanel metaPanel = new JPanel();
		//		metaPanel.setLayout(new BoxLayout(metaPanel, BoxLayout.PAGE_AXIS));
		//		metaPanel.add(p);
		//		JScrollPane sp = new JScrollPane(new KeySignaturesDialog(key));
		//		sp.setPreferredSize(new Dimension(0,200));
		//		metaPanel.add(sp);

		JOptionPane.showMessageDialog(Main.parent, p, tr("PGP-Key details"), JOptionPane.INFORMATION_MESSAGE);
	}


	public PGPSecretKey generateKey() throws NoSuchAlgorithmException, NoSuchProviderException, PGPException, FileNotFoundException, IOException {

		JTextField userId = new JTextField();
		NameGenerator nameGen = new NameGenerator(Main.pref.getPluginsDirectory().getPath()+"/trustosm/resources/syllables.txt");
		userId.setText(nameGen.compose(3));

		final String[] sizes = {"1024", "2048", "3072", "4096"};

		final JComboBox strengthBox = new JComboBox(sizes);
		strengthBox.setEnabled(false);

		/*		final String[] curves = {"prime192v1", "prime192v2", "prime192v3", "prime239v1", "prime239v2", "prime239v3", "prime256v1", "secp224r1", "secp256r1", "secp384r1", "secp521r1", "P-224", "P-256", "P-384", "P-521", "c2pnb163v1", "c2pnb163v2", "c2pnb163v3", "c2pnb176w1", "c2tnb191v2", "c2tnb191v1", "c2tnb191v3", "c2pnb208w1", "c2tnb239v1", "c2tnb239v2", "c2tnb239v3", "c2pnb272w1", "c2pnb304w1", "c2tnb359v1", "c2pnb368w1", "c2tnb431r1", "sect163r2", "sect233r1", "sect283r1", "sect409r1", "sect571r1", "B-163", "B-233", "B-283", "B-409", "B-571", "brainpoolp160r1", "brainpoolp160t1", "brainpoolp192r1", "brainpoolp192t1", "brainpoolp224r1", "brainpoolp224t1", "brainpoolp256r1", "brainpoolp256t1", "brainpoolp320r1", "brainpoolp320t1", "brainpoolp384r1", "brainpoolp384t1", "brainpoolp512r1", "brainpoolp512t1"};
		final String[] curvesizes = {"192", "192", "192", "239", "239", "239", "256", "224", "256", "384", "521", "224", "256", "384", "521", "163", "163", "163", "176", "191", "191", "191", "208", "239", "239", "239", "272", "304", "359", "368", "431", "163", "233", "283", "409", "571", "163", "233", "283", "409", "571", "160", "160", "192", "192", "224", "224", "256", "256", "320", "320", "384", "384", "512", "512"};
		final JComboBox curveBox = new JComboBox(curves);
		curveBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				strengthBox.setSelectedIndex(((JComboBox)e.getSource()).getSelectedIndex());
			}});
		curveBox.setEnabled(false);
		 */

		//		final String[] algos = {"DSA","RSA","ECDSA"};
		final String[] algos = {"DSA","RSA"};
		final JComboBox algoBox = new JComboBox(algos);
		algoBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				String alg = (String)cb.getSelectedItem();
				if (alg.equals("DSA")) {
					strengthBox.setSelectedItem("1024");
					strengthBox.setEnabled(false);
				} else
					strengthBox.setEnabled(true);
				/*if (alg.equals("ECDSA")) {
					curveBox.setEnabled(true);
					strengthBox.setModel(new DefaultComboBoxModel(curvesizes));
					strengthBox.setSelectedItem(curvesizes[curveBox.getSelectedIndex()]);
					strengthBox.setEnabled(false);
				} else {
					curveBox.setEnabled(false);
					strengthBox.setModel(new DefaultComboBoxModel(sizes));
					strengthBox.setEnabled(true);
				}*/
			}
		});





		final String[] protectAlgos = {"AES_256", "AES_192", "AES_128", "BLOWFISH", "CAST5", "DES", "IDEA", "SAFER", "TRIPLE_DES", "TWOFISH", "NULL"};
		int[] protAl = {PGPEncryptedData.AES_256, PGPEncryptedData.AES_192, PGPEncryptedData.AES_128, PGPEncryptedData.BLOWFISH, PGPEncryptedData.CAST5, PGPEncryptedData.DES, PGPEncryptedData.IDEA, PGPEncryptedData.SAFER, PGPEncryptedData.TRIPLE_DES, PGPEncryptedData.TWOFISH, PGPEncryptedData.NULL};
		final JComboBox protectBox = new JComboBox(protectAlgos);

		final JDateChooser cal = new JDateChooser(null, null, null, new JSpinnerDateEditor());
		cal.setPreferredSize(new Dimension(130,cal.getPreferredSize().height));

		final String[] labels = {tr("User-ID:"), tr("Select algorithm:"), tr("Choose Bitlength (Strength):"), tr("Encryption algorithm to protect private key:"), tr("Choose an expiry date for the key:")};
		final JComponent[] values = {userId, algoBox, strengthBox, protectBox, cal};

		int numPairs = labels.length;

		//Create and populate the panel.
		JPanel p = new JPanel(new SpringLayout());
		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			p.add(l);
			l.setLabelFor(values[i]);
			p.add(values[i]);
		}

		//Lay out the panel.
		SpringUtilities.makeCompactGrid(p,
				numPairs, 2, //rows, cols
				6, 6,        //initX, initY
				16, 6);       //xPad, yPad

		int n = JOptionPane.showOptionDialog(Main.parent, p, tr("Create a new signing key"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (n != JOptionPane.OK_OPTION)
			return null;


		String algo = (String)algoBox.getSelectedItem();

		KeyPairGenerator Kpg = KeyPairGenerator.getInstance(algo, "BC");

		int al;
		/*		if (algo.equals("ECDSA")) {
			al = PGPPublicKey.ECDSA;
			ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec((String)curveBox.getSelectedItem());
			try {
				Kpg.initialize(ecSpec);
			} catch (InvalidAlgorithmParameterException e1) {
				// TODO Auto-generated catch block
				System.err.println("EC-Parameter not accepted");
				e1.printStackTrace();
			}
		}
		else {*/
		Kpg.initialize(Integer.parseInt((String)strengthBox.getSelectedItem()));
		//
		// this takes a while as the key generator has to generate some DSA params
		// before it generates the key.
		//

		if (algo.equals("RSA")) al = PGPPublicKey.RSA_GENERAL;
		else al = PGPPublicKey.DSA;
		//		}


		KeyPair kp = Kpg.generateKeyPair();

		Date now = new Date();
		PGPKeyPair pgpKp = new PGPKeyPair(al, kp, now);

		getPasswordfromUser();

		PGPSignatureSubpacketVector subPck = null;
		PGPSignatureSubpacketGenerator spGen = null;
		Date expire = cal.getDate();
		if (expire != null && expire.after(now)) {
			spGen = new PGPSignatureSubpacketGenerator();
			spGen.setKeyExpirationTime(true, (expire.getTime()-now.getTime())/1000);
			subPck = spGen.generate();
		}

		PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, pgpKp,
				userId.getText(), protAl[protectBox.getSelectedIndex()], password, true, subPck, null, new SecureRandom(), "BC");

		if (pgpPub == null) {
			Vector<PGPPublicKeyRing> pubKeyRing = new Vector<PGPPublicKeyRing>(1);
			pubKeyRing.add(keyRingGen.generatePublicKeyRing());
			pgpPub = new PGPPublicKeyRingCollection(pubKeyRing);
		} else {
			pgpPub = PGPPublicKeyRingCollection.addPublicKeyRing(pgpPub, keyRingGen.generatePublicKeyRing());
		}

		PGPSecretKeyRing secRing = keyRingGen.generateSecretKeyRing();
		if (pgpSec == null) {
			Vector<PGPSecretKeyRing> secKeyRing = new Vector<PGPSecretKeyRing>(1);
			secKeyRing.add(secRing);
			pgpSec = new PGPSecretKeyRingCollection(secKeyRing);
		} else {
			pgpSec = PGPSecretKeyRingCollection.addSecretKeyRing(pgpSec, secRing);
		}


		writeGpgFiles();

		return secRing.getSecretKey();
	}

}
