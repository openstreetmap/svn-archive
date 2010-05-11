package org.openstreetmap.josm.plugins.videomapping;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;

import javax.swing.JFrame;

import org.openstreetmap.josm.Main;

public class VideoWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2099614201397118560L;

	public VideoWindow(File vfile) throws HeadlessException {
		super();
		try {
			setTitle(vfile.getCanonicalPath());
			setSize(200,200);			
			setAlwaysOnTop(true);
			
			try {
				setLayout( new BorderLayout());
				Player mediaPlayer = Manager.createRealizedPlayer( vfile.toURL() );
				 Component video = mediaPlayer.getVisualComponent();
				 Component controls = mediaPlayer.getControlPanelComponent();
				add( video, BorderLayout.CENTER );
				add( controls, BorderLayout.SOUTH );
			} catch (NoPlayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CannotRealizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setVisible(true);		
		
	}

}
