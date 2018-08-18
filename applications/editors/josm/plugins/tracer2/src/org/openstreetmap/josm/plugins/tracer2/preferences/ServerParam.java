// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tracer2.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.widgets.JMultilineLabel;
import org.openstreetmap.josm.tools.GBC;

public class ServerParam {

    protected boolean m_bEnabled;
    private String m_strName = "Name";
    private String m_strDescription = "";
    private String m_strUrl = "";
    private String m_strTileSize = "0.0004";
    private String m_strResolution = "2048";
    private String m_strSkipBottom = "0";
    private String m_strMode = "boundary";
    private String m_strThreshold = "127";
    private String m_strPointsPerCircle = "16";
    private String m_strTag = "building";
    private String m_strPreferredValues = "yes;house;garage";

    protected JMenuItem m_oMenuItem;

    public boolean isEnabled() {
        return m_bEnabled;
    }

    public void setEnabled(boolean enabled) {
        if (!m_bEnabled ^ enabled)
            return;
        m_bEnabled = enabled;
    }

    public String getName() {
        return m_strName;
    }

    public void setName(String name) {
        m_strName = name;
    }

    public String getDescription() {
        return m_strDescription;
    }

    public void setDescription(String description) {
        m_strDescription = description;
    }

    public String getUrl() {
        return m_strUrl;
    }

    public void setUrl(String url) {
        m_strUrl = url;
    }

    public String getTileSize() {
        return m_strTileSize;
    }

    public void setTileSize(String tileSize) {
        m_strTileSize = tileSize;
    }

    public String getResolution() {
        return m_strResolution;
    }

    public void setResolution(String resolution) {
        m_strResolution = resolution;
    }

    public String getSkipBottom() {
        return m_strSkipBottom;
    }

    public void setSkipBottom(String skipBottom) {
        m_strSkipBottom = skipBottom;
    }

    public String getMode() {
        return m_strMode;
    }

    public void setMode(String mode) {
        m_strMode = mode;
    }

    public String getThreshold() {
        return m_strThreshold;
    }

    public void setThreshold(String threshold) {
        m_strThreshold = threshold;
    }

    public String getPointsPerCircle() {
        return m_strPointsPerCircle;
    }

    public void setPointsPerCircle(String pointsPerCircle) {
        m_strPointsPerCircle = pointsPerCircle;
    }

    public String getTag() {
        return m_strTag;
    }

    public void setTag(String tag) {
        m_strTag = tag;
    }

    public String getPreferredValues() {
        return m_strPreferredValues;
    }

    public void setPreferredValues(String preferredValues) {
        m_strPreferredValues = preferredValues;
    }

    public ServerParam() {
        m_bEnabled = false;
    }

    public ServerParam(String name) {
        this();
        m_strName = name;
    }

    public String serialize() {
        StringBuilder oBuilder = new StringBuilder();
        oBuilder.append("name=").append(m_strName).append('\n');
        oBuilder.append("description=").append(m_strDescription).append('\n');
        oBuilder.append("url=").append(m_strUrl).append('\n');
        oBuilder.append("tileSize=").append(m_strTileSize).append('\n');
        oBuilder.append("resolution=").append(m_strResolution).append('\n');
        oBuilder.append("skipBottom=").append(m_strSkipBottom).append('\n');
        oBuilder.append("mode=").append(m_strMode).append('\n');
        oBuilder.append("threshold=").append(m_strThreshold).append('\n');
        oBuilder.append("pointsPerCircle=").append(m_strPointsPerCircle).append('\n');
        oBuilder.append("tag=").append(m_strTag).append('\n');
        oBuilder.append("preferredValues=").append(m_strPreferredValues).append('\n');
        oBuilder.append("enabled=").append(m_bEnabled).append('\n');
        oBuilder.append('\n');
        return oBuilder.toString();
    }

    public static ServerParam unserialize(String str) {
        ServerParam oParam = new ServerParam();
        String[] lines = str.split("\n");
        for (String line : lines) {
            String[] parts = line.split("=", 2);
            if (parts[0].equals("name"))
                oParam.m_strName = parts[1];
            else if (parts[0].equals("description"))
                oParam.m_strDescription = parts[1];
            else if (parts[0].equals("url"))
                oParam.m_strUrl = parts[1];
            else if (parts[0].equals("tileSize"))
                oParam.m_strTileSize = parts[1];
            else if (parts[0].equals("resolution"))
                oParam.m_strResolution = parts[1];
            else if (parts[0].equals("skipBottom"))
                oParam.m_strSkipBottom = parts[1];
            else if (parts[0].equals("mode"))
                oParam.m_strMode = parts[1];
            else if (parts[0].equals("threshold"))
                oParam.m_strThreshold = parts[1];
            else if (parts[0].equals("pointsPerCircle"))
                oParam.m_strPointsPerCircle = parts[1];
            else if (parts[0].equals("tag"))
                oParam.m_strTag = parts[1];
            else if (parts[0].equals("preferredValues"))
                oParam.m_strPreferredValues = parts[1];
            else if (parts[0].equals("enabled"))
                oParam.m_bEnabled = parts[1].equalsIgnoreCase("true");
        }
        return oParam;
    }

    protected void showErrorMessage(String message, String details) {
        final JPanel p = new JPanel(new GridBagLayout());
        p.add(new JMultilineLabel(message), GBC.eol());
        if (details != null) {
            JTextArea info = new JTextArea(details, 20, 60);
            info.setCaretPosition(0);
            info.setEditable(false);
            p.add(new JScrollPane(info), GBC.eop());
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(), p, tr("Tracer2 error"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
