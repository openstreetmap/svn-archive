package org.openstreetmap.josm.plugins.videomapping.video;
import java.awt.Adjustable;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.DateTimeDateFormat;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable ;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.videomapping.VideoObserver;
import static org.openstreetmap.josm.tools.I18n.*;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.VideoMetaData;
import uk.co.caprica.vlcj.player.embedded.*;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.windows.WindowsRuntimeUtil;

//basic class of a videoplayer for one video
public class SimpleVideoPlayer extends JFrame implements MediaPlayerEventListener, WindowListener{
    private EmbeddedMediaPlayer mp;
    private Timer t;
    private JPanel screenPanel,controlsPanel;
    private JSlider timeline;
    private JButton play,back,forward;
    private JToggleButton loop,mute;
    private JSlider speed;
    private Canvas scr;
    private final String[] mediaOptions = {""};
    private boolean syncTimeline=false;
    private boolean looping=false;
    private SimpleDateFormat ms;
    private static final Logger LOG = Logger.getLogger(MediaPlayerFactory.class);
    private int jumpLength=1000;
    private int  loopLength=6000;
    private static Set<VideoObserver> observers = new HashSet<VideoObserver>(); //we have to implement our own Observer pattern
    
    public SimpleVideoPlayer() {
        super();
        try
        {
        	//some workarounds to detect libVLC and DNA on windows        
        	if(RuntimeUtil.isWindows()) {
        		System.setProperty("jna.library.path",WindowsRuntimeUtil.getVlcInstallDir());  //FIXME doesn't work even with this workaround!
            }
        	System.setProperty("logj4.configuration","file:log4j.xml"); //TODO still unsure if we can't link this to the JOSM log4j instance        	        
            //we don't need any options
            String[] libvlcArgs = {""};
            String[] standardMediaOptions = {""};
            //System.out.println("libvlc version: " + LibVlc.INSTANCE.libvlc_get_version());
            //setup Media Player
            //TODO we have to deal with unloading things again ....
            MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(libvlcArgs);
            FullScreenStrategy fullScreenStrategy = new DefaultFullScreenStrategy(this);
            mp = mediaPlayerFactory.newMediaPlayer(fullScreenStrategy);
            mp.setStandardMediaOptions(standardMediaOptions);
            //setup GUI
            setSize(400, 300); //later we apply movie size
            setAlwaysOnTop(true);
            createUI();
            setLayout();
            addListeners(); //registering shortcuts is task of the outer plugin class!
            //embed vlc player
            scr.setVisible(true);
            setVisible(true);
            mp.setVideoSurface(scr);
            mp.addMediaPlayerEventListener(this);
            //mp.pause();
            //jump(0);
            //create updater
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(new Runnable() {
				//We have to do syncing in the main thread
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
				          //here we update
				        public void run() {
				            if (isPlaying()) updateTime(); //if the video is seeking we get a mess
				        }
				      });
				}
			}, 0L, 500L, TimeUnit.MILLISECONDS);
            //setDefaultCloseOperation(EXIT_ON_CLOSE);
            addWindowListener(this);
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

    private void createUI() {
        //setIconImage();
        ms = new SimpleDateFormat("hh:mm:ss");
        scr=new Canvas();
        timeline = new JSlider(0,100,0);
        timeline.setMajorTickSpacing(10);
        timeline.setMajorTickSpacing(5);
        timeline.setPaintTicks(true);
        //TODO we need Icons instead
        play= new JButton(tr("play"));
        back= new JButton("<");
        forward= new JButton(">");
        loop= new JToggleButton(tr("loop"));
        mute= new JToggleButton(tr("mute"));
        speed = new JSlider(-200,200,0);
        speed.setMajorTickSpacing(100);
        speed.setPaintTicks(true);          
        speed.setOrientation(Adjustable.VERTICAL);
        Hashtable labelTable = new Hashtable ();
        labelTable.put( new Integer( 0 ), new JLabel("1x") );
        labelTable.put( new Integer( -200 ), new JLabel("-2x") );
        labelTable.put( new Integer( 200 ), new JLabel("2x") );
        speed.setLabelTable( labelTable );
        speed.setPaintLabels(true);
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
        controlsPanel.add(mute);
        loop.setSelected(false);
        mute.setSelected(false);
    }

    //add UI functionality
    private void addListeners() {
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
                if(mp.isPlaying()) mp.pause(); else mp.play();              
            }
        });
        
        back.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent arg0) {
                backward();
            }
        });
        
        forward.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent arg0) {
                forward();
            }
        });
        
        loop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                loop();
            }
        });
        
        mute.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                mute();
            }
        });
        
        speed.addChangeListener(new ChangeListener() {
            
            public void stateChanged(ChangeEvent arg0) {
                if(!speed.getValueIsAdjusting()&&(mp.isPlaying()))
                {
                    int perc = speed.getValue();
                    float ratio= (float) (perc/400f*1.75);
                    ratio=ratio+(9/8);
                    mp.setRate(ratio);
                }
                
            }
        });
        
    }   

    public void finished(MediaPlayer arg0) {
            
    }

    public void lengthChanged(MediaPlayer arg0, long arg1) {

    }

    public void metaDataAvailable(MediaPlayer arg0, VideoMetaData data) {
        final float perc = 0.5f;
        Dimension org=data.getVideoDimension();
        scr.setSize(new Dimension((int)(org.width*perc), (int)(org.height*perc)));
        pack();
        //send out metadatas to all observers
        for (VideoObserver o : observers) {
            o.metadata(0, hasSubtitles());
        }
    }

    public void paused(MediaPlayer arg0) {

    }

    public void playing(MediaPlayer arg0) {

    }

    public void positionChanged(MediaPlayer arg0, float arg1) {
        
    }

    public void stopped(MediaPlayer arg0) {
                
    }

    public void timeChanged(MediaPlayer arg0, long arg1) {

    }
    

    public void windowActivated(WindowEvent arg0) { }

    public void windowClosed(WindowEvent arg0) {    }

    //we have to unload and disconnect to the VLC engine
    public void windowClosing(WindowEvent evt) {
        if(LOG.isDebugEnabled()) {LOG.debug("windowClosing(evt=" + evt + ")");}
        pause();
        //FIXME stop timers etc.
        mp.release();
        mp = null;
      }

    public void windowDeactivated(WindowEvent arg0) {   }

    public void windowDeiconified(WindowEvent arg0) {   }

    public void windowIconified(WindowEvent arg0) { }

    public void windowOpened(WindowEvent arg0) {    }   
    
    public void setFile(File f)
    {
        String mediaPath = f.getAbsoluteFile().toString();
        mp.playMedia(mediaPath, mediaOptions);
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
    
    public boolean isPlaying()
    {
        return mp.isPlaying();
    }
    
    //gets called by the Syncer thread to update all observers
    public void updateTime ()
    {
        if(mp.isPlaying())
        {
        	long millis=mp.getTime();
        	String s = String.format("%02d:%02d:%02d", //dont know why normal Java date utils doesn't format the time right
		      TimeUnit.MILLISECONDS.toHours(millis),
		      TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), 
		      TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
		    );
            //setTitle(ms.format(new Time(sec)));
        	setTitle(s);
            syncTimeline=true;
            timeline.setValue(Math.round(mp.getPosition()*100));
            syncTimeline=false;
            notifyObservers(mp.getTime());
        }
    }
    
    //allow externals to extend the ui
    public void addComponent(JComponent c)
    {
        controlsPanel.add(c);
        pack();
    }

    public long getLength() {       
        return mp.getLength();
    }

    public void setDeinterlacer(String string) {
        mp.setDeinterlace(string);
        
    }

    public void setJumpLength(Integer integer) {
        jumpLength=integer;
        
    }

    public void setLoopLength(Integer integer) {
        loopLength = integer;
        
    }

    public void loop() {
        if(looping)
        {
            t.cancel();
            looping=false;
        }
        else            
        {
            final long resetpoint=(long) mp.getTime()-loopLength/2;
            TimerTask ani=new TimerTask() {
                
                @Override
                public void run() {
                    mp.setTime(resetpoint);
                }
            };
            t= new Timer();
            t.schedule(ani,loopLength/2,loopLength); //first run a half looptime till reset
            looping=true;
            }
        
    }
    
    protected void mute() {
        mp.mute();
        
    }

    public void forward() {
        mp.setTime((long) (mp.getTime()+jumpLength));
    }

    public void backward() {
        mp.setTime((long) (mp.getTime()-jumpLength));
        
    }

    public void removeVideo() {
        if (mp.isPlaying()) mp.stop();
        mp.release();
        
    }
    
    public void toggleSubs()
    {
        //vlc manages it's subtitles in a own list so we have to cycle trough
        int spu = mp.getSpu();
        if(spu > -1) {
          spu++;
          if(spu > mp.getSpuCount()) {
            spu = -1;
          }
        }
        else {
          spu = 0;
        }
        mp.setSpu(spu);
    }

    public static void addObserver(VideoObserver observer) {

            observers.add(observer);

        }

     

        public static void removeObserver(VideoObserver observer) {

            observers.remove(observer);

        }

        private static void notifyObservers(long newTime) {

            for (VideoObserver o : observers) {
                o.playing(newTime);
            }

        }

        public String getNativePlayerInfos() {
            return "VLC "+LibVlc.INSTANCE.libvlc_get_version();
        }

        public void faster() {
            speed.setValue(speed.getValue()+100);
            
        }

        public void slower() {
            speed.setValue(speed.getValue()-100);
            
        }

        public void pause() {
            if (mp.isPlaying()) mp.pause();
            
        }

        public boolean playing() {
            return mp.isPlaying();
        }

        public void error(MediaPlayer arg0) {
            // TODO Auto-generated method stub
            
        }

        public void mediaChanged(MediaPlayer arg0) {
            // TODO Auto-generated method stub
            
        }

        public boolean hasSubtitles() {
            if (mp.getSpuCount()==0) return false; else   return true;
        }

		public void backward(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}

		public void buffering(MediaPlayer arg0) {
			System.out.println("buffering!");
			
		}

		public void forward(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}

		public void opening(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			System.out.println("opening!");
			
		}

		public void pausableChanged(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		public void seekableChanged(MediaPlayer arg0, int arg1) {
			System.out.println("seeking!");
			
		}

		public void snapshotTaken(MediaPlayer arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		public void titleChanged(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

    

}
