(function () {
	var osmarender_frontend = window.osmarender_frontend = function () {
		return new osmarender_frontend();
	};

	osmarender_frontend.version = "0.3.20091016-nightly";
	osmarender_frontend.date = "20091016"
	osmarender_frontend.isDebug = true;
	osmarender_frontend.isOnline = false;

	dojo.registerModulePath("osmafrontend","../../osmafrontend");
	dojo.registerModulePath("cmyk","../../cmyk");
	dojo.registerModulePath("juice","../../juice");
	dojo.require("dojo.parser");
	dojo.require("dijit.layout.ContentPane");
	dojo.require("dijit.layout.BorderContainer");
	dojo.require("osmafrontend.version");
	dojo.require("osmafrontend.load.load");

	dojo.addOnLoad(function () {
	
	})

	osmarender_frontend.loadOsmRule = function (osm_file,rule_file) {
	}
}());
