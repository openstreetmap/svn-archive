<?php
  /// THIS IS WORK-IN-PROGRESS BY SPAETZ TO HAVE A GENERIC
  /// FEEDBACK API FOR CLIENT TO REPORT ERRORS (OR SUCCESS) OF
  /// RENDERING BACK TO THE SERVER
  ///
  /// For a list of return codes, see documentation of FeedbacReturn();

  $X = $_GET["x"];
  $Y = $_GET["y"];
  $Z = $_GET["z"];
  $Src = $_GET["src"];
  $layer = $_GET["layer"];
  $result = $_GET["result"];
  $reason = $_GET["reason"];
  
  if($Z != 12 && $Z != 8){
     FeedbackReturn(400,'Invalid Z');
     exit;
  }
  $Max = pow(2,$Z);
  if($X < 0 || $Y < 0 || $X >= $Max || $Y >= $Max){
     FeedbackReturn(400,'Invalid XY');
     exit;
  }
  
  include("../connect/connect.php");
  include("../lib/requests.inc");

  if (requestExists($X,$Y,$Z,NULL)){
     //no unfinished request in queue, so we and client
     //can ignore the error and get on with life.
     FeedbackReturn(205,'no such unfinished request in queue. ignore error and get next job.');
     exit;
  } else {
    $SQL = sprintf(
       "INSERT into tiles_queue (`x`,`y`,`z`,`status`,`src`,`date`,`priority`,`ip`, `request_date`) values (%d,%d,%d,%d,'%s',now(),%s,'%s',now());", 
       $X, $Y, $Z, REQUEST_PENDING, mysql_escape_string($Src), $P, $_SERVER['REMOTE_ADDR']);

     mysql_query($SQL);
     if(mysql_error()){
       printf("Database error %s\n", mysql_error());
       exit;
     }
  FeedbackReturn();
  }


/** Outputs a reply to client feedback back to the client
 *
 * Return codes are sent as HTTP error codes
 * and as a plaintext line.
 * 200: everything is fine
 * 205: no unfinished request existed
 * 400: bad request. something was invalid.
 */
function FeedbackReturn($code=200, $reason='OK') {
  header('HTTP/1.1 '.$code.' '.$reason);
  header('Content-type: text/plain');
  printf("%d: %s\n", $code, $reason);
}
?>
