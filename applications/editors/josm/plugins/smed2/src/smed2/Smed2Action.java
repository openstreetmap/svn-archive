package smed2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.Main;

import panels.PanelMain;

public class Smed2Action extends JosmAction {

	private static final long serialVersionUID = 1L;
	private static String editor = tr("SeaMap Editor");
	private JFrame frame = null;
	private boolean isOpen = false;
	public PanelMain panelMain = null;
	public ImageryLayer rendering;

	public Smed2Action() {
		super(editor, "Smed2", editor, null, true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!isOpen) createFrame();
				else frame.toFront();
		        isOpen = true;
			}
		});
	}

	protected void createFrame() {
		frame = new JFrame(editor);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setResizable(true);
		frame.setAlwaysOnTop(false);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeDialog();
			}
		});
		frame.setSize(new Dimension(480, 480));
		frame.setVisible(true);
		panelMain = new PanelMain();
		frame.add(panelMain);
System.out.println("hello");
		rendering = ImageryLayer.create(new ImageryInfo("OpenSeaMap"));
		Main.main.addLayer(rendering);		
	}

	public void closeDialog() {
		if (isOpen) {
			Main.main.removeLayer(rendering);
			frame.setVisible(false);
			frame.dispose();
		}
		isOpen = false;
	}

}
