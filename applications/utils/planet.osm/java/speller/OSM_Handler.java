import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.Writer;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class OSM_Handler extends DefaultHandler {
    
    private boolean inDouble = false;
    private int current_type = 0 ; // 0=none, 1=node, 2=segment, 3=way
    private int current_key_type = 0 ; // 0=none, 1=node, 2=segment, 3=way, 4=seg
    private int current_key = 0; // See handle_key
    private OSM_node node;
    private OSM_segment segment;
    private OSM_way way;
    private Writer logfile;
    private WorkingStorage ws;
    private boolean badnode;
    private int badnodecode;
    private Vector words = new Vector(50);
    private Vector wordstates = new Vector(50);
    private int lastgoodword=0;
    private String filename;
    private boolean resuming = false;
    private int nodetype;
    private int nodeid;

    OSM_Handler(Writer w_in, WorkingStorage ws_in, String filename_in) {
	System.out.println("OSM_handler constructor!");
	logfile = w_in;
	ws = ws_in;
	filename = filename_in;
	resuming = false;

	get_wordlist();
    }

    OSM_Handler(Writer w_in, WorkingStorage ws_in, String filename_in, String nodetype_in, String nodeid_in) {
	System.out.println("OSM_handler constructor!");
	logfile = w_in;
	ws = ws_in;
	filename = filename_in;
	resuming = true;
	if (nodetype_in.equals("node")) {
	    nodetype = 1;
	} else if (nodetype_in.equals("segment")) {
	    nodetype = 2;
	} else if (nodetype_in.equals("way")) {
	    nodetype = 3;
	} else {
	    System.err.println("Faulty nodetype in resumefile");
	    System.exit(1);
	}
	nodeid = Integer.parseInt(nodeid_in);

	get_wordlist();
    }

    private void add_word(String word) {
	if (word.charAt(0) == '+') {
	    words.add(word.substring(1));
	    wordstates.add(0);
	    lastgoodword=words.size()-1;
	} else if(word.charAt(0) == '-') {
	    words.add(word.substring(1));
	    wordstates.add(lastgoodword);
	} else if(word.charAt(0) == '#') {
	    //ignore_comment;
	} else {
	    System.out.println("Bad line in words.cfg, ignored:"+word);
	}
    }

    private void get_wordlist() {
	String s;
	String hf="";

	try {
	    FileInputStream fis = new FileInputStream("words.cfg");
	    if (fis != null) {
		BufferedReader fr = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

		while (hf!=null) {
		    hf = fr.readLine();
		    if ((hf != null) && (hf.length()>1)){
			add_word(hf);
		    }
		}
	    }
	} catch (Exception e) { System.out.println("get wordlist failed:"+e.getMessage()); }
	
// 	System.out.println("Read words:");
// 	int i=0;
// 	while (i<words.size()) {
// 	    System.out.println(i+":"+words.get(i)+":"+wordstates.get(i));
// 	    i++;
// 	}
    }

    public void startElement(String namespaceURI, String localName,
			     String qualifiedName, Attributes atts) throws SAXException {
	//if (localName.compareToIgnoreCase("double")==0) inDouble = true;
//  	System.out.println("START namespaceURI="+namespaceURI+
//  			   "  localName="+localName+
//  			   "  qualifiedName="+qualifiedName);
	if(qualifiedName.compareToIgnoreCase("node")==0) {
	    current_type = 1;
	    current_key_type = 1;
	    badnode = false;
	    node = new OSM_node();
 	} else if(qualifiedName.compareToIgnoreCase("seg")==0) {
// 	    THIS IS NOT A SEGMENT, this is segment ref in way-object
	    current_key_type = 4;
	} else if(qualifiedName.compareToIgnoreCase("segment")==0) {
	    current_type = 2;
	    current_key_type = 2;
	    badnode = false;
	    segment = new OSM_segment();
	} else if(qualifiedName.compareToIgnoreCase("way")==0) {
	    current_type = 3;
	    current_key_type = 3;
	    badnode = false;
	    way = new OSM_way();
	} else if(qualifiedName.compareToIgnoreCase("tag")==0) {
	    // null
	} else if(qualifiedName.compareToIgnoreCase("osm")==0) {
	    // Maybe should do something with this to make sure
	    // it is an osm we are reading
	    badnode = false;
	} else {
	    System.err.println("Unhandled qualifiedName:"+qualifiedName);
	}

	for (int i=0; i<atts.getLength(); i++) {
// 	    System.out.println("  Att "+i+": Name: <"+atts.getQName(i)+
// 			       "> Value:<"+atts.getValue(i)+">");
	    handle_att(atts.getQName(i), atts.getValue(i));
	}
    }

    private double safeParseDouble(String str) {
	double retval;
	try {
	    retval = Double.parseDouble(str);
	}
	catch(java.lang.NumberFormatException e) {
	    badnode = true;
	    badnodecode = 1;
	  //   System.err.println("DoubleParse error:"+str);
// 	    System.err.println("Probable node id: "+node.get_ID());
// 	    System.err.println(e);
	    retval = 0;
	}
	return retval;
    }
    
    private int safeParseInt(String str) {
	int retval;
	try {
	    retval = Integer.parseInt(str);
	}
	catch(java.lang.NumberFormatException e) {
	    badnode = true;
	    badnodecode = 2;
	    //System.err.println("IntParse error:"+str);
	    //System.err.println(e);
	    retval = 0;
	}
	return retval;
    }
    
    private void handle_att(String name, String value) {
	if(name.compareToIgnoreCase("lat")==0) {
	    switch(current_type) {
	    case 0 : System.err.println("Fail"); break;
	    case 1 : node.set_lat(safeParseDouble(value)); break;
	    default : //System.err.println("Fail foo");
	    }
	} else if(name.compareToIgnoreCase("lon")==0) {
	    switch(current_type) {
	    case 0 : System.err.println("Fail"); break;
	    case 1 : node.set_lon(safeParseDouble(value)); break;
	    default : //System.err.println("Fail foo");
	    }
	} else if(name.compareToIgnoreCase("id")==0) {
	    if(current_key_type != 4) {
		switch(current_type) {
		case 0 : System.err.println("Fail"); break;
		case 1 : node.set_ID(safeParseInt(value)); break;
		case 2 : segment.set_ID(safeParseInt(value)); break;
		case 3 : way.set_ID(safeParseInt(value)); break;
		default : //System.err.println("Fail foo");
		}
	    }
	} else if(name.compareToIgnoreCase("version")==0) {
	    System.err.println("Reading version: "+value);
	} else if(name.compareToIgnoreCase("k")==0) {
	    handle_key(value);
	} else if(name.compareToIgnoreCase("v")==0) {
	    handle_value(value);
	} else if(name.compareToIgnoreCase("timestamp")==0) {
	} else if(name.compareToIgnoreCase("to")==0) {
	} else if(name.compareToIgnoreCase("from")==0) {
	} else if(name.compareToIgnoreCase("generator")==0) {
	} else {
	    System.err.println("Unhandled Attribute:"+name);
	}
    }
    
    private void handle_key(String keyval) {
	int val;
	
	val = spelling(keyval);
	switch(val) {
	case -1 : current_key = 0;
	    break; //correctly spelled, do nothing
	case -3 : // report unknown key
	    System.err.println("Unhandled key: >"+ keyval+ "<");
	    switch(current_type) {
	    case 0 : break;
	    case 1 : 
		System.err.println("Node id:"+node.get_ID());
		break;
	    case 2 : 
		System.err.println("Segment id:"+segment.get_ID());
		break;
	    case 3 : 
		System.err.println("way id:"+way.get_ID());
		break;
	    }
	    add_word("+"+keyval);
	    current_key = 0;
	    break;
	default :
	    if (val >= 0) {
		current_key = -1;
	    } else {
		System.err.println("handle_key::spelling has gone abyss:"+val+"/"+words.size());
		System.exit(1);
	    }
	    break;
	}
    }
    
    private void handle_value(String value) {
	if (current_key < 0) {
	    badnode = true;
	    badnodecode = current_key;

	}
    }

    private void dump_dom_object(Document object, OutputStream os) {
	//System.out.println("dump_dom_object TODO");
	ws.write_dom(object, os);
    }

    private void dump_dom_object(Document object, Writer w) {
	//System.out.println("dump_dom_object TODO");
	ws.write_dom(object, w);
    }

    private Document dom_get_object(int nodeid, String objecttype) {
	String user = "jonass@lysator.liu.se";    // TODO
	String passwd = "popper";                 // TODO !!
  	String userPassword = user + ":" + passwd;
	String hf="";
	Document doc = null;
	
	try {
 	    String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
	    URL url = new URL("http://www.openstreetmap.org/api/0.3/"+objecttype+"/"+nodeid);
 	    URLConnection uc = url.openConnection();
 	    uc.setRequestProperty("Authorization", "Basic " + encoding);
	    
	    if (uc == null) {
		throw new Exception("Got a null URLConnection object!");
	    }
	    InputStream is = uc.getInputStream();
	    
	    final DocumentBuilderFactory dbf =
		DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = null;
	    try {
		db = dbf.newDocumentBuilder();
	    } catch (ParserConfigurationException pce) {
		System.err.println("dom_get_node1:"+pce);
		System.exit(1);
	    }
	    doc = db.parse(is);
	} catch (java.io.FileNotFoundException e) {
	    System.err.println("dom_get_node2:"+e);
	} catch (Exception e) {
	    System.err.println("dom_get_node2:"+e);
	    System.exit(1);
	}
	return doc;
    }

    private int spelling(String name) {
	// return -1 for correct
	// return -3 for unknown
	// return positive index for correct spelling otherwise
	int i=0;
 	while (i<words.size()) {
 	    if (words.get(i).equals(name)) {
		//System.err.println("ERR:"+(Integer)wordstates.get(i));
		if(((Integer)wordstates.get(i)).intValue()==0) {
		    return -1;
		} else {
		    return ((Integer)wordstates.get(i)).intValue();
		}
	    }
	    i++;
 	}
	return -3;
    }
    
    private int spell_node(Node tag) {
	int fixed = 0;

	NamedNodeMap nnm = tag.getAttributes();
	if (nnm != null) {
	    for (int i = 0; i <nnm.getLength(); i++) {
		//System.out.println("spell_node:" + nnm.item(i).getNodeName());
		if (nnm.item(i).getNodeName().equals("k")) {
		    //System.out.println("spell_node: "+nnm.item(i).getNodeValue());
		    int spellingstate = spelling(nnm.item(i).getNodeValue());
		    switch(spellingstate) {
		    case -1 : break; //correctly spelled, do nothing
		    case -3 : // do nothing (assuming already reported)
			break;
		    default :
			if ((spellingstate < 0) || (spellingstate >= words.size())) {
			    System.err.println("spelling has gone abyss:"+spellingstate+"/"+words.size());
			    System.exit(1);
			} else {  // switch word
			    nnm.item(i).setNodeValue((String)words.get(spellingstate));
			    fixed++;
			}
		    }
		}
	    }
	}
	return fixed;
    }
    
    private int fix_node(Document node) {
	int fixed = 0;
	final Node root = node.getFirstChild();
 	for (Node child = root.getFirstChild(); child != null;
	     child = child.getNextSibling()) {
 	    if (child.getNodeName() == "node") {
 		for (Node cchild = child.getFirstChild(); cchild != null;
 		     cchild = cchild.getNextSibling()) {
 		    //System.out.println("cchild name:"+cchild.getNodeName());
 		    if (cchild.getNodeName() == "tag") {
			fixed += spell_node(cchild);
 		    }
 		}
 	    }
 	}
	return fixed;
    }

    private int fix_segment(Document node) {
	int fixed = 0;
	final Node root = node.getFirstChild();
 	for (Node child = root.getFirstChild(); child != null;
	     child = child.getNextSibling()) {
 	    if (child.getNodeName() == "segment") {
 		for (Node cchild = child.getFirstChild(); cchild != null;
 		     cchild = cchild.getNextSibling()) {
 		    //System.out.println("cchild name:"+cchild.getNodeName());
 		    if (cchild.getNodeName() == "tag") {
			fixed += spell_node(cchild);
 		    }
 		}
 	    }
 	}
	return fixed;
    }

    private int fix_way(Document node) {
	int fixed = 0;
	final Node root = node.getFirstChild();
 	for (Node child = root.getFirstChild(); child != null;
	     child = child.getNextSibling()) {
 	    if (child.getNodeName() == "way") {
 		for (Node cchild = child.getFirstChild(); cchild != null;
 		     cchild = cchild.getNextSibling()) {
 		    //System.out.println("cchild name:"+cchild.getNodeName());
 		    if (cchild.getNodeName() == "tag") {
			fixed += spell_node(cchild);
 		    }
 		}
 	    }
 	}
	return fixed;
    }

    private void upload_object(Document node, int nodeid, String objecttype) {
	try {
	    logfile.write("AFTER:\n");
	    dump_dom_object(node, logfile);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.err.println("upload_node failed1:"+e);
	}
	System.out.println("Sleeping 30 seks before uploading");
	try {
	    Thread.sleep(30*1000);
	} catch (InterruptedException e) {
	    System.out.println("upload_node2:"+e);
	}
	
	String user = "jonass@lysator.liu.se";    // TODO
	String passwd = "popper";                 // TODO !!
  	String userPassword = user + ":" + passwd;
	String hf="";
	
	try {
	    String s;
 	    String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
	    URL url = new URL("http://www.openstreetmap.org/api/0.3/"+objecttype+"/"+nodeid);
	    HttpURLConnection h = (HttpURLConnection)url.openConnection();
	    h.setDoOutput(true);
	    h.setDoInput(true);
 	    h.setRequestProperty("Authorization", "Basic " + encoding);
	    h.setRequestMethod("PUT");
	    h.setRequestProperty("Connection", "close");
	    h.setRequestProperty("Content-type", "text/plain");
	    OutputStream os = h.getOutputStream();
	    
	    ws.write_dom(node, os);
	    os.flush();
	    BufferedReader br = new BufferedReader(
						   new InputStreamReader(h.getInputStream()));
	    h.connect();
	    int code = h.getResponseCode();
	    if (code >= 200 && code < 300) { 
		System.out.println("Server OK:"+code);
	    } else {
		System.out.println("Servererror:"+code);
	    }
	    while ( (s = br.readLine()) != null )
		System.out.println(s);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("upload_node failed3: "+e.getMessage());
	}

	System.out.println("Sleeping 10 secs after uploading");
	try {
	    Thread.sleep(10*1000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    System.out.println("upload_node failed4: "+e);
	}

    }


    private void fixnode(int nodeid) {
	Document node = dom_get_object(nodeid, "node");
	if (node != null) {
	    System.out.println("Sleeping 30 seks after downloading");
	    try {
		Thread.sleep(30*1000);
	    } catch (InterruptedException e) {
		System.out.println("fixnode1:"+e);
	    }
	    
	    
	    StringWriter beforestate = new StringWriter(10000);
	    dump_dom_object(node, beforestate);
	    int errors = 0;
	    errors = fix_node(node);
	    if(errors>0) {
		try {
		    logfile.write("BEFORE:\n");
		    logfile.write(beforestate.toString());
		    logfile.write("Fixed "+errors+" tags in node "+nodeid+"\n");
		}
		catch(Exception e) {
		    e.printStackTrace();
		    System.err.println("fixnode failed:"+e);
		}
		upload_object(node, nodeid,"node");
	    }
	}
    }
    
    private void fixsegment(int segmentid) {
	Document node = dom_get_object(segmentid, "segment");
	if (node != null) {
	    System.out.println("Sleeping 30 seks after downloading");
	    try {
		Thread.sleep(30*1000);
	    } catch (InterruptedException e) {
		System.out.println("fixsegment1:"+e);
	    }
	    
	    
	    StringWriter beforestate = new StringWriter(10000);
	    dump_dom_object(node, beforestate);
	    //System.out.println("Looking at segment: "+beforestate);
	    int errors = 0;
	    errors = fix_segment(node);
	    if(errors>0) {
		try {
		    logfile.write("BEFORE:\n");
		    logfile.write(beforestate.toString());
		    logfile.write("Fixed "+errors+" tags in segment "+segmentid+"\n");
		}
		catch(Exception e) {
		    e.printStackTrace();
		    System.err.println("fixsegment failed:"+e);
		}
		upload_object(node, segmentid, "segment");
	    }
	}
    }
    
    private void fixway(int wayid) {
	Document node = dom_get_object(wayid, "way");
	if (node != null) {
	    System.out.println("Sleeping 30 seks after downloading");
	    try {
		Thread.sleep(30*1000);
	    } catch (InterruptedException e) {
		System.out.println("fixway1:"+e);
	    }
	    
	    
	    StringWriter beforestate = new StringWriter(10000);
	    dump_dom_object(node, beforestate); 
	    int errors = 0;
	    errors = fix_way(node);
	    if(errors>0) {
		try {
		    logfile.write("BEFORE:\n");
		    logfile.write(beforestate.toString());
		    logfile.write("Fixed "+errors+" tags in way "+wayid+"\n");
		}
		catch(Exception e) {
		    e.printStackTrace();
		    System.err.println("fixway failed:"+e);
		}
		upload_object(node, wayid, "way");
	    }
	}
    }
    
    private void show_broken_object() {
	switch(current_type) {
	case 0 : System.err.println(" show_broken_object 0"); break;
	case 1 : 
	    fixnode(node.get_ID());
	    break;
	case 2 : //System.err.println(" show_broken_object 2");
	    fixsegment(segment.get_ID());
	    //segment.dump(badnodecode);
	    break;
	case 3 : //System.err.println(" show_broken_object 3"); 
	    fixway(way.get_ID());
	    //way.dump(badnodecode);
	    break;
	}
    }

    private void set_ele(double ele) {
	switch(current_type) {
	    //case 0 : node.set_ele(ele); break;
	    //case 1 : segment.set_ele(ele); break;
	    //case 2 : way.set_ele(ele); break;
	}
    }
    
    private void set_created(String created) {
	switch(current_type) {
	    //case 0 : node.set_created(created); break;
	    //case 1 : segment.set_created(created); break;
	    //case 2 : way.set_created(created); break;
	}
    }
    
    private void set_time(String time) {
	switch(current_type) {
	    //case 0 : node.set_time(time); break;
	    //case 1 : segment.set_time(time); break;
	    //case 2 : way.set_time(time); break;
	}
    }
    
    private void set_name(String name) {
	switch(current_type)
	    {
	    case 1 : node.set_name(name); break;
	    case 2 : segment.set_name(name); break;
	    case 3 : way.set_name(name); break;
	    }
    }
    
    public void endElement(String namespaceURI, String localName,
			   String qualifiedName) throws SAXException {
	
	//  	System.out.println("END namespaceURI="+namespaceURI+
	//  			   "  localName="+localName+
	//  			   "  qualifiedName="+qualifiedName);
	
	if ((qualifiedName.compareToIgnoreCase("node")==0) ||
	    (qualifiedName.compareToIgnoreCase("segment")==0) ||
	    (qualifiedName.compareToIgnoreCase("way")==0) ) {
	    if (badnode && (!resuming)) {
		show_broken_object();
	    }
	    // Update resume-file
	    if (!resuming) {
		try {
		    BufferedWriter out = new BufferedWriter(new FileWriter("resume.osp"));
		    out.write(filename+"\n");
		    out.write("bz"+"\n");
		    switch(current_type)
			{
			case 1 : out.write("node"+"\n");
			    out.write(node.get_ID()+"\n");
			    break;
			case 2 : out.write("segment"+"\n");
			    out.write(segment.get_ID()+"\n");
			    break;
			case 3 : out.write("way"+"\n");
			    out.write(way.get_ID()+"\n");
			    break;
			}
		    out.close();
		} catch (IOException e) {
		    System.err.println("Trouble writing resume file:"+e);
		}
	    }
	    if ((current_type == nodetype) &&
		(nodetype == 1) &&
		(node.get_ID() == nodeid)) {
		System.err.println("Finished resuming");
		resuming = false;
	    }
	    if ((current_type == nodetype) &&
		(nodetype == 2) &&
		(segment.get_ID() == nodeid)) {
		System.err.println("Finished resuming");
		resuming = false;
	    }
	    if ((current_type == nodetype) &&
		(nodetype == 3) &&
		(way.get_ID() == nodeid)) {
		System.err.println("Finished resuming");
		resuming = false;
	    }
	    current_type = 0;
	    current_key_type = 0;
	}
	// if tag&current_type = node then add tag to node and so on.
    }
    
    public void characters(char[] ch, int start, int length)
	throws SAXException {
	
// 	System.out.print("chars:");
// 	for (int i = start; i < start+length; i++) {
// 	    System.out.print(ch[i]); 
// 	}
// 	System.out.println();
    }

    public void error(SAXParseException exception) {
	System.err.println("OSM_handler error:"+exception);
    }

    public void fatalError(SAXParseException exception) {
	System.err.println("OSM_handler fatal:"+exception);
    }
    
    public void warning(SAXParseException exception) {
	System.err.println("OSM_handler warning:"+exception);
    }
}
