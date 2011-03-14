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
package org.openstreetmap.josm.plugins.mapdust.gui.component.dialog;


import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.action.adapter.WindowClose;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteCancel;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteCommentBug;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustButtonPanel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * This class is used for creating a dialog window for the comment MapDust Bug
 * action.
 *
 * @author Bea
 *
 */
public class CommentIssueDialog extends AbstractDialog {

    /** The serial version UID */
    private static final long serialVersionUID = 7788698281871951326L;

    /** The message text */
    private final String messageText;

    /** Custom text */
    private JScrollPane cmpMessage;

    /** Nickname label */
    private JLabel lblNickname;

    /** Nickname text field */
    private JTextField txtNickname;

    /** Comment label */
    private JLabel lblComment;

    /** Comment text area */
    private JScrollPane cmpComment;

    /** The comment */
    private JTextArea txtComment;

    /** Cancel button */
    private JButton btnCancel;

    /** OK button */
    private JButton btnOk;

    /**
     * Builds a new <code>ChangeStatusDialog</code> object with the given
     * parameters.
     *
     * @param title The title of the dialog
     * @param iconName The name of the icon
     * @param messageText The text of the message component.
     * @param firedButton The button which action was to show this dialog
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public CommentIssueDialog(String title, String iconName,
            String messageText, JToggleButton firedButton,
            MapdustPlugin mapdustPlugin) {
        this.messageText = messageText;
        if (firedButton != null) {
            setFiredButton(firedButton);
        }
        setTitle(title);
        setModal(true);
        Image image = ImageProvider.get(iconName).getImage();
        setIconImage(image);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setFont(new Font("Times New Roman", Font.BOLD, 14));
        setBackground(getContentPane().getBackground());
        setResizable(false);
        setForeground(Color.black);
        setLayout(null);
        addComponents(mapdustPlugin);
        MapdustButtonPanel btnPanel =
                mapdustPlugin.getMapdustGUI().getPanel().getBtnPanel();
        addWindowListener(new WindowClose(this, btnPanel));
    }

    /**
     * Add the components to the dialog window.
     *
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    @Override
    public void addComponents(MapdustPlugin mapdustPlugin) {
        Color backgroundColor = getContentPane().getBackground();
        Font font = new Font("Times New Roman", Font.BOLD, 14);
        /* message text */
        if (cmpMessage == null) {
            JTextPane txtPane = ComponentUtil.createJTextPane(messageText,
                    backgroundColor);
            Rectangle bounds = new Rectangle(10, 10, 320, 50);
            cmpMessage = ComponentUtil.createJScrollPane(txtPane, bounds,
                    backgroundColor, true, true);
        }
        /* nickaname */
        if (lblNickname == null) {
            Rectangle bounds = new Rectangle(10, 70, 91, 25);
            lblNickname = ComponentUtil.createJLabel("Nickname", font, bounds);
        }
        if (txtNickname == null) {
            Rectangle bounds = new Rectangle(100, 70, 230, 25);
            txtNickname = ComponentUtil.createJTextField(bounds);
            /* get the nickname */
            String nickname = Main.pref.get("mapdust.nickname");
            if (nickname.isEmpty()) {
                /* if nickname is empty, get JOSM username */
                nickname = Main.pref.get("mapdust.josmUserName");
            }
            if (nickname != null && !nickname.isEmpty()) {
                txtNickname.setText(nickname);
            }
        }
        /* comment */
        if (lblComment == null) {
            Rectangle bounds = new Rectangle(10, 110, 79, 25);
            lblComment = ComponentUtil.createJLabel("Comment", font, bounds);
        }
        if (cmpComment == null) {
            Rectangle bounds = new Rectangle(100, 110, 230, 50);
            txtComment = new JTextArea();
            txtComment.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            txtComment.setLineWrap(true);
            cmpComment = ComponentUtil.createJScrollPane(txtComment, bounds,
                    backgroundColor, false, true);
        }
        /* cancel button */
        if (btnCancel == null) {
            Rectangle bounds = new Rectangle(240, 170, 90, 25);
            ExecuteCancel cancelAction = new ExecuteCancel(this,
                    mapdustPlugin.getMapdustGUI());
            btnCancel = ComponentUtil.createJButton("Cancel", bounds,
                    cancelAction);
        }
        /* ok button */
        if (btnOk == null) {
            Rectangle bounds = new Rectangle(170, 170, 60, 25);
            ExecuteCommentBug okAction = new ExecuteCommentBug(this,
                    mapdustPlugin.getMapdustGUI());
            okAction.addObserver(mapdustPlugin);
            okAction.addObserver(mapdustPlugin.getMapdustGUI());
            btnOk = ComponentUtil.createJButton("OK", bounds, okAction);
        }
        /* add components */
        add(cmpMessage);
        add(lblNickname);
        add(txtNickname);
        add(lblComment);
        add(cmpComment);
        add(btnCancel);
        add(btnOk);
        setSize(340, 210);
    }

    /**
     * Returns the message <code>JScrollPane</code> object
     *
     * @return the cmpMessage
     */
    public JScrollPane getCmpMessage() {
        return cmpMessage;
    }

    /**
     * Returns the nickname <code>JLabel</code> object
     *
     * @return the lblNickname
     */
    public JLabel getLblNickname() {
        return lblNickname;
    }

    /**
     * Returns the nickname <code>JTextField</code> object
     *
     * @return the txtNickname
     */
    public JTextField getTxtNickname() {
        return txtNickname;
    }

    /**
     * Returns the comment <code>JLabel</code> object
     *
     * @return the lblComment
     */
    public JLabel getLblComment() {
        return lblComment;
    }

    /**
     * Returns the comment <code>JScrollPane</code> object
     *
     * @return the cmpComment
     */
    public JScrollPane getCmpComment() {
        return cmpComment;
    }

    /**
     * Returns the comment <code>JTextArea</code> object
     *
     * @return the txtComment
     */
    public JTextArea getTxtComment() {
        return txtComment;
    }

    /**
     * Returns the cancel <code>JButton</code> object
     *
     * @return the btnCancel
     */
    public JButton getBtnCancel() {
        return btnCancel;
    }

    /**
     * Returns the ok <code>JButton</code> object
     *
     * @return the btnOk
     */
    public JButton getBtnOk() {
        return btnOk;
    }

    /**
     * Returns the message text
     *
     * @return the messageText
     */
    public String getMessageText() {
        return messageText;
    }

}