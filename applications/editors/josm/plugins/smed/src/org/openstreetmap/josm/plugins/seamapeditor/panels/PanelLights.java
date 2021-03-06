// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.seamapeditor.panels;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EnumMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.openstreetmap.josm.plugins.seamapeditor.SmedAction;
import org.openstreetmap.josm.plugins.seamapeditor.messages.Messages;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Cat;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Fnc;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Obj;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Shp;

public class PanelLights extends JPanel {

    private SmedAction dlg;

    public JLabel categoryLabel;

    public JComboBox<String> landCatBox;
    public EnumMap<Cat, Integer> landCats = new EnumMap<>(Cat.class);
    private ActionListener alLandCatBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : landCats.keySet()) {
                int idx = landCats.get(cat);
                if (dlg.node != null && (idx == landCatBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                    SmedAction.panelMain.mark.testValid();
                }
            }
        }
    };
    public JComboBox<String> trafficCatBox;
    public EnumMap<Cat, Integer> trafficCats = new EnumMap<>(Cat.class);
    private ActionListener alTrafficCatBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : trafficCats.keySet()) {
                int idx = trafficCats.get(cat);
                if (dlg.node != null && (idx == trafficCatBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                    SmedAction.panelMain.mark.testValid();
                }
            }
        }
    };
    public JComboBox<String> warningCatBox;
    public EnumMap<Cat, Integer> warningCats = new EnumMap<>(Cat.class);
    private ActionListener alWarningCatBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : warningCats.keySet()) {
                int idx = warningCats.get(cat);
                if (dlg.node != null && (idx == warningCatBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                    SmedAction.panelMain.mark.testValid();
                }
            }
        }
    };
    public JComboBox<String> platformCatBox;
    public EnumMap<Cat, Integer> platformCats = new EnumMap<>(Cat.class);
    private ActionListener alPlatformCatBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : platformCats.keySet()) {
                int idx = platformCats.get(cat);
                if (dlg.node != null && (idx == platformCatBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                    SmedAction.panelMain.mark.testValid();
                }
            }
        }
    };
    public JComboBox<String> pilotCatBox;
    public EnumMap<Cat, Integer> pilotCats = new EnumMap<>(Cat.class);
    private ActionListener alPilotCatBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : pilotCats.keySet()) {
                int idx = pilotCats.get(cat);
                if (dlg.node != null && (idx == pilotCatBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                    SmedAction.panelMain.mark.testValid();
                }
            }
        }
    };
    public JComboBox<String> rescueCatBox;
    public EnumMap<Cat, Integer> rescueCats = new EnumMap<>(Cat.class);
    private ActionListener alRescueCatBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : rescueCats.keySet()) {
                int idx = rescueCats.get(cat);
                if (dlg.node != null && (idx == rescueCatBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                    SmedAction.panelMain.mark.testValid();
                }
            }
        }
    };
    public JComboBox<String> radioCatBox;
    public EnumMap<Cat, Integer> radioCats = new EnumMap<>(Cat.class);
    private ActionListener alRadioCatBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : radioCats.keySet()) {
                int idx = radioCats.get(cat);
                if (dlg.node != null && (idx == radioCatBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                    SmedAction.panelMain.mark.testValid();
                }
            }
        }
    };
    public JComboBox<String> radarCatBox;
    public EnumMap<Cat, Integer> radarCats = new EnumMap<>(Cat.class);
    private ActionListener alRadarCatBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : radarCats.keySet()) {
                int idx = radarCats.get(cat);
                if (dlg.node != null && (idx == radarCatBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                    SmedAction.panelMain.mark.testValid();
                }
            }
        }
    };
    public JLabel functionLabel;
    public JComboBox<String> functionBox;
    public EnumMap<Fnc, Integer> functions = new EnumMap<>(Fnc.class);
    private ActionListener alfunctionBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Fnc fnc : functions.keySet()) {
                int idx = functions.get(fnc);
                if (dlg.node != null && (idx == functionBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setFunc(fnc);
                    SmedAction.panelMain.mark.testValid();
                }
            }
        }
    };
    private ButtonGroup objButtons = new ButtonGroup();
    public JRadioButton houseButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LighthouseButton.png")));
    public JRadioButton majorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMajorButton.png")));
    public JRadioButton minorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMinorButton.png")));
    public JRadioButton vesselButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightVesselButton.png")));
    public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightFloatButton.png")));
    public JRadioButton landButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LandmarkButton.png")));
    public JRadioButton trafficButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TrafficButton.png")));
    public JRadioButton warningButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WarningButton.png")));
    public JRadioButton platformButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PlatformButton.png")));
    public JRadioButton coastguardButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CoastguardButton.png")));
    public JRadioButton pilotButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PilotButton.png")));
    public JRadioButton rescueButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RescueButton.png")));
    public JRadioButton radioButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RadioStationButton.png")));
    public JRadioButton radarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RadarStationButton.png")));
    public EnumMap<Obj, JRadioButton> objects = new EnumMap<>(Obj.class);
    private ActionListener alObj = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Obj obj : objects.keySet()) {
                JRadioButton button = objects.get(obj);
                if (button.isSelected()) {
                    SmedAction.panelMain.mark.setObject(obj);
                    button.setBorderPainted(true);
                } else {
                    button.setBorderPainted(false);
                }
            }
            if (SmedAction.panelMain.mark.getObject() == Obj.LITVES) {
                SmedAction.panelMain.mark.setShape(Shp.SUPER);
            } else if (SmedAction.panelMain.mark.getObject() == Obj.LITFLT) {
                SmedAction.panelMain.mark.setShape(Shp.FLOAT);
            } else {
                SmedAction.panelMain.mark.setShape(Shp.UNKSHP);
            }
            functionLabel.setVisible(false);
            categoryLabel.setVisible(false);
            functionLabel.setVisible(false);
            functionBox.setVisible(false);
            landCatBox.setVisible(false);
            trafficCatBox.setVisible(false);
            warningCatBox.setVisible(false);
            platformCatBox.setVisible(false);
            pilotCatBox.setVisible(false);
            rescueCatBox.setVisible(false);
            radioCatBox.setVisible(false);
            radarCatBox.setVisible(false);
            chLabel.setVisible(false);
            chBox.setVisible(false);
            SmedAction.panelMain.mark.setCategory(Cat.NOCAT);
            if (landButton.isSelected()) {
                functionLabel.setVisible(true);
                categoryLabel.setVisible(true);
                functionBox.setVisible(true);
                landCatBox.setVisible(true);
                alLandCatBox.actionPerformed(null);
            } else if (trafficButton.isSelected()) {
                categoryLabel.setVisible(true);
                trafficCatBox.setVisible(true);
                chLabel.setVisible(true);
                chBox.setVisible(true);
                alTrafficCatBox.actionPerformed(null);
            } else if (warningButton.isSelected()) {
                categoryLabel.setVisible(true);
                warningCatBox.setVisible(true);
                chLabel.setVisible(true);
                chBox.setVisible(true);
                alWarningCatBox.actionPerformed(null);
            } else if (platformButton.isSelected()) {
                categoryLabel.setVisible(true);
                platformCatBox.setVisible(true);
                alPlatformCatBox.actionPerformed(null);
            } else if (pilotButton.isSelected()) {
                categoryLabel.setVisible(true);
                pilotCatBox.setVisible(true);
                chLabel.setVisible(true);
                chBox.setVisible(true);
                alPilotCatBox.actionPerformed(null);
            } else if (rescueButton.isSelected()) {
                categoryLabel.setVisible(true);
                rescueCatBox.setVisible(true);
                alRescueCatBox.actionPerformed(null);
            } else if (radioButton.isSelected()) {
                categoryLabel.setVisible(true);
                radioCatBox.setVisible(true);
                chLabel.setVisible(true);
                chBox.setVisible(true);
                alRadioCatBox.actionPerformed(null);
            } else if (radarButton.isSelected()) {
                categoryLabel.setVisible(true);
                radarCatBox.setVisible(true);
                chLabel.setVisible(true);
                chBox.setVisible(true);
                alRadarCatBox.actionPerformed(null);
            }
            SmedAction.panelMain.mark.testValid();
        }
    };
    public JLabel chLabel;
    public JTextField chBox;
    private FocusListener flCh = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            SmedAction.panelMain.mark.setChannel(chBox.getText());
        }
    };

    public PanelLights(SmedAction dia) {
        dlg = dia;
        setLayout(null);
        add(getObjButton(houseButton, 0, 0, 34, 32, "Lighthouse", Obj.LITHSE));
        add(getObjButton(majorButton, 34, 0, 34, 32, "MajorLight", Obj.LITMAJ));
        add(getObjButton(minorButton, 68, 0, 34, 32, "MinorLight", Obj.LITMIN));
        add(getObjButton(landButton, 102, 0, 34, 32, "Landmark", Obj.LNDMRK));
        add(getObjButton(platformButton, 136, 0, 34, 32, "Platform", Obj.OFSPLF));
        add(getObjButton(vesselButton, 0, 32, 34, 32, "LightVessel", Obj.LITVES));
        add(getObjButton(floatButton, 34, 32, 34, 32, "LightFloat", Obj.LITFLT));
        add(getObjButton(trafficButton, 68, 32, 34, 32, "SSTraffic", Obj.SISTAT));
        add(getObjButton(warningButton, 102, 32, 34, 32, "SSWarning", Obj.SISTAW));
        add(getObjButton(coastguardButton, 0, 64, 34, 32, "CoastguardStation", Obj.CGUSTA));
        add(getObjButton(pilotButton, 34, 64, 34, 32, "PilotBoarding", Obj.PILBOP));
        add(getObjButton(rescueButton, 68, 64, 34, 32, "RescueStation", Obj.RSCSTA));
        add(getObjButton(radioButton, 102, 64, 34, 32, "RadioStation", Obj.RDOSTA));
        add(getObjButton(radarButton, 136, 64, 34, 32, "RadarStation", Obj.RADSTA));

        functionLabel = new JLabel(Messages.getString("Function"), SwingConstants.CENTER);
        functionLabel.setBounds(new Rectangle(5, 94, 160, 18));
        add(functionLabel);
        functionLabel.setVisible(false);

        functionBox = new JComboBox<>();
        functionBox.setBounds(new Rectangle(5, 110, 160, 18));
        add(functionBox);
        functionBox.addActionListener(alfunctionBox);
        addLFItem("", Fnc.UNKFNC);
        addLFItem(Messages.getString("Church"), Fnc.CHCH);
        addLFItem(Messages.getString("Chapel"), Fnc.CHPL);
        addLFItem(Messages.getString("Temple"), Fnc.TMPL);
        addLFItem(Messages.getString("Pagoda"), Fnc.PGDA);
        addLFItem(Messages.getString("ShintoShrine"), Fnc.SHSH);
        addLFItem(Messages.getString("BuddhistTemple"), Fnc.BTMP);
        addLFItem(Messages.getString("Mosque"), Fnc.MOSQ);
        addLFItem(Messages.getString("Marabout"), Fnc.MRBT);
        functionBox.setVisible(false);

        categoryLabel = new JLabel(Messages.getString("Category"), SwingConstants.CENTER);
        categoryLabel.setBounds(new Rectangle(5, 125, 160, 18));
        add(categoryLabel);
        categoryLabel.setVisible(false);

        landCatBox = new JComboBox<>();
        landCatBox.setBounds(new Rectangle(5, 142, 160, 18));
        add(landCatBox);
        landCatBox.addActionListener(alLandCatBox);
        addLCItem("", Cat.NOCAT);
        addLCItem(Messages.getString("Tower"), Cat.LMK_TOWR);
        addLCItem(Messages.getString("Chimney"), Cat.LMK_CHMY);
        addLCItem(Messages.getString("Mast"), Cat.LMK_MAST);
        addLCItem(Messages.getString("Column"), Cat.LMK_CLMN);
        addLCItem(Messages.getString("DishAerial"), Cat.LMK_DSHA);
        addLCItem(Messages.getString("Flagstaff"), Cat.LMK_FLGS);
        addLCItem(Messages.getString("FlareStack"), Cat.LMK_FLRS);
        addLCItem(Messages.getString("Monument"), Cat.LMK_MNMT);
        addLCItem(Messages.getString("WindMotor"), Cat.LMK_WNDM);
        addLCItem(Messages.getString("WindSock"), Cat.LMK_WNDS);
        addLCItem(Messages.getString("Obelisk"), Cat.LMK_OBLK);
        addLCItem(Messages.getString("Statue"), Cat.LMK_STAT);
        addLCItem(Messages.getString("Cross"), Cat.LMK_CROS);
        addLCItem(Messages.getString("Dome"), Cat.LMK_DOME);
        addLCItem(Messages.getString("RadarScanner"), Cat.LMK_SCNR);
        addLCItem(Messages.getString("Windmill"), Cat.LMK_WNDL);
        addLCItem(Messages.getString("Spire"), Cat.LMK_SPIR);
        addLCItem(Messages.getString("Minaret"), Cat.LMK_MNRT);
        addLCItem(Messages.getString("Cairn"), Cat.LMK_CARN);
        landCatBox.setVisible(false);

        trafficCatBox = new JComboBox<>();
        trafficCatBox.setBounds(new Rectangle(5, 140, 160, 20));
        add(trafficCatBox);
        trafficCatBox.addActionListener(alTrafficCatBox);
        addTCItem("", Cat.NOCAT);
        addTCItem(Messages.getString("Traffic"), Cat.SIS_TRFC);
        addTCItem(Messages.getString("PortControl"), Cat.SIS_PTCL);
        addTCItem(Messages.getString("PortEntry"), Cat.SIS_PTED);
        addTCItem(Messages.getString("IPT"), Cat.SIS_IPT);
        addTCItem(Messages.getString("Berthing"), Cat.SIS_BRTH);
        addTCItem(Messages.getString("Dock"), Cat.SIS_DOCK);
        addTCItem(Messages.getString("Lock"), Cat.SIS_LOCK);
        addTCItem(Messages.getString("Barrage"), Cat.SIS_FBAR);
        addTCItem(Messages.getString("Bridge"), Cat.SIS_BRDG);
        addTCItem(Messages.getString("Dredging"), Cat.SIS_DRDG);
        trafficCatBox.setVisible(false);

        warningCatBox = new JComboBox<>();
        warningCatBox.setBounds(new Rectangle(5, 140, 160, 20));
        add(warningCatBox);
        warningCatBox.addActionListener(alWarningCatBox);
        addWCItem("", Cat.NOCAT);
        addWCItem(Messages.getString("Danger"), Cat.SIS_DNGR);
        addWCItem(Messages.getString("Storm"), Cat.SIS_STRM);
        addWCItem(Messages.getString("Weather"), Cat.SIS_WTHR);
        addWCItem(Messages.getString("Obstruction"), Cat.SIS_OBST);
        addWCItem(Messages.getString("Cable"), Cat.SIS_CABL);
        addWCItem(Messages.getString("Distress"), Cat.SIS_DSTR);
        addWCItem(Messages.getString("Time"), Cat.SIS_TIME);
        addWCItem(Messages.getString("Tide"), Cat.SIS_TIDE);
        addWCItem(Messages.getString("TidalStream"), Cat.SIS_TSTM);
        addWCItem(Messages.getString("TideGauge"), Cat.SIS_TGAG);
        addWCItem(Messages.getString("TideScale"), Cat.SIS_TSCL);
        addWCItem(Messages.getString("Diving"), Cat.SIS_DIVE);
        addWCItem(Messages.getString("Ice"), Cat.SIS_ICE);
        addWCItem(Messages.getString("LevelGauge"), Cat.SIS_LGAG);
        addWCItem(Messages.getString("Military"), Cat.SIS_MILY);
        warningCatBox.setVisible(false);

        platformCatBox = new JComboBox<>();
        platformCatBox.setBounds(new Rectangle(5, 140, 160, 20));
        add(platformCatBox);
        platformCatBox.addActionListener(alPlatformCatBox);
        addPLItem("", Cat.NOCAT);
        addPLItem(Messages.getString("Oil"), Cat.OFP_OIL);
        addPLItem(Messages.getString("Production"), Cat.OFP_PRD);
        addPLItem(Messages.getString("Observation"), Cat.OFP_OBS);
        addPLItem(Messages.getString("ALP"), Cat.OFP_ALP);
        addPLItem(Messages.getString("SALM"), Cat.OFP_SALM);
        addPLItem(Messages.getString("MooringTower"), Cat.OFP_MOR);
        addPLItem(Messages.getString("ArtificialIsland"), Cat.OFP_ISL);
        addPLItem(Messages.getString("FPSO"), Cat.OFP_FPSO);
        addPLItem(Messages.getString("Accommodation"), Cat.OFP_ACC);
        addPLItem(Messages.getString("NCCB"), Cat.OFP_NCCB);
        platformCatBox.setVisible(false);

        pilotCatBox = new JComboBox<>();
        pilotCatBox.setBounds(new Rectangle(5, 140, 160, 20));
        add(pilotCatBox);
        pilotCatBox.addActionListener(alPilotCatBox);
        addPTItem("", Cat.NOCAT);
        addPTItem(Messages.getString("CruisingVessel"), Cat.PIL_VESS);
        addPTItem(Messages.getString("Helicopter"), Cat.PIL_HELI);
        addPTItem(Messages.getString("FromShore"), Cat.PIL_SHORE);
        pilotCatBox.setVisible(false);

        rescueCatBox = new JComboBox<>();
        rescueCatBox.setBounds(new Rectangle(5, 140, 160, 20));
        add(rescueCatBox);
        rescueCatBox.addActionListener(alRescueCatBox);
        addRSItem("", Cat.NOCAT);
        addRSItem(Messages.getString("Lifeboat"), Cat.RSC_LFB);
        addRSItem(Messages.getString("Rocket"), Cat.RSC_RKT);
        addRSItem(Messages.getString("ShipwreckedRefuge"), Cat.RSC_RSW);
        addRSItem(Messages.getString("IntertidalRefuge"), Cat.RSC_RIT);
        addRSItem(Messages.getString("MooredLifeboat"), Cat.RSC_MLB);
        addRSItem(Messages.getString("Radio"), Cat.RSC_RAD);
        addRSItem(Messages.getString("FirstAid"), Cat.RSC_FAE);
        addRSItem(Messages.getString("Seaplane"), Cat.RSC_SPL);
        addRSItem(Messages.getString("Aircraft"), Cat.RSC_AIR);
        addRSItem(Messages.getString("Tug"), Cat.RSC_TUG);
        rescueCatBox.setVisible(false);

        radioCatBox = new JComboBox<>();
        radioCatBox.setBounds(new Rectangle(5, 140, 160, 20));
        add(radioCatBox);
        radioCatBox.addActionListener(alRadioCatBox);
        addROItem("", Cat.NOCAT);
        addROItem(Messages.getString("CircularBeacon"), Cat.ROS_OMNI);
        addROItem(Messages.getString("DirectionalBeacon"), Cat.ROS_DIRL);
        addROItem(Messages.getString("RotatingBeacon"), Cat.ROS_ROTP);
        addROItem(Messages.getString("ConsolBeacon"), Cat.ROS_CNSL);
        addROItem(Messages.getString("DirectionFinding"), Cat.ROS_RDF);
        addROItem(Messages.getString("QTGService"), Cat.ROS_QTG);
        addROItem(Messages.getString("AeronaticalBeacon"), Cat.ROS_AERO);
        addROItem(Messages.getString("Decca"), Cat.ROS_DECA);
        addROItem(Messages.getString("LoranC"), Cat.ROS_LORN);
        addROItem(Messages.getString("DGPS"), Cat.ROS_DGPS);
        addROItem(Messages.getString("Toran"), Cat.ROS_TORN);
        addROItem(Messages.getString("Omega"), Cat.ROS_OMGA);
        addROItem(Messages.getString("Syledis"), Cat.ROS_SYLD);
        addROItem(Messages.getString("Chiaka"), Cat.ROS_CHKA);
        addROItem(Messages.getString("PublicCommunication"), Cat.ROS_PCOM);
        addROItem(Messages.getString("CommercialBroadcast"), Cat.ROS_COMB);
        addROItem(Messages.getString("Facsimile"), Cat.ROS_FACS);
        addROItem(Messages.getString("TimeSignal"), Cat.ROS_TIME);
        addROItem(Messages.getString("AIS"), Cat.ROS_PAIS);
        addROItem(Messages.getString("S-AIS"), Cat.ROS_SAIS);
        addROItem(Messages.getString("V-AIS"), Cat.ROS_VAIS);
        addROItem(Messages.getString("V-AISNC"), Cat.ROS_VANC);
        addROItem(Messages.getString("V-AISSC"), Cat.ROS_VASC);
        addROItem(Messages.getString("V-AISEC"), Cat.ROS_VAEC);
        addROItem(Messages.getString("V-AISWC"), Cat.ROS_VAWC);
        addROItem(Messages.getString("V-AISPL"), Cat.ROS_VAPL);
        addROItem(Messages.getString("V-AISSL"), Cat.ROS_VASL);
        addROItem(Messages.getString("V-AISID"), Cat.ROS_VAID);
        addROItem(Messages.getString("V-AISSW"), Cat.ROS_VASW);
        addROItem(Messages.getString("V-AISSP"), Cat.ROS_VASP);
        addROItem(Messages.getString("V-AISWK"), Cat.ROS_VAWK);
        radioCatBox.setVisible(false);

        radarCatBox = new JComboBox<>();
        radarCatBox.setBounds(new Rectangle(5, 140, 160, 20));
        add(radarCatBox);
        radarCatBox.addActionListener(alRadarCatBox);
        addRAItem("", Cat.NOCAT);
        addRAItem(Messages.getString("Surveillance"), Cat.RAS_SRV);
        addRAItem(Messages.getString("CoastRadar"), Cat.RAS_CST);
        radarCatBox.setVisible(false);

        chLabel = new JLabel("Ch:", SwingConstants.CENTER);
        chLabel.setBounds(new Rectangle(140, 32, 30, 15));
        add(chLabel);
        chBox = new JTextField();
        chBox.setBounds(new Rectangle(140, 45, 30, 20));
        chBox.setHorizontalAlignment(SwingConstants.CENTER);
        add(chBox);
        chBox.addFocusListener(flCh);
    }

    public void syncPanel() {
        functionLabel.setVisible(false);
        functionBox.setVisible(false);
        categoryLabel.setVisible(false);
        landCatBox.setVisible(false);
        trafficCatBox.setVisible(false);
        warningCatBox.setVisible(false);
        platformCatBox.setVisible(false);
        pilotCatBox.setVisible(false);
        rescueCatBox.setVisible(false);
        radioCatBox.setVisible(false);
        radarCatBox.setVisible(false);
        chLabel.setVisible(false);
        chBox.setVisible(false);
        chBox.setText(SmedAction.panelMain.mark.getChannel());
        if ((SmedAction.panelMain.mark.getObject() == Obj.LNDMRK)
                && ((SmedAction.panelMain.mark.getCategory() != Cat.NOCAT) || (SmedAction.panelMain.mark.getFunc() != Fnc.UNKFNC))) {
            functionLabel.setVisible(true);
            categoryLabel.setVisible(true);
            functionBox.setVisible(true);
            landCatBox.setVisible(true);
            for (Fnc fnc : functions.keySet()) {
                int item = functions.get(fnc);
                if (SmedAction.panelMain.mark.getFunc() == fnc) {
                    functionBox.setSelectedIndex(item);
                }
            }
            for (Cat cat : landCats.keySet()) {
                int item = landCats.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    landCatBox.setSelectedIndex(item);
                }
            }
        } else if (SmedAction.panelMain.mark.getObject() == Obj.SISTAT) {
            categoryLabel.setVisible(true);
            trafficCatBox.setVisible(true);
            for (Cat cat : trafficCats.keySet()) {
                int item = trafficCats.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    trafficCatBox.setSelectedIndex(item);
                }
            }
            chLabel.setVisible(true);
            chBox.setVisible(true);
        } else if (SmedAction.panelMain.mark.getObject() == Obj.SISTAW) {
            categoryLabel.setVisible(true);
            warningCatBox.setVisible(true);
            for (Cat cat : warningCats.keySet()) {
                int item = warningCats.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    warningCatBox.setSelectedIndex(item);
                }
            }
            chLabel.setVisible(true);
            chBox.setVisible(true);
        } else if (SmedAction.panelMain.mark.getObject() == Obj.OFSPLF) {
            categoryLabel.setVisible(true);
            platformCatBox.setVisible(true);
            for (Cat cat : platformCats.keySet()) {
                int item = platformCats.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    platformCatBox.setSelectedIndex(item);
                }
            }
        } else if (SmedAction.panelMain.mark.getObject() == Obj.PILBOP) {
            categoryLabel.setVisible(true);
            pilotCatBox.setVisible(true);
            for (Cat cat : pilotCats.keySet()) {
                int item = pilotCats.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    pilotCatBox.setSelectedIndex(item);
                }
            }
            chLabel.setVisible(true);
            chBox.setVisible(true);
        } else if (SmedAction.panelMain.mark.getObject() == Obj.RSCSTA) {
            categoryLabel.setVisible(true);
            rescueCatBox.setVisible(true);
            for (Cat cat : rescueCats.keySet()) {
                int item = rescueCats.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    rescueCatBox.setSelectedIndex(item);
                }
            }
        } else if (SmedAction.panelMain.mark.getObject() == Obj.RDOSTA) {
            categoryLabel.setVisible(true);
            radioCatBox.setVisible(true);
            for (Cat cat : radioCats.keySet()) {
                int item = radioCats.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    radioCatBox.setSelectedIndex(item);
                }
            }
            chLabel.setVisible(true);
            chBox.setVisible(true);
        } else if (SmedAction.panelMain.mark.getObject() == Obj.RADSTA) {
            categoryLabel.setVisible(true);
            radarCatBox.setVisible(true);
            for (Cat cat : radarCats.keySet()) {
                int item = radarCats.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    radarCatBox.setSelectedIndex(item);
                }
            }
            chLabel.setVisible(true);
            chBox.setVisible(true);
        }
        for (Obj obj : objects.keySet()) {
            JRadioButton button = objects.get(obj);
            button.setBorderPainted(SmedAction.panelMain.mark.getObject() == obj);
        }
        SmedAction.panelMain.mark.testValid();
    }

    private void addLCItem(String str, Cat cat) {
        landCats.put(cat, landCatBox.getItemCount());
        landCatBox.addItem(str);
    }

    private void addTCItem(String str, Cat cat) {
        trafficCats.put(cat, trafficCatBox.getItemCount());
        trafficCatBox.addItem(str);
    }

    private void addWCItem(String str, Cat cat) {
        warningCats.put(cat, warningCatBox.getItemCount());
        warningCatBox.addItem(str);
    }

    private void addPLItem(String str, Cat cat) {
        platformCats.put(cat, platformCatBox.getItemCount());
        platformCatBox.addItem(str);
    }

    private void addPTItem(String str, Cat cat) {
        pilotCats.put(cat, pilotCatBox.getItemCount());
        pilotCatBox.addItem(str);
    }

    private void addRSItem(String str, Cat cat) {
        rescueCats.put(cat, rescueCatBox.getItemCount());
        rescueCatBox.addItem(str);
    }

    private void addROItem(String str, Cat cat) {
        radioCats.put(cat, radioCatBox.getItemCount());
        radioCatBox.addItem(str);
    }

    private void addRAItem(String str, Cat cat) {
        radarCats.put(cat, radarCatBox.getItemCount());
        radarCatBox.addItem(str);
    }

    private void addLFItem(String str, Fnc fnc) {
        functions.put(fnc, functionBox.getItemCount());
        functionBox.addItem(str);
    }

    private JRadioButton getObjButton(JRadioButton button, int x, int y, int w, int h, String tip, Obj obj) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alObj);
        objButtons.add(button);
        objects.put(obj, button);
        return button;
    }

}
