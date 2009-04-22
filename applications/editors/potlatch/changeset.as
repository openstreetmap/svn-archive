
	// Changeset management code
	
	// -----------------------------------------------------------------------
	// closeChangeset
	// prompts for a comment, then closes current changeset 
	// and starts a new one

	function closeChangeset() {
		_root.panel.properties.enableTabs(false);
		_root.windows.attachMovie("modal","cs",++windowdepth);
		_root.windows.cs.init(300, 140, [iText("Cancel",'cancel'), iText("Ok",'ok')], completeClose);

		var z = 5;
		var box = _root.windows.cs.box;
		
		box.createTextField("title",z++,7,7,280,20);
		box.title.text = iText("Close changeset $1",'prompt_closechangeset',_root.changeset);
		with (box.title) { wordWrap=true; setTextFormat(boldText); selectable=false; type='dynamic'; }

		box.createTextField("prompt",z++,7,27,280,20);
		box.prompt.text = iText("Enter a description of your changes:",'prompt_changesetcomment');
		with (box.prompt) { wordWrap=true; setTextFormat(plainSmall); selectable=false; type='dynamic'; }
		
		box.createTextField("search",z++,10,50,280,50);
		with (box.search) {
			setNewTextFormat(plainSmall);
			type='input';
			backgroundColor=0xDDDDDD;
			background=true;
			border=true;
			borderColor=0xFFFFFF;
			multiline=true;
			wordWrap=true;
		}
		Selection.setFocus(box.search);
		box.search.onChanged=function() {					// swallow 'C'
			if (box.search.text.toUpperCase()=='C') { box.search.text=''; }
			box.search.onChanged=null;						//  |
		};													//  |
	}
	
	function completeClose(button) {
		if (button!=iText("Ok",'ok')) { return false; }
		startChangeset(_root.windows.cs.box.search.text);
	}
	
	// -----------------------------------------------------------------------
	// startChangeset
	// Closes current changeset if it exists (with optional comment)
	// then starts a new one
	
	function startChangeset(comment) {
		csresponder=function() {};
		csresponder.onResult = function(result) {
			var code=result.shift(); if (code) { handleError(code,result); return; }
			// ** probably needs to fail really dramatically here...
			_root.changeset=result[0];
		};

		var cstags=new Object();				// Changeset tags
		cstags['created_by']=_root.signature;	//  |

		remote_write.call('startchangeset',csresponder,_root.usertoken,cstags,_root.changeset,comment);
	}
