package org.openstreetmap.josm.plugins.graphview.plugin.preferences;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.AXLELOAD;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.HEIGHT;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.LENGTH;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.MAX_INCLINE_DOWN;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.MAX_INCLINE_UP;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.MAX_TRACKTYPE;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.SPEED;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.SURFACE_BLACKLIST;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.WEIGHT;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.WIDTH;

import java.awt.Color;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessType;
import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyType;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.ColorScheme;
import org.openstreetmap.josm.plugins.graphview.plugin.layer.PreferencesColorScheme;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.VehiclePropertyStringParser.PropertyValueSyntaxException;

/**
 * preferences of the GraphView plugin.
 * Observers will be notified when preferences change,
 * changes will also be synchronized (two-way) with JOSM's preference storage.
 * This is a singleton class.
 *
 * Note: Currently, manual updates in the "advanced preferences" will not have any effect
 * because this class isn't registered as a preference listener.
 */
public class GraphViewPreferences extends Observable {

    private static GraphViewPreferences instance;

    /**
     * returns the single instance of GraphViewPreferences.
     * @param ignoreSyntaxErrors
     * @return
     */
    public static GraphViewPreferences getInstance() {
        if (instance == null) {
            instance = new GraphViewPreferences();
        }
        return instance;
    }

    private boolean useInternalRulesets;
    private File rulesetFolder;
    private File currentRulesetFile;
    private InternalRuleset currentInternalRuleset;

    private String currentParameterBookmarkName;
    private Map<String, PreferenceAccessParameters> parameterBookmarks;

    private ColorScheme currentColorScheme;
    private Color nodeColor;
    private Color segmentColor;
    private Color arrowheadFillColor;

    private boolean separateDirections;

    private double arrowheadPlacement;

    public synchronized boolean getUseInternalRulesets() {
        return useInternalRulesets;
    }
    public synchronized void setUseInternalRulesets(boolean useInternalRulesets) {
        this.useInternalRulesets = useInternalRulesets;
    }

    public synchronized File getRulesetFolder() {
        return rulesetFolder;
    }
    public synchronized void setRulesetFolder(File rulesetFolder) {
        this.rulesetFolder = rulesetFolder;
    }

    public synchronized File getCurrentRulesetFile() {
        return currentRulesetFile;
    }
    public synchronized void setCurrentRulesetFile(File currentRulesetFile) {
        this.currentRulesetFile = currentRulesetFile;
    }

    public synchronized InternalRuleset getCurrentInternalRuleset() {
        return currentInternalRuleset;
    }
    public synchronized void setCurrentInternalRuleset(InternalRuleset internalRuleset) {
        this.currentInternalRuleset = internalRuleset;
    }

    /**
     * returns the name (map key) of the currently selected parameter bookmark
     * or null if none is selected.
     * If a name is returned, is has to be a key of the map returned by
     * {@link #getParameterBookmarks()}.
     */
    public synchronized String getCurrentParameterBookmarkName() {
        assert parameterBookmarks.containsKey(currentParameterBookmarkName);
        return currentParameterBookmarkName;
    }

    /**
     * returns the access parameters of the currently selected parameter bookmark
     * or null if none is selected.
     */
    public synchronized AccessParameters getCurrentParameterBookmark() {
        if (currentParameterBookmarkName == null) {
            return null;
        } else {
            assert parameterBookmarks.containsKey(currentParameterBookmarkName);
            return parameterBookmarks.get(currentParameterBookmarkName);
        }
    }

    /**
     * sets the active parameter bookmark using its name as an identifier
     * @param currentParameters  name of bookmark to set or null (no active bookmark).
     *                           Non-null values must be keys of the map returned by
     *                           {@link #getParameterBookmarks()}.
     */
    public synchronized void setCurrentParameterBookmarkName(String parameterBookmarkName) {
        assert parameterBookmarks.containsKey(parameterBookmarkName);
        this.currentParameterBookmarkName = parameterBookmarkName;
    }

    public synchronized Map<String, PreferenceAccessParameters> getParameterBookmarks() {
        return Collections.unmodifiableMap(parameterBookmarks);
    }
    public synchronized void setParameterBookmarks(
            Map<String, PreferenceAccessParameters> parameterBookmarks) {
        assert parameterBookmarks != null;

        this.parameterBookmarks =
            new HashMap<String, PreferenceAccessParameters>(parameterBookmarks);
    }

    public synchronized ColorScheme getCurrentColorScheme() {
        return currentColorScheme;
    }
    public synchronized void setCurrentColorScheme(ColorScheme currentColorScheme) {
        this.currentColorScheme = currentColorScheme;
    }

    public synchronized Color getNodeColor() {
        return nodeColor;
    }
    public synchronized void setNodeColor(Color nodeColor) {
        this.nodeColor = nodeColor;
    }

    public synchronized Color getSegmentColor() {
        return segmentColor;
    }
    public synchronized void setSegmentColor(Color segmentColor) {
        this.segmentColor = segmentColor;
    }

    public synchronized Color getArrowheadFillColor() {
		return arrowheadFillColor;
	}
	public synchronized void setArrowheadFillColor(Color arrowheadFillColor) {
		this.arrowheadFillColor = arrowheadFillColor;
	}

	public synchronized boolean getSeparateDirections() {
        return separateDirections;
    }
    public synchronized void setSeparateDirections(boolean separateDirections) {
        this.separateDirections = separateDirections;
    }

    public synchronized double getArrowheadPlacement() {
    	return arrowheadPlacement;
    }
    public synchronized void setArrowheadPlacement(double arrowheadPlacement) {
        this.arrowheadPlacement = arrowheadPlacement;
    }

    /**
     * writes changes to JOSM's preferences and notifies observers.
     * Must be called explicitly after setters (to prevent distributing incomplete changes).
     */
    public void distributeChanges() {
        writePreferences();
        setChanged();
        notifyObservers();
    }

    private GraphViewPreferences() {

        /* set defaults first (in case preferences are incomplete) */

        fillDefaults();

        /* read preferences and overwrite defaults */

        readPreferences();

        /* write preferences
         * (this will restore missing/defect preferences,
         *  but will simply rewrite valid preferences) */

        writePreferences();

    }

    private void fillDefaults() {

        parameterBookmarks = GraphViewPreferenceDefaults.createDefaultAccessParameterBookmarks();

        if (parameterBookmarks.size() > 0) {
            currentParameterBookmarkName = parameterBookmarks.keySet().iterator().next();
        } else {
            currentParameterBookmarkName = null;
        }

        useInternalRulesets = true;
        rulesetFolder = GraphViewPreferenceDefaults.getDefaultRulesetFolder();
        currentRulesetFile = null;
        currentInternalRuleset = null;

        currentColorScheme = new PreferencesColorScheme(this);

        nodeColor = Color.WHITE;
        segmentColor = Color.WHITE;
        arrowheadFillColor = Color.BLACK;

        separateDirections = false;

    }

    private void writePreferences() {

        Main.pref.put("graphview.parameterBookmarks",
                createAccessParameterBookmarksString(parameterBookmarks));

        if (currentParameterBookmarkName != null) {
            Main.pref.put("graphview.activeBookmark", currentParameterBookmarkName);
        }

        Main.pref.put("graphview.useInternalRulesets", useInternalRulesets);

        Main.pref.put("graphview.rulesetFolder", rulesetFolder.getPath());

        if (currentRulesetFile != null) {
            Main.pref.put("graphview.rulesetFile", currentRulesetFile.getPath());
        }
        if (currentInternalRuleset != null) {
            Main.pref.put("graphview.rulesetResource", currentInternalRuleset.toString());
        }

        Main.pref.put("graphview.defaultNodeColor", createColorString(nodeColor));
        Main.pref.put("graphview.defaultSegmentColor", createColorString(segmentColor));
        Main.pref.put("graphview.defaultArrowheadCoreColor", createColorString(arrowheadFillColor));

        Main.pref.put("graphview.separateDirections", separateDirections);

        Main.pref.putDouble("graphview.arrowheadPlacement", arrowheadPlacement);

    }

    private void readPreferences() {

        if (Main.pref.hasKey("graphview.parameterBookmarks")) {
            String bookmarksString = Main.pref.get("graphview.parameterBookmarks");
            parameterBookmarks = parseAccessParameterBookmarksString(bookmarksString);
        }

        if (Main.pref.hasKey("graphview.activeBookmark")) {
            currentParameterBookmarkName = Main.pref.get("graphview.activeBookmark");
        }
        if (!parameterBookmarks.containsKey(currentParameterBookmarkName)) {
            currentParameterBookmarkName = null;
        }


        useInternalRulesets = Main.pref.getBoolean("graphview.useInternalRulesets", true);

        if (Main.pref.hasKey("graphview.rulesetFolder")) {
            String dirString = Main.pref.get("graphview.rulesetFolder");
            rulesetFolder = new File(dirString);
        }
        if (Main.pref.hasKey("graphview.rulesetFile")) {
            String fileString = Main.pref.get("graphview.rulesetFile");
            currentRulesetFile = new File(fileString);
        }

        if (Main.pref.hasKey("graphview.rulesetResource")) {
            String rulesetString = Main.pref.get("graphview.rulesetResource");
            //get the enum value for the string
            //(InternalRuleset.valueOf cannot be used because it cannot handle invalid strings well)
            for (InternalRuleset ruleset : InternalRuleset.values()) {
                if (ruleset.toString().equals(rulesetString)) {
                    currentInternalRuleset = ruleset;
                    break;
                }
            }
        }

        if (Main.pref.hasKey("graphview.defaultNodeColor")) {
            Color color = parseColorString(Main.pref.get("graphview.defaultNodeColor"));
            if (color != null) {
                nodeColor = color;
            }
        }
        if (Main.pref.hasKey("graphview.defaultSegmentColor")) {
            Color color = parseColorString(Main.pref.get("graphview.defaultSegmentColor"));
            if (color != null) {
                segmentColor = color;
            }
        }
        if (Main.pref.hasKey("graphview.defaultArrowheadCoreColor")) {
            Color color = parseColorString(Main.pref.get("graphview.defaultArrowheadCoreColor"));
            if (color != null) {
            	arrowheadFillColor = color;
            }
        }

        separateDirections = Main.pref.getBoolean("graphview.separateDirections", false);

        arrowheadPlacement = Main.pref.getDouble("graphview.arrowheadPlacement", 1.0);
        if (arrowheadPlacement < 0.0 || arrowheadPlacement >= 1.0) {
        	arrowheadPlacement = 1.0;
        }

    }

    private static final Pattern ACCESS_PARAM_PATTERN = Pattern.compile("^([^;]*);([^;]*);types=\\{([^\\}]*)\\};properties=\\{([^\\}]*)\\}$");

    private static final Pattern PROPERTY_MAP_ENTRY_PATTERN = Pattern.compile("^([^=]*)=(.*)$");

    private static final Map<VehiclePropertyType<?>, String> VEHICLE_PROPERTY_TYPE_NAME_MAP =
        new HashMap<VehiclePropertyType<?>, String>();


    static {
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(AXLELOAD, "AXLELOAD");
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(HEIGHT, "HEIGHT");
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(LENGTH, "LENGTH");
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(MAX_INCLINE_DOWN, "MAX_INCLINE_DOWN");
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(MAX_INCLINE_UP, "MAX_INCLINE_UP");
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(MAX_TRACKTYPE, "MAX_TRACKTYPE");
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(SPEED, "SPEED");
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(SURFACE_BLACKLIST, "SURFACE_BLACKLIST");
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(WEIGHT, "WEIGHT");
        VEHICLE_PROPERTY_TYPE_NAME_MAP.put(WIDTH, "WIDTH");
    }

    private static String createAccessParameterBookmarksString(
            Map<String, PreferenceAccessParameters> parameterBookmarks) {

        StringBuilder stringBuilder = new StringBuilder();

        boolean firstEntry = true;

        for (String bookmarkName : parameterBookmarks.keySet()) {

            if (!firstEntry) {
                stringBuilder.append("|");
            } else {
                firstEntry = false;
            }

            stringBuilder.append(createAccessParameterBookmarkString(
                    bookmarkName,
                    parameterBookmarks.get(bookmarkName)));

        }

        return stringBuilder.toString();
    }

    private static String createAccessParameterBookmarkString(
            String bookmarkName, PreferenceAccessParameters parameters) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(bookmarkName).append(";");

        stringBuilder.append(parameters.getAccessClass());

        stringBuilder.append(";types={");
        for (AccessType accessType : AccessType.values()) {
            if (parameters.getAccessTypeUsable(accessType)) {
                stringBuilder.append(accessType).append(",");
            }
        }

        if(stringBuilder.charAt(stringBuilder.length()-1) == ',') {
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        stringBuilder.append("}");

        stringBuilder.append(";properties={");

        for (VehiclePropertyType<?> vehiclePropertyType : VEHICLE_PROPERTY_TYPE_NAME_MAP.keySet()) {
            String propertyString = parameters.getVehiclePropertyString(vehiclePropertyType);
            if (propertyString != null) {
                stringBuilder.append(VEHICLE_PROPERTY_TYPE_NAME_MAP.get(vehiclePropertyType));
                stringBuilder.append("=");
                stringBuilder.append(propertyString);
                stringBuilder.append(",");
            }
        }

        if(stringBuilder.charAt(stringBuilder.length()-1) == ',') {
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        stringBuilder.append("}");

        assert ACCESS_PARAM_PATTERN.matcher(stringBuilder.toString()).matches();

        return stringBuilder.toString();
    }

    private static Map<String, PreferenceAccessParameters> parseAccessParameterBookmarksString(
            String string) {

        Map<String, PreferenceAccessParameters> resultMap =
            new HashMap<String, PreferenceAccessParameters>();

        String[] bookmarkStrings = string.split("\\|");

        for (String bookmarkString : bookmarkStrings) {
            parseAccessParameterBookmarkString(bookmarkString, resultMap);
        }

        return resultMap;
    }

    private static void parseAccessParameterBookmarkString(String bookmarkString,
            Map<String, PreferenceAccessParameters> resultMap) {

        Matcher matcher = ACCESS_PARAM_PATTERN.matcher(bookmarkString);

        if (matcher.matches()) {

            String bookmarkName = matcher.group(1);

            String accessClass = matcher.group(2);

            String[] accessTypeStrings = matcher.group(3).split(",");
            Collection<AccessType> accessTypes = new LinkedList<AccessType>();
            for (String accessTypeString : accessTypeStrings) {
                AccessType accessType = AccessType.valueOf(accessTypeString);
                if (accessType != null) {
                    accessTypes.add(accessType);
                }
            }


            String[] vehiclePropertyStrings = matcher.group(4).split(",");
            Map<VehiclePropertyType<?>, String> vehiclePropertyMap =
                new HashMap<VehiclePropertyType<?>, String>();

            for (String vehiclePropertyString : vehiclePropertyStrings) {

                Matcher entryMatcher = PROPERTY_MAP_ENTRY_PATTERN.matcher(vehiclePropertyString);
                if (entryMatcher.matches()) {

                    String propertyTypeString = entryMatcher.group(1);
                    String propertyValueString = entryMatcher.group(2);

                    for (VehiclePropertyType<?> propertyType :
                        VEHICLE_PROPERTY_TYPE_NAME_MAP.keySet()) {

                        if (propertyTypeString.equals(
                                VEHICLE_PROPERTY_TYPE_NAME_MAP.get(propertyType))) {

                            vehiclePropertyMap.put(propertyType, propertyValueString);

                        }

                    }

                }

            }

            try {

                PreferenceAccessParameters accessParameters =
                    new PreferenceAccessParameters(accessClass, accessTypes, vehiclePropertyMap);

                resultMap.put(bookmarkName, accessParameters);

            } catch (PropertyValueSyntaxException e) {
                //don't add bookmark
            }

        }
    }

    private static final Pattern COLOR_PATTERN =
        Pattern.compile("^(\\d{1,3}),\\s*(\\d{1,3}),\\s*(\\d{1,3})$");

    private String createColorString(Color color) {
        return color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
    }

    private Color parseColorString(String string) {
        Matcher matcher = COLOR_PATTERN.matcher(string);
        if (!matcher.matches()) {
            return null;
        } else {
            int r = Integer.parseInt(matcher.group(1));
            int g = Integer.parseInt(matcher.group(2));
            int b = Integer.parseInt(matcher.group(3));
            return new Color(r, g, b);
        }
    }

}
