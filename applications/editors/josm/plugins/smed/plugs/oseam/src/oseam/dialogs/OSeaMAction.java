package oseam.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

import oseam.Messages;
import oseam.OSeaM;

public class OSeaMAction {

	private JPanel oseamPanel = null;

	private ButtonGroup typeButtons = null;
	private JRadioButton chanButton = null;
	private JRadioButton hazButton = null;
	private JRadioButton specButton = null;
	private JRadioButton lightButton = null;

	public OSeaMAction() {

		String str = Main.pref.get("mappaint.style.sources");
		if (!str.contains("dev.openseamap.org")) {
			if (!str.isEmpty())
				str += new String(new char[] { 0x1e });
			Main.pref.put("mappaint.style.sources", str
					+ "http://dev.openseamap.org/josm/seamark_styles.xml");
		}
		str = Main.pref.get("color.background");
		if (str.equals("#000000") || str.isEmpty())
			Main.pref.put("color.background", "#606060");

	}

	public JPanel getOSeaMPanel() {
		if (oseamPanel == null) {
			oseamPanel = new JPanel();
			oseamPanel.setLayout(null);
			oseamPanel.setSize(new Dimension(400, 360));

			oseamPanel.add(getChanButton(), null);
			oseamPanel.add(getHazButton(), null);
			oseamPanel.add(getSpecButton(), null);
			oseamPanel.add(getLightButton(), null);
			typeButtons = new ButtonGroup();
			typeButtons.add(chanButton);
			typeButtons.add(hazButton);
			typeButtons.add(specButton);
			typeButtons.add(lightButton);
			ActionListener alType = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					chanButton.setEnabled(!chanButton.isSelected());
					hazButton.setEnabled(!hazButton.isSelected());
					specButton.setEnabled(!specButton.isSelected());
					lightButton.setEnabled(!lightButton.isSelected());
//System.out.println("pressed: " + chanButton.isSelected() + " " + hazButton.isSelected() + " " + specButton.isSelected() + " " + lightButton.isSelected());
				}
			};
			chanButton.addActionListener(alType);
			hazButton.addActionListener(alType);
			specButton.addActionListener(alType);
			lightButton.addActionListener(alType);
		}
		return oseamPanel;
	}

	private JRadioButton getChanButton() {
		if (chanButton == null) {
			chanButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("ChanButton"))));
			chanButton.setBounds(new Rectangle(5, 0, 105, 40));
			chanButton.setToolTipText(Messages.getString("ChanTip"));
		}
		return chanButton;
	}

	private JRadioButton getHazButton() {
		if (hazButton == null) {
			hazButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("HazButton"))));
			hazButton.setBounds(new Rectangle(5, 40, 105, 40));
			hazButton.setToolTipText(Messages.getString("HazTip"));
		}
		return hazButton;
	}

	private JRadioButton getSpecButton() {
		if (specButton == null) {
			specButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("SpecButton"))));
			specButton.setBounds(new Rectangle(5, 80, 105, 40));
			specButton.setToolTipText(Messages.getString("SpecTip"));
		}
		return specButton;
	}

	private JRadioButton getLightButton() {
		if (lightButton == null) {
			lightButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("LightsButton"))));
			lightButton.setBounds(new Rectangle(5, 120, 105, 40));
			lightButton.setToolTipText(Messages.getString("LightsTip"));
		}
		return lightButton;
	}

}
