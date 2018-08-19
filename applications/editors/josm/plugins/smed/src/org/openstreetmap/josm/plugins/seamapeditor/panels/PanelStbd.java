// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.seamapeditor.panels;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openstreetmap.josm.plugins.seamapeditor.SmedAction;
import org.openstreetmap.josm.plugins.seamapeditor.messages.Messages;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Cat;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Col;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Obj;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Pat;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Shp;

public class PanelStbd extends JPanel {

    private SmedAction dlg;
    public ButtonGroup shapeButtons = new ButtonGroup();
    public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
    public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
    public JRadioButton coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
    public JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
    public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
    public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
    public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
    public JRadioButton perchButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PerchSButton.png")));
    public JRadioButton stakeButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/StakeButton.png")));
    public EnumMap<Shp, JRadioButton> shapes = new EnumMap<>(Shp.class);
    public EnumMap<Shp, Obj> objects = new EnumMap<>(Shp.class);
    public ActionListener alShape = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Shp shp : shapes.keySet()) {
                JRadioButton button = shapes.get(shp);
                if (button.isSelected()) {
                    SmedAction.panelMain.mark.setShape(shp);
                    SmedAction.panelMain.mark.setObject(objects.get(shp));
                    button.setBorderPainted(true);
                } else {
                    button.setBorderPainted(false);
                }
            }
            if (SmedAction.panelMain.mark.testValid()) {
                SmedAction.panelMain.panelChan.topmarkButton.setVisible(true);
                SmedAction.panelMain.panelChan.lightButton.setVisible(true);
                if (SmedAction.panelMain.mark.getCategory() == Cat.LAM_STBD) {
                    switch (SmedAction.panelMain.mark.getRegion()) {
                    case A:
                        SmedAction.panelMain.mark.setObjPattern(Pat.NOPAT);
                        SmedAction.panelMain.mark.setObjColour(Col.GREEN);
                        break;
                    case B:
                        SmedAction.panelMain.mark.setObjPattern(Pat.NOPAT);
                        SmedAction.panelMain.mark.setObjColour(Col.RED);
                        break;
                    case C:
                        SmedAction.panelMain.mark.setObjPattern(Pat.HSTRP);
                        SmedAction.panelMain.mark.setObjColour(Col.GREEN);
                        SmedAction.panelMain.mark.addObjColour(Col.WHITE);
                        SmedAction.panelMain.mark.addObjColour(Col.GREEN);
                        SmedAction.panelMain.mark.addObjColour(Col.WHITE);
                        break;
                    default:
                        break;
                    }
                } else {
                    SmedAction.panelMain.mark.setObjPattern(Pat.HSTRP);
                    switch (SmedAction.panelMain.mark.getRegion()) {
                    case A:
                        SmedAction.panelMain.mark.setObjColour(Col.GREEN);
                        SmedAction.panelMain.mark.addObjColour(Col.RED);
                        SmedAction.panelMain.mark.addObjColour(Col.GREEN);
                        break;
                    case B:
                        SmedAction.panelMain.mark.setObjColour(Col.RED);
                        SmedAction.panelMain.mark.addObjColour(Col.GREEN);
                        SmedAction.panelMain.mark.addObjColour(Col.RED);
                        break;
                    case C:
                        SmedAction.panelMain.mark.setObjColour(Col.RED);
                        SmedAction.panelMain.mark.addObjColour(Col.GREEN);
                        SmedAction.panelMain.mark.addObjColour(Col.RED);
                        SmedAction.panelMain.mark.addObjColour(Col.GREEN);
                        break;
                    default:
                        break;
                    }
                }
                SmedAction.panelMain.panelMore.syncPanel();
            } else {
                SmedAction.panelMain.panelChan.topmarkButton.setVisible(false);
                SmedAction.panelMain.panelChan.lightButton.setVisible(false);
            }
        }
    };

    public PanelStbd(SmedAction dia) {
        dlg = dia;
        setLayout(null);
        add(getShapeButton(pillarButton, 0, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYLAT));
        add(getShapeButton(sparButton, 0, 32, 34, 32, "Spar", Shp.SPAR, Obj.BOYLAT));
        add(getShapeButton(coneButton, 0, 64, 34, 32, "Cone", Shp.CONI, Obj.BOYLAT));
        add(getShapeButton(sphereButton, 0, 96, 34, 32, "Sphere", Shp.SPHERI, Obj.BOYLAT));
        add(getShapeButton(floatButton, 0, 128, 34, 32, "Float", Shp.FLOAT, Obj.FLTLAT));
        add(getShapeButton(beaconButton, 35, 0, 34, 32, "Beacon", Shp.BEACON, Obj.BCNLAT));
        add(getShapeButton(towerButton, 35, 32, 34, 32, "TowerB", Shp.TOWER, Obj.BCNLAT));
        add(getShapeButton(perchButton, 35, 64, 34, 32, "Perch", Shp.PERCH, Obj.BCNLAT));
        add(getShapeButton(stakeButton, 35, 96, 34, 32, "Stake", Shp.STAKE, Obj.BCNLAT));
    }

    public void syncPanel() {
        for (Shp shp : shapes.keySet()) {
            JRadioButton button = shapes.get(shp);
            if (SmedAction.panelMain.mark.getShape() == shp) {
                button.setBorderPainted(true);
            } else {
                button.setBorderPainted(false);
            }
        }
    }

    private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp, Obj obj) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alShape);
        shapeButtons.add(button);
        shapes.put(shp, button);
        objects.put(shp, obj);
        return button;
    }

}
