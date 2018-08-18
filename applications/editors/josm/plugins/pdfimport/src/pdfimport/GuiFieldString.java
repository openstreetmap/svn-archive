// License: GPL. For details, see LICENSE file.

package pdfimport;

import javax.swing.JTextField;
import javax.swing.border.Border;

public class GuiFieldString extends JTextField {
    /*
     * TODO: integrate presentation of dataValid;
     */

    protected Border defaultBorder;
    protected boolean dataValid;

    public GuiFieldString() {
        super();
        defaultBorder = getBorder();
    }

    public GuiFieldString(String text) {
        super(text);
        defaultBorder = getBorder();
    }

    public boolean isDataValid() {
        return dataValid;
    }

}


