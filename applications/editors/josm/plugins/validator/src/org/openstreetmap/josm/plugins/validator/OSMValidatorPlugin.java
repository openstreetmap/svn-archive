package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.validator.tests.*;
import org.openstreetmap.josm.plugins.validator.util.Util;

/**
 * 
 * A OSM data validator
 * 
 * @author Francisco R. Santos <frsantos@gmail.com>
 */
public class OSMValidatorPlugin extends Plugin 
{
    /** The validate action */
    ValidateAction validateAction = new ValidateAction();
    
    /** The validation dialog */
    ValidatorDialog validationDialog;
    
    /** The list of errors */
    List<TestError> errors = new ArrayList<TestError>(30);

    /** 
     * All available tests 
     * TODO: is there any way to find out automagically all available tests? 
     */
    public static Class[] allAvailableTests = new Class[]
    { 
    	DuplicateNode.class, 
    	DuplicateSegment.class, 
    	SingleNodeSegment.class, 
    	TaggedSegment.class, 
    	UnorderedWay.class, 
    	SpellCheck.class,
    	UntaggedNode.class, 
    	OrphanSegment.class, 
    };

	/**
	 * Creates the plugin, and starts the HTTP server
	 */
	public OSMValidatorPlugin()
	{
        initializeTests( getTests(true) );
	}
	
    @Override
	public PreferenceSetting getPreferenceSetting() 
	{
		return new PreferenceEditor();
	}
	
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) 
	{
		if (newFrame != null)
		{
            errors = new ArrayList<TestError>(50);
		    validationDialog = new ValidatorDialog();
	        newFrame.addToggleDialog(validationDialog);
            Main.main.addLayer(new ErrorLayer(tr("Validation errors")));
            try
            {
                ((UploadAction)Main.main.menu.upload).uploadHooks.add( new ValidateUploadHook() );
            }
            catch(Throwable t)
            {
                // JOSM has no upload hooks in older versions 
            }
		}
	}

	
	/**
	 * Utility method for classes that can't access the plugin directly
	 * 
	 * @return The plugin object
	 */
	public static OSMValidatorPlugin getPlugin() 
	{
		return (OSMValidatorPlugin)Util.getPlugin(OSMValidatorPlugin.class);
	}
	
	/**
	 * Gets a collection with the available tests
	 * 
	 * @param onlyActive if true, gets only active tests
	 * @return A collection with the available tests
	 */
	public static Collection<Test> getTests(boolean onlyActive)
	{
		Map<String, Test> enabledTests = new LinkedHashMap<String, Test>();
		for(Class<Test> testClass : getAllAvailableTests() )
		{
			Test test;
			try {
				test = testClass.newInstance();
			}
			catch( Exception e)
			{
				e.printStackTrace();
				continue;
			}
			test.enabled = true;
            
            String simpleName = testClass.getSimpleName();
            test.testBeforeUpload = Main.pref.getBoolean( "tests." + simpleName + ".checkBeforeUpload");            
			enabledTests.put(simpleName, test);
		}

		Pattern regexp = Pattern.compile("(\\w+)=(true|false),?");
		Matcher m = regexp.matcher(Main.pref.get("tests"));
		int pos = 0;
		while( m.find(pos) )
		{
			String testName = m.group(1);
			Test test = enabledTests.get(testName);
			if( test != null )
			{
				test.enabled = Boolean.valueOf(m.group(2)).booleanValue();
				if( onlyActive && !test.enabled)
					enabledTests.remove(test.getClass().getSimpleName() );
			}
			pos = m.end();
		}
		return enabledTests.values();
	}
	
    /**
     * Gets the list of all available test classes
     * 
     * @return An array of the test classes
     */
    public static Class[] getAllAvailableTests()
    {
        return allAvailableTests;
    }
    
	/**
	 * Initializes all tests
	 * @param allTests The tests to initialize
	 */
	public void initializeTests(Collection<Test> allTests)
	{
		for( Test test : allTests )
		{
			try
			{
				if( test.enabled )
				{
					test.getClass().getMethod("initialize", new Class[] { OSMValidatorPlugin.class} ).invoke(null, new Object[] {this});
				}
			} 
			catch(Exception e) 
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, tr("Error initializing test {0}.", test.getClass().getSimpleName()));
			}
		}
	}
}
