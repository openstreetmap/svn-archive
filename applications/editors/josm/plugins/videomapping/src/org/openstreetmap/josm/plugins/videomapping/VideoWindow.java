package org.openstreetmap.josm.plugins.videomapping;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.openstreetmap.josm.Main;

import uk.co.caprica.vlcj.player.*;

public class VideoWindow extends JFrame {

	private static final long serialVersionUID = 2099614201397118560L;

	public VideoWindow(File vfile) throws HeadlessException {
		super();
		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(null);
		FullScreenStrategy fullScreenStrategy = new DefaultFullScreenStrategy(this);
		MediaPlayer mediaPlayer = mediaPlayerFactory.newMediaPlayer(fullScreenStrategy);
		mediaPlayer.setStandardMediaOptions(null);
		//mediaPlayer.addMediaPlayerEventListener
		Canvas videoSurface = new Canvas();		
		mediaPlayer.setVideoSurface(videoSurface);
		add(videoSurface);
		try {
			mediaPlayer.playMedia(vfile.getCanonicalPath(),null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setVisible(true);		
		mediaPlayer.release();
		mediaPlayerFactory.release();
	}

}
