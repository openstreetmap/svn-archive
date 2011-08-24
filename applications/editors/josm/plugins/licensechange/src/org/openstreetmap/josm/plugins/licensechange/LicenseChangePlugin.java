// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.BufferedWriter;

import javax.swing.JOptionPane;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.visitor.Visitor;
import org.openstreetmap.josm.data.projection.Epsg4326;
import org.openstreetmap.josm.data.projection.Lambert;
import org.openstreetmap.josm.data.projection.Mercator;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 *
 * Plugin that highlights data with unclear re-licensing status
 *
 * @author Frederik Ramm <frederik@remote.org>
 * based on old Validator plugin by Francisco R. Santos and Dirk Stoecker
 */
public class LicenseChangePlugin extends Plugin implements LayerChangeListener 
{

    protected static ProblemLayer problemLayer = null;

    /** The validate action */
    CheckAction checkAction = new CheckAction(this);

    /** The validation dialog */
    LicenseChangeDialog problemDialog;

    /** The list of errors per layer*/
    Map<Layer, List<LicenseProblem>> layerProblems = new HashMap<Layer, List<LicenseProblem>>();

    /** Database of users who have edited something. */
    private final Map<Long, List<User>> nodeUsers = new HashMap<Long, List<User>>();
    private final Map<Long, List<User>> wayUsers = new HashMap<Long, List<User>>();
    private final Map<Long, List<User>> relationUsers = new HashMap<Long, List<User>>();

    public List<User> getUsers(Node n) { return nodeUsers.get(n.getId()); }
    public List<User> getUsers(Way n) { return wayUsers.get(n.getId()); }
    public List<User> getUsers(Relation n) { return relationUsers.get(n.getId()); }

    /**
     * Creates the plugin
     */
    public LicenseChangePlugin(PluginInformation info) 
    {
        super(info);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) 
    {
        if (newFrame != null) 
        {
            problemDialog = new LicenseChangeDialog(this);
            newFrame.addToggleDialog(problemDialog);
            MapView.addLayerChangeListener(this);
        } 
        else
        {
            MapView.removeLayerChangeListener(this);
        }
    }

    public void initializeProblemLayer() 
    {
        if (problemLayer == null) 
        {
            problemLayer = new ProblemLayer(this);
            Main.main.addLayer(problemLayer);
        }
    }

    /* -------------------------------------------------------------------------- */
    /* interface LayerChangeListener                                              */
    /* -------------------------------------------------------------------------- */

    public void activeLayerChange(Layer oldLayer, Layer newLayer) 
    {
        if (newLayer instanceof OsmDataLayer) {
            List<LicenseProblem> errors = layerProblems.get(newLayer);
            problemDialog.tree.setErrorList(errors);
            Main.map.repaint();
        }
    }

    public void layerAdded(Layer newLayer) 
    {
        if (newLayer instanceof OsmDataLayer) {
            layerProblems.put(newLayer, new ArrayList<LicenseProblem>());
        }
    }

    public void layerRemoved(Layer oldLayer) 
    {
        if (oldLayer == problemLayer) {
            problemLayer = null;
            return;
        }
        layerProblems.remove(oldLayer);
        if (Main.map.mapView.getLayersOfType(OsmDataLayer.class).isEmpty()) {
            if (problemLayer != null) {
                Main.map.mapView.removeLayer(problemLayer);
            }
        }
    }

    private class QhsParser extends DefaultHandler 
    {
        List<User> theList = null;

        @Override
        public void startDocument() throws SAXException {
        }

        @Override public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException 
        {
            if ("node".equals(qName) || "way".equals(qName) || "relation".equals(qName)) 
            {
                 Map<Long, List<User>> theMap = ("node".equals(qName)) ? nodeUsers : ("way".equals(qName)) ? wayUsers : relationUsers;
                 // we always overwrite a list that might already exist
                 theMap.put(Long.decode(atts.getValue("id")), theList = new ArrayList<User>());
            }
            else if ("user".equals(qName))
            {
                String v = atts.getValue("version");
                String i = atts.getValue("id");
                String d = atts.getValue("decision");
                User u = User.createOsmUser(Long.parseLong(i), null);
                if ("first".equals(v)) theList.add(0, u); else theList.add(u);
                u.setRelicensingStatus(
                    "undecided".equals(d) ? User.STATUS_UNDECIDED :
                    "auto".equals(d) ? User.STATUS_AUTO_AGREED :
                    "yes".equals(d) ? User.STATUS_AGREED :
                    "no".equals(d) ? User.STATUS_NOT_AGREED :
                    "anonymous".equals(d) ? User.STATUS_ANONYMOUS :
                    User.STATUS_UNKNOWN);
            }
        }
    }

    public void loadDataFromQuickHistoryService(Collection<OsmPrimitive> objectList)
    {
        final StringBuffer nodesToLoad = new StringBuffer();
        final StringBuffer waysToLoad = new StringBuffer();
        final StringBuffer relationsToLoad = new StringBuffer();

        Visitor v = new Visitor() {
            public void visit(Node n) {
                if (!nodeUsers.containsKey(n.getId())) {
                    nodesToLoad.append(",");
                    nodesToLoad.append(n.getId());
                }
            }
            public void visit(Way n) {
                if (!wayUsers.containsKey(n.getId())) {
                    waysToLoad.append(",");
                    waysToLoad.append(n.getId());
                }
            }
            public void visit(Relation n) {
                if (!relationUsers.containsKey(n.getId())) {
                    relationsToLoad.append(",");
                    relationsToLoad.append(n.getId());
                }
            }
            public void visit(Changeset c) {};
        };

        for (OsmPrimitive p : objectList) if (p.getId() > 0) p.visit(v);


        if (nodesToLoad.length()==0 && waysToLoad.length()==0 && relationsToLoad.length()==0) return;

        try {
            URL qhs = new URL("http://wtfe.gryph.de/api/0.6/userlist");
            HttpURLConnection activeConnection = (HttpURLConnection)qhs.openConnection();
            activeConnection.setRequestMethod("POST");
            activeConnection.setDoOutput(true);
            activeConnection.setRequestProperty("Content-type", "text/xml");
            OutputStream out = activeConnection.getOutputStream();
            BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            if (nodesToLoad.length() > 0) 
            {
                bwr.write("&nodes=0");
                bwr.write(nodesToLoad.toString());
            }
            if (waysToLoad.length() > 0) 
            {
                bwr.write("&ways=0");
                bwr.write(waysToLoad.toString());
            }
            if (relationsToLoad.length() > 0) 
            {
                bwr.write("&relations=0");
                bwr.write(relationsToLoad.toString());
            }
            bwr.flush();

            activeConnection.connect();
            System.out.println(activeConnection.getResponseMessage());
            int retCode = activeConnection.getResponseCode();

            InputStream i = null;
            try {
                i = activeConnection.getInputStream();
            } catch (IOException ioe) {
                i = activeConnection.getErrorStream();
            }

            StringBuffer responseBody = new StringBuffer();


            if (i != null) {
                InputSource inputSource = new InputSource(i);
                SAXParserFactory.newInstance().newSAXParser().parse(inputSource, new QhsParser());
            }

            activeConnection.disconnect();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}

