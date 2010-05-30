import java.awt.Canvas;
import java.awt.Dimension;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.co.caprica.vlcj.player.*;


public class videotest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame window = new JFrame("video test");
		window.setSize(new Dimension(600,600));
		window.setVisible(true);
		SimpleVideoPlayer sVP = new SimpleVideoPlayer(window);
		window.add(sVP);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}
