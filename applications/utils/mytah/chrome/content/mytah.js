//TODO: Icon
//TODO: Additional info to retrieve number of tiles (tooltip?), and different orders, details in own homepage

const mytah_version="0.1";
const mytah_build="20081217";

const USER_BY_ID = 0;
const USER_BY_NAME = 1;

var user;
var user_type;
var display="current_rank";
var timeout=60;
var current_text;
var current_rank="updating...";
var number_of_tiles="updating...";
var number_of_kb="updating...";
var idle_time="updating...";
var tiles_to_better_rank="updating...";

var interval_id=null;

function mytah_init() {
	current_rank="updating...";
	number_of_tiles="updating...";
	number_of_kb="updating...";
	idle_time="updating...";
	tiles_to_better_rank="updating...";
	document.getElementById("mytah_sbmi").label="MyTah: "+eval(display);
	document.getElementById("mytah_currentrank").value=current_rank;
	document.getElementById("mytah_numberoftiles").value=number_of_tiles;
	document.getElementById("mytah_numberofkb").value=number_of_kb;
	document.getElementById("mytah_idletime").value=idle_time;
	document.getElementById("mytah_tilestobetterrank").value=tiles_to_better_rank;

	this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
	if (this.prefs.getPrefType("mytah.user_type") == this.prefs.PREF_INT) {
		user_type = this.prefs.getIntPref("mytah.user_type");
	} else {
		this.prefs.setIntPref("mytah.user_type", USER_BY_NAME);
		user_type = USER_BY_NAME;
	}
	if (this.prefs.getPrefType("mytah.user") == this.prefs.PREF_STRING) {
		user = this.prefs.getCharPref("mytah.user");
	} else {
		user = "";
		this.prefs.setCharPref("mytah.user", user);
	}
	if (this.prefs.getPrefType("mytah.timeout") == this.prefs.PREF_INT) {
		timeout = this.prefs.getIntPref("mytah.timeout");
	}
	else {
		timeout = 60;
		this.prefs.setIntPref("mytah.timeout", timeout);
	}
	if (this.prefs.getPrefType("mytah.display") == this.prefs.PREF_STRING) {
		display = this.prefs.getCharPref("mytah.display");
	} else {
		display = "current_rank";
		this.prefs.setCharPref("mytah.display", display);
	}
	if (interval_id!=null) {
		clearInterval(interval_id);
	}
	interval_id=setInterval("mytah_init()",(timeout*60*1000));
	if (user=="") {
		document.getElementById("mytah_sbmi").label="MyTah: No name";
	}
	else {
		mytah_update_text();
	}
}

function mytah_update_text() {
	var req = new XMLHttpRequest();
		req.open('GET', 'http://server.tah.openstreetmap.org/User/show/?order=tiles', true);
		req.onreadystatechange = function (aEvt) {
		  if (req.readyState == 4) {
		     if(req.status == 200) {
		     	  current_text=req.responseText;
			      mytah_getRanking();
			}
		     else {
		      dump("Error loading page\n");
		      }
		  }
		};
	req.send(null); 
}

function mytah_getRanking() {
	var regexs = {
		by_id: {
			current_rank: '(?:(<tr><td>))(.*?)(?=(<\/td><td><a href="\/User\/show\/byid\/'+user+'\/">))',
			number_of_tiles: '(?:(\/byid\/'+user+'\/">.*?<\/a><\/td><td>))(.*)(?=(<\/td><td>.*?<\/td><td>.*?<\/td><\/tr>))',
			number_of_kb: '(?:(\/byid\/'+user+'\/">.*?<\/a><\/td><td>.*?<\/td><td>))(.*)(?=(<\/td><td>))',
			idle_time: '(?:(\/byid\/'+user+'\/">.*?<\/a><\/td><td>.*?<\/td><td>.*?<\/td><td>))(.*)(?=(<\/td><\/tr>))',
			tiles_to_better_rank: '(?:(<\/a><\/td><td>))(.*?)(?=(<\/td><td>.*?<\/td><td>.*?<\/td><\/tr>\\s*<tr><td>.*?<\/td><td><a href="\/User\/show\/byid\/'+user+'\/">))'
		},
		by_name: {
			current_rank: '(?:(<tr><td>))(.*?)(?=(<\/td><td><a href="\/User\/show\/byid\/\\d*\/">'+user+'))',
			number_of_tiles: '(?:('+user+'<\/a><\/td><td>))(.*?)(?=(<\/td><td>))',
			number_of_kb: '(?:('+user+'<\/a><\/td><td>.*?<\/td><td>)(.*?)(?=(<\/td><td>)))',
			idle_time: '(?:('+user+'<\/a><\/td><td>.*?<\/td><td>.*?<\/td><td>)(.*?)(?=(<\/td><\/tr>)))',
			tiles_to_better_rank: '(?:(<\/a><\/td><td>))(.*?)(?=(<\/td><td>.*?<\/td><td>.*?<\/td><\/tr>\\s*<tr><td>.*?<\/td><td><a href="\/User\/show\/byid\/\\d*\/">'+user+'<\/a>))'
		}
	};
	var looper;
	if (user_type==USER_BY_ID) looper=regexs.by_id; else looper=regexs.by_name;
	for (var a in looper) {
		var m = new RegExp(looper[a]).exec(current_text);
		eval(a+"='"+m[2]+"'");
	}
	tiles_to_better_rank="-"+(tiles_to_better_rank-number_of_tiles);
	document.getElementById("mytah_sbmi").label="MyTaH: "+eval(display);
	document.getElementById("mytah_currentrank").value=current_rank;
	document.getElementById("mytah_numberoftiles").value=number_of_tiles;
	document.getElementById("mytah_numberofkb").value=number_of_kb;
	document.getElementById("mytah_idletime").value=idle_time;
	document.getElementById("mytah_tilestobetterrank").value=tiles_to_better_rank;
}

function mytah_showPreferences() {
	var params={out:null};
	window.openDialog("chrome://MyTaH/content/preferences.xul","","chrome,dialog,modal",params).focus();
	if (params.out) {
		mytah_updateNow();
	}
	else {
	}
}

function mytah_updateNow() {
		mytah_init();
}

function mytah_showAbout() {
	window.openDialog("chrome://MyTaH/content/about.xul","","chrome,dialog").focus();
}