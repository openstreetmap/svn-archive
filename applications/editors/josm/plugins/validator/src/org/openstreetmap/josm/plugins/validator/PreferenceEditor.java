package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.validator.util.Util;
import org.openstreetmap.josm.plugins.validator.util.Util.Version;
import org.openstreetmap.josm.tools.GBC;

/**
 * Preference settings for the validator plugin
 * 
 * @author frsantos
 */
public class PreferenceEditor implements PreferenceSetting
{
	/** The preferences prefix */
	public static final String PREFIX = "validator";

	/** The preferences key for debug preferences */
	public static final String PREF_DEBUG = PREFIX + ".debug";

	/** The preferences key for enabled tests */
	public static final String PREF_TESTS = PREFIX + ".tests";

	/** The preferences key for enabled tests before upload*/
	public static final String PREF_TESTS_BEFORE_UPLOAD = PREFIX + ".testsBeforeUpload";

	/** The list of all tests */
	private Collection<Test> allTests;

    public void addGui(PreferenceDialog gui)
    {
		JPanel testPanel = new JPanel(new GridBagLayout());
		testPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
        testPanel.add( new JLabel(), GBC.std() );
        testPanel.add( new JLabel("On upload"), GBC.eop() );
        
		allTests = OSMValidatorPlugin.getAllTests();
		for(Test test: allTests) 
		{
            test.addGui(testPanel);
            test.setGuiEnabled(test.enabled || test.testBeforeUpload);
		}
		
		JScrollPane testPane = new JScrollPane(testPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		testPane.setBorder(null);

		Version ver = Util.getVersion();
		String description = tr("A OSM data validator that checks for common errors made by users and editor programs.");
		if( ver != null )
			description += "<br><br>" + tr("Version: {0}<br>Last change at {1}", ver.revision, ver.time);
    	JPanel tab = gui.createPreferenceTab("validator", tr("Data validator"), description);
		tab.add(testPane, GBC.eol().fill(GBC.BOTH));
		tab.add(GBC.glue(0,10), GBC.eol());
    }

	public void ok() 
	{
		StringBuilder tests = new StringBuilder();
		StringBuilder testsBeforeUpload = new StringBuilder();
		
		for (Test test : allTests)
		{
			test.ok();
			String name = test.getClass().getSimpleName();
			tests.append( ',' ).append( name ).append( '=' ).append( test.enabled );
			testsBeforeUpload.append( ',' ).append( name ).append( '=' ).append( test.testBeforeUpload );
		}
		
		if (tests.length() > 0 ) tests = tests.deleteCharAt(0);
		if (testsBeforeUpload.length() > 0 ) testsBeforeUpload = testsBeforeUpload.deleteCharAt(0);
		
		OSMValidatorPlugin.getPlugin().initializeTests( allTests );
		
		Main.pref.put( PREF_TESTS, tests.toString());
		Main.pref.put( PREF_TESTS_BEFORE_UPLOAD, testsBeforeUpload.toString());
	}
	
	/**
	 * Import old stored preferences
	 */
	public static void importOldPreferences()
	{
		if( !Main.pref.hasKey("tests") || !Pattern.matches("(\\w+=(true|false),?)*", Main.pref.get("tests")) )
			return;
		
		String enabledTests = Main.pref.get("tests");
		Main.pref.put(PREF_TESTS, enabledTests);
		Main.pref.put("tests", null );
		
		StringBuilder testsBeforeUpload = new StringBuilder();
		Map<String, String> oldPrefs = Main.pref.getAllPrefix("tests");
		for( Map.Entry<String, String> pref : oldPrefs.entrySet() )
		{
			String key = pref.getKey();
			String value = pref.getValue();
			if( key.endsWith(".checkBeforeUpload") )
			{
				String testName = key.substring(6, key.length() - 18);
				testsBeforeUpload.append( ',' ).append( testName ).append( '=' ).append( value );
			}
			else
				Main.pref.put( PREFIX + key.substring(5), value );
			Main.pref.put(key, null );
		}
		
		if (testsBeforeUpload.length() > 0 ) testsBeforeUpload = testsBeforeUpload.deleteCharAt(0);
		Main.pref.put( PREF_TESTS_BEFORE_UPLOAD, testsBeforeUpload.toString());
	}

}
