// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.XmlObjectParser;
import org.xml.sax.SAXException;

import livegps.LiveGpsPlugin;

/**
 * @author cdaller
 *
 */
public class SurveyorShowAction extends JosmAction {
    private static final long serialVersionUID = 2184570223633094734L;
    private static final String DEFAULT_SOURCE = "resource://resources/surveyor.xml";
    private JFrame surveyorFrame;
    private LiveGpsPlugin gpsPlugin;

    public SurveyorShowAction(LiveGpsPlugin gpsPlugin) {
        super(tr("Surveyor..."), "surveyormenu.png", tr("Open surveyor tool."),
        Shortcut.registerShortcut("surveyor:open", tr("Tool: {0}", tr("Surveyor...")),
        KeyEvent.VK_R, Shortcut.CTRL_SHIFT), true);
        this.gpsPlugin = gpsPlugin;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (surveyorFrame == null) {
            surveyorFrame = new JFrame();

            SurveyorComponent comp = createComponent();

            // add component as gps event listener:
            gpsPlugin.addPropertyChangeListener(comp);

            // add some hotkeys to the component:
            ActionMap actionMap = comp.getActionMap();
            InputMap inputMap = comp.getInputMap();
            // zoomout:
            actionMap.put("zoomout", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (MainApplication.getMap() != null && MainApplication.getMap().mapView != null) {
                        MainApplication.getMap().mapView.zoomToFactor(2);
                    }
                }
            });
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "zoomout");
            // zoomin:
            actionMap.put("zoomin", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (MainApplication.getMap() != null && MainApplication.getMap().mapView != null) {
                        MainApplication.getMap().mapView.zoomToFactor(1/2);
                    }
                }
            });
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "zoomin");
            // autocenter:
            actionMap.put("autocenter", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // toggle autocenter
                    gpsPlugin.setAutoCenter(!gpsPlugin.isAutoCenter());
                }
            });
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "autocenter");

            surveyorFrame.add(comp);
            surveyorFrame.pack();
            surveyorFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            //surveyorFrame.setTitle((String)getValue(AbstractAction.NAME));
            surveyorFrame.setTitle(tr("Surveyor"));
            // <FIXXME date="28.04.2007" author="cdaller">
            // TODO get old pos of frame from properties
            // </FIXXME>
            SurveyorPlugin.setSurveyorFrame(surveyorFrame);
        }
        surveyorFrame.setAlwaysOnTop(true);
        surveyorFrame.setVisible(true);

    }

    public static SurveyorComponent createComponent() {
        String source = Config.getPref().get("surveyor.source");
        if (source == null || source.isEmpty()) {
            source = DEFAULT_SOURCE;
            Config.getPref().put("surveyor.source", DEFAULT_SOURCE);
        }
        try (CachedFile cf = new CachedFile(source); InputStream in = cf.getInputStream()) {
            return createComponent(in);
        } catch (IOException e) {
            Logging.error(e);
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Could not read surveyor definition: {0}", source));
        } catch (SAXException e) {
            Logging.error(e);
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Error parsing {0}: {1}", source, e.getMessage()));
        }
        return null;
    }

    /**
     * Parse an xml file containing the definitions for the surveyor component.
     * @param in the inputstream to read the xml from.
     * @return the component.
     * @throws SAXException if the xml could not be read.
     */
    public static SurveyorComponent createComponent(InputStream in) throws SAXException {
        XmlObjectParser parser = new XmlObjectParser();
        parser.mapOnStart("surveyor", SurveyorComponent.class);
        parser.map("button", ButtonDescription.class);
        parser.map("action", SurveyorActionDescription.class);

        SurveyorComponent surveyorComponent = null;
        parser.start(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
        List<SurveyorActionDescription> actions = new ArrayList<>();
        while (parser.hasNext()) {
            Object object = parser.next();
            if (object instanceof SurveyorComponent) {
                surveyorComponent = (SurveyorComponent) object;
            } else if (object instanceof ButtonDescription) {
                ((ButtonDescription) object).setActions(actions);
                surveyorComponent.addButton(((ButtonDescription) object));
                actions = new ArrayList<>();
            } else if (object instanceof SurveyorActionDescription) {
                actions.add((SurveyorActionDescription) object);
            } else {
                Logging.error("surveyor: unknown xml element: " + object);
            }
        }
        return surveyorComponent;
    }
}
