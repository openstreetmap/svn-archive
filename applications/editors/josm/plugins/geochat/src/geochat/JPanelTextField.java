package geochat;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.openstreetmap.josm.Main;

/**
 * JTextField tweaked to work in a JOSM panel. It prevents unwanted keystrokes
 * to be caught by the editor.
 * 
 * @author zverik
 */
public class JPanelTextField extends JTextField {

    public JPanelTextField() {
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet<KeyStroke>());
    }

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
                System.out.println("Autocomplete! Word " + start + "-" + caret + " (not inclusive)");
                if( start < caret ) {
                    String word = text.substring(start, caret);
                    String complete = word == null ? null : autoComplete(word);
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
            // Do not pass other events to JOSM
            if( code != KeyEvent.VK_LEFT && code != KeyEvent.VK_HOME && code != KeyEvent.VK_RIGHT && code != KeyEvent.VK_END && code != KeyEvent.VK_BACK_SPACE && code != KeyEvent.VK_DELETE )
                e.consume();
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
    protected String autoComplete( String word ) {
        return word;
    }
}
