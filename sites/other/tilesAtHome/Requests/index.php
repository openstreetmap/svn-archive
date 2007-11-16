<?php
  // This method of requesting jobs was using GET and has been deprecated.
  // All Requests should be done via the Request2.php now

  header("Content-type:text/plain");
  $APIVersion=99; #make clients using GET stop and not re-request every 60 seconds by setting arbitrarily high API version number

  printf("OK|%d||||index.php is deprecated. Update client to later version.\n\nNew URL is Requests/Request2.php",
      $APIVersion); #OK as a workaround for *really* old clients which would only look at api version if the request was starting "OK"
?>
