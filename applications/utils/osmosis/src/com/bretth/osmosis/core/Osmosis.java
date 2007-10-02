package com.bretth.osmosis.core; 

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.core.cli.CommandLineParser;
import com.bretth.osmosis.core.pipeline.common.Pipeline;


/**
 * The main entry point for the command line application.
 * 
 * @author Brett Henderson
 */
public class Osmosis {
	private static final Logger log = Logger.getLogger(Osmosis.class.getName());
	
	
	/**
	 * The entry point to the application.
	 * 
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(String[] args) {
		try {
			CommandLineParser commandLineParser;
			Pipeline pipeline;
			
			configureLoggingConsole();
			
			commandLineParser = new CommandLineParser();
			
			// Parse the command line arguments into a consumable form.
			commandLineParser.parse(args);
			
			// Configure the new logging level.
			configureLoggingLevel(commandLineParser.getLogLevel());
			
			log.info("Osmosis Version " + OsmosisConstants.VERSION);
			TaskRegistrar.initialize();
			
			pipeline = new Pipeline();
			
			log.fine("Preparing pipeline.");
			pipeline.prepare(commandLineParser.getTaskInfoList());
			
			log.fine("Executing pipeline.");
			pipeline.execute();
			
			log.fine("Pipeline executing, waiting for completion.");
			pipeline.waitForCompletion();
			
			log.fine("Pipeline complete.");
			
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Main thread aborted.", t);
		}
	}
	
	
	/**
	 * Configures logging to write all output to the console.
	 */
	private static final void configureLoggingConsole() {
		Logger rootLogger;
		Handler consoleHandler;
		
		rootLogger = Logger.getLogger("");
		
		// Remove any existing handlers.
		for (Handler handler : rootLogger.getHandlers()) {
			rootLogger.removeHandler(handler);
		}
		
		// Add a new console handler.
		consoleHandler = new ConsoleHandler();
		rootLogger.addHandler(consoleHandler);
	}
	
	
	/**
	 * Configures the logging level.
	 * 
	 * @param level
	 *            The new logging level to apply.
	 */
	private static final void configureLoggingLevel(Level level) {
		Logger rootLogger;
		
		rootLogger = Logger.getLogger("");
		
		// Set the required logging level.
		rootLogger.setLevel(level);
	}
}
