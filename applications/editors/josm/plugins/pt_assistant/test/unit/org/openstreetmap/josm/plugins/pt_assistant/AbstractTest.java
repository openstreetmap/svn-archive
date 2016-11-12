// License: GPL. For details, see LICENSE file.
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

 public static final String PATH_TO_ROUNDABOUT_ONEWAY = "test/data/duesseldorf_roundabout.osm";

 public static final String PATH_TO_ROAD_TYPE_ERROR = "test/data/road-type.osm";

 public static final String PATH_TO_ONEWAY_BAD_MEMBER_SORTING = "test/data/oneway-bad-member-sorting.osm";

 public static final String PATH_TO_ONEWAY_WRONG_DIRECTION = "test/data/oneway-wrong-direction.osm";
 public static final String PATH_TO_ONEWAY_WRONG_DIRECTION2 = "test/data/oneway-wrong-direction2.osm";

 public static final String PATH_TO_SOLITARY_STOP_POSITION = "test/data/solitary-stop-position.osm";

 public static final String PATH_TO_STOP_AREA_MEMBERS = "test/data/stop-area-members.osm";
 public static final String PATH_TO_STOP_AREA_RELATIONS = "test/data/stop-area-relations.osm";
 public static final String PATH_TO_STOP_AREA_NO_STOPS = "test/data/stop-area-no-stops.osm";
 public static final String PATH_TO_STOP_AREA_MANY_STOPS = "test/data/stop-area-many-stops.osm";
 public static final String PATH_TO_STOP_AREA_NO_PLATFORMS = "test/data/stop-area-no-platform.osm";
 public static final String PATH_TO_STOP_AREA_MANY_PLATFORMS = "test/data/stop-area-many-platforms.osm";


 public static final String PATH_TO_SEGMENT_TEST = "test/data/segment-test.osm";

  /**
   * Initiates the basic parts of JOSM.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    TestUtil.initPlugin();
  }
}
