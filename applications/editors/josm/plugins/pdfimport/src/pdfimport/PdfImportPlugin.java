// License: GPL. For details, see LICENSE file.
package pdfimport;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * A plugin to import a PDF file.
 */
public class PdfImportPlugin extends Plugin {

    public PdfImportPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().imagerySubMenu, new PdfImportAction());
    }
}
