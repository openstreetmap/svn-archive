package org.openstreetmap.josm.plugins.pt_assistant;

import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * Abstract class for tests that require JOSM's preferences running.
 *
 * @author nokutu, modified
 *
 */
@Ignore
public abstract class AbstractTest {

 public static final String PATH_TO_DL131_BEFORE = "test/data/DL131_before.osm"; // 
 public static final String PATH_TO_DL131_AFTER = "test/data/DL131_after.osm";
 
 public static final String PATH_TO_DL4_BEFORE = "test/data/DL4_before.osm";
 public static final String PATH_TO_DL4_AFTER = "test/data/DL4_after.osm";
 
 public static final String PATH_TO_DL49_BEFORE = "test/data/DL49_before.osm"; // has wrong way sorting
 public static final String PATH_TO_DL49_AFTER = "test/data/DL49_after.osm";
 
 public static final String PATH_TO_DL60_BEFORE = "test/data/DL60_before.osm";
 public static final String PATH_TO_DL60_AFTER = "test/data/DL60_after.osm";
 
 public static final String PATH_TO_DL94_BEFORE = "test/data/DL94_before.osm";
 public static final String PATH_TO_DL94_AFTER = "test/data/DL94_after.osm";
 
 public static final String PATH_TO_DL286_BEFORE = "test/data/DL286_before.osm";
 public static final String PATH_TO_DL286_AFTER = "test/data/DL286_after.osm";
 
 public static final String PATH_TO_TEC366_BEFORE = "test/data/TL366_before.osm";
 public static final String PATH_TO_TEC366_AFTER = "test/data/TL366_after.osm";
 
 public static final String PATH_TO_PLATFORM_AS_WAY = "test/data/route-with-platform-as-way.osm";
 
 
 
  /**
   * Initiates the basic parts of JOSM.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    TestUtil.initPlugin();
  }
}