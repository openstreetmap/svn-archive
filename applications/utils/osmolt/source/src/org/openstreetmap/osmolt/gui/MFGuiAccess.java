package org.openstreetmap.osmolt.gui;

import org.jdom.Element;
import org.openstreetmap.osmolt.OutputInterface;


public interface MFGuiAccess extends OutputInterface {
	void setWorkFilter(Element filter);
	Element getWorkFilter();
	void updateGui();
	String getLookAndFeelClassName();

	String translate(String s);
	void loadFilter();
	void applyChanges();
}
