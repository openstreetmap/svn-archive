import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class MapFeatures {
	boolean comandline;

	String mapFeaturesfile;
	String outputfolder;

	ArrayList<String[]> knowenTyes = new ArrayList<String[]>();

	ArrayList<String> names = new ArrayList<String>();

	Element root = new Element("MapFeatures"); // Wurzelelement erzeugen

	Document doc;

	public MapFeatures(boolean comandline) {
		this.comandline = comandline;
	}

	public void openFile(String mapFeaturesfile) {

		Document newdoc = null;
		try {
			// validierenden Parser nutzen
			SAXBuilder b = new SAXBuilder(true);
			newdoc = b.build(new File(mapFeaturesfile));
			root = newdoc.getRootElement();

			// root.addContent();
			updatesknowenTyes();
		} catch (JDOMException j) {
			// nur eine Ausnahme für alle Situationen
			if (comandline)
				System.out.println(j.getMessage());
			else
				JOptionPane.showMessageDialog(null, j.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updatesknowenTyes();
	}

	public void saveFile(String mapFeaturesfile) {
		FileOutputStream out;
		try {
			System.out.println(root.getNamespace());
			DocType doct = new DocType("MapFeatures");
			System.out.println(root.getParentElement());
			doct.setSystemID("grammar.dtd");
			Document doc = new Document((Element) root.detach());
			doc.setDocType(doct);
			out = new FileOutputStream(mapFeaturesfile);
			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
			serializer.output(doc, out);
			out.flush();
			out.close();

			BufferedWriter grammarout = new BufferedWriter(new FileWriter(
					new File(mapFeaturesfile).getParentFile() + "/grammar.dtd"));
			grammarout.write(Grammar.getGrammar());
			grammarout.close();

			// getGrammar
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void updatesknowenTyes() {
		names.clear();
		knowenTyes.clear();
		List children = root.getChildren();
		for (int i = 0; i < children.size(); i++) {
			Element element = (Element) children.get(i);
			names.add(element.getAttribute("name").getValue());
			String[] keyPair = { "", "" };
			keyPair[0] = element.getAttribute("osmKey").getValue();
			keyPair[1] = element.getAttribute("osmValue").getValue();
			knowenTyes.add(keyPair);
		}

	}

	public Element addEntry(String name) {
		List rootChildren = root.getChildren();

		Element element = new Element("entry");
		rootChildren.add(element);

		element.setAttribute("name", name);
		element.setAttribute("osmKey", "");
		element.setAttribute("osmValue", "");
		element.setAttribute("filename", name + ".txt");
		element.setAttribute("image", name + ".png");
		element.setAttribute("imagesize", "20,20");
		element.setAttribute("imageoffset", "0,0");

		Element filter = new Element("filter");
		filter.setAttribute("name", "root");
		filter.setAttribute("logical", "and");
		element.addContent(filter);

		Element restriction = new Element("restriction");
		restriction.setAttribute("osmKey", "");
		restriction.setAttribute("osmValue", "");
		filter.addContent(restriction);

		Element titel = new Element("titel");
		titel.addContent(name);
		element.addContent(titel);

		Element description = new Element("description");
		description.addContent("");
		element.addContent(description);
		updatesknowenTyes();
		return element;

	}

	public Element getElementByName(String name) {
		List children = root.getChildren();
		Element result = null;
		for (int i = 0; i < children.size(); i++) {
			Element element = (Element) children.get(i);
			if (element.getAttribute("name").getValue().equals(name))
				result = element;
		}
		return result;
	}

	public ArrayList<String> getNames() {
		return names;
	}

	public void name(String name) {
		mapFeaturesfile = name;
	}

	String[] getTopFilter(String filtername) {
		String[] result = { "", "" };
		List children = root.getChildren();
		for (int i = 0; i < children.size(); i++) {
			Element element = (Element) children.get(i);
			if (element.getAttribute("name").equals(filtername)) {
				result[0] = element.getAttribute("osmKey").getValue();
				result[1] = element.getAttribute("osmValue").getValue();
			}
		}
		return result;
	}

	// public HashMap<String, String> getInfo(String osmKey, String osmVal) {
	// String[] test = { osmKey, osmVal };
	// return mapFeatures.get(test);
	// }
	//
	// public String getType(String osmKey, String osmVal) {
	// String[] test = { osmKey, osmVal };
	// return mapFeatures.get(test[0] + "=" + test[1]).get("type");
	// }
	//
	// public String getName(String osmKey, String osmVal) {
	// String[] test = { osmKey, osmVal };
	// return mapFeatures.get(test[0] + "=" + test[1]).get("name");
	// }
	//
	// public String getImage(String osmKey, String osmVal) {
	// String[] test = { osmKey, osmVal };
	// return mapFeatures.get(test[0] + "=" + test[1]).get("image");
	// }
	//
	// public String getfilename(String osmKey, String osmVal) {
	// String[] test = { osmKey, osmVal };
	// return mapFeatures.get(test[0] + "=" + test[1]).get("filename");
	// }
	//
	// public String getImagesize(String osmKey, String osmVal) {
	// String[] test = { osmKey, osmVal };
	// return mapFeatures.get(test[0] + "=" + test[1]).get("imagesize");
	// }
	//
	// public String getImageoffset(String osmKey, String osmVal) {
	// String[] test = { osmKey, osmVal };
	// return mapFeatures.get(test[0] + "=" + test[1]).get("imageoffset");
	// }
	//
	// public ArrayList<String[]> getKnowenTyes() {
	// return knowenTyes;
	// }
	//	

	ArrayList<String> getfilenames() {
		ArrayList<String> result = new ArrayList<String>();

		List children = root.getChildren();
		for (int i = 0; i < children.size(); i++) {
			Element element = (Element) children.get(i);
			result.add(element.getAttribute("filename").getValue());
		}
		return result;
	}

	Element getFilterByName(String Name) {
		List children = root.getChildren();
		for (int i = 0; i < children.size(); i++) {
			Element element = (Element) children.get(i);
			if (element.getAttribute("name").getValue().equals(Name))
				return element.getChild("filter");

		}
		System.out.println("fehler");
		return null;
	}

	public String getOutputfolder() {
		return outputfolder;
	}

	public void setOutputfolder(String outputfolder) {
		this.outputfolder = outputfolder;
	}

}
