package pdfimport.pdfbox;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.io.File;
import java.util.List;

import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.PDFStreamEngine;

import pdfimport.GraphicsProcessor;
import pdfimport.PathOptimizer;

public class PdfBoxParser extends PDFStreamEngine{
	private final PathOptimizer target;

	public PdfBoxParser(PathOptimizer target){
		this.target = target;
	}

	@SuppressWarnings("unchecked")
	public void parse(File file) throws Exception
	{
		PDDocument document = PDDocument.load( file);

		if( document.isEncrypted() ){
			throw new Exception(tr("Encrypted documents not supported."));
		}

		List allPages = document.getDocumentCatalog().getAllPages();

		if (allPages.size() != 1) {
			throw new Exception(tr("The PDF file must have exactly one page."));
		}

		PDPage page = (PDPage)allPages.get(0);
		PDRectangle pageSize = page.findMediaBox();
		Dimension pageDimension = pageSize.createDimension();

		GraphicsProcessor p = new GraphicsProcessor(target);
		PageDrawer drawer = new PageDrawer();
		drawer.drawPage(p, page, pageDimension);
	}
}
