
	// =====================================================================
	// Error handling

	function handleError(code,msg,result) {
		if (code==-2 && msg.indexOf('allocate memory')>-1) { code=-1; }
		switch (code) {
			case -1:	errorDialogue(msg,150); break;
			case -2:	errorDialogue(msg+iText("\n\nPlease e-mail richard\@systemeD.net with a bug report, saying what you were doing at the time.",'emailauthor'),200); break;
			case -3:	resolveConflict(msg,result); break;		// version conflict
			case -4:	deleteObject(msg,result[0]); break;		// object not found
			case -5: 	break;									// not executed due to previous error
		}
	}

	function errorDialogue(t,h) {
		abortUpload();
		_root.windows.attachMovie("modal","error",++windowdepth);
		_root.windows.error.init(350,h,new Array(iText('Ok','ok')),null);
		_root.windows.error.box.createTextField("prompt",2,7,9,325,h-30);
		writeText(_root.windows.error.box.prompt,t);
		_root.windows.error.box.prompt.selectable=true;
	}

	function handleWarning() {
		abortUpload();
		_root.windows.attachMovie("modal","error",++windowdepth);
		_root.windows.error.init(275,130,new Array(iText('Retry','retry'),iText('Cancel','cancel')),handleWarningAction);
		_root.windows.error.box.createTextField("prompt",2,7,9,250,100);
        _root.uploading=false;
		if (writeError) {
			writeText(_root.windows.error.box.prompt,iText("Sorry - the connection to the OpenStreetMap server failed. Any recent changes have not been saved.\n\nWould you like to try again?",'error_connectionfailed'));
		} else {
			writeText(_root.windows.error.box.prompt,iText("Sorry - the OpenStreetMap server didn't respond when asked for data.\n\nWould you like to try again?",'error_readfailed'));
		}
	};

	function handleWarningAction(choice) {
		_root.panel.i_warning._visible=false;
		_root.writesrequested=0;
		_root.waysrequested=_root.waysreceived=_root.whichrequested=_root.whichreceived=0;
        var retry=(choice==iText('Retry','retry'));
		if (writeError) {
			// loop through all ways which are uploading, and reupload
			if (retry) { establishConnections(); }
			for (var q in _root.map.ways) {
				if (_root.map.ways[q].uploading) {
					_root.map.ways[q].uploading=false;
					_root.map.ways[q].clean=false;
					var z=_root.map.ways[q].path; for (var i in z) { z[i].uploading=false; }
					if (!_root.sandbox && retry) { _root.map.ways[q].upload(); }
				}
			}
			for (var q in _root.map.relations) {
				if (_root.map.relations[q].uploading) {
					_root.map.relations[q].uploading=false;
					_root.map.relations[q].clean=false;
					if (!_root.sandbox && retry) { _root.map.relations[q].upload(); }
				}
			}
			for (var q in _root.map.pois) {
				if (_root.map.pois[q].uploading) {
					_root.map.pois[q].uploading=false;
					_root.map.pois[q].clean=false;
					if (!_root.sandbox && retry) { _root.map.pois[q].upload(); }
				}
			}
			if (_root.sandbox && retry) { prepareUpload(); }
		}
		if (readError) { 
			bigedge_l=bigedge_b= 999999;
			bigedge_r=bigedge_t=-999999;
			whichWays();
		}
		writeError=false; readError=false;
	};

	// Delete object from Potlatch if server reports it doesn't exist

	function deleteObject(type,id) {
		switch (type) {
			case 'way':
				if (_root.map.ways[id]) {
					var w=_root.map.ways[id];
					w.removeNodeIndex();
					memberDeleted('Way', id);
					if (id==wayselected) { stopDrawing(); deselectAll(); }
					removeMovieClip(_root.map.areas[id]);
					removeMovieClip(w);
				}
				break;
			case 'relation':
				if (_root.map.relations[id]) {
					var mems=_root.map.relations[id].members;
					for (var i in mems) { var r=findLinkedHash(mems[i][0],mems[i][1]); delete r[id]; }
					removeMovieClip(_root.map.relations[id]);
				}
				break;
			case 'node':
				if (_root.map.pois[id]) {
					if (id==_poiselected) { deselectAll(); }
					removeMovieClip(_root.map.pois[id]);
				}
				break;
		}
	}

	// =====================================================================
	// Conflict management

	function resolveConflict(msg,result) {
		//  msg[0] is type (node/way)
		//  msg[1] is version
		// (msg[2] is type actually causing conflict, and msg[3] id, if in a way)
		//  result[0] is id
		_root.cmsg=msg; _root.cresult=result;		// _root. so we can access them from the "Ok" function
		var ctype=msg[0]; var cid=result[0];
		var t1,t2;

		abortUpload();
		switch (ctype) {
			case 'way':		var n=getName(_root.map.ways[cid].attr,waynames); if (n) { n=" ("+n+")"; }
							t1=iText("Since you started editing, someone else has changed way $1$2.","conflict_waychanged",cid,n);
							t2=iText("Click 'Ok' to show the way.","conflict_visitway"); break;
			case 'node':	var n=getName(_root.map.pois[cid].attr,nodenames); if (n) { n=" ("+n+")"; }
							t1=iText("Since you started editing, someone else has changed point $1$2.","conflict_poichanged",cid,n);
							t2=iText("Click 'Ok' to show the point.","conflict_visitpoi"); break;
			case 'relation':var n=_root.map.relations[cid].verboseText(); if (n) { n=" ("+n+")"; }
							t1=iText("Since you started editing, someone else has changed relation $1$2.","conflict_relchanged",cid,n);
							t2=""; break;
		}
		_root.windows.attachMovie("modal","resolve",++windowdepth);
		_root.windows.resolve.init(300,150,new Array('Ok'),function() { handleConflictAction(_root.windows.resolve.box.fixoption.selected); });
		var box=_root.windows.resolve.box;

		box.createTextField("prompt1",2,7,9,282,200);
		writeText(box.prompt1,t1); var t=box.prompt1.textHeight;

		box.attachMovie("radio","fixoption",3);
		box.fixoption.addButton(10,t+22,iText("Download their version",'conflict_download'));
		box.fixoption.addButton(10,t+40,iText("Overwrite their version",'conflict_overwrite'));
		box.fixoption.select(1);

		if (t2) {
			box.createTextField("prompt2",4,7,t+60,282,20);
			writeText(box.prompt2,t2);
		}
	}
	
	function handleConflictAction(choice) {
		var tx,ty;
		var ctype=_root.cmsg[0]; var cid=_root.cresult[0];
		if		(ctype=='way'  && _root.map.ways[cid]) { preway=cid;  tx=_root.map.ways[cid].path[0].x; ty=_root.map.ways[cid].path[0].y; }
		else if (ctype=='node' && _root.map.pois[cid]) { prenode=cid; tx=_root.nodes[cid].x; ty=_root.nodes[cid].y; }

		if (choice==1) { deselectAll(); deleteObject(ctype,cid); }	// downloading new version, so delete old one from Potlatch
		if (tx && ty && (tx<long2coord(_root.edge_l) || tx>long2coord(_root.edge_r) || ty<lat2coord(_root.edge_t) || ty>lat2coord(_root.edge_b))) {
			updateCoords(tx,ty);
		}

		if (choice==1) {
			// download new (more up-to-date) version and select it
			whichWays(true);
		} else {
			// select problem object
			if		(ctype=='way' ) { _root.map.ways[cid].select(); }
			else if (ctype=='node') { _root.map.pois[cid].select(); }
			// change version number
			var v=_root.cmsg[1]; var rtype,rid;
			if (_root.cmsg[2]) { rtype=_root.cmsg[2]; rid=_root.cmsg[3]; ctype=ctype+","+rtype; }
			switch (ctype) {
				case 'way,way':		_root.map.ways[cid].version=v; _root.map.ways[cid].clean=false; break;
				case 'way,node':	_root.nodes[rid].version=v; _root.nodes[rid].markDirty(); _root.map.ways[cid].clean=false; break;
				case 'node':		_root.nodes[cid].version=v; _root.nodes[cid].markDirty(); _root.map.pois[cid].clean=false; break;
				case 'relation':	_root.map.relations[cid].version=v; _root.map.relations[cid].clean=false; break;
			}
		}
	}
