
	// Changeset management code
	
	// -----------------------------------------------------------------------
	// closeChangeset
	// prompts for a comment, then closes current changeset 
	// and starts a new one

	function closeChangeset() {
		if (!_root.changeset) { return; }
        if (_root.sandbox) {
			_root.panel.advanced.disableOption(5);
            pleaseWait(iText('closechangeset'));
		    startChangeset(false);
        } else {
		    changesetRequest(iText('prompt_closechangeset',_root.changeset),completeClose,'');
		}
	}

	// -----------------------------------------------------------------------
	// renewChangeset
	// renew a right changeset within us (if it's over an hour since the last)
	// returns:
	//		false - no changeset needed, continue
	//		true  - new changeset needed
	
	function renewChangeset() {
		if (!_root.changeset) { return false; }
		var t=new Date(); if (t.getTime()-_root.csopened<3400000) { return false; }
		pleaseWait(iText('openchangeset'),abortOpen);
		_root.changeset=null; startChangeset(true);
		return true;
	}
	
	function abortOpen() { _root.changeset=null; }
	function freshenChangeset() { var t=new Date(); _root.csopened=t.getTime(); }
	
	// changesetRequest(text,exit routine,comment)

	function changesetRequest(prompt,doOnClose,comment) {
		_root.panel.properties.enableTabs(false);
		_root.windows.attachMovie("modal","cs",++windowdepth);
		_root.windows.cs.init(300, 160, [iText('cancel'), iText('ok')], completeClose);

		var z = 5;
		var box = _root.windows.cs.box;
		
		box.createTextField("title",z++,7,7,280,20);
		box.title.text = prompt;
		with (box.title) { wordWrap=true; setTextFormat(boldText); selectable=false; type='dynamic'; }
		box.title.adjustTextField();

		box.createTextField("prompt",z++,7,27,280,20);
		box.prompt.text = iText('prompt_changesetcomment');
		with (box.prompt) { wordWrap=true; setTextFormat(plainSmall); selectable=false; type='dynamic'; }
		box.prompt.adjustTextField();

		box.attachMovie("checkbox","twitter",z++);
		box.twitter.init(10,110,iText('prompt_twitter',100),preferences.data.twitter,function(n) { preferences.data.twitter=n; },(preferences.data.twitterid!='' && preferences.data.twitterpwd!=''));
		
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
			_root.windows.cs.box.twitter.prompt.text=iText('prompt_twitter',100-box.cscomment.text.length);
			_root.csswallowed=true;
		};
	}
	
	function completeClose(button) {
		if (button!=iText('ok')) { return false; }
        _root.changecomment=_root.windows.cs.box.cscomment.text;
		if (_root.windows.cs.box.twitter.state) { twitterPost(); }
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
			freshenChangeset();
			if (_root.windows.pleasewait) { _root.windows.pleasewait.remove(); }
            if (_root.sandbox && !_root.uploading && _root.changeset) {
				startUpload();
			} else if (!_root.sandbox) { 
				uploadDirtyWays();
				uploadDirtyPOIs();
				uploadDirtyRelations();
			}
		};

		var cstags=new Object();														// Changeset tags
		cstags['created_by']="Potlatch "+_root.signature + ' (' + (_root.sandbox ? 'save' : 'live') + ' ' + iText('__potlatch_locale') + ')';

		remote_write.call('startchangeset',csresponder,_root.usertoken,cstags,_root.changeset,_root.changecomment,open_new);
		if (open_new) {	_root.panel.advanced.enableOption(5); }
	}

	// -----------------------------------------------------------------------
	// shortLink (from site.js)

	function shortLink(lat, lon, zoom) {
	    var char_array="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_@";
		var i;
	    var x=Math.round((lon+180.0) * ((1 << 30) /90.0));
	    var y=Math.round((lat+ 90.0) * ((1 << 30) /45.0));
 	    var c1=0, c2=0;
	    for (i=31; i>16; --i) {
			c1=(c1 << 1) | ((x >> i) & 1);
			c1=(c1 << 1) | ((y >> i) & 1);
	    }
	    for (i=16; i>1; --i) {
			c2=(c2 << 1) | ((x >> i) & 1);
			c2=(c2 << 1) | ((y >> i) & 1);
	    }
	    var str="";
	    for (i=0; i<Math.ceil((zoom+8) / 3.0) && i<5; ++i) {
			digit=(c1 >> (24 - 6 * i)) & 0x3f;
			str += char_array.charAt(digit);
	    }
	    for (i=5; i<Math.ceil((zoom+8) / 3.0); ++i) {
			digit=(c2 >> (24 - 6 * (i - 5))) & 0x3f;
			str += char_array.charAt(digit);
	    }
	    for (i=0; i<((zoom+8) % 3); ++i) {
			str += "-";
	    }
	    return str;
	}

	function twitterPost() {
		if (preferences.data.twitterid=='' || preferences.data.twitterpwd=='' || _root.changecomment=='') { return; }
		var rv=new LoadVars();
		var lv=new LoadVars();
        rv.onLoad = function(success) {
            if (rv.success == 0) {
                errorDialogue(iText("error_twitter_long", rv.errcode, rv.errmsg, rv.errerr),130);
            } else {
                setAdvice(false, iText('advice_twatted'), true);
            }
        };
		lv.twitter_id =preferences.data.twitterid;
		lv.twitter_pwd=preferences.data.twitterpwd;
		lv.clientver = _root.signature;
		lv.tweet      ="#osmedit "+_root.changecomment+" http://osm.org/go/"+shortLink(centrelat(0),centrelong(0),Math.min(_root.scale,18));
		lv.lat        =centrelat(0);
		lv.long       =centrelong(0);
		
		lv.sendAndLoad("http://richard.dev.openstreetmap.org/cgi-bin/potlatchtweet.cgi",rv,"POST");

/*
	The following code will be used when Twitter relax crossdomain restrictions on api.twitter.com (as promised).
	Until then we have to go via a proxy.
		_root.twitterresponse=new Object();
		var lv=new LoadVars();
		lv.addRequestHeader("Authorization","BASIC "+base64(preferences.data.twitterid+":"+preferences.data.twitterpwd));
		lv.addRequestHeader("X-Twitter-Client","Potlatch");
		lv.onLoad=function(success) { _root.chat.text+="onLoad "+success+"\n"; twitterUpdate(); };
		resp=lv.sendAndLoad("http://twitter.com/account/verify_credentials.json",_root.twitterresponse,"POST");

	...and then in the twitterUpdate function, the same lv stuff plus:
		lv['status']="#osmedit "+_root.changecomment+" http://osm.org/go/"+shortLink(centrelat(0),centrelong(0),Math.min(_root.scale,18));
		lv['lat']=centrelat(0);
		lv['long']=centrelong(0);
		lv.sendAndLoad("http://twitter.com/account/update.json",_root.twitterresponse,"POST");

	And for the Base64 function:

		// base64 - public domain from http://rumkin.com/tools/compression/base64.php (thanks!)
		function base64(input) {
			var char_array="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
			var output='';
			var i=0;

			while (i < input.length) {
				var chr1 = input.charCodeAt(i++);
				var chr2 = input.charCodeAt(i++);
				var chr3 = input.charCodeAt(i++);

				var enc1 = chr1 >> 2;
				var enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
				var enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
				var enc4 = chr3 & 63;

				if (isNaN(chr2)) { enc3 = enc4 = 64; }
				else if (isNaN(chr3)) { enc4 = 64; }

				output+=char_array.charAt(enc1) + char_array.charAt(enc2) + char_array.charAt(enc3) + char_array.charAt(enc4);
		   }
		   return output;
		}

*/

	}

	
