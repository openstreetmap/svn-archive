<?php

/**
 * Transport for Cross-domain AJAX calls
 *
 * This is an implementation of a transport channel for utilizing cross-domain
 * AJAX calls. This script is passed the data through AJAX along with two special
 * hidden field containing the action URL and the http method (GET/POST). It then  
 * sends the form fields to that URL and returns the response.
 *
 * @package		CrossDomainAjax
 * @category	CURL
 * @author		Md Emran Hasan <phpfour@gmail.com>
 * @link		http://www.phpfour.com
 */

// The actual form action
$action = $_REQUEST['url'];

// Submission method
$method = $_REQUEST['method'];

// Query string
$fields = '';

// Prepare the fields for query string, don't include the action URL OR method
if (count($_REQUEST) > 2)
{
    foreach ($_REQUEST as $key => $value)
    {
        if ($key != 'url' && $key != 'method')
        {
            $fields .= $key . '=' . rawurlencode($value) . '&';
        }
    }
}

// Strip the last comma
$fields = substr($fields, 0, strlen($fields) - 1);

// Initiate cURL
$ch = curl_init();

// Do we need to POST of GET ?
if (strtoupper($method) == 'POST')
{   
    curl_setopt($ch, CURLOPT_URL, $action);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
}
else
{
    curl_setopt($ch, CURLOPT_URL, $action . '?' . $fields);   
}

// Follow redirects and return the transfer
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);

// Get result and close cURL
$result = curl_exec($ch);
curl_close($ch);

// Return the response
echo $result;

?>
