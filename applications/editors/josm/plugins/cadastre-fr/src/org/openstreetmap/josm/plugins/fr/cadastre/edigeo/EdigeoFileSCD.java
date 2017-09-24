// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileDIC.AttributeDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileDIC.ObjectDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileDIC.RelationDef;
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
     * MCD definition with attributes.
     */
    abstract static class ScdTaggedDef extends ScdBlock {
        /** AAC */ int nAttributes;
        /** AAP */ final List<List<String>> lAttributes = new ArrayList<>();
        /** QAC */ int nQualities;
        // TODO: qualities ?

        final List<McdAttributeDef> attributes = new ArrayList<>();

        ScdTaggedDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "AAC": nAttributes = safeGetInt(r); break;
            case "AAP": lAttributes.add(r.values); break;
            case "QAC": nQualities = safeGetInt(r); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        void resolvePhase1() {
            super.resolvePhase1();
            for (List<String> values : lAttributes) {
                attributes.add(lot.scd.find(values, McdAttributeDef.class));
            }
            lAttributes.clear();
        }

        @Override
        boolean isValid() {
            return super.isValid() && areSameSize(nAttributes, attributes);
        }
    }

    /**
     * MCD Object definition.
     */
    public static class McdObjectDef extends ScdTaggedDef {

        enum ObjectKind {
            COMPLEX("CPX"),
            POINT("PCT"),
            LINE("LIN"),
            AREA("ARE");

            final String code;
            ObjectKind(String code) {
                this.code = code;
            }

            public static ObjectKind of(String code) {
                for (ObjectKind s : values()) {
                    if (s.code.equals(code)) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(code);
            }
        }

        /** DIP */ ObjectDef dictRef;
        /** KND */ ObjectKind kind;

        McdObjectDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "DIP": dictRef = lot.dic.find(r.values, ObjectDef.class); break;
            case "KND": safeGet(r, s -> kind = ObjectKind.of(s)); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(dictRef, kind);
        }

        @Override
        public String toString() {
            return "McdObjectDef [dictRef=" + dictRef + ", kind=" + kind + ", nAttributes=" + nAttributes
                    + ", attributes=" + attributes + ", nQualities=" + nQualities + ", type=" + type + ", identifier="
                    + identifier + ']';
        }
    }

    /**
     * MCD Attribute definition.
     */
    public static class McdAttributeDef extends ScdBlock {

        /** DIP */ AttributeDef dictRef;
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
            case "DIP": dictRef = lot.dic.find(r.values, AttributeDef.class); break;
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

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(dictRef);
        }

        @Override
        public String toString() {
            return "McdAttributeDef [identifier=" + identifier + ']';
        }
    }

    /**
     * MCD Primitive definition.
     */
    public static class McdPrimitiveDef extends ScdTaggedDef {

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

        McdPrimitiveDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "KND": safeGet(r, s -> kind = PrimitiveKind.of(s)); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(kind);
        }

        @Override
        public String toString() {
            return "McdPrimitiveDef [kind=" + kind + ", nAttributes=" + nAttributes + ", attributes=" + attributes
                    + ", nQualities=" + nQualities + ", type=" + type + ", identifier=" + identifier + ']';
        }
    }

    /**
     * MCD Relation definition.
     */
    abstract static class McdRelationDef extends ScdTaggedDef {

        /** CA1 */ int minCardinal;
        /** CA2 */ int maxCardinal;
        /** SCC */ int nTypes;
        /** SCP */ final List<ScdBlock> scdRef = new ArrayList<>();
        /** OCC */ final List<Integer> nOccurences = new ArrayList<>();

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
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(minCardinal, maxCardinal) && areSameSize(nTypes, scdRef, nOccurences);
        }
    }

    /**
     * MCD Semantic Relation definition.
     */
    public static class McdSemanticRelationDef extends McdRelationDef {

        /** DIP */ RelationDef dictRef;

        McdSemanticRelationDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "DIP": dictRef = lot.dic.find(r.values, RelationDef.class); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(dictRef);
        }

        @Override
        public String toString() {
            return "McdSemanticRelationDef [dictRef=" + dictRef + ", minCardinal=" + minCardinal + ", maxCardinal="
                    + maxCardinal + ", scdRef=" + scdRef + ", nOccurences=" + nOccurences + ", attributes=" + attributes
                    + ", type=" + type + ", identifier=" + identifier + ']';
        }
    }

    /**
     * MCD Construction Relation definition.
     */
    public static class McdConstructionRelationDef extends McdRelationDef {

        enum RelationKind {
            IS_COMPOSED_OF("ICO"),
            IS_MADE_OF("IDB"),
            IS_MADE_OF_ARC("IDR"),
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

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(kind);
        }

        @Override
        public String toString() {
            return "McdConstructionRelationDef [kind=" + kind + ", minCardinal=" + minCardinal + ", maxCardinal="
                    + maxCardinal + ", scdRef=" + scdRef + ", nOccurences=" + nOccurences + ", attributes=" + attributes
                    + ", type=" + type + ", identifier=" + identifier + ']';
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
}
