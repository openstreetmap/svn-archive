// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Edigeo SCD file.
 */
public class EdigeoFileSCD extends EdigeoFile {

    /**
     * MCD Object definition.
     */
    public static class McdObjDef extends Block {

        /** DIP */ String dictRef = "";
        /** KND */ String kind = "";
        /** AAC */ int nAttributes;
        /** AAP */ final List<String> attributes = new ArrayList<>();
        /** QAC */ int nQualities;

        McdObjDef(String type) {
            super(type);
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
    public static class McdAttrDef extends Block {

        /** DIP */ String dictRef = "";
        /** CAN */ int nMaxChars;
        /** CAD */ int nMaxDigits;
        /** CAE */ int nMaxExponent;
        /** UNI */ String unit = "";
        /** AV1 */ String min = "";
        /** AV2 */ String max = "";

        McdAttrDef(String type) {
            super(type);
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
    public static class McdPrimDef extends Block {

        enum PrimitiveKind {
            NODE("NOD"),
            ARC("ARC"),
            FACE("FAC");

            String code;
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

        McdPrimDef(String type) {
            super(type);
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
    abstract static class McdRelDef extends Block {

        /** CA1 */ int minCardinal;
        /** CA2 */ int maxCardinal;
        /** SCC */ int nTypes;
        /** SCP */ final List<String> scdRef = new ArrayList<>();
        /** OCC */ final List<Integer> nOccurences = new ArrayList<>();
        /** AAC */ int nAttributes;
        /** QAC */ int nQualities;

        McdRelDef(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CA1": minCardinal = safeGetInt(r); break;
            case "CA2": maxCardinal = safeGetInt(r); break;
            case "SCC": nTypes = safeGetInt(r); break;
            case "SCP": safeGet(r, scdRef); break;
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
    public static class McdSemRelDef extends McdRelDef {

        /** DIP */ String dictRef = "";

        McdSemRelDef(String type) {
            super(type);
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
    public static class McdBuildRelDef extends McdRelDef {

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

            String code;
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

        McdBuildRelDef(String type) {
            super(type);
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
     * @param path path to SCD file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileSCD(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        switch (type) {
        case "OBJ":
            return new McdObjDef(type);
        case "ATT":
            return new McdAttrDef(type);
        case "PGE":
            return new McdPrimDef(type);
        case "ASS":
            return new McdSemRelDef(type);
        case "REL":
            return new McdBuildRelDef(type);
        default:
            throw new IllegalArgumentException(type);
        }
    }
}
