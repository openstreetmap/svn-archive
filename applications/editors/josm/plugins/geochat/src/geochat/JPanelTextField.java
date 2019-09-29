// License: WTFPL. For details, see LICENSE file.
package geochat;

import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.widgets.DisableShortcutsOnFocusGainedTextField;

/**
 * JTextField tweaked to work in a JOSM panel. It prevents unwanted keystrokes
 * to be caught by the editor.
 *
 * @author zverik
 */
public class JPanelTextField extends DisableShortcutsOnFocusGainedTextField {

    public JPanelTextField() {
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet<KeyStroke>());
        standardKeys = getInputMap(JComponent.WHEN_FOCUSED).allKeys();
    }

    // list of "standard" OS keys for JTextFiels = cursor moving, selection, copy/paste
    private final KeyStroke[] standardKeys;
    private static final int MODIFIERS_MASK =
            InputEvent.META_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_ENTER) {
                String text = getText();
                if (text.length() > 0) {
                    processEnter(text);
                    setText("");
                }
            } else if (code == KeyEvent.VK_TAB) {
                String text = getText();
                int caret = getCaretPosition();
                int start = caret - 1;
                while (start >= 0 && Character.isJavaIdentifierPart(text.charAt(start))) {
                    start--;
                }
                start++;
                if (start < caret) {
                    String word = text.substring(start, caret);
                    String complete = word == null ? null : autoComplete(word, start == 0);
                    if (complete != null && !complete.equals(word)) {
                        StringBuilder sb = new StringBuilder();
                        if (start > 0)
                            sb.append(text.substring(0, start));
                        sb.append(complete);
                        if (caret < text.length())
                            sb.append(text.substring(caret));
                        setText(sb.toString());
                        setCaretPosition(start + complete.length());
                    }
                }
            } else if (code == KeyEvent.VK_ESCAPE) {
                if (MainApplication.isDisplayingMapView())
                    MainApplication.getMap().mapView.requestFocus();
            }

            boolean keyIsStandard = false;
            for (KeyStroke ks: standardKeys) {
                if (code == ks.getKeyCode() &&
                        (e.getModifiersEx() & MODIFIERS_MASK) == (ks.getModifiers() & MODIFIERS_MASK)) {
                    keyIsStandard = true;
                    break;
                }
            }
            // Do not pass other events to JOSM
            if (!keyIsStandard) {
                e.consume();
            }
        }
        super.processKeyEvent(e);
    }

    /**
     * Process VK_ENTER. Override this to submit the text.
     *
     * @param text Contents of the text field.
     */
    protected void processEnter(String text) { }

    /**
     * Autocomplete the word.
     * @param word Partly typed word.
     * @return The whole word.
     */
    protected String autoComplete(String word, boolean atStart) {
        return word;
    }
}
