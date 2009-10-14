package org.openstreetmap.osmolt;

/**
 * specifies the interface to 
 * 
 * @license GPL. Copyright 2009
 * @author Josias Polchau
 */
public interface OutputInterface {
  
  /**
   * fügt der Statusanzeige ein element hinzu
   */
  void processAdd();
  
  /**
   * method to set the percent of the progress
   * 
   * @param percent
   */
  void processSetPercent(int percent);
  
  /**
   * starts the progress
   */
  void processStart();

  /**
   * starts the progress
   */
  void processStop();
  
  void processSetName(String s);
  
  void processSetStatus(String s);
  
  void osmoltStart();
  
  void osmoltEnd();
  
  void printMessage(String message);
  
  void printDebugMessage(String classname, String message);
  
  void printWarning(String warning);
  
  void printError(String error);
  
  void printError(Throwable error);
  
  void printTranslatedMessage(String message);
  
  void printTranslatedWarning(String warning);
  
  void printTranslatedError(String error);
  
}
