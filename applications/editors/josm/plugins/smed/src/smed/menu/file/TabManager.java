package smed.menu.file;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;

import smed.io.SmedFile;
import smed.jide.swing.CheckBoxList;
import smed.jide.swing.CheckBoxListSelectionModel;
import smed.plug.ifc.SmedPluggable;
import smed.tabs.SmedTabbedPane;


public class TabManager extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DefaultListModel model;
	private CheckBoxListSelectionModel selModel;
	private List<SmedPluggable> plugins = null;
	private SmedFile splugDir = null;
	private int modelSize = 0;

	private JDialog tabManagerDialog = null;  //  @jve:decl-index=0:visual-constraint="59,23"
	private JPanel tabManagerPanel = null;
	private JScrollPane tabScrollPane = null;
	private CheckBoxList tabList = null;
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

	public TabManager() {
		model = new DefaultListModel(); //model;
		plugins = SmedTabbedPane.getPlugins();
		String pluginDirName = Main.pref.getPluginsDirectory().getAbsolutePath();
		splugDir = new SmedFile(pluginDirName + "/splug");
		
		if(plugins != null) {
			for(SmedPluggable p : plugins){
				if(splugDir.isVisible(p.getFileName()) && !splugDir.isDeleted(p.getFileName())) model.addElement (p.getName());
				else if(splugDir.isDeleted(p.getFileName())) model.addElement("delete - " + p.getName());
				else model.addElement("invisible - " + p.getName());
			}
		} else model.addElement("no plugin loaded");
		
		modelSize = model.getSize();
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
	private CheckBoxList getTabList() {
		if (tabList == null) {
			tabList = new CheckBoxList(model);
			selModel = ((CheckBoxList) tabList).getCheckBoxListSelectionModel();
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
			tabButtonOk.addActionListener(this);
			tabButtonOk.setActionCommand("ok");
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
			tabButtonCancel.setText(tr("Cancel"));
			tabButtonCancel.addActionListener(this);
			tabButtonCancel.setActionCommand("cancel");
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
			tabButtonUndo.setText(tr("Undo"));
			tabButtonUndo.addActionListener(this);
			tabButtonUndo.setActionCommand("undo");
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
			tabButtonLoad.setFont(new Font("Dialog", Font.PLAIN, 12));
			tabButtonLoad.setText(tr("Load"));
			tabButtonLoad.addActionListener(this);
			tabButtonLoad.setActionCommand("load");
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
			tabButtonSave.setFont(new Font("Dialog", Font.PLAIN, 12));
			tabButtonSave.setText(tr("Save"));
			tabButtonSave.addActionListener(this);
			tabButtonSave.setActionCommand("save");
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
			tabButtonDelete.setFont(new Font("Dialog", Font.PLAIN, 12));
			tabButtonDelete.setText(tr("Delete"));
			tabButtonDelete.addActionListener(this);
			tabButtonDelete.setActionCommand("delete");
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
			tabButtonVisible.setFont(new Font("Dialog", Font.PLAIN, 12));
			tabButtonVisible.setText(tr("invisible"));
			tabButtonVisible.addActionListener(this);
			tabButtonVisible.setActionCommand("invisible");
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
			tabButtonAll.setText(tr("all"));
			tabButtonAll.addActionListener(this);
			tabButtonAll.setActionCommand("all");
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
			tabButtonNone.setText(tr("none"));
			tabButtonNone.addActionListener(this);
			tabButtonNone.setActionCommand("none");
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

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if(cmd.equals("ok")) {
			int i = 0;
			JTabbedPane tabbedPane = SmedTabbedPane.getTabbedPane();
			
			if(plugins != null) {
				for(SmedPluggable p : plugins) {
					String str = model.get(i).toString();

					if(str.length() > 9 && str.substring(0,9).equals("invisible")) { 
						splugDir.setVisible(p.getFileName(),false);
					} else splugDir.setVisible(p.getFileName(),true);
				
					if(str.length() > 6 && str.substring(0,6).equals("delete")) {
						splugDir.setDeleted(p.getFileName(),true);
					} else splugDir.setDeleted(p.getFileName(),false);
				
					i++;
				}
			
				tabbedPane.removeAll();
				JComponent panel = null;
				ImageIcon icon = null;
			
				for(SmedPluggable p : plugins) {
					if(splugDir.isVisible(p.getFileName()) && !splugDir.isDeleted(p.getFileName())) {
						panel = p.getComponent();
						icon = p.getIcon();

						tabbedPane.addTab(p.getName(),icon, panel, p.getInfo());
					}
				}
			}

			System.out.println("Aufraeumarbeiten beginnen");
			tabManagerDialog.setVisible(false);
			tabManagerDialog.dispose();
			return;
		}
		
		if(cmd.equals("cancel")) {
			tabManagerDialog.setVisible(false);
			tabManagerDialog.dispose();
			return;
		}
		
		if(cmd.equals("all")) {
			selModel.addSelectionInterval(0, modelSize - 1);
			return;
		}
		
		if(cmd.equals("none")) {
			selModel.removeSelectionInterval(0, modelSize - 1);
			return;
		}
		
		if(cmd.equals("invisible")) cmd("invisible - ");
		if(cmd.equals("delete")) cmd ("delete - ");
		if(cmd.equals("undo"))  cmd("");
	}

	private void cmd(String s) {
		int i = 0;
		
		if(plugins != null) {
			for(SmedPluggable p : plugins) { 
				if(selModel.isSelectedIndex(i)) model.set(i,s + p.getName());
				i++;
			}
		}
	}

}
