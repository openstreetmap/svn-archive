package surveyor2;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.Shortcut;

/**
 *
 * @author zverik
 */
public class SurveyorTimelineDialog extends ToggleDialog {

    public SurveyorTimelineDialog() {
        super(tr("Surveyor Timeline"), "surveyor2", tr("Open surveyor2 time line panel"), null, 150, false);
    }
    
}
