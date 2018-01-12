/*
 * Indoorhelper is a JOSM plug-in to support users when creating their own indoor maps.
 *  Copyright (C) 2016  Erik Gruschka
 *  Copyright (C) 2018  Rebecca Schmidt
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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


/**
*
*
*
*
*
*
*
*
*/
}
