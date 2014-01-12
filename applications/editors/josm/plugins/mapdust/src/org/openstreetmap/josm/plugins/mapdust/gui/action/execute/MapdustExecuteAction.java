/* Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.mapdust.gui.action.execute;


import javax.swing.AbstractAction;
import javax.swing.JToggleButton;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustGUI;
import org.openstreetmap.josm.plugins.mapdust.gui.component.dialog.AbstractDialog;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;


/**
 * This is an abstract class, defining the commonly used methods by the
 * ExecuteAction classes.
 *
 * @author Bea
 *
 */
public abstract class MapdustExecuteAction extends AbstractAction {

    /** Serial version UID */
    private static final long serialVersionUID = 4318259806647818543L;

    /** The abstract dialog object */
    protected AbstractDialog dialog;

    /** The MapDust GUI object */
    protected MapdustGUI mapdustGUI;

    /**
     * Validates the nickname and the commentText fields, and returns the
     * corresponding message. If the fields are both valid, the method return
     * null.
     *
     * @param nickname The nickname of the user
     * @param commentText A comment given by the user
     * @return An error string
     */
    protected String validate(String nickname, String commentText) {
        String errorMessage = null;
        String invalidNickname = " Your nickname length has to be between 3";
        invalidNickname += " and 16 characters. Please use letters,numbers, ";
        invalidNickname += " '-', '.' or '_' '.'";
        if (emptyValue(nickname) && emptyValue(commentText)) {
            errorMessage = " Missing nickname and comment.";
            errorMessage += invalidNickname;
        } else {
            if (emptyValue(nickname)) {
                errorMessage = " Missing nickname.";
                errorMessage += nickname;
            } else {
                if (emptyValue(commentText)) {
                    if (!validNickname(nickname)) {
                        errorMessage = " Missing comment and invalid nickname.";
                        errorMessage += invalidNickname;
                    } else {
                        errorMessage = " The comment is missing.";
                    }
                } else {
                    if (!validNickname(nickname)) {
                        errorMessage = " Invalid nickname.";
                        errorMessage += invalidNickname;
                    }
                }
            }
        }
        return errorMessage;
    }

    /**
     * Returns the icon path for the given MapDust bug. The icons are located in
     * the "dialogs" directory under the "images" directory. The name of an icon
     * for a given bug has the following format: status_type.png.
     *
     * @param bug The <code>MapdustBug</code> object
     * @return The path of the icon
     */
    protected String getIconPath(MapdustBug bug) {
        String iconPath = "bugs/normal/";
        iconPath += bug.getStatus().getValue().toLowerCase();
        iconPath += "_";
        iconPath += bug.getType().getKey();
        iconPath += ".png";
        return iconPath;
    }

    /**
     * Enables the given button. This button had fired a corresponding action,
     * and after the action finishes, the button need to become enabled.
     *
     * @param button A <code>JToggleButton</code> object
     */
    protected void enableFiredButton(JToggleButton button) {
        if (button != null) {
            button.setSelected(false);
            button.setFocusable(false);
        }
    }

    /**
     * Verifies if the given value is empty or not.
     *
     * @param value A string
     * @return true if the value is empty false otherwise
     */
    private boolean emptyValue(String value) {
        String modifiedValue = value != null ? value.trim() : "";
        if (value == null || value.isEmpty() || modifiedValue.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Validates the given nickname. A nickname is valid if its length is
     * between 3 and 16 characters.
     *
     * @param value The nickname
     * @return true if the nickname is valid false otherwise
     */
    private boolean validNickname(String value) {
        int length = value.length();
        if (length < 3 || length > 16) {
            return false;
        }
        return true;
    }

    /**
     * Returns the abstract dialog
     *
     * @return the dialog
     */
    public AbstractDialog getDialog() {
        return this.dialog;
    }

    /**
     * Sets the abstract dialog
     *
     * @param dialog the dialog to set
     */
    public void setDialog(AbstractDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * Returns the <code>MapdustGUI</code> object
     *
     * @return the mapdustGUI
     */
    public MapdustGUI getMapdustGUI() {
        return this.mapdustGUI;
    }

    /**
     * Sets the <code>MapdustGUI</code> object
     *
     * @param mapdustGUI the mapdustGUI to set
     */
    public void setMapdustGUI(MapdustGUI mapdustGUI) {
        this.mapdustGUI = mapdustGUI;
    }

}
