package org.openstreetmap.josm.plugins.pt_assistant.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class IncompleteMembersDownloadDialog extends JPanel {

	private static final long serialVersionUID = -4275151182361040329L;

	// indicates if the user needs to be asked before fetching incomplete
	// members of a relation.
	private enum ASK_TO_FETCH {
		DO_ASK, DONT_ASK_AND_FETCH, DONT_ASK_AND_DONT_FETCH
	};

	// by default, the user should be asked
	private static ASK_TO_FETCH askToFetch = ASK_TO_FETCH.DO_ASK;

	String message;
	private JCheckBox checkbox;
	private String[] options;
	private int selectedOption;

	public IncompleteMembersDownloadDialog(long id) {
		selectedOption = Integer.MIN_VALUE;

//		message = tr("The relation (id={0}) has incomplete members.\n"+
//		    "They need to be downloaded to proceed with validation of this relation.\nDo you want to download incomplete members?", id);
		message = tr("Route relations have incomplete members.\nThey need to be downloaded to proceed with validation.\nDo you want to download them?");
		checkbox = new JCheckBox(tr("Remember my choice and do not ask me again in this session"));
		options = new String[2];
		options[0] = tr("Yes");
		options[1] = tr("No");

	}

	/**
	 * Finds out whether the user wants to download incomplete members. In the
	 * default case, creates a JOptionPane to ask.
	 * 
	 * @return JOptionPane.YES_OPTION if the incomplete members should be
	 *         downloaded, JOptionPane.NO_OPTION otherwise.
	 */
	public int getUserSelection() {

		if (askToFetch == ASK_TO_FETCH.DONT_ASK_AND_FETCH) {
			return JOptionPane.YES_OPTION;
		}

		if (askToFetch == ASK_TO_FETCH.DONT_ASK_AND_DONT_FETCH) {
			return JOptionPane.NO_OPTION;
		}

		// this.createDialog(); // FIXME
		

		Object[] params = {message, checkbox};
		selectedOption = JOptionPane.showOptionDialog(this, params, tr("PT_Assistant Fetch Request"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, 0);
		
		if (checkbox.isSelected()) {
			if (selectedOption == JOptionPane.YES_OPTION) {
				askToFetch = ASK_TO_FETCH.DONT_ASK_AND_FETCH;
			} else {
				askToFetch = ASK_TO_FETCH.DONT_ASK_AND_DONT_FETCH;
			}
		}

		return selectedOption;
	}

	private void createDialog() {
		if (!SwingUtilities.isEventDispatchThread()) {
			Object[] params = {message, checkbox};
			selectedOption = JOptionPane.showOptionDialog(null, params, tr("Fetch Request"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, 0);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					createDialog();
				}
			});

		}
	}

}
