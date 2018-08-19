// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor.action.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * @author cdaller
 *
 */
public class WaypointDialog {

    public String openDialog(JFrame frame, String message, long timeout) {

        JTextField textField = new JTextField(10);

        //Create an array of the text and components to be displayed.
        Object[] array = {message, textField};

        //Create an array specifying the number of dialog buttons and their text.
        Object[] options = {"OK"};

        //Create the JOptionPane.
        final JOptionPane optionPane = new JOptionPane(array,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.OK_OPTION,
                                    null,
                                    options,
                                    options[0]);

        final JDialog dialog = new JDialog(frame, "Enter Description", true);
        DialogClosingThread closer = new DialogClosingThread(dialog, timeout);
        closer.observe(textField);
        dialog.setContentPane(optionPane);
        optionPane.addPropertyChangeListener(e -> {
            String prop = e.getPropertyName();

            if (dialog.isVisible() && (e.getSource() == optionPane)
                            && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                // If you were going to check something
                // before closing the window, you'd do it here.
                dialog.setVisible(false);
            }
        });
        closer.start();
        dialog.pack();
        dialog.setVisible(true);

        return textField.getText();
    }
}
