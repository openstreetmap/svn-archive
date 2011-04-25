package org.openstreetmap.josm.plugins.videomapping.video;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.JPanel;

import uk.co.caprica.vlcj.player.MediaPlayer;

//basic informations about one single video playback instance
public class Video {
	public File filename;
	public String id; //unique id to make it easy to identify
	public MediaPlayer player;
	public Canvas canvas;
	public JPanel panel;	
	
	public Video(File filename, String id) {
		this.filename=filename;
		this.id=id;
		canvas=new Canvas();
		panel=new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(canvas,BorderLayout.CENTER);
	}
	
	public long getCurrentTime()
	{
		return player.getTime();
	}
	

}
