#!/usr/bin/perl
$File = "temp/stats.txt";
$In = "modules";
$Out = "output";
print "Running stats programs in $In (output to $Out)\n";
# Index shows you what modules are available
open(INDEX,">$Out/index.htm") || die("Can't write to index ($!)\n");
print INDEX "<html><head><title>Tiles\@home stats</title>\n<link rel=\"stylesheet\" href=\"../styles.css\"</head><body>\n<h1>Stats modules available</h1>";

opendir(DIR, $In) || die("No directory $In");
while($File = readdir(DIR)){
  if($File =~ /^mod_(\w+)/){
    $Name = $1;
    $Program = "$In/$File";
    $OutDir = "$Out/$Name";
    
    print "* Module: \"$Name\"\n";
    
    # Look for description
    $Description = readfile("$In/description_$Name.txt");
    
    # Create a directory for the module's results
    mkdir($OutDir) if(!-d $OutDir);
    
    # Run the program
    `$Program $File $Out > $OutDir/index.htm 2>$OutDir/errors.txt`;
    
    # Index
    print INDEX "<p><a href=\"$Name/index.htm\">$Name</a> - $Description (<a href=\"$Name/errors.txt\">*</a>)</p>\n";
    
    }

}
print INDEX "</body></html>";
close;

sub readfile{
  $file = shift();
  return("no description (use $file)") if(!-f $file);
  open(my $fp, "<$file") || return("Can't open $file");
  $Data = join("", <$fp>);
  close $fp;
  return($Data);
}