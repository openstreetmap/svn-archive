// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cache;
//License: GPL. For details, see LICENSE file.

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;

import org.openstreetmap.josm.plugins.streetside.cache.StreetsideCache.Type;
import org.openstreetmap.josm.plugins.streetside.utils.TestUtil.StreetsideTestRules;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class StreetsideCacheTest {

  @Rule
  public JOSMTestRules rules = new StreetsideTestRules().preferences();

  @Test
  public void test() {
    StreetsideCache cache = new StreetsideCache("00000", Type.FULL_IMAGE);
    assertNotNull(cache.getUrl());
    assertNotNull(cache.getCacheKey());

    assertFalse(cache.isObjectLoadable());

    cache = new StreetsideCache("00000", Type.THUMBNAIL);
    assertNotNull(cache.getCacheKey());
    assertNotNull(cache.getUrl());

    cache = new StreetsideCache(null, null);
    assertNull(cache.getCacheKey());
    assertNull(cache.getUrl());
  }
}
