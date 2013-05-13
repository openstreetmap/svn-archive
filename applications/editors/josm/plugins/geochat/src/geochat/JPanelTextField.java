// License: WTFPL
package geochat;

import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.widgets.PopupMenuLauncher;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * JTextField tweaked to work in a JOSM panel. It prevents unwanted keystrokes
 * to be caught by the editor.
 *
 * @author zverik
 */
public class JPanelTextField extends JTextField {
    
    public JPanelTextField() {
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet<KeyStroke>());
        PopupMenuLauncher launcher = new PopupMenuLauncher(createEditMenu());
        addMouseListener(launcher);
        standardKeys = getInputMap(JComponent.WHEN_FOCUSED).allKeys();
    }

    private JPopupMenu createEditMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(createMenuItem(DefaultEditorKit.cutAction, tr("Cut")));
        menu.add(createMenuItem(DefaultEditorKit.copyAction, tr("Copy")));
        menu.add(createMenuItem(DefaultEditorKit.pasteAction, tr("Paste")));
        menu.add(createMenuItem(DefaultEditorKit.selectAllAction, tr("Select All")));
        return menu;
    }

    private JMenuItem createMenuItem( String action, String label ) {
        JMenuItem item = new JMenuItem(getActionMap().get(action));
        item.setText(label);
        return item;
    }

    // list of "standard" OS keys for JTextFiels = cursor moving, selection, copy/paste
    private final KeyStroke[] standardKeys;
    private static final int MODIFIERS_MASK = InputEvent.META_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;

    @Override
    protected void processKeyEvent( KeyEvent e ) {
        if( e.getID() == KeyEvent.KEY_PRESSED ) {
            int code = e.getKeyCode();
            if( code == KeyEvent.VK_ENTER ) {
                String text = getText();
                if( text.length() > 0 ) {
                    processEnter(text);
                    setText("");
                }
            } else if( code == KeyEvent.VK_TAB ) {
                String text = getText();
                int caret = getCaretPosition();
                int start = caret - 1;
                while( start >= 0 && Character.isJavaIdentifierPart(text.charAt(start)) )
                    start--;
                start++;
                if( start < caret ) {
                    String word = text.substring(start, caret);
                    String complete = word == null ? null : autoComplete(word, start == 0);
                    if( complete != null && !complete.equals(word) ) {
                        StringBuilder sb = new StringBuilder();
                        if( start > 0 )
                            sb.append(text.substring(0, start));
                        sb.append(complete);
                        if( caret < text.length() )
                            sb.append(text.substring(caret));
                        setText(sb.toString());
                        setCaretPosition(start + complete.length());
                    }
                }
            } else if( code == KeyEvent.VK_ESCAPE ) {
                if( Main.map != null && Main.map.mapView != null )
                    Main.map.mapView.requestFocus();
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
            if( !keyIsStandard ) {
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
    protected void processEnter( String text ) { }

    /**
     * Autocomplete the word.
     * @param word Partly typed word.
     * @return The whole word.
     */
    protected String autoComplete( String word, boolean atStart ) {
        return word;
    }
}
