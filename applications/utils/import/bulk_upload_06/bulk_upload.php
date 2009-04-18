<?php

/**
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE":
 * <ivan@sanchezortega.es> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */

/// NOTE: this script is NOT guaranteed to work right. It is unknown whether there are any bugs on the 0.6 API at this time. The script has NOT gone under strict review nor tests. Use at your own risk!

/// TODO: add support for command-line options: username, passwd, server URL, file, batch sizes
/// TODO: add support for uploading an entire directory instead of a single .osm file
/// TODO: add support for parsing and uploading relations - right now relations are NOT handled AT ALL


$username = 'ivan@sanchezortega.es';
$passwd   = '12345678';
$server_base_url = 'localhost:3000/api';
// $server_base_url = 'api06.dev.openstreetmap.org/api';
// $server_base_url = 'osmapi06.shaunmcdonald.me.uk/api';

/// TODO: scan for files in the current dir or allow for multiple files to be passed as parameters
// $file = 'osm/b0001c2.osm';
$file = $argv[1];

/// TODO: fix this, use mktempfile() or something
$tmpfilename = '/tmp/bulk_upload_temp_' . posix_getpid() ;

$node_batch_size = 25000;
$way_batch_size  = 1000;



$xml = simplexml_load_file($file);

$batch = 0;

$generator = $xml['generator'];

$changeset = NULL;

$updated_node_ids = array();

$username = urlencode($username);
$passwd   = urlencode($passwd);



function init_payload(&$payload)
{
// 	$payload = "<osmChange>";
	$payload = "<osmChange version='0.6' generator='php_bulk_uploader'><create version='0.6' generator='php_bulk_uploader'>\n";
// 	$payload = "<osmChange version='0.6' generator='php_bulk_uploader'><create version='0.6' generator='php_bulk_uploader'>";
}

function close_payload(&$payload)
{
// 	$payload .= "</modify></osmChange>";
	$payload .= "</create></osmChange>";
}

function open_changeset($object_type)
{
	global $username, $passwd, $server_base_url, $file, $tmpfilename, $batch, $generator, $changeset;

	$batch++;

	/// FIXME: escape XML by using xmlwriter.
	
	file_put_contents($tmpfilename,$request = "<osm><changeset><tag k='created_by' v='$generator'/><tag k='uploaded_by' v='php_bulk_upload'/><tag k='comment' v='Upload of file $file, {$object_type}s, batch $batch'/></changeset></osm>");
	
	echo "PUT http://$username:$passwd@$server_base_url/0.6/changeset/create\n";
	
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL,"http://$username:$passwd@$server_base_url/0.6/changeset/create");  
	curl_setopt($ch, CURLOPT_PUT, 1); 
	curl_setopt($ch, CURLOPT_INFILE, $fp = fopen($tmpfilename,'r')); 
	curl_setopt($ch, CURLOPT_INFILESIZE, strlen($request)); 
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); 
	$changeset = curl_exec($ch);
	fclose($fp);
	unlink ($tmpfilename);
	
	$info = curl_getinfo($ch);
	echo "API returned HTTP code {$info['http_code']}\n";
	
	if (!is_numeric($changeset))
	{
		trigger_error("API didn't provide a new changeset ID, aborting. We sent \n" . file_get_contents($tmpfilename) ."\n",E_USER_ERROR);
		die();
	}
	
	echo "File \"$file\" batch \"$batch\" is being uploaded to changeset \"$changeset\" \n";
	sleep(1);
}	
	
	
function send_payload(&$payload)
{
	global $username, $passwd, $server_base_url, $file, $tmpfilename, $batch, $generator, $changeset;
	
	file_put_contents($tmpfilename,$payload);

// 	echo "\nPOST http://$username:$passwd@$server_base_url/0.6/changeset/$changeset/upload\n$payload\n\n";
	echo "POST http://$username:$passwd@$server_base_url/0.6/changeset/$changeset/upload\n";

	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL,"http://$username:$passwd@$server_base_url/0.6/changeset/$changeset/upload");  
// 	curl_setopt($ch, CURLOPT_VERBOSE, 1); 
	curl_setopt($ch, CURLOPT_POST, 1); 
	curl_setopt($ch, CURLOPT_POSTFIELDS,$payload); 
// 	curl_setopt($ch, CURLOPT_PUT, 1); 
// 	curl_setopt($ch, CURLOPT_INFILE, $fp = fopen($tmpfilename,'r')); 
// 	curl_setopt($ch, CURLOPT_INFILESIZE, filesize($tmpfilename)); 
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); 
	curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1); 
	$payload_updates = curl_exec($ch);
	unlink ($tmpfilename);
	
	$info = curl_getinfo($ch);
	echo "API returned HTTP code {$info['http_code']}\n";
	
	if ($info['http_code'] != 200)
	{
		trigger_error("The API returned: \n$payload_updates\n\nAPI didn't succesfully accept the the last uploaded batch, aborting.\n",E_USER_ERROR);
		die();
	}
	
// 	echo "\n\nIDs updated: \"\n$payload_updates\n\"\n\n\n";

	update_ids($payload_updates);
	
	echo "PUT http://$username:$passwd@$server_base_url/0.6/changeset/$changeset/close\n";
	
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL,"http://$username:$passwd@$server_base_url/0.6/changeset/$changeset/close");  
	curl_setopt($ch, CURLOPT_PUT, 1); 
	curl_setopt($ch, CURLOPT_INFILE, $fp = fopen('/dev/null','r')); 
	curl_setopt($ch, CURLOPT_INFILESIZE, 0); 
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); 
	$close_error = curl_exec($ch);
	fclose($fp);

	$info = curl_getinfo($ch);
	echo "API returned HTTP code {$info['http_code']}\n";
	echo "Changeset $changeset closed, error was \"$error\"\n\n\n";
	$changeset = NULL;
	sleep(10);
}





function update_ids($payload_updates)
{
	global $updated_node_ids;

	$updates = simplexml_load_string($payload_updates);
	
// 	print_r($updates);
	$updated_node_count = 0;
	$updated_way_count  = 0;
	
	foreach($updates->node as $updated_node)
	{
		$updated_node_ids[ (int) ($updated_node['old_id']) ] = (int) ($updated_node['new_id']);
		$updated_node_count++;
	}
	
	foreach($updates->way as $updated_way)
	{
		$updated_way_ids[ (int) ($updated_way['old_id']) ] = (int) ($updated_way['new_id']);
		$updated_way_count++;
	}
	/// TODO: update relation IDs.
	
	if ($update_node_count) echo "Updated IDs of $update_node_count nodes.\n";
	if ($update_way_count)  echo "Updated IDs of $update_way_count ways.\n";
	
}





// open_changeset('node');
init_payload($payload);

// echo $xml->node[0]->asXML();

// $node_count      = count($xml->node);
// $way_count       = count($xml->way);
// $relation_count  = count($xml->relation);

$i = 0;

foreach($xml->node as $node)
{
	if ($i == $node_batch_size) 
	{
		close_payload($payload);
		send_payload($payload);
		init_payload($payload);
		open_changeset('node');
		$i = 0;
	}
	
	if (!$changeset)
		open_changeset('node');
	
	$node->addAttribute('changeset',$changeset);	/// HACK to make it work with API 0.6
// 	$node->addAttribute('version',1);
	$payload .= $node->asXML() ."\n";
	
	$i++;
}

close_payload($payload);
send_payload($payload);

echo "All nodes from file $file fully uploaded; starting to upload ways.\n";

init_payload($payload);
$changeset = null;
$i = 0;

foreach($xml->way as $way)
{
	if ($i == $way_batch_size) 
	{
		close_payload($payload);
		send_payload($payload);
		init_payload($payload);
		open_changeset('way');
		$i = 0;
	}
	
	if (!$changeset)
		open_changeset('way');
	
	$way->addAttribute('changeset',$changeset);	/// HACK to make it work with API 0.6
// 	$node->addAttribute('version',1);
	foreach($way->nd as $nd)	// Only change IDs of nodes if they have been just uploaded... positive IDs will remain the same. This will fail if there are negative IDs which have not been *just* uploaded.
	{
		if ( isset($updated_node_ids[ (int) $nd['ref']]) )
			$nd['ref'] = $updated_node_ids[ (int) $nd['ref'] ];
	}
	$payload .= $way->asXML() ."\n";
	$i++;
	
}

close_payload($payload);
send_payload($payload);


/// TODO: upload relations !!!!




echo "File $file fully uploaded.\n\n\n";




