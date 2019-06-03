// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.command;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;

import javax.swing.Icon;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.plugins.piclayer.transform.PictureTransform;
import org.openstreetmap.josm.tools.ImageProvider;

public class TransformCommand extends Command {

    private PicLayerAbstract layer;
    private PictureTransform beforeTransform;
    private PictureTransform afterTransform;
    private String actionName;

    public TransformCommand(PicLayerAbstract layer, String actionName) {
        super(new DataSet()); // not really a command...
        this.layer = layer;
        this.actionName = actionName;
        beforeTransform = extractTransform();
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public boolean executeCommand() {
        placeTransform(afterTransform);
        layer.invalidate();
        return true;
    }

    @Override
    public void undoCommand() {
        placeTransform(beforeTransform);
        layer.invalidate();
    }

    private void placeTransform(PictureTransform transform) {
        layer.getTransformer().setTransform(transform.getTransform());
        layer.getTransformer().setOriginPoints(transform.getOriginPoints());
        layer.getTransformer().setImagePosition(transform.getImagePosition());
    }

    private PictureTransform extractTransform() {
        PictureTransform transform = new PictureTransform();
        transform.setOriginPoints(layer.getTransformer().getOriginPoints());
        transform.setTransform(layer.getTransformer().getTransform());
        transform.setImagePosition(layer.getTransformer().getImagePosition());
        return transform;
    }

    @Override
    public Icon getDescriptionIcon() {
        return ImageProvider.get("layericon");
    }

    @Override
    public String getDescriptionText() {
        return tr("PicLayer: {0}", actionName);
    }

    private boolean alreadyAdded = false;
    public void addIfChanged() {

        afterTransform = extractTransform();

        boolean changed = !beforeTransform.getTransform().equals(afterTransform.getTransform()) ||
            !beforeTransform.getOriginPoints().equals(afterTransform.getOriginPoints()) ||
            !beforeTransform.getImagePosition().equals(afterTransform.getImagePosition());
        if (changed && !alreadyAdded) {
            UndoRedoHandler.getInstance().add(this);
            alreadyAdded = true;
        }
    }
}
