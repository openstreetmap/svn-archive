// License: GPL. Copyright 2007 by Tim Haussmann

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * 
 * @author Tim Haussmann
 */

public class Logger {

	private static Logger iSelf;
	
	private File iLogFile;
	private FileOutputStream iFout;
	
	private Logger(String aLogFilePath){
		if(aLogFilePath != null && !aLogFilePath.equals(""))
			iLogFile = new File(aLogFilePath);
		else
			iLogFile = new File("c:/LogFile.txt");
		
		try {
			iFout = new FileOutputStream(iLogFile, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void log(String aMessage){
		if(iSelf == null)
			iSelf = new Logger(null);
		iSelf.logMessage(aMessage);
	}
	
	public static void close(){
		iSelf.saveLog();
		iSelf = null;
	}
	
	private void saveLog() {
		try {
			iFout.flush();
			iFout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setLogFile(String aLogFilePath){
		iSelf = new Logger(aLogFilePath);
	}

	private void logMessage(String aMessage){
		try {
			iFout.write((aMessage + "\n").getBytes());
			iFout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
