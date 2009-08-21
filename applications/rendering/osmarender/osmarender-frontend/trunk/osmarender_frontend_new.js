(function () {
	var osmarender_frontend = window.osmarender_frontend = function () {
		return new osmarender_frontend();
	};

	osmarender_frontend.version = "0.3.20090821-nightly";
	osmarender_frontend.date = "20090821"

	dojo.registerModulePath("osmafrontend","../../osmafrontend");
	dojo.registerModulePath("cmyk","../../cmyk");
	dojo.registerModulePath("juice","../../juice");
	dojo.require("dojo.parser");
	dojo.require("dijit.layout.ContentPane");
	dojo.require("dijit.layout.BorderContainer");
	dojo.require("osmafrontend.version");
	dojo.require("osmafrontend.load");

	dojo.addOnLoad(function () {
	
	})
}());
