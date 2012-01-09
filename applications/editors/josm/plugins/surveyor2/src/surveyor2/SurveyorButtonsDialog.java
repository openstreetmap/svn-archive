package surveyor2;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.Shortcut;

/**
 *
 * @author zverik
 */
public class SurveyorButtonsDialog extends ToggleDialog {

    public SurveyorButtonsDialog() {
        super(tr("Surveyor Actions"), "surveyor2", tr("Open surveyor2 button panel"), null, 300, false);
    }
    
}
