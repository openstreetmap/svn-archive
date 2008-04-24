#!/usr/bin/perl -w
#
use strict;
use warnings;

# osmunbraid.pl

# Copyright 2008 by Alan Millar.  Released under the GNU Public
#   License.  Everybody share and play nicely together.

# OpenStreetMap utilty - fix "braided" streets.  These are streets with
#   two ways (typically a two-way street with an island or divider
#   down the middle) where the sections switch across each other.  This
#   utility will fix them so that the same side is in a single way.
#   See http://wiki.openstreetmap.org/index.php/Tiger_fixup

#----------------------------------------
#program version
my $VERSION="0.1";

my $OsmServer="http://api.openstreetmap.org";
my $DownloadNodeLimit=500;	# download this many nodes at a time

my $DownloadMargin=0.001;	# bbox margin in decimal degrees

#-------------------------------------------------------
use XML::Simple;
use Data::Dumper;
use Getopt::Long;
use Math::Trig qw(:radial rad2deg deg2rad);
use LWP::Simple;

#----------------------------------------
my (
  $xmlobj,		# parser object
  $osm,			# parsed data hash ref
  $way1,		# way number
  $way2,		# way number
  $OsmXmlFile,		# input file
  $NewXmlFile,		# output file
  $ErrorFile,		# error file
  %intersectCount,	# intersecting nodes of two ways
  $intersections,
  @IntersectList,	# list of intersection nodes, in order of way1
  %WayNodeList,		# list of nodes in way
  %WayNodeIndex,	# order of nodes within way
  %NewWayNodeList,	# list of nodes in way, after swapping
  %NewWayNodeIndex,	# order of nodes within way, after swapping
  %NodeWays,		# ways using node
  %newlist,		# new way nodes
  $NodeCountBefore1, $NodeCountAfter1,
  $NodeCountBefore2, $NodeCountAfter2,
  %NodeChanges,		# existing nodes with changed lat/lon
  %NewNodes,		# newly created nodes with new lat/lon
  $StraightenFlag,	# Whether or not to straighten intersections
  $OnewayFlag,		# Whether or not to tag each side as oneway
  $UseBBox,		# whether to download in bounding box
) ;

#==================================================

#defaults
$StraightenFlag=1;
$OnewayFlag=1;
$UseBBox=1;

GetOptions(
  "inputfile:s"=>\$OsmXmlFile,
  "outputfile:s"=>\$NewXmlFile,
  "errorfile:s"=>\$ErrorFile,
  "straighten!"=>\$StraightenFlag,
  "oneway!"=>\$OnewayFlag,
  "bbox!"=>\$UseBBox,
);

($way1,$way2) = @ARGV;

if ( ! $way1 || ! $way2 ) {
	die "ERROR: must specify way1 and way2\n";
}

if ( ! $NewXmlFile ) {
  # default output file name
  $NewXmlFile="new-".$way1."-".$way2.".osm";
}

if ( ! $ErrorFile ) {
  # default output file name
  $ErrorFile="error-".$way1."-".$way2.".osm";
}

#-------------------------
# Main body

# Set up parser object.
#  NOTE: do NOT add "ref" as a keyattr for way->nd->ref because the
#     hash key order would be random, messing up the way node order.
$xmlobj=new XML::Simple(RootName=>'osm',KeyAttr=>'id');

&GetAndParseXML();
#print Dumper($osm);

$NodeCountBefore1=scalar(@{$WayNodeList{$way1}});
$NodeCountBefore2=scalar(@{$WayNodeList{$way2}});

#print Dumper($osm);

$intersections=&GetIntersections();

print "Intersections=$intersections\n";
if ( $intersections < 3 ) {
	&ErrorExit("Must be at least 3 common nodes between the two ways\n");
} # if too few

# &PrintIntersections();

&SwapSides();

$NodeCountAfter1=scalar(@{$newlist{$way1}});
$NodeCountAfter2=scalar(@{$newlist{$way2}});

print "Node counts before:\n";
print "$way1 $NodeCountBefore1 $way2 $NodeCountBefore2\n"; 
print "Node counts after:\n";
print "$way1 $NodeCountAfter1 $way2 $NodeCountAfter2\n"; 

if ( $NodeCountBefore1 + $NodeCountBefore2 != 
      $NodeCountAfter1 + $NodeCountAfter2 ) {

  &ErrorExit("Counts do not add up.\n");
}

#print Dumper(%newlist);

if ( $StraightenFlag ) {
  print "Straightening intersections\n";
  &GetAndParseCrossways();
  &StraightenIntersections();
} else {
  print "Skip straightening intersections\n";
} # if straighten

&SaveNewData();

#print Dumper($osm);

exit;

#----------------------------------------
sub GetAndParseXML {
  my (
    $way,	# OSM way number
    $osmnode,	# OSM node number
    $wayhashref,
    $content,
    $url,
    $tempxml,
    @nodes,
    $nodehash,
    $nodelist,
  );


  if ( $OsmXmlFile ) {
    # input file was specified; use it
    $osm = $xmlobj->XMLin($OsmXmlFile);

  } else {
    $osm={};
    # download required data

    #first retrieve the two ways, with the list of their nodes
    $url=$OsmServer."/api/0.5/ways?ways=".$way1.",".$way2;
    #print "Downloading $url\n";
    $content=get($url) || &ErrorExit("Cannot retrieve $url\n");
    $tempxml= $xmlobj->XMLin($content);

    # save into main structure
    &OsmMerge($tempxml,$osm);

    #print Dumper ($tempxml);
    undef @nodes;
    foreach $way ( $way1, $way2 ) {
      foreach $nodehash ( @{$tempxml->{'way'}{$way}{'nd'}} ) {
        $osmnode=$$nodehash{ref};
        push @nodes, $osmnode;
	#print "main node $osmnode\n";
      } # foreach nodehash
    } # foreach way

    #retrieve all nodes in ways
    &GetAndParseNodes(@nodes);

    if ( $UseBBox ) {
      my ( 
        $lat, $lon,
        $minlat,	# minimum latitude
        $minlon,	# minimum longitude
        $maxlat,	# maximum latitude
        $maxlon,	# maximum longitude
      ) ;
      
      # determine min/max lat/lon
      ( $osmnode ) = keys %{$osm->{'node'}} ;
      $minlat=$osm->{'node'}{$osmnode}{lat};
      $minlon=$osm->{'node'}{$osmnode}{lon};
      $maxlat=$minlat;
      $maxlon=$minlon;
  
      foreach $osmnode ( keys %{$osm->{'node'}} ) {
        $lat=$osm->{'node'}{$osmnode}{lat};
        $lon=$osm->{'node'}{$osmnode}{lon};
        if ( $lat < $minlat ) { $minlat = $lat; } ;
        if ( $lat > $maxlat ) { $maxlat = $lat; } ;
        if ( $lon < $minlon ) { $minlon = $lon; } ;
        if ( $lon > $maxlon ) { $maxlon = $lon; } ;
      } # foreach osmnode
      # add margin 
      $minlat -= $DownloadMargin;
      $maxlat += $DownloadMargin;
      $minlon -= $DownloadMargin;
      $maxlon += $DownloadMargin;
  
      # now we have dimensions; download everything
      $url=$OsmServer."/api/0.5/map?bbox=$minlon,$minlat,$maxlon,$maxlat";
      print "Downloading $url\n";
      $content=get($url) || &ErrorExit("Cannot retrieve $url\n");
  
      print "Downloaded.  Now parsing... ";
      $tempxml= $xmlobj->XMLin($content);
      print "Done.\n";
      # save into main structure
      &OsmMerge($tempxml,$osm);
      print "Parsed.\n";

    } # if usebbox

  } # if inputfile

  #----------------------
  # got data; now unpack into simpler cross-reference structures

  $wayhashref=$osm->{'way'};

  foreach $way ( keys %$wayhashref ) {
    &IndexWay($way);
  } #foreach way

}

#----------------------------------------
# find other cross-street ways which use our intersection points
sub GetAndParseCrossways {
  my (
    $way,	# OSM way number
    $osmnode,	# OSM node number
    @WayGetList,   
    $content,
    $url,
    $TempXml,
    @nodes,
    $nodehash,
    $crossnode,
  );

  #if we are using a file, assume they are in it.  If not, download them
  if ( ! $OsmXmlFile ) {
  
    if ( ! $UseBBox ) {
      # not using bbox download; retrieve specify cross-street ways
      undef @WayGetList;
      # Loop through intersection nodes
      foreach $osmnode ( @IntersectList) {
  
        # get list of cross-street ways for this intersection node
        $url=$OsmServer."/api/0.5/node/".$osmnode."/ways";
        print "Downloading $url\n";
        $content=get($url) || &ErrorExit("Cannot retrieve $url\n");
        print "Parsing ways for $osmnode ... ";
        $TempXml= $xmlobj->XMLin($content);
        print "Done.\n";
        # save into main structure
        &OsmMerge($TempXml,$osm);
  
        #add to list of ways found
        push @WayGetList, keys %{$TempXml->{'way'}} ;
        #print "Waylist=@WayGetList\n";
  
      } # foreach intersection node
  
      # Get nodes in all the found cross-streets
      undef @nodes;
      foreach $way ( @WayGetList ) {
        foreach $nodehash ( @{$osm->{'way'}{$way}{'nd'}} ) {
            $crossnode=$$nodehash{'ref'};
            push @nodes, $crossnode ;
  	  #print "crossnode $crossnode\n";
        } # foreach cross-street nodehash
      } # foreach cross-street way
    
      #retrieve nodes in cross-streets
      &GetAndParseNodes( @nodes );
  
    } # if usebbox

    #----------------------
    # got data; now unpack into simpler cross-reference structures
    foreach $way ( keys %{ $osm->{'way'} } ) {
      &IndexWay($way);
    } #foreach way
  
  } # if not inputfile

} # sub GetAndParseCrossways

#----------------------------------------

sub GetAndParseNodes {
  my (
    @InputNodes,
  )=@_;
  my (
    @nodes,
    $ListSize,
    $nodelist,
    $url,
    $content,
    $TempNodeXml,
    $IndexStart,
    $IndexEnd,
  )=@_;

  @nodes=@InputNodes;

  $ListSize = scalar(@nodes);
  print "Starting to retrieve $ListSize nodes\n";
  if ( $ListSize > $DownloadNodeLimit ) {
    $ListSize= $DownloadNodeLimit ;
  } 
  $IndexStart=0;
  $IndexEnd=$ListSize - 1 ;

  while ( $ListSize > 0  ) {
      print "ListSize=$ListSize start=$IndexStart end=$IndexEnd\n";

      $nodelist=join(',',@nodes[ $IndexStart .. $IndexEnd ] ) ;

      $url=$OsmServer."/api/0.5/nodes?nodes=".$nodelist;
      #print "Downloading $url\n";
      $content=get($url) || &ErrorExit("Cannot retrieve $url\n");
      print "Parsing nodes ... ";
      $TempNodeXml= $xmlobj->XMLin($content);
      print "Done.\n";
      #print Dumper ($TempNodeXml);
      # save cross-street nodes
      &OsmMerge($TempNodeXml,$osm);
      #print Dumper ($TempNodeXml);

      $IndexStart += $ListSize;

      $ListSize = scalar(@nodes) - $IndexStart;
      #print "Remaining: $ListSize nodes\n";

      if ( $ListSize > $DownloadNodeLimit ) {
        $ListSize= $DownloadNodeLimit ;
      } 

      $IndexEnd = $IndexStart + $ListSize - 1 ;

  } # while listsize
  print "Done retrieving nodes\n";

} # sub GetAndParseNodes 

#----------------------------------------
sub IndexWay {
  my (
    $way,
  )=@_;
  my ( 
    $wayhashref,
    $nodelistref,
    $nodehash,
    $listlength,
    $osmnode,
    $i,
  );
    #print "way $way\n";

    #loop through nodes for this way, and mark them as used
  $nodelistref=$osm->{'way'}{$way}{'nd'};
  if ( ref  $nodelistref eq 'ARRAY' ) {
    $listlength= scalar(@$nodelistref);

    for ( $i = 0; $i < $listlength; $i++ ) {
      $nodehash=$$nodelistref[$i];
      $osmnode=$$nodehash{ref};
      $WayNodeList{$way}[$i]=$osmnode;
      $WayNodeIndex{$way}{$osmnode}=$i;
      $NodeWays{$osmnode}{$way}=1;
      #print "node $i $osmnode\n";
    } #foreach node
  } # if defined
} # sub IndexWay
#----------------------------------------
sub IndexNewWays {
  my (
    $way,
    $nodelistref,
    $listlength,
    $osmnode,
    $i,
  );
  #print "way $way\n";

  foreach $way ( $way1, $way2 ) {
    #loop through new nodes in way, and mark them as used
    $nodelistref=$newlist{$way};
    $listlength= scalar(@$nodelistref);

    for ( $i = 0; $i < $listlength; $i++ ) {
      $osmnode=$$nodelistref[$i];
      $NewWayNodeList{$way}[$i]=$osmnode;
      $NewWayNodeIndex{$way}{$osmnode}=$i;
    } #foreach node
  } # foreach way
} # sub IndexNewWays
#----------------------------------------
sub GetIntersections {
  # determine if these two ways have any nodes in common, and how many
  my $matches;	# return value : count of intersecting nodes
  my (
    $way,
    $osmnode,	# node number
    $nodelistref,
  ); 

  foreach $way ( $way1, $way2 ) {
    print "way $way\n";

    #loop through nodes for this way, and mark them as used
    $nodelistref=$WayNodeList{$way};
    #print "ref=$nodelistref\n";
    foreach $osmnode (  @$nodelistref ) {
      #print "node $osmnode\n";
      $intersectCount{$osmnode}++;	# record count of ways where node is used.
    } #foreach node
  } #foreach way

  $matches=0;
  # figure out how many nodes were used in both ways
  foreach $osmnode ( keys %intersectCount ) {
    if ( $intersectCount{$osmnode} >= 2 ) {
      $matches++;
    } else {
      delete $intersectCount{$osmnode};
    } # if
  } # foreach

  return $matches;

} ; # sub GetIntersections

#----------------------------------------
sub PrintIntersections {
  my (
    $node,
  );
  foreach $node ( keys %intersectCount ) {
    print "$node $WayNodeIndex{$way1}{$node} " .
	    "$WayNodeIndex{$way2}{$node}\n";
  } # foreach
} # sub PrintIntersections
#----------------------------------------
sub NodeAverage {
  my (
    $key,	# coord key "lat" or "lon"
    @nodelist,	# list of OSM node numbers
    )=@_;
  my $avg;	# return value
  my (
    $node,	# OSM node number
    $value,	# value of key for that node
    $total,	# sum of values
   );

  $total = 0;
  foreach $node ( @nodelist ) {
    $value = $osm->{'node'}{$node}{$key};
    #print "Node=$node key=$key value=$value\n";
    $total += $value;
  } # foreach node

  if ( scalar(@nodelist) > 0 ) {
    $avg = $total / scalar(@nodelist);
  } else {
    $avg=0;
  } # if list > 0

  return $avg;

} # sub NodeAverage
#----------------------------------------
sub SwapSides {
  # swap nodes to other way if they are on the wrong side according to geometry
  my $swapCount;	# return value : count of intersecting nodes
  my (
    $way,
    $osmnode,	# node number
    $wayhashref,
    $nodelistref,
    $nodehash,
    $i,
    $listlength,
    %CurrentWayIndex,
    $CurrentIntersectionIndex,
    @segment,
    %segments,
    $StartNode, $EndNode,
    %avglat, %avglon,	# average latitude/longitude for side segments
    $centerlat, $centerlon,
    $CenterBearing,
    $latside1,$lonside1,
    $latside2,$lonside2,
    $bearingside1, $bearingside2,
    $DeltaAngle1, $DeltaAngle2,
    $SidesFound,
  ); 

  # get ordered list of intersection nodes in first way
  $nodelistref=$WayNodeList{$way1};
  $listlength= scalar(@$nodelistref);
  for ( $i = 0; $i < $listlength; $i++ ) {
    $osmnode=$WayNodeList{$way1}[$i];
    if ( $intersectCount{$osmnode} && 
         $intersectCount{$osmnode} > 0 ) {
      push @IntersectList, $osmnode;
    } # if intersectCount > 0
  } # for way1

  $i= scalar(@IntersectList);
  if ( $i != $intersections ) {
    &ErrorExit("Intersectlist length $i didn't match $intersections\n");
  }

  #--------------------------------------
  # see if second way runs the opposite direction
  $StartNode=$IntersectList[0];
  $EndNode=$IntersectList[ $intersections - 1 ];
  if ( $WayNodeIndex{$way2}{$StartNode} >
      $WayNodeIndex{$way2}{$EndNode}) {
    # reverse second way to put in same order as first way
    $wayhashref=$osm->{'way'};
    $nodelistref=$wayhashref->{$way2}{'nd'};
    @$nodelistref = reverse ( @$nodelistref );
    &IndexWay($way2);
  } # if start > end

  #--------------------------------------
  # copy node numbers into new lists

  # start with any loose nodes preceding first intersection
  foreach $way ( $way1, $way2 ) {
    $CurrentWayIndex{$way}=0;
    $i=0;
    while ( $WayNodeList{$way}[$i] != $IntersectList[0] ) {
      push @{$newlist{$way}}, $WayNodeList{$way}[$i];
      $i++;
    } # while 
    $CurrentWayIndex{$way}=$i;
    if ( defined ( @{$newlist{$way}} ) ) {
      #print "way $way $i leading nodes @{$newlist{$way}} \n";
    } 

    # and first intersection
    #push @$nodelistref, $WayNodeList{$way}[$i];
  } # foreach way

  #print Dumper(%newlist);

  $swapCount=0;
  $CurrentIntersectionIndex=0;
  
  # loop through all intersecting segments
  while ( $CurrentIntersectionIndex < $intersections - 1 ) {
    # in one segment
    $StartNode=$IntersectList[$CurrentIntersectionIndex ];
    #print "index=$CurrentIntersectionIndex start=$StartNode\n";
    # loop through each side of the segment
    undef %segments;
    $SidesFound=0;
    foreach $way ( $way1, $way2 ) {
      undef @segment;
      $i=$CurrentWayIndex{$way } + 1;
      # collect nodes used on this side up until next intersection
      while ( $WayNodeList{$way}[$i] != 
		$IntersectList[$CurrentIntersectionIndex + 1 ] ) {
        push @segment, $WayNodeList{$way}[$i];
        $i++;
      } # while 
      $CurrentWayIndex{$way}=$i;

      #print "index=$CurrentIntersectionIndex way=$way seg=@segment\n";
      if ( scalar(@segment ) ) {
        # we found some side nodes
  
        @{$segments{$way}} = @segment;

        $avglat{$way}=&NodeAverage('lat',@segment);
        $avglon{$way}=&NodeAverage('lon',@segment);
	$SidesFound++;
      } # if segment
    } # foreach way

    $EndNode=$IntersectList[$CurrentIntersectionIndex + 1 ];
    #print "index=$CurrentIntersectionIndex end=$EndNode\n";
  
    #-----------------------------
    push @{$newlist{$way1}} , $StartNode;
    push @{$newlist{$way2}} , $StartNode;

    if ( $SidesFound == 2 ) {

      #$centerlat=&NodeAverage('lat',$StartNode,$EndNode);
      #$centerlon=&NodeAverage('lon',$StartNode,$EndNode);
  
      #-----------------------------
      # now figure angles
  
      $CenterBearing=&CalcNodeBearing($StartNode,$EndNode);
      print "segment $CurrentIntersectionIndex startnode=$StartNode bearing=$CenterBearing\n";
  
      $latside1=&NodeAverage('lat',@{$segments{$way1}});
      $lonside1=&NodeAverage('lon',@{$segments{$way1}});
  
      $latside2=&NodeAverage('lat',@{$segments{$way2}});
      $lonside2=&NodeAverage('lon',@{$segments{$way2}});
  
      $bearingside1=&CalcLatLonBearing($latside2,$lonside2,$latside1,$lonside1);
      print "bearing1=$bearingside1 \n";
      #$bearingside2=&CalcLatLonBearing($centerlat,$centerlon,$latside2,$lonside2);
      #print "bearing1=$bearingside1 bearing2=$bearingside2\n";
  
      $DeltaAngle1=&CalcAngleDelta($CenterBearing,$bearingside1);
      #$DeltaAngle2=&CalcAngleDelta($CenterBearing,$bearingside2);
      #print "angle1=$DeltaAngle1 angle2=$DeltaAngle2\n";
      print "delta angle=$DeltaAngle1\n";
  
      # the moment of truth
      if (  $DeltaAngle1 > 0 ) {
        print"same side\n";
        push @{$newlist{$way1}} , @{$segments{$way1}};
        push @{$newlist{$way2}} , @{$segments{$way2}};
      } else {
        print"swap side\n";
        push @{$newlist{$way1}} , @{$segments{$way2}};
        push @{$newlist{$way2}} , @{$segments{$way1}};
      } # if angle
    } else {
	print "Sides found: $SidesFound\n";
    } # if sides found

    #-----------------------------
    # done with this segment
    $CurrentIntersectionIndex++;
  } # while intersections

  # Do last intersection and trailing nodes 
  foreach $way ( $way1, $way2 ) {
    $i=$CurrentWayIndex{$way} ;

    $listlength= scalar(@{$WayNodeList{$way}});

    #print "i=$i len=$listlength\n";
    if ( $i < $listlength  ) {
      undef @segment;
      while ( $i < $listlength ) {
        push @segment, $WayNodeList{$way}[$i];
	$i++;
      } # while
      #print "way=$way trail=@segment\n";
      push @{$newlist{$way}}, @segment;
    } # if < listlength

  } # foreach way

  # redo indexes
  &IndexNewWays;

  return $swapCount;

} ; # sub SwapSides

#----------------------------------------
sub CalcNodeBearing {
  # Calculate bearing from one node to another
  my (
    $StartNode,
    $EndNode,
  )=@_;

  return &CalcLatLonBearing ( 
    $osm->{'node'}{$StartNode}{lat},
    $osm->{'node'}{$StartNode}{lon},
    $osm->{'node'}{$EndNode}{lat} ,
    $osm->{'node'}{$EndNode}{lon} 
   );

} # sub CalcNodeBearing
#----------------------------------------
sub CalcAngleDelta {
  # Calculate angle difference between two bearings
  my (
    $Bearing1,
    $Bearing2,
  )=@_;
  my (
    $Diff,
  ) ;

  $Diff=$Bearing2 - $Bearing1;

  #normalize angle between -180 and +180
  while ( $Diff > 180 ) {
    $Diff -= 360 ;
  };
  while ( $Diff < -180 ) {
    $Diff += 360 ;
  };

  return $Diff;

} # sub CalcAngleDelta
#----------------------------------------
sub CalcLatLonBearing {
  # Calculate bearing from one lat/lon to another
  my (
    $StartLat,
    $StartLon,
    $EndLat,
    $EndLon,
  )=@_;
  my $Bearing;
  my (
    $deltaX,
    $deltaY,
    $distanceX,
    $distanceY,
    $distance,
    $z,
    $AngleRadians,
  );
  my $Equator=40000; # meters

  $deltaX = $EndLon - $StartLon;
  $deltaY = $EndLat - $StartLat;

  $distanceY=$deltaY * $Equator ;
  $distanceX=$deltaX * cos ( deg2rad ( $deltaX ) ) * $Equator ;
  #print "distanceX=$distanceX distanceY=$distanceY\n";

  ($distance, $AngleRadians,$z) =
	  cartesian_to_cylindrical($distanceX,$distanceY,0);
  $Bearing = 90 - rad2deg($AngleRadians);

  #print "rad=$AngleRadians deg=$Bearing\n";

  return $Bearing;

} # sub CalcLatLonBearing

#----------------------------------------
sub SaveNewData {
  # Replace existing way node list with new node lists

  my (
    $way,
    $node,
    $newxml,
  );

  foreach $way ( keys %newlist ) {
    # delete existing node list
    $osm->{'way'}{$way}{'action'}='modify';
    undef $osm->{'way'}{$way}{'nd'};
    # insert new node list
    foreach $node ( @{$newlist{$way}} ) {
      #print "way $way node $node\n";
      push @{$osm->{'way'}{$way}{'nd'}} , {'ref'=>$node};
    } # foreach node
  } # foreach way

  # move changed nodes
  foreach $node ( keys %NodeChanges ) {
    $osm->{'node'}{$node}{'action'}='modify';
    $osm->{'node'}{$node}{'lat'}=$NodeChanges{$node}{'lat'};
    $osm->{'node'}{$node}{'lon'}=$NodeChanges{$node}{'lon'};
  } # foreach node

  # add new nodes
  foreach $node ( keys %NewNodes ) {
    $osm->{'node'}{$node}=$NewNodes{$node};
  } # foreach node


  if ( $OnewayFlag ) {
    print "Making sides oneway\n";
    &MakeOneway();
  } else {
    print "Skip making sides oneway\n";
  } # if oneway

  $osm->{'generator'}='osmunbraid.pl';

  print "Saving to file $NewXmlFile\n";

  $xmlobj->XMLout($osm,OutputFile=>$NewXmlFile);

} # sub SaveNewData

#----------------------------------------
sub StraightenIntersections {
  #Take dual-way intersections with single node, and change to two nodes.  
  # - Straighten out existing node in one way.
  # - Add a new node for other way.
  my (
    $inum,	# intersection number
    $node,	# osm node number at intersection
    $newnode,	# osm node number at intersection
    $wayindex,	# position of node within way
    $lat, $lon,
    $PrevNode, $NextNode,
    $CrossWay,
    $CrossFlag,	
    $CenterBearing,
    $SideNode,
    $SideBearing,
    $DeltaAngle,
    $SplicePoint,	
    $offset,
  ); 

  #  Exclude first and last intersections 
  #     (Note: Perl array starts at 0, so 1 is second entry )
  for ($inum = 1; $inum < scalar(@IntersectList) - 1 ; $inum++ ) {
    #-----------------------------
    # Move intersection node from center into path of first way.
    $node=$IntersectList[$inum ];
    print "Intersection=$inum node=$node\n";

    $wayindex= $NewWayNodeIndex{$way1}{$node};

    $PrevNode=$NewWayNodeList{$way1}[$wayindex - 1 ];
    $NextNode=$NewWayNodeList{$way1}[$wayindex + 1 ];
    print " prev=$PrevNode next=$NextNode\n";

    $lat=&NodeAverage('lat',$PrevNode,$NextNode);
    $lon=&NodeAverage('lon',$PrevNode,$NextNode);
    $NodeChanges{$node}{'lat'}=$lat;
    $NodeChanges{$node}{'lon'}=$lon;

    #-----------------------------
    # Make another node in path of second way.
    $newnode=$inum * -1 ;
    #print "index=$inum node=$node\n";

    # find position of node in way2
    $wayindex= $NewWayNodeIndex{$way2}{$node};

    $PrevNode=$NewWayNodeList{$way2}[$wayindex - 1 ];
    $NextNode=$NewWayNodeList{$way2}[$wayindex + 1 ];
    print " prev=$PrevNode next=$NextNode\n";

    $lat=&NodeAverage('lat',$PrevNode,$NextNode);
    $lon=&NodeAverage('lon',$PrevNode,$NextNode);
    $NewNodes{$newnode}{'visible'}='true';
    $NewNodes{$newnode}{'lat'}=$lat;
    $NewNodes{$newnode}{'lon'}=$lon;

    # replace with new node in way2
    $newlist{$way2}[$wayindex]=$newnode;
    #-----------------------------

    # insert new node into cross-street which uses
    #    this intersection node, if any.  
    $CrossFlag=0;
    foreach $CrossWay ( keys %{$NodeWays{$node}} ) {
      if ( $CrossWay != $way1 && $CrossWay != $way2 && ! $CrossFlag ) {
        # find position of node in cross-street
	$wayindex=$WayNodeIndex{$CrossWay}{$node};

        # copy list of nodes into new list
	@{$newlist{$CrossWay}}=@{$WayNodeList{$CrossWay}};

        # decide if new node should go before or after existing node
        #    based on bearing of adjacent node in cross street

        $CenterBearing=&CalcNodeBearing($PrevNode,$NextNode);

	if ( $wayindex == 0 ) {
		$offset=1;
	} else {
		$offset=-1;
	} # if wayindex == 0

	$SideNode=$WayNodeList{$CrossWay}[ $wayindex + $offset ] ;

        $SideBearing=&CalcNodeBearing($node,$SideNode);
  
        $DeltaAngle=&CalcAngleDelta($CenterBearing,$SideBearing);

	print "crossway=$CrossWay index=$wayindex delta=$DeltaAngle sidenode=$SideNode\n";

	if ( ( $offset == 1  && $DeltaAngle > 0 ) ||
	     ( $offset == -1 && $DeltaAngle < 0 ) )  {
	    # insert new node before existing node in cross-street
            $SplicePoint = $wayindex ;
	} else {
	    # insert new node after existing node in cross-street
            $SplicePoint = $wayindex + 1 ;
	} # if angle

	splice @{$newlist{$CrossWay}}, $SplicePoint, 0, $newnode;

        #$CrossFlag=1;

      } # if not way1 or way2
    } # foreach way


  } # for inum

} # sub StraightenIntersections

#----------------------------------------
sub MakeOneway {
  my (
    $nodelistref,
    $way,
  );
  #Reverse the left-side way, and flag both sides as oneway

  #Reverse the left-side way
  $nodelistref= $osm->{'way'}{$way2}{'nd'};
  @$nodelistref = reverse ( @$nodelistref );

  #flag both sides as oneway
  foreach $way ( $way1, $way2) {
    push @{$osm->{'way'}{$way}{'tag'}} , 
       { 'k' => 'oneway',
         'v' => 'yes' };
  } # foreach way

} # sub MakeOneway
#----------------------------------------

# osm copy - copy contents of osm data from one hash to another
sub OsmMerge {
  my (
    $inputref,
    $outputref,
  )=@_;
  my (
    $key,
    $itemnum,
  );

  foreach $key ( keys %{$inputref} ) {
    #print "Copy key $key $$inputref{$key}\n";
    if ( ref $$inputref{$key} ) {
      foreach $itemnum ( keys %{$$inputref{$key}} ) {

	#print "Deep copy $key $itemnum\n";
        $$outputref{$key}{$itemnum}=deep_copy($$inputref{$key}{$itemnum});
      } # foreach itemnum
    } else {
	#print "Simple copy $key $$inputref{$key}\n";;
      $$outputref{$key}=$$inputref{$key};
    } # if ref
  } # foreach key
} # sub OsmMerge

# Deep_copy from Randall Schwartz's Unix Review magazine article
sub deep_copy {
  my $this = shift;
  if (not ref $this) {
    $this;
  } elsif (ref $this eq "ARRAY") {
    [map deep_copy($_), @$this];
  } elsif (ref $this eq "HASH") {
    +{map { $_ => deep_copy($this->{$_}) } keys %$this};
  } else {
    die "what type is $_?" }
} # sub deep_copy

#----------------------------------------
sub ErrorExit {
  my (
    $ErrorMsg,
  )=@_;

  print "FATAL ERROR: $ErrorMsg\n" .
	"Saving data to $ErrorFile\n";

  $osm->{'generator'}='osmunbraid.pl';

  $xmlobj->XMLout($osm,OutputFile=>$ErrorFile);

  die "Exiting\n";

} # sub ErrorExit

#----------------------------------------

__END__

=head1 NAME

osmunbraid - unravel "braided" streets in OpenStreetMap

=head1 SYNOPSIS

  osmunbraid.pl [--nostraighten] [--nooneway] [--nobbox]
    [--inputfile filename] [--outputfile filename] [--errorfile filename]
    waynumber1 waynumber2

Run this and specify two way numbers.  It will produce a change file which
can be opened in JOSM and uploaded to the OSM server.

=head1 DESCRIPTION

This is an OpenStreetMap utilty to fix "braided" streets.  These are 
streets with two ways (typically a two-way street with an island or divider
down the middle) where the sections switch across each other.  This
utility will fix them so that each side is in a single way.

These streets are an artifact of the import process which loaded US Census
Bureau TIGER street data into OSM.

The script processes two primary ways, and the nodes that they use.  It
also can adjust other ways which intersect and use the same nodes.

Options:

  way numbers  	OSM way numbers.  Required.

  inputfile     Optional.  The name of an OSM XML file to process.  
                Default action is for this script to download the correct
		data automatically for you.  Recommendation: only use this 
		option for testing; it is unneeded for real use.  

  outputfile    Optional.  The name of the file to be created. Default
  		is to create a file name for you based on the way numbers.

  errorfile	Optional.  The name of the file to save intermediate
  		working OSM data in case of an error.  Default is to 
  		create a file name for you based on the way numbers.

  nostraighten  Optional.  Specify nostraighten to skip the automatic 
                straightening of the intersections.  Default is
		to straighten them.

  nooneway      Optional.  Don't change the sides to oneway=yes

  nobbox        Optional.  Don't use a bounding box to download the
                surrounding area; only download the directly-connected ways.

How To Use This Script:

Find two braided ways in OpenStreetMap, and get their way numbers.

Run this script, specifying the two way numbers.

Open the output file in JOSM, and inspect for accuracy, direction,
and minor adjustments.  When it is correct, upload to OpenStreetMap.

Programming Notes:

US TIGER data:  It is assumed that this will be used to fix streets
uploaded to OSM from the US Census Bureau TIGER data.  Therefore,
by default the directions are set to driving forward on the right.
Other countries drive differently, but won't have TIGER data.  You
can supress the automatic oneway tagging with the --nooneway option.

Angular Geometry: Calculations are done in bearings based on a
360 degree circle.  Absolute bearings range from -180 to 360 degrees, 
where 0 is north, 90 is east, +/- 180 is south, and -90 or 270 is west.  
Relative bearings range from -180 to +180, where 0 is straight ahead, 
+90 is on the right, -90 is on the left, and +/-180 is directly behind.

Tested in Linux with Perl 5.8, using OpenStreetMap API 0.5.

=head1 SEE ALSO

For a list of braided streets that need correction, see
 http://wiki.openstreetmap.org/index.php/Tiger_fixup

=head1 AUTHOR

 Alan Millar

=cut
