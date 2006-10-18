package osm;
use WWW::Curl::easy;

sub new(){bless{}};

sub setup(){
  my $self = shift();
  $self->{Username} = shift();
  $self->{Password} = shift();
  $self->{UserAgent} = shift();
  
  # You might need to change this to Easy with a capital E
  $self->{Curl} = new WWW::Curl::easy;

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

  my $login = sprintf("%s:%s", $self->{Username}, $self->{Password});
  
  open(my $FileToSend, ">", $self->{file1});
  print $FileToSend $data;
  close $FileToSend;
  
  my $url = "http://www.openstreetmap.org/api/0.3/$path";  
  my $curl = $self->{Curl};
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

=head1 NAME

Geo::OSM - Upload data to OpenStreetMap

=head1 SYNOPSIS

  use Geo::OSM;
  my $osm = new Geo::OSM;
  $osm->setup("user\@domain.com","password","my_useragentname");
  $osm->tempfiles("temp1.txt", "temp2.txt");
  
  $osm->uploadNode($Lat, $Long, $Tags);
  $osm->uploadSegment($Node1, $Node2, $Tags);
  $osm->uploadWay($Tags, @Segments);


=head1 DESCRIPTION

This module allows you to upload data to OpenStreetMap, using its REST API

=head2 Methods

=over 12

=item C<new>

Returns a new Geo::OSM object.

=item C<setup($Username,$Password,$UserAgent)>

Username is typically an email address, remembering that the @ symbol must be escaped in string

Password is your openstreetmap password

User Agent is a name to identify your program.  It will be added in the "created_by" tag to make it easier for people to identify features your program has created

=item C<tempfiles($File1, $File2)>

Two files that get used by CURL for sending and receiving POST data

=item C<uploadNode($Lat, $Long, $Tags)>

Uploads a node to OpenStreetMap, and returns the index of the new node, or 0 if it couldn't be created.

Latitude and Longitude are in degrees, reference to WGS-84 

Tags are of the form <tag k="amenity" v="pub"/>

See "Map Features" page on the OpenStreetMap wiki for a list of tags that are typically used

=item C<uploadSegment($FromNode, $ToNode, $Tags)>

Uploads a segment to OpenStreetMap, and returns the index of the new segment, or 0 if it couldn't be created.

FromNode and ToNode are the indexes of two nodes

Tags are of the form <tag k="highway" v="motorway"/>

=item C<uploadWay($Tags, @Segments)>

Uploads a way (list of segments) to OpenStreetMap, and returns the index of the new way, or 0 if it couldn't be created.

Tags are same as used elsewhere

@Segments is an array of segment IDs

=back

=head1 LICENSE

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=head1 AUTHOR

Oliver White - L<http://www.blibbleblobble.co.uk/>

=head1 SEE ALSO

L<http://openstreetmap.org/>

L<http://wiki.openstreetmap.org/>

=cut

1