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
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.GBC;

public class CadastreInterface {
    public boolean downloadCancelled = false;
    public HttpURLConnection urlConn = null;

    private CadastreGrabber cadastreGrabber;
    private String cookie;
    private String interfaceRef = null;
    private URL searchFormURL;
    private Vector<String> listOfCommunes = new Vector<String>();
    private Vector<String> listOfTA = new Vector<String>();

    final String baseURL = "http://www.cadastre.gouv.fr";
    final String cImageFormat = "Cette commune est au format ";
    final String cCommuneListStart = "<select name=\"codeCommune\"";
    final String cCommuneListEnd = "</select>";
    final String c0ptionListStart = "<option value=\"";
    final String cOptionListEnd = "</option>";
    final String cBBoxCommunStart = "new GeoBox(";
    final String cBBoxCommunEnd = ")";

    final String cInterfaceVector = "afficherCarteCommune.do";
    final String cInterfaceRaster = "afficherCarteTa.do";

    CadastreInterface(CadastreGrabber cadastreGrabber) {
        this.cadastreGrabber = cadastreGrabber;
    }

    public boolean retrieveInterface(WMSLayer wmsLayer) throws DuplicateLayerException {
        if (wmsLayer.getName().equals(""))
            return false;
        // open the session with the French Cadastre web front end
        downloadCancelled = false;
        try {
            if (cookie == null || !wmsLayer.getName().equals(cadastreGrabber.getLastWMSLayerName())) {
                getCookie();
                getInterface(wmsLayer);
                cadastreGrabber.setLastWMSLayerName(wmsLayer.getName());
            }
            openInterface();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Town/city {0} not found or not available in WMS.\n" +
                            "Please check its availibility on www.cadastre.gouv.fr", wmsLayer.getLocation()));
            return false;
        }
        return true;
    }

    private void getCookie() throws IOException {
        try {
            // first, get the cookie from Cadastre to allow next downloads
            searchFormURL = new URL(baseURL + "/scpc/rechercherPlan.do");
            urlConn = (HttpURLConnection)searchFormURL.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.connect();
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Cannot get Cadastre cookie.");
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while(in.readLine() != null) {}  // read the buffer otherwise we sent POST too early
            String headerName=null;
            for (int i=1; (headerName = urlConn.getHeaderFieldKey(i))!=null; i++) {
                if (headerName.equals("Set-Cookie")) {
                    cookie = urlConn.getHeaderField(i);
                    cookie = cookie.substring(0, cookie.indexOf(";"));
                    System.out.println("Cookie="+cookie);
                }
            }
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(
                "Illegal url.").initCause(e);
        }
    }

    public void resetCookie() {
        cadastreGrabber.setLastWMSLayerName(null);
    }

    public void resetCookieIfNewLayer(String newWMSLayerName) {
        if (!newWMSLayerName.equals(cadastreGrabber.getLastWMSLayerName())) {
            resetCookie();
        }
    }

    public void setCookie() {
        urlConn.setRequestProperty("Cookie", cookie);
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
                    wmsLayer.setCodeCommune(selectCommuneDialog());
                    checkLayerDuplicates(wmsLayer);
                    interfaceRef = postForm(wmsLayer, wmsLayer.getCodeCommune());
                }
                if (wmsLayer.isRaster() && listOfTA.size() > 1) {
                    // commune known but raster format. Select "tableau d'assemblage" from list.
                    wmsLayer.setCodeCommune(selectTADialog());
                    checkLayerDuplicates(wmsLayer);
                    interfaceRef = buildRasterInterfaceRef(wmsLayer.getCodeCommune());
                }
            }
        }

        if (interfaceRef == null)
            throw new IOException("Town/city " + wmsLayer.getLocation() + " not found.");
    }

    private void openInterface() throws IOException  {
        try {
            // finally, open the interface on server side giving access to the wms server
            URL interfaceURL = new URL(baseURL + "/scpc/"+interfaceRef);
            urlConn = (HttpURLConnection)interfaceURL.openConnection();
            urlConn.setRequestMethod("GET");
            setCookie();
            urlConn.connect();
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Cannot open Cadastre interface. GET response:"+urlConn.getResponseCode());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while(in.readLine() != null) {}  // read the buffer otherwise we sent POST too early
            System.out.println("GET to open interface sent");
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
     *
     * @param location
     * @param codeCommune
     * @return retURL url to available cadastre vectorised master piece; "" if not found
     * @throws IOException
     */
    private String postForm(WMSLayer wmsLayer, String codeCommune) throws IOException {
        try {
            String ln = null;
            String line = null;
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
            content += "&nbResultatParPage=10";
            urlConn = (HttpURLConnection)searchFormURL.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            setCookie();
            OutputStream wr = urlConn.getOutputStream();
            wr.write(content.getBytes());
            wr.flush();
            wr.close();
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((ln = rd.readLine()) != null) {
                line += ln;
            }
            rd.close();
            urlConn.disconnect();
            System.out.println("POST="+line);
            if (line.indexOf(cImageFormat) != -1) {
                int i = line.indexOf(cImageFormat);
                int j = line.indexOf(".", i);
                wmsLayer.setRaster(line.substring(i+cImageFormat.length(), j).equals("image"));
            }
            if (!wmsLayer.isRaster() && line.indexOf(cInterfaceVector) != -1) {  // "afficherCarteCommune.do"
                // shall be something like: interfaceRef = "afficherCarteCommune.do?c=X2269";
                line = line.substring(line.indexOf(cInterfaceVector),line.length());
                line = line.substring(0, line.indexOf("'"));
                System.out.println("interface ref.:"+line);
                return line;
            } else if (wmsLayer.isRaster() && line.indexOf(cInterfaceRaster) != -1) { // "afficherCarteTa.do"
                // list of values parsed in listOfTA (Tableau d'assemblage)
                parseTAList(line.substring(line.indexOf(cInterfaceRaster)));
                if (listOfTA.size() == 1) {
                    wmsLayer.setCodeCommune(listOfTA.firstElement());
                    return buildRasterInterfaceRef(listOfTA.firstElement());
                }
                return null;
            } else if (line.indexOf(cCommuneListStart) != -1 && line.indexOf(cCommuneListEnd) != -1) {
                // list of values parsed in listOfCommunes
                int i = line.indexOf(cCommuneListStart);
                int j = line.indexOf(cCommuneListEnd, i);
                parseCommuneList(line.substring(i, j));
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

    private void parseTAList(String input) {
        while (input.indexOf(cInterfaceRaster) != -1) {
            input = input.substring(input.indexOf(cInterfaceRaster));
            String codeTA = input.substring(0, input.indexOf("'"));
            codeTA = codeTA.substring(codeTA.indexOf("=")+1);
            if (!listOfTA.contains(codeTA)) {
                System.out.println("parse "+codeTA);
                listOfTA.add(codeTA);
            }
            input = input.substring(cInterfaceRaster.length());
        }
    }

    private String selectCommuneDialog() {
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
        pane.createDialog(Main.parent, tr("Select commune")).setVisible(true);
        if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue()))
            return null;
        String result = listOfCommunes.elementAt(inputCommuneList.getSelectedIndex()-1);
        return result.substring(1, result.indexOf(">")-2);
    }

    private String selectTADialog() {
        JPanel p = new JPanel(new GridBagLayout());
        JComboBox inputTAList = new JComboBox(listOfTA);
        p.add(inputTAList, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 0, 0, 0));
        JOptionPane pane = new JOptionPane(p, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null) {
            private static final long serialVersionUID = 1L;
        };
        pane.createDialog(Main.parent, tr("Select Tableau d'Assemblage")).setVisible(true);
        if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue()))
            return null;
        String result = listOfTA.elementAt(inputTAList.getSelectedIndex());
        return result;
    }

    private String buildRasterInterfaceRef(String codeCommune) {
        return cInterfaceRaster + "?f=" + codeCommune;
    }

    public EastNorthBound retrieveCommuneBBox() throws IOException {
        if (interfaceRef == null)
            return null;
        String ln = null;
        String line = null;
        // send GET opening normally the small window with the commune overview
        String content = baseURL + "/scpc/" + interfaceRef;
        content += "&dontSaveLastForward&keepVolatileSession=";
        searchFormURL = new URL(content);
        System.out.println("HEAD:"+content);
        urlConn = (HttpURLConnection)searchFormURL.openConnection();
        urlConn.setRequestMethod("GET");
        setCookie();
        urlConn.connect();
        if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Cannot get Cadastre response.");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        while ((ln = in.readLine()) != null) {
            line += ln;
        }
        in.close();
        urlConn.disconnect();
        return parseBBoxCommune(line);
    }

    private EastNorthBound parseBBoxCommune(String input) {
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
            return new EastNorthBound(new EastNorth(minx,miny), new EastNorth(maxx,maxy));
        }
        return null;
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
        downloadCancelled = true;
        cadastreGrabber.setLastWMSLayerName(null);
    }

}
