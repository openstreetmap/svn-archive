package org.openstreetmap.josm.plugins.tageditor.preset.io;

import java.io.Reader;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.josm.plugins.tageditor.preset.Group;
import org.openstreetmap.josm.plugins.tageditor.preset.Item;
import org.openstreetmap.josm.plugins.tageditor.preset.Presets;
import org.openstreetmap.josm.plugins.tageditor.preset.Tag;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import static org.openstreetmap.josm.tools.I18n.tr;

public class Parser {
	
	static private Logger logger = Logger.getLogger(Parser.class.getName());
    private Presets presets = null;
	private Reader reader;
	private Stack<Group> currentGroup;
	private Item currentItem;
	private boolean inOptionalKeys = false; 
	private XMLReader parser;

	public Parser() {
		currentGroup = new Stack<Group>();
		currentItem = null;
	}
	
	public Parser(Reader reader) {		
		this();
		if (reader == null) {
			throw new IllegalArgumentException("reader must not be null");
		}
		this.reader = reader; 
	}
	
	public void setReader(Reader reader) {
		this.reader = reader;
	}
	
	public Reader getReader() {
		return reader;
	}
	
	public Presets getPresets() {
		return presets;
	}

	public void setPresets(Presets presets) {
		this.presets = presets;
	}

	protected void init() throws PresetIOException {
		try {
			parser = XMLReaderFactory.createXMLReader();
			Handler handler = new Handler();
			parser.setContentHandler(handler);
			parser.setErrorHandler(handler);
			parser.setFeature( "http://xml.org/sax/features/validation", false);
			parser.setFeature("http://xml.org/sax/features/namespaces", false);
			parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
		} catch(SAXException e) {
			logger.log(Level.SEVERE, "exception while creating SAX parser", e);
			throw new PresetIOException("exception while creating SAX parser",e);			
		}
	}

	/**
	 * parses the presets given by the input source in {@see #getReader()}. 
	 * Creates a new set of presets if {@see #getPresets()} is null, otherwise
	 * parses the presets into the set of presets given by {@see #getPresets()}.
	 * 
	 * Call {@see #getPresets()} to retrieve the parsed presets after parsing.
	 * 
	 * @throws IllegalStateException thrown, if {@see #getReader()} is null. Set a properly initialized reader first.
	 * @throws PresetIOException if an exception is detected during parsing 
	 */
	public void parse()  throws PresetIOException {
		if (getReader() == null) {
			throw new IllegalStateException("reader is null. set reader first.");
		}
		if (getPresets() == null) {
			logger.warning("presets is null. Creating a new set of presets");
			setPresets(new Presets());
		}
		parse(getReader());
		return;
	}
	
	/**
	 * parses the presets given by the input source in {@see #getReader()}. 
	 * Creates a new set of presets if {@see #getPresets()} is null, otherwise
	 * parses the presets into the set of presets given by {@see #getPresets()}.
	 * 
	 * Call {@see #getPresets()} to retrieve the parsed presets after parsing.
	 * 
	 * @param reader  a properly initialized reader 
	 * @throws PresetIOException if an exception is detected during parsing 
	 */
	public void parse(Reader reader) throws PresetIOException {
		init();
		if (getPresets() == null) {
			logger.warning("presets is null. Creating a new set of presets");
			setPresets(new Presets());
		}
		try {
			parser.parse(new InputSource(reader));
		} catch(Exception e) {
			logger.log(Level.SEVERE, "exception while parsing preset file", e);
			throw new PresetIOException(e);
		}
		// "release" XML parser 
		parser = null;		
	}
	
	
	protected String translatedAttributeValue(String attrValue) {
		if (attrValue == null) {
			return null;
		} else {
			return tr(attrValue);
		}
	}
	
	protected void onStartGroup(String name, String iconName) {
		Group g = new Group();
		g.setName(translatedAttributeValue(name));
		g.setIconName(iconName);
		currentGroup.push(g);
	}
	
	protected void onEndGroup() {
		Group g = currentGroup.pop();
		presets.addGroup(g); 
	}
	
	protected void onStartItem(String name, String iconName) {
		currentItem = new Item();
		currentItem.setName(translatedAttributeValue(name));
		currentItem.setIconName(iconName);
	}
	
	protected void onEndItem() {
		if (currentGroup == null) {
			logger.log(Level.SEVERE, "illegal state. no current group defined");
			throw new IllegalStateException("illegal state. no current group defined");
		}
		currentGroup.peek().addItem(currentItem);
		currentItem = null; 
	}
	
	protected void onStartOptionalKeys() {
		this.inOptionalKeys = true; 
	}
	
	protected void onEndOptionalKeys() {
		this.inOptionalKeys = false; 
	}
	
	protected void onTag(String key, String value, String displayName){
		Tag tag = new Tag();
		tag.setKey(key);
		tag.setValue(value);
		tag.setDisplayName(translatedAttributeValue(displayName));
		tag.setOptional(inOptionalKeys);
		
		if (currentItem == null) {
			logger.log(Level.SEVERE, "illegal state. no current item defined");
			throw new IllegalStateException("illegal state. no current item defined");			
		}
		currentItem.addTag(tag);	
	}
	
	protected void onLabel(String label) {
		if (currentItem == null) {
			logger.log(Level.SEVERE, "illegal state. no current item defined");
			throw new IllegalStateException("illegal state. no current item defined");						
		}
		currentItem.setLabel(label);
	}
	
	/**
	 * The SAX handler for reading XML files with tag specifications 
	 *
	 *
	 */
	class Handler extends DefaultHandler {
		
		@Override
		public void endDocument() throws SAXException {
			logger.log(Level.FINE,"END");
		}

		@Override
		public void error(SAXParseException e) throws SAXException {
			logger.log(Level.SEVERE, "XML parsing error", e);
		}

		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			logger.log(Level.SEVERE, "XML parsing error", e);
		}

		@Override
		public void startDocument() throws SAXException {
			logger.log(Level.FINE,"START");
		}

		protected String getAttribute(Attributes attributes, String qName) {
			for (int i =0; i < attributes.getLength();i++) {
				if (attributes.getQName(i).equals(qName)) {
					return attributes.getValue(i);
				}
			}
			return null;
		}
		
		@Override
		public void startElement(String namespaceURI, String localName, String qName,
				Attributes atts) throws SAXException {
			if ("group".equals(qName)) {
				onStartGroup(getAttribute(atts, "name"), getAttribute(atts, "icon"));
			} else if ("item".equals(qName)) {
				onStartItem(getAttribute(atts, "name"), getAttribute(atts, "icon"));
			} else if ("label".equals(qName)) {
				onLabel(getAttribute(atts, "text")); 
			} else if ("optional".equals(qName)) {
				onStartOptionalKeys();
			} else if ("key".equals(qName) || "text".equals(qName) || "combo".equals(qName) 
					   || "check".equals(qName)) {
				onTag(getAttribute(atts, "key"), getAttribute(atts, "value"), getAttribute(atts, "text"));
			}
			
		}
		
		@Override
		public void endElement(String namespaceURI, String localName, String qName)
				throws SAXException {
			if ("group".equals(qName)) {
				onEndGroup();
			} else if ("item".equals(qName)) {
				onEndItem();
			} else if ("label".equals(qName)) {
				// do nothing
			} else if ("optional".equals(qName)) {
				onEndOptionalKeys();
			}			
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
		 */
		@Override
		public void warning(SAXParseException e) throws SAXException {
			// TODO Auto-generated method stub
			logger.log(Level.WARNING, "XML parsing warning", e);
		}
	}
}
