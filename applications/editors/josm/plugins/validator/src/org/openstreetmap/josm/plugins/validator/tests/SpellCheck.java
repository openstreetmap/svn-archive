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
import org.openstreetmap.josm.gui.annotation.AnnotationPreset;
import org.openstreetmap.josm.gui.annotation.AnnotationPreset.*;
import org.openstreetmap.josm.gui.preferences.AnnotationPresetPreference;
import org.openstreetmap.josm.plugins.validator.*;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.plugins.validator.util.Util;
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
	/** The default spellcheck data file */
    public static final String SPELLCHECK_DATA_FILE = "http://svn.openstreetmap.org/applications/utils/planet.osm/java/speller/words.cfg";

    /** The spell check key substitutions: the key should be substituted by the value */
	protected static Map<String, String> spellCheckKeyData;

	/** The spell check preset values */
	protected static Bag<String, String> spellCheckValueData;
	
    /** Preference name for checking values */
    public static final String PREF_CHECK_VALUES = "tests." + SpellCheck.class.getSimpleName() + ".checkValues";
    /** Preference name for checking values */
    public static final String PREF_CHECK_KEYS = "tests." + SpellCheck.class.getSimpleName() + ".checkKeys";
    /** Preference name for checking FIXMES */
    public static final String PREF_CHECK_FIXMES = "tests." + SpellCheck.class.getSimpleName() + ".checkFixmes";
    /** Preference name for sources */
    public static final String PREF_SOURCES = "tests." + SpellCheck.class.getSimpleName() + ".sources";
    /** Preference name for global upload check */
    public static final String PREF_CHECK_BEFORE_UPLOAD = "tests." + SpellCheck.class.getSimpleName() + ".checkBeforeUpload";
    /** Preference name for keys upload check */
    public static final String PREF_CHECK_KEYS_BEFORE_UPLOAD = "tests." + SpellCheck.class.getSimpleName() + ".checkKeysBeforeUpload";
    /** Preference name for values upload check */
    public static final String PREF_CHECK_VALUES_BEFORE_UPLOAD = "tests." + SpellCheck.class.getSimpleName() + ".checkValuesBeforeUpload";
    /** Preference name for fixmes upload check */
    public static final String PREF_CHECK_FIXMES_BEFORE_UPLOAD = "tests." + SpellCheck.class.getSimpleName() + ".checkFixmesBeforeUpload";
	
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

	/** Empty values error */
	protected static int EMPTY_VALUES 	= 0;
	/** Invalid key error */
	protected static int INVALID_KEY  	= 1;
    /** Invalid value error */
    protected static int INVALID_VALUE  = 2;
    /** fixme error */
    protected static int FIXME          = 3;
	
    /** List of sources for spellcheck data */
    protected JList spellcheckSources;

    /** Whether this test must check the keys before upload. Used by peferences */
    protected boolean testKeysBeforeUpload;
    /** Whether this test must check the values before upload. Used by peferences */
    protected boolean testValuesBeforeUpload;
    /** Whether this test must check form fixmes in values before upload. Used by peferences */
    protected boolean testFixmesBeforeUpload;
    
	/**
	 * Constructor
	 */
	public SpellCheck() 
	{
		super(tr("Properties checker."),
			  tr("This plugin checks for errors in property keys and values."));
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
		spellCheckKeyData = new HashMap<String, String>();
        String sources = Main.pref.get( PREF_SOURCES );
        if( sources == null || sources.length() == 0)
            sources = SPELLCHECK_DATA_FILE;
        
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
            throw new IOException( tr("Could not download spellcheck data file:\n {0}", errorSources) );

	}
	
	/**
	 * Reads the presets data.
	 * 
	 * @param plugin The validator plugin
	 * @throws Exception
	 */
	public static void initializePresets(@SuppressWarnings("unused") OSMValidatorPlugin plugin) throws Exception
	{
		if( !Main.pref.getBoolean(PREF_CHECK_VALUES) )
			return;
		
		Collection<AnnotationPreset> presets = AnnotationPresetPreference.annotationPresets;
		if( presets == null || presets.isEmpty() )
		{
			// Skip re-reading presets if there are none available
			return;
		}
		
		spellCheckValueData = new Bag<String, String>();
		readPresetFromPreferences();
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
			if( checkValues && (value==null || value.trim().length() == 0) && !withErrors.contains(p, "EV"))
			{
				errors.add( new TestError(this, Severity.WARNING, tr("Tags with empty values"), p, EMPTY_VALUES) );
				withErrors.add(p, "EV");
			}
			if( checkKeys && spellCheckKeyData.containsKey(key) && !withErrors.contains(p, "IPK"))
			{
				errors.add( new TestError(this, Severity.WARNING, tr("Invalid property keys"), p, INVALID_KEY) );
				withErrors.add(p, "IPK");
			}
            if( checkValues && value != null && value.length() > 0 && spellCheckValueData != null)
            {
                List<String> values = spellCheckValueData.get(key);
                if( values != null && !values.contains(prop.getValue()) && !withErrors.contains(p, "UPV"))
                {
                    errors.add( new TestError(this, Severity.OTHER, tr("Unknown property values"), p, INVALID_VALUE) );
                    withErrors.add(p, "UPV");
                }
            }
            if( checkFixmes && value != null && value.length() > 0 )
            {
                if( value.contains("FIXME") && !withErrors.contains(p, "FIXME"))
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
        testPanel.add( new JLabel(), GBC.eol());
        
        boolean checkKeys = Main.pref.getBoolean(PREF_CHECK_KEYS, true);
        prefCheckKeys = new JCheckBox(tr("Check property keys."), checkKeys);
        prefCheckKeys .setToolTipText(tr("Validate that property keys are valid checking against list of words."));
        testPanel.add(prefCheckKeys, GBC.std().insets(40,0,0,0));

        prefCheckKeysBeforeUpload = new JCheckBox();
        prefCheckKeysBeforeUpload.setSelected(Main.pref.getBoolean(PREF_CHECK_KEYS_BEFORE_UPLOAD, true));
        testPanel.add(prefCheckKeysBeforeUpload, GBC.eop().insets(20,0,0,0));
        
        spellcheckSources = new JList(new DefaultListModel());
        if( !Main.pref.hasKey(PREF_SOURCES))
            Main.pref.put(PREF_SOURCES, SPELLCHECK_DATA_FILE);
        
        String sources = Main.pref.get( PREF_SOURCES );
        StringTokenizer st = new StringTokenizer(sources, ";");
        while (st.hasMoreTokens())
            ((DefaultListModel)spellcheckSources.getModel()).addElement(st.nextToken());
        
        addSrcButton = new JButton(tr("Add"));
        addSrcButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String source = JOptionPane.showInputDialog(Main.parent, tr("Spellcheck source"));
                if (source == null)
                    return;
                ((DefaultListModel)spellcheckSources.getModel()).addElement(source);
            }
        });

        editSrcButton = new JButton(tr("Edit"));
        editSrcButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (spellcheckSources.getSelectedIndex() == -1)
                    JOptionPane.showMessageDialog(Main.parent, tr("Please select the row to edit."));
                else {
                    String source = JOptionPane.showInputDialog(Main.parent, tr("Spellcheck source"), spellcheckSources.getSelectedValue());
                    if (source == null)
                        return;
                    ((DefaultListModel)spellcheckSources.getModel()).setElementAt(source, spellcheckSources.getSelectedIndex());
                }
            }
        });

        deleteSrcButton = new JButton(tr("Delete"));
        deleteSrcButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (spellcheckSources.getSelectedIndex() == -1)
                    JOptionPane.showMessageDialog(Main.parent, tr("Please select the row to delete."));
                else {
                    ((DefaultListModel)spellcheckSources.getModel()).remove(spellcheckSources.getSelectedIndex());
                }
            }
        });
        spellcheckSources.setVisibleRowCount(3);

        spellcheckSources.setToolTipText(tr("The sources (url or filename) of spell check data files. See http://wiki.openstreetmap.org/index.php/User:JLS/speller for help."));
        addSrcButton.setToolTipText(tr("Add a new spellcheck source to the list."));
        editSrcButton.setToolTipText(tr("Edit the selected source."));
        deleteSrcButton.setToolTipText(tr("Delete the selected source from the list."));

        testPanel.add(new JLabel(tr("Spellcheck data sources")), GBC.eol().insets(40,0,0,0));
        testPanel.add(new JScrollPane(spellcheckSources), GBC.eol().insets(40,0,0,0));
        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        testPanel.add(buttonPanel, GBC.eol().fill(GBC.HORIZONTAL));
        buttonPanel.add(addSrcButton, GBC.std().insets(0,5,0,0));
        buttonPanel.add(editSrcButton, GBC.std().insets(5,5,5,0));
        buttonPanel.add(deleteSrcButton, GBC.std().insets(0,5,0,0));
        
        prefCheckKeys.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                boolean selected = prefCheckKeys.isSelected();
                spellcheckSources.setEnabled( selected );
                addSrcButton.setEnabled(selected);
                editSrcButton.setEnabled(selected);
                deleteSrcButton.setEnabled(selected);
            }
        });
        
        spellcheckSources.setEnabled( checkKeys );
        buttonPanel.setEnabled( checkKeys );
        
        boolean checkValues = Main.pref.getBoolean(PREF_CHECK_VALUES, true);
        prefCheckValues = new JCheckBox(tr("Check property values."), checkValues);
        prefCheckValues .setToolTipText(tr("Validate that property values are valid checking against presets."));
		testPanel.add(prefCheckValues, GBC.std().insets(40,0,0,0));

        prefCheckValuesBeforeUpload = new JCheckBox();
        prefCheckValuesBeforeUpload.setSelected(Main.pref.getBoolean(PREF_CHECK_VALUES_BEFORE_UPLOAD, true));
        testPanel.add(prefCheckValuesBeforeUpload, GBC.eop().insets(20,0,0,0));

        boolean checkFixmes = Main.pref.getBoolean(PREF_CHECK_FIXMES, true);
        prefCheckFixmes = new JCheckBox(tr("Check for FIXMES."), checkFixmes);
        prefCheckFixmes.setToolTipText(tr("Looks for nodes, segments or ways with FIXME in any property value."));
        testPanel.add(prefCheckFixmes, GBC.std().insets(40,0,0,0));

        prefCheckFixmesBeforeUpload = new JCheckBox();
        prefCheckFixmesBeforeUpload.setSelected(Main.pref.getBoolean(PREF_CHECK_FIXMES_BEFORE_UPLOAD, true));
        testPanel.add(prefCheckFixmesBeforeUpload, GBC.eop().insets(20,0,0,0));
	}

    public void setGuiEnabled(boolean enabled)
    {
        prefCheckKeys.setEnabled(enabled);
        prefCheckKeysBeforeUpload.setEnabled(enabled);
        spellcheckSources.setEnabled( enabled );
        addSrcButton.setEnabled(enabled);
        editSrcButton.setEnabled(enabled);
        deleteSrcButton.setEnabled(enabled);
        prefCheckValues.setEnabled(enabled);
        prefCheckValuesBeforeUpload.setEnabled(enabled);
        prefCheckFixmes.setEnabled(enabled);
        prefCheckFixmesBeforeUpload.setEnabled(enabled);
    } 
    
	@Override
	public void ok() 
	{
        Main.pref.put(PREF_CHECK_VALUES, prefCheckValues.isSelected());
        Main.pref.put(PREF_CHECK_KEYS, prefCheckKeys.isSelected());
        Main.pref.put(PREF_CHECK_FIXMES, prefCheckFixmes.isSelected());
        Main.pref.put(PREF_CHECK_VALUES_BEFORE_UPLOAD, prefCheckValuesBeforeUpload.isSelected());
        Main.pref.put(PREF_CHECK_KEYS_BEFORE_UPLOAD, prefCheckKeysBeforeUpload.isSelected());
        Main.pref.put(PREF_CHECK_FIXMES_BEFORE_UPLOAD, prefCheckFixmesBeforeUpload.isSelected());            
        Main.pref.put(PREF_CHECK_BEFORE_UPLOAD, prefCheckKeysBeforeUpload.isSelected() || prefCheckValuesBeforeUpload.isSelected() || prefCheckFixmesBeforeUpload.isSelected());
        String sources = "";
        if( spellcheckSources.getModel().getSize() > 0 )
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < spellcheckSources.getModel().getSize(); ++i)
                sb.append(";"+spellcheckSources.getModel().getElementAt(i));
            sources = sb.substring(1);
        }
        Main.pref.put(PREF_SOURCES, sources );

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
	
	
