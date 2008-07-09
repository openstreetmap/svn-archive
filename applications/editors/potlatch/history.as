
	// =====================================================================================
	// History functions
	// wayHistory - show dialogue for previous versions of the way
	//				(calling handleRevert to actually initiate the revert)
	// getDeleted - load all deleted ways (like whichways), but locked
	
	function wayHistory() {
		historyresponder = function() { };
		historyresponder.onResult = function(result) {
			_root.windows.attachMovie("modal","history",++windowdepth);
			_root.windows.history.init(275,90,new Array(iText('Revert','revert'),iText('Cancel','cancel')),handleRevert);
			_root.windows.history.box.createTextField("prompt",2,7,9,250,100);
			writeText(_root.windows.history.box.prompt,iText("Revert to an earlier saved version:",'prompt_revertversion'));

			var versionlist=new Array();
			_root.versionnums=new Array();
			for (i=0; i<result[0].length; i+=1) {
				versionlist.push(result[0][i][1]+' ('+result[0][i][3]+')');
				versionnums[i]=result[0][i][0];
			}
			_root.windows.history.box.attachMovie("menu","version",6);
			_root.windows.history.box.version.init(9,32,0,versionlist,
				iText('Choose the version to revert to','tip_revertversion'),
				function(n) { _root.revertversion=versionnums[n]; },0);
			_root.revertversion=versionnums[0];
		};
		remote.call('getway_history',historyresponder,Math.floor(_root.wayselected));
	};
	function handleRevert(choice) {
		if (choice==iText('Cancel','cancel')) { return; }
		_root.ws.loadFromDeleted(_root.revertversion);
	};
	function getDeleted() {
		whichdelresponder=function() {};
		whichdelresponder.onResult=function(result) {
			waylist=result[0];
			for (i in waylist) {										// ways
				way=waylist[i];											//  |
				if (!_root.map.ways[way]) {								//  |
					_root.map.ways.attachMovie("way",way,++waydepth);	//  |
					_root.map.ways[way].loadFromDeleted(-1);			//  |
					_root.waycount+=1;									//  |
				}
			}
		};
		remote.call('whichways_deleted',whichdelresponder,_root.edge_l,_root.edge_b,_root.edge_r,_root.edge_t);
	};
