// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo.ruian;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.gui.datatransfer.ClipboardUtils;
import org.openstreetmap.josm.plugins.pointinfo.PointInfoUtils;

/**
 * Private class contains RUIAN data
 * @author Marián Kyral
 */
class RuianRecord {

    // CHECKSTYLE.OFF: SingleSpaceSeparator
    private double   m_coor_lat, m_coor_lon;
    private String   m_source;

    private long     m_objekt_ruian_id;
    private int      m_objekt_podlazi;
    private int      m_objekt_byty;
    private String   m_objekt_zpusob_vyuziti;
    private String   m_objekt_zpusob_vyuziti_kod;
    private String   m_objekt_zpusob_vyuziti_key;
    private String   m_objekt_zpusob_vyuziti_val;
    private String   m_objekt_dokonceni;
    private String   m_objekt_plati_od;

    private String  m_err_user;
    private String  m_err_date;
    private String  m_err_type;
    private String  m_err_note;

    private ArrayList<ObjectWithoutGeometry> m_so_bez_geometrie;

    private ArrayList<AddrPlaces> m_adresni_mista;

    private long     m_parcela_ruian_id;
    private String   m_parcela_druh_pozemku;
    private String   m_parcela_zpusob_vyuziti;
    private String   m_parcela_plati_od;

    private long     m_ulice_ruian_id;
    private String   m_ulice_jmeno;

    private long     m_katastr_ruian_id;
    private String   m_katastr_nazev;
    private long     m_katastr_obec_kod;
    private String   m_katastr_obec;
    private long     m_katastr_okres_kod;
    private String   m_katastr_okres;
    private long     m_katastr_kraj_kod;
    private String   m_katastr_kraj;
    // CHECKSTYLE.ON: SingleSpaceSeparator

    /**
     * Constructor
     *
     */
    RuianRecord() {
        init();
    }

    /**
     * Initialization
     *
     */
    private void init() {

        m_coor_lat = 0;
        m_coor_lon = 0;
        m_source = "";

        m_objekt_ruian_id = 0;
        m_objekt_podlazi = 0;
        m_objekt_byty = 0;
        m_objekt_zpusob_vyuziti = "";
        m_objekt_zpusob_vyuziti_kod = "";
        m_objekt_zpusob_vyuziti_key = "";
        m_objekt_zpusob_vyuziti_val = "";
        m_objekt_dokonceni = "";
        m_objekt_plati_od = "";

        m_err_user = "";
        m_err_date = "";
        m_err_type = "";
        m_err_note = "";

        m_so_bez_geometrie = new ArrayList<>();
        m_adresni_mista = new ArrayList<>();

        m_parcela_ruian_id = 0;
        m_parcela_druh_pozemku = "";
        m_parcela_zpusob_vyuziti = "";
        m_parcela_plati_od = "";

        m_ulice_ruian_id = 0;
        m_ulice_jmeno = "";

        m_katastr_ruian_id = 0;
        m_katastr_nazev = "";
        m_katastr_obec_kod = 0;
        m_katastr_obec = "";
        m_katastr_okres_kod = 0;
        m_katastr_okres = "";
        m_katastr_kraj_kod = 0;
        m_katastr_kraj = "";
    }

    /**
     * Parse given JSON string and fill variables with RUIAN data
     * @param jsonStr JSON string
     */
    public void parseJSON(String jsonStr) {

        init();

        JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(jsonStr.getBytes(StandardCharsets.UTF_8)));
        JsonObject obj = jsonReader.readObject();
        jsonReader.close();

        parseCoordinates(obj);

        // =========================================================================
        parseStavebniObjekt(obj);

        // =========================================================================
        parseNahlasenyProblem(obj);

        // =========================================================================
        parseSoBezGeometrie(obj);

        // =========================================================================
        parseAdresniMista(obj);

        // =========================================================================
        parseParcela(obj);

        // =========================================================================
        parseUlice(obj);

        // =========================================================================
        parseKatastr(obj);
    }

    private JsonObject getSafeJsonObject(JsonObject obj, String key) {
        JsonValue val = obj.get(key);
        if (val instanceof JsonObject) {
            return (JsonObject) val;
        } else if (val instanceof JsonArray) {
            JsonArray array = (JsonArray) val;
            if (!array.isEmpty()) {
                return array.getJsonObject(0);
            }
        }
        throw new IllegalArgumentException("No value for " + key);
    }

    private void parseCoordinates(JsonObject obj) {
        try {
            JsonObject coorObjekt = obj.getJsonObject("coordinates");

            try {
                m_coor_lat = Double.parseDouble(coorObjekt.getString("lat"));
            } catch (NumberFormatException e) {
                Main.warn(e, "coordinates.lat:");
            }

            try {
                m_coor_lon = Double.parseDouble(coorObjekt.getString("lon"));
            } catch (NumberFormatException e) {
                Main.warn(e, "coordinates.lon:");
            }

            try {
                m_source = obj.getString("source");
            } catch (RuntimeException e) {
                Main.warn(e, "source:");
            }

        } catch (Exception e) {
            Main.warn(e, "coordinates:");
        }
    }

    private void parseStavebniObjekt(JsonObject obj) {
        try {
            JsonObject stavebniObjekt = getSafeJsonObject(obj, "stavebni_objekt");

            try {
                m_objekt_ruian_id = Long.parseLong(stavebniObjekt.getString("ruian_id"));
            } catch (Exception e) {
                Main.warn(e, "stavebni_objekt.ruian_id:");
            }

            try {
                m_objekt_podlazi = Integer.parseInt(stavebniObjekt.getString("pocet_podlazi"));
            } catch (Exception e) {
                Main.warn(e, "stavebni_objekt.pocet_podlazi:");
            }

            try {
                m_objekt_byty = Integer.parseInt(stavebniObjekt.getString("pocet_bytu"));
            } catch (Exception e) {
                Main.warn(e, "stavebni_objekt.pocet_bytu:");
            }

            try {
                m_objekt_zpusob_vyuziti = stavebniObjekt.getString("zpusob_vyuziti");
            } catch (Exception e) {
                Main.warn(e, "stavebni_objekt.zpusob_vyuziti:");
            }

            try {
                m_objekt_zpusob_vyuziti_kod = stavebniObjekt.getString("zpusob_vyuziti_kod");
            } catch (Exception e) {
                Main.warn(e, "stavebni_objekt.m_objekt_zpusob_vyuziti_kod:");
            }

            try {
                m_objekt_zpusob_vyuziti_key = stavebniObjekt.getString("zpusob_vyuziti_key");
            } catch (Exception e) {
                Main.warn(e, "stavebni_objekt.zpusob_vyuziti_key:");
            }

            try {
                m_objekt_zpusob_vyuziti_val = stavebniObjekt.getString("zpusob_vyuziti_val");
            } catch (Exception e) {
                Main.warn(e, "stavebni_objekt.m_objekt_zpusob_vyuziti_val:");
            }

            try {
                m_objekt_plati_od = stavebniObjekt.getString("plati_od");
            } catch (Exception e) {
                Main.warn(e, "stavebni_objekt.plati_od:");
            }

            try {
                m_objekt_dokonceni = stavebniObjekt.getString("dokonceni");
            } catch (Exception e) {
                Main.warn(e, "stavebni_objekt.dokonceni:");
            }

        } catch (Exception e) {
            Main.warn(e, "stavebni_objekt:");
        }
    }

    private void parseNahlasenyProblem(JsonObject obj) {
        try {
            JsonObject errObjekt = getSafeJsonObject(obj, "nahlaseny_problem");

            try {
                m_err_user = errObjekt.getString("uzivatel");
            } catch (Exception e) {
                Main.warn(e, "nahlaseny_problem.uzivatel:");
            }

            try {
                m_err_date = errObjekt.getString("datum");
            } catch (Exception e) {
                Main.warn(e, "nahlaseny_problem.datum:");
            }

            try {
                m_err_type = errObjekt.getString("duvod");
            } catch (Exception e) {
                Main.warn(e, "nahlaseny_problem.duvod:");
            }

            try {
                m_err_note = errObjekt.getString("poznamka");
            } catch (Exception e) {
                Main.warn(e, "nahlaseny_problem.poznamka:");
            }

        } catch (Exception e) {
            Main.warn(e, "nahlaseny_problem:");
        }
    }

    private void parseSoBezGeometrie(JsonObject obj) {
        try {
            JsonArray arr = obj.getJsonArray("so_bez_geometrie");

            for (int i = 0; i < arr.size(); i++) {
                JsonObject soBezGeom = arr.getJsonObject(i);
                ObjectWithoutGeometry so = new ObjectWithoutGeometry();

                try {
                    so.setRuianID(Long.parseLong(soBezGeom.getString("ruian_id")));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.ruian_id:");
                }

                try {
                    so.setPodlazi(Integer.parseInt(soBezGeom.getString("pocet_podlazi")));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.pocet_podlazi:");
                }

                try {
                    so.setByty(Integer.parseInt(soBezGeom.getString("pocet_bytu")));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.pocet_bytu:");
                }

                try {
                    so.setZpusobVyuziti(soBezGeom.getString("zpusob_vyuziti"));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.zpusob_vyuziti:");
                }

                try {
                    so.setZpusobVyuzitiKod(soBezGeom.getString("zpusob_vyuziti_kod"));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.zpusob_vyuziti_kod:");
                }

                try {
                    so.setZpusobVyuzitiKey(soBezGeom.getString("zpusob_vyuziti_key"));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.zpusob_vyuziti_key:");
                }

                try {
                    so.setZpusobVyuzitiVal(soBezGeom.getString("zpusob_vyuziti_val"));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.zpusob_vyuziti_val:");
                }

                try {
                    so.setDokonceni(soBezGeom.getString("dokonceni"));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.dokonceni:");
                }

                try {
                    so.setPlatiOd(soBezGeom.getString("plati_od"));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.plati_od:");
                }

                try {
                    so.setVzdalenost(Float.parseFloat(soBezGeom.getString("vzdalenost")));
                } catch (Exception e) {
                    Main.warn(e, "so_bez_geometrie.vzdalenost:");
                }

                m_so_bez_geometrie.add(so);
            }
        } catch (Exception e) {
            Main.warn(e, "so_bez_geometrie:");
        }
    }

    private void parseAdresniMista(JsonObject obj) {
        try {
            JsonArray arr = obj.getJsonArray("adresni_mista");

            for (int i = 0; i < arr.size(); i++) {
                JsonObject adresniMisto = arr.getJsonObject(i);
                AddrPlaces am = new AddrPlaces();

                try {
                    am.setRuianID(Long.parseLong(adresniMisto.getString("ruian_id")));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.ruian_id:");
                }

                try {
                    JsonArray node = adresniMisto.getJsonArray("pozice");
                    am.setPosition(new LatLon(
                            LatLon.roundToOsmPrecision(node.getJsonNumber(1).doubleValue()),
                            LatLon.roundToOsmPrecision(node.getJsonNumber(0).doubleValue()))
                            );
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.pozice:");
                }

                try {
                    am.setBudovaID(Long.parseLong(adresniMisto.getString("budova_kod")));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.budova_kod:");
                }

                try {
                    am.setCisloTyp(adresniMisto.getString("cislo_typ"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.cislo_typ:");
                }

                try {
                    am.setCisloDomovni(adresniMisto.getString("cislo_domovni"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.cislo_domovni:");
                }

                try {
                    am.setCisloOrientacni(adresniMisto.getString("cislo_orientacni"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.cislo_orientacni:");
                }

                try {
                    am.setUliceID(Long.parseLong(adresniMisto.getString("ulice_kod")));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.ulice_kod:");
                }

                try {
                    am.setUlice(adresniMisto.getString("ulice"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.ulice:");
                }

                try {
                    am.setCastObceID(Long.parseLong(adresniMisto.getString("cast_obce_kod")));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.cast_obce_kod:");
                }

                try {
                    am.setCastObce(adresniMisto.getString("cast_obce"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.m_cast_obce:");
                }

                try {
                    am.setMestskaCastID(Long.parseLong(adresniMisto.getString("mestska_cast_kod")));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.mestska_cast_kod:");
                }

                try {
                    am.setMestskaCast(adresniMisto.getString("mestska_cast"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.mestska_cast:");
                }

                try {
                    am.setObecID(Long.parseLong(adresniMisto.getString("obec_kod")));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.obec_kod:");
                }

                try {
                    am.setObec(adresniMisto.getString("obec"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.obec:");
                }

                try {
                    am.setOkresID(Long.parseLong(adresniMisto.getString("okres_kod")));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.okres_kod:");
                }

                try {
                    am.setOkres(adresniMisto.getString("okres"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.okres:");
                }

                try {
                    am.setKrajID(Long.parseLong(adresniMisto.getString("kraj_kod")));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.kraj_kod:");
                }

                try {
                    am.setKraj(adresniMisto.getString("kraj"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.kraj:");
                }

                try {
                    am.setPsc(adresniMisto.getString("psc"));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.psc:");
                }

                try {
                    am.setVzdalenost(Float.parseFloat(adresniMisto.getString("vzdalenost")));
                } catch (Exception e) {
                    Main.warn(e, "adresni_mista.vzdalenost:");
                }

                m_adresni_mista.add(am);
            }
        } catch (Exception e) {
            Main.warn(e, "adresni_mista:");
        }
    }

    private void parseParcela(JsonObject obj) {
        try {
            JsonObject parcela = getSafeJsonObject(obj, "parcela");

            try {
                m_parcela_ruian_id = Long.parseLong(parcela.getString("ruian_id"));
            } catch (Exception e) {
                Main.warn(e, "parcela.ruian_id:");
            }

            try {
                m_parcela_druh_pozemku = parcela.getString("druh_pozemku");
            } catch (Exception e) {
                Main.warn(e, "parcela.druh_pozemku:");
            }

            try {
                m_parcela_zpusob_vyuziti = parcela.getString("zpusob_vyuziti");
            } catch (Exception e) {
                Main.warn(e, "parcela.zpusob_vyuziti:");
            }

            try {
                m_parcela_plati_od = parcela.getString("plati_od");
            } catch (Exception e) {
                Main.warn(e, "parcela.plati_od:");
            }

        } catch (Exception e) {
            Main.warn(e, "parcela:");
        }
    }

    private void parseUlice(JsonObject obj) {
        try {
            JsonObject ulice = getSafeJsonObject(obj, "ulice");

            try {
                m_ulice_ruian_id = Long.parseLong(ulice.getString("ruian_id"));
            } catch (Exception e) {
                Main.warn(e, "ulice.ruian_id:");
            }

            try {
                m_ulice_jmeno = ulice.getString("jmeno");
            } catch (Exception e) {
                Main.warn(e, "ulice.jmeno:");
            }

        } catch (Exception e) {
            Main.warn(e, "ulice:");
        }
    }

    private void parseKatastr(JsonObject obj) {
        try {
            JsonObject katastr = getSafeJsonObject(obj, "katastr");

            try {
                m_katastr_ruian_id = Long.parseLong(katastr.getString("ruian_id"));
            } catch (Exception e) {
                Main.warn(e, "katastr.ruian_id:");
            }

            try {
                m_katastr_nazev = katastr.getString("nazev");
            } catch (Exception e) {
                Main.warn(e, "katastr.nazev:");
            }

            try {
                m_katastr_obec_kod = Long.parseLong(katastr.getString("obec_kod"));
            } catch (Exception e) {
                Main.warn(e, "katastr.obec_kod:");
            }

            try {
                m_katastr_obec = katastr.getString("obec");
            } catch (Exception e) {
                Main.warn(e, "katastr.okres:");
            }

            try {
                m_katastr_okres_kod = Long.parseLong(katastr.getString("okres_kod"));
            } catch (Exception e) {
                Main.warn(e, "katastr.okres_kod:");
            }

            try {
                m_katastr_okres = katastr.getString("okres");
            } catch (Exception e) {
                Main.warn(e, "katastr.okres:");
            }

            try {
                m_katastr_kraj_kod = Long.parseLong(katastr.getString("kraj_kod"));
            } catch (Exception e) {
                Main.warn(e, "katastr.kraj_kod:");
            }

            try {
                m_katastr_kraj = katastr.getString("kraj");
            } catch (Exception e) {
                Main.warn(e, "katastr.kraj:");
            }

        } catch (Exception e) {
            Main.warn(e, "katastr:");
        }
    }

    /**
     * Return Html text representation
     * @return String htmlText
     */
    public String getHtml() {

        String icon_ext_link = "<img src=" +getClass().getResource(
                "/images/dialogs/open-external-link.png")+" border=0 alt=\"Zobrazit na externích stránkách\"/>";
        String icon_ext_link_ruian = "<img src=" +getClass().getResource(
                "/images/dialogs/open-external-link.png")+" border=0 alt=\"Zobrazit na stránkách RUIAN\"/>";
        String icon_ext_link_kn = "<img src=" +getClass().getResource(
                "/images/dialogs/open-external-link-kn.png")+" border=0 alt=\"Zobrazit na stránkách katastru nemovitostí\"/>";
        String icon_copy_tags = "<img src=" +getClass().getResource(
                "/images/dialogs/copy-tags.png")+" border=0 alt=\"Kopírovat tagy\"/>";
        String icon_create_addr = "<img src=" +getClass().getResource(
                "/images/dialogs/create-addr.png")+" border=0 alt=\"Vytvořit adresní bod\"/>";
        String icon_create_addr_ruian = "<img src=" +getClass().getResource(
                "/images/dialogs/create-addr-ruian.png")+" border=0 alt=\"Vytvořit adresní bod dle RUIANu\"/>";
        String icon_ruian_error = "<img src=" +getClass().getResource(
                "/images/dialogs/create-bug-report.png")+" border=0 alt=\"Nahlásit problém v datech\"/>";
        // CHECKSTYLE.OFF: LineLength
        String url_cpost = "http://www.postaonline.cz/vyhledani-psc?p_p_id=psc_WAR_pcpvpp&p_p_lifecycle=1&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&_psc_WAR_pcpvpp_struts.portlet.action=%2Fview%2FdetailPost&_psc_WAR_pcpvpp_struts.portlet.mode=view&_psc_WAR_pcpvpp_zipCode=";
        // CHECKSTYLE.ON: LineLength
        String url_stavebni_objekt = "http://vdp.cuzk.cz/vdp/ruian/stavebniobjekty/";
        String url_adresni_misto = "http://vdp.cuzk.cz/vdp/ruian/adresnimista/";
        String url_parcela = "http://vdp.cuzk.cz/vdp/ruian/parcely/";
        String url_katastralni_uzemi = "http://vdp.cuzk.cz/vdp/ruian/katastralniuzemi/";
        String url_ulice = "http://vdp.cuzk.cz/vdp/ruian/ulice/";
        String url_mistni_cast = "http://vdp.cuzk.cz/vdp/ruian/castiobce/";
        String url_mestska_cast = "http://vdp.cuzk.cz/vdp/ruian/mestskecasti/";
        String url_obec = "http://vdp.cuzk.cz/vdp/ruian/obce/";
        String url_okres = "http://vdp.cuzk.cz/vdp/ruian/okresy/";
        String url_kraj = "http://vdp.cuzk.cz/vdp/ruian/vusc/";
        String url_vlastnici = "http://vdp.cuzk.cz/vdp/ruian/vlastnici?typ=";

        String url_ruian_error = "http://ruian.poloha.net/building.php?kod=";

        StringBuilder r = new StringBuilder();

        if (m_objekt_ruian_id == 0 &&
            m_parcela_ruian_id == 0 &&
            m_adresni_mista.size() == 0 &&
            m_ulice_ruian_id == 0 &&
            m_katastr_ruian_id == 0)
            return "";

        r.append("<html><body bgcolor=\"white\" color=\"black\" ><table><tr><td>");
        r.append("<br/>");
        if (m_objekt_ruian_id > 0) {
            r.append("<i><u>Informace o budově</u></i><br/>")
             .append("<b>RUIAN id: </b>"+ m_objekt_ruian_id +"&nbsp;&nbsp;<a href="+ url_stavebni_objekt + m_objekt_ruian_id +">"
                    + icon_ext_link_ruian + "</a>")
             .append("&nbsp;&nbsp;<a href="+ url_vlastnici + "so&id=" + m_objekt_ruian_id + ">"+ icon_ext_link_kn +"</a>")
             .append("&nbsp;&nbsp;<a href=file://tags.copy/building>"+ icon_copy_tags +"</a>")
             .append("&nbsp;&nbsp;<a href=" + url_ruian_error + m_objekt_ruian_id + ">"+ icon_ruian_error +"</a><br/>");
            if (m_adresni_mista.size() == 0) r.append("<b>Budova: </b> bez č.p./č.e<br/>");
            else if (m_adresni_mista.get(0).getCisloTyp().equals("Číslo popisné")) r.append("<b>Budova: </b>s číslem popisným<br/>");
            else r.append("<b>Budova: </b>s číslem evidenčním<br/>");
            if (m_objekt_podlazi > 0) r.append("<b>Počet podlaží: </b>" + m_objekt_podlazi + "<br/>");
            if (m_objekt_byty > 0) r.append("<b>Počet bytů: </b>" + m_objekt_byty + "<br/>");
            r.append("<b>Způsob využití: </b>" + m_objekt_zpusob_vyuziti + "<br/>")
             .append("<b>Datum dokončení: </b>" + m_objekt_dokonceni + "<br/>")
             .append("<b>Platí od: </b>" + m_objekt_plati_od + "<br/>");

            if (m_adresni_mista.size() > 1) {
                r.append("<i><u>Informace o adrese</u></i><br/>");
                // More address places
                int i = 0;
                r.append("<br/>")
                 .append("<b>" + m_adresni_mista.get(i).getCisloTyp() + "</b> (více adres)<b>: </b>"
                        + m_adresni_mista.get(i).getCisloDomovni() + "<br/>")
                 .append("<b>Část obce: </b>" + m_adresni_mista.get(i).getCastObce())
                 .append("&nbsp;&nbsp;<a href="+ url_mistni_cast + m_adresni_mista.get(i).getCastObceID() +">" + icon_ext_link_ruian + "</a><br/>");
                if (m_adresni_mista.get(i).getMestskaCast().length() > 0) {
                    r.append("<b>Městská část: </b>" + m_adresni_mista.get(i).getMestskaCast())
                     .append("&nbsp;&nbsp;<a href="+ url_mestska_cast + m_adresni_mista.get(i).getMestskaCastID() +">"
                            + icon_ext_link_ruian + "</a><br/>");
                }
                r.append("<b>Obec: </b>" + m_adresni_mista.get(i).getObec())
                 .append("&nbsp;&nbsp;<a href="+ url_obec + m_adresni_mista.get(i).getObecID() +">" + icon_ext_link_ruian + "</a><br/>")
                 .append("<b>Okres: </b>" + m_adresni_mista.get(i).getOkres())
                 .append("&nbsp;&nbsp;<a href="+ url_okres + m_adresni_mista.get(i).getOkresID() +">" + icon_ext_link_ruian + "</a><br/>")
                 .append("<b>Kraj: </b>" + m_adresni_mista.get(i).getKraj())
                 .append("&nbsp;&nbsp;<a href="+ url_kraj + m_adresni_mista.get(i).getKrajID() +">" + icon_ext_link_ruian + "</a><br/>");

            } else if (m_adresni_mista.size() == 1
                    && (m_adresni_mista.get(0).getCisloDomovni() == null || m_adresni_mista.get(0).getCisloDomovni().isEmpty())) {
                // Without building number
                int i = 0;
                r.append("<br/>")
                 .append("<i><u>Informace o adrese</u></i><br/>")
                 .append("<b>Budova: </b>" + m_adresni_mista.get(i).getCisloTyp() + "<br/>");
                if (m_adresni_mista.get(i).getMestskaCast().length() > 0) {
                    r.append("<b>Městská část: </b>" + m_adresni_mista.get(i).getMestskaCast())
                     .append("&nbsp;&nbsp;<a href="+ url_mestska_cast + m_adresni_mista.get(i).getMestskaCastID() +">"
                            + icon_ext_link_ruian + "</a><br/>");
                }
                r.append("<b>Obec: </b>" + m_adresni_mista.get(i).getObec())
                 .append("&nbsp;&nbsp;<a href="+ url_obec + m_adresni_mista.get(i).getObecID() +">" + icon_ext_link_ruian + "</a><br/>")
                 .append("<b>Okres: </b>" + m_adresni_mista.get(i).getOkres())
                 .append("&nbsp;&nbsp;<a href="+ url_okres + m_adresni_mista.get(i).getOkresID() +">" + icon_ext_link_ruian + "</a><br/>")
                 .append("<b>Kraj: </b>" + m_adresni_mista.get(i).getKraj())
                 .append("&nbsp;&nbsp;<a href="+ url_kraj + m_adresni_mista.get(i).getKrajID() +">" + icon_ext_link_ruian + "</a><br/>");

            } else if (m_adresni_mista.size() == 1) {
                // Only one address place
                int i = 0;
                String x = "";
                String x_name = "";
                if (!m_adresni_mista.get(i).getCisloOrientacni().isEmpty()) {
                    x = "/" + m_adresni_mista.get(i).getCisloOrientacni();
                    x_name = "/orientační";
                }
                r.append("<br/>")
                 .append("<i><u>Informace o adrese</u></i><br/>")
                 .append("<b>RUIAN id: </b>"+ m_adresni_mista.get(i).getRuianID() +"&nbsp;&nbsp;<a href="+ url_adresni_misto
                        + m_adresni_mista.get(i).getRuianID() +">" + icon_ext_link_ruian + "</a>")
                 .append("&nbsp;&nbsp;<a href=file://tags.copy/address:"+i+">"+ icon_copy_tags +"</a>")
                 .append("&nbsp;&nbsp;<a href=file://tags.create/address:"+i+">"+ icon_create_addr +"</a>")
                 .append("&nbsp;&nbsp;<a href=file://tags.create-on-place/address:"+i+">"+ icon_create_addr_ruian +"</a><br/>")
                 .append("<b>" + m_adresni_mista.get(i).getCisloTyp() + x_name + ": </b>" + m_adresni_mista.get(i).getCisloDomovni() + x)
                 .append("<br/>");
                if (!m_adresni_mista.get(i).getUlice().isEmpty()) {
                    r.append("<b>Ulice: </b>" + m_adresni_mista.get(i).getUlice())
                     .append("&nbsp;&nbsp;<a href="+ url_ulice + m_adresni_mista.get(i).getUliceID() +">" + icon_ext_link_ruian + "</a><br/>");
                }
                r.append("<b>Část obce: </b>" + m_adresni_mista.get(i).getCastObce())
                 .append("&nbsp;&nbsp;<a href="+ url_mistni_cast + m_adresni_mista.get(i).getCastObceID() +">" + icon_ext_link_ruian + "</a><br/>");
                if (m_adresni_mista.get(i).getMestskaCast().length() > 0) {
                    r.append("<b>Městská část: </b>" + m_adresni_mista.get(i).getMestskaCast())
                     .append("&nbsp;&nbsp;<a href="+ url_mestska_cast + m_adresni_mista.get(i).getMestskaCastID() +">"
                            + icon_ext_link_ruian + "</a><br/>");
                }
                r.append("<b>Obec: </b>" + m_adresni_mista.get(i).getObec())
                 .append("&nbsp;&nbsp;<a href="+ url_obec + m_adresni_mista.get(i).getObecID() +">" + icon_ext_link_ruian + "</a><br/>")
                 .append("<b>Okres: </b>" + m_adresni_mista.get(i).getOkres())
                 .append("&nbsp;&nbsp;<a href="+ url_okres + m_adresni_mista.get(i).getOkresID() +">" + icon_ext_link_ruian + "</a><br/>")
                 .append("<b>Kraj: </b>" + m_adresni_mista.get(i).getKraj())
                 .append("&nbsp;&nbsp;<a href="+ url_kraj + m_adresni_mista.get(i).getKrajID() +">" + icon_ext_link_ruian + "</a><br/>")
                 .append("<b>PSČ: </b>" + m_adresni_mista.get(i).getPsc())
                 .append("&nbsp;&nbsp;<a href="+ url_cpost + m_adresni_mista.get(i).getPsc() +">" + icon_ext_link_ruian + "</a><br/>");

            }
            r.append("<br/>");
        }

        // Reported errors
        if (m_objekt_ruian_id > 0 && !m_err_user.isEmpty()) {
            r.append("<i><u>Nahlášený problém</u></i>")
             .append("&nbsp;&nbsp;<a href=" + url_ruian_error + m_objekt_ruian_id + ">"+ icon_ext_link +"</a><br/>")
             .append("<b>Nahlásil: </b>" + m_err_user)
             .append("<br/>")
             .append("<b>Dne: </b>" + m_err_date)
             .append("<br/>")
             .append("<b>Typ problému: </b>" + m_err_type)
             .append("<br/>");
            if (!m_err_note.isEmpty()) {
                r.append("<b>Poznámka: </b>" + m_err_note)
                 .append("<br/>");
            }
            r.append("<br/>");
        }

        // Address places
        if (m_adresni_mista.size() > 1 && m_objekt_ruian_id > 0) {
            String x = "";
            if (m_adresni_mista.get(0).getCisloTyp().equals("Číslo evidenční")) {
                x = "ev.";
            }
            r.append("<i><u>Adresní místa</u></i><br/>");
            for (int i = 0; i < m_adresni_mista.size(); i++) {
                r.append(m_adresni_mista.get(i).getUlice() + " " + x + m_adresni_mista.get(i).getCisloDomovni());
                if (!m_adresni_mista.get(i).getCisloOrientacni().isEmpty()) {
                    r.append("/" + m_adresni_mista.get(i).getCisloOrientacni());
                }
                r.append("&nbsp;&nbsp;<a href="+ url_adresni_misto + m_adresni_mista.get(i).getRuianID() + ">"+ icon_ext_link_ruian +"</a> ")
                 .append("&nbsp;&nbsp;<a href=file://tags.copy/address:"+i+">"+ icon_copy_tags +"</a>")
                 .append("&nbsp;&nbsp;<a href=file://tags.create/address:"+i+">"+ icon_create_addr +"</a>")
                 .append("&nbsp;&nbsp;<a href=file://tags.create-on-place/address:"+i+">"+ icon_create_addr_ruian +"</a>")
                 .append("<br/>");
            }
            r.append("<br/>");
        }

        // Land
        if (m_parcela_ruian_id > 0) {
            r.append("<i><u>Informace o pozemku</u></i>")
             .append("<br/>")
             .append("<b>RUIAN id: </b>"+ m_parcela_ruian_id +"&nbsp;&nbsp;<a href="+ url_parcela + m_parcela_ruian_id +">"
                    + icon_ext_link_ruian + "</a>")
             .append("&nbsp;&nbsp;<a href="+ url_vlastnici + "pa&id=" + m_parcela_ruian_id + ">"+ icon_ext_link_kn +"</a><br/>")
            // .append("&nbsp;&nbsp;<a href=file://tags.copy/parcel>"+ icon_copy_tags +"</a>")
             .append("<b>Druh pozemku: </b>" + m_parcela_druh_pozemku +"<br/>");
            if (m_parcela_zpusob_vyuziti != "") r.append("<b>Způsob využití: </b>" + m_parcela_zpusob_vyuziti +"<br/>");
            r.append("<b>Platí od: </b>" + m_parcela_plati_od +"<br/>")
             .append("<br/>");
        }

        // Street
        if (m_ulice_ruian_id > 0) {
            r.append("<i><u>Informace o ulici</u></i><br/>")
             .append("<b>RUIAN id: </b>"+ m_ulice_ruian_id +"&nbsp;&nbsp;<a href="+ url_ulice + m_ulice_ruian_id +">" + icon_ext_link_ruian + "</a>")
             .append("&nbsp;&nbsp;<a href=file://tags.copy/street>"+ icon_copy_tags +"</a><br/>")
             .append("<b>Jméno: </b>" + m_ulice_jmeno +"<br/>")
             .append("<br/>");
        }

        // Cadastral area
        if (m_katastr_ruian_id > 0) {
            r.append("<b>Katastrální území: </b>" + m_katastr_nazev)
             .append("&nbsp;&nbsp;<a href="+ url_katastralni_uzemi + m_katastr_ruian_id +">" + icon_ext_link_ruian + "</a><br/>")
             .append("<b>Obec: </b>" + m_katastr_obec)
             .append("&nbsp;&nbsp;<a href="+ url_obec + m_katastr_obec_kod +">" + icon_ext_link_ruian + "</a><br/>")
             .append("<b>Okres: </b>" + m_katastr_okres)
             .append("&nbsp;&nbsp;<a href="+ url_okres + m_katastr_okres_kod +">" + icon_ext_link_ruian + "</a><br/>")
             .append("<b>Kraj: </b>" + m_katastr_kraj)
             .append("&nbsp;&nbsp;<a href="+url_kraj + m_katastr_kraj_kod +">" + icon_ext_link_ruian + "</a><br/>")
             .append("<br/>");
        }

        // Near address places
        if (!m_adresni_mista.isEmpty() && m_objekt_ruian_id == 0) {
            String x, x_name;
            r.append("<i><u>Adresní místa v okolí</u></i><br/>")
             .append("<table>");
            for (int i = 0; i < m_adresni_mista.size(); i++) {
                x = "";
                x_name = "";
                if (m_adresni_mista.get(i).getCisloTyp().equals("Číslo evidenční")) {
                    x = "ev.";
                }
                x += m_adresni_mista.get(i).getCisloDomovni();
                if (!m_adresni_mista.get(i).getCisloOrientacni().isEmpty()) {
                    x += "/" + m_adresni_mista.get(i).getCisloOrientacni();
                    x_name += "/orientační";
                }
                r.append("<tr><td bgcolor=#e5e5ff>");
                if (!m_adresni_mista.get(i).getUlice().isEmpty()) {
                    r.append(m_adresni_mista.get(i).getVzdalenost())
                     .append("</td><td valign=\"top\"  bgcolor=#e5e5ff>")
                     .append(m_adresni_mista.get(i).getUlice() + " " + x)
                     .append("<br/><u>" + m_adresni_mista.get(i).getObec() + "</u>")
                     .append("</td><td valign=\"top\"  bgcolor=#e5e5ff>")
                     .append("<a href="+ url_adresni_misto + m_adresni_mista.get(i).getRuianID() + ">"+ icon_ext_link_ruian +"</a>")
                     .append("&nbsp;&nbsp;<a href=file://tags.copy/address:"+i+">"+ icon_copy_tags +"</a>")
                     .append("&nbsp;&nbsp;<a href=file://tags.create/address:"+i+">"+ icon_create_addr +"</a>")
                     .append("&nbsp;&nbsp;<a href=file://tags.create-on-place/address:"+i+">"+ icon_create_addr_ruian +"</a>");
                } else {
                    r.append(m_adresni_mista.get(i).getVzdalenost())
                     .append("</td><td valign=\"top\"  bgcolor=#e5e5ff>")
                     .append(m_adresni_mista.get(i).getCastObce() + " " + x + "&nbsp;");
                    if (!m_adresni_mista.get(i).getCastObce().equals(m_adresni_mista.get(i).getObec())) {
                        r.append("<br/><u>" + m_adresni_mista.get(i).getObec() + "</u>");
                    }
                    r.append("</td><td valign=\"top\"  bgcolor=#e5e5ff>")
                     .append("<a href="+ url_adresni_misto + m_adresni_mista.get(i).getRuianID() + ">"+ icon_ext_link_ruian +"</a>")
                     .append("&nbsp;&nbsp;<a href=file://tags.copy/address:"+i+">"+ icon_copy_tags +"</a>")
                     .append("&nbsp;&nbsp;<a href=file://tags.create/address:"+i+">"+ icon_create_addr +"</a>")
                     .append("&nbsp;&nbsp;<a href=file://tags.create-on-place/address:"+i+">"+ icon_create_addr_ruian +"</a>");
                }
                r.append("</td></tr>");
            }
            r.append("</table><br/>");
        }

        if (!m_so_bez_geometrie.isEmpty()) {
            r.append("<i><u>Budovy bez geometrie v okolí</u></i><br/>")
             .append("<table>");
            for (int i = 0; i < m_so_bez_geometrie.size(); i++) {
                r.append("<tr><td bgcolor=#e5e5ff>")
                 .append(m_so_bez_geometrie.get(i).getVzdalenost())
                 .append("</td><td valign=\"top\"  bgcolor=#e5e5ff>")
                 .append(m_so_bez_geometrie.get(i).getRuianID());
                if (m_so_bez_geometrie.get(i).getZpusobVyuziti().length() > 0) {
                    r.append(" - " + m_so_bez_geometrie.get(i).getZpusobVyuziti());
                }
                r.append("</td><td valign=\"top\"  bgcolor=#e5e5ff>")
                 .append("&nbsp;&nbsp;<a href="+ url_stavebni_objekt + m_so_bez_geometrie.get(i).getRuianID() + ">"+ icon_ext_link_ruian +"</a> ")
                 .append("&nbsp;&nbsp;<a href=file://tags.copy/ghost:"+i+">"+ icon_copy_tags +"</a></br>")
                 .append("</td></tr>");
            }
            r.append("</table><br/>")
             .append("<br/>");
        }

        r.append("<hr/>")
         .append("<center><i><small>Zdroj: <a href=\"http://www.ruian.cz/\">" + m_source + "</a></small></i></center>")
         .append("</td></tr></table></body></html>");

        return r.toString();
    }

    /**
     * Convert date from Czech to OSM format
     * @param ruianDate Date in RUIAN (Czech) format DD.MM.YYYY
     * @return String with date converted to OSM data format YYYY-MM-DD
     */
    String convertDate(String ruianDate) {
        String r = new String();
        String[] parts = ruianDate.split("\\.");
        try {
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            r = Integer.toString(year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);
        } catch (Exception e) {
            Main.warn(e);
        }

        return r;
    }

    /**
     * Construct tag string for clipboard
     * @param k OSM Key
     * @param v OSM Value
     * @return String OSM tag string for clipboard
     */
    String tagToString(String k, String v) {
        return "\"" + k + "\"=\"" + v + "\"\n";
    }

    /**
     * Prepare OSM keys
     * @param keyType What to prepare (building, address, parcel. street)
     * @return String with OSM tags
     */
    String getKeys(String keyType) {
        StringBuilder c = new StringBuilder();

        // Copy building tags to clipboard
        if (keyType.equals("building") && m_objekt_ruian_id > 0) {
            c.append(tagToString("ref:ruian:building", Long.toString(m_objekt_ruian_id)));
            if (!m_objekt_zpusob_vyuziti_key.isEmpty() &&
                !m_objekt_zpusob_vyuziti_val.isEmpty()
                    ) {
                c.append(tagToString(m_objekt_zpusob_vyuziti_key, m_objekt_zpusob_vyuziti_val));
            }
            if (m_objekt_podlazi > 0) {
                c.append(tagToString("building:levels", Integer.toString(m_objekt_podlazi)));
            }
            if (m_objekt_byty > 0) {
                c.append(tagToString("building:flats", Integer.toString(m_objekt_byty)));
            }
            if (m_objekt_dokonceni.length() > 0 && convertDate(m_objekt_dokonceni).length() > 0) {
                c.append(tagToString("start_date", convertDate(m_objekt_dokonceni)));
            }
            if (m_objekt_zpusob_vyuziti_kod.length() > 0) {
                c.append(tagToString("building:ruian:type", m_objekt_zpusob_vyuziti_kod));
            }
            c.append(tagToString("source", "cuzk:ruian"));
        }

        if (keyType.startsWith("ghost") && !m_so_bez_geometrie.isEmpty()) {
            String[] key = keyType.split(":");
            int i = Integer.parseInt(key[1]);
            System.out.println("Ghost ID: " + i);

            c.append(tagToString("ref:ruian:building", Long.toString(m_so_bez_geometrie.get(i).getRuianID())));
            if (m_so_bez_geometrie.get(i).getZpusobVyuzitiKey().length() > 0 &&
                    m_so_bez_geometrie.get(i).getZpusobVyuzitiVal().length() > 0
                    ) {
                c.append(tagToString(m_so_bez_geometrie.get(i).getZpusobVyuzitiKey(), m_so_bez_geometrie.get(i).getZpusobVyuzitiVal()));
            }
            if (m_so_bez_geometrie.get(i).getPodlazi() > 0) {
                c.append(tagToString("building:levels", Integer.toString(m_so_bez_geometrie.get(i).getPodlazi())));
            }
            if (m_so_bez_geometrie.get(i).getByty() > 0) {
                c.append(tagToString("building:flats", Integer.toString(m_so_bez_geometrie.get(i).getByty())));
            }
            if (m_so_bez_geometrie.get(i).getDokonceni().length() > 0 && convertDate(m_so_bez_geometrie.get(i).getDokonceni()).length() > 0) {
                c.append(tagToString("start_date", convertDate(m_so_bez_geometrie.get(i).getDokonceni())));
            }
            if (m_so_bez_geometrie.get(i).getZpusobVyuzitiKod().length() > 0) {
                c.append(tagToString("building:ruian:type", m_so_bez_geometrie.get(i).getZpusobVyuzitiKod()));
            }
            c.append(tagToString("source", "cuzk:ruian"));
        }

        // Copy address tags to clipboard
        if (keyType.startsWith("address")) {
            if (!m_adresni_mista.isEmpty()) {
                int i;

                if (m_adresni_mista.isEmpty()) {
                    i = 0;
                } else {
                    String[] key = keyType.split(":");
                    i = Integer.valueOf(key[1]);
                    Main.info("Address ID: " + i);
                }

                // Only one address place
                if (!m_adresni_mista.get(i).getCisloTyp().equals("Číslo evidenční")) {
                    // Cislo popisne
                    c.append(tagToString("addr:conscriptionnumber", m_adresni_mista.get(i).getCisloDomovni()));
                } else {
                    // Cislo evidencni
                    c.append(tagToString("addr:provisionalnumber", m_adresni_mista.get(i).getCisloDomovni()));
                }

                // Cislo orientacni
                if (!m_adresni_mista.get(i).getCisloOrientacni().isEmpty()) {
                    c.append(tagToString("addr:streetnumber", m_adresni_mista.get(i).getCisloOrientacni()));
                }

                // Domovni cislo
                StringBuilder addr = new StringBuilder();
                if (!m_adresni_mista.get(i).getCisloTyp().equals("Číslo evidenční")) {
                    addr.append(m_adresni_mista.get(i).getCisloDomovni());
                } else {
                    addr.append("ev." + m_adresni_mista.get(i).getCisloDomovni());
                }
                if (!m_adresni_mista.get(i).getCisloOrientacni().isEmpty()) {
                    addr.append("/" + m_adresni_mista.get(i).getCisloOrientacni());
                }
                c.append(tagToString("addr:housenumber", addr.toString()));

                // Street
                if (!m_adresni_mista.get(i).getUlice().isEmpty()) {
                    c.append(tagToString("addr:street", m_adresni_mista.get(i).getUlice()));
                }
                //RUIAN ID
                if (m_adresni_mista.get(i).getRuianID() > 0) {
                    c.append(tagToString("ref:ruian:addr", Long.toString(m_adresni_mista.get(i).getRuianID())));
                }

                // Place
                if (!m_adresni_mista.get(i).getCastObce().isEmpty()) {
                    c.append(tagToString("addr:place", m_adresni_mista.get(i).getCastObce()));
                }

                if (!m_adresni_mista.get(i).getMestskaCast().isEmpty()) {
                    c.append(tagToString("addr:suburb", m_adresni_mista.get(i).getMestskaCast()));
                }

                // City
                if (!m_adresni_mista.get(i).getObec().isEmpty()) {
                    c.append(tagToString("addr:city", m_adresni_mista.get(i).getObec()));
                }

                // Postcode
                if (!m_adresni_mista.get(i).getPsc().isEmpty()) {
                    c.append(tagToString("addr:postcode", m_adresni_mista.get(i).getPsc()));
                }

                // Country
                c.append(tagToString("addr:country", "CZ"));

                // Source
                c.append(tagToString("source:addr", "cuzk:ruian"));
            }
        }

        // Copy parcel tags to clipboard

        // Copy street tags to clipboard
        if (keyType.equals("street") && m_ulice_ruian_id > 0) {
            c.append(tagToString("ref:ruian:street", Long.toString(m_ulice_ruian_id)));
            c.append(tagToString("name", m_ulice_jmeno));
            c.append(tagToString("source", "cuzk:ruian"));
        }

        return c.toString();
    }

    /**
     * Create new address poing on current location with given tags
     * @param cmd What to do: create on clicked position or on ruian position
     * @param t OSM tags in string
     */
    void createAddrPoint(String cmd, String t) {
        Collection<Command> commands = new LinkedList<>();
        Node node;
        if (cmd.startsWith("tags.create-on-place")) {
            String[] key = cmd.split(":");
            int i = Integer.parseInt(key[1]);
            node = new Node(m_adresni_mista.get(i).getPosition());
        } else {
            node = new Node(new LatLon(m_coor_lat, m_coor_lon));
        }
        commands.add(new AddCommand(node));

        Collection<OsmPrimitive> coll = new LinkedList<>();
        coll.add(node);

        TagCollection tc = new TagCollection();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(t.split("\n")));
        for (String line : list) {
            String[] tag = line.split("\"=\"");
            Main.info("<" + tag[0] + ">. <" + tag[1] +">");
            tc.add(new Tag(tag[0].substring(1), tag[1].substring(0, tag[1].length()-1)));
        }

        tc.applyTo(coll);

        Main.main.undoRedo.add(new SequenceCommand(tr("Add new address point"), commands));
    }

    /**
     * Perform given action
     *  e.g.: copy tags to clipboard
     * @param act Action to be performed
     */
    public void performAction(String act) {

        Main.info("act: " + act.substring(7));
        String[] params = act.substring(7).split("/");
        if (!params[0].equals("tags.copy") && !params[0].startsWith("tags.create")) {
            return;
        }

        String task = getKeys(params[1]);

        // Copy tags to clipboard
        if (params[0].equals("tags.copy")) {
            if (!task.isEmpty()) {
                ClipboardUtils.copyString(task);
                PointInfoUtils.showNotification(tr("Tags copied to clipboard."), "info");
            }
        }

        // Create address node
        if (params[0].startsWith("tags.create")) {
            if (!task.isEmpty()) {
                createAddrPoint(act.substring(7), task);
                PointInfoUtils.showNotification(tr("New address point added."), "info");
            }
        }
    }
}
