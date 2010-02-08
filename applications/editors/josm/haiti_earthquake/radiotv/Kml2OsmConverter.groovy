package ch.guggis.haiti.radiotv;

import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.BufferedReader;
import javax.xml.xpath.*
import groovy.util.IndentPrinter;
import groovy.util.XmlParser
import groovy.xml.MarkupBuilder;
import groovy.xml.DOMBuilder;
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import groovy.xml.StreamingMarkupBuilder
import org.w3c.dom.Node 
import java.text.SimpleDateFormat
import groovy.util.CliBuilder


/**
 * A radio or tv studio in the IMS list 
 */
class Studio {
	private static def xpath = XPathFactory.newInstance().newXPath()

    def id
	def lat
	def lon
	def String name = null
	def String type = null
	def String address = null
	def String contact = null
	def String email = null
	def String phone = null
	def String frequency = null
	def String notes = null
	
	/**
	 * Create a studio from a Placemark in the KML file 
	 */
	public Studio(Node placemark) {
		initId(placemark)
		initName(placemark)
		initType(placemark)
		initAddress(placemark)
		initContact(placemark)
		initPhone(placemark)
		initFrequency(placemark)
		initNotes(placemark)
		initEmail(placemark)
		initLatLon(placemark)
	}

	private def normalize(String s) {
		if (s == null) return s
		s = s.trim()
		s = s.replaceAll("\n", " ")
		s = s.substring(0,Math.min(s.length(), 255))
		return s
	}

	/**
	 * init the the from the Placemark node 
	 */
	static private def exprId = xpath.compile("./@id")
	private def initId(placemark) {
		id = exprId.evaluate(placemark, XPathConstants.STRING)
	}
	
	/**
	 * init the lat/lon-coordinates from the Placemark node 
	 */
	static private def exprLatLon = xpath.compile("./Point/coordinates/text()")
	private def initLatLon(placemark) {
		def latlon = exprLatLon.evaluate(placemark, XPathConstants.STRING)
		lat = null
		lon = null
		if (latlon == null) {
			return
		}
		def latlonarr = latlon.split(",")
		if (latlonarr == null || latlonarr.length != 2) {
			println "Error: illegal format of latlon '${latlon}' for studio '${id}'"
			return
		}
		lat = latlonarr[1].trim()
		lon = latlonarr[0].trim()
	}
	
	/**
	 * init the name from the Placemark 
	 */
	static private def exprName = xpath.compile("./name/text()")
	def initName(placemark) {
		name = exprName.evaluate(placemark, XPathConstants.STRING)
		if (name != null) name = name.trim()
	}
	
	/**
	 * init the media type from the Placemark 
	 */
	static private def exprType = xpath.compile("./ExtendedData/Data[@name = 'Type']/value/text()")
	def initType(placemark) {
		type = exprType.evaluate(placemark, XPathConstants.STRING)		
		if (type != null) type = type.trim()
	}
	
	/**
	 * init the address from the Placemark 
	 */
	static private def exprAddress = xpath.compile("./ExtendedData/Data[@name = 'Address']/value/text()")
	def initAddress(placemark) {
		address = normalize(exprAddress.evaluate(placemark, XPathConstants.STRING))		
	}
	
	/**
	 * init the contact information from the Placemark 
	 */
	static private def exprContact = xpath.compile("./ExtendedData/Data[@name = 'Contact']/value/text()")
	def initContact(placemark) {
		contact = normalize(exprContact.evaluate(placemark, XPathConstants.STRING))		
	}
	
	/**
	 * init the email address from the Placemark 
	 */
	static private def exprEmail = xpath.compile("./ExtendedData/Data[@name = 'Email']/value/text()")
	def initEmail(placemark) {
		email = normalize(exprEmail.evaluate(placemark, XPathConstants.STRING))		
	}
	
	/**
	 * init the frequency from the Placemark
	 */
	static private def exprFrequency = xpath.compile("./ExtendedData/Data[@name = 'Frequency']/value/text()")
	def initFrequency(placemark) {
		frequency = normalize(exprFrequency.evaluate(placemark, XPathConstants.STRING))		
	}

	/**
	 * init the notes from the Placemark
	 */
	static private def exprNotes = xpath.compile("./ExtendedData/Data[@name = 'Notes']/value/text()")
	def initNotes(placemark) {
		notes = normalize(exprNotes.evaluate(placemark, XPathConstants.STRING))		
	}

	/**
	 * init the phone from the Placemark 
	 */
	static private def exprPhone = xpath.compile("./ExtendedData/Data[@name = 'Subtitle']/value/text()")
	def initPhone(placemark) {
		phone = normalize(exprPhone.evaluate(placemark, XPathConstants.STRING))		
		if (phone != null) {
			def matcher = phone =~ /^\s*Tel\s*:\s*(.*)/
			if (matcher.matches()) {
				phone = matcher[0][1]
			}			
		}
	}

	/**
	 * Replies true if this studio has a valid position
	 */
	def boolean isValidPosition() {
		return lat != null && lon != null
	}
	
	def boolean isTower() {
		return notes != null && notes.startsWith("This location was found using GPS at the site")
	}
	
	def boolean isTvStation() {
		return type == "TV Stations"
	}
}


/**
 * The converter
 */
class Kml2OsmConverter {
	static final public String RADIO_TV_FILE = "C:/data/projekte/haiti/radiotv/RadioStationsHaitiJan2010.xml"
	private def xpath = XPathFactory.newInstance().newXPath()

    def reader
	def writer

	def process() {
		def builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		def doc  = builder.parse(new InputSource(reader)).documentElement
		def markup = new MarkupBuilder(writer)	
		def nodeId = 0
		markup.getMkp().pi(xml: [version: "1.0", encoding:"UTF-8"])
		markup.getMkp().comment("""
Automatically generated from this list of radio and tv studios in haiti:
http://spreadsheets.google.com/pub?key=tZP0wXS4HMLAWEhDlzdW36w&output=txt&output=txt&gid=0&range=kml_output

Generated on: ${new SimpleDateFormat().format(new Date())}					
""")					
		markup.osm(version: "0.6", generator: "Ism2Osm") {
			xpath.evaluate("//Placemark", doc, XPathConstants.NODESET).each {
				Node placemark ->
				def studio = new Studio(placemark)
				println "Processing studio '${studio.id}' with name '${studio.name}'"
				if (!studio.isValidPosition()) {
					println "Error: studio '${studio.id}' doesn't have a valid position. Skipping."
					return
				}
				if (!studio.id) {
					println "Error: studio '${studio.id}' doesn't have a valid IMS id. Skipping."
					return
				}
				nodeId--
				node(id:nodeId, version:1, lat: studio.lat, lon: studio.lon) {
					if (studio.isTower()) {
						tag(k:"man_made", v:"tower");
						tag(k:"tower:type", v:"communication")
					} else {
						tag(k:"amenity", v:"studio")
						if (studio.isTvStation()) {
							tag(k:"type", v:"video")
						}
					}
					if (studio.name) {
						tag(k:"name", v:studio.name)
					}
					if (studio.type) {
						tag(k:"ims:media_type", v:studio.type)
					}	
					tag(k:"ims:id", v:studio.id)
					if (studio.address) {
						tag(k:"addr", v:studio.address)
					}
					if (studio.contact) {
						tag(k:"contact", v:studio.contact)
					}
					if (studio.email) {
						tag(k: "contact:email", v:studio.email)
					}
					if (studio.phone) {
						tag(k: "phone", v:studio.phone)
					}
					if (studio.frequency) {
						tag(k: "ims:frequency", v:studio.frequency)
					}
					if (studio.notes) {
						tag(k: "note", v:studio.notes)
					}
					tag(k:"source_ref", v: "http://spreadsheets.google.com/pub?key=tZP0wXS4HMLAWEhDlzdW36w&output=txt&output=txt&gid=0&range=kml_output")
					tag(k:"source", "CartONG - http://www.cartong.org")
				}
			}			
		}
			
	}

	def usage() {
		println """
groovy ch.guggis.haiti.radiotv.Kml2OsmConverter [options]
Options:
		-h, --help				show help information
		-i, --input-file 		the input file. Reads from stdin if missing
		-o, --output-file		the output file. Writes to stdout if missing. 
"""
	}
	
	def fail(msg) {
		println msg
		usage()
		System.exit(1)
	}

    def processCommandLineOptions(argArray) {
		def inputFile
		def outputFile
		def args = Arrays.asList(argArray)
		args = args.reverse()
		def arg = args.pop()
		while(arg != null) {
			switch(arg) {
				case "-i":
				case "--input-file":
					inputFile = args.pop()
					if (inputFile == null) {
						fail "Error: missing input file"
					}
					break
				case "-o":
				case "--output-file":
					outputFile = args.pop()
					if (outputFile == null) {
						fail "Error: missing output file"
					}
					break
					
				case "-h":
				case "--help":
					usage()
					System.exit(0)
					break		
				
				default:
					fail "Illegal argument ${arg}"
			}
			arg = args.empty ? null : args.pop()
		}

		if (inputFile) {
			reader = new File(inputFile).newReader("UTF-8")
		} else {
			reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"))
		}
		if (outputFile) {
			writer = new File(outputFile).newWriter("UTF-8")
		} else {
			writer = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"))
		}	
	}
		
	static public void main(args) {
		def task = new Kml2OsmConverter()
		task.processCommandLineOptions(args)
		task.process()
	}
}
