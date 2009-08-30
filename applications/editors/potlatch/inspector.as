
	// ================================================================
	// Inspector
	
	function toggleInspector() {
		if (_root.palettes.inspector) {
			removeMovieClip(_root.palettes.inspector);
		} else {
			_root.palettes.attachMovie("palette","inspector",++palettedepth);
			_root.palettes.inspector.initHTML(Stage.width-210,Stage.height-300,"Inspector","<h3>Connections</h3><p></p>");
			updateInspector();
		}
	};

	function updateInspector() {
		if (!_root.palettes.inspector) { return; }

		var t='';
		if (_root.poiselected) { t=_root.map.pois[poiselected].inspect(); }
		else if (_root.pointselected>-2) { t=_root.ws.path[pointselected].inspect(); }
		else if (_root.ws) { t=_root.ws.inspect(); }
		_root.palettes.inspector.desc.htmlText=t;
	};
