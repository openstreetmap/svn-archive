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
my $Dir = "html"; mkdir $Dir if ! -d $Dir;
my $DataDir = "Data";
open(SAMPLE_REQUESTS, ">sample_requests.txt");

my @Languages = ("en","fr","de");

my $c = MediaWiki->new;
$c->setup({'wiki' => {
            'host' => 'wiki.openstreetmap.org',
            'path' => ''}});
my $wp = MediaWiki->new;
$wp->setup({'wiki' => {
            'host' => 'en.wikipedia.org',
            'path' => 'w'}});

# P.O.S. website doesn't even expose an REST API... :(
# my $omega = MediaWiki->new;
# $omega->setup({'wiki' => {
#            'host' => 'www.omegawiki.org',
#            'path' => ''}});
# print $omega->text("DefinedMeaning:river_(3253)");die;
 
my $Descriptions;
foreach my $Lang(@Languages){
  $Descriptions->{$Lang} = GetDescriptions($Lang);
}


foreach my $Language(@Languages){
  open(INDEX, ">$Dir/index_$Language.htm");
  print INDEX htmlHeader("OpenStreetMap tagging");
  print INDEX InterLingualLinks("index_%s.htm",  $Language);
  print INDEX "<h2>List of all tags found</h2>\n";
  AllTags();
  
  print INDEX "<h2>Watchlist tags</h2>\n";
  foreach my $Line(split(/\n/, $c->text("Tagwatch/Watchlist"))){
    if($Line =~ m{\* (\w+)}){
      Watchlist($1, $Language);
    }
  }
  print INDEX htmlFooter();
  close INDEX;
}

sub InterLingualLinks{
  my ($UrlPattern, $CurrentLanguage) = @_;
  
  my @InterLanguageLinks;
  foreach my $L(@Languages){
    if($L eq $CurrentLanguage){
      push(@InterLanguageLinks, "<b>$L</b>");
    }
    else{
      my $URL = sprintf($UrlPattern, $L);
      push(@InterLanguageLinks, "<a href=\"$URL\">$L</a>");
    }
  }
  return("<p>In other languages: ". join(", ", @InterLanguageLinks). "</p>");
}
sub Watchlist{
   my($Tag, $Language) = @_;
   my $Filename = "${Language}_tag_$Tag.htm";
   Index($Filename, "$Tag");
   open(OUT, ">$Dir/$Filename");
   print OUT htmlHeader($Tag);
   print OUT "<p><a href=\"./index_$Language.htm\">Back to index</a></p>\n";
   print OUT "<h1>$Tag</h1>\n";
   
   print OUT "<p>Discuss <a href=\"http://wiki.openstreetmap.org/index.php/Key:$Tag\">$Tag</a> on the wiki</p>\n";
   
   print OUT InterLingualLinks("%s_tag_$Tag.htm",  $Language);
   
   my $Values = GetValues($Tag);
   my $Max = Max($Values);
   my @Others;
   print OUT table();
   foreach my $Value(sort {$Values->{$b} <=> $Values->{$a}} keys %{$Values}){
     if($Values->{$Value} > ($Max / 120)){
       my $Image = sprintf("Photos/%s_%s.jpg", $Tag, $Value);
       my $ImageHtml = '-';
       $ImageHtml = "<img src=\"$Image\">" if(-f "$Dir/$Image");
       
       my $Text = $Descriptions->{$Language}->{$Tag}->{$Value};
       $Text = '&nbsp;' if(!$Text);
       
       
       my @Links = (
       "<a href=\"http://$Language.wikipedia.org/wiki/$Value\">Wikipedia</a>",
       "Translate" # TODO
       );
       
       my $Sample = GetSampleImage($Tag,$Value);
       my $SampleHtml = "<img src=\"$Sample\">";
       
       printf OUT "<tr><td>%s = %s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n", 
       $Tag, 
       $Value, 
       $ImageHtml,
       $SampleHtml, 
       $Text,
       "<small>".join(",<br>", @Links)."</small>";
     }
     else{
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
   print OUT "</table>\n";
   
   printf OUT "<h2>Other values used</h2>\n<p>%s</p>\n", join(",\n", @Others);
   print OUT htmlFooter();
   close OUT;
}

sub GetSampleImage{
  my ($Tag, $Value) = @_;
  # Add this tag to a list of requests, so makeSamples.pl will 
  # generate the image later
  printf SAMPLE_REQUESTS "%s = %s\n", $Tag, $Value;
  
  # Return the location where makeSamples.pl will put the PNG image
  return sprintf("Samples/%s_%s.png", $Tag, $Value);
}

sub AllTags{
  my $Filename = "tags.htm";
  Index($Filename, "All tags");
  
  my $Tags = GetTags();
  open(OUT, ">$Dir/$Filename");
  print OUT table();
  print OUT "<tr><th>Key</th><th>Examples</th></tr>\n";
  foreach my $Tag(sort {$Tags->{$b} <=> $Tags->{$a}} keys %{$Tags}){
    my $Values = GetValues($Tag);
    my $Count = 0;
    my @Examples;
    foreach my $Value(sort {$Values->{$b} <=> $Values->{$a}} keys %{$Values}){
       my $Text = $Value;
       my $Text = sprintf("<b>$Value</b> (%d)", $Values->{$Value}) if($Values->{$Value} > 100);
       
      push(@Examples, $Text) if($Count++ < 10 && $Text);
    }
    printf OUT "<tr><td>%s</td><td>%s</td></tr>\n", $Tag, join(", ", @Examples);
    
  }
  print OUT "</table>\n";
  close OUT;
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
sub GetDescriptions{
  my ($Language) = @_;
  my %D;
  foreach my $Line(split(/\n/, $c->text("Tagwatch/Descriptions/$Language"))){
    if($Line =~ m{\*\s*(\w+)\s*=\s*(\w+)\s*:\s*(.*?)\s*$}){
      $D{$1}->{$2} = $3;
    }
  }
  return \%D;
}
sub Index{
  my ($Page, $Title) = @_;
  print INDEX "<p><a href=\"$Page\">$Title</a></p>\n";
}

sub Max{
  my ($Values) = @_;
  my $Max = 0;
  while(my($k,$v) = each(%{$Values})){
     $Max = $v if($v > $Max);
  }
  return $Max;
}
sub GetValues{
  my ($Tag) = @_;
  open(IN, "<$DataDir/tag_$Tag.txt") || return;
  my %Values;
  while(my $Line = <IN>){
    if($Line =~ m{(\d+) (.*)}){
      $Values{$2} = $1 if($1 && $2);
    }
  }
  close IN;
  return \%Values;
}
sub table{
  "<table border=1 cellpadding=4 cellspacing=0>\n"
}
sub htmlHeader{
  my $Title = shift();
  return("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>$Title</title></head><body>");
}
sub htmlFooter{
  my $URL = "http://wiki.openstreetmap.org/index.php/Tagwatch";
  my $Footer = "<p>Generated by <a href=\"$URL\">TagWatch</a></p>";
  return("<p>&nbsp;</p>\n<hr>\n$Footer\n</body>\n</html>");
}

sub translate{
  my($Word, $Language) = @_;
  my $Data = $wp->text($Word);
  print "Translating $Word into $Language\n";
  if($Data =~ m{ \[\[ fr : (.+?) \]\] }x ){
    return($1, 1);
  }
  return($Word, 0);
}