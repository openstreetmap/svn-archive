<?php
  // This method of requesting jobs was using GET and has been deprecated.
  // All Requests should be done via the Request2.php now

  header("Content-type:text/plain");
  $APIVersion=3;

  printf("XX|%d||||index.php is deprecated. Update client to later version.\n\nNew URL is Requests/Request2.php",
      $APIVersion);
?>
