/**
 *  PointInfo - plugin for JOSM
 *  Marian Kyral
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openstreetmap.josm.plugins.pointinfo;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Utils;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;


import java.util.*;
import java.lang.StringBuilder;

/**
 * Private class to store address places
 *
 */

class addrPlaces {
    private long    m_ruian_id;
    private LatLon  m_position;
    private long    m_budova_id;
    private String  m_cislo_typ;
    private String  m_cislo_domovni;
    private String  m_cislo_orientacni;
    private long    m_ulice_kod;
    private String  m_ulice;
    private long    m_cast_obce_kod;
    private String  m_cast_obce;
    private long    m_mestska_cast_kod;
    private String  m_mestska_cast;
    private long    m_obec_kod;
    private String  m_obec;
    private long    m_okres_kod;
    private String  m_okres;
    private long    m_kraj_kod;
    private String  m_kraj;
    private String  m_psc;

    public addrPlaces () {
      init();
    }

    private void init () {
      m_ruian_id = 0;
      m_position = new LatLon(0., 0.);
      m_budova_id = 0;
      m_cislo_typ = "";
      m_cislo_domovni = "";
      m_cislo_orientacni = "";
      m_ulice_kod = 0;
      m_ulice = "";
      m_cast_obce_kod = 0;
      m_cast_obce = "";
      m_mestska_cast_kod = 0;
      m_mestska_cast = "";
      m_obec_kod = 0;
      m_obec = "";
      m_okres_kod = 0;
      m_okres = "";
      m_kraj_kod = 0;
      m_kraj = "";
      m_psc = "";
    }

    public void setRuianID (long v) {
      m_ruian_id = v;
    }

    public void setPosition (LatLon v) {
      m_position = v;
    }

    public void setBudovaID (long v) {
      m_budova_id = v;
    }

    public void setCisloTyp (String v) {
      m_cislo_typ = v;
    }

    public void setCisloDomovni (String v) {
      m_cislo_domovni = v;
    }

    public void setCisloOrientacni (String v) {
      m_cislo_orientacni = v;
    }

    public void setUliceID (long v) {
      m_ulice_kod = v;
    }

    public void setUlice (String v) {
      m_ulice = v;
    }

    public void setCastObceID (long v) {
      m_cast_obce_kod = v;
    }

    public void setCastObce (String v) {
      m_cast_obce = v;
    }

    public void setMestskaCastID (long v) {
      m_mestska_cast_kod = v;
    }

    public void setMestskaCast (String v) {
      m_mestska_cast = v;
    }

    public void setObecID (long v) {
      m_obec_kod = v;
    }

    public void setObec (String v) {
      m_obec = v;
    }

    public void setOkresID (long v) {
      m_okres_kod = v;
    }

    public void setOkres (String v) {
      m_okres = v;
    }

    public void setKrajID (long v) {
      m_kraj_kod = v;
    }

    public void setKraj (String v) {
      m_kraj = v;
    }

    public void setPsc (String v) {
      m_psc = v;
    }

    public long getRuianID () {
      return m_ruian_id;
    }

    public long getBudovaID () {
      return m_budova_id;
    }

    public LatLon getPosition () {
      return m_position;
    }

    public String getCisloTyp () {
      return m_cislo_typ;
    }

    public String getCisloDomovni () {
      return m_cislo_domovni;
    }

    public String getCisloOrientacni () {
      return m_cislo_orientacni;
    }

    public long getUliceID () {
      return m_ulice_kod;
    }

    public String getUlice () {
      return m_ulice;
    }

    public long getCastObceID () {
      return m_cast_obce_kod;
    }

    public String getCastObce () {
      return m_cast_obce;
    }

    public long getMestskaCastID () {
      return m_mestska_cast_kod;
    }

    public String getMestskaCast () {
      return m_mestska_cast;
    }

    public long getObecID () {
      return m_obec_kod;
    }

    public String getObec () {
      return m_obec;
    }

    public long getOkresID () {
      return m_okres_kod;
    }

    public String getOkres () {
      return m_okres;
    }

    public long getKrajID () {
      return m_kraj_kod;
    }

    public String getKraj () {
      return m_kraj;
    }

    public String getPsc () {
      return m_psc;
    }

}

/**
 * Private class contains RUIAN data
 *
 */

class ruianRecord {

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

    private ArrayList <addrPlaces> m_adresni_mista;

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


    /**
    * Constructor
    *
    */
    public ruianRecord () {
      init();
    }

    /**
    * Initialization
    *
    */
    private void init () {

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

      m_adresni_mista = new ArrayList<addrPlaces> ();

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
    *
    */
    public void parseJSON (String jsonStr) {


      init();

      JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(jsonStr.getBytes()));
      JsonObject obj = jsonReader.readObject();
      jsonReader.close();

      try {
        JsonObject coorObjekt = obj.getJsonObject("coordinates");

        try {
          m_coor_lat = Double.parseDouble(coorObjekt.getString("lat"));
        } catch (Exception e) {
          System.out.println("coordinates.lat: " + e.getMessage());
        }

        try {
          m_coor_lon = Double.parseDouble(coorObjekt.getString("lon"));
        } catch (Exception e) {
          System.out.println("coordinates.lon: " + e.getMessage());
        }

        try {
          m_source = obj.getString("source");
        } catch (Exception e) {
          System.out.println("source: " + e.getMessage());
        }

      } catch (Exception e) {
        System.out.println("coordinates: " + e.getMessage());
      }

// =========================================================================
      try {
        JsonObject stavebniObjekt = obj.getJsonObject("stavebni_objekt");

        try {
          m_objekt_ruian_id = Long.parseLong(stavebniObjekt.getString("ruian_id"));
        } catch (Exception e) {
          System.out.println("stavebni_objekt.ruian_id: " + e.getMessage());
        }

        try {
          m_objekt_podlazi = Integer.parseInt(stavebniObjekt.getString("pocet_podlazi"));
        } catch (Exception e) {
          System.out.println("stavebni_objekt.pocet_podlazi: " + e.getMessage());
        }

        try {
          m_objekt_byty = Integer.parseInt(stavebniObjekt.getString("pocet_bytu"));
        } catch (Exception e) {
          System.out.println("stavebni_objekt.pocet_bytu: " + e.getMessage());
        }

        try {
          m_objekt_zpusob_vyuziti = stavebniObjekt.getString("zpusob_vyuziti");
        } catch (Exception e) {
          System.out.println("stavebni_objekt.zpusob_vyuziti: " + e.getMessage());
        }

        try {
          m_objekt_zpusob_vyuziti_kod = stavebniObjekt.getString("zpusob_vyuziti_kod");
        } catch (Exception e) {
          System.out.println("stavebni_objekt.m_objekt_zpusob_vyuziti_kod: " + e.getMessage());
        }

        try {
          m_objekt_zpusob_vyuziti_key = stavebniObjekt.getString("zpusob_vyuziti_key");
        } catch (Exception e) {
          System.out.println("stavebni_objekt.zpusob_vyuziti_key: " + e.getMessage());
        }

        try {
          m_objekt_zpusob_vyuziti_val = stavebniObjekt.getString("zpusob_vyuziti_val");
        } catch (Exception e) {
          System.out.println("stavebni_objekt.m_objekt_zpusob_vyuziti_val: " + e.getMessage());
        }

        try {
          m_objekt_plati_od = stavebniObjekt.getString("plati_od");
        } catch (Exception e) {
          System.out.println("stavebni_objekt.plati_od: " + e.getMessage());
        }

        try {
          m_objekt_dokonceni = stavebniObjekt.getString("dokonceni");
        } catch (Exception e) {
          System.out.println("stavebni_objekt.dokonceni: " + e.getMessage());
        }

      } catch (Exception e) {
        System.out.println("stavebni_objekt: " + e.getMessage());
      }

// =========================================================================
      try {
        JsonArray arr = obj.getJsonArray("adresni_mista");

        for(int i = 0; i < arr.size(); i++)
        {
          JsonObject adresniMisto = arr.getJsonObject(i);
          addrPlaces am = new addrPlaces();

          try {
            am.setRuianID(Long.parseLong(adresniMisto.getString("ruian_id")));
          } catch (Exception e) {
            System.out.println("adresni_mista.ruian_id: " + e.getMessage());
          }

          try {
            JsonArray node = adresniMisto.getJsonArray("pozice");
            am.setPosition(new LatLon(
              LatLon.roundToOsmPrecisionStrict(node.getJsonNumber(1).doubleValue()),
              LatLon.roundToOsmPrecisionStrict(node.getJsonNumber(0).doubleValue()))
            );
          } catch (Exception e) {
            System.out.println("adresni_mista.pozice: " + e.getMessage());
          }

          try {
            am.setBudovaID(Long.parseLong(adresniMisto.getString("budova_kod")));
          } catch (Exception e) {
            System.out.println("adresni_mista.budova_kod: " + e.getMessage());
          }

          try {
            am.setCisloTyp(adresniMisto.getString("cislo_typ"));
          } catch (Exception e) {
            System.out.println("adresni_mista.cislo_typ: " + e.getMessage());
          }

          try {
            am.setCisloDomovni(adresniMisto.getString("cislo_domovni"));
          } catch (Exception e) {
            System.out.println("adresni_mista.cislo_domovni: " + e.getMessage());
          }

          try {
            am.setCisloOrientacni(adresniMisto.getString("cislo_orientacni"));
          } catch (Exception e) {
            System.out.println("adresni_mista.cislo_orientacni: " + e.getMessage());
          }

          try {
            am.setUliceID(Long.parseLong(adresniMisto.getString("ulice_kod")));
          } catch (Exception e) {
            System.out.println("adresni_mista.ulice_kod: " + e.getMessage());
          }

          try {
            am.setUlice(adresniMisto.getString("ulice"));
          } catch (Exception e) {
            System.out.println("adresni_mista.ulice: " + e.getMessage());
          }

          try {
            am.setCastObceID(Long.parseLong(adresniMisto.getString("cast_obce_kod")));
          } catch (Exception e) {
            System.out.println("adresni_mista.cast_obce_kod: " + e.getMessage());
          }

          try {
            am.setCastObce(adresniMisto.getString("cast_obce"));
          } catch (Exception e) {
            System.out.println("adresni_mista.m_cast_obce: " + e.getMessage());
          }

          try {
            am.setMestskaCastID(Long.parseLong(adresniMisto.getString("mestska_cast_kod")));
          } catch (Exception e) {
            System.out.println("adresni_mista.mestska_cast_kod: " + e.getMessage());
          }

          try {
            am.setMestskaCast(adresniMisto.getString("mestska_cast"));
          } catch (Exception e) {
            System.out.println("adresni_mista.mestska_cast: " + e.getMessage());
          }

          try {
            am.setObecID(Long.parseLong(adresniMisto.getString("obec_kod")));
          } catch (Exception e) {
            System.out.println("adresni_mista.obec:_kod " + e.getMessage());
          }

          try {
            am.setObec(adresniMisto.getString("obec"));
          } catch (Exception e) {
            System.out.println("adresni_mista.obec: " + e.getMessage());
          }

          try {
            am.setOkresID(Long.parseLong(adresniMisto.getString("okres_kod")));
          } catch (Exception e) {
            System.out.println("adresni_mista.okres_kod: " + e.getMessage());
          }

          try {
            am.setOkres(adresniMisto.getString("okres"));
          } catch (Exception e) {
            System.out.println("adresni_mista.okres: " + e.getMessage());
          }

          try {
            am.setKrajID(Long.parseLong(adresniMisto.getString("kraj_kod")));
          } catch (Exception e) {
            System.out.println("adresni_mista.kraj_kod: " + e.getMessage());
          }

          try {
            am.setKraj(adresniMisto.getString("kraj"));
          } catch (Exception e) {
            System.out.println("adresni_mista.kraj: " + e.getMessage());
          }

          try {
            am.setPsc(adresniMisto.getString("psc"));
          } catch (Exception e) {
            System.out.println("adresni_mista.psc: " + e.getMessage());
          }

          m_adresni_mista.add(am);
        }
      } catch (Exception e) {
        System.out.println("adresni_mista: " + e.getMessage());
      }

// =========================================================================
      try {
        JsonObject parcela = obj.getJsonObject("parcela");

        try {
          m_parcela_ruian_id = Long.parseLong(parcela.getString("ruian_id"));
        } catch (Exception e) {
          System.out.println("parcela.ruian_id: " + e.getMessage());
        }

        try {
          m_parcela_druh_pozemku = parcela.getString("druh_pozemku");
        } catch (Exception e) {
          System.out.println("parcela.druh_pozemku: " + e.getMessage());
        }

        try {
          m_parcela_zpusob_vyuziti = parcela.getString("zpusob_vyuziti");
        } catch (Exception e) {
          System.out.println("parcela.zpusob_vyuziti: " + e.getMessage());
        }

        try {
          m_parcela_plati_od = parcela.getString("plati_od");
        } catch (Exception e) {
          System.out.println("parcela.plati_od: " + e.getMessage());
        }

      } catch (Exception e) {
        System.out.println("parcela: " + e.getMessage());
      }

// =========================================================================
      try {
        JsonObject ulice = obj.getJsonObject("ulice");

        try {
          m_ulice_ruian_id = Long.parseLong(ulice.getString("ruian_id"));
        } catch (Exception e) {
          System.out.println("ulice.ruian_id: " + e.getMessage());
        }

        try {
          m_ulice_jmeno = ulice.getString("jmeno");
        } catch (Exception e) {
          System.out.println("ulice.jmeno: " + e.getMessage());
        }

      } catch (Exception e) {
        System.out.println("ulice: " + e.getMessage());
      }

// =========================================================================
      try {
        JsonObject katastr = obj.getJsonObject("katastr");

        try {
          m_katastr_ruian_id = Long.parseLong(katastr.getString("ruian_id"));
        } catch (Exception e) {
          System.out.println("katastr.ruian_id: " + e.getMessage());
        }

        try {
          m_katastr_nazev = katastr.getString("nazev");
        } catch (Exception e) {
          System.out.println("katastr.nazev: " + e.getMessage());
        }

        try {
          m_katastr_obec_kod = Long.parseLong(katastr.getString("obec_kod"));
        } catch (Exception e) {
          System.out.println("katastr.obec_kod: " + e.getMessage());
        }

        try {
          m_katastr_obec = katastr.getString("obec");
        } catch (Exception e) {
          System.out.println("katastr.okres: " + e.getMessage());
        }

        try {
          m_katastr_okres_kod = Long.parseLong(katastr.getString("okres_kod"));
        } catch (Exception e) {
          System.out.println("katastr.okres_kod: " + e.getMessage());
        }

        try {
          m_katastr_okres = katastr.getString("okres");
        } catch (Exception e) {
          System.out.println("katastr.okres: " + e.getMessage());
        }

        try {
          m_katastr_kraj_kod = Long.parseLong(katastr.getString("kraj_kod"));
        } catch (Exception e) {
          System.out.println("katastr.kraj_kod: " + e.getMessage());
        }

        try {
          m_katastr_kraj = katastr.getString("kraj");
        } catch (Exception e) {
          System.out.println("katastr.kraj: " + e.getMessage());
        }

      } catch (Exception e) {
        System.out.println("katastr: " + e.getMessage());
      }
    }

    /**
     * Return Html text representation
     * @return String htmlText
     */
    public String getHtml () {

      String icon_ext_link = "<img src=" +getClass().getResource("/images/dialogs/open-external-link.png")+" border=0 alt=\"Zobrazit na stránkách RUIAN\"/>";
      String icon_copy_tags = "<img src=" +getClass().getResource("/images/dialogs/copy-tags.png")+" border=0 alt=\"Zobrazit na stránkách RUIAN\"/>";
      String icon_create_addr = "<img src=" +getClass().getResource("/images/dialogs/create-addr.png")+" border=0 alt=\"Zobrazit na stránkách RUIAN\"/>";
      String icon_create_addr_ruian = "<img src=" +getClass().getResource("/images/dialogs/create-addr-ruian.png")+" border=0 alt=\"Zobrazit na stránkách RUIAN\"/>";

      String url_cpost = "http://www.postaonline.cz/vyhledani-psc?p_p_id=psc_WAR_pcpvpp&p_p_lifecycle=1&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&_psc_WAR_pcpvpp_struts.portlet.action=%2Fview%2FdetailPost&_psc_WAR_pcpvpp_struts.portlet.mode=view&_psc_WAR_pcpvpp_zipCode=";
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

      StringBuilder r = new StringBuilder();

      if (m_objekt_ruian_id == 0 &&
          m_parcela_ruian_id == 0 &&
          m_adresni_mista.size() == 0 &&
          m_ulice_ruian_id == 0 &&
          m_katastr_ruian_id == 0 )
        return "";

      r.append("<html>");
      r.append("<br/>");
      if (m_objekt_ruian_id > 0) {
        r.append("<i><u>Informace o budově</u></i><br/>");
        r.append("<b>RUIAN id: </b>"+ m_objekt_ruian_id +"&nbsp;&nbsp;<a href="+ url_stavebni_objekt + m_objekt_ruian_id +">" + icon_ext_link + "</a>");
        r.append("&nbsp;&nbsp;<a href=file://tags.copy/building>"+ icon_copy_tags +"</a><br/>");
        if (m_adresni_mista.size() == 0 ) r.append("<b>Budova: </b> bez č.p./č.e<br/>");
        else if (m_adresni_mista.get(0).getCisloTyp().equals("Číslo popisné")) r.append("<b>Budova: </b>s číslem popisným<br/>");
          else r.append("<b>Budova: </b>s číslem evidenčním<br/>");
        if (m_objekt_podlazi > 0) r.append("<b>Počet podlaží: </b>" + m_objekt_podlazi + "<br/>");
        if (m_objekt_byty > 0) r.append("<b>Počet bytů: </b>" + m_objekt_byty + "<br/>");
        r.append("<b>Způsob využití: </b>" + m_objekt_zpusob_vyuziti + "<br/>");
        r.append("<b>Datum dokončení: </b>" + m_objekt_dokonceni + "<br/>");
        r.append("<b>Platí od: </b>" + m_objekt_plati_od + "<br/>");
        r.append("<br/>");

        if (m_adresni_mista.size() > 1) {
          r.append("<i><u>Informace o adrese</u></i><br/>");
          // More address places
          int i = 0;
          r.append("<b>" + m_adresni_mista.get(i).getCisloTyp() + "</b> (více adres)<b>: </b>" + m_adresni_mista.get(i).getCisloDomovni() + "<br/>");
          r.append("<b>Část obce: </b>" + m_adresni_mista.get(i).getCastObce());
            r.append("&nbsp;&nbsp;<a href="+ url_mistni_cast + m_adresni_mista.get(i).getCastObceID() +">" + icon_ext_link + "</a><br/>");
          if (m_adresni_mista.get(i).getMestskaCast().length() > 0) {
            r.append("<b>Městská část: </b>" + m_adresni_mista.get(i).getMestskaCast());
              r.append("&nbsp;&nbsp;<a href="+ url_mestska_cast + m_adresni_mista.get(i).getMestskaCastID() +">" + icon_ext_link + "</a><br/>");
          }
          r.append("<b>Obec: </b>" + m_adresni_mista.get(i).getObec());
            r.append("&nbsp;&nbsp;<a href="+ url_obec + m_adresni_mista.get(i).getObecID() +">" + icon_ext_link + "</a><br/>");
          r.append("<b>Okres: </b>" + m_adresni_mista.get(i).getOkres());
            r.append("&nbsp;&nbsp;<a href="+ url_okres + m_adresni_mista.get(i).getOkresID() +">" + icon_ext_link + "</a><br/>");
          r.append("<b>Kraj: </b>" + m_adresni_mista.get(i).getKraj());
            r.append("&nbsp;&nbsp;<a href="+ url_kraj + m_adresni_mista.get(i).getKrajID() +">" + icon_ext_link + "</a><br/>");

        } else if (m_adresni_mista.size() == 1 && (m_adresni_mista.get(0).getCisloDomovni() == null || m_adresni_mista.get(0).getCisloDomovni().isEmpty())) {
          // Without building number
          int i = 0;
          r.append("<i><u>Informace o adrese</u></i><br/>");
          r.append("<b>Budova: </b>" + m_adresni_mista.get(i).getCisloTyp() + "<br/>");
          if (m_adresni_mista.get(i).getMestskaCast().length() > 0) {
            r.append("<b>Městská část: </b>" + m_adresni_mista.get(i).getMestskaCast());
              r.append("&nbsp;&nbsp;<a href="+ url_mestska_cast + m_adresni_mista.get(i).getMestskaCastID() +">" + icon_ext_link + "</a><br/>");
          }
          r.append("<b>Obec: </b>" + m_adresni_mista.get(i).getObec());
            r.append("&nbsp;&nbsp;<a href="+ url_obec + m_adresni_mista.get(i).getObecID() +">" + icon_ext_link + "</a><br/>");
          r.append("<b>Okres: </b>" + m_adresni_mista.get(i).getOkres());
            r.append("&nbsp;&nbsp;<a href="+ url_okres + m_adresni_mista.get(i).getOkresID() +">" + icon_ext_link + "</a><br/>");
          r.append("<b>Kraj: </b>" + m_adresni_mista.get(i).getKraj());
            r.append("&nbsp;&nbsp;<a href="+ url_kraj + m_adresni_mista.get(i).getKrajID() +">" + icon_ext_link + "</a><br/>");

        } else if (m_adresni_mista.size() == 1) {
          // Only one address place
          int i = 0;
          String x = "";
          String x_name = "";
          if ( ! m_adresni_mista.get(i).getCisloOrientacni().isEmpty()) {
            x = "/" + m_adresni_mista.get(i).getCisloOrientacni();
            x_name = "/orientační";
          }
          r.append("<i><u>Informace o adrese</u></i><br/>");
          r.append("<b>RUIAN id: </b>"+ m_adresni_mista.get(i).getRuianID() +"&nbsp;&nbsp;<a href="+ url_adresni_misto + m_adresni_mista.get(i).getRuianID() +">" + icon_ext_link + "</a>");
          r.append("&nbsp;&nbsp;<a href=file://tags.copy/address:"+i+">"+ icon_copy_tags +"</a>");
          r.append("&nbsp;&nbsp;<a href=file://tags.create/address:"+i+">"+ icon_create_addr +"</a>");
          r.append("&nbsp;&nbsp;<a href=file://tags.create-on-place/address:"+i+">"+ icon_create_addr_ruian +"</a><br/>");
          r.append("<b>" + m_adresni_mista.get(i).getCisloTyp() + x_name + ": </b>" + m_adresni_mista.get(i).getCisloDomovni() + x);
          r.append("<br/>");
          if (!m_adresni_mista.get(i).getUlice().isEmpty()) {
            r.append("<b>Ulice: </b>" + m_adresni_mista.get(i).getUlice());
              r.append("&nbsp;&nbsp;<a href="+ url_ulice + m_adresni_mista.get(i).getUliceID() +">" + icon_ext_link + "</a><br/>");
          }
          r.append("<b>Část obce: </b>" + m_adresni_mista.get(i).getCastObce());
            r.append("&nbsp;&nbsp;<a href="+ url_mistni_cast + m_adresni_mista.get(i).getCastObceID() +">" + icon_ext_link + "</a><br/>");
          if (m_adresni_mista.get(i).getMestskaCast().length() > 0) {
            r.append("<b>Městská část: </b>" + m_adresni_mista.get(i).getMestskaCast());
              r.append("&nbsp;&nbsp;<a href="+ url_mestska_cast + m_adresni_mista.get(i).getMestskaCastID() +">" + icon_ext_link + "</a><br/>");
          }
          r.append("<b>Obec: </b>" + m_adresni_mista.get(i).getObec());
            r.append("&nbsp;&nbsp;<a href="+ url_obec + m_adresni_mista.get(i).getObecID() +">" + icon_ext_link + "</a><br/>");
          r.append("<b>Okres: </b>" + m_adresni_mista.get(i).getOkres());
            r.append("&nbsp;&nbsp;<a href="+ url_okres + m_adresni_mista.get(i).getOkresID() +">" + icon_ext_link + "</a><br/>");
          r.append("<b>Kraj: </b>" + m_adresni_mista.get(i).getKraj());
            r.append("&nbsp;&nbsp;<a href="+ url_kraj + m_adresni_mista.get(i).getKrajID() +">" + icon_ext_link + "</a><br/>");
          r.append("<b>PSČ: </b>" + m_adresni_mista.get(i).getPsc());
            r.append("&nbsp;&nbsp;<a href="+ url_cpost + m_adresni_mista.get(i).getPsc() +">" + icon_ext_link + "</a><br/>");

        }
        r.append("<br/>");
      }
      if (m_adresni_mista.size() > 1 && m_objekt_ruian_id > 0) {
        String x = "";
        if (m_adresni_mista.get(0).getCisloTyp().equals("Číslo evidenční")) {
          x = "ev.";
        }
        r.append("<i><u>Adresní místa</u></i><br/>");
        for (int i=0; i<m_adresni_mista.size(); i++) {
//           r.append(m_adresni_mista.get(i).getRuianID());
          r.append(m_adresni_mista.get(i).getUlice() + " " + x + m_adresni_mista.get(i).getCisloDomovni());
          if (!m_adresni_mista.get(i).getCisloOrientacni().isEmpty()) {
            r.append("/" + m_adresni_mista.get(i).getCisloOrientacni());
          }
          r.append("&nbsp;&nbsp;<a href="+ url_adresni_misto + m_adresni_mista.get(i).getRuianID() + ">"+ icon_ext_link +"</a> ");
          r.append("&nbsp;&nbsp;<a href=file://tags.copy/address:"+i+">"+ icon_copy_tags +"</a>");
          r.append("&nbsp;&nbsp;<a href=file://tags.create/address:"+i+">"+ icon_create_addr +"</a>");
          r.append("&nbsp;&nbsp;<a href=file://tags.create-on-place/address:"+i+">"+ icon_create_addr_ruian +"</a>");
          r.append("<br/>");
        }
        r.append("<br/>");
      }

      // Land
      if (m_parcela_ruian_id > 0) {
        r.append("<i><u>Informace o pozemku</u></i>");
        r.append("<br/>");
        r.append("<b>RUIAN id: </b>"+ m_parcela_ruian_id +"&nbsp;&nbsp;<a href="+ url_parcela + m_parcela_ruian_id +">" + icon_ext_link + "</a><br/>");
//         r.append("&nbsp;&nbsp;<a href=file://tags.copy/parcel>"+ icon_copy_tags +"</a>");
        r.append("<b>Druh pozemku: </b>" + m_parcela_druh_pozemku +"<br/>");
        if (m_parcela_zpusob_vyuziti != "") r.append("<b>Způsob využití: </b>" + m_parcela_zpusob_vyuziti +"<br/>");
        r.append("<b>Platí od: </b>" + m_parcela_plati_od +"<br/>");
        r.append("<br/>");
      }

      // Street
      if (m_ulice_ruian_id > 0) {
        r.append("<i><u>Informace o ulici</u></i><br/>");
        r.append("<b>RUIAN id: </b>"+ m_ulice_ruian_id +"&nbsp;&nbsp;<a href="+ url_ulice + m_ulice_ruian_id +">" + icon_ext_link + "</a>");
        r.append("&nbsp;&nbsp;<a href=file://tags.copy/street>"+ icon_copy_tags +"</a><br/>");
        r.append("<b>Jméno: </b>" + m_ulice_jmeno +"<br/>");
        r.append("<br/>");
      }

      // Cadastral area
      if (m_katastr_ruian_id > 0) {
        r.append("<b>Katastrální území: </b>" + m_katastr_nazev);
        r.append("&nbsp;&nbsp;<a href="+ url_katastralni_uzemi + m_katastr_ruian_id +">" + icon_ext_link + "</a><br/>");
        r.append("<b>Obec: </b>" + m_katastr_obec);
        r.append("&nbsp;&nbsp;<a href="+ url_obec + m_katastr_obec_kod +">" + icon_ext_link + "</a><br/>");
        r.append("<b>Okres: </b>" + m_katastr_okres);
        r.append("&nbsp;&nbsp;<a href="+ url_okres + m_katastr_okres_kod +">" + icon_ext_link + "</a><br/>");
        r.append("<b>Kraj: </b>" + m_katastr_kraj);
        r.append("&nbsp;&nbsp;<a href="+url_kraj + m_katastr_kraj_kod +">" + icon_ext_link + "</a><br/>");
        r.append("<br/>");
      }

      // Near address places
      if (m_adresni_mista.size() > 0 && m_objekt_ruian_id == 0) {
        String x, x_name;
        r.append("<i><u>Adresní místa v okolí</u></i><br/>");
        r.append("<table>");
        for (int i=0; i<m_adresni_mista.size(); i++) {
          x = "";
          x_name = "";
          if (m_adresni_mista.get(i).getCisloTyp().equals("Číslo evidenční")) {
            x = "ev.";
          }
          x += m_adresni_mista.get(i).getCisloDomovni();
          if ( !m_adresni_mista.get(i).getCisloOrientacni().isEmpty()) {
            x += "/" + m_adresni_mista.get(i).getCisloOrientacni();
            x_name += "/orientační";
          }
          r.append("<tr><td bgcolor=#e5e5ff>");
          if (!m_adresni_mista.get(i).getUlice().isEmpty()) {
            r.append(m_adresni_mista.get(i).getUlice() + " " + x);
            r.append("<br/><u>" + m_adresni_mista.get(i).getObec() + "</u>");
            r.append("</td><td valign=\"top\"  bgcolor=#e5e5ff>");
            r.append("<a href="+ url_adresni_misto + m_adresni_mista.get(i).getRuianID() + ">"+ icon_ext_link +"</a>");
            r.append("&nbsp;&nbsp;<a href=file://tags.copy/address:"+i+">"+ icon_copy_tags +"</a>");
            r.append("&nbsp;&nbsp;<a href=file://tags.create/address:"+i+">"+ icon_create_addr +"</a>");
            r.append("&nbsp;&nbsp;<a href=file://tags.create-on-place/address:"+i+">"+ icon_create_addr_ruian +"</a>");
          } else {
            r.append(m_adresni_mista.get(i).getCastObce() + " " + x + "&nbsp;");
            if (!m_adresni_mista.get(i).getCastObce().equals(m_adresni_mista.get(i).getObec())) {
              r.append("<br/><u>" + m_adresni_mista.get(i).getObec() + "</u>");
            }
            r.append("</td><td valign=\"top\"  bgcolor=#e5e5ff>");
            r.append("<a href="+ url_adresni_misto + m_adresni_mista.get(i).getRuianID() + ">"+ icon_ext_link +"</a>");
            r.append("&nbsp;&nbsp;<a href=file://tags.copy/address:"+i+">"+ icon_copy_tags +"</a>");
            r.append("&nbsp;&nbsp;<a href=file://tags.create/address:"+i+">"+ icon_create_addr +"</a>");
            r.append("&nbsp;&nbsp;<a href=file://tags.create-on-place/address:"+i+">"+ icon_create_addr_ruian +"</a>");
          }
          r.append("</td></tr>");
        }
        r.append("</table><br/>");
      }
      r.append("<hr/>");
      r.append("<center><i><small>Zdroj: <a href=\"http://www.ruian.cz/\">" + m_source + "</a></small></i></center>");
      r.append("</html>");

      return r.toString();
    }

    /**
     * Convert date from Czech to OSM format
     * @param ruianDate Date in RUIAN (Czech) format DD.MM.YYYY
     * @return String with date converted to OSM data format YYYY-MM-DD
     */
    String convertDate (String ruianDate) {
      String r = new String();
      String[] parts = ruianDate.split("\\.");
      try {
        int day =   Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year =  Integer.parseInt(parts[2]);
        r = new Integer(year).toString() + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);
      } catch (Exception e) {
      }

      return r;
    }

    /**
     * Construct tag string for clipboard
     * @param k OSM Key
     * @param v OSM Value
     * @return String OSM tag string for clipboard
     */
    String tagToString (String k, String v) {
      String r = "\"" + k + "\"=\"" + v + "\"\n";
      return r;
    }

    /**
     * Prepare OSM keys
     * @param keyType What to prepare (building, address, parcel. street)
     * @return String with OSM tags
     */
    String getKeys (String keyType) {
      StringBuilder c = new StringBuilder();

      // Copy building tags to clipboard
      if (keyType.equals("building") && m_objekt_ruian_id > 0) {
        c.append(tagToString("ref:ruian:building", Long.toString(m_objekt_ruian_id)));
        if (m_objekt_zpusob_vyuziti_key.length() > 0 &&
            m_objekt_zpusob_vyuziti_val.length() > 0
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

      // Copy address tags to clipboard
      if (keyType.startsWith("address")) {
        if (m_adresni_mista.size() > 0) {
          int i;

          if (m_adresni_mista.size() == 0) {
            i = 0;
          } else {
            String[] key = keyType.split(":");
            i = new Integer(key[1]);
            System.out.println("Address ID: " + i);
          }

          // Only one address place
          if (! m_adresni_mista.get(i).getCisloTyp().equals("Číslo evidenční")) {
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
          if (! m_adresni_mista.get(i).getCisloTyp().equals("Číslo evidenční")) {
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
    void createAddrPoint (String cmd, String t) {
      Collection<Command> commands = new LinkedList<Command>();
      Node node;
      if (cmd.startsWith("tags.create-on-place")) {
        String[] key = cmd.split(":");
        int i = new Integer(key[1]);
        node = new Node(m_adresni_mista.get(i).getPosition());
      } else {
        node = new Node(new LatLon(m_coor_lat, m_coor_lon));
      }
      commands.add(new AddCommand(node));

      Collection<OsmPrimitive> coll = new LinkedList<OsmPrimitive>();
      coll.add(node);

      TagCollection tc = new TagCollection();
      ArrayList <String> list = new ArrayList<String>(Arrays.asList(t.split("\n")));
      for (String line : list) {
        String[] tag = line.split("\"=\"");
        System.out.println("<" + tag[0] + ">. <" + tag[1] +">");
        tc.add(new Tag(tag[0].substring(1), tag[1].substring(0,tag[1].length()-1)));
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

      System.out.println("act: " + act.substring(7));
      String[] params = act.substring(7).split("/");
      if (!params[0].equals("tags.copy") && !params[0].startsWith("tags.create")) {
        return;
      }

      String task = getKeys(params[1]);

      // Copy tags to clipboard
      if (params[0].equals("tags.copy")) {
        if (task.length() > 0) {
          Utils.copyToClipboard(task);
          PointInfoUtils.showNotification(tr("Tags copied to clipboard."), "info");
        }
      }

      // Create address node
      if (params[0].startsWith("tags.create")) {
        if (task.length() > 0) {
          createAddrPoint(act.substring(7), task);
          PointInfoUtils.showNotification(tr("New address point added."), "info");
        }
      }
    }

}

/**
 * An module for the Czech RUIAN database
 *
 */
public class ruianModule {

    private String m_text = "";
    private String URL = "http://josm.poloha.net/pointInfo/v2/index.php";
    protected PointInfoServer server = new PointInfoServer();

    private ruianRecord m_record = new ruianRecord();

    public ruianModule() {

    }

    /**
     * Return Html text representation
     * @return String htmlText
     */
    public String getHtml() {

      return m_record.getHtml();
    }

    /**
     * Perform given action
     *  e.g.: copy tags to clipboard
     * @param act Action to be performed
     */
    public void performAction(String act) {

      m_record.performAction(act);
    }

    /**
     * Get a information about given position from RUIAN database.
     * @param pos Position on the map
     */
    public void prepareData(LatLon pos) {
        try {

             String request = URL + "?lat=" + pos.lat() + "&lon=" + pos.lon();
             System.out.println("Request: "+ request);
             String content = server.callServer(request);
             System.out.println("Reply: " + content);
             m_record.parseJSON(content);
        } catch (Exception e) {

        }
    }
}
