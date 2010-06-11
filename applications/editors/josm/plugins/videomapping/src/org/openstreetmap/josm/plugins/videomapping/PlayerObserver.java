package org.openstreetmap.josm.plugins.videomapping;

//an Interface for communication for both players
public interface PlayerObserver {
	void paused();
	void start_playing();
	void jumping(long relTime);
	void changeSpeed(float ratio);

}
