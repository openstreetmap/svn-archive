/**
 *  Tracer2 - plug-in for JOSM to capture contours
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openstreetmap.josm.plugins.tracer2.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

public class ServerParamDialog extends ExtendedDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3229680217088662218L;
	
	private String[] m_astrTileSize = new String[] {"0.0001", "0.0002", "0.0004", "0.0008", "0.001", "0.002", "0.004", "0.008", "0.01"};
	private String[] m_astrResolution = new String[] {"512", "1024", "2048", "4096"};
	private String[] m_astrMode = new String[] {"boundary", "match color"};
	private String[] m_astrPointsPerCircle = new String[] {"0", "8", "12", "16", "20", "24", "32"};
	
	private ServerParam m_oParam;
	
    private JPanel m_oPanel = new JPanel(new GridBagLayout());
    private JTextField m_oName = new JTextField();
    private JTextField m_oDescription = new JTextField();
    private JTextArea m_oUrl = new JTextArea(5,5);
    private JComboBox<String> m_oTileSize;
    private JComboBox<String> m_oResolution;
    //private JTextField m_oSkipBottom = new JTextField();
    private JComboBox<String> m_oMode;
    private JTextField m_oThreshold = new JTextField();
    private JComboBox<String> m_oPointsPerCircle;
    private JTextField m_oTag = new JTextField();
    private JTextField m_oPreferredValues = new JTextField();
    
    private JScrollPane m_oScrollpaneUrl;
    
    public ServerParam getServerParam() {
    	return m_oParam;
    }
    
    private void addLabelled(String str, Component c) {
        JLabel label = new JLabel(str);
        m_oPanel.add(label, GBC.std());
        label.setLabelFor(c);
        m_oPanel.add(c, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }
    
    private void addGap() {
        JPanel p = new JPanel();
        p.setMinimumSize(new Dimension(10,0));
        m_oPanel.add(p, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }
    
    private void load() {
        m_oName.setText(m_oParam.getName());
        m_oDescription.setText(m_oParam.getDescription());
        m_oUrl.setText(m_oParam.getUrl());
        loadComboBox( m_oTileSize, m_oParam.getTileSize(), m_astrTileSize);
        loadComboBox( m_oResolution, m_oParam.getResolution(), m_astrResolution);
        //m_oSkipBottom.setText(param.getSkipBottom());
        loadComboBox( m_oMode, m_oParam.getMode(), m_astrMode);
        m_oThreshold.setText(m_oParam.getThreshold());
        loadComboBox( m_oPointsPerCircle, m_oParam.getPointsPerCircle(), m_astrPointsPerCircle);
        m_oTag.setText(m_oParam.getTag());
        m_oPreferredValues.setText(m_oParam.getPreferredValues());
    }
    
    private void save() {
    	m_oParam.setName(m_oName.getText());
    	m_oParam.setDescription(m_oDescription.getText());
    	m_oParam.setUrl(m_oUrl.getText());
    	m_oParam.setTileSize(saveComboBox(m_oTileSize, m_astrTileSize));
    	m_oParam.setResolution(saveComboBox(m_oResolution, m_astrResolution));
    	//m_oParam.setSkipBottom(m_oSkipBottom.getText());
    	m_oParam.setMode(saveComboBox(m_oMode, m_astrMode));
    	m_oParam.setThreshold(m_oThreshold.getText());
    	m_oParam.setPointsPerCircle(saveComboBox(m_oPointsPerCircle, m_astrPointsPerCircle));
    	m_oParam.setTag(m_oTag.getText());
    	m_oParam.setPreferredValues(m_oPreferredValues.getText());
    }
    
    private void loadComboBox( JComboBox<?> c, String strValue, String[] astrValues ) {
        int pos = 0;
        for ( String str: astrValues ) {
        	if (strValue.equals(str)) {
            	c.setSelectedIndex(pos);
            	return;
        	}
        	pos++;
        }
    }
    
    private String saveComboBox( JComboBox<?> c, String[] astrValues ) {
        return astrValues[c.getSelectedIndex()];
    }
    
    public ServerParamDialog(ServerParam param) {
        super(Main.parent, tr("Tracer2") + " - " + tr("Parameter for server request"),
                new String[] { tr("OK"), tr("Cancel") },
                true);
        if (param == null) {
        	m_oParam = new ServerParam();
        } else {
        	m_oParam = param;
        }
        
        contentInsets = new Insets(15, 15, 5, 15);
        setButtonIcons(new String[] { "ok.png", "cancel.png" });
        
        m_oTileSize = new JComboBox<>(m_astrTileSize);
        m_oResolution = new JComboBox<>(m_astrResolution);
        m_oMode = new JComboBox<>(m_astrMode);
        m_oPointsPerCircle = new JComboBox<>(m_astrPointsPerCircle);
        
        load();
        
        addLabelled(tr("Name:"), m_oName);
        addLabelled(tr("Description:"), m_oDescription);
        addGap();
        m_oUrl.setLineWrap(true);
        m_oScrollpaneUrl = new JScrollPane(m_oUrl);
        addLabelled(tr("URL:"), m_oScrollpaneUrl);
        addGap();
        addLabelled(tr("Tile size:"), m_oTileSize);
        addLabelled(tr("Resolution:"), m_oResolution);
        //addLabelled(tr("Skip bottom:"), m_oSkipBottom);
        addGap();
        addLabelled(tr("Mode:"), m_oMode);
        addLabelled(tr("Threshold:"), m_oThreshold);
        addGap();
        addLabelled(tr("Points per circle:"), m_oPointsPerCircle);
        addGap();
        addLabelled(tr("Tag:"), m_oTag);
        addLabelled(tr("Preferred values:"), m_oPreferredValues);
        
        setMinimumSize(new Dimension(500, 0));
        
        setContent(m_oPanel);
        setupDialog();
    }
    
    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        if (evt.getActionCommand().equals(tr("OK"))) {
            save();
        } else {
        	m_oParam = null;
        }
        super.buttonAction(buttonIndex, evt);
    }
}
