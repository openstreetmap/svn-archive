#!/usr/bin/perl -w

	# Generate localisation files for Potlatch
	# v2, creates separate directories for each language
	# (will be expanded to read help files, too)

	use LWP::Simple;
	use LWP::UserAgent;
	use HTTP::Request;
	$ua=LWP::UserAgent->new;
	$ua->agent("localise.pl");

	$config="../../../sites/rails_port/config/potlatch";
	
	# -----	Get index page	

	$index=get("http://wiki.openstreetmap.org/index.php/Category:Potlatch_translation");
	die "Couldn't fetch category index" unless defined $index;
	
	# -----	Get each page
	
	%translations=();

	while ($index=~/\/wiki\/([^:]+):Potlatch\/Translation/gs) {
		$lang=lc $1; next if $lang eq 'template';
		$req=HTTP::Request->new(GET=>"http://wiki.openstreetmap.org/index.php/$lang:Potlatch/Translation");
		$res=$ua->request($req);
		if ($res->is_success) { $wiki=$res->content; }
						 else { die "Bugger! ".$res->status_line."\n"; }
		print "Reading $lang\n";
		$lang=~s/[\-_](.+)/\-\U$1\E/;	# dialect in uppercase
		while ($wiki=~/^<td>\s*([^<]+)<\/td><td>\s*([^<]+)<\/td><td>\s*(.+)$/gm) {
			$id=$1; $en=$2; $tr=$3;
			$id=~s/\s+$//g;
			$en=~s/\s+$//g;
			$tr=~s/&nbsp;/ /g; $tr=~s/^\s+//;
			if ($tr=~/^".+[^"]$/) { $tr.='"'; }
			if ($tr=~/[:']/ and $tr!~/"/) { $tr="\"$tr\""; }
			$substitute=1;
			next if ($id eq '');
			next if ($tr eq '...');
			while ($tr=~/\%([^%]+)\%/g) { $tr=~s/\%([^%]+)\%/\$$substitute/;
										  $substitute++; }
			unless (exists $translations{$lang}) { $translations{$lang}=''; }
			$translations{$lang}.="\"$id\": $tr\n";
		}
	}

	# -----	Output translation file
	
	print "Writing output files\n";
	foreach $lang (sort keys %translations) {
		unless (-d "$config/localised/$lang") { mkdir "$config/localised/$lang"; }
		open (OUTFILE,">$config/localised/$lang/localised.yaml") or die "Can't open output file: $!\n";
		print OUTFILE chr(0xEF).chr(0xBB).chr(0xBF);	# utf8 BOM
		print OUTFILE $translations{$lang};
		close OUTFILE;
	}

	
	# Potlatch 'intText' routine needs to:
	# - replace '\n' with a real line-break
	# - replace $1 etc. with parameters passed

	# Change /lib/potlatch.rb to read this in as a YAML file
	# (the parameters at the end will still be passed back via AMF as is,
	#  so nothing else needs to be changed other than the SWF)
