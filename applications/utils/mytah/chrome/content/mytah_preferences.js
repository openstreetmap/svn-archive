const USER_BY_ID = 0;
const USER_BY_NAME = 1;

function mytah_preferences_init() {
	this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
	var user;
	var user_type;
	var timeout;
	
	if (this.prefs.getPrefType("mytah.user_type") == this.prefs.PREF_INT) {
		user_type = this.prefs.getIntPref("mytah.user_type");
	} else {
		user_type = USER_BY_NAME;
	}
	document.getElementById("mytah_usertype").selectedIndex=user_type;
	
	if (this.prefs.getPrefType("mytah.user") == this.prefs.PREF_STRING) {
		user = this.prefs.getCharPref("mytah.user");
	} else {
		user = "Merio";
	}
	document.getElementById("mytah_user").value=user;
	
	if (this.prefs.getPrefType("mytah.timeout") == this.prefs.PREF_INT) {
		timeout = this.prefs.getIntPref("mytah.timeout");
	}
	else {
		timeout = 60;
	}
	document.getElementById("mytah_timeout").value = timeout;
}


function mytah_preferences_doOK() {
	var user;
	var timeout;

	if (document.getElementById("mytah_username").selected) {
		this.prefs.setIntPref("mytah.user_type", USER_BY_NAME);
	} else if (document.getElementById("mytah_userid").selected) {
		this.prefs.setIntPref("mytah.user_type", USER_BY_ID);
	}
	
	user = document.getElementById("mytah_user").value;
	timeout = document.getElementById("mytah_timeout").value;

	this.prefs.setCharPref("mytah.user", user);
	this.prefs.setIntPref("mytah.timeout", timeout);
	window.arguments[0].out={result:"ok"};
	return true;
}

function mytah_preferences_doCancel() {
	return true;
}