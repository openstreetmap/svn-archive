#!/usr/bin/perl
#------------------------------------------------------------------------
# Program to parse the osmathome upload stats file, and format it as a
# wiki page
#
# (c) 2006, Oliver White. GNU GPL v2 or later
#
# Usage:  tilestat.pl [optional URL] > textfile
#------------------------------------------------------------------------
use LWP::Simple;
$URL = shift() || "http://osmathome.bandnet.org/Upload/log.txt";
$TempFile = "log.txt";
print "Downloading $URL\n";
getstore($URL, $TempFile) || die("Couldn't fetch file\n");
open(IN, "<", $TempFile) || die("Can't read $TempFile\n");
while($Line = <IN>){
        if($Line =~ /(\d+\-\d+\-\d+) (\d+:\d+) (\w+) uploaded (\d+) tiles \((.*)\)/){
                my($Date, $Time, $User, $Num, $Size) = ($1,$2,$3,$4,$5);
                $UploadsByDate{$Date}++;
                $TilesByDate{$Date} += $Num;		
                $SizeByDate{$Date} += $Size;

                $Users{$User}++;

                $UploadsByUser{$User}++;
                $TilesByUser{$User} += $Num;		
                $SizeByUser{$User} += $Size;

                $TotalUploads++;
                $TotalTiles += $Num;
                $TotalSize += $Size;
        }
}
close IN;

$StartTable = "{| border=1 cellpadding=5\n| || Uploads || Tiles || Compressed size\n|-\n";

printf "==General==\n";
print "Total data in logfile\n";
printf "* %d uploads\n", $TotalUploads;
printf "* %d tiles\n", $TotalTiles;
printf "* %1.1f GB\n", $TotalSize / (1024 * 1024);
print "\n\n";

printf "==By date==\n$StartTable";
foreach $Date(sort keys %TilesByDate){
        printf "|-\n|%s || %d || %d tiles || %1.1fM\n",
                $Date,
                $UploadsByDate{$Date},
                $TilesByDate{$Date},
                $SizeByDate{$Date} / 1024;
}
print "|}\n\n";

printf "==By users==\n$StartTable";
foreach $User(sort keys %TilesByUser){
        printf "|-\n|%s || %d || %d tiles || %1.1fM\n",
                $User,
                $UploadsByUser{$User},
                $TilesByUser{$User},
                $SizeByUser{$User} / 1024;
}
print "|}\n\n";

