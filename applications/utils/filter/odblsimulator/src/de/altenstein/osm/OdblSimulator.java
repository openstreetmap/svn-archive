package de.altenstein.osm;

public class OdblSimulator {
	public static void main(String[] args){
		
		// defines output type and thus underlying analysis concepts. further documentation is provided in VersionComparator
		int outputType = 1;
		String inputFilename = "testfiles/1way.osm";
		String agreeListFilename = "users_agreed_110315.txt";
		String outputFilename = "testfiles/1way-exclude_deleted-mode" + outputType + ".osm";
		
		HistoryParser parser = new HistoryParser(inputFilename, agreeListFilename,outputFilename, outputType);
		parser.parseNodes();
		parser.parseWays();
			
		
	}
}
