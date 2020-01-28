// License: GPL. For details, see LICENSE file.
package views;

import javax.swing.JButton;

import model.TagCatalog.IndoorObject;

/**
 * Button with a specific IndoorObject attached to it.
 * @author egru
 *
 */
@SuppressWarnings("serial")
class PresetButton extends JButton {

    private IndoorObject indoorObject;

    PresetButton(IndoorObject object) {
        this.setIndoorObject(object);
    }

    public IndoorObject getIndoorObject() {
        return this.indoorObject;
    }

    public void setIndoorObject(IndoorObject object) {
        this.indoorObject = object;
        this.setText(indoorObject.toString());
        this.setToolTipText("Fast Tag: "+indoorObject.toString());
    }
}
