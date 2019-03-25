// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fastdraw;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.datatransfer.ClipboardUtils;
import org.openstreetmap.josm.gui.datatransfer.importers.TextTagPaster;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.TextTagParser;

public class FastDrawConfigDialog extends ExtendedDialog {

    private final JLabel label1 = new JLabel(tr("Epsilon multiplier"));
    private final JLabel label2 = new JLabel(tr("Starting Epsilon"));
    private final JLabel label3 = new JLabel(tr("Max points count per 1 km"));
    private final JLabel label4 = new JLabel(/* I18n: Combobox to select what a press to return key does */ tr("Enter key mode"));
    private final JLabel label5 = new JLabel(tr("Auto add tags"));
    private final JFormattedTextField text1 = new JFormattedTextField(NumberFormat.getInstance());
    private final JFormattedTextField text2 = new JFormattedTextField(NumberFormat.getInstance());
    private final JFormattedTextField text3 = new JFormattedTextField(NumberFormat.getInstance());
    private final JComboBox<String> combo1 = new JComboBox<>(new String[]{tr("Autosimplify"),
            tr("Simplify with initial epsilon"), tr("Save as is")});
    private final JCheckBox snapCb = new JCheckBox(tr("Snap to nodes"));
    private final JCheckBox fixedClickCb = new JCheckBox(tr("Add fixed points on click"));
    private final JCheckBox fixedSpaceCb = new JCheckBox(tr("Add fixed points on spacebar"));
    private final JCheckBox allowEditExistingWaysCb = new JCheckBox(tr("Allow edit existing ways"));
    private final JCheckBox drawClosedCb = new JCheckBox(tr("Draw closed polygons only"));
    private final HistoryComboBox addTags = new HistoryComboBox();
    private final FDSettings settings;

    public FastDrawConfigDialog(FDSettings settings) {
        super(MainApplication.getMainFrame(), tr("FastDraw configuration"), new String[] {tr("Ok"), tr("Cancel")});
        this.settings = settings;

        JPanel all = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        all.setLayout(layout);
        JButton pasteButton = new JButton(new AbstractAction(tr("Paste"), ImageProvider.get("apply")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = ClipboardUtils.getClipboardStringContent();
                if (s != null) {
                    if (TextTagParser.getValidatedTagsFromText(s, TextTagPaster::warning) != null) {
                        addTags.setText(s);
                    }
                }
            }
        });
        pasteButton.setToolTipText(tr("Try copying tags from properties table"));

        ArrayList<String> history = new ArrayList<>(Config.getPref().getList("fastdraw.tags-history"));
        while (history.remove("")) { };
        addTags.setPossibleItems(history);

        all.add(label1, GBC.std().insets(10, 0, 0, 0));
        all.add(text1, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));
        all.add(label2, GBC.std().insets(10, 0, 0, 0));
        all.add(text2, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));
        all.add(label3, GBC.std().insets(10, 0, 0, 0));
        all.add(text3, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));
        all.add(label4, GBC.std().insets(10, 0, 0, 0));
        all.add(combo1, GBC.eop().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        all.add(label5, GBC.std().insets(10, 0, 0, 0));
        all.add(pasteButton, GBC.eop().insets(0, 0, 0, 5));

        all.add(addTags, GBC.eop().fill(GBC.HORIZONTAL).insets(10, 0, 5, 10));

        all.add(snapCb, GBC.eop().insets(20, 0, 0, 0));

        all.add(fixedClickCb, GBC.eop().insets(20, 0, 0, 0));
        all.add(fixedSpaceCb, GBC.eop().insets(20, 0, 0, 0));
        all.add(drawClosedCb, GBC.eop().insets(20, 0, 0, 0));

        all.add(allowEditExistingWaysCb, GBC.eop().insets(20, 0, 0, 0));

        addTags.setText(settings.autoTags);
        text1.setValue(settings.epsilonMult);
        text2.setValue(settings.startingEps);
        text3.setValue(settings.maxPointsPerKm);
        snapCb.setSelected(settings.snapNodes);
        fixedClickCb.setSelected(settings.fixedClick);
        fixedSpaceCb.setSelected(settings.fixedSpacebar);
        drawClosedCb.setSelected(settings.drawClosed);
        allowEditExistingWaysCb.setSelected(settings.allowEditExistingWays);
        combo1.setSelectedIndex(settings.simplifyMode);

        setContent(all, false);
        setButtonIcons(new String[] {"ok.png", "cancel.png"});
        setToolTipTexts(new String[] {
                tr("Save settings"),
                tr("Cancel")
        });
        setDefaultButton(1);
        //configureContextsensitiveHelp("/Action/DownloadObject", true /* show help button */);
    }

    @Override
    public ExtendedDialog showDialog() {
        ExtendedDialog result = super.showDialog();
        if (getValue() == 1) {
            try {
                settings.epsilonMult = NumberFormat.getInstance().parse(text1.getText()).doubleValue();
                settings.startingEps = NumberFormat.getInstance().parse(text2.getText()).doubleValue();
                settings.maxPointsPerKm = NumberFormat.getInstance().parse(text3.getText()).doubleValue();
                settings.snapNodes = snapCb.isSelected();
                settings.fixedClick = fixedClickCb.isSelected();
                settings.fixedSpacebar = fixedSpaceCb.isSelected();
                settings.allowEditExistingWays = allowEditExistingWaysCb.isSelected();
                settings.drawClosed = drawClosedCb.isSelected();
                settings.simplifyMode = combo1.getSelectedIndex();
                settings.autoTags = addTags.getText();
                if (!settings.autoTags.isEmpty()) {
                    addTags.addCurrentItemToHistory();
                }
                Config.getPref().putList("fastdraw.tags-history", addTags.getHistory());
                settings.savePrefs();
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                        tr("Can not read settings"));
            }
        }
        return result;
    }
}
