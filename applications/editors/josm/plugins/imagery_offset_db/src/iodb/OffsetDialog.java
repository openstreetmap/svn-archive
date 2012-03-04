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
public class OffsetDialog extends JDialog {
    private List<ImageryOffsetBase> offsets;
    private int selectedOffset;

    public OffsetDialog( List<ImageryOffsetBase> offsets ) {
        super(JOptionPane.getFrameForComponent(Main.parent), tr("Imagery Offset"), ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.offsets = offsets;
    }
    
    private void prepareDialog() {
        JPanel buttonPanel = new JPanel(new GridLayout(offsets.size() + 1, 1));
        for( ImageryOffsetBase offset : offsets ) {
            buttonPanel.add(new OffsetDialogButton(offset));
        }
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedOffset = -1;
                OffsetDialog.this.setVisible(false);
            }
        });
        buttonPanel.add(cancelButton); // todo: proper button
        setContentPane(buttonPanel);
        pack();
        setLocationRelativeTo(Main.parent);
    }
    
    public ImageryOffsetBase showDialog() {
        selectedOffset = -1;
        prepareDialog();
        setVisible(true);
        return selectedOffset < 0 ? null : offsets.get(selectedOffset);
    }
}
