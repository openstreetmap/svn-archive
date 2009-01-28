// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
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
			run(args);
			
			System.exit(0);
			
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Execution aborted.", t);
		}
		
		System.exit(1);
	}
	
	
	/**
	 * This contains the real functionality of the main method. It is kept
	 * separate to allow the application to be invoked within other applications
	 * without a System.exit being called.
	 * <p>
	 * Typically an application shouldn't directly invoke this method, it should
	 * instantiate its own pipeline.
	 * 
	 * @param args
	 *            The command line arguments.
	 */
	public static void run(String[] args) {
		CommandLineParser commandLineParser;
		TaskRegistrar taskRegistrar;
		Pipeline pipeline;
		long startTime;
		long finishTime;
		
		startTime = System.currentTimeMillis();
		
		configureLoggingConsole();
		
		commandLineParser = new CommandLineParser();
		
		// Parse the command line arguments into a consumable form.
		commandLineParser.parse(args);
		
		// Configure the new logging level.
		configureLoggingLevel(commandLineParser.getLogLevel());
		
		log.info("Osmosis Version " + OsmosisConstants.VERSION);
		taskRegistrar = new TaskRegistrar();
		taskRegistrar.initialize(commandLineParser.getPlugins());
		
		pipeline = new Pipeline(taskRegistrar.getFactoryRegister());
		
		log.info("Preparing pipeline.");
		pipeline.prepare(commandLineParser.getTaskInfoList());
		
		log.info("Launching pipeline execution.");
		pipeline.execute();
		
		log.info("Pipeline executing, waiting for completion.");
		pipeline.waitForCompletion();
		
		log.info("Pipeline complete.");
		
		finishTime = System.currentTimeMillis();
		
		log.info("Total execution time: " + (finishTime - startTime) + " milliseconds.");
	}
	
	
	/**
	 * Configures logging to write all output to the console.
	 */
	private static void configureLoggingConsole() {
		Logger rootLogger;
		Handler consoleHandler;
		
		rootLogger = Logger.getLogger("");
		
		// Remove any existing handlers.
		for (Handler handler : rootLogger.getHandlers()) {
			rootLogger.removeHandler(handler);
		}
		
		// Add a new console handler.
		consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		rootLogger.addHandler(consoleHandler);
	}
	
	
	/**
	 * Configures the logging level.
	 * 
	 * @param level
	 *            The new logging level to apply.
	 */
	private static void configureLoggingLevel(Level level) {
		Logger rootLogger;
		
		rootLogger = Logger.getLogger("");
		
		// Set the required logging level.
		rootLogger.setLevel(level);
		
		// Set the JPF logger to one level lower.
		Logger.getLogger("org.java.plugin").setLevel(Level.WARNING);
	}
}
