package org.openstreetmap.josm.plugins.videomapping.video;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_player_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.VideoMetaData;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.runtime.windows.WindowsCanvas;
import uk.co.caprica.vlcj.runtime.windows.WindowsRuntimeUtil;

import static org.openstreetmap.josm.tools.I18n.*;

//concrete Player library that is able to playback multiple videos
public class VideoEngine implements MediaPlayerEventListener{
	private FullScreenStrategy fullScreenStrategy;
	private MediaPlayerFactory mediaPlayerFactory;
	private List<Video> videos;
	private List<VideosObserver> observers;
	private final String[] libvlcArgs = {""};
    private final String[] standardMediaOptions = {""};
    private final static String[] deinterlacers = {"bob","linear"};
    private final float initialCanvasFactor = 0.5f;
	private boolean singleVideoMode; //commands will only affect the last added video
	private Video lastAddedVideo;
	
	//called at plugin start to setup library
	public static void setupPlayer()
	{
		NativeLibrary.addSearchPath("libvlc", WindowsRuntimeUtil.getVlcInstallDir());
	}
	
	public VideoEngine(Window parent)
	{
		System.setProperty("logj4.configuration","file:log4j.xml"); //TODO still unsure if we can't link this to the JOSM log4j instance
		videos = new LinkedList<Video>();
		observers = new LinkedList<VideosObserver>();
		try
		{
			mediaPlayerFactory = new MediaPlayerFactory(libvlcArgs);
	        fullScreenStrategy = new DefaultFullScreenStrategy(parent);
		}
		catch (NoClassDefFoundError e)
        {
            System.err.println(tr("Unable to find JNA Java library!"));
        }
        catch (UnsatisfiedLinkError e)
        {
            System.err.println(tr("Unable to find native libvlc library!"));
        }        
	}
	
	public void add(Video video)
	{
		try
		{
			EmbeddedMediaPlayer mp;
			mp = mediaPlayerFactory.newMediaPlayer(fullScreenStrategy);
			video.player=mp;
			mp.setStandardMediaOptions(standardMediaOptions);
			videos.add(video);
			lastAddedVideo=video;
			mp.setVideoSurface(video.canvas);
	        mp.addMediaPlayerEventListener(this);
	        String mediaPath = video.filename.getAbsoluteFile().toString();
	        mp.playMedia(mediaPath); 
	        //now fetching and playback starts automatically			
		}
		catch (NoClassDefFoundError e)
        {
            System.err.println(tr("Unable to find JNA Java library!"));
        }
        catch (UnsatisfiedLinkError e)
        {
            System.err.println(tr("Unable to find native libvlc library!"));
        }
	}
	
	private Video getVideo(MediaPlayer mp)
	{
		for (Video video : videos) {
			if (video.player==mp) return video;
		}
		return null;
	}

	public List<Video> getVideos() {		
		return videos;
	}

	public void play()	
	{
		if (singleVideoMode)
		{
			lastAddedVideo.player.play();
		}
		else
		{
			for (Video video : videos) {
				video.player.play();
			}
		}
		System.out.println("abspielen");
	}
	
	//toggles pause and play 
	public void pause()
	{
		if (singleVideoMode)
		{
			lastAddedVideo.player.pause();
		}
		else
		{
			for (Video video : videos) {
				video.player.pause();
			}
		}
	}
	
	//ensures that all stop
	public void pauseAll() {
		for (Video video : videos) {
			if (video.player.isPlaying())
				video.player.pause();
		}	
	}

	//jumps relative for ms in all videos
	public void jumpFor(long ms) {
		if (singleVideoMode)
		{
			long start=lastAddedVideo.player.getTime();
			lastAddedVideo.player.setTime(start+ms);
		}
		else
		{
			for (Video video : videos) {
				long start=video.player.getTime();
				video.player.setTime(start+ms);
			}
		}
		notifyObservers(VideoObserversEvents.jumping);		
	}

	//jumps in all videos to this absolute video time
	public void jumpTo(long msVideo)
	{
		if (singleVideoMode)
		{
			lastAddedVideo.player.setTime(msVideo);
		}
		else
		{
			for (Video video : videos) {
				video.player.setTime(msVideo);
			}
		}
		notifyObservers(VideoObserversEvents.jumping);
	}
			
	//TODO muss evtl. auf Rückgabe für alle Videos erweitert werden
	public long getVideoTime()
	{
		return videos.get(0).player.getTime();
	}
	
	//jumps in all videos to this absolute video time percentage
	public void jumpToPosition(int percent)
	{
		float position = ((float)percent/100f);
		if (singleVideoMode)
		{
			lastAddedVideo.player.setPosition(position);
		}
		else
		{
			for (Video video : videos) {
				video.player.setPosition(position);
			}
		}
		notifyObservers(VideoObserversEvents.jumping);
	}
	
	//TODO muss evtl. auf Rückgabe für alle Videos erweitert werden
	public int getPosition()
	{
		return (int) (videos.get(0).player.getPosition()*100);
	}	
	
	public void setSpeed(int percent)
	{
		if (singleVideoMode)
		{
			lastAddedVideo.player.setRate((float)(percent/100f));
		}
		for (Video video : videos) {
			video.player.setRate((float)(percent/100f));
		}
		notifyObservers(VideoObserversEvents.speeding);
	}
	
	//TODO muss evtl. auf Rückgabe für alle Videos erweitert werden
	public int getSpeed()
	{
		return (int) (videos.get(0).player.getRate()*100);
	}
	
	//returns if at least one video has subtitles
	public boolean hasSubtitles()
	{
		for (Video video : videos) {
			if (video.player.getSpuCount()>0) return true;
		}
		return false;
	}
	
	
	public void setSubtitles (boolean enabled)
	{
		if (enabled)
		{
			//VLC uses a list of sub picture units
			for (Video video : videos) {
				video.player.setSpu(0);
			}
		}
		else
		{
			for (Video video : videos) {
				video.player.setSpu(-1);
			}
		}
	}
		
	
	public void setDeinterlacer (String deinterlacer)
	{
		if (singleVideoMode)
		{
			lastAddedVideo.player.setDeinterlace(deinterlacer);
		}
		else
		{
			for (Video video : videos) {
				video.player.setDeinterlace(deinterlacer);
			}
		}
	}
	
	public static String[] getDeinterlacers()
	{
		return deinterlacers;
	}
	
	public void mute()
	{
		if (singleVideoMode)
		{
			lastAddedVideo.player.mute();
		}
		for (Video video : videos) {
			video.player.mute();
		}
	}
	
	public void unload()
	{
		for (Video video : videos) {
			video.player.stop();
			video.player.release();
			video.player=null;
			video.canvas=null;
		}
		mediaPlayerFactory.release();        
	}

	public void addObserver(VideosObserver observer) {
		observers.add(observer);
		
	}

	private void notifyObservers(VideoObserversEvents event)
	{
		for (VideosObserver observer : observers) {
			observer.update(event);
		}
	}

	public void backward(MediaPlayer arg0) {
		
	}

	public void buffering(MediaPlayer arg0) {
		
	}

	public void error(MediaPlayer arg0) {
		
	}

	public void finished(MediaPlayer arg0) {
		
	}

	public void forward(MediaPlayer arg0) {
		
	}

	public void lengthChanged(MediaPlayer arg0, long arg1) {
		
	}

	public void mediaChanged(MediaPlayer arg0) {
		
	}

	public void metaDataAvailable(MediaPlayer mp, VideoMetaData data) {
		Dimension org=data.getVideoDimension();
		getVideo(mp).canvas.setSize(new Dimension((int)(org.width*initialCanvasFactor), (int)(org.height*initialCanvasFactor)));
		notifyObservers(VideoObserversEvents.resizing);		
	}

	public void opening(MediaPlayer arg0) {
		
	}

	public void pausableChanged(MediaPlayer arg0, int arg1) {
		
	}

	public void paused(MediaPlayer arg0) {
			
	}

	public void playing(MediaPlayer arg0) {
		
	}

	public void positionChanged(MediaPlayer arg0, float arg1) {
		
	}

	public void seekableChanged(MediaPlayer arg0, int arg1) {
		
	}

	public void snapshotTaken(MediaPlayer arg0, String arg1) {
		
	}

	public void stopped(MediaPlayer arg0) {
		
	}

	public void timeChanged(MediaPlayer arg0, long arg1) {
		
	}

	public void titleChanged(MediaPlayer arg0, int arg1) {
		
	}

	public boolean isNoVideoPlaying() {
		for (Video video : videos) {
			if (video.player.isPlaying())
				return false;
		}
		return true;
	}

	public void enableSingleVideoMode(boolean enabled) {
		singleVideoMode = true;
		
	}

}
