package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets;

public enum ToulouseMunicipalities {

    AIGREFEUILLE(31003),
    AUCAMVILLE(31022),
    AUSSONNE(31032),
    BALMA(31044),
    BEAUPUY(31053),
    BEAUZELLE(31056),
    BLAGNAC(31069),
    BRAX(31088),
    BRUGUIERES(31091),
    CASTELGINEST(31116),
    COLOMIERS(31149),
    CORNEBARRIEU(31150),
    CUGNAUX(31157),
    DREMIL_LAFAGE(31163),
    FENOUILLET(31182),
    FLOURENS(31184),
    FONBEAUZARD(31186),
    GAGNAC_SUR_GARONNE(31205),
    GRATENTOUR(31230),
    LAUNAGUET(31282),
    LESPINASSE(31293),
    L_UNION(31561),
    MONDONVILLE(31351),
    MONDOUZIL(31352),
    MONS(31355),
    MONTRABE(31389),
    PIBRAC(31417),
    PIN_BALMA(31418),
    QUINT_FONSEGRIVES(31445),
    SAINT_ALBAN(31467),
    SAINT_JEAN(31488),
    SAINT_JORY(31490),
    SAINT_ORENS_DE_GAMEVILLE(31506),
    SEILH(31541),
    TOULOUSE(31555),
    TOURNEFEUILLE(31557),
    VILLENEUVE_TOLOSANE(31588),
    ALL(-1);
    
    private final int inseeCode;
    
    private ToulouseMunicipalities(int inseeCode) {
        this.inseeCode = inseeCode;
    }

    public final int getInseeCode() {
        return inseeCode;
    }
}
