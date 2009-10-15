
	// =====================================================================================
	// Offline upload

	// uploadtasks is an array of: [function,hash of IDs]
	// (each function returns a text comment)

	// -------------------------------------------------------------------------------------
	// prepareUpload
	// creates upload task list

	function prepareUpload() {
		var todo;
		if (!_root.sandbox) { return; }
		if (_root.uploading) { return; }
		deselectAll();

		_root.uploadtasks=new Array();

		// Assemble POIs
		var poi=new Object(); todo=0;
		for (var id in _root.map.pois) {
			if (!_root.map.pois[id].clean && !_root.map.pois[id].locked) {
				poi[id]=true; todo++;
				if (todo==5) { uploadtasks.push([uploadPOIs,poi]); poi=new Object(); todo=0; }
			}
		}
		if (todo>0) { uploadtasks.push([uploadPOIs,poi]); }
		
		// Assemble ways
		var way=new Object(); todo=0;
		for (var id in _root.map.ways) {
			if (!_root.map.ways[id].clean && !_root.map.ways[id].locked) {
				way[id]=true; todo++;
				if (todo==5 || _root.map.ways[id].path.length>200) { uploadtasks.push([uploadWays,way]); way=new Object(); todo=0; }
			}
		}
		if (todo>0) { uploadtasks.push([uploadWays,way]); }
		
		// Assemble relations
		var rel=new Object(); todo=0;
		for (var id in _root.map.relations) {
			if (!_root.map.relations[id].clean && !_root.map.relations[id].locked) {
				rel[id]=true; todo++;
				if (todo==5 || _root.map.relations[id].members.length>200) { uploadtasks.push([uploadRelations,rel]); rel=new Object(); todo=0; }
			}
		}
		if (todo>0) { uploadtasks.push([uploadRelations,rel]); }

		// Assemble ways to delete
		var z=_root.waystodelete; todo=false;
		for (var id in z) { todo=true; }
		if (todo) { uploadtasks.push([uploadDeletedWays,deepCopy(_root.waystodelete)]); }

		// Assemble POIs to delete
		var z=_root.poistodelete; todo=false;
		for (var id in z) { todo=true; }
		if (todo) { uploadtasks.push([uploadDeletedPOIs,deepCopy(_root.poistodelete)]); }

		// Check there's something to do
		if (uploadtasks.length==0) { 
		    setAdvice(true,iText('advice_uploadempty'));
			return;
		}
		
		// Ask for changeset comment if not already created
		if (_root.changeset) { if (!renewChangeset()) { startUpload(); } } 
		                else { changesetRequest(iText('prompt_savechanges'),completeClose,''); }
	}

	// -------------------------------------------------------------------------------------
	// startUpload
	// 'Save' button pressed, so set up screen progress display and begin upload

	function startUpload() {
	    _root.uploading=true;

		_root.windows.attachMovie("modal","upload",++windowdepth);
		_root.windows.upload.init(300,250,[iText('cancel')],abortUpload);

        box=_root.windows.upload.box;
		box.createTextField("title",10,7,7,280,20);
		box.title.text = iText("uploading");
		with (box.title) { setTextFormat(boldText); selectable=false; type='dynamic'; }
		adjustTextField(box.title);

        box.createTextField("progress",11,10,40,280,170);
        with (box.progress) {
            background=true; backgroundColor=0xF3F3F3;
            border=true; borderColor=0xFFFFFF;
            type='dynamic'; wordWrap=true; multiline=true;
            setNewTextFormat(plainSmall); text='';
        }
        box.progress.onScroller=function() { _root.windows.upload.box.progress.scroll=_root.windows.upload.box.progress.maxscroll; };

	    operationDone('first');
    }

	// -------------------------------------------------------------------------------------
	// abortUpload
	// Stop upload in case of failure

	function abortUpload() {
		var i,z;
		if (!_root.uploading) { return; }
		_root.uploading=false;
		setAdvice(false,iText('advice_uploadfail'));
		_root.windows.upload.remove();
		z=_root.map.pois; for (i in z) { z[i].uploading=false; }
		z=_root.map.ways; for (i in z) { z[i].uploading=false; }
		z=_root.nodes; for (i in z) { z[i].uploading=false; }
		z=_root.map.relations; for (i in z) { z[i].uploading=false; }
	}

	// ----------------------------------------------------------------------------------------
	// operationDone( id | all | first )
	// removes element from current task from list, and does next if task complete
	// ('all' takes the entire first task; 'first' doesn't take anything, just fires the first)

	function operationDone(id) {
		if (!_root.uploading) { return; }

		var doNext=false;
		if (id=='all') {
			// Entire first event done (e.g. startChangeset)
			_root.uploadtasks.shift(); doNext=true;
		} else if (id=='first') {
			// First task, so we want to fire it
			doNext=true;
		} else {
			// Remove task from list, do next if none left
			var a=_root.uploadtasks[0][1]; delete a[id];
			var c=0; for (i in a) { c++; }
			doNext=(c==0); if (doNext) { 
				_root.uploadtasks.shift();
			}
		}

		if (!doNext) { return; }
		if (_root.uploadtasks.length>0) { 
			_root.uploadtasks[0][0].call(null,_root.uploadtasks[0][1]); 
    		return;
    	}
		
		// Hooray, we're complete
		_root.uploading=false;
		_root.waystodelete=new Object();
		_root.poistodelete=new Object();
//        _root.windows.upload.box.progress.text+="Upload complete";
//		var box=_root.windows.upload.box;
//		box.createEmptyMovieClip(1,1);
//		drawButton(box[1],140,220,iText("ok"),"");
//		box[1].onPress=function() { _root.windows.upload.remove(); };
        _root.windows.upload.remove(); 
		setAdvice(false,iText('advice_uploadsuccess'));
        markClean(true);
	}

	// -------------------------------------------------------------------------------------
	// individual upload tasks

	function uploadPOIs(list) {
		for (var id in list) {
			var n=getName(_root.map.pois[id].attr,nodenames);
            if (n!='') {
                _root.windows.upload.box.progress.text += iText('uploading_poi_name', id, n) + "\n";
            } else {
                _root.windows.upload.box.progress.text += iText('uploading_poi', id) + "\n";
            }
			_root.map.pois[id].upload();
		}
	}

	function uploadWays(list) {
		for (var id in list) {
			var n=getName(_root.map.ways[id].attr,waynames);
            if (n!='') {
                _root.windows.upload.box.progress.text += iText('uploading_way_name', id, n) + "\n";
            } else {
                _root.windows.upload.box.progress.text += iText('uploading_way', id) + "\n";
            }
			_root.map.ways[id].upload();
		}
	}
	
	function uploadRelations(list) {
		for (var id in list) {
			var n=getName(_root.map.relations[id].attr,[]);
            if (n!='') {
                _root.windows.upload.box.progress.text += iText('uploading_relation_name', id, n) + "\n";
            } else {
                _root.windows.upload.box.progress.text += iText('uploading_relation', id) + "\n";
            }
			_root.map.relations[id].upload();
		}
	}
	
	function uploadDeletedWays(list) {
        _root.windows.upload.box.progress.text+=iText('uploading_deleting_ways');
		for (var id in list) {
            _root.windows.upload.box.progress.text+=".";
			deleteresponder = function() { };
			deleteresponder.onResult = function(result) { deletewayRespond(result); };
			_root.writesrequested++;
			remote_write.call('deleteway',deleteresponder,_root.usertoken,_root.changeset,Number(id),waystodelete[id][0],waystodelete[id][1]);
		}
        _root.windows.upload.box.progress.text+="\n";
	}

	function uploadDeletedPOIs(list) {
        _root.windows.upload.box.progress.text+=iText('uploading_deleting_pois');
		for (var id in list) {
            _root.windows.upload.box.progress.text+=".";
			poidelresponder = function() { };
			poidelresponder.onResult = function(result) { deletepoiRespond(result); };
			_root.writesrequested++;
			remote_write.call('putpoi',poidelresponder,_root.usertoken,_root.changeset,Number(poistodelete[id][0]),Number(id),poistodelete[id][1],poistodelete[id][2],poistodelete[id][3],0);
		}
        _root.windows.upload.box.progress.text+="\n";
	}


