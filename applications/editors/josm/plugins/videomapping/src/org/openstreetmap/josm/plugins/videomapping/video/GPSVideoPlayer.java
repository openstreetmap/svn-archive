package org.openstreetmap.josm.plugins.videomapping.video;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.plugins.videomapping.GpsPlayer;
import org.openstreetmap.josm.plugins.videomapping.PlayerObserver;

//combines video and GPS playback, major control has the video player
public class GPSVideoPlayer implements PlayerObserver{
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
				markSyncedPoints();
				gps.play();
			}
		});
		setSyncMode(true);
		video.addComponent(syncBtn);
		//allow sync
		SimpleVideoPlayer.addObserver(new PlayerObserver() {

			public void playing(long time) {
				//sync the GPS back
				if(synced) gps.jump(getGPSTime(time));
				
			}

			public void jumping(long time) {
			
			}
			
			
		});
		t = new Timer();		
	}
	
	//marks all points that are covered by video AND GPS track
	private void markSyncedPoints() {
		long time;
		//TODO this is poor, a start/end calculation would be better
		for (WayPoint wp : gps.getTrack()) {
			time=getVideoTime(gps.getRelativeTime(wp));
			if(time>0) wp.attr.put("synced", "true");
		}
		
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
		//video is already playing
		jumpToGPSTime(gpsstart);
		gps.jump(gpsstart);
		//gps.play();
	}
	
	public void play()
	{
		video.play();
	}
	
	public void pause()
	{
		video.pause();
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

	

	public void setJumpLength(Integer integer) {
		video.setJumpLength(integer);
		
	}

	public void setLoopLength(Integer integer) {
		video.setLoopLength(integer);
		
	}

	public void loop() {
		video.loop();
		
	}

	public void forward() {
		video.forward();
		
	}

	public void backward() {
		video.backward();
		
	}

	public void removeVideo() {
		video.removeVideo();
		
	}

	public File getVideo() {
		return file;
	}

	public float getCoverage() {
		return gps.getLength()/video.getLength();
	}

	public void setDeinterlacer(String string) {
		video.setDeinterlacer(string);
		
	}

	public void setAutoCenter(boolean selected) {
		gps.setAutoCenter(selected);
		
	}

	
	//not called by GPS
	public boolean playing() {
		return video.playing();
	}

	//when we clicked on the layer, here we update the video position
	public void jumping(long time) {
		if(synced) jumpToGPSTime(gps.getRelativeTime());
		
	}

	public String getNativePlayerInfos() {
		return video.getNativePlayerInfos();
	}

	public void faster() {
		video.faster();
		
	}

	public void slower() {
		video.slower();
		
	}

	public void playing(long time) {
		// TODO Auto-generated method stub
		
	}

	
}
