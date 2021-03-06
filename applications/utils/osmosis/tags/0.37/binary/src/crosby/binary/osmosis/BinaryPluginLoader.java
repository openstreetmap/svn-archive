// This software is released into the Public Domain.  See copying.txt for details.
package crosby.binary.osmosis;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;

/** Register the binary reading and writing functions. */
public class BinaryPluginLoader implements PluginLoader {
  @Override
  public Map<String, TaskManagerFactory> loadTaskFactories() {
          Map<String, TaskManagerFactory> factoryMap;
          
          factoryMap = new HashMap<String, TaskManagerFactory>();
          factoryMap.put("read-pbf", new OsmosisReaderFactory());
          factoryMap.put("read-bin", new OsmosisReaderFactory());
          factoryMap.put("rb", new OsmosisReaderFactory());
          factoryMap.put("write-pbf", new OsmosisSerializerFactory());
          factoryMap.put("write-bin", new OsmosisSerializerFactory());
          factoryMap.put("wb", new OsmosisReaderFactory());

          factoryMap.put("read-pbf-0.6", new OsmosisReaderFactory());
          factoryMap.put("write-pbf-0.6", new OsmosisSerializerFactory());
          return factoryMap;
    }
  } 
