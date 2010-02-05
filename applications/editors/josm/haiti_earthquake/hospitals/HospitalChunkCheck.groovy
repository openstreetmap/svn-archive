package ch.guggis.haiti.hospital;
import java.io.BufferedReader;
import javax.xml.xpath.*
import groovy.util.XmlParser
import groovy.xml.DOMBuilder

/**
 * Takes one of the chunks of health facilities posted here 
 * http://github.com/wonderchook/PAHO_to_OSM/ and checks for each
 * entry
 * <ol>
 *   <li>whether there are other already mapped hospitals in the neighborhood</li>
 *   <li>whether there are other already mapped health facilities in the neighborhood</li>
 *   <li>whether an OSM object with the given PAHO id already exists</li>
 * </ol>
 * 
 * <strong>Usage</strong> 
 * <pre>
 * 	groovy ch.guggis.haiti.hospital.HospitalChunkCheck [options] file
 *
 *	Options:
 *		-h, --help         show help information
 * </pre>
 */
class HospitalChunkCheck {	
	
	def File inputFile
	def xpath = XPathFactory.newInstance().newXPath()

    public HospitalChunkCheck(File inputFile) {
		this.inputFile = inputFile
	}
	
	def String buildQueryBbox( node) {
		def minLat = xpath.evaluate("./@lat", node, XPathConstants.NUMBER) - 0.003
		def minLon = xpath.evaluate("./@lon", node, XPathConstants.NUMBER) - 0.003
		def maxLat = xpath.evaluate("./@lat", node, XPathConstants.NUMBER) + 0.003
		def maxLon = xpath.evaluate("./@lon", node, XPathConstants.NUMBER) + 0.003
		return "bbox=${minLon},${minLat},${maxLon},${maxLat}"		
	}
	
	def int countHospitalsInProximity(node) {
		def bbox = buildQueryBbox(node)
		def queryUrl = "http://xapi.openstreetmap.org/api/0.6/*[amenity=hospital][${bbox}]"
		def xapiResponse = DOMBuilder.parse(new InputStreamReader(new URL(queryUrl).openStream(), "UTF-8"))
		def int  count = xpath.evaluate("count(//tag[@k = 'amenity'][@v = 'hospital']/..)", xapiResponse, XPathConstants.NUMBER)
		return count
	}
	
	def int countHealthFacilitiesInProximity(node) {
		def bbox = buildQueryBbox(node)
		def queryUrl = "http://xapi.openstreetmap.org/api/0.6/*[health_facility:paho_id=*][${bbox}]"
		def xapiResponse = DOMBuilder.parse(new InputStreamReader(new URL(queryUrl).openStream(), "UTF-8"))
		def int count = xpath.evaluate("count(//tag[@k = 'health_facility:paho_id']/..)", xapiResponse, XPathConstants.NUMBER)
		return count
	}
	
	def healthFacilityAlreadyMapped(node) {
		def id = getHealthFacilityId(node)
		def queryUrl = "http://xapi.openstreetmap.org/api/0.6/*[health_facility:paho_id=${id}]"
		def xapiResponse = DOMBuilder.parse(new InputStreamReader(new URL(queryUrl).openStream(), "UTF-8"))
		def int count = xpath.evaluate("count(//tag[@k = 'health_facility:paho_id'][@v ='${id}'])", xapiResponse, XPathConstants.NUMBER)
		return count > 0
	}
	
	def getHealthFacilityId(node) {
		return xpath.evaluate("./tag[@k = 'health_facility:paho_id']/@v", node)
	}
	
	def getName(node) {
		return xpath.evaluate("./tag[@k = 'name']/@v", node)		
	}
	
	def getNodeId(node) {
		return xpath.evaluate("./@id", node)		
	}
	
	def process() {
		inputFile.withReader("UTF-8") {
			reader ->
			def doc = DOMBuilder.parse(reader)	
			def nodes = xpath.evaluate("//node", doc, XPathConstants.NODESET)
			nodes.each {
				def int numHospitals = countHospitalsInProximity(it)				
				def int numHealthFacilities = countHealthFacilitiesInProximity(it)
				def name = getName(it)
				def id = getNodeId(it)
				def alreadyMapped = healthFacilityAlreadyMapped(it)
				def action = "AUTOMATIC"
				if (alreadyMapped || numHospitals > 0 || numHealthFacilities >0) {
					action = "MANUAL MERGE"
				}
				println "${action}: id:${id},name: ${name}, num hospitals: ${numHospitals}, num health facilities: ${numHealthFacilities}, already mapped: ${alreadyMapped}"
			}
		}
	}
	
	static def usage() {
		println """
groovy ch.guggis.haiti.hospital.HospitalChunkCheck [options] file

Options:
		-h, --help         show help information
"""
	}
	
	
	
	static public void main(args) {
		def files = []
		args.each {
			arg ->
			switch(arg) {
				case "-h":
				case "--help":
					usage();
					System.exit(0)
				default: 
					files << arg
			}
		}
		if (files.size() != 1) {
			System.err.println("Error: Exactly one input file required")
			usage()
			System.exit(1)
		}
		def checker = new HospitalChunkCheck(new File(files[0]))
		checker.process()		
	}
}
