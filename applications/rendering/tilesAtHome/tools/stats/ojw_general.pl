
#/usr/bin/perl
#------------------------------------------------------------------
# Analyses tiles@home stats file, produces HTML with tables of
# tile count per user,z,date,etc.
# 
# Usage: ojw_general.pl < stats.txt > ojw_general.htm
#
# See the download script for how to obtain stats.txt
#
# OJW 2007, GNU GPL v2 or later
#------------------------------------------------------------------
use Date::Format;
while(($X,$Y,$Z,$User,$Size,$Date) = split(/,/,<>)){
  chomp $Date;
  
  # Count lots of different things...
  $CountByDate{int($Date/86400)}++;
  $CountByHour{int(($Date%86400)/3600)}++;
  $CountByWeek{int($Date/(7*86400))}++;
  $CountBySize{int($Size/4096)}++;
  $CountByZ{$Z}++;
  $SizeByZ{$Z}+=$Size;
  $CountByUser{$User}++;
  $SizeByUser{$User}+=$Size;
  $Count++;
  
  #last if($Count >= 200); # Optional: only process a few lines of input, to test code
}

$Date = time2str("%o %h",time());
$Title = "tiles\@home stats, $Date";
print "<html><head><title>$Title</title></head><body><h1>$Title</h1>\n";
print "<p>Analysed $Count tiles</p>\n";

$Pass = sub{shift()};

# Report: by user
reportCountAndSize(\%CountByUser, \%SizeByUser, sub{$a cmp $b}, "Users");

reportCountAndSize(\%CountByZ, \%SizeByZ, sub{$a <=> $b}, "Zoom levels");

# Tile count by filesize
report(\%CountBySize, "By size", 
  sub{$U = 4; $S1 = shift() * $U; sprintf("%d-%d KB",$S1,$S1+$U)});

report(\%CountByDate, "By date", 
  sub{time2str("%o %h",shift() * 86400)});

report(\%CountByWeek, "By week", 
  sub{"w/b: ". time2str("%o %h",shift() * 86400*7)});

report(\%CountByHour, "Hour of day", 
  sub{sprintf("%02d:00", shift())});

print "</body></html>";

# ---------------------------------------------------------------
# Displays a hash as HTML
# ---------------------------------------------------------------
# * Ref is a hash array that we want to display
# * Title of the report
# * Fn is a function for formatting the keys of the hash
#
sub report{
  my ($Ref, $Title, $Fn) = @_;
  print "<h2>$Title</h2>\n";
  print "<table border=1 cellpadding=4 cellspacing=0>\n";
  foreach $k (sort { $a <=> $b } keys %$Ref){
    $v = $$Ref{$k};
    printf("<tr><td>%s</td><td>%d</td></tr>\n", $Fn->($k),$v);
  }
  print "</table>\n\n\n";
}

# ---------------------------------------------------------------
# Displays two hashes (count,size) as HTML
# ---------------------------------------------------------------
# * RefA is a hash array of tile count
# * RefB is a hash array of total tile size (with the same keys as RefA)
# * SortFn is a function for sorting the keys
# * Title of the report
#
sub reportCountAndSize{
  my ($RefA, $RefB, $SortFn, $Title) = @_;
  print "<h2>$Title</h2>\n";
  print "<table border=1 cellpadding=4 cellspacing=0>\n";
  foreach $k (sort $SortFn keys %$RefA){
    $Count = $$RefA{$k};
    $Size = $$RefB{$k};
    printf("<tr><td>%s</td><td>%d tiles</td><td>%1.1f MB</td><td>avg %1.1f KB</td></tr>\n", 
      $k,
      $Count,
      $Size / (1024 * 1024),
      ($Size / $Count) / 1024);
  }
  print "</table>\n\n\n";
}
