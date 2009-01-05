use strict; 

#--------------------------------------------------------------------------
# Reads a tiles@home config file, returns a hash array
#--------------------------------------------------------------------------
sub ReadConfig{
  my %Config;
  while (my $Filename = shift()){
  
    open(my $fp,"<$Filename") || die("Can't open \"$Filename\" ($!)\n");
    while(my $Line = <$fp>){
      $Line =~ s/#.*$//; # Comments
      $Line =~ s/\s*$//; # Trailing whitespace
      
      if($Line =~ m{
	       ^
	        \s*
	        ([A-Za-z0-9._-]+) # Keyword: just one single word no spaces
	        \s*            # Optional whitespace
	        =              # Equals
	        \s*            # Optional whitespace
	        (.*)           # Value
	        }x){

        # Store config options in a hash array
        $Config{$1} = $2;
        print "Found $1 ($2)\n" if(0); # debug option
        }
    }
    close $fp;

  }
  
  return %Config;
}


1;
