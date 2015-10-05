package reverter;

import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.text.JTextComponent;

import org.openstreetmap.josm.gui.widgets.AbstractIdTextField;
import org.openstreetmap.josm.gui.widgets.ChangesetIdTextField;

public class ChangesetIdsTextField extends AbstractIdTextField<ChangesetIdsTextField.OsmIdsValidator> {

    public ChangesetIdsTextField() {
        super(OsmIdsValidator.class);
    }

    @Override
    public boolean readIds() {
        return validator.readChangesetId();
    }

    public Collection<Integer> getIdsInReverseOrder() {
        return validator.ids;
    }

    public static class OsmIdsValidator extends ChangesetIdTextField.ChangesetIdValidator {

        private Collection<Integer> ids = new TreeSet<>(Collections.reverseOrder());

        public OsmIdsValidator(JTextComponent tc) {
            super(tc);
        }

        @Override
        public boolean readChangesetId() {
            ids.clear();
            String value = getComponent().getText();
            try {
                final StringTokenizer tokenizer = new StringTokenizer(value, ",.+/ \t\n");
                while(tokenizer.hasMoreTokens()) {
                    ids.add(Integer.parseInt(tokenizer.nextToken().trim()));
                }
                return true;
            } catch (NumberFormatException e) {
                ids.clear();
                return false;
            }
        }
    }
}
