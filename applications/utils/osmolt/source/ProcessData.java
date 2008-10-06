import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

public class ProcessData extends Thread {

	MapFeatures mapFeatures;

	boolean isway = false;

	ArrayList<String[]> tags;

	ArrayList<Integer> points;
	String osmfilename="";

	double lon;

	double lat;

	String folder;

	public ProcessData(String folder, MapFeatures mapFeatures, double lon,
			double lat, ArrayList<String[]> tags) {
		this.mapFeatures = mapFeatures;
		this.folder = folder;
		this.lon = lon;
		this.lat = lat;
		this.tags = tags;
	}

	// way
	public ProcessData(String folder, MapFeatures mapFeatures,
			ArrayList<String[]> tags, ArrayList<Integer> points,String osmfilename) {
		this.mapFeatures = mapFeatures;
		this.folder = folder;
		this.tags = tags;
		this.points = points;
		this.osmfilename = osmfilename;
		isway = true;
	}

	public void run() {

		try {
			ArrayList<String> names = mapFeatures.getNames();
			boolean found = false;
			int i = 0;
			while ((i < names.size()) && (!found)) {

				Element filter = mapFeatures.getFilterByName(names.get(i));
				found = processFilter(filter, " ");
				if (found) {
					
					float[] midpoint = SearchAreaMidpoint.startSearch(osmfilename, points);
					if (midpoint.length !=2) System.err.println("warning wrong midpoint");
					makeLine(mapFeatures.getElementByName(names.get(i)));
				}
				i++;
			}

			// if (found)
			// System.out.println(this.getId() + " gefunden");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	boolean processFilter(Element filter, String spaces) {
		boolean result = true;
		boolean neg = false;
		// System.out.println(this.getId() + spaces + filter.getName());
		if ((filter.getAttribute("negation") != null)
				&& (filter.getAttribute("negation").getValue().equals("true")))
			neg = true;
		if (filter.getName().equals("filter")) {
			if (filter.getAttribute("logical").getValue().equals("and")) {
				// true = neutrale element zur
				// and-verknüpfung
				List<Element> children = filter.getChildren();
				for (Iterator iter = children.iterator(); iter.hasNext();) {
					Element element = (Element) iter.next();
					result = result && processFilter(element, spaces + " ");
				}
				return result != neg; // xor
			}
			if (filter.getAttribute("logical").getValue().equals("or")) {
				// or-verknüpfung
				result = false;
				List<Element> children = filter.getChildren();
				for (Iterator iter = children.iterator(); iter.hasNext();) {
					Element element = (Element) iter.next();
					result = result || processFilter(element, spaces + " ");
				}
				return result != neg;
			}

		} else if (filter.getName().equals("restriction")) {
			if ((filter.getAttribute("osmKey") != null)
					&& (filter.getAttribute("osmValue") != null)) {
				for (int i = 0; i < tags.size(); i++) {
					if ((filter.getAttribute("osmKey").getValue().equals(tags
							.get(i)[0]))
							&& (filter.getAttribute("osmValue").getValue()
									.equals(tags.get(i)[1]))) {
						return true != neg;
					}
				}
			}
			// System.out.println(this.getId() + spaces + "false");
			return false;
		}
		return true;
	}

	private void makeLine(Element filter) throws IOException {
		String titel = makeTitel(filter.getChild("titel"));
		String description = makeTitel(filter.getChild("description"));
		Double[] merc = Mercator.merc(lon, lat);
		// System.out.println(titel + " : " + description);
		String filename = filter.getAttributeValue("filename");

		FileWriter file;
		// String filename = mapFeatures.getfilename(osmKey, osmVal);
		file = new FileWriter(folder + filename, true);
		file.append(merc[1] + "," + merc[0] + "	" + titel + "	" + description
				+ "	" + filter.getAttributeValue("image") + "	"
				+ filter.getAttributeValue("imagesize") + "	"
				+ filter.getAttributeValue("imageoffset") + "	" + "\n");

		file.close();
	}

	private String makeTitel(Element filter) {
		String result = "";

		List<Content> elements = filter.getContent();
		for (int i = 0; i < elements.size(); i++) {
			Content content = elements.get(i);
			if (content.getClass().equals(new Element("test").getClass())) {
				Element element = (Element) content;
				if (element.getName().equals("valueof")) {
					for (int j = 0; j < tags.size(); j++) {
						if (tags.get(j)[0].equals(element
								.getAttribute("osmKey").getValue())) {
							result = result + tags.get(j)[1];
						}
					}
				} else if (element.getName().equals("br")) {
					result = result + "<br/>";
				}

			} else if (content.getClass().equals(new Text("").getClass())) {

				StringBuffer text = new StringBuffer(content.getValue());
				if ((content.getValue().charAt(0) == ' ')
						|| (content.getValue().charAt(0) == '	')
						|| (content.getValue().charAt(0) == '\n'))
					result = " " + result;
				result = result + text.toString().trim();
				if ((content.getValue().endsWith(" "))
						|| (content.getValue().endsWith("	"))
						|| (content.getValue().endsWith("\n")))
					result = result + " ";
			}
		}
		return result;
	}
}
