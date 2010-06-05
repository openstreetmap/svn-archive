
import java.awt.Canvas;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.plugins.videomapping.video.SimpleVideoPlayer;

import uk.co.caprica.vlcj.player.*;


public class videotest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleVideoPlayer sVP = new SimpleVideoPlayer();
		sVP.setFile(new File("C:\\TEMP\\test.mpg"));		
		sVP.play();
		sVP.jump(605000);

	}

}
