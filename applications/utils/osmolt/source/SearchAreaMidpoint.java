import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SearchAreaMidpoint {
	public static float[] startSearch(String osmfile, ArrayList<Integer> points) {
		float[] result= {0,0};
		
		double wayLat=0;

		double wayLon=0;
		
		int pointcount=0;
		System.out.println("mid");
		try {		
			
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser;
			parser = factory
					.createXMLStreamReader(new FileInputStream(osmfile));
			
			
			
			boolean isend = false;
			
			
			while (parser.hasNext() && !isend) {
				switch (parser.getEventType()) {
				case XMLStreamConstants.START_DOCUMENT:
					// System.out.println("START_DOCUMENT: " +
					// parser.getVersion());
					break;

				case XMLStreamConstants.END_DOCUMENT:
					// System.out.println("END_DOCUMENT: ");
					parser.close();
					break;

				case XMLStreamConstants.NAMESPACE:
					// System.out.println("NAMESPACE: " +
					// parser.getNamespaceURI());
					break;

				case XMLStreamConstants.START_ELEMENT:
					if (parser.getLocalName().equals("node")) {

						double pointLat=0;

						double pointLon=0;
						
						int  id=0;
						
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String attName = parser
									.getAttributeLocalName(i);
							String attVal = parser.getAttributeValue(i);
							if (attName.equals("lat"))
								pointLat = new Double(attVal);
							else if (attName.equals("lon"))
								pointLon = new Double(attVal);
							else if (attName.equals("id"))
								id = new Integer(attVal);
							
						}
						
						for (int i = 0; i < points.size(); i++) {
							if ((id == points.get(i))&&(pointLat !=0)&&(pointLon!=0)) {
								pointcount+=1;
								wayLat+=pointLat;
								wayLon+=pointLon;								
							}
						}
						
						
						
						
					}

					break;
				case XMLStreamConstants.END_ELEMENT:

					break;

				default:
					break;
				}
				parser.next();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (pointcount !=0){
			result[0] = ((float)wayLon)/pointcount;
			result[1] = ((float)wayLat)/pointcount; 
		}
		return result;

	}

}
