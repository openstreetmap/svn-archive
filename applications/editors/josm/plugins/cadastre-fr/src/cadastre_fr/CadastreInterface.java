// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.GBC;

public class CadastreInterface {
    public boolean downloadCanceled = false;
    public HttpURLConnection urlConn = null;

    private String cookie;
    private String interfaceRef = null;
    private String lastWMSLayerName = null;
    private URL searchFormURL;
    private Vector<String> listOfCommunes = new Vector<String>();
    private Vector<String> listOfTA = new Vector<String>();
    class PlanImage {
        String name;
        String ref;
        PlanImage(String name, String ref) {
            this.name = name;
            this.ref = ref;
        }
    }
    private Vector<PlanImage> listOfFeuilles = new Vector<PlanImage>();
    private long cookieTimestamp;

    final String baseURL = "http://www.cadastre.gouv.fr";
    final String cImageFormat = "Cette commune est au format ";
    final String cCommuneListStart = "<select name=\"codeCommune\"";
    final String cCommuneListEnd = "</select>";
    final String c0ptionListStart = "<option value=\"";
    final String cOptionListEnd = "</option>";
    final String cBBoxCommunStart = "new GeoBox(";
    final String cBBoxCommunEnd = ")";

    final String cInterfaceVector = "afficherCarteCommune.do";
    final String cInterfaceRasterTA = "afficherCarteTa.do";
    final String cInterfaceRasterFeuille = "afficherCarteFeuille.do";
    final String cImageLinkStart = "<a href=\"#\" class=\"raster\" onClick=\"popup('afficherCarteFeuille.do?f=";
    final String cTAImageLinkStart = "<a href=\"#\" class=\"raster\" onClick=\"popup('afficherCarteTa.do?f=";
    final String cImageNameStart = ">Feuille ";
    final String cTAImageNameStart = "Tableau d'assemblage <strong>";

    final static long cCookieExpiration = 30 * 60 * 1000; // 30 minutes expressed in milliseconds

    final  int cRetriesGetCookie = 10; // 10 times every 3 seconds means 30 seconds trying to get a cookie

    public boolean retrieveInterface(WMSLayer wmsLayer) throws DuplicateLayerException, WMSException {
        if (wmsLayer.getName().equals(""))
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
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Town/city {0} not found or not available\n" +
                            "or action canceled", wmsLayer.getLocation()));
            return false;
        }
        return true;
    }

    /**
     *
     * @return true if a cookie is delivered by WMS and false is WMS is not opening a client session
     *         (too many clients or in maintenance)
     * @throws IOException
     */
    private void getCookie() throws IOException {
        boolean cookied = false;
        int retries = cRetriesGetCookie;
        try {
            searchFormURL = new URL(baseURL + "/scpc/accueil.do");
            while (cookied == false && retries > 0) {
                urlConn = (HttpURLConnection)searchFormURL.openConnection();
                urlConn.setRequestMethod("GET");
                urlConn.connect();
                if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    System.out.println("GET "+searchFormURL);
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while(in.readLine() != null) {}  // read the buffer otherwise we sent POST too early
                    String headerName=null;
                    for (int i=1; (headerName = urlConn.getHeaderFieldKey(i))!=null; i++) {
                        if (headerName.equals("Set-Cookie")) {
                            cookie = urlConn.getHeaderField(i);
                            cookie = cookie.substring(0, cookie.indexOf(";"));
                            cookieTimestamp = new Date().getTime();
                            System.out.println("received cookie=" + cookie + " at " + new Date(cookieTimestamp));
                            cookied = true;
                        }
                    }
                } else {
                    System.out.println("Request to home page failed. Http error:"+urlConn.getResponseCode()+". Try again "+retries+" times");
                    CadastrePlugin.safeSleep(3000);
                    retries --;
                }
            }
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(
                "Illegal url.").initCause(e);
        }
    }

    public void resetCookie() {
        lastWMSLayerName = null;
        cookie = null;
    }

    public boolean isCookieExpired() {
        long now = new Date().getTime();
        if ((now - cookieTimestamp) > cCookieExpiration) {
            System.out.println("cookie received at "+new Date(cookieTimestamp)+" expired (now is "+new Date(now)+")");
            return true;
        }
        return false;
    }

    public void resetInterfaceRefIfNewLayer(String newWMSLayerName) {
        if (!newWMSLayerName.equals(lastWMSLayerName)) {
            interfaceRef = null;
            cookie = null; // new since WMS server requires that we come back to the main form
        }
    }

    public void setCookie() {
        this.urlConn.setRequestProperty("Cookie", this.cookie);
    }

    public void setCookie(HttpURLConnection urlConn) {
        urlConn.setRequestProperty("Cookie", this.cookie);
    }

    private void getInterface(WMSLayer wmsLayer) throws IOException, DuplicateLayerException {
        // first attempt : search for given name without codeCommune
        interfaceRef = postForm(wmsLayer, "");
        // second attempt either from known codeCommune (e.g. from cache) or from ComboBox
        if (interfaceRef == null) {
            if (!wmsLayer.getCodeCommune().equals("")) {
                // codeCommune is already known (from previous request or from cache on disk)
                interfaceRef = postForm(wmsLayer, wmsLayer.getCodeCommune());
            } else {
                if (listOfCommunes.size() > 1) {
                    // commune unknown, prompt the list of communes from
                    // server and try with codeCommune
                    String selected = selectMunicipalityDialog(wmsLayer);
                    if (selected != null) {
                        String newCodeCommune = selected.substring(1, selected.indexOf(">")-2);
                        String newLocation = selected.substring(selected.indexOf(">")+1, selected.lastIndexOf(" - "));
                        wmsLayer.setCodeCommune(newCodeCommune);
                        wmsLayer.setLocation(newLocation);
                        Main.pref.put("cadastrewms.codeCommune", newCodeCommune);
                        Main.pref.put("cadastrewms.location", newLocation);
                    }
                    checkLayerDuplicates(wmsLayer);
                    interfaceRef = postForm(wmsLayer, wmsLayer.getCodeCommune());
                }
                if (listOfCommunes.size() == 1 && wmsLayer.isRaster()) {
                    // commune known but raster format. Select "Feuille" (non-georeferenced image) from list.
                    int res = selectFeuilleDialog();
                    if (res != -1) {
                        wmsLayer.setCodeCommune(listOfFeuilles.elementAt(res).name);
                        checkLayerDuplicates(wmsLayer);
                        interfaceRef = buildRasterFeuilleInterfaceRef(wmsLayer.getCodeCommune());
                    }
                }
            }
        }

        if (interfaceRef == null)
            throw new IOException("Town/city " + wmsLayer.getLocation() + " not found.");
    }

    private void openInterface() throws IOException  {
        try {
            // finally, open the interface on server side giving access to the wms server
            String lines = null;
            String ln = null;
            URL interfaceURL = new URL(baseURL + "/scpc/"+interfaceRef);
            urlConn = (HttpURLConnection)interfaceURL.openConnection();
            urlConn.setRequestMethod("GET");
            setCookie();
            urlConn.connect();
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Cannot open Cadastre interface. GET response:"+urlConn.getResponseCode());
            }
            System.out.println("GET "+interfaceURL);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            // read the buffer otherwise we sent POST too early
            while ((ln = in.readLine()) != null) {
                lines += ln;
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
     * - we don't receive a single answer but a list of possible values. This answer looks like:
     *   <select name="codeCommune" class="long erreur" id="codeCommune">
     *   <option value="">Choisir</option>
     *   <option value="50061" >COLMARS - 04370</option>
     *   <option value="QK066" >COLMAR - 68000</option>
     *   </select>
     * The returned string is the interface name used in further requests, e.g. "afficherCarteCommune.do?c=QP224"
     * where QP224 is the code commune known by the WMS (or "afficherCarteTa.do?c=..." for raster images).
     *
     * @param location
     * @param codeCommune
     * @return retURL url to available code commune in the cadastre; "" if not found
     * @throws IOException
     */
    private String postForm(WMSLayer wmsLayer, String codeCommune) throws IOException {
        try {
            String ln = null;
            String lines = null;
            listOfCommunes.clear();
            listOfTA.clear();
            // send a POST request with a city/town/village name
            String content = "numerovoie=";
            content += "&indiceRepetition=";
            content += "&nomvoie=";
            content += "&lieuDit=";
            if (codeCommune == "") {
                content += "&ville=" + new String(java.net.URLEncoder.encode(wmsLayer.getLocation(), "UTF-8"));
                content += "&codePostal=";
            } else {
                content += "&codeCommune=" + codeCommune;
            }
            content += "&codeDepartement=";
            content += wmsLayer.getDepartement();
            content += "&nbResultatParPage=10";
            content += "&x=0&y=0";
            searchFormURL = new URL(baseURL + "/scpc/rechercherPlan.do");
            urlConn = (HttpURLConnection)searchFormURL.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            setCookie();
            OutputStream wr = urlConn.getOutputStream();
            wr.write(content.getBytes());
            System.out.println("POST "+content);
            wr.flush();
            wr.close();
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((ln = rd.readLine()) != null) {
                lines += ln;
            }
            rd.close();
            urlConn.disconnect();
            if (lines != null) {
                if (lines.indexOf(cImageFormat) != -1) {
                    int i = lines.indexOf(cImageFormat);
                    int j = lines.indexOf(".", i);
                    wmsLayer.setRaster(lines.substring(i+cImageFormat.length(), j).equals("image"));
                }
                if (!wmsLayer.isRaster() && lines.indexOf(cInterfaceVector) != -1) {  // "afficherCarteCommune.do"
                    // shall be something like: interfaceRef = "afficherCarteCommune.do?c=X2269";
                    lines = lines.substring(lines.indexOf(cInterfaceVector),lines.length());
                    lines = lines.substring(0, lines.indexOf("'"));
                    System.out.println("interface ref.:"+lines);
                    return lines;
                } else if (wmsLayer.isRaster() && lines.indexOf(cInterfaceRasterTA) != -1) { // "afficherCarteTa.do"
                    // list of values parsed in listOfFeuilles (list all non-georeferenced images)
                    lines = getFeuillesList();
                    if (!downloadCanceled) {
                        parseFeuillesList(lines);
                        if (listOfFeuilles.size() > 0) {
                            int res = selectFeuilleDialog();
                            if (res != -1) {
                                wmsLayer.setCodeCommune(listOfFeuilles.elementAt(res).name);
                                checkLayerDuplicates(wmsLayer);
                                interfaceRef = buildRasterFeuilleInterfaceRef(wmsLayer.getCodeCommune());
                                wmsLayer.setCodeCommune(listOfFeuilles.elementAt(res).ref);
                                lines = buildRasterFeuilleInterfaceRef(listOfFeuilles.elementAt(res).ref);
                                System.out.println("interface ref.:"+lines);
                                return lines;
                            }
                        }
                    }
                    return null;
                } else if (lines.indexOf(cCommuneListStart) != -1 && lines.indexOf(cCommuneListEnd) != -1) {
                    // list of values parsed in listOfCommunes
                    int i = lines.indexOf(cCommuneListStart);
                    int j = lines.indexOf(cCommuneListEnd, i);
                    parseCommuneList(lines.substring(i, j));
                }
            }
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(
                "Illegal url.").initCause(e);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void parseCommuneList(String input) {
        if (input.indexOf(c0ptionListStart) != -1) {
            while (input.indexOf("<option value=\"") != -1) {
                int i = input.indexOf(c0ptionListStart);
                int j = input.indexOf(cOptionListEnd, i+c0ptionListStart.length());
                int k = input.indexOf("\"", i+c0ptionListStart.length());
                if (j != -1 && k > (i + c0ptionListStart.length())) {
                    String lov = new String(input.substring(i+c0ptionListStart.length()-1, j));
                    if (lov.indexOf(">") != -1) {
                        System.out.println("parse "+lov);
                        listOfCommunes.add(lov);
                    } else
                        System.err.println("unable to parse commune string:"+lov);
                }
                input = input.substring(j+cOptionListEnd.length());
            }
        }
    }

    private String getFeuillesList() {
        // get all images in one html page
        String ln = null;
        String lines = null;
        HttpURLConnection urlConn2 = null;
        try {
            URL getAllImagesURL = new URL(baseURL + "/scpc/listerFeuillesParcommune.do?keepVolatileSession=&offset=2000");
            urlConn2 = (HttpURLConnection)getAllImagesURL.openConnection();
            setCookie(urlConn2);
            urlConn2.connect();
            System.out.println("GET "+getAllImagesURL);
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConn2.getInputStream()));
            while ((ln = rd.readLine()) != null) {
                lines += ln;
            }
            rd.close();
            urlConn2.disconnect();
            //System.out.println("GET="+lines);
        } catch (IOException e) {
            listOfFeuilles.clear();
            e.printStackTrace();
        }
        return lines;
    }

    private void parseFeuillesList(String input) {
        listOfFeuilles.clear();
        // get "Tableau d'assemblage"
        String inputTA = input;
        if (Main.pref.getBoolean("cadastrewms.useTA", false)) {
            while (inputTA.indexOf(cTAImageLinkStart) != -1) {
                inputTA = inputTA.substring(inputTA.indexOf(cTAImageLinkStart) + cTAImageLinkStart.length());
                String refTA = inputTA.substring(0, inputTA.indexOf("'"));
                String nameTA = inputTA.substring(inputTA.indexOf(cTAImageNameStart) + cTAImageNameStart.length());
                nameTA = nameTA.substring(0, nameTA.indexOf("<"));
                listOfFeuilles.add(new PlanImage(nameTA, refTA));
            }
        }
        // get "Feuilles"
        while (input.indexOf(cImageLinkStart) != -1) {
            input = input.substring(input.indexOf(cImageLinkStart)+cImageLinkStart.length());
            String refFeuille = input.substring(0, input.indexOf("'"));
            String nameFeuille = input.substring(
                    input.indexOf(cImageNameStart)+cImageNameStart.length(),
                    input.indexOf(" -"));
            listOfFeuilles.add(new PlanImage(nameFeuille, refFeuille));
        }
    }

    private String selectMunicipalityDialog(WMSLayer wmsLayer) {
        JPanel p = new JPanel(new GridBagLayout());
        String[] communeList = new String[listOfCommunes.size() + 1];
        communeList[0] = tr("Choose from...");
        for (int i = 0; i < listOfCommunes.size(); i++) {
            communeList[i + 1] = listOfCommunes.elementAt(i).substring(listOfCommunes.elementAt(i).indexOf(">")+1);
        }
        JComboBox inputCommuneList = new JComboBox(communeList);
        p.add(inputCommuneList, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 0, 0, 0));
        JOptionPane pane = new JOptionPane(p, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null) {
            private static final long serialVersionUID = 1L;
        };
        //pane.createDialog(Main.parent, tr("Select commune")).setVisible(true);
        // this below is a temporary workaround to fix the "always on top" issue
        JDialog dialog = pane.createDialog(Main.parent, tr("Select commune"));
        CadastrePlugin.prepareDialog(dialog);
        dialog.setVisible(true);
        // till here
        if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue()))
            return null;
        return listOfCommunes.elementAt(inputCommuneList.getSelectedIndex()-1);
    }

    private int selectFeuilleDialog() {
        JPanel p = new JPanel(new GridBagLayout());
        Vector<String> ImageNames = new Vector<String>();
        for (PlanImage src : listOfFeuilles) {
            ImageNames.add(src.name);
        }
        JComboBox inputFeuilleList = new JComboBox(ImageNames);
        p.add(inputFeuilleList, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 0, 0, 0));
        JOptionPane pane = new JOptionPane(p, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
        //pane.createDialog(Main.parent, tr("Select Feuille")).setVisible(true);
        // this below is a temporary workaround to fix the "always on top" issue
        JDialog dialog = pane.createDialog(Main.parent, tr("Select Feuille"));
        CadastrePlugin.prepareDialog(dialog);
        dialog.setVisible(true);
        // till here
        if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue()))
            return -1;
        int result = inputFeuilleList.getSelectedIndex();
        return result;
    }

    private String buildRasterFeuilleInterfaceRef(String codeCommune) {
        return cInterfaceRasterFeuille + "?f=" + codeCommune;
    }

    /**
     * Retrieve the bounding box size in pixels of the whole commune (point 0,0 at top, left corner)
     * and store it in given wmsLayer
     * In case of raster image, we also check in the same http request if the image is already georeferenced
     * and store the result in the wmsLayer as well.
     * @param wmsLayer the WMSLayer where the commune data and images are stored
     * @throws IOException
     */
    public void retrieveCommuneBBox(WMSLayer wmsLayer) throws IOException {
        if (interfaceRef == null)
            return;
        String ln = null;
        String line = null;
        // send GET opening normally the small window with the commune overview
        String content = baseURL + "/scpc/" + interfaceRef;
        content += "&dontSaveLastForward&keepVolatileSession=";
        searchFormURL = new URL(content);
        urlConn = (HttpURLConnection)searchFormURL.openConnection();
        urlConn.setRequestMethod("GET");
        setCookie();
        urlConn.connect();
        if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Cannot get Cadastre response.");
        }
        System.out.println("GET "+searchFormURL);
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        while ((ln = in.readLine()) != null) {
            line += ln;
        }
        in.close();
        urlConn.disconnect();
        parseBBoxCommune(wmsLayer, line);
        if (wmsLayer.isRaster() && !wmsLayer.isAlreadyGeoreferenced()) {
            parseGeoreferences(wmsLayer, line);
        }
    }

    private void parseBBoxCommune(WMSLayer wmsLayer, String input) {
        if (input.indexOf(cBBoxCommunStart) != -1) {
            input = input.substring(input.indexOf(cBBoxCommunStart));
            int i = input.indexOf(",");
            double minx = Double.parseDouble(input.substring(cBBoxCommunStart.length(), i));
            int j = input.indexOf(",", i+1);
            double miny = Double.parseDouble(input.substring(i+1, j));
            int k = input.indexOf(",", j+1);
            double maxx = Double.parseDouble(input.substring(j+1, k));
            int l = input.indexOf(cBBoxCommunEnd, k+1);
            double maxy = Double.parseDouble(input.substring(k+1, l));
            wmsLayer.setCommuneBBox( new EastNorthBound(new EastNorth(minx,miny), new EastNorth(maxx,maxy)));
        }
    }

    private void parseGeoreferences(WMSLayer wmsLayer, String input) {
        if (input.lastIndexOf(cBBoxCommunStart) != -1) {
            input = input.substring(input.lastIndexOf(cBBoxCommunStart));
            input = input.substring(input.indexOf(cBBoxCommunEnd)+cBBoxCommunEnd.length());
            int i = input.indexOf(",");
            int j = input.indexOf(",", i+1);
            String str = input.substring(i+1, j);
            double unknown_yet = tryParseDouble(str);
            int j_ = input.indexOf(",", j+1);
            double angle = Double.parseDouble(input.substring(j+1, j_));
            int k = input.indexOf(",", j_+1);
            double scale_origin = Double.parseDouble(input.substring(j_+1, k));
            int l = input.indexOf(",", k+1);
            double dpi = Double.parseDouble(input.substring(k+1, l));
            int m = input.indexOf(",", l+1);
            double fX = Double.parseDouble(input.substring(l+1, m));
            int n = input.indexOf(",", m+1);
            double fY = Double.parseDouble(input.substring(m+1, n));
            int o = input.indexOf(",", n+1);
            double X0 = Double.parseDouble(input.substring(n+1, o));
            int p = input.indexOf(",", o+1);
            double Y0 = Double.parseDouble(input.substring(o+1, p));
            if (X0 != 0.0 && Y0 != 0) {
                wmsLayer.setAlreadyGeoreferenced(true);
                wmsLayer.fX = fX;
                wmsLayer.fY = fY;
                wmsLayer.angle = angle;
                wmsLayer.X0 = X0;
                wmsLayer.Y0 = Y0;
            }
            System.out.println("parse georef:"+unknown_yet+","+angle+","+scale_origin+","+dpi+","+fX+","+
                    fY+","+X0+","+Y0);
        }
    }

    private double tryParseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void checkLayerDuplicates(WMSLayer wmsLayer) throws DuplicateLayerException {
        if (Main.map != null) {
            for (Layer l : Main.map.mapView.getAllLayers()) {
                if (l instanceof WMSLayer && l.getName().equals(wmsLayer.getName()) && (l != wmsLayer)) {
                    System.out.println("Try to grab into a new layer when "+wmsLayer.getName()+" is already opened.");
                    // remove the duplicated layer
                    Main.map.mapView.removeLayer(wmsLayer);
                    throw new DuplicateLayerException();
                }
            }
        }
    }

    public void cancel() {
        if (urlConn != null) {
            urlConn.setConnectTimeout(1);
            urlConn.setReadTimeout(1);
            //urlConn.disconnect();
        }
        downloadCanceled = true;
        lastWMSLayerName = null;
    }

}
