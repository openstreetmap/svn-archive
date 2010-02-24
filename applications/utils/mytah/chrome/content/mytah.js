//TODO: Icon
//TODO: Additional info to retrieve number of tiles (tooltip?), and different orders, details in own homepage

(function() {

  var mytah = window.mytah = function() {
    return new mytah();
  };

  mytah.version = "0.3";
  mytah.build = "20100224";

  mytah.USER_BY_ID = 0;
  mytah.USER_BY_NAME = 1;

  mytah.prefs = {
    user: "",
    user_type: 0,
    user_id: "",
    timeout: 60,
    display: "current_rank"
  };

  var to_display = {
    current_text: "",
    current_rank: "updating...",
    number_of_tiles: "updating...",
    number_of_kb: "updating...",
    idle_time: "updating...",
    tiles_to_better_rank: "updating..."
  }

  var interval_id=null;


  mytah.init = function() {
    to_display.current_rank="updating...";
    to_display.number_of_tiles="updating...";
    to_display.number_of_kb="updating...";
    to_display.idle_time="updating...";
    to_display.tiles_to_better_rank="updating...";
    document.getElementById("mytah_sbmi").label="MyTah: "+to_display[mytah.prefs.display];
    document.getElementById("mytah_currentrank").value=to_display.current_rank;
    document.getElementById("mytah_numberoftiles").value=to_display.number_of_tiles;
    document.getElementById("mytah_numberofkb").value=to_display.number_of_kb;
    document.getElementById("mytah_idletime").value=to_display.idle_time;
    document.getElementById("mytah_tilestobetterrank").value=to_display.tiles_to_better_rank;

    var preferences = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);

    if (preferences.getPrefType("mytah.user_type") == preferences.PREF_INT) {
      mytah.prefs.user_type = preferences.getIntPref("mytah.user_type");
    } else {
      preferences.setIntPref("mytah.user_type", mytah.USER_BY_NAME);
      mytah.prefs.user_type = mytah.USER_BY_NAME;
    }
    if (preferences.getPrefType("mytah.user") == preferences.PREF_STRING) {
      mytah.prefs.user = preferences.getCharPref("mytah.user");
    } else {
      mytah.prefs.user = "";
      preferences.setCharPref("mytah.user", mytah.prefs.user);
    }
    if (preferences.getPrefType("mytah.timeout") == preferences.PREF_INT) {
      mytah.prefs.timeout = preferences.getIntPref("mytah.timeout");
    }
    else {
      mytah.prefs.timeout = 60;
      preferences.setIntPref("mytah.timeout", mytah.prefs.timeout);
    }
    if (preferences.getPrefType("mytah.display") == preferences.PREF_STRING) {
      mytah.prefs.display = preferences.getCharPref("mytah.display");
    } else {
      mytah.prefs.display = "current_rank";
      preferences.setCharPref("mytah.display", mytah.prefs.display);
    }
    if (interval_id!=null) {
      clearInterval(interval_id);
    }
    interval_id=setInterval(mytah.init,(mytah.prefs.timeout*60*1000));
    if (mytah.prefs.user=="") {
      document.getElementById("mytah_sbmi").label="MyTah: No name";
    }
    else {
      mytah.update_text();
    }
  };

  mytah.update_text = function() {
    var req = new XMLHttpRequest();
    req.open('GET', 'http://server.tah.openstreetmap.org/User/show/?order=tiles', true);
    req.onreadystatechange = function (aEvt) {
      if (req.readyState == 4) {
        if(req.status == 200) {
          to_display.current_text=req.responseText;
          mytah.getRanking();
        }
        else {
          dump("Error loading page\n");
        }
      }
    };
    req.send(null); 
  }

  mytah.getRanking = function() {
    var regexs = {
      by_id: {
        user_id: 'variable:user',
        current_rank: '(?:(<tr><td>))(.*?)(?=(<\/td><td><a href="\/User\/show\/byid\/'+mytah.prefs.user+'\/">))',
        number_of_tiles: '(?:(\/byid\/'+mytah.prefs.user+'\/">.*?<\/a><\/td><td>))(.*)(?=(<\/td><td>.*?<\/td><td>.*?<\/td><\/tr>))',
        number_of_kb: '(?:(\/byid\/'+mytah.prefs.user+'\/">.*?<\/a><\/td><td>.*?<\/td><td>))(.*)(?=(<\/td><td>))',
        idle_time: '(?:(\/byid\/'+mytah.prefs.user+'\/">.*?<\/a><\/td><td>.*?<\/td><td>.*?<\/td><td>))(.*)(?=(<\/td><\/tr>))',
        tiles_to_better_rank: '(?:(<\/a><\/td><td>))(.*?)(?=(<\/td><td>.*?<\/td><td>.*?<\/td><\/tr>\\s*<tr><td>.*?<\/td><td><a href="\/User\/show\/byid\/'+mytah.prefs.user+'\/">))'
      },
      by_name: {
        user_id: '(?:(<tr><td>.*?<\/td><td><a href="\/User\/show\/byid\/))(.*?)(?=(/">'+mytah.prefs.user+'))',
        current_rank: '(?:(<tr><td>))(.*?)(?=(<\/td><td><a href="\/User\/show\/byid\/\\d*\/">'+mytah.prefs.user+'))',
        number_of_tiles: '(?:('+mytah.prefs.user+'<\/a><\/td><td>))(.*?)(?=(<\/td><td>))',
        number_of_kb: '(?:('+mytah.prefs.user+'<\/a><\/td><td>.*?<\/td><td>)(.*?)(?=(<\/td><td>)))',
        idle_time: '(?:('+mytah.prefs.user+'<\/a><\/td><td>.*?<\/td><td>.*?<\/td><td>)(.*?)(?=(<\/td><\/tr>)))',
        tiles_to_better_rank: '(?:(<\/a><\/td><td>))(.*?)(?=(<\/td><td>.*?<\/td><td>.*?<\/td><\/tr>\\s*<tr><td>.*?<\/td><td><a href="\/User\/show\/byid\/\\d*\/">'+mytah.prefs.user+'<\/a>))'
      }
    };
    var looper;
    if (mytah.prefs.user_type==mytah.USER_BY_ID) looper=regexs.by_id; else looper=regexs.by_name;
    for (var a in looper) {
      if (looper[a].indexOf("variable:")==-1) {
        var m = new RegExp(looper[a]).exec(to_display.current_text);
        to_display[a] = m[2];
      }
      else {
        m = looper[a].substring((looper[a].indexOf(":")+1),looper[a].length);
        to_display[a] = m;
      }
    }
    to_display.tiles_to_better_rank="-"+(to_display.tiles_to_better_rank-to_display.number_of_tiles);
    document.getElementById("mytah_sbmi").label="MyTaH: "+to_display[mytah.prefs.display];
    document.getElementById("mytah_currentrank").value=to_display.current_rank;
    document.getElementById("mytah_numberoftiles").value=to_display.number_of_tiles;
    document.getElementById("mytah_numberofkb").value=to_display.number_of_kb;
    document.getElementById("mytah_idletime").value=to_display.idle_time;
    document.getElementById("mytah_tilestobetterrank").value=to_display.tiles_to_better_rank;
  }

  mytah.showPreferences = function() {
    var params={mytah:mytah,out:null};
    window.openDialog("chrome://MyTaH/content/preferences.xul","","chrome,dialog,modal",params).focus();
    if (params.out) {
      mytah.updateNow();
    }
    else {
    }
  }

  mytah.updateNow = function() {
    mytah.init();
  }

  mytah.showAbout = function() {
    window.openDialog("chrome://MyTaH/content/about.xul","","chrome,dialog",{mytah:mytah}).focus();
  }

  mytah.openPage = function(whichpage) {
    switch (whichpage) {
      case 'order_tiles' : {
        gBrowser.selectedTab = gBrowser.addTab("http://server.tah.openstreetmap.org/User/show/?order=tiles");
        break;
      }
      case 'order_upload': {
        gBrowser.selectedTab = gBrowser.addTab("http://server.tah.openstreetmap.org/User/show/?order=upload");
        break;
      }
      case 'order_lastactivity': {
        gBrowser.selectedTab = gBrowser.addTab("http://server.tah.openstreetmap.org/User/show/?order=last");
        break;
      }
      case 'personal' : {
        if (user!="" && user_id!=null) {
          gBrowser.selectedTab = gBrowser.addTab("http://tah.openstreetmap.org/User/show/byid/"+user_id);
        }
        break;
      }
    }
  }

  mytah.shutdown = function () {
    window.removeEventListener("load", mytah.init, false);
    window.removeEventListener("unload", mytah.shutdown, false);
  }

  window.addEventListener("load", mytah.init, false);
  window.addEventListener("unload", mytah.shutdown, false);
}());

