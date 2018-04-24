// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo.catastro;

import java.text.DecimalFormat;

class ObjectWithoutGeometry {
    // CHECKSTYLE.OFF: SingleSpaceSeparator
    private long    m_ruian_id;
    private int     m_podlazi;
    private int     m_byty;
    private String  m_zpusob_vyuziti;
    private String  m_zpusob_vyuziti_kod;
    private String  m_zpusob_vyuziti_key;
    private String  m_zpusob_vyuziti_val;
    private String  m_dokonceni;
    private String  m_plati_od;
    private float   m_vzdalenost;
    // CHECKSTYLE.ON: SingleSpaceSeparator

    ObjectWithoutGeometry() {
        init();
    }

    private void init() {
        m_ruian_id = 0;
        m_podlazi = 0;
        m_byty = 0;
        m_zpusob_vyuziti = "";
        m_zpusob_vyuziti_kod = "";
        m_zpusob_vyuziti_key = "";
        m_zpusob_vyuziti_val = "";
        m_dokonceni = "";
        m_plati_od = "";
        m_vzdalenost = 0;
    }

    public void setRuianID(long v) {
        m_ruian_id = v;
    }

    public void setPodlazi(int v) {
        m_podlazi = v;
    }

    public void setByty(int v) {
        m_byty = v;
    }

    public void setZpusobVyuziti(String v) {
        m_zpusob_vyuziti = v;
    }

    public void setZpusobVyuzitiKod(String v) {
        m_zpusob_vyuziti_kod = v;
    }

    public void setZpusobVyuzitiKey(String v) {
        m_zpusob_vyuziti_key = v;
    }

    public void setZpusobVyuzitiVal(String v) {
        m_zpusob_vyuziti_val = v;
    }

    public void setDokonceni(String v) {
        m_dokonceni = v;
    }

    public void setPlatiOd(String v) {
        m_plati_od = v;
    }

    public void setVzdalenost(float v) {
        m_vzdalenost = v;
    }

    public long getRuianID() {
        return m_ruian_id;
    }

    public int getPodlazi() {
        return m_podlazi;
    }

    public int getByty() {
        return m_byty;
    }

    public String getZpusobVyuziti() {
        return m_zpusob_vyuziti;
    }

    public String getZpusobVyuzitiKod() {
        return m_zpusob_vyuziti_kod;
    }

    public String getZpusobVyuzitiKey() {
        return m_zpusob_vyuziti_key;
    }

    public String getZpusobVyuzitiVal() {
        return m_zpusob_vyuziti_val;
    }

    public String getDokonceni() {
        return m_dokonceni;
    }

    public String getPlatiOd() {
        return m_plati_od;
    }

    public String getVzdalenost() {
        return new DecimalFormat("0.00").format(m_vzdalenost) + "m";
    }
}
