package org.openstreetmap.josm.plugins.videomapping.video;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.plugins.videomapping.GpsPlayer;

//combines video and GPS playback
public class GPSVideoPlayer{
	Timer t;
	TimerTask syncGPSTrack;
	private GpsPlayer gps;
	private SimpleVideoPlayer video;
	private JButton syncBtn;
	private GPSVideoFile file;
	private boolean synced=false; //do we playback the players together?
	

	public GPSVideoPlayer(File f, final GpsPlayer pl) {
		super();
		this.gps = pl;
		//test sync
		video = new SimpleVideoPlayer();
		/*
		long gpsT=(9*60+20)*1000;
		long videoT=10*60*1000+5*1000;
		setFile(new GPSVideoFile(f, gpsT-videoT)); */
		setFile(new GPSVideoFile(f, 0L));
		//extend GUI
		syncBtn= new JButton("sync");
		syncBtn.setBackground(Color.RED);
		syncBtn.addActionListener(new ActionListener() {
			//do a sync
			public void actionPerformed(ActionEvent e) {
				long diff=gps.getRelativeTime()-video.getTime();
				file= new GPSVideoFile(file, diff);
				syncBtn.setBackground(Color.GREEN);
				synced=true;
				gps.play();
			}
		});
		setSyncMode(true);
		video.addComponent(syncBtn);
		t = new Timer();		
	}
	
	public void setSyncMode(boolean b)
	{
		if(b)
		{
			syncBtn.setVisible(true);
		}
		else
		{
			syncBtn.setVisible(false);
		}
	}
	
		
	public void setFile(GPSVideoFile f)
	{
		
		file=f;
		video.setFile(f.getAbsoluteFile());
		video.play();
	}
	
	public void play(long gpsstart)
	{
		jumpToGPSTime(gpsstart);
		gps.jump(gpsstart);
		gps.play();
	}
	
	//jumps in video to the corresponding linked time
	public void jumpToGPSTime(long gpsT)
	{
		video.jump(getVideoTime(gpsT));
	}
	
	//calc synced timecode from video
	private long getVideoTime(long GPStime)
	{
		return GPStime-file.offset;
	}
	
	//calc corresponding GPS time
	private long getGPSTime(long videoTime)
	{
		return videoTime+file.offset;
	}

	//when we clicked on the layer, here we update the video position
	public void notifyGPSClick() {
		if(synced) jumpToGPSTime(gps.getRelativeTime());
		
	}
	
}
