
	// Changeset management code
	
	// -----------------------------------------------------------------------
	// closeChangeset
	// prompts for a comment, then closes current changeset 
	// and starts a new one
	
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
