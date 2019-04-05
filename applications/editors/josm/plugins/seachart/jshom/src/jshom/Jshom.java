/**
 * @author Malcolm Herring
 */
// License: GPL. For details, see LICENSE file.
package jshom;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import s57.S57obj;
import s57.S57obj.*;
import s57.S57att;
import s57.S57att.*;
import s57.S57val;

public final class Jshom {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java -jar jshom.jar <seamark type> <input file> <output file>");
            System.exit(-1);
        }
        String type = args[0];
        String srcfile = args[1];
        String dstfile = args[2];

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(srcfile);

            NodeList nList = doc.getElementsByTagName("node");
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList tList = node.getChildNodes();
                    for (int j = 0; j < tList.getLength(); j++) {
                        Node tag = tList.item(j);
                        if (tag.getNodeType() == tag.ELEMENT_NODE) {
                            Element tElement = (Element) tag;
                            String key = tElement.getAttribute("k");
                            String val = tElement.getAttribute("v");
                            try {
                                Obj obj = Obj.valueOf(key.toUpperCase());
                                if (!val.equals("0")) {
                                    tElement.setAttribute("k", "seamark:" + S57obj.stringType(obj));
                                    tElement.setAttribute("v", "yes");
                                } else {
                                    node.removeChild(tag);                                
                                }
                                continue;
                            } catch (Exception eObj) {
                                try {
                                    Att att = Att.valueOf(key.toUpperCase());
                                    tElement.setAttribute("k", "seamark:" + type + ":" + S57att.stringAttribute(att));
                                    tElement.setAttribute("v", S57val.stringValue(S57val.decodeValue(val, att), att));
                                    continue;
                                } catch (Exception eAtt) {
                                    node.removeChild(tag);                                
                                }
                            }
                        }
                    }
                    Element newTag = doc.createElement("tag");
                    newTag.setAttribute("k", "seamark:type");
                    newTag.setAttribute("v", type);
                    node.appendChild(newTag);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(dstfile));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
