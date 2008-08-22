#!/usr/bin/perl

print "version 300
charset \"utf-8\"
coordsys earth projection 1, 104
delimiter \",\"
columns 1
cat integer
data\n";

my $p=1000000;
$wayid=0;

while (<>) {
        $line = $_;
        chomp($line);
	if(0) {
	} elsif ($wayid>0) {
		if ($line =~ /<\/way>/){ # ende des way
                	printf "PLINE %i\n" , $wayix ;
			for(my $i = 0; $i < $wayix; $i++) {
				#printf " %i \n" , $way{$i};
				printf " %f %f\n" , $lon{$way{$i}+$p} ,$lat{$way{$i}+$p};
			}
                	#printf "end: wayid %i\n" , $wayix ;
			$wayid = 0;
        	} elsif ($line =~ /<nd ref=["'](.*?)["']\/>/) {
			$way{$wayix} =$1  ;   
                	#printf "line: %i \n" , $way{$wayix};
			$wayix++;
		}
        } elsif ($line =~ /<node id=["'](.*?)["'].*?lat=["'](.*?)["'].*?lon=["'](.*?)["']/) {
                $lat{ $1 +$p} = $2;
                $lon{ $1 +$p} = $3;
                #printf "line: %f %f  \n" ,$2 ,$3;
        } elsif ($line =~ /<way id=["'](.*?)["']/) { #way gefunden:
		$wayid = $1;
		$wayix = 0;
                #printf "wayid: %i  \n" ,$wayid ;
        }
}

