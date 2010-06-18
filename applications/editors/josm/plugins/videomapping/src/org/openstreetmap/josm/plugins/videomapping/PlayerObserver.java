package org.openstreetmap.josm.plugins.videomapping;

//an Interface for communication for both players
public interface PlayerObserver {
	void playing(long time);
	void jumping(long time);

}
