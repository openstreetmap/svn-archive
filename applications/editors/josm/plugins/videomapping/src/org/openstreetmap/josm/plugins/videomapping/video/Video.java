package org.openstreetmap.josm.plugins.videomapping.video;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.JPanel;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

//basic informations about one single video playback instance
public class Video {
	public final File filename;
	public final String id; //unique id to make it easy to identify
	public MediaPlayer player;
	public Canvas canvas;
	public CanvasVideoSurface videoSurface;
	public final JPanel panel;
	public final MediaPlayerFactory mediaPlayerFactory;
	
	public Video(File filename, String id, MediaPlayerFactory mediaPlayerFactory) {
		this.filename = filename;
		this.id = id;
		this.mediaPlayerFactory = mediaPlayerFactory;
		this.canvas = new Canvas();
		this.videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
		this.panel = new JPanel();
		this.panel.setLayout(new FlowLayout());
		this.panel.add(videoSurface.canvas(), BorderLayout.CENTER);
	}
	
	public long getCurrentTime() {
		return player.getTime();
	}
}
