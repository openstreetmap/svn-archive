<?php

// http://blog.mypapit.net/2006/02/sending-http-post-with-php-curl.html
//
$url = 'http://altitude.sprovoost.nl/profile/gchart/xml/';
$ch = curl_init();

// set the target url
curl_setopt($ch, CURLOPT_URL,$url);

// how many parameter to post
curl_setopt($ch, CURLOPT_POST, 1);

$xml_str = $_POST[xml_str];
// xml comes with " replaced by \"
$xml_str = str_replace('\"', '"', $xml_str);

curl_setopt($ch, CURLOPT_POSTFIELDS,$xml_str);

$result= curl_exec ($ch);
curl_close ($ch);

print $result;

?>

