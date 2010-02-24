(function() {

  var mytah = window.arguments[0].mytah;

  mytah.about = function () {
    return new mytah.about();
  };

  mytah.about.init = function() {
    document.getElementById("about_version").innerHTML += mytah.version;
    document.getElementById("about_build_date").innerHTML += mytah.build;
  }

  mytah.about.shutdown = function () {
    window.removeEventListener("load", mytah.about.init, false);
    window.removeEventListener("unload", mytah.about.shutdown, false);
  };

  window.addEventListener("load", mytah.about.init, false);
  window.addEventListener("unload", mytah.about.shutdown, false);

}());


