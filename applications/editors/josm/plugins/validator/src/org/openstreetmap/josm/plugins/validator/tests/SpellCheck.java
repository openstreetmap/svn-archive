package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.annotation.AnnotationPreset;
import org.openstreetmap.josm.gui.annotation.AnnotationPreset.*;
import org.openstreetmap.josm.gui.preferences.AnnotationPresetPreference;
import org.openstreetmap.josm.plugins.validator.*;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.XmlObjectParser;
import org.xml.sax.SAXException;

/**
 * Check for mispelled properties
 * 
 * @author frsantos
 */
public class SpellCheck extends Test 
{
	/** The spell check key substitutions: the key should be substituted by the value */
	protected static Map<String, String> spellCheckKeyData;

	/** The spell check preset values */
	protected static Bag<String, String> spellCheckValueData;
	
	/** Preference name for checking values */
	public static final String PREF_CHECK_VALUES = "tests." + SpellCheck.class.getSimpleName() + ".checkValues";
	
	/** Whether to check values too */
	protected boolean checkValues = false;

	/** Preferences checkbox */
	protected JCheckBox prefCheckValues;

	/** Empty values error */
	protected static int EMPTY_VALUES 	= 0;
	/** Invalid key error */
	protected static int INVALID_KEY  	= 1;
	/** Invalid value error */
	protected static int INVALID_VALUE 	= 2;
	
	/**
	 * Constructor
	 */
	public SpellCheck() 
	{
		super(tr("Spell checker."),
			  tr("This plugin checks misspelled property keys and values."));
	}

	public static void initialize(OSMValidatorPlugin plugin) throws Exception
	{
		initializeSpellCheck(plugin);
		initializePresets(plugin);
	}

	/**
	 * Reads the spellcheck file into a HashMap.
	 * <p>
	 * The data file is a list of words, beginning with +/-. If it starts with +,
	 * the word is valid, but if it starts with -, the word should be replaced
	 * by the nearest + word before this.
	 * 
	 * @param plugin The validator plugin 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	private static void initializeSpellCheck(OSMValidatorPlugin plugin) throws FileNotFoundException, IOException 
	{
		plugin.copy("/resources/spellCheck.data", "spellCheck.data");
		BufferedReader reader = new BufferedReader( new FileReader(plugin.getPluginDir() + "/spellCheck.data") );
		
		spellCheckKeyData = new HashMap<String, String>();
		String okValue = null;
		do
		{
			String line = reader.readLine();
			if( line == null || line.length() == 0 )
				break;

			if( line.charAt(0) == '+' )
			{
				okValue = line.substring(1);
			}
			else if( line.charAt(0) == '-' && okValue != null )
			{
				spellCheckKeyData.put(line.substring(1), okValue);
			}
			else
			{
				System.err.println("Invalid spellcheck line:" + line);
			}
		}
		while( true );
	}
	
	/**
	 * Reads the presets data.
	 * 
	 * @param plugin The validator plugin
	 * @throws Exception
	 */
	public static void initializePresets(@SuppressWarnings("unused") OSMValidatorPlugin plugin) throws Exception
	{
		if( Main.pref.getBoolean(PREF_CHECK_VALUES) )
			return;
		
		Collection<AnnotationPreset> presets = AnnotationPresetPreference.annotationPresets;
		if( presets == null || presets.isEmpty() )
		{
			// Skip re-reading presets if there are none available
			return;
		}
		
		spellCheckValueData = new Bag<String, String>();
		readPresetFromPreferences();
		
		// TODO: allow per user word definitions
	}
	
	
	@Override
	public void visit(Node n)
	{
		checkPrimitive(n);
	}


	@Override
	public void visit(Segment s) 
	{
		checkPrimitive(s);
	}


	@Override
	public void visit(Way w) 
	{
		checkPrimitive(w);
	}
	
	/**
	 * Checks the spelling of the primitive properties
	 * @param p The primitive to check
	 */
	private void checkPrimitive(OsmPrimitive p)
	{
	    // Just a collection to know if a primitive has been already marked with error
        Bag<OsmPrimitive, String> withErrors = new Bag<OsmPrimitive, String>();
        
        Map<String, String> props = (p.keys == null) ? Collections.<String, String>emptyMap() : p.keys;
		for(Entry<String, String> prop: props.entrySet() )
		{
			String key = prop.getKey();
			String value = prop.getValue();
			if( (value==null || value.trim().length() == 0) && !withErrors.contains(p, "EV"))
			{
				errors.add( new TestError(this, Severity.WARNING, tr("Tags with empty values"), p, EMPTY_VALUES) );
				withErrors.add(p, "EV");
			}
			if( spellCheckKeyData.containsKey(key) && !withErrors.contains(p, "IPK"))
			{
				errors.add( new TestError(this, Severity.WARNING, tr("Invalid property keys"), p, INVALID_KEY) );
				withErrors.add(p, "IPK");
			}
			if( checkValues && value != null && value.length() > 0 )
			{
				List<String> values = spellCheckValueData.get(key);
				if( values != null && !values.contains(prop.getValue()) && !withErrors.contains(p, "UPV"))
				{
					errors.add( new TestError(this, Severity.OTHER, tr("Unknown property values"), p, INVALID_VALUE) );
					withErrors.add(p, "UPV");
				}
			}
		}
	}
	
	/**
	 * Parse an anotation preset from a stream
	 * 
	 * @param inStream The stream of the anotstion preset
	 * @throws SAXException
	 */
	public static void readPresets(InputStream inStream) throws SAXException 
	{
		BufferedReader in = null;
		try 
		{
			in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
			in = new BufferedReader(new InputStreamReader(inStream));
		}
		
		XmlObjectParser parser = new XmlObjectParser();
		parser.mapOnStart("item", AnnotationPreset.class);
		parser.map("text", Text.class);
		parser.map("check", Check.class);
		parser.map("combo", Combo.class);
		parser.map("label", Label.class);
		parser.map("key", Key.class);
		parser.start(in);
		
		while(parser.hasNext()) 
		{
			Object obj = parser.next();
			if (obj instanceof Combo) {
				Combo combo = (Combo)obj;
				for(String value :  combo.values.split(",") )
					spellCheckValueData.add(combo.key, value);
			}
		}
	}

	/**
	 * Reads the annotations presets
	 */
	public static void readPresetFromPreferences() 
	{
		String allAnnotations = Main.pref.get("annotation.sources");
		StringTokenizer st = new StringTokenizer(allAnnotations, ";");
		while (st.hasMoreTokens()) 
		{
			InputStream in = null;
			String source = st.nextToken();
			try 
			{
				if (source.startsWith("http") || source.startsWith("ftp") || source.startsWith("file"))
					in = new URL(source).openStream();
				else if (source.startsWith("resource://"))
					in = Main.class.getResourceAsStream(source.substring("resource:/".length()));
				else
					in = new FileInputStream(source);
				readPresets(in);
				in.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(Main.parent, tr("Could not read annotation preset source: {0}",source));
			} 
			catch (SAXException e) 
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(Main.parent, tr("Error parsing {0}: ", source)+e.getMessage());
			}
		}
	}

	@Override
	public void startTest() 
	{
		checkValues = Main.pref.getBoolean("tests." + getClass().getSimpleName() + ".checkValues");
	}

	@Override
	public void addGui(JPanel testPanel)
	{
		boolean checkValues = Main.pref.getBoolean(PREF_CHECK_VALUES);
		
		String text = tr("Check also property values from presets");
		prefCheckValues = new JCheckBox(text, checkValues);
		prefCheckValues.setToolTipText(text);
		testPanel.add(prefCheckValues, GBC.eop().insets(40,0,0,0));
	}

	@Override
	public void ok() 
	{
		Main.pref.put(PREF_CHECK_VALUES, prefCheckValues.isSelected());
	}
	
	@Override
	public Command fixError(TestError testError)
	{
		List<Command> commands = new ArrayList<Command>(50);
		
		int i = -1;
		List<OsmPrimitive> primitives = testError.getPrimitives();
		for(OsmPrimitive p : primitives )
		{
			i++;
			Map<String, String> tags = p.keys;
			if( tags == null || tags.size() == 0 )
				continue;
		
			for(Entry<String, String> prop: tags.entrySet() )
			{
				String key = prop.getKey();
				String value = prop.getValue();
				if( value == null || value.trim().length() == 0 )
					commands.add( new ChangePropertyCommand(primitives.subList(i, i+1), key, null) );
				else
				{
					String replacementKey = spellCheckKeyData.get(key);
					if( replacementKey != null )
						commands.add( new ChangePropertyKeyCommand(primitives.subList(i, i+1), key, replacementKey) );					
				}
			}
		}
		
		return commands.size() > 1 ? new SequenceCommand("Fix properties", commands) : commands.get(0);
	}
	
	@Override
	public boolean isFixable(TestError testError)
	{
		if( testError.getTester() instanceof SpellCheck)
		{
			int code = testError.getInternalCode();
			return code == INVALID_KEY || code == EMPTY_VALUES;
		}
		
		return false;
	}	
}
	
	
