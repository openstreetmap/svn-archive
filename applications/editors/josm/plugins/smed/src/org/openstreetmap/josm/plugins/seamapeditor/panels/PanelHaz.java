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
import javax.swing.JToggleButton;

import org.openstreetmap.josm.plugins.seamapeditor.SmedAction;
import org.openstreetmap.josm.plugins.seamapeditor.messages.Messages;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Att;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Cat;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Col;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Grp;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Obj;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Pat;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Shp;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Top;

public class PanelHaz extends JPanel {

    private SmedAction dlg;
    public ButtonGroup catButtons = new ButtonGroup();
    public JRadioButton northButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardNButton.png")));
    public JRadioButton southButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardSButton.png")));
    public JRadioButton eastButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardEButton.png")));
    public JRadioButton westButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardWButton.png")));
    public JRadioButton isolButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/IsolButton.png")));
    private ActionListener alCat = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            SmedAction.panelMain.mark.setObjPattern(Pat.HSTRP);
            if (northButton.isSelected()) {
                SmedAction.panelMain.mark.setCategory(Cat.CAM_NORTH);
                SmedAction.panelMain.mark.setObjColour(Col.BLACK);
                SmedAction.panelMain.mark.addObjColour(Col.YELLOW);
                northButton.setBorderPainted(true);
            } else {
                northButton.setBorderPainted(false);
            }
            if (southButton.isSelected()) {
                SmedAction.panelMain.mark.setCategory(Cat.CAM_SOUTH);
                SmedAction.panelMain.mark.setObjColour(Col.YELLOW);
                SmedAction.panelMain.mark.addObjColour(Col.BLACK);
                southButton.setBorderPainted(true);
            } else {
                southButton.setBorderPainted(false);
            }
            if (eastButton.isSelected()) {
                SmedAction.panelMain.mark.setCategory(Cat.CAM_EAST);
                SmedAction.panelMain.mark.setObjColour(Col.BLACK);
                SmedAction.panelMain.mark.addObjColour(Col.YELLOW);
                SmedAction.panelMain.mark.addObjColour(Col.BLACK);
                eastButton.setBorderPainted(true);
            } else {
                eastButton.setBorderPainted(false);
            }
            if (westButton.isSelected()) {
                SmedAction.panelMain.mark.setCategory(Cat.CAM_WEST);
                SmedAction.panelMain.mark.setObjColour(Col.YELLOW);
                SmedAction.panelMain.mark.addObjColour(Col.BLACK);
                SmedAction.panelMain.mark.addObjColour(Col.YELLOW);
                westButton.setBorderPainted(true);
            } else {
                westButton.setBorderPainted(false);
            }
            if (isolButton.isSelected()) {
                SmedAction.panelMain.mark.setCategory(Cat.NOCAT);
                SmedAction.panelMain.mark.setObjColour(Col.BLACK);
                SmedAction.panelMain.mark.addObjColour(Col.RED);
                SmedAction.panelMain.mark.addObjColour(Col.BLACK);
                isolButton.setBorderPainted(true);
            } else {
                isolButton.setBorderPainted(false);
            }
            topmarkButton.setVisible(SmedAction.panelMain.mark.testValid());
            lightButton.setVisible(SmedAction.panelMain.mark.testValid());
            SmedAction.panelMain.panelMore.syncPanel();
        }
    };
    private ButtonGroup shapeButtons = new ButtonGroup();
    public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
    public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
    public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
    public JRadioButton canButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CanButton.png")));
    public JRadioButton coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
    public JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
    public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
    public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
    public EnumMap<Shp, JRadioButton> shapes = new EnumMap<>(Shp.class);
    public EnumMap<Shp, Obj> carObjects = new EnumMap<>(Shp.class);
    public EnumMap<Shp, Obj> isdObjects = new EnumMap<>(Shp.class);
    private ActionListener alShape = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Shp shp : shapes.keySet()) {
                JRadioButton button = shapes.get(shp);
                if (button.isSelected()) {
                    SmedAction.panelMain.mark.setShape(shp);
                    if (isolButton.isSelected()) {
                        SmedAction.panelMain.mark.setObject(isdObjects.get(shp));
                    } else {
                        SmedAction.panelMain.mark.setObject(carObjects.get(shp));
                    }
                    button.setBorderPainted(true);
                } else {
                    button.setBorderPainted(false);
                }
            }
            topmarkButton.setVisible(SmedAction.panelMain.mark.testValid());
            lightButton.setVisible(SmedAction.panelMain.mark.testValid());
        }
    };
    public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/HazTopButton.png")));
    private ActionListener alTop = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (topmarkButton.isSelected()) {
                SmedAction.panelMain.mark.setTopPattern(Pat.NOPAT);
                SmedAction.panelMain.mark.setTopColour(Col.BLACK);
                switch (SmedAction.panelMain.mark.getCategory()) {
                case CAM_NORTH:
                    SmedAction.panelMain.mark.setTopmark(Top.NORTH);
                    break;
                case CAM_SOUTH:
                    SmedAction.panelMain.mark.setTopmark(Top.SOUTH);
                    break;
                case CAM_EAST:
                    SmedAction.panelMain.mark.setTopmark(Top.EAST);
                    break;
                case CAM_WEST:
                    SmedAction.panelMain.mark.setTopmark(Top.WEST);
                    break;
                default:
                    SmedAction.panelMain.mark.setTopmark(Top.SPHERES2);
                    break;
                }
                topmarkButton.setBorderPainted(true);
            } else {
                SmedAction.panelMain.mark.setTopmark(Top.NOTOP);
                SmedAction.panelMain.mark.setTopPattern(Pat.NOPAT);
                SmedAction.panelMain.mark.setTopColour(Col.UNKCOL);
                topmarkButton.setBorderPainted(false);
            }
            SmedAction.panelMain.panelTop.syncPanel();
        }
    };
    public JToggleButton lightButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/DefLitButton.png")));
    private ActionListener alLit = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (lightButton.isSelected()) {
                SmedAction.panelMain.mark.setLightAtt(Att.COL, 0, Col.WHITE);
                switch (SmedAction.panelMain.mark.getCategory()) {
                case CAM_NORTH:
                    SmedAction.panelMain.mark.setLightAtt(Att.CHR, 0, "Q");
                    SmedAction.panelMain.mark.setLightAtt(Att.GRP, 0, "");
                    break;
                case CAM_SOUTH:
                    SmedAction.panelMain.mark.setLightAtt(Att.CHR, 0, "Q+LFl");
                    SmedAction.panelMain.mark.setLightAtt(Att.GRP, 0, "6");
                    break;
                case CAM_EAST:
                    SmedAction.panelMain.mark.setLightAtt(Att.CHR, 0, "Q");
                    SmedAction.panelMain.mark.setLightAtt(Att.GRP, 0, "3");
                    break;
                case CAM_WEST:
                    SmedAction.panelMain.mark.setLightAtt(Att.CHR, 0, "Q");
                    SmedAction.panelMain.mark.setLightAtt(Att.GRP, 0, "9");
                    break;
                default:
                    SmedAction.panelMain.mark.setLightAtt(Att.CHR, 0, "Fl");
                    SmedAction.panelMain.mark.setLightAtt(Att.GRP, 0, "2");
                    break;
                }
                lightButton.setBorderPainted(true);
            } else {
                SmedAction.panelMain.mark.clrLight();
                lightButton.setBorderPainted(false);
            }
            SmedAction.panelMain.panelLit.syncPanel();
        }
    };

    public PanelHaz(SmedAction dia) {
        dlg = dia;
        setLayout(null);
        add(getCatButton(northButton, 0, 0, 52, 32, "North"));
        add(getCatButton(southButton, 0, 32, 52, 32, "South"));
        add(getCatButton(eastButton, 0, 64, 52, 32, "East"));
        add(getCatButton(westButton, 0, 96, 52, 32, "West"));
        add(getCatButton(isolButton, 0, 128, 52, 32, "Isol"));

        add(getShapeButton(pillarButton, 55, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYCAR, Obj.BOYISD));
        add(getShapeButton(sparButton, 55, 32, 34, 32, "Spar", Shp.SPAR, Obj.BOYCAR, Obj.BOYISD));
        add(getShapeButton(canButton, 55, 64, 34, 32, "Can", Shp.CAN, Obj.BOYCAR, Obj.BOYISD));
        add(getShapeButton(coneButton, 55, 96, 34, 32, "Cone", Shp.CONI, Obj.BOYCAR, Obj.BOYISD));
        add(getShapeButton(sphereButton, 55, 128, 34, 32, "Sphere", Shp.SPHERI, Obj.BOYCAR, Obj.BOYISD));
        add(getShapeButton(floatButton, 90, 0, 34, 32, "Float", Shp.FLOAT, Obj.LITFLT, Obj.LITFLT));
        add(getShapeButton(beaconButton, 90, 32, 34, 32, "Beacon", Shp.BEACON, Obj.BCNCAR, Obj.BCNISD));
        add(getShapeButton(towerButton, 90, 64, 34, 32, "TowerB", Shp.TOWER, Obj.BCNCAR, Obj.BCNISD));

        topmarkButton.setBounds(new Rectangle(130, 0, 34, 32));
        topmarkButton.setToolTipText(Messages.getString("Topmark"));
        topmarkButton.setBorder(BorderFactory.createLoweredBevelBorder());
        topmarkButton.addActionListener(alTop);
        topmarkButton.setVisible(false);
        add(topmarkButton);
        lightButton.setBounds(new Rectangle(130, 32, 34, 32));
        lightButton.setToolTipText(Messages.getString("Light"));
        lightButton.setBorder(BorderFactory.createLoweredBevelBorder());
        lightButton.addActionListener(alLit);
        lightButton.setVisible(false);
        add(lightButton);
    }

    public void syncPanel() {
        northButton.setBorderPainted(SmedAction.panelMain.mark.getCategory() == Cat.CAM_NORTH);
        southButton.setBorderPainted(SmedAction.panelMain.mark.getCategory() == Cat.CAM_SOUTH);
        eastButton.setBorderPainted(SmedAction.panelMain.mark.getCategory() == Cat.CAM_EAST);
        westButton.setBorderPainted(SmedAction.panelMain.mark.getCategory() == Cat.CAM_WEST);
        isolButton.setBorderPainted(SeaMark.GrpMAP.get(SmedAction.panelMain.mark.getObject()) == Grp.ISD);
        for (Shp shp : shapes.keySet()) {
            JRadioButton button = shapes.get(shp);
            button.setBorderPainted(SmedAction.panelMain.mark.getShape() == shp);
        }
        topmarkButton.setBorderPainted(SmedAction.panelMain.mark.getTopmark() != Top.NOTOP);
        topmarkButton.setSelected(SmedAction.panelMain.mark.getTopmark() != Top.NOTOP);
        topmarkButton.setVisible(SmedAction.panelMain.mark.testValid());
        Boolean lit = (SmedAction.panelMain.mark.getLightAtt(Att.COL, 0) != Col.UNKCOL)
                && !((String) SmedAction.panelMain.mark.getLightAtt(Att.CHR, 0)).isEmpty();
        lightButton.setBorderPainted(lit);
        lightButton.setSelected(lit);
        lightButton.setVisible(SmedAction.panelMain.mark.testValid());
    }

    private JRadioButton getCatButton(JRadioButton button, int x, int y, int w, int h, String tip) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alCat);
        catButtons.add(button);
        return button;
    }

    private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp, Obj car, Obj isd) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alShape);
        shapeButtons.add(button);
        shapes.put(shp, button);
        carObjects.put(shp, car);
        isdObjects.put(shp, isd);
        return button;
    }

}
