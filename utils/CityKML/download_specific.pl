#!/usr/bin/perl
use LWP::Simple;

# downloads one map then exits
$numfail = 0;
$i = shift() || die("Usage: $0 [city number]\n");

$URL = "http://almien.co.uk/OSM/Places/Data/$i.osm.gz";
$File = "data/data_$i.osm.gz";

# If we don't already have a file, download it
if(!is_success(getstore($URL,$File))){
  die("Failed\n");
}
