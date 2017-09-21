// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.nio.file.Path;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileGEN.GenBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.ChildBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.Lot;

/**
 * Edigeo GEN file.
 */
public class EdigeoFileGEN extends EdigeoLotFile<GenBlock> {

    abstract static class GenBlock extends ChildBlock {
        GenBlock(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Geographic bounds.
     */
    public static class GeoBounds extends GenBlock {
        /** CM1 */ String min = "";
        /** CM2 */ String max = "";

        GeoBounds(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CM1": safeGet(r, s -> min += s); break;
            case "CM2": safeGet(r, s -> max += s); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns the minimal coordinates.
         * @return the minimal coordinates
         */
        public final String getMinCm1() {
            return min;
        }

        /**
         * Returns the maximal coordinates.
         * @return the maximal coordinates
         */
        public final String getMaxCm2() {
            return max;
        }
    }

    /**
     * Geographic data.
     */
    public static class GeoData extends GenBlock {

        /**
         * Data structure.
         */
        enum Structure {
            TOPO_VECTOR(1),
            NETWORK(2),
            SPAGHETTI(3),
            REAL_MATRIX(4),
            CODED_MATRIX(5);

            final int code;
            Structure(int code) {
                this.code = code;
            }

            public static Structure of(int code) {
                for (Structure s : values()) {
                    if (s.code == code) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Integer.toString(code));
            }
        }

        /** INF */ String information = "";
        /** STR */ Structure structure;
        /** REG */ String offsetId = "";

        GeoData(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "INF": safeGetAndLog(r, s -> information += s, tr("Information")); break;
            case "STR": structure = Structure.of(safeGetInt(r)); break;
            case "REG": safeGet(r, s -> offsetId += s); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns general information.
         * @return general information
         */
        public final String getInformation() {
            return information;
        }

        /**
         * Returns data structure.
         * @return data structure
         */
        public final Structure getStructure() {
            return structure;
        }

        /**
         * Returns offset identifier.
         * @return offset identifier
         */
        public final String getOffsetId() {
            return offsetId;
        }
    }

    /**
     * Constructs a new {@code EdigeoFileGEN}.
     * @param lot parent lot
     * @param seId subset id
     * @param path path to GEN file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileGEN(Lot lot, String seId, Path path) throws IOException {
        super(lot, seId, path);
        register("DEG", GeoBounds.class);
        register("GSE", GeoData.class);
        lot.gen = this;
    }

    @Override
    public EdigeoFileGEN read(DataSet ds) throws IOException, ReflectiveOperationException {
        super.read(ds);
        return this;
    }
}
