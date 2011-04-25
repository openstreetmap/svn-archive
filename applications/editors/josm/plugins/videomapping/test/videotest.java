
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.plugins.videomapping.video.VideoEngine;
import org.openstreetmap.josm.plugins.videomapping.video.VideoPlayer;

import uk.co.caprica.vlcj.player.*;


//simple app to test videoplayer alone
public class videotest {
    public static void main(String[] args) {
    	VideoEngine.setupPlayer();
    	VideoPlayer testplayer= new VideoPlayer(new SimpleDateFormat("hh:mm:ss"));
    	testplayer.setJumpLength(1000);
    	testplayer.setLoopLength(3000);
    	testplayer.addVideo(new File("C:\\TEMP\\test.mpg"),"1");
    	testplayer.addVideo(new File("C:\\TEMP\\aoe-drachen_dvdscr.avi"),"2");
    	testplayer.enableSingleVideoMode(true);
        
    }

}
