// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.canvec_helper;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.UploadPolicy;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.io.importexport.OsmImporter;
import org.openstreetmap.josm.gui.io.importexport.OsmImporter.OsmImporterData;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.Logging;

class CanVecTile {
    private CanvecLayer layer;
    boolean canDownload = false;
    private List<String> subTileIds = new ArrayList<>();
    private boolean zipScanned = false;

    private List<CanVecTile> subTiles = new ArrayList<>();
    private boolean subTilesMade = false;

    private List<String> index;
    private int depth;

    private int corda;
    private int cordc;
    private boolean valid = false;
    private String cordb;
    private String cordd;
    private Bounds bounds;
    String tileid;
    CanVecTile(String tileid, CanvecLayer layer) {
        String parta, partb, partc, partd;
        parta = tileid.substring(0, 3);
        partb = tileid.substring(3, 4);
        partc = tileid.substring(4, 6);
        partd = tileid.substring(6);
        int a, c;
        a = Integer.parseInt(parta);
        c = Integer.parseInt(partc);
        realInit(a, partb, c, partd, layer, new ArrayList<String>());
    }

    CanVecTile(int a, String b, int c, String d, CanvecLayer layer, List<String> index) {
        realInit(a, b, c, d, layer, index);
    }

    private void realInit(int a, String b, int c, String d, CanvecLayer layer, List<String> index) {
        this.index = index;
        this.layer = layer;
        corda = a;
        cordb = b;
        cordc = c;
        cordd = d;
        double zeroPointLat, zeroPointLon;
        double latSpan, lonSpan;
        double lat2, lon2;
        if ((a >= 0) && (a <= 119)) { // main block of tiles
            int column = a / 10;
            int row = a % 10;
            if (row > 6) {
                // cant handle x7 x8 and x9 yet
                return;
            }
            zeroPointLat = 40 + 4 * row;
            zeroPointLon = -56 - 8 * column;

            // size of each grid
            if (row <= 6) {
                // each is 4x8 degrees, broken into a 4x4 grid
                latSpan = 4;
                lonSpan = 8;
                depth = 1;
            } else {
                return;
            }
        } else { // last few tiles, very far north
            return;
        }

        // a 4x4 grid of A thru P
        // map A-P to 1-16
        int grid2;
        if (b.isEmpty()) grid2 = 0;
        else grid2 = b.charAt(0) - 64;
        int[] rows1 = {0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3};
        int[] cols1 = {0, 3, 2, 1, 0, 0, 1, 2, 3, 3, 2, 1, 0, 0, 1, 2, 3};
        lat2 = zeroPointLat + (latSpan/4)*rows1[grid2];
        lon2 = zeroPointLon + (lonSpan/4)*cols1[grid2];

        if (grid2 != 0) {
            latSpan = latSpan / 4;
            lonSpan = lonSpan / 4;
            depth = 2;
        }

        int[] rows3 = {0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3};
        lat2 = lat2 + (latSpan/4)*rows3[c];
        int[] cols3 = {0, 3, 2, 1, 0, 0, 1, 2, 3, 3, 2, 1, 0, 0, 1, 2, 3};
        lon2 = lon2 + (lonSpan/4)*cols3[c];

        if (c != 0) {
            latSpan = latSpan / 4;
            lonSpan = lonSpan / 4;
            depth = 3;
        }

        if (!cordd.isEmpty()) {
            depth = 4;
            String[] foo = cordd.split("\\.");
            for (int i = 0; i < foo.length; i++) {
                int cell;
                if ("osm".equals(foo[i])) break;
                if (foo[i].isEmpty()) continue;
                try {
                    cell = Integer.parseInt(foo[i]);
                } catch (NumberFormatException e) {
                    continue;
                }
                switch (cell) {
                case 0:
                    break;
                case 1:
                    lat2 = lat2 + latSpan/2;
                    break;
                case 2:
                    lat2 = lat2 + latSpan/2;
                    lon2 = lon2 + lonSpan/2;
                    break;
                case 3:
                    lon2 = lon2 + lonSpan/2;
                    break;
                }
                latSpan = latSpan/2;
                lonSpan = lonSpan/2;
            }
        }

        bounds = new Bounds(lat2, lon2, lat2+latSpan, lon2+lonSpan);
        if (cordb.isEmpty()) this.tileid = String.format("%03d", corda);
        else if (cordc == 0) this.tileid = String.format("%03d%s", corda, cordb);
        else if (cordd.isEmpty()) this.tileid = String.format("%03d%s%02d", corda, cordb, cordc);
        else this.tileid = String.format("%03d%s%02d%s", corda, cordb, cordc, cordd);
        valid = true;
    }

    boolean isValid() {
        return valid;
    }

    String getTileId() {
        return this.tileid;
    }

    boolean isVisible(Bounds view) {
        return view.intersects(bounds);
    }

    Point[] getCorners(MapView mv) {
        LatLon min = bounds.getMin();
        LatLon max = bounds.getMax();
        LatLon x1 = new LatLon(min.lat(), max.lon());
        LatLon x2 = new LatLon(max.lat(), min.lon());
        return new Point[] {
            mv.getPoint(min), // south west
            mv.getPoint(x1),
            mv.getPoint(max),
            mv.getPoint(x2) // north west
            };
    }

    public String getDownloadUrl() {
        return String.format("http://ftp2.cits.rncan.gc.ca/OSM/pub/%1$03d/%2$s/%1$03d%2$s%3$02d.zip", corda, cordb, cordc);
    }

    private ZipFile openZip() throws IOException {
        File downloadPath = layer.plugin.getPluginDirs().getUserDataDirectory(true);
        CachedFile tileZip = new CachedFile(getDownloadUrl()).setDestDir(downloadPath.toString());
        return new ZipFile(tileZip.getFile());
    }

    void downloadSelf() {
        if (zipScanned) return;
        ZipFile zipFile;
        try {
            zipFile = openZip();
        } catch (IOException e) {
            Logging.error(e);
            return;
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if ("Metadata.txt".equals(entry.getName())) continue;
            subTileIds.add(entry.getName());
            zipScanned = true;
            CanVecTile finalTile = new CanVecTile(entry.getName(), layer);
            if (finalTile.isValid()) {
                subTiles.add(finalTile);
            }
        }
    }

    void loadRawOsm() {
        ZipFile zipFile;
        try {
            zipFile = openZip();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (tileid.equals(entry.getName())) {
                    InputStream rawtile = zipFile.getInputStream(entry);
                    OsmImporter importer = new OsmImporter();
                    OsmImporterData temp = importer.loadLayer(rawtile, null, entry.getName(), null);
                    MainApplication.worker.submit(temp.getPostLayerTask());
                    MainApplication.getLayerManager().addLayer(temp.getLayer());
                    temp.getLayer().data.setUploadPolicy(UploadPolicy.NORMAL);
                }
            }
        } catch (IOException | IllegalDataException e) {
            Logging.error(e);
            return;
        }
    }

    private void makeSubTiles(int layer) {
        List<String> buffer = new ArrayList<>();
        Pattern p;
        if (subTilesMade) return;
        switch (layer) {
        case 1:
            p = Pattern.compile("\\d\\d\\d([A-Z]).*");
            String lastCell = "";
            for (int i = 0; i < index.size(); i++) {
                Matcher m = p.matcher(index.get(i));
                m.matches();

                String cell = m.group(1);
                if (cell.equals(lastCell)) {
                    buffer.add(m.group(0));
                } else if (lastCell.isEmpty()) {
                    buffer.add(m.group(0));
                } else {
                    subTiles.add(new CanVecTile(corda, lastCell, 0, "", this.layer, buffer));
                    buffer = new ArrayList<>();
                    buffer.add(m.group(0));
                }
                lastCell = cell;
            }
            subTiles.add(new CanVecTile(corda, lastCell, 0, "", this.layer, buffer));
            break;
        case 2:
            p = Pattern.compile("\\d\\d\\d[A-Z](\\d\\d).*");
            int lastCell2 = -1;
            for (int i = 0; i < index.size(); i++) {
                Matcher m = p.matcher(index.get(i));
                m.matches();

                int cell = Integer.parseInt(m.group(1));
                if (cell == lastCell2) {
                    buffer.add(m.group(0));
                } else if (lastCell2 == -1) {
                    buffer.add(m.group(0));
                } else {
                    subTiles.add(new CanVecTile(corda, cordb, lastCell2, "", this.layer, buffer));
                    buffer = new ArrayList<>();
                    buffer.add(m.group(0));
                }
                lastCell2 = cell;
            }
            if (lastCell2 != -1) subTiles.add(new CanVecTile(corda, cordb, lastCell2, "", this.layer, buffer));
            break;
        }
        subTilesMade = true;
    }

    void paint(Graphics2D g, MapView mv, Bounds bounds, int maxZoom) {
        boolean showSubTiles = false;
        if (!isVisible(bounds)) return;
        if (depth == 4) {
            layer.openable.add(this);
        }
        if ((depth == 3) && (bounds.getArea() < 0.5)) { // 022B01
            if (zipScanned) {
                showSubTiles = true;
            } else if (canDownload) {
                downloadSelf();
                showSubTiles = true;
            } else {
                layer.downloadable.add(this);
            }
        } else if ((depth == 2) && (bounds.getArea() < 20)) { // its a layer2 tile
            makeSubTiles(2);
            showSubTiles = true;
        } else if ((depth == 1) && (bounds.getArea() < 40)) { // its a layer1 tile and zoom too small
            // draw layer2 tiles for self
            makeSubTiles(1);
            showSubTiles = true;
        }
        if (showSubTiles && (depth < maxZoom)) {
            for (int i = 0; i < subTiles.size(); i++) {
                CanVecTile tile = subTiles.get(i);
                tile.paint(g, mv, bounds, maxZoom);
            }
        } else {
            Point[] corners = getCorners(mv);
            int[] xs = {corners[0].x, corners[1].x, corners[2].x, corners[3].x };
            int[] ys = {corners[0].y, corners[1].y, corners[2].y, corners[3].y };
            Polygon shape = new Polygon(xs, ys, 4);
            g.draw(shape);
            g.drawString(getTileId(), corners[0].x, corners[0].y);
        }
    }
}
