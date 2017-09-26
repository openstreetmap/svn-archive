// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.download;

/**
 * Encapsulates data that is required to download from the Cadastre.
 */
public class CadastreDownloadData {
    private final boolean downloadWater;
    private final boolean downloadBuilding;
    private final boolean downloadSymbol;
    private final boolean downloadParcel;
    private final boolean downloadParcelNumber;
    private final boolean downloadAddress;
    private final boolean downloadLocality;
    private final boolean downloadSection;
    private final boolean downloadCommune;

    /**
     * Constructs a new {@code CadastreDownloadData}.
     */
    CadastreDownloadData(boolean downloadWater, boolean downloadBuilding, boolean downloadSymbol,
            boolean downloadParcel, boolean downloadParcelNumber, boolean downloadAddress, boolean downloadLocality,
            boolean downloadSection, boolean downloadCommune) {
        this.downloadWater = downloadWater;
        this.downloadBuilding = downloadBuilding;
        this.downloadSymbol = downloadSymbol;
        this.downloadParcel = downloadParcel;
        this.downloadParcelNumber = downloadParcelNumber;
        this.downloadAddress = downloadAddress;
        this.downloadLocality = downloadLocality;
        this.downloadSection = downloadSection;
        this.downloadCommune = downloadCommune;
    }

    final boolean isDownloadWater() {
        return downloadWater;
    }

    final boolean isDownloadBuilding() {
        return downloadBuilding;
    }

    final boolean isDownloadSymbol() {
        return downloadSymbol;
    }

    final boolean isDownloadParcel() {
        return downloadParcel;
    }

    final boolean isDownloadParcelNumber() {
        return downloadParcelNumber;
    }

    final boolean isDownloadAddress() {
        return downloadAddress;
    }

    final boolean isDownloadLocality() {
        return downloadLocality;
    }

    final boolean isDownloadSection() {
        return downloadSection;
    }

    final boolean isDownloadCommune() {
        return downloadCommune;
    }
}
