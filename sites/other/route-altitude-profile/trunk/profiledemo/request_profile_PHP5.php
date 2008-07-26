<?php

// http://netevil.org/blog/2006/nov/http-post-from-php-without-curl
// POST without curl
function do_post_request($url, $data, $optional_headers = null)
 {
    $params = array('http' => array(
                 'method' => 'POST',
                 'content' => $data
              ));
    if ($optional_headers !== null) {
       $params['http']['header'] = $optional_headers;
    }
    $ctx = stream_context_create($params);
    $fp = @fopen($url, 'rb', false, $ctx);
    if (!$fp) {
       throw new Exception("Problem with $url, $php_errormsg");
    }
    $response = @stream_get_contents($fp);
    if ($response === false) {
       throw new Exception("Problem reading data from $url, $php_errormsg");
    }

    // http://www.jonasjohn.de/snippets/php/post-request.htm 
    // split the result header from the content
    $result = explode("\r\n\r\n", $response, 2);

    $header = isset($result[0]) ? $result[0] : '';
    // $content = isset($result[1]) ? $result[1] : '';
 
    return $header;
    
 }

$xml_str = $_POST[xml_str];
// xml comes with " replaced by \"
$xml_str = str_replace('\"', '"', $xml_str);

$url = 'http://altitude.sprovoost.nl/profile/gchart/xml/';

$r = do_post_request($url, $xml_str);

print_r($r);

?>
