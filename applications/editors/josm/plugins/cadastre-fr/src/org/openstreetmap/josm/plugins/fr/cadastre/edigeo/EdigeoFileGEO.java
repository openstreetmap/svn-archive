// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.nio.file.Path;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileGEO.GeoBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.ChildBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.Lot;

/**
 * Edigeo GEO file.
 */
public class EdigeoFileGEO extends EdigeoLotFile<GeoBlock> {

    abstract static class GeoBlock extends ChildBlock {
        GeoBlock(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Coordinates reference.
     */
    public static class CoorReference extends GeoBlock {

        enum ReferenceType {
            CARTESIAN("CAR"),
            GEOGRAPHIC("GEO"),
            PROJECTED("MAP");

            String code;
            ReferenceType(String code) {
                this.code = code;
            }

            public static ReferenceType of(String code) {
                for (ReferenceType r : values()) {
                    if (r.code.equals(code)) {
                        return r;
                    }
                }
                throw new IllegalArgumentException(code);
            }
        }

        enum AltitudeSystem {
            TWO_DIM_PLUS_ALTITUDE(1),
            THREE_DIM_OR_NO_ALTITUDE(2);

            int code;
            AltitudeSystem(int code) {
                this.code = code;
            }

            public static AltitudeSystem of(int code) {
                for (AltitudeSystem s : values()) {
                    if (s.code == code) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Integer.toString(code));
            }
        }

        /** RET */ ReferenceType type;
        /** REN */ String name = "";
        /** REL */ String code = "";
        /** DIM */ int nDim;
        /** ALS */ AltitudeSystem altitudeSystem;
        /** UNH */ String unit = "";

        CoorReference(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "RET": safeGet(r, s -> type = ReferenceType.of(s)); break;
            case "REN": safeGetAndLog(r, s -> name += s, tr("Projection")); break;
            case "REL": safeGetAndLog(r, s -> code += s, tr("Projection")); break;
            case "DIM": nDim = safeGetInt(r); break;
            case "ALS": altitudeSystem = AltitudeSystem.of(safeGetInt(r)); break;
            case "UNH": safeGet(r, s -> unit += s); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns reference type.
         * @return reference type
         */
        public final ReferenceType getReferenceType() {
            return type;
        }

        /**
         * Returns reference name.
         * @return reference name
         */
        public final String getReferenceName() {
            return name;
        }

        /**
         * Returns reference code.
         * @return reference code
         */
        public final String getReferenceCode() {
            return code;
        }

        /**
         * Returns number of dimensions.
         * @return number of dimensions
         */
        public final int getNumberOfDimensions() {
            return nDim;
        }

        /**
         * Returns altitude system.
         * @return altitude system
         */
        public final AltitudeSystem getAltitudeSystem() {
            return altitudeSystem;
        }

        /**
         * Returns unit.
         * @return unit
         */
        public final String getUnit() {
            return unit;
        }
    }

    /**
     * Constructs a new {@code EdigeoFileGEO}.
     * @param lot parent lot
     * @param seId subset id
     * @param path path to GEO file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileGEO(Lot lot, String seId, Path path) throws IOException {
        super(lot, seId, path);
        register("GEO", CoorReference.class);
        lot.geo = this;
    }

    @Override
    public EdigeoFileGEO read(DataSet ds) throws IOException, ReflectiveOperationException {
        super.read(ds);
        return this;
    }
}
