package sk.zdila.josm.plugin.simplify;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

public class SimplifyAreaPreferenceSetting extends DefaultTabPreferenceSetting {

    static final String DIST_FACTOR = "simplify-area.dist.factor";
    static final String DIST_THRESHOLD = "simplify-area.dist.threshold";
    static final String AREA_FACTOR = "simplify-area.area.factor";
    static final String AREA_THRESHOLD = "simplify-area.area.threshold";
    static final String ANGLE_FACTOR = "simplify-area.angle.factor";
    static final String ANGLE_THRESHOLD = "simplify-area.angle.threshold";
    static final String MERGE_THRESHOLD = "simplify-area.merge.threshold";

    private final JTextField mergeThreshold = new JTextField(8);
    private final JTextField angleThreshold = new JTextField(8);
    private final JTextField angleFactor = new JTextField(8);
    private final JTextField areaThreshold = new JTextField(8);
    private final JTextField areaFactor = new JTextField(8);
    private final JTextField distanceThreshold = new JTextField(8);
    private final JTextField distanceFactor = new JTextField(8);

    public SimplifyAreaPreferenceSetting() {
        super("simplifyArea", "Simplify Area", "Node of the way (area) is removed if all of <u>Angle Weight</u>, <u>Area Weight</u> and <u>Distance Weight</u> are greater than 1. " +
                "<u>Weight</u> is computed as <u>Value</u> / <u>Threshold</u>, where <u>Value</u> is one of <u>Angle</u>, <u>Area</u> and <u>Distance</u> " +
                "computed from every three adjanced points of the way." +
                "<ul><li><u>Value</u> of <u>Angle</u> is angle in degrees on the second node</li>" +
                "<li><u>Value</u> of <u>Area</u> is area formed by triangle</li>" +
                "<li><u>Value</u> of the <u>Distance</u> is Cross Track Error Distance</li></ul>" +
                "All three <u>Weight</u>s multiplied by its <u>Factor</u>s are summed and node of the lowest sum is removed first. " +
                "Removal continues until there is no node to remove." +
                "Merge Nearby Nodes is another step of the simplification that merges adjanced nodes that are closer than <u>Threshold</u> meters.");
    }

    @Override
    public void addGui(final PreferenceTabbedPane gui) {
        final JPanel tab = gui.createPreferenceTab(this);

        angleThreshold.setText(Main.pref.get(ANGLE_THRESHOLD, "10"));
        tab.add(new JLabel(tr("Angle Threshold")), GBC.std());
        tab.add(angleThreshold, GBC.eol().fill(GBC.HORIZONTAL).insets(5,0,0,5));

        angleFactor.setText(Main.pref.get(ANGLE_FACTOR, "1.0"));
        tab.add(new JLabel(tr("Angle Factor")), GBC.std());
        tab.add(angleFactor, GBC.eol().fill(GBC.HORIZONTAL).insets(5,0,0,5));

        areaThreshold.setText(Main.pref.get(AREA_THRESHOLD, "5.0"));
        tab.add(new JLabel(tr("Area Threshold")), GBC.std());
        tab.add(areaThreshold, GBC.eol().fill(GBC.HORIZONTAL).insets(5,0,0,5));

        areaFactor.setText(Main.pref.get(AREA_FACTOR, "1.0"));
        tab.add(new JLabel(tr("Area Factor")), GBC.std());
        tab.add(areaFactor, GBC.eol().fill(GBC.HORIZONTAL).insets(5,0,0,5));

        distanceThreshold.setText(Main.pref.get(DIST_THRESHOLD, "3"));
        tab.add(new JLabel(tr("Distance Threshold")), GBC.std());
        tab.add(distanceThreshold, GBC.eol().fill(GBC.HORIZONTAL).insets(5,0,0,5));

        distanceFactor.setText(Main.pref.get(DIST_FACTOR, "3"));
        tab.add(new JLabel(tr("Distance Factor")), GBC.std());
        tab.add(distanceFactor, GBC.eol().fill(GBC.HORIZONTAL).insets(5,0,0,5));

        mergeThreshold.setText(Main.pref.get(MERGE_THRESHOLD, "0.2"));
        // mergeThreshold.setToolTipText(tr("bla bla"));
        tab.add(new JLabel(tr("Merge Nearby Nodes Threshold")), GBC.std());
        tab.add(mergeThreshold, GBC.eol().fill(GBC.HORIZONTAL).insets(5,0,0,5));

        tab.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
    }

    @Override
    public boolean ok() {
        Main.pref.put(MERGE_THRESHOLD, mergeThreshold.getText());
        Main.pref.put(ANGLE_THRESHOLD, angleThreshold.getText());
        Main.pref.put(ANGLE_FACTOR, angleFactor.getText());
        Main.pref.put(AREA_THRESHOLD, areaThreshold.getText());
        Main.pref.put(AREA_FACTOR, areaFactor.getText());
        Main.pref.put(DIST_THRESHOLD, distanceThreshold.getText());
        Main.pref.put(DIST_FACTOR, distanceFactor.getText());
        return false;
    }

}
