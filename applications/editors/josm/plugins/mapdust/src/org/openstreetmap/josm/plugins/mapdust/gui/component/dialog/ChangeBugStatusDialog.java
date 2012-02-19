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
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.action.adapter.WindowClose;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteCancel;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteCloseBug;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteInvalidateBug;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteReOpenBug;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustButtonPanel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * This class is used for creating a dialog window for the following MapDust BUG
 * related actions: close bug report, invalidate bug report and re-open bug
 * report.
 *
 * @author Bea
 */
public class ChangeBugStatusDialog extends AbstractDialog {

    /** The serial version UID */
    private static final long serialVersionUID = -4106150600118847229L;

    /** Nickname label */
    private JLabel lblNickname;

    /** Nickname text field */
    private JTextField txtNickname;

    /** Comment label */
    private JLabel lblComment;

    /** Description text area */
    private JScrollPane cmpDescription;

    /** The description text */
    private JTextArea txtDescription;

    /** Cancel button */
    private JButton btnCancel;

    /** OK button */
    private JButton btnOk;

    /** The type */
    private final String type;

    /**
     * Builds a new <code>ChangeStatusDialog</code> object with the given
     * parameters.
     *
     * @param title The title of the dialog
     * @param iconName The name of the icon
     * @param type The type of the dialog ( close ,invalidate , re-open)
     * @param firedButton The button which fired this action
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public ChangeBugStatusDialog(String title, String iconName, String type,
            JToggleButton firedButton, MapdustPlugin mapdustPlugin) {
        this.type = type;
        if (firedButton != null) {
            setFiredButton(firedButton);
        }
        setTitle(title);
        setModal(true);
        Image image = ImageProvider.get(iconName).getImage();
        setIconImage(image);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setFont(new Font("Times New Roman", Font.BOLD, 14));
        setBackground(Color.white);
        setResizable(false);
        setForeground(Color.black);
        setLayout(null);
        addComponents(mapdustPlugin);
        MapdustButtonPanel btnPanel =
                mapdustPlugin.getMapdustGUI().getPanel().getBtnPanel();
        addWindowListener(new WindowClose(this, btnPanel));
    }

    /**
     * Adds the components to the dialog window.
     *
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    @Override
    public void addComponents(MapdustPlugin mapdustPlugin) {
        /* initialize components of the JDialog */
        Color backgroundColor = getContentPane().getBackground();
        Font font = new Font("Times New Roman", Font.BOLD, 14);
        /* create the nickname label and text field */
        if (lblNickname == null) {
            Rectangle bounds = new Rectangle(10, 10, 85, 25);
            lblNickname = ComponentUtil.createJLabel("Nickname", font, bounds,
                    null);
        }
        if (txtNickname == null) {
            Rectangle bounds = new Rectangle(100, 10, 250, 25);
            txtNickname = ComponentUtil.createJTextField(bounds);
            /* get the nickname */
            String nickname = Main.pref.get("mapdust.nickname");
            if (nickname.isEmpty()) {
                /* if nickname is empty, then get JOSM username */
                nickname = Main.pref.get("mapdust.josmUserName");
            }
            if (nickname != null && !nickname.isEmpty()) {
                txtNickname.setText(nickname);
            }
        }
        /* creates the comment label and text area */
        if (lblComment == null) {
            Rectangle bounds = new Rectangle(10, 50, 89, 25);
            lblComment = ComponentUtil.createJLabel("Comment", font, bounds,
                    null);
        }
        if (cmpDescription == null) {
            Rectangle bounds = new Rectangle(100, 50, 250, 80);
            txtDescription = new JTextArea();
            txtDescription.setLineWrap(true);
            txtDescription.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            cmpDescription = ComponentUtil.createJScrollPane(txtDescription,
                    bounds, backgroundColor, false, true);
        }
        /* creates the cancel action */
        ExecuteCancel cancelAction = new ExecuteCancel(this,
                mapdustPlugin.getMapdustGUI());
        AbstractAction okAction;
        if (type.equals("close")) {
            /* create the execute close bug action */
            okAction = new ExecuteCloseBug(this, mapdustPlugin.getMapdustGUI());
            ((ExecuteCloseBug) okAction).addObserver(mapdustPlugin);
            ((ExecuteCloseBug) okAction).addObserver(mapdustPlugin
                    .getMapdustGUI());
        } else {
            if (type.equals("invalidate")) {
                /* create the invalidate bug action */
                okAction = new ExecuteInvalidateBug(this,
                        mapdustPlugin.getMapdustGUI());
                ((ExecuteInvalidateBug) okAction).addObserver(mapdustPlugin);
                ((ExecuteInvalidateBug) okAction).addObserver(mapdustPlugin
                        .getMapdustGUI());
            } else {
                /* executes the re-open bug action */
                okAction = new ExecuteReOpenBug(this,
                        mapdustPlugin.getMapdustGUI());
                ((ExecuteReOpenBug) okAction).addObserver(mapdustPlugin);
                ((ExecuteReOpenBug) okAction).addObserver(mapdustPlugin
                        .getMapdustGUI());
            }
        }
        /* creates the cancel button */
        if (btnCancel == null) {
            Rectangle bounds = new Rectangle(260, 140, 90, 25);
            btnCancel = ComponentUtil.createJButton("Cancel", bounds,
                    cancelAction);
        }
        /* creates the ok button */
        if (btnOk == null) {
            Rectangle bounds = new Rectangle(190, 140, 60, 25);
            btnOk = ComponentUtil.createJButton("OK", bounds, okAction);
        }
        /* add components to the frame */
        add(lblNickname);
        add(txtNickname);
        add(lblComment);
        add(cmpDescription);
        add(btnCancel);
        add(btnOk);
        setSize(360, 170);
    }

    /**
     * Returns the <code>JLabel</code> of the nickname
     *
     * @return the lblNickname
     */
    public JLabel getLblNickname() {
        return this.lblNickname;
    }

    /**
     * Returns the <code>JTextField</code> of the nickname
     *
     * @return the txtNickname
     */
    public JTextField getTxtNickname() {
        return this.txtNickname;
    }

    /**
     * Returns the <code>JLabel</code> of the comment
     *
     * @return the lblComment
     */
    public JLabel getLblComment() {
        return this.lblComment;
    }

    /**
     * Returns the <code>JScrollPane</code> of the description
     *
     * @return the cmpDescription
     */
    public JScrollPane getCmpDescription() {
        return this.cmpDescription;
    }

    /**
     * Returns the <code>JTextArea</code> of the description
     *
     * @return the txtDescription
     */
    public JTextArea getTxtDescription() {
        return this.txtDescription;
    }

    /**
     * Returns the cancel button
     *
     * @return the btnCancel
     */
    public JButton getBtnCancel() {
        return this.btnCancel;
    }

    /**
     * Returns the ok buttons
     *
     * @return the btnOk
     */
    public JButton getBtnOk() {
        return this.btnOk;
    }

    /**
     * Returns the type of the dialog window
     *
     * @return the type
     */
// caused compile error
//    public String getType() {        return this.type;    }

}
