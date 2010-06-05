package org.openstreetmap.josm.plugins.videomapping.video;
import java.awt.Adjustable;
import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.Main;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.check.EnvironmentCheckerFactory;
import uk.co.caprica.vlcj.player.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.FullScreenStrategy;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.VideoMetaData;

//basic class of a videoplayer for one video
public class SimpleVideoPlayer extends JFrame implements MediaPlayerEventListener, WindowListener{
	private MediaPlayer mp;
	private Timer t;
	private JPanel screenPanel,controlsPanel;
	private JSlider timeline;
	private JButton play,back,forward;
	private JToggleButton loop;
	private JSlider speed;
	private Canvas scr;
	private final String[] mediaOptions = {""};
	private boolean syncTimeline=false;
	private SimpleDateFormat df;
	private static final Logger LOG = Logger.getLogger(SimpleVideoPlayer.class);
	
	public SimpleVideoPlayer() {
		super();
		/*TODO new EnvironmentCheckerFactory().newEnvironmentChecker().checkEnvironment();
		 * if(RuntimeUtil.isWindows()) {
	  			vlcArgs.add("--plugin-path=" + WindowsRuntimeUtil.getVlcInstallDir() + "\\plugins");
			}
		 */
		try
		{			
			String[] libvlcArgs = {""};
			String[] standardMediaOptions = {""}; 
			
			System.out.println("libvlc version: " + LibVlc.INSTANCE.libvlc_get_version());
			//setup Media Player
			//TODO we have to deal with unloading things....
			MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(libvlcArgs);
		    FullScreenStrategy fullScreenStrategy = new DefaultFullScreenStrategy(this);
		    mp = mediaPlayerFactory.newMediaPlayer(fullScreenStrategy);
		    mp.setStandardMediaOptions(standardMediaOptions);
		    //setup GUI
		    setSize(400, 300);
		    df = new SimpleDateFormat("hh:mm:ss:S");
		    scr=new Canvas();
		    timeline = new JSlider(0,100,0);
		    play= new JButton("play");
		    back= new JButton("<");
		    forward= new JButton(">");
		    loop= new JToggleButton("loop");
		    speed = new JSlider(0,2,1);
			speed.setPaintTicks(true);
			speed.setMajorTickSpacing(5);
			speed.setOrientation(Adjustable.VERTICAL);
		    setLayout();
			addListeners();
		    //embed vlc player
			scr.setVisible(true);
			setVisible(true);
			mp.setVideoSurface(scr);
			mp.addMediaPlayerEventListener(this);
			mp.pause();
			jump(0);
			//set updater
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleAtFixedRate(new Syncer(this), 0L, 500L, TimeUnit.MILLISECONDS);
		    //setDefaultCloseOperation(EXIT_ON_CLOSE);
			addWindowListener(this);
		}
		catch (NoClassDefFoundError e)
		{
			System.err.println("Unable to find JNA Java library!");
		}
		catch (UnsatisfiedLinkError e)
		{
			System.err.println("Unable to find native libvlc library!");
		}
		
	}
	
	//creates a layout like the most mediaplayers are...
	private void setLayout() {
		this.setLayout(new BorderLayout());
		screenPanel=new JPanel();
		screenPanel.setLayout(new BorderLayout());
		controlsPanel=new JPanel();
		controlsPanel.setLayout(new FlowLayout());
		add(screenPanel,BorderLayout.CENTER);
		add(controlsPanel,BorderLayout.SOUTH);
		//fill screen panel
		screenPanel.add(scr,BorderLayout.CENTER);
		screenPanel.add(timeline,BorderLayout.SOUTH);
		screenPanel.add(speed,BorderLayout.EAST);
		controlsPanel.add(play);
		controlsPanel.add(back);
		controlsPanel.add(forward);
		controlsPanel.add(loop);
		loop.setSelected(false);
	}

	//add UI functionality
	private void addListeners() {
		final float JUMP_LENGTH=1000;
		final int  LOOP_LENGTH=6000;
		timeline.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(!syncTimeline) //only if user moves the slider by hand
				{
					if(!timeline.getValueIsAdjusting()) //and the slider is fixed
					{
						//recalc to 0.x percent value
						mp.setPosition((float)timeline.getValue()/100.0f);
					}					
				}
			}
		    });
		
		play.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				if(mp.isPlaying()) mp.stop(); else mp.play();				
			}
		});
		
		back.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				mp.setTime((long) (mp.getTime()-JUMP_LENGTH));
				//jump(600000); //10,05
				
			}
		});
		
		forward.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				mp.setTime((long) (mp.getTime()+JUMP_LENGTH));
				
			}
		});
		
		loop.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
			if(!loop.isSelected())
			{
				t.cancel();
			}
			else			
			{
				final long resetpoint=(long) mp.getTime()-LOOP_LENGTH/2;
				TimerTask ani=new TimerTask() {
					
					@Override
					public void run() {
						mp.setTime(resetpoint);
					}
				};
				t= new Timer();
				t.schedule(ani,LOOP_LENGTH/2,LOOP_LENGTH); //first run a half looptime till reset	
				}
			}
		});
		
		speed.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent arg0) {
				// TODO change playback speed
				
			}
		});
		
	}

	public void finished(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	public void lengthChanged(MediaPlayer arg0, long arg1) {
		// TODO Auto-generated method stub
		
	}

	public void metaDataAvailable(MediaPlayer arg0, VideoMetaData arg1) {
		// TODO Auto-generated method stub
		
	}

	public void paused(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	public void playing(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	public void positionChanged(MediaPlayer arg0, float arg1) {
		
	}

	public void stopped(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	public void timeChanged(MediaPlayer arg0, long arg1) {
		// TODO Auto-generated method stub
		
	}
	

	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	//we have to unload and disconnect to the VLC engine
	public void windowClosing(WindowEvent evt) {
        if(LOG.isDebugEnabled()) {LOG.debug("windowClosing(evt=" + evt + ")");}
        mp.release();
        mp = null;
        System.exit(0);
      }

	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}	
	
	public void setFile(File f)
	{
		String mediaPath = f.getAbsoluteFile().toString();
		mp.playMedia(mediaPath, mediaOptions);
		jump(8000);
		jump(0);
		mp.stop();
		pack();
	}
	
	public void play()
	{
		mp.play();
	}
	
	public void jump(long time)
	{
		/*float pos = (float)mp.getLength()/(float)time;
		mp.setPosition(pos);*/
		mp.setTime(time);
	}
	
	public long getTime()
	{
		return mp.getTime();
	}
	
	public float getPosition()
	{
		return mp.getPosition();
	}
	
	//gets called by the Syncer to update all components
	public void updateTime ()
	{
		if(mp.isPlaying())
		{
			setTitle(df.format(new Date(mp.getTime()))+" "+mp.getTime());
			syncTimeline=true;
			timeline.setValue(Math.round(mp.getPosition()*100));
			syncTimeline=false;
		}
	}
	

}
