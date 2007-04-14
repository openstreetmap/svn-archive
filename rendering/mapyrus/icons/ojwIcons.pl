#!/usr/bin/perl
use strict;
our ($X, $Y, $W,$H, $DX,$DY) = (5,95, 100, 100,25,-8);
open(OUT, ">osm.def");
print OUT "newpage 'pdf','output.pdf', $W,$H,resolution=500\n";
print OUT "box 0,0,$W,$H\ncolor 'lightgreen'\nfill\n" if(0); # optional background colour
my $Usage;
foreach my $Line(<DATA>){
  print OUT $Line;
  if($Line =~ /#\s*usage:\s*(.*?)$/){
    my $MapyrusCommand = $1;
    
    my $Label;
    $Label = $1 if($MapyrusCommand =~ /^(\w+)/);
  
    $Usage .= "  clearpath\n  move $X,$Y\n  $MapyrusCommand\n";
    $Usage .= sprintf "  clearpath\n  move %f,%f\n  color 'black'\n  font Arial,2\n  label '%s'\n",$X+3,$Y,$Label;
    NextLocation();
  }
}
print OUT "#" . "-" x 80 . "\n# Test functions\n";
print OUT "begin ojwTestSuite\n";
print OUT $Usage;
print OUT "end\n\n#Comment this line out to use as a library\nojwTestSuite\n";
close;

my $Java = "/usr/java/jre1.5.0_06/bin/java"; # Location of java 5
`$Java -classpath ../mapyrus.jar org.mapyrus.Mapyrus osm.def`;

sub NextLocation(){
	our ($X, $Y, $W, $H);
	$Y += $DY;
	if($Y <= 0){
		$Y = $H + $DY;
		$X+= $DX;
	}
}
__DATA__

# usage: ojwChurch 0,0
# usage: ojwChurch 1,0
# usage: ojwChurch 0,1
begin ojwChurch tower,spire
  clearpath
  move -1,0
  draw 1,0
  move 0,-1 
  draw 0,1
  linestyle 0.2
  color "black"
  stroke
        
  if spire then
    clearpath
    circle 0,-2,1
    fill
  endif
  if tower then
    clearpath
    box -1,-3,1,-1
    fill
  endif
end

# usage: ojwRailStation 0,0
# usage: ojwRailStation 1,0
# usage: ojwRailStation 0,1
begin ojwRailStation mainline, disused
  clearpath
  if mainline then
    box -1.25, -1.25, 1.25, 1.25
  else
    circle 0,0,1.2
  endif
  
  if disused then
    color "#bbbbbb"
  else
    color "red"
  endif
  fill
  
  linestyle 0.4
  color "black"
  stroke
end

# usage: ojwServices
begin ojwServices
  clearpath
  circle 0,0,1.6
  color "#0000AA"
  fill
  
  clearpath
  font Arial,3
  move -0.95,-1.1
  color 'white'
  label 'S'
end

# usage: ojwTower
begin ojwTower
  clearpath
  move 0,1 
  draw -0.6,-1, 0.6,-1, 0,1
  color "lightgrey"
  fill
  linestyle 0.1
  color "black"
  stroke
end

# usage: ojwLighthouse
begin ojwLighthouse
  ojwTower
  
  clearpath
  move -0.3,1
  draw -1.6,1.5, -1.6,0.5
  color "#FFFF00"
  fill
  
  move 0.3,1
  draw 1.6,1.5, 1.6,0.5
  color "#FFFF00"
  fill
end

# usage: ojwTrigpoint
begin ojwTrigpoint
  clearpath
  triangle 0,0,1.2,0
  linestyle 0.1
  color "black"
  stroke
  
  clearpath
  circle 0,0,0.15
  fill
end

# usage: ojwParking 0 
# usage: ojwParking 1
begin ojwParking private
  if private then
    color "grey"
  else
    color "blue"
  endif
  clearpath
  box -1,-1,1,1
  fill
  
  clearpath
  move -0.6,-0.8
  font Arial,2.3
  color "white"
  label "P"
end

# usage: ojwTeashop
begin ojwTeashop
  clearpath
  box -1,-0.8, 0.2,0.4
  color "#dd9966"
  fill
  
  clearpath
  move -1,0.7
  draw -1,-0.8, 0.2,-0.8, 0.2,0.7
  linestyle 0.15
  color "black"
  stroke
    
  clearpath
  move 0.2,0.5
  draw 1,0.5, 1,-0.7, 0.2,-0.7
  linestyle 0.1
  color "black"
  stroke  
end

# usage: ojwMuseum
begin ojwMuseum
  color "#444444"
  
  clearpath  
  move -1,0.7
  draw 0,1, 1,0.7
  fill
  
  clearpath
  move -1,-1
  draw 1,-1
  move -0.7,-0.8
  draw -0.7,0.5
  
  move 0,-0.8
  draw 0,0.5
  
  move 0.7,-0.8
  draw 0.7,0.5
  
  linestyle 0.2
  stroke  
end

# usage: ojwPub
begin ojwPub
  clearpath
  move -0.85,1
  draw -0.5,-1, 0.5,-1, 0.85,1
  color "#FFFF66"
  fill
  
  color "#664400"
  linestyle 0.15
  stroke
end

# usage: ojwHospital
begin ojwHospital
  box -1.6,-1.6,1.6,1.6
  color "red"
  fill
  
  clearpath
  move 0,-1.1
  draw 0,1.1
  move -1.1,0
  draw 1.1,0
  linestyle 0.4
  color "white"
  stroke
end

# usage: ojwEnglishHeritage
begin ojwEnglishHeritage
  clearpath
  repeat 4 do
    rotate 90
    move -1,0.6
    draw 1,0.6
  done    
  linestyle 0.3
  color "red"
  stroke
end

# usage: ojwPoi
begin ojwPoi 
  repeat 4 do
    rotate 90
    clearpath
    move -0.6,0.6
    draw 0.6,0.6
    circle 1.0, 1.0, 0.5
    linestyle 0.15
    color 'brown'
    stroke
  done
end

# usage: ojwAirport
begin ojwAirport
  clearpath
  circle 0,0,2
  color "grey"
  linestyle 0.3,"round","round",0.9,0.4
  stroke
  color "white"
  fill
  
  color "black"
  clearpath
  move 0,-0.6
  draw 0,0.75
  linestyle 0.5
  stroke
  
  clearpath
  move 0,0.7
  draw -2,0
  draw 0,0.3
  draw 2,0
  fill
  
  clearpath
  move 0,-0.6
  draw -0.8,-1
  draw 0,-0.8
  draw 0.8,-1
  fill
  
  clearpath
  triangle 0, 1.25, 0.25, 0
  fill
  
  
end

