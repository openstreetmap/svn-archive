import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import javax.imageio.ImageIO;
//import com.sun.image.codec.jpeg.*;
import java.awt.image.*;
import org.apache.tools.bzip2.CBZip2InputStream;

final class config {
    Document doc = null;
    String current_name;

    config() {
	//System.out.println("config constructor");
    }

    private void dumpChild(final Node root, int level) {
	int i;
	String indent="";
		
	for (i=0; i < level; i++) {
	    indent = indent + " ";
	}
	
	if (root != null) {
	    if (root.getNodeName() != "#text") {
		System.out.println(indent+"CONFIG NODE:"+ root.getNodeName());
		if (root.getNodeValue() != null) {
		    System.out.println(indent+"CONFIG NODEV:"+ root.getNodeValue());
		}
	    }
	    NamedNodeMap nnm = root.getAttributes();
	    if (nnm != null) {
		for (i = 0; i <nnm.getLength(); i++) {
		    dumpChild(nnm.item(i), level+1);
		}
	    }

	    for (Node child = root.getFirstChild(); child != null;
		 child = child.getNextSibling()) {
		dumpChild(child, level+2);
	    }		
	}
    }
    
    private int ckn(final Node root, int level, String searchstring) {
	int i;
	String indent="";
	int ret=0;

	for (i=0; i < level; i++) {
	    indent = indent + " ";
	}
	
	if (root != null) {
	    if (root.getNodeName().compareTo("name")==0) {
		//System.out.println(indent+"CONFIG NODE:"+ root.getNodeName());
		//System.out.println("NAME root test:" + searchstring + ":"+root.getNodeValue());
		if (root.getNodeValue().compareTo(searchstring)==0) {
		    return level;
		    //System.out.println(indent+"CONFIG NODEV:"+ root.getNodeValue());
		}
	    }
	    NamedNodeMap nnm = root.getAttributes();
	    if (nnm != null) {
		for (i = 0; i <nnm.getLength(); i++) {
		    ret=ckn(nnm.item(i), level+1, searchstring);
		    if (ret!=0) {
			return ret;
		    }
		}
	    }

	    for (Node child = root.getFirstChild(); child != null;
		 child = child.getNextSibling()) {
		ret=ckn(child, level+2, searchstring);
		if(ret!=0) {
		    return ret;
		}
	    }		
	}
	return 0;
    }

    private String vkn(final Node root, int level, String searchstring) {
	int i;
	String indent="";
	String ret=null;
	String name;

	if (root != null) {
	    if (root.getNodeName().compareTo("name")==0) {
		if (level == 1) {
		    current_name = root.getNodeValue();
		}
		if (level == 3) {
		    if (root.getNodeValue().compareTo(searchstring)==0) {
			return current_name;
		    }
		}
	    }
	    NamedNodeMap nnm = root.getAttributes();
	    if (nnm != null) {
		for (i = 0; i <nnm.getLength(); i++) {
		    ret=vkn(nnm.item(i), level+1, searchstring);
		    if (ret!=null) {
			return ret;
		    }
		}
	    }

	    for (Node child = root.getFirstChild(); child != null;
		 child = child.getNextSibling()) {
		ret=vkn(child, level+2, searchstring);
		if(ret!=null) {
		    return ret;
		}
	    }		
	}
	return null;
    }

    /** 
     * This method.
     *
     * returns
     *   0 = unknown
     *   1 = valid
     *   2 = bad spelling, known misspelling
     */
    public int check_key_name(String name) {
	int ret=0;

	final Node root = doc.getFirstChild();
	//System.err.println("CONFIG.LOAD10");
	for (Node child = root.getFirstChild(); child != null;
             child = child.getNextSibling()) {
	    //System.err.println("CONFIG.LOAD11");
	    ret = ckn(child, 0, name);
	    if (ret!=0) {
		break;
	    }
	}
	if (ret == 1) return 1;
	else if (ret == 3) return 2;
	else return 0;
    }

    /** 
     * This method.
     *
     * returns a properly spelled key except if name is already correctly
     * spelled or if name is not known
     */
    public String valid_key_name(String name) {
	String ret="";

	final Node root = doc.getFirstChild();
	//System.err.println("CONFIG.LOAD10");
	for (Node child = root.getFirstChild(); child != null;
             child = child.getNextSibling()) {
	    //System.err.println("CONFIG.LOAD11");
	    ret = vkn(child, 0, name);
	    if (ret!=null) {
		break;
	    }
	}
	return ret;
    }

    /** 
     * This method.
     *
     */
    public void dump() {
	System.out.println("--->>  BEGIN config DUMP  <<---");
	
 	final Node root = doc.getFirstChild();
 	//System.err.println("CONFIG.LOAD10");
 	for (Node child = root.getFirstChild(); child != null;
              child = child.getNextSibling()) {
 	    //System.err.println("CONFIG.LOAD11");
 	    dumpChild(child, 0);
 	}
	System.out.println("highway = "+ check_key_name("highway"));
	System.out.println("higway = "+ check_key_name("higway"));
	System.out.println("highwau = "+ check_key_name("highwau"));
	System.out.println("created_by = "+ check_key_name("created_by"));
	System.out.println("creayed_by = "+ check_key_name("creayed_by"));
	System.out.println("higway1 = "+ check_key_name(" higway"));
	System.out.println("higway2 = "+ check_key_name("higway "));

	System.out.println("highway = "+ valid_key_name("highway"));
	System.out.println("higway = "+ valid_key_name("higway"));
	System.out.println("highwau = "+ valid_key_name("highwau"));
	System.out.println("created_by = "+ valid_key_name("created_by"));
	System.out.println("creayed_by = "+ valid_key_name("creayed_by"));
	System.out.println("higway1 = "+ valid_key_name(" higway"));
	System.out.println("higway2 = "+ valid_key_name("higway "));
	
	System.out.println("--->>  END DUMP  <<---");
    }

    /** 
     * This method.
     *
     * @param filename String
     */
    public void load(String filename) {
	File f = new File(filename);
	if (f != null) {
	    load_DOM(f);
	}
    }


    /** 
     * This method.
     *
     * @param file File
     */
    public void load_DOM(final File file) {
        // Step 1: create a DocumentBuilderFactory and configure it
        final DocumentBuilderFactory dbf =
            DocumentBuilderFactory.newInstance();
	
        // Optional: set various configuration options
        //dbf.setValidating(validation);
        //dbf.setIgnoringComments(ignoreComments);
        //dbf.setIgnoringElementContentWhitespace(true);
        //dbf.setCoalescing(putCDATAIntoText);
        // The opposite of creating entity ref nodes is expanding them inline
        //dbf.setExpandEntityReferences(!createEntityRefs);

        // At this point the DocumentBuilderFactory instance can be saved
        // and reused to create any number of DocumentBuilder instances
        // with the same configuration options.

        // Step 2: create a DocumentBuilder that satisfies the constraints
        // specified by the DocumentBuilderFactory
        DocumentBuilder db = null;
	//System.err.println("config.LOAD1");
        try {
            db = dbf.newDocumentBuilder();
	    //System.err.println("config.LOAD2");
        } catch (ParserConfigurationException pce) {
	    System.err.println("config.LOAD3");
            System.err.println(pce);
            System.exit(1);
        }
	//System.err.println("config.LOAD4");

        // Set an ErrorHandler before parsing
        //OutputStreamWriter errorWriter =
        //    new OutputStreamWriter(System.err, outputEncoding);
        //db.setErrorHandler(
        //    new MyErrorHandler(new PrintWriter(errorWriter, true)));

        // Step 3: parse the input file
	//System.err.println("config.LOAD5");
        try {
            doc = db.parse(file);
	    //System.err.println("config.LOAD6");
        } catch (SAXException se) {
	    System.err.println("CONFIG.LOAD7");
            System.err.println(se.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
	    System.err.println("CONFIG.LOAD8");
            System.err.println(ioe);
            System.exit(1);
        }
	//System.err.println("CONFIG.LOAD9");

// 	final Node root = doc.getFirstChild();
// 	System.err.println("CONFIG.LOAD10");
// 	for (Node child = root.getFirstChild(); child != null;
//              child = child.getNextSibling()) {
// 	    System.err.println("CONFIG.LOAD11");
// 	    if(child.getNodeName().compareToIgnoreCase("Accounts") == 0)
// 		loadAccounts(child);
// 	}
    }

    /** 
     * This method.
     *
     * @param root Node
     */
    private void loadAccounts(final Node root) {
	for (Node child = root.getFirstChild(); child != null;
             child = child.getNextSibling()) {
	    if(child.getNodeName().compareToIgnoreCase("Account") == 0) {
		//System.out.println("foo");
	    }		
	}
    }

    /** 
     * This method.
     *
     * @param root Node
     */
    public void StoreNode(OSM_node node) {
 	System.err.println("STORE node "+node.get_ID()+" : lat="+
 			   node.get_lat()+
 			   " lon="+
 			   node.get_lon()+
 			   " name="+
 			   node.get_name());
    }
}

