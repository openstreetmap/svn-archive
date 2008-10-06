import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.MissingResourceException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

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

	private JFrame frame = new JFrame();

	private Keylist panelSelect;

	Element workElement;

	private JTextField tf_elm_name = new JTextField("", 30);

	private JTextField tf_elm_osmKey = new JTextField("", 30);

	private JTextField tf_elm_osmValue = new JTextField("", 30);

	private JTextField tf_elm_filename = new JTextField("", 30);

	private JTextField tf_elm_image = new JTextField("", 30);

	private JTextField tf_elm_imagesize = new JTextField("", 30);

	private JTextField tf_elm_imageoffset = new JTextField("", 30);

	public MapFeaturesGUI(MapFeatures mapFeatures) {
		this.mapFeatures = mapFeatures;
		panelSelect = new Keylist(mapFeatures, this);
	}

	public void start() {
		try {
		updateFields();
		
		tf_elm_name.setToolTipText(osm2olt.bundle.getString("tool_tf_elm_name"));
		tf_elm_osmKey.setToolTipText(osm2olt.bundle.getString("tool_tf_elm_osmKey"));
		tf_elm_osmValue.setToolTipText(osm2olt.bundle.getString("tool_tf_elm_osmValue"));
		tf_elm_filename.setToolTipText(osm2olt.bundle.getString("tool_tf_elm_filename"));
		tf_elm_image.setToolTipText(osm2olt.bundle.getString("tool_tf_elm_image"));
		tf_elm_imagesize.setToolTipText(osm2olt.bundle.getString("tool_tf_elm_imagesize"));
		tf_elm_imageoffset.setToolTipText(osm2olt.bundle.getString("tool_tf_elm_imageoffset"));

		JButton btnSave = new JButton(osm2olt.bundle.getString("btn_Save_in_File"));
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
								osm2olt.bundle.getString("qu_File_overwrite"), osm2olt.bundle.getString("qu_head_File_overwrite"),
								JOptionPane.YES_NO_CANCEL_OPTION) == 0)
							mapFeatures.saveFile(file.getAbsolutePath());
					} else
						mapFeatures.saveFile(file.getAbsolutePath());
				}
			}
		});

		JButton btnOpen = new JButton(osm2olt.bundle.getString("btn_openFile"));
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.setCurrentDirectory(new File(Options.std_MF_file));
				fileChooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".xml")
								|| f.isDirectory();
					}

					public String getDescription() {
						return "XML Files (*.xml)";
					}
				});
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.exists()) {
						String mfFile = file.getAbsolutePath();
						mapFeatures.openFile(mfFile);
						updateFields();						
						if (osm2olt.useOption)
							Options.std_OSM_file = mfFile;
						if (osm2olt.useOption)
							Options.saveOptions();
					} else
						JOptionPane.showMessageDialog(frame,osm2olt.bundle.getString("err_correct_file"), "Error",
								JOptionPane.ERROR_MESSAGE);

				}

			}
		});

		JPanel paneControl = new JPanel();
		paneControl.add(btnSave);
		paneControl.add(btnOpen);

		JButton btnEditTitel = new JButton(osm2olt.bundle.getString("btn_edit_Titel"));
		btnEditTitel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (workElement != null) {
					EditMixed editMixed = new EditMixed();
					editMixed.start(workElement.getChild("titel"));
				}
			}
		});

		JButton btnEditDescription = new JButton(osm2olt.bundle.getString("btn_edit_Description"));
		btnEditDescription.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (workElement != null) {
					EditMixed editMixed = new EditMixed();
					editMixed.start(workElement.getChild("description"));
				}
			}
		});

		JButton btnEditFilter = new JButton(osm2olt.bundle.getString("btn_edit_Filter"));
		btnEditFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (workElement != null) {
					EditFilter editFilter = new EditFilter();
					editFilter.start(workElement.getChild("filter"));
				}
			}
		});

		JButton btnApply = new JButton(osm2olt.bundle.getString("btn_Apply"));
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (workElement != null) {
					workElement.setAttribute("name", tf_elm_name.getText());
					workElement.setAttribute("filename", tf_elm_filename
							.getText());
					workElement.setAttribute("image", tf_elm_image.getText());
					workElement.setAttribute("imageoffset", tf_elm_imageoffset
							.getText());
					workElement.setAttribute("imagesize", tf_elm_imagesize
							.getText());
					workElement.setAttribute("osmKey", tf_elm_osmKey.getText());
					workElement.setAttribute("osmValue", tf_elm_osmValue
							.getText());
				}
			}
		});

		frame.setLayout(new MigLayout("fill"));
		frame.setTitle(osm2olt.bundle.getString("MFGuiTitle"));

		frame.add(new JLabel(osm2olt.bundle.getString("lb_Filter_Name")), "");
		frame.add(tf_elm_name, "wrap");
		frame.add(new JLabel(osm2olt.bundle.getString("lb_osmkey")), "");
		frame.add(tf_elm_osmKey, "wrap");
		frame.add(new JLabel(osm2olt.bundle.getString("lb_osmvalue")), "");
		frame.add(tf_elm_osmValue, "wrap");
		frame.add(new JLabel(osm2olt.bundle.getString("lb_filename")), "");
		frame.add(tf_elm_filename, "wrap");
		frame.add(new JLabel(osm2olt.bundle.getString("lb_imagefile")), "");
		frame.add(tf_elm_image, "wrap");
		frame.add(new JLabel(osm2olt.bundle.getString("lb_imagesize")), "");
		frame.add(tf_elm_imagesize, "wrap");
		frame.add(new JLabel(osm2olt.bundle.getString("lb_imageoffset")), "");
		frame.add(tf_elm_imageoffset, "wrap");

		frame.add(new JLabel(osm2olt.bundle.getString("lb_editcomponents")), "");
		frame.add(btnEditTitel, "split 3");
		frame.add(btnEditDescription, "");
		frame.add(btnEditFilter, "wrap");
		frame.add(btnApply, "span 2, growx,wrap");

		//frame.add(new JLabel(" "), "");
		frame.add(paneControl, "span");
		frame.add(panelSelect, "west,grow");

		frame.pack();
		frame.setVisible(true);
		} catch (MissingResourceException e) {
			JOptionPane.showMessageDialog(frame,
					"Error in Languagefile.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

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
