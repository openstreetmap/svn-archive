package org.openstreetmap.josm.plugins.contourmerge
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses([
	ContourMergeModelTest.class,
	WaySliceTest.class
])
class AllUnitTests {}
