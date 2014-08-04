package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import org.junit.*;
import static org.junit.Assert.*;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.ExceptValueModel;

class ExceptValueModelTest {

	@Test
	public void constructor() {
		new ExceptValueModel()
		
		def evm = new ExceptValueModel(null)
		evm = new ExceptValueModel("")	
		evm = new ExceptValueModel("  ")
		evm = new ExceptValueModel("hgv")
		evm = new ExceptValueModel("hgv;psv")
		evm = new ExceptValueModel("non_standard")
	}
	
	@Test
	public void setValue() {
		def evm
		
		// null value allowed - means no vehicle exceptions 
		evm = new ExceptValueModel()
		evm.setValue(null)
		assert evm.getValue() == ""
		assert evm.isStandard()
		
		// empty string allowed - means no vehicle expections 
		evm = new ExceptValueModel()
		evm.setValue("")
		assert evm.getValue() == ""
		assert evm.isStandard()

		// a single standard vehicle exeption 
		evm = new ExceptValueModel()
		evm.setValue("hgv")
		assert evm.getValue() == "hgv"
		assert evm.isVehicleException("hgv")
		assert ! evm.isVehicleException("psv")
		assert evm.isStandard()

		// two standard vehicle exceptions 
		evm = new ExceptValueModel()
		evm.setValue("hgv;psv")
		assert evm.getValue() == "hgv;psv"
		assert evm.isVehicleException("hgv")
		assert evm.isVehicleException("psv")
		assert evm.isStandard()
		
		// white space and lowercase/uppercase mix allowed. Should be normalized
		// by the except value model
		evm = new ExceptValueModel()
		evm.setValue(" hGv ; PsV  ")
		assert evm.getValue() == "hgv;psv"
		assert evm.isVehicleException("hgv")
		assert evm.isVehicleException("psv")
		assert evm.isStandard()
		
		// non standard value allowed 
		evm = new ExceptValueModel()
		evm.setValue("Non Standard")
		assert evm.getValue() == "Non Standard"
		assert !evm.isVehicleException("hgv")
		assert !evm.isVehicleException("psv")
		assert !evm.isStandard()
	}	 
}
