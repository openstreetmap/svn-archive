<?php
/*
Healthwhere, a web service to find local pharmacies and hospitals
Copyright (C) 2009-2010 Russell Phillips (russ@phillipsuk.org)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

//Functions used when editing OSM data
$osm_auth = array ("httpauth"=>"$osm_user:$osm_password");
$waynode = $_GET ['waynode'];

/*
 * Create a changeset
 * Parameter: changeset comment (optional)
 * Returns changeset ID
*/
function osm_create_changeset ($comment = "") {
	global $osm_auth, $osm_api_base, $debug_log;
	//Default comment
	if ($comment == "")
		$comment = "Updates from " . SERVICE_NAME . " " . VERSION;

	$xml = "<osm>\n<changeset>\n<tag k='created_by' " .
		"v='" . SERVICE_NAME . " " . VERSION. "'/>\n" .
		"<tag k='comment' v='$comment'/>\n</changeset>\n</osm>";

	$url = $osm_api_base . "/changeset/create";
	$info = "";

	$cs = http_put_data ($url, $xml, $osm_auth, $info);
	$out = print_r ($info, True);
	file_put_contents ($debug_log, "\n------------------\n" .
		date ("Y-m-d H:i:s") . "\tCreate changeset URL: $url\n", FILE_APPEND);

	//Get final line as an integer
	$aLines = explode ("\n", $cs);
	$iLine = count ($aLines) -1;
	file_put_contents ($debug_log, "Changeset ID: {$aLines [$iLine]}\n", FILE_APPEND);
	//Save changeset ID to a cookie and return it
	if (setcookie ('csID', $aLines [$iLine]) === False)
		file_put_contents ($debug_log, "Could not set cookie csID. Value = {$aLines [$iLine]}\n", FILE_APPEND);
	return (int) $aLines [$iLine];
}

/*
 * Update a node
 * Parameters:
 * $id: ID of node to be updated
 * $xml: XML string
 * $csID: changeset ID
 * Returns True if successful, False if not
*/
function osm_update_node ($id, $xml, $csID) {
	global $osm_auth, $osm_api_base, $debug_log, $db_file;

	file_put_contents ($debug_log, "\n------------------\n", FILE_APPEND);
	file_put_contents ($debug_log, "Updating node $id\n", FILE_APPEND);

	$cs = "changeset=\"$csID\"";
	//Set changeset ID in XML string
	$xml = ereg_replace ('changeset="[0-9]*"', $cs, $xml);
	$url = "$osm_api_base/node/$id";
	http_put_data ($url, $xml, $osm_auth, $info);
	//Check for 409 error
	if ($info ['response_code'] == 409) {
		file_put_contents ($debug_log, "409 Error. Creating new changeset\n", FILE_APPEND);
		//409 response code means changeset is closed. Create a new one and use that
		$csid = osm_create_changeset ();
		$_COOKIE ['csID'] = $csid;
		$cs = "changeset=\"$csid\"";
		//Set changeset ID in XML string
		$xml = ereg_replace ('changeset="[0-9]*"', $cs, $xml);
		http_put_data ($url, $xml, $osm_auth, $info);
	}
	//Log debugging info
	$out = print_r ($info, True);
	file_put_contents ($debug_log, "Update node URL: $url\n", FILE_APPEND);
	file_put_contents ($debug_log, "XML:\n$xml\n", FILE_APPEND);
	file_put_contents ($debug_log, "Result:\n$out\n", FILE_APPEND);
	if ($info ['response_code'] >= 400 && $info ['response_code'] <= 499)
		return False;
	else
		return True;
}

/*
 * Close a changeset
 * Parameter: Changeset ID
 * Returns nothing
*/
function osm_close_changeset ($id) {
	global $osm_auth, $osm_api_base, $debug_log;

	$url = "$osm_api_base/changeset/$id/close";
	http_put_data ($url, "", $osm_auth, $info);
	$out = print_r ($info, True);
	file_put_contents ($debug_log, "Close changeset URL: $url\n", FILE_APPEND);
	file_put_contents ($debug_log, "Result:\n$out\n", FILE_APPEND);
}
?>
