// License: GPL.
package pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Loads a PDF file into a new layer.
 *
 */
public class PdfImportAction extends JosmAction {

	public PdfImportAction() {
		super(tr("Import PDF file"), "pdf_import",
		    tr("Import PDF file."), 
		    Shortcut.registerShortcut("tools:pdfimport", tr("Tool: {0}",tr("Import PDF file")),
		    KeyEvent.VK_F, Shortcut.ALT_CTRL_SHIFT)
		    , true);
	}

	/**
	 * The action button has been clicked
	 *
	 * @param e
	 *            Action Event
	 */
	public void actionPerformed(ActionEvent e) {

		//show dialog asking to select coordinate axes and input coordinates and projection.
		LoadPdfDialog dialog = new LoadPdfDialog();
		dialog.setAlwaysOnTop(true);
		dialog.setTitle(tr("Import PDF"));
		dialog.setVisible(true);
	}
}
