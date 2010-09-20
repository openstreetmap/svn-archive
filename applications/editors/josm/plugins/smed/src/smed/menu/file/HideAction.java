package smed.menu.file;


import java.awt.Dialog;

import javax.swing.DefaultListModel;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;

import smed.list.CheckBoxJList;
public class HideAction extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JDialog hideDialog = null;  //  @jve:decl-index=0:visual-constraint="62,8"
	private JPanel jContentPane = null;
	private JButton okButton = null;
	private CheckBoxJList hideList = null;
	private JCheckBox jCheckBox = null;
	private DefaultListModel model = null;
	
	
	public HideAction(DefaultListModel model) {
		this.model = model;
		
		getHideDialog().setVisible(true);
	}
	
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
					hideDialog.setVisible(true);
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
	public JList getHideList() {
		if (hideList == null) {
			hideList = new CheckBoxJList();
			hideList.setModel (model);
			hideList.setBounds(new Rectangle(20, 15, 315, 370));
			hideList.setBorder(LineBorder.createBlackLineBorder());
			hideList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					int i = hideList.getSelectedIndex();
					System.out.println("i:\t" + i);
				}
			});
		}
		
		return hideList;
	}
}
