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
	public SeaMark mark = null;
	public PanelChan panelChan = null;
	public PanelHaz panelHaz = null;
	public PanelSpec panelSpec = null;
	public PanelLights panelLights = null;
	public PanelMore panelMore = null;
	public PanelTop panelTop = null;
	public PanelFog panelFog = null;
	public PanelRadar panelRadar = null;
	public PanelLit panelLit = null;
	public JLabel nameLabel = null;
	public JTextField nameBox = null;
	private ActionListener alName = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			mark.setName(nameBox.getText());
		}
	};
	public JButton saveButton = null;
	private ActionListener alSave = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			mark.saveSign(dlg.node);
		}
	};
	public JButton moreButton = null;
	private ActionListener alMore = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (panelMore.isVisible()) {
				moreButton.setText(">>");
				panelMore.setVisible(false);
				topButton.setEnabled(true);
				radButton.setEnabled(true);
				fogButton.setEnabled(true);
				litButton.setEnabled(true);
			} else {
				panelMore.setVisible(true);
				moreButton.setText("<<");
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
			if (chanButton.isSelected()) {
				chanButton.setBorderPainted(true);
				panelChan.syncPanel();
				panelChan.setVisible(true);
			} else {
				chanButton.setBorderPainted(false);
				panelChan.setVisible(false);
			}
			if (hazButton.isSelected()) {
				hazButton.setBorderPainted(true);
				panelHaz.syncPanel();
				panelHaz.setVisible(true);
			} else {
				hazButton.setBorderPainted(false);
				panelHaz.setVisible(false);
			}
			if (specButton.isSelected()) {
				specButton.setBorderPainted(true);
				panelSpec.syncPanel();
				panelSpec.setVisible(true);
			} else {
				specButton.setBorderPainted(false);
				panelSpec.setVisible(false);
			}
			if (lightsButton.isSelected()) {
				lightsButton.setBorderPainted(true);
				panelLights.syncPanel();
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
			if (topButton.isSelected()) {
				moreButton.setText(">>");
				panelMore.setVisible(false);
				topButton.setBorderPainted(true);
				panelTop.setVisible(true);
			} else {
				topButton.setBorderPainted(false);
				panelTop.setVisible(false);
			}
			if (fogButton.isSelected()) {
				moreButton.setText(">>");
				panelMore.setVisible(false);
				fogButton.setBorderPainted(true);
				panelFog.setVisible(true);
			} else {
				fogButton.setBorderPainted(false);
				panelFog.setVisible(false);
			}
			if (radButton.isSelected()) {
				moreButton.setText(">>");
				panelMore.setVisible(false);
				radButton.setBorderPainted(true);
				panelRadar.setVisible(true);
			} else {
				radButton.setBorderPainted(false);
				panelRadar.setVisible(false);
			}
			if (litButton.isSelected()) {
				moreButton.setText(">>");
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
		setLayout(null);
		mark = new SeaMark(dlg);
		mark.setBounds(new Rectangle(235, 0, 165, 160));
		add(mark);
		panelChan = new PanelChan(dlg);
		panelChan.setBounds(new Rectangle(65, 0, 170, 160));
		panelChan.setVisible(false);
		panelHaz = new PanelHaz(dlg);
		panelHaz.setBounds(new Rectangle(65, 0, 170, 160));
		panelHaz.setVisible(false);
		panelSpec = new PanelSpec(dlg);
		panelSpec.setBounds(new Rectangle(65, 0, 170, 160));
		panelSpec.setVisible(false);
		panelLights = new PanelLights(dlg);
		panelLights.setBounds(new Rectangle(65, 0, 170, 160));
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

		add(getButton(chanButton, 0, 0, 62, 40, "Chan"), null);
		add(getButton(hazButton, 0, 40, 62, 40, "Haz"), null);
		add(getButton(specButton, 0, 80, 62, 40, "Spec"), null);
		add(getButton(lightsButton, 0, 120, 62, 40, "Lights"), null);
		add(panelChan);
		add(panelHaz);
		add(panelSpec);
		add(panelLights);
		add(panelMore);
		add(panelTop);
		add(panelFog);
		add(panelRadar);
		add(panelLit);
		typeButtons = new ButtonGroup();
		typeButtons.add(chanButton);
		typeButtons.add(hazButton);
		typeButtons.add(specButton);
		typeButtons.add(lightsButton);
		chanButton.addActionListener(alType);
		hazButton.addActionListener(alType);
		specButton.addActionListener(alType);
		lightsButton.addActionListener(alType);

		add(getButton(topButton, 0, 185, 34, 32, "Topmarks"));
		add(getButton(fogButton, 0, 220, 34, 32, "FogSignals"));
		add(getButton(radButton, 0, 255, 34, 32, "Radar"));
		add(getButton(litButton, 0, 290, 34, 32, "Lit"));
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
		add(nameLabel);
		nameBox = new JTextField();
		nameBox.setBounds(new Rectangle(60, 330, 200, 20));
		nameBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(nameBox);
		nameBox.addActionListener(alName);

		saveButton = new JButton();
		saveButton.setBounds(new Rectangle(285, 330, 100, 20));
		saveButton.setText(tr("Save"));
		add(saveButton);
		saveButton.addActionListener(alSave);

		moreButton = new JButton();
		moreButton.setBounds(new Rectangle(0, 165, 34, 15));
		moreButton.setMargin(new Insets(0, 0, 0, 0));
		moreButton.setText(">>");
		add(moreButton);
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
		topButton.setBorderPainted(false);
		fogButton.setEnabled(false);
		fogButton.setBorderPainted(false);
		radButton.setEnabled(false);
		radButton.setBorderPainted(false);
		litButton.setEnabled(false);
		litButton.setBorderPainted(false);
		saveButton.setEnabled(false);
		moreButton.setVisible(false);
		moreButton.setText(">>");
		moreButton.setSelected(false);
		panelChan.setVisible(false);
		panelHaz.setVisible(false);
		panelSpec.setVisible(false);
		panelLights.setVisible(false);
		panelMore.setVisible(false);
		panelTop.setVisible(false);
		panelFog.setVisible(false);
		panelRadar.setVisible(false);
		panelLit.setVisible(false);
		nameBox.setEnabled(false);
		if (mark != null) {
			nameBox.setEnabled(true);
			chanButton.setEnabled(true);
			hazButton.setEnabled(true);
			specButton.setEnabled(true);
			lightsButton.setEnabled(true);
			nameBox.setText(mark.getName());
			switch (SeaMark.GrpMAP.get(mark.getObject())) {
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
			case LGT:
			case SIS:
				lightsButton.setBorderPainted(true);
				topButton.setEnabled(false);
				panelLights.setVisible(true);
				panelLights.syncPanel();
				break;
			}
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
