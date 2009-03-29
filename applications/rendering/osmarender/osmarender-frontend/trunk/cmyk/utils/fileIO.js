dojo.provide("cmyk.utils.fileIO");

/**
	@lends cmyk.utils.fileIO
*/

/*
http://straxus.javadevelopersjournal.com/creating_a_mozillafirefox_drag_and_drop_file_upload_script_p.htm
http://www.captain.at/programming/xul/
https://developer.mozilla.org/En/XUL_Tutorial/Open_and_Save_Dialogs
https://developer.mozilla.org/Special:Search?search=save+file&type=fulltext&go=Search
https://developer.mozilla.org/index.php?title=En/Code_snippets/File_I%2F%2FO
https://developer.mozilla.org/en/NsIFile/create	
https://developer.mozilla.org/en/NsIFile
http://jslib.mozdev.org/
http://kb.mozillazine.org/Io.js
*/

dojo.declare("cmyk.utils.fileIO",null,{
	/** 
	      @class A class that loads and saves a file from Hard Disk and returns the path (if loading)
	      @memberOf cmyk.utils
	*/

	constructor: function() {
	},
	loadFile: function() {
		netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
		var nsIFilePicker = Components.interfaces.nsIFilePicker;
		var fp = Components.classes["@mozilla.org/filepicker;1"].createInstance(nsIFilePicker);
		fp.init(window, "Select a File", nsIFilePicker.modeOpen);
		var res = fp.show();
		if (res == nsIFilePicker.returnOK)
			return fp.file.path;
		else
			return false;
	},
	saveFile: function(string) {
		netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
		var nsIFilePicker = Components.interfaces.nsIFilePicker;
		var fp = Components.classes["@mozilla.org/filepicker;1"].createInstance(nsIFilePicker);
		fp.init(window, "Select a File", nsIFilePicker.modeSave);
		var res = fp.show();
		if (res == nsIFilePicker.returnOK){
			var file = fp.file;
			file.create( Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 420 );
			var outputStream = Components.classes["@mozilla.org/network/file-output-stream;1"].createInstance( Components.interfaces.nsIFileOutputStream );
			outputStream.init( file, 0x04 | 0x08 | 0x20, 420, 0 );
			var output = string;
			var result = outputStream.write( output, output.length );
			outputStream.close();
			return true;
		}
		else {
			return false;
		}
	}
});


 
