package org.openstreetmap.josm.plugins.trustosm.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmDataParsingException;
import org.openstreetmap.josm.io.UTFInputStreamReader;
import org.openstreetmap.josm.plugins.trustosm.data.TrustNode;
import org.openstreetmap.josm.plugins.trustosm.data.TrustOsmPrimitive;
import org.openstreetmap.josm.plugins.trustosm.data.TrustRelation;
import org.openstreetmap.josm.plugins.trustosm.data.TrustSignatures;
import org.openstreetmap.josm.plugins.trustosm.data.TrustWay;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SigReader {

	private final Map<String,TrustOsmPrimitive> trustitems = new HashMap<String,TrustOsmPrimitive>();
	private final Set<OsmPrimitive> missingData = new HashSet<OsmPrimitive>();

	public Map<String,TrustOsmPrimitive> getTrustItems() {
		return trustitems;
	}

	public Set<OsmPrimitive> getMissingData() {
		return missingData;
	}

	private class Parser extends DefaultHandler {
		private Locator locator;

		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}

		protected void throwException(String msg) throws OsmDataParsingException{
			throw new OsmDataParsingException(msg).rememberLocation(locator);
		}

		/**
		 * The current TrustOSMItem to be read.
		 */
		private TrustOsmPrimitive trust;


		/**
		 * The current Signatures.
		 */
		private TrustSignatures tsigs;


		private StringBuffer tmpbuf = new StringBuffer();


		@Override public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

			try {
				if (qName.equals("trustnode") || qName.equals("trustway") || qName.equals("trustrelation")) {
					if (atts == null) {
						throwException(tr("Missing mandatory attribute ''{0}'' of XML element {1}.", "osmid", qName));
					}

					String osmid = atts.getValue("osmid");
					if (osmid == null){
						throwException(tr("Missing mandatory attribute ''{0}''.", "osmid"));
					} else if (!osmid.matches("\\d+")) {
						throwException(tr("Only digits allowed in osmid: ''{0}''.", osmid));
					}
					long uid = Long.parseLong(osmid);

					OsmPrimitiveType t = OsmPrimitiveType.NODE;
					if (qName.equals("trustway")) t = OsmPrimitiveType.WAY;
					else if (qName.equals("trustrelation")) t = OsmPrimitiveType.RELATION;

					// search corresponding OsmPrimitive
					OsmPrimitive osm = Main.main.getCurrentDataSet().getPrimitiveById(uid, t);
					if (osm == null) {
						switch (t) {
						case NODE: osm = new Node(uid); break;
						case WAY: osm = new Way(uid); break;
						case RELATION: osm = new Relation(uid); break;
						}
						missingData.add(osm);
					}
					trust = TrustOsmPrimitive.createTrustOsmPrimitive(osm);

				} else if (qName.equals("key") || qName.equals("node") || qName.equals("segment") || qName.equals("member")) {
					tsigs = new TrustSignatures();
				} else if (qName.equals("openpgp")) {
					tmpbuf = new StringBuffer();
				}
			} catch (Exception e) {
				throw new SAXParseException(e.getMessage(), locator, e);
			}
		}

		@Override public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			if (qName.equals("trustnode") || qName.equals("trustway") || qName.equals("trustrelation")) {
				trustitems.put(TrustOsmPrimitive.createUniqueObjectIdentifier(trust.getOsmPrimitive()), trust);
			} else if (qName.equals("openpgp")) {
				// System.out.println(tmpbuf.toString());
				try {
					parseOpenPGP(tmpbuf.toString());
				} catch (IOException e) {
					throw new OsmDataParsingException(tr("Could not parse OpenPGP message."),e).rememberLocation(locator);
				}
			} else if (qName.equals("key")) {
				String[] kv = TrustOsmPrimitive.generateTagsFromSigtext(tsigs.getOnePlainText());
				trust.setTagRatings(kv[0], tsigs);
			} else if (qName.equals("node")) {
				((TrustNode)trust).setNodeRatings(tsigs);
			} else if (qName.equals("segment")) {
				List<Node> nodes = TrustWay.generateSegmentFromSigtext(tsigs.getOnePlainText());
				((TrustWay)trust).setSegmentRatings(nodes,tsigs);
			} else if (qName.equals("member")) {
				RelationMember member = TrustRelation.generateRelationMemberFromSigtext(tsigs.getOnePlainText());
				((TrustRelation)trust).setMemberRating(TrustOsmPrimitive.createUniqueObjectIdentifier(member.getMember()), tsigs);
			}
		}

		@Override public void characters(char[] ch, int start, int length) {
			tmpbuf.append(ch, start, length);
		}

		public void parseOpenPGP(String clearsigned) throws IOException {

			// handle different newline characters and match them all to \n
			//clearsigned = clearsigned.replace('\r', '\n').replaceAll("\n\n", "\n");

			String plain = "";

			ArmoredInputStream aIn = new ArmoredInputStream(new ByteArrayInputStream(clearsigned.getBytes(Charset.forName("UTF-8"))));
			PGPObjectFactory pgpFact = new PGPObjectFactory(aIn);
			// read plain text
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			if (aIn.isClearText()) {
				int ch = aIn.read();
				do {
					bout.write(ch);
					ch = aIn.read();
				} while (aIn.isClearText());
			}
			plain = bout.toString();

			// remove the last \n because it is not part of the plaintext
			plain = plain.substring(0, plain.length()-1);

			PGPSignatureList siglist = (PGPSignatureList)pgpFact.nextObject();
			for (int i=0; i<siglist.size();i++) {
				tsigs.addSignature(siglist.get(i), plain);
			}


		}


	}

	/**
	 * Parse the given input source and return the TrustosmItems.
	 *
	 * @param source the source input stream. Must not be null.
	 * @param progressMonitor  the progress monitor. If null, {@see NullProgressMonitor#INSTANCE} is assumed
	 * @param missingData	every OsmPrimitive that is not present in the current Datalayer while parsing is stored in that set
	 *
	 * @return a map of the parsed OSM Signatures (TrustOSMItem) with their related OSM-ID as key
	 * @throws IllegalDataException thrown if the an error was found while parsing the data from the source
	 * @throws IllegalArgumentException thrown if source is null
	 */
	public static Map<String,TrustOsmPrimitive> parseSignatureXML(InputStream source, ProgressMonitor progressMonitor) throws IllegalDataException {
		if (progressMonitor == null) {
			progressMonitor = NullProgressMonitor.INSTANCE;
		}
		CheckParameterUtil.ensureParameterNotNull(source, "source");
		SigReader reader = new SigReader();
		try {
			progressMonitor.beginTask(tr("Prepare stuff...", 2));
			progressMonitor.indeterminateSubTask(tr("Parsing Signature data..."));

			InputSource inputSource = new InputSource(UTFInputStreamReader.create(source, "UTF-8"));
			SAXParserFactory.newInstance().newSAXParser().parse(inputSource, reader.new Parser());
			//			if (missingData != null)
			//				missingData.addAll(reader.getMissingData());
			progressMonitor.worked(1);

			return reader.getTrustItems();
		} catch(ParserConfigurationException e) {
			throw new IllegalDataException(e.getMessage(), e);
		} catch (SAXParseException e) {
			throw new IllegalDataException(tr("Line {0} column {1}: ", e.getLineNumber(), e.getColumnNumber()) + e.getMessage(), e);
		} catch(SAXException e) {
			throw new IllegalDataException(e.getMessage(), e);
		} catch(Exception e) {
			throw new IllegalDataException(e);
		} finally {
			progressMonitor.finishTask();
		}
	}


}
