package osm;

use strict;
use warnings;


use WWW::Curl::easy;

sub new(){bless{}};

sub setup(){
  my $self = shift();
  $self->{Username} = shift();
  $self->{Password} = shift();
  $self->{UserAgent} = shift();
}

sub tempfiles(){
  my $self = shift();
  $self->{file1} = shift();
  $self->{file2} = shift();
}

sub uploadWay(){
  my ($self, $Tags, @Segments) = @_;
  $Tags .= sprintf("<tag k=\"created_by\" v=\"%s\"/>", $self->{UserAgent});
  
  my $Segments = "";
  foreach $Segment(@Segments){
    $Segments .= "<seg id=\"$Segment\"/>";
  }
  
  my $Way = "<way id=\"0\">$Segments$Tags</way>";
  my $OSM = "<osm version=\"0.3\">$Way</osm>";
  my $data = "<?xml version=\"1.0\"?>\n$OSM";
  my $path = "way/0";

  my ($response, $http_code) = $self->upload($data, $path);
  return($response);
}

sub uploadSegment(){
  my ($self, $Node1,$Node2,$Tags) = @_;
  $Tags .= sprintf("<tag k=\"created_by\" v=\"%s\"/>", $self->{UserAgent});
  
  my $Segment = sprintf("<segment id=\"0\" from=\"%d\" to=\"%d\">$Tags</segment>", $Node1,$Node2);
  my $OSM = "<osm version=\"0.3\">$Segment</osm>";
  my $data = "<?xml version=\"1.0\"?>\n$OSM";
  my $path = "segment/0";

  my ($response, $http_code) = $self->upload($data, $path);
  
  
  return($response);
}

sub uploadNode(){
  my ($self, $Lat, $Long, $Tags) = @_;
  $Tags .= sprintf("<tag k=\"created_by\" v=\"%s\"/>", $self->{UserAgent});
  
  my $Node = sprintf("<node id=\"0\" lon=\"%f\" lat=\"%f\">$Tags</node>", $Long, $Lat);
  my $OSM = "<osm version=\"0.3\">$Node</osm>";
  my $data = "<?xml version=\"1.0\"?>\n$OSM";
  my $path = "node/0";

  my ($response, $http_code) = $self->upload($data, $path);
  
  return($response);
}

sub upload(){
  my($self, $data, $path) = @_;
  
  my $curl = new WWW::Curl::easy;

  my $login = sprintf("%s:%s", $self->{Username}, $self->{Password});
  
  open(my $FileToSend, ">", $self->{file1});
  print $FileToSend $data;
  close $FileToSend;
  
  my $url = "http://www.openstreetmap.org/api/0.3/$path";  
  
  open(my $TxFile, "<", $self->{file1});
  open(my $RxFile, ">",$self->{file2});
  $curl->setopt(CURLOPT_URL,$url);
  $curl->setopt(CURLOPT_RETURNTRANSFER,-1);
  $curl->setopt(CURLOPT_HEADER,0);
  $curl->setopt(CURLOPT_USERPWD,$login);
  $curl->setopt(CURLOPT_PUT,-1);
  $curl->setopt(CURLOPT_INFILE,$TxFile);
  $curl->setopt(CURLOPT_INFILESIZE, -s $self->{file1});
  $curl->setopt(CURLOPT_FILE, $RxFile);
  
  $curl->perform();
  my $http_code = $curl->getinfo(CURLINFO_HTTP_CODE);
  my $err = $curl->errbuf;
  $curl->close();
  close $TxFile;
  close $RxFile;
  
  open(my $ResponseFile, "<", $self->{file2});
  my $response = int(<$ResponseFile>);
  close $ResponseFile;
  
  print "Code $http_code\n" if($http_code != 200);
  
  return($response, $http_code);
}

1
