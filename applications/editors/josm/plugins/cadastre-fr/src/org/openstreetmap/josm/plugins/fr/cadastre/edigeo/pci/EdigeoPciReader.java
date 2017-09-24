// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo.pci;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.DataSet.UploadPolicy;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileVEC;

/**
 * Reader for French Cadastre - Edigéo files.
 */
public class EdigeoPciReader extends AbstractReader {

    private static final Map<String, List<String>> highways = new HashMap<>();
    static {
        highways.put("motorway", Arrays.asList("Autoroute"));
        highways.put("trunk", Arrays.asList("Rocade"));
        highways.put("secondary", Arrays.asList("Avenue", "Boulevard", "Allee", "Allée", "Allees", "Allées", "Pont", "Port", "Route"));
        highways.put("residential", Arrays.asList("Chemin", "Impasse", "Place", "Rue", "Quai", "Voie", "Grand Rue"));

        EdigeoFileVEC.addIgnoredObject("SYM_id",
                "31", // Connecting arrows between parcelles and numbers
                "62", // "Sports ground, small streams". What the fuck France?
                "64"  // "parking, terrace, overhang". What the fuck France?
        );

        EdigeoFileVEC.addObjectPostProcessor("19", "boundary=administrative;admin_level=8"); // Municipality limit trigger
        EdigeoFileVEC.addObjectPostProcessor("21", "highway=road"); // Path
        EdigeoFileVEC.addObjectPostProcessor("33", "barrier=wall;bridge=yes"); // bridge parapet
        EdigeoFileVEC.addObjectPostProcessor("34", "landuse=reservoir;natural=water;water=reservoir"); // reservoir, lake
        EdigeoFileVEC.addObjectPostProcessor("39", "barrier=wall"); // Common wall
        EdigeoFileVEC.addObjectPostProcessor("40", "barrier=wall"); // Non-adjacent wall
        EdigeoFileVEC.addObjectPostProcessor("45", "barrier=hedge"); // Common hedge
        EdigeoFileVEC.addObjectPostProcessor("46", "barrier=hedge"); // Non-adjacent hedge
        EdigeoFileVEC.addObjectPostProcessor("65", "leisure=swimming_pool;access=private"); // Swimming pool

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            StringBuffer sb = new StringBuffer(p.get("TEX_id").trim());
            p.remove("TEX_id");
            for (String t : Arrays.asList("TEX2_id", "TEX3_id", "TEX4_id", "TEX5_id", "TEX6_id", "TEX7_id", "TEX8_id", "TEX9_id")) {
                String v = p.get(t);
                if (v == null) {
                    break;
                }
                sb.append(' ').append(v.trim());
                p.remove(t);
            }
            p.put("name", sb.toString().replaceAll("   ", " ").replaceAll("  ", " "));
        }, "TEX_id");

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("highway", "road");
            String name = p.get("name");
            if (name != null && name.contains(" ")) {
                String[] words = name.split(" ");
                if (!setCorrectHighway(p, words)) {
                    if (highways.values().stream().anyMatch(l -> l.contains(words[words.length - 1]))) {
                        String[] newWords = new String[words.length];
                        newWords[0] = words[words.length - 1];
                        System.arraycopy(words, 0, newWords, 1, words.length - 1);
                        p.put("name", String.join(" ", newWords));
                        setCorrectHighway(p, newWords);
                    }
                }
            }
        }, o -> o.hasScdIdentifier("ZONCOMMUNI_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("boundary", "administrative");
            p.put("admin_level", "8");
            p.put("ref:INSEE", "XX"+p.get("IDU_id")); // TODO: find department number
            p.put("name", p.get("TEX2_id")); // TODO: lowercase
            p.remove("IDU_id");
            p.remove("TEX2_id");
        }, o -> o.hasScdIdentifier("COMMUNE_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("boundary", "cadastral");
            p.put("ref", p.get("IDU_id"));
            p.remove("IDU_id");
        }, o -> o.hasScdIdentifier("SECTION_id") || o.hasScdIdentifier("SUBDSECT_id") || o.hasScdIdentifier("PARCELLE_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> p.put("wall", "no"), "DUR_id", "02");
        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("building", "yes");
            p.remove("DUR_id");
        }, o -> o.hasScdIdentifier("BATIMENT_id"));
    }

    private static boolean setCorrectHighway(OsmPrimitive p, String[] words) {
        String type = words[0];
        for (Entry<String, List<String>> e : highways.entrySet()) {
            if (e.getValue().contains(type)) {
                p.put("highway", e.getKey());
                return true;
            }
        }
        return false;
    }

    /**
     * Constructs a new {@code EdigeoReader}.
     */
    public EdigeoPciReader() {
    }

    static DataSet parseDataSet(InputStream in, File file, ProgressMonitor instance) throws IOException {
        if (in != null) {
            in.close();
        }
        try {
            return new EdigeoPciReader().parse(file.toPath(), instance);
        } catch (IOException e) {
            throw e;
        } catch (Exception | AssertionError e) {
            throw new IOException(e);
        }
    }

    DataSet parse(Path path, ProgressMonitor instance) throws IOException, ReflectiveOperationException {
        DataSet data = new DataSet();
        data.setUploadPolicy(UploadPolicy.DISCOURAGED);
        EdigeoFileTHF thf = new EdigeoFileTHF(path).read().fill(data);
        data.setName(thf.getSupport().getBlockIdentifier());
        return data;
    }

    @Override
    protected DataSet doParseDataSet(InputStream source, ProgressMonitor progressMonitor) throws IllegalDataException {
        return null;
    }
}
