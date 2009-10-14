package org.openstreetmap.osmolt.gui;
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
import org.openstreetmap.osmolt.*;
/**
 * this is no longer needed
 * @author josias
 *
 */
public class MapFeaturesGUI implements MFGuiAccess{
	/** 
	 * 
	 */

	MapFeatures mapFeatures = MapFeatures.mapFeatures;

	OsmoltGui mainframe = new OsmoltGui();

	final JFileChooser fileChooser = OsmoltGui.fileChooser;

	ArrayList<String> names = new ArrayList<String>();

	private static final long serialVersionUID = 1L;

	private JFrame frame = new JFrame();

	private MFFilterlist panelSelect;

	Element workElement;

	private JTextField tf_elm_name = new JTextField("", 30);

	private JTextField tf_elm_osmKey = new JTextField("", 30);

	private JTextField tf_elm_osmValue = new JTextField("", 30);

	private JTextField tf_elm_filename = new JTextField("", 30);

	private JTextField tf_elm_image = new JTextField("", 30);

	private JTextField tf_elm_imagesize = new JTextField("", 30);

	private JTextField tf_elm_imageoffset = new JTextField("", 30);

	JButton btnApply;

	JButton btnEditTitel;

	JButton btnEditDescription;

	JButton btnEditFilter;

	public MapFeaturesGUI(MapFeatures mapFeatures) {
		this.mapFeatures = mapFeatures;
		panelSelect = new MFFilterlist(mapFeatures, this);
	}

	public void start() {
		try {
			updateFields();

			tf_elm_name.setToolTipText(OsmoltGui.bundle
					.getString("tool_tf_elm_name"));
			tf_elm_osmKey.setToolTipText(OsmoltGui.bundle
					.getString("tool_tf_elm_osmKey"));
			tf_elm_osmValue.setToolTipText(OsmoltGui.bundle
					.getString("tool_tf_elm_osmValue"));
			tf_elm_filename.setToolTipText(OsmoltGui.bundle
					.getString("tool_tf_elm_filename"));
			tf_elm_image.setToolTipText(OsmoltGui.bundle
					.getString("tool_tf_elm_image"));
			tf_elm_imagesize.setToolTipText(OsmoltGui.bundle
					.getString("tool_tf_elm_imagesize"));
			tf_elm_imageoffset.setToolTipText(OsmoltGui.bundle
					.getString("tool_tf_elm_imageoffset"));

			JButton btnSave = new JButton(OsmoltGui.bundle
					.getString("btn_Save_in_File"));
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fileChooser.setCurrentDirectory(new File(
							Options.std_MF_file));
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
							if (JOptionPane
									.showConfirmDialog(
											null,
											OsmoltGui.bundle
													.getString("qu_File_overwrite"),
													OsmoltGui.bundle
													.getString("qu_head_File_overwrite"),
											JOptionPane.YES_NO_CANCEL_OPTION) == 0)
								mapFeatures.saveFile(file.getAbsolutePath());

							if (OsmoltGui.useOption)
								Options.std_MF_file = file.getAbsolutePath();
							if (OsmoltGui.useOption)
								Options.saveOptions();
						} else
							mapFeatures.saveFile(file.getAbsolutePath());
						if (OsmoltGui.useOption)
							Options.std_MF_file = file.getAbsolutePath();
						if (OsmoltGui.useOption)
							Options.saveOptions();
					}
				}
			});

			JButton btnOpen = new JButton(OsmoltGui.bundle
					.getString("btn_openFile"));
			btnOpen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fileChooser.setCurrentDirectory(new File(
							Options.std_MF_file));
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
							if (OsmoltGui.useOption)
								Options.std_MF_file = mfFile;
							if (OsmoltGui.useOption)
								Options.saveOptions();

							mapFeatures.updatesknowenTyes();
							panelSelect.updateGui();
						} else
							JOptionPane.showMessageDialog(frame, OsmoltGui.bundle
									.getString("err_correct_file"), "Error",
									JOptionPane.ERROR_MESSAGE);

					}

				}
			});

			JPanel paneControl = new JPanel();
			paneControl.add(btnSave);
			paneControl.add(btnOpen);

			btnEditTitel = new JButton(OsmoltGui.bundle
					.getString("btn_edit_Titel"));
			btnEditTitel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (workElement != null) {
						MFEditMixed editMixed = new MFEditMixed(null, mapFeatures, mainframe);
						editMixed.updateElement(workElement.getChild("titel"));
					}
				}
			});

			btnEditDescription = new JButton(OsmoltGui.bundle
					.getString("btn_edit_Description"));
			btnEditDescription.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (workElement != null) {
						MFEditMixed editMixed = new MFEditMixed(null, mapFeatures, mainframe);
						editMixed.updateElement(workElement.getChild("description"));
					}
				}
			});

			btnEditFilter = new JButton(OsmoltGui.bundle
					.getString("btn_edit_Filter"));
			btnEditFilter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (workElement != null) {
						MFEditFilter editFilter = new MFEditFilter(mapFeatures, mainframe);
						editFilter.updateElement(workElement.getChild("filter"));
					}
				}
			});

			btnApply = new JButton(OsmoltGui.bundle.getString("btn_Apply"));
			btnApply.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (workElement != null) {
						workElement.setAttribute("name", tf_elm_name.getText());
						workElement.setAttribute("filename", tf_elm_filename
								.getText());
						workElement.setAttribute("image", tf_elm_image
								.getText());
						workElement.setAttribute("imageoffset",
								tf_elm_imageoffset.getText());
						workElement.setAttribute("imagesize", tf_elm_imagesize
								.getText());
						workElement.setAttribute("osmKey", tf_elm_osmKey
								.getText());
						workElement.setAttribute("osmValue", tf_elm_osmValue
								.getText());
						mapFeatures.updatesknowenTyes();
						panelSelect.updateGui();
					}
				}
			});

			frame.setLayout(new MigLayout("fill"));
			frame.setTitle(OsmoltGui.bundle.getString("MFGuiTitle"));

			frame.add(new JLabel(OsmoltGui.bundle.getString("lb_Filter_Name")),
					"");
			frame.add(tf_elm_name, "wrap");
//			frame.add(new JLabel(OsmoltGui.bundle.getString("lb_osmkey")), "");
//			frame.add(tf_elm_osmKey, "wrap");
//			frame.add(new JLabel(OsmoltGui.bundle.getString("lb_osmvalue")), "");
//			frame.add(tf_elm_osmValue, "wrap");
			frame.add(new JLabel(OsmoltGui.bundle.getString("lb_filename")), "");
			frame.add(tf_elm_filename, "wrap");
			frame.add(new JLabel(OsmoltGui.bundle.getString("lb_imagefile")), "");
			frame.add(tf_elm_image, "wrap");
			frame.add(new JLabel(OsmoltGui.bundle.getString("lb_imagesize")), "");
			frame.add(tf_elm_imagesize, "wrap");
			frame.add(new JLabel(OsmoltGui.bundle.getString("lb_imageoffset")),
					"");
			frame.add(tf_elm_imageoffset, "wrap");

			frame.add(
					new JLabel(OsmoltGui.bundle.getString("lb_editcomponents")),
					"");
			frame.add(btnEditTitel, "split 3");
			frame.add(btnEditDescription, "");
			frame.add(btnEditFilter, "wrap");
			frame.add(btnApply, "span 2, growx,wrap");

			// frame.add(new JLabel(" "), "");
			frame.add(paneControl, "span");
			frame.add(panelSelect, "west,grow");

			frame.pack();
			frame.setVisible(true);
			updateFields();
		} catch (MissingResourceException e) {
			JOptionPane.showMessageDialog(frame, "Error in Languagefile.",
					"Error", JOptionPane.ERROR_MESSAGE);
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
		
		tf_elm_name.setEnabled(workElement != null);
		tf_elm_filename.setEnabled(workElement != null);
		tf_elm_image.setEnabled(workElement != null);
		tf_elm_imageoffset.setEnabled(workElement != null);
		tf_elm_imagesize.setEnabled(workElement != null);
		tf_elm_osmKey.setEnabled(workElement != null);
		tf_elm_osmValue.setEnabled(workElement != null);
		if(btnApply!=null) btnApply.setEnabled(workElement != null);
		if(btnEditTitel!=null) btnEditTitel.setEnabled(workElement != null);
		if(btnEditDescription!=null) btnEditDescription.setEnabled(workElement != null);
		if(btnEditFilter!=null) btnEditFilter.setEnabled(workElement != null);
		panelSelect.updateGui();
	}

	public void setWorkFilter(Element workElement) {
		this.workElement = workElement;
		updateFields();
	}

	public String getLookAndFeelClassName() {
		// TODO Automatisch erstellter Methoden-Stub
		return null;
	}

	public String translate(String s) {
		// TODO Automatisch erstellter Methoden-Stub
		return null;
	}

	public void printError(String error) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void printMessage(String message) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void printTranslatedError(String error) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void printTranslatedMessage(String message) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void printTranslatedWarning(String warning) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void printWarning(String warning) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void processAdd() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void processStart() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void processStop() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void loadFilter() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public Element getWorkFilter() {
		// TODO Automatisch erstellter Methoden-Stub
		return null;
	}

	public void updateGui() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void applyChanges() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void processSetName(String s) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void osmoltEnd() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void osmoltStart() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void processSetPercent(int percent) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void processSetStatus(String s) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

  public void printError(Throwable error) {
    // TODO Auto-generated method stub
    
  }

  public void printDebugMessage(String classname, String message) {
    // TODO Auto-generated method stub
    
  }

}
