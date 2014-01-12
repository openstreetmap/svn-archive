// License: GPL. For details, see LICENSE file.
package mirrored_download;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.io.MirroredInputStream;
import org.openstreetmap.josm.tools.Utils;

public class UrlSelectionDialog
{
  private JDialog jDialog = null;
  private JTabbedPane tabbedPane = null;
  private JComboBox cbSelectUrl = null;
  private JCheckBox cbAddMeta = null;

  public UrlSelectionDialog() {
    Frame frame = JOptionPane.getFrameForComponent(Main.parent);
    jDialog = new JDialog(frame, tr("Select OSM mirror URL"), false);
    tabbedPane = new JTabbedPane();
    JPanel tabSettings = new JPanel();
    tabbedPane.addTab(tr("Settings"), tabSettings);
    tabbedPane.setEnabledAt(0, true);
    jDialog.add(tabbedPane);

    //Settings Tab
    JPanel contentPane = tabSettings;
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints layoutCons = new GridBagConstraints();
    contentPane.setLayout(gridbag);

    JLabel label = new JLabel(tr("Base URL"));

    layoutCons.gridx = 0;
    layoutCons.gridy = 0;
    layoutCons.gridwidth = 2;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    cbSelectUrl = new JComboBox();
    cbSelectUrl.setEditable(true);

    cbSelectUrl.addItem(MirroredDownloadPlugin.getDownloadUrl());

    for (String url: getURLs()) {
      cbSelectUrl.addItem(url);
    }

    cbSelectUrl.setActionCommand("selectURL");
    cbSelectUrl.addActionListener(new UrlChangedAction());

    layoutCons.gridx = 0;
    layoutCons.gridy = 1;
    layoutCons.gridwidth = 2;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(cbSelectUrl, layoutCons);
    contentPane.add(cbSelectUrl);

    cbAddMeta = new JCheckBox(tr("Enforce meta data"));
    cbAddMeta.setEnabled(true);
    cbAddMeta.setSelected(MirroredDownloadPlugin.getAddMeta());

    cbAddMeta.setActionCommand("selectMetaFlag");
    cbAddMeta.addActionListener(new MetaFlagChangedAction());

    layoutCons.gridx = 0;
    layoutCons.gridy = 2;
    layoutCons.gridwidth = 2;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(cbAddMeta, layoutCons);
    contentPane.add(cbAddMeta);

    jDialog.pack();
    jDialog.setLocationRelativeTo(frame);
  }

  private Collection<String> getURLs() {
    // List can be edited at http://josm.openstreetmap.de/wiki/MirroredDownloadInfo
    String src = Main.pref.get("plugin.mirrored_download.url-src", "http://josm.openstreetmap.de/mirrored_download_info");
    Collection<String> urls = new ArrayList<String>();
    InputStream in = null;
    try {
      in = new MirroredInputStream(src, 24*60*60);
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line = null;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.equals("")) {
          urls.add(line);
        }
      }
      Utils.close(reader);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      Utils.close(in);
    }
    for (String url : Main.pref.getCollection("plugin.mirrored_download.custom-urls")) {
      urls.add(url);
    }
    return urls;
  }

  public class UrlChangedAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      MirroredDownloadPlugin.setDownloadUrl(cbSelectUrl.getSelectedItem().toString());
      Main.pref.put("plugin.mirrored_download.preferred-url",
          cbSelectUrl.getSelectedItem().toString());
    }

  }

  public class MetaFlagChangedAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      MirroredDownloadPlugin.setAddMeta(cbAddMeta.isSelected());
      Main.pref.put("plugin.mirrored_download.preferred-meta-flag",
          cbAddMeta.isSelected() ? "meta" : "void");
    }

  }

  public void setVisible(boolean visible) {
    jDialog.setVisible(visible);
  }

  private static UrlSelectionDialog singleton = null;

  public static UrlSelectionDialog getInstance() {

    if (singleton == null)
      singleton = new UrlSelectionDialog();

    return singleton;
  }
}
