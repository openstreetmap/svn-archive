package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;

/**
 * Copyright 2008, Friedrich Maier 
 * 
 * This file is part of JTileDownloader. 
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 *    JTileDownloader is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JTileDownloader is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy (see file COPYING.txt) of the GNU 
 *    General Public License along with JTileDownloader.  
 *    If not, see <http://www.gnu.org/licenses/>.
 */
public class NetworkPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    JCheckBox _chkUseProxyServer = new JCheckBox("Use Proxy Server?");
    JLabel _labelProxyServer = new JLabel("Proxy Server:");
    JTextField _textProxyServer = new JTextField();
    JLabel _labelProxyPort = new JLabel("Proxy Port:");
    JTextField _textProxyPort = new JTextField();
    JCheckBox _chkAuthRequired = new JCheckBox("Authentication required?");
    JLabel _labelProxyUser = new JLabel("Proxy User:");
    JTextField _textProxyUser = new JTextField();
    JLabel _labelProxyPassword = new JLabel("Proxy Password:");
    JTextField _textProxyPassWord = new JPasswordField();

    /**
     * 
     */
    public NetworkPanel()
    {
        super();

        createNetworkPanel();
        initializeNetworkPanel();

    }

    /**
     * @return
     */
    private void createNetworkPanel()
    {
        setLayout(new GridBagLayout());
        JPanel panelProxySettings = new JPanel();

        GridBagConstraints constraintsProxySettings = new GridBagConstraints();
        constraintsProxySettings.gridwidth = GridBagConstraints.REMAINDER;
        constraintsProxySettings.weightx = 1.0;
        constraintsProxySettings.fill = GridBagConstraints.HORIZONTAL;
        constraintsProxySettings.insets = new Insets(5, 5, 0, 5);

        panelProxySettings.setLayout(new GridBagLayout());
        Border border = new TitledBorder("Proxy Settings");
        panelProxySettings.setBorder(border);

        panelProxySettings.add(_chkUseProxyServer, constraintsProxySettings);
        panelProxySettings.add(_labelProxyServer, constraintsProxySettings);
        panelProxySettings.add(_textProxyServer, constraintsProxySettings);
        panelProxySettings.add(_labelProxyPort, constraintsProxySettings);
        panelProxySettings.add(_textProxyPort, constraintsProxySettings);
        panelProxySettings.add(_chkAuthRequired, constraintsProxySettings);
        panelProxySettings.add(_labelProxyUser, constraintsProxySettings);
        panelProxySettings.add(_textProxyUser, constraintsProxySettings);
        panelProxySettings.add(_labelProxyPassword, constraintsProxySettings);
        panelProxySettings.add(_textProxyPassWord, constraintsProxySettings);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 5, 0, 5);
        add(panelProxySettings, constraints);

        constraints.weighty = 1.0;
        add(new JPanel(), constraints);

    }

    /**
     * 
     */
    private void initializeNetworkPanel()
    {
        _chkUseProxyServer.setSelected(AppConfiguration.getInstance().getUseProxyServer());
        _textProxyServer.setText(AppConfiguration.getInstance().getProxyServer());
        _textProxyPort.setText(AppConfiguration.getInstance().getProxyServerPort());
        _chkAuthRequired.setSelected(AppConfiguration.getInstance().getUseProxyServerAuth());
        _textProxyUser.setText(AppConfiguration.getInstance().getProxyServerUser());
    }

    /**
     * @return
     */
    public boolean isUseProxyServer()
    {
        return _chkUseProxyServer.isSelected();
    }

    /**
     * @return
     */
    public String getProxyServer()
    {
        return _textProxyServer.getText().trim();
    }

    /**
     * @return
     */
    public String getProxyServerPort()
    {
        return _textProxyPort.getText().trim();
    }

    /**
     * @return
     */
    public boolean isUseProxyServerAuth()
    {
        return _chkAuthRequired.isSelected();
    }

    /**
     * @return
     */
    public String getProxyServerUser()
    {
        return _textProxyUser.getText().trim();
    }

}
