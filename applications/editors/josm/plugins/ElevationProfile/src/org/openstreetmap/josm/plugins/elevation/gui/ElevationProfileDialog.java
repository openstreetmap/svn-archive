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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.elevation.ElevationModel;
import org.openstreetmap.josm.plugins.elevation.GeoidCorrectionKind;
import org.openstreetmap.josm.plugins.elevation.IElevationModelListener;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.plugins.elevation.WayPointHelper;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author Oliver
 * 
 */
public class ElevationProfileDialog extends ToggleDialog implements
		PropertyChangeListener, LayerChangeListener, EditLayerChangeListener,
		ComponentListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -868463893732535577L;
	private JLabel minHeightLabel;
	private JLabel maxHeightLabel;

	private IElevationProfile profile;
	private ElevationProfilePanel profPanel;
	private GpxLayer activeLayer = null;
	private HashMap<GpxLayer, ElevationModel> layerMap = new HashMap<GpxLayer, ElevationModel>();
	private List<IElevationModelListener> listeners = new ArrayList<IElevationModelListener>();
	private JLabel avrgHeightLabel;
	private JLabel elevationGainLabel;
	private JLabel totalTimeLabel;

	private JRadioButton geoidNone;
	private JRadioButton geoidAuto;
	private JRadioButton geoidFixed;
	private JTextField geoidFixedValue;
	
	private ElevationProfileLayer profileLayer;
	private JLabel distLabel;

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
				
		JPanel dataPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;

		// first row
		c.gridx = 0;
		c.gridy = 0;
		dataPanel.add(new JLabel(tr("")), c); // filler
		c.gridx = 1;
		c.gridy = 0;
		dataPanel.add(new JLabel(tr("Min")), c);
		c.gridx = 2;
		c.gridy = 0;
		dataPanel.add(new JLabel(tr("Avrg")), c);
		c.gridx = 3;
		c.gridy = 0;
		dataPanel.add(new JLabel(tr("Max")), c);
		c.gridx = 4;
		c.gridy = 0;
		dataPanel.add(new JLabel(tr("Dist")), c);
		c.gridx = 5;
		c.gridy = 0;
		dataPanel.add(new JLabel(tr("Gain")), c);
		c.gridx = 6;
		c.gridy = 0;
		dataPanel.add(new JLabel(tr("Time")), c);

		// second row
		c.gridx = 0;
		c.gridy = 1;
		dataPanel.add(new JLabel(tr("Ele")), c);
		c.gridx = 1;
		c.gridy = 1;
		minHeightLabel = new JLabel("0 m");
		dataPanel.add(minHeightLabel, c);

		c.gridx = 2;
		c.gridy = 1;
		avrgHeightLabel = new JLabel("0 m");
		dataPanel.add(avrgHeightLabel, c);

		c.gridx = 3;
		c.gridy = 1;
		maxHeightLabel = new JLabel("0 m");
		dataPanel.add(maxHeightLabel, c);
		
		c.gridx = 4;
		c.gridy = 1;
		distLabel = new JLabel("0 km");
		dataPanel.add(distLabel, c);
		
		c.gridx = 5;
		c.gridy = 1;
		elevationGainLabel = new JLabel("0 m");
		dataPanel.add(elevationGainLabel, c);
		
		c.gridx = 6;
		c.gridy = 1;
		totalTimeLabel = new JLabel("0");
		dataPanel.add(totalTimeLabel, c);

		// sep
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 7;
		dataPanel.add(new JSeparator(), c);

		// Geoid
		JLabel geoidHead = new JLabel(tr("Geoid Correction"));
		geoidHead.setFont(getFont().deriveFont(Font.BOLD));
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 5;
		dataPanel.add(geoidHead, c);

		geoidNone = new JRadioButton(tr("None"));
		// TODO: Obtain value from preferences
		geoidNone.setSelected(true);
		geoidNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WayPointHelper.setGeoidKind(GeoidCorrectionKind.None);
				geoidFixedValue.setEnabled(false);
				getModel().updateElevationData();
				updateView();
			}
		});

		geoidAuto = new JRadioButton(tr("Automatic"));
		geoidAuto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WayPointHelper.setGeoidKind(GeoidCorrectionKind.Auto);
				geoidFixedValue.setEnabled(false);
				getModel().updateElevationData();
				updateView();
			}
		});

		geoidFixed = new JRadioButton(tr("Fixed value"));
		geoidFixed.setEnabled(false);
		geoidFixed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WayPointHelper.setGeoidKind(GeoidCorrectionKind.Fixed);
				geoidFixedValue.setEnabled(true);
				getModel().updateElevationData();
				updateView();
			}
		});

		// TODO: Obtain value from preferences
		geoidFixedValue = new JTextField("0");
		geoidFixedValue.setEnabled(false);
		geoidFixedValue.setAlignmentX(RIGHT_ALIGNMENT);
		ButtonGroup grp = new ButtonGroup();
		grp.add(geoidAuto);
		grp.add(geoidNone);
		grp.add(geoidFixed);

		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 4;
		dataPanel.add(geoidNone, c);
		c.gridx = 1;
		c.gridy = 4;
		dataPanel.add(geoidAuto, c);
		c.gridx = 2;
		c.gridy = 4;
		dataPanel.add(geoidFixed, c);
		c.gridx = 3;
		c.gridy = 4;
		dataPanel.add(geoidFixedValue, c);
		c.gridx = 4;
		c.gridy = 4;
		dataPanel.add(new JLabel(" m"), c);

		add(dataPanel, BorderLayout.PAGE_END);
		profile = new ElevationModel();

		profPanel = new ElevationProfilePanel(null);
		add(profPanel, BorderLayout.CENTER);
		profPanel.addComponentListener(this);

		if (WayPointHelper.getGeoidKind() == GeoidCorrectionKind.Auto) {
			geoidAuto.setSelected(true);
		}
		if (WayPointHelper.getGeoidKind() == GeoidCorrectionKind.Fixed) {
			geoidFixed.setSelected(true);
		}
	}

	public IElevationProfile getModel() {
		return profile;
	}

	public void setModel(IElevationProfile model) {
		if (this.profile != model) {
			if (model != null) {
				// System.out.println("Set model " + model);
				this.profile = model;
				profPanel.setElevationModel(model);
				updateView();
			}
		}
	}

	public ElevationProfileLayer getProfileLayer() {
		return profileLayer;
	}

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
	 * Refreshes the dialog when model data have changed.
	 * 
	 * @param model
	 */
	private void updateView() {
		// TODO: Offer also ft here
		if (profile.hasElevationData()) {
			long diff = profile.getTimeDifference();
			long minutes = diff / (1000 * 60);
			long hours = minutes / 60;
			minutes = minutes % 60;
			
			minHeightLabel.setText(String.format("%d m", profile.getMinHeight()));
			maxHeightLabel.setText(String.format("%d m", profile.getMaxHeight()));
			avrgHeightLabel.setText(String.format("%d m", profile
					.getAverageHeight()));
			elevationGainLabel.setText(String.format("%d m", profile
					.getGain()));
			totalTimeLabel.setText(String.format("%d:%d h", hours, minutes));
			distLabel.setText(String.format("%5.2f km", profile
					.getDistance()));
		} else {
			minHeightLabel.setText("-");
			maxHeightLabel.setText("-");
			avrgHeightLabel.setText("-");
			elevationGainLabel.setText("-");
			totalTimeLabel.setText("-");
			distLabel.setText("-");
		}
		
		fireModelChanged();
		repaint();
	}

	/**
	 * Fires the 'model changed' event to all listeners.
	 */
	protected void fireModelChanged() {
		for (IElevationModelListener listener : listeners) {
			listener.elevationProfileChanged(getModel());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */

	/*
	 * WORKAROUND: The layer list dialog does not notify any listener when the
	 * selection of the list changes. So the user has to toggle the visibility
	 * of the layer to update the elevation profile.
	 */
	public void propertyChange(PropertyChangeEvent event) {
		Object src = event.getSource();
		if (src instanceof MapView) {
			MapView mapView = (MapView) src;
			Layer l = mapView.getActiveLayer();
			if (l instanceof GpxLayer) {
				setActiveLayer((GpxLayer) l);
			}
		} else if (src instanceof GpxLayer) {
			GpxLayer gpxLayer = (GpxLayer) src;
			setActiveLayer(gpxLayer);
		}
	}

	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if (newLayer instanceof GpxLayer) {
			setActiveLayer((GpxLayer) newLayer);
		}
	}

	private void createLayer(Layer newLayer) {
		if (newLayer != null) {
			if (newLayer instanceof GpxLayer) {
				newLayer.addPropertyChangeListener(this);
				GpxLayer gpxLayer = (GpxLayer) newLayer;
				setActiveLayer(gpxLayer);
			}
		}
	}

	private void setActiveLayer(GpxLayer newLayer) {
		if (activeLayer != newLayer) {
			int slices = 250;
			if (profPanel != null && profPanel.getPlotArea().width > 0) {
				slices = profPanel.getPlotArea().width;
			}

			if (!layerMap.containsKey(newLayer)) {
				GpxData gpxData = newLayer.data;
				ElevationModel em = new ElevationModel(newLayer.getName(),
						gpxData, slices);
				layerMap.put(newLayer, em);
			}
			
			System.out.println("Active layer: " + newLayer.getName());
			ElevationModel em = layerMap.get(newLayer);
			em.setSliceSize(slices);
			setModel(em);
			
		}
	}

	public void layerAdded(Layer newLayer) {
		createLayer(newLayer);
		System.out.println("layerAdded: " + newLayer.getName());
	}

	public void layerRemoved(Layer oldLayer) {
		if (layerMap.containsKey(oldLayer)) {
			// TODO: Handle UI stuff properly
			layerMap.remove(oldLayer);
		}

	}

	public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
		// Nothing to do...
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentHidden(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent
	 * )
	 */
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentResized(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
		int slices = 100;
		if (profPanel != null) {
			slices = profPanel.getPlotArea().width;
		}

		if (profile != null && profile.getSliceSize() != slices) {
			profile.setSliceSize(slices);
		}
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
