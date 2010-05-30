import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

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
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

//basic class of a videoplayer for one video
public class SimpleVideoPlayer extends JFrame{
	private MediaPlayer mp;
	private Timer t;
	private JPanel screenPanel,controlsPanel;
	private JSlider timeline;
	private JButton play,back,forward;
	private JToggleButton loop;
	private JSlider speed;
	private Canvas scr;
	
	public SimpleVideoPlayer(JFrame mainwindow) {
		super();
		//TODO new EnvironmentCheckerFactory().newEnvironmentChecker().checkEnvironment();
		try
		{
			String mediaPath = "C:\\Dokumente und Einstellungen\\g\\Eigene Dateien\\Eigene Videos\\23C3-1610-en-fudging_with_firmware.m4v";
			String[] libvlcArgs = {""};
			String[] standardMediaOptions = {""}; 
			String[] mediaOptions = {""};
			System.out.println("libvlc version: " + LibVlc.INSTANCE.libvlc_get_version());
			//setup Media Player
			//TODO we have to deal with unloading things....
			MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(libvlcArgs);
		    FullScreenStrategy fullScreenStrategy = new DefaultFullScreenStrategy(mainwindow);
		    mp = mediaPlayerFactory.newMediaPlayer(fullScreenStrategy);
		    mp.setStandardMediaOptions(standardMediaOptions);
		    //mp.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {});
		    //setup GUI
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
		    //embed player
			scr.setVisible(true);
			setVisible(true);
			mp.setVideoSurface(scr);
		    mp.playMedia(mediaPath, mediaOptions);
		    mainwindow.pack();
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
	}

	//add UI functionality
	private void addListeners() {
		final float JUMP_LENGTH=1000;
		final int  LOOP_LENGTH=6000;
		timeline.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(!timeline.getValueIsAdjusting())
				{
					//recalc to 0.x percent value
					mp.setPosition((float)timeline.getValue() / 100.0f);
				}
			}
		    });
		
		play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mp.play();
				
			}
		});
		
		play.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				mp.play();				
			}
		});
		
		back.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				mp.setTime((long) (mp.getTime()-JUMP_LENGTH));
				
			}
		});
		
		forward.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				mp.setTime((long) (mp.getTime()+JUMP_LENGTH));
				
			}
		});
		
		loop.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
			if(!loop.getModel().isPressed())
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
			else
			{
				t.cancel();
			}
			}
		});
		
		speed.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent arg0) {
				// TODO get integrated with future VLCj relase
				
			}
		});
		
	}
	

}
