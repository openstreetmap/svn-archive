
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
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
		final SimpleVideoPlayer sVP = new SimpleVideoPlayer();
		sVP.setFile(new File("C:\\TEMP\\test.mpg"));
		sVP.play(); //FIXME We have a bug so we get out of sync if we jump before the video is up (and this we CAN'T DETECT!!!)
		JButton b = new JButton("jump");
		b.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				sVP.jump(610000);				
			}
		});
		sVP.add(b);

	}

}
