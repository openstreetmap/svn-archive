// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.ScdBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.ChildBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.Lot;

/**
 * Edigeo SCD file.
 */
public class EdigeoFileSCD extends EdigeoLotFile<ScdBlock> {

    /**
     * MCD definition.
     */
    abstract static class ScdBlock extends ChildBlock {
        ScdBlock(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * MCD Object definition.
     */
    public static class McdObjectDef extends ScdBlock {

        /** DIP */ String dictRef = "";
        /** KND */ String kind = "";
        /** AAC */ int nAttributes;
        /** AAP */ final List<String> attributes = new ArrayList<>();
        /** QAC */ int nQualities;

        McdObjectDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "DIP": safeGet(r, s -> dictRef += s); break;
            case "KND": safeGet(r, s -> kind += s); break;
            case "AAC": nAttributes = safeGetInt(r); break;
            case "AAP": safeGet(r, attributes); break;
            case "QAC": nQualities = safeGetInt(r); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * MCD Attribute definition.
     */
    public static class McdAttributeDef extends ScdBlock {

        /** DIP */ String dictRef = "";
        /** CAN */ int nMaxChars;
        /** CAD */ int nMaxDigits;
        /** CAE */ int nMaxExponent;
        /** UNI */ String unit = "";
        /** AV1 */ String min = "";
        /** AV2 */ String max = "";

        McdAttributeDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "DIP": safeGet(r, s -> dictRef += s); break;
            case "CAN": nMaxChars = safeGetInt(r); break;
            case "CAD": nMaxDigits = safeGetInt(r); break;
            case "CAE": nMaxExponent = safeGetInt(r); break;
            case "UNI": safeGet(r, s -> unit += s); break;
            case "AV1": safeGet(r, s -> min += s); break;
            case "AV2": safeGet(r, s -> max += s); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * MCD Primitive definition.
     */
    public static class McdPrimitiveDef extends ScdBlock {

        enum PrimitiveKind {
            NODE("NOD"),
            ARC("ARC"),
            FACE("FAC");

            final String code;
            PrimitiveKind(String code) {
                this.code = code;
            }

            public static PrimitiveKind of(String code) {
                for (PrimitiveKind s : values()) {
                    if (s.code.equals(code)) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(code);
            }
        }

        /** KND */ PrimitiveKind kind;
        /** AAC */ int nAttributes;
        /** QAC */ int nQualities;

        McdPrimitiveDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "KND": safeGet(r, s -> kind = PrimitiveKind.of(s)); break;
            case "AAC": nAttributes = safeGetInt(r); break;
            case "QAC": nQualities = safeGetInt(r); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * MCD Relation definition.
     */
    abstract static class McdRelationDef extends ScdBlock {

        /** CA1 */ int minCardinal;
        /** CA2 */ int maxCardinal;
        /** SCC */ int nTypes;
        /** SCP */ final List<ScdBlock> scdRef = new ArrayList<>();
        /** OCC */ final List<Integer> nOccurences = new ArrayList<>();
        /** AAC */ int nAttributes;
        /** QAC */ int nQualities;

        McdRelationDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CA1": minCardinal = safeGetInt(r); break;
            case "CA2": maxCardinal = safeGetInt(r); break;
            case "SCC": nTypes = safeGetInt(r); break;
            case "SCP": scdRef.add(lot.scd.find(r.values)); break;
            case "OCC": nOccurences.add(safeGetInt(r)); break;
            case "AAC": nAttributes = safeGetInt(r); break;
            case "QAC": nQualities = safeGetInt(r); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * MCD Semantic Relation definition.
     */
    public static class McdSemanticRelationDef extends McdRelationDef {

        /** DIP */ String dictRef = "";

        McdSemanticRelationDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "DIP": safeGet(r, s -> dictRef += s); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * MCD Construction Relation definition.
     */
    public static class McdConstructionRelationDef extends McdRelationDef {

        enum RelationKind {
            IS_COMPOSED_OF("ICO"),
            IS_DISPLAYED_BY("IDB"),
            IS_DISPLAYED_BY_ARC("IDR"),
            HAS_FOR_INITIAL_NODE("IND"),
            HAS_FOR_FINAL_NODE("FND"),
            HAS_FOR_LEFT_FACE("LPO"),
            HAS_FOR_RIGHT_FACE("RPO"),
            IS_INCLUDED_IN("ILI"),
            BELONG_TO("BET");

            final String code;
            RelationKind(String code) {
                this.code = code;
            }

            public static RelationKind of(String code) {
                for (RelationKind s : values()) {
                    if (s.code.equals(code)) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(code);
            }
        }

        /** KND */ RelationKind kind;

        McdConstructionRelationDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "KND": safeGet(r, s -> kind = RelationKind.of(s)); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * Constructs a new {@code EdigeoFileSCD}.
     * @param lot parent lot
     * @param seId subset id
     * @param path path to SCD file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileSCD(Lot lot, String seId, Path path) throws IOException {
        super(lot, seId, path);
        register("OBJ", McdObjectDef.class);
        register("ATT", McdAttributeDef.class);
        register("PGE", McdPrimitiveDef.class);
        register("ASS", McdSemanticRelationDef.class);
        register("REL", McdConstructionRelationDef.class);
        lot.scd = this;
    }

    @Override
    public EdigeoFileSCD read(DataSet ds) throws IOException, ReflectiveOperationException {
        super.read(ds);
        return this;
    }
}
