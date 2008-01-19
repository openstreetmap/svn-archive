#!/usr/bin/perl -w

my $filename=$ARGV[0] or die;

print "
SET NAMES 'utf8';

DROP TABLE IF EXISTS \`nodes\`;
CREATE TABLE \`nodes\` (
  \`id\` int(11) NOT NULL,
  \`name\` varchar(255) NOT NULL,
  \`lat\` double default NULL,
  \`lon\` double default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS \`tages\`;
CREATE TABLE \`tages\` (
  \`id\` int(11) NOT NULL,
  \`k\` varchar(255) NOT NULL,
  \`v\` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


";
open(FILE,"zcat $filename|") or die;
foreach my $line (<FILE>) {

    if (($line=~/node id=\"(.*)\" lat=\"(.*)\" lon=\"(.*)\" timestamp=\"(.*?)\"/) or 
	($line=~/node id='(.*)' lat='(.*)' lon='(.*?)' .* timestamp='(.*?)'/) or
	($line=~/node id='(.*)' lat='(.*)' lon='(.*)' timestamp='(.*?)'/)) 
    {
	
	$id=$1;
	$lat=$2;
	$lon=$3;
#        $timestamp=$4;

     %tag=();
 } elsif (($line=~/tag k=\"(.*)\" v=\"(.*)\" \/\>/) or 
	  ($line=~/tag k='(.*)' v='(.*)'\/\>/)) { 
     $key=$1;
     $value=$2;

     $tag{$key}=$value;
 } elsif ($line=~/\<\/node\>/) {
     $print=1;
     if (($lat<44) or ($lat>56)) {
	 $print=0;
     } elsif (($lon<1) or ($lon>18)) {
	 $print=0;
     }
     if (!(defined($tag{"place"}))) {
	 $print=0;
     }
     if ($print) {
	 if (defined($tag{"name"})) {
	     $name=$tag{"name"};
	 } else {
	     $name="";
	 }
	 print "\ninsert into nodes set id=\"$id\",lat=\"$lat\",lon=\"$lon\",name=\"$name\";\n";

	 foreach $key (keys %tag) {
	     $value=$tag{$key};
	     print "insert into tages set id=\"$id\",k=\"$key\", v=\"$value\";\n";
	 }
     }
 }

}
close(FILE);

print "
create index nodes_lon_idx on nodes(lon);
create index nodes_lat_idx on nodes(lat);
create index nodes_name_idx on nodes(name);
create index tages_id_idx on tages(id);

";
