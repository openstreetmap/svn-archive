package org.openstreetmap.josm.plugins.videomapping.video;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.JPanel;

import uk.co.caprica.vlcj.player.MediaPlayer;

public class Video {
	public File filename;
	public MediaPlayer player;
	public Canvas canvas;
	public JPanel panel;
	
	public Video(File filename) {
		this.filename=filename;
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
