package ch.guggis.haiti.hospital;
import groovy.xml.MarkupBuilder
import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Input: a CSV export of the PAHO Health Facilities List 
 * Output: an OSM file with the PAHO Health Facilities 
 * 
 * !!! DO NOT upload the output file without careful examination      !!!
 * !!! which Health Facilities are already mapped in OSM.             !!!
 * !!! For each generated node, check whether there is already a node !!!
 * !!! or a way with the same health_facility:paho_id                 !!!
 * 
 * Usage:
 * groovy ch.guggis.haiti.hospital.MasterToOsmConverter [options]
 * Options:
 *   -h, --help              show help information
 *   -i, --input-file        the input file. Reads from stdin if missing
 *   -o, --output-file       the output file. Writes to stdout if missing.
 *    
 */
class MasterToOsmConverter {	
	def reader
	def writer
	
	def process() {
		def builder = new MarkupBuilder(writer)
		def int id = 0
		builder.mkp.xmlDeclaration([version:"1.0", encoding:"UTF-8"])
		builder.mkp.comment("""
Automatically generated from v2 of the PAHO master list of Health Facilities.

Generated on ${new SimpleDateFormat().format(new Date())}by MasterToOsmConverter"""
)
		builder.osm(version:"0.6") {	
			def nonemptytag = {
				k,v -> 
				if (v) {
					tag(k:k, v:v)
				}				
			}
			def notnulltag = {
				k,v -> 
				if (v && v != "0" ) {
					tag(k:k, v:v)
				}	
			}
			reader.eachLine() { 
				line ->
				def fields = line.split("\t")				
				fields = fields.collect {
					v ->
					v = v.replaceAll("^\"", "")
					v = v.replaceAll("\"\$", "")
					//v= "${i++}:" + v
					v
				}
				if (fields[0] == "FID") {
					// skip first line. This is a hack because
					// reader.eachLine(1) doesn't work 
					//
					return
				}
				id--								
				node(id:id, lat: fields[26], lon: fields[25], version:1) {
					notnulltag("health_facility:commune_id",fields[4])
					nonemptytag("health_facility:commune", fields[6])
					notnulltag("health_facility:department", fields[5])
					notnulltag("health_facility:region_id", fields[2])
					notnulltag("health_facility:district_id", fields[3])
					nonemptytag("health_facility:paho_id",fields[9])
					nonemptytag("name", fields[10])
					nonemptytag("pcode", fields[17])
					nonemptytag("operator", fields[14].trim())
					if (fields[15].trim()) {
						switch(fields[15].trim()) {
							case "DISP": 
							tag(k:"health_facility:type",v:"dispensary")
							break
							case "HOP": 
							tag(k:"amenity",v:"hospital")
							tag(k:"health_facility:type",v:"hospital")
							break
							case "C/S":
							tag(k:"health_facility:type",v:"cs_health_center")
							break;
							case "CSL":
							tag(k:"health_facility:type",v:"cs_health_center")
							tag(k:"bed", v:"no")
							break;
							case "CAL":
							tag(k:"health_facility:type",v:"cs_health_center")
							tag(k:"bed", v:"yes")
							break;
							case "Other":
							case "Unknown":
							tag(k:"health_facility:type",v:"unspecified")
						}
					}
					nonemptytag("source:health_facility", fields[28])
				}						
			}
		}
	}
	
	def usage() {
		println """
groovy ch.guggis.haiti.hospital.MasterToOsmConverter [options]
Options:
  -h, --help              show help information
  -i, --input-file        the input file. Reads from stdin if missing
  -o, --output-file       the output file. Writes to stdout if missing. 
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
		def task = new MasterToOsmConverter()
		task.processCommandLineOptions args
		task.process()
	}
}
