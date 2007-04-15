package annotationtester;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * Fires up the annotation tester
 * @author Immanuel.Scholz
 */
public class AnnotationTesterAction extends JosmAction {

	public AnnotationTesterAction() {
		super(tr("Annotation Preset Tester"), "annotation-tester", tr("Open the annotation preset test tool for previewing annotation preset dialogs."), KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK, true);
		Main.main.menu.helpMenu.addSeparator();
		Main.main.menu.helpMenu.add(this);
	}

	public void actionPerformed(ActionEvent e) {
		String annotationSources = Main.pref.get("annotation.sources");
		if (annotationSources.equals("")) {
			JOptionPane.showMessageDialog(Main.parent, tr("You have to specify annotation sources in the preferences first."));
			return;
		}
		String[] args = annotationSources.split(";");
		new AnnotationTester(args);
	}
}
