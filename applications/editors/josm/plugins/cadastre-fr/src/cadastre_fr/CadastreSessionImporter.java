package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.GpxImporter;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.session.SessionLayerImporter;
import org.openstreetmap.josm.io.session.SessionReader.ImportSupport;
import org.w3c.dom.Element;

public class CadastreSessionImporter implements SessionLayerImporter{

	@Override
	public Layer load(Element elem, ImportSupport support,
			ProgressMonitor progressMonitor) throws IOException,
			IllegalDataException {
        String version = elem.getAttribute("version");
        if (!"0.1".equals(version)) {
            throw new IllegalDataException(tr("Version ''{0}'' of meta data for imagery layer is not supported. Expected: 0.1", version));
        }
        try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            XPathExpression fileExp = xpath.compile("file/text()");
            String fileStr = (String) fileExp.evaluate(elem, XPathConstants.STRING);
            if (fileStr == null || fileStr.isEmpty()) {
                throw new IllegalDataException(tr("File name expected for layer no. {0}", support.getLayerIndex()));
            }

            fileStr = URLDecoder.decode(fileStr, "UTF-8");
            fileStr = fileStr.substring(fileStr.indexOf(":/")+2);
    		String filename = fileStr.substring(fileStr.lastIndexOf("/")+1,fileStr.length());
            String ext = (filename.lastIndexOf(".")==-1)?"":filename.substring(filename.lastIndexOf(".")+1,filename.length());
            // create layer and load cache
            if (ext.length() == 3 && ext.substring(0, CacheControl.cLambertCC9Z.length()).equals(CacheControl.cLambertCC9Z))
                ext = ext.substring(2);
            else if (ext.length() == 4 && ext.substring(0, CacheControl.cUTM20N.length()).equals(CacheControl.cUTM20N))
                ext = ext.substring(3);
            else if (ext.length() == 2 || ext.length() > 4)
                throw new IllegalDataException(tr("Unexpected file extension. {0}", ext));

            int layoutZone = Integer.parseInt(ext)-1;
            WMSLayer wmsLayer = new WMSLayer("", "", layoutZone);
            File file = new File(fileStr);
            wmsLayer.grabThread.getCacheControl().loadCache(file, layoutZone);
            return wmsLayer;

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
	}

}
