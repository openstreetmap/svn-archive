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
import groovy.xml.XmlUtil

/**
 * Takes a file with the newest data for studios from IMS and creates an OSM file
 * with property ids, version info and action="modify" attributes, in order to open
 * it in JOSM and upload it to OSM. 
 * 
 */
class UpdateFileBuilder {
	private static def xpath = XPathFactory.newInstance().newXPath()
	def lastChangesetId
	def reader
	def writer
	
	def queryApi(url) {
		def builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		def doc  = builder.parse(
			new InputSource(
				new InputStreamReader(
					new URL(url).openStream(),
					"UTF-8"
				)
			)
		).documentElement
		return doc	
	}
	/**
	 * Retrieves the nodes which have been updated and create in the last update of
	 * the studio dataset 
	 * 
	 */
	def fetchChangeset() {
		return queryApi("http://api.openstreetmap.org/api/0.6/changeset/${lastChangesetId}/download")
	}
	
	
	/**
	 * Fetch the current version for all nodes representing IMS studios 
	 * 
	 */
	def currentIMSStudioObjectsFromOsm(changeset) {
		def ids = []
		xpath.evaluate("//node", changeset, XPathConstants.NODESET).each { node ->
			ids << xpath.evaluate("./@id", node, XPathConstants.STRING)
		}
		return queryApi("http://api.openstreetmap.org/api/0.6/nodes?nodes=" + ids.join(","))
	}
	
	/** 
	 * Load the new data from the OSM file
	 * 
	 */
	def loadNewStudios() {
		def builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		def doc  = builder.parse(
				new InputSource(
						reader
				)
		).documentElement
		return doc	
	}
	
	/**
	 * Update the id and version in the studio objects. 
	 */
	def updateIdsAndVersions(newStudios, oldStudios) {
		xpath.evaluate("//node", newStudios, XPathConstants.NODESET).each { newStudio ->
			def imsid = xpath.evaluate("./tag[@k = 'ims:id']/@v", newStudio, XPathConstants.STRING)
			println "Processing studio '${imsid}' ..."			
			def osmid = xpath.evaluate("//tag[@k = 'ims:id'][@v = '${imsid}']/../@id", oldStudios, XPathConstants.STRING)
			def osmversion = xpath.evaluate("//tag[@k = 'pid'][@v = '${imsid}']/../@version", oldStudios, XPathConstants.STRING)
			if (!osmid) {
				println "Warning: didn't find an existing node for studio '${imsid}'. Adding the studio instead of updating it."				
				return
			} 			
			newStudio.setAttribute("id", osmid)
			newStudio.setAttribute("version", osmversion)
			newStudio.setAttribute("action", "modify")
		}
		return newStudios
	}
	
	
	def process() {
		println "Fetching changeset '${lastChangesetId}' from OSM API ..."
		def changeset = fetchChangeset()
		println "Fetching the current versions of the nodes from the OSM API ..."
		def oldStudios = currentIMSStudioObjectsFromOsm(changeset)
		println "Loading the new nodes for IMS studios ..."
		def newStudios = loadNewStudios()
		println "Updating the ids and versions of the new studios ..."
		updateIdsAndVersions(newStudios, oldStudios)
		println "Writing the update changeset file ..."
		writer.println newStudios		
		writer.flush()		
	}
	
	def usage() {
		println """
groovy ch.guggis.haiti.radiotv.UpdateFileBuilder [options]
Options:
  -cs, --last-changeset   the changeset id used in the last upload. Mandatory.
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
				case "-cs":
				case "--last-changeset":
					lastChangesetId = args.pop()
					if (lastChangesetId == null) {
						fail "Error: missing changeset id"
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
		
		if (!lastChangesetId) {
			fail("Mandatory command line option '-cs' missing.")
			System.exit(1)
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
		def task = new UpdateFileBuilder()
		task.processCommandLineOptions(args)
		task.process()
	}
}
