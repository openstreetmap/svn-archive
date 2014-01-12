package org.openstreetmap.josm.plugins.videomapping.video;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Window;
import java.util.LinkedList;
import java.util.List;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.DeinterlaceMode;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.windows.WindowsRuntimeUtil;

import com.sun.jna.NativeLibrary;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

//concrete Player library that is able to playback multiple videos
public class VideoEngine implements MediaPlayerEventListener {
	private FullScreenStrategy fullScreenStrategy;
	public MediaPlayerFactory mediaPlayerFactory;
	private List<Video> videos;
	private List<VideosObserver> observers;
	private final String[] libvlcArgs = {""};
    private final String[] standardMediaOptions = {""};
    private final static String[] deinterlacers = {"bob","linear"};
    //private final float initialCanvasFactor = 0.5f;
	private boolean singleVideoMode; //commands will only affect the last added video
	private Video lastAddedVideo;
	
	//called at plugin start to setup library
	public static void setupPlayer() {
	    String vlcInstallDir = null;
	    
	    if (RuntimeUtil.isWindows()) {
	        vlcInstallDir = WindowsRuntimeUtil.getVlcInstallDir();
	        String arch = System.getProperty("os.arch");
	        if (vlcInstallDir == null && arch.equals("amd64")) {
	            try {
	                vlcInstallDir = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, 
	                        WindowsRuntimeUtil.VLC_REGISTRY_KEY.replaceFirst("\\\\", "\\\\Wow6432Node\\\\"), 
	                        WindowsRuntimeUtil.VLC_INSTALL_DIR_KEY);
	            } catch (RuntimeException e) {
	                System.err.println(e.getMessage());
	            }
	        }
	    } else if (RuntimeUtil.isMac()) {
	        // TODO
	    } else if (RuntimeUtil.isNix()) {
            // TODO
	    }
	    
        if (vlcInstallDir != null) {
            System.out.println("videomapping: found VLC install dir: "+vlcInstallDir);
            NativeLibrary.addSearchPath("libvlc", vlcInstallDir);
        } else {
            System.err.println("videomapping: unable to locate VLC install dir");
        }
	}
	
	public VideoEngine(Window parent)
	{
		System.setProperty("logj4.configuration","file:log4j.xml"); //TODO still unsure if we can't link this to the JOSM log4j instance
		videos = new LinkedList<Video>();
		observers = new LinkedList<VideosObserver>();
		try {
			mediaPlayerFactory = new MediaPlayerFactory(libvlcArgs);
	        fullScreenStrategy = new DefaultFullScreenStrategy(parent);
		} catch (NoClassDefFoundError e) {
            System.err.println(tr("Unable to find JNA Java library!"));
        } catch (UnsatisfiedLinkError e) {
            System.err.println(tr("Unable to find native libvlc library!"));
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
	}
	
	public void add(Video video)
	{
		try
		{
			EmbeddedMediaPlayer mp = mediaPlayerFactory.newEmbeddedMediaPlayer(fullScreenStrategy);
			video.player=mp;
			mp.setStandardMediaOptions(standardMediaOptions);
			videos.add(video);
			lastAddedVideo=video;
			mp.setVideoSurface(video.videoSurface);
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
	/*
	private Video getVideo(MediaPlayer mp)
	{
		for (Video video : videos) {
			if (video.player==mp) return video;
		}
		return null;
	}
*/
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
		
	
	public void setDeinterlacer (DeinterlaceMode deinterlacer)
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
			video.videoSurface=null;
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

        @Override
	public void backward(MediaPlayer arg0) { }
/*
	public void buffering(MediaPlayer arg0) {
		
	}
*/
        @Override
	public void error(MediaPlayer arg0) { }

        @Override
	public void finished(MediaPlayer arg0) { }

        @Override
	public void forward(MediaPlayer arg0) { }

        @Override
	public void lengthChanged(MediaPlayer arg0, long arg1) { }
/*
	public void mediaChanged(MediaPlayer arg0) {
		
	}

	public void metaDataAvailable(MediaPlayer mp, VideoMetaData data) {
		Dimension org=data.getVideoDimension();
		getVideo(mp).canvas.setSize(new Dimension((int)(org.width*initialCanvasFactor), (int)(org.height*initialCanvasFactor)));
		notifyObservers(VideoObserversEvents.resizing);		
	}
*/
        @Override
	public void opening(MediaPlayer arg0) {	}

        @Override
	public void pausableChanged(MediaPlayer arg0, int arg1) { }

        @Override
	public void paused(MediaPlayer arg0) { }

        @Override
	public void playing(MediaPlayer arg0) { }

        @Override
	public void positionChanged(MediaPlayer arg0, float arg1) { }

        @Override
	public void seekableChanged(MediaPlayer arg0, int arg1) { }

        @Override
	public void snapshotTaken(MediaPlayer arg0, String arg1) { }

        @Override
	public void stopped(MediaPlayer arg0) { }

        @Override
	public void timeChanged(MediaPlayer arg0, long arg1) { }

        @Override
	public void titleChanged(MediaPlayer arg0, int arg1) { }

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

    @Override
    public void mediaChanged(MediaPlayer mediaPlayer, libvlc_media_t media, String mrl) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void buffering(MediaPlayer mediaPlayer, float newCache) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mediaSubItemAdded(MediaPlayer mediaPlayer, libvlc_media_t subItem) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mediaParsedChanged(MediaPlayer mediaPlayer, int newStatus) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mediaFreed(MediaPlayer mediaPlayer) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mediaStateChanged(MediaPlayer mediaPlayer, int newState) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void newMedia(MediaPlayer mediaPlayer) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void subItemPlayed(MediaPlayer mediaPlayer, int subItemIndex) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void subItemFinished(MediaPlayer mediaPlayer, int subItemIndex) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void endOfSubItems(MediaPlayer mediaPlayer) {
        // TODO Auto-generated method stub
        
    }

}
