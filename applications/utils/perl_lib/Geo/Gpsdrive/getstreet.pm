package Geo::Gpsdrive::getstreet;
#noch nicht alle variablen angepasst
use LWP::UserAgent;
use WWW::Mechanize;
use Text::Query;
use Getopt::Long; #maybe not needed any more
use Pod::Usage;
use DBI;
use strict;
#use Thread;
use threads;
    my %hash;
    my @header;
    my @lines;
    my @rows;
    my $x; #counter
    my $use_street;
    my $use_city;
    our  $street = $main::street;  #not needed
    our  $use_zip = $main::plz;
    our  $ort = $main::ort;		#not needed
    our  $mysql =$main::sql;
    our  $file = $main::file;
    our  $country;
    our  $version;
    our  $area;		# dont work at the moment not changed since moved form .pl to .pm
    our  $scale;		# dont work
    our  $name;
    our  @street;
    our  $type="STREET";
    our  $comment="";
    our $help;
    our  $VER="getstreet (c) SHB\nInitial Version (Mar,2005) by SHB\n";
sub streets(){
#	'nname=s'	=>	\$name,
#	'n=s'		=>	\$name,
#	'country=s'	=>	\$country,
#	'c=s'		=>	\$country,
#	'f=s'		=>	\$file,
#	'file=s'	=>	\$file,
#	'scale=s'	=>	\$scale,
#	'a=s'		=>	\$area,
#	'type=s'	=>	\$type,
#	't=s'		=>	\$type,
#	'comment=s'	=>	\$comment,

    if($help){
	print "\n\nHelp\n\n";
	
	print "Usage:\n";
	print "Usage getstreet.pl\n";
	print "-p, --plz\t zipcode\n";
	print "-o, --ort\t country\n";
	print "-s, --street \t\t\"name of street\"\n";
	print "-f, --file\t\t name of a file instat a streetname";
	print "\n\n";
	print "optional parameters:\n";
	print "-a Umkreis --scale scale\n";
	print "-t, --type\ttype \n";
	print "--sql insert query in the mysql database\n";
	print "optional sql parameters:\n";
	print "\t --comment \t insert a comment into the database";
	print "\n\n";
	print "-h, --help \tshow this display and exit the programm\n";
	print "-v, --version show the version\n ";
	print "At the moment only germany is supportet\n\n";
	print "note these script is beta\n\n";
    }
    if($country eq "help"){
    	#needed for other country than germany, not working at the moment
	print "List of countrys:\n\n";
	print "Australien : 22\n";
	print "Belgien : 1\n";
	print "Brasilien : 23\n";
	print "Dänemark : 3\n";
	print "Deutschland : 5\n";
	print "Finnland (Helsinki) : 16\n";
	print "Frankreich : 4\n";
	print "Griechenland (Athen) : 24\n";
	print "Großbritannien : 11\n";
	print "Italien : 6\n";
	print "Kanada : 2\n";
	print "Luxemburg : 7\n";
	print "Niederlande : 8\n";
	print "Norwegen : 17\n";
	print "Österreich : 0\n";
	print "Portugal : 18\n";
	print "Schweden : 19\n";
	print "Schweiz : 10\n";
	print "Spanien : 9\n";
	print "Vereinigte Staaten : 12\n";
	print "\n\n";
    }
if($main::street && $main::ort){
	$use_street = $main::street;
	$use_city = $main::ort;
	$use_zip = $main::plz;
	if($main::thread){
		print "using threads\n";
		my $thread = threads->new( \&getstreet );
		$thread->join;
	}else{
		&getstreet;
	}
}
    if($main::file){	
	open(FILE, "< file");
	while( <FILE>){
	    $_ =~ s/\n//g;		#maybe delete if errors in the array, but i dont think so
	    push(@street,$_);
	}
	close(FILE);
	my $sum_str =@street;
	if($street[0] =~ /^<.*>$/){
		$street[0] =~ s/\<//g; 		# remove this <
		$street[0] =~ s/\>//g; 		#remove this >
		#$street[0] =~ s/\n//g; 	#not need if $_ =~ s/\n//g; work
		@header = split(/;/,$street[0]);
		my $a=0;
		my $sum_header = @header;
		for(@header){
			my $y=0;
			$x = 1;
				while($y<$sum_str){
				@lines = split(/;/,$street[$x]);
		##		print "$lines[$a]";##
	#			if( $street[$x] !~ /^#/){
					$rows[$y] = $lines[$a];
	#			}
				$x++;
				$y++;
				}
			$a++;
		#	print "Hash $_ Erstellt\n";  #can remove, only for showing which hashs are createt###
			$hash{$_} = [@rows];	
			
		}
		if(!$hash{street}){
			print STDERR "a <street> section is needed in the \$file \n";  #\$file must be changed in $main::file
			exit 2;
		}
		if(!$hash{city}){ 
			print STDERR "a <city> section is needet in the \$file \n";
			exit 3;
		}
		if(!$hash{zip}){
			print STDERR "<zip> don't exitsts, this doesent matter, but maybe you will need it\n";
		}
#	print "@header\n"; #not needed remove, only for testing
	#print "test hash print: $hash{city}[1]\n";
	}
	else
	{
		print " file has wrong format\n"; #\$file musst be changed in main::file #give an example for the file
		exit 4;
	}

	$x=0;
	my @Threads;
	my $hash_length=@{$hash{street}};
#	print $hash_length;
	my $time =0;
	for(0..$hash_length){
	    $use_street=$hash{street}[$x];
	    $use_city=$hash{city}[$x];
	    $use_zip=$hash{zip}[$x];
	    if($use_city eq ""){
		$use_city = $hash{city}[$x-1];
		$hash{city}[$x]=$hash{city}[$x-1];
	    }
	    if($use_zip eq ""){
		$use_zip = $hash{zip}[$x-1];
		$hash{zip}[$x]=$hash{zip}[$x-1];
	    }
	    
	    if($main::thread){
#	    print "using threads\n";
	    push(@Threads,threads->new(\&getstreet));
	  $time++;
	  if($time>=9){
	  	sleep 30;
		$time=0;
	  }
	  }else{
		&getstreet;
	  }
	    $x++;
	}
	if($main::thread){
		foreach(@Threads)
		{
		  $_->join();
		 # $_->detach();
		}
	}
    }
    return 1;
}
sub getstreet {
	print "$use_street|$use_city|$use_zip\n";	#needed for testing ;)
	my $url="http://mappoint.msn.de";
	my $val = "MapForm:POIText";
	my $v_ort = $use_city;
	my $v_plz = $use_zip;
	my $v_str = $use_street	;	# erinerung
	$v_str =~ s/ü/u/g;
	$v_str =~ s/ö/o/g;
	$v_str =~ s/ä/a/g;
	$v_str =~ s/Ü/U/g;
	$v_str =~ s/Ö/O/g;
	$v_str =~ s/Ä/Ä/g;
	$v_str =~ s/ß/ss/g;
	my $ort = "FndControl:CityText";
	my $plz = "FndControl:ZipText";
	my $str = "FndControl:StreetText";
	my $cou = "FndControl:ARegionSelect";
	my $form = "FindForm";
	my $mech= WWW::Mechanize->new();
	$mech->agent_alias( 'Windows IE 6' );
	$mech->get($url);
	$mech->form_name($form);
	##
	$mech->field($ort,$v_ort);
	$mech->field($plz,$v_plz);
	$mech->field($str,$v_str);
	$mech->submit();
	if($mech->form_name('MCForm')){
	    $mech->form_name('MCForm');
	    my $out= $mech->value($val);
	    my @split=split(/\|/,$out);
	    my @cord=split("%2c",$split[1]);
	    my @addr=split("%2c",$split[3]);
	    my $str=$addr[0];
	    my @ort_plz=split(/\+/,$addr[1]);
	    $ort=$ort_plz[2];		#unused
	    $plz=$ort_plz[1];		#unused
	    $str =~ s/%c3%9f/ss/g;
	    $str =~ s/%c3%bc/ue/g;
	    $str =~ s/\+/\_/g;
	    if($str eq ""){
		print "$str\n";
		print "$out\n";
		print "Strasse nicht gefunden\n";
	    }else{
		print STDERR "$out\n";
		if($name ne ""){
		    $str=$name;
		}
		my $ausgabe= "$str\t$cord[0]\t$cord[1]\t$main::type\n";
		my $datei = "$ENV{'HOME'}/.gpsdrive/way.txt";
		if($main::sql){
		$comment = $split[3];
		print "comment: $comment\n";
		    my $dbh = DBI->connect( "dbi:mysql:$main::GPSDRIVE_DB_NAME", $main::db_user, $main::db_password )
			|| die "Kann keine Verbindung zum MySQL-Server aufbauen: $DBI::errstr\n";
		    my $query ="insert into waypoints(name,lat,lon,type,comment) values('$str','$cord[0]','$cord[1]','$type','$comment')";
		    $dbh->prepare($query)->execute;
		}else{
		    open(FILE,">>$datei")||die "Error: File not found\n";
		}
		print FILE $ausgabe;
		close(FILE);
		if($area && $scale){
		    system("gpsfetchmap.pl -w $str -a $area --scale $scale");
		}
	    }
	}elsif($mech->form_name('FindForm')){
	   #this will happen, if you get a multiple choice answer
	    #print $mech->value('FndControl:AmbiguousSelect');
	  #  print "\n";
	    print "Multiplie choice \nstill in development\n";
	   
	}
    }

1;
