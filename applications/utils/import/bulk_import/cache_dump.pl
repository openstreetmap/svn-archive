#!/usr/bin/perl
use warnings;
use strict;

use DB_File;

my $file = shift or die "Must provide cache filename\n";

# Open the cache file, as DB cache
tie my %db_file, "DB_File", $file, O_CREAT|O_RDWR, 0666, $DB_HASH
  or die "Could not open dbcachefile '$file' ($!)\n";

for my $key (keys %db_file)
{
  print "$key: $db_file{$key}\n";
}
