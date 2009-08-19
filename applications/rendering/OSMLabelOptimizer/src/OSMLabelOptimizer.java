import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.openstreetmap.labelling.annealing.AnnealingMaterial;
import org.openstreetmap.labelling.annealing.AnnealingOven;
import org.openstreetmap.labelling.osm.OSMMapFactory;
import org.openstreetmap.preprocessing.osm.OSMPreprocessors;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 * 
 */

/**
 * @author sebi
 *
 */
public class OSMLabelOptimizer
{
    public static void main(String[] args)
    {
        try
        {
        SVGDocument svgDoc = readSVGDocument(args[0]);
        BatikContext bat = bootBatik(svgDoc);
        doLabelOptimization(bat, svgDoc);
        saveDocument(svgDoc, args[1]);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    private static SVGDocument readSVGDocument(String filename) throws IOException
    {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        File file = new File(filename);
        Document doc = f.createDocument(file.toURI().toString());
        SVGDocument svgDoc = (SVGDocument)doc;
        
        return svgDoc;
    }
    
    private static BatikContext bootBatik(SVGDocument svgDoc)
    {
        BatikContext bat = new BatikContext();
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        bat.ctx = new BridgeContext(userAgent, loader);
        bat.ctx.setDynamicState(BridgeContext.DYNAMIC);
        bat.builder = new GVTBuilder();
        bat.rootGN = (GraphicsNode)bat.builder.build(bat.ctx, svgDoc);
        bat.up = new UpdateManager(bat.ctx, bat.rootGN, svgDoc);
        bat.up.resume();
        return bat;
    }
    
    private static void doLabelOptimization(final BatikContext bat, final SVGDocument svgDoc)
    {
        OSMPreprocessors p = new OSMPreprocessors(svgDoc, bat.ctx.getUpdateManager());
        p.preprocessAll();
        System.out.println("Preprocessing finished!");
        AnnealingOven oven = new AnnealingOven();
        OSMMapFactory fac = new OSMMapFactory(svgDoc, bat.ctx.getUpdateManager());
        oven.setAnnealingMaterial(new AnnealingMaterial(fac.getMap(), fac.getEvaluation()));
        oven.anneal();
        fac.render();
    }
    
    private static void saveDocument(SVGDocument svgDoc, String outputFilename)
    {
        File f = new File(outputFilename);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource        source = new DOMSource(svgDoc);
            FileOutputStream os     = new FileOutputStream(f);
            StreamResult     result = new StreamResult(os);
            transformer.transform( source, result );
        } 
        catch (TransformerConfigurationException ex) 
        {
            ex.printStackTrace();
        } 
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        } 
        catch (TransformerException ex)
        {
            ex.printStackTrace();
        }
    }
    
    private static class BatikContext
    {
        public BridgeContext ctx;
        public GVTBuilder builder;
        public GraphicsNode rootGN;
        public UpdateManager up;
    }
}
