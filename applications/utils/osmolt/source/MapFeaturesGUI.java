import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.jdom.Element;

public class MapFeaturesGUI {
	/**
	 * 
	 */

	MapFeatures mapFeatures;

	osm2olt mainframe = new osm2olt();

	final JFileChooser fileChooser = mainframe.getFileChooser();

	ArrayList<String> names = new ArrayList<String>();

	private static final long serialVersionUID = 1L;

	private JFrame fr_edit = new JFrame();

	private JFrame frame = new JFrame();

	private Keylist panelSelect;

	private JComboBox listNames = new JComboBox();

	private JComboBox cb_Name = new JComboBox();

	Element workElement;

	private JTextField test = new JTextField("re", 30);

	private JTextField tf_elm_name = new JTextField("", 30);

	private JTextField tf_elm_osmKey = new JTextField("", 30);

	private JTextField tf_elm_osmValue = new JTextField("", 30);

	private JTextField tf_elm_filename = new JTextField("", 30);

	private JTextField tf_elm_image = new JTextField("", 30);

	private JTextField tf_elm_imagesize = new JTextField("", 30);

	private JTextField tf_elm_imageoffset = new JTextField("", 30);

	public MapFeaturesGUI(MapFeatures mapFeatures) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// Unschalten des Look & Feels für das Applikations-Frame
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.mapFeatures = mapFeatures;
		panelSelect = new Keylist(mapFeatures,this);
	}

	public void start() {
		updateFields();

		JButton btnRemoveElement = new JButton("remove");
		JButton btnAddElement = new JButton("add");
		btnAddElement.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = JOptionPane.showInputDialog(null, new String(
						"Name"));
				if ((name != null) && (!name.equals("")))
					workElement = mapFeatures.addEntry(name);
				updateFields();
			}
		});

		JPanel paneControl = new JPanel();

		
		
		
		
		JButton btnSave = new JButton("Save in File");
		paneControl.add(btnSave);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".xml")
								|| f.isDirectory();
					}

					public String getDescription() {
						return "Filter (*.xml)";
					}
				});
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.exists()) {
						if (JOptionPane.showConfirmDialog(null,
								"File overwrite?", "overwrite",
								JOptionPane.YES_NO_CANCEL_OPTION) == 0)
							mapFeatures.saveFile(file.getAbsolutePath());
					} else
						mapFeatures.saveFile(file.getAbsolutePath());
				}
			}
		});
		JButton btnApply = new JButton("Apply");
		paneControl.add(btnApply);
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (workElement != null){
					workElement.setAttribute("name", tf_elm_name.getText());
					workElement.setAttribute("filename", tf_elm_filename.getText());
					workElement.setAttribute("image", tf_elm_image.getText());
					workElement.setAttribute("imageoffset", tf_elm_imageoffset.getText());
					workElement.setAttribute("imagesize", tf_elm_imagesize.getText());
					workElement.setAttribute("osmKey", tf_elm_osmKey.getText());
					workElement.setAttribute("osmValue", tf_elm_osmValue.getText());
				}
			}
		});
		
		
		
		

		JPanel paneEdit_elm = new JPanel();
		paneEdit_elm.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		paneEdit_elm.add(new JLabel("name"), c);
		c.gridx = 1;
		c.gridy = 0;
		paneEdit_elm.add(tf_elm_name, c);
		c.gridx = 0;
		c.gridy = 1;
		paneEdit_elm.add(new JLabel("osmKey"), c);
		c.gridx = 1;
		c.gridy = 1;
		paneEdit_elm.add(tf_elm_osmKey, c);
		c.gridx = 0;
		c.gridy = 2;
		paneEdit_elm.add(new JLabel("osmValue"), c);
		c.gridx = 1;
		c.gridy = 2;
		paneEdit_elm.add(tf_elm_osmValue, c);
		c.gridx = 0;
		c.gridy = 3;
		paneEdit_elm.add(new JLabel("filename"), c);
		c.gridx = 1;
		c.gridy = 3;
		paneEdit_elm.add(tf_elm_filename, c);
		c.gridx = 0;
		c.gridy = 4;
		paneEdit_elm.add(new JLabel("image"), c);
		c.gridx = 1;
		c.gridy = 4;
		paneEdit_elm.add(tf_elm_image, c);
		c.gridx = 0;
		c.gridy = 5;
		paneEdit_elm.add(new JLabel("imagesize"), c);
		c.gridx = 1;
		c.gridy = 5;
		paneEdit_elm.add(tf_elm_imagesize, c);
		c.gridx = 0;
		c.gridy = 6;
		paneEdit_elm.add(new JLabel("imageoffset"), c);
		c.gridx = 1;
		c.gridy = 6;
		paneEdit_elm.add(tf_elm_imageoffset, c);
		c.gridx = 1;
		c.gridy = 6;
		paneEdit_elm.add(tf_elm_imageoffset, c);

		
		

		JButton btnEditTitel = new JButton("edit Titel");
		btnEditTitel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (workElement != null){
					EditMixed editMixed = new EditMixed();
					editMixed.start(workElement.getChild("titel"));
				}
			}
		});

		c.gridx = 1;
		c.gridy = 7;
		paneEdit_elm.add(btnEditTitel, c);
		
		
		JButton btnEditDescription = new JButton("edit Description");
		btnEditDescription.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (workElement != null){
					EditMixed editMixed = new EditMixed();
					editMixed.start(workElement.getChild("description"));
				}
			}
		});

		c.gridx = 1;
		c.gridy = 8;
		paneEdit_elm.add(btnEditDescription, c);
		
		

		JButton btnEditFilter = new JButton("edit Filter");
		btnEditFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (workElement != null){
					EditFilter editFilter = new EditFilter();
					editFilter.start(workElement.getChild("filter"));
				}
			}
		});

		c.gridx = 1;
		c.gridy = 9;
		paneEdit_elm.add(btnEditFilter, c);
		
		
		
		JPanel paneEdit = new JPanel();
		paneEdit.add(paneEdit_elm);

		frame.setLayout(new BorderLayout());
		frame.setTitle("Client");

		frame.add(panelSelect, BorderLayout.WEST);
		frame.add(paneControl, BorderLayout.SOUTH);
		frame.add(paneEdit, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);

	}

	private void updateFields() {
		if (workElement != null) {
			tf_elm_name.setText(workElement.getAttributeValue("name"));
			tf_elm_filename.setText(workElement.getAttributeValue("filename"));
			tf_elm_image.setText(workElement.getAttributeValue("image"));
			tf_elm_imageoffset.setText(workElement
					.getAttributeValue("imageoffset"));
			tf_elm_imagesize
					.setText(workElement.getAttributeValue("imagesize"));
			tf_elm_osmKey.setText(workElement.getAttributeValue("osmKey"));
			tf_elm_osmValue.setText(workElement.getAttributeValue("osmValue"));
		}
		tf_elm_name.setEditable(workElement != null);
		tf_elm_filename.setEditable(workElement != null);
		tf_elm_image.setEditable(workElement != null);
		tf_elm_imageoffset.setEditable(workElement != null);
		tf_elm_imagesize.setEditable(workElement != null);
		tf_elm_osmKey.setEditable(workElement != null);
		tf_elm_osmValue.setEditable(workElement != null);
		panelSelect.updateList();
	}

	public void setWorkElement(Element workElement) {
		this.workElement = workElement;
		updateFields();
	}

}
