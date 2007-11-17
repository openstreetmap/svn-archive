<?php
  /// \file
  /// THIS IS WORK-IN-PROGRESS BY SPAETZ TO HAVE A GENERIC
  /// FEEDBACK API FOR CLIENT TO REPORT ERRORS (OR SUCCESS) OF
  /// RENDERING BACK TO THE SERVER
  ///
  /// For a list of return codes, see documentation of FeedbackReturn();

  $X = $_POST["x"]?$_POST["x"]:$_GET["x"]; ///< x coord of feedback tile(set)
  $Y = $_POST["y"]?$_POST["y"]:$_GET["y"]; ///< y coord of feedback tile(set)
  $Z = $_POST["z"]?$_POST["z"]:$_GET["z"]; ///< z level of feedback tile(set)
  $layer = $_POST['layer']?$_POST['layer']:$_GET['layer'];  ///< layer of feedback tile(set)

  /// feedback code
  ///
  /// 200: Tileset rendered OK. resMsg contains the render time in seconds.
  $resCode = $_POST['resCode']?$_POST['resCode']:$_GET['resCode'];
  /// feedback message
  $resMsg = $_POST['resMsg']?$_POST['resMsg']:urldecode($_GET['resMsg']); 

  $client = $_POST['user']?$_POST['user']:urldecode($_GET['user']); ///< user auth name
  $passwd = $_POST['passwd']?$_POST['passwd']:urldecode($_GET['passwd']); ///< user auth password

  //sanity checks
  if($Z != 12 && $Z != 8){
     FeedbackReturn(400,'Invalid Z');
  }
  $Max = pow(2,$Z);
  if($X < 0 || $Y < 0 || $X >= $Max || $Y >= $Max){
     FeedbackReturn(400,'Invalid XY');
  }
  
  include("../lib/users.inc");
  if (($UID = checkUser($client,'letmein')) < 1) {
    FeedbackReturn(401,'Wrong username/passsword');
  }

  //include("../connect/connect.php");
  include("../lib/requests.inc");

  if (requestExists($X,$Y,$Z,NULL)){
     // no unfinished request in queue, so we and client
     // can ignore the error and get on with life.
     FeedbackReturn(205,'no such unfinished request in queue. ignore error and get next job.');
  } else {
    // The request exists and is in unfinished state. Check the error code and act accordingly
    switch($result) {
      case 200:
        // feedback: tileset rendered OK. 
        break;
      case default:
        // catch all unknown feedback codes
        FeedbackReturn(501,'feedback code not rmplemented');
    }
  }


/// Outputs a reply to client feedback back to the client
///
/// Return codes are sent as HTTP error codes
/// and as a plaintext line.
/// 200: everything is fine
/// 205: no unfinished request existed
/// 400: bad request. something in the request was invalid.
/// 501: the sent feedback code was unknown.
///
function FeedbackReturn($code=200, $reason='OK') {
  header('HTTP/1.1 '.$code.' '.$reason);
  header('Content-type: text/plain');
  printf("%d: %s\n", $code, $reason);
  exit;
}
?>
