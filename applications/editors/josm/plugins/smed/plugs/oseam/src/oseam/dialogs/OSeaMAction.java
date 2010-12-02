package oseam.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

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
	private JButton chanButton = null;
	private JButton hazButton = null;
	private JButton specButton = null;
	private JButton lightButton = null;
	
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
			oseamPanel.add(getHazButton(),  null);
			oseamPanel.add(getChanButton(),  null);
			oseamPanel.add(getSpecButton(),  null);
			oseamPanel.add(getLightButton(),  null);
		}
		return oseamPanel;
	}

	private JButton getChanButton() {
		if (chanButton == null) {
			chanButton = new JButton();
			chanButton.setBounds(new Rectangle(340, 56, 50, 50));
			chanButton.setText("");
			chanButton.setIcon(new ImageIcon(getClass().getResource("/images/Chan.png")));
			chanButton.setToolTipText("Channel Marks");
			chanButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
/*					panelLimits.setVisible(false);
					panelServices.setVisible(false);
					panelEnv.setVisible(false);
					panelRelations.setVisible(false);
					panelGeneral.setVisible(true);
*/				}
			});
		}
		return chanButton;
	}

	private JButton getHazButton() {
		if (hazButton == null) {
			hazButton = new JButton();
			hazButton.setBounds(new Rectangle(340, 111, 50, 50));
			hazButton.setText("");
			hazButton.setIcon(new ImageIcon(getClass().getResource("/images/Haz.png")));
			hazButton.setToolTipText("Danger Marks");
			hazButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
/*					panelLimits.setVisible(false);
					panelServices.setVisible(false);
					panelEnv.setVisible(false);
					panelRelations.setVisible(false);
					panelGeneral.setVisible(true);
*/				}
			});
		}
		return hazButton;
	}

	private JButton getSpecButton() {
		if (specButton == null) {
			specButton = new JButton();
			specButton.setBounds(new Rectangle(340, 166, 50, 50));
			specButton.setText("");
			specButton.setIcon(new ImageIcon(getClass().getResource("/images/Spec.png")));
			specButton.setToolTipText("Special Marks");
			specButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
/*					panelLimits.setVisible(false);
					panelServices.setVisible(false);
					panelEnv.setVisible(false);
					panelRelations.setVisible(false);
					panelGeneral.setVisible(true);
*/				}
			});
		}
		return specButton;
	}

	private JButton getLightButton() {
		if (lightButton == null) {
			lightButton = new JButton();
			lightButton.setBounds(new Rectangle(340, 221, 50, 50));
			lightButton.setText("");
			lightButton.setIcon(new ImageIcon(getClass().getResource("/images/Lights.png")));
			lightButton.setToolTipText("Light Marks");
			lightButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
/*					panelLimits.setVisible(false);
					panelServices.setVisible(false);
					panelEnv.setVisible(false);
					panelRelations.setVisible(false);
					panelGeneral.setVisible(true);
*/				}
			});
		}
		return lightButton;
	}

}
