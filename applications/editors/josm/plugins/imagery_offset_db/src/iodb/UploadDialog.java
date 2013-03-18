package iodb;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * A dialog for uploading stuff. Can be extended for different types of offsets.
 * 
 * @author zverik
 */
public class UploadDialog extends JDialog implements ActionListener {

    public UploadDialog() {
        super(JOptionPane.getFrameForComponent(Main.parent), tr("Imagery Offset"), ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void prepareDialog() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        setContentPane(dialogPanel);
        pack();
        setLocationRelativeTo(Main.parent);
    }

    public ImageryOffsetBase showDialog() {
        prepareDialog();
        setVisible(true);
        return null;
    }
    
    protected void constructImageryOffset() {
        
    }

    public void actionPerformed( ActionEvent e ) {
        setVisible(false);
    }
}
