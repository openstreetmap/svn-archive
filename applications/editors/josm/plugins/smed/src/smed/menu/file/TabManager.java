package smed.menu.file;


import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JList;
import java.awt.Rectangle;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class TabManager extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	DefaultListModel model;


	private JDialog tabManagerDialog = null;  //  @jve:decl-index=0:visual-constraint="59,23"

	private JPanel tabManagerPanel = null;

	private JScrollPane tabScrollPane = null;

	private JList tabList = null;

	private JButton tabButtonOk = null;

	private JButton tabButtonCancel = null;

	private JButton tabButtonUndo = null;

	private JButton tabButtonLoad = null;

	private JButton tabButtonSave = null;

	private JButton tabButtonDelete = null;

	private JButton tabButtonVisible = null;

	private JButton tabButtonAll = null;

	private JButton tabButtonNone = null;

	private JLabel tabLabelSelect = null;

	private JLabel tabLabelRename = null;

	private JTextField tabTextFieldRename = null;

	public TabManager(DefaultListModel model) {
		this.model = model;
		
		getTabManagerDialog().setVisible(true);
	}

	/**
	 * This method initializes tabManagerDialog	
	 * 	
	 * @return javax.swing.JDialog	
	 */
	private JDialog getTabManagerDialog() {
		if (tabManagerDialog == null) {
			tabManagerDialog = new JDialog(this);
			tabManagerDialog.setSize(new Dimension(409, 442));
			tabManagerDialog.setResizable(false);
			tabManagerDialog.setModal(true);
			tabManagerDialog.setContentPane(getTabManagerPanel());
			tabManagerDialog.setTitle("Tabmanager");
		}
		return tabManagerDialog;
	}

	/**
	 * This method initializes tabManagerPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getTabManagerPanel() {
		if (tabManagerPanel == null) {
			tabLabelRename = new JLabel();
			tabLabelRename.setBounds(new Rectangle(15, 334, 70, 25));
			tabLabelRename.setText("Rename:");
			tabLabelSelect = new JLabel();
			tabLabelSelect.setBounds(new Rectangle(16, 304, 72, 15));
			tabLabelSelect.setFont(new Font("Dialog", Font.PLAIN, 12));
			tabLabelSelect.setText("Select:");
			tabManagerPanel = new JPanel();
			tabManagerPanel.setLayout(null);
			tabManagerPanel.setOpaque(true);
			// tabManagerPanel.add(getJList(), null);
			tabManagerPanel.add(getTabScrollPane(), null);
			tabManagerPanel.add(getTabButtonOk(), null);
			tabManagerPanel.add(getTabButtonCancel(), null);
			tabManagerPanel.add(getTabButtonUndo(), null);
			tabManagerPanel.add(getTabButtonLoad(), null);
			tabManagerPanel.add(getTabButtonSave(), null);
			tabManagerPanel.add(getTabButtonDelete(), null);
			tabManagerPanel.add(getTabButtonVisible(), null);
			tabManagerPanel.add(getTabButtonAll(), null);
			tabManagerPanel.add(getTabButtonNone(), null);
			tabManagerPanel.add(tabLabelSelect, null);
			tabManagerPanel.add(tabLabelRename, null);
			tabManagerPanel.add(getTabTextFieldRename(), null);
		}
		return tabManagerPanel;
	}


	/**
	 * This method initializes tabScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getTabScrollPane() {
		if (tabScrollPane == null) {
			tabScrollPane = new JScrollPane();
			tabScrollPane.setBounds(new Rectangle(15, 8, 225, 285));
			tabScrollPane.setViewportView(getTabList());
			tabScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
		return tabScrollPane;
	}

	/**
	 * This method initializes tabList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getTabList() {
		if (tabList == null) {
			tabList = new JList();
		}
		return tabList;
	}

	/**
	 * This method initializes tabButtonOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTabButtonOk() {
		if (tabButtonOk == null) {
			tabButtonOk = new JButton();
			tabButtonOk.setBounds(new Rectangle(254, 4, 130, 30));
			tabButtonOk.setFont(new Font("Dialog", Font.BOLD, 12));
			tabButtonOk.setText("Ok");
		}
		return tabButtonOk;
	}

	/**
	 * This method initializes tabButtonCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTabButtonCancel() {
		if (tabButtonCancel == null) {
			tabButtonCancel = new JButton();
			tabButtonCancel.setBounds(new Rectangle(254, 44, 130, 30));
			tabButtonCancel.setFont(new Font("Dialog", Font.BOLD, 12));
			tabButtonCancel.setText("Cancel");
		}
		return tabButtonCancel;
	}

	/**
	 * This method initializes tabButtonUndo	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTabButtonUndo() {
		if (tabButtonUndo == null) {
			tabButtonUndo = new JButton();
			tabButtonUndo.setBounds(new Rectangle(254, 84, 130, 30));
			tabButtonUndo.setFont(new Font("Dialog", Font.BOLD, 12));
			tabButtonUndo.setText("Undo");
		}
		return tabButtonUndo;
	}

	/**
	 * This method initializes tabButtonLoad	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTabButtonLoad() {
		if (tabButtonLoad == null) {
			tabButtonLoad = new JButton();
			tabButtonLoad.setBounds(new Rectangle(186, 328, 104, 30));
			tabButtonLoad.setFont(new Font("Dialog", Font.BOLD, 12));
			tabButtonLoad.setText("Load");
		}
		return tabButtonLoad;
	}

	/**
	 * This method initializes tabButtonSave	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTabButtonSave() {
		if (tabButtonSave == null) {
			tabButtonSave = new JButton();
			tabButtonSave.setBounds(new Rectangle(293, 328, 104, 30));
			tabButtonSave.setFont(new Font("Dialog", Font.BOLD, 12));
			tabButtonSave.setText("Save");
		}
		return tabButtonSave;
	}

	/**
	 * This method initializes tabButtonDelete	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTabButtonDelete() {
		if (tabButtonDelete == null) {
			tabButtonDelete = new JButton();
			tabButtonDelete.setBounds(new Rectangle(186, 362, 104, 30));
			tabButtonDelete.setText("Delete");
		}
		return tabButtonDelete;
	}

	/**
	 * This method initializes tabButtonVisible	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTabButtonVisible() {
		if (tabButtonVisible == null) {
			tabButtonVisible = new JButton();
			tabButtonVisible.setBounds(new Rectangle(293, 362, 104, 30));
			tabButtonVisible.setText("visible");
		}
		return tabButtonVisible;
	}

	/**
	 * This method initializes tabButtonAll	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTabButtonAll() {
		if (tabButtonAll == null) {
			tabButtonAll = new JButton();
			tabButtonAll.setBounds(new Rectangle(92, 300, 72, 20));
			tabButtonAll.setFont(new Font("Dialog", Font.PLAIN, 12));
			tabButtonAll.setText("all");
		}
		return tabButtonAll;
	}

	/**
	 * This method initializes tabButtonNone	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTabButtonNone() {
		if (tabButtonNone == null) {
			tabButtonNone = new JButton();
			tabButtonNone.setBounds(new Rectangle(166, 300, 72, 20));
			tabButtonNone.setFont(new Font("Dialog", Font.PLAIN, 12));
			tabButtonNone.setText("none");
		}
		return tabButtonNone;
	}

	/**
	 * This method initializes tabTextFieldRename	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTabTextFieldRename() {
		if (tabTextFieldRename == null) {
			tabTextFieldRename = new JTextField();
			tabTextFieldRename.setBounds(new Rectangle(14, 362, 167, 32));
		}
		return tabTextFieldRename;
	}

}
