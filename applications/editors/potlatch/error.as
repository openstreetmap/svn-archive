
	// =====================================================================
	// Error handling

	function handleError(code,msg,result) {
		if (code==-2 && msg.indexOf('allocate memory')>-1) { code=-1; }
		if (msg.indexOf('changeset')>-1) { _root.changeset=null; msg+=iText('newchangeset'); }	// we really need a dedicated error code for this
		if (msg.indexOf('logged')>-1) { loginDialogue(); return; }
		switch (code) {
			case -1:	errorDialogue(msg,150); break;
			case -2:	errorDialogue(msg+iText('emailauthor'),200); break;
			case -3:	resolveConflict(msg,result); break;		// version conflict
			case -4:	deleteObject(msg,result[0]); break;		// object not found
			case -5: 	break;									// not executed due to previous error
		}
	}

	function errorDialogue(t,h) {
		abortUpload();
		_root.windows.attachMovie("modal","error",++windowdepth);
		_root.windows.error.init(350,h,new Array(iText('ok')),null);
		_root.windows.error.box.createTextField("prompt",2,7,9,325,h-40);
		writeText(_root.windows.error.box.prompt,t);
		_root.windows.error.box.prompt.selectable=true;
	}

	function handleWarning() {
		abortUpload();
		_root.windows.attachMovie("modal","error",++windowdepth);
		_root.windows.error.init(275,130,new Array(iText('cancel'),iText('retry')),handleWarningAction);
		_root.windows.error.box.createTextField("prompt",2,7,9,250,100);
        _root.uploading=false;
		if (writeError) {
			writeText(_root.windows.error.box.prompt,iText('error_connectionfailed'));
		} else {
			writeText(_root.windows.error.box.prompt,iText('error_readfailed'));
		}
	};

	function handleWarningAction(choice) {
		_root.panel.i_warning._visible=false;
		_root.writesrequested=0;
		_root.waysrequested=_root.waysreceived=_root.whichrequested=_root.whichreceived=0;
        var retry=(choice==iText('retry'));
		if (writeError) {
			// loop through all ways which are uploading, and reupload
			if (retry) { establishConnections(); }
			for (var q in _root.nodes) {
				if (_root.nodes[q].uploading) {
					_root.nodes[q].uploading=false;
					_root.nodes[q].clean=false;
				}
			}
			for (var q in _root.map.ways) {
				if (_root.map.ways[q].uploading) {
					_root.map.ways[q].uploading=false;
					_root.map.ways[q].clean=false;
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
	// Login management
	
	function loginDialogue() {
		_root.windows.attachMovie("modal","login",++windowdepth);
		_root.windows.login.init(300,140,new Array(iText('cancel'),iText('retry')),retryLogin); 
		var box=_root.windows.login.box;
		
		box.createTextField("title",10,7,7,280,20);
		box.title.text = iText("login_title");
		with (box.title) { setTextFormat(boldText); selectable=false; type='dynamic'; }
		adjustTextField(box.title);

		box.createTextField('prompt',11, 8,33,280,40);
		with (box.prompt) { text=iText('login_retry'); setTextFormat(plainSmall); selectable=false; }
		adjustTextField(box.prompt);

		box.createTextField('uidt',12, 8,63,160,20);
		with (box.uidt) { text=iText('login_uid'); setTextFormat(plainSmall); selectable=false; }
		adjustTextField(box.uidt); var r=box.uidt.textWidth+25;
		box.createTextField('uidi',13,r,63,120,17);
		box.uidi.setNewTextFormat(plainSmall); box.uidi.type='input';
		box.uidi.text='';
		box.uidi.background=true; box.uidi.backgroundColor=0xDDDDDD;
		box.uidi.border=true; box.uidi.borderColor=0xFFFFFF;

		box.createTextField('pwdt',14, 8,83,160,20);
		with (box.pwdt) { text=iText('login_pwd'); setTextFormat(plainSmall); selectable=false; }
		adjustTextField(box.pwdt);
		box.createTextField('pwdi',15,r,83,120,17);
		box.pwdi.setNewTextFormat(plainSmall); box.pwdi.type='input';
		box.pwdi.text='';
		box.pwdi.background=true; box.pwdi.backgroundColor=0xDDDDDD;
		box.pwdi.border=true; box.pwdi.borderColor=0xFFFFFF;
		box.pwdi.password=true;
	}
	
	function retryLogin(choice) {
		if (choice==iText('retry')) {
			_root.usertoken=_root.windows.login.box.uidi.text+":"+_root.windows.login.box.pwdi.text;
		}
		_root.changeset=null;
        _root.uploading=false;
		removeMovieClip(_root.windows.login);
		removeMovieClip(_root.windows.upload);
		removeMovieClip(_root.windows.pleasewait);
		if (!_root.sandbox) { _root.changecomment=''; startChangeset(true); }
		writeError=true; handleWarningAction(choice);
	}

	// =====================================================================
	// Conflict management

	function resolveConflict(rootobj,conflictobj) {
		//  rootobj is     [root object, id]
		//  conflictobj is [conflicting object, id, version]
		_root.croot=rootobj; _root.cconflict=conflictobj;		// _root. so we can access them from the "Ok" function
		var t1,t2; var id=rootobj[1];

		abortUpload();
		switch (rootobj[0]) {
			case 'way':		var n=getName(_root.map.ways[id].attr,waynames); if (n) { n=" ("+n+")"; }
							t1=iText("conflict_waychanged",id,n);
							t2=iText("conflict_visitway"); break;
			case 'node':	var n=getName(_root.map.pois[id].attr,nodenames); if (n) { n=" ("+n+")"; }
							t1=iText("conflict_poichanged",id,n);
							t2=iText("conflict_visitpoi"); break;
			case 'relation':var n=_root.map.relations[id].verboseText(); if (n) { n=" ("+n+")"; }
							t1=iText("conflict_relchanged",id,n);
							t2=""; break;
		}
		_root.windows.attachMovie("modal","resolve",++windowdepth);
		_root.windows.resolve.init(300,150,new Array('Ok'),function() { handleConflictAction(_root.windows.resolve.box.fixoption.selected); });
		var box=_root.windows.resolve.box;

		box.createTextField("prompt1",2,7,9,282,200);
		writeText(box.prompt1,t1); var t=box.prompt1.textHeight;

		box.attachMovie("radio","fixoption",3);
		box.fixoption.addButton(10,t+22,iText('conflict_download'));
		box.fixoption.addButton(10,t+40,iText('conflict_overwrite'));
		box.fixoption.select(1);

		if (t2) {
			box.createTextField("prompt2",4,7,t+60,282,20);
			writeText(box.prompt2,t2);
		}
	}
	
	function handleConflictAction(choice) {
		var tx,ty;
		var ctype=_root.croot[0]; var cid=_root.croot[1];
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
			var rtype=_root.cconflict[0];
			var rid  =_root.cconflict[1];
			var v    =_root.cconflict[2];
			switch (ctype+","+_root.cconflict[0]) {
				case 'way,way':				_root.map.ways[cid].version=v; _root.map.ways[cid].clean=false; break;
				case 'way,node':			_root.nodes[rid].version=v; _root.nodes[rid].markDirty(); _root.map.ways[cid].clean=false; break;
				case 'node,node':			_root.nodes[cid].version=v; _root.nodes[cid].markDirty(); _root.map.pois[cid].clean=false; break;
				case 'relation,relation':	_root.map.relations[cid].version=v; _root.map.relations[cid].clean=false; break;
			}
		}
	}
