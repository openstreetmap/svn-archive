package org.openstreetmap.josm.plugins.videomapping.video;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.openstreetmap.josm.plugins.videomapping.GpsPlayer;


public class GPSVideoPlayer extends SimpleVideoPlayer{
	Timer t;
	TimerTask syncGPSTrack;
	private GpsPlayer pl;
	private GPSVideoFile file;

	public GPSVideoPlayer(File f, final GpsPlayer pl) {
		super();
		this.pl = pl;
		this.file = new GPSVideoFile(f, (long)0);//10,05
		setFile(file.getAbsoluteFile());
		t = new Timer();
		syncGPSTrack= new TimerTask() {
			
			@Override
			public void run() {
				pl.next();
				//pl.jump(file.offset+)
				
			}
		};
		//t.schedule(syncGPSTrack, 1000, 1000);
	}
}
