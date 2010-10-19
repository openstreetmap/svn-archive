// License: GPL.
package pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.Page;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.tokens.FileFormatException;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;


import org.openstreetmap.josm.tools.Shortcut;

/**
 * Loads a PDF file into a new layer.
 *
 */
public class PdfImportAction extends JosmAction {

    public PdfImportAction() {
        super(tr("Import PDF file"), "pdf_import",
                tr("Import PDF file."), Shortcut.registerShortcut(
                        "tools:pdfimport", tr("Tool: {0}",
                                tr("Import PDF file")), KeyEvent.VK_P,
                        Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
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
