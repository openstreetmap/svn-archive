(function() {

  var mytah = window.arguments[0].mytah;

  mytah.preferences = function () {
    return new mytah.preferences();
  };

  mytah.preferences.init = function() {
    document.getElementById("mytah_usertype").selectedIndex=mytah.prefs.user_type;
    document.getElementById("mytah_user").value=mytah.prefs.user;
    document.getElementById("mytah_timeout").value = mytah.prefs.timeout;
    document.getElementById("mytah_display").selectedItem=document.getElementById("mytah_menuitem_"+mytah.prefs.display);

    document.getElementById("mytah_dialog_preferences").addEventListener("dialogaccept", mytah.preferences.doOk, false);
    document.getElementById("mytah_dialog_preferences").addEventListener("dialogcancel", mytah.preferences.doCancel, false);

  };

  mytah.preferences.doOk = function() {
    var user;
    var timeout;
    var display;
    var preferences = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);


    if (document.getElementById("mytah_username").selected) {
      preferences.setIntPref("mytah.user_type", mytah.USER_BY_NAME);
    } else if (document.getElementById("mytah_userid").selected) {
      preferences.setIntPref("mytah.user_type", mytah.USER_BY_ID);
    }

    user = document.getElementById("mytah_user").value;
    timeout = document.getElementById("mytah_timeout").value;
    display = document.getElementById("mytah_display").selectedItem.value;

    preferences.setCharPref("mytah.user", user);
    mytah.prefs.user = user;
    preferences.setIntPref("mytah.timeout", timeout);
    mytah.prefs.timeout = timeout;
    preferences.setCharPref("mytah.display", display);
    mytah.prefs.display = display;
    window.arguments[0].out={result:"ok"};

    return true;
  };

  mytah.preferences.doCancel = function() {
    return true;
  };

  mytah.preferences.shutdown = function () {
    window.removeEventListener("load", mytah.preferences.init, false);
    window.removeEventListener("unload", mytah.preferences.shutdown, false);
    document.getElementById("mytah_dialog_preferences").removeEventListener("dialogaccept", mytah.preferences.doOk, false);
    document.getElementById("mytah_dialog_preferences").removeEventListener("dialogcancel", mytah.preferences.doCancel, false);
  };

  window.addEventListener("load", mytah.preferences.init, false);
  window.addEventListener("unload", mytah.preferences.shutdown, false);

}());


