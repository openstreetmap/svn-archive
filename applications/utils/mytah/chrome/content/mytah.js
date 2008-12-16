//TODO: Icon
//TODO: Additional info to retrieve number of tiles (tooltip?), and different orders, details in own homepage


const USER_BY_ID = 0;
const USER_BY_NAME = 1;

var user;
var user_type;
var timeout=60;
var current_text;
var current_rank="updating...";
var interval_id=null;

function mytah_init() {
	var current_rank="updating...";
	document.getElementById("mytah_sbmi").label="My Tah: "+current_rank;

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
		user = "Merio";
		this.prefs.setCharPref("mytah.user", user);
	this.prefs.setIntPref("mytah.timeout", timeout);
	}
	if (this.prefs.getPrefType("mytah.timeout") == this.prefs.PREF_INT) {
		timeout = this.prefs.getIntPref("mytah.timeout");
	}
	else {
		timeout = 60;
		this.prefs.setIntPref("mytah.timeout", timeout);
	}
	if (interval_id!=null) {
		clearInterval(interval_id);
	}
	interval_id=setInterval("mytah_init()",(timeout*60*1000));
	mytah_update_text();
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
	var myRegExp = "";
	
	if (user_type==USER_BY_ID) {
		myRegExp = '(?:(<tr><td>))(.*?)(?=(<\/td><td><a href="\/User\/show\/byid\/'+user+'\/">))';
	} else if (user_type==USER_BY_NAME) {
		myRegExp = '(?:(<tr><td>))(.*?)(?=(<\/td><td><a href="\/User\/show\/byid\/\\d*\/">'+user+'))';
	}
	var re = new RegExp(myRegExp);
	var m = re.exec(current_text);
	current_rank = m[2];
	document.getElementById("mytah_sbmi").label="MyTaH: "+current_rank;
}

function mytah_showPreferences() {
	var params={out:null};
	window.openDialog("chrome://MyTaH/content/preferences.xul","","chrome,dialog,modal",params).focus();

	  if (params.out) {
    current_rank="updating...";
    mytah_init();
  }
  else {
  }
}

function mytah_showAbout() {
	window.openDialog("chrome://MyTaH/content/about.xul","","chrome,dialog").focus();
}