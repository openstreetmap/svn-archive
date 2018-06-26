// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.download;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.data.osm.UploadPolicy;
import org.openstreetmap.josm.gui.download.AbstractDownloadSourcePanel;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.download.DownloadSettings;
import org.openstreetmap.josm.gui.download.DownloadSource;

/**
 * Download source to download data directly from French cadastre.
 */
public class CadastreDownloadSource implements DownloadSource<CadastreDownloadData> {

    @Override
    public AbstractDownloadSourcePanel<CadastreDownloadData> createPanel(DownloadDialog dialog) {
        return new CadastreDownloadSourcePanel(this);
    }

    @Override
    public void doDownload(CadastreDownloadData data, DownloadSettings settings) {
        if (settings.getDownloadBounds().isPresent()) {
            new CadastreDownloadTask(data).download(
                    new DownloadParams().withUploadPolicy(UploadPolicy.BLOCKED).withNewLayer(settings.asNewLayer()),
                    settings.getDownloadBounds().get(), null);
        }
    }

    @Override
    public String getLabel() {
        return tr("Download from Cadastre");
    }

    @Override
    public boolean onlyExpert() {
        return false;
    }
}
