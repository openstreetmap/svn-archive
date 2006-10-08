package plastic_laf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAction.DownloadTask;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

public class Plugin {

	public Plugin() {
		try {
			UIManager.getDefaults().put("ClassLoader", getClass().getClassLoader());
			UIManager.setLookAndFeel(new PlasticLookAndFeel());

			SwingUtilities.updateComponentTreeUI(Main.parent);
			SwingUtilities.updateComponentTreeUI(Main.pleaseWaitDlg);
			for (DownloadTask task : Main.main.downloadAction.downloadTasks)
				SwingUtilities.updateComponentTreeUI(task.getCheckBox());
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new RuntimeException(e);
		}
	}
}
