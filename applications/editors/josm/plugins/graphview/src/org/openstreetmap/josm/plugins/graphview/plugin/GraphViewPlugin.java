package org.openstreetmap.josm.plugins.graphview.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessRuleset;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessRulesetReader;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessRulesetReader.AccessRulesetSyntaxException;
import org.openstreetmap.josm.plugins.graphview.core.graph.TSBasedWayGraph;
import org.openstreetmap.josm.plugins.graphview.core.graph.WayGraph;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadIncline;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadMaxaxleload;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadMaxheight;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadMaxlength;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadMaxspeed;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadMaxweight;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadMaxwidth;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadMinspeed;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadPropertyType;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadSurface;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadTracktype;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadWidth;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.DefaultNodePositioner;
import org.openstreetmap.josm.plugins.graphview.plugin.data.JOSMTransitionStructure;
import org.openstreetmap.josm.plugins.graphview.plugin.dialogs.GraphViewDialog;
import org.openstreetmap.josm.plugins.graphview.plugin.dialogs.GraphViewPreferenceEditor;
import org.openstreetmap.josm.plugins.graphview.plugin.layer.GraphViewLayer;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.GraphViewPreferences;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.InternalRuleset;

/**
 * A routing graph visualization tool for JOSM
 */
public class GraphViewPlugin extends Plugin implements LayerChangeListener, Observer {

	private static final Collection<RoadPropertyType<?>> PROPERTIES;

	static {
		PROPERTIES = new LinkedList<RoadPropertyType<?>>();
		PROPERTIES.add(new RoadIncline());
		PROPERTIES.add(new RoadMaxaxleload());
		PROPERTIES.add(new RoadMaxheight());
		PROPERTIES.add(new RoadMaxlength());
		PROPERTIES.add(new RoadMaxspeed());
		PROPERTIES.add(new RoadMaxweight());
		PROPERTIES.add(new RoadMaxwidth());
		PROPERTIES.add(new RoadMinspeed());
		PROPERTIES.add(new RoadSurface());
		PROPERTIES.add(new RoadTracktype());
		PROPERTIES.add(new RoadWidth());
	}

	private final GraphViewPreferences preferences;

	private JOSMTransitionStructure transitionStructure;
	private GraphViewLayer graphViewLayer;

	/** creates the plugin */
	public GraphViewPlugin() {

		preferences = GraphViewPreferences.getInstance();
		this.preferences.addObserver(this);

	}

	/** allows creation/update of GraphViewLayer */
	public void createGraphViewLayer() {

		try {

			if (graphViewLayer != null) {

				AccessRuleset accessRuleset = getAccessRuleset();

				if (accessRuleset == null) {
					JOptionPane.showMessageDialog(Main.parent, "No ruleset has been selected!", "No ruleset", JOptionPane.ERROR_MESSAGE);
				} else {
					transitionStructure.setAccessParametersAndRuleset(preferences.getCurrentParameterBookmark(), accessRuleset);
					transitionStructure.forceUpdate();
				}

			} else {

				AccessRuleset accessRuleset = getAccessRuleset();

				if (accessRuleset == null) {
					JOptionPane.showMessageDialog(Main.parent, "No ruleset has been selected!",
							"No ruleset", JOptionPane.ERROR_MESSAGE);
				} else {

					transitionStructure = new JOSMTransitionStructure(
							preferences.getCurrentParameterBookmark(),
							accessRuleset,
							PROPERTIES);

					WayGraph graph = new TSBasedWayGraph(transitionStructure);

					graphViewLayer = new GraphViewLayer();
					graphViewLayer.setWayGraph(graph);
					graphViewLayer.setColorScheme(preferences.getCurrentColorScheme());
					graphViewLayer.setNodePositioner(new DefaultNodePositioner());

					Main.main.addLayer(graphViewLayer);

				}

			}

		} catch (AccessRulesetSyntaxException e) {
			JOptionPane.showMessageDialog(Main.parent, "syntax exception in access ruleset:\n" + e);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(Main.parent, "file not found:\n" + e);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Main.parent, "problem when accessing a file:\n" + e);
			e.printStackTrace();
		}

	}

	/** allows update of GraphViewLayer */
	public void updateGraphViewLayer() {

		try {

			if (graphViewLayer != null) {

				AccessRuleset accessRuleset = getAccessRuleset();

				if (accessRuleset == null) {
					JOptionPane.showMessageDialog(Main.parent, "No ruleset has been selected!",
							"No ruleset", JOptionPane.ERROR_MESSAGE);
				} else {
					transitionStructure.setAccessParametersAndRuleset(
							preferences.getCurrentParameterBookmark(), accessRuleset);
					transitionStructure.forceUpdate();
				}

			}

		} catch (AccessRulesetSyntaxException e) {
			JOptionPane.showMessageDialog(Main.parent, "syntax exception in access ruleset:\n" + e);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(Main.parent, "file not found:\n" + e);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Main.parent, "problem when accessing a file:\n" + e);
			e.printStackTrace();
		}

	}

	/** repaints the GraphViewLayer without recalculating the graph (visual update) */
	public void repaintGraphViewLayer() {

		if (graphViewLayer != null) {
			Main.panel.repaint();
		}

	}

	/**
	 * @return ruleset read from a source as specified by preferences, null if the preferences
	 *         don't specify a ruleset source
	 * @throws AccessRulesetSyntaxException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private AccessRuleset getAccessRuleset()
	throws AccessRulesetSyntaxException, IOException, FileNotFoundException {

		InputStream rulesetInputStream;

		if (preferences.getUseInternalRulesets()) {

			InternalRuleset ruleset = preferences.getCurrentInternalRuleset();

			if (ruleset == null) {
				return null;
			}

			ClassLoader classLoader = this.getClass().getClassLoader();
			URL rulesetURL = classLoader.getResource(ruleset.getResourceName());

			if (rulesetURL != null) {
				rulesetInputStream = rulesetURL.openStream();
			} else {
				throw new FileNotFoundException("couldn't find built-in ruleset " + ruleset);
			}

		} else {

			File rulesetFile = preferences.getCurrentRulesetFile();

			if (rulesetFile == null) {
				return null;
			}

			rulesetInputStream = new FileInputStream(rulesetFile);

		}

		return AccessRulesetReader.readAccessRuleset(rulesetInputStream);

	}

	@Override
	public PreferenceSetting getPreferenceSetting() {
		return new GraphViewPreferenceEditor();
	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (newFrame != null) {
			if (oldFrame == null) {
				final GraphViewDialog laneDialog
					= new GraphViewDialog(this);
				newFrame.addToggleDialog(laneDialog);
			}
			Layer.listeners.add(this);
		} else {
			Layer.listeners.remove(this);
		}
	}

	public void layerRemoved(Layer oldLayer) {
		if (oldLayer == graphViewLayer) {
			graphViewLayer = null;
		} else if (oldLayer == Main.map.mapView.getEditLayer()) { //data layer removed
			if (graphViewLayer != null) {
				Main.map.mapView.removeLayer(graphViewLayer);
				graphViewLayer = null;
			}
		}
	}

	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		//do nothing
	}

	public void layerAdded(Layer newLayer) {
		//do nothing
	}

	public void update(Observable arg0, Object arg1) {
		if (arg0 == preferences) {
			if (graphViewLayer != null) {
				graphViewLayer.setColorScheme(preferences.getCurrentColorScheme());
			}
		}
	}

}
