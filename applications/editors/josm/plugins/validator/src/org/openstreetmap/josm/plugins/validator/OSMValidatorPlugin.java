package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.actions.UploadAction.UploadHook;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener;
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
public class OSMValidatorPlugin extends Plugin implements LayerChangeListener
{
    /** The validate action */
    ValidateAction validateAction = new ValidateAction(this);
    
    /** The validation dialog */
    ValidatorDialog validationDialog;
    
    /** The list of errors per layer*/
    Map<Layer, List<TestError>> layerErrors = new HashMap<Layer, List<TestError>>();

	/**
	 * All available tests
	 * TODO: is there any way to find out automagically all available tests?
	 */
	public static Class[] allAvailableTests = new Class[]
	{
		DuplicateNode.class,
		OverlappingWays.class,
		UntaggedNode.class,
		UntaggedWay.class,
		SelfIntersectingWay.class,
		SpellCheck.class,
		DuplicatedWayNodes.class,
		CrossingWays.class,
		SimilarNamedWays.class,
		NodesWithSameName.class,
		Coastlines.class,
		WronglyOrderedWays.class,
		UnclosedWays.class,
		TagChecker.class,
	};

	/**
	 * Creates the plugin, and starts the HTTP server
	 */
	public OSMValidatorPlugin()
	{
		PreferenceEditor.importOldPreferences();
		initializeTests( getTests() );
	}

	@Override
	public PreferenceSetting getPreferenceSetting() 
	{
		return new PreferenceEditor(this);
	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) 
	{
		if (newFrame != null)
		{
		    validationDialog = new ValidatorDialog(this);
	        newFrame.addToggleDialog(validationDialog);
            Main.main.addLayer(new ErrorLayer(this));
        	if( Main.pref.hasKey(PreferenceEditor.PREF_DEBUG + ".grid") )
        		Main.main.addLayer(new GridLayer(tr("Grid")));
            Layer.listeners.add(this); 
		}
		else
            Layer.listeners.remove(this); 
        
		LinkedList<UploadHook> hooks = ((UploadAction)Main.main.menu.upload).uploadHooks;
		Iterator<UploadHook> hooksIt = hooks.iterator(); 
		while( hooksIt.hasNext() )
		{
			if( hooksIt.next() instanceof ValidateUploadHook )
			{
				if( newFrame == null )
					hooksIt.remove();
				break;
			}
		}
		if( newFrame != null )
			hooks.add( 0, new ValidateUploadHook() );
	}

	/** Gets a map from simple names to all tests. */
	public static Map<String, Test> getAllTestsMap() {
		Map<String, Test> tests = new HashMap<String, Test>();
		for(Class<Test> testClass : getAllAvailableTests() )
		{
			try {
				Test test = testClass.newInstance();
				tests.put(testClass.getSimpleName(), test);
			}
			catch( Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}
		applyPrefs(tests, false);
		applyPrefs(tests, true);
		return tests;
	}

	private static void applyPrefs(Map<String, Test> tests, boolean beforeUpload) {
		Pattern regexp = Pattern.compile("(\\w+)=(true|false),?");
		Matcher m = regexp.matcher(Main.pref.get(beforeUpload
			? PreferenceEditor.PREF_TESTS_BEFORE_UPLOAD
			: PreferenceEditor.PREF_TESTS));
		int pos = 0;
		while( m.find(pos) )
		{
			String testName = m.group(1);
			Test test = tests.get(testName);
			if( test != null )
			{
				boolean enabled = Boolean.valueOf(m.group(2));
				if (beforeUpload) {
					test.testBeforeUpload = enabled;
				} else {
					test.enabled = enabled;
				}
			}
			pos = m.end();
		}
	}

	public static Collection<Test> getTests() {
		return getAllTestsMap().values();
	}

	public static Collection<Test> getEnabledTests(boolean beforeUpload) {
		Collection<Test> enabledTests = getTests();
		for (Test t : new ArrayList<Test>(enabledTests)) {
			if (beforeUpload ? t.testBeforeUpload : t.enabled) continue;
			enabledTests.remove(t);
		}
		return enabledTests;
	}

    /**
     * Gets the list of all available test classes
     * 
     * @return An array of the test classes	        validationDialog.tree.setErrorList(errors);
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
            catch(InvocationTargetException ite) 
            {
                ite.getCause().printStackTrace();
                JOptionPane.showMessageDialog(null, tr("Error initializing test {0}:\n {1}", test.getClass().getSimpleName(), ite.getCause().getMessage()));
            }
            catch(Exception e) 
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, tr("Error initializing test {0}:\n {1}", test.getClass().getSimpleName(), e));
            }
		}
	}
	
	public void activeLayerChange(Layer oldLayer, Layer newLayer) 
	{
		if( newLayer instanceof OsmDataLayer )
		{
	        List<TestError> errors = layerErrors.get(newLayer);
	        validationDialog.tree.setErrorList(errors);
			Main.map.repaint();	        
		}
	}

	public void layerAdded(Layer newLayer) 
	{
		if( newLayer instanceof OsmDataLayer )
		{
			layerErrors.put(newLayer, new ArrayList<TestError>() );
		}
	}

	public void layerRemoved(Layer oldLayer) 
	{
		layerErrors.remove(oldLayer);
	}
}
