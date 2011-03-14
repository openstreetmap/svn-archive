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
package org.openstreetmap.josm.plugins.mapdust.gui.action.show;


import javax.swing.AbstractAction;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustButtonPanel;


/**
 * This class is a base action class. Defines the common functionality for the
 * other 'show' action classes.
 *
 * @author Bea
 *
 */
public abstract class MapdustShowAction extends AbstractAction {

    /** The serial version UID */
    private static final long serialVersionUID = -4894445690647156702L;

    /** The title of the dialog window */
    protected String title;

    /** The icon name of the dialog window */
    protected String iconName;

    /** The message to display on the dialog */
    protected String messageText;

    /** The <code>MapdustPlugin</code> */
    protected MapdustPlugin mapdustPlugin;

    /**
     * Returns the <code>MapdustButtonPanel</code> object.
     *
     * @return MapdustButtonPanel
     */
    protected MapdustButtonPanel getButtonPanel() {
        MapdustButtonPanel btnPanel =
                this.mapdustPlugin.getMapdustGUI().getPanel().getBtnPanel();
        return btnPanel;
    }

    /**
     * Returns the <code>MapdustPlugin</code> object
     *
     * @return the mapdustPlugin
     */
    public MapdustPlugin getMapdustPlugin() {
        return this.mapdustPlugin;
    }

    /**
     * Sets the <code>MapdustPlugin</code> object
     *
     * @param mapdustPlugin the mapdustPlugin to set
     */
    public void setMapdustPlugin(MapdustPlugin mapdustPlugin) {
        this.mapdustPlugin = mapdustPlugin;
    }

    /**
     * Returns the title
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the name of the icon
     *
     * @return the iconName
     */
    public String getIconName() {
        return this.iconName;
    }

    /**
     * Sets the name of the icon
     *
     * @param iconName the iconName to set
     */
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    /**
     * Returns the message text
     *
     * @return the messageText
     */
    public String getMessageText() {
        return this.messageText;
    }

    /**
     * Returns the message text
     *
     * @param messageText the messageText to set
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    /**
     * Disables the buttons from the given button panel.
     *
     * @param buttonPanel The <code>MapdustButtonPanel</code> object
     */
    abstract void disableButtons(MapdustButtonPanel buttonPanel);

}
