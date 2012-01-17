package wayarea.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import wayarea.Messages;
import wayarea.dialogs.WayAreaAction;

public class PanelMain extends JPanel {

	private WayAreaAction dlg;
//	public PanelF panelF = null;
//	public PanelJ panelJ = null;
//	public PanelK panelK = null;
//	public PanelL panelL = null;
//	public PanelM panelM = null;
//	public PanelN panelN = null;
	public JButton saveButton = null;
	private ActionListener alSave = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
//			mark.saveSign(dlg.node);
		}
	};
	public ButtonGroup typeButtons = null;
	public JRadioButton fButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ChanButton.png")));
	public JRadioButton jButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/HazButton.png")));
	public JRadioButton kButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SpecButton.png")));
	public JRadioButton lButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightsButton.png")));
	public JRadioButton mButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightsButton.png")));
	public JRadioButton nButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightsButton.png")));
	private ActionListener alType = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (fButton.isSelected()) {
				fButton.setBorderPainted(true);
//				panelF.syncPanel();
//				panelF.setVisible(true);
			} else {
				fButton.setBorderPainted(false);
//				panelF.setVisible(false);
			}
			if (jButton.isSelected()) {
				jButton.setBorderPainted(true);
//				panelJ.syncPanel();
//				panelJ.setVisible(true);
			} else {
				jButton.setBorderPainted(false);
//				panelJ.setVisible(false);
			}
			if (kButton.isSelected()) {
				kButton.setBorderPainted(true);
//				panelK.syncPanel();
//				panelK.setVisible(true);
			} else {
				kButton.setBorderPainted(false);
//				panelK.setVisible(false);
			}
			if (lButton.isSelected()) {
				lButton.setBorderPainted(true);
//				panelL.syncPanel();
//				panelL.setVisible(true);
			} else {
				lButton.setBorderPainted(false);
//				panelL.setVisible(false);
			}
			if (mButton.isSelected()) {
				mButton.setBorderPainted(true);
//				panelM.syncPanel();
//				panelM.setVisible(true);
			} else {
				mButton.setBorderPainted(false);
//				panelM.setVisible(false);
			}
			if (nButton.isSelected()) {
				nButton.setBorderPainted(true);
//				panelN.syncPanel();
//				panelN.setVisible(true);
			} else {
				nButton.setBorderPainted(false);
//				panelN.setVisible(false);
			}
		}
	};

	public PanelMain(WayAreaAction dia) {

		dlg = dia;
		setLayout(null);

	}

	public void syncPanel() {
//		typeButtons.clearSelection();
//		fButton.setBorderPainted(false);
//		jButton.setEnabled(false);
//		kButton.setBorderPainted(false);
//		lButton.setEnabled(false);
//		mButton.setBorderPainted(false);
//		nButton.setEnabled(false);
	}

	private JRadioButton getButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		return button;
	}

}
