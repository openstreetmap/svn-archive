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


import java.awt.event.ActionEvent;
import javax.swing.JToggleButton;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.component.dialog.CommentIssueDialog;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustButtonPanel;


/**
 * Displays the comment bug dialog.
 *
 * @author Bea
 *
 */
public class ShowCommentBugAction extends MapdustShowAction {

    /** The serial version UID */
    private static final long serialVersionUID = 1L;

    /**
     * Builds a <code>ShowCommentBugAction</code> object
     */
    public ShowCommentBugAction() {}

    /**
     * Builds a <code>ShowCommentBugAction</code> object based on the given
     * argument.
     *
     * @param mapdustPlugin The <code>MapdustPlugin</code>
     */
    public ShowCommentBugAction(MapdustPlugin mapdustPlugin) {
        setMapdustPlugin(mapdustPlugin);
        setTitle("Create bug report comment");
        setIconName("dialogs/comment.png");
        String text = "In order to comment a bug report, you need to ";
        text += "provide your nickname and a brief comment for the bug.";
        setMessageText(text);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event != null) {
            JToggleButton btn = null;
            if (event.getSource() instanceof JToggleButton) {
                btn = (JToggleButton) event.getSource();
                btn.setEnabled(false);
            }
            disableButtons(getButtonPanel());
            CommentIssueDialog dialog = new CommentIssueDialog(getTitle(),
                    getIconName(), getMessageText(), btn, getMapdustPlugin());
            dialog.setLocationRelativeTo(null);
            dialog.getContentPane().setPreferredSize(dialog.getSize());
            dialog.pack();
            dialog.setVisible(true);
        }
    }

    @Override
    void disableButtons(MapdustButtonPanel buttonPanel) {
        buttonPanel.getBtnWorkOffline().setEnabled(false);
        buttonPanel.getBtnRefresh().setEnabled(false);
        buttonPanel.getBtnFixBugReport().setEnabled(false);
        buttonPanel.getBtnInvalidateBugReport().setEnabled(false);
        buttonPanel.getBtnReOpenBugReport().setEnabled(false);
    }

}
