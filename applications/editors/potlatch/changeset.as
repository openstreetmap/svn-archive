
	// Changeset management code
	
	// -----------------------------------------------------------------------
	// closeChangeset
	// prompts for a comment, then closes current changeset 
	// and starts a new one

	function closeChangeset() {
        if (_root.sandbox) {
            if (!_root.changeset) { return; }
            pleaseWait(iText("Closing changeset",'closechangeset'));
		    startChangeset(false);
        } else {
		    changesetRequest(iText("Close changeset $1",'prompt_closechangeset',_root.changeset),completeClose,'');
		}
	}

	// -----------------------------------------------------------------------
	// renewChangeset
	// renew a right changeset within us (if it's over an hour since the last)
	// returns false if it hasn't requested a new one, true if it has
	
	function renewChangeset() {
		if (!_root.changeset) { return false; }
		var t=new Date(); if (t.getTime()-_root.csopened<3400000) { return false; }
		pleaseWait(iText("Opening changeset",'openchangeset'));
		_root.changeset=null; startChangeset(true);
		return true;
	}

	// changesetRequest(text,exit routine,comment)

	function changesetRequest(prompt,doOnClose,comment) {
		_root.panel.properties.enableTabs(false);
		_root.windows.attachMovie("modal","cs",++windowdepth);
		_root.windows.cs.init(300, 140, [iText("Cancel",'cancel'), iText("Ok",'ok')], completeClose);

		var z = 5;
		var box = _root.windows.cs.box;
		
		box.createTextField("title",z++,7,7,280,20);
		box.title.text = prompt;
		with (box.title) { wordWrap=true; setTextFormat(boldText); selectable=false; type='dynamic'; }

		box.createTextField("prompt",z++,7,27,280,20);
		box.prompt.text = iText("Enter a description of your changes:",'prompt_changesetcomment');
		with (box.prompt) { wordWrap=true; setTextFormat(plainSmall); selectable=false; type='dynamic'; }
		
		box.createTextField("cscomment",z++,10,50,280,50);
		with (box.cscomment) {
			setNewTextFormat(plainSmall);
			type='input';
			backgroundColor=0xDDDDDD;
			background=true;
			border=true;
			borderColor=0xFFFFFF;
			wordWrap=true;
			text=comment;
		}
		_root.csswallowed=false;
		Selection.setFocus(box.cscomment);
		box.cscomment.onChanged=function() {				// swallow 'C'
			fixUTF8();
			if ((box.cscomment.text.toUpperCase()=='C' || box.cscomment.text.toUpperCase()=='S') && !_root.csswallowed ) {
				box.cscomment.text='';
			}
			box.csswallowed=true;
		};
	}
	
	function completeClose(button) {
		if (button!=iText("Ok",'ok')) { return false; }
        _root.changecomment=_root.windows.cs.box.cscomment.text;
		startChangeset(true);
	}
	

	// -----------------------------------------------------------------------
	// startChangeset
	// Closes current changeset if it exists (with optional comment)
	// then starts a new one
	
	function startChangeset(open_new) {
		csresponder=function() {};
		csresponder.onResult = function(result) {
			var code=result.shift(); var msg=result.shift(); if (code) { handleError(code,msg,result); return; }
			// ** probably needs to fail really catastrophically here...
			_root.changeset=result[0];
			var t=new Date(); _root.csopened=t.getTime();
			if (_root.windows.pleasewait) { _root.windows.pleasewait.remove(); }
            if (_root.sandbox && !_root.uploading && _root.changeset) { startUpload(); }
		};

		var cstags=new Object();				// Changeset tags
		cstags['created_by']=_root.signature;	//  |

		remote_write.call('startchangeset',csresponder,_root.usertoken,cstags,_root.changeset,_root.changecomment,open_new);
	}
