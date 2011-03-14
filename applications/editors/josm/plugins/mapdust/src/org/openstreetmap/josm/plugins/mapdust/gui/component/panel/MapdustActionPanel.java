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
package org.openstreetmap.josm.plugins.mapdust.gui.component.panel;


import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteActionList;
import org.openstreetmap.josm.plugins.mapdust.gui.component.model.ActionListModel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustAction;


/**
 * Defines a JPanel for the <code>MapdustAction</code> object.
 *
 * @author Bea
 *
 */
public class MapdustActionPanel extends JPanel {

    /** The serial version UID */
    private static final long serialVersionUID = -6648507056357610823L;

    /** The scroll pane */
    private JScrollPane cmpActionList;

    /** The JList containing the MapDust action objects */
    private JList actionJList;

    /** The list of <code>MapdustAction</code> objects */
    private List<MapdustAction> actionList;

    /**
     * Builds a <code>MapdustActionPanel</code> object based on the given
     * arguments.
     *
     * @param actionList The list of <code>MapdustAction</code> objects
     * @param name The name of the panel
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public MapdustActionPanel(List<MapdustAction> actionList, String name,
            MapdustPlugin mapdustPlugin) {
        this.actionList = actionList;
        setLayout(new BorderLayout());
        addComponents(actionList, mapdustPlugin);
        setName(tr(name));
    }

    /**
     * Creates and adds the components to the MapdustAction panel.
     *
     * @param list The list of <code>MapdustAction</code> objects
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    private void addComponents(List<MapdustAction> list,
            MapdustPlugin mapdustPlugin) {
        /* create components */
        AbstractAction action = new ExecuteActionList
        (mapdustPlugin.getMapdustGUI());
        JToggleButton btnUpload = ComponentUtil.createJButton("Upload list data",
                null, null, action);
        ((ExecuteActionList) action).addObserver(mapdustPlugin);
        if (cmpActionList == null) {
            actionJList = ComponentUtil.createJList(list);
            cmpActionList = ComponentUtil.createJScrollPane(actionJList);
        }
        add(cmpActionList, BorderLayout.CENTER);
        add(btnUpload, BorderLayout.SOUTH);
    }

    /**
     * Updates the <code>MapdustActionPanel</code> with the given list of
     * <code>MapdustAction</code>s.
     *
     * @param actionList The list of <code>MapdustAction</code> objects
     */
    public void updateComponents(List<MapdustAction> actionList) {
        setActionList(actionList);
        actionJList.setModel(new ActionListModel(actionList));
        cmpActionList.getViewport().setView(actionJList);
        cmpActionList.invalidate();

    }

    /**
     * Returns the list of <code>MapdustAction</code>s
     *
     * @return the actionList
     */
    public List<MapdustAction> getActionList() {
        return actionList;
    }

    /**
     * Sets the list of <code>MapdustAction</code>s
     *
     * @param actionList the actionList to set
     */
    public void setActionList(List<MapdustAction> actionList) {
        this.actionList = actionList;
    }

}