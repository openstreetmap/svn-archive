//License: GPL. For details, see README file.

package org.openstreetmap.josm.plugins.epsg31287;


import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.data.APIDataSet;

public class EPSG31287UploadHook implements UploadHook {

    private final Epsg31287 plugin;

    public EPSG31287UploadHook(Epsg31287 plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean checkUpload(APIDataSet arg0) {
        if (Main.proj.toCode().equals(ProjectionEPSG31287.getProjCode())) {
            plugin.toggleProjection();
        }
        return true;
    }

}
