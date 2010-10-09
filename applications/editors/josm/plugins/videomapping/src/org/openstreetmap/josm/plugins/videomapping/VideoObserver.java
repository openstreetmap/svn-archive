package org.openstreetmap.josm.plugins.videomapping;

//an Interface for communication for both players
public interface VideoObserver {
    void playing(long time);
    void jumping(long time);
    void metadata(long time,boolean subtitles);

}
