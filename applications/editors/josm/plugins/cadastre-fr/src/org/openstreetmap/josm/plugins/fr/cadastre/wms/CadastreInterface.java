// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.validation.util.Entities;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.HttpClient.Response;
import org.openstreetmap.josm.tools.Logging;

public class CadastreInterface {
    boolean downloadCanceled;
    private Response urlConn;

    private String csrfToken;
    private String cookie;
    private String interfaceRef;
    private String lastWMSLayerName;
    private URL searchFormURL;
    private List<String> listOfCommunes = new ArrayList<>();
    private List<String> listOfTA = new ArrayList<>();
    static class PlanImage {
        String name;
        String ref;
        PlanImage(String name, String ref) {
            this.name = name;
            this.ref = ref;
        }
    }

    private List<PlanImage> listOfFeuilles = new ArrayList<>();
    private long cookieTimestamp;

    static final String BASE_URL = "https://www.cadastre.gouv.fr";
    static final String C_IMAGE_FORMAT = "Cette commune est au format ";
    static final String C_COMMUNE_LIST_START = "<select name=\"codeCommune\"";
    static final String C_COMMUNE_LIST_END = "</select>";
    static final String C_OPTION_LIST_START = "<option value=\"";
    static final String C_OPTION_LIST_END = "</option>";
    static final String C_BBOX_COMMUN_START = "new GeoBox(";
    static final String C_BBOX_COMMUN_END = ")";

    static final String C_INTERFACE_VECTOR = "afficherCarteCommune.do";
    static final String C_INTERFACE_RASTER_TA = "afficherCarteTa.do";
    static final String C_INTERFACE_RASTER_FEUILLE = "afficherCarteFeuille.do?CSRF_TOKEN=";
    static final String C_IMAGE_LINK_START = "<a href=\"#\" class=\"raster\" onClick=\"popup('" + C_INTERFACE_RASTER_FEUILLE;
    static final String C_TA_IMAGE_LINK_START = "<a href=\"#\" class=\"raster\" onClick=\"popup('afficherCarteTa.do?CSRF_TOKEN=";
    static final String C_IMAGE_NAME_START = ">Feuille ";
    static final String C_TA_IMAGE_NAME_START = "Tableau d'assemblage <strong>";

    static final String C_CSRF_TOKEN = "CSRF_TOKEN=";
    static final String C_F = "&amp;f=";

    static final long COOKIE_EXPIRATION = 30 * 60 * 1000L; // 30 minutes expressed in milliseconds

    static final int RETRIES_GET_COOKIE = 10; // 10 times every 3 seconds means 30 seconds trying to get a cookie

    boolean retrieveInterface(WMSLayer wmsLayer) throws DuplicateLayerException, WMSException {
        if (wmsLayer.getName().isEmpty())
            return false;
        boolean isCookieExpired = isCookieExpired();
        if (wmsLayer.getName().equals(lastWMSLayerName) && !isCookieExpired)
            return true;
        if (!wmsLayer.getName().equals(lastWMSLayerName))
            interfaceRef = null;
        // open the session with the French Cadastre web front end
        downloadCanceled = false;
        try {
            if (cookie == null || isCookieExpired) {
                getCookie();
                interfaceRef = null;
            }
            if (cookie == null)
                throw new WMSException(tr("Cannot open a new client session.\nServer in maintenance or temporary overloaded."));
            if (interfaceRef == null) {
                getInterface(wmsLayer);
                this.lastWMSLayerName = wmsLayer.getName();
            }
            openInterface();
        } catch (IOException e) {
            Logging.error(e);
            GuiHelper.runInEDT(() ->
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("Town/city {0} not found or not available\n" +
                            "or action canceled", wmsLayer.getLocation())));
            return false;
        }
        return true;
    }

    /**
     * Returns if a cookie is delivered by WMS, throws exception if WMS is not opening a client session
     * (too many clients or in maintenance)
     * @throws IOException if an I/O error occurs
     */
    private void getCookie() throws IOException {
        boolean success = false;
        int retries = RETRIES_GET_COOKIE;
        try {
            searchFormURL = new URL(BASE_URL + "/scpc/accueil.do");
            while (!success && retries > 0) {
                urlConn = getHttpClient(searchFormURL).setHeader("Connection", "close").connect();
                if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // read the buffer otherwise we sent POST too early
                    urlConn.fetchContent();
                    success = true;
                    // See https://bugs.openjdk.java.net/browse/JDK-8036017
                    // When a cookie handler is setup, "Set-Cookie" header returns empty values
                    CookieHandler cookieHandler = CookieHandler.getDefault();
                    if (cookieHandler != null) {
                        if (handleCookie(cookieHandler.get(searchFormURL.toURI(), new HashMap<String, List<String>>()).get("Cookie").get(0))) {
                            break;
                        }
                    } else {
                        for (Entry<String, List<String>> e : urlConn.getHeaderFields().entrySet()) {
                            if ("Set-Cookie".equals(e.getKey()) && e.getValue() != null && !e.getValue().isEmpty()
                                    && handleCookie(e.getValue().get(0))) {
                                break;
                            }
                        }
                    }
                } else {
                    Logging.warn("Request to home page failed. Http error:"+urlConn.getResponseCode()+". Try again "+retries+" times");
                    CadastrePlugin.safeSleep(3000);
                    retries--;
                }
            }
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IOException("Illegal url.", e);
        }
    }

    private boolean handleCookie(String pCookie) {
        cookie = pCookie;
        if (cookie == null || cookie.isEmpty()) {
            Logging.warn("received empty cookie");
            cookie = null;
        } else {
            int index = cookie.indexOf(';');
            if (index > -1) {
                cookie = cookie.substring(0, index);
            }
            cookieTimestamp = new Date().getTime();
            Logging.info("received cookie=" + cookie + " at " + new Date(cookieTimestamp));
        }
        return cookie != null;
    }

    void resetCookie() {
        lastWMSLayerName = null;
        cookie = null;
    }

    boolean isCookieExpired() {
        long now = new Date().getTime();
        if ((now - cookieTimestamp) > COOKIE_EXPIRATION) {
            Logging.info("cookie received at "+new Date(cookieTimestamp)+" expired (now is "+new Date(now)+")");
            return true;
        }
        return false;
    }

    void resetInterfaceRefIfNewLayer(String newWMSLayerName) {
        if (!newWMSLayerName.equals(lastWMSLayerName)) {
            interfaceRef = null;
            cookie = null; // new since WMS server requires that we come back to the main form
        }
    }

    HttpClient getHttpClient(URL url) {
        return HttpClient.create(url).setHeader("Cookie", cookie);
    }

    HttpClient getHttpClient(URL url, String method) {
        return HttpClient.create(url, method).setHeader("Cookie", cookie);
    }

    private void getInterface(WMSLayer wmsLayer) throws IOException, DuplicateLayerException {
        // first attempt : search for given name without codeCommune
        interfaceRef = postForm(wmsLayer, "");
        // second attempt either from known codeCommune (e.g. from cache) or from ComboBox
        if (interfaceRef == null) {
            if (!wmsLayer.getCodeCommune().isEmpty()) {
                // codeCommune is already known (from previous request or from cache on disk)
                interfaceRef = postForm(wmsLayer, wmsLayer.getCodeCommune());
            } else {
                if (listOfCommunes.size() > 1) {
                    // commune unknown, prompt the list of communes from server and try with codeCommune
                    String selected = selectMunicipalityDialog();
                    if (selected != null) {
                        String newCodeCommune = selected.substring(1, selected.indexOf('>') - 2);
                        String newLocation = selected.substring(selected.indexOf('>') + 1, selected.lastIndexOf(" - "));
                        wmsLayer.setCodeCommune(newCodeCommune);
                        wmsLayer.setLocation(newLocation);
                        Config.getPref().put("cadastrewms.codeCommune", newCodeCommune);
                        Config.getPref().put("cadastrewms.location", newLocation);
                    }
                    checkLayerDuplicates(wmsLayer);
                    interfaceRef = postForm(wmsLayer, wmsLayer.getCodeCommune());
                }
                if (listOfCommunes.size() == 1 && wmsLayer.isRaster()) {
                    // commune known but raster format. Select "Feuille" (non-georeferenced image) from list.
                    int res = selectFeuilleDialog();
                    if (res != -1) {
                        wmsLayer.setCodeCommune(listOfFeuilles.get(res).name);
                        checkLayerDuplicates(wmsLayer);
                        interfaceRef = buildRasterFeuilleInterfaceRef(wmsLayer.getCodeCommune(), csrfToken);
                    }
                }
            }
        }

        if (interfaceRef == null)
            throw new IOException("Town/city " + wmsLayer.getLocation() + " not found.");
    }

    private void openInterface() throws IOException {
        try {
            // finally, open the interface on server side giving access to the wms server
            URL interfaceURL = new URL(BASE_URL + "/scpc/"+interfaceRef);
            urlConn = getHttpClient(interfaceURL).connect();
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Cannot open Cadastre interface. GET response:"+urlConn.getResponseCode());
            }
            // read the buffer otherwise we sent POST too early
            String lines = urlConn.fetchContent();
            if (Logging.isDebugEnabled()) {
                Logging.debug(lines.toString());
            }
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(
                "CadastreGrabber: Illegal url.").initCause(e);
        }
    }

    /**
     * Post the form with the commune name and check the returned answer which is embedded
     * in HTTP XML packets. This function doesn't use an XML parser yet but that would be a good idea
     * for the next releases.
     * Two possibilities :
     * - either the commune name matches and we receive an URL starting with "afficherCarteCommune.do" or
     * - we don't receive a single answer but a list of possible values. This answer looks like:<pre>{@code
     * <select name="codeCommune" class="long erreur" id="codeCommune">
     *   <option value="">Choisir</option>
     *   <option value="50061" >COLMARS - 04370</option>
     *   <option value="QK066" >COLMAR - 68000</option>
     * </select>}</pre>
     * The returned string is the interface name used in further requests, e.g. "afficherCarteCommune.do?c=QP224"
     * where QP224 is the code commune known by the WMS (or "afficherCarteTa.do?c=..." for raster images).
     * @param wmsLayer WMS layer
     * @param codeCommune Commune code
     *
     * @return retURL url to available code commune in the cadastre; "" if not found
     * @throws IOException if an I/O error occurs
     */
    private String postForm(WMSLayer wmsLayer, String codeCommune) throws IOException {
        try {
            listOfCommunes.clear();
            listOfTA.clear();
            // send a POST request with a city/town/village name
            String content = "numerovoie=";
            content += "&indiceRepetition=";
            content += "&nomvoie=";
            content += "&lieuDit=";
            if (codeCommune.isEmpty()) {
                content += "&ville=" + java.net.URLEncoder.encode(wmsLayer.getLocation(), "UTF-8");
                content += "&codePostal=";
            } else {
                content += "&codeCommune=" + codeCommune;
            }
            content += "&codeDepartement=";
            content += wmsLayer.getDepartement();
            content += "&nbResultatParPage=10";
            content += "&x=0&y=0";
            searchFormURL = new URL(BASE_URL + "/scpc/rechercherPlan.do");
            urlConn = getHttpClient(searchFormURL, "POST").setRequestBody(content.getBytes(StandardCharsets.UTF_8)).connect();
            String lines = urlConn.fetchContent();
            urlConn.disconnect();
            if (lines != null) {
                int i = lines.indexOf(C_IMAGE_FORMAT);
                if (i > -1) {
                    wmsLayer.setRaster("image".equals(lines.substring(i+C_IMAGE_FORMAT.length(), lines.indexOf('.', i))));
                }
                csrfToken = null;
                i = lines.indexOf(C_CSRF_TOKEN);
                if (i > -1) {
                    csrfToken = lines.substring(i+C_CSRF_TOKEN.length(), Math.min(lines.indexOf('"', i), lines.indexOf('&', i)));
                }
                i = lines.indexOf(C_INTERFACE_VECTOR);
                if (!wmsLayer.isRaster() && i != -1) {  // "afficherCarteCommune.do"
                    // shall be something like: interfaceRef = "afficherCarteCommune.do?c=X2269";
                    lines = lines.substring(i, lines.length());
                    lines = lines.substring(0, lines.indexOf('\''));
                    lines = Entities.unescape(lines);
                    Logging.info("interface ref.:"+lines);
                    return lines;
                } else if (wmsLayer.isRaster() && lines.indexOf(C_INTERFACE_RASTER_TA) != -1) { // "afficherCarteTa.do"
                    // list of values parsed in listOfFeuilles (list all non-georeferenced images)
                    lines = getFeuillesList(csrfToken);
                    if (!downloadCanceled) {
                        parseFeuillesList(lines, csrfToken);
                        if (!listOfFeuilles.isEmpty()) {
                            int res = selectFeuilleDialog();
                            if (res != -1) {
                                wmsLayer.setCodeCommune(listOfFeuilles.get(res).name);
                                checkLayerDuplicates(wmsLayer);
                                interfaceRef = buildRasterFeuilleInterfaceRef(wmsLayer.getCodeCommune(), csrfToken);
                                wmsLayer.setCodeCommune(listOfFeuilles.get(res).ref);
                                lines = buildRasterFeuilleInterfaceRef(listOfFeuilles.get(res).ref, csrfToken);
                                lines = Entities.unescape(lines);
                                Logging.info("interface ref.:"+lines);
                                return lines;
                            }
                        }
                    }
                    return null;
                } else if (lines.indexOf(C_COMMUNE_LIST_START) != -1 && lines.indexOf(C_COMMUNE_LIST_END) != -1) {
                    // list of values parsed in listOfCommunes
                    i = lines.indexOf(C_COMMUNE_LIST_START);
                    parseCommuneList(lines.substring(i, lines.indexOf(C_COMMUNE_LIST_END, i)));
                }
            }
        } catch (MalformedURLException e) {
            throw (IOException) new IOException("Illegal url.").initCause(e);
        } catch (DuplicateLayerException e) {
            Logging.error(e);
        }
        return null;
    }

    private void parseCommuneList(String input) {
        if (input.indexOf(C_OPTION_LIST_START) != -1) {
            while (input.indexOf("<option value=\"") != -1) {
                int i = input.indexOf(C_OPTION_LIST_START);
                int j = input.indexOf(C_OPTION_LIST_END, i+C_OPTION_LIST_START.length());
                int k = input.indexOf('"', i+C_OPTION_LIST_START.length());
                if (j != -1 && k > (i + C_OPTION_LIST_START.length())) {
                    String lov = input.substring(i+C_OPTION_LIST_START.length()-1, j);
                    if (lov.indexOf('>') != -1) {
                        Logging.info("parse "+lov);
                        listOfCommunes.add(lov);
                    } else
                        Logging.error("unable to parse commune string:"+lov);
                }
                input = input.substring(j+C_OPTION_LIST_END.length());
            }
        }
    }

    private String getFeuillesList(String csrfToken) {
        // get all images in one html page
        try {
            URL getAllImagesURL = new URL(BASE_URL + "/scpc/listerFeuillesParcommune.do?CSRF_TOKEN=" +
                    csrfToken + "&keepVolatileSession=&offset=2000");
            return getHttpClient(getAllImagesURL).connect().fetchContent();
        } catch (IOException e) {
            listOfFeuilles.clear();
            Logging.error(e);
        }
        return "";
    }

    private void parseFeuillesList(String input, String csrfToken) {
        listOfFeuilles.clear();
        // get "Tableau d'assemblage"
        String inputTA = input;
        if (Config.getPref().getBoolean("cadastrewms.useTA", false)) {
            while (inputTA.indexOf(C_TA_IMAGE_LINK_START) != -1) {
                inputTA = inputTA.substring(inputTA.indexOf(C_TA_IMAGE_LINK_START) + C_TA_IMAGE_LINK_START.length()
                    + csrfToken.length() + C_F.length());
                String refTA = inputTA.substring(0, inputTA.indexOf('\''));
                String nameTA = inputTA.substring(inputTA.indexOf(C_TA_IMAGE_NAME_START) + C_TA_IMAGE_NAME_START.length());
                nameTA = nameTA.substring(0, nameTA.indexOf('<'));
                listOfFeuilles.add(new PlanImage(nameTA, refTA));
            }
        }
        // get "Feuilles"
        while (input.indexOf(C_IMAGE_LINK_START) != -1) {
            input = input.substring(input.indexOf(C_IMAGE_LINK_START)+C_IMAGE_LINK_START.length()
                + csrfToken.length() + C_F.length());
            String refFeuille = input.substring(0, input.indexOf('\''));
            String nameFeuille = input.substring(
                    input.indexOf(C_IMAGE_NAME_START)+C_IMAGE_NAME_START.length(),
                    input.indexOf(" -"));
            listOfFeuilles.add(new PlanImage(nameFeuille, refFeuille));
        }
    }

    private String selectMunicipalityDialog() {
        return GuiHelper.runInEDTAndWaitAndReturn(() -> {
            JPanel p = new JPanel(new GridBagLayout());
            String[] communeList = new String[listOfCommunes.size() + 1];
            communeList[0] = tr("Choose from...");
            for (int i = 0; i < listOfCommunes.size(); i++) {
                communeList[i + 1] = listOfCommunes.get(i).substring(listOfCommunes.get(i).indexOf('>')+1);
            }
            JComboBox<String> inputCommuneList = new JComboBox<>(communeList);
            p.add(inputCommuneList, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 0, 0, 0));
            JOptionPane pane = new JOptionPane(p, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
            // this below is a temporary workaround to fix the "always on top" issue
            JDialog dialog = pane.createDialog(MainApplication.getMainFrame(), tr("Select commune"));
            CadastrePlugin.prepareDialog(dialog);
            dialog.setVisible(true);
            // till here
            if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue()))
                return null;
            return listOfCommunes.get(inputCommuneList.getSelectedIndex()-1);
        });
    }

    private int selectFeuilleDialog() {
        return GuiHelper.runInEDTAndWaitAndReturn(() -> {
            JPanel p = new JPanel(new GridBagLayout());
            List<String> imageNames = new ArrayList<>();
            for (PlanImage src : listOfFeuilles) {
                imageNames.add(src.name);
            }
            JComboBox<String> inputFeuilleList = new JComboBox<>(imageNames.toArray(new String[]{}));
            p.add(inputFeuilleList, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 0, 0, 0));
            JOptionPane pane = new JOptionPane(p, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
            // this below is a temporary workaround to fix the "always on top" issue
            JDialog dialog = pane.createDialog(MainApplication.getMainFrame(), tr("Select Feuille"));
            CadastrePlugin.prepareDialog(dialog);
            dialog.setVisible(true);
            // till here
            if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue()))
                return -1;
            return inputFeuilleList.getSelectedIndex();
        });
    }

    private static String buildRasterFeuilleInterfaceRef(String codeCommune, String csrfToken) {
        return C_INTERFACE_RASTER_FEUILLE + csrfToken + "&f=" + codeCommune;
    }

    /**
     * Retrieve the bounding box size in pixels of the whole commune (point 0,0 at top, left corner)
     * and store it in given wmsLayer
     * In case of raster image, we also check in the same http request if the image is already georeferenced
     * and store the result in the wmsLayer as well.
     * @param wmsLayer the WMSLayer where the commune data and images are stored
     * @throws IOException if any I/O error occurs
     */
    void retrieveCommuneBBox(WMSLayer wmsLayer) throws IOException {
        if (interfaceRef == null)
            return;
        // send GET opening normally the small window with the commune overview
        searchFormURL = new URL(BASE_URL + "/scpc/" + interfaceRef + "&dontSaveLastForward&keepVolatileSession=");
        String line = getHttpClient(searchFormURL).connect().fetchContent();
        parseBBoxCommune(wmsLayer, line);
        if (wmsLayer.isRaster() && !wmsLayer.isAlreadyGeoreferenced()) {
            parseGeoreferences(wmsLayer, line);
        }
    }

    private static void parseBBoxCommune(WMSLayer wmsLayer, String input) {
        if (input.indexOf(C_BBOX_COMMUN_START) != -1) {
            input = input.substring(input.indexOf(C_BBOX_COMMUN_START));
            int i = input.indexOf(',');
            double minx = Double.parseDouble(input.substring(C_BBOX_COMMUN_START.length(), i));
            int j = input.indexOf(',', i+1);
            double miny = Double.parseDouble(input.substring(i+1, j));
            int k = input.indexOf(',', j+1);
            double maxx = Double.parseDouble(input.substring(j+1, k));
            int l = input.indexOf(C_BBOX_COMMUN_END, k+1);
            double maxy = Double.parseDouble(input.substring(k+1, l));
            wmsLayer.setCommuneBBox(new EastNorthBound(new EastNorth(minx, miny), new EastNorth(maxx, maxy)));
        }
    }

    private static void parseGeoreferences(WMSLayer wmsLayer, String input) {
        /* commented since cadastre WMS changes mid july 2013
         * until new GeoBox coordinates parsing is solved */
//        if (input.lastIndexOf(cBBoxCommunStart) != -1) {
//            input = input.substring(input.lastIndexOf(cBBoxCommunStart));
//            input = input.substring(input.indexOf(cBBoxCommunEnd)+cBBoxCommunEnd.length());
//            int i = input.indexOf(",");
//            int j = input.indexOf(",", i+1);
//            String str = input.substring(i+1, j);
//            double unknown_yet = tryParseDouble(str);
//            int j_ = input.indexOf(",", j+1);
//            double angle = Double.parseDouble(input.substring(j+1, j_));
//            int k = input.indexOf(",", j_+1);
//            double scale_origin = Double.parseDouble(input.substring(j_+1, k));
//            int l = input.indexOf(",", k+1);
//            double dpi = Double.parseDouble(input.substring(k+1, l));
//            int m = input.indexOf(",", l+1);
//            double fX = Double.parseDouble(input.substring(l+1, m));
//            int n = input.indexOf(",", m+1);
//            double fY = Double.parseDouble(input.substring(m+1, n));
//            int o = input.indexOf(",", n+1);
//            double X0 = Double.parseDouble(input.substring(n+1, o));
//            int p = input.indexOf(",", o+1);
//            double Y0 = Double.parseDouble(input.substring(o+1, p));
//            if (X0 != 0.0 && Y0 != 0) {
//                wmsLayer.setAlreadyGeoreferenced(true);
//                wmsLayer.fX = fX;
//                wmsLayer.fY = fY;
//                wmsLayer.angle = angle;
//                wmsLayer.X0 = X0;
//                wmsLayer.Y0 = Y0;
//            }
//            Main.info("parse georef:"+unknown_yet+","+angle+","+scale_origin+","+dpi+","+fX+","+fY+","+X0+","+Y0);
//        }
    }

    private static void checkLayerDuplicates(WMSLayer wmsLayer) throws DuplicateLayerException {
        if (MainApplication.getMap() != null) {
            for (Layer l : MainApplication.getLayerManager().getLayers()) {
                if (l instanceof WMSLayer && l.getName().equals(wmsLayer.getName()) && !l.equals(wmsLayer)) {
                    Logging.info("Try to grab into a new layer when "+wmsLayer.getName()+" is already opened.");
                    // remove the duplicated layer
                    MainApplication.getLayerManager().removeLayer(wmsLayer);
                    throw new DuplicateLayerException();
                }
            }
        }
    }

    void cancel() {
        if (urlConn != null) {
            urlConn.disconnect();
        }
        downloadCanceled = true;
        lastWMSLayerName = null;
    }

    InputStream getContent(URL url) throws IOException, OsmTransferException {
        urlConn = getHttpClient(url).setHeader("Connection", "close").connect();
        return urlConn.getContent();
    }
}
