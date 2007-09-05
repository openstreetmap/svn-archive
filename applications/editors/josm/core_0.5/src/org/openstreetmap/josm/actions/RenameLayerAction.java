// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Action to rename an specific layer. Provides the option to rename the
 * file, this layer was loaded from as well (if it was loaded from a file).
 * 
 * @author Imi
 */
public class RenameLayerAction extends AbstractAction {

	private File file;
	private Layer layer;

	/**
	 * @param file The filen of the original location of this layer.
	 * 		If null, no possibility to "rename the file as well" is provided. 
	 */
	public RenameLayerAction(File file, Layer layer) {
		super(tr("Rename layer"), ImageProvider.get("dialogs", "edit"));
		this.file = file;
		this.layer = layer;
	}

	public void actionPerformed(ActionEvent e) {
		Box panel = Box.createVerticalBox();
		final JTextField name = new JTextField(layer.name);
		panel.add(name);
		JCheckBox filerename = new JCheckBox(tr("Also rename the file"));
		if (Main.applet) {
			filerename.setEnabled(false);
			filerename.setSelected(false);
		} else {
			panel.add(filerename);
			filerename.setEnabled(file != null);
		}
		if (filerename.isEnabled())
			filerename.setSelected(Main.pref.getBoolean("layer.rename-file", true));

		final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION){
			@Override public void selectInitialValue() {
				name.requestFocusInWindow();
				name.selectAll();
			}
		};
		final JDialog dlg = optionPane.createDialog(Main.parent, tr("Rename layer"));
		dlg.setVisible(true);

		Object answer = optionPane.getValue();
		if (answer == null || answer == JOptionPane.UNINITIALIZED_VALUE ||
				(answer instanceof Integer && (Integer)answer != JOptionPane.OK_OPTION)) {
			return;
		}

		String nameText = name.getText();
		if (filerename.isEnabled()) {
			Main.pref.put("layer.rename-file", filerename.isSelected());
			if (filerename.isSelected()) {
				String newname = nameText;
				if (newname.indexOf("/") == -1 && newname.indexOf("\\") == -1)
					newname = file.getParent() + File.separator + newname;
				String oldname = file.getName();
				if (name.getText().indexOf('.') == -1 && oldname.indexOf('.') >= 0)
					newname += oldname.substring(oldname.lastIndexOf('.'));
				File newFile = new File(newname);
				if (file.renameTo(newFile)) {
					layer.associatedFile = newFile;
					nameText = newFile.getName();
				} else {
					JOptionPane.showMessageDialog(Main.parent, tr("Could not rename the file \"{0}\".", file.getPath()));
					return;
				}
			}
		}
		layer.name = nameText;
		Main.parent.repaint();
	}
}
