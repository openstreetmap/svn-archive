package iodb;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The dialog which presents a choice between imagery align options.
 * 
 * @author zverik
 */
public class OffsetDialog extends JDialog implements ActionListener {
    private List<ImageryOffsetBase> offsets;
    private ImageryOffsetBase selectedOffset;

    public OffsetDialog( List<ImageryOffsetBase> offsets ) {
        super(JOptionPane.getFrameForComponent(Main.parent), tr("Imagery Offset"), ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.offsets = offsets;
    }
    
    private void prepareDialog() {
        JPanel buttonPanel = new JPanel(new GridLayout(offsets.size() + 1, 1));
        for( ImageryOffsetBase offset : offsets ) {
            OffsetDialogButton button = new OffsetDialogButton(offset);
            button.addActionListener(this);
            buttonPanel.add(button);
        }
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton); // todo: proper button
        setContentPane(buttonPanel);
        pack();
        setLocationRelativeTo(Main.parent);
    }
    
    public ImageryOffsetBase showDialog() {
        selectedOffset = null;
        prepareDialog();
        setVisible(true);
        return selectedOffset;
    }

    public void actionPerformed( ActionEvent e ) {
        if( e.getSource() instanceof OffsetDialogButton ) {
            selectedOffset = ((OffsetDialogButton)e.getSource()).getOffset();
        } else
            selectedOffset = null;
        setVisible(false);
    }
}
