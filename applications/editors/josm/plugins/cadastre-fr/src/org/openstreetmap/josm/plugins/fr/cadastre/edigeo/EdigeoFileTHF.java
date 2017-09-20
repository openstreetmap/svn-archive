// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Edigeo THF file.
 */
public class EdigeoFileTHF extends EdigeoFile {

    /**
     * Support descriptor.
     */
    public static class Support extends Block {

        enum SecurityClassification {
            MILITARY_SECRECY(1),
            INDUSTRIAL_SECRECY(2),
            CONFIDENTIAL(3),
            MILITARY_CONFIDENTIAL(4),
            INDUSTRIAL_CONFIDENTIAL(5),
            RESTRICTED_DIFFUSION(6),
            NOT_PROTECTED(7);

            final int level;
            SecurityClassification(int level) {
                this.level = level;
            }

            public static SecurityClassification of(int level) {
                for (SecurityClassification s : values()) {
                    if (s.level == level) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Integer.toString(level));
            }
        }

        /** AUT */ String author;
        /** ADR */ String recipient;
        /** LOC */ int nLots;
        /** VOC */ int nVolumes;
        /** SEC */ SecurityClassification security;
        /** RDI */ String diffusionRestriction;
        /** VER */ String edigeoVersion;
        /** VDA */ LocalDate edigeoDate;
        /** TRL */ String transmissionName;
        /** EDN */ int transmissionEdition;
        /** TDA */ LocalDate transmissionDate;
        /** INF */ String transmissionInformation;

        Support(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "AUT": author = safeGetAndLog(r, tr("Author")); break;
            case "ADR": recipient = safeGetAndLog(r, tr("Recipient")); break;
            case "LOC": nLots = safeGetInt(r); break;
            case "VOC": nVolumes = safeGetInt(r); break;
            case "SEC": security = SecurityClassification.of(safeGetInt(r)); break;
            case "RDI": diffusionRestriction = safeGetAndLog(r, tr("Diffusion restriction")); break;
            case "VER": edigeoVersion = safeGet(r); break;
            case "VDA": edigeoDate = safeGetDate(r); break;
            case "TRL": transmissionName = safeGet(r); break;
            case "EDN": transmissionEdition = safeGetInt(r); break;
            case "TDA": transmissionDate = safeGetDateAndLog(r, tr("Date")); break;
            case "INF": transmissionInformation = safeGetAndLog(r, tr("Information")); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns author.
         * @return author
         */
        public final String getAuthor() {
            return author;
        }

        /**
         * Returns recipient.
         * @return recipient
         */
        public final String getRecipient() {
            return recipient;
        }

        /**
         * Returns number of geographic lots.
         * @return number of geographic lots
         */
        public final int getnLots() {
            return nLots;
        }

        /**
         * Returns number of volumes.
         * @return number of volumes
         */
        public final int getnVolumes() {
            return nVolumes;
        }

        /**
         * Returns security classification.
         * @return security classification
         */
        public final SecurityClassification getSecurity() {
            return security;
        }

        /**
         * Returns diffusion restriction.
         * @return diffusion restriction
         */
        public final String getDiffusionRestriction() {
            return diffusionRestriction;
        }

        /**
         * Returns Edigeo version.
         * @return Edigeo version
         */
        public final String getEdigeoVersion() {
            return edigeoVersion;
        }

        /**
         * Returns Edigeo date.
         * @return Edigeo date
         */
        public final LocalDate getEdigeoDate() {
            return edigeoDate;
        }

        /**
         * Returns name of transmission.
         * @return name of transmission
         */
        public final String getTransmissionName() {
            return transmissionName;
        }

        /**
         * Returns edition number of transmission.
         * @return edition number of transmission
         */
        public final int getTransmissionEdition() {
            return transmissionEdition;
        }

        /**
         * Returns date of transmission.
         * @return date of transmission
         */
        public final LocalDate getTransmissionDate() {
            return transmissionDate;
        }

        /**
         * Returns general information about transmission.
         * @return general information about transmission
         */
        public final String getTransmissionInformation() {
            return transmissionInformation;
        }
    }

    /**
     * Geographic lot descriptor.
     */
    public static class Lot extends Block {

        /** LON */ String name;
        /** INF */ String information;
        /** GNN */ String genDataName;
        /** GNI */ String genDataId;
        /** GON */ String coorRefName;
        /** GOI */ String coorRefId;
        /** QAN */ String qualityName;
        /** QAI */ String qualityId;
        /** DIN */ String dictName;
        /** DII */ String dictId;
        /** SCN */ String scdName;
        /** SCI */ String scdId;
        /** GDC */ int nGeoData;
        /** GDN */ final List<String> geoDataName = new ArrayList<>();
        /** GDI */ final List<String> geoDataId = new ArrayList<>();

        Lot(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "LON": name = safeGetAndLog(r, tr("Name")); break;
            case "INF": information = safeGetAndLog(r, tr("Information")); break;
            case "GNN": genDataName = safeGet(r); break;
            case "GNI": genDataId = safeGet(r); break;
            case "GON": coorRefName = safeGet(r); break;
            case "GOI": coorRefId = safeGet(r); break;
            case "QAN": qualityName = safeGet(r); break;
            case "QAI": qualityId = safeGet(r); break;
            case "DIN": dictName = safeGet(r); break;
            case "DII": dictId = safeGet(r); break;
            case "SCN": scdName = safeGet(r); break;
            case "SCI": scdId = safeGet(r); break;
            case "GDC": nGeoData = safeGetInt(r); break;
            case "GDN": geoDataName.add(safeGet(r)); break;
            case "GDI": geoDataId.add(safeGet(r)); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns name.
         * @return name
         */
        public final String getName() {
            return name;
        }

        /**
         * Returns general information.
         * @return general information
         */
        public final String getInformation() {
            return information;
        }

        /**
         * Returns general data subset name.
         * @return general data subset name
         */
        public final String getGenDataName() {
            return genDataName;
        }

        /**
         * Returns general data subset identifier.
         * @return general data subset identifier
         */
        public final String getGenDataId() {
            return genDataId;
        }

        /**
         * Returns coordinates reference subset name.
         * @return coordinates reference subset name
         */
        public final String getCoorRefName() {
            return coorRefName;
        }

        /**
         * Returns coordinates reference subset identifier.
         * @return coordinates reference subset identifier
         */
        public final String getCoorRefId() {
            return coorRefId;
        }

        /**
         * Returns quality subset name.
         * @return quality subset name
         */
        public final String getQualityName() {
            return qualityName;
        }

        /**
         * Returns quality subset identifier.
         * @return quality subset identifier
         */
        public final String getQualityId() {
            return qualityId;
        }

        /**
         * Returns dictionary subset name.
         * @return dictionary subset name
         */
        public final String getDictName() {
            return dictName;
        }

        /**
         * Returns dictionary subset identifier.
         * @return dictionary subset identifier
         */
        public final String getDictId() {
            return dictId;
        }

        /**
         * Returns SCD subset name.
         * @return SCD subset name
         */
        public final String getScdName() {
            return scdName;
        }

        /**
         * Returns SCD subset identifier.
         * @return SCD subset identifier
         */
        public final String getScdId() {
            return scdId;
        }

        /**
         * Returns number of geographic data subsets.
         * @return number of geographic data subsets
         */
        public final int getNumberOfGeoData() {
            return nGeoData;
        }

        /**
         * Returns geographic data subset name at index i.
         * @param i index
         * @return geographic data subset name at index i
         */
        public final String getGeoDataName(int i) {
            return geoDataName.get(i);
        }

        /**
         * Returns list of geographic data subset names.
         * @return list of geographic data subset names
         */
        public final List<String> getGeoDataNames() {
            return Collections.unmodifiableList(geoDataName);
        }

        /**
         * Returns list of geographic data subset identifiers.
         * @return list of geographic data subset identifiers
         */
        public final List<String> getGeoDataIds() {
            return Collections.unmodifiableList(geoDataId);
        }

        /**
         * Returns geographic data subset identifier at index i.
         * @param i index
         * @return geographic data subset identifier at index i
         */
        public final String getGeoDataId(int i) {
            return geoDataId.get(i);
        }
    }

    Support support;
    List<Lot> lots;

    /**
     * Constructs a new {@code EdigeoFileTHF}.
     * @param path path to THF file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileTHF(Path path) throws IOException {
        super(path);
    }

    @Override
    protected void init() {
        lots = new ArrayList<>();
    }

    /**
     * Returns the support descriptor.
     * @return the support descriptor
     */
    public Support getSupport() {
        return support;
    }

    /**
     * Returns the list of geographic lot descriptors.
     * @return the list of geographic lot descriptors
     */
    public List<Lot> getLots() {
        return Collections.unmodifiableList(lots);
    }

    @Override
    protected Block createBlock(String type) {
        switch (type) {
            case "GTS":
                support = new Support(type);
                return support;
            case "GTL":
                Lot lot = new Lot(type);
                lots.add(lot);
                return lot;
            default:
                throw new IllegalArgumentException(type);
        }
    }
}
