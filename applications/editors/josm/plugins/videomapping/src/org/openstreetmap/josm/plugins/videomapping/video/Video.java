package org.openstreetmap.josm.plugins.videomapping.video;

import java.awt.Canvas;
import java.io.File;

import uk.co.caprica.vlcj.player.MediaPlayer;

public class Video {
	public File filename;
	public MediaPlayer player;
	public Canvas canvas;
	
	public Video(File filename, Canvas canvas) {
		this.filename=filename;
		this.canvas=canvas;
	}
	
	public long getTime()
	{
		return player.getTime();
	}
	

}
