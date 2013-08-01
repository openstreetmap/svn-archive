package pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * A plugin to import a PDF file.
 */
public class PdfImportPlugin extends Plugin {

	protected String name;

	public PdfImportPlugin(PluginInformation info) {
		super(info);
		name = tr("Import PDf file");
		MainMenu.add(Main.main.menu.dataMenu, new PdfImportAction());
	}
}
