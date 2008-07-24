package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.tagging.TaggingPreset;
import org.openstreetmap.josm.gui.tagging.TaggingPreset.*;
import org.openstreetmap.josm.gui.preferences.TaggingPresetPreference;
import org.openstreetmap.josm.plugins.validator.*;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.plugins.validator.util.Util;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.XmlObjectParser;
import org.xml.sax.SAXException;

/**
 * Check for mispelled or wrong properties
 *
 * @author frsantos
 */
public class TagChecker extends Test
{
	/** The default data files */
	public static final String DATA_FILE = "http://svn.openstreetmap.org/applications/editors/josm/plugins/validator/tagchecker.cfg";
	public static final String SPELL_FILE = "http://svn.openstreetmap.org/applications/utils/planet.osm/java/speller/words.cfg";

	/** The spell check key substitutions: the key should be substituted by the value */
	protected static Map<String, String> spellCheckKeyData;
	/** The spell check preset values */
	protected static Bag<String, String> presetsValueData;

	/** The preferences prefix */
	protected static final String PREFIX = PreferenceEditor.PREFIX + "." + TagChecker.class.getSimpleName();

	/** Preference name for checking values */
	public static final String PREF_CHECK_VALUES = PREFIX + ".checkValues";
	/** Preference name for checking values */
	public static final String PREF_CHECK_KEYS = PREFIX + ".checkKeys";
	/** Preference name for checking FIXMES */
	public static final String PREF_CHECK_FIXMES = PREFIX + ".checkFixmes";
	/** Preference name for sources */
	public static final String PREF_SOURCES = PREFIX + ".sources";
	/** Preference name for sources */
	public static final String PREF_USE_DATA_FILE = PREFIX + ".usedatafile";
	/** Preference name for sources */
	public static final String PREF_USE_SPELL_FILE = PREFIX + ".usespellfile";
	/** Preference name for keys upload check */
	public static final String PREF_CHECK_KEYS_BEFORE_UPLOAD = PREFIX + ".checkKeysBeforeUpload";
	/** Preference name for values upload check */
	public static final String PREF_CHECK_VALUES_BEFORE_UPLOAD = PREFIX + ".checkValuesBeforeUpload";
	/** Preference name for fixmes upload check */
	public static final String PREF_CHECK_FIXMES_BEFORE_UPLOAD = PREFIX + ".checkFixmesBeforeUpload";

	/** Whether to check keys */
	protected boolean checkKeys = false;
	/** Whether to check values */
	protected boolean checkValues = false;
	/** Whether to check for fixmes in values */
	protected boolean checkFixmes = false;

	/** Preferences checkbox for keys */
	protected JCheckBox prefCheckKeys;
	/** Preferences checkbox for values */
	protected JCheckBox prefCheckValues;
	/** Preferences checkbox for FIXMES */
	protected JCheckBox prefCheckFixmes;
	/** The preferences checkbox for validation of keys on upload */
	protected JCheckBox prefCheckKeysBeforeUpload;
	/** The preferences checkbox for validation of values on upload */
	protected JCheckBox prefCheckValuesBeforeUpload;
	/** The preferences checkbox for validation of fixmes on upload */
	protected JCheckBox prefCheckFixmesBeforeUpload;
	/** The add button */
	protected JButton addSrcButton;
	/** The edit button */
	protected JButton editSrcButton;
	/** The delete button */
	protected JButton deleteSrcButton;

	protected static int EMPTY_VALUES = 0; /** Empty values error */
	protected static int INVALID_KEY = 1; /** Invalid key error */
	protected static int INVALID_VALUE = 2; /** Invalid value error */
	protected static int FIXME = 3; /** fixme error */
	protected static int INVALID_SPACE = 3; /** space in value (start/end) */

	/** List of sources for spellcheck data */
	protected JList Sources;

	/** Whether this test must check the keys before upload. Used by peferences */
	protected boolean testKeysBeforeUpload;
	/** Whether this test must check the values before upload. Used by peferences */
	protected boolean testValuesBeforeUpload;
	/** Whether this test must check form fixmes in values before upload. Used by peferences */
	protected boolean testFixmesBeforeUpload;

	/**
	 * Constructor
	 */
	public TagChecker()
	{
		super(tr("Properties checker :"),
			  tr("This plugin checks for errors in property keys and values."));
	}

	public static void initialize(OSMValidatorPlugin plugin) throws Exception
	{
		initializeData();
		initializePresets();
	}

	/**
	 * Reads the spellcheck file into a HashMap.
	 * <p>
	 * The data file is a list of words, beginning with +/-. If it starts with +,
	 * the word is valid, but if it starts with -, the word should be replaced
	 * by the nearest + word before this.
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void initializeData() throws FileNotFoundException, IOException
	{
		spellCheckKeyData = new HashMap<String, String>();
		String sources = Main.pref.get( PREF_SOURCES );
		if(Main.pref.getBoolean(PREF_USE_DATA_FILE))
		{
			if( sources == null || sources.length() == 0)
				sources = DATA_FILE;
			else
				sources = DATA_FILE + ";" + sources;
		}
		if(Main.pref.getBoolean(PREF_USE_SPELL_FILE))
		{
			if( sources == null || sources.length() == 0)
				sources = SPELL_FILE;
			else
				sources = SPELL_FILE + ";" + sources;
		}
		
		StringTokenizer st = new StringTokenizer(sources, ";");
		StringBuilder errorSources = new StringBuilder();
		while (st.hasMoreTokens())
		{
			String source = st.nextToken();
			File sourceFile = Util.mirror(new URL(source), Util.getPluginDir(), -1);
			if( sourceFile == null || !sourceFile.exists() )
			{
				errorSources.append(source).append("\n");
				continue;
			}

			BufferedReader reader = new BufferedReader(new FileReader(sourceFile));

			String okValue = null;
			do
			{
				String line = reader.readLine();
				if( line == null || line.length() == 0 )
					break;
				if( line.startsWith("#") )
					continue;
	
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

		if( errorSources.length() > 0 )
			throw new IOException( tr("Could not download data file(s):\n{0}", errorSources) );
	}
	
	/**
	 * Reads the presets data.
	 *
	 * @throws Exception
	 */
	public static void initializePresets() throws Exception
	{
		if( !Main.pref.getBoolean(PREF_CHECK_VALUES) )
			return;

		Collection<TaggingPreset> presets = TaggingPresetPreference.taggingPresets;
		if( presets == null || presets.isEmpty() )
		{
			// Skip re-reading presets if there are none available
			return;
		}

		presetsValueData = new Bag<String, String>();
		readPresetFromPreferences();
	}
	
	
	@Override
	public void visit(Node n)
	{
		checkPrimitive(n);
	}


	@Override
	public void visit(Way w)
	{
		checkPrimitive(w);
	}

	/**
	 * Checks the primitive properties
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
			if( checkValues && (value==null || value.trim().length() == 0) && !withErrors.contains(p, "EV"))
			{
				errors.add( new TestError(this, Severity.WARNING, tr("Tags with empty values"), p, EMPTY_VALUES) );
				withErrors.add(p, "EV");
			}
			if( checkKeys && spellCheckKeyData.containsKey(key) && !withErrors.contains(p, "IPK"))
			{
				errors.add( new TestError(this, Severity.WARNING, tr("Invalid property key ''{0}''", key), p, INVALID_KEY) );
				withErrors.add(p, "IPK");
			}
			if( checkKeys && key.indexOf(" ") >= 0 && !withErrors.contains(p, "IPK"))
			{
				errors.add( new TestError(this, Severity.WARNING, tr("Invalid white space in property key ''{0}''", key), p, INVALID_KEY) );
				withErrors.add(p, "IPK");
			}
			if( checkValues && value != null && (value.startsWith(" ") || value.endsWith(" ")) && !withErrors.contains(p, "SPACE"))
			{
				errors.add( new TestError(this, Severity.OTHER, tr("Property values start or end with white space"), p, INVALID_SPACE) );
				withErrors.add(p, "SPACE");
			}
			if( checkValues && value != null && value.length() > 0 && presetsValueData != null)
			{
				List<String> values = presetsValueData.get(key);
				if( values != null && !values.contains(prop.getValue()) && !withErrors.contains(p, "UPV"))
				{
					errors.add( new TestError(this, Severity.OTHER, tr("Unknown property values"), p, INVALID_VALUE) );
					withErrors.add(p, "UPV");
				}
			}
			if( checkFixmes && value != null && value.length() > 0 )
			{
				if( (value.contains("FIXME") || value.contains("check and delete") || key.contains("todo") || key.contains("fixme"))
				&& !withErrors.contains(p, "FIXME"))
				{
					errors.add( new TestError(this, Severity.OTHER, tr("FIXMES"), p, FIXME) );
					withErrors.add(p, "FIXME");
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
		parser.mapOnStart("item", TaggingPreset.class);
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
				for(String value : combo.values.split(",") )
					presetsValueData.add(combo.key, value);
			}
		}
	}

	/**
	 * Reads the tagging presets
	 */
	public static void readPresetFromPreferences()
	{
		String allAnnotations = Main.pref.get("taggingpreset.sources");
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
				// Error already reported by JOSM
			} 
			catch (SAXException e)
			{
				// Error already reported by JOSM
			}
		}
	}

	@Override
	public void startTest()
	{
		checkKeys = Main.pref.getBoolean(PREF_CHECK_KEYS);
		if( isBeforeUpload )
			checkKeys = checkKeys && Main.pref.getBoolean(PREF_CHECK_KEYS_BEFORE_UPLOAD, true);

		checkValues = Main.pref.getBoolean(PREF_CHECK_VALUES);
		if( isBeforeUpload )
			checkValues = checkValues && Main.pref.getBoolean(PREF_CHECK_VALUES_BEFORE_UPLOAD, true);

		checkFixmes = Main.pref.getBoolean(PREF_CHECK_FIXMES);
		if( isBeforeUpload )
			checkFixmes = checkFixmes && Main.pref.getBoolean(PREF_CHECK_FIXMES_BEFORE_UPLOAD, true);
	}

	@Override
	public void visit(Collection<OsmPrimitive> selection)
	{
		if( checkKeys || checkValues)
			super.visit(selection);
	}
	
	@Override
	public void addGui(JPanel testPanel)
	{
		GBC a = GBC.eol();
		a.anchor = GBC.EAST;

		testPanel.add( new JLabel(name), GBC.eol().insets(3,0,0,0) );
		
		boolean checkKeys = Main.pref.getBoolean(PREF_CHECK_KEYS, true);
		prefCheckKeys = new JCheckBox(tr("Check property keys."), checkKeys);
		prefCheckKeys.setToolTipText(tr("Validate that property keys are valid checking against list of words."));
		testPanel.add(prefCheckKeys, GBC.std().insets(20,0,0,0));

		prefCheckKeysBeforeUpload = new JCheckBox();
		prefCheckKeysBeforeUpload.setSelected(Main.pref.getBoolean(PREF_CHECK_KEYS_BEFORE_UPLOAD, true));
		testPanel.add(prefCheckKeysBeforeUpload, a);

		Sources = new JList(new DefaultListModel());

		String sources = Main.pref.get( PREF_SOURCES );
		StringTokenizer st = new StringTokenizer(sources, ";");
		while (st.hasMoreTokens())
			((DefaultListModel)Sources.getModel()).addElement(st.nextToken());

		addSrcButton = new JButton(tr("Add"));
		addSrcButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String source = JOptionPane.showInputDialog(Main.parent, tr("TagChecker source"));
				if (source != null)
					((DefaultListModel)Sources.getModel()).addElement(source);
				Sources.clearSelection();
			}
		});

		editSrcButton = new JButton(tr("Edit"));
		editSrcButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int row = Sources.getSelectedIndex();
				if(row == -1 && Sources.getModel().getSize() == 1)
				{
					Sources.setSelectedIndex(0);
					row = 0;
				}
				if (row == -1)
				{
					if(Sources.getModel().getSize() == 0)
					{
						String source = JOptionPane.showInputDialog(Main.parent, tr("TagChecker source"));
						if (source != null)
							((DefaultListModel)Sources.getModel()).addElement(source);
					}
					else
					{
						JOptionPane.showMessageDialog(Main.parent, tr("Please select the row to edit."));
					}
				}
				else {
					String source = JOptionPane.showInputDialog(Main.parent, tr("TagChecker source"), Sources.getSelectedValue());
					if (source != null)
						((DefaultListModel)Sources.getModel()).setElementAt(source, row);
				}
				Sources.clearSelection();
			}
		});

		deleteSrcButton = new JButton(tr("Delete"));
		deleteSrcButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (Sources.getSelectedIndex() == -1)
					JOptionPane.showMessageDialog(Main.parent, tr("Please select the row to delete."));
				else {
					((DefaultListModel)Sources.getModel()).remove(Sources.getSelectedIndex());
				}
			}
		});
		Sources.setVisibleRowCount(3);

		Sources.setToolTipText(tr("The sources (url or filename) of spell check (see http://wiki.openstreetmap.org/index.php/User:JLS/speller) or tag checking data files."));
		addSrcButton.setToolTipText(tr("Add a new source to the list."));
		editSrcButton.setToolTipText(tr("Edit the selected source."));
		deleteSrcButton.setToolTipText(tr("Delete the selected source from the list."));

		testPanel.add(new JLabel(tr("Data sources")), GBC.eol().insets(23,0,0,0));
		testPanel.add(new JScrollPane(Sources), GBC.eol().insets(23,0,0,0).fill(GBC.HORIZONTAL));
		final JPanel buttonPanel = new JPanel(new GridBagLayout());
		testPanel.add(buttonPanel, GBC.eol().fill(GBC.HORIZONTAL));
		buttonPanel.add(addSrcButton, GBC.std().insets(0,5,0,0));
		buttonPanel.add(editSrcButton, GBC.std().insets(5,5,5,0));
		buttonPanel.add(deleteSrcButton, GBC.std().insets(0,5,0,0));

		ActionListener disableCheckKeysActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				boolean selected = prefCheckKeys.isSelected() || prefCheckKeysBeforeUpload.isSelected();
				Sources.setEnabled( selected );
				addSrcButton.setEnabled(selected);
				editSrcButton.setEnabled(selected);
				deleteSrcButton.setEnabled(selected);
			}
		};
		prefCheckKeys.addActionListener(disableCheckKeysActionListener);
		prefCheckKeysBeforeUpload.addActionListener(disableCheckKeysActionListener);

		Sources.setEnabled( checkKeys );
		buttonPanel.setEnabled( checkKeys );

		boolean checkValues = Main.pref.getBoolean(PREF_CHECK_VALUES, true);
		prefCheckValues = new JCheckBox(tr("Check property values."), checkValues);
		prefCheckValues.setToolTipText(tr("Validate that property values are valid checking against presets."));
		testPanel.add(prefCheckValues, GBC.std().insets(20,0,0,0));

		prefCheckValuesBeforeUpload = new JCheckBox();
		prefCheckValuesBeforeUpload.setSelected(Main.pref.getBoolean(PREF_CHECK_VALUES_BEFORE_UPLOAD, true));
		testPanel.add(prefCheckValuesBeforeUpload, a);

		boolean checkFixmes = Main.pref.getBoolean(PREF_CHECK_FIXMES, true);
		prefCheckFixmes = new JCheckBox(tr("Check for FIXMES."), checkFixmes);
		prefCheckFixmes.setToolTipText(tr("Looks for nodes or ways with FIXME in any property value."));
		testPanel.add(prefCheckFixmes, GBC.std().insets(20,0,0,0));

		prefCheckFixmesBeforeUpload = new JCheckBox();
		prefCheckFixmesBeforeUpload.setSelected(Main.pref.getBoolean(PREF_CHECK_FIXMES_BEFORE_UPLOAD, true));
		testPanel.add(prefCheckFixmesBeforeUpload, a);

		boolean useDataFile = Main.pref.getBoolean(PREF_USE_DATA_FILE, true);
		JCheckBox prefUseDataFile = new JCheckBox(tr("Use default data file."), checkValues);
		prefUseDataFile.setToolTipText(tr("Use the default data file (recommended)."));
		testPanel.add(prefUseDataFile, GBC.eol().insets(20,0,0,0));

		boolean useSpellFile = Main.pref.getBoolean(PREF_USE_SPELL_FILE, true);
		JCheckBox prefUseSpellFile = new JCheckBox(tr("Use default spellcheck file."), checkValues);
		prefUseSpellFile.setToolTipText(tr("Use the default spellcheck file (recommended)."));
		testPanel.add(prefUseSpellFile, GBC.eol().insets(20,0,0,0));
	}

	@Override
	public void ok()
	{
		enabled = prefCheckKeys.isSelected() || prefCheckValues.isSelected() || prefCheckFixmes.isSelected();
		testBeforeUpload = prefCheckKeysBeforeUpload.isSelected() || prefCheckValuesBeforeUpload.isSelected() || prefCheckFixmesBeforeUpload.isSelected();

		Main.pref.put(PREF_CHECK_VALUES, prefCheckValues.isSelected());
		Main.pref.put(PREF_CHECK_KEYS, prefCheckKeys.isSelected());
		Main.pref.put(PREF_CHECK_FIXMES, prefCheckFixmes.isSelected());
		Main.pref.put(PREF_CHECK_VALUES_BEFORE_UPLOAD, prefCheckValuesBeforeUpload.isSelected());
		Main.pref.put(PREF_CHECK_KEYS_BEFORE_UPLOAD, prefCheckKeysBeforeUpload.isSelected());
		Main.pref.put(PREF_CHECK_FIXMES_BEFORE_UPLOAD, prefCheckFixmesBeforeUpload.isSelected());
		Main.pref.put(PREF_USE_DATA_FILE, prefCheckFixmesBeforeUpload.isSelected());
		Main.pref.put(PREF_USE_SPELL_FILE, prefCheckFixmesBeforeUpload.isSelected());
		String sources = "";
		if( Sources.getModel().getSize() > 0 )
		{
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < Sources.getModel().getSize(); ++i)
				sb.append(";"+Sources.getModel().getElementAt(i));
			sources = sb.substring(1);
		}
		Main.pref.put(PREF_SOURCES, sources );
	}

	@Override
	public Command fixError(TestError testError)
	{
		List<Command> commands = new ArrayList<Command>(50);

		int i = -1;
		List<? extends OsmPrimitive> primitives = testError.getPrimitives();
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
					commands.add( new ChangePropertyCommand(Collections.singleton(primitives.get(i)), key, null) );
				else if(value.startsWith(" ") || value.endsWith(" "))
					commands.add( new ChangePropertyCommand(Collections.singleton(primitives.get(i)), key, value.trim()) );
				else
				{
					String replacementKey = spellCheckKeyData.get(key);
					if( replacementKey != null )
						commands.add( new ChangePropertyKeyCommand(Collections.singleton(primitives.get(i)), key, replacementKey) );
				}
			}
		}

		if( commands.size() == 0 )
			return null;
		else if( commands.size() == 1 )
			return commands.get(0);
		else
			return new SequenceCommand(tr("Fix properties"), commands);
	}

	@Override
	public boolean isFixable(TestError testError)
	{
		if( testError.getTester() instanceof TagChecker)
		{
			int code = testError.getInternalCode();
			return code == INVALID_KEY || code == EMPTY_VALUES || code == INVALID_SPACE;
		}

		return false;
	}
}
