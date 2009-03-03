#-----------------------------------------------------------------
# Parses an OpenStreetMap XML file looking for tags, and counting
# how often each one is used
#-----------------------------------------------------------------
# Will create an ./Output/ directory and fill it with text files
# describing the tags used in data.osm
#-----------------------------------------------------------------
# This file is part of Tagwatch
# Tagwatch is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Tagwatch is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Tagwatch.  If not, see <http://www.gnu.org/licenses/>.
#---------------------------------------------------------------
use strict;
use libs::getWikiSettings;

sub processOSMFiles
{
	my (%Config) = @_;

	my $SelectedOutputDir = $Config{'cache_folder'};
	mkdir $SelectedOutputDir if ! -d $SelectedOutputDir;

	# clean all old cached files
	my $cleanFolders = sprintf("rm -f -R %s/output*", $Config{'cache_folder'});
	system($cleanFolders);

	my %IgnoreTags = ();
	if($Config{'use_ignorelist_tags'} eq "yes")
        {
		%IgnoreTags = getIgnoredTags("$Config{'cache_folder'}/wiki_settings");
	}
	my @IgnoreValues;
	my $IgnoreCount = 0;
	if($Config{'use_ignorelist_values'} eq "yes")
        {
          @IgnoreValues = getIgnoredValues("$Config{'cache_folder'}/wiki_settings");
        }
	else
	{
          $IgnoreCount = $Config{'max_volatile_count'} || 100;
	}
	my %WatchedKeys  = buildCombiPageList($Config{'cache_folder'});

	my @OsmFileList  = getOsmFileList($Config{'osmfile_folder'});

	foreach my $File (@OsmFileList)
	{
		my @FileDetails=split(/\|/,$File);

		my $Country = $FileDetails[0];
		$Country =~ s/.osm(\.bz2|.gz)?$//g;
		$Country = ucfirst($Country);
		
		my $Date = $FileDetails[1];
		my $OSMfile = $FileDetails[0];

		my $OutputDir = sprintf("%s/output_%s", $Config{'cache_folder'}, $Country);
		mkdir $OutputDir if ! -d $OutputDir;
	
		print "\tstart processing :: $OSMfile\n";

		my $Tagtype = '-';               # What object the parser is in
		my %Keys;
		my %Tags;
		my %Values;
		my %IgnoredValues;
		my %Usage;
		my %User;
		my %Editors;
		my %Stats;
	
		my @TempCombi;
		my %Combinations;
		my %Relations;
		my @TempMember;
		my $Relationtype=="-";

		my $name = "$Config{'osmfile_folder'}/$OSMfile";
		my $splitvals = ($Config{'split_values'} || "no") eq "yes";
		my $openmode = "<$name";
		$openmode = "bunzip2 <$name |" if($name =~ /\.bz2$/);
		$openmode = "gunzip <$name |" if($name =~ /\.gz$/);

		open(OSMFILE, $openmode) || die("Could not open osm file! :: $OSMfile");
		my $count = 1;
	
		while(my $Line = <OSMFILE>)
		{
			print "\tprocessed line ".int($count/1000000).",000,000\n" if !($count % 10000000);
			++$count;
			if($Line =~ m{<tag k=["'](.*?)["'] v=["'](.*?)["']\s*/>})
			{
				# Tag within an object
				my ($Key, @Values) = ($1, $2);

				if($splitvals)
				{
					@Values = split(" *; *", $Values[0]);
				}

				$Keys{$Key}++ if !$IgnoreTags{$Key};

				foreach my $Value (@Values)
				{
					next if !$Value;

					# save the type of the Relation if we have one.
					if($Key eq "type")
					{
						$Relationtype = $Value;
						$Relationtype =~ s/^$/-/g;
						$Relationtype =~ s/^ $/-/g;
					}

					# get some more statistics about the used editors
					if($Key eq 'created_by')
					{
						$Editors{$Value}++;
					}
					if(!$IgnoreTags{$Key})      # Ignored tags
					{
						my $TempTagName;
						if($IgnoreCount)
						{
							my $OrigValue = $Value;
							my $num = 0;
							$num = scalar(keys %{$Values{$Key}}) if exists($Values{$Key});
							if($num == 1 && exists($Values{$Key}->{"*"}))
							{
								$Value = "*";
							}
							elsif($num >= $IgnoreCount)
							{
								my $count = 0;
								foreach my $n (values %{$Values{$Key}})
								{
									my $old = "$Key=$Value";
									my $new = "$Key=*";
									if(exists($Combinations{$old}))
									{
										foreach my $c (keys %{$Combinations{$old}})
										{
											$Combinations{$c}{$new} += $Combinations{$c}{$old};
											delete $Combinations{$c}{$old};
											$Combinations{$new}{$c} += $Combinations{$old}{$c};
											delete $Combinations{$old}{$c};
										}
										delete $Combinations{$old};
									}
									$count += $n;
								}
								$IgnoredValues{$Key} = $Values{$Key};
								$Values{$Key} = {"*" => $count };
								$Value = "*";
								print "\tAutoIgnoring key $Key - reached $IgnoreCount entries\n";
							}
							$IgnoredValues{$Key}{$OrigValue}++ if($Value eq "*" && exists($IgnoredValues{$Key}{$OrigValue}));

							$TempTagName = "$Key=$Value";
						}
						else
						{
							foreach my $regex(@IgnoreValues)   # Values that will be ignored (grouped with "*")
							{
								if($Key =~ m{$regex}i)
								{
									$TempTagName = "$Key=*";
									$Value = "*";
									last;
								}
								else
								{
									$TempTagName = "$Key=$Value";
								}
							}
						}
						push(@TempCombi,$TempTagName);

						$Tags{$Key}++;
						$Values{$Key}->{$Value}++;
						$Usage{$Key}->{$Value}->{$Tagtype}++;
					}
				}
			}

			#parse relation members
			elsif($Line =~ m{<member type=["'](.*?)["'] ref=["'](.*)["'] role=["'](.*?)["']\s*/>})
			{
				# parse out numbers in relation member names for the route relation
				# just a first hack to deal with this kind of relations.
				my $Membername = $3;
				my $Membertype = $1;
				if($Membername =~ m{^(.*)_stop_(\d*)$}i)
				{
					$Membername = "$1_stop_*";
				}

				if($Membername =~ m{^stop_(\d*)}i)
				{
					$Membername = "stop_*";
				}

				push(@TempMember, "$Membertype=$Membername");
			}

			# Beginning of an object
			elsif($Line =~ m{<(node|way|relation) (.*?) user=["'](.*?)["'](.*?|/>)})
			{
				@TempCombi = 0;
				shift @TempCombi;
				@TempMember = 0;
				shift @TempMember;
				
				$User{$3}++;
				$Tagtype = substr($1,0,1);
				$Stats{$Tagtype}++;
				$Relationtype="-";
			}

			# Beginning of an object
			elsif($Line =~ m{<(node|way|relation) (.*?)})
			{
				@TempCombi = 0;
				shift @TempCombi;
				@TempMember = 0;
				shift @TempMember;
				
				$User{"unknown"}++;
				$Tagtype = substr($1,0,1);
				$Stats{$Tagtype}++;
				$Relationtype="-";
			}
			# End of an item
			elsif($Line =~ m{</(node|way|relation)})
			{
				foreach my $tc (@TempCombi)
				{
					foreach my $tc2 (@TempCombi)
					{
						if($tc ne $tc2)
						{
							if($Tagtype eq "r")
							{
								$Relations{$Relationtype}->{'combi'}->{$tc}->{$tc2}++;
							}

							$Combinations{$tc}->{$tc2}++;
						}
					}
					$Relations{$Relationtype}->{'tags'}->{$tc}++;
				}

				if($Tagtype eq "r")
				{
					$Relations{$Relationtype}->{'count'}++;

					foreach my $tm (@TempMember)
					{
						$Relations{$Relationtype}->{'members'}->{$tm}++;	
					}
				}

				# cleanup for next entry
				$Tagtype = '-';
				@TempCombi = 0;
				shift @TempCombi;
				@TempMember = 0;
				shift @TempMember;
				$Relationtype="-";
			}
		}
		
		#+++++++++++++++++++++++++++++++++++
		# Write down key combination pages
		#+++++++++++++++++++++++++++++++++++
		foreach my $c1 (keys %Combinations)
		{
			# build combipages only for keys that are on the watchlist
			my ($Key,$Value) = split(/=/, $c1);
			
			if(($WatchedKeys{$Key} eq 1) || ($Config{'full_key_details'} eq "yes"))
			{
				open(COMBI, ">","$OutputDir/combi_$c1.txt");
		
				foreach my $TagName(sort {$Combinations{$c1}->{$b} <=> $Combinations{$c1}->{$b}} keys(%{$Combinations{$c1}}))
				{
					printf COMBI "%d %s\n", $Combinations{$c1}->{$TagName},  $TagName;
				}

				close COMBI; 
			}
		}

		#+++++++++++++++++++++++++++++++++++
		# Write down ignored stuff
		#+++++++++++++++++++++++++++++++++++
		foreach my $ign (keys %IgnoredValues)
		{
			open(IGN, ">","$OutputDir/ignored_$ign.txt");

			foreach my $Val (sort keys (%{$IgnoredValues{$ign}}))
			{
				printf IGN "%d %s\n", $IgnoredValues{$ign}{$Val},  $Val;
			}

			close IGN;
		}
		
		#+++++++++++++++++++++++++++++++++++
		# Write down all tag pages
		#+++++++++++++++++++++++++++++++++++
		open(OUT, ">","$OutputDir/tags.txt");
		foreach my $Tag(keys %Tags)
		{
			printf OUT "%d %s\n", $Tags{$Tag}, $Tag;
			$Stats{"keys"}++;
		
			open(TAG, ">","$OutputDir/tag_$Tag.txt");
		
			foreach my $Value(sort {$Values{$b} <=> $Values{$a}} keys(%{$Values{$Tag}}))
			{
				printf TAG "%d %d %d %d %s\n", $Values{$Tag}->{$Value}, $Usage{$Tag}->{$Value}->{'n'}, $Usage{$Tag}->{$Value}->{'w'}, $Usage{$Tag}->{$Value}->{'r'}, $Value;
				$Stats{"tags"}++;
			}
		
			close TAG; 
		}
		close OUT;
		
		#+++++++++++++++++++++++++++++++++++
		# Write down relation pages
		#+++++++++++++++++++++++++++++++++++
		open(OUT, ">","$OutputDir/relations.txt");
		foreach my $Relationtype(keys (%Relations))
		{
			# uhm yeah badly hacked but the keys function does not what i expected -.-
			if($Relations{$Relationtype}->{'count'} > 0) {
			printf OUT "%d %s\n", $Relations{$Relationtype}->{'count'}, $Relationtype;
			$Stats{"relation"}++;
		
			open(RELATION, ">","$OutputDir/relation_$Relationtype.txt");

			foreach my $Member(sort {$Values{$b} <=> $Values{$a}} keys(%{$Relations{$Relationtype}->{'members'}}))
			{
				printf RELATION "member %d %s\n", $Relations{$Relationtype}->{'members'}->{$Member},  $Member;
			}

			foreach my $TagName(sort {$Relations{$Relationtype}->{$b} <=> $Relations{$Relationtype}->{$b}} keys(%{$Relations{$Relationtype}->{'tags'}}))
			{
				printf RELATION "tag %d %s\n", $Relations{$Relationtype}->{'tags'}->{$TagName},  $TagName;
			}
			close RELATION; }
		}
		close OUT;

		#+++++++++++++++++++++++++++++++++++
		# Write down key usage list
		#+++++++++++++++++++++++++++++++++++
		open(KEYUSAGE, ">","$OutputDir/keylist.txt");

		foreach my $KeyName(sort {$Keys{$b} <=> $Keys{$a}} keys %Keys) 
		{
			printf KEYUSAGE "%d %s\n", $Keys{$KeyName}, $KeyName;		
		}
		close KEYUSAGE;

		#+++++++++++++++++++++++++++++++++++
		# Write down editor usage stats
		#+++++++++++++++++++++++++++++++++++
		open(EDITORUSAGE, ">","$OutputDir/editorlist.txt");

		foreach my $EditorName(sort {$Editors{$b} <=> $Editors{$a}} keys %Editors) 
		{
			printf EDITORUSAGE "%d %s\n", $Editors{$EditorName}, $EditorName;		
		}
		close EDITORUSAGE;

		#+++++++++++++++++++++++++++++++++++
		# Write down the user statistics page
		#+++++++++++++++++++++++++++++++++++
		open(USER, ">","$OutputDir/user.txt");

		foreach my $Name(sort {$User{$b} <=> $User{$a}} keys %User) 
		{
			printf USER "%d %s\n", $User{$Name}, $Name;	
			$Stats{"user"}++;	
		}
		close OUT;

		#+++++++++++++++++++++++++++++++++++
		# Write down the general statistic page
		#+++++++++++++++++++++++++++++++++++		
		open(STATS, ">","$OutputDir/stats.txt");

			printf STATS "%d user\n",	$Stats{"user"};
			printf STATS "%d keys\n",	$Stats{"keys"};	
			printf STATS "%d tags\n",	$Stats{"tags"};	
			printf STATS "%d unique_relations\n",	$Stats{"relation"};
			printf STATS "%d relations\n",	$Stats{"r"};	
			printf STATS "%d nodes\n",	$Stats{"n"};	
			printf STATS "%d ways\n", 	$Stats{"w"};	

		close STATS;
	}
}

#--------------------------------------------------------------------------
# Create a list of all tags that should be lookied into in deeper detail
#--------------------------------------------------------------------------
sub buildCombiPageList
{
	my ($CacheFolder) = @_;
	my %WatchedKeys = getWatchedKeys("$CacheFolder/wiki_settings");

	# read in all keys that have a description on the wiki
	open(KEYLIST, "<","$CacheFolder/wiki_desc/key_list.txt") || return "";
	
	while(my $Line = <KEYLIST>)
	{
		if($Line =~ m{^\s*(.*)\s*})
		{
			$WatchedKeys{$1}  = 1;
		}
  	}
  	close KEYLIST;

	return %WatchedKeys;
}

sub getOsmFileList
{
	my ($FileFolder) = @_;
	my @FileList;

	open(FILELIST, "<","$FileFolder/filelist.txt") || die("missing flilelist :: don't know what osm fiels should be used.");

	while(my $Line = <FILELIST>)
	{
		push(@FileList,$Line);
  	}
  	close FILELIST;

	return @FileList;
}

1;