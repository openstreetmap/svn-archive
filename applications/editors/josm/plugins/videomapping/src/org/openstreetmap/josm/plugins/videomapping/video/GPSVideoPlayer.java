package org.openstreetmap.josm.plugins.videomapping.video;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.plugins.videomapping.VideoObserver;
import org.openstreetmap.josm.plugins.videomapping.VideoPositionLayer;

import static org.openstreetmap.josm.tools.I18n.*;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;

//extends video playback, major control has the video player
public class GPSVideoPlayer extends VideoPlayer
{
	private List<GPSVideo> videos;
	private VideoPositionLayer videoPositionLayer;

	public GPSVideoPlayer(DateFormat videoTimeFormat,VideoPositionLayer videoPositionLayer) throws HeadlessException {
		super(videoTimeFormat);
		videos = new LinkedList<GPSVideo>();
		this.videoPositionLayer=videoPositionLayer;
		videoPositionLayer.setGPSVideoPlayer(this);
	}


	@Override
	public GPSVideo addVideo(File Videofile) {		
		GPSVideo video = new GPSVideo(super.addVideo(Videofile));
		videos.add(video);
		JButton syncButton= new JButton(tr("Sync"));
		syncButton.setBackground(Color.RED);		
		syncButton.addActionListener(new ActionListener() {
            //do a sync
            public void actionPerformed(ActionEvent e) {
            	GPSVideo v=findVideo((JButton)e.getSource());
            	v.doSync(videoPositionLayer);
            }
		});
		video.SyncComponent=syncButton;
		//video.panel.add(syncButton,BorderLayout.SOUTH);
		controlsPanel.add(syncButton);
		return video;
	}	

	protected GPSVideo findVideo(JButton source) {
		for (GPSVideo v : videos) {
			if (v.SyncComponent==source) return v;
		}
		return null;
	}
	
	public void jumpTo(Date date)
	{
		for (GPSVideo video : videos) {
			video.jumpTo(date);
		}
	}
	
	public boolean areAllVideosSynced()
	{

		for (GPSVideo video : videos) {
			if (!video.isSynced()) return false;
		}
		return true;		
	}
	
	
	
    
    


    
    

    
}
