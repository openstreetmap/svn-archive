// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.canvec_helper;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.tools.Logging;

// most of the layout was copied from the openstreetbugs plugin to get things started
class CanvecLayer extends Layer {
    private Icon layerIcon = null;
    private int maxZoom = 4;
    final CanvecHelper plugin;
    private List<CanVecTile> tiles = new ArrayList<>();
    List<CanVecTile> downloadable = new ArrayList<>();
    List<CanVecTile> openable = new ArrayList<>();

    CanvecLayer(String name, CanvecHelper self) {
        super(name);
        plugin = self;
        this.setBackgroundLayer(true);
        layerIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/images/layericon.png")));
        long start = System.currentTimeMillis();
        try (
            InputStream index = new CachedFile("http://ftp2.cits.rncan.gc.ca/OSM/pub/ZippedOsm.txt").getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(index, StandardCharsets.UTF_8));
        ) {
            Pattern p = Pattern.compile("(\\d\\d\\d)([A-Z]\\d\\d).*");
            String line;
            int lastCell = -1;
            List<String> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    int cell = Integer.parseInt(m.group(1));
                    if (cell == lastCell) {
                        list.add(m.group(0));
                    } else if (lastCell != -1) {
                        CanVecTile tile = new CanVecTile(lastCell, "", 0, "", this, list);
                        if (tile.isValid()) tiles.add(tile);
                        list = new ArrayList<>();
                        list.add(m.group(0));
                    }
                    lastCell = cell;
                } else if (!line.contains("Metadata.txt")) {
                    Logging.warn("bad line '" + line + "'\n");
                }
            }
            CanVecTile tile = new CanVecTile(lastCell, "", 0, "", this, list);
            if (tile.isValid()) tiles.add(tile);

            if (Logging.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                Logging.debug((end-start)+"ms spent");
            }
        } catch (IOException e) {
            Logging.error("exception getting index");
            Logging.error(e);
        }
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[]{
            LayerListDialog.getInstance().createShowHideLayerAction(),
            LayerListDialog.getInstance().createDeleteLayerAction(),
            SeparatorLayerAction.INSTANCE,
            new LayerListPopup.InfoAction(this),
            new MaxZoomAction(this),
            new DownloadCanvecAction(this),
            new OpenOsmAction(this)};
    }

    private static final class MaxZoomAction extends AbstractAction implements LayerAction {
        private final CanvecLayer parent;
        private MaxZoomAction(CanvecLayer parent) {
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Do nothing
        }

        @Override
        public boolean supportLayers(List<Layer> layers) {
            return false;
        }

        @Override
        public Component createMenuComponent() {
            JMenu maxZoomMenu = new JMenu("max zoom");
            maxZoomMenu.add(new JMenuItem(new SetMaxZoom(parent, 1)));
            maxZoomMenu.add(new JMenuItem(new SetMaxZoom(parent, 2)));
            maxZoomMenu.add(new JMenuItem(new SetMaxZoom(parent, 3)));
            maxZoomMenu.add(new JMenuItem(new SetMaxZoom(parent, 4)));
            return maxZoomMenu;
        }
    }

    private static final class AllowDownload extends AbstractAction {
        private final CanVecTile tile;
        private AllowDownload(CanVecTile tile) {
            super(tile.tileid);
            this.tile = tile;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            tile.canDownload = true;
        }
    }

    private static final class OpenOsmAction extends AbstractAction implements LayerAction {
        private CanvecLayer layer;
        private OpenOsmAction(CanvecLayer layer) {
            this.layer = layer;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Do nothing
        }

        @Override
        public Component createMenuComponent() {
            JMenu openOsm = new JMenu("Open tile");
            for (int i = 0; i < layer.openable.size(); i++) {
                openOsm.add(new JMenuItem(new DoOpenOsm(layer.openable.get(i))));
            }
            return openOsm;
        }

        @Override
        public boolean supportLayers(List<Layer> layers) {
            return false;
        }
    }

    private static final class DoOpenOsm extends AbstractAction {
        private final CanVecTile tile;
        private DoOpenOsm(CanVecTile tile) {
            super(tile.tileid);
            this.tile = tile;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tile.loadRawOsm();
        }
    }

    private static final class DownloadCanvecAction extends AbstractAction implements LayerAction {
        private CanvecLayer parent;
        private DownloadCanvecAction(CanvecLayer parent) {
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Do nothing
        }

        @Override
        public boolean supportLayers(List<Layer> layers) {
            return false;
        }

        @Override
        public Component createMenuComponent() {
            JMenu downloadCanvec = new JMenu("Download zip's");
            for (int i = 0; i < parent.downloadable.size(); i++) {
                downloadCanvec.add(new JMenuItem(new AllowDownload(parent.downloadable.get(i))));
            }
            return downloadCanvec;
        }
    }

    void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public String getToolTipText() {
        return tr("canvec tile helper");
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        // Do nothing
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
        // Do nothing
    }

    @Override
    public Icon getIcon() {
        return layerIcon;
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        downloadable = new ArrayList<>();
        openable = new ArrayList<>();
        // loop over each canvec tile in the db and check bounds.intersects(Bounds)
        g.setColor(Color.red);
        for (int i = 0; i < tiles.size(); i++) {
            CanVecTile tile = tiles.get(i);
            tile.paint(g, mv, bounds, maxZoom);
        }
    }
}
