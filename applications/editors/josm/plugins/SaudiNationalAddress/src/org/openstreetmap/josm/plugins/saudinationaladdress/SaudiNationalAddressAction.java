package org.openstreetmap.josm.plugins.saudinationaladdress;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.coor.conversion.DecimalDegreesCoordinateFormat;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

/**
 * Created by tom on 02/08/15. (originaly for AustriaAddressHelper plugin)
 * Mouath Ibrahim 05/03/20
 */
public class SaudiNationalAddressAction extends JosmAction {
    static final String baseUrl = "https://apina.address.gov.sa/NationalAddress/v3.1/Address/address-geocode";

    public SaudiNationalAddressAction() {
        super(tr("Get Address"), new ImageProvider("icon.png"), tr("Get Address"),
                Shortcut.registerShortcut("Get Address", tr("Get Address"),
                        KeyEvent.VK_G, Shortcut.CTRL), true, "getAddress",
                true);
    }

    public static OsmPrimitive loadAddress(OsmPrimitive selectedObject) {
        final String API_KEY = Config.getPref().get(SaudiNationalAddressPreference.API_KEY);

        if (!API_KEY.isEmpty()) {
            LatLon center = selectedObject.getBBox().getCenter();
            // https://apina.address.gov.sa/NationalAddress/v3.1/Address/address-geocode?lat=25.36919&long=49.55076&language=E&format=json&encode=utf8
            URL url = null;
            try {
                url = new URL(baseUrl
                        + "?lat=" + URLEncoder.encode(DecimalDegreesCoordinateFormat.INSTANCE.latToString(center), "UTF-8")
                        + "&long=" + URLEncoder.encode(DecimalDegreesCoordinateFormat.INSTANCE.lonToString(center), "UTF-8")
                        + "&language=A" // A for Arabic, E for English
                        + "&format=json"
                        + "&encode=utf8"
                );
            } catch (MalformedURLException | UnsupportedEncodingException e) {
                notification(e.getMessage(), JOptionPane.ERROR_MESSAGE);
            }

            JsonObject json = null;
            try (BufferedReader in = HttpClient.create(url)
                    .setReasonForRequest("JOSM Plugin Saudi National Address")
                    .setHeader("api_key", API_KEY)
                    .connect()
                    .getContentReader();
                 JsonReader reader = Json.createReader(in)) {
                json = reader.readObject();
            } catch (IOException e) {
                notification(e.getMessage(), JOptionPane.ERROR_MESSAGE);

            }

            // The api is not consistent in what it returns for error messages, hence this smart AI.
            try {
                if (json != null) {
                    if (json.containsKey("statusCode")) {
                        if (!(json.getInt("statusCode") == 200)) {
                            notification(json.getString("message"), JOptionPane.ERROR_MESSAGE); // message: auth, rate limit errors
                        }
                    } else if (json.isNull("Addresses")) {
                        notification(tr("No address was found for this object."), JOptionPane.WARNING_MESSAGE);
                    } else {
                        final JsonArray addressItems = json.getJsonArray("Addresses");
                        final JsonObject firstAddress = addressItems.getJsonObject(0);

                        String country = "SA";
                        //                    String province = firstAddress.getString("RegionName");
                        String district = firstAddress.getString("District");
                        String postcode = firstAddress.getString("PostCode") + "-" + firstAddress.getString("AdditionalNumber");
                        String city = firstAddress.getString("City");
                        String buildingNumber = firstAddress.getString("BuildingNumber");
                        String street = firstAddress.getString("Street");

                        final OsmPrimitive newObject = selectedObject instanceof Node
                                ? new Node(((Node) selectedObject))
                                : selectedObject instanceof Way
                                ? new Way((Way) selectedObject)
                                : selectedObject instanceof Relation
                                ? new Relation((Relation) selectedObject)
                                : null;


                        // seperate arabic and english
                        String[] districtArray = district.split(",");
                        String[] cityArray = city.split(",");

                        //                newObject.put("addr:country", country); // can be determined from boundary relations
                        //                newObject.put("addr:province", province); // can be determined from boundary relations
                        newObject.put("addr:postcode", postcode);
                        newObject.put("addr:city", cityArray[1]);
                        newObject.put("addr:city:en", cityArray[0]);
                        newObject.put("addr:district", districtArray[1]); // Arabic and common version
                        newObject.put("addr:district:en", districtArray[0]);
                        newObject.put("addr:housenumber", buildingNumber);
                        if (!street.isEmpty()) {
                            newObject.put("addr:street", street);
                        } // the api is still missing street names in some areas

                        String msg = tr("Successfully added address to selected object:") + "<br />"
                                + encodeHTML(street) + " "
                                + encodeHTML(buildingNumber) + ", "
                                + encodeHTML(postcode) + " "
                                + encodeHTML(district) + " ("
                                + encodeHTML(country) + ")<br/>";
                        notification(msg, JOptionPane.INFORMATION_MESSAGE);

                        return newObject;
                    }
                }
            } catch (NullPointerException e) {
                notification(tr("Unknown Error: ") + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            notification(tr("Please set your API key in the preference window!"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return null;
    }

    private static String encodeHTML(String s) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>') {
                out.append("&#" + (int) c + ";");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static void notification(String message, int messageType) {
        new Notification("<strong>" + tr("Saudi National Address") + "</strong><br>" + message)
                .setIcon(messageType)
                .setDuration(3000)
                .show();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // Get the currently selected object
        final Collection<OsmPrimitive> sel = MainApplication.getLayerManager().getEditDataSet().getSelected();

        if (sel.size() != 1) {
            notification(tr("Please select exactly one object."), JOptionPane.ERROR_MESSAGE);
            return;
        }

        final List<Command> commands = new ArrayList<>();
        for (OsmPrimitive selectedObject : sel) {
            OsmPrimitive newObject = loadAddress(selectedObject);
            if (newObject != null) {
                commands.add(new ChangeCommand(selectedObject, newObject));
            }
        }
        if (!commands.isEmpty()) {
            UndoRedoHandler.getInstance().add(new SequenceCommand(trn("Get address", "Get addresses", commands.size()), commands));
        }

    }

    @Override
    protected void updateEnabledState() {
        if (getLayerManager().getEditDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getLayerManager().getEditDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(final Collection<? extends OsmPrimitive> selection) {
        // Enable it only if exactly one object is selected.
        setEnabled(selection != null && selection.size() == 1);
    }
}