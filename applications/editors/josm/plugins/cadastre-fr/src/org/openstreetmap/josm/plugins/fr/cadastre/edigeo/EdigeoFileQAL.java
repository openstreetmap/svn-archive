// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileQAL.QalBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.ChildBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.Lot;

/**
 * Edigeo QAL file.
 */
public class EdigeoFileQAL extends EdigeoLotFile<QalBlock> {

    abstract static class QalBlock extends ChildBlock {
        QalBlock(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Genealogy descriptor (6.4.5.1). TODO
     */
    public static class Genealogy extends QalBlock {
        Genealogy(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Update descriptor (6.4.5.2).
     */
    public static class Update extends QalBlock {

        enum UpdateType {
            NO_MODIFICATION(0),
            CREATION(1),
            REPLACEMENT(2),
            DELETION(3);

            final int code;
            UpdateType(int code) {
                this.code = code;
            }

            public static UpdateType of(int code) {
                for (UpdateType s : values()) {
                    if (s.code == code) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Integer.toString(code));
            }
        }

        enum Perennity {
            TEMPORARY(1),
            DEFINITIVE(2);

            final int code;
            Perennity(int code) {
                this.code = code;
            }

            public static Perennity of(int code) {
                for (Perennity s : values()) {
                    if (s.code == code) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Integer.toString(code));
            }
        }

        /** ODA */ LocalDate observationDate;
        /** UTY */ UpdateType updateType;
        /** ULO */ Perennity perennity;
        /** UDA */ LocalDate updateDate;
        /** RAT */ double annualRatio;
        /** EDA */ LocalDate endOfValidity;
        /** COC */ int nElements;
        /** COP */ final List<String> mcdRef = new ArrayList<>();

        Update(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "ODA": observationDate = safeGetDate(r); break;
            case "UTY": updateType = UpdateType.of(safeGetInt(r)); break;
            case "ULO": perennity = Perennity.of(safeGetInt(r)); break;
            case "UDA": updateDate = safeGetDate(r); break;
            case "RAT": annualRatio = safeGetDouble(r); break;
            case "EDA": endOfValidity = safeGetDate(r); break;
            case "COC": nElements = safeGetInt(r); break;
            case "COP": safeGet(r, mcdRef); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * Horizontal precision descriptor (6.4.5.3). TODO
     */
    public static class HorizontalPrecision extends QalBlock {
        HorizontalPrecision(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Vertical precision descriptor (6.4.5.4). TODO
     */
    public static class VerticalPrecision extends QalBlock {
        VerticalPrecision(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Metric precision descriptor (6.4.5.5). TODO
     */
    public static class MetricPrecision extends QalBlock {
        MetricPrecision(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Exhaustivity descriptor (6.4.5.6). TODO
     */
    public static class Exhaustivity extends QalBlock {
        Exhaustivity(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Semantic precision descriptor (6.4.5.7). TODO
     */
    public static class SemanticPrecision extends QalBlock {
        SemanticPrecision(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Logical coherence descriptor (6.4.5.8). TODO
     */
    public static class LogicalCoherence extends QalBlock {
        LogicalCoherence(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Specific quality descriptor (6.4.5.9). TODO
     */
    public static class SpecificQuality extends QalBlock {
        SpecificQuality(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Constructs a new {@code EdigeoFileQAL}.
     * @param lot parent lot
     * @param seId subset id
     * @param path path to QAL file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileQAL(Lot lot, String seId, Path path) throws IOException {
        super(lot, seId, path);
        register("QUP", Update.class);
        lot.qal = this;
    }
}
