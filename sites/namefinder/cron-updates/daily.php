<?php

$dailyplaneturl = "http://planet.openstreetmap.org/daily/";
$mailto = "whoever@wherever.com";
$mailfrom = "gazetteer@openstreetmap.org";
$localplanetdirectory = "/home/namefinder/planet"; // which is called cron-updates in svn
$localupdatedirectory = "/home/namefinder/namefinder/util";
$outputscriptname = "{$localplanetdirectory}/update-daily.sh";
$namefindersqlpassword = "CHANGEME";

$scriptheader =<<<EOD
#!/bin/sh
cd {$localupdatedirectory}

EOD;

file_put_contents($outputscriptname, $scriptheader);
chmod($outputscriptname, 0700);

$dailypage = file_get_contents($dailyplaneturl);
if ($dailypage === FALSE) {
  mailerror("cannot read daily planets");
}

if (! preg_match_all('/\\<a href\\=\\"([0-9]{8}-[0-9]{8}\\.osc\\.gz)\\"\\>/', 
                     $dailypage, $matches, PREG_PATTERN_ORDER)) 
{
  mailerror("cannot find any files on daily planets page");
}

$files = $matches[1];

foreach ($files as $file) {
  $localfile = "{$localplanetdirectory}/{$file}";
  if (! file_exists($localfile) && ! file_exists("{$localfile}.done")) {
    echo "fetching {$file}...\n";
    $ch = curl_init("{$dailyplaneturl}{$file}");
    $fp = fopen($localfile, "w");
    if ($fp === FALSE) { mailerror("cannot open output file {$localfile}"); }
    curl_setopt($ch, CURLOPT_FILE, $fp);
    curl_setopt($ch, CURLOPT_HEADER, 0);
    if (curl_exec($ch) === FALSE) { mailerror("cannot fetch {$file}"); }
    curl_close($ch);
    fclose($fp);
  }
}

$processed = "files queued:\n";
$d = dir($localplanetdirectory);
$entries = array();
$dones = array();
while (FALSE !== ($entry = $d->read())) {
  if (preg_match('/^([0-9]{8})-([0-9]{8})\\.osc\\.gz$/', $entry, $matches)) {
    $entries[$matches[2]] = $entry;
  } else if (preg_match('/^([0-9]{8})-([0-9]{8})\\.osc\\.gz\\.done$/', $entry, $matches)) {
    $dones[$matches[2]] = $entry;
  }
}

if (count($entries) == 0) { mailerror ("no entries to update"); }

asort($dones);
asort($entries);
if (count($dones) == 0) {
  $dates = array();
} else {
  $lastdate = '';
  foreach ($dones as $date => $entry) { $lastdate = $date; }
  $dates = array(strtotime($lastdate));
}
foreach ($entries as $date => $entry) { $dates[] = strtotime($date); }

/* check now that entries are consecutive and the first one leads on from the last done */
if (count($dates) > 1) {
  for ($i = 1; $i < count($dates); $i++) {
    if ($dates[$i] != $dates[$i-1] + 24 * 60 * 60) {
      if ($dates[$i] != $dates[$i-1] + 23 * 60 * 60 && $dates[$i] != $dates[$i-1] + 25 * 60 * 60) {
        // special cases for daylight saving change
        mailerror("updates are not consecutive");
      }
    }
  }
}

$plus = '';
foreach ($entries as $entry) {
  $localfile = "{$localplanetdirectory}/{$entry}";
  $command =<<<EOD
php -d memory_limit=128M import.php {$plus}{$localfile} &> {$localfile}.import.log
rm {$localfile} 
touch {$localfile}.done

EOD;
  if (file_put_contents($outputscriptname, $command, FILE_APPEND) === FALSE) {
    rename ($outputscriptname, "{$outputscriptname}.error");
    mailerror("cannot append to $outputscriptname");
  }
  $filesize = filesize($localfile);
  $processed .= $entry . ' ' . $filesize . "\n";
  $plus = '+';
}
$d->close();

$command = "php -d memory_limit=128M update.php &> {$localfile}.update.log\n";
if (file_put_contents($outputscriptname, $command, FILE_APPEND) === FALSE) {
  rename ($outputscriptname, "{$outputscriptname}.error");
  mailerror("cannot append to $outputscriptname");
}

sendmail("updates running", $processed);

// --------------------------------------------------
function mailerror($s) {
  sendmail($s);
  die($s."\n");
}

// --------------------------------------------------
function sendmail($s, $c=NULL) {
  global $mailto, $mailfrom;
  if (is_null($c)) { $c = $s; }
  mail($mailto, "namefinder: ".$s, $c, "From: {$mailfrom}\r\n");
}

?>