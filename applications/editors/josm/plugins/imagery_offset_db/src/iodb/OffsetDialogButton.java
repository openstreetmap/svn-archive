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
        super(offset.getDescription() + " (" + Math.round(offset.getPosition().greatCircleDistance(ImageryOffsetTools.getMapCenter())) + " m)");
        this.offset = offset;
    }

    public ImageryOffsetBase getOffset() {
        return offset;
    }
    
}
