// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.administration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;
import org.openstreetmap.josm.tools.Pair;

/**
 * Handler for GeoFla 2.0. Compatibility for previous version 1.1 has been dropped.
 * See http://professionnels.ign.fr/sites/default/files/DC_GEOFLA_2-0.pdf
 */
public class GeoFlaHandler extends DataGouvDataSetHandler {
    
    private static final String ADMIN_LEVEL = "admin_level";
    
    /**
     * Constructs a new {@code GeoFlaHandler}.
     */
    public GeoFlaHandler() {
        setName("GEOFLA®");
        getShpHandler().setPreferMultipolygonToSimpleWay(true);
        try {
            setLocalPortalURL("http://professionnels.ign.fr/geofla#tab-3");
        } catch (MalformedURLException e) {
            Main.error(e);
        }
    }

    @Override
    public String getLocalPortalIconName() {
        return ICON_IGN_24;
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return isDepartementFile(filename) || isCommuneFile(filename) || isCantonFile(filename) || isArrondissementFile(filename);
    }
        
    protected boolean isDepartementFile(String filename) {
        return acceptsShpMifFilename(filename, "DEPARTEMENT") || acceptsShpMifFilename(filename, "LIMITE_DEPARTEMENT");
    }

    protected boolean isCommuneFile(String filename) {
        return acceptsShpFilename(filename, "COMMUNE") || acceptsShpFilename(filename, "LIMITE_COMMUNE");
    }

    protected boolean isCantonFile(String filename) {
        return acceptsShpFilename(filename, "CANTON") || acceptsShpFilename(filename, "LIMITE_CANTON");
    }

    protected boolean isArrondissementFile(String filename) {
        return acceptsShpFilename(filename, "ARRONDISSEMENT") || acceptsShpFilename(filename, "LIMITE_ARRONDISSEMENT");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        final String filename = getAssociatedFile().getName();
        if (isDepartementFile(filename)) {
            setNationalPortalPath("GEOFLA®-Départements-30383060");
        } else if (isCommuneFile(filename)) {
            setNationalPortalPath("GEOFLA®-Communes-30383083");
        }
        for (OsmPrimitive p : ds.allPrimitives()) {
            if (hasKeyIgnoreCase(p, "Id_geofla", "Id_GéoFLA")) {
                String deptName = WordUtils.capitalizeFully(getAndRemoveIgnoreCase(p, "Nom_dept", "Nom_Département"));
                if ("Reunion".equals(deptName)) {
                    deptName = "La Réunion";
                }
                if (isDepartementFile(filename)) {
                    p.put("name", deptName);
                } else if (isCommuneFile(filename)) {
                    p.put("name", WordUtils.capitalizeFully(getAndRemoveIgnoreCase(p, "NOM_COM")));
                    replace(p, "INSEE_COM", "ref:INSEE");
                }
                getAndRemoveIgnoreCase(p, "NOM_REG");
                replace(p, "POPULATION", "population");
                p.put("boundary", "administrative");
                String nature = getIgnoreCase(p, "Nature");
                if ("Frontière internationale".equalsIgnoreCase(nature) || "Limite côtière".equalsIgnoreCase(nature)) {
                    p.put(ADMIN_LEVEL, "2");
                } else if ("Limite de région".equalsIgnoreCase(nature)) {
                    p.put(ADMIN_LEVEL, "4");
                } else if (isDepartementFile(filename) || "Limite de département".equalsIgnoreCase(nature)) {
                    p.put(ADMIN_LEVEL, "6");
                } else if(isArrondissementFile(filename) || "Limite d'arrondissement".equalsIgnoreCase(nature)) {
                    p.put(ADMIN_LEVEL, "7");
                } else if(isCommuneFile(filename)) {
                    p.put(ADMIN_LEVEL, "8");
                }
                if (p instanceof Relation) {
                    p.put("type", "boundary");
                }
                LatLon llCentroid = getLatLon(p, deptName, "centroid", "Centroïde");
                if (llCentroid != null) {
                    Node centroid = new Node(llCentroid);
                    ds.addPrimitive(centroid);
                    if (p instanceof Relation) {
                        ((Relation) p).addMember(new RelationMember("centroid", centroid));
                    }
                }
                LatLon llChefLieu = getLatLon(p, deptName, "chf_lieu", "Chef_Lieu");
                if (llChefLieu != null) {
                    Node chefLieu = new Node(llChefLieu);
                    ds.addPrimitive(chefLieu);
                    String name = WordUtils.capitalizeFully(getAndRemoveIgnoreCase(p, "Nom_chf", "Nom_Chef_lieu"));
                    if (name != null) {
                        if (isArrondissementFile(filename)) {
                            p.put("name", name);
                        }
                        chefLieu.put("name", name);
                    }
                    String population = p.get("population");
                    if (population != null) {
                        try {
                            int pop = Integer.parseInt(population);
                            if (pop < 2000) {
                                chefLieu.put("place", "village");
                            } else if (pop < 100000) {
                                chefLieu.put("place", "town");
                            } else {
                                chefLieu.put("place", "city");
                            }
                        } catch (NumberFormatException e) {
                            Main.warn("Invalid population: "+population);
                        }
                    }
                    if (p instanceof Relation) {
                        ((Relation) p).addMember(new RelationMember("admin_centre", chefLieu));
                    }
                }
            }
        }
    }
    
    protected static boolean hasKeyIgnoreCase(OsmPrimitive p, String ... strings) {
        return getIgnoreCase(p, strings) != null;
    }

    protected static String getIgnoreCase(OsmPrimitive p, String ... strings) {
        String result = null;
        for (String s : strings) {
            if (result == null) result = p.get(s);
            if (result == null) result = p.get(s.toUpperCase());
            if (result == null) result = p.get(s.toLowerCase());
        }
        return result;
    }

    protected static void removeIgnoreCase(OsmPrimitive p, String ... strings) {
        for (String s : strings) {
            p.remove(s);
            p.remove(s.toUpperCase());
            p.remove(s.toLowerCase());
        }
    }
    
    protected static String getAndRemoveIgnoreCase(OsmPrimitive p, String ... strings) {
        String result = getIgnoreCase(p, strings);
        removeIgnoreCase(p, strings);
        return result;
    }

    protected static LatLon getLatLon(OsmPrimitive p, String dptName, String shortAttribute, String longAttribute) {
        String x = getAndRemoveIgnoreCase(p, "X_"+shortAttribute, "Abscisse_"+longAttribute);
        String y = getAndRemoveIgnoreCase(p, "Y_"+shortAttribute, "Ordonnée_"+longAttribute);
        if (x != null && y != null) {
            try {
                String dptCode = getIgnoreCase(p, "Code_dept", "Code_Département");
                if (dptCode != null && dptCode.equals("97") && dptName != null) {
                    if (dptName.equals("Guadeloupe")) {
                        dptCode = "971";
                    } else if (dptName.equals("Martinique")) {
                        dptCode = "972";
                    } else if (dptName.equals("Guyane")) {
                        dptCode = "973";
                    } else if (dptName.equals("La Réunion")) {
                        dptCode = "974";
                    } else if (dptName.equals("Mayotte")) {
                        dptCode = "976";
                    } else {
                        Main.error("Unknown French department: "+dptName);
                    }
                }
                return getLatLonByDptCode(new EastNorth(Double.parseDouble(x), Double.parseDouble(y)), dptCode, false);
            } catch (NumberFormatException e) {
                Main.error(e);
            }
        }
        return null;
    }
    
    private Pair<String, URL> getGeoflaURL(String name, String urlSuffix) throws MalformedURLException {
        return new Pair<>(name, new URL("https://wxs-telechargement.ign.fr/oikr5jryiph0iwhw36053ptm/telechargement/inspire/"+urlSuffix));
    }

    @Override
    public List<Pair<String, URL>> getDataURLs() {
        List<Pair<String, URL>> result = new ArrayList<>();
        try {
            // Communes
            result.add(getGeoflaURL("2014 Communes France Métropolitaine",        "GEOFLA_THEME-COMMUNE_2014_GEOFLA_2-0_COMMUNE_SHP_LAMB93_FXX_2014-12-05/file/GEOFLA_2-0_COMMUNE_SHP_LAMB93_FXX_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Communes Guadeloupe",                   "GEOFLA_THEME-COMMUNE_2014_GEOFLA_2-0_COMMUNE_SHP_UTM20W84GUAD_D971_2014-12-08/file/GEOFLA_2-0_COMMUNE_SHP_UTM20W84GUAD_D971_2014-12-08.7z"));
            result.add(getGeoflaURL("2014 Communes Martinique",                   "GEOFLA_THEME-COMMUNE_2014_GEOFLA_2-0_COMMUNE_SHP_UTM20W84MART_D972_2014-12-08/file/GEOFLA_2-0_COMMUNE_SHP_UTM20W84MART_D972_2014-12-08.7z"));
            result.add(getGeoflaURL("2014 Communes Guyane",                       "GEOFLA_THEME-COMMUNE_2014_GEOFLA_2-0_COMMUNE_SHP_UTM22RGFG95_D973_2014-12-05/file/GEOFLA_2-0_COMMUNE_SHP_UTM22RGFG95_D973_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Communes Réunion",                      "GEOFLA_THEME-COMMUNE_2014_GEOFLA_2-0_COMMUNE_SHP_RGR92UTM40S_D974_2014-12-05/file/GEOFLA_2-0_COMMUNE_SHP_RGR92UTM40S_D974_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Communes Mayotte",                      "GEOFLA_THEME-COMMUNE_2014_GEOFLA_2-0_COMMUNE_SHP_RGM04UTM38S_D976_2014-12-05/file/GEOFLA_2-0_COMMUNE_SHP_RGM04UTM38S_D976_2014-12-05.7z"));
            // Cantons
            result.add(getGeoflaURL("2014 Cantons France Métropolitaine",         "GEOFLA_THEME-CANTON_2014_GEOFLA_2-0_CANTON_SHP_LAMB93_FXX_2014-12-05/file/GEOFLA_2-0_CANTON_SHP_LAMB93_FXX_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Cantons Guadeloupe",                    "GEOFLA_THEME-CANTON_2014_GEOFLA_2-0_CANTON_SHP_UTM20W84GUAD_D971_2014-12-08/file/GEOFLA_2-0_CANTON_SHP_UTM20W84GUAD_D971_2014-12-08.7z"));
            result.add(getGeoflaURL("2014 Cantons Martinique",                    "GEOFLA_THEME-CANTON_2014_GEOFLA_2-0_CANTON_SHP_UTM20W84MART_D972_2014-12-08/file/GEOFLA_2-0_CANTON_SHP_UTM20W84MART_D972_2014-12-08.7z"));
            result.add(getGeoflaURL("2014 Cantons Guyane",                        "GEOFLA_THEME-CANTON_2014_GEOFLA_2-0_CANTON_SHP_UTM22RGFG95_D973_2014-12-05/file/GEOFLA_2-0_CANTON_SHP_UTM22RGFG95_D973_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Cantons Réunion",                       "GEOFLA_THEME-CANTON_2014_GEOFLA_2-0_CANTON_SHP_RGR92UTM40S_D974_2014-12-05/file/GEOFLA_2-0_CANTON_SHP_RGR92UTM40S_D974_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Cantons Mayotte",                       "GEOFLA_THEME-CANTON_2014_GEOFLA_2-0_CANTON_SHP_RGM04UTM38S_D976_2014-12-05/file/GEOFLA_2-0_CANTON_SHP_RGM04UTM38S_D976_2014-12-05.7z"));
            // Arrondissements
            result.add(getGeoflaURL("2014 Arrondissements France Métropolitaine", "GEOFLA_THEME-ARRONDISSEMENT_2014_GEOFLA_2-0_ARRONDISSEMENT_SHP_LAMB93_FXX_2014-12-05/file/GEOFLA_2-0_ARRONDISSEMENT_SHP_LAMB93_FXX_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Arrondissements Guadeloupe",            "GEOFLA_THEME-ARRONDISSEMENT_2014_GEOFLA_2-0_ARRONDISSEMENT_SHP_UTM20W84GUAD_D971_2014-12-08/file/GEOFLA_2-0_ARRONDISSEMENT_SHP_UTM20W84GUAD_D971_2014-12-08.7z"));
            result.add(getGeoflaURL("2014 Arrondissements Martinique",            "GEOFLA_THEME-ARRONDISSEMENT_2014_GEOFLA_2-0_ARRONDISSEMENT_SHP_UTM20W84MART_D972_2014-12-08/file/GEOFLA_2-0_ARRONDISSEMENT_SHP_UTM20W84MART_D972_2014-12-08.7z"));
            result.add(getGeoflaURL("2014 Arrondissements Guyane",                "GEOFLA_THEME-ARRONDISSEMENT_2014_GEOFLA_2-0_ARRONDISSEMENT_SHP_UTM22RGFG95_D973_2014-12-05/file/GEOFLA_2-0_ARRONDISSEMENT_SHP_UTM22RGFG95_D973_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Arrondissements Réunion",               "GEOFLA_THEME-ARRONDISSEMENT_2014_GEOFLA_2-0_ARRONDISSEMENT_SHP_RGR92UTM40S_D974_2014-12-05/file/GEOFLA_2-0_ARRONDISSEMENT_SHP_RGR92UTM40S_D974_2014-12-05.7z"));
            // Départements
            result.add(getGeoflaURL("2014 Départements France Métropolitaine",    "GEOFLA_THEME-DEPARTEMENTS_2014_GEOFLA_2-0_DEPARTEMENT_SHP_LAMB93_FXX_2014-12-05/file/GEOFLA_2-0_DEPARTEMENT_SHP_LAMB93_FXX_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Départements Guadeloupe",               "GEOFLA_THEME-DEPARTEMENTS_2014_GEOFLA_2-0_DEPARTEMENT_SHP_UTM20W84GUAD_D971_2014-12-08/file/GEOFLA_2-0_DEPARTEMENT_SHP_UTM20W84GUAD_D971_2014-12-08.7z"));
            result.add(getGeoflaURL("2014 Départements Martinique",               "GEOFLA_THEME-DEPARTEMENTS_2014_GEOFLA_2-0_DEPARTEMENT_SHP_UTM20W84MART_D972_2014-12-08/file/GEOFLA_2-0_DEPARTEMENT_SHP_UTM20W84MART_D972_2014-12-08.7z"));
            result.add(getGeoflaURL("2014 Départements Guyane",                   "GEOFLA_THEME-DEPARTEMENTS_2014_GEOFLA_2-0_DEPARTEMENT_SHP_UTM22RGFG95_D973_2014-12-05/file/GEOFLA_2-0_DEPARTEMENT_SHP_UTM22RGFG95_D973_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Départements Réunion",                  "GEOFLA_THEME-DEPARTEMENTS_2014_GEOFLA_2-0_DEPARTEMENT_SHP_RGR92UTM40S_D974_2014-12-05/file/GEOFLA_2-0_DEPARTEMENT_SHP_RGR92UTM40S_D974_2014-12-05.7z"));
            result.add(getGeoflaURL("2014 Départements Mayotte",                  "GEOFLA_THEME-DEPARTEMENTS_2014_GEOFLA_2-0_DEPARTEMENT_SHP_RGM04UTM38S_D976_2014-12-05/file/GEOFLA_2-0_DEPARTEMENT_SHP_RGM04UTM38S_D976_2014-12-05.7z"));
        } catch (MalformedURLException e) {
            Main.error(e);
        }
        return result;
    }
}
