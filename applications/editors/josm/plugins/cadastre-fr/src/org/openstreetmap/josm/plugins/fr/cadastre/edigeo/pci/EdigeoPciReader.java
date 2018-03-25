// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo.pci;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.UploadPolicy;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileVEC;
import org.openstreetmap.josm.tools.Utils;

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

        // Ignored objects
        EdigeoFileVEC.addIgnoredScdObjects(
                "PTCANV_id",   // Canvas point
                "BORNE_id",    // Property boundary marker
                "BOULON_id",   // Property boundary marker for Alsace and Moselle
                "CROIX_id",    // Property boundary marker for Alsace and Moselle
                "SYMBLIM_id"); // Common wall symbol
        EdigeoFileVEC.addIgnoredObject("SYM_id",
                "30", // Water stream arrow
                "31", // Connecting arrows between parcelles and numbers
                "62", // "Sports ground, small streams". What the fuck France?
                "64"  // "parking, terrace, overhang". What the fuck France?
        );

        // SYM_id mapping
        EdigeoFileVEC.addObjectPostProcessor("12", "historic=wayside_cross"); // Calvaire
        EdigeoFileVEC.addObjectPostProcessor("14", "amenity=place_of_worship;religion=christian"); // Church
        EdigeoFileVEC.addObjectPostProcessor("15", "amenity=place_of_worship;religion=muslim"); // Mosque
        EdigeoFileVEC.addObjectPostProcessor("16", "amenity=place_of_worship;religion=jewish"); // Synagogue
        EdigeoFileVEC.addObjectPostProcessor("17", "boundary=administrative;admin_level=2"); // State limit
        EdigeoFileVEC.addObjectPostProcessor("18", "boundary=administrative;admin_level=6"); // Department limit
        EdigeoFileVEC.addObjectPostProcessor("19", "boundary=administrative;admin_level=8"); // Municipality limit trigger
        EdigeoFileVEC.addObjectPostProcessor("21", "highway=road"); // Way
        EdigeoFileVEC.addObjectPostProcessor("22", "highway=road"); // Road trigger
        EdigeoFileVEC.addObjectPostProcessor((o, p) -> { // Path / Footway
            String highwayValue = "path";
            if (p instanceof Way) {
                Way w = (Way) p;
                Predicate<Way> isBuilding = x -> x.hasKey("building");
                if (w.firstNode().getParentWays().stream().anyMatch(isBuilding)
                  || w.lastNode().getParentWays().stream().anyMatch(isBuilding)) {
                    highwayValue = "footway";
                }
            }
            p.put("highway", highwayValue);
            p.remove("SYM_id");
        }, "SYM_id", "23"); // Path / Footway
        EdigeoFileVEC.addObjectPostProcessor("24", "man_made=pipeline"); // Pipeline
        EdigeoFileVEC.addObjectPostProcessor("25", "man_made=pipeline"); // Aqueduct
        EdigeoFileVEC.addObjectPostProcessor("26", "aerialway=cable_car"); // Aerialway
        EdigeoFileVEC.addObjectPostProcessor("27", "power=line"); // Force transport line
        EdigeoFileVEC.addObjectPostProcessor("29", "railway=rail"); // Railway
        EdigeoFileVEC.addObjectPostProcessor("33", "bridge=yes"); // Bridge
        EdigeoFileVEC.addObjectPostProcessor("34", "landuse=reservoir;natural=water;water=reservoir"); // reservoir, lake
        EdigeoFileVEC.addObjectPostProcessor("37", "tunnel=yes"); // Tunnel
        EdigeoFileVEC.addObjectPostProcessor("47", "railway=halt"); // Halt
        EdigeoFileVEC.addObjectPostProcessor("48", "railway=stop"); // Stop
        EdigeoFileVEC.addObjectPostProcessor("49", "railway=station"); // Station
        EdigeoFileVEC.addObjectPostProcessor("50", "man_made=mast"); // Pylon
        EdigeoFileVEC.addObjectPostProcessor("51", "landuse=cemetery;religion=christian"); // Christian cemetery
        EdigeoFileVEC.addObjectPostProcessor("52", "landuse=cemetery;religion=muslim"); // Muslim cemetery
        EdigeoFileVEC.addObjectPostProcessor("53", "landuse=cemetery;religion=jewish"); // Jewish cemetery
        EdigeoFileVEC.addObjectPostProcessor("63", "man_made=water_well"); // Well
        EdigeoFileVEC.addObjectPostProcessor("65", "leisure=swimming_pool;access=private"); // Swimming pool

        // Mapping TEX*_id => name (first step)
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
            setName(p, sb.toString());
        }, "TEX_id");

        // Objects mapping
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
            p.put("name", WordUtils.capitalizeFully(p.get("TEX2_id")));
            p.remove("IDU_id");
            p.remove("TEX2_id");
        }, o -> o.hasScdIdentifier("COMMUNE_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("boundary", "cadastral");
            p.put("ref", p.get("IDU_id"));
            p.remove("IDU_id");
            p.remove("ICL_id");
            p.remove("COAR_id");
            p.remove("COPL_id");
            p.remove("DEDI_id");
            p.remove("DIS_id");
            p.remove("DRED_id");
            p.remove("EOR_id");
            p.remove("INDP_id");
            p.remove("INP_id");
            p.remove("QUPL_id");
            p.remove("SUPF_id");
        }, o -> o.hasScdIdentifier("SECTION_id") || o.hasScdIdentifier("SUBDSECT_id")
             || o.hasScdIdentifier("PARCELLE_id") || o.hasScdIdentifier("SUBDFISC_id") || o.hasScdIdentifier("CHARGE_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> p.put("wall", "no"), "DUR_id", "02");
        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("building", "yes");
            p.remove("DUR_id");
        }, o -> o.hasScdIdentifier("BATIMENT_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("addr:housenumber", p.get("name"));
            p.remove("name");
        }, o -> o.hasScdIdentifier("NUMVOIE_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("place", "unknown");
            p.put("fixme", "place type");
        }, o -> o.hasScdIdentifier("LIEUDIT_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.remove("ORI_id");
        }, o -> o.hasScdIdentifier("TPOINT_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("highway", "road");
            p.put("area", "yes");
        }, o -> o.hasScdIdentifier("TRONROUTE_id"));

        EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
            p.put("waterway", "riverbank");
        }, o -> o.hasScdIdentifier("TRONFLUV_id"));

        // Mapping TEX*_id => name (last step)
        for (String t : Arrays.asList("TEX2_id", "TEX3_id", "TEX4_id", "TEX5_id", "TEX6_id", "TEX7_id", "TEX8_id", "TEX9_id")) {
            EdigeoFileVEC.addObjectPostProcessor((o, p) -> {
                setName(p, p.get(t));
                p.remove(t);
            }, t);
        }
    }

    private static void setName(OsmPrimitive p, String input) {
        if (input != null) {
            String name = input.replaceAll("    ", " ").replaceAll("   ", " ").replaceAll("  ", " ");
            if (name.matches("([A-Za-z] )+[A-Za-z]")) {
                name = name.replaceAll(" ", "");
            }
            if (name.length() > 2 && StringUtils.isAllUpperCase(name.replaceAll(" ", "").replaceAll("'", "").replaceAll("-", ""))) {
                name = WordUtils.capitalizeFully(name);
            }
            p.put("name", name);
        }
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
        Path tmpDir = null;
        Path thfPath = path;
        try {
            if (thfPath.toString().endsWith(".tar.bz2")) {
                try (InputStream fin = Files.newInputStream(path);
                     BufferedInputStream in = new BufferedInputStream(fin);
                     BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
                     TarArchiveInputStream tar = new TarArchiveInputStream(bzIn)
                ) {
                    TarArchiveEntry entry;
                    tmpDir = Files.createTempDirectory(Utils.getJosmTempDir().toPath(), "cadastre");
                    while ((entry = tar.getNextTarEntry()) != null) {
                        File file = tmpDir.resolve(entry.getName()).toFile();
                        try (FileOutputStream out = new FileOutputStream(file)) {
                            if (IOUtils.copy(tar, out) < entry.getSize()) {
                                throw new IOException(String.format("Unable to write ''%s'' entirely", file));
                            } else if (file.toString().endsWith(".THF")) {
                                thfPath = file.toPath();
                            }
                        }
                    }
                }
            }
            DataSet data = new DataSet();
            data.setUploadPolicy(UploadPolicy.DISCOURAGED);
            EdigeoFileTHF thf = new EdigeoFileTHF(thfPath).read().fill(data);
            data.setName(thf.getSupport().getBlockIdentifier());
            return data;
        } finally {
            if (tmpDir != null) {
                Utils.deleteDirectory(tmpDir.toFile());
            }
        }
    }

    @Override
    protected DataSet doParseDataSet(InputStream source, ProgressMonitor progressMonitor) throws IllegalDataException {
        return null;
    }
}
