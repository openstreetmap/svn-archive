// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Edigeo QAL file.
 */
public class EdigeoFileQAL extends EdigeoFile {

    /**
     * Update descriptor.
     */
    public static class Update extends Block {

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

        Update(String type) {
            super(type);
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
     * Constructs a new {@code EdigeoFileQAL}.
     * @param path path to QAL file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileQAL(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        if ("QUP".equals(type)) {
            return new Update(type);
        }
        throw new IllegalArgumentException(type);
    }
}
