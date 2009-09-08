package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.actions.UploadAction.UploadHook;
import org.openstreetmap.josm.data.projection.Epsg4326;
import org.openstreetmap.josm.data.projection.Lambert;
import org.openstreetmap.josm.data.projection.Mercator;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.validator.tests.Coastlines;
import org.openstreetmap.josm.plugins.validator.tests.CrossingWays;
import org.openstreetmap.josm.plugins.validator.tests.DuplicateNode;
import org.openstreetmap.josm.plugins.validator.tests.DuplicateWay;
import org.openstreetmap.josm.plugins.validator.tests.DuplicatedWayNodes;
import org.openstreetmap.josm.plugins.validator.tests.NodesWithSameName;
import org.openstreetmap.josm.plugins.validator.tests.OverlappingWays;
import org.openstreetmap.josm.plugins.validator.tests.SelfIntersectingWay;
import org.openstreetmap.josm.plugins.validator.tests.SimilarNamedWays;
import org.openstreetmap.josm.plugins.validator.tests.TagChecker;
import org.openstreetmap.josm.plugins.validator.tests.UnclosedWays;
import org.openstreetmap.josm.plugins.validator.tests.UnconnectedWays;
import org.openstreetmap.josm.plugins.validator.tests.UntaggedNode;
import org.openstreetmap.josm.plugins.validator.tests.UntaggedWay;
import org.openstreetmap.josm.plugins.validator.tests.WronglyOrderedWays;
import org.openstreetmap.josm.plugins.validator.util.Util;

/**
 *
 * A OSM data validator
 *
 * @author Francisco R. Santos <frsantos@gmail.com>
 */
public class OSMValidatorPlugin extends Plugin implements LayerChangeListener {

    protected static OSMValidatorPlugin plugin;

    protected static ErrorLayer errorLayer = null;

    /** The validate action */
    ValidateAction validateAction = new ValidateAction(this);

    /** The validation dialog */
    ValidatorDialog validationDialog;

    /** The list of errors per layer*/
    Map<Layer, List<TestError>> layerErrors = new HashMap<Layer, List<TestError>>();

    /** Grid detail, multiplier of east,north values for valuable cell sizing */
    public static double griddetail;

    public Collection<String> ignoredErrors = new TreeSet<String>();

    /**
     * All available tests
     * TODO: is there any way to find out automagically all available tests?
     */
    @SuppressWarnings("unchecked")
    public static Class<Test>[] allAvailableTests = new Class[] { DuplicateNode.class, // ID    1 ..   99
            OverlappingWays.class, // ID  101 ..  199
            UntaggedNode.class, // ID  201 ..  299
            UntaggedWay.class, // ID  301 ..  399
            SelfIntersectingWay.class, // ID  401 ..  499
            DuplicatedWayNodes.class, // ID  501 ..  599
            CrossingWays.class, // ID  601 ..  699
            SimilarNamedWays.class, // ID  701 ..  799
            NodesWithSameName.class, // ID  801 ..  899
            Coastlines.class, // ID  901 ..  999
            WronglyOrderedWays.class, // ID 1001 .. 1099
            UnclosedWays.class, // ID 1101 .. 1199
            TagChecker.class, // ID 1201 .. 1299
            UnconnectedWays.class, // ID 1301 .. 1399
            DuplicateWay.class, // ID 1401 .. 1499
    };

    /**
     * Creates the plugin
     */
    public OSMValidatorPlugin() {
        plugin = this;
        checkPluginDir();
        initializeGridDetail();
        initializeTests(getTests());
        loadIgnoredErrors();
    }

    /**
     * Check if plugin directory exists (store ignored errors file)
     */
    private void checkPluginDir() {
        try {
        File pathDir = new File(Util.getPluginDir());
        if (!pathDir.exists())
            pathDir.mkdirs();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadIgnoredErrors() {
        ignoredErrors.clear();
        if (Main.pref.getBoolean(PreferenceEditor.PREF_USE_IGNORE, true)) {
            try {
                final BufferedReader in = new BufferedReader(new FileReader(Util.getPluginDir() + "ignorederrors"));
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    ignoredErrors.add(line);
                }
            } catch (final FileNotFoundException e) {
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveIgnoredErrors() {
        try {
            final PrintWriter out = new PrintWriter(new FileWriter(Util.getPluginDir() + "ignorederrors"), false);
            for (String e : ignoredErrors)
                out.println(e);
            out.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new PreferenceEditor(this);
    }

    private ValidateUploadHook uploadHook;
    
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            validationDialog = new ValidatorDialog(this);
            newFrame.addToggleDialog(validationDialog);
            initializeErrorLayer();
            if (Main.pref.hasKey(PreferenceEditor.PREF_DEBUG + ".grid"))
                Main.main.addLayer(new GridLayer(tr("Grid")));
            Layer.listeners.add(this);
        } else
            Layer.listeners.remove(this);

        if (newFrame != null) {
        	UploadAction.registerUploadHook(uploadHook = new ValidateUploadHook(this));
        } else {
        	UploadAction.unregisterUploadHook(uploadHook);
        	uploadHook = null;
        }
    }

    public void initializeErrorLayer() {
        if (!Main.pref.getBoolean(PreferenceEditor.PREF_LAYER, true))
            return;
        if (errorLayer == null) {
            errorLayer = new ErrorLayer(this);
            Main.main.addLayer(errorLayer);
        }
    }

    /** Gets a map from simple names to all tests. */
    public static Map<String, Test> getAllTestsMap() {
        Map<String, Test> tests = new HashMap<String, Test>();
        for (Class<Test> testClass : getAllAvailableTests()) {
            try {
                Test test = testClass.newInstance();
                tests.put(testClass.getSimpleName(), test);
            } catch (Exception e) {
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
        Matcher m = regexp.matcher(Main.pref.get(beforeUpload ? PreferenceEditor.PREF_TESTS_BEFORE_UPLOAD
                : PreferenceEditor.PREF_TESTS));
        int pos = 0;
        while (m.find(pos)) {
            String testName = m.group(1);
            Test test = tests.get(testName);
            if (test != null) {
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
            if (beforeUpload ? t.testBeforeUpload : t.enabled)
                continue;
            enabledTests.remove(t);
        }
        return enabledTests;
    }

    /**
     * Gets the list of all available test classes
     *
     * @return An array of the test classes
     */
    public static Class<Test>[] getAllAvailableTests() {
        return allAvailableTests;
    }

    /**
     * Initialize grid details based on current projection system. Values based on
     * the original value fixed for EPSG:4326 (10000) using heuristics (that is, test&error
     * until most bugs were discovered while keeping the processing time reasonable)
     */
    public void initializeGridDetail() {
        if (Main.proj.toString().equals(new Epsg4326().toString()))
            OSMValidatorPlugin.griddetail = 10000;
        else if (Main.proj.toString().equals(new Mercator().toString()))
            OSMValidatorPlugin.griddetail = 100000;
        else if (Main.proj.toString().equals(new Lambert().toString()))
            OSMValidatorPlugin.griddetail = 0.1;
    }

    /**
     * Initializes all tests
     * @param allTests The tests to initialize
     */
    public void initializeTests(Collection<Test> allTests) {
        for (Test test : allTests) {
            try {
                if (test.enabled) {
                    test.getClass().getMethod("initialize", new Class[] { OSMValidatorPlugin.class }).invoke(null,
                            new Object[] { this });
                }
            } catch (InvocationTargetException ite) {
                ite.getCause().printStackTrace();
                JOptionPane.showMessageDialog(Main.parent, 
                		tr("Error initializing test {0}:\n {1}", test.getClass()
                        .getSimpleName(), ite.getCause().getMessage()),
                        tr("Error"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(Main.parent, 
                		tr("Error initializing test {0}:\n {1}", test.getClass()
                        .getSimpleName(), e),
                        tr("Error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        if (newLayer instanceof OsmDataLayer) {
            List<TestError> errors = layerErrors.get(newLayer);
            validationDialog.tree.setErrorList(errors);
            Main.map.repaint();
        }
    }

    public void layerAdded(Layer newLayer) {
        if (newLayer instanceof OsmDataLayer) {
            layerErrors.put(newLayer, new ArrayList<TestError>());
        }
    }

    public void layerRemoved(Layer oldLayer) {
        layerErrors.remove(oldLayer);
    }
}
