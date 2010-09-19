package smed.menu.file;


import java.awt.Dialog;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.Rectangle;
import javax.swing.JList;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;

import smed.list.JCheckBoxList;
public class HideAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JDialog hideDialog = null;  //  @jve:decl-index=0:visual-constraint="62,8"
	private JPanel jContentPane = null;
	private JButton okButton = null;
	private JCheckBoxList hideList = null;
	private JCheckBox jCheckBox = null;
	/**
	 * This method initializes hideDialog	
	 * 	
	 * @return javax.swing.JDialog	
	 */
	public JDialog getHideDialog() {
		if (hideDialog == null) {
			hideDialog = new JDialog();
			hideDialog.setResizable(false);
			hideDialog.setSize(new Dimension(360, 480));
			hideDialog.setModal(true);
			hideDialog.setTitle("Hide Tab");
			hideDialog.setContentPane(getJContentPane());
		}
		return hideDialog;
	}
	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getOkButton(), null);
			jContentPane.add(getHideList(), null);
		}
		return jContentPane;
	}
	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setBounds(new Rectangle(115, 400, 110, 20));
			okButton.setName("");
			okButton.setText("Ok");
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					hideDialog.dispose();
				}
			});
		}
		return okButton;
	}
	/**
	 * This method initializes hideList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JCheckBoxList getHideList() {
		if (hideList == null) {
			hideList = new JCheckBoxList();
			hideList.setBounds(new Rectangle(20, 15, 315, 370));
			hideList.setBorder(LineBorder.createBlackLineBorder());
			hideList.add(getJCheckBox());			
			}
		return hideList;
	}
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setBounds(new Rectangle(10, 10, 100, 20));
		}
		return jCheckBox;
	}

}
