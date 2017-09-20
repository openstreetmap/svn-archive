// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Edigeo GEN file.
 */
public class EdigeoFileGEN extends EdigeoFile {

    /**
     * Geographic bounds.
     */
    public static class GeoBounds extends Block {
        String cm1;
        String cm2;

        GeoBounds(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CM1": cm1 = safeGet(r); break;
            case "CM2": cm2 = safeGet(r); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns the minimal coordinates.
         * @return the minimal coordinates
         */
        public final String getCm1() {
            return cm1;
        }

        /**
         * Returns the maximal coordinates.
         * @return the maximal coordinates
         */
        public final String getCm2() {
            return cm2;
        }
    }

    /**
     * Geographic data.
     */
    public static class GeoData extends Block {

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

        String information;
        Structure structure;
        String offsetId;

        GeoData(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "INF": information = safeGetAndLog(r, tr("Information")); break;
            case "STR": structure = Structure.of(safeGetInt(r)); break;
            case "REG": offsetId = safeGet(r); break;
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

    GeoBounds bounds;
    GeoData geodata;

    /**
     * Constructs a new {@code EdigeoFileGEN}.
     * @param path path to GEN file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileGEN(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        switch (type) {
        case "DEG":
            bounds = new GeoBounds(type);
            return bounds;
        case "GSE":
            geodata = new GeoData(type);
            return geodata;
        default:
            throw new IllegalArgumentException(type);
        }
    }
}
