import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPath;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

public class Test
{
	public static void main(String[] args)
	{
		SVGDocument    svgDoc;
		UserAgent      userAgent;
		DocumentLoader loader;
		BridgeContext  ctx;
		GVTBuilder     builder;
		RootGraphicsNode   rootGN;

		try {
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			String uri = "file:///home/sebi/tmp/test.svg";
			Document doc = f.createDocument(uri);
			svgDoc = (SVGDocument)doc;


			userAgent = new UserAgentAdapter();
			loader    = new DocumentLoader(userAgent);
			ctx       = new BridgeContext(userAgent, loader);
			ctx.setDynamicState(BridgeContext.DYNAMIC);
			builder   = new GVTBuilder();
			rootGN    = (RootGraphicsNode)builder.build(ctx, svgDoc);

			SVGSVGElement svgRoot = svgDoc.getRootElement();
			Element text = svgRoot.getElementById("text2170");
			System.out.println(text.getClass());
			GraphicsNode textN = ctx.getGraphicsNode(text);
			System.out.println(textN.getClass());
			TextNode textN2 = (TextNode)textN;
            System.out.println(textN2.getOutline());
            AttributedCharacterIterator ai = textN2.getAttributedCharacterIterator();
            Map<GVTAttributedCharacterIterator.Attribute, Object> map = ai.getAttributes();
            TextPath path = (TextPath)map.get(GVTAttributedCharacterIterator.TextAttribute.TEXTPATH);
            System.out.println(path.pointAtLength(new Float(0)));
            System.out.println(path.pointAtLength(new Float(1 * path.lengthOfPath())));
            for (Map.Entry<GVTAttributedCharacterIterator.Attribute, Object> entry : map.entrySet())
            {
                System.out.printf("%s:%s", entry.getKey(), entry.getValue());
                System.out.println();
            }
			System.out.println();
		} catch (IOException ex) {
			System.out.println(ex);
		}

	}
}
