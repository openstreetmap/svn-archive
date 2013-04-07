package org.openstreetmap.josm.plugins.imagerycache;

/**
 * Interface that contain methods to work with tile database
 * each tile is described by source and id (id is 
 */
public interface TileDAO {
    public DBTile getById(String source, long id);
    public void updateModTime(String source, long id, DBTile dbTile);
    public void updateTile(String source, long id, DBTile dbTile);
    public void deleteTile(String source, long id);
}
