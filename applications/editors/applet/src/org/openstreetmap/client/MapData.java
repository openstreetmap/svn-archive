package org.openstreetmap.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.Way;

/**
 * Low-level local store of map data - not ensuring referential integrity / 
 * validity of objects, but acting as a convenient holder, giving some typesafe 
 * accessors, and providing for thread-safe access.
 * 
 * The data is intended to reflect the state of portion of a remote master store
 * (i.e. OSM map database), bearing in mind that  
 * 
 * <h3> Assumed caller roles and thread-safe usage</h3>
 * 
 * Callers editing the map data transactionally, or protecting iterator use against
 * concurrently modification, should make all associated calls to the instance,
 * iterator or constituent objects from within a sync block on the this mapdata instance.
 * The map lock is safe for deadlocks if used as a lowest-level lock - i.e. don't acquire
 * any other locks from within such a sync block, so that map lock always last acquired.
 * 
 * It is assumed that the applet draw thread accesses the map in a read-only manner.
 * 
 * It is assumed that there is a caller role/thread that independently fetches updated
 * map data from the master source (i.e. OSM database), which (alone) calls 
 * <code>updateMap</code> to refresh the underlying map data wholesale.
 * 
 * <h3>Edit commit checking</h3>
 * 
 * As such, when accessors/editors retain information / make assumptions about local 
 * map state outside of a map sync, then the caller should beware that these assumptions 
 * may no longer hold true.
 * 
 * While this is necessarily acceptable (compare: our local copy of map will not 
 * necessarily reflect the server data, unless locking APIs introduced), in order to keep 
 * the local map uncorrupted, and so that user edits perform the actual changes
 * expected of them, when edits are finally to be committed to the map based on such 
 * assumptions, they should ideally be done (transactionally, under sync), first checking 
 * that those assumptions still hold true.
 * 
 * <h4>Map state assumption checking - a.k.a. coping with refresh from server</h4>
 * 
 * <code>getUpdateId()</code> is providea simple way of making basic assumption checks.
 * 
 * If the <code>getUpdateId()</code> result has changed from when map state information
 * last read, a caller knows the map has been updated from server, and they should discard
 * / allow for their assumptions, i.e. perhaps: 
 * <ul>
 * <li>refresh display data from map; or</li>
 * <li>cancel editing (perhaps for simple edits, e.g. node move); or</li>
 * <li>continue edit process until server commit, then update local map via requesting server refresh; or</li>
 * <li>re-acquire references to map objects (checking existence) and:
 *   <ul>
 *     <li>refresh data from object, perhaps checking its data is still compatible, and continue; or</li>
 *     <li>continue to server commit only, if not in new map (i.e. edited item may not be within scope of latest
 *     server refresh, e.g. dragged map to new location)</li>
 *   </ul>
 * </li>
 * </ul>
 * 
 * Obviously 
 * <ul>
 *   <li>it is assumed any editing processes (e.g. server post-commit code, GUI editing code) cooperate
 *     correctly, so that their assumptions will hold true without data updates from server. </li> 
 *   <li>these are suggested only: handling is down to the implementation of the editing processes.</li>
 * </ul>
 * 
 * TODO Describe current working of edit commits, rollback and server updates
 *  - not sure precisely what intended model is w.r.t local and remote maps
 */
public class MapData {
  
  /**
   * Map of OSMNodes (may or may not be projected into screen space).
   * Type: String node.key() -> Node node
   *  -or- Long (random gen) -> new Node (no ID yet from server)
   */
  private Map nodes = new HashMap();

  /**
   * Collection of OSMLines
   * Type: String line.key() -> Line (or LineOnlyId) line
   *  -or- Long (random gen) -> new Line (no ID yet from server)
   */
  private Map lines = new HashMap();
  
  /**
   * Collection of OSM ways
   * Type: String way.key() -> Way way
   * -or- Long (random gen) -> new Way (no ID yet from server)
   */
  private Map ways = new HashMap();

  /**
   * Increments each time this instance updated (acquires whole new dataset).
   */
  private int updateId = 0;
  
  // TODO replace map accessors or trust to convention? 
  synchronized public Map getLines() {
    return lines;
  }
  synchronized public Map getNodes() {
    return nodes;
  }
  synchronized public Map getWays() {
    return ways;
  }

  synchronized public void putNode(Node n) {
    nodes.put(n.key(), n);
  }
  /**
   * Adds at temporary key (because keys usually from id, but all new ids are 0).
   * @param tempKey Temporary random key.
   * @param n New node, not yet acknowledged from server.
   */
  synchronized public void putNewNode(String tempKey, Node n) {
    nodes.put(tempKey, n);
  }
  synchronized public void putLine(Line l) {
    lines.put(l.key(), l);
  }
  synchronized public void putNewLine(String tempKey, Line l) {
    lines.put(tempKey, l);
  }
  synchronized public void putWay(Way w) {
    ways.put(w.key(), w);
  }
  synchronized public void putNewWay(String tempKey, Way w) {
    ways.put(tempKey, w);
  }
  synchronized public void removeNode(String key) {
    nodes.remove(key);
  }
  synchronized public void removeNode(Node n) {
    removeNode(n.key());
  }
  synchronized public void removeLine(Line line) {
    removeLine(line.key());
  }
  synchronized public void removeLine(String key) {
    lines.remove(key);
  }
  synchronized public void removeWay(String key) {
    ways.remove(key);
  }
  synchronized public void removeWay(Way w) {
    ways.remove(w.key());
  }

  synchronized public Iterator nodesIterator() {
    return nodes.values().iterator(); 
  }
  synchronized public Iterator linesIterator() {
    return lines.values().iterator(); 
  }
  synchronized public Iterator waysIterator() {
    return ways.values().iterator(); 
  }

  synchronized public Line getLine(String lineKey) {
    return (Line) lines.get(lineKey);
  }
  synchronized public Node getNode(String NodeKey) {
    return (Node) nodes.get(NodeKey);
  }
  synchronized public Way getWay(String WayKey) {
    return (Way) ways.get(WayKey);
  }
  
  
  /**
   * Updates alternate (screen) space coords from lat/lon. 
   * @param projection Defines the mapping lat/long -> (screen) space.
   */
  synchronized public void reProject(Projection projection) {
    for (Iterator it = nodesIterator(); it.hasNext();)
      ((Node)it.next()).coor.project(projection);
  }
  
  /**
   * Take on underlying map data from specified instance.
   * 
   * This is done to allow maintenance of this as a consistent lock instance for callers.
   * 
   * Intended to be called by a data updating (thread) independent of the
   * drawing thread and editing thread(s).
   * 
   * @param map Source of map data.
   */
  synchronized public void updateData(MapData map) {
    nodes = map.nodes;
    lines = map.lines;
    ways = map.ways;
    ++updateId;
  }
  
  /**
   * Exposes an identifier that changes if the underlying map data changes.
   * 
   * This allows a editing / accessing caller to determine whether assumptions 
   * about the internal data of the map can be relied upon, given that an
   * map updater (OSM data fetch thread) is working independently.
   * 
   * Assuming local editors / accessors
   * e.g. if the user a property of a node, n, when the map id is '1', a
   * reference to n is   
   *  
   * @return The update identifier, <code>0</code> if not updated since instantiation.
   */
  synchronized public int getUpdateId() {
    return updateId;
  }
}
