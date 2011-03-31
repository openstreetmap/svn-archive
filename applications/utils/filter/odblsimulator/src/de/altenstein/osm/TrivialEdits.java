package de.altenstein.osm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class TrivialEdits {
	ArrayList<String> trivialNewKeys = new ArrayList<String>();
	ArrayList<String> trivialDelKeys = new ArrayList<String>();
	ArrayList<String[]> trivialValueChanges = new ArrayList<String[]>();
	
	/**
	 * Constructs a new TrivialEdits object and fills the object's ArrayLists by calling fillList(...)
	 * @param trivialNewTagsFile
	 * @param trivialDelTagsFile
	 * @param trivialValueChangesFile
	 * @param trivialTagSwapsFiles
	 */
	public TrivialEdits(String trivialNewTagsFile, String trivialDelTagsFile, String trivialValueChangesFile, String trivialTagSwapsFiles){
		try {			
			BufferedReader newReader = new BufferedReader(new FileReader(trivialNewTagsFile));
			BufferedReader delReader = new BufferedReader(new FileReader(trivialDelTagsFile));
			BufferedReader valueReader = new BufferedReader(new FileReader(trivialValueChangesFile));
			this.fillList(newReader, delReader, valueReader);
			newReader.close();
		} catch (FileNotFoundException e) {
			System.err.println("trivialEdit files not found. Sorry for any inconvenience!");
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println("Error while parsing trivial edit lists");
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the four files defined by BufferedReader objects and fills the correspondent ArrayLists.
	 * @param newReader
	 * @param delReader
	 * @param valueReader
	 * @param swapsReader
	 * @throws IOException
	 */
	private void fillList(BufferedReader newReader, BufferedReader delReader, BufferedReader valueReader) throws IOException{
		// read trivial new tag keys
		while (newReader.ready()){
			String lineString = newReader.readLine();
			if (lineString.length() > 0){
				trivialNewKeys.add(lineString);
			}
		}
		
		// read trivial deleted keys
		while (delReader.ready()){
			String lineString = delReader.readLine();
			if (lineString.length() > 0){
				trivialDelKeys.add(lineString);
			}
		}
		
		// read trivial change key/values
		while (valueReader.ready()){
			String lineString = valueReader.readLine();
			if (lineString.length() > 0){
				StringTokenizer strTok = new StringTokenizer(lineString," ");
				if (strTok.countTokens() == 3){
					String[] kvv = new String[]{strTok.nextToken(),strTok.nextToken(),strTok.nextToken()};
					trivialValueChanges.add(kvv);
				}
			}
		}
		// System.out.println("trivial new keys file parsed");
	}
	
	// returns boolean, true if trivialNewKeys contains the given key string
	public boolean containsNewKey(String key){
		return trivialNewKeys.contains(key);
	}
	
	public boolean containsDelKey(String key){
		return trivialDelKeys.contains(key);
	}
	
	public boolean containsChangedValues(String key, String v1, String v2){
		for (int i = 0; i < trivialValueChanges.size(); i++){
			String[] kvv = trivialValueChanges.get(0);
			if (kvv[0].equals(key) & kvv[1].equals(v1) & kvv[2].equals(v2)){
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * Returns a String saying which trivial edits have been read from the given files.
	 */
	public String toString(){
		String str = "___________\nTrivial edits:\nNew Keys: ";
		for (int i = 0; i < trivialNewKeys.size(); i++){
			str += "\n   " + trivialNewKeys.get(i);
		}
		str += "\nDeleted keys:";
		for (int i = 0; i < trivialDelKeys.size(); i++){
			str += "\n   " + trivialDelKeys.get(i);
		}
		str += "\nChanged values:";

		for (int i = 0; i < trivialValueChanges.size(); i++){
			String[] kvv = trivialValueChanges.get(i);
			str += "\n   " + kvv[0] + "=" + kvv[1] + " --> " + kvv[0] + "=" + kvv[2];
		}
		
		str += "\n__________";
		return str;
	}

}
