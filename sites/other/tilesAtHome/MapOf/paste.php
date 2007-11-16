<?php

Handle($_REQUEST["paste"]);

function Handle($Permalink){
  # Look for a query string
  if(preg_match("/\?(.*)/", $Permalink, $Matches)){
    $Query = $Matches[1];
    
    # Split into arguments
    $Args = explode("&", $Query);
    
    # For each argument
    $Keys = Array();
    foreach($Args as $Arg){
      if(preg_match("/^(\w+)=(.*)$/", $Arg, $Matches)){
        # Store the name=value pair in an array
        $Keys[$Matches[1]] = $Matches[2];
      }
    }
    
    # Create a URL showing this area in the MapOf viewer
    $MapURL = sprintf(
      "http://tah.openstreetmap.org/MapOf/?lat=%f&long=%f&z=%d&w=%d&h=%d&format=jpeg",
      $Keys['lat'],
      $Keys['lon'],
      $Keys['zoom'],
      1280,
      1024);
    
    # Redirect to the map image
    header("Location: " . $MapURL);
    exit;
  }
 
}

# If no slippy-map permalink found, then display the form to let people enter one
?>
<p>Paste the URL of a permalink from informationfreeway or openstreetmap:</p>

<p><form action="paste.php" method="get">
<input type="text" name="paste" size="150">
<input type="submit" value="OK">
</form></p>

<p>...to get a downloadable map (tiles@home layer only)</p>
