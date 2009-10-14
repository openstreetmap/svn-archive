package org.openstreetmap.osmolt;

public interface InputOutputAccess {
	void processAdd();

	void processSetPercent(int percent);

	void processStart();

	void processStop();

	void processSetName(String s);
	void processSetStatus(String s); 
	void osmoltStart();

	void osmoltEnd();

	void printMessage(String message);

	void printWarning(String warning);

  void printError(String error);
  void printError(Throwable error);

	void printTranslatedMessage(String message);

	void printTranslatedWarning(String warning);

	void printTranslatedError(String error);

}
