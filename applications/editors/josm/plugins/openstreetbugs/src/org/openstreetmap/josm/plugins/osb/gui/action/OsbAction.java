/* Copyright (c) 2008, Henrik Niehaus
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
package org.openstreetmap.josm.plugins.osb.gui.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.osb.ConfigKeys;

public abstract class OsbAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private static List<OsbActionObserver> observers = new ArrayList<OsbActionObserver>();

    private static Node selectedNode;

    public OsbAction(String name) {
        super(name);
    }

    public static Node getSelectedNode() {
        return selectedNode;
    }

    public static void setSelectedNode(Node selectedNode) {
        OsbAction.selectedNode = selectedNode;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            doActionPerformed(e);
            for (OsbActionObserver obs : observers) {
                obs.actionPerformed(this);
            }
        } catch (Exception e1) {
            System.err.println("Couldn't execute action " + getClass().getSimpleName());
            e1.printStackTrace();
        }
    }

    protected abstract void doActionPerformed(ActionEvent e) throws Exception;

    public static void addActionObserver(OsbActionObserver obs) {
        observers.add(obs);
    }

    public static void removeActionObserver(OsbActionObserver obs) {
        observers.remove(obs);
    }
    
    protected String addMesgInfo(String msg) {
        // get the user nickname
        String nickname = Main.pref.get(ConfigKeys.OSB_NICKNAME);
        if(nickname == null || nickname.length() == 0) {
            nickname = JOptionPane.showInputDialog(Main.parent, tr("Please enter a user name"));
            if(nickname == null) {
                nickname = "NoName";
            } else {
                Main.pref.put(ConfigKeys.OSB_NICKNAME, nickname);
            }
        }
        
        // concatenate nickname and date, if date should be included
        String info = nickname;
        if(Main.pref.getBoolean(ConfigKeys.OSB_INCLUDE_DATE)) {
            // get the date
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, Locale.getDefault());
            String date = df.format(new Date());
            
            // concatenate nickname and date
            info = info.concat(", ").concat(date);
        }
        
        // add user and date info to the message
        msg = msg.concat(" [").concat(info).concat("]");
        
        return msg;
    }
}
