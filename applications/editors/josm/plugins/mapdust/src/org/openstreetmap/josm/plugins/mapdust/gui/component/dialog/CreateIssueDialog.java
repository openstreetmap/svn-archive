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
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.action.adapter.WindowClose;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteAddBug;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteCancel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustButtonPanel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.renderer.ComboBoxRenderer;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * This class is used for creating a dialog window for the new issue Mapdust Bug
 * action.
 *
 * @author Bea
 *
 */
public class CreateIssueDialog extends AbstractDialog {

    /** The serial version UID */
    private static final long serialVersionUID = 1L;

    /** The create issue text */
    private String createIssueText;

    /** Text message */
    private JScrollPane cmpMessage;

    /** Type */
    private JLabel lblType;

    /** The type combo box */
    private JComboBox cbbType;

    /** Nickname */
    private JLabel lblNickname;

    /** The txt nickname */
    private JTextField txtNickname;

    /** Description */
    private JLabel lblDescription;

    /** The description scroll pane */
    private JScrollPane cmpDescription;

    /** The description text area */
    private JTextArea txtDescription;

    /** The cancel button */
    private JButton btnCancel;

    /** The ok button */
    private JButton btnOk;

    /** The point */
    private final Point point;

    /**
     * Builds a new <code>ChangeStatusDialog</code> object with the given
     * parameters.
     *
     * @param title The title of the dialog
     * @param iconName The name of the icon
     * @param messageText The text of the message component.
     * @param point The position where the bug was created
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public CreateIssueDialog(Point point, MapdustPlugin mapdustPlugin) {
        this.createIssueText= "In order to create a new bug report you";
        this.createIssueText += " need to provide your nickname and a brief";
        this.createIssueText+= " description for the bug.";
        this.point = point;
        /* set JDialog settings */
        initializeDialog();
        /* add components to the dialog */
        addComponents(mapdustPlugin);
        /* add window listenet */
        MapdustButtonPanel btnPanel = mapdustPlugin.getMapdustGUI().getPanel().getBtnPanel();
        addWindowListener(new WindowClose(this, btnPanel, null));
    }

    /**
     * Initializes the dialog default fields.
     */
    private void initializeDialog(){
        /* set JDialog settings */
        setTitle("Create bug report");
        setModal(true);
        Image image = ImageProvider.get("dialogs/open.png").getImage();
        setIconImage(image);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        getContentPane().setFont(new Font("Times New Roman", Font.BOLD, 14));
        setResizable(false);
        setForeground(Color.black);
        setLayout(null);

    }

    /**
     * Displays the dialog.
     */
    public void showDialog(){
       setLocationRelativeTo(null);
       getContentPane().setPreferredSize(getSize());
       pack();
       setVisible(true);
    }

    @Override
    public void addComponents(MapdustPlugin mapdustPlugin) {
        Color backgroundColor = getContentPane().getBackground();
        Font font = new Font("Times New Roman", Font.BOLD, 14);
        /* create the message cmp */
        if (cmpMessage == null) {
            JTextPane txtPane = ComponentUtil.createJTextPane(createIssueText,
                    backgroundColor);
            cmpMessage = ComponentUtil.createJScrollPane(txtPane,
                    new Rectangle(10, 10, 330, 50), backgroundColor, true, true);
        }
        /* the type label and combo box */
        if (lblType == null) {
            lblType = ComponentUtil.createJLabel("Type", font,
                    new Rectangle(10, 70, 91, 25));
        }
        if (cbbType == null) {
            ComboBoxRenderer renderer = new ComboBoxRenderer();
            cbbType = ComponentUtil.createJComboBox(new Rectangle(110, 70, 230, 25),
                    renderer, backgroundColor);
        }
        /* create the nickname label and text field */
        if (lblNickname == null) {
            lblNickname = ComponentUtil.createJLabel("Nickname", font,
                    new Rectangle(10, 110, 79, 25));
        }
        if (txtNickname == null) {
            txtNickname = ComponentUtil.createJTextField(new Rectangle(110, 110, 230, 25));
            /* nickname was set before */
            String nickname = Main.pref.get("mapdust.nickname");
            if (nickname != null && !nickname.isEmpty()) {
                txtNickname.setText(nickname);
            }
        }
        /* creates the description label and text area */
        if (lblDescription == null) {
            lblDescription = ComponentUtil.createJLabel("Description", font,
                    new Rectangle(10, 150, 95, 25));
        }
        if (cmpDescription == null) {
            txtDescription = new JTextArea();
            txtDescription.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            txtDescription.setLineWrap(true);
            cmpDescription = ComponentUtil.createJScrollPane(txtDescription,
                    new Rectangle(110, 150, 230, 50), backgroundColor, false, true);
        }
        /* creates the cancel button */
        if (btnCancel == null) {
            Rectangle bounds = new Rectangle(250, 210, 90, 25);
            ExecuteCancel cancelAction = new ExecuteCancel(this,
                    mapdustPlugin.getMapdustGUI());
            btnCancel = ComponentUtil.createJButton("Cancel", bounds,
                    cancelAction);
        }
        /* creates the ok button */
        if (btnOk == null) {
            Rectangle bounds = new Rectangle(180, 210, 60, 25);
            ExecuteAddBug okAction = new ExecuteAddBug(this,
                    mapdustPlugin.getMapdustGUI());
            okAction.addObserver(mapdustPlugin);
            okAction.addObserver(mapdustPlugin.getMapdustGUI());
            btnOk = ComponentUtil.createJButton("OK", bounds, okAction);
        }
        /* add components to the frame */
        add(cmpMessage);
        add(lblType);
        add(cbbType);
        add(lblNickname);
        add(txtNickname);
        add(lblDescription);
        add(cmpDescription);
        add(btnCancel);
        add(btnOk);
        setSize(350, 250);
    }

    /**
     * Returns the message <code>JScrollPane</code>
     *
     * @return the cmpMessage
     */
    public JScrollPane getCmpMessage() {
        return cmpMessage;
    }

    /**
     * Returns the type <code>JLabel</code>
     *
     * @return the lblType
     */
    public JLabel getLblType() {
        return lblType;
    }

    /**
     * Returns the type <code>JComboBox</code>
     *
     * @return the cbbType
     */
    public JComboBox getCbbType() {
        return cbbType;
    }

    /**
     * Returns the nickname <code>JLabel</code>
     *
     * @return the lblNickname
     */
    public JLabel getLblNickname() {
        return lblNickname;
    }

    /**
     * Returns the nickname <code>JTextField</code>
     *
     * @return the txtNickname
     */
    public JTextField getTxtNickname() {
        return txtNickname;
    }

    /**
     * Returns the description <code>JLabel</code>
     *
     * @return the lblDescription
     */
    public JLabel getLblDescription() {
        return lblDescription;
    }

    /**
     * Returns the description <code>JScrollPane</code>
     *
     * @return the cmpDescription
     */
    public JScrollPane getCmpDescription() {
        return cmpDescription;
    }

    /**
     * Returns the description <code>JTextArea</code>
     *
     * @return the txtDescription
     */
    public JTextArea getTxtDescription() {
        return txtDescription;
    }

    /**
     * Returns the <code>Point</code>
     *
     * @return point
     */
    public Point getPoint() {
        return point;
    }

}
