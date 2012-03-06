package iodb;

import javax.swing.JButton;

/**
 * A button which shows offset information.
 * 
 * @author zverik
 */
public class OffsetDialogButton extends JButton {
    
    private ImageryOffsetBase offset;

    public OffsetDialogButton( ImageryOffsetBase offset ) {
        super(offset.getDescription() + " (" + offset.getPosition().lat() + ", " + offset.getPosition().lon() + ")");
        this.offset = offset;
    }

    public ImageryOffsetBase getOffset() {
        return offset;
    }
    
}
