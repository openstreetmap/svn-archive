// License: GPL. For details, see LICENSE file.
package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.text.JTextComponent;

import org.openstreetmap.josm.gui.widgets.AbstractIdTextField;
import org.openstreetmap.josm.gui.widgets.AbstractTextComponentValidator;

/**
 * A text field designed to enter one or more OSM changeset IDs.
 */
public class ChangesetIdsTextField extends AbstractIdTextField<ChangesetIdsTextField.OsmIdsValidator> {

    /**
     * Constructs a new {@link ChangesetIdsTextField}
     */
    public ChangesetIdsTextField() {
        super(OsmIdsValidator.class);
    }

    /**
     * Reads the changeset ids.
     * @return true if a list of valid changeset id has been successfully read, false otherwise
     */
    @Override
    public boolean readIds() {
        return validator.readChangesetIds();
    }

    /**
     * @return sorted changeset IDs (highest ID first)
     */
    public Collection<Integer> getIdsInReverseOrder() {
        return validator.ids;
    }

    /**
     * Validator for a list of changeset IDs entered in a {@link JTextComponent}.
     */
    public static class OsmIdsValidator extends AbstractTextComponentValidator {

        private Collection<Integer> ids = new TreeSet<>(Collections.reverseOrder());

        /**
         * Constructs a new {@link OsmIdsValidator}
         * @param tc The text component to validate
         */
        public OsmIdsValidator(JTextComponent tc) {
            super(tc);
        }

        @Override
        public void validate() {
            if (!isValid()) {
                feedbackInvalid(tr("The current input is not a list of valid changeset IDs. "
                        + "Please enter one or more integer values > 0 or full changeset URLs."));
            } else {
                feedbackValid(tr("Please enter one or more integer values > 0 or full changeset URLs."));
            }
        }

        /**
         * Read list of changeset IDs or changeset URLs.
         * @return true if list is valid
         */
        public boolean readChangesetIds() {
            ids.clear();
            String value = getComponent().getText();
            try {
                final StringTokenizer tokenizer = new StringTokenizer(value, ",+ \t\n");
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken().trim();
                    if (token.matches("http.*/changeset/[0-9]+")) {
                        // full URL given, extract id
                        token = token.substring(token.lastIndexOf('/') + 1);
                    }
                    ids.add(Integer.parseInt(token));
                }
                return true;
            } catch (NumberFormatException e) {
                ids.clear();
                return false;
            }
        }

        @Override
        public boolean isValid() {
            return readChangesetIds();
        }
    }
}
