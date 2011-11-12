package oseam.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class PanelMain extends JPanel {

	private OSeaMAction dlg;
	public PanelChan panelChan = null;
	public PanelHaz panelHaz = null;
	public PanelSpec panelSpec = null;
	public PanelLights panelLights = null;
	public PanelMore panelMore = null;
	public PanelTop panelTop = null;
	public PanelFog panelFog = null;
	public PanelRadar panelRadar = null;
	public PanelLit panelLit = null;
	public JLabel shapeIcon = null;
	public JLabel lightIcon = null;
	public JLabel topIcon = null;
	public JLabel radarIcon = null;
	public JLabel radarLabel = null;
	public JLabel fogIcon = null;
	public JLabel fogLabel = null;
	public JLabel colLabel = null;
	public JLabel nameLabel = null;
	public JTextField nameBox = null;
	private ActionListener alName = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark != null)
				dlg.mark.setName(nameBox.getText());
		}
	};
	public JButton saveButton = null;
	private ActionListener alSave = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark != null) {
				dlg.mark.saveSign(dlg.node);
			}
		}
	};
	public JButton moreButton = null;
	private ActionListener alMore = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (panelMore.isVisible()) {
				moreButton.setText("v v v");
				panelMore.setVisible(false);
				topButton.setEnabled(true);
				radButton.setEnabled(true);
				fogButton.setEnabled(true);
				litButton.setEnabled(true);
			} else {
				panelMore.setVisible(true);
				moreButton.setText("^ ^ ^");
				miscButtons.clearSelection();
				panelTop.setVisible(false);
				topButton.setBorderPainted(false);
				topButton.setEnabled(false);
				panelRadar.setVisible(false);
				radButton.setBorderPainted(false);
				radButton.setEnabled(false);
				panelFog.setVisible(false);
				fogButton.setBorderPainted(false);
				fogButton.setEnabled(false);
				panelLit.setVisible(false);
				litButton.setBorderPainted(false);
				litButton.setEnabled(false);
			}
		}
	};
	public ButtonGroup typeButtons = null;
	public JRadioButton chanButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ChanButton.png")));
	public JRadioButton hazButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/HazButton.png")));
	public JRadioButton specButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SpecButton.png")));
	public JRadioButton lightsButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightsButton.png")));
	private ActionListener alType = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.node == null)
				typeButtons.clearSelection();
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
	private ButtonGroup miscButtons = null;
	public JRadioButton topButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TopButton.png")));
	public JRadioButton fogButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FogButton.png")));
	public JRadioButton radButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RadarButton.png")));
	public JRadioButton litButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LitButton.png")));
	private ActionListener alMisc = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark == null) {
				miscButtons.clearSelection();
			}
			if (topButton.isSelected()) {
				moreButton.setText("v v v");
				panelMore.setVisible(false);
				topButton.setBorderPainted(true);
				panelTop.setVisible(true);
			} else {
				topButton.setBorderPainted(false);
				panelTop.setVisible(false);
			}
			if (fogButton.isSelected()) {
				moreButton.setText("v v v");
				panelMore.setVisible(false);
				fogButton.setBorderPainted(true);
				panelFog.setVisible(true);
			} else {
				fogButton.setBorderPainted(false);
				panelFog.setVisible(false);
			}
			if (radButton.isSelected()) {
				moreButton.setText("v v v");
				panelMore.setVisible(false);
				radButton.setBorderPainted(true);
				panelRadar.setVisible(true);
			} else {
				radButton.setBorderPainted(false);
				panelRadar.setVisible(false);
			}
			if (litButton.isSelected()) {
				moreButton.setText("v v v");
				panelMore.setVisible(false);
				litButton.setBorderPainted(true);
				panelLit.setVisible(true);
			} else {
				litButton.setBorderPainted(false);
				panelLit.setVisible(false);
			}
		}
	};

	public PanelMain(OSeaMAction dia) {

		dlg = dia;
		panelChan = new PanelChan(dlg);
		panelChan.setBounds(new Rectangle(65, 0, 180, 160));
		panelChan.setVisible(false);
		panelHaz = new PanelHaz(dlg);
		panelHaz.setBounds(new Rectangle(65, 0, 180, 160));
		panelHaz.setVisible(false);
		panelSpec = new PanelSpec(dlg);
		panelSpec.setBounds(new Rectangle(65, 0, 180, 160));
		panelSpec.setVisible(false);
		panelLights = new PanelLights(dlg);
		panelLights.setBounds(new Rectangle(65, 0, 180, 160));
		panelLights.setVisible(false);
		panelMore = new PanelMore(dlg);
		panelMore.setBounds(new Rectangle(40, 165, 360, 160));
		panelMore.setVisible(false);
		panelTop = new PanelTop(dlg);
		panelTop.setBounds(new Rectangle(40, 165, 360, 160));
		panelTop.setVisible(false);
		panelFog = new PanelFog(dlg);
		panelFog.setBounds(new Rectangle(40, 165, 360, 160));
		panelFog.setVisible(false);
		panelRadar = new PanelRadar(dlg);
		panelRadar.setBounds(new Rectangle(40, 165, 360, 160));
		panelRadar.setVisible(false);
		panelLit = new PanelLit(dlg);
		panelLit.setBounds(new Rectangle(40, 165, 360, 160));
		panelLit.setVisible(false);

		shapeIcon = new JLabel();
		shapeIcon.setBounds(new Rectangle(235, 0, 150, 185));
		this.add(shapeIcon, null);
		lightIcon = new JLabel();
		lightIcon.setBounds(new Rectangle(235, 0, 150, 185));
		this.add(lightIcon, null);
		topIcon = new JLabel();
		topIcon.setBounds(new Rectangle(235, 0, 150, 185));
		this.add(topIcon, null);
		radarIcon = new JLabel();
		radarIcon.setBounds(new Rectangle(235, 0, 150, 185));
		this.add(radarIcon, null);
		fogIcon = new JLabel();
		fogIcon.setBounds(new Rectangle(235, 0, 150, 185));
		this.add(fogIcon, null);
		radarLabel = new JLabel("", SwingConstants.CENTER);
		radarLabel.setBounds(new Rectangle(210, 35, 100, 20));
		this.add(radarLabel, null);
		fogLabel = new JLabel("", SwingConstants.CENTER);
		fogLabel.setBounds(new Rectangle(210, 60, 100, 20));
		this.add(fogLabel, null);
		colLabel = new JLabel("", SwingConstants.CENTER);
		colLabel.setBounds(new Rectangle(235, 100, 150, 20));
		this.add(colLabel, null);

		this.add(getButton(chanButton, 0, 0, 62, 40, "Chan"), null);
		this.add(getButton(hazButton, 0, 40, 62, 40, "Haz"), null);
		this.add(getButton(specButton, 0, 80, 62, 40, "Spec"), null);
		this.add(getButton(lightsButton, 0, 120, 62, 40, "Lights"), null);
		this.add(panelChan, null);
		this.add(panelHaz, null);
		this.add(panelSpec, null);
		this.add(panelLights, null);
		this.add(panelMore, null);
		this.add(panelTop, null);
		this.add(panelFog, null);
		this.add(panelRadar, null);
		this.add(panelLit, null);
		typeButtons = new ButtonGroup();
		typeButtons.add(chanButton);
		typeButtons.add(hazButton);
		typeButtons.add(specButton);
		typeButtons.add(lightsButton);
		chanButton.addActionListener(alType);
		hazButton.addActionListener(alType);
		specButton.addActionListener(alType);
		lightsButton.addActionListener(alType);

		this.add(getButton(topButton, 0, 165, 34, 32, "Topmarks"), null);
		this.add(getButton(fogButton, 0, 205, 34, 32, "FogSignals"), null);
		this.add(getButton(radButton, 0, 245, 34, 32, "Radar"), null);
		this.add(getButton(litButton, 0, 285, 34, 32, "Lit"), null);
		miscButtons = new ButtonGroup();
		miscButtons.add(topButton);
		miscButtons.add(fogButton);
		miscButtons.add(radButton);
		miscButtons.add(litButton);
		topButton.addActionListener(alMisc);
		fogButton.addActionListener(alMisc);
		radButton.addActionListener(alMisc);
		litButton.addActionListener(alMisc);

		nameLabel = new JLabel();
		nameLabel.setBounds(new Rectangle(5, 329, 60, 20));
		nameLabel.setText(tr("Name:"));
		this.add(nameLabel, null);
		nameBox = new JTextField();
		nameBox.setBounds(new Rectangle(60, 330, 200, 20));
		this.add(nameBox, null);
		nameBox.addActionListener(alName);

		saveButton = new JButton();
		saveButton.setBounds(new Rectangle(285, 330, 100, 20));
		saveButton.setText(tr("Save"));
		this.add(saveButton, null);
		saveButton.addActionListener(alSave);

		moreButton = new JButton();
		moreButton.setBounds(new Rectangle(350, 145, 40, 15));
		moreButton.setMargin(new Insets(0, 0, 0, 0));
		moreButton.setText("v v v");
		this.add(moreButton, null);
		moreButton.addActionListener(alMore);

	}

	public void syncPanel() {
		typeButtons.clearSelection();
		chanButton.setBorderPainted(false);
		chanButton.setEnabled(false);
		hazButton.setBorderPainted(false);
		hazButton.setEnabled(false);
		specButton.setBorderPainted(false);
		specButton.setEnabled(false);
		lightsButton.setBorderPainted(false);
		lightsButton.setEnabled(false);
		miscButtons.clearSelection();
		topButton.setEnabled(false);
		fogButton.setEnabled(false);
		radButton.setEnabled(false);
		litButton.setEnabled(false);
		saveButton.setEnabled(false);
		moreButton.setVisible(false);
		moreButton.setSelected(false);
		panelChan.setVisible(false);
		panelHaz.setVisible(false);
		panelSpec.setVisible(false);
		panelLights.setVisible(false);
		panelMore.setVisible(false);
		radarLabel.setText("");
		fogLabel.setText("");
		colLabel.setText("");
		shapeIcon.setIcon(null);
		lightIcon.setIcon(null);
		topIcon.setIcon(null);
		radarIcon.setIcon(null);
		fogIcon.setIcon(null);
		nameBox.setEnabled(false);
		if (dlg.mark != null) {
			nameBox.setEnabled(true);
			chanButton.setEnabled(true);
			hazButton.setEnabled(true);
			specButton.setEnabled(true);
			lightsButton.setEnabled(true);
			nameBox.setText(dlg.mark.getName());
			if (dlg.mark.isValid()) {
				moreButton.setVisible(true);
				moreButton.setEnabled(true);
				topButton.setEnabled(true);
				fogButton.setEnabled(true);
				radButton.setEnabled(true);
				litButton.setEnabled(true);
				saveButton.setEnabled(true);
			}
			switch (SeaMark.GrpMAP.get(dlg.mark.getObject())) {
			case LAT:
			case SAW:
				chanButton.setBorderPainted(true);
				panelChan.setVisible(true);
				panelChan.syncPanel();
				break;
			case CAR:
			case ISD:
				hazButton.setBorderPainted(true);
				panelHaz.setVisible(true);
				panelHaz.syncPanel();
				break;
			case SPP:
				specButton.setBorderPainted(true);
				panelSpec.setVisible(true);
				panelSpec.syncPanel();
				break;
			case LIT:
			case SIS:
				lightsButton.setBorderPainted(true);
				topButton.setEnabled(false);
				panelLights.setVisible(true);
				panelLights.syncPanel();
				break;
			}
			panelChan.syncPanel();
			panelHaz.syncPanel();
			panelSpec.syncPanel();
			panelLights.syncPanel();
			panelMore.syncPanel();
			panelTop.syncPanel();
			panelFog.syncPanel();
			panelRadar.syncPanel();
			panelLit.syncPanel();
		}
	}

	private JRadioButton getButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		return button;
	}

}
