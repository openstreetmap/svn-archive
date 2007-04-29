import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

final class WorkingStorage {

    WorkingStorage() {
	System.out.println("WorkingStorage constructor");
    }

    /** 
     * This method.
     *
     * @param val int
     */
    public void debugDump(final int val) {
	System.out.println("--->>  BEGIN DUMP  <<---");

	System.out.println("--->>  END DUMP  <<---");
    }

    /** 
     * This method.
     *
     * @param file File
     */
    public void load(final File file, String filename) {
	try {
	    Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
	    load_SAX(file, w);
	    w.close();
	}
	catch(java.io.IOException e) {
	    e.printStackTrace();
	    System.err.println("WS.load fail:"+e);
	}
    }

    /** 
     * This method.
     *
     * @param file File
     */
    public void load2(final File file, String logfilename) {
	try {
	    FileInputStream fis = new FileInputStream(file);
	    fis.skip(2); //skips "BZ", should be verified to be correct
	    if (fis != null) {
		CBZip2InputStream is2 = new CBZip2InputStream(fis);
		Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logfilename), "UTF-8"));
		load_SAX2(is2, w, file.getPath());
		w.close();
	    }
	}
	catch(java.io.IOException e) {
	    e.printStackTrace();
	    System.err.println("WS.load fail:"+e);
	}
    }

    /** 
     * This method.
     *
     * @param file File
     */
    public void resume(String filename, String nodetype, String nodeid, String logfilename) {
	try {
	    FileInputStream fis = new FileInputStream(new File(filename));
	    fis.skip(2); //skips "BZ", should be verified to be correct
	    if (fis != null) {
		CBZip2InputStream is2 = new CBZip2InputStream(fis);
		Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logfilename), "UTF-8"));
		resume_SAX2(is2, w, filename, nodetype, nodeid);
		w.close();
	    }
	}
	catch(java.io.IOException e) {
	    e.printStackTrace();
	    System.err.println("WS.load fail:"+e);
	}
    }


    /** 
     * This method.
     *
     * @param file File
     */
    public void load_SAX2(final CBZip2InputStream fis, Writer w, String filename) {
	try {
	    DefaultHandler handler = new OSM_Handler(w, this, filename);
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser saxParser = factory.newSAXParser();
	    saxParser.parse(fis, handler);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("load_SAX1:"+e); 
	}
    }

    /** 
     * This method.
     *
     * @param file File
     */
    public void resume_SAX2(final CBZip2InputStream fis, Writer w, String filename, String nodetype, String nodeid) {
	try {
	    DefaultHandler handler = new OSM_Handler(w, this, filename, nodetype, nodeid);
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser saxParser = factory.newSAXParser();
	    saxParser.parse(fis, handler);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("load_SAX1:"+e); 
	}
    }

    /** 
     * This method.
     *
     * @param file File
     */
    public void load_SAX(final File file, Writer w) {
	try {
	    DefaultHandler handler = new OSM_Handler(w, this, file.getPath());
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser saxParser = factory.newSAXParser();
	    saxParser.parse(file, handler);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("load_SAX2:"+e); 
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
	System.err.println("WS.LOAD1");
        try {
            db = dbf.newDocumentBuilder();
	    System.err.println("WS.LOAD2");
        } catch (ParserConfigurationException pce) {
	    System.err.println("WS.LOAD3");
            System.err.println(pce);
            System.exit(1);
        }
	System.err.println("WS.LOAD4");

        // Set an ErrorHandler before parsing
        //OutputStreamWriter errorWriter =
        //    new OutputStreamWriter(System.err, outputEncoding);
        //db.setErrorHandler(
        //    new MyErrorHandler(new PrintWriter(errorWriter, true)));

        // Step 3: parse the input file
        Document doc = null;
	System.err.println("WS.LOAD5");
        try {
            doc = db.parse(file);
	    System.err.println("WS.LOAD6");
        } catch (SAXException se) {
	    System.err.println("WS.LOAD7");
            System.err.println(se.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
	    System.err.println("WS.LOAD8");
            System.err.println(ioe);
            System.exit(1);
        }
	System.err.println("WS.LOAD9");

	final Node root = doc.getFirstChild();
	System.err.println("WS.LOAD10");
	for (Node child = root.getFirstChild(); child != null;
             child = child.getNextSibling()) {
	    System.err.println("WS.LOAD11");
	    if(child.getNodeName().compareToIgnoreCase("Accounts") == 0)
		loadAccounts(child);
	}
    }


    /** 
     * This method.
     *
     * @param root Node
     */
    public void write_dom(Document dom, OutputStream os) {
	try {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    tf.setAttribute("indent-number", new Integer(4));
	    Transformer t = tf.newTransformer();
	    t.setOutputProperty(OutputKeys.INDENT, "yes");
	    t.setOutputProperty(OutputKeys.METHOD, "xml");
	    t.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
	    
	    t.transform(new DOMSource(dom), new StreamResult(new OutputStreamWriter(os, "UTF-8")));
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.err.println("write_dom failed:"+e);
	}
    }
    
    /** 
     * This method.
     *
     * @param root Node
     */
    public void write_dom(Document dom, Writer w) {
	try {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    tf.setAttribute("indent-number", new Integer(4));
	    Transformer t = tf.newTransformer();
	    t.setOutputProperty(OutputKeys.INDENT, "yes");
	    t.setOutputProperty(OutputKeys.METHOD, "xml");
	    t.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
	    t.transform(new DOMSource(dom), new StreamResult(w));
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.err.println("write_dom failed:"+e);
	}
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
}

