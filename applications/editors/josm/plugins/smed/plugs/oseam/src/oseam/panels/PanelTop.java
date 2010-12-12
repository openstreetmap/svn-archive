package oseam.panels;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JButton;

import oseam.Messages;

import java.awt.Cursor;
import java.awt.event.ActionListener;

public class PanelTop extends JPanel {

	private ButtonGroup topButtons = null;
	private JRadioButton noTopButton = null;

	public PanelTop() {
		super();
		initialize();
	}

	private void initialize() {
		this.setLayout(null);
		this.add(getNoTopButton(), null);
		topButtons = new ButtonGroup();
		topButtons.add(noTopButton);
	}

	private JRadioButton getNoTopButton() {
		if (noTopButton == null) {
			noTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/NoTopButton.png")));
			noTopButton.setBounds(new Rectangle(5, 5, 30, 30));
			noTopButton.setToolTipText(Messages.getString("NorthTip"));
		}
		return noTopButton;
	}

}
