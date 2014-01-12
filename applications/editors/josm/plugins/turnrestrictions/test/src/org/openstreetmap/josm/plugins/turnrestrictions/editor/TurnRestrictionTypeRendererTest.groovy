package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.junit.Assert.*;
import org.junit.*;

import groovy.util.GroovyTestCase;

import java.awt.Component
import org.openstreetmap.josm.plugins.turnrestrictions.fixtures.JOSMFixture;

class TurnRestrictionTypeRendererTest extends GroovyTestCase{

	@Before
	public void setUp() {
		JOSMFixture.createUnitTestFixture().init()			
	}
	
	@Test
	public void test_Constructor() {
		TurnRestrictionTypeRenderer renderer = new TurnRestrictionTypeRenderer();
		
		assert renderer.@icons != null
		assert renderer.@icons.get(TurnRestrictionType.NO_LEFT_TURN) != null
	}
	
	@Test
	public void test_getListCellRendererComponent_1() {
		TurnRestrictionTypeRenderer renderer = new TurnRestrictionTypeRenderer();
		
		def c = renderer.getListCellRendererComponent(null, null, 0, false, false)		
		assert c.getIcon() == null
		assert c.getText() != null
		
		c = renderer.getListCellRendererComponent(null, "non-standard-value", 0, false, false)		
		assert c.getIcon() == null
		assert c.getText() == "non-standard-value"	

		c = renderer.getListCellRendererComponent(null, TurnRestrictionType.NO_LEFT_TURN, 0, false, false)		
		assert c.getIcon() == renderer.@icons.get(TurnRestrictionType.NO_LEFT_TURN)
		assert c.getText() == TurnRestrictionType.NO_LEFT_TURN.getDisplayName()
	}
}
