#-----------------------------------------------------------------
# Creates pages describing the tagging schemes in use within 
# OpenStreetmap
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
use HTML::Template;
use Math::Round;


# holds all Key/Tag/Relation Information from the Wiki
my %WikiDescription;
my %WatchList;

# holds the data for a specific .osm files
my %OsmKeyUsage;
my %OsmRelationUsage;
my %UsedUndocumentedKeys;
my %UsedUndocumentedTags;
my %UsedUndocumentedRelations;
my %EditorUsage;
my $Country;
my $Date;
my $curDataDir;
my $curOutputDir;
my %Statistics;
my $CountryCount;
my %SampleRequest;

# General config
my %Config;
my %Interface;
my $lang_links;

sub constructHTML
{
	my (%Configs) = @_;

	%Config = %Configs;

	my @Languages = split(/,/, $Config{'languages'});

	my $OutputTopDir = $Config{'output_folder'};
	mkdir $OutputTopDir if ! -d $OutputTopDir;

	my $OutputPhotoDir = $OutputTopDir."/photos";
	mkdir $OutputPhotoDir if ! -d $OutputPhotoDir;

	my $MainDir = $Config{'main_folder'};
	mkdir $MainDir if ! -d $MainDir;

	# read in all wiki Keys/Tag/Relation information
	print "+ load cached wiki information ...\n";
	%WikiDescription = loadWikiInformation($Config{'cache_folder'});

	%WatchList = getWatchedKeys("$Config{'cache_folder'}/wiki_settings");

	# read in Interface
	%Interface = getInterfaceTranslations(%Config);

	# read in the list of all .osm files
	my @OsmFileList  = getOsmFileList($Config{'osmfile_folder'});
	my @tmpl_file_loop;

	# create language navigation list
	my @templist;
	foreach my $Language(@Languages)
	{
		$Language = ucfirst($Language);
		push(@templist,"<a href=\"../$Language/%s\">$Language</a>");
	}
	$lang_links  = join(", ",@templist);

	# create HTML pages for every <name>.osm file
	foreach my $File (@OsmFileList)
	{
		my @FileDetails=split(/\|/,$File);

		$Country = $FileDetails[0];
		$Country =~ s/.osm(\.bz2|.gz)?$//g;
		$Country = ucfirst($Country);
		$Date = $FileDetails[1];
		my $OSMfile = $FileDetails[0];

		my %row = ("country" => $Country,
			   "indexfile" => $Config{indexname},
			   "date"    => $Date);
		push(@tmpl_file_loop, \%row);
		$CountryCount++;
	
		printf "\tstart html construction :: %s\n", $Country;

		$curDataDir   = sprintf("%s/output_%s",$Config{'cache_folder'},$Country);

		# delete old html files
		my $cleanFolders = sprintf("rm -f -R %s/%s", $Config{'output_folder'},$Country);
		system($cleanFolders);

		my $OutputDir = sprintf("%s/%s",$Config{'output_folder'},$Country);
		mkdir $OutputDir if ! -d $OutputDir;

		# load all used keys and relations for this .osm file
		%OsmKeyUsage      = LoadKeyUsage($curDataDir);
		%OsmRelationUsage = LoadRelationUsage($curDataDir);

		# process stats and build html pages
		foreach my $Language(@Languages)
		{
			$Language = ucfirst($Language);

			$curOutputDir = sprintf("%s/%s", $OutputDir, $Language);
			mkdir $curOutputDir if ! -d $curOutputDir;

			print "\t\t construct language :: $Language\n";

			# build general pages
			buildAllKeyList($Language);
			buildTopUndocumentedKeys($Language);
			buildTopUndocumentedTags($Language);
			buildTopUsedEditors($Language);

			# build index pages
			buildIndexWatchlist($Language);
			buildIndexGroupList($Language);
			buildIndexRelationList($Language);

			# all key pages with greater details
			buildKeyPages($Language);
			buildRelationPages($Language);
			buildTopUndocumentedRelations($Language);

			#index pages
			buildIndexGeneral($Language);
		}

		printf "\tfinished construction of %s.\n",$Country;
	}

	buildCountryIndex(@tmpl_file_loop);
	buildCountryTopList();

	printf "\tcopy images and css file to output dir.\n",$Country;
	system("cp template/style.css $Config{'output_folder'}");

	my $iconDir = sprintf("%s/images", $Config{'output_folder'});
	mkdir $iconDir if ! -d $iconDir;

	system("cp -v -u template/images/*.* $iconDir");

	#generate sample output request file
	print "\tgenerate sample_request.txt.\n";
	open(SAMPLE_REQUESTS, ">","$Config{'main_folder'}/sample_requests.txt");
	
	foreach my $request(keys %SampleRequest)
	{
		printf SAMPLE_REQUESTS "%s\n", $request;
	}

	close SAMPLE_REQUESTS;
}

#--------------------------------------------------------------------------
# create list with all osm files
#--------------------------------------------------------------------------
sub buildCountryIndex
{
	my (@tmpl_file_loop) = @_;

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/countrylist.tmpl');

	$template->param(indexlist    => \@tmpl_file_loop);
	$template->param(countrycount => $CountryCount);
	$template->param(toplistfile  => $Config{indexname_toplist});

	open(TOP, ">","$Config{'output_folder'}/$Config{'indexname_countries'}");
	print TOP $template->output;
  	close TOP;
}

#--------------------------------------------------------------------------
# compare the .osm fiels with each other and build some statistics
#--------------------------------------------------------------------------
sub buildCountryTopList
{
	my $count = 0;
	my @tmpl_TopNodes;
	# check for total used nodes
	foreach my $CountryName (sort {$Statistics{$b}->{'nodes'} <=> $Statistics{$a}->{'nodes'}} keys %Statistics)
	{
		if( $count >= $Config{'top_countrys'})
		{
			last;
		}
		else
		{
			$count++;
			my %row = ("number"=> $count,
				   "name"  => $CountryName,
		        	   "usage" => $Statistics{$CountryName}->{'nodes'});
			push(@tmpl_TopNodes, \%row);
		}
	}

	$count = 0;
	my @tmpl_TopWays;
	# check for total used ways
	foreach my $CountryName (sort {$Statistics{$b}->{'ways'} <=> $Statistics{$a}->{'ways'}} keys %Statistics)
	{	
		if( $count >= $Config{'top_countrys'})
		{
			last;
		}
		else
		{
			$count++;
			my %row = ("number"=> $count,
				   "name"  => $CountryName,
			           "usage" => $Statistics{$CountryName}->{'ways'});
			push(@tmpl_TopWays, \%row);
		}
	}

	$count = 0;
	my @tmpl_TopRelations;
	# check for total used relations
	foreach my $CountryName (sort {$Statistics{$b}->{'relations'} <=> $Statistics{$a}->{'relations'}} keys %Statistics)
	{	
		if( $count >= $Config{'top_countrys'})
		{
			last;
		}
		else
		{
			$count++;
			my %row = ("number"=> $count,
				   "name"  => $CountryName,
			           "usage" => $Statistics{$CountryName}->{'relations'});
			push(@tmpl_TopRelations, \%row);
		}
	}

	$count = 0;
	my @tmpl_TopUniqueKeys;
	# check for maximum unique keys count
	foreach my $CountryName (sort {$Statistics{$b}->{'keys'} <=> $Statistics{$a}->{'keys'}} keys %Statistics)
	{	
		if( $count >= $Config{'top_countrys'})
		{
			last;
		}
		else
		{
			$count++;
			my %row = ("number"=> $count,
				   "name"  => $CountryName,
			           "usage" => $Statistics{$CountryName}->{'keys'});
			push(@tmpl_TopUniqueKeys, \%row);
		}
	}

	$count = 0;
	my @tmpl_TopUniqueTags;
	# check for maximum unique keys count
	foreach my $CountryName (sort {$Statistics{$b}->{'tags'} <=> $Statistics{$a}->{'tags'}} keys %Statistics)
	{	
		if( $count >= $Config{'top_countrys'})
		{
			last;
		}
		else
		{
			$count++;
			my %row = ("number"=> $count,
				   "name"  => $CountryName,
			           "usage" => $Statistics{$CountryName}->{'tags'});
			push(@tmpl_TopUniqueTags, \%row);
		}
	}

	$count = 0;
	my @tmpl_TopUniqueRelations;
	# check for maximum unique keys count
	foreach my $CountryName (sort {$Statistics{$b}->{'unique_relations'} <=> $Statistics{$a}->{'unique_relations'}} keys %Statistics)
	{	
		if( $count >= $Config{'top_countrys'})
		{
			last;
		}
		else
		{
			$count++;
			my %row = ("number"=> $count,
				   "name"  => $CountryName,
			           "usage" => $Statistics{$CountryName}->{'unique_relations'});
			push(@tmpl_TopUniqueRelations, \%row);
		}
	}
	$count = 0;
	my @tmpl_TopUndocumentedKeys;
	# check for maximum undocumented keys count
	foreach my $CountryName (sort {$Statistics{$b}->{'undocumented_keys'}->{'En'} <=> $Statistics{$a}->{'undocumented_keys'}->{'En'}} keys %Statistics)
	{	
		if( $count >= $Config{'top_countrys'})
		{
			last;
		}
		else
		{
			$count++;
			my %row = ("number"=> $count,
				   "name"  => $CountryName,
			           "usage" => $Statistics{$CountryName}->{'undocumented_keys'}->{'En'});
			push(@tmpl_TopUndocumentedKeys, \%row);
		}
	}

	$count = 0;
	my @tmpl_TopUndocumentedTags;
	# check for maximum undocumented keys count
	foreach my $CountryName (sort {$Statistics{$b}->{'undocumented_tags'}->{'En'} <=> $Statistics{$a}->{'undocumented_tags'}->{'En'}} keys %Statistics)
	{	
		if( $count >= $Config{'top_countrys'})
		{
			last;
		}
		else
		{
			$count++;
			my %row = ("number"=> $count,
				   "name"  => $CountryName,
			           "usage" => $Statistics{$CountryName}->{'undocumented_tags'}->{'En'});
			push(@tmpl_TopUndocumentedTags, \%row);
		}
	}

	$count = 0;
	my @tmpl_TopUndocumentedRelations;
	# check for maximum undocumented relations count
	foreach my $CountryName (sort {$Statistics{$b}->{'relationlist_undocumented_and_used'}->{'En'} <=> $Statistics{$a}->{'relationlist_undocumented_and_used'}->{'En'}} keys %Statistics)
	{	
		if( $count >= $Config{'top_countrys'})
		{
			last;
		}
		else
		{
			$count++;
			my %row = ("number"=> $count,
				   "name"  => $CountryName,
			           "usage" => $Statistics{$CountryName}->{'relationlist_undocumented_and_used'}->{'En'});
			push(@tmpl_TopUndocumentedRelations, \%row);
		}
	}

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/countrytoplist.tmpl');

	$template->param(indexfile                => $Config{indexname_countries});
	$template->param(topnodes                 => \@tmpl_TopNodes);
	$template->param(topways                  => \@tmpl_TopWays);
	$template->param(toprelations             => \@tmpl_TopRelations);
	$template->param(topuniquekeys            => \@tmpl_TopUniqueKeys);
	$template->param(topuniquetags            => \@tmpl_TopUniqueTags);
	$template->param(topuniquerelations       => \@tmpl_TopUniqueRelations);
	$template->param(topundocumentedkeys      => \@tmpl_TopUndocumentedKeys);
	$template->param(topundocumentedtags      => \@tmpl_TopUndocumentedTags);
	$template->param(topundocumentedrelations => \@tmpl_TopUndocumentedRelations);

	my $Language="En";
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_countrytoplist  => sprintf($Interface{'countrytoplist'}->{$Language}, $Country, $Date));
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});

	open(TOP, ">","$Config{'output_folder'}/$Config{indexname_toplist}");
	print TOP $template->output;
  	close TOP;
}

#--------------------------------------------------------------------------
# reads in all Keys and their mostly used Values to build an overview
# page with all found Tags in the specific country.osm
#--------------------------------------------------------------------------
sub buildAllKeyList
{
	my ($Language) = @_;
	my @tmplLoop;
	my %KeyList = GetKeyCount();

	# go through all keys in the .osm file
	foreach my $Key(sort keys %OsmKeyUsage)
	{
		my %TagList = GetTagCount($Key);
		my @ExampleTags;
		my $ValueCount = 0;

		# go through all tags for this particular key
		foreach my $Value(sort {$TagList{$b}->{'t'} <=> $TagList{$a}->{'t'}} keys %TagList)
		{
			my $Text = sprintf("<b><a href=\"%s\" target=\"_blank\">%s</a></b> (%d)", api_link($Key,$Value), markSpaceCharacter($Value), $TagList{$Value}->{'t'}) if($TagList{$Value}->{'t'} > 0);
			if($Value eq '*' && buildAutoIgnoredList($Language, $Key))
			{
				$Text .= sprintf " (<a href=\"ignored_".name_encode($Key)."$Config{html_file_extension}\">$Interface{ignoreentries}->{$Language}</a>)",$Config{max_volatile_count};
			}

			push(@ExampleTags, $Text) if($ValueCount++ < $Config{'example_tags'} && $Text);

			# check for each tag if a wiki description exist
			if($Value ne '*')
			{
				if(!(exists $WikiDescription{'Tag'}->{$Key}->{$Value}->{'description'}->{$Language}) && !(exists $WikiDescription{'Grouped_Keys'}->{"$Key=*"}))
				{
					$UsedUndocumentedTags{$Language}->{"$Key=$Value"} = $TagList{$Value}->{'t'};
					$Statistics{$Country}->{'undocumented_tags'}->{$Language}++;
				}
				elsif(exists $WikiDescription{'Tag'}->{$Key}->{$Value}->{'description'}->{$Language})
				{
					$Statistics{$Country}->{'documented_tags'}->{$Language}++;
				}
				if(exists $WikiDescription{'Tag'}->{$Key}->{$Value}->{'onNode'})
				{
					$Statistics{$Country}->{'mentioned_tags'}->{$Language}++;
				}
			}
		}
		
		my %row = ("key"     => markSpaceCharacter($Key),
			   "keyAPI"  => api_link($Key,"*"),
               		   "values"  => join(", ", @ExampleTags));
		push(@tmplLoop, \%row);

		if(exists $WikiDescription{'Key'}->{$Key}->{'description'}->{$Language})
		{
			$Statistics{$Country}->{'documented_keys'}->{$Language}++;
		}
		else
		{
			# TODO check for more than just single language (stats should
			# check if atleast one description no matter what language is availible
			# and treat it as documented than)
			$Statistics{$Country}->{'undocumented_keys'}->{$Language}++;
			$UsedUndocumentedKeys{$Language}->{$Key} = $KeyList{$Key};
		}
		if(exists $WikiDescription{'Key'}->{$Key}->{'group'})
		{
			$Statistics{$Country}->{'mentioned_keys'}->{$Language}++;
		}
	}

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/tags.tmpl');

	# header informations
	$template->param(country   => $Country);
	$template->param(indexfile => $Config{indexname});

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/tags$Config{html_file_extension}/g;
	$template->param(lang_links => $linklist);

	# add statistical information
	$template->param(taglist  => \@tmplLoop);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_key           => $Interface{'key'}->{$Language});
	$template->param(interlang_examplevalues => $Interface{'examplevalues'}->{$Language});
	$template->param(interlang_allkeys       => sprintf($Interface{'allkeys'}->{$Language},$Statistics{$Country}->{'used_unique_keys'} ,$Country));
	
	open(OUT, ">","$curOutputDir/tags$Config{html_file_extension}");
	print OUT $template->output;
  	close OUT;
}


#--------------------------------------------------------------------------
# builds a page for the auto ignored keys and their partial data set
#--------------------------------------------------------------------------
sub buildAutoIgnoredList
{
	my ($Language, $Key) = @_;
	my @tmplLoop;
	my $Ignored = GetAutoIgnored($Key);

	return if !$Ignored;

	foreach my $Value (sort keys %{$Ignored})
	{
		my %row = ("keyAPI"  => api_link($Key,$Value),
			   "key"     => $Value,
			   "values"  => $Ignored->{$Value});
		push(@tmplLoop, \%row);
	}

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/tags.tmpl');

	# header informations
	$template->param(country   => $Country);
	$template->param(indexfile => $Config{indexname});

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/tags$Config{html_file_extension}/g;
	$template->param(lang_links => $linklist);

	# add statistical information
	$template->param(taglist  => \@tmplLoop);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_key           => $Interface{'ignorekey'}->{$Language});
	$template->param(interlang_examplevalues => $Interface{'ignorecount'}->{$Language});
	$template->param(interlang_allkeys       => sprintf($Interface{'ignorelist'}->{$Language},$Key,$Config{max_volatile_count}));
	
	open(OUT, ">","$curOutputDir/ignored_".name_encode($Key)."$Config{html_file_extension}");
	print OUT $template->output;
	close OUT;
	return 1;
}

#--------------------------------------------------------------------------
# create list with all tags that are not documented in the wiki
#--------------------------------------------------------------------------
sub buildTopUndocumentedKeys
{
	my ($Language) = @_;

	my @tmplLoop;
	my $count = 0;

	# go through the list of all undocumented tags found by the allKeyList routine
	foreach my $Key(sort {$UsedUndocumentedKeys{$Language}->{$b} <=> $UsedUndocumentedKeys{$Language}->{$a}} keys %{$UsedUndocumentedKeys{$Language}})
	{
		my %row = ("tag"   => markSpaceCharacter($Key),
               		   "count" => $UsedUndocumentedKeys{$Language}->{$Key});

		# only list as many tags as mentioned in the config file
		if($count <= $Config{'undocumented_list'})
		{ 
			push(@tmplLoop, \%row);
		}
		$count++;
		$Statistics{$Country}->{'keys_undocumented'}->{$Language}++;
	}

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/top_undocumented.tmpl');

	# header informations
	$template->param(country   => $Country);
	$template->param(indexfile => $Config{indexname});

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/top_undocumented_keys$Config{html_file_extension}/g;
	$template->param(lang_links => $linklist);

	# add statistical information
	$template->param(toplist  => \@tmplLoop);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_tag             => $Interface{'key'}->{$Language});
	$template->param(interlang_usage           => $Interface{'usage'}->{$Language});
	$template->param(interlang_topundocumented => sprintf($Interface{'topundocumented_keys'}->{$Language}, $Config{'undocumented_list'}));

	open(TOP, ">","$curOutputDir/top_undocumented_keys$Config{html_file_extension}");
	print TOP $template->output;
  	close TOP;
}

#--------------------------------------------------------------------------
# create list with all tags that are not documented in the wiki
#--------------------------------------------------------------------------
sub buildTopUndocumentedTags
{
	my ($Language) = @_;

	my @tmplLoop;
	my $count = 0;

	# go through the list of all undocumented tags found by the allKeyList routine
	foreach my $Tag(sort {$UsedUndocumentedTags{$Language}->{$b} <=> $UsedUndocumentedTags{$Language}->{$a}} keys %{$UsedUndocumentedTags{$Language}})
	{
		my %row = ("tag"   => markSpaceCharacter($Tag),
               		   "count" => $UsedUndocumentedTags{$Language}->{$Tag});

		# only list as many tags as mentioned in the config file
		if($count <= $Config{'undocumented_list'})
		{ 
			push(@tmplLoop, \%row);
		}
		$count++;
		$Statistics{$Country}->{'tags_undocumented'}->{$Language}++;
	}

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/top_undocumented.tmpl');

	# header informations
	$template->param(country => $Country);

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/top_undocumented_tags$Config{html_file_extension}/g;
	$template->param(lang_links => $linklist);

	# add statistical information
	$template->param(toplist  => \@tmplLoop);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_tag             => $Interface{'tag'}->{$Language});
	$template->param(interlang_usage           => $Interface{'usage'}->{$Language});
	$template->param(interlang_topundocumented => sprintf($Interface{'topundocumented_tags'}->{$Language},$Config{'undocumented_list'}));
	
	open(TOP, ">","$curOutputDir/top_undocumented_tags$Config{html_file_extension}");
	print TOP $template->output;
  	close TOP;
}

#--------------------------------------------------------------------------
# create list with all tags that are not documented in the wiki
#--------------------------------------------------------------------------
sub buildTopUndocumentedRelations
{
	my ($Language) = @_;

	my @tmplLoop;
	my $count = 0;

	# go through the list of all undocumented tags found by the allKeyList routine
	foreach my $Relation(sort {$UsedUndocumentedRelations{$Language}->{$b} <=> $UsedUndocumentedRelations{$Language}->{$a}} keys %{$UsedUndocumentedRelations{$Language}})
	{
		my %row = ("tag"   => markSpaceCharacter($Relation),
               		   "count" => $UsedUndocumentedRelations{$Language}->{$Relation});

		# only list as many tags as mentioned in the config file
		if($count <= $Config{'undocumented_list'})
		{ 
			push(@tmplLoop, \%row);
		}
		$count++;
	}

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/top_undocumented.tmpl');

	# header informations
	$template->param(country => $Country);

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/top_undocumented_relations$Config{html_file_extension}/g;
	$template->param(lang_links => $linklist);

	# add statistical information
	$template->param(toplist  => \@tmplLoop);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_tag             => $Interface{'relation'}->{$Language});
	$template->param(interlang_usage           => $Interface{'usage'}->{$Language});
	$template->param(interlang_topundocumented => sprintf($Interface{'topundocumented_relations'}->{$Language},$Config{'undocumented_list'}));
	
	open(TOP, ">","$curOutputDir/top_undocumented_relations$Config{html_file_extension}");
	print TOP $template->output;
  	close TOP;
}


#--------------------------------------------------------------------------
# create list with all tags that are not documented in the wiki
#--------------------------------------------------------------------------
sub buildTopUsedEditors
{
	my ($Language) = @_;

	my %editorlist;

	# read in the list og the used editors
	open(EDITORLIST, "<","$curDataDir/editorlist.txt") || return "";
		
	while(my $Line = <EDITORLIST>)
	{
		if($Line =~ m{(\d+) (.*)})
		{
			$editorlist{$2}  = $1;
		}
	}
	close EDITORLIST;

	my @tmplLoop;
	my $count = 0;

	# go through the list
	foreach my $Editor(sort {$editorlist{$b} <=> $editorlist{$a}} keys %editorlist)
	{
		my %row = ("tag"    => $Editor,
			   "tagAPI" => api_link("created_by",$Editor),
               		   "count"  => $editorlist{$Editor});

		# only list as many tags as mentioned in the config file
		if($count <= $Config{'undocumented_list'})
		{ 
			push(@tmplLoop, \%row);
		}
		$count++;

		# build short usage statistics
		if($Editor =~ m{Potlatch (.*)})
		{
			$EditorUsage{'Potlatch'} += $editorlist{$Editor};
		}

		# build short usage statistics
		if($Editor =~ m{JOSM(.*)})
		{
			$EditorUsage{'JOSM'} += $editorlist{$Editor};
		}

		# build short usage statistics
		if($Editor =~ m{Merkaartor (.*)})
		{
			$EditorUsage{'Merkaartor'} += $editorlist{$Editor};
		}

		# build short usage statistics
		if($Editor =~ m{YahooApplet (.*)})
		{
			$EditorUsage{'YahooApplet'} += $editorlist{$Editor};
		}
	}

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/top_editors.tmpl');

	# header informations
	$template->param(country   => $Country);
	$template->param(indexfile => $Config{indexname});

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/top_used_editors$Config{html_file_extension}/g;
	$template->param(lang_links => $linklist);

	# add statistical information
	$template->param(toplist  => \@tmplLoop);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_tag             => $Interface{'editors'}->{$Language});
	$template->param(interlang_usage           => $Interface{'usage'}->{$Language});
	$template->param(interlang_topundocumented => sprintf($Interface{'top_used_editors'}->{$Language},$Config{'undocumented_list'}));
	
	open(TOP, ">","$curOutputDir/top_used_editors$Config{html_file_extension}");
	print TOP $template->output;
  	close TOP;
}

#--------------------------------------------------------------------------
# creates an index page for all Tags that are on the Watchlist
#--------------------------------------------------------------------------
sub buildIndexWatchlist
{
	my ($Language) = @_;
	my @tmpl_loop;

	# go through all keys that are mentioned on the Tagwatch watchlist
	foreach my $WatchedKey (sort keys %WatchList)
	{
		my $keydesc = "";
		# get the description for the Key
		if(exists $WikiDescription{'Key'}->{$WatchedKey}->{'description'}->{$Language})
		{
			$keydesc = $WikiDescription{'Key'}->{$WatchedKey}->{'description'}->{$Language};
		}
		# if no translated description exist take english as default
		elsif(exists $WikiDescription{'Key'}->{$WatchedKey}->{'description'}->{'En'})
		{
			$keydesc = $WikiDescription{'Key'}->{$WatchedKey}->{'description'}->{'En'};
			$Statistics{$Country}->{'watchlist_untranslated'}->{$Language}++;

			if(exists $OsmKeyUsage{$WatchedKey})
			{
				$Statistics{$Country}->{'watchlist_untranslated_and_used'}->{$Language}++;
			}
		}
		else
		{
			$Statistics{$Country}->{'watchlist_undocumented'}->{$Language}++;
			if(exists $OsmKeyUsage{$WatchedKey})
			{
				$Statistics{$Country}->{'watchlist_undocumented_and_used'}->{$Language}++;
			}
		}

		# iterate through all interresting languages for the documentation
		my @tmpl_wikiloop = getWikiDescriptionList($WikiDescription{'Key'}->{$WatchedKey});

		my %row = ("name"     => $WatchedKey,
			   "nameESC"  => name_encode($WatchedKey).$Config{html_file_extension},
			   "desc"     => $keydesc,
			   "desclist" => \@tmpl_wikiloop,
			   "usage"    => $OsmKeyUsage{$WatchedKey},
			   "interlang_stats" => $Interface{'statistics'}->{$Language});
		push(@tmpl_loop, \%row);

		$Statistics{$Country}->{'watchlist_keycount'}->{$Language}++;
	}

	# cosmetical issue ... returns a 0 if nothing exists in the hash
	my $undocumented_tags = 0;
	$undocumented_tags += $Statistics{$Country}->{'watchlist_undocumented'}->{$Language};

	my $undocumented_used_tags = 0;
	$undocumented_used_tags += $Statistics{$Country}->{'watchlist_undocumented_and_used'}->{$Language};
	
	my $untranslated_tags = 0;
	$untranslated_tags += $Statistics{$Country}->{'watchlist_untranslated'}->{$Language};

	my $untranslated_used_tags = 0;
	$untranslated_used_tags += $Statistics{$Country}->{'watchlist_untranslated_and_used'}->{$Language};


	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/watchlist.tmpl');

	# header informations
	$template->param(country   => $Country);
	$template->param(indexfile => $Config{indexname});

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/watchlist$Config{html_file_extension}/g;
	$template->param(lang_links => $linklist);

	# add statistical information		
	$template->param(watchlist    => \@tmpl_loop);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_watchlist      => $Interface{'watchlist'}->{$Language});
	$template->param(interlang_watchlisttext1 => sprintf($Interface{'watchlisttext1'}->{$Language}, $Statistics{$Country}->{'watchlist_keycount'}->{$Language}));
	$template->param(interlang_mflisttext3    => sprintf($Interface{'mflisttext3'}->{$Language}, $undocumented_tags, $undocumented_used_tags));
	$template->param(interlang_mflisttext4    => sprintf($Interface{'mflisttext4'}->{$Language}, $untranslated_tags, $untranslated_used_tags));

	$template->param(interlang_key         => $Interface{'key'}->{$Language});
	$template->param(interlang_desc        => $Interface{'desc'}->{$Language});
	$template->param(interlang_wikidesc    => $Interface{'wikidesc'}->{$Language});
	$template->param(interlang_usage       => $Interface{'usage'}->{$Language});
	$template->param(interlang_details     => $Interface{'details'}->{$Language});

	open(INDEXWL, ">","$curOutputDir/watchlist$Config{html_file_extension}"); 
	print INDEXWL $template->output;
	close INDEXWL;
}

#--------------------------------------------------------------------------
# creates an index page for all Tags that are availible in the Wiki
# Grouped by their Top level group according to the Keypage or Map Feature page.
#--------------------------------------------------------------------------
sub buildIndexGroupList
{
	my ($Language) = @_;
	my @tmpl_loop_outer;

	# go through all Keys sorted by their group
	foreach my $Group(sort keys %{$WikiDescription{'KeyByGroup'}})
	{
		my @Keylist = sort(@{$WikiDescription{'KeyByGroup'}->{$Group}});
		my @tmpl_loop_inner;

		foreach my $KeyName(@Keylist)
		{
			my $keydesc = "";
			# get the description for the Key
			if(exists $WikiDescription{'Key'}->{$KeyName}->{'description'}->{$Language})
			{
				$keydesc = $WikiDescription{'Key'}->{$KeyName}->{'description'}->{$Language};
			}
			# if no translated description exist take english as default
			elsif(exists $WikiDescription{'Key'}->{$KeyName}->{'description'}->{'En'})
			{
				$keydesc = $WikiDescription{'Key'}->{$KeyName}->{'description'}->{'En'};
				$Statistics{$Country}->{'grouplist_untranslated'}->{$Language}++;

				if(exists $OsmKeyUsage{$KeyName})
				{
					$Statistics{$Country}->{'grouplist_untranslated_and_used'}->{$Language}++;
				}
			}
			else
			{
				$Statistics{$Country}->{'grouplist_undocumented'}->{$Language}++;

				if(exists $OsmKeyUsage{$KeyName})
				{
					$Statistics{$Country}->{'grouplist_undocumented_and_used'}->{$Language}++;
				}
			}

			# iterate through all interresting languages for the documentation
			my @tmpl_wikiloop = getWikiDescriptionList($WikiDescription{'Key'}->{$KeyName});			

			my %row_inner = ("name"     => $KeyName,
				         "nameESC"  => name_encode($KeyName).$Config{html_file_extension},
				         "desc"     => $keydesc,
				         "desclist" => \@tmpl_wikiloop,
				         "usage"    => $OsmKeyUsage{$KeyName},
					 "interlang_stats" => $Interface{'statistics'}->{$Language});
			push(@tmpl_loop_inner, \%row_inner);

			$Statistics{$Country}->{'grouplist_keycount'}->{$Language}++;
		}
		
		my %row_outer = ("group"   => $Group,
				 "keylist" => \@tmpl_loop_inner);
		push(@tmpl_loop_outer, \%row_outer);
	}

	# cosmetical issue ... returns a 0 if nothing exists in the hash
	my $undocumented_tags = 0;
	$undocumented_tags += $Statistics{$Country}->{'grouplist_undocumented'}->{$Language};

	my $undocumented_used_tags = 0;
	$undocumented_used_tags += $Statistics{$Country}->{'grouplist_undocumented_and_used'}->{$Language};
	
	my $untranslated_tags = 0;
	$untranslated_tags += $Statistics{$Country}->{'grouplist_untranslated'}->{$Language};

	my $untranslated_used_tags = 0;
	$untranslated_used_tags += $Statistics{$Country}->{'grouplist_untranslated_and_used'}->{$Language};

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/grouplist.tmpl');

	# header informations
	$template->param(country   => $Country);
	$template->param(indexfile => $Config{indexname});

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/grouplist$Config{html_file_extension}/g;
	$template->param(lang_links => $linklist);

	# add statistical information		
	$template->param(grouplist => \@tmpl_loop_outer);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_mflist      => $Interface{'mflist'}->{$Language});
	$template->param(interlang_mflisttext1 => sprintf($Interface{'mflisttext1'}->{$Language}, $Statistics{$Country}->{'grouplist_keycount'}->{$Language}));
	$template->param(interlang_mflisttext3 => sprintf($Interface{'mflisttext3'}->{$Language}, $undocumented_tags, $undocumented_used_tags));
	$template->param(interlang_mflisttext4 => sprintf($Interface{'mflisttext4'}->{$Language}, $untranslated_tags, $untranslated_used_tags));

	$template->param(interlang_key         => $Interface{'key'}->{$Language});
	$template->param(interlang_desc        => $Interface{'desc'}->{$Language});
	$template->param(interlang_wikidesc    => $Interface{'wikidesc'}->{$Language});
	$template->param(interlang_usage       => $Interface{'usage'}->{$Language});
	$template->param(interlang_details     => $Interface{'details'}->{$Language});
	
	open(INDEXWL, ">","$curOutputDir/grouplist$Config{html_file_extension}"); 
	print INDEXWL $template->output;
	close INDEXWL;
}

#--------------------------------------------------------------------------
# creates an index page for all Tags that are on the Watchlist
#--------------------------------------------------------------------------
sub buildIndexRelationList
{
	my ($Language) = @_;
	my @tmpl_loop_outer;

	my %RelationGroupList = ();
	%RelationGroupList = %{$WikiDescription{'RelationByGroup'}};

	# this adds all relations that are not mentioned on the wiki
	# to the group "undocumented" and make it possible
	# to see deeper details of this relations
	# can cause big html files for big .osm files
	if($Config{'full_relation_details'} eq "yes")
	{
		foreach my $RelationName (keys %OsmRelationUsage)
		{
			if(!exists $WikiDescription{'Relation'}->{$RelationName}->{'group'})
			{
				push(@{$RelationGroupList{'undocumented'}}, $RelationName);
			}
		}
	}

	foreach my $GroupName (sort keys %RelationGroupList)
	{
		my @Relationlist = sort(@{$RelationGroupList{$GroupName}});
		my @tmpl_loop_inner;

		foreach my $RelationName(@Relationlist)
		{
			my $RelationDesc = "";
			# get the description for the Relation
			if(exists $WikiDescription{'Relation'}->{$RelationName}->{'description'}->{$Language})
			{
				$RelationDesc = $WikiDescription{'Relation'}->{$RelationName}->{'description'}->{$Language};
				if(exists $OsmRelationUsage{$RelationName})
				{
					$Statistics{$Country}->{'relationlist_documented'}->{$Language}++;
				}
			}
			# if no translated description exist take english as default
			elsif(exists $WikiDescription{'Relation'}->{$RelationName}->{'description'}->{'En'})
			{
				$RelationDesc = $WikiDescription{'Relation'}->{$RelationName}->{'description'}->{'En'};
				$Statistics{$Country}->{'relationlist_untranslated'}->{$Language}++;

				if(exists $OsmRelationUsage{$RelationName})
				{
					$Statistics{$Country}->{'relationlist_untranslated_and_used'}->{$Language}++;
				}
			}
			else
			{
				$Statistics{$Country}->{'relationlist_undocumented'}->{$Language}++;
				if(exists $OsmRelationUsage{$RelationName})
				{
					$Statistics{$Country}->{'relationlist_undocumented_and_used'}->{$Language}++;
					$UsedUndocumentedRelations{$Language}->{$RelationName} = $OsmRelationUsage{$RelationName};
				}
			}

			# iterate through all interresting languages for the documentation
			my @tmpl_wikiloop = getWikiDescriptionList($WikiDescription{'Relation'}->{$RelationName});			

			my %row_inner = ("name"     => markSpaceCharacter($RelationName),
				         "nameESC"  => name_encode($RelationName).$Config{html_file_extension},
				         "desc"     => $RelationDesc,
				         "desclist" => \@tmpl_wikiloop,
				         "usage"    => $OsmRelationUsage{$RelationName},
					 "interlang_stats" => $Interface{'statistics'}->{$Language});
			push(@tmpl_loop_inner, \%row_inner);

			$Statistics{$Country}->{'relationlist_typecount_all'}->{$Language}++;
		}
		
		my %row_outer = ("group"         => $GroupName,
				 "relationslist" => \@tmpl_loop_inner);
		push(@tmpl_loop_outer, \%row_outer);
	}

	# cosmetical issue ... returns a 0 if nothing exists in the hash
	my $undocumented_relations = 0;
	$undocumented_relations += $Statistics{$Country}->{'relationlist_undocumented'}->{$Language};
	
	my $undocumented_used_relations = 0;
	$undocumented_used_relations += $Statistics{$Country}->{'relationlist_undocumented_and_used'}->{$Language};

	my $untranslated_relations = 0;
	$untranslated_relations += $Statistics{$Country}->{'relationlist_untranslated'}->{$Language};

	my $untranslated_used_relations = 0;
	$untranslated_used_relations += $Statistics{$Country}->{'relationlist_untranslated_and_used'}->{$Language};

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/relationslist.tmpl');

	# header informations
	$template->param(country => $Country);
	$template->param(indexfile => $Config{indexname});

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/relationslist$Config{html_file_extension}/g;
	$template->param(lang_links => $linklist);

	$template->param(grouplist    => \@tmpl_loop_outer);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upmain      => $Interface{'upmain'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_relationslist => $Interface{'relationslist'}->{$Language});
	$template->param(interlang_rlisttext1    => sprintf($Interface{'rlisttext1'}->{$Language}, ,$Statistics{$Country}->{'relationlist_typecount_all'}->{$Language}));
	$template->param(interlang_mflisttext3   => sprintf($Interface{'mflisttext3'}->{$Language}, $undocumented_relations, $undocumented_used_relations));
	$template->param(interlang_mflisttext4   => sprintf($Interface{'mflisttext4'}->{$Language}, $untranslated_relations, $untranslated_used_relations));

	$template->param(interlang_type        => $Interface{'type'}->{$Language});
	$template->param(interlang_desc        => $Interface{'desc'}->{$Language});
	$template->param(interlang_wikidesc    => $Interface{'wikidesc'}->{$Language});
	$template->param(interlang_usage       => $Interface{'usage'}->{$Language});
	$template->param(interlang_details     => $Interface{'details'}->{$Language});

	open(INDEXWL, ">","$curOutputDir/relationslist$Config{html_file_extension}"); 
	print INDEXWL $template->output;
	close INDEXWL;
}
#--------------------------------------------------------------------------
# create detailed statistics for every Key that is mentioned on the wiki
# or the watchlist
#--------------------------------------------------------------------------
sub buildRelationPages
{
	my ($Language) = @_;

	# all keys that will be looked into in deeper detail
	my %RelationList = buildWatchedRelationsList();

	# go trough all keys that are watched in greater detail
	foreach my $RelationName (sort keys %RelationList)
	{
		my %RelationCount      = GetRelationCount($RelationName);
		my $CountDiffMembers   = 0; 
		my $CountDiffTags      = 0; 
		my @tmpl_member_loop;
		my @tmpl_tag_loop;

		# go through all members of this relation
		foreach my $Member(sort {$RelationCount{'member'}->{$b} <=> $RelationCount{'member'}->{$a}} keys %{$RelationCount{'member'}})
		{
			$CountDiffMembers++;
			my @MemberInfo = split(/=/, $Member);

			# add member statistics of this relation
			my %row = ("memberrole" => markSpaceCharacter($MemberInfo[1]),
			   	   "membertype" => $MemberInfo[0],
			   	   "memberused" => $RelationCount{'member'}->{$Member});

			push(@tmpl_member_loop, \%row);
		}

		# go through all description tags of this relation
		foreach my $Tag(sort {$RelationCount{'tag'}->{$b} <=> $RelationCount{'tag'}->{$a}} keys %{$RelationCount{'tag'}})
		{
			$CountDiffTags++;
			my @TagInfo = split(/=/, $Tag);

			# iterate through all interresting languages for the documentation
			my @tmpl_wikiloop;
			if(exists $WikiDescription{'Grouped_Keys'}->{'$TagInfo[0]=*'} )
			{
				@tmpl_wikiloop = getWikiDescriptionList($WikiDescription{'Tag'}->{$TagInfo[0]}->{'*'});
			}
			else
			{
				@tmpl_wikiloop = getWikiDescriptionList($WikiDescription{'Tag'}->{$TagInfo[0]}->{$TagInfo[1]});
			}

			my $TagDesc = "";
			# get the description for the Relation
			if(exists $WikiDescription{'Tag'}->{$TagInfo[0]}->{$TagInfo[1]}->{'description'}->{$Language})
			{
				$TagDesc = $WikiDescription{'Tag'}->{$TagInfo[0]}->{$TagInfo[1]}->{'description'}->{$Language};
			}
			# if no translated description exist take english as default
			elsif(exists $WikiDescription{'Tag'}->{$TagInfo[0]}->{$TagInfo[1]}->{'description'}->{'En'})
			{
				$TagDesc = $WikiDescription{'Tag'}->{$TagInfo[0]}->{$TagInfo[1]}->{'description'}->{'En'};
			}
			# add member statistics of this relation
			my %row = ("tagname"        => markSpaceCharacter($Tag),
			   	   "tagdescription" => parseWikiSyntax($TagDesc,$Language),
			   	   "desclist"       => \@tmpl_wikiloop,
			   	   "tagused"        => $RelationCount{'tag'}->{$Tag});

			push(@tmpl_tag_loop, \%row);
		}

		my $RelationDesc = "";
		# get the description for the Relation
		if(exists $WikiDescription{'Relation'}->{$RelationName}->{'description'}->{$Language})
		{
			$RelationDesc = $WikiDescription{'Relation'}->{$RelationName}->{'description'}->{$Language};
		}
		# if no translated description exist take english as default
		elsif(exists $WikiDescription{'Relation'}->{$RelationName}->{'description'}->{'En'})
		{
			$RelationDesc = $WikiDescription{'Relation'}->{$RelationName}->{'description'}->{'En'};
		}

		# create memberlist from the information in the OSMwiki
		my @tmpl_wikimember_loop;
		foreach my $Member(keys %{$WikiDescription{'Relation'}->{$RelationName}->{'member'}})
		{
			my %row = ("icon" => parseWikiSyntax($WikiDescription{'Relation'}->{$RelationName}->{'member'}->{$Member}),
			   	   "name" => $Member);

			push(@tmpl_wikimember_loop, \%row);
		}
	
		#++++++++++++++++++++++++++++
		# fill template
		#++++++++++++++++++++++++++++
		my $template = HTML::Template->new(filename => 'template/relationstats.tmpl');
	
		# header informations
		$template->param(country => $Country);
		$template->param(ext => $Config{html_file_extension});

		# create translation navigationlist
		my $linklist = $lang_links;
		
		my $esclink = name_encode("relationstats_$RelationName$Config{html_file_extension}");
		$linklist  =~ s/%s/$esclink/g;
		$template->param(lang_links => $linklist);

		# general information
		# TODO add image link!!!!!!
		$template->param(relationgroup       => $WikiDescription{'Relation'}->{$RelationName}->{'group'});
		$template->param(wikimembers         => \@tmpl_wikimember_loop);
		$template->param(relationdescription => parseWikiSyntax($RelationDesc, $Language));

		# general statistics
		$template->param(memberlist         => \@tmpl_member_loop);
		$template->param(taglist            => \@tmpl_tag_loop);
		$template->param(type               => $RelationName);
		$template->param(count_members_diff => $CountDiffMembers);
		$template->param(count_tags_diff    => $CountDiffTags);
		$template->param(count_total        => $OsmRelationUsage{$RelationName});

		# parse translated interface sections
		$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
		$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
		$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
		$template->param(interlang_uprelations => $Interface{'uprelations'}->{$Language});
		$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));
	
		$template->param(interlang_stats             => $Interface{'statistics'}->{$Language});
		$template->param(interlang_wikidiscussion    => sprintf($Interface{'wikidiscussion'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Relation:$RelationName\" target=\"_blank\">$RelationName</a>"));
		$template->param(interlang_tablerelationinfo => $Interface{'tablerelationinfo'}->{$Language});
		$template->param(interlang_group             => $Interface{'group'}->{$Language});
		$template->param(interlang_member            => $Interface{'member'}->{$Language});
		$template->param(interlang_generalstats      => $Interface{'generalstats'}->{$Language});

		$template->param(interlang_diffmembers => $Interface{'diffmembers'}->{$Language});
		$template->param(interlang_usedintotal => $Interface{'usedintotal'}->{$Language});
		$template->param(interlang_difftags    => $Interface{'difftags'}->{$Language});

		$template->param(interlang_listofmember => $Interface{'listofmember'}->{$Language});
		$template->param(interlang_listoftags   => $Interface{'listoftags'}->{$Language});
		$template->param(interlang_role         => $Interface{'role'}->{$Language});
		$template->param(interlang_type         => $Interface{'type'}->{$Language});

		$template->param(interlang_wikidesc => $Interface{'wikidesc'}->{$Language});
		$template->param(interlang_tag      => $Interface{'tag'}->{$Language});
		$template->param(interlang_desc     => $Interface{'desc'}->{$Language});
		$template->param(interlang_usage    => $Interface{'usage'}->{$Language});

		my $filename = name_encode($RelationName);
		open(OUT, ">","$curOutputDir/relationstats_".$filename.$Config{html_file_extension});
		print OUT $template->output;
		close OUT;
	}
}

#--------------------------------------------------------------------------
# create detailed statistics for every Key that is mentioned on the wiki
# or the watchlist
#--------------------------------------------------------------------------
sub buildKeyPages
{
	my ($Language) = @_;

	# all keys that will be looked into in deeper detail
	my %KeyList = buildWatchedKeyList();

	# go trough all keys that are watched in greater detail
	foreach my $KeyName (sort keys %KeyList)
	{
		my %TagCount = GetTagCount($KeyName);
		my @tmpl_loop;
		my $CountDiffTags   = 0; 
		my $CountTagUsage_t = 0; 
		my $CountTagUsage_n = 0; 
		my $CountTagUsage_w = 0;
		my $CountTagUsage_r = 0;

		# sort allavilible tags by total usage
		foreach my $Value(sort {$TagCount{$b}->{'t'} <=> $TagCount{$a}->{'t'}} keys %TagCount)
		{
			# iterate through all interresting languages for the documentation
			my @tmpl_wikiloop;
			if(exists $WikiDescription{'Grouped_Keys'}->{"$KeyName=*"} )
			{
				@tmpl_wikiloop = getWikiDescriptionList($WikiDescription{'Tag'}->{$KeyName}->{'*'});
			}
			else
			{
				@tmpl_wikiloop = getWikiDescriptionList($WikiDescription{'Tag'}->{$KeyName}->{$Value});
			}

			# add general statistics of this tag
			my %row = ("value"    => markSpaceCharacter($Value),
			   	   "tagESC"   => name_encode("$KeyName=$Value$Config{html_file_extension}"),
			   	   "tagAPI"   => api_link($KeyName,$Value),
			   	   "total"    => $TagCount{$Value}->{'t'},
			   	   "node"     => $TagCount{$Value}->{'n'},
			   	   "way"      => $TagCount{$Value}->{'w'},
			   	   "relation" => $TagCount{$Value}->{'r'},
			   	   "desclist" => \@tmpl_wikiloop,
				   "interlang_details"     => $Interface{'details'}->{$Language});

			push(@tmpl_loop, \%row);
	
			$CountDiffTags++;
			$CountTagUsage_t += $TagCount{$Value}->{'t'};
			$CountTagUsage_n += $TagCount{$Value}->{'n'};
			$CountTagUsage_w += $TagCount{$Value}->{'w'};
			$CountTagUsage_r += $TagCount{$Value}->{'r'};

			# build the detailed tag page
			buildTagPages($KeyName, $Value, ucfirst($Language));
		}

		# do some percentage calculation
		my $cnp = 0;
		my $cwp = 0;
		my $crp = 0;
		if($CountTagUsage_t > 0)
		{
			$cnp = round((100 / $CountTagUsage_t) * $CountTagUsage_n);
			$cwp = round((100 / $CountTagUsage_t) * $CountTagUsage_w);
			$crp = round((100 / $CountTagUsage_t) * $CountTagUsage_r);
		}

		# get Key description
		my $keydesc;
		if(exists $WikiDescription{'Key'}->{$KeyName}->{'description'}->{$Language})
		{
			$keydesc = $WikiDescription{'Key'}->{$KeyName}->{'description'}->{$Language};
		}
		# if no translated description exist take english as default
		elsif(exists $WikiDescription{'Key'}->{$KeyName}->{'description'}->{'En'})
		{
			$keydesc = $WikiDescription{'Key'}->{$KeyName}->{'description'}->{'En'};
		}

		# check if photo description of this Key exist and copy it to the output folder
		my $ImageLink;
		foreach my $ext (".jpg", ".png")
		{
			if(-e "$Config{cache_folder}/photos/$KeyName$ext")
			{
				if(!(-e "$Config{output_folder}/photos/$KeyName$ext"))
				{
					system "cp -R -u $Config{cache_folder}/photos/$KeyName$ext "
					. "$Config{output_folder}/photos/$KeyName$ext";
				}
				$ImageLink = "<img src=\"../../photos/$KeyName$ext\" alt=\"Key:$KeyName\" border=\"0\">";
			}
		}
		$ImageLink = "-#-" if !$ImageLink;

		#++++++++++++++++++++++++++++
		# fill template
		#++++++++++++++++++++++++++++
		my $template = HTML::Template->new(filename => 'template/keystats.tmpl');
	
		# header informations
		$template->param(country => $Country);
		$template->param(ext => $Config{html_file_extension});

		# create translation navigationlist
		my $linklist = $lang_links;
		my $esclink = name_encode("keystats_$KeyName$Config{html_file_extension}");
		$linklist  =~ s/%s/$esclink/g;
		$template->param(lang_links => $linklist);

		# general information
		$template->param(imagelink      => $ImageLink);
		$template->param(keygroup       => $WikiDescription{'Key'}->{$KeyName}->{'group'});
		$template->param(keyelements    => parseElementIcons($WikiDescription{'Key'}->{$KeyName}));
		$template->param(keydescription => parseWikiSyntax($keydesc, $Language));

		# general statistics
		$template->param(statlist         => \@tmpl_loop);
		$template->param(key              => $KeyName);
		$template->param(count_diff       => $CountDiffTags);
		$template->param(count_total      => $CountTagUsage_t);
		$template->param(count_node       => $CountTagUsage_n);
		$template->param(count_node_p     => $cnp);
		$template->param(count_way        => $CountTagUsage_w);
		$template->param(count_way_p      => $cwp);
		$template->param(count_relation   => $CountTagUsage_r);
		$template->param(count_relation_p => $crp);

		# parse translated interface sections
		$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
		$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
		$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
		$template->param(interlang_upkeylist   => $Interface{'upkeylist'}->{$Language});
		$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));
	
		$template->param(interlang_stats           => $Interface{'statistics'}->{$Language});
		$template->param(interlang_wikidiscussion  => sprintf($Interface{'wikidiscussion'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Key:$KeyName\" target=\"_blank\">$KeyName</a>"));
		$template->param(interlang_tablekeyinfo    => $Interface{'tablekeyinfo'}->{$Language});
		$template->param(interlang_group           => $Interface{'group'}->{$Language});
		$template->param(interlang_elements        => $Interface{'elements'}->{$Language});
		$template->param(interlang_generalstats    => $Interface{'generalstats'}->{$Language});
		$template->param(interlang_diffvalues      => $Interface{'diffvalues'}->{$Language});
		$template->param(interlang_usedintotal     => $Interface{'usedintotal'}->{$Language});
		$template->param(interlang_usedonnodes     => $Interface{'usedonnodes'}->{$Language});
		$template->param(interlang_usedonways      => $Interface{'usedonways'}->{$Language});
		$template->param(interlang_usedinrelations => $Interface{'usedinrelations'}->{$Language});
		$template->param(interlang_value           => $Interface{'value'}->{$Language});

		$template->param(interlang_wikidesc    => $Interface{'wikidesc'}->{$Language});
		$template->param(interlang_usage       => $Interface{'usage'}->{$Language});
		$template->param(interlang_details     => $Interface{'details'}->{$Language});

		my $filename = name_encode($KeyName);
		open(OUT, ">","$curOutputDir/keystats_$filename$Config{html_file_extension}");
		print OUT $template->output;
		close OUT;
	}
}

#--------------------------------------------------------------------------
# create statistical page for the tags combinations.
#--------------------------------------------------------------------------
sub buildTagPages
{
	my($KeyName, $Value, $Language) = @_;

	my $Combis = GetCombinations("$KeyName=$Value");
	my %TagCount = GetTagCount($KeyName);
  	my @tmpl_loop;
	my $CountDiffValues;

	# go through all known combinations of this tag
	foreach my $Comb(sort {$Combis->{$b} <=> $Combis->{$a}} keys %{$Combis})
	{
		my %row = ("combi_tag" => markSpaceCharacter($Comb),
                           "tag_count" => $Combis->{$Comb});

		push(@tmpl_loop, \%row);

		$CountDiffValues++;
	}

	# get tag description
	my $tagdesc;
	if(exists $WikiDescription{'Tag'}->{$KeyName}->{$Value}->{'description'}->{$Language})
	{
		$tagdesc = $WikiDescription{'Tag'}->{$KeyName}->{$Value}->{'description'}->{$Language};
	}
	# if no translated description exist take english as default
	elsif(exists $WikiDescription{'Tag'}->{$KeyName}->{$Value}->{'description'}->{'En'})
	{
		$tagdesc = $WikiDescription{'Tag'}->{$KeyName}->{$Value}->{'description'}->{'En'};
	}

	# cosmetical issue ... returns a 0 if nothing exists in the hash
	my $CountTagUsage_t = 0;
	my $CountTagUsage_n = 0;
	my $CountTagUsage_w = 0;
	my $CountTagUsage_r = 0;
	$CountTagUsage_t += $TagCount{$Value}->{'t'};
	$CountTagUsage_n += $TagCount{$Value}->{'n'};
	$CountTagUsage_w += $TagCount{$Value}->{'w'};
	$CountTagUsage_r += $TagCount{$Value}->{'r'};

	# do some percentage calculation
	my $cnp = 0;
	my $cwp = 0;
	my $crp = 0;
	if($CountTagUsage_t > 0)
	{
		$cnp = round((100 / $CountTagUsage_t) * $CountTagUsage_n);
		$cwp = round((100 / $CountTagUsage_t) * $CountTagUsage_w);
		$crp = round((100 / $CountTagUsage_t) * $CountTagUsage_r);
	}
	
	# check if photo description of this Key exist and copy it to the output folder
	my $ImageLink;
	if(-e $Config{'cache_folder'}."/photos/".$KeyName."_".$Value.".jpg")
	{
		if(!(-e $Config{'output_folder'}."/photos/".$KeyName."_".$Value.".jpg"))
		{
			my $cmd = "cp -R -u ".$Config{'cache_folder'}."/photos/".$KeyName."_".$Value.".jpg ".$Config{'output_folder'}."/photos/".$KeyName."_".$Value.".jpg";
			system ($cmd);
		}
		$ImageLink = "<img src=\"../../photos/".$KeyName."_".$Value.".jpg\" alt=\"Tag:$KeyName=$Value\" border=\"0\">";
	}
	else
	{
		$ImageLink = "-#-";
	}

	#++++++++++++++++++++++++++++
	# fill templatetagstatistics
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/tagstats.tmpl');
	
	# header informations
	$template->param(country => $Country);

	# create translation navigationlist
	my $linklist = $lang_links;
	my $esclink = name_encode("tagstats_$KeyName=$Value$Config{html_file_extension}");
	$linklist  =~ s/%s/$esclink/g;
	$template->param(lang_links => $linklist);

	# general information
	$template->param(imagelink      => $ImageLink);
	$template->param(taggroup       => $WikiDescription{'Key'}->{$KeyName}->{'group'});
	$template->param(tagelements    => parseElementIcons($WikiDescription{'Tag'}->{$KeyName}->{$Value}));
	$template->param(tagdescription => parseWikiSyntax($tagdesc, $Language));
	$template->param(osmr_link      => getOSMRlink($KeyName, $Value));

	# general statistics
	$template->param(combilist        => \@tmpl_loop);
	$template->param(tag              => "$KeyName=$Value");
	$template->param(keyESC           => name_encode($KeyName).$Config{html_file_extension});
	$template->param(count_diff       => $CountDiffValues);
	$template->param(count_total      => $CountTagUsage_t);
	$template->param(count_node       => $CountTagUsage_n);
	$template->param(count_node_p     => $cnp);
	$template->param(count_way        => $CountTagUsage_w);
	$template->param(count_way_p      => $cwp);
	$template->param(count_relation   => $CountTagUsage_r);
	$template->param(count_relation_p => $crp);
	
	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_uptag       => $Interface{'uptag'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));
	
	$template->param(interlang_stats           => $Interface{'statistics'}->{$Language});
	$template->param(interlang_wikidiscussion  => sprintf($Interface{'wikidiscussion'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tag:$KeyName=$Value\" target=\"_blank\">$KeyName=$Value</a>"));
	$template->param(interlang_tabletaginfo    => $Interface{'tablekeyinfo'}->{$Language});
	$template->param(interlang_group           => $Interface{'group'}->{$Language});
	$template->param(interlang_elements        => $Interface{'elements'}->{$Language});
	$template->param(interlang_generalstats    => $Interface{'generalstats'}->{$Language});
	$template->param(interlang_diffvalues      => $Interface{'diffvalues'}->{$Language});
	$template->param(interlang_usedintotal     => $Interface{'usedintotal'}->{$Language});
	$template->param(interlang_usedonnodes     => $Interface{'usedonnodes'}->{$Language});
	$template->param(interlang_usedonways      => $Interface{'usedonways'}->{$Language});
	$template->param(interlang_usedinrelations => $Interface{'usedinrelations'}->{$Language});
	$template->param(interlang_tagcombi        => sprintf($Interface{'tagcombi'}->{$Language}, "<b>$KeyName=$Value</b>"));
	$template->param(interlang_osmrexampleheader => $Interface{'osmrexampleheader'}->{$Language});

	$template->param(interlang_usage       => $Interface{'usage'}->{$Language});
	$template->param(interlang_othertags   => $Interface{'othertags'}->{$Language});

	my $filename = name_encode("$KeyName=$Value");
	open(TAGCOMBI, ">","$curOutputDir/tagstats_$filename$Config{html_file_extension}");
	print TAGCOMBI $template->output;
  	close TAGCOMBI;
}

#--------------------------------------------------------------------------
# create the main index page
#--------------------------------------------------------------------------
sub buildIndexGeneral
{
	my ($Language) = @_;

	# get some general stats
	open(STATS, "<","$curDataDir/stats.txt") || return;
	while(my $Line = <STATS> )
	{
		if($Line =~ m{(\d+) (.*)})
		{
			$Statistics{$Country}-> {$2} = $1 if($1 && $2);
		}
	}
	close STATS;

	my @tmplLoop;
	my $count = 0;

	# go through the list
	foreach my $Editor(sort {$EditorUsage{$b} <=> $EditorUsage{$a}} keys %EditorUsage)
	{
		my %row = ("editor_name" => $Editor,
               		   "editor_usage"  => $EditorUsage{$Editor});
		push(@tmplLoop, \%row);
	}

	#++++++++++++++++++++++++++++
	# fill template
	#++++++++++++++++++++++++++++
	my $template = HTML::Template->new(filename => 'template/index.tmpl');
	
	# header informations
	$template->param(country => $Country);
	$template->param(language => $Language);
	$template->param(mainindexfile => $Config{indexname_countries});
	$template->param(ext => $Config{html_file_extension});

	# create translation navigationlist
	my $linklist = $lang_links;
	$linklist  =~ s/%s/$Config{indexname}/g;
	$template->param(lang_links => $linklist);

	# general statistics
	$template->param(all_keys             => $Statistics{$Country}->{'keys'});
	$template->param(mentioned_keys       => $Statistics{$Country}->{'mentioned_keys'}->{$Language});
	$template->param(en_documented_keys   => $Statistics{$Country}->{'documented_keys'}->{'En'});
	$template->param(lang_documented_keys => $Statistics{$Country}->{'documented_keys'}->{$Language});

	$template->param(all_tags             => $Statistics{$Country}->{'tags'});
	$template->param(mentioned_tags       => $Statistics{$Country}->{'mentioned_tags'}->{$Language});
	$template->param(en_documented_tags   => $Statistics{$Country}->{'documented_tags'}->{'En'});
	$template->param(lang_documented_tags => $Statistics{$Country}->{'documented_tags'}->{$Language});

	$template->param(unique_relations          => $Statistics{$Country}->{'unique_relations'});
	$template->param(mentioned_relations       => $Statistics{$Country}->{'unique_relations'} - $Statistics{$Country}->{'relationlist_undocumented'}->{'En'});
	$template->param(en_documented_relations   => $Statistics{$Country}->{'relationlist_documented'}->{'En'});
	$template->param(lang_documented_relations => $Statistics{$Country}->{'relationlist_documented'}->{$Language});

	$template->param(all_nodes            => $Statistics{$Country}->{'nodes'});
	$template->param(all_ways             => $Statistics{$Country}->{'ways'});
	$template->param(all_relations        => $Statistics{$Country}->{'relations'});

	#editor stats
	$template->param(editorlist             => \@tmplLoop);

	# parse translated interface sections
	$template->param(interlang_headertitle => sprintf($Interface{'headertitle'}->{$Language}, $Country));
	$template->param(interlang_headertext  => sprintf($Interface{'headertext'}->{$Language}, $Country, $Date));
	$template->param(interlang_interlang   => $Interface{'interlang'}->{$Language});
	$template->param(interlang_upover      => $Interface{'upover'}->{$Language});
	$template->param(interlang_credits     => sprintf($Interface{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	$template->param(interlang_tagstatistics              => $Interface{'tagstatistics'}->{$Language});
	$template->param(interlang_indextags                  => $Interface{'indextags'}->{$Language});
	$template->param(interlang_mflist                     => $Interface{'mflist'}->{$Language});
	$template->param(interlang_watchlist                  => $Interface{'watchlist'}->{$Language});
	$template->param(interlang_relationslist              => $Interface{'relationslist'}->{$Language});
	$template->param(interlang_top_undocumented_tags      => sprintf($Interface{'topundocumented_tags'}->{$Language}, $Config{'undocumented_list'}));
	$template->param(interlang_top_undocumented_keys      => sprintf($Interface{'topundocumented_keys'}->{$Language}, $Config{'undocumented_list'}));
	$template->param(interlang_top_undocumented_relations => sprintf($Interface{'topundocumented_relations'}->{$Language}, $Config{'undocumented_list'}));

	$template->param(interlang_generalstats  => $Interface{'generalstats'}->{$Language});
	$template->param(interlang_generalstats1 => sprintf($Interface{'generalstats1'}->{$Language}, $Country));
	$template->param(interlang_generalstats2 => sprintf($Interface{'generalstats2'}->{$Language}, $Statistics{$Country}->{'user'}));

	$template->param(interlang_editorstats  => $Interface{'editorstats'}->{$Language});
	$template->param(interlang_editorstats1 => sprintf($Interface{'editorstats1'}->{$Language},"<a href=\"top_used_editors$Config{html_file_extension}\">$Interface{editorstats2}->{$Language}</a>"));
	$template->param(interlang_editors      => $Interface{'editors'}->{$Language});
	$template->param(interlang_usage        => $Interface{'usage'}->{$Language});

	$template->param(interlang_elements    => $Interface{'elements'}->{$Language});
	$template->param(interlang_key         => $Interface{'key'}->{$Language});
	$template->param(interlang_desc        => $Interface{'desc'}->{$Language});
	$template->param(interlang_trans       => $Interface{'trans'}->{$Language});
	$template->param(interlang_mentioned   => $Interface{'mentioned'}->{$Language});

	open(INDEXGENERAL, ">","$curOutputDir/$Config{indexname}"); 
	print INDEXGENERAL $template->output;
	close INDEXGENERAL;
}

#--------------------------------------------------------------------------
#--------------------------------------------------------------------------
# Helper functions
#--------------------------------------------------------------------------
#--------------------------------------------------------------------------

#--------------------------------------------------------------------------
# reads all caches wiki data into a hash array
#--------------------------------------------------------------------------
sub loadWikiInformation
{
	my ($CacheFolder) = @_;

	my $GroupName = "";

	open(WIKIGROUP, "<","$CacheFolder/wiki_desc/group_list.txt") || die("Could not find group_list for all keys.");

	# go through the conmplete group list to get the Key/Tag informations
	while(my $GroupLine = <WIKIGROUP>)
	{
		$GroupLine  =~ s/\n//g;

		# found groupname
		if($GroupLine =~ m{^\*\s(.*)})
		{
			$GroupName = $1;
		}
		# found keyname
		elsif($GroupLine =~ m{^\*\*\s(.*)})
		{
			my $Key = $1;

			#add key to grouplist
			push(@{$WikiDescription{'KeyByGroup'}->{$GroupName}},$Key);
			
			open(WIKIKEY, "<","$CacheFolder/wiki_desc/Key:$Key.txt") || next;

			# parse all Key informations
			while(my $KeyLine = <WIKIKEY>)
			{
				$KeyLine  =~ s/\n//g;

				# parse key description
				if($KeyLine =~ m{^(.*):desc=(.*)})
				{
					$WikiDescription{'Key'}->{$Key}->{'description'}->{ucfirst($1)} = $2;
				}
				# parse availible tags
				elsif($KeyLine =~ m{^$Key\s=\s(.*)\s*$})
				{
					my $TagValue = $1;
	
					open(WIKITAG, "<","$CacheFolder/wiki_desc/Tag:$Key=$TagValue.txt") || next;

					# parse all tag informations
					while(my $TagLine = <WIKITAG>)
					{
						$TagLine  =~ s/\n//g;
						# parse tag description
						if($TagLine =~ m{(.*):desc=(.*)\s*})
						{
							$WikiDescription{'Tag'}->{$Key}->{$TagValue}->{'description'}->{ucfirst($1)} = $2;
						}
						# parse all other Tag informations
						elsif($TagLine =~ m{(.*)=(.*)})
						{
							$WikiDescription{'Tag'}->{$Key}->{$TagValue}->{$1} = $2;
						}
					}
					close WIKITAG;
				}
				# parse all other Key informations
				elsif($KeyLine =~ m{^(.*)=(.*)\s*$})
				{
					$WikiDescription{'Key'}->{$Key}->{$1} = $2;
				}
			}
			close WIKIKEY;
		}
	}
	close WIKIGROUP;

	#Go through the Relations list to get all relation informations
	open(RELATIONLIST, "<","$CacheFolder/wiki_desc/relation_list.txt") || die("Could not find relation_list for all relations.");

	# go through the conmplete group list to get the Key/Tag informations
	while(my $RelationListLine = <RELATIONLIST>)
	{
		$RelationListLine  =~ s/\n//g;

		# open relation description file
		open(RELATIONDESC, "<","$CacheFolder/wiki_desc/Relation:$RelationListLine.txt") || next;

		# parse all relation informations
		while(my $RelationLine = <RELATIONDESC>)
		{
			$RelationLine  =~ s/\n//g;

			# parse relation description
			if($RelationLine =~ m{^(\w*):desc=(.*)\s*$})
			{
				$WikiDescription{'Relation'}->{$RelationListLine}->{'description'}->{ucfirst($1)} = $2;
			}
			# parse all member informations
			elsif($RelationLine =~ m{^member=(.*)\s-\s(.*)$})
			{
				$WikiDescription{'Relation'}->{$RelationListLine}->{'member'}->{$2} = $1;
			}
			# parse all other Tag informations
			elsif($RelationLine =~ m{^(.*)=(.*)\s*$})
			{
				$WikiDescription{'Relation'}->{$RelationListLine}->{$1} = $2;

				if($1 eq 'group')
				{
					#add relation to grouplist
					push(@{$WikiDescription{'RelationByGroup'}->{$WikiDescription{'Relation'}->{$RelationListLine}->{'group'}}},$RelationListLine);
				}
			}
		}
		close RELATIONDESC;
	}
	close RELATIONLIST;

	# Go through tthe file with all keys where the values will be ignored for the description
	# these are tags with numbers/names etc as values
	open(GROUPEDKEYLIST, "<","$CacheFolder/wiki_desc/grouped_keys.txt") || next;

	# go through the conmplete group list to get the Key/Tag informations
	while(my $Line = <GROUPEDKEYLIST>)
	{
		$Line  =~ s/\n//g;
		if($Line =~ m{^(\w*)=\*$})
		{
			$WikiDescription{'Grouped_Keys'}->{"$1=*"} = 1;
		}
	}
	close GROUPEDKEYLIST;

	return %WikiDescription;
}

#--------------------------------------------------------------------------
# read in the list of all .osm files
#--------------------------------------------------------------------------
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

#--------------------------------------------------------------------------
# reads all used keys and their usage count
#--------------------------------------------------------------------------
sub LoadKeyUsage
{
	my ($curDataDir) = @_;

	open(IN, "<","$curDataDir/tags.txt") || return;
	my %Keys;
	while(my $Line = <IN>)
	{
		if($Line =~ m{(\d+) (.*)})
		{
			$Keys{$2} = $1 if($1 && $2);
			$Statistics{$Country}->{'used_unique_keys'}++;
		}
	}
	close IN;

	return %Keys;
}

#--------------------------------------------------------------------------
# reads all used relations and their usage count
#--------------------------------------------------------------------------
sub LoadRelationUsage
{
	my ($curDataDir) = @_;

	open(IN, "<","$curDataDir/relations.txt") || return;
	my %Relations;
	while(my $Line = <IN>)
	{
		if($Line =~ m{(\d+) (.*)})
		{
			$Relations{$2} = $1 if($1 && $2);
			$Statistics{$Country}->{'used_unique_relations'}++;
		}
	}
	close IN;

	return %Relations;
}

#--------------------------------------------------------------------------
# get information about how often each key was used
#--------------------------------------------------------------------------
sub GetKeyCount
{
	open(IN, "<","$curDataDir/keylist.txt") || return;

	my %KeyList;
	while(my $Line = <IN>)
	{
		if($Line =~ m{(\d+) (.*)})
		{
			$KeyList{$2} = $1 if($1 && $2);
		}
	}
	close IN;
	return %KeyList;
}

#--------------------------------------------------------------------------
# get information about how often each tag was used on ways/areas/nodes
#--------------------------------------------------------------------------
sub GetTagCount
{
	my ($Key) = @_;
	open(IN, "<","$curDataDir/tag_$Key.txt") || return;

	my %TagList;
	while(my $Line = <IN>)
	{
		if($Line =~ m{(\d+) (\d+) (\d+) (\d+) (.*)})
		{
			$TagList{$5}->{'t'} = $1 if($1 && $5);
			$TagList{$5}->{'n'} = $2 if($2 && $5);
			$TagList{$5}->{'w'} = $3 if($3 && $5);
			$TagList{$5}->{'r'} = $4 if($4 && $5);
		}
	}
	close IN;
	return %TagList;
}

#--------------------------------------------------------------------------
# get information about how often each tag was used on ways/areas/nodes
#--------------------------------------------------------------------------
sub GetRelationCount
{
	my ($Type) = @_;
	open(IN, "<","$curDataDir/relation_$Type.txt") || return;

	my %RelationList;
	while(my $Line = <IN>)
	{
		if($Line =~ m{(\w+) (\d+) (.*)})
		{
			$RelationList{$1}->{$3} = $2 if($1 && $3);
		}
	}
	close IN;
	return %RelationList;
}


#--------------------------------------------------------------------------
# reads in all used combination for this Tag
#--------------------------------------------------------------------------
sub GetCombinations
{
	my ($Tag) = @_;
	open(IN, "<","$curDataDir/combi_$Tag.txt") || return;
	my %Combis;
	while(my $Line = <IN> )
	{
		if($Line =~ m{(\d+) (.*)})
		{
			$Combis{$2} = $1 if($1 && $2);
		}
	}
	close IN;
	return \%Combis;
}

#--------------------------------------------------------------------------
# reads in the ignored header for this key
#--------------------------------------------------------------------------
sub GetAutoIgnored
{
	my ($Key) = @_;
	open(IN, "<","$curDataDir/ignored_$Key.txt") || return;
	my %Ignored;
	while(my $Line = <IN> )
	{
		if($Line =~ m{(\d+) (.*)})
		{
			$Ignored{$2} = $1 if($1 && $2);
		}
	}
	close IN;
	return \%Ignored;
}

#--------------------------------------------------------------------------
# Create a list of all tags that should be looked into in deeper detail
#--------------------------------------------------------------------------
sub buildWatchedKeyList
{
	my %WatchedKeys = getWatchedKeys("$Config{'cache_folder'}/wiki_settings");

	# read in all keys that have a description on the wiki
	open(KEYLIST, "<","$Config{'cache_folder'}/wiki_desc/key_list.txt") || return "";
	
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

#--------------------------------------------------------------------------
# Create a list of all relations that should be looked into in deeper detail
#--------------------------------------------------------------------------
sub buildWatchedRelationsList
{
	my %WatchedKeys;

	# this adds all relations that are used in the .osm file
	# can cause big html files for big .osm files
	if($Config{'full_relation_details'} eq "yes")
	{
		foreach my $RelationName (sort keys %OsmRelationUsage)
		{
			$WatchedKeys{$RelationName}  = 1;

		}
	}
	# read in all keys that have a description on the wiki
	open(KEYLIST, "<","$Config{'cache_folder'}/wiki_desc/relation_list.txt") || return "";
		
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

#--------------------------------------------------------------------------
# Check the element for all availible wiki descriptions
# returns a hash array for the template inclusion
#--------------------------------------------------------------------------
sub getWikiDescriptionList
{
	my ($Element) = @_;

	#all languages that this site will be translated into (for tag description details)
	my @Languages = split(/,/, $Config{'languages_wikidesc'});

	# iterate through all interresting languages for the documentation
	my @wikiloop;			
	foreach my $Language(@Languages)
	{
		my %row_wiki;
		if(exists $Element->{'description'}->{ucfirst($Language)} && $Element->{'description'}->{ucfirst($Language)} ne ""  && $Element->{'description'}->{ucfirst($Language)} ne " ")
		{
			%row_wiki = (wikidesc => "wiki_green",
		 		     language => ucfirst($Language));
		}
		# check if maxbe the description exist in the grouped version keyname=*
		
		else
		{
			%row_wiki = (wikidesc => "wiki_red",
		 		     language => ucfirst($Language));
		}
		push(@wikiloop, \%row_wiki);
	}

	return @wikiloop;
}

#--------------------------------------------------------------------------
# Check the tag for all availible wiki descriptions
#--------------------------------------------------------------------------
sub checkIfWikiTagDescriptionExist
{
	my ($KeyName, $Value) = @_;

	#all languages that this site will be translated into (for tag description details)
	my @Languages = split(/,/, $Config{'languages_wikidesc'});
		
	foreach my $Language(@Languages)
	{
		if(exists $WikiDescription{'Tag'}->{$KeyName}->{$Value}->{'description'}->{ucfirst($Language)} && $WikiDescription{'Tag'}->{$KeyName}->{$Value}->{'description'}->{ucfirst($Language)} ne ""  && $WikiDescription{'Tag'}->{$KeyName}->{$Value}->{'description'}->{ucfirst($Language)} ne " ")
		{
			return 1;
		}
		# check if maxbe the description exist in the grouped version keyname=*
		elsif(exists $WikiDescription{'Tag'}->{$KeyName}->{'*'}->{'description'}->{ucfirst($Language)} && $WikiDescription{'Tag'}->{$KeyName}->{'*'}->{'description'}->{ucfirst($Language)} ne ""  && $WikiDescription{'Tag'}->{$KeyName}->{'*'}->{'description'}->{ucfirst($Language)} ne " ")
		{
			return 1;
		}
	}

	return 0;
}


#--------------------------------------------------------------------------
# parse wiki descriptions
# change templates / wikilinks / wikihtml code to normal html code
#--------------------------------------------------------------------------
sub parseWikiSyntax
{
	my ($Text, $Language) = @_;

	my $all_replaced = 0;
	while($all_replaced != 1)
	{
		$all_replaced = 1;

		#replace {{Tag|<key>|<value>}}
		$all_replaced = 0 if $Text =~ s/\{\{Tag\|(.*?)\|(.*?)\}\}/<span class=\"tagtemplate\"><a href=\"..\/$Language\/keystats_$1$Config{html_file_extension}\">$1<\/a>=<a href=\"..\/$Language\/tagstats_$1=$2$Config{html_file_extension}\">$2<\/a><\/span>/g;

		#replace {{Tag|<key>||<value>}}
		$all_replaced = 0 if $Text =~ s/\{\{Tag\|(.*?)\|\|(.*?)\}\}/<span class=\"tagtemplate\"><a href=\"..\/$Language\/keystats_$1$Config{html_file_extension}\">$1<\/a>=$2<\/span>/g;

		#replace {{Tag|<key>}}
		$all_replaced = 0 if $Text =~ s/\{\{Tag\|(.*?)\}\}/<span class=\"tagtemplate\"><a href=\"..\/$Language\/keystats_$1$Config{html_file_extension}\">$1<\/a>=*<\/span>/g;

		#replace [[wikilink|description]]
		$all_replaced = 0 if $Text =~ s/\[\[(.*?)\|(.*?)\]\]/<a href=\"http:\/\/wiki.openstreetmap.org\/index.php\/$1\">$2<\/a>/g;

		#replace [http .... ]
		$all_replaced = 0 if $Text =~ s/\[{1}(\S*)\s{1}(.*)\]{1}/<a href=\"$1\">$2<\/a>/g;

		# replace icons for ways
		$all_replaced = 0 if $Text =~ s/\{\{Icon(.*?)\}\}/"<IMG src=\"..\/..\/images\/Mf_".lc($1).".png\" width=\"20\" height=\"20\" border=\"0\">"/ge;
	}

	return $Text;
}

#--------------------------------------------------------------------------
# parse elemet usage (node/way/icon)
# replace yes/no values with icons
#--------------------------------------------------------------------------
sub parseElementIcons
{
	my ($Tag) = @_;

	my $images = "&nbsp;";
	if($Tag->{'onNode'} eq "yes")
	{
		$images = "$images<IMG src=\"..\/..\/images\/Mf_node.png\" width=\"20\" height=\"20\" border=\"0\">&nbsp;";
	}

	if($Tag->{'onWay'} eq "yes")
	{
		$images = "$images<IMG src=\"..\/..\/images\/Mf_way.png\" width=\"20\" height=\"20\" border=\"0\">&nbsp;";
	}

	if($Tag->{'onArea'} eq "yes")
	{
		$images = "$images<IMG src=\"..\/..\/images\/Mf_area.png\" width=\"20\" height=\"20\" border=\"0\">&nbsp;";
	}

	return $images;
} 

#--------------------------------------------------------------------------
# marks space charakter in a key/tag/relation name
# when it starts or end with one. output will be a red block to indicate
# that this is a mistake
#--------------------------------------------------------------------------
sub markSpaceCharacter
{
	my ($Name) = @_;

	$Name=~s/^\s+/<span class="mark_space">&nbsp;<\/span>/;
	$Name=~s/\s+=/<span class="mark_space">&nbsp;<\/span>=/;
	$Name=~s/\s+$/<span class="mark_space">&nbsp;<\/span>/;

	return $Name;

}
#--------------------------------------------------------------------------
# get osmarender image linklist
# sets the tag on the queue for the samples generation and
# returns a link to the image
# creates samples only for documented feature ... because
# otherwise we end up with way to many samples.
#--------------------------------------------------------------------------
sub getOSMRlink
{
	my ($Key, $Value) = @_;

	if(checkIfWikiTagDescriptionExist($Key, $Value))
	{
		$SampleRequest{"$Key=$Value"} = 1;

		return sprintf("../../samples/%s_%s.png", $Key, $Value);
	}

	return 0;
}

#--------------------------------------------------------------------------
# clear the filename, so no dangerous things may happen
#--------------------------------------------------------------------------
sub name_encode
{
	my $name = shift;
	$name =~ s/[^a-zA-Z0-9._-]/_/g;
	return $name;
}

sub api_link
{
	my ($key, $value) = @_;
	$key =~ s/([^A-Za-z0-9*_-])/sprintf("%%%02X", ord($1))/seg;
	$value =~ s/([^A-Za-z0-9*_-])/sprintf("%%%02X", ord($1))/seg;
	return $Config{osmxapi_url} . "*[$key=$value]";
}

1;
