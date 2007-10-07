

my $BlankFileSize = 540;
my $dir = "html/Samples";

open(OUT, ">html/unrendered_tags.htm") || die("Can't write to output file\n");
print OUT "<h1>Unrendered popular tags</h1>\n<p>(at zoom-17)</p>\n<ol>";

opendir(my $dp, $dir) || die("No renderings found\n");
while(my $file = readdir($dp)){
  my $fullfile = $dir.'/'.$file;
  if(-s $fullfile == $BlankFileSize){
    #if($file =~ m{(\w+?)\_(.*)\.png}){
    #  push(@Unrendered, "$1 = $2");
    #}
    
    my $text = substr($file,0,-4);
    $text =~ s/_/ /g;
    print OUT "<li>$text</li>\n";
  }
}

print OUT "</ol>\n";
close OUT;
