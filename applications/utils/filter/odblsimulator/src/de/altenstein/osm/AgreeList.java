package de.altenstein.osm;

import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class AgreeList {
	
	ArrayList<Integer> list = new ArrayList<Integer>();
	int agreedUserCount;
	int totalUsers = 286582;
	
	/**
	 * Constructs a new AgreeList object and fills object's ArrayList<Integer> list by calling fillList(...)
	 * @param filename pointing onto a txt file containing id's which agreed to license change.
	 */
	public AgreeList(String filename){
		try {			
			BufferedReader bufferedReader;
			bufferedReader = new BufferedReader(new FileReader(filename));
			this.fillList(bufferedReader);
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Agreed users file not found: " + filename);
		} catch (IOException e) {
			System.out.println("Error while parsing agree list. Please use format as provided on planet.openstreetmap.org/users_agreed.txt");
			e.printStackTrace();			
		}
	}
	
	/**
	 * Parses the file which bufferedReader is pointing onto and adds all encountered ids to ArrayList list.
	 * Furthermore counts the number of agreed users.
	 * @param bufferedReader
	 * @throws IOException
	 */
	private void fillList(BufferedReader bufferedReader) throws IOException{
		agreedUserCount = 0;
		while (bufferedReader.ready()){
			String lineString = bufferedReader.readLine();
			
			if (!lineString.contains("#") && lineString.length() > 0){
				StringTokenizer tok = new StringTokenizer(lineString, " ");
				list.add(Integer.parseInt(tok.nextToken()));
				agreedUserCount++;
			}
		}
		// System.out.println("user list has been parsed!");
	}
	
	/**
	 * Checks whether the object's ArrayList list contains the given userID.
	 * @param userID
	 * @return true if user agreed or if userID >= 286582 (in which case the user agreed anyways as part of the signup process)
	 */
	public boolean contains(int userID){
		if (userID >= 286582){
			return true;
		}
		return list.contains(userID);
	}
	
	/**
	 * Creates a String representation of various statistics.
	 * @return String holding statistics
	 */
	public String getStatistics(){
		double percentage = (double)agreedUserCount/totalUsers;
		return "Total users: " + totalUsers + "\nUsers agreed: " + agreedUserCount + "\nPercentage agreed: " + percentage + " %\n___________";
	}
	
}
