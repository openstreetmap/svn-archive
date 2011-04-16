package org.openstreetmap.josm.plugins.videomapping.video;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
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
import org.openstreetmap.josm.plugins.videomapping.GpsPlayer;
import org.openstreetmap.josm.plugins.videomapping.VideoObserver;
import org.openstreetmap.josm.plugins.videomapping.VideoPositionLayer;

import static org.openstreetmap.josm.tools.I18n.*;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;

//extends video playback, major control has the video player
public class GPSVideoPlayer extends VideoPlayer
{
	private JPanel syncPanel;
	private List<GPSVideo> videos;
	private List<JButton> syncButtons;
	private VideoPositionLayer videoPositionLayer;

	public GPSVideoPlayer(DateFormat videoTimeFormat,VideoPositionLayer videoPositionLayer) throws HeadlessException {
		super(videoTimeFormat);
		extendUI();
		syncButtons=new LinkedList<JButton>();
		this.videoPositionLayer=videoPositionLayer;
	}

	@Override
	public GPSVideo addVideo(File Videofile) {
		
		GPSVideo video=(GPSVideo) super.addVideo(Videofile);
		videos.add(video);
		JButton syncButton= new JButton(tr("Sync"));
		syncButton.setBackground(Color.RED);
		syncPanel.add(syncButton);
		syncButton.addActionListener(new ActionListener() {
            //do a sync
            public void actionPerformed(ActionEvent e) {
            	GPSVideo v=findVideo((JButton)e.getSource());
            	doSync(v,videoPositionLayer);
            }
		});
		return video;
	}

	protected void doSync(GPSVideo v, VideoPositionLayer layer) {
		WayPoint first=getFirstGPS(v.getTime(),layer.getGPSDate());
		
	}

	//make sure we don't leave the GPS track
	private WayPoint getFirstGPS(long videoTime, java.util.Date gpsDate) {
		Date start=new Date(gpsDate.getTime()-videoTime);
		if(start.before(videoPositionLayer.getFirstWayPoint().getTime()))
		{
			return videoPositionLayer.getFirstWayPoint();
		}
		else
		{
			return videoPositionLayer.getWayPointBefore(start);
		}
	}
	
	//make sure we don't leave the GPS track
	private WayPoint getLastGPS(long videoTime, java.util.Date gpsDate) {
		Date end=new Date(gpsDate.getTime()-videoTime);
		if(end.after(videoPositionLayer.getLastWayPoint().getTime()))
		{
			return videoPositionLayer.getLastWayPoint();
		}
		else
		{
			return videoPositionLayer.getWayPointBefore(end);
		}
	}

	protected GPSVideo findVideo(JButton source) {
		for (GPSVideo v : videos) {
			if (v.SyncComponent==source) return v;
		}
		return null;
	}

	private void extendUI() {
		syncPanel=new JPanel(new FlowLayout());
		screenPanel.add(syncPanel,BorderLayout.EAST);
				
	}
	
	
	
    
    


    
    

    
}
