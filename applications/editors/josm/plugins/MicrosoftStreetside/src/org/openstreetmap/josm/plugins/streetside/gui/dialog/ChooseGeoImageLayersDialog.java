// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;

public class ChooseGeoImageLayersDialog extends JDialog {
  private static final long serialVersionUID = -1793622345412435234L;
  private static final String QUESTION = I18n.marktr("Which image layers do you want to import into the Streetside layer?");

  public ChooseGeoImageLayersDialog(final Component parent, final List<GeoImageLayer> layers) {
      super(GuiHelper.getFrameForComponent(parent), I18n.tr(QUESTION));
    final Container c = getContentPane();
    c.setLayout(new BorderLayout(10, 10));

    final JPanel questionPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
    questionPanel.add(new JLabel(I18n.tr(QUESTION)));
    c.add(questionPanel, BorderLayout.NORTH);

    final JList<GeoImageLayer> list = new JList<>();
    list.setModel(new BasicListModel<>(layers));
    list.setCellRenderer(new GeoImageLayerListCellRenderer());
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    c.add(list, BorderLayout.CENTER);

    final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    final JButton cancelButton = new JButton(I18n.tr("Cancel"), new ImageProvider("cancel").get());
    cancelButton.addActionListener(e -> dispose());
    cancelButton.requestFocus();
    buttonPanel.add(cancelButton);
    final JButton importButton = new JButton(I18n.tr("Import"), new ImageProvider("copy").get());

    // Streetside does not support importing images (Mapillary relic)
    importButton.setEnabled(false);

    buttonPanel.add(importButton);
    c.add(buttonPanel, BorderLayout.SOUTH);

    setModal(true);
    pack();
    setLocationRelativeTo(parent);
  }

  protected static class GeoImageLayerListCellRenderer implements ListCellRenderer<GeoImageLayer> {
    @Override
    public Component getListCellRendererComponent(
      JList<? extends GeoImageLayer> list, GeoImageLayer value, int index, boolean isSelected, boolean cellHasFocus
    ) {
      final JLabel result = value == null
          ? null
          /* i18n: {0} is the layer name, {1} the number of images in it */
          : new JLabel(I18n.tr("{0} ({1} images)", value.getName(), value.getImages().size()), value.getIcon(), SwingConstants.LEADING);
      if (result != null) {
        result.setOpaque(true);
        result.setBackground(isSelected ? UIManager.getColor("List.selectionBackground") : UIManager.getColor("List.background"));
      }
      return result;
    }
  }

  private static class BasicListModel<T> extends AbstractListModel<T> {
    private static final long serialVersionUID = 3107247955341855290L;
    private final List<T> list;

    public BasicListModel(List<T> list) {
      this.list = list == null ? new ArrayList<>() : list;
    }

    @Override
    public int getSize() {
      return list.size();
    }

    @Override
    public T getElementAt(int index) {
      return list.get(index);
    }
  }
}
