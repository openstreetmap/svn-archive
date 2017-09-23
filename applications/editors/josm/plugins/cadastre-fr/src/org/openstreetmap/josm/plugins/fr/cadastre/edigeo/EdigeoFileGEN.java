// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.nio.file.Path;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.projection.Projection;
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
        /** CM1 */ EastNorth min;
        /** CM2 */ EastNorth max;

        GeoBounds(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CM1": min = safeGetEastNorth(r); break;
            case "CM2": max = safeGetEastNorth(r); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(min, max);
        }

        /**
         * Returns the minimal coordinates.
         * @return the minimal coordinates
         */
        public final EastNorth getMinCm1() {
            return min;
        }

        /**
         * Returns the maximal coordinates.
         * @return the maximal coordinates
         */
        public final EastNorth getMaxCm2() {
            return max;
        }

        /**
         * Returns the bounds.
         * @return the bounds
         */
        public Bounds getBounds() {
/*
            for (String s : Projections.getAllProjectionCodes()) {
                Projection p = Projections.getProjectionByCode(s);
                LatLon en = p.eastNorth2latlon(min);
                double lat = en.lat();
                double lon = en.lon();
                if (43 <= lat && lat <= 44 && 1.38 <= lon && lon <= 1.45) {
                    System.out.println(s + ": " + p);
                }
            }
*/
            Projection proj = lot.geo.getCoorReference().getProjection();
            return new Bounds(proj.eastNorth2latlon(min), proj.eastNorth2latlon(max));
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

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(structure);
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

    /**
     * Returns the geographic bounds.
     * @return the geographic bounds
     */
    public final GeoBounds getGeoBounds() {
        return blocks.getInstances(GeoBounds.class).get(0);
    }
}
