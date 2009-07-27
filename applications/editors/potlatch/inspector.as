
	// ================================================================
	// Inspector
	
	function toggleInspector() {
		if (_root.palettes.inspector) {
			removeMovieClip(_root.palettes.inspector);
		} else {
			_root.palettes.attachMovie("palette","inspector",++palettedepth);
			_root.palettes.inspector.initHTML(400,300,"Inspector","<h3>Connections</h3><p>Inspector stuff\ngoes here\nand here</p>");
			updateInspector();
		}
	};

	function updateInspector() {
		_root.palettes.inspector.desc.htmlText=_root.ws.inspect();
	};