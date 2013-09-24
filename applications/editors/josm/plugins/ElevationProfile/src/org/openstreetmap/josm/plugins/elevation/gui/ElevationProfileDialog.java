/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.josm.plugins.elevation.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.elevation.ElevationHelper;
import org.openstreetmap.josm.plugins.elevation.IElevationModel;
import org.openstreetmap.josm.plugins.elevation.IElevationModelListener;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.plugins.elevation.gpx.ElevationModel;
import org.openstreetmap.josm.plugins.elevation.gpx.GeoidCorrectionKind;
import org.openstreetmap.josm.tools.Shortcut;
/**
 * @author Oliver Wieland <oliver.wieland@online.de>
 * Implements a JOSM ToggleDialog to show the elevation profile. It monitors the 
 * connection between layer and elevation profile. 
 */
public class ElevationProfileDialog extends ToggleDialog implements LayerChangeListener, ComponentListener {

	private static final String EMPTY_DATA_STRING = "-";
	/**
	 * 
	 */
	private static final long serialVersionUID = -868463893732535577L;
	/* Elevation profile instance */
	private IElevationModel model;
	/* GPX data */
	private GpxLayer activeLayer = null;
	private HashMap<GpxLayer, ElevationModel> layerMap = new HashMap<GpxLayer, ElevationModel>();
	
	/* UI elements */
	private ElevationProfilePanel profPanel;
	private JLabel minHeightLabel;
	private JLabel maxHeightLabel;
	private JLabel avrgHeightLabel;
	private JLabel elevationGainLabel;
	private JLabel totalTimeLabel;
	private JLabel distLabel;
	/* Listener to the elevation model */
	private List<IElevationModelListener> listeners = new ArrayList<IElevationModelListener>();
	
	/**
	 * Corresponding layer instance within map view.
	 */
	private ElevationProfileLayer profileLayer;	

	/**
	 * Default constructor
	 */
	public ElevationProfileDialog() {
		this(tr("Elevation Profile"), "elevation",
				tr("Open the elevation profile window."), null, 200, true);
	}

	/**
	 * Constructor (see below)
	 */
	public ElevationProfileDialog(String name, String iconName, String tooltip,
			Shortcut shortcut, int preferredHeight) {
		this(name, iconName, tooltip, shortcut, preferredHeight, false);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            the name of the dialog
	 * @param iconName
	 *            the name of the icon to be displayed
	 * @param tooltip
	 *            the tool tip
	 * @param shortcut
	 *            the shortcut
	 * @param preferredHeight
	 *            the preferred height for the dialog
	 * @param defShow
	 *            if the dialog should be shown by default, if there is no
	 *            preference
	 */
	public ElevationProfileDialog(String name, String iconName, String tooltip,
			Shortcut shortcut, int preferredHeight, boolean defShow) {
		super(name, iconName, tooltip, shortcut, preferredHeight, defShow);
				
		JPanel dataPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(2, 6);
		dataPanel.setLayout(gridLayout);

		// first row: Headlines with bold font
		JLabel lbl = new JLabel(tr("Min"));
		lbl.setFont(getFont().deriveFont(Font.BOLD));
		dataPanel.add(lbl);
		lbl = new JLabel(tr("Avrg"));
		lbl.setFont(getFont().deriveFont(Font.BOLD));
		dataPanel.add(lbl);
		lbl = new JLabel(tr("Max"));
		lbl.setFont(getFont().deriveFont(Font.BOLD));
		dataPanel.add(lbl);
		lbl = new JLabel(tr("Dist"));
		lbl.setFont(getFont().deriveFont(Font.BOLD));
		dataPanel.add(lbl);
		lbl = new JLabel(tr("Gain"));
		lbl.setFont(getFont().deriveFont(Font.BOLD));
		dataPanel.add(lbl);
		lbl = new JLabel(tr("Time"));
		lbl.setFont(getFont().deriveFont(Font.BOLD));
		dataPanel.add(lbl);

		// second row
		minHeightLabel = new JLabel("0 m");
		dataPanel.add(minHeightLabel);
		avrgHeightLabel = new JLabel("0 m");
		dataPanel.add(avrgHeightLabel);
		maxHeightLabel = new JLabel("0 m");
		dataPanel.add(maxHeightLabel);
		distLabel = new JLabel("0 km");
		dataPanel.add(distLabel);
		elevationGainLabel = new JLabel("0 m");
		dataPanel.add(elevationGainLabel);
		totalTimeLabel = new JLabel("0");
		dataPanel.add(totalTimeLabel);

		add(dataPanel, BorderLayout.PAGE_END);
		model = new ElevationModel();

		profPanel = new ElevationProfilePanel(null);
		add(profPanel, BorderLayout.CENTER);
		profPanel.addComponentListener(this);
		
		dock();
	}
	
	@Override
	public void showNotify() {
		MapView.addLayerChangeListener(this);
		if (Main.isDisplayingMapView()) {
			Layer layer = Main.map.mapView.getActiveLayer();
			if (layer instanceof GpxLayer) {
				setActiveLayer((GpxLayer) layer);
			}
		}
	}
	
	@Override
	public void hideNotify() {
		MapView.removeLayerChangeListener(this);
	}

	/**
	 * Gets the elevation model instance.
	 * @return
	 */
	public IElevationModel getModel() {
		return model;
	}

	/**
	 * Sets the elevation model instance.
	 * @param model The new model.
	 */
	public void setModel(IElevationModel model) {
		if (this.model != model) {
		    	this.model = model;
			profPanel.setElevationModel(model);
			updateView();
		}
	}

	/**
	 * Gets the associated layer instance of the elevation profile.
	 * @return
	 */
	public ElevationProfileLayer getProfileLayer() {
		return profileLayer;
	}

	/**
	 * Sets the associated layer instance of the elevation profile.
	 * @param profileLayer The elevation profile layer.
	 */
	public void setProfileLayer(ElevationProfileLayer profileLayer) {
		if (this.profileLayer != profileLayer) {
			if (this.profileLayer != null) {
				profPanel.removeSelectionListener(this.profileLayer);
			}
			this.profileLayer = profileLayer;
			profPanel.addSelectionListener(this.profileLayer);
		}
	}

	/**
	 * Refreshes the dialog when model data have changed and notifies clients
	 * that the model has changed.
	 */
	private void updateView() {
	    	if (model == null) return;

		IElevationProfile profile = model.getCurrentProfile();

		if (profile != null) {
		    // Show name of profile in title 
		    setTitle(String.format("%s: %s", tr("Elevation Profile"), profile.getName()));

		    if (profile.hasElevationData()) {
			// Show elevation data
			minHeightLabel.setText(
				ElevationHelper.getElevationText(profile.getMinHeight()));
			maxHeightLabel.setText(
				ElevationHelper.getElevationText(profile.getMaxHeight()));
			avrgHeightLabel.setText(
				ElevationHelper.getElevationText(profile.getAverageHeight()));
			elevationGainLabel.setText(
				ElevationHelper.getElevationText(profile.getGain()));
		    }

		    // compute values for time and distance
		    long diff = profile.getTimeDifference();
		    long minutes = diff / (1000 * 60);
		    long hours = minutes / 60;
		    minutes = minutes % 60;

		    double dist = profile.getDistance();

		    totalTimeLabel.setText(String.format("%d:%d h", hours, minutes));
		    distLabel.setText(NavigatableComponent.getSystemOfMeasurement().getDistText(dist));
		} else { // no elevation data, -> switch back to empty view
		    setTitle(String.format("%s: (No data)", tr("Elevation Profile")));

		    minHeightLabel.setText(EMPTY_DATA_STRING);
		    maxHeightLabel.setText(EMPTY_DATA_STRING);
		    avrgHeightLabel.setText(EMPTY_DATA_STRING);
		    elevationGainLabel.setText(EMPTY_DATA_STRING);
		    totalTimeLabel.setText(EMPTY_DATA_STRING);
		    distLabel.setText(EMPTY_DATA_STRING);
		}

		fireModelChanged();
		repaint();	    
	}

	/**
	 * Fires the 'model changed' event to all listeners.
	 */
	protected void fireModelChanged() {
		for (IElevationModelListener listener : listeners) {
			listener.elevationProfileChanged(getModel().getCurrentProfile());
		}
	}

	/**
	 * Adds a model listener to this instance.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addModelListener(IElevationModelListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Removes a model listener from this instance.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeModelListener(IElevationModelListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Removes all listeners from this instance.
	 */
	public void removeAllListeners() {
		this.listeners.clear();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.MapView.LayerChangeListener#activeLayerChange(org.openstreetmap.josm.gui.layer.Layer, org.openstreetmap.josm.gui.layer.Layer)
	 */
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if (newLayer instanceof GpxLayer) {
			setActiveLayer((GpxLayer) newLayer);
		}
	}

	private void setActiveLayer(GpxLayer newLayer) {
		if (activeLayer != newLayer) {
			activeLayer = newLayer;

			// layer does not exist -> create
			if (!layerMap.containsKey(newLayer)) {
				GpxData gpxData = newLayer.data;
				ElevationModel newEM = new ElevationModel(newLayer.getName(),
						gpxData);
				layerMap.put(newLayer, newEM);
			}
			
			ElevationModel em = layerMap.get(newLayer);
			setModel(em);			
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.MapView.LayerChangeListener#layerAdded(org.openstreetmap.josm.gui.layer.Layer)
	 */
	public void layerAdded(Layer newLayer) {
		if (newLayer instanceof GpxLayer) {
			GpxLayer gpxLayer = (GpxLayer) newLayer;
			setActiveLayer(gpxLayer);
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.MapView.LayerChangeListener#layerRemoved(org.openstreetmap.josm.gui.layer.Layer)
	 */
	public void layerRemoved(Layer oldLayer) {
		if (layerMap.containsKey(oldLayer)) {
			layerMap.remove(oldLayer);
		}
		
		if (layerMap.size() == 0) {
			setModel(null);
			if (profileLayer != null) {
				profileLayer.setProfile(null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentHidden(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent
	 * )
	 */
	public void componentMoved(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentResized(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent
	 * )
	 */
	public void componentShown(ComponentEvent e) {
	}
}
