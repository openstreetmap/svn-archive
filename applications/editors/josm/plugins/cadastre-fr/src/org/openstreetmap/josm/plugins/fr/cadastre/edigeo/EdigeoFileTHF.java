// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openstreetmap.josm.actions.SimplifyWayAction;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.fr.cadastre.download.CadastreDownloadData;
import org.openstreetmap.josm.tools.Logging;

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

        /** AUT */ String author = "";
        /** ADR */ String recipient = "";
        /** LOC */ int nLots;
        /** VOC */ int nVolumes;
        /** VOL */ final List<String> volumeLabels = new ArrayList<>();
        /** SEC */ SecurityClassification security;
        /** RDI */ String diffusionRestriction = "";
        /** VER */ String edigeoVersion = "";
        /** VDA */ LocalDate edigeoDate;
        /** TRL */ String transmissionName = "";
        /** EDN */ int transmissionEdition;
        /** TDA */ LocalDate transmissionDate;
        /** INF */ String transmissionInformation = "";

        Support(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "AUT": safeGetAndLog(r, s -> author += s, tr("Author")); break;
            case "ADR": safeGetAndLog(r, s -> recipient += s, tr("Recipient")); break;
            case "LOC": nLots = safeGetInt(r); break;
            case "VOC": nVolumes = safeGetInt(r); break;
            case "VOL": safeGet(r, volumeLabels); break;
            case "SEC": security = SecurityClassification.of(safeGetInt(r)); break;
            case "RDI": safeGetAndLog(r, s -> diffusionRestriction += s, tr("Diffusion restriction")); break;
            case "VER": safeGet(r, s -> edigeoVersion += s); break;
            case "VDA": edigeoDate = safeGetDate(r); break;
            case "TRL": safeGet(r, s -> transmissionName += s); break;
            case "EDN": transmissionEdition = safeGetInt(r); break;
            case "TDA": transmissionDate = safeGetDateAndLog(r, tr("Date")); break;
            case "INF": safeGetAndLog(r, s -> transmissionInformation += s, tr("Information")); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotEmpty(author, recipient, edigeoVersion, transmissionName)
                    && areSameSize(nVolumes, volumeLabels) && transmissionEdition >= 1;
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
         * Returns volume labels.
         * @return volume labels
         */
        public final List<String> getVolumeLabels() {
            return Collections.unmodifiableList(volumeLabels);
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

        /** LON */ String name = "";
        /** INF */ String information = "";
        /** GNN */ String genName = "";
        /** GNI */ String genId = "";
        /** GON */ String geoName = "";
        /** GOI */ String geoId = "";
        /** QAN */ String qalName = "";
        /** QAI */ String qalId = "";
        /** DIN */ String dicName = "";
        /** DII */ String dicId = "";
        /** SCN */ String scdName = "";
        /** SCI */ String scdId = "";
        /** GDC */ int nVec;
        /** GDN */ final List<String> vecName = new ArrayList<>();
        /** GDI */ final List<String> vecId = new ArrayList<>();

        EdigeoFileGEN gen;
        EdigeoFileGEO geo;
        EdigeoFileDIC dic;
        EdigeoFileSCD scd;
        EdigeoFileQAL qal;
        final List<EdigeoFileVEC> vec = new ArrayList<>();
        final List<EdigeoLotFile<?>> allFiles = new ArrayList<>();

        Lot(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "LON": safeGetAndLog(r, s -> name += s, tr("Name")); break;
            case "INF": safeGetAndLog(r, s -> information += s, tr("Information")); break;
            case "GNN": safeGet(r, s -> genName += s); break;
            case "GNI": safeGet(r, s -> genId += s); break;
            case "GON": safeGet(r, s -> geoName += s); break;
            case "GOI": safeGet(r, s -> geoId += s); break;
            case "QAN": safeGet(r, s -> qalName += s); break;
            case "QAI": safeGet(r, s -> qalId += s); break;
            case "DIN": safeGet(r, s -> dicName += s); break;
            case "DII": safeGet(r, s -> dicId += s); break;
            case "SCN": safeGet(r, s -> scdName += s); break;
            case "SCI": safeGet(r, s -> scdId += s); break;
            case "GDC": nVec = safeGetInt(r); break;
            case "GDN": safeGet(r, vecName); break;
            case "GDI": safeGet(r, vecId); break;
            default:
                super.processRecord(r);
            }
        }

        void readFiles(Path path) throws IOException, ReflectiveOperationException {
            Path dir = path.getParent();
            allFiles.add(new EdigeoFileGEN(this, genId, dir.resolve(name + genName + ".GEN")).read());
            allFiles.add(new EdigeoFileGEO(this, geoId, dir.resolve(name + geoName + ".GEO")).read());
            allFiles.add(new EdigeoFileDIC(this, dicId, dir.resolve(name + dicName + ".DIC")).read());
            allFiles.add(new EdigeoFileSCD(this, scdId, dir.resolve(name + scdName + ".SCD")).read());
            allFiles.add(new EdigeoFileQAL(this, qalId, dir.resolve(name + qalName + ".QAL")).read());
            for (int i = 0; i < getNumberOfGeoData(); i++) {
                allFiles.add(new EdigeoFileVEC(this, vecId.get(i), dir.resolve(name + vecName.get(i) + ".VEC")).read());
            }
            allFiles.forEach(EdigeoFile::resolve);
            for (EdigeoFile f : allFiles) {
                boolean valid = f.isValid();
                if (valid) {
                    Logging.debug(f.path + ": valid");
                } else {
                    Logging.warn(f.path + ": invalid!");
                }
            }
        }

        void fill(DataSet ds, CadastreDownloadData data) {
            allFiles.forEach(f -> f.fill(ds, data));
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotEmpty(name, genName, genId, geoName, geoId);
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
            return genName;
        }

        /**
         * Returns general data subset identifier.
         * @return general data subset identifier
         */
        public final String getGenDataId() {
            return genId;
        }

        /**
         * Returns coordinates reference subset name.
         * @return coordinates reference subset name
         */
        public final String getCoorRefName() {
            return geoName;
        }

        /**
         * Returns coordinates reference subset identifier.
         * @return coordinates reference subset identifier
         */
        public final String getCoorRefId() {
            return geoId;
        }

        /**
         * Returns quality subset name.
         * @return quality subset name
         */
        public final String getQualityName() {
            return qalName;
        }

        /**
         * Returns quality subset identifier.
         * @return quality subset identifier
         */
        public final String getQualityId() {
            return qalId;
        }

        /**
         * Returns dictionary subset name.
         * @return dictionary subset name
         */
        public final String getDictName() {
            return dicName;
        }

        /**
         * Returns dictionary subset identifier.
         * @return dictionary subset identifier
         */
        public final String getDictId() {
            return dicId;
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
            return nVec;
        }

        /**
         * Returns geographic data subset name at index i.
         * @param i index
         * @return geographic data subset name at index i
         */
        public final String getGeoDataName(int i) {
            return vecName.get(i);
        }

        /**
         * Returns list of geographic data subset names.
         * @return list of geographic data subset names
         */
        public final List<String> getGeoDataNames() {
            return Collections.unmodifiableList(vecName);
        }

        /**
         * Returns list of geographic data subset identifiers.
         * @return list of geographic data subset identifiers
         */
        public final List<String> getGeoDataIds() {
            return Collections.unmodifiableList(vecId);
        }

        /**
         * Returns geographic data subset identifier at index i.
         * @param i index
         * @return geographic data subset identifier at index i
         */
        public final String getGeoDataId(int i) {
            return vecId.get(i);
        }
    }

    /**
     * Block inside {@link EdigeoLotFile}.
     */
    public static class ChildBlock extends Block {

        protected final Lot lot;

        ChildBlock(Lot lot, String type) {
            super(type);
            this.lot = Objects.requireNonNull(lot, "lot");
        }
    }

    /** GTS */ Support support;
    /** GTL */ final List<Lot> lots = new ArrayList<>();

    /**
     * Constructs a new {@code EdigeoFileTHF}.
     * @param path path to THF file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileTHF(Path path) throws IOException {
        super(path);
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
            case "GTS": return support = new Support(type);
            case "GTL": return addBlock(lots, new Lot(type));
            default:
                throw new IllegalArgumentException(type);
        }
    }

    @Override
    public EdigeoFileTHF read() throws IOException, ReflectiveOperationException {
        super.read();
        for (Lot lot : getLots()) {
            lot.readFiles(path);
        }
        return this;
    }

    @Override
    public EdigeoFileTHF fill(DataSet ds, CadastreDownloadData data) {
        super.fill(ds, data);
        for (Lot lot : getLots()) {
            //ds.addDataSource(new DataSource(lot.gen.getGeoBounds().getBounds(), support.author));
            lot.fill(ds, data);
        }
        ds.getWays().forEach(w -> {
            SequenceCommand command = SimplifyWayAction.simplifyWay(w, 0.25);
            if (command != null) {
                command.executeCommand();
            }
        });
        return this;
    }

    @Override
    boolean isValid() {
        return support.isValid() && lots.stream().allMatch(Block::isValid);
    }
}
