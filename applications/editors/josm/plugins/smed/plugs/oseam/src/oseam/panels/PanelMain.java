package oseam.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import oseam.panels.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;

import oseam.Messages;
import oseam.OSeaM;

public class PanelMain extends JPanel {

	private JLabel shapeIcon = null;
	private JLabel lightIcon = null;
	private JLabel topIcon = null;
	private JLabel reflIcon = null;
	private JLabel radarIcon = null;
	private JLabel fogIcon = null;
	private JLabel nameLabel = null;
	private JTextField nameBox = null;
	private JButton saveButton = null;
	private ButtonGroup typeButtons = null;
	private JRadioButton chanButton = null;
	private JRadioButton hazButton = null;
	private JRadioButton specButton = null;
	private JRadioButton lightsButton = null;
	private ButtonGroup miscButtons = null;
	private JRadioButton topButton = null;
	private JRadioButton fogButton = null;
	private JRadioButton radarButton = null;
	private JRadioButton litButton = null;
	private PanelChan panelChan = null;
	private PanelHaz panelHaz = null;
	private PanelSpec panelSpec = null;
	private PanelLights panelLights = null;
	private PanelTop panelTop = null;
	private PanelFog panelFog = null;
	private PanelRadar panelRadar = null;
	private PanelLit panelLit = null;

	public PanelMain() {

		panelChan = new PanelChan();
		panelChan.setBounds(new Rectangle(65, 0, 335, 160));
		panelChan.setVisible(false);
		panelHaz = new PanelHaz();
		panelHaz.setBounds(new Rectangle(65, 0, 335, 160));
		panelHaz.setVisible(false);
		panelSpec = new PanelSpec();
		panelSpec.setBounds(new Rectangle(65, 0, 335, 160));
		panelSpec.setVisible(false);
		panelLights = new PanelLights();
		panelLights.setBounds(new Rectangle(65, 0, 335, 160));
		panelLights.setVisible(false);
		panelTop = new PanelTop();
		panelTop.setBounds(new Rectangle(40, 165, 220, 160));
		panelTop.setVisible(false);
		panelFog = new PanelFog();
		panelFog.setBounds(new Rectangle(40, 165, 220, 160));
		panelFog.setVisible(false);
		panelRadar = new PanelRadar();
		panelRadar.setBounds(new Rectangle(40, 165, 220, 160));
		panelRadar.setVisible(false);
		panelLit = new PanelLit();
		panelLit.setBounds(new Rectangle(40, 165, 220, 160));
		panelLit.setVisible(false);

		shapeIcon = new JLabel(new ImageIcon(getClass().getResource(
				"/images/Cardinal_Pillar_South.png")));
		shapeIcon.setBounds(new Rectangle(265, 170, 130, 185));
		this.add(shapeIcon, null);
		lightIcon = new JLabel(new ImageIcon(getClass().getResource(
				"/images/Light_White_120.png")));
		lightIcon.setBounds(new Rectangle(265, 170, 125, 185));
		this.add(lightIcon, null);
		topIcon = new JLabel();
		topIcon.setBounds(new Rectangle(265, 170, 125, 185));
		this.add(topIcon, null);
		reflIcon = new JLabel(new ImageIcon(getClass().getResource(
				"/images/Radar_Reflector_355.png")));
		reflIcon.setBounds(new Rectangle(265, 170, 125, 185));
		this.add(reflIcon, null);
		radarIcon = new JLabel(new ImageIcon(getClass().getResource(
				"/images/Radar_Station.png")));
		radarIcon.setBounds(new Rectangle(265, 170, 130, 185));
		this.add(radarIcon, null);
		fogIcon = new JLabel(new ImageIcon(getClass().getResource(
				"/images/Fog_Signal.png")));
		fogIcon.setBounds(new Rectangle(265, 170, 125, 185));
		this.add(fogIcon, null);

		this.add(getChanButton(), null);
		this.add(getHazButton(), null);
		this.add(getSpecButton(), null);
		this.add(getLightsButton(), null);
		this.add(panelChan, null);
		this.add(panelHaz, null);
		this.add(panelSpec, null);
		this.add(panelLights, null);
		this.add(panelTop, null);
		this.add(panelFog, null);
		this.add(panelRadar, null);
		this.add(panelLit, null);
		typeButtons = new ButtonGroup();
		typeButtons.add(chanButton);
		typeButtons.add(hazButton);
		typeButtons.add(specButton);
		typeButtons.add(lightsButton);
		ActionListener alType = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (chanButton.isSelected()) {
					chanButton.setBorderPainted(true);
					panelChan.setVisible(true);
				} else {
					chanButton.setBorderPainted(false);
					panelChan.setVisible(false);
				}
				if (hazButton.isSelected()) {
					hazButton.setBorderPainted(true);
					panelHaz.setVisible(true);
				} else {
					hazButton.setBorderPainted(false);
					panelHaz.setVisible(false);
				}
				if (specButton.isSelected()) {
					specButton.setBorderPainted(true);
					panelSpec.setVisible(true);
				} else {
					specButton.setBorderPainted(false);
					panelSpec.setVisible(false);
				}
				if (lightsButton.isSelected()) {
					lightsButton.setBorderPainted(true);
					panelLights.setVisible(true);
				} else {
					lightsButton.setBorderPainted(false);
					panelLights.setVisible(false);
				}
			}
		};
		chanButton.addActionListener(alType);
		hazButton.addActionListener(alType);
		specButton.addActionListener(alType);
		lightsButton.addActionListener(alType);

		this.add(getTopButton(), null);
		this.add(getFogButton(), null);
		this.add(getRadarButton(), null);
		this.add(getLitButton(), null);
		miscButtons = new ButtonGroup();
		miscButtons.add(topButton);
		miscButtons.add(fogButton);
		miscButtons.add(radarButton);
		miscButtons.add(litButton);
		ActionListener alMisc = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (topButton.isSelected()) {
					topButton.setBorderPainted(true);
					panelTop.setVisible(true);
				} else {
					topButton.setBorderPainted(false);
					panelTop.setVisible(false);
				}
				if (fogButton.isSelected()) {
					fogButton.setBorderPainted(true);
					panelFog.setVisible(true);
				} else {
					fogButton.setBorderPainted(false);
					panelFog.setVisible(false);
				}
				if (radarButton.isSelected()) {
					radarButton.setBorderPainted(true);
					panelRadar.setVisible(true);
				} else {
					radarButton.setBorderPainted(false);
					panelRadar.setVisible(false);
				}
				if (litButton.isSelected()) {
					litButton.setBorderPainted(true);
					panelLit.setVisible(true);
				} else {
					litButton.setBorderPainted(false);
					panelLit.setVisible(false);
				}
			}
		};
		topButton.addActionListener(alMisc);
		fogButton.addActionListener(alMisc);
		radarButton.addActionListener(alMisc);
		litButton.addActionListener(alMisc);

		nameLabel = new JLabel();
		nameLabel.setBounds(new Rectangle(5, 329, 60, 20));
		nameLabel.setText(tr("Name:"));
		this.add(nameLabel, null);
		nameBox = new JTextField();
		nameBox.setBounds(new Rectangle(60, 330, 200, 20));
		this.add(nameBox, null);
		saveButton = new JButton();
		saveButton.setBounds(new Rectangle(285, 330, 100, 20));
		saveButton.setText(tr("Save"));
		this.add(saveButton, null);
	}

	private JRadioButton getChanButton() {
		if (chanButton == null) {
			chanButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/ChanButton.png")));
			chanButton.setBounds(new Rectangle(0, 0, 62, 40));
			chanButton.setBorder(BorderFactory.createLineBorder(Color.magenta,
					2));
			chanButton.setToolTipText(Messages.getString("ChanTip"));
		}
		return chanButton;
	}

	private JRadioButton getHazButton() {
		if (hazButton == null) {
			hazButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/HazButton.png")));
			hazButton.setBounds(new Rectangle(0, 40, 62, 40));
			hazButton.setBorder(BorderFactory
					.createLineBorder(Color.magenta, 2));
			hazButton.setToolTipText(Messages.getString("HazTip"));
		}
		return hazButton;
	}

	private JRadioButton getSpecButton() {
		if (specButton == null) {
			specButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/SpecButton.png")));
			specButton.setBounds(new Rectangle(0, 80, 62, 40));
			specButton.setBorder(BorderFactory.createLineBorder(Color.magenta,
					2));
			specButton.setToolTipText(Messages.getString("SpecTip"));
		}
		return specButton;
	}

	private JRadioButton getLightsButton() {
		if (lightsButton == null) {
			lightsButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/LightsButton.png")));
			lightsButton.setBounds(new Rectangle(0, 120, 62, 40));
			lightsButton.setBorder(BorderFactory.createLineBorder(
					Color.magenta, 2));
			lightsButton.setToolTipText(Messages.getString("LightsTip"));
		}
		return lightsButton;
	}

	private JRadioButton getTopButton() {
		if (topButton == null) {
			topButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/TopButton.png")));
			topButton.setBounds(new Rectangle(0, 165, 34, 32));
			topButton.setBorder(BorderFactory
					.createLineBorder(Color.magenta, 2));
			topButton.setToolTipText(Messages.getString("TopmarksTip"));
		}
		return topButton;
	}

	private JRadioButton getFogButton() {
		if (fogButton == null) {
			fogButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/FogButton.png")));
			fogButton.setBounds(new Rectangle(0, 205, 34, 32));
			fogButton.setBorder(BorderFactory
					.createLineBorder(Color.magenta, 2));
			fogButton.setToolTipText(Messages.getString("FogSignalsTip"));
		}
		return fogButton;
	}

	private JRadioButton getRadarButton() {
		if (radarButton == null) {
			radarButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/RadarButton.png")));
			radarButton.setBounds(new Rectangle(0, 245, 34, 32));
			radarButton.setBorder(BorderFactory.createLineBorder(Color.magenta,
					2));
			radarButton.setToolTipText(Messages.getString("RadarTip"));
		}
		return radarButton;
	}

	private JRadioButton getLitButton() {
		if (litButton == null) {
			litButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/LitButton.png")));
			litButton.setBounds(new Rectangle(0, 285, 34, 32));
			litButton.setBorder(BorderFactory
					.createLineBorder(Color.magenta, 2));
			litButton.setToolTipText(Messages.getString("LitTip"));
		}
		return litButton;
	}

}
