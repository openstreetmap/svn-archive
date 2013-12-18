package org.openstreetmap.josm.plugins.imagerycache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Serializable class to store one tile
 */
public class DBTile implements Serializable {
    public byte[] data;
    public Map<String, String> metaData;
    public long lastModified;

    public DBTile(DBTile dbTile) {
        data = dbTile.data.clone();
        if (dbTile.metaData != null) {
            metaData = new HashMap<String, String>(dbTile.metaData);
        } else {
            metaData = new HashMap<String, String>();
        }
        lastModified = dbTile.lastModified;
    }

    public DBTile() {
    }
    
}
