// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo.ruian;

import java.text.DecimalFormat;

import org.openstreetmap.josm.data.coor.LatLon;

/**
 * Private class to store address places
 * @author Marián Kyral
 */
class AddrPlaces {
    // CHECKSTYLE.OFF: SingleSpaceSeparator
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
    private float   m_vzdalenost;
    // CHECKSTYLE.ON: SingleSpaceSeparator

    AddrPlaces() {
        init();
    }

    private void init() {
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
        m_vzdalenost = 0;
    }

    public void setRuianID(long v) {
        m_ruian_id = v;
    }

    public void setPosition(LatLon v) {
        m_position = v;
    }

    public void setBudovaID(long v) {
        m_budova_id = v;
    }

    public void setCisloTyp(String v) {
        m_cislo_typ = v;
    }

    public void setCisloDomovni(String v) {
        m_cislo_domovni = v;
    }

    public void setCisloOrientacni(String v) {
        m_cislo_orientacni = v;
    }

    public void setUliceID(long v) {
        m_ulice_kod = v;
    }

    public void setUlice(String v) {
        m_ulice = v;
    }

    public void setCastObceID(long v) {
        m_cast_obce_kod = v;
    }

    public void setCastObce(String v) {
        m_cast_obce = v;
    }

    public void setMestskaCastID(long v) {
        m_mestska_cast_kod = v;
    }

    public void setMestskaCast(String v) {
        m_mestska_cast = v;
    }

    public void setObecID(long v) {
        m_obec_kod = v;
    }

    public void setObec(String v) {
        m_obec = v;
    }

    public void setOkresID(long v) {
        m_okres_kod = v;
    }

    public void setOkres(String v) {
        m_okres = v;
    }

    public void setKrajID(long v) {
        m_kraj_kod = v;
    }

    public void setKraj(String v) {
        m_kraj = v;
    }

    public void setPsc(String v) {
        m_psc = v;
    }

    public void setVzdalenost(float v) {
        m_vzdalenost = v;
    }

    public long getRuianID() {
        return m_ruian_id;
    }

    public long getBudovaID() {
        return m_budova_id;
    }

    public LatLon getPosition() {
        return m_position;
    }

    public String getCisloTyp() {
        return m_cislo_typ;
    }

    public String getCisloDomovni() {
        return m_cislo_domovni;
    }

    public String getCisloOrientacni() {
        return m_cislo_orientacni;
    }

    public long getUliceID() {
        return m_ulice_kod;
    }

    public String getUlice() {
        return m_ulice;
    }

    public long getCastObceID() {
        return m_cast_obce_kod;
    }

    public String getCastObce() {
        return m_cast_obce;
    }

    public long getMestskaCastID() {
        return m_mestska_cast_kod;
    }

    public String getMestskaCast() {
        return m_mestska_cast;
    }

    public long getObecID() {
        return m_obec_kod;
    }

    public String getObec() {
        return m_obec;
    }

    public long getOkresID() {
        return m_okres_kod;
    }

    public String getOkres() {
        return m_okres;
    }

    public long getKrajID() {
        return m_kraj_kod;
    }

    public String getKraj() {
        return m_kraj;
    }

    public String getPsc() {
        return m_psc;
    }

    public String getVzdalenost() {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(m_vzdalenost) + "m";
    }
}
