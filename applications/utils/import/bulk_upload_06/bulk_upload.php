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

/// Set this to false in order to not save temporary files after every batch is uploaded.
$make_backup = true;



/// TODO: fix this, use mktempfile() or something
$tmpfilename = '/tmp/bulk_upload_temp_' . posix_getpid() ;

$node_batch_size = 25;
$way_batch_size  = 10;



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
	
	echo "\n\nIDs updated: \"\n$payload_updates\n\"\n\n\n";

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
	global $nodes, $ways, $inverse_waynodes;

	$updates = simplexml_load_string($payload_updates);
	
// 	print_r($updates);
	$updated_node_count    = 0;
	$updated_way_count     = 0;
	$updated_waynode_count = 0;
	
	foreach($updates->node as $updated_node)
	{
// 		$updated_node_ids[ (int) ($updated_node['old_id']) ] = (int) ($updated_node['new_id']);
		$old_id = (int)$updated_node['old_id'];
		$new_id = (int)$updated_node['new_id'];
		if (!$nodes[$old_id])
		{
			echo " ERROR: API returned updated node id for non-existing node $old_id (new ID: $new_id). This should not happen.\n";
		}
		else
		{
			$node = $nodes[$old_id];
			unset ($nodes[$old_id]);
			$node['id'] = $new_id;
			$node['version'] = 
			$nodes[$new_id] = $node;
			/// TODO: add sanity check to make sure that this updated node has been indeed uploaded
			$updated_node_count++;
// 			echo "Updated node: $old_id -> $new_id\n";
		}
		
		if ($inverse_waynodes[$old_id])
		{
			foreach($inverse_waynodes[$old_id] as $way_id=>$foo)
			{
				foreach($ways[$way_id]->nd as $nd)	// Only change IDs of nodes if they have been just uploaded... positive IDs will remain the same. This will fail if there are negative IDs which have not been *just* uploaded.
				{
					if ( $nd['ref'] == $old_id )
					{
						$nd['ref'] = $new_id;
						$updated_waynode_count++;
// 						echo "Updated waynode: $way_id ($old_id) -> $new_id\n";
					}
				}
			}
			$inverse_waynodes[$new_id] = $inverse_waynodes[$old_id]; 
			unset ($inverse_waynodes[$old_id]);
		}
	}
	
	foreach($updates->way as $updated_way)
	{
// 		$updated_way_ids[ (int) ($updated_way['old_id']) ] = (int) ($updated_way['new_id']);
		$old_id = (int)$updated_way['old_id'];
		$new_id = (int)$updated_way['new_id'];
		if (!$ways[$old_id])
		{
			echo " ERROR: API returned updated node id for non-existing way $old_id (new ID: $new_id). This should not happen.\n";
		}
		else
		{
			$way = $ways[$old_id];
			unset ($ways[$old_id]);
			$way['id'] = $new_id;
			$ways[$new_id] = $way;
			/// TODO: add sanity check to make sure that this updated node has been indeed uploaded
			$updated_way_count++;
// 			echo "Updated way: $old_id -> $new_id\n";
		}	
	}
	/// TODO: update relation IDs.
	
	if ($updated_node_count)    echo "Updated IDs of $updated_node_count nodes.\n";
	if ($updated_way_count)     echo "Updated IDs of $updated_way_count ways.\n";
	if ($updated_waynode_count) echo "Updated IDs of $updated_waynode_count way nodes.\n";

	global $make_backup;
	if ($make_backup)
	{
		global $batch;
		$file = "/tmp/bulk_uploader_backup_$batch.osm";
		echo "Writing data backup after batch $batch to file $file ...\n";
		$fd = fopen($file,'w');
		fwrite($fd,"<?xml version='1.0' encoding='UTF-8'?><osm version='0.6' generator='php_bulk_uploader_backup'>");
		foreach ($nodes as $node)
		{
			fwrite($fd,$node->asXML() . "\n");
		}
		foreach ($ways as $way)
		{
			fwrite($fd,$way->asXML() . "\n");
		}
		/// TODO: backup relations!!
		fwrite($fd,"</osm>");
		fclose($fd);
		echo "Backup wrote.\n";
	}
}






/// Main stuff




/// Build up auxiliary arrays
$nodes = array();
$ways = array();
$inverse_waynodes = array();

echo "Preparing nodes...\n";
if ($xml->node)
foreach($xml->node as $node)
{
	$nodeid = (int) $node['id'];
	$nodes[$nodeid] = $node;
}

echo "Preparing ways...\n";
if ($xml->way)
foreach($xml->way as $way)
{
	$wayid = (int) $way['id'];
	$ways[$wayid] = $way;
	foreach($way->nd as $nd)
	{
		$ref = (int) $nd['ref'];
		$inverse_waynodes[$ref][$wayid] = true;
	}
	
}
echo "Data prepared.\n";
unset($xml);

// print_r($inverse_waynodes);

// die();


// open_changeset('node');
init_payload($payload);

// echo $xml->node[0]->asXML();

// $node_count      = count($xml->node);
// $way_count       = count($xml->way);
// $relation_count  = count($xml->relation);

$i = 0;

if ($nodes)
{
	foreach($nodes as $node)
	{
		if ($node['uploaded'])
		{
			echo "Skipping node " . $node['id'] . "\n";
		}
		else
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
			
			$node['uploaded'] = true;
			
			$i++;
		}
	}

	close_payload($payload);
	send_payload($payload);
	echo "All nodes from file $file fully uploaded; starting to upload ways.\n";
}
else
{
	echo "No nodes to upload\n";
}


init_payload($payload);
$changeset = null;
$i = 0;

if ($ways)
{
	foreach($ways as $way)
	{
		if ($way['uploaded'])
		{
			echo "Skipping way " . $node['id'] . "\n";
		}
		else
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
	
		$payload .= $way->asXML() ."\n";
		
		$way['uploaded'] = true;
		
		$i++;
		}
	}
	
	close_payload($payload);
	send_payload($payload);
	echo "All nodes from file $file fully uploaded; starting to upload ways.\n";
}
else
{
	echo "No ways to upload\n";
}

/// TODO: upload relations !!!!




echo "File $file fully uploaded.\n\n\n";




