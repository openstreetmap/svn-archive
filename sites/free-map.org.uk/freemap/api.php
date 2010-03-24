<?php

require_once('inc.php');
session_start();

?>
<html>
<head>
<title>The Freemap API</title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
<body> 

<?php write_sidebar(); ?>

<div id='main' class='content'>
<h1>The Freemap API</h1>
<hr/>
<h2>Introduction</h2>
<p>Freemap aims to provide a wide range of free data for countryside users,
including path annotations (such as 
points of difficulty, blockages and interesting views).
The <em>Freemap API</em> now allows you to use this data from other websites
and applications, so if you are developing any sort of countryside website or 
standalone application, you can now use the <em>Freemap API</em> to access and 
even alter the Freemap data!</p>
<h2>Documentation</h2>
<h3>Markers</h3>
<p>The URL for markers is as follows:
<pre>http://www.free-map.org.uk/freemap/api/markers.php</pre>
The URL expects an <em>action</em> parameter which should be one of
<em>add</em>,<em>delete</em>, <em>edit</em> and <em>get</em>, e.g:
<pre>http://www.free-map.org.uk/freemap/api/markers.php?action=add</pre>
Each of the four actions expects further query string parameters, detailed
below; failure to provide all parameters will result in a 400 Bad Request.</p>
<h4>Adding a marker (action=add)</h4>
<p>The <em>add</em> action expects four further parameters:
    <ul>
        <li><strong>lat</strong> - the latitude of the marker;</li>
        <li><strong>lon</strong> - the longitude of the marker;</li>
        <li><strong>type</strong> - the type of the marker, currently
        <em>hazard</em> (e.g. a path blockage), <em>directions</em>
        (path directions, to help people work out the correct way to go)
        or <em>info</em> (an interesting place, e.g. a viewpoint or 
        historical site)</li>
        <li><strong>description</strong> - a description, e.g. a description of 
        the hazard or the path directions)</li>
    </ul>
The <em>add</em> call will return the numerical ID of the marker in the
response body, or a 400 Bad Request error code if one or more of the
required parameters were missing.  </p>
<h4>Retrieving markers (action=get)</h4>
<p>The <em>get</em> action takes one further parameter: <em>bbox</em>.
This is the bounding box of the area to retrieve markers from taking the
standard format: west,south,east,north. Markers are returned in GeoRSS
format.</p>
<h4>Editing a marker (action=edit)</h4>
<p>The <em>edit</em> action takes an <em>id</em> parameter containing the
marker ID (as returned by <em>add</em>,
above) and one or both of the <em>type</em> and
<em>description</em> parameters (see above) and changes them appropriately
for the specified marker. It will return 404 Not Found if the ID is
invalid.</p>
<h4>Deleting a marker (action=delete)</h4>
<p>The <em>delete</em> action takes an <em>id</em> parameter containing the
marker ID (as returned by <em>add</em>, above) and deletes the specified
marker. Again it returns 404 Not Found if the ID is invalid.</p>

</body>
</html>
