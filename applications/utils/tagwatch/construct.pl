#-----------------------------------------------------------------
# Creates pages describing the tagging schemes in use within 
# OpenStreetmap
#-----------------------------------------------------------------
# Usage: perl construct.pl
# Will create an ./html/ directory and fill it with HTML files
# Uses input from http://wiki.openstreetmap.org/index.php/Tagwatch/*
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
use MediaWiki;
use strict;
use LWP::Simple;
use GD;
use HTML::Template;
use Math::Round;
use process;


print "read config ...\n";
my %Config = ReadConfig("tagwatch.conf");

my @Languages = split(/,/, $Config{'languages'});

my $Dir = "";
my $DataDir = "";
my $PDir = "html/photos";
my $CacheDir = "original_photos";
mkdir $PDir if ! -d $PDir;
mkdir $CacheDir if ! -d $CacheDir;

my $c = MediaWiki->new;
$c->setup({'wiki' => {
	   'host' => 'wiki.openstreetmap.org',
	   'path' => ''}});
my $wp = MediaWiki->new;
$wp->setup({'wiki' => {
	    'host' => 'en.wikipedia.org',
	    'path' => 'w'}});

# grab interface translation information
print "get interface translation ...\n";
my $Interface = LoadInterface();
	
# grab all pages in the Keys and tags category
my($key_articles, $key_subcats) = $c->readcat("Keys");
my($tag_articles, $tag_subcats) = $c->readcat("Tags");

#parse map feature templates to get approved pages
my @approvedKeySections = (
"abutters",
"accessories",
"aerialway",
"aeroway",
"amenity",
"annotation",
"boundary",
"bridge",
"cycleway",
"editor_keys"
"highway",
"historic",
"landuse",
"leisure",
"man_made",
"military",
"name",
"natural",
"place",
"power",
"properties",
"railway",
"references",
"restrictions",
"route",
"shop",
"sport",
"tourism",
"tracktype",
"waterway",
);
#print "get list of \"approved\" tags...\n";
#my $Approved_tags = GetApprovedTags();
	
#Fallback to get the description from the Tagwatch subpage if no Tag:Key=Value page exist.
my $DescriptionsFallback;
foreach my $Lang(@Languages){
	$DescriptionsFallback->{$Lang} = GetDescriptionsFallback($Lang);
}
	
my @watchedKeys;

my $country = "";
my $date = "";
my $osmfile = "";
my @tmpl2_loop;

my %SampleRequest;

foreach my $Line(split(/\n/, $c->text("Tagwatch/Watchlist"))){
	if($Line =~ m{\* (\w+)}){ 
		push(@watchedKeys, $1); }
}
print "start processing files ...\n";
for (my $count = 1; $count <= $Config{'files'}; $count++) {

	my $conf_country = sprintf("country_%s", $count);
	$country = $Config{$conf_country};
	my $conf_date = sprintf("date_%s", $count);
	$date = $Config{$conf_date};
	$osmfile = sprintf("osmfile_%s", $count);
	$DataDir = sprintf("Output_%s", $Config{$conf_country});

	$Dir = "html/$country"; mkdir $Dir if ! -d $Dir;

	my %indexrow = (country => $country,
			date => $date);

	push(@tmpl2_loop, \%indexrow);
	
	
	print "start processing :: $Config{$osmfile}\n";
	process($Config{$osmfile}, $DataDir);
	print "finished processing.\n";

	print "start html construction :: $Config{$conf_country}\n";

	
	
	# create site with all tags
	my $KeyCount = AllTags();
	
	#############################################################################################
	#
	# build the sites for each language
	#
	#############################################################################################
	foreach my $Language(@Languages){

		print "\t construct language :: $Language\n";

		my $WatchlistCount = 0; #counts how many keys are on the watchlist
	
		foreach my $Line(@watchedKeys){
			Watchlist($Line, $Language);
			TagStats($Line, $Language);
			$WatchlistCount++;
		}
	
		my @tmpl_loop;
		@watchedKeys = sort(@watchedKeys);
	
		foreach my $wk(@watchedKeys) {
			my $keydesc = GetKeyDescription("Key:$wk", $Language);
			my %row = (name => $wk,
				desc => $keydesc->{'description'},
				lang => $Language);
			push(@tmpl_loop, \%row);
		}
		
		my $template = HTML::Template->new(filename => 'template/index_lang.tmpl');
	
		$template->param(watchlist => \@tmpl_loop);
		$template->param(count => $KeyCount - $WatchlistCount);
		$template->param(country => $country);
	
		$template->param(interlang_header => sprintf($Interface->{'header'}->{$Language}, $country, $date));
		$template->param(interlang_interlang => $Interface->{'interlang'}->{$Language});
		$template->param(interlang_everything => $Interface->{'everything'}->{$Language});
		$template->param(interlang_up => $Interface->{'up'}->{$Language});
		$template->param(interlang_watchlist => $Interface->{'watchlist'}->{$Language});
		$template->param(interlang_credits => sprintf($Interface->{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));
		
	
		open(INDEX, ">$Dir/index_$Language.htm"); 
		print INDEX $template->output;
		close INDEX;
	}

	print "finished construction of $Config{$conf_country}.\n";
}

my $template = HTML::Template->new(filename => 'template/index.tmpl');

$template->param(indexlist => \@tmpl2_loop);
		
open(TOPINDEX, ">html/index.htm"); 
print TOPINDEX $template->output;
close TOPINDEX;

#generate sample output request file
print "generate sample_request.txt.\n";
open(SAMPLE_REQUESTS, ">sample_requests.txt");
foreach my $request(keys %SampleRequest) {
	printf SAMPLE_REQUESTS "%s\n", $request;
}
close SAMPLE_REQUESTS;

#############################################################################################
#
# create description page for the tags which are on the watchlist.
#
#############################################################################################
sub Watchlist{
	my($Key, $Language) = @_;
	my $Filename = "${Language}_tag_$Key.htm";

	my $Values = GetValues($Key);
	my $Max = Max($Values);
	my $pagename;
	my @tmpl_loop;
	my @Others;

	foreach my $Value(sort {$Values->{$b} <=> $Values->{$a}} keys %{$Values}){
		if($Language eq "en"){
			$pagename = "Tag:$Key=$Value"; }
		else {
			$pagename = ucfirst("$Language:Tag:$Key=$Value"); }

		my $KeyDescription = GetTagDescription($pagename, $Language);

		if(exists $KeyDescription->{'description'}){
			# TODO grab photos from wiki Tag:Key=value page 
			#GetPhotos($Key, $Value,$KeyDescription->{'image'});
			my $Image = sprintf("%s_%s.jpg", $Key, $Value);
			my $elementNode= "&nbsp;";
			my $elementWay= "&nbsp;";
			my $elementArea= "&nbsp;";
			my $ImageHtml = "<img src=\"../photos/$Image\" alt=\"Photo example\">" if(-f "$PDir/$Image");

			if($KeyDescription->{'onNode'} eq "yes"){
				$elementNode = "<img src=../images/Mf_node.png>"; }

			if($KeyDescription->{'onWay'} eq "yes"){
				$elementWay = "<img src=../images/Mf_way.png>"; }

			if($KeyDescription->{'onArea'} eq "yes"){
				$elementArea = "<img src=../images/Mf_area.png>"; }

			my $Element = sprintf("%s %s %s", $elementNode, $elementWay, $elementArea);

			my $Sample = GetSampleImage($Key,$Value);

			my %row = (tag => "$Key=$Value",
               		   	  value => $Value,
               		   	  pagename => $pagename,
               		   	  element => $Element,
               		   	  desc => $KeyDescription->{'description'},
               		   	  photo => $ImageHtml,
               		   	  render => $Sample);

			push(@tmpl_loop, \%row);
    		}
    		else {
        		my $Log = 1 + (log($Values->{$Value} / $Max) / log(10));
        		my $Darkness = $Log * -60;
        		$Darkness = 200 if($Darkness > 200);
        		my $Grey = "#" . sprintf("%02x", $Darkness) x 3;
        		my $Size = 120 + $Log * 10;
        		$Size = 80 if($Size < 80);
        		my $Stuff = sprintf("<span style=\"color:%s;font-size:%d%%\">%s</span>", $Grey, $Size, $Value);
        		push(@Others, $Stuff);
    		}
  	}

	my $otherlist = join(",\n", @Others);

	my $template = HTML::Template->new(filename => 'template/lang_tag_key.tmpl');

	$template->param(taglist => \@tmpl_loop);
	$template->param(key => $Key);
	$template->param(other => $otherlist);
	$template->param(lang => $Language);
	$template->param(country => $country);

	$template->param(interlang_header => sprintf($Interface->{'header'}->{$Language}, $country, $date));
	$template->param(interlang_interlang => $Interface->{'interlang'}->{$Language});
	$template->param(interlang_up => $Interface->{'up'}->{$Language});
	$template->param(interlang_wiki => sprintf($Interface->{'wiki'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Key:$Key\" target=\"_blank\">$Key</a>"));
	$template->param(interlang_othertags => $Interface->{'othertags'}->{$Language});
	$template->param(interlang_credits => sprintf($Interface->{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	open(OUT, ">$Dir/$Filename");
	print OUT $template->output;
  	close OUT;

}

#############################################################################################
#
# create statistical page for the tags which are on the watchlist.
#
#############################################################################################
sub TagStats{
	my($Key, $Language) = @_;
	my $Filename = "${Language}_stats_$Key.htm";

  	my $Values = GetValues($Key);
	my $ValuesCount = GetValuesCount($Key);
  	my @tmpl_loop;
  	my $Count = 0; 
  	my $Count_t = 0; 
  	my $Count_n = 0; 
  	my $Count_w = 0;

  	foreach my $Value(sort {$Values->{$b} <=> $Values->{$a}} keys %{$Values}) {

		my $wiki_de = "wiki_red";
		my $wiki_en = "wiki_red";
		my $wiki_tr = "wiki_red";
		my $wiki_fr = "wiki_red";
		my $wiki_sl = "wiki_red";
		my $wiki_nl = "wiki_red";
		if(CheckIfExist("Tag:$Key=$Value", $tag_articles)) {
			$wiki_en = "wiki_green"; }
		if(CheckIfExist("De:Tag:$Key=$Value", $tag_articles)) {
			$wiki_de = "wiki_green"; }
		if(CheckIfExist("Fr:Tag:$Key=$Value", $tag_articles)) {
			$wiki_fr = "wiki_green"; }
		if(CheckIfExist("Sl:Tag:$Key=$Value", $tag_articles)) {
			$wiki_sl = "wiki_green"; }
		if(CheckIfExist("Nl:Tag:$Key=$Value", $tag_articles)) {
			$wiki_nl = "wiki_green"; }
		if(CheckIfExist("Tr:Tag:$Key=$Value", $tag_articles)) {
			$wiki_tr = "wiki_green"; }

		my %row = (key => $Key,
               		   value => $Value,
               		   total => $Values->{$Value},
               		   node => $ValuesCount->{$Value}->{'n'},
               		   way => $ValuesCount->{$Value}->{'w'},
               		   wiki_de => $wiki_de,
               		   wiki_en => $wiki_en,
               		   wiki_tr => $wiki_tr,
               		   wiki_fr => $wiki_fr,
               		   wiki_sl => $wiki_sl,
               		   wiki_nl => $wiki_nl);

		push(@tmpl_loop, \%row);

    		$Count++;
		$Count_t += $Values->{$Value};
		$Count_n += $ValuesCount->{$Value}->{'n'};
		$Count_w += $ValuesCount->{$Value}->{'w'};

		#generate combination page for this tag
                if($Language eq 'en') {
			TagCombi($Key,$Value,$Language); }
  	}

	my $cnp = 0;
	my $cwp = 0;
	if($Count_t > 0) {
		$cnp = round((100 / $Count_t) * $Count_n);
		$cwp = round((100 / $Count_t) * $Count_w); }

	my $template = HTML::Template->new(filename => 'template/lang_stats_key.tmpl');

	$template->param(statlist => \@tmpl_loop);
	$template->param(key => $Key);
	$template->param(lang => $Language);
	$template->param(country => $country);
	$template->param(count_diff => $Count);
	$template->param(count_total => $Count_t);
	$template->param(count_node => $Count_n);
	$template->param(count_node_p => $cnp);
	$template->param(count_way => $Count_w);
	$template->param(count_way_p => $cwp);

	$template->param(interlang_header => sprintf($Interface->{'header'}->{$Language}, $country, $date));
	$template->param(interlang_interlang => $Interface->{'interlang'}->{$Language});
	$template->param(interlang_up => $Interface->{'up'}->{$Language});
	$template->param(interlang_wiki => sprintf($Interface->{'wiki'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Key:$Key\" target=\"_blank\">$Key</a>"));
	$template->param(interlang_credits => sprintf($Interface->{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	open(OUT, ">$Dir/$Filename");
	print OUT $template->output;
  	close OUT;
}

#############################################################################################
#
# create statistical page for the tags combinations.
#
#############################################################################################
sub TagCombi{
	my($Key, $Value, $Language) = @_;
	my $Filename = "${Language}_combination_$Key=$Value.htm";

	my $Combis = GetCombinations("$Key=$Value");
  	my @tmpl_loop;

	foreach my $Comb(sort {$Combis->{$b} <=> $Combis->{$a}} keys %{$Combis}) {

		my %row = (combi_tag => $Comb,
                           tag_count => $Combis->{$Comb});

		push(@tmpl_loop, \%row);
	}

	my $template = HTML::Template->new(filename => 'template/lang_combi_tag.tmpl');

	$template->param(combilist => \@tmpl_loop);
	$template->param(tag => "$Key=$Value");
	$template->param(lang => $Language);
	$template->param(country => $country);

	$template->param(interlang_header => sprintf($Interface->{'header'}->{$Language}, $country, $date));
	#$template->param(interlang_interlang => $Interface->{'interlang'}->{$Language});
	$template->param(interlang_up => $Interface->{'up'}->{$Language});
	$template->param(interlang_wiki => sprintf($Interface->{'wiki'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tag:$Key=$Value\" target=\"_blank\">$Key=$Value</a>"));
	$template->param(interlang_credits => sprintf($Interface->{'credits'}->{$Language}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	open(OUT, ">$Dir/$Filename");
	print OUT $template->output;
  	close OUT;

}

#############################################################################################
#
# create an overview page with all found keys and the most used values
#
#############################################################################################
sub AllTags{
	my $Filename = "tags.htm";
	my $Filename1 = "top_unapproved.htm";
	my $Filename2 = "top_undocumented.htm";

	my $Tags = GetTags();
	my @tmpl_loop;
	my $KeyCount = 0;
	my %IgnoreValues = IgnoreValues();
	my $unapproved;
	my @tmpl_loop2;
	my $undocumented;
	my @tmpl_loop3;

	foreach my $Tag(sort {$Tags->{$b} <=> $Tags->{$a}} keys %{$Tags}){
		my $Values = GetValues($Tag);
		my $Count = 0;
		my @Examples;

		foreach my $Value(sort {$Values->{$b} <=> $Values->{$a}} keys %{$Values}){
			my $Text = $Value;
			my $Text = sprintf("<b>$Value</b> (%d)", $Values->{$Value}) if($Values->{$Value} > 0);
			
			push(@Examples, $Text) if($Count++ < 50 && $Text);

			# build list with unapproved tags that do not take a user defined value such as the name tag
			#my $xy = "$Tag=$Value";
			#if(!$IgnoreValues{$Tag} && ($Approved_tags->{$xy} ne 1 )) {
			#	$unapproved->{"$Tag=$Value"} = $Values->{$Value}; }
			# build list with unapproved tags that do not take a user defined value such as the name tag
			if(!$IgnoreValues{$Tag} && !CheckIfExist("Tag:$Tag=$Value", $tag_articles)) {
				$undocumented->{"$Tag=$Value"} = $Values->{$Value};
			}			
    		}

		my %row = (key => $Tag,
               		   values => join(", ", @Examples));
		push(@tmpl_loop, \%row);
    		$KeyCount++;
  	}

	#############################################################################
	# overview page with all tags
	#############################################################################
	my $template = HTML::Template->new(filename => 'template/tags.tmpl');
	
	$template->param(taglist => \@tmpl_loop);
	$template->param(country => $country);

	$template->param(interlang_header => sprintf($Interface->{'header'}->{'en'}, $country, $date));
	$template->param(interlang_up => $Interface->{'up'}->{'en'});
	$template->param(interlang_credits => sprintf($Interface->{'credits'}->{'en'}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	open(OUT, ">$Dir/$Filename");
	print OUT $template->output;
  	close OUT;

	#############################################################################
	# overview page with top 100 unapproved tags
	#############################################################################
	#$count_top = 0;
	#foreach my $Tag(sort {$unapproved->{$b} <=> $unapproved->{$a}} keys %{$unapproved}){
	#	my %row = (tag => $Tag,
        #       		   count => $unapproved->{$Tag});
	#	if($count_top <= 100) {
	#		push(@tmpl_loop2, \%row); }
	#	$count_top++;
	#}

	my $template = HTML::Template->new(filename => 'template/top_unapproved.tmpl');
	
	$template->param(toplist => \@tmpl_loop2);
	$template->param(country => $country);

	$template->param(interlang_header => sprintf($Interface->{'header'}->{'en'}, $country, $date));
	$template->param(interlang_up => $Interface->{'up'}->{'en'});
	$template->param(interlang_credits => sprintf($Interface->{'credits'}->{'en'}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	open(TOP, ">$Dir/$Filename1");
	print TOP $template->output;
  	close TOP;

	#############################################################################
	# overview page with top 100 undocumented tags
	#############################################################################
	my $count_top = 0;
	foreach my $Tag(sort {$undocumented->{$b} <=> $undocumented->{$a}} keys %{$undocumented}){
		my %row = (tag => $Tag,
               		   count => $undocumented->{$Tag});
		if($count_top <= 100) {
			push(@tmpl_loop3, \%row); }
		$count_top++;
	}

	my $template = HTML::Template->new(filename => 'template/top_undocumented.tmpl');
	
	$template->param(toplist => \@tmpl_loop3);
	$template->param(country => $country);

	$template->param(interlang_header => sprintf($Interface->{'header'}->{'en'}, $country, $date));
	$template->param(interlang_up => $Interface->{'up'}->{'en'});
	$template->param(interlang_credits => sprintf($Interface->{'credits'}->{'en'}, "<a href=\"http://wiki.openstreetmap.org/index.php/Tagwatch\" target=\"_blank\">Tagwatch</a>"));

	open(TOP, ">$Dir/$Filename2");
	print TOP $template->output;
  	close TOP;

	return $KeyCount;
}

























##############################################################
#
# basic subroutines 
#
##############################################################

sub LoadInterface{
  my $X;
  foreach my $Line(split(/\n/, $c->text("Tagwatch/Interface"))){
    if($Line =~ m{ \* \s* (\w+) \: (\w+) \: \s* (.*) }x){
      $X->{$2}->{$1} = $3;
    }
  }
  return $X;
}

sub GetApprovedTags{
  my $X;
  foreach my $key(@approvedKeySections) {
    foreach my $Line(split(/\n/, $c->text("Template:Map_Features:$key"))){
      if($Line =~ m{ \{\{\{(.*):value(.*) }x){
        my $xyz= "$key=$1";
        $X->{$xyz} = 1;
      }
    }
  }


  return $X;
}

sub Max{
  my ($Values) = @_;
  my $Max = 0;
  while(my($k,$v) = each(%{$Values})){
     $Max = $v if($v > $Max);
  }
  return $Max;
}

sub GetTags{
  open(IN, "<$DataDir/tags.txt") || return;
  my %Values;
  while(my $Line = <IN>){
    if($Line =~ m{(\d+) (.*)}){
      $Values{$2} = $1 if($1 && $2);
    }
  }
  close IN;
  return \%Values;
}

sub GetValues{
  my ($Key) = @_;
  open(IN, "<$DataDir/tag_$Key.txt") || return;
  my %Values;
  while(my $Line = <IN>){
    if($Line =~ m{(\d+) (\d+) (\d+) (.*)}){
      $Values{$4} = $1 if($1 && $4);
    }
  }
  close IN;
  return \%Values;
}

sub GetValuesCount{
  my ($Key) = @_;
  open(IN, "<$DataDir/tag_$Key.txt") || return;
  my %Values;
  while(my $Line = <IN>){
    if($Line =~ m{(\d+) (\d+) (\d+) (.*)}){
      $Values{$4}->{'t'} = $1 if($1 && $4);
      $Values{$4}->{'n'} = $2 if($2 && $4);
      $Values{$4}->{'w'} = $3 if($3 && $4);
    }
  }
  close IN;
  return \%Values;
}

sub GetCombinations{
  my ($Tag) = @_;
  open(IN, "<$DataDir/combi_$Tag.txt") || return;
  my %Combis;
  while(my $Line = <IN>){
    if($Line =~ m{(\d+) (.*)}){
      $Combis{$2} = $1 if($1 && $2);
    }
  }
  close IN;
  return \%Combis;
}

sub GetSampleImage{
  my ($Key, $Value) = @_;
  # Add this tag to a list of requests, so makeSamples.pl will 
  # generate the image later
  #printf SAMPLE_REQUESTS "%s = %s\n", $Key, $Value;
  my $x = "$Key=$Value";
  $SampleRequest{$x} = 1;

  # Return the location where makeSamples.pl will put the PNG image
  return sprintf("samples/%s_%s.png", $Key, $Value);
}

#check if the key or tag is found in the accociated category
sub CheckIfExist{
  my ($name, $array_articles) = @_;

  foreach my $Value (@{$array_articles}) {
    $Value =~ s/ /_/;
    if($Value eq $name){
        return 1;
    }
  }
  return 0;
}

#parse the wiki site for the Key description
#site has to be Key:keyname
#or the international version e.g. De:Key:keyname etc
sub GetKeyDescription{
  my ($Pagename,$Language) = @_;
  my $KeyInfo;
  my $Checkpage;

  if($Language ne 'en') {
    $Checkpage = "$Language:$Pagename"; }
  else {
    $Checkpage = $Pagename; }

  if(CheckIfExist($Checkpage, $key_articles)){
    foreach my $Line(split(/\n/, $c->text($Pagename))){
      if($Line =~ m{\s*(\w+)\s*=\s*(.*?)\s*\|\s*$}){
        $KeyInfo->{$1} = $2;
      }
      # end parsing at the end of the header wiki template
      if($Line =~ m{.*?\}\}\s*$}){
        last;
      }
    }
  }
  else {
    if(CheckIfExist($Pagename, $key_articles)){
      foreach my $Line(split(/\n/, $c->text($Pagename))){
        if($Line =~ m{\s*(\w+)\s*=\s*(.*?)\s*\|\s*$}){
          $KeyInfo->{$1} = $2;
        }
        # end parsing at the end of the header wiki template
        if($Line =~ m{.*?\}\}\s*$}){
          last;
        }
      }
    }
  }
  return $KeyInfo;
}

#parse the wiki site for the Tag description
#site has to be Tag:keyname=value
#or the international version e.g. De:Key:keyname etc
sub GetTagDescription{
  my ($Pagename,$Language) = @_;
  my $TagInfo;
  if(CheckIfExist($Pagename, $tag_articles)){
    foreach my $Line(split(/\n/, $c->text($Pagename))){
      if($Line =~ m{\s*(\w+)\s*=\s*(.*?)\s*\|\s*$}){
        $TagInfo->{$1} = $2;
      }
      # end parsing at the end of the header wiki template
      if($Line =~ m{.*?\}\}\s*$}){
        last;
      }
    }
  }
  # Fallback get description from tagwatch subpages
  else {
        if(exists $DescriptionsFallback->{$Language}->{ucfirst($Pagename)}->{'description'}) {
            $TagInfo->{'description'} = $DescriptionsFallback->{$Language}->{ucfirst($Pagename)}->{'description'};
            $TagInfo->{'key'} = $DescriptionsFallback->{$Language}->{$Pagename}->{'key'};
            $TagInfo->{'value'} = $DescriptionsFallback->{$Language}->{$Pagename}->{'value'};
            $TagInfo->{'image'} = "-";
            $TagInfo->{'onNode'} = "&nbsp;";
            $TagInfo->{'onWay'} = "&nbsp;";
            $TagInfo->{'onArea'} = "&nbsp;";
            $TagInfo->{'lang'} = $Language; 
         }
  }
  return $TagInfo;
}

#Fallback to get the description on the Tagwatch subpages if no Tag:Key=Value page exist
sub GetDescriptionsFallback{
  my ($Language) = @_;
  my %D;
  my $pagename;
  foreach my $Line(split(/\n/, $c->text("Tagwatch/Descriptions/$Language"))){
    if($Line =~ m{\*\s*(\w+)\s*=\s*(\w+)\s*:\s*(.*?)\s*$}){
        if($Language eq 'en') {
        $pagename = ucfirst(sprintf("Tag:%s=%s", $1, $2)); } else {
        $pagename = ucfirst(sprintf("%s:Tag:%s=%s", $Language, $1, $2)); }
        $D{$pagename}->{'description'} = $3;
        $D{$pagename}->{'key'} = $1;
        $D{$pagename}->{'value'} = $2;
    }
  }
  return \%D;
}

# just the same as the getPhotos.pl but with the image given from the osm wiki
# TODO does not work yet
sub GetPhotos{
  my ($Key, $Value, $ImageName) = @_;
  # Download the image
  my $Filename = "$PDir/$Key\_$Value.jpg";

  my $CacheName = $c->filepath("Dsc01078 clip.jpg");
  $CacheName =~ s/\W/_/g;
  $CacheName = "$CacheDir/$CacheName.jpg";
  mirror($ImageName, $CacheName);

  my $Data;
  {
    local($/);
    open(LOAD, $CacheName) || die;
    binmode LOAD;
    $Data = <LOAD>;
    close LOAD;
  }

  # Open it in GD
  my $Image1 = GD::Image->newFromJpegData($Data);

  # Calculate image size
  my $WO = $Image1->width;
  my $W = 200;
  my $Ratio = $W / $WO;
  my $HO = $Image1->height;
  my $H = $HO * $Ratio;

  # Make a resized copy, and save that
  my $Image2 = new GD::Image($W,$H);
  $Image2->copyResampled($Image1,0,0,0,0,$W,$H,$WO,$HO);
  open(IMOUT, ">$Filename") || die;
  binmode IMOUT;
  print IMOUT $Image2->jpeg();
  close IMOUT;
}

#--------------------------------------------------------------------------
# Reads the tagwatch config file, returns a hash array
#--------------------------------------------------------------------------
sub ReadConfig {
	my ($Filename) = @_;
	my %Config;

	open(my $fp,"<$Filename") || die("Can't open \"$Filename\" ($!)\n");

        while(my $Line = <$fp>)
        {
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
                        }x)
            {
	        # Store config options in a hash array
                $Config{$1} = $2;
                #print "Found $1 ($2)\n";  # debug option
            }
        }
        close $fp;

    return %Config;
}


1;










