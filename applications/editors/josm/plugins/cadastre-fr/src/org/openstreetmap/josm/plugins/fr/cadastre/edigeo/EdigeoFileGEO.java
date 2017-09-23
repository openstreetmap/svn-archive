// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.Projections;
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
     * Coordinates reference. 7.4.2.1
     */
    public static class CoorReference extends GeoBlock {

        enum ReferenceType {
            CARTESIAN("CAR"),
            GEOGRAPHIC("GEO"),
            PROJECTED("MAP");

            final String code;
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

            final int code;
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

        enum AltitudeSystemType {
            TERRESTRIAL(1),
            BATHYMETRIC(2);

            final int code;
            AltitudeSystemType(int code) {
                this.code = code;
            }

            public static AltitudeSystemType of(int code) {
                for (AltitudeSystemType s : values()) {
                    if (s.code == code) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Integer.toString(code));
            }
        }

        interface Reference {
            String getEpsgCode();
        }

        enum CartesianReference implements Reference {
            NTF("EPSG:4275"),
            ED50("EPSG:4230"),
            WGS72("EPSG:4322"),
            WGS84("EPSG:4326"),
            REUN47("EPSG:4626"),
            MART38("EPSG:4625"),
            GUAD48("EPSG:4622"),
            CSG67("EPSG:4623"),
            MAYO50("EPSG:4632"),
            STPM50("EPSG:4638");

            final String epsg;
            CartesianReference(String epsg) {
                this.epsg = epsg;
            }

            @Override
            public String getEpsgCode() {
                return epsg;
            }
        }

        enum GeographicReference implements Reference {
            NTFG("EPSG:4275"),
            NTFP("EPSG:4807"),
            ED50G("EPSG:4230"),
            WGS72G("EPSG:4322"),
            WGS84G("EPSG:4326"),
            REUN47GEO("EPSG:4626"),
            MART38GEO("EPSG:4625"),
            GUAD48GEO("EPSG:4622"),
            CSG67GEO("EPSG:4623"),
            MAYO50GEO("EPSG:4632"),
            STPM50GEO("EPSG:4638");

            final String epsg;
            GeographicReference(String epsg) {
                this.epsg = epsg;
            }

            @Override
            public String getEpsgCode() {
                return epsg;
            }
        }

        enum ProjectedReference implements Reference {
            LAMB1("EPSG:27561"),
            LAMB2("EPSG:27562"),
            LAMB3("EPSG:27563"),
            LAMB4("EPSG:27564"),
            LAMB1C("EPSG:27571"),
            LAMB2C("EPSG:27572"),
            LAMB3C("EPSG:27573"),
            LAMB4C("EPSG:27574"),
            LAMBE("EPSG:27572"),
            LAMB93("EPSG:2154"),
            UTM30("EPSG:23030"),
            UTM31("EPSG:23031"),
            UTM32("EPSG:23032"),
            UTM30W72("EPSG:32230"),
            UTM31W72("EPSG:32231"),
            UTM32W72("EPSG:32232"),
            UTM30W84("EPSG:32630"),
            UTM31W84("EPSG:32631"),
            UTM32W84("EPSG:32632"),
            REUN47GAUSSL("EPSG:3727"),
            MART38UTM20("EPSG:2973"),
            GUAD48UTM20("EPSG:2970"),
            CSG67UTM21("EPSG:3312"),
            CSG67UTM22("EPSG:2971"),
            MAYO50UTM38S("EPSG:2980"),
            STPM50UTM21("EPSG:2987");

            final String epsg;
            ProjectedReference(String epsg) {
                this.epsg = epsg;
            }

            @Override
            public String getEpsgCode() {
                return epsg;
            }
        }

        /** RET */ ReferenceType type;
        /** REN */ String name = "";
        /** REL */ String code = "";
        /** DIM */ int nDim;
        /** ALS */ AltitudeSystem altitudeSystem;
        /** ALT */ AltitudeSystemType altitudeSystemType;
        /** ALN */ String altitudeSystemName = "";
        /** ALL */ String altitudeSystemCode = "";
        /** UNH */ String unitHorizontal = "";
        /** UNV */ String unitVertical = "";

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
            case "ALT": altitudeSystemType = AltitudeSystemType.of(safeGetInt(r)); break;
            case "ALN": safeGet(r, s -> altitudeSystemName += s); break;
            case "ALL": safeGet(r, s -> altitudeSystemCode += s); break;
            case "UNH": safeGet(r, s -> unitHorizontal += s); break;
            case "UNV": safeGet(r, s -> unitVertical += s); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(type, altitudeSystem) && areNotEmpty(code, unitHorizontal)
                    && (nDim == 2 || (nDim == 3 && areNotEmpty(unitVertical)))
                    && (AltitudeSystem.THREE_DIM_OR_NO_ALTITUDE == altitudeSystem
                    || areNotNull(altitudeSystemType, altitudeSystemName, altitudeSystemCode));
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
         * Returns horizontal unit.
         * @return horizontal unit
         */
        public final String getUnitHorizontal() {
            return unitHorizontal;
        }

        /**
         * Returns vertical unit.
         * @return vertical unit
         */
        public final String getUnitVertical() {
            return unitVertical;
        }

        /**
         * Returns the projection.
         * @return the projection
         */
        public final Projection getProjection() {
            Reference ref;
            switch (type.code) {
            case "CAR": ref = CartesianReference.valueOf(code); break;
            case "GEO": ref = GeographicReference.valueOf(code); break;
            case "MAP": ref = ProjectedReference.valueOf(code); break;
            default: throw new IllegalArgumentException(code);
            }
            return Projections.getProjectionByCode(ref.getEpsgCode());
        }
    }

    /**
     * Offset. 7.4.2.2
     */
    public static class Offset extends GeoBlock {

        /** RPC */ int nOffsetPoints;
        /** RPI */ final List<String> offsetPointIds = new ArrayList<>();
        /** RP1 */ final List<EastNorth> offsetInputCoor = new ArrayList<>();
        /** RP2 */ final List<EastNorth> offsetReferCoor = new ArrayList<>();
        /** CPC */ int nControlPoints;
        /** CPI */ final List<String> controlPointIds = new ArrayList<>();
        /** CP1 */ final List<EastNorth> controlInputCoor = new ArrayList<>();
        /** CP2 */ final List<EastNorth> controlReferCoor = new ArrayList<>();

        Offset(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "RPC": nOffsetPoints = safeGetInt(r); break;
            case "RPI": safeGet(r, offsetPointIds); break;
            case "RP1": offsetInputCoor.add(safeGetEastNorth(r)); break;
            case "RP2": offsetReferCoor.add(safeGetEastNorth(r)); break;
            case "CPC": nControlPoints = safeGetInt(r); break;
            case "CPI": safeGet(r, controlPointIds); break;
            case "CP1": controlInputCoor.add(safeGetEastNorth(r)); break;
            case "CP2": controlReferCoor.add(safeGetEastNorth(r)); break;
            default: super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid()
                    && areSameSize(nOffsetPoints, offsetPointIds, offsetInputCoor, offsetReferCoor)
                    && areSameSize(nControlPoints, controlPointIds, controlInputCoor, controlReferCoor);
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
        register("RPR", Offset.class);
        lot.geo = this;
    }

    /**
     * Returns the coordinate reference system.
     * @return the coordinate reference system
     */
    public CoorReference getCoorReference() {
        return blocks.getInstances(CoorReference.class).get(0);
    }

    /**
     * Returns the offset descriptor.
     * @return the offset descriptor
     */
    public Offset getOffset() {
        return blocks.getInstances(Offset.class).get(0);
    }
}
